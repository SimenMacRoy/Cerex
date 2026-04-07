package com.cerex.service;

import com.cerex.domain.*;
import com.cerex.dto.order.IngredientSourcingDTO;
import com.cerex.dto.order.IngredientSourcingDTO.*;
import com.cerex.dto.order.IngredientSourcingRequest;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.repository.GroceryProductRepository;
import com.cerex.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Smart ingredient sourcing service.
 * <p>
 * Algorithm:
 * 1. Fetch recipe ingredients scaled to requested servings.
 * 2. Query grocery products near the user that match required ingredient IDs.
 * 3. Group available products by grocery store.
 * 4. Pick the primary store = the one covering the most ingredients (ties broken by distance).
 * 5. For uncovered ingredients, pick the next nearest store that has them.
 * 6. Remaining unfound ingredients go into the "unavailable" list with fallback FCFA prices.
 * 7. Return a multi-store sourcing plan with per-item pricing and bulk-only warnings.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class IngredientSourcingService {

    private final RecipeRepository recipeRepository;
    private final GroceryProductRepository groceryProductRepository;

    // ── Currency detection by rough geolocation ──────────────
    private static final Map<String, String> CURRENCY_RULES = Map.ofEntries(
        Map.entry("XAF", "XAF"),   // Central Africa (CEMAC)
        Map.entry("XOF", "XOF"),   // West Africa (UEMOA)
        Map.entry("EUR", "EUR"),
        Map.entry("GHS", "GHS"),
        Map.entry("NGN", "NGN"),
        Map.entry("USD", "USD"),
        Map.entry("GBP", "GBP")
    );

    /**
     * Build a sourcing quote for a recipe at a given location.
     */
    public IngredientSourcingDTO buildQuote(IngredientSourcingRequest request) {
        log.info("Building sourcing quote for recipe={} servings={} at ({},{})",
            request.getRecipeId(), request.getServings(),
            request.getLatitude(), request.getLongitude());

        // 1. Load recipe with ingredients
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
            .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", request.getRecipeId()));

        int baseServings = recipe.getServings() != null ? recipe.getServings() : 1;
        double ratio = (double) request.getServings() / baseServings;

        // 2. Collect needed ingredient IDs and scaled quantities
        List<RecipeIngredient> recipeIngredients = recipe.getIngredients();
        List<UUID> neededIngredientIds = recipeIngredients.stream()
            .map(RecipeIngredient::getIngredientId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (neededIngredientIds.isEmpty()) {
            return IngredientSourcingDTO.builder()
                .recipeId(recipe.getId())
                .recipeTitle(recipe.getTitle())
                .requestedServings(request.getServings())
                .storePlans(List.of())
                .unavailableItems(List.of())
                .grandTotal(BigDecimal.ZERO)
                .currency(detectCurrency(request))
                .hasBulkItems(false)
                .userLatitude(request.getLatitude())
                .userLongitude(request.getLongitude())
                .build();
        }

        // 3. Find matching products from nearby stores
        List<GroceryProduct> availableProducts = groceryProductRepository
            .findNearbyByIngredientIds(
                neededIngredientIds,
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusKm()
            );

        log.debug("Found {} products from nearby stores for {} needed ingredients",
            availableProducts.size(), neededIngredientIds.size());

        // 4. Group products by grocery store
        Map<UUID, List<GroceryProduct>> productsByStore = availableProducts.stream()
            .collect(Collectors.groupingBy(p -> p.getGrocery().getId()));

        // 5. Build store metadata with distances
        Map<UUID, Grocery> storeMap = availableProducts.stream()
            .map(GroceryProduct::getGrocery)
            .collect(Collectors.toMap(Grocery::getId, g -> g, (a, b) -> a));

        Map<UUID, Double> storeDistances = new HashMap<>();
        for (var entry : storeMap.entrySet()) {
            Grocery g = entry.getValue();
            storeDistances.put(entry.getKey(),
                haversineKm(request.getLatitude(), request.getLongitude(), g.getLatitude(), g.getLongitude()));
        }

        // 6. Greedy set-cover: pick stores to maximize ingredient coverage
        String userCurrency = detectCurrency(request);
        Set<UUID> coveredIngredients = new HashSet<>();
        List<StorePlan> storePlans = new ArrayList<>();
        boolean hasBulk = false;

        // Sort stores by: coverage count DESC, then distance ASC
        List<UUID> sortedStoreIds = productsByStore.keySet().stream()
            .sorted(Comparator
                .<UUID>comparingInt(sid -> {
                    Set<UUID> storeIngIds = productsByStore.get(sid).stream()
                        .map(GroceryProduct::getIngredientId)
                        .collect(Collectors.toSet());
                    storeIngIds.removeAll(coveredIngredients);
                    return storeIngIds.size();
                }).reversed()
                .thenComparingDouble(sid -> storeDistances.getOrDefault(sid, 999.0)))
            .toList();

        for (UUID storeId : sortedStoreIds) {
            List<GroceryProduct> storeProducts = productsByStore.get(storeId);
            Grocery grocery = storeMap.get(storeId);

            // For each uncovered ingredient, pick the cheapest product from this store
            List<SourcingItem> items = new ArrayList<>();
            BigDecimal storeTotal = BigDecimal.ZERO;

            // Group products by ingredient
            Map<UUID, List<GroceryProduct>> byIngredient = storeProducts.stream()
                .filter(p -> p.getIngredientId() != null)
                .collect(Collectors.groupingBy(GroceryProduct::getIngredientId));

            for (var ingEntry : byIngredient.entrySet()) {
                UUID ingredientId = ingEntry.getKey();
                if (coveredIngredients.contains(ingredientId)) continue;

                // Find the cheapest product for this ingredient
                GroceryProduct cheapest = ingEntry.getValue().stream()
                    .min(Comparator.comparing(GroceryProduct::getPrice))
                    .orElse(null);
                if (cheapest == null) continue;

                // Find the matching recipe ingredient for context
                RecipeIngredient ri = recipeIngredients.stream()
                    .filter(r -> ingredientId.equals(r.getIngredientId()))
                    .findFirst().orElse(null);
                if (ri == null) continue;

                BigDecimal scaledQty = ri.getQuantity() != null
                    ? ri.getQuantity().multiply(BigDecimal.valueOf(ratio)).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ONE;

                BigDecimal lineTotal = cheapest.getPrice().multiply(scaledQty)
                    .setScale(2, RoundingMode.HALF_UP);

                if (Boolean.TRUE.equals(cheapest.getIsBulkOnly())) {
                    hasBulk = true;
                }

                items.add(SourcingItem.builder()
                    .productId(cheapest.getId())
                    .ingredientId(ingredientId)
                    .ingredientName(cheapest.getName())
                    .productName(cheapest.getName())
                    .productDescription(cheapest.getDescription())
                    .recipeQuantity(scaledQty)
                    .recipeUnit(ri.getUnit())
                    .displayText(ri.getDisplayText())
                    .isOptional(Boolean.TRUE.equals(ri.getIsOptional()))
                    .price(cheapest.getPrice())
                    .pricePerUnit(cheapest.getPricePerUnit())
                    .productUnit(cheapest.getUnit())
                    .currency(cheapest.getCurrency() != null ? cheapest.getCurrency() : userCurrency)
                    .lineTotal(lineTotal)
                    .bulkOnly(Boolean.TRUE.equals(cheapest.getIsBulkOnly()))
                    .minimumQuantity(cheapest.getMinimumQuantity())
                    .organic(Boolean.TRUE.equals(cheapest.getIsOrganic()))
                    .local(Boolean.TRUE.equals(cheapest.getIsLocal()))
                    .fairTrade(Boolean.TRUE.equals(cheapest.getIsFairTrade()))
                    .ecoScore(cheapest.getEcoScore())
                    .build());

                storeTotal = storeTotal.add(lineTotal);
                coveredIngredients.add(ingredientId);
            }

            if (!items.isEmpty()) {
                storePlans.add(StorePlan.builder()
                    .groceryId(grocery.getId())
                    .groceryName(grocery.getName())
                    .grocerySlug(grocery.getSlug())
                    .city(grocery.getCity())
                    .addressLine1(grocery.getAddressLine1())
                    .latitude(grocery.getLatitude())
                    .longitude(grocery.getLongitude())
                    .distanceKm(Math.round(storeDistances.getOrDefault(storeId, 0.0) * 10.0) / 10.0)
                    .averageRating(grocery.getAverageRating())
                    .supportsDelivery(Boolean.TRUE.equals(grocery.getSupportsDelivery()))
                    .supportsPickup(Boolean.TRUE.equals(grocery.getSupportsPickup()))
                    .minimumOrderAmount(grocery.getMinimumOrderAmount())
                    .items(items)
                    .storeTotal(storeTotal)
                    .currency(items.get(0).getCurrency())
                    .coverageCount(items.size())
                    .totalNeeded(neededIngredientIds.size())
                    .build());
            }
        }

        // 7. Unavailable items (ingredients not covered by any store)
        List<UnavailableItem> unavailable = new ArrayList<>();
        for (RecipeIngredient ri : recipeIngredients) {
            if (ri.getIngredientId() == null) continue;
            if (coveredIngredients.contains(ri.getIngredientId())) continue;

            BigDecimal scaledQty = ri.getQuantity() != null
                ? ri.getQuantity().multiply(BigDecimal.valueOf(ratio)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ONE;

            Ingredient ing = ri.getIngredient();
            unavailable.add(UnavailableItem.builder()
                .ingredientId(ri.getIngredientId())
                .ingredientName(ing != null ? ing.getName() : ri.getDisplayText())
                .recipeQuantity(scaledQty)
                .recipeUnit(ri.getUnit())
                .displayText(ri.getDisplayText())
                .isOptional(Boolean.TRUE.equals(ri.getIsOptional()))
                .estimatedPriceFcfa(ing != null ? ing.getEstimatedPriceFcfa() : BigDecimal.valueOf(300))
                .build());
        }

        // 8. Grand total
        BigDecimal grandTotal = storePlans.stream()
            .map(StorePlan::getStoreTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return IngredientSourcingDTO.builder()
            .recipeId(recipe.getId())
            .recipeTitle(recipe.getTitle())
            .requestedServings(request.getServings())
            .storePlans(storePlans)
            .unavailableItems(unavailable)
            .grandTotal(grandTotal)
            .currency(userCurrency)
            .hasBulkItems(hasBulk)
            .userLatitude(request.getLatitude())
            .userLongitude(request.getLongitude())
            .build();
    }

    // ── Helpers ──────────────────────────────────────────────

    /**
     * Detect the user's currency from their geolocation.
     * Rough bounding boxes for African + European regions.
     */
    private String detectCurrency(IngredientSourcingRequest req) {
        if (req.getCurrency() != null && !req.getCurrency().isBlank()) {
            return req.getCurrency().toUpperCase();
        }

        double lat = req.getLatitude().doubleValue();
        double lng = req.getLongitude().doubleValue();

        // CEMAC region (Cameroon, Chad, CAR, Congo, Gabon, Eq. Guinea)
        if (lat >= -5 && lat <= 15 && lng >= 8 && lng <= 25) return "XAF";
        // UEMOA region (Senegal, Mali, Burkina, Ivory Coast, etc.)
        if (lat >= 0 && lat <= 18 && lng >= -18 && lng <= 5) return "XOF";
        // Ghana
        if (lat >= 4 && lat <= 12 && lng >= -4 && lng <= 2) return "GHS";
        // Nigeria
        if (lat >= 4 && lat <= 14 && lng >= 2 && lng <= 15) return "NGN";
        // Europe (rough)
        if (lat >= 35 && lat <= 72 && lng >= -10 && lng <= 40) return "EUR";
        // UK
        if (lat >= 49 && lat <= 61 && lng >= -8 && lng <= 2) return "GBP";
        // North America
        if (lat >= 15 && lat <= 72 && lng >= -170 && lng <= -50) return "USD";

        return "XAF"; // default
    }

    /**
     * Haversine distance in kilometers.
     */
    private double haversineKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 999.0;

        double R = 6371.0;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1.doubleValue()))
            * Math.cos(Math.toRadians(lat2.doubleValue()))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

package com.cerex.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for the ingredient sourcing quote.
 * Contains the optimal split of ingredients across nearby grocery stores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientSourcingDTO {

    private UUID recipeId;
    private String recipeTitle;
    private int requestedServings;

    /** Ordered list of store plans (primary store first, then secondary, etc.). */
    private List<StorePlan> storePlans;

    /** Ingredients that could not be found at any nearby store. */
    private List<UnavailableItem> unavailableItems;

    /** Grand total across all stores in the user's detected currency. */
    private BigDecimal grandTotal;
    private String currency;

    /** Whether any bulk-only products are included. */
    private boolean hasBulkItems;

    /** User location used for sourcing. */
    private BigDecimal userLatitude;
    private BigDecimal userLongitude;

    // ─────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorePlan {
        private UUID groceryId;
        private String groceryName;
        private String grocerySlug;
        private String city;
        private String addressLine1;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private double distanceKm;
        private BigDecimal averageRating;

        /** Store capabilities */
        private boolean supportsDelivery;
        private boolean supportsPickup;
        private BigDecimal minimumOrderAmount;

        /** Items sourced from this store. */
        private List<SourcingItem> items;

        /** Total cost for items from this store. */
        private BigDecimal storeTotal;
        private String currency;

        /** How many of the recipe's needed ingredients this store covers. */
        private int coverageCount;
        private int totalNeeded;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourcingItem {
        private UUID productId;
        private UUID ingredientId;
        private String ingredientName;
        private String productName;
        private String productDescription;

        /** Recipe quantity scaled to requested servings. */
        private BigDecimal recipeQuantity;
        private String recipeUnit;
        private String displayText;
        private boolean isOptional;

        /** Product pricing. */
        private BigDecimal price;
        private BigDecimal pricePerUnit;
        private String productUnit;
        private String currency;

        /** Computed line total. */
        private BigDecimal lineTotal;

        /** Bulk-only info. */
        private boolean bulkOnly;
        private Integer minimumQuantity;

        /** Quality tags. */
        private boolean organic;
        private boolean local;
        private boolean fairTrade;
        private Integer ecoScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnavailableItem {
        private UUID ingredientId;
        private String ingredientName;
        private BigDecimal recipeQuantity;
        private String recipeUnit;
        private String displayText;
        private boolean isOptional;

        /** Fallback estimated price from the ingredient master catalog. */
        private BigDecimal estimatedPriceFcfa;
    }
}

package com.cerex.service;

import com.cerex.domain.*;
import com.cerex.domain.Restaurant.RestaurantStatus;
import com.cerex.dto.restaurant.*;
import com.cerex.exception.BusinessException;
import com.cerex.exception.DuplicateResourceException;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.exception.UnauthorizedException;
import com.cerex.repository.MenuRepository;
import com.cerex.repository.RestaurantRepository;
import com.cerex.repository.RestaurantReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service layer for restaurant lifecycle management.
 *
 * <p>Handles CRUD, verification, menu management, reviews, and geo-search.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final RestaurantReviewRepository reviewRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    // ─────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantDTO createRestaurant(UUID ownerId, CreateRestaurantRequest request) {
        String slug = generateSlug(request.getName());
        if (restaurantRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        Map<String, Restaurant.OperatingHoursSlot> hours = null;
        if (request.getOperatingHours() != null) {
            hours = new HashMap<>();
            for (var entry : request.getOperatingHours().entrySet()) {
                hours.put(entry.getKey(), new Restaurant.OperatingHoursSlot(
                    entry.getValue().getOpenTime(),
                    entry.getValue().getCloseTime(),
                    entry.getValue().isClosed()
                ));
            }
        }

        Restaurant restaurant = Restaurant.builder()
            .ownerId(ownerId)
            .name(request.getName())
            .description(request.getDescription())
            .slug(slug)
            .cuisineType(request.getCuisineType())
            .cuisineTags(request.getCuisineTags() != null ? request.getCuisineTags() : new HashSet<>())
            .phone(request.getPhone())
            .email(request.getEmail())
            .website(request.getWebsite())
            .addressLine1(request.getAddressLine1())
            .addressLine2(request.getAddressLine2())
            .city(request.getCity())
            .stateProvince(request.getStateProvince())
            .postalCode(request.getPostalCode())
            .countryId(request.getCountryId())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .deliveryRadiusKm(request.getDeliveryRadiusKm() != null ? request.getDeliveryRadiusKm() : 10.0)
            .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
            .logoUrl(request.getLogoUrl())
            .coverImageUrl(request.getCoverImageUrl())
            .operatingHours(hours)
            .minimumOrderAmount(request.getMinimumOrderAmount() != null ? request.getMinimumOrderAmount() : BigDecimal.ZERO)
            .averagePreparationTimeMin(request.getAveragePreparationTimeMin() != null ? request.getAveragePreparationTimeMin() : 30)
            .supportsTakeaway(request.getSupportsTakeaway() != null ? request.getSupportsTakeaway() : true)
            .supportsDelivery(request.getSupportsDelivery() != null ? request.getSupportsDelivery() : true)
            .supportsDineIn(request.getSupportsDineIn() != null ? request.getSupportsDineIn() : true)
            .acceptsReservations(request.getAcceptsReservations() != null ? request.getAcceptsReservations() : false)
            .offersCatering(request.getOffersCatering() != null ? request.getOffersCatering() : false)
            .build();

        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant created: {} [{}]", restaurant.getName(), restaurant.getId());

        kafkaTemplate.send("cerex.restaurant.created", restaurant.getId().toString(),
            Map.of("restaurantId", restaurant.getId(), "name", restaurant.getName(), "ownerId", ownerId));

        return toDTO(restaurant);
    }

    // ─────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────

    public RestaurantDTO getRestaurantBySlug(String slug) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "slug", slug));
        return toDTO(restaurant);
    }

    public RestaurantDTO getRestaurantById(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return toDTO(restaurant);
    }

    public Page<RestaurantDTO> listActiveRestaurants(Pageable pageable) {
        return restaurantRepository.findActiveRestaurants(pageable).map(this::toDTO);
    }

    public Page<RestaurantDTO> getMyRestaurants(UUID ownerId, Pageable pageable) {
        return restaurantRepository.findByOwnerId(ownerId, pageable).map(this::toDTO);
    }

    public Page<RestaurantDTO> searchRestaurants(String query, Pageable pageable) {
        return restaurantRepository.searchRestaurants(query, pageable).map(this::toDTO);
    }

    public Page<RestaurantDTO> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm, Pageable pageable) {
        return restaurantRepository.findNearbyRestaurants(lat, lng, radiusKm, pageable).map(this::toDTO);
    }

    public Page<RestaurantDTO> findByCity(String city, Pageable pageable) {
        return restaurantRepository.findByCity(city, pageable).map(this::toDTO);
    }

    public Page<RestaurantDTO> findByCuisine(String cuisine, Pageable pageable) {
        return restaurantRepository.findByCuisineType(cuisine, pageable).map(this::toDTO);
    }

    public Page<RestaurantDTO> findTopRated(int minReviews, Pageable pageable) {
        return restaurantRepository.findTopRated(minReviews, pageable).map(this::toDTO);
    }

    public Page<RestaurantDTO> findEcoFriendly(int minScore, Pageable pageable) {
        return restaurantRepository.findEcoFriendly(minScore, pageable).map(this::toDTO);
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantDTO updateRestaurant(UUID ownerId, UUID restaurantId, CreateRestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(restaurantId, ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        if (request.getName() != null) restaurant.setName(request.getName());
        if (request.getDescription() != null) restaurant.setDescription(request.getDescription());
        if (request.getCuisineType() != null) restaurant.setCuisineType(request.getCuisineType());
        if (request.getCuisineTags() != null) restaurant.setCuisineTags(request.getCuisineTags());
        if (request.getPhone() != null) restaurant.setPhone(request.getPhone());
        if (request.getEmail() != null) restaurant.setEmail(request.getEmail());
        if (request.getWebsite() != null) restaurant.setWebsite(request.getWebsite());
        if (request.getAddressLine1() != null) restaurant.setAddressLine1(request.getAddressLine1());
        if (request.getCity() != null) restaurant.setCity(request.getCity());
        if (request.getLatitude() != null) restaurant.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) restaurant.setLongitude(request.getLongitude());
        if (request.getLogoUrl() != null) restaurant.setLogoUrl(request.getLogoUrl());
        if (request.getCoverImageUrl() != null) restaurant.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getSupportsTakeaway() != null) restaurant.setSupportsTakeaway(request.getSupportsTakeaway());
        if (request.getSupportsDelivery() != null) restaurant.setSupportsDelivery(request.getSupportsDelivery());
        if (request.getSupportsDineIn() != null) restaurant.setSupportsDineIn(request.getSupportsDineIn());

        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant updated: {} [{}]", restaurant.getName(), restaurant.getId());
        return toDTO(restaurant);
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Verification & Moderation
    // ─────────────────────────────────────────────────────────

    @Transactional
    public RestaurantDTO verifyRestaurant(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        restaurant.verify();
        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant verified: {} [{}]", restaurant.getName(), restaurant.getId());

        kafkaTemplate.send("cerex.restaurant.verified", restaurant.getId().toString(),
            Map.of("restaurantId", restaurant.getId(), "name", restaurant.getName()));

        return toDTO(restaurant);
    }

    @Transactional
    public RestaurantDTO suspendRestaurant(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        restaurant.suspend();
        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant suspended: [{}]", restaurant.getId());
        return toDTO(restaurant);
    }

    // ─────────────────────────────────────────────────────────
    // MENU MANAGEMENT
    // ─────────────────────────────────────────────────────────

    @Transactional
    public MenuDTO addMenu(UUID ownerId, UUID restaurantId, String name, String description, String menuType) {
        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(restaurantId, ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        Menu menu = Menu.builder()
            .name(name)
            .description(description)
            .menuType(menuType != null ? Menu.MenuType.valueOf(menuType) : Menu.MenuType.REGULAR)
            .sortOrder(restaurant.getMenus().size())
            .build();

        restaurant.addMenu(menu);
        restaurantRepository.save(restaurant);

        return toMenuDTO(menu);
    }

    @Transactional
    public MenuItemDTO addMenuItem(UUID ownerId, UUID restaurantId, UUID menuId, CreateMenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(restaurantId, ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        Menu menu = restaurant.getMenus().stream()
            .filter(m -> m.getId().equals(menuId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", menuId));

        MenuItem item = MenuItem.builder()
            .name(request.getName())
            .description(request.getDescription())
            .category(request.getCategory())
            .price(request.getPrice())
            .discountPrice(request.getDiscountPrice())
            .currencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode() : "EUR")
            .calories(request.getCalories())
            .portionSize(request.getPortionSize())
            .isVegan(request.getIsVegan() != null ? request.getIsVegan() : false)
            .isVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false)
            .isGlutenFree(request.getIsGlutenFree() != null ? request.getIsGlutenFree() : false)
            .isHalal(request.getIsHalal() != null ? request.getIsHalal() : false)
            .isKosher(request.getIsKosher() != null ? request.getIsKosher() : false)
            .allergens(request.getAllergens() != null ? request.getAllergens() : new HashSet<>())
            .imageUrl(request.getImageUrl())
            .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
            .isSpicy(request.getIsSpicy() != null ? request.getIsSpicy() : false)
            .spiceLevel(request.getSpiceLevel() != null ? request.getSpiceLevel() : 0)
            .preparationTimeMin(request.getPreparationTimeMin())
            .ecoScore(request.getEcoScore() != null ? request.getEcoScore() : 0)
            .carbonFootprintGrams(request.getCarbonFootprintGrams())
            .recipeId(request.getRecipeId())
            .sortOrder(menu.getItems().size())
            .build();

        menu.addItem(item);
        menuRepository.save(menu);

        return toMenuItemDTO(item);
    }

    public List<MenuDTO> getRestaurantMenus(UUID restaurantId) {
        List<Menu> menus = menuRepository.findActiveMenusByRestaurant(restaurantId);
        return menus.stream().map(this::toMenuDTO).toList();
    }

    // ─────────────────────────────────────────────────────────
    // Slug Generation
    // ─────────────────────────────────────────────────────────

    private String generateSlug(String input) {
        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ROOT).replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
    }

    // ─────────────────────────────────────────────────────────
    // DTO Mapping
    // ─────────────────────────────────────────────────────────

    private RestaurantDTO toDTO(Restaurant r) {
        Map<String, RestaurantDTO.OperatingHoursDTO> hoursDTO = null;
        if (r.getOperatingHours() != null) {
            hoursDTO = r.getOperatingHours().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> RestaurantDTO.OperatingHoursDTO.builder()
                        .openTime(e.getValue().getOpenTime())
                        .closeTime(e.getValue().getCloseTime())
                        .closed(e.getValue().isClosed())
                        .build()
                ));
        }

        return RestaurantDTO.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .slug(r.getSlug())
            .status(r.getStatus().name())
            .cuisineType(r.getCuisineType())
            .cuisineTags(r.getCuisineTags())
            .phone(r.getPhone())
            .email(r.getEmail())
            .website(r.getWebsite())
            .addressLine1(r.getAddressLine1())
            .addressLine2(r.getAddressLine2())
            .city(r.getCity())
            .stateProvince(r.getStateProvince())
            .postalCode(r.getPostalCode())
            .latitude(r.getLatitude())
            .longitude(r.getLongitude())
            .deliveryRadiusKm(r.getDeliveryRadiusKm())
            .logoUrl(r.getLogoUrl())
            .coverImageUrl(r.getCoverImageUrl())
            .galleryImages(r.getGalleryImages())
            .averageRating(r.getAverageRating())
            .totalReviews(r.getTotalReviews())
            .totalOrders(r.getTotalOrders())
            .supportsTakeaway(r.getSupportsTakeaway())
            .supportsDelivery(r.getSupportsDelivery())
            .supportsDineIn(r.getSupportsDineIn())
            .acceptsReservations(r.getAcceptsReservations())
            .offersCatering(r.getOffersCatering())
            .isVerified(r.getIsVerified())
            .isPremiumPartner(r.getIsPremiumPartner())
            .operatingHours(hoursDTO)
            .minimumOrderAmount(r.getMinimumOrderAmount())
            .averagePreparationTimeMin(r.getAveragePreparationTimeMin())
            .ecoScore(r.getEcoScore())
            .ownerId(r.getOwnerId())
            .build();
    }

    private MenuDTO toMenuDTO(Menu m) {
        return MenuDTO.builder()
            .id(m.getId())
            .name(m.getName())
            .description(m.getDescription())
            .menuType(m.getMenuType().name())
            .isActive(m.getIsActive())
            .availableFrom(m.getAvailableFrom())
            .availableUntil(m.getAvailableUntil())
            .items(m.getItems().stream().map(this::toMenuItemDTO).toList())
            .build();
    }

    private MenuItemDTO toMenuItemDTO(MenuItem i) {
        return MenuItemDTO.builder()
            .id(i.getId())
            .name(i.getName())
            .description(i.getDescription())
            .category(i.getCategory())
            .price(i.getPrice())
            .discountPrice(i.getDiscountPrice())
            .effectivePrice(i.getEffectivePrice())
            .currencyCode(i.getCurrencyCode())
            .calories(i.getCalories())
            .portionSize(i.getPortionSize())
            .isVegan(i.getIsVegan())
            .isVegetarian(i.getIsVegetarian())
            .isGlutenFree(i.getIsGlutenFree())
            .isHalal(i.getIsHalal())
            .isKosher(i.getIsKosher())
            .allergens(i.getAllergens())
            .isSpicy(i.getIsSpicy())
            .spiceLevel(i.getSpiceLevel())
            .imageUrl(i.getImageUrl())
            .isAvailable(i.getIsAvailable())
            .isFeatured(i.getIsFeatured())
            .preparationTimeMin(i.getPreparationTimeMin())
            .ecoScore(i.getEcoScore())
            .carbonFootprintGrams(i.getCarbonFootprintGrams())
            .recipeId(i.getRecipeId())
            .build();
    }
}

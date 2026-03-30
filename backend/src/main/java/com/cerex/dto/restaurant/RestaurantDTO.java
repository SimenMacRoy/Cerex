package com.cerex.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for restaurant listing / detail responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {

    private UUID id;
    private String name;
    private String description;
    private String slug;
    private String status;
    private String cuisineType;
    private Set<String> cuisineTags;

    // Contact
    private String phone;
    private String email;
    private String website;

    // Location
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateProvince;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double deliveryRadiusKm;

    // Media
    private String logoUrl;
    private String coverImageUrl;
    private List<String> galleryImages;

    // Ratings
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer totalOrders;

    // Features
    private Boolean supportsTakeaway;
    private Boolean supportsDelivery;
    private Boolean supportsDineIn;
    private Boolean acceptsReservations;
    private Boolean offersCatering;
    private Boolean isVerified;
    private Boolean isPremiumPartner;

    // Operating
    private Map<String, OperatingHoursDTO> operatingHours;
    private BigDecimal minimumOrderAmount;
    private Integer averagePreparationTimeMin;
    private Integer ecoScore;

    // Menus
    private List<MenuDTO> menus;

    // Owner info (for management views)
    private UUID ownerId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHoursDTO {
        private String openTime;
        private String closeTime;
        private boolean closed;
    }
}

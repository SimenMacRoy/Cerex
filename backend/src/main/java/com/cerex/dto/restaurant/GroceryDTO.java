package com.cerex.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for grocery store responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroceryDTO {

    private UUID id;
    private String name;
    private String description;
    private String slug;
    private String status;
    private String groceryType;
    private Set<String> specialtyTags;

    // Contact
    private String phone;
    private String email;
    private String website;

    // Location
    private String addressLine1;
    private String city;
    private String stateProvince;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double deliveryRadiusKm;

    // Media
    private String logoUrl;
    private String coverImageUrl;

    // Ratings
    private BigDecimal averageRating;
    private Integer totalReviews;

    // Features
    private Boolean supportsDelivery;
    private Boolean supportsPickup;
    private BigDecimal minimumOrderAmount;
    private Boolean isOrganicCertified;
    private Boolean isVerified;
    private Integer ecoScore;

    // Products summary
    private Integer productCount;
    private List<GroceryProductDTO> featuredProducts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroceryProductDTO {
        private UUID id;
        private String name;
        private String description;
        private String category;
        private String brand;
        private BigDecimal price;
        private BigDecimal pricePerUnit;
        private String unit;
        private String imageUrl;
        private Boolean isInStock;
        private Boolean isOrganic;
        private Boolean isLocal;
        private Integer ecoScore;
        private String nutriScore;
        private Set<String> allergens;
    }
}

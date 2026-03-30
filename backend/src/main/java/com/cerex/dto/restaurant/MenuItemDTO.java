package com.cerex.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for menu item responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {

    private UUID id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal effectivePrice;
    private String currencyCode;

    // Dietary
    private Integer calories;
    private String portionSize;
    private Boolean isVegan;
    private Boolean isVegetarian;
    private Boolean isGlutenFree;
    private Boolean isHalal;
    private Boolean isKosher;
    private Set<String> allergens;
    private Boolean isSpicy;
    private Integer spiceLevel;

    // Display
    private String imageUrl;
    private Boolean isAvailable;
    private Boolean isFeatured;
    private Integer preparationTimeMin;

    // Eco
    private Integer ecoScore;
    private Integer carbonFootprintGrams;
    private String nutriScore;

    private UUID recipeId;
}

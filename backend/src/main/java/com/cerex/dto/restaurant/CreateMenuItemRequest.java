package com.cerex.dto.restaurant;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for adding/updating a menu item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuItemRequest {

    @NotBlank(message = "Le nom de l'article est obligatoire")
    @Size(max = 200)
    private String name;

    @Size(max = 1000)
    private String description;

    private String category;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.00")
    private BigDecimal price;

    private BigDecimal discountPrice;
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
    private Boolean isFeatured;
    private Integer preparationTimeMin;

    // Eco
    private Integer ecoScore;
    private Integer carbonFootprintGrams;

    private UUID recipeId;
}

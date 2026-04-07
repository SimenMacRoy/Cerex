package com.cerex.dto.recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new recipe.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipeRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 300)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000)
    private String description;

    private String story;

    // Classification
    private UUID continentId;
    private UUID countryId;
    private UUID cultureId;
    private UUID categoryId;

    // Optional — defaults to STANDARD if not provided
    private String recipeType;

    private String cuisineType;
    private String courseType;

    @NotNull(message = "Difficulty level is required")
    private String difficultyLevel;

    @Min(0) @Max(5)
    private Integer spiceLevel;

    // Timing
    @NotNull @Min(0)
    private Integer prepTimeMinutes;

    @NotNull @Min(0)
    private Integer cookTimeMinutes;

    @Min(0)
    private Integer restTimeMinutes;

    @NotNull @Min(1) @Max(500)
    private Integer servings;

    private String servingsUnit;

    // Nutrition (optional)
    private BigDecimal caloriesKcal;
    private BigDecimal proteinG;
    private BigDecimal carbsG;
    private BigDecimal fatG;
    private BigDecimal fiberG;
    private BigDecimal sugarG;
    private BigDecimal sodiumMg;

    // Dietary flags
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Boolean isGlutenFree;
    private Boolean isDairyFree;
    private Boolean isHalal;
    private Boolean isKosher;
    private Boolean isNutFree;
    private Boolean isLowCarb;

    // Media
    private String coverImageUrl;
    private String videoUrl;

    // Content
    @NotEmpty(message = "At least one ingredient is required")
    @Valid
    private List<IngredientRequest> ingredients;

    @NotEmpty(message = "At least one step is required")
    @Valid
    private List<StepRequest> steps;

    private String[] tags;

    /** DRAFT or PENDING_REVIEW */
    private String status;

    // ── Nested request DTOs ─────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientRequest {
        /**
         * Optional: ID from the ingredient master catalog.
         * If null, {@code name} is used to look up or auto-create the ingredient.
         */
        private UUID ingredientId;

        /** Free-text ingredient name (used when ingredientId is not provided). */
        private String name;

        private BigDecimal quantity;
        private String unit;
        private String displayText;
        private Boolean isOptional;
        private String groupName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepRequest {
        @NotBlank(message = "Instruction is required")
        @Size(max = 2000)
        private String instruction;
        private Integer durationMinutes;
        private String imageUrl;
        private String tip;
    }
}

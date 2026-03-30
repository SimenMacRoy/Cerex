package com.cerex.dto.recipe;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full recipe detail DTO for the recipe detail page.
 * Includes ingredients, steps, nutrition, and engagement data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecipeDetailDTO {

    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String story;
    private String coverImageUrl;
    private String videoUrl;

    // Author
    private AuthorDTO author;

    // Classification
    private String recipeType;
    private String cuisineType;
    private String courseType;
    private String difficultyLevel;
    private Integer spiceLevel;

    // Timing
    private Integer prepTimeMinutes;
    private Integer cookTimeMinutes;
    private Integer restTimeMinutes;
    private Integer totalTimeMinutes;
    private Integer servings;
    private String servingsUnit;

    // Nutrition
    private NutritionDTO nutrition;

    // Dietary flags
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Boolean isGlutenFree;
    private Boolean isDairyFree;
    private Boolean isHalal;
    private Boolean isKosher;
    private Boolean isNutFree;
    private Boolean isLowCarb;

    // Content
    private List<IngredientLineDTO> ingredients;
    private List<StepDTO> steps;
    private String[] tags;

    // Engagement
    private Long viewCount;
    private Integer likeCount;
    private Integer saveCount;
    private BigDecimal avgRating;
    private Integer ratingCount;
    private Integer commentCount;

    // Flags
    private Boolean isPremium;
    private Boolean isAiGenerated;
    private Boolean isFeatured;
    private String status;
    private Integer version;

    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;

    // ── Nested DTOs ─────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDTO {
        private UUID id;
        private String displayName;
        private String avatarUrl;
        private Boolean isVerifiedChef;
        private Integer recipeCount;
        private Integer followerCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionDTO {
        private BigDecimal caloriesKcal;
        private BigDecimal proteinG;
        private BigDecimal carbsG;
        private BigDecimal fatG;
        private BigDecimal fiberG;
        private BigDecimal sugarG;
        private BigDecimal sodiumMg;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientLineDTO {
        private UUID id;
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
    public static class StepDTO {
        private Integer stepNumber;
        private String instruction;
        private Integer durationMinutes;
        private String imageUrl;
        private String tip;
    }
}

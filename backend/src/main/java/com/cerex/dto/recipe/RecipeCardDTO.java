package com.cerex.dto.recipe;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Compact recipe card DTO for lists and feeds.
 * Contains only the data needed to render a recipe preview card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecipeCardDTO {

    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String coverImageUrl;
    private String thumbnailUrl;

    // Author
    private UUID authorId;
    private String authorName;
    private String authorAvatarUrl;

    // Classification
    private String cuisineType;
    private String courseType;
    private String difficultyLevel;
    private Integer spiceLevel;

    // Timing
    private Integer prepTimeMinutes;
    private Integer cookTimeMinutes;
    private Integer totalTimeMinutes;
    private Integer servings;

    // Engagement
    private BigDecimal avgRating;
    private Integer ratingCount;
    private Integer likeCount;
    private Integer viewCount;

    // Flags
    private Boolean isVegan;
    private Boolean isVegetarian;
    private Boolean isGlutenFree;
    private Boolean isHalal;
    private Boolean isPremium;
    private Boolean isAiGenerated;
    private Boolean isFeatured;

    private String[] tags;
    private Instant publishedAt;
}

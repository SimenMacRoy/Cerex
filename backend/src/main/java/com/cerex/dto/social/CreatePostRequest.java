package com.cerex.dto.social;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating a new social post.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "Le contenu est obligatoire")
    @Size(max = 5000)
    private String content;

    @Size(max = 200)
    private String title;

    private String postType; // GENERAL, RECIPE_SHARE, RECIPE_REPRODUCTION, FOOD_PHOTO, etc.

    private UUID recipeId;
    private UUID restaurantId;

    private List<String> mediaUrls;
    private String videoUrl;
    private String thumbnailUrl;

    private Set<String> hashtags;

    private String locationName;
    private Double latitude;
    private Double longitude;

    // For reproductions
    private UUID originalPostId;
    private Integer reproductionRating;
    private String reproductionNotes;
}

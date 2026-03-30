package com.cerex.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for the current authenticated user's full profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDTO {

    private UUID id;
    private String email;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String coverImageUrl;
    private String role;
    private String subscriptionPlan;

    // Location
    private String city;
    private String timezone;
    private String preferredLanguage;
    private String preferredCurrency;

    // Culinary preferences
    private String cookingSkillLevel;
    private String[] dietaryPreferences;
    private String[] favoriteCuisines;
    private String[] allergens;
    private Integer spiceTolerance;

    // Social stats
    private Integer followerCount;
    private Integer followingCount;
    private Integer recipeCount;

    // Status
    private Boolean isVerifiedChef;
    private Boolean emailVerified;
    private Instant createdAt;
}

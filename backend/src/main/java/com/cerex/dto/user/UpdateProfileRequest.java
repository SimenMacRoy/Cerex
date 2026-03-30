package com.cerex.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating the current user's profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String displayName;

    @Size(max = 1000)
    private String bio;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 500)
    private String coverImageUrl;

    @Size(max = 100)
    private String city;

    @Size(max = 50)
    private String timezone;

    @Size(max = 10)
    private String preferredLanguage;

    @Size(max = 3)
    private String preferredCurrency;

    private String cookingSkillLevel;
    private String[] dietaryPreferences;
    private String[] favoriteCuisines;
    private String[] allergens;
    private Integer spiceTolerance;
}

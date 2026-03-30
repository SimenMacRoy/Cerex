package com.cerex.service;

import com.cerex.domain.User;
import com.cerex.domain.UserProfile;
import com.cerex.dto.user.UpdateProfileRequest;
import com.cerex.dto.user.UserProfileDTO;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.repository.UserProfileRepository;
import com.cerex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for user profile management.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;

    /**
     * Get the full profile for the current authenticated user.
     */
    @Cacheable(value = "userProfiles", key = "#userId")
    public UserProfileDTO getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserProfile profile = profileRepository.findByUserId(userId)
            .orElse(null);

        return mapToDTO(user, profile);
    }

    /**
     * Get a public profile for any user.
     */
    @Cacheable(value = "userProfiles", key = "'public:' + #userId")
    public UserProfileDTO getPublicProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserProfile profile = profileRepository.findByUserId(userId)
            .orElse(null);

        // Return a subset — no private fields
        return UserProfileDTO.builder()
            .id(user.getId())
            .displayName(profile != null ? profile.getDisplayName() : user.getEmail())
            .bio(profile != null ? profile.getBio() : null)
            .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
            .coverImageUrl(profile != null ? profile.getCoverImageUrl() : null)
            .role(user.getRole().name())
            .followerCount(profile != null ? profile.getFollowerCount() : 0)
            .followingCount(profile != null ? profile.getFollowingCount() : 0)
            .recipeCount(profile != null ? profile.getRecipeCount() : 0)
            .isVerifiedChef(profile != null ? profile.getIsVerifiedChef() : false)
            .createdAt(user.getCreatedAt())
            .build();
    }

    /**
     * Update the current user's profile.
     */
    @Transactional
    @CacheEvict(value = "userProfiles", allEntries = true)
    public UserProfileDTO updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserProfile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserProfile newProfile = UserProfile.builder()
                    .user(user)
                    .displayName(request.getDisplayName() != null ? request.getDisplayName() : user.getEmail())
                    .build();
                return profileRepository.save(newProfile);
            });

        // Apply partial updates
        if (request.getDisplayName() != null)       profile.setDisplayName(request.getDisplayName());
        if (request.getBio() != null)               profile.setBio(request.getBio());
        if (request.getAvatarUrl() != null)         profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getCoverImageUrl() != null)     profile.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getCity() != null)              profile.setCity(request.getCity());
        if (request.getTimezone() != null)          profile.setTimezone(request.getTimezone());
        if (request.getPreferredLanguage() != null) profile.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getPreferredCurrency() != null) profile.setPreferredCurrency(request.getPreferredCurrency());
        if (request.getCookingSkillLevel() != null) profile.setCookingSkillLevel(request.getCookingSkillLevel());
        if (request.getDietaryPreferences() != null) profile.setDietaryPreferences(request.getDietaryPreferences());
        if (request.getFavoriteCuisines() != null)  profile.setFavoriteCuisines(request.getFavoriteCuisines());
        if (request.getAllergens() != null)          profile.setAllergens(request.getAllergens());
        if (request.getSpiceTolerance() != null)    profile.setSpiceTolerance(request.getSpiceTolerance());

        profileRepository.save(profile);
        log.info("Updated profile for user {}", userId);
        return mapToDTO(user, profile);
    }

    /**
     * Initiate GDPR account deletion (30-day grace period).
     */
    @Transactional
    @CacheEvict(value = "userProfiles", allEntries = true)
    public void requestAccountDeletion(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.requestDeletion();
        userRepository.save(user);
        log.info("Account deletion requested for user {}", userId);
    }

    // ── Mapping ─────────────────────────────────────────────

    private UserProfileDTO mapToDTO(User user, UserProfile profile) {
        return UserProfileDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .displayName(profile != null ? profile.getDisplayName() : user.getEmail())
            .bio(profile != null ? profile.getBio() : null)
            .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
            .coverImageUrl(profile != null ? profile.getCoverImageUrl() : null)
            .role(user.getRole().name())
            .subscriptionPlan(user.getSubscriptionPlan())
            .city(profile != null ? profile.getCity() : null)
            .timezone(profile != null ? profile.getTimezone() : null)
            .preferredLanguage(profile != null ? profile.getPreferredLanguage() : null)
            .preferredCurrency(profile != null ? profile.getPreferredCurrency() : null)
            .cookingSkillLevel(profile != null ? profile.getCookingSkillLevel() : null)
            .dietaryPreferences(profile != null ? profile.getDietaryPreferences() : null)
            .favoriteCuisines(profile != null ? profile.getFavoriteCuisines() : null)
            .allergens(profile != null ? profile.getAllergens() : null)
            .spiceTolerance(profile != null ? profile.getSpiceTolerance() : null)
            .followerCount(profile != null ? profile.getFollowerCount() : 0)
            .followingCount(profile != null ? profile.getFollowingCount() : 0)
            .recipeCount(profile != null ? profile.getRecipeCount() : 0)
            .isVerifiedChef(profile != null ? profile.getIsVerifiedChef() : false)
            .emailVerified(user.getEmailVerified())
            .createdAt(user.getCreatedAt())
            .build();
    }
}

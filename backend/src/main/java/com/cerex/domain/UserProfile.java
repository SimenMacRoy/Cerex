package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Extended user profile and preferences.
 *
 * <p>Separated from {@link User} to keep authentication data lean.
 * Contains display information, culinary preferences, and social metadata.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "user_profiles",
    schema = "users_schema",
    indexes = {
        @Index(name = "idx_user_profiles_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_user_profiles_country",  columnList = "country_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    // ── Relationship to User ────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    // ── Display Info ────────────────────────────────────────
    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 100)
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Size(max = 1000)
    @Column(name = "bio", length = 1000)
    private String bio;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Size(max = 500)
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    // ── Personal Info ───────────────────────────────────────
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 10)
    @Column(name = "gender", length = 10)
    private String gender;

    // ── Location ────────────────────────────────────────────
    @Column(name = "country_id")
    private UUID countryId;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 50)
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Size(max = 10)
    @Column(name = "preferred_language", length = 10)
    @Builder.Default
    private String preferredLanguage = "en";

    @Size(max = 3)
    @Column(name = "preferred_currency", length = 3)
    @Builder.Default
    private String preferredCurrency = "EUR";

    // ── Culinary Preferences ────────────────────────────────
    @Column(name = "cooking_skill_level", length = 20)
    @Builder.Default
    private String cookingSkillLevel = "BEGINNER";

    @Column(name = "dietary_preferences", columnDefinition = "VARCHAR[]")
    @Builder.Default
    private String[] dietaryPreferences = new String[]{};

    @Column(name = "favorite_cuisines", columnDefinition = "VARCHAR[]")
    @Builder.Default
    private String[] favoriteCuisines = new String[]{};

    @Column(name = "allergens", columnDefinition = "VARCHAR[]")
    @Builder.Default
    private String[] allergens = new String[]{};

    @Min(0) @Max(5)
    @Column(name = "spice_tolerance")
    @Builder.Default
    private Integer spiceTolerance = 3;

    // ── Social Counters ─────────────────────────────────────
    @Column(name = "follower_count")
    @Builder.Default
    private Integer followerCount = 0;

    @Column(name = "following_count")
    @Builder.Default
    private Integer followingCount = 0;

    @Column(name = "recipe_count")
    @Builder.Default
    private Integer recipeCount = 0;

    // ── Verification ────────────────────────────────────────
    @Column(name = "is_verified_chef")
    @Builder.Default
    private Boolean isVerifiedChef = false;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    // ── Timestamps ──────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

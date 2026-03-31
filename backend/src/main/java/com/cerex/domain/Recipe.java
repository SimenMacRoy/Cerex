package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core domain entity representing a culinary recipe on the Cerex platform.
 *
 * <p>A Recipe is the central aggregate of the Cerex platform. It captures:
 * <ul>
 *   <li>Core culinary information (ingredients, steps, techniques)</li>
 *   <li>Cultural and geographic metadata (continent, country, culture)</li>
 *   <li>Nutritional data (calories, macros)</li>
 *   <li>Dietary flags (vegan, halal, gluten-free, etc.)</li>
 *   <li>Engagement metrics (views, likes, ratings)</li>
 *   <li>Publication lifecycle (draft → review → published → archived)</li>
 * </ul>
 *
 * <p>Uses soft delete ({@code deleted_at}) to preserve data for analytics.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 * @since 2026-03-30
 */
@Entity
@Table(
    name = "recipes",
    schema = "recipes_schema",
    indexes = {
        @Index(name = "idx_recipes_author_id",   columnList = "author_id"),
        @Index(name = "idx_recipes_slug",         columnList = "slug", unique = true),
        @Index(name = "idx_recipes_status",       columnList = "status"),
        @Index(name = "idx_recipes_continent_id", columnList = "continent_id"),
        @Index(name = "idx_recipes_country_id",   columnList = "country_id"),
        @Index(name = "idx_recipes_culture_id",   columnList = "culture_id"),
        @Index(name = "idx_recipes_published_at", columnList = "published_at"),
    }
)
@SQLDelete(sql = "UPDATE recipes_schema.recipes SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"ingredients", "steps", "media"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Recipe {

    // ─────────────────────────────────────────────────────────────
    // Primary Key
    // ─────────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    // ─────────────────────────────────────────────────────────────
    // Author & Ownership
    // ─────────────────────────────────────────────────────────────

    /** The user who created this recipe. */
    @NotNull(message = "Author is required")
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private User author;

    // ─────────────────────────────────────────────────────────────
    // Cultural & Geographic Classification
    // ─────────────────────────────────────────────────────────────

    /** The continent this recipe originates from (e.g., Africa, Asia). */
    @Column(name = "continent_id")
    private UUID continentId;

    /** The country this recipe originates from (e.g., Senegal, Japan). */
    @Column(name = "country_id")
    private UUID countryId;

    /** The specific cultural group (e.g., Wolof, Oaxacan). */
    @Column(name = "culture_id")
    private UUID cultureId;

    /** Recipe category (e.g., main-course, dessert, bread). */
    @Column(name = "category_id")
    private UUID categoryId;

    // ─────────────────────────────────────────────────────────────
    // Core Recipe Information
    // ─────────────────────────────────────────────────────────────

    /** Recipe title in the default language (English). */
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 300, message = "Title must be between 3 and 300 characters")
    @Column(name = "title", nullable = false, length = 300)
    private String title;

    /** French translation of the title. */
    @Size(max = 300)
    @Column(name = "title_fr", length = 300)
    private String titleFr;

    /** Spanish translation of the title. */
    @Size(max = 300)
    @Column(name = "title_es", length = 300)
    private String titleEs;

    /** Chinese translation of the title. */
    @Size(max = 300)
    @Column(name = "title_zh", length = 300)
    private String titleZh;

    /** Arabic translation of the title. */
    @Size(max = 300)
    @Column(name = "title_ar", length = 300)
    private String titleAr;

    /**
     * URL-friendly slug for SEO. Auto-generated from title.
     * Example: "poulet-yassa-senegalais"
     */
    @NotBlank
    @Column(name = "slug", nullable = false, length = 400, unique = true)
    private String slug;

    /** Full description of the recipe (English). Min 10 characters for quality. */
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000)
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /** French translation of the description. */
    @Column(name = "description_fr", columnDefinition = "TEXT")
    private String descriptionFr;

    /** Spanish translation of the description. */
    @Column(name = "description_es", columnDefinition = "TEXT")
    private String descriptionEs;

    /**
     * Cultural story and history behind the dish.
     * Adds richness for cultural exploration.
     */
    @Column(name = "story", columnDefinition = "TEXT")
    private String story;

    // ─────────────────────────────────────────────────────────────
    // Culinary Classification
    // ─────────────────────────────────────────────────────────────

    /**
     * Type of recipe.
     * @see RecipeType
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recipe_type", nullable = false, length = 30)
    @Builder.Default
    private RecipeType recipeType = RecipeType.DISH;

    /** Cuisine style (e.g., "West African", "Japanese", "Mediterranean"). */
    @Size(max = 100)
    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;

    /** Course type within a meal (starter, main, dessert, etc.). */
    @Size(max = 50)
    @Column(name = "course_type", length = 50)
    private String courseType;

    /**
     * Difficulty level for home cooks.
     * @see DifficultyLevel
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    /**
     * Spice/heat level on a scale of 0-5.
     * 0 = no spice, 5 = extremely spicy.
     */
    @Min(0) @Max(5)
    @Column(name = "spice_level")
    @Builder.Default
    private Integer spiceLevel = 1;

    // ─────────────────────────────────────────────────────────────
    // Timing
    // ─────────────────────────────────────────────────────────────

    /** Active preparation time in minutes (chopping, mixing, etc.). */
    @NotNull
    @Min(value = 0, message = "Prep time cannot be negative")
    @Column(name = "prep_time_minutes", nullable = false)
    @Builder.Default
    private Integer prepTimeMinutes = 0;

    /** Active cooking time in minutes (baking, frying, etc.). */
    @NotNull
    @Min(value = 0, message = "Cook time cannot be negative")
    @Column(name = "cook_time_minutes", nullable = false)
    @Builder.Default
    private Integer cookTimeMinutes = 0;

    /** Passive rest time in minutes (marinating, resting, chilling). */
    @Min(0)
    @Column(name = "rest_time_minutes")
    @Builder.Default
    private Integer restTimeMinutes = 0;

    // ─────────────────────────────────────────────────────────────
    // Servings
    // ─────────────────────────────────────────────────────────────

    /** Default number of servings this recipe produces. */
    @NotNull
    @Min(value = 1, message = "Servings must be at least 1")
    @Max(value = 500)
    @Column(name = "servings", nullable = false)
    @Builder.Default
    private Integer servings = 4;

    /** Unit for servings (persons, portions, pieces, etc.). */
    @Column(name = "servings_unit", length = 50)
    @Builder.Default
    private String servingsUnit = "persons";

    // ─────────────────────────────────────────────────────────────
    // Nutritional Information (per serving)
    // ─────────────────────────────────────────────────────────────

    /** Calories per serving in kcal. */
    @DecimalMin("0.0")
    @Column(name = "calories_kcal", precision = 8, scale = 2)
    private BigDecimal caloriesKcal;

    /** Protein content per serving in grams. */
    @DecimalMin("0.0")
    @Column(name = "protein_g", precision = 8, scale = 2)
    private BigDecimal proteinG;

    /** Total carbohydrates per serving in grams. */
    @DecimalMin("0.0")
    @Column(name = "carbs_g", precision = 8, scale = 2)
    private BigDecimal carbsG;

    /** Total fat per serving in grams. */
    @DecimalMin("0.0")
    @Column(name = "fat_g", precision = 8, scale = 2)
    private BigDecimal fatG;

    /** Dietary fiber per serving in grams. */
    @DecimalMin("0.0")
    @Column(name = "fiber_g", precision = 8, scale = 2)
    private BigDecimal fiberG;

    /** Sugar content per serving in grams. */
    @DecimalMin("0.0")
    @Column(name = "sugar_g", precision = 8, scale = 2)
    private BigDecimal sugarG;

    /** Sodium content per serving in milligrams. */
    @DecimalMin("0.0")
    @Column(name = "sodium_mg", precision = 8, scale = 2)
    private BigDecimal sodiumMg;

    // ─────────────────────────────────────────────────────────────
    // Dietary Flags
    // ─────────────────────────────────────────────────────────────

    /** True if the recipe contains no meat or fish. */
    @Column(name = "is_vegetarian")
    @Builder.Default
    private Boolean isVegetarian = false;

    /** True if the recipe contains no animal products whatsoever. */
    @Column(name = "is_vegan")
    @Builder.Default
    private Boolean isVegan = false;

    /** True if the recipe is free from gluten-containing ingredients. */
    @Column(name = "is_gluten_free")
    @Builder.Default
    private Boolean isGlutenFree = false;

    /** True if the recipe contains no dairy products. */
    @Column(name = "is_dairy_free")
    @Builder.Default
    private Boolean isDairyFree = false;

    /** True if the recipe adheres to Islamic dietary law. */
    @Column(name = "is_halal")
    @Builder.Default
    private Boolean isHalal = false;

    /** True if the recipe adheres to Jewish dietary law. */
    @Column(name = "is_kosher")
    @Builder.Default
    private Boolean isKosher = false;

    /** True if the recipe contains no nuts or nut-derived products. */
    @Column(name = "is_nut_free")
    @Builder.Default
    private Boolean isNutFree = false;

    /** True if the recipe is low in carbohydrates (typically < 20g net carbs/serving). */
    @Column(name = "is_low_carb")
    @Builder.Default
    private Boolean isLowCarb = false;

    // ─────────────────────────────────────────────────────────────
    // Media
    // ─────────────────────────────────────────────────────────────

    /** URL to the hero/cover image (1200x800 WebP via CloudFront CDN). */
    @Size(max = 500)
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    /** URL to a cooking video for this recipe. */
    @Size(max = 500)
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    /** URL to the thumbnail image (400x300 WebP). */
    @Size(max = 500)
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    // ─────────────────────────────────────────────────────────────
    // Engagement Metrics
    // ─────────────────────────────────────────────────────────────

    /** Total number of times this recipe page has been viewed. */
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    /** Total number of likes this recipe has received. */
    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    /** Number of times users saved this to their collection. */
    @Column(name = "save_count")
    @Builder.Default
    private Integer saveCount = 0;

    /** Number of times this recipe has been ordered via the platform. */
    @Column(name = "order_count")
    @Builder.Default
    private Integer orderCount = 0;

    /** Number of times this recipe has been shared. */
    @Column(name = "share_count")
    @Builder.Default
    private Integer shareCount = 0;

    /** Number of comments on this recipe. */
    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    /**
     * Average user rating (0.00 - 5.00).
     * Updated via database trigger after each new review.
     */
    @DecimalMin("0.0") @DecimalMax("5.0")
    @Column(name = "avg_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    /** Total number of ratings (reviews) received. */
    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    // ─────────────────────────────────────────────────────────────
    // Publication Status & Moderation
    // ─────────────────────────────────────────────────────────────

    /**
     * Current lifecycle status of the recipe.
     * @see RecipeStatus
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RecipeStatus status = RecipeStatus.DRAFT;

    /** Whether this recipe was generated by AI. */
    @Column(name = "is_ai_generated")
    @Builder.Default
    private Boolean isAiGenerated = false;

    /** The AI model version that generated this recipe (e.g., "gpt-4o-2024-11"). */
    @Size(max = 50)
    @Column(name = "ai_model_version", length = 50)
    private String aiModelVersion;

    /** Whether this recipe is editorially featured. */
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    /** Whether this recipe requires a premium subscription to access in full. */
    @Column(name = "is_premium")
    @Builder.Default
    private Boolean isPremium = false;

    /** Moderation review status. */
    @Size(max = 30)
    @Column(name = "moderation_status", length = 30)
    @Builder.Default
    private String moderationStatus = "PENDING";

    /** Notes from the moderator (rejection reason, feedback). */
    @Column(name = "moderation_note", columnDefinition = "TEXT")
    private String moderationNote;

    // ─────────────────────────────────────────────────────────────
    // SEO Metadata
    // ─────────────────────────────────────────────────────────────

    /** SEO meta title (overrides recipe title for search engines). */
    @Size(max = 200)
    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    /** SEO meta description (for Google snippet). */
    @Size(max = 500)
    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    /** Array of SEO keywords. */
    @Column(name = "keywords", columnDefinition = "VARCHAR[]")
    @Builder.Default
    private String[] keywords = new String[]{};

    /** User-applied tags for discoverability. */
    @Column(name = "tags", columnDefinition = "VARCHAR[]")
    @Builder.Default
    private String[] tags = new String[]{};

    // ─────────────────────────────────────────────────────────────
    // Versioning & Forking
    // ─────────────────────────────────────────────────────────────

    /** Current version number (incremented on each published edit). */
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    /** Points to the original recipe if this is a version. */
    @Column(name = "original_recipe_id")
    private UUID originalRecipeId;

    /** Points to the recipe this was forked/adapted from. */
    @Column(name = "forked_from_id")
    private UUID forkedFromId;

    // ─────────────────────────────────────────────────────────────
    // Timestamps
    // ─────────────────────────────────────────────────────────────

    /** When the recipe was first published (set on first PUBLISHED status change). */
    @Column(name = "published_at")
    private Instant publishedAt;

    /** Managed by Hibernate — automatically set on INSERT. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Managed by Hibernate — automatically updated on each UPDATE. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Soft delete timestamp — null means not deleted. */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ─────────────────────────────────────────────────────────────
    // Relationships
    // ─────────────────────────────────────────────────────────────

    /** Ordered list of ingredient lines for this recipe. */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    /** Ordered list of cooking steps. */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    @Builder.Default
    private List<RecipeStep> steps = new ArrayList<>();

    /** Associated media assets (photos, videos). */
    @OneToMany(mappedBy = "entityId", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Media> media = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns the average rating for this recipe.
     * Delegates to {@code avgRating} which is the persisted column name.
     *
     * @return the average rating (0.00–5.00), or {@code null} if not yet rated
     */
    public java.math.BigDecimal getAverageRating() {
        return avgRating;
    }

    /**
     * Calculates total time from prep + cook + rest times.
     * @return total preparation and cooking time in minutes
     */
    public int getTotalTimeMinutes() {
        return (prepTimeMinutes != null ? prepTimeMinutes : 0)
             + (cookTimeMinutes != null ? cookTimeMinutes : 0)
             + (restTimeMinutes != null ? restTimeMinutes : 0);
    }

    /**
     * Adds an ingredient line to this recipe.
     * Maintains display order automatically.
     *
     * @param ingredient the ingredient line to add
     */
    public void addIngredient(RecipeIngredient ingredient) {
        ingredient.setRecipe(this);
        ingredient.setDisplayOrder(this.ingredients.size());
        this.ingredients.add(ingredient);
    }

    /**
     * Adds a cooking step, enforcing sequential step numbering.
     *
     * @param step the cooking step to add
     */
    public void addStep(RecipeStep step) {
        step.setRecipe(this);
        step.setStepNumber(this.steps.size() + 1);
        this.steps.add(step);
    }

    /**
     * Determines if this recipe is ready for publication.
     * A recipe is valid when it has a title, description, at least one ingredient,
     * and at least one cooking step.
     *
     * @return true if the recipe meets minimum publication requirements
     */
    public boolean isPublishable() {
        return title != null && !title.isBlank()
            && description != null && !description.isBlank()
            && !ingredients.isEmpty()
            && !steps.isEmpty();
    }

    /**
     * Domain method to publish the recipe. Validates state and transitions status.
     *
     * @throws IllegalStateException if the recipe is not in DRAFT status
     * @throws IllegalArgumentException if the recipe fails publishability checks
     */
    public void publish() {
        if (this.status != RecipeStatus.DRAFT) {
            throw new IllegalStateException(
                "Recipe can only be published from DRAFT status. Current: " + this.status
            );
        }
        if (!isPublishable()) {
            throw new IllegalArgumentException(
                "Recipe is not publishable. Ensure it has title, description, ingredients, and steps."
            );
        }
        this.status = RecipeStatus.PENDING_REVIEW;
    }

    /**
     * Marks the recipe as approved and published by a moderator.
     */
    public void approve() {
        this.status = RecipeStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.moderationStatus = "APPROVED";
    }

    /**
     * Rejects the recipe with a moderator note.
     *
     * @param note explanation for the rejection
     */
    public void reject(String note) {
        this.status = RecipeStatus.REJECTED;
        this.moderationStatus = "REJECTED";
        this.moderationNote = note;
    }

    // ─────────────────────────────────────────────────────────────
    // Enums
    // ─────────────────────────────────────────────────────────────

    /** Lifecycle status of a recipe. */
    public enum RecipeStatus {
        DRAFT,           // Work in progress
        PENDING_REVIEW,  // Submitted for moderation
        PUBLISHED,       // Live on the platform
        ARCHIVED,        // Hidden from public, preserved for author
        REJECTED         // Failed moderation review
    }

    /** Difficulty classification for recipes. */
    public enum DifficultyLevel {
        BEGINNER,     // Simple recipes with few steps and common ingredients
        EASY,         // Accessible for home cooks with basic skills
        MEDIUM,       // Requires some cooking experience
        HARD,         // Complex techniques or multi-step processes
        EXPERT        // Professional-level techniques required
    }

    /** Type classification for culinary items. */
    public enum RecipeType {
        DISH,         // Main dish
        BEVERAGE,     // Hot or cold drink
        DESSERT,      // Sweet courses
        SNACK,        // Light bites
        SAUCE,        // Condiment or sauce
        BREAD,        // Baked bread or pastry
        SOUP,         // Soup or stew
        SALAD,        // Salad or cold starter
        SIDE_DISH,    // Accompaniment
        MARINADE,     // Marinade or rub
        BREAKFAST,    // Morning meal
        STREET_FOOD   // Street food or fast casual
    }
}

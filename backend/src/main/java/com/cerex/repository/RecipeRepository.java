package com.cerex.repository;

import com.cerex.domain.Recipe;
import com.cerex.domain.Recipe.DifficultyLevel;
import com.cerex.domain.Recipe.RecipeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Recipe} entities.
 *
 * <p>Provides data access for the recipe domain including:
 * <ul>
 *   <li>Standard CRUD via JpaRepository</li>
 *   <li>Dynamic filtering via JpaSpecificationExecutor</li>
 *   <li>Cultural/geographic filter queries (by continent, country, culture)</li>
 *   <li>Full-text search (PostgreSQL tsvector)</li>
 *   <li>Trending and popularity ranking with native SQL</li>
 *   <li>Atomic counter updates for engagement metrics</li>
 * </ul>
 *
 * <p>All queries on the public feed target status = 'PUBLISHED' and deleted_at IS NULL
 * (enforced by the {@code @Where} clause on the entity).
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID>,
                                          JpaSpecificationExecutor<Recipe> {

    // ─────────────────────────────────────────────────────────────
    // Simple Derived Queries
    // ─────────────────────────────────────────────────────────────

    /**
     * Find a published recipe by its URL slug.
     * Used for recipe detail page resolution.
     */
    Optional<Recipe> findBySlugAndStatus(String slug, RecipeStatus status);

    /**
     * Find a recipe by ID only if it belongs to the given author.
     * Used for authorization checks.
     */
    Optional<Recipe> findByIdAndAuthorId(UUID id, UUID authorId);

    /**
     * Check whether a slug is already taken (for unique slug generation).
     */
    boolean existsBySlug(String slug);

    /**
     * Check ownership — used in security expressions.
     */
    boolean existsByIdAndAuthorId(UUID id, UUID authorId);

    /**
     * List all recipes with a given status, newest first.
     */
    Page<Recipe> findByStatus(RecipeStatus status, Pageable pageable);

    /**
     * Count recipes published by a specific author.
     */
    long countByAuthorIdAndStatus(UUID authorId, RecipeStatus status);

    // ─────────────────────────────────────────────────────────────
    // Author / Feed Queries
    // ─────────────────────────────────────────────────────────────

    /**
     * Get all published recipes by a specific author, newest first.
     * Used for public profile pages.
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.authorId = :authorId
          AND r.status = 'PUBLISHED'
        ORDER BY r.publishedAt DESC
        """)
    Page<Recipe> findPublishedByAuthor(@Param("authorId") UUID authorId, Pageable pageable);

    /**
     * Get all recipes (any status) by the current user, newest first.
     * Used for the author's "My Recipes" dashboard.
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.authorId = :authorId
        ORDER BY r.updatedAt DESC
        """)
    Page<Recipe> findAllByAuthor(@Param("authorId") UUID authorId, Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Cultural / Geographic Filtering
    // ─────────────────────────────────────────────────────────────

    /**
     * Filter published recipes by continent, with optional country and dietary filters.
     * Supports the main cultural exploration feature.
     *
     * @param continentId     required continent filter
     * @param countryId       optional country filter (pass null to skip)
     * @param cultureId       optional culture filter (pass null to skip)
     * @param isVegan         optional dietary filter (pass null to skip)
     * @param isGlutenFree    optional dietary filter
     * @param isHalal         optional dietary filter
     * @param pageable        pagination and sort
     */
    @Query("""
        SELECT r FROM Recipe r
        JOIN FETCH r.author a
        WHERE r.status = 'PUBLISHED'
          AND r.continentId = :continentId
          AND (:countryId IS NULL  OR r.countryId  = :countryId)
          AND (:cultureId IS NULL  OR r.cultureId  = :cultureId)
          AND (:isVegan IS NULL    OR r.isVegan     = :isVegan)
          AND (:isGlutenFree IS NULL OR r.isGlutenFree = :isGlutenFree)
          AND (:isHalal IS NULL    OR r.isHalal     = :isHalal)
        """)
    Page<Recipe> findByCulturalFilters(
        @Param("continentId")  UUID continentId,
        @Param("countryId")    UUID countryId,
        @Param("cultureId")    UUID cultureId,
        @Param("isVegan")      Boolean isVegan,
        @Param("isGlutenFree") Boolean isGlutenFree,
        @Param("isHalal")      Boolean isHalal,
        Pageable pageable
    );

    /**
     * Find published recipes by country ISO code.
     * Joins through the countries table to allow filtering by ISO code string.
     */
    @Query(value = """
        SELECT r.* FROM recipes_schema.recipes r
        JOIN geo_schema.countries c ON c.id = r.country_id
        WHERE r.status = 'PUBLISHED'
          AND r.deleted_at IS NULL
          AND c.iso_code = :isoCode
        ORDER BY r.avg_rating DESC, r.view_count DESC
        """, nativeQuery = true)
    Page<Recipe> findByCountryIsoCode(@Param("isoCode") String isoCode, Pageable pageable);

    /**
     * Find published recipes by continent code.
     */
    @Query(value = """
        SELECT r.* FROM recipes_schema.recipes r
        JOIN geo_schema.continents cont ON cont.id = r.continent_id
        WHERE r.status = 'PUBLISHED'
          AND r.deleted_at IS NULL
          AND cont.code = :continentCode
        ORDER BY r.avg_rating DESC, r.view_count DESC
        """, nativeQuery = true)
    Page<Recipe> findByContinentCode(@Param("continentCode") String continentCode, Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Full-Text Search (PostgreSQL native)
    // ─────────────────────────────────────────────────────────────

    /**
     * Full-text search using PostgreSQL tsvector.
     * Searches across title, description, and tags.
     * Ranks results by text relevance using ts_rank.
     *
     * @param searchQuery the search terms (e.g., "spicy chicken curry")
     */
    @Query(value = """
        SELECT r.*,
               ts_rank(
                 to_tsvector('simple',
                   coalesce(r.title,'') || ' ' ||
                   coalesce(r.description,'') || ' ' ||
                   array_to_string(r.tags, ' ')
                 ),
                 plainto_tsquery('simple', :searchQuery)
               ) AS rank
        FROM recipes_schema.recipes r
        WHERE r.status = 'PUBLISHED'
          AND r.deleted_at IS NULL
          AND to_tsvector('simple',
                coalesce(r.title,'') || ' ' ||
                coalesce(r.description,'') || ' ' ||
                array_to_string(r.tags, ' ')
              ) @@ plainto_tsquery('simple', :searchQuery)
        ORDER BY rank DESC, r.avg_rating DESC
        """, nativeQuery = true)
    Page<Object[]> fullTextSearch(@Param("searchQuery") String searchQuery, Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Difficulty & Time Filters
    // ─────────────────────────────────────────────────────────────

    /**
     * Find published recipes by difficulty level with optional time constraint.
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.status = 'PUBLISHED'
          AND (:difficultyLevel IS NULL OR r.difficultyLevel = :difficultyLevel)
          AND (:maxTimeMinutes IS NULL
               OR (r.prepTimeMinutes + r.cookTimeMinutes + r.restTimeMinutes) <= :maxTimeMinutes)
          AND (:minRating IS NULL OR r.avgRating >= :minRating)
        """)
    Page<Recipe> findByDifficultyAndTime(
        @Param("difficultyLevel") DifficultyLevel difficultyLevel,
        @Param("maxTimeMinutes")  Integer maxTimeMinutes,
        @Param("minRating")       BigDecimal minRating,
        Pageable pageable
    );

    // ─────────────────────────────────────────────────────────────
    // Trending & Popularity
    // ─────────────────────────────────────────────────────────────

    /**
     * Find trending recipes for a continent using a weighted engagement score.
     *
     * <p>Trending score formula:
     * <pre>
     *   score = (avg_rating × 0.4)
     *         + (log10(view_count + 1) × 0.3)
     *         + (recent_orders_7d × 0.3)
     * </pre>
     *
     * @param continentId the continent to scope the query to
     * @param limit       maximum number of results (typically 10-20)
     */
    @Query(value = """
        SELECT r.*,
               (r.avg_rating * 0.4
                + LOG(r.view_count + 1) * 0.3
                + COALESCE(recent.order_count, 0) * 0.3) AS trending_score
        FROM recipes_schema.recipes r
        LEFT JOIN (
            SELECT oi.recipe_id, COUNT(*) AS order_count
            FROM orders_schema.order_items oi
            JOIN orders_schema.orders o ON o.id = oi.order_id
            WHERE o.created_at >= NOW() - INTERVAL '7 days'
              AND o.status != 'CANCELLED'
            GROUP BY oi.recipe_id
        ) recent ON recent.recipe_id = r.id
        WHERE r.status = 'PUBLISHED'
          AND r.deleted_at IS NULL
          AND r.continent_id = :continentId
        ORDER BY trending_score DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTrendingByContinent(
        @Param("continentId") UUID continentId,
        @Param("limit") int limit
    );

    /**
     * Find globally trending recipes (across all continents).
     */
    @Query(value = """
        SELECT r.*,
               (r.avg_rating * 0.4
                + LOG(r.view_count + 1) * 0.3
                + COALESCE(recent.order_count, 0) * 0.3) AS trending_score
        FROM recipes_schema.recipes r
        LEFT JOIN (
            SELECT oi.recipe_id, COUNT(*) AS order_count
            FROM orders_schema.order_items oi
            JOIN orders_schema.orders o ON o.id = oi.order_id
            WHERE o.created_at >= NOW() - INTERVAL '7 days'
              AND o.status != 'CANCELLED'
            GROUP BY oi.recipe_id
        ) recent ON recent.recipe_id = r.id
        WHERE r.status = 'PUBLISHED'
          AND r.deleted_at IS NULL
          AND r.published_at >= NOW() - INTERVAL '30 days'
        ORDER BY trending_score DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findGloballyTrending(@Param("limit") int limit);

    /**
     * Find most popular recipes by view count (all time).
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.status = 'PUBLISHED'
          AND (:categoryId IS NULL OR r.categoryId = :categoryId)
        ORDER BY r.viewCount DESC
        """)
    Page<Recipe> findMostPopular(
        @Param("categoryId") UUID categoryId,
        Pageable pageable
    );

    /**
     * Find trending recipes globally, ranked by a weighted engagement score.
     * Used by the recommendation engine and daily discovery feed.
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.status = 'PUBLISHED'
        ORDER BY (r.avgRating * 0.4 + r.viewCount * 0.3 + r.likeCount * 0.3) DESC
        """)
    Page<Recipe> findTrendingRecipes(Pageable pageable);

    /**
     * Find published recipes by continent ID.
     * Used by the content-based similarity engine.
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.continentId = :continentId
          AND r.status = :status
        ORDER BY r.avgRating DESC
        """)
    Page<Recipe> findByContinentAndStatus(
        @Param("continentId") UUID continentId,
        @Param("status") RecipeStatus status,
        Pageable pageable
    );

    // ─────────────────────────────────────────────────────────────
    // Recommendation Support
    // ─────────────────────────────────────────────────────────────

    /**
     * Find top-rated recipes from users that a given user follows.
     * Used as the base for the social recommendation layer.
     *
     * @param userId      the user whose followed authors' recipes to fetch
     * @param excludedIds list of recipe IDs already seen (to avoid repetition)
     */
    @Query(value = """
        SELECT r.*
        FROM recipes_schema.recipes r
        JOIN social_schema.follows f ON f.followee_id = r.author_id
        WHERE f.follower_id = :userId
          AND r.status = 'PUBLISHED'
          AND r.deleted_at IS NULL
          AND r.id NOT IN (:excludedIds)
        ORDER BY r.avg_rating DESC, r.published_at DESC
        LIMIT 20
        """, nativeQuery = true)
    List<Recipe> findFromFollowedAuthors(
        @Param("userId") UUID userId,
        @Param("excludedIds") List<UUID> excludedIds
    );

    /**
     * Find top-rated recipes by similar users (for collaborative filtering).
     * Called with pre-computed similar user IDs from the AI service.
     *
     * @param similarUserIds  list of user IDs with similar taste profiles
     * @param excludedIds     recipe IDs to exclude (already seen)
     * @param limit           maximum results
     */
    @Query(value = """
        SELECT r.*, AVG(rv.overall_rating) AS similar_user_avg_rating
        FROM recipes_schema.recipes r
        JOIN social_schema.reviews rv ON rv.entity_id = r.id
            AND rv.entity_type = 'RECIPE'
            AND rv.reviewer_id = ANY(:similarUserIds)
            AND rv.overall_rating >= 4
        WHERE r.status = 'PUBLISHED'
          AND r.deleted_at IS NULL
          AND r.id != ALL(:excludedIds)
        GROUP BY r.id
        ORDER BY similar_user_avg_rating DESC, r.avg_rating DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopRatedBySimilarUsers(
        @Param("similarUserIds") UUID[] similarUserIds,
        @Param("excludedIds")    UUID[] excludedIds,
        @Param("limit")          int limit
    );

    // ─────────────────────────────────────────────────────────────
    // Premium & AI-Generated
    // ─────────────────────────────────────────────────────────────

    /**
     * Find recently published AI-generated recipes.
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.status = 'PUBLISHED'
          AND r.isAiGenerated = true
        ORDER BY r.publishedAt DESC
        """)
    Page<Recipe> findAiGenerated(Pageable pageable);

    // ─────────────────────────────────────────────────────────────
    // Moderation Queries
    // ─────────────────────────────────────────────────────────────

    /**
     * Find all recipes in PENDING_REVIEW status, oldest first.
     * Used in the moderation dashboard.
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.status = 'PENDING_REVIEW'
        ORDER BY r.createdAt ASC
        """)
    Page<Recipe> findPendingReview(Pageable pageable);

    /**
     * Find recipes submitted for review before a given timestamp.
     * Used to detect stale moderation queues.
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.status = 'PENDING_REVIEW'
          AND r.updatedAt < :before
        ORDER BY r.updatedAt ASC
        """)
    List<Recipe> findPendingReviewBefore(@Param("before") Instant before);

    // ─────────────────────────────────────────────────────────────
    // Atomic Engagement Counter Updates
    // ─────────────────────────────────────────────────────────────

    /**
     * Atomically increment the view counter for a recipe.
     * Uses a direct UPDATE statement to avoid loading the full entity.
     * Called asynchronously on every recipe page view.
     *
     * @param id the recipe ID to increment
     */
    @Modifying
    @Transactional
    @Query("UPDATE Recipe r SET r.viewCount = r.viewCount + 1 WHERE r.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    /**
     * Atomically adjust the like counter (delta = +1 or -1).
     *
     * @param id    the recipe ID
     * @param delta +1 to add a like, -1 to remove
     */
    @Modifying
    @Transactional
    @Query("UPDATE Recipe r SET r.likeCount = r.likeCount + :delta WHERE r.id = :id")
    void updateLikeCount(@Param("id") UUID id, @Param("delta") int delta);

    /**
     * Atomically adjust the save counter.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Recipe r SET r.saveCount = r.saveCount + :delta WHERE r.id = :id")
    void updateSaveCount(@Param("id") UUID id, @Param("delta") int delta);

    /**
     * Atomically increment the order counter when a recipe is ordered.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Recipe r SET r.orderCount = r.orderCount + 1 WHERE r.id = :id")
    void incrementOrderCount(@Param("id") UUID id);

    /**
     * Atomically increment the share counter.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Recipe r SET r.shareCount = r.shareCount + 1 WHERE r.id = :id")
    void incrementShareCount(@Param("id") UUID id);

    /**
     * Update the rating aggregate after a new review is submitted.
     * Recomputes average from the stored count (avoids full table scan).
     *
     * @param id        the recipe ID
     * @param newRating the rating submitted by the reviewer (1-5)
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE Recipe r
        SET r.avgRating   = ((r.avgRating * r.ratingCount) + :newRating) / (r.ratingCount + 1),
            r.ratingCount = r.ratingCount + 1
        WHERE r.id = :id
        """)
    void updateRatingAggregate(@Param("id") UUID id, @Param("newRating") int newRating);

    // ─────────────────────────────────────────────────────────────
    // Analytics Queries
    // ─────────────────────────────────────────────────────────────

    /**
     * Count published recipes grouped by continent.
     * Used for the global map visualization.
     */
    @Query(value = """
        SELECT cont.code AS continent_code,
               cont.name AS continent_name,
               COUNT(r.id) AS recipe_count
        FROM recipes_schema.recipes r
        JOIN geo_schema.continents cont ON cont.id = r.continent_id
        WHERE r.status = 'PUBLISHED'
          AND r.deleted_at IS NULL
        GROUP BY cont.code, cont.name
        ORDER BY recipe_count DESC
        """, nativeQuery = true)
    List<Object[]> countPublishedByContinent();

    /**
     * Count published recipes grouped by country.
     */
    @Query(value = """
        SELECT co.iso_code,
               co.name AS country_name,
               co.flag_emoji,
               COUNT(r.id) AS recipe_count
        FROM recipes_schema.recipes r
        JOIN geo_schema.countries co ON co.id = r.country_id
        WHERE r.status = 'PUBLISHED'
          AND r.deleted_at IS NULL
          AND r.continent_id = :continentId
        GROUP BY co.iso_code, co.name, co.flag_emoji
        ORDER BY recipe_count DESC
        """, nativeQuery = true)
    List<Object[]> countPublishedByCountry(@Param("continentId") UUID continentId);

    /**
     * Get recently published recipes (for the "New" feed).
     */
    @Query("""
        SELECT r FROM Recipe r
        WHERE r.status = 'PUBLISHED'
          AND r.publishedAt >= :since
        ORDER BY r.publishedAt DESC
        """)
    Page<Recipe> findRecentlyPublished(@Param("since") Instant since, Pageable pageable);

    /**
     * Find all recipe IDs for a batch Elasticsearch re-index job.
     */
    @Query(value = """
        SELECT id FROM recipes_schema.recipes
        WHERE status = 'PUBLISHED'
          AND deleted_at IS NULL
        ORDER BY updated_at DESC
        """, nativeQuery = true)
    List<UUID> findAllPublishedIds();
}

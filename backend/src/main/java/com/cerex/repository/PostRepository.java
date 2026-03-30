package com.cerex.repository;

import com.cerex.domain.Post;
import com.cerex.domain.Post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for social Post operations.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    // ─────────────────────────────────────────────────────────
    // Feed Queries
    // ─────────────────────────────────────────────────────────

    /**
     * Get personalized feed: posts from users I follow, ordered by boost score.
     */
    @Query("""
        SELECT p FROM Post p
        WHERE p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
          AND p.authorId IN (
              SELECT f.followeeId FROM Follow f WHERE f.followerId = :userId
          )
        ORDER BY p.boostScore DESC, p.createdAt DESC
        """)
    Page<Post> findFollowingFeed(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Explore / Discover feed: trending posts from all users.
     */
    @Query("""
        SELECT p FROM Post p
        WHERE p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
        ORDER BY p.boostScore DESC
        """)
    Page<Post> findTrendingPosts(Pageable pageable);

    /**
     * Get posts by a specific author.
     */
    @Query("SELECT p FROM Post p WHERE p.authorId = :authorId AND p.status = 'ACTIVE' AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorId(@Param("authorId") UUID authorId, Pageable pageable);

    /**
     * Get posts linked to a recipe.
     */
    @Query("SELECT p FROM Post p WHERE p.recipeId = :recipeId AND p.status = 'ACTIVE' AND p.deletedAt IS NULL ORDER BY p.boostScore DESC")
    Page<Post> findByRecipeId(@Param("recipeId") UUID recipeId, Pageable pageable);

    /**
     * Search posts by hashtag.
     */
    @Query("""
        SELECT p FROM Post p
        JOIN p.hashtags h
        WHERE p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
          AND LOWER(h) = LOWER(:hashtag)
        ORDER BY p.boostScore DESC
        """)
    Page<Post> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    /**
     * Get recipe reproduction posts.
     */
    @Query("SELECT p FROM Post p WHERE p.postType = 'RECIPE_REPRODUCTION' AND p.recipeId = :recipeId AND p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    Page<Post> findReproductions(@Param("recipeId") UUID recipeId, Pageable pageable);

    /**
     * Get featured posts.
     */
    @Query("SELECT p FROM Post p WHERE p.isFeatured = true AND p.status = 'ACTIVE' AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Post> findFeaturedPosts(Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // Atomic Counter Updates
    // ─────────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :postId")
    void decrementLikeCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.shareCount = p.shareCount + 1 WHERE p.id = :postId")
    void incrementShareCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.reproduceCount = p.reproduceCount + 1 WHERE p.id = :postId")
    void incrementReproduceCount(@Param("postId") UUID postId);

    // ─────────────────────────────────────────────────────────
    // Analytics
    // ─────────────────────────────────────────────────────────

    long countByAuthorIdAndStatus(UUID authorId, PostStatus status);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :since AND p.status = 'ACTIVE'")
    long countNewPostsSince(@Param("since") Instant since);
}

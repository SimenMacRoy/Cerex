package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

/**
 * Domain entity representing a social post on the Cerex culinary network.
 *
 * <p>A post can be:
 * <ul>
 *   <li>A recipe share / "J'ai reproduit cette recette"</li>
 *   <li>A food photo / culinary tip</li>
 *   <li>A restaurant review / recommendation</li>
 *   <li>A cooking video / reel</li>
 * </ul>
 *
 * <p>Posts participate in the "Booster" ranking system
 * (engagement score determines visibility in feed).
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "posts",
    schema = "social_schema",
    indexes = {
        @Index(name = "idx_posts_author_id",   columnList = "author_id"),
        @Index(name = "idx_posts_post_type",   columnList = "post_type"),
        @Index(name = "idx_posts_recipe_id",   columnList = "recipe_id"),
        @Index(name = "idx_posts_created_at",  columnList = "created_at"),
        @Index(name = "idx_posts_boost_score", columnList = "boost_score"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"comments", "mediaUrls", "hashtags"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 30)
    @Builder.Default
    private PostType postType = PostType.GENERAL;

    @Column(name = "recipe_id")
    private UUID recipeId;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Size(max = 5000)
    @Column(name = "content", length = 5000)
    private String content;

    @Size(max = 200)
    @Column(name = "title", length = 200)
    private String title;

    // ─────────────────────────────────────────────────────────
    // Media
    // ─────────────────────────────────────────────────────────

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "post_media",
        schema = "social_schema",
        joinColumns = @JoinColumn(name = "post_id")
    )
    @Column(name = "media_url", length = 500)
    @Builder.Default
    private List<String> mediaUrls = new ArrayList<>();

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    // ─────────────────────────────────────────────────────────
    // Engagement Metrics
    // ─────────────────────────────────────────────────────────

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "share_count")
    @Builder.Default
    private Integer shareCount = 0;

    @Column(name = "reproduce_count")
    @Builder.Default
    private Integer reproduceCount = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "save_count")
    @Builder.Default
    private Integer saveCount = 0;

    /**
     * Booster ranking score.
     * Formula: (likes * 2) + (comments * 3) + (shares * 4) + (reproduces * 5) - decay(age)
     */
    @Column(name = "boost_score")
    @Builder.Default
    private Double boostScore = 0.0;

    // ─────────────────────────────────────────────────────────
    // Hashtags & Location
    // ─────────────────────────────────────────────────────────

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "post_hashtags",
        schema = "social_schema",
        joinColumns = @JoinColumn(name = "post_id")
    )
    @Column(name = "hashtag", length = 100)
    @Builder.Default
    private Set<String> hashtags = new HashSet<>();

    @Column(name = "location_name", length = 200)
    private String locationName;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // ─────────────────────────────────────────────────────────
    // Moderation
    // ─────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.ACTIVE;

    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "report_count")
    @Builder.Default
    private Integer reportCount = 0;

    // ─────────────────────────────────────────────────────────
    // Reproduction metadata
    // ─────────────────────────────────────────────────────────

    @Column(name = "original_post_id")
    private UUID originalPostId;

    @Column(name = "reproduction_rating")
    private Integer reproductionRating; // 1-5 how well they reproduced

    @Size(max = 1000)
    @Column(name = "reproduction_notes", length = 1000)
    private String reproductionNotes;

    // ─────────────────────────────────────────────────────────
    // Relationships
    // ─────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Audit
    // ─────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ─────────────────────────────────────────────────────────
    // Enums
    // ─────────────────────────────────────────────────────────

    public enum PostType {
        GENERAL,
        RECIPE_SHARE,
        RECIPE_REPRODUCTION,
        FOOD_PHOTO,
        COOKING_TIP,
        RESTAURANT_REVIEW,
        VIDEO,
        REEL,
        STORY
    }

    public enum PostStatus {
        ACTIVE,
        HIDDEN,
        FLAGGED,
        DELETED
    }

    // ─────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────

    public void recalculateBoostScore() {
        double rawScore = (likeCount * 2.0) + (commentCount * 3.0) +
                          (shareCount * 4.0) + (reproduceCount * 5.0);

        // Time decay: score halves every 24 hours
        if (createdAt != null) {
            long hoursAge = (Instant.now().toEpochMilli() - createdAt.toEpochMilli()) / (3600 * 1000);
            double decay = Math.pow(0.5, hoursAge / 24.0);
            this.boostScore = rawScore * decay;
        } else {
            this.boostScore = rawScore;
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
        this.commentCount = comments.size();
        recalculateBoostScore();
    }

    public void incrementLikes() {
        this.likeCount++;
        recalculateBoostScore();
    }

    public void decrementLikes() {
        this.likeCount = Math.max(0, this.likeCount - 1);
        recalculateBoostScore();
    }

    public void incrementShares() {
        this.shareCount++;
        recalculateBoostScore();
    }

    public void incrementReproductions() {
        this.reproduceCount++;
        recalculateBoostScore();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.status = PostStatus.DELETED;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}

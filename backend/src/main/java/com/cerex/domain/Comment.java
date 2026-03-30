package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity for comments on social posts.
 *
 * <p>Supports nested replies (parent_comment_id) for threaded discussions.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "comments",
    schema = "social_schema",
    indexes = {
        @Index(name = "idx_comments_post_id",   columnList = "post_id"),
        @Index(name = "idx_comments_author_id", columnList = "author_id"),
        @Index(name = "idx_comments_parent_id", columnList = "parent_comment_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"post", "replies"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    // ─────────────────────────────────────────────────────────
    // Nested Replies
    // ─────────────────────────────────────────────────────────

    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @OneToMany(mappedBy = "parentCommentId", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Engagement
    // ─────────────────────────────────────────────────────────

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "reply_count")
    @Builder.Default
    private Integer replyCount = 0;

    // ─────────────────────────────────────────────────────────
    // Moderation
    // ─────────────────────────────────────────────────────────

    @Column(name = "is_edited")
    @Builder.Default
    private Boolean isEdited = false;

    @Column(name = "is_flagged")
    @Builder.Default
    private Boolean isFlagged = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ─────────────────────────────────────────────────────────
    // Audit
    // ─────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ─────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────

    public void edit(String newContent) {
        this.content = newContent;
        this.isEdited = true;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.content = "[Commentaire supprimé]";
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isReply() {
        return this.parentCommentId != null;
    }
}

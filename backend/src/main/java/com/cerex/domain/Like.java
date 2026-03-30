package com.cerex.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity for "likes" on posts, comments, and recipes.
 *
 * <p>Uses a generic target model with entity_type + entity_id.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "likes",
    schema = "social_schema",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_like_unique",
            columnNames = {"user_id", "entity_type", "entity_id"}
        )
    },
    indexes = {
        @Index(name = "idx_likes_user_id",    columnList = "user_id"),
        @Index(name = "idx_likes_entity",     columnList = "entity_type, entity_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private LikeableEntity entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", length = 20)
    @Builder.Default
    private ReactionType reactionType = ReactionType.LIKE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum LikeableEntity {
        POST,
        COMMENT,
        RECIPE,
        RESTAURANT_REVIEW
    }

    public enum ReactionType {
        LIKE,
        LOVE,
        DELICIOUS,
        FIRE,
        CLAP
    }
}

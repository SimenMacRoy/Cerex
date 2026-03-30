package com.cerex.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity for user follows (follower → followee).
 *
 * <p>Implements the "following" social graph between users.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "follows",
    schema = "social_schema",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_follow_pair",
            columnNames = {"follower_id", "followee_id"}
        )
    },
    indexes = {
        @Index(name = "idx_follows_follower_id", columnList = "follower_id"),
        @Index(name = "idx_follows_followee_id", columnList = "followee_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Column(name = "followee_id", nullable = false)
    private UUID followeeId;

    @Column(name = "is_muted")
    @Builder.Default
    private Boolean isMuted = false;

    @Column(name = "is_close_friend")
    @Builder.Default
    private Boolean isCloseFriend = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

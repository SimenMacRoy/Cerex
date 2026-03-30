package com.cerex.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity tracking shares of posts and recipes.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "shares",
    schema = "social_schema",
    indexes = {
        @Index(name = "idx_shares_user_id",     columnList = "user_id"),
        @Index(name = "idx_shares_entity",      columnList = "entity_type, entity_id"),
        @Index(name = "idx_shares_created_at",  columnList = "created_at"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private ShareableEntity entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "share_platform", length = 30)
    @Builder.Default
    private SharePlatform sharePlatform = SharePlatform.INTERNAL;

    @Column(name = "message", length = 500)
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum ShareableEntity {
        POST,
        RECIPE,
        RESTAURANT
    }

    public enum SharePlatform {
        INTERNAL,
        WHATSAPP,
        FACEBOOK,
        TWITTER,
        INSTAGRAM,
        TIKTOK,
        EMAIL,
        LINK_COPY
    }
}

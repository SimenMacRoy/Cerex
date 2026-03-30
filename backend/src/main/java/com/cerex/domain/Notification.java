package com.cerex.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity for "Notifications" delivered to users.
 *
 * <p>Covers push, in-app, and email notification tracking.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "notifications",
    schema = "social_schema",
    indexes = {
        @Index(name = "idx_notif_recipient_id",  columnList = "recipient_id"),
        @Index(name = "idx_notif_is_read",       columnList = "is_read"),
        @Index(name = "idx_notif_created_at",    columnList = "created_at"),
        @Index(name = "idx_notif_type",          columnList = "notification_type"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "sender_id")
    private UUID senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "entity_type", length = 30)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum NotificationType {
        FOLLOW,
        LIKE_POST,
        LIKE_COMMENT,
        LIKE_RECIPE,
        COMMENT,
        REPLY,
        SHARE,
        REPRODUCE,
        MENTION,
        ORDER_STATUS,
        RECIPE_APPROVED,
        RECIPE_REJECTED,
        RESTAURANT_VERIFIED,
        BADGE_EARNED,
        SUBSCRIPTION_EXPIRING,
        SYSTEM
    }

    // ─────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────

    public void markAsRead() {
        this.isRead = true;
        this.readAt = Instant.now();
    }
}

package com.cerex.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for notification responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private UUID id;
    private String notificationType;
    private String title;
    private String message;
    private String entityType;
    private UUID entityId;
    private String actionUrl;
    private String imageUrl;
    private Boolean isRead;
    private Instant createdAt;

    // Sender info
    private UUID senderId;
    private String senderDisplayName;
    private String senderAvatarUrl;
}

package com.cerex.service;

import com.cerex.domain.Notification;
import com.cerex.dto.social.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Service for real-time order tracking and notifications via WebSocket.
 *
 * <p>Publishes to STOMP topics:
 * <ul>
 *   <li>{@code /topic/orders/{orderId}} — order status updates + GPS</li>
 *   <li>{@code /topic/kitchen/{restaurantId}} — new orders for kitchen display</li>
 *   <li>{@code /user/{userId}/queue/notifications} — personal notifications</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingService {

    private final SimpMessagingTemplate messagingTemplate;

    // ─────────────────────────────────────────────────────────
    // Order Status Updates
    // ─────────────────────────────────────────────────────────

    /**
     * Send order status update to all subscribers.
     */
    public void sendOrderStatusUpdate(UUID orderId, String status, String message) {
        OrderStatusUpdate update = OrderStatusUpdate.builder()
            .orderId(orderId)
            .status(status)
            .message(message)
            .timestamp(Instant.now())
            .build();

        messagingTemplate.convertAndSend("/topic/orders/" + orderId, update);
        log.info("Order status update sent: [{}] → {}", orderId, status);
    }

    /**
     * Send delivery GPS location update.
     */
    public void sendDeliveryLocationUpdate(UUID orderId, BigDecimal latitude, BigDecimal longitude,
                                            Integer estimatedMinutes) {
        DeliveryLocationUpdate location = DeliveryLocationUpdate.builder()
            .orderId(orderId)
            .latitude(latitude)
            .longitude(longitude)
            .estimatedMinutesRemaining(estimatedMinutes)
            .timestamp(Instant.now())
            .build();

        messagingTemplate.convertAndSend("/topic/orders/" + orderId + "/location", location);
        log.debug("Delivery location update for order [{}]: ({}, {})", orderId, latitude, longitude);
    }

    // ─────────────────────────────────────────────────────────
    // Kitchen Display System
    // ─────────────────────────────────────────────────────────

    /**
     * Notify restaurant kitchen of a new order.
     */
    public void sendNewOrderToKitchen(UUID restaurantId, UUID orderId, String orderNumber,
                                       String orderType, int itemCount) {
        KitchenOrderNotification notification = KitchenOrderNotification.builder()
            .orderId(orderId)
            .orderNumber(orderNumber)
            .orderType(orderType)
            .itemCount(itemCount)
            .timestamp(Instant.now())
            .build();

        messagingTemplate.convertAndSend("/topic/kitchen/" + restaurantId, notification);
        log.info("New order notification sent to kitchen [{}]: {}", restaurantId, orderNumber);
    }

    // ─────────────────────────────────────────────────────────
    // User Notifications
    // ─────────────────────────────────────────────────────────

    /**
     * Push a notification to a specific user in real-time.
     */
    public void sendNotificationToUser(UUID userId, NotificationDTO notification) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        );
        log.debug("Notification sent to user [{}]: {}", userId, notification.getNotificationType());
    }

    // ─────────────────────────────────────────────────────────
    // DTOs for WebSocket messages
    // ─────────────────────────────────────────────────────────

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderStatusUpdate {
        private UUID orderId;
        private String status;
        private String message;
        private Instant timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeliveryLocationUpdate {
        private UUID orderId;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Integer estimatedMinutesRemaining;
        private Instant timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KitchenOrderNotification {
        private UUID orderId;
        private String orderNumber;
        private String orderType;
        private int itemCount;
        private Instant timestamp;
    }
}

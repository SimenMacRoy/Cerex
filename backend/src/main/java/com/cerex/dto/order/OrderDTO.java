package com.cerex.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for order details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private UUID id;
    private String orderNumber;
    private String orderType;
    private String status;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal taxAmount;
    private BigDecimal tipAmount;
    private BigDecimal totalAmount;
    private String currencyCode;

    // Payment
    private String paymentStatus;
    private String paymentMethod;

    // Delivery
    private Map<String, Object> deliveryAddress;
    private String deliveryNotes;
    private Instant estimatedDelivery;
    private Instant actualDelivery;

    // Items
    private List<OrderItemDTO> items;

    // Timestamps
    private Instant confirmedAt;
    private Instant preparingAt;
    private Instant readyAt;
    private Instant deliveredAt;
    private Instant cancelledAt;
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private UUID id;
        private UUID recipeId;
        private String recipeTitle;
        private String recipeThumbnailUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String specialInstructions;
    }
}

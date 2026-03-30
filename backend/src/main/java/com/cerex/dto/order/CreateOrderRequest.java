package com.cerex.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for placing a new order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Restaurant ID is required")
    private UUID restaurantId;

    @NotNull(message = "Order type is required")
    private String orderType;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItemRequest> items;

    // Delivery details
    private Map<String, Object> deliveryAddress;
    private String deliveryNotes;

    // Payment
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String promoCode;

    @DecimalMin("0.00")
    private BigDecimal tipAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull private UUID recipeId;
        @NotNull @Min(1) private Integer quantity;
        private String specialInstructions;
    }
}

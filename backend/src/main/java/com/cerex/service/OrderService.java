package com.cerex.service;

import com.cerex.domain.Order;
import com.cerex.domain.Order.OrderStatus;
import com.cerex.domain.Order.OrderType;
import com.cerex.domain.OrderItem;
import com.cerex.domain.Recipe;
import com.cerex.dto.order.CreateOrderRequest;
import com.cerex.dto.order.OrderDTO;
import com.cerex.exception.BusinessException;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.exception.UnauthorizedException;
import com.cerex.repository.OrderRepository;
import com.cerex.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for order lifecycle management.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RecipeRepository recipeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05"); // 5%
    private static final BigDecimal DEFAULT_DELIVERY_FEE = new BigDecimal("2.90");

    /**
     * Place a new order.
     */
    @Transactional
    public OrderDTO createOrder(UUID userId, CreateOrderRequest request) {
        Order order = Order.builder()
            .userId(userId)
            .restaurantId(request.getRestaurantId())
            .orderNumber(generateOrderNumber())
            .orderType(OrderType.valueOf(request.getOrderType()))
            .status(OrderStatus.PENDING)
            .paymentMethod(request.getPaymentMethod())
            .deliveryAddress(request.getDeliveryAddress())
            .deliveryNotes(request.getDeliveryNotes())
            .promoCode(request.getPromoCode())
            .tipAmount(request.getTipAmount() != null ? request.getTipAmount() : BigDecimal.ZERO)
            .currencyCode("EUR")
            .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Recipe recipe = recipeRepository.findById(itemReq.getRecipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", itemReq.getRecipeId()));

            // In a real system, price comes from the restaurant menu; here we use a placeholder
            BigDecimal unitPrice = new BigDecimal("12.99");
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem item = OrderItem.builder()
                .recipeId(recipe.getId())
                .recipeTitle(recipe.getTitle())
                .recipeThumbnailUrl(recipe.getThumbnailUrl())
                .quantity(itemReq.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .specialInstructions(itemReq.getSpecialInstructions())
                .build();

            order.addItem(item);
            subtotal = subtotal.add(totalPrice);

            // Increment recipe order count
            recipeRepository.incrementOrderCount(recipe.getId());
        }

        // Calculate totals
        order.setSubtotal(subtotal);
        BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE);
        order.setServiceFee(serviceFee);

        BigDecimal deliveryFee = order.getOrderType() == OrderType.DELIVERY
            ? DEFAULT_DELIVERY_FEE : BigDecimal.ZERO;
        order.setDeliveryFee(deliveryFee);

        order.setTotalAmount(order.calculateExpectedTotal());
        order.setEstimatedDelivery(Instant.now().plusSeconds(2700)); // 45 min estimate

        order = orderRepository.save(order);

        log.info("Order placed: {} by user {} — total: {} {}",
            order.getOrderNumber(), userId, order.getTotalAmount(), order.getCurrencyCode());

        // Publish event
        try {
            kafkaTemplate.send("cerex.order.created", order.getId().toString(), order.getId());
        } catch (Exception e) {
            log.warn("Failed to publish order.created event: {}", e.getMessage());
        }

        return mapToDTO(order);
    }

    /**
     * Get order details for the authenticated user.
     */
    public OrderDTO getOrderById(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToDTO(order);
    }

    /**
     * Get order history for the authenticated user.
     */
    public Page<OrderDTO> getOrderHistory(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Cancel an order.
     */
    @Transactional
    public OrderDTO cancelOrder(UUID orderId, UUID userId, String reason) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.isCancellable()) {
            throw new BusinessException("ORDER_NOT_CANCELLABLE",
                "Order cannot be cancelled in current status: " + order.getStatus());
        }

        order.cancel(reason);
        order = orderRepository.save(order);

        log.info("Order cancelled: {} — reason: {}", order.getOrderNumber(), reason);

        try {
            kafkaTemplate.send("cerex.order.status-changed", order.getId().toString(), order.getStatus());
        } catch (Exception e) {
            log.warn("Failed to publish order status change event: {}", e.getMessage());
        }

        return mapToDTO(order);
    }

    /**
     * Confirm an order (restaurant action).
     */
    @Transactional
    public OrderDTO confirmOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.confirm();
        order = orderRepository.save(order);
        log.info("Order confirmed: {}", order.getOrderNumber());
        return mapToDTO(order);
    }

    /**
     * Mark order as preparing.
     */
    @Transactional
    public OrderDTO startPreparing(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.startPreparing();
        order = orderRepository.save(order);
        return mapToDTO(order);
    }

    /**
     * Mark order as ready for pickup/delivery.
     */
    @Transactional
    public OrderDTO markReady(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.markReady();
        order = orderRepository.save(order);
        return mapToDTO(order);
    }

    /**
     * Mark order as delivered.
     */
    @Transactional
    public OrderDTO markDelivered(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.markDelivered();
        order = orderRepository.save(order);
        log.info("Order delivered: {}", order.getOrderNumber());
        return mapToDTO(order);
    }

    // ── Helpers ─────────────────────────────────────────────

    private String generateOrderNumber() {
        String year = String.valueOf(Year.now().getValue());
        String lastNumber = orderRepository.findLastOrderNumber()
            .map(on -> on.substring(on.lastIndexOf('-') + 1))
            .orElse("000000");

        int next = Integer.parseInt(lastNumber) + 1;
        return String.format("CRX-%s-%06d", year, next);
    }

    private OrderDTO mapToDTO(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .orderType(order.getOrderType().name())
            .status(order.getStatus().name())
            .subtotal(order.getSubtotal())
            .discountAmount(order.getDiscountAmount())
            .deliveryFee(order.getDeliveryFee())
            .serviceFee(order.getServiceFee())
            .taxAmount(order.getTaxAmount())
            .tipAmount(order.getTipAmount())
            .totalAmount(order.getTotalAmount())
            .currencyCode(order.getCurrencyCode())
            .paymentStatus(order.getPaymentStatus().name())
            .paymentMethod(order.getPaymentMethod())
            .deliveryAddress(order.getDeliveryAddress())
            .deliveryNotes(order.getDeliveryNotes())
            .estimatedDelivery(order.getEstimatedDelivery())
            .actualDelivery(order.getActualDelivery())
            .items(order.getItems().stream().map(item ->
                OrderDTO.OrderItemDTO.builder()
                    .id(item.getId())
                    .recipeId(item.getRecipeId())
                    .recipeTitle(item.getRecipeTitle())
                    .recipeThumbnailUrl(item.getRecipeThumbnailUrl())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .totalPrice(item.getTotalPrice())
                    .specialInstructions(item.getSpecialInstructions())
                    .build()
            ).collect(Collectors.toList()))
            .confirmedAt(order.getConfirmedAt())
            .preparingAt(order.getPreparingAt())
            .readyAt(order.getReadyAt())
            .deliveredAt(order.getDeliveredAt())
            .cancelledAt(order.getCancelledAt())
            .createdAt(order.getCreatedAt())
            .build();
    }
}

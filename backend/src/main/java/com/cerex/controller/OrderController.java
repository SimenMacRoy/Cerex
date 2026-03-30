package com.cerex.controller;

import com.cerex.dto.ApiResponse;
import com.cerex.dto.order.CreateOrderRequest;
import com.cerex.dto.order.OrderDTO;
import com.cerex.security.CerexUserDetails;
import com.cerex.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for order lifecycle management.
 *
 * <p>Base path: {@code /api/v1/orders}
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement, tracking, and management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @AuthenticationPrincipal CerexUserDetails currentUser,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(currentUser.getUserId(), request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(order, "Order placed successfully"));
    }

    @GetMapping
    @Operation(summary = "Get order history for the current user")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrderHistory(
            @AuthenticationPrincipal CerexUserDetails currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<OrderDTO> orders = orderService.getOrderHistory(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal CerexUserDetails currentUser) {
        OrderDTO order = orderService.getOrderById(id, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order (only PENDING or CONFIRMED)")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable UUID id,
            @RequestParam String reason,
            @AuthenticationPrincipal CerexUserDetails currentUser) {
        OrderDTO order = orderService.cancelOrder(id, currentUser.getUserId(), reason);
        return ResponseEntity.ok(ApiResponse.ok(order, "Order cancelled"));
    }

    // ── Restaurant / Admin Endpoints ────────────────────────

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Confirm an order (restaurant)")
    public ResponseEntity<ApiResponse<OrderDTO>> confirmOrder(@PathVariable UUID id) {
        OrderDTO order = orderService.confirmOrder(id);
        return ResponseEntity.ok(ApiResponse.ok(order, "Order confirmed"));
    }

    @PatchMapping("/{id}/prepare")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Mark order as preparing")
    public ResponseEntity<ApiResponse<OrderDTO>> startPreparing(@PathVariable UUID id) {
        OrderDTO order = orderService.startPreparing(id);
        return ResponseEntity.ok(ApiResponse.ok(order, "Order preparation started"));
    }

    @PatchMapping("/{id}/ready")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Mark order as ready for pickup/delivery")
    public ResponseEntity<ApiResponse<OrderDTO>> markReady(@PathVariable UUID id) {
        OrderDTO order = orderService.markReady(id);
        return ResponseEntity.ok(ApiResponse.ok(order, "Order is ready"));
    }

    @PatchMapping("/{id}/delivered")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Mark order as delivered")
    public ResponseEntity<ApiResponse<OrderDTO>> markDelivered(@PathVariable UUID id) {
        OrderDTO order = orderService.markDelivered(id);
        return ResponseEntity.ok(ApiResponse.ok(order, "Order delivered"));
    }
}

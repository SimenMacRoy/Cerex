package com.cerex.service;

import com.cerex.domain.Order;
import com.cerex.domain.Order.OrderStatus;
import com.cerex.domain.Order.OrderType;
import com.cerex.domain.Recipe;
import com.cerex.dto.order.CreateOrderRequest;
import com.cerex.dto.order.OrderDTO;
import com.cerex.exception.BusinessException;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.repository.OrderRepository;
import com.cerex.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for OrderService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private UUID userId;
    private UUID restaurantId;
    private Order testOrder;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();

        testRecipe = Recipe.builder()
            .id(UUID.randomUUID())
            .title("Poulet Yassa")
            .thumbnailUrl("https://cdn.cerex.com/thumb.jpg")
            .build();

        testOrder = Order.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .restaurantId(restaurantId)
            .orderNumber("CRX-2026-000001")
            .orderType(OrderType.DELIVERY)
            .status(OrderStatus.PENDING)
            .subtotal(BigDecimal.valueOf(25.98))
            .serviceFee(BigDecimal.valueOf(1.30))
            .deliveryFee(BigDecimal.valueOf(2.90))
            .totalAmount(BigDecimal.valueOf(30.18))
            .currencyCode("EUR")
            .build();
    }

    @Nested
    @DisplayName("Create Order")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create an order successfully")
        void shouldCreateOrder() {
            // Given
            CreateOrderRequest.OrderItemRequest itemReq = new CreateOrderRequest.OrderItemRequest();
            itemReq.setRecipeId(testRecipe.getId());
            itemReq.setQuantity(2);

            CreateOrderRequest request = CreateOrderRequest.builder()
                .restaurantId(restaurantId)
                .orderType("DELIVERY")
                .paymentMethod("STRIPE")
                .deliveryAddress("123 Rue de Paris")
                .items(List.of(itemReq))
                .build();

            given(recipeRepository.findById(testRecipe.getId())).willReturn(Optional.of(testRecipe));
            given(orderRepository.save(any(Order.class))).willAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(UUID.randomUUID());
                return o;
            });
            given(orderRepository.findLastOrderNumber()).willReturn(Optional.empty());

            // When
            OrderDTO result = orderService.createOrder(userId, request);

            // Then
            assertThat(result).isNotNull();
            then(orderRepository).should().save(any(Order.class));
            then(recipeRepository).should().incrementOrderCount(testRecipe.getId());
        }

        @Test
        @DisplayName("Should reject order with unknown recipe")
        void shouldRejectOrderWithUnknownRecipe() {
            // Given
            UUID unknownRecipeId = UUID.randomUUID();
            CreateOrderRequest.OrderItemRequest itemReq = new CreateOrderRequest.OrderItemRequest();
            itemReq.setRecipeId(unknownRecipeId);
            itemReq.setQuantity(1);

            CreateOrderRequest request = CreateOrderRequest.builder()
                .restaurantId(restaurantId)
                .orderType("DELIVERY")
                .paymentMethod("STRIPE")
                .items(List.of(itemReq))
                .build();

            given(recipeRepository.findById(unknownRecipeId)).willReturn(Optional.empty());
            given(orderRepository.findLastOrderNumber()).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Order Lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("Should confirm a pending order")
        void shouldConfirmPendingOrder() {
            // Given
            given(orderRepository.findById(testOrder.getId())).willReturn(Optional.of(testOrder));
            given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // When
            orderService.confirmOrder(testOrder.getId());

            // Then
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should cancel a pending order")
        void shouldCancelPendingOrder() {
            // Given
            given(orderRepository.findById(testOrder.getId())).willReturn(Optional.of(testOrder));
            given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // When
            orderService.cancelOrder(testOrder.getId(), userId, "Changed my mind");

            // Then
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should not cancel a delivered order")
        void shouldNotCancelDeliveredOrder() {
            // Given
            testOrder.setStatus(OrderStatus.DELIVERED);
            given(orderRepository.findById(testOrder.getId())).willReturn(Optional.of(testOrder));

            // When & Then
            assertThatThrownBy(() -> orderService.cancelOrder(testOrder.getId(), userId, "Too late"))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should follow full order lifecycle")
        void shouldFollowFullLifecycle() {
            // Given
            given(orderRepository.findById(testOrder.getId())).willReturn(Optional.of(testOrder));
            given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

            // When: PENDING → CONFIRMED → PREPARING → READY → DELIVERED
            orderService.confirmOrder(testOrder.getId());
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            orderService.startPreparing(testOrder.getId());
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PREPARING);

            orderService.markReady(testOrder.getId());
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.READY);

            orderService.markDelivered(testOrder.getId());
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }
    }

    @Nested
    @DisplayName("Get Orders")
    class GetOrderTests {

        @Test
        @DisplayName("Should get user orders")
        void shouldGetUserOrders() {
            // Given
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
            given(orderRepository.findByUserId(userId, PageRequest.of(0, 20))).willReturn(orderPage);

            // When
            Page<OrderDTO> result = orderService.getUserOrders(userId, PageRequest.of(0, 20));

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should throw not found for unknown order")
        void shouldThrowNotFoundForUnknownOrder() {
            // Given
            UUID unknownId = UUID.randomUUID();
            given(orderRepository.findById(unknownId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderService.confirmOrder(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

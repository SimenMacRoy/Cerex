package com.cerex.repository;

import com.cerex.domain.Order;
import com.cerex.domain.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Order} entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
        SELECT o FROM Order o
        WHERE o.userId = :userId
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.userId = :userId
          AND o.status = :status
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByUserIdAndStatus(
        @Param("userId") UUID userId,
        @Param("status") OrderStatus status,
        Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.restaurantId = :restaurantId
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByRestaurantId(@Param("restaurantId") UUID restaurantId, Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.restaurantId = :restaurantId
          AND o.status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY')
        ORDER BY o.createdAt ASC
        """)
    List<Order> findActiveOrdersByRestaurant(@Param("restaurantId") UUID restaurantId);

    /**
     * Count orders placed within a time range (for analytics).
     */
    long countByCreatedAtBetween(Instant start, Instant end);

    /**
     * Count orders by status (for dashboard).
     */
    long countByStatus(OrderStatus status);

    /**
     * Get the last order number for generating the next one.
     */
    @Query(value = """
        SELECT order_number FROM orders_schema.orders
        ORDER BY created_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<String> findLastOrderNumber();
}

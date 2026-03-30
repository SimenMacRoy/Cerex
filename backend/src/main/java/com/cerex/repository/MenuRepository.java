package com.cerex.repository;

import com.cerex.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Menu and MenuItem operations.
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    List<Menu> findByRestaurantIdOrderBySortOrderAsc(UUID restaurantId);

    @Query("SELECT m FROM Menu m WHERE m.restaurant.id = :restaurantId AND m.isActive = true ORDER BY m.sortOrder ASC")
    List<Menu> findActiveMenusByRestaurant(@Param("restaurantId") UUID restaurantId);

    void deleteByRestaurantIdAndId(UUID restaurantId, UUID menuId);
}

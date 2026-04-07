package com.cerex.repository;

import com.cerex.domain.GroceryProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for grocery product queries, including ingredient-based lookups.
 */
@Repository
public interface GroceryProductRepository extends JpaRepository<GroceryProduct, UUID> {

    /**
     * Find all in-stock products from active groceries that match any of the given ingredient IDs.
     */
    @Query("""
        SELECT gp FROM GroceryProduct gp
        JOIN FETCH gp.grocery g
        WHERE gp.ingredientId IN :ingredientIds
          AND gp.isInStock = true
          AND g.status = 'ACTIVE'
        ORDER BY gp.price ASC
        """)
    List<GroceryProduct> findInStockByIngredientIds(
        @Param("ingredientIds") List<UUID> ingredientIds
    );

    /**
     * Find products from a specific grocery that match ingredient IDs.
     */
    @Query("""
        SELECT gp FROM GroceryProduct gp
        WHERE gp.grocery.id = :groceryId
          AND gp.ingredientId IN :ingredientIds
          AND gp.isInStock = true
        ORDER BY gp.price ASC
        """)
    List<GroceryProduct> findByGroceryIdAndIngredientIds(
        @Param("groceryId") UUID groceryId,
        @Param("ingredientIds") List<UUID> ingredientIds
    );

    /**
     * Find in-stock products from groceries near a given location.
     */
    @Query("""
        SELECT gp FROM GroceryProduct gp
        JOIN FETCH gp.grocery g
        WHERE gp.ingredientId IN :ingredientIds
          AND gp.isInStock = true
          AND g.status = 'ACTIVE'
          AND g.latitude IS NOT NULL
          AND g.longitude IS NOT NULL
          AND (6371 * acos(cos(radians(:lat)) * cos(radians(g.latitude))
             * cos(radians(g.longitude) - radians(:lng))
             + sin(radians(:lat)) * sin(radians(g.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(g.latitude))
             * cos(radians(g.longitude) - radians(:lng))
             + sin(radians(:lat)) * sin(radians(g.latitude)))) ASC, gp.price ASC
        """)
    List<GroceryProduct> findNearbyByIngredientIds(
        @Param("ingredientIds") List<UUID> ingredientIds,
        @Param("lat") java.math.BigDecimal latitude,
        @Param("lng") java.math.BigDecimal longitude,
        @Param("radiusKm") double radiusKm
    );

    List<GroceryProduct> findByGroceryId(UUID groceryId);
}

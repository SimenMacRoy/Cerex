package com.cerex.repository;

import com.cerex.domain.Grocery;
import com.cerex.domain.Grocery.GroceryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Grocery domain operations.
 */
@Repository
public interface GroceryRepository extends JpaRepository<Grocery, UUID> {

    Optional<Grocery> findBySlug(String slug);

    Optional<Grocery> findByIdAndOwnerId(UUID id, UUID ownerId);

    Page<Grocery> findByStatus(GroceryStatus status, Pageable pageable);

    Page<Grocery> findByOwnerId(UUID ownerId, Pageable pageable);

    boolean existsBySlug(String slug);

    @Query("SELECT g FROM Grocery g WHERE g.status = 'ACTIVE' ORDER BY g.averageRating DESC")
    Page<Grocery> findActiveGroceries(Pageable pageable);

    @Query("""
        SELECT g FROM Grocery g
        WHERE g.status = 'ACTIVE'
          AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(g.city) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY g.averageRating DESC
        """)
    Page<Grocery> searchGroceries(@Param("query") String query, Pageable pageable);

    @Query("""
        SELECT g FROM Grocery g
        WHERE g.status = 'ACTIVE'
          AND g.latitude IS NOT NULL
          AND g.longitude IS NOT NULL
          AND (6371 * acos(cos(radians(:lat)) * cos(radians(g.latitude))
             * cos(radians(g.longitude) - radians(:lng))
             + sin(radians(:lat)) * sin(radians(g.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(g.latitude))
             * cos(radians(g.longitude) - radians(:lng))
             + sin(radians(:lat)) * sin(radians(g.latitude)))) ASC
        """)
    Page<Grocery> findNearbyGroceries(
        @Param("lat") BigDecimal latitude,
        @Param("lng") BigDecimal longitude,
        @Param("radiusKm") double radiusKm,
        Pageable pageable
    );

    @Query("SELECT g FROM Grocery g WHERE g.status = 'ACTIVE' AND LOWER(g.city) = LOWER(:city) ORDER BY g.averageRating DESC")
    Page<Grocery> findByCity(@Param("city") String city, Pageable pageable);

    @Query("SELECT g FROM Grocery g WHERE g.status = 'ACTIVE' AND g.isOrganicCertified = true ORDER BY g.ecoScore DESC")
    Page<Grocery> findOrganicGroceries(Pageable pageable);

    long countByStatus(GroceryStatus status);
}

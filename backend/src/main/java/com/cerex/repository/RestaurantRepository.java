package com.cerex.repository;

import com.cerex.domain.Restaurant;
import com.cerex.domain.Restaurant.RestaurantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Restaurant domain operations.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    // ─────────────────────────────────────────────────────────
    // Basic Finders
    // ─────────────────────────────────────────────────────────

    Optional<Restaurant> findBySlug(String slug);

    Optional<Restaurant> findByIdAndOwnerId(UUID id, UUID ownerId);

    Page<Restaurant> findByStatus(RestaurantStatus status, Pageable pageable);

    Page<Restaurant> findByOwnerId(UUID ownerId, Pageable pageable);

    boolean existsBySlug(String slug);

    // ─────────────────────────────────────────────────────────
    // Active Restaurants
    // ─────────────────────────────────────────────────────────

    @Query("SELECT r FROM Restaurant r WHERE r.status = 'ACTIVE' ORDER BY r.averageRating DESC")
    Page<Restaurant> findActiveRestaurants(Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // Geo-Search (within radius)
    // ─────────────────────────────────────────────────────────

    @Query("""
        SELECT r FROM Restaurant r
        WHERE r.status = 'ACTIVE'
          AND r.latitude IS NOT NULL
          AND r.longitude IS NOT NULL
          AND (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude))
             * cos(radians(r.longitude) - radians(:lng))
             + sin(radians(:lat)) * sin(radians(r.latitude)))) <= :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude))
             * cos(radians(r.longitude) - radians(:lng))
             + sin(radians(:lat)) * sin(radians(r.latitude)))) ASC
        """)
    Page<Restaurant> findNearbyRestaurants(
        @Param("lat") BigDecimal latitude,
        @Param("lng") BigDecimal longitude,
        @Param("radiusKm") double radiusKm,
        Pageable pageable
    );

    // ─────────────────────────────────────────────────────────
    // Search by Name / Cuisine
    // ─────────────────────────────────────────────────────────

    @Query("""
        SELECT r FROM Restaurant r
        WHERE r.status = 'ACTIVE'
          AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(r.city) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY r.averageRating DESC
        """)
    Page<Restaurant> searchRestaurants(@Param("query") String query, Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // By City
    // ─────────────────────────────────────────────────────────

    @Query("SELECT r FROM Restaurant r WHERE r.status = 'ACTIVE' AND LOWER(r.city) = LOWER(:city) ORDER BY r.averageRating DESC")
    Page<Restaurant> findByCity(@Param("city") String city, Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // By Cuisine Type
    // ─────────────────────────────────────────────────────────

    @Query("SELECT r FROM Restaurant r WHERE r.status = 'ACTIVE' AND LOWER(r.cuisineType) = LOWER(:cuisine) ORDER BY r.averageRating DESC")
    Page<Restaurant> findByCuisineType(@Param("cuisine") String cuisineType, Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // Top Rated
    // ─────────────────────────────────────────────────────────

    @Query("SELECT r FROM Restaurant r WHERE r.status = 'ACTIVE' AND r.totalReviews >= :minReviews ORDER BY r.averageRating DESC")
    Page<Restaurant> findTopRated(@Param("minReviews") int minReviews, Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // By Country
    // ─────────────────────────────────────────────────────────

    @Query("SELECT r FROM Restaurant r WHERE r.status = 'ACTIVE' AND r.countryId = :countryId ORDER BY r.averageRating DESC")
    Page<Restaurant> findByCountry(@Param("countryId") UUID countryId, Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // Eco-Friendly
    // ─────────────────────────────────────────────────────────

    @Query("SELECT r FROM Restaurant r WHERE r.status = 'ACTIVE' AND r.ecoScore >= :minScore ORDER BY r.ecoScore DESC")
    Page<Restaurant> findEcoFriendly(@Param("minScore") int minScore, Pageable pageable);

    // ─────────────────────────────────────────────────────────
    // Delivery capable near location
    // ─────────────────────────────────────────────────────────

    @Query("""
        SELECT r FROM Restaurant r
        WHERE r.status = 'ACTIVE'
          AND r.supportsDelivery = true
          AND r.latitude IS NOT NULL
          AND (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude))
             * cos(radians(r.longitude) - radians(:lng))
             + sin(radians(:lat)) * sin(radians(r.latitude)))) <= r.deliveryRadiusKm
        ORDER BY r.averageRating DESC
        """)
    Page<Restaurant> findDeliveringToLocation(
        @Param("lat") BigDecimal latitude,
        @Param("lng") BigDecimal longitude,
        Pageable pageable
    );

    // ─────────────────────────────────────────────────────────
    // Counts
    // ─────────────────────────────────────────────────────────

    long countByStatus(RestaurantStatus status);

    long countByOwnerId(UUID ownerId);
}

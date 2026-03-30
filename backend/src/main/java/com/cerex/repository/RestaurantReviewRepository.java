package com.cerex.repository;

import com.cerex.domain.RestaurantReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for restaurant reviews.
 */
@Repository
public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, UUID> {

    Page<RestaurantReview> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId, Pageable pageable);

    Optional<RestaurantReview> findByRestaurantIdAndUserId(UUID restaurantId, UUID userId);

    boolean existsByRestaurantIdAndUserId(UUID restaurantId, UUID userId);

    @Query("SELECT AVG(r.rating) FROM RestaurantReview r WHERE r.restaurant.id = :restaurantId")
    Double getAverageRating(@Param("restaurantId") UUID restaurantId);

    long countByRestaurantId(UUID restaurantId);
}

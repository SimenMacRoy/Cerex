package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a user review of a restaurant.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "restaurant_reviews",
    schema = "orders_schema",
    indexes = {
        @Index(name = "idx_rest_reviews_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_rest_reviews_user_id",       columnList = "user_id"),
        @Index(name = "idx_rest_reviews_rating",        columnList = "rating"),
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_restaurant_review_user",
            columnNames = {"restaurant_id", "user_id"}
        )
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RestaurantReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "order_id")
    private UUID orderId;

    @NotNull
    @Min(1) @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "food_rating")
    @Min(1) @Max(5)
    private Integer foodRating;

    @Column(name = "service_rating")
    @Min(1) @Max(5)
    private Integer serviceRating;

    @Column(name = "delivery_rating")
    @Min(1) @Max(5)
    private Integer deliveryRating;

    @Column(name = "ambiance_rating")
    @Min(1) @Max(5)
    private Integer ambianceRating;

    @Size(max = 3000)
    @Column(name = "comment", length = 3000)
    private String comment;

    @Column(name = "is_verified_purchase")
    @Builder.Default
    private Boolean isVerifiedPurchase = false;

    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(name = "is_flagged")
    @Builder.Default
    private Boolean isFlagged = false;

    @Column(name = "restaurant_reply", length = 2000)
    private String restaurantReply;

    @Column(name = "replied_at")
    private Instant repliedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;

/**
 * Domain entity representing a restaurant or food establishment.
 *
 * <p>Supports:
 * <ul>
 *   <li>Full restaurant profile with geo-location</li>
 *   <li>Multiple cuisine types and categories</li>
 *   <li>Operating hours with timezone awareness</li>
 *   <li>Menu management (1:N Menu → MenuItem)</li>
 *   <li>Rating & review aggregation</li>
 *   <li>Delivery zone / radius management</li>
 *   <li>Stripe Connect for restaurant payouts</li>
 *   <li>Verification and partnership status</li>
 * </ul>
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "restaurants",
    schema = "orders_schema",
    indexes = {
        @Index(name = "idx_restaurants_owner_id",    columnList = "owner_id"),
        @Index(name = "idx_restaurants_status",       columnList = "status"),
        @Index(name = "idx_restaurants_city",         columnList = "city"),
        @Index(name = "idx_restaurants_country_id",   columnList = "country_id"),
        @Index(name = "idx_restaurants_cuisine_type", columnList = "cuisine_type"),
        @Index(name = "idx_restaurants_avg_rating",   columnList = "average_rating"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"menus", "reviews"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Restaurant {

    // ─────────────────────────────────────────────────────────
    // Primary Key
    // ─────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ─────────────────────────────────────────────────────────
    // Owner & Status
    // ─────────────────────────────────────────────────────────

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.PENDING_VERIFICATION;

    // ─────────────────────────────────────────────────────────
    // Basic Info
    // ─────────────────────────────────────────────────────────

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @Size(max = 300)
    @Column(name = "slug", unique = true, length = 300)
    private String slug;

    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "restaurant_cuisine_tags",
        schema = "orders_schema",
        joinColumns = @JoinColumn(name = "restaurant_id")
    )
    @Column(name = "tag")
    @Builder.Default
    private Set<String> cuisineTags = new HashSet<>();

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "website", length = 500)
    private String website;

    // ─────────────────────────────────────────────────────────
    // Location
    // ─────────────────────────────────────────────────────────

    @Column(name = "address_line1", length = 300)
    private String addressLine1;

    @Column(name = "address_line2", length = 300)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state_province", length = 100)
    private String stateProvince;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country_id")
    private UUID countryId;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "delivery_radius_km")
    @Builder.Default
    private Double deliveryRadiusKm = 10.0;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    // ─────────────────────────────────────────────────────────
    // Operating Hours (JSON)
    // ─────────────────────────────────────────────────────────

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "operating_hours", columnDefinition = "jsonb")
    private Map<String, OperatingHoursSlot> operatingHours;

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "average_preparation_time_min")
    @Builder.Default
    private Integer averagePreparationTimeMin = 30;

    // ─────────────────────────────────────────────────────────
    // Media
    // ─────────────────────────────────────────────────────────

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "restaurant_gallery",
        schema = "orders_schema",
        joinColumns = @JoinColumn(name = "restaurant_id")
    )
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> galleryImages = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Ratings & Metrics
    // ─────────────────────────────────────────────────────────

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;

    // ─────────────────────────────────────────────────────────
    // Payment & Subscription
    // ─────────────────────────────────────────────────────────

    @Column(name = "stripe_connect_account_id", length = 100)
    private String stripeConnectAccountId;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal commissionRate = new BigDecimal("0.15"); // 15%

    @Column(name = "is_premium_partner")
    @Builder.Default
    private Boolean isPremiumPartner = false;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    // ─────────────────────────────────────────────────────────
    // Eco & Features
    // ─────────────────────────────────────────────────────────

    @Column(name = "eco_score")
    @Builder.Default
    private Integer ecoScore = 0;

    @Column(name = "supports_takeaway")
    @Builder.Default
    private Boolean supportsTakeaway = true;

    @Column(name = "supports_delivery")
    @Builder.Default
    private Boolean supportsDelivery = true;

    @Column(name = "supports_dine_in")
    @Builder.Default
    private Boolean supportsDineIn = true;

    @Column(name = "accepts_reservations")
    @Builder.Default
    private Boolean acceptsReservations = false;

    @Column(name = "offers_catering")
    @Builder.Default
    private Boolean offersCatering = false;

    // ─────────────────────────────────────────────────────────
    // Relationships
    // ─────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<RestaurantReview> reviews = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Audit
    // ─────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    // ─────────────────────────────────────────────────────────
    // Enums
    // ─────────────────────────────────────────────────────────

    public enum RestaurantStatus {
        PENDING_VERIFICATION,
        ACTIVE,
        SUSPENDED,
        TEMPORARILY_CLOSED,
        PERMANENTLY_CLOSED
    }

    // ─────────────────────────────────────────────────────────
    // Value Objects
    // ─────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHoursSlot {
        private String openTime;  // "09:00"
        private String closeTime; // "22:00"
        private boolean closed;
    }

    // ─────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────

    public void addMenu(Menu menu) {
        menus.add(menu);
        menu.setRestaurant(this);
    }

    public void addReview(RestaurantReview review) {
        reviews.add(review);
        review.setRestaurant(this);
        recalculateRating();
    }

    public void verify() {
        this.isVerified = true;
        this.verifiedAt = Instant.now();
        if (this.status == RestaurantStatus.PENDING_VERIFICATION) {
            this.status = RestaurantStatus.ACTIVE;
        }
    }

    public void suspend() {
        this.status = RestaurantStatus.SUSPENDED;
    }

    public void activate() {
        this.status = RestaurantStatus.ACTIVE;
    }

    public void temporarilyClose() {
        this.status = RestaurantStatus.TEMPORARILY_CLOSED;
    }

    public boolean isOpen() {
        return this.status == RestaurantStatus.ACTIVE;
    }

    private void recalculateRating() {
        if (reviews.isEmpty()) {
            this.averageRating = BigDecimal.ZERO;
            this.totalReviews = 0;
            return;
        }
        double avg = reviews.stream()
            .mapToInt(RestaurantReview::getRating)
            .average()
            .orElse(0.0);
        this.averageRating = BigDecimal.valueOf(avg);
        this.totalReviews = reviews.size();
    }
}

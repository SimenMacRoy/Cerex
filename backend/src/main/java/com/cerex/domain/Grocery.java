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
import java.util.*;

/**
 * Domain entity representing a grocery store / épicerie.
 *
 * <p>Groceries sell raw ingredients and culinary products. Users can buy
 * ingredient kits linked to recipes, or individual products.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "groceries",
    schema = "orders_schema",
    indexes = {
        @Index(name = "idx_groceries_owner_id",  columnList = "owner_id"),
        @Index(name = "idx_groceries_status",     columnList = "status"),
        @Index(name = "idx_groceries_city",       columnList = "city"),
        @Index(name = "idx_groceries_country_id", columnList = "country_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"products"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Grocery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private GroceryStatus status = GroceryStatus.PENDING_VERIFICATION;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "slug", unique = true, length = 300)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "grocery_type", length = 30)
    @Builder.Default
    private GroceryType groceryType = GroceryType.GENERAL;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "grocery_specialty_tags",
        schema = "orders_schema",
        joinColumns = @JoinColumn(name = "grocery_id")
    )
    @Column(name = "tag")
    @Builder.Default
    private Set<String> specialtyTags = new HashSet<>();

    // ─────────────────────────────────────────────────────────
    // Contact
    // ─────────────────────────────────────────────────────────

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
    private Double deliveryRadiusKm = 15.0;

    // ─────────────────────────────────────────────────────────
    // Operating Hours
    // ─────────────────────────────────────────────────────────

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "operating_hours", columnDefinition = "jsonb")
    private Map<String, Restaurant.OperatingHoursSlot> operatingHours;

    // ─────────────────────────────────────────────────────────
    // Media
    // ─────────────────────────────────────────────────────────

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    // ─────────────────────────────────────────────────────────
    // Ratings
    // ─────────────────────────────────────────────────────────

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    // ─────────────────────────────────────────────────────────
    // Features
    // ─────────────────────────────────────────────────────────

    @Column(name = "supports_delivery")
    @Builder.Default
    private Boolean supportsDelivery = true;

    @Column(name = "supports_pickup")
    @Builder.Default
    private Boolean supportsPickup = true;

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "is_organic_certified")
    @Builder.Default
    private Boolean isOrganicCertified = false;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "eco_score")
    @Builder.Default
    private Integer ecoScore = 0;

    // ─────────────────────────────────────────────────────────
    // Payment
    // ─────────────────────────────────────────────────────────

    @Column(name = "stripe_connect_account_id", length = 100)
    private String stripeConnectAccountId;

    // ─────────────────────────────────────────────────────────
    // Relationships
    // ─────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "grocery", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroceryProduct> products = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Audit
    // ─────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ─────────────────────────────────────────────────────────
    // Enums
    // ─────────────────────────────────────────────────────────

    public enum GroceryStatus {
        PENDING_VERIFICATION,
        ACTIVE,
        SUSPENDED,
        CLOSED
    }

    public enum GroceryType {
        GENERAL,
        ORGANIC,
        ETHNIC,
        BUTCHER,
        FISHMONGER,
        BAKERY,
        SPECIALTY,
        FARMERS_MARKET,
        WHOLESALE
    }

    // ─────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────

    public void addProduct(GroceryProduct product) {
        products.add(product);
        product.setGrocery(this);
    }

    public void verify() {
        this.isVerified = true;
        if (this.status == GroceryStatus.PENDING_VERIFICATION) {
            this.status = GroceryStatus.ACTIVE;
        }
    }

    public boolean isOpen() {
        return this.status == GroceryStatus.ACTIVE;
    }
}

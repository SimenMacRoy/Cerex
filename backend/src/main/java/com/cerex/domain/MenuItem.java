package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Domain entity representing a single item on a restaurant menu.
 *
 * <p>Captures pricing, dietary flags, portion info, and linked recipe (optional).
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "menu_items",
    schema = "orders_schema",
    indexes = {
        @Index(name = "idx_menu_items_menu_id",   columnList = "menu_id"),
        @Index(name = "idx_menu_items_recipe_id", columnList = "recipe_id"),
        @Index(name = "idx_menu_items_category",  columnList = "category"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"menu"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "recipe_id")
    private UUID recipeId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "category", length = 100)
    private String category; // "Entrées", "Main Courses", "Desserts", etc.

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "currency_code", length = 3)
    @Builder.Default
    private String currencyCode = "EUR";

    // ─────────────────────────────────────────────────────────
    // Dietary & Nutritional Info
    // ─────────────────────────────────────────────────────────

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "portion_size", length = 50)
    private String portionSize; // "350g", "1 piece"

    @Column(name = "is_vegan")
    @Builder.Default
    private Boolean isVegan = false;

    @Column(name = "is_vegetarian")
    @Builder.Default
    private Boolean isVegetarian = false;

    @Column(name = "is_gluten_free")
    @Builder.Default
    private Boolean isGlutenFree = false;

    @Column(name = "is_halal")
    @Builder.Default
    private Boolean isHalal = false;

    @Column(name = "is_kosher")
    @Builder.Default
    private Boolean isKosher = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "menu_item_allergens",
        schema = "orders_schema",
        joinColumns = @JoinColumn(name = "menu_item_id")
    )
    @Column(name = "allergen")
    @Builder.Default
    private Set<String> allergens = new HashSet<>();

    // ─────────────────────────────────────────────────────────
    // Media & Display
    // ─────────────────────────────────────────────────────────

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_spicy")
    @Builder.Default
    private Boolean isSpicy = false;

    @Column(name = "spice_level")
    @Builder.Default
    private Integer spiceLevel = 0; // 0-5

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "preparation_time_min")
    private Integer preparationTimeMin;

    // ─────────────────────────────────────────────────────────
    // Eco Score
    // ─────────────────────────────────────────────────────────

    @Column(name = "eco_score")
    @Builder.Default
    private Integer ecoScore = 0; // 0-100

    @Column(name = "carbon_footprint_g")
    private Integer carbonFootprintGrams;

    // ─────────────────────────────────────────────────────────
    // Stats
    // ─────────────────────────────────────────────────────────

    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ─────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────

    public BigDecimal getEffectivePrice() {
        return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0
            ? discountPrice
            : price;
    }

    public boolean hasDiscount() {
        return discountPrice != null && discountPrice.compareTo(price) < 0;
    }

    public BigDecimal getDiscountPercentage() {
        if (!hasDiscount()) return BigDecimal.ZERO;
        return BigDecimal.ONE
            .subtract(discountPrice.divide(price, 4, java.math.RoundingMode.HALF_UP))
            .multiply(BigDecimal.valueOf(100));
    }
}

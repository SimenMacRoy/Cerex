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
 * Domain entity representing a product sold in a grocery store.
 *
 * <p>May be linked to a global {@link Ingredient} for recipe-kit matching.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "grocery_products",
    schema = "orders_schema",
    indexes = {
        @Index(name = "idx_grocery_products_grocery_id",    columnList = "grocery_id"),
        @Index(name = "idx_grocery_products_ingredient_id", columnList = "ingredient_id"),
        @Index(name = "idx_grocery_products_category",      columnList = "category"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"grocery"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroceryProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grocery_id", nullable = false)
    private Grocery grocery;

    @Column(name = "ingredient_id")
    private UUID ingredientId; // link to global ingredient catalog

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "brand", length = 100)
    private String brand;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "price_per_unit", precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "unit", length = 30)
    private String unit; // "kg", "L", "piece", "100g"

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "is_in_stock")
    @Builder.Default
    private Boolean isInStock = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "origin_country", length = 100)
    private String originCountry;

    /** If true, this product can only be sold in wholesale quantities (not retail). */
    @Column(name = "is_bulk_only")
    @Builder.Default
    private Boolean isBulkOnly = false;

    /** Minimum purchase quantity (e.g., 5 for bulk-only items). Defaults to 1. */
    @Column(name = "minimum_quantity")
    @Builder.Default
    private Integer minimumQuantity = 1;

    /** Currency code for the price (ISO 4217, e.g., XAF, EUR, USD). */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "XAF";

    // ─────────────────────────────────────────────────────────
    // Dietary & Quality
    // ─────────────────────────────────────────────────────────

    @Column(name = "is_organic")
    @Builder.Default
    private Boolean isOrganic = false;

    @Column(name = "is_local")
    @Builder.Default
    private Boolean isLocal = false;

    @Column(name = "is_fair_trade")
    @Builder.Default
    private Boolean isFairTrade = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "grocery_product_allergens",
        schema = "orders_schema",
        joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "allergen")
    @Builder.Default
    private Set<String> allergens = new HashSet<>();

    @Column(name = "eco_score")
    @Builder.Default
    private Integer ecoScore = 0;

    @Column(name = "nutri_score", length = 2)
    private String nutriScore; // A, B, C, D, E

    @Column(name = "carbon_footprint_g")
    private Integer carbonFootprintGrams;

    // ─────────────────────────────────────────────────────────
    // Stats
    // ─────────────────────────────────────────────────────────

    @Column(name = "total_sold")
    @Builder.Default
    private Integer totalSold = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ─────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────

    public boolean isAvailable() {
        return isInStock && stockQuantity > 0;
    }

    public void decrementStock(int quantity) {
        this.stockQuantity = Math.max(0, this.stockQuantity - quantity);
        if (this.stockQuantity == 0) {
            this.isInStock = false;
        }
    }

    public void incrementStock(int quantity) {
        this.stockQuantity += quantity;
        this.isInStock = true;
    }
}

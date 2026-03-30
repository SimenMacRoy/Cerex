package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Individual item within an order.
 *
 * <p>Captures the recipe ordered, quantity, unit price at time of purchase,
 * and any customization notes.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "order_items",
    schema = "orders_schema",
    indexes = {
        @Index(name = "idx_order_items_order_id",  columnList = "order_id"),
        @Index(name = "idx_order_items_recipe_id", columnList = "recipe_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** The recipe being ordered. */
    @NotNull
    @Column(name = "recipe_id", nullable = false)
    private UUID recipeId;

    /** Recipe title at time of order (denormalized for order history). */
    @NotBlank
    @Size(max = 300)
    @Column(name = "recipe_title", nullable = false, length = 300)
    private String recipeTitle;

    /** Thumbnail URL of the recipe at time of order. */
    @Size(max = 500)
    @Column(name = "recipe_thumbnail_url", length = 500)
    private String recipeThumbnailUrl;

    /** Number of servings/portions ordered. */
    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    /** Unit price per item at time of order (price snapshot). */
    @NotNull
    @DecimalMin("0.01")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** Line total: quantity × unit_price. */
    @NotNull
    @DecimalMin("0.01")
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /** Customer customization notes (e.g., "no onions", "extra spicy"). */
    @Size(max = 500)
    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Domain Methods ──────────────────────────────────────
    /**
     * Calculates the total price for this line item.
     */
    public BigDecimal calculateTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

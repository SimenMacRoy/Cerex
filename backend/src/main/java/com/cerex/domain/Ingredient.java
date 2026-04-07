package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Global ingredient master catalog.
 *
 * <p>Normalized ingredient data shared across all recipes.
 * Includes nutritional data, allergen flags, and category information.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "ingredients",
    schema = "recipes_schema",
    indexes = {
        @Index(name = "idx_ingredients_name",     columnList = "name"),
        @Index(name = "idx_ingredients_category",  columnList = "category"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 200)
    @Column(name = "name_fr", length = 200)
    private String nameFr;

    @Size(max = 200)
    @Column(name = "name_es", length = 200)
    private String nameEs;

    @Size(max = 100)
    @Column(name = "category", length = 100)
    private String category;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Whether this ingredient contains common allergens. */
    @Column(name = "is_allergen")
    @Builder.Default
    private Boolean isAllergen = false;

    /** Allergen type (e.g., GLUTEN, DAIRY, NUTS, SHELLFISH). */
    @Column(name = "allergen_type", length = 50)
    private String allergenType;

    /** Calories per 100g. */
    @Column(name = "calories_per_100g")
    private Integer caloriesPer100g;

    /** Estimated market price in FCFA per base unit (e.g., per kg, per piece). */
    @Column(name = "estimated_price_fcfa", precision = 10, scale = 2)
    private java.math.BigDecimal estimatedPriceFcfa;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

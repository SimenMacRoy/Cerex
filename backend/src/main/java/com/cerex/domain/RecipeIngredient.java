package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Junction entity linking a recipe to an ingredient with quantity and unit.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "recipe_ingredients",
    schema = "recipes_schema",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"recipe_id", "ingredient_id"})
    },
    indexes = {
        @Index(name = "idx_recipe_ingredients_recipe_id", columnList = "recipe_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "ingredient_id", insertable = false, updatable = false)
    private UUID ingredientId;

    /** Quantity of the ingredient (e.g., 2.5). */
    @DecimalMin("0.0")
    @Column(name = "quantity", precision = 10, scale = 3)
    private BigDecimal quantity;

    /** Measurement unit (e.g., "cups", "g", "tbsp", "pieces"). */
    @Size(max = 50)
    @Column(name = "unit", length = 50)
    private String unit;

    /** Free-text display name override (e.g., "2 large ripe tomatoes"). */
    @Size(max = 300)
    @Column(name = "display_text", length = 300)
    private String displayText;

    /** Whether this ingredient is optional. */
    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = false;

    /** Group label for ingredient sections (e.g., "For the sauce", "For garnish"). */
    @Size(max = 100)
    @Column(name = "group_name", length = 100)
    private String groupName;

    /** Display order within the recipe ingredient list. */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}

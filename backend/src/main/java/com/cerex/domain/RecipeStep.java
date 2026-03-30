package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * Ordered cooking instruction step within a recipe.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "recipe_steps",
    schema = "recipes_schema",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"recipe_id", "step_number"})
    },
    indexes = {
        @Index(name = "idx_recipe_steps_recipe_id", columnList = "recipe_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RecipeStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /** Sequential step number (1, 2, 3, ...). */
    @NotNull
    @Min(1)
    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    /** Full instruction text for this step. */
    @NotBlank
    @Size(max = 2000)
    @Column(name = "instruction", nullable = false, columnDefinition = "TEXT")
    private String instruction;

    /** Estimated duration for this step in minutes. */
    @Min(0)
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /** URL to an image illustrating this step. */
    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Specific tip or technique for this step. */
    @Size(max = 500)
    @Column(name = "tip", length = 500)
    private String tip;
}

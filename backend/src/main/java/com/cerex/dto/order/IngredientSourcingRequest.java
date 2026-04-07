package com.cerex.dto.order;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body for obtaining an ingredient sourcing quote.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientSourcingRequest {

    @NotNull(message = "Recipe ID is required")
    private UUID recipeId;

    @Min(value = 1, message = "Servings must be at least 1")
    @Max(value = 100, message = "Servings cannot exceed 100")
    @Builder.Default
    private int servings = 4;

    @NotNull(message = "User latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @NotNull(message = "User longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    /** Search radius in km (default 30 km). */
    @Builder.Default
    private double radiusKm = 30.0;

    /** Preferred currency override (auto-detected if null). */
    private String currency;
}

package com.cerex.controller;

import com.cerex.dto.ApiResponse;
import com.cerex.dto.order.IngredientSourcingDTO;
import com.cerex.dto.order.IngredientSourcingRequest;
import com.cerex.service.IngredientSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for ingredient ordering and sourcing.
 *
 * <p>Base path: {@code /api/v1/ingredient-orders}
 */
@RestController
@RequestMapping("/api/v1/ingredient-orders")
@RequiredArgsConstructor
@Tag(name = "Ingredient Orders", description = "Smart ingredient sourcing and ordering")
public class IngredientOrderController {

    private final IngredientSourcingService sourcingService;

    /**
     * Get a sourcing quote: finds nearby stores, matches recipe ingredients
     * to real products, splits across multiple stores if needed, computes
     * pricing in the user's local currency.
     */
    @PostMapping("/quote")
    @Operation(summary = "Get ingredient sourcing quote",
        description = "Finds the best combination of nearby grocery stores to source all recipe ingredients")
    public ResponseEntity<ApiResponse<IngredientSourcingDTO>> getQuote(
            @Valid @RequestBody IngredientSourcingRequest request) {

        IngredientSourcingDTO quote = sourcingService.buildQuote(request);

        return ResponseEntity.ok(ApiResponse.ok(quote,
            String.format("Found %d store(s) covering your ingredients",
                quote.getStorePlans().size())));
    }
}

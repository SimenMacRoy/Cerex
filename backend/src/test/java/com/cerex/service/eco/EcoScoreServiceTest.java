package com.cerex.service.eco;

import com.cerex.service.eco.EcoScoreService.EcoScoreResult;
import com.cerex.service.eco.EcoScoreService.IngredientEcoData;
import com.cerex.service.eco.EcoScoreService.PortionSuggestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for EcoScoreService.
 */
@DisplayName("EcoScoreService Tests")
class EcoScoreServiceTest {

    private final EcoScoreService ecoScoreService = new EcoScoreService();

    @Test
    @DisplayName("Should compute high eco score for local organic seasonal ingredients")
    void shouldComputeHighEcoScore() {
        // Given
        List<IngredientEcoData> ingredients = List.of(
            IngredientEcoData.builder().name("Tomate").carbonFootprintGrams(50)
                .isLocal(true).isOrganic(true).isSeasonal(true).build(),
            IngredientEcoData.builder().name("Basilic").carbonFootprintGrams(10)
                .isLocal(true).isOrganic(true).isSeasonal(true).build(),
            IngredientEcoData.builder().name("Mozzarella").carbonFootprintGrams(200)
                .isLocal(true).isOrganic(false).isSeasonal(true).build()
        );

        // When
        EcoScoreResult result = ecoScoreService.computeRecipeEcoScore(ingredients);

        // Then
        assertThat(result.getScore()).isGreaterThan(60);
        assertThat(result.getGrade()).isIn("A", "B");
    }

    @Test
    @DisplayName("Should compute low eco score for non-local non-organic ingredients")
    void shouldComputeLowEcoScore() {
        // Given
        List<IngredientEcoData> ingredients = List.of(
            IngredientEcoData.builder().name("Avocat").carbonFootprintGrams(2000)
                .isLocal(false).isOrganic(false).isSeasonal(false).build(),
            IngredientEcoData.builder().name("Crevettes").carbonFootprintGrams(3000)
                .isLocal(false).isOrganic(false).isSeasonal(false).build()
        );

        // When
        EcoScoreResult result = ecoScoreService.computeRecipeEcoScore(ingredients);

        // Then
        assertThat(result.getScore()).isLessThan(50);
        assertThat(result.getGrade()).isIn("C", "D", "E");
    }

    @Test
    @DisplayName("Should return default for empty ingredients")
    void shouldReturnDefaultForEmpty() {
        // When
        EcoScoreResult result = ecoScoreService.computeRecipeEcoScore(List.of());

        // Then
        assertThat(result.getScore()).isEqualTo(50);
        assertThat(result.getGrade()).isEqualTo("C");
    }

    @Test
    @DisplayName("Should suggest portion reduction for high waste users")
    void shouldSuggestPortionReduction() {
        // When
        PortionSuggestion result = ecoScoreService.optimizePortions(4, 35.0);

        // Then
        assertThat(result.getSuggestedServings()).isLessThan(4);
        assertThat(result.getEstimatedWasteSavedGrams()).isGreaterThan(0);
        assertThat(result.getTip()).contains("réduire");
    }

    @Test
    @DisplayName("Should not reduce portions for low waste users")
    void shouldNotReducePortionsForLowWaste() {
        // When
        PortionSuggestion result = ecoScoreService.optimizePortions(4, 10.0);

        // Then
        assertThat(result.getSuggestedServings()).isEqualTo(4);
        assertThat(result.getTip()).contains("bien calibrées");
    }
}

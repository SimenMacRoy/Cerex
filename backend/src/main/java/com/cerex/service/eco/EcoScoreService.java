package com.cerex.service.eco;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for computing eco-scores for recipes, restaurants, and products.
 *
 * <p>Factors:
 * <ul>
 *   <li>Carbon footprint of ingredients</li>
 *   <li>Seasonality (local & in-season = higher score)</li>
 *   <li>Food miles (distance from origin)</li>
 *   <li>Organic / fair trade certification</li>
 *   <li>Waste potential (packaging, leftovers)</li>
 *   <li>Animal welfare considerations</li>
 * </ul>
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EcoScoreService {

    // ─────────────────────────────────────────────────────────
    // Recipe Eco Score
    // ─────────────────────────────────────────────────────────

    /**
     * Compute eco score (0-100) for a recipe based on its ingredients.
     */
    public EcoScoreResult computeRecipeEcoScore(List<IngredientEcoData> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return EcoScoreResult.builder().score(50).grade("C").details(Map.of()).build();
        }

        double totalCarbon = 0;
        int localCount = 0;
        int organicCount = 0;
        int seasonalCount = 0;
        int total = ingredients.size();

        for (IngredientEcoData ing : ingredients) {
            totalCarbon += ing.carbonFootprintGrams;
            if (ing.isLocal) localCount++;
            if (ing.isOrganic) organicCount++;
            if (ing.isSeasonal) seasonalCount++;
        }

        // Scoring
        double carbonScore = Math.max(0, 100 - (totalCarbon / (total * 10.0))); // less carbon = higher
        double localScore = (localCount * 100.0) / total;
        double organicScore = (organicCount * 100.0) / total;
        double seasonalScore = (seasonalCount * 100.0) / total;

        // Weighted average
        int finalScore = (int) (carbonScore * 0.35 + localScore * 0.25 +
                                 organicScore * 0.20 + seasonalScore * 0.20);
        finalScore = Math.max(0, Math.min(100, finalScore));

        String grade = computeGrade(finalScore);

        Map<String, Object> details = Map.of(
            "carbonFootprintTotal", totalCarbon,
            "carbonScore", carbonScore,
            "localPercentage", localScore,
            "organicPercentage", organicScore,
            "seasonalPercentage", seasonalScore,
            "ingredientCount", total
        );

        log.debug("Eco score computed: {} ({}) for {} ingredients", finalScore, grade, total);
        return EcoScoreResult.builder().score(finalScore).grade(grade).details(details).build();
    }

    // ─────────────────────────────────────────────────────────
    // Restaurant Eco Score
    // ─────────────────────────────────────────────────────────

    /**
     * Compute eco score for a restaurant.
     */
    public EcoScoreResult computeRestaurantEcoScore(
            double wasteReductionPercent,
            boolean usesRenewableEnergy,
            boolean hasComposting,
            double localSourcingPercent,
            boolean noSingleUsePlastic,
            int communityEngagementScore) {

        double score = 0;

        // Waste reduction (max 25 pts)
        score += Math.min(25, wasteReductionPercent * 0.25);

        // Renewable energy (15 pts)
        if (usesRenewableEnergy) score += 15;

        // Composting (10 pts)
        if (hasComposting) score += 10;

        // Local sourcing (max 25 pts)
        score += Math.min(25, localSourcingPercent * 0.25);

        // No single-use plastic (10 pts)
        if (noSingleUsePlastic) score += 10;

        // Community engagement (max 15 pts)
        score += Math.min(15, communityEngagementScore);

        int finalScore = (int) Math.min(100, score);
        String grade = computeGrade(finalScore);

        return EcoScoreResult.builder()
            .score(finalScore)
            .grade(grade)
            .details(Map.of(
                "wasteReduction", wasteReductionPercent,
                "renewableEnergy", usesRenewableEnergy,
                "composting", hasComposting,
                "localSourcing", localSourcingPercent,
                "noPlastic", noSingleUsePlastic,
                "communityScore", communityEngagementScore
            ))
            .build();
    }

    // ─────────────────────────────────────────────────────────
    // Portion Optimizer
    // ─────────────────────────────────────────────────────────

    /**
     * Suggest optimized portions to minimize waste.
     */
    public PortionSuggestion optimizePortions(int requestedServings, double averageLeftoverPercent) {
        // If historical data shows >20% leftovers, suggest reducing portions
        int suggestedServings = requestedServings;
        String tip = null;

        if (averageLeftoverPercent > 30) {
            suggestedServings = (int) Math.ceil(requestedServings * 0.75);
            tip = "Vos restes habituels sont élevés. Nous suggérons de réduire les portions de 25%.";
        } else if (averageLeftoverPercent > 20) {
            suggestedServings = (int) Math.ceil(requestedServings * 0.85);
            tip = "Légère réduction recommandée pour minimiser le gaspillage.";
        } else {
            tip = "Vos portions sont bien calibrées. Continuez ainsi !";
        }

        double estimatedWasteSaved = (requestedServings - suggestedServings) * 150.0; // ~150g per serving

        return PortionSuggestion.builder()
            .originalServings(requestedServings)
            .suggestedServings(suggestedServings)
            .estimatedWasteSavedGrams(estimatedWasteSaved)
            .tip(tip)
            .build();
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    private String computeGrade(int score) {
        if (score >= 80) return "A";
        if (score >= 60) return "B";
        if (score >= 40) return "C";
        if (score >= 20) return "D";
        return "E";
    }

    // ─────────────────────────────────────────────────────────
    // DTOs
    // ─────────────────────────────────────────────────────────

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IngredientEcoData {
        private String name;
        private double carbonFootprintGrams;
        private boolean isLocal;
        private boolean isOrganic;
        private boolean isSeasonal;
        private String originCountry;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EcoScoreResult {
        private int score;
        private String grade;
        private Map<String, Object> details;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortionSuggestion {
        private int originalServings;
        private int suggestedServings;
        private double estimatedWasteSavedGrams;
        private String tip;
    }
}

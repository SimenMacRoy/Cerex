package com.cerex.controller;

import com.cerex.dto.ApiResponse;
import com.cerex.security.CerexUserDetails;
import com.cerex.service.ai.AIService;
import com.cerex.service.ai.RecommendationService;
import com.cerex.service.ai.RecommendationService.RecommendedRecipe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for AI-powered features.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>Recipe generation from ingredients</li>
 *   <li>Recipe adaptation (dietary)</li>
 *   <li>Recipe translation</li>
 *   <li>Cooking tips & substitutions</li>
 *   <li>Meal plan generation</li>
 *   <li>Personalized recommendations</li>
 *   <li>Anti-waste recipe suggestions</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "Intelligence artificielle culinaire")
public class AIController {

    private final AIService aiService;
    private final RecommendationService recommendationService;

    // ─────────────────────────────────────────────────────────
    // Recipe Generation
    // ─────────────────────────────────────────────────────────

    @PostMapping("/generate-recipe")
    @Operation(summary = "Générer une recette à partir d'ingrédients")
    public ResponseEntity<ApiResponse<String>> generateRecipe(
            @RequestParam List<String> ingredients,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String dietaryRestrictions,
            @RequestParam(defaultValue = "4") int servings,
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(defaultValue = "fr") String language) {
        String recipe = aiService.generateRecipe(ingredients, cuisine, dietaryRestrictions,
                                                   servings, difficultyLevel, language);
        return ResponseEntity.ok(ApiResponse.success(recipe));
    }

    @PostMapping("/adapt-recipe")
    @Operation(summary = "Adapter une recette à un régime alimentaire")
    public ResponseEntity<ApiResponse<String>> adaptRecipe(
            @RequestBody String recipeContent,
            @RequestParam String targetDiet,
            @RequestParam(defaultValue = "fr") String language) {
        String adapted = aiService.adaptRecipe(recipeContent, targetDiet, language);
        return ResponseEntity.ok(ApiResponse.success(adapted));
    }

    @PostMapping("/translate-recipe")
    @Operation(summary = "Traduire une recette")
    public ResponseEntity<ApiResponse<String>> translateRecipe(
            @RequestBody String recipeContent,
            @RequestParam String targetLanguage) {
        String translated = aiService.translateRecipe(recipeContent, targetLanguage);
        return ResponseEntity.ok(ApiResponse.success(translated));
    }

    // ─────────────────────────────────────────────────────────
    // Tips & Substitutions
    // ─────────────────────────────────────────────────────────

    @GetMapping("/substitutions")
    @Operation(summary = "Obtenir des substituts pour un ingrédient")
    public ResponseEntity<ApiResponse<String>> getSubstitutions(
            @RequestParam String ingredient,
            @RequestParam(required = false) String dietaryContext) {
        String subs = aiService.getIngredientSubstitutions(ingredient, dietaryContext);
        return ResponseEntity.ok(ApiResponse.success(subs));
    }

    @GetMapping("/cooking-tips")
    @Operation(summary = "Obtenir des conseils de cuisine")
    public ResponseEntity<ApiResponse<String>> getCookingTips(
            @RequestParam String context) {
        String tips = aiService.getCookingTips(context);
        return ResponseEntity.ok(ApiResponse.success(tips));
    }

    // ─────────────────────────────────────────────────────────
    // Meal Planning
    // ─────────────────────────────────────────────────────────

    @PostMapping("/meal-plan")
    @Operation(summary = "Générer un plan de repas")
    public ResponseEntity<ApiResponse<String>> generateMealPlan(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "2000") int caloriesPerDay,
            @RequestParam(required = false) String dietaryRestrictions,
            @RequestParam(required = false) String cuisine,
            @RequestParam(defaultValue = "50") int budget,
            @RequestParam(defaultValue = "fr") String language) {
        String plan = aiService.generateMealPlan(days, caloriesPerDay, dietaryRestrictions,
                                                   cuisine, budget, language);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    // ─────────────────────────────────────────────────────────
    // Recommendations
    // ─────────────────────────────────────────────────────────

    @GetMapping("/recommendations")
    @Operation(summary = "Obtenir des recommandations personnalisées")
    public ResponseEntity<ApiResponse<List<RecommendedRecipe>>> getRecommendations(
            @AuthenticationPrincipal CerexUserDetails user,
            @RequestParam(required = false) List<String> preferences,
            @RequestParam(required = false) List<String> cuisines,
            @RequestParam(defaultValue = "10") int limit) {
        List<RecommendedRecipe> recs = recommendationService.getPersonalizedRecommendations(
            user.getUserId(), preferences, cuisines, limit);
        return ResponseEntity.ok(ApiResponse.success(recs));
    }

    @GetMapping("/recipes/{recipeId}/similar")
    @Operation(summary = "Recettes similaires")
    public ResponseEntity<ApiResponse<List<RecommendedRecipe>>> getSimilarRecipes(
            @PathVariable UUID recipeId,
            @RequestParam(defaultValue = "6") int limit) {
        List<RecommendedRecipe> similar = recommendationService.getSimilarRecipes(recipeId, limit);
        return ResponseEntity.ok(ApiResponse.success(similar));
    }

    @GetMapping("/daily-discovery")
    @Operation(summary = "Découverte du jour")
    public ResponseEntity<ApiResponse<List<RecommendedRecipe>>> getDailyDiscovery(
            @AuthenticationPrincipal CerexUserDetails user,
            @RequestParam(defaultValue = "5") int limit) {
        List<RecommendedRecipe> discovery = recommendationService.getDailyDiscovery(user.getUserId(), limit);
        return ResponseEntity.ok(ApiResponse.success(discovery));
    }

    // ─────────────────────────────────────────────────────────
    // Anti-Waste
    // ─────────────────────────────────────────────────────────

    @PostMapping("/anti-waste")
    @Operation(summary = "Recettes anti-gaspillage à partir de restes")
    public ResponseEntity<ApiResponse<String>> antiWasteRecipes(
            @RequestParam List<String> leftovers,
            @RequestParam(defaultValue = "30") int maxPrepTime) {
        String recipes = aiService.suggestAntiWasteRecipes(leftovers, maxPrepTime);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }
}

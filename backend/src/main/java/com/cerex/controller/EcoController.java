package com.cerex.controller;

import com.cerex.dto.ApiResponse;
import com.cerex.security.CerexUserDetails;
import com.cerex.service.eco.EcoScoreService;
import com.cerex.service.eco.EcoScoreService.*;
import com.cerex.service.eco.WasteManagementService;
import com.cerex.service.eco.WasteManagementService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for ecology & sustainability features.
 */
@RestController
@RequestMapping("/api/v1/eco")
@RequiredArgsConstructor
@Tag(name = "Ecology", description = "Écologie & durabilité")
public class EcoController {

    private final EcoScoreService ecoScoreService;
    private final WasteManagementService wasteManagementService;

    @PostMapping("/score/recipe")
    @Operation(summary = "Calculer l'éco-score d'une recette")
    public ResponseEntity<ApiResponse<EcoScoreResult>> computeRecipeEcoScore(
            @RequestBody List<IngredientEcoData> ingredients) {
        EcoScoreResult result = ecoScoreService.computeRecipeEcoScore(ingredients);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/score/restaurant")
    @Operation(summary = "Calculer l'éco-score d'un restaurant")
    public ResponseEntity<ApiResponse<EcoScoreResult>> computeRestaurantEcoScore(
            @RequestParam double wasteReductionPercent,
            @RequestParam boolean usesRenewableEnergy,
            @RequestParam boolean hasComposting,
            @RequestParam double localSourcingPercent,
            @RequestParam boolean noSingleUsePlastic,
            @RequestParam(defaultValue = "5") int communityEngagementScore) {
        EcoScoreResult result = ecoScoreService.computeRestaurantEcoScore(
            wasteReductionPercent, usesRenewableEnergy, hasComposting,
            localSourcingPercent, noSingleUsePlastic, communityEngagementScore);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/optimize-portions")
    @Operation(summary = "Optimiser les portions pour réduire le gaspillage")
    public ResponseEntity<ApiResponse<PortionSuggestion>> optimizePortions(
            @RequestParam int requestedServings,
            @RequestParam(defaultValue = "15") double averageLeftoverPercent) {
        PortionSuggestion suggestion = ecoScoreService.optimizePortions(
            requestedServings, averageLeftoverPercent);
        return ResponseEntity.ok(ApiResponse.success(suggestion));
    }

    @PostMapping("/badges/check")
    @Operation(summary = "Vérifier et attribuer les éco-badges")
    public ResponseEntity<ApiResponse<List<BadgeAward>>> checkBadges(
            @AuthenticationPrincipal CerexUserDetails user,
            @RequestBody UserProgress progress) {
        List<BadgeAward> newBadges = wasteManagementService.checkAndAwardBadges(
            user.getUserId(), progress);
        return ResponseEntity.ok(ApiResponse.success(newBadges));
    }

    @GetMapping("/community-stats")
    @Operation(summary = "Statistiques de durabilité de la communauté")
    public ResponseEntity<ApiResponse<CommunitySustainabilityStats>> getCommunityStats() {
        CommunitySustainabilityStats stats = wasteManagementService.getCommunityStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

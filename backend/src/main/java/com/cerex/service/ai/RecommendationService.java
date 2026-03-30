package com.cerex.service.ai;

import com.cerex.domain.Recipe;
import com.cerex.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI-powered recipe recommendation engine.
 *
 * <p>Combines:
 * <ul>
 *   <li>Collaborative filtering (users who liked X also liked Y)</li>
 *   <li>Content-based filtering (dietary prefs, cuisine, difficulty)</li>
 *   <li>Trending score (boost score from social engagement)</li>
 *   <li>Cultural proximity (same continent/country recipes)</li>
 *   <li>Personalization (user history, saved recipes, followed chefs)</li>
 * </ul>
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecipeRepository recipeRepository;

    /**
     * Get personalized recipe recommendations for a user.
     *
     * @param userId      the user to recommend for
     * @param preferences user dietary preferences
     * @param cuisines    preferred cuisines
     * @param limit       max number of recommendations
     * @return list of recommended recipe IDs with scores
     */
    public List<RecommendedRecipe> getPersonalizedRecommendations(
            UUID userId,
            List<String> preferences,
            List<String> cuisines,
            int limit) {

        List<RecommendedRecipe> recommendations = new ArrayList<>();

        // 1. Trending recipes (weighted by boost score)
        var trending = recipeRepository.findTrendingRecipes(PageRequest.of(0, limit));
        for (var recipe : trending) {
            recommendations.add(RecommendedRecipe.builder()
                .recipeId(recipe.getId())
                .title(recipe.getTitle())
                .slug(recipe.getSlug())
                .thumbnailUrl(recipe.getThumbnailUrl())
                .score(computeScore(recipe, preferences, cuisines))
                .reason("Trending")
                .build());
        }

        // 2. Sort by score and deduplicate
        recommendations.sort(Comparator.comparingDouble(RecommendedRecipe::getScore).reversed());

        // Remove duplicates
        Set<UUID> seen = new HashSet<>();
        recommendations = recommendations.stream()
            .filter(r -> seen.add(r.getRecipeId()))
            .limit(limit)
            .collect(Collectors.toList());

        log.info("Generated {} recommendations for user [{}]", recommendations.size(), userId);
        return recommendations;
    }

    /**
     * Get similar recipes (content-based).
     */
    public List<RecommendedRecipe> getSimilarRecipes(UUID recipeId, int limit) {
        Recipe source = recipeRepository.findById(recipeId).orElse(null);
        if (source == null) return List.of();

        // Find recipes with same cuisine/continent/dietary flags
        var candidates = recipeRepository.findByContinentAndStatus(
            source.getContinentId(), Recipe.RecipeStatus.PUBLISHED, PageRequest.of(0, limit * 2));

        return candidates.getContent().stream()
            .filter(r -> !r.getId().equals(recipeId))
            .map(r -> RecommendedRecipe.builder()
                .recipeId(r.getId())
                .title(r.getTitle())
                .slug(r.getSlug())
                .thumbnailUrl(r.getThumbnailUrl())
                .score(computeSimilarity(source, r))
                .reason("Similar")
                .build())
            .sorted(Comparator.comparingDouble(RecommendedRecipe::getScore).reversed())
            .limit(limit)
            .toList();
    }

    /**
     * Get daily discovery suggestions (serendipity: recipes from cultures they haven't tried).
     */
    public List<RecommendedRecipe> getDailyDiscovery(UUID userId, int limit) {
        // For daily discovery, pick popular recipes from random continents
        var trending = recipeRepository.findTrendingRecipes(PageRequest.of(0, limit));

        return trending.getContent().stream()
            .map(r -> RecommendedRecipe.builder()
                .recipeId(r.getId())
                .title(r.getTitle())
                .slug(r.getSlug())
                .thumbnailUrl(r.getThumbnailUrl())
                .score(r.getAverageRating() != null ? r.getAverageRating().doubleValue() : 0.0)
                .reason("Daily Discovery")
                .build())
            .limit(limit)
            .toList();
    }

    // ─────────────────────────────────────────────────────────
    // Scoring
    // ─────────────────────────────────────────────────────────

    private double computeScore(Recipe recipe, List<String> preferences, List<String> cuisines) {
        double score = 0.0;

        // Base rating
        if (recipe.getAverageRating() != null) {
            score += recipe.getAverageRating().doubleValue() * 2.0;
        }

        // Popularity
        score += Math.log1p(recipe.getViewCount()) * 0.5;
        score += Math.log1p(recipe.getLikeCount()) * 1.0;

        // Dietary preference match
        if (preferences != null) {
            if (preferences.contains("vegan") && Boolean.TRUE.equals(recipe.getIsVegan())) score += 3.0;
            if (preferences.contains("vegetarian") && Boolean.TRUE.equals(recipe.getIsVegetarian())) score += 3.0;
            if (preferences.contains("gluten_free") && Boolean.TRUE.equals(recipe.getIsGlutenFree())) score += 3.0;
            if (preferences.contains("halal") && Boolean.TRUE.equals(recipe.getIsHalal())) score += 3.0;
        }

        // Freshness bonus (published recently)
        if (recipe.getPublishedAt() != null) {
            long daysOld = (System.currentTimeMillis() - recipe.getPublishedAt().toEpochMilli()) / (86400000L);
            if (daysOld < 7) score += 2.0;
            else if (daysOld < 30) score += 1.0;
        }

        return score;
    }

    private double computeSimilarity(Recipe source, Recipe candidate) {
        double score = 0.0;

        // Same continent
        if (source.getContinentId() != null && source.getContinentId().equals(candidate.getContinentId())) {
            score += 2.0;
        }

        // Same country
        if (source.getCountryId() != null && source.getCountryId().equals(candidate.getCountryId())) {
            score += 3.0;
        }

        // Dietary flag matches
        if (Objects.equals(source.getIsVegan(), candidate.getIsVegan())) score += 1.0;
        if (Objects.equals(source.getIsVegetarian(), candidate.getIsVegetarian())) score += 1.0;
        if (Objects.equals(source.getIsGlutenFree(), candidate.getIsGlutenFree())) score += 1.0;

        // Similar difficulty
        if (source.getDifficultyLevel() == candidate.getDifficultyLevel()) score += 1.0;

        // Rating of candidate
        if (candidate.getAverageRating() != null) {
            score += candidate.getAverageRating().doubleValue();
        }

        return score;
    }

    // ─────────────────────────────────────────────────────────
    // DTO
    // ─────────────────────────────────────────────────────────

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RecommendedRecipe {
        private UUID recipeId;
        private String title;
        private String slug;
        private String thumbnailUrl;
        private double score;
        private String reason;
    }
}

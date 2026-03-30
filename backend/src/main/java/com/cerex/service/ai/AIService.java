package com.cerex.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Service for AI-powered recipe generation using GPT-4o.
 *
 * <p>Integrates with OpenAI API to:
 * <ul>
 *   <li>Generate recipes from ingredients / description</li>
 *   <li>Adapt recipes to dietary restrictions</li>
 *   <li>Translate recipes between languages</li>
 *   <li>Generate cooking tips and substitutions</li>
 *   <li>Create meal plans</li>
 * </ul>
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    @Value("${cerex.ai.openai-api-key:}")
    private String openAiApiKey;

    @Value("${cerex.ai.model:gpt-4o}")
    private String model;

    @Value("${cerex.ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        }
        return webClient;
    }

    // ─────────────────────────────────────────────────────────
    // Recipe Generation
    // ─────────────────────────────────────────────────────────

    /**
     * Generate a recipe from a list of ingredients and preferences.
     */
    public String generateRecipe(List<String> ingredients, String cuisine, String dietaryRestrictions,
                                  int servings, String difficultyLevel, String language) {
        String prompt = buildRecipeGenerationPrompt(ingredients, cuisine, dietaryRestrictions,
                                                      servings, difficultyLevel, language);
        return callGPT(prompt, 2000);
    }

    /**
     * Adapt an existing recipe to dietary restrictions.
     */
    public String adaptRecipe(String recipeContent, String targetDiet, String language) {
        String prompt = String.format("""
            Tu es un chef cuisinier expert. Adapte la recette suivante pour qu'elle soit compatible avec le régime "%s".
            Remplace les ingrédients incompatibles par des alternatives savoureuses.
            Donne la recette complète adaptée avec les ingrédients et les étapes.
            Langue de réponse: %s
            
            Recette originale:
            %s
            """, targetDiet, language, recipeContent);

        return callGPT(prompt, 2000);
    }

    /**
     * Translate a recipe to another language while preserving culinary terms.
     */
    public String translateRecipe(String recipeContent, String targetLanguage) {
        String prompt = String.format("""
            Traduis la recette suivante en %s. Conserve les termes culinaires spécifiques quand ils n'ont
            pas d'équivalent direct. Adapte les unités de mesure au système local si nécessaire.
            
            %s
            """, targetLanguage, recipeContent);

        return callGPT(prompt, 2000);
    }

    // ─────────────────────────────────────────────────────────
    // Cooking Tips & Substitutions
    // ─────────────────────────────────────────────────────────

    /**
     * Get ingredient substitutions.
     */
    public String getIngredientSubstitutions(String ingredient, String dietaryContext) {
        String prompt = String.format("""
            Tu es un expert en nutrition et cuisine. Donne 5 substituts pour l'ingrédient "%s"
            dans un contexte %s. Pour chaque substitut, indique:
            - Le nom du substitut
            - Le ratio de remplacement
            - L'impact sur le goût
            - L'impact nutritionnel
            Réponds en JSON.
            """, ingredient, dietaryContext != null ? dietaryContext : "général");

        return callGPT(prompt, 1000);
    }

    /**
     * Generate cooking tips for a specific recipe or technique.
     */
    public String getCookingTips(String context) {
        String prompt = String.format("""
            Tu es un chef étoilé. Donne 5 conseils de pro pour: %s
            Sois précis et pratique. Inclus des astuces que seuls les professionnels connaissent.
            """, context);

        return callGPT(prompt, 800);
    }

    // ─────────────────────────────────────────────────────────
    // Meal Planning
    // ─────────────────────────────────────────────────────────

    /**
     * Generate a weekly meal plan.
     */
    public String generateMealPlan(int days, int caloriesPerDay, String dietaryRestrictions,
                                    String cuisine, int budget, String language) {
        String prompt = String.format("""
            Crée un plan de repas pour %d jours avec les contraintes suivantes:
            - Calories par jour: ~%d kcal
            - Restrictions alimentaires: %s
            - Cuisine préférée: %s
            - Budget approximatif: %d€ pour la période
            - Langue: %s
            
            Pour chaque jour, inclus:
            - Petit-déjeuner, Déjeuner, Dîner et 1 collation
            - Liste d'ingrédients par repas
            - Temps de préparation estimé
            - Calories par repas
            
            Réponds en JSON structuré.
            """, days, caloriesPerDay,
            dietaryRestrictions != null ? dietaryRestrictions : "aucune",
            cuisine != null ? cuisine : "variée",
            budget, language != null ? language : "fr");

        return callGPT(prompt, 4000);
    }

    // ─────────────────────────────────────────────────────────
    // Trend Analysis
    // ─────────────────────────────────────────────────────────

    /**
     * Analyze food trends based on provided data.
     */
    public String analyzeTrends(String trendData) {
        String prompt = String.format("""
            Tu es un analyste des tendances culinaires. Analyse les données suivantes et identifie:
            1. Les 5 principales tendances émergentes
            2. Les cuisines en croissance
            3. Les ingrédients tendance
            4. Prédictions pour les 6 prochains mois
            
            Données: %s
            
            Réponds en JSON structuré.
            """, trendData);

        return callGPT(prompt, 2000);
    }

    // ─────────────────────────────────────────────────────────
    // Waste Optimization
    // ─────────────────────────────────────────────────────────

    /**
     * Suggest recipes to minimize food waste from leftover ingredients.
     */
    public String suggestAntiWasteRecipes(List<String> leftovers, int maxPrepTime) {
        String ingredientList = String.join(", ", leftovers);
        String prompt = String.format("""
            Tu es un expert en cuisine anti-gaspillage. Avec les ingrédients restants suivants: %s
            
            Propose 3 recettes:
            - Temps de préparation max: %d minutes
            - Utilise un maximum de ces ingrédients
            - Indique quels ingrédients supplémentaires basiques pourraient être nécessaires
            - Donne un score anti-gaspillage (1-10)
            
            Réponds en JSON structuré avec titre, ingrédients, étapes, temps, et score.
            """, ingredientList, maxPrepTime);

        return callGPT(prompt, 2000);
    }

    // ─────────────────────────────────────────────────────────
    // Internal: GPT Call
    // ─────────────────────────────────────────────────────────

    private String callGPT(String prompt, int maxTokens) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            log.warn("OpenAI API key not configured. Returning mock response.");
            return "{\"error\": \"AI service not configured. Set cerex.ai.openai-api-key.\"}";
        }

        try {
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content",
                        "Tu es Cerex AI, l'assistant culinaire intelligent de la plateforme Cerex. " +
                        "Tu es expert en gastronomie mondiale, nutrition, et tendances culinaires. " +
                        "Réponds toujours de manière structurée et précise."),
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", maxTokens,
                "temperature", 0.7
            );

            String response = getWebClient()
                .post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            log.info("GPT response received. Prompt length: {} chars", prompt.length());
            return response;

        } catch (Exception e) {
            log.error("Error calling GPT API: {}", e.getMessage());
            return "{\"error\": \"AI service temporarily unavailable: " + e.getMessage() + "\"}";
        }
    }

    private String buildRecipeGenerationPrompt(List<String> ingredients, String cuisine,
                                                 String dietaryRestrictions, int servings,
                                                 String difficultyLevel, String language) {
        return String.format("""
            Génère une recette complète avec les paramètres suivants:
            - Ingrédients disponibles: %s
            - Cuisine: %s
            - Restrictions alimentaires: %s
            - Nombre de portions: %d
            - Niveau de difficulté: %s
            - Langue: %s
            
            La recette doit inclure en JSON:
            {
              "title": "...",
              "description": "...",
              "prepTime": minutes,
              "cookTime": minutes,
              "servings": %d,
              "difficulty": "...",
              "ingredients": [{"name": "...", "quantity": "...", "unit": "..."}],
              "steps": [{"stepNumber": 1, "instruction": "...", "duration": minutes, "tip": "..."}],
              "nutrition": {"calories": ..., "protein": ..., "carbs": ..., "fat": ...},
              "tags": ["..."],
              "chefTip": "..."
            }
            """,
            String.join(", ", ingredients),
            cuisine != null ? cuisine : "libre",
            dietaryRestrictions != null ? dietaryRestrictions : "aucune",
            servings,
            difficultyLevel != null ? difficultyLevel : "intermédiaire",
            language != null ? language : "fr",
            servings);
    }
}

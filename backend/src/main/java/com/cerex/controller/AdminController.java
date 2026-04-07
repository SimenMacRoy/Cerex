package com.cerex.controller;

import com.cerex.dto.ApiResponse;
import com.cerex.dto.auth.AuthResponse;
import com.cerex.dto.recipe.CreateRecipeRequest;
import com.cerex.dto.recipe.RecipeDetailDTO;
import com.cerex.domain.Recipe;
import com.cerex.domain.User;
import com.cerex.domain.UserProfile;
import com.cerex.repository.RecipeRepository;
import com.cerex.repository.UserProfileRepository;
import com.cerex.repository.UserRepository;
import com.cerex.security.CerexUserDetails;
import com.cerex.service.RecipeService;
import com.cerex.service.ai.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin REST controller — accessible only to ADMIN and SUPER_ADMIN roles.
 * Base path: /api/v1/admin
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Admin", description = "Platform administration endpoints")
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;
    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ──────────────────────────────────────────────────────────
    // Dashboard stats
    // ──────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Get global platform statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalUsers    = userRepository.count();
        long totalRecipes  = recipeRepository.count();
        long published     = recipeRepository.countByStatus(Recipe.RecipeStatus.PUBLISHED);
        long pending       = recipeRepository.countByStatus(Recipe.RecipeStatus.PENDING_REVIEW);
        long drafts        = recipeRepository.countByStatus(Recipe.RecipeStatus.DRAFT);
        long rejected      = recipeRepository.countByStatus(Recipe.RecipeStatus.REJECTED);

        // New users last 7 days
        Instant since7d = Instant.now().minus(7, ChronoUnit.DAYS);
        long newUsers7d = userRepository.countByCreatedAtAfter(since7d);

        // New recipes last 7 days
        long newRecipes7d = recipeRepository.countByCreatedAtAfter(since7d);

        stats.put("totalUsers",   totalUsers);
        stats.put("newUsers7d",   newUsers7d);
        stats.put("totalRecipes", totalRecipes);
        stats.put("newRecipes7d", newRecipes7d);
        stats.put("published",    published);
        stats.put("pending",      pending);
        stats.put("drafts",       drafts);
        stats.put("rejected",     rejected);

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    // ──────────────────────────────────────────────────────────
    // Users management
    // ──────────────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        var users = userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<Map<String, Object>> result = users.getContent().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",        u.getId());
            m.put("email",     u.getEmail());
            m.put("role",      u.getRole());
            m.put("status",    u.getStatus());
            m.put("createdAt", u.getCreatedAt());
            // enrich with profile display name
            profileRepository.findByUserId(u.getId()).ifPresent(p -> {
                m.put("displayName", p.getDisplayName());
                m.put("avatarUrl",   p.getAvatarUrl());
            });
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content",       result);
        response.put("totalElements", users.getTotalElements());
        response.put("totalPages",    users.getTotalPages());
        response.put("number",        users.getNumber());
        response.put("size",          users.getSize());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>ok(response));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Change a user's role")
    public ResponseEntity<ApiResponse<String>> changeRole(
            @PathVariable UUID userId,
            @RequestParam String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(User.UserRole.valueOf(role.toUpperCase()));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Role updated to " + role));
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Suspend or activate a user")
    public ResponseEntity<ApiResponse<String>> changeStatus(
            @PathVariable UUID userId,
            @RequestParam String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(User.UserStatus.valueOf(status.toUpperCase()));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Status updated to " + status));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Hard-delete a user account")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable UUID userId) {
        userRepository.deleteById(userId);
        return ResponseEntity.ok(ApiResponse.ok("User deleted"));
    }

    // ──────────────────────────────────────────────────────────
    // Recipes moderation
    // ──────────────────────────────────────────────────────────

    @GetMapping("/recipes")
    @Operation(summary = "List all recipes (any status)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listAllRecipes(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(required = false)      String status) {

        org.springframework.data.domain.Page<Recipe> recipes;
        if (status != null && !status.isBlank()) {
            Recipe.RecipeStatus rs = Recipe.RecipeStatus.valueOf(status.toUpperCase());
            recipes = recipeRepository.findByStatus(rs, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            recipes = recipeRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }

        List<Map<String, Object>> items = recipes.getContent().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           r.getId());
            m.put("title",        r.getTitle());
            m.put("status",       r.getStatus());
            m.put("cuisineType",  r.getCuisineType());
            m.put("difficultyLevel", r.getDifficultyLevel());
            m.put("authorId",     r.getAuthorId());
            m.put("avgRating",    r.getAvgRating());
            m.put("viewCount",    r.getViewCount());
            m.put("createdAt",    r.getCreatedAt());
            m.put("publishedAt",  r.getPublishedAt());
            m.put("coverImageUrl", r.getCoverImageUrl());
            // enrich with author name
            profileRepository.findByUserId(r.getAuthorId()).ifPresent(p ->
                m.put("authorName", p.getDisplayName())
            );
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content",       items);
        response.put("totalElements", recipes.getTotalElements());
        response.put("totalPages",    recipes.getTotalPages());
        response.put("number",        recipes.getNumber());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ──────────────────────────────────────────────────────────
    // AI Recipe Generation
    // ──────────────────────────────────────────────────────────

    @PostMapping("/ai/generate-recipe")
    @Operation(summary = "Generate a complete recipe from its name using AI, then save as draft")
    public ResponseEntity<ApiResponse<RecipeDetailDTO>> generateRecipeWithAI(
            @RequestParam String recipeName,
            @AuthenticationPrincipal CerexUserDetails currentUser) {

        // 1. Ask AI to generate the full recipe JSON
        String json = aiService.generateRecipeFromName(recipeName);

        // 2. Parse the JSON into a CreateRecipeRequest
        CreateRecipeRequest request;
        try {
            JsonNode node = objectMapper.readTree(json);

            request = new CreateRecipeRequest();
            request.setTitle(text(node, "title", recipeName));
            request.setDescription(text(node, "description", ""));
            request.setStory(text(node, "story", null));
            request.setCuisineType(text(node, "cuisineType", "AFRICAN"));
            request.setCourseType(text(node, "courseType", null));
            request.setRecipeType(text(node, "recipeType", "DISH"));
            request.setDifficultyLevel(text(node, "difficultyLevel", "MEDIUM"));
            request.setPrepTimeMinutes(intVal(node, "prepTimeMinutes", 20));
            request.setCookTimeMinutes(intVal(node, "cookTimeMinutes", 30));
            request.setServings(intVal(node, "servings", 4));
            request.setCaloriesKcal(decimal(node, "caloriesKcal"));
            request.setProteinG(decimal(node, "proteinG"));
            request.setCarbsG(decimal(node, "carbsG"));
            request.setFatG(decimal(node, "fatG"));
            request.setFiberG(decimal(node, "fiberG"));
            request.setIsVegetarian(bool(node, "isVegetarian"));
            request.setIsVegan(bool(node, "isVegan"));
            request.setIsGlutenFree(bool(node, "isGlutenFree"));
            request.setIsHalal(bool(node, "isHalal"));
            request.setIsDairyFree(bool(node, "isDairyFree"));
            // Mark as AI-generated via tags
            List<String> aiTags = new ArrayList<>();
            aiTags.add("ai-generated");

            // Tags
            if (node.has("tags") && node.get("tags").isArray()) {
                node.get("tags").forEach(t -> aiTags.add(t.asText()));
            }
            request.setTags(aiTags.toArray(new String[0]));

            // Ingredients
            if (node.has("ingredients") && node.get("ingredients").isArray()) {
                List<CreateRecipeRequest.IngredientRequest> ings = new ArrayList<>();
                for (JsonNode ing : node.get("ingredients")) {
                    CreateRecipeRequest.IngredientRequest ir = new CreateRecipeRequest.IngredientRequest();
                    ir.setName(text(ing, "name", "Ingrédient"));
                    double qty = ing.path("quantity").asDouble(1.0);
                    ir.setQuantity(BigDecimal.valueOf(qty));
                    ir.setUnit(text(ing, "unit", "unité"));
                    ir.setDisplayText(text(ing, "displayText", null));
                    ir.setIsOptional(bool(ing, "isOptional"));
                    ir.setGroupName(text(ing, "groupName", null));
                    ings.add(ir);
                }
                request.setIngredients(ings);
            }

            // Steps
            if (node.has("steps") && node.get("steps").isArray()) {
                List<CreateRecipeRequest.StepRequest> steps = new ArrayList<>();
                for (JsonNode step : node.get("steps")) {
                    CreateRecipeRequest.StepRequest sr = new CreateRecipeRequest.StepRequest();
                    sr.setInstruction(text(step, "instruction", "Étape"));
                    sr.setDurationMinutes(step.path("durationMinutes").isNull() ? null : step.path("durationMinutes").asInt());
                    sr.setTip(text(step, "tip", null));
                    steps.add(sr);
                }
                request.setSteps(steps);
            }

        } catch (Exception e) {
            log.error("Failed to parse AI recipe JSON for '{}': {}", recipeName, e.getMessage());
            log.debug("Raw AI response: {}", json);
            String userMsg = json.contains("QUOTA_EXCEEDED") || json.contains("insufficient_quota")
                ? "Quota OpenAI dépassé. Ajoutez des crédits sur platform.openai.com/settings/billing"
                : json.contains("API key")
                ? "Clé API OpenAI invalide."
                : "L'IA n'a pas retourné un JSON valide. Réessayez.";
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(userMsg));
        }

        // 3. Save as draft via RecipeService
        RecipeDetailDTO saved = recipeService.createRecipe(currentUser.getUserId(), request);
        log.info("AI-generated recipe '{}' saved as draft {}", recipeName, saved.getId());

        return ResponseEntity.ok(ApiResponse.ok(saved, "Recette générée et sauvegardée comme brouillon"));
    }

    // ── JSON parsing helpers ──────────────────────────────────

    private String text(JsonNode node, String field, String fallback) {
        JsonNode n = node.path(field);
        return (n.isMissingNode() || n.isNull() || n.asText().isBlank()) ? fallback : n.asText().trim();
    }

    private int intVal(JsonNode node, String field, Integer fallback) {
        JsonNode n = node.path(field);
        return (n.isMissingNode() || n.isNull()) ? (fallback != null ? fallback : 0) : n.asInt();
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return (n.isMissingNode() || n.isNull()) ? null : BigDecimal.valueOf(n.asDouble());
    }

    private Boolean bool(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return (n.isMissingNode() || n.isNull()) ? false : n.asBoolean();
    }
}

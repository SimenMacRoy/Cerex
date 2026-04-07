package com.cerex.service;

import com.cerex.domain.*;
import com.cerex.dto.recipe.*;
import com.cerex.exception.BusinessException;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.exception.UnauthorizedException;
import com.cerex.mapper.RecipeMapper;
import com.cerex.repository.IngredientRepository;
import com.cerex.repository.RecipeRepository;
import com.cerex.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service layer for recipe lifecycle management.
 *
 * <p>Handles creation, publishing, moderation, search delegation, and
 * engagement metric updates.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserProfileRepository profileRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeMapper recipeMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    // ─────────────────────────────────────────────────────────
    // READ Operations
    // ─────────────────────────────────────────────────────────

    /**
     * Get a recipe by slug (public detail page).
     */
    @Cacheable(value = "recipeDetail", key = "#slug")
    public RecipeDetailDTO getRecipeBySlug(String slug) {
        Recipe recipe = recipeRepository.findBySlugAndStatus(slug, Recipe.RecipeStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe", "slug", slug));

        // Async view count increment
        incrementViewCountAsync(recipe.getId());

        return buildDetailDTO(recipe);
    }

    /**
     * Get a recipe by ID.
     */
    public RecipeDetailDTO getRecipeById(UUID id) {
        Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", id));

        return buildDetailDTO(recipe);
    }

    /**
     * List published recipes (no filters).
     */
    public Page<RecipeCardDTO> listPublishedRecipes(Pageable pageable) {
        return recipeRepository.findByStatus(Recipe.RecipeStatus.PUBLISHED, pageable).map(this::buildCardDTO);
    }

    /**
     * Get published recipes by a specific author.
     */
    public Page<RecipeCardDTO> getRecipesByAuthor(UUID authorId, Pageable pageable) {
        Page<Recipe> recipePage = recipeRepository.findPublishedByAuthor(authorId, pageable);
        return recipePage.map(this::buildCardDTO);
    }

    /**
     * Get all recipes (any status) for the authenticated author.
     */
    public Page<RecipeCardDTO> getMyRecipes(UUID authorId, Pageable pageable) {
        Page<Recipe> recipePage = recipeRepository.findAllByAuthor(authorId, pageable);
        return recipePage.map(this::buildCardDTO);
    }

    /**
     * Get trending recipes ordered by engagement score.
     */
    public Page<RecipeCardDTO> getTrendingRecipes(Pageable pageable) {
        return recipeRepository.findTrendingRecipes(pageable).map(this::buildCardDTO);
    }

    /**
     * Filter recipes by cultural/geographic criteria.
     */
    public Page<RecipeCardDTO> filterByCulture(
            UUID continentId, UUID countryId, UUID cultureId,
            Boolean isVegan, Boolean isGlutenFree, Boolean isHalal,
            Pageable pageable) {
        Page<Recipe> page = recipeRepository.findByCulturalFilters(
            continentId, countryId, cultureId, isVegan, isGlutenFree, isHalal, pageable);
        return page.map(this::buildCardDTO);
    }

    /**
     * Filter recipes by optional cuisine type, difficulty level and keyword search.
     * Supports the frontend combined filters on the recipes listing page.
     */
    public Page<RecipeCardDTO> filterRecipes(
            String cuisineType, String difficultyLevel, String keyword, Pageable pageable) {

        String cuisine = (cuisineType != null && !cuisineType.isBlank()) ? cuisineType : null;
        String difficulty = (difficultyLevel != null && !difficultyLevel.isBlank()) ? difficultyLevel.toUpperCase() : null;
        String search = (keyword != null && !keyword.isBlank()) ? keyword : null;

        // Strip JPA sort from pageable — native query has its own ORDER BY
        Pageable unsorted = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Recipe> page = recipeRepository.findByFilters(cuisine, difficulty, search, unsorted);
        return page.map(this::buildCardDTO);
    }

    /**
     * Full-text search for recipes using PostgreSQL tsvector.
     */
    public Page<RecipeCardDTO> searchRecipes(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return listPublishedRecipes(pageable);
        }
        // Use the JPQL-based filter as a simpler fallback that returns entities
        Page<Recipe> page = recipeRepository.findByFilters(null, null, query, pageable);
        return page.map(this::buildCardDTO);
    }

    /**
     * Filter by difficulty and time.
     */
    public Page<RecipeCardDTO> filterByDifficultyAndTime(
            Recipe.DifficultyLevel difficulty, Integer maxTime,
            java.math.BigDecimal minRating, Pageable pageable) {
        Page<Recipe> page = recipeRepository.findByDifficultyAndTime(
            difficulty, maxTime, minRating, pageable);
        return page.map(this::buildCardDTO);
    }

    /**
     * Build a grocery shopping list for a recipe, scaled to the requested servings.
     */
    public java.util.Map<String, Object> getGroceryList(UUID recipeId, int requestedServings) {
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", recipeId));

        int baseServings = recipe.getServings() != null ? recipe.getServings() : 1;
        double ratio = requestedServings > 0
            ? (double) requestedServings / baseServings
            : 1.0;

        java.math.BigDecimal totalPrice = java.math.BigDecimal.ZERO;
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();

        for (com.cerex.domain.RecipeIngredient ri : recipe.getIngredients()) {
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            com.cerex.domain.Ingredient ing = ri.getIngredient();

            java.math.BigDecimal qty = ri.getQuantity() != null
                ? ri.getQuantity().multiply(java.math.BigDecimal.valueOf(ratio)).setScale(2, java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO;

            java.math.BigDecimal unitPrice = ing != null && ing.getEstimatedPriceFcfa() != null
                ? ing.getEstimatedPriceFcfa()
                : java.math.BigDecimal.valueOf(300);

            java.math.BigDecimal lineTotal = unitPrice.multiply(qty).setScale(0, java.math.RoundingMode.HALF_UP);
            if (!Boolean.TRUE.equals(ri.getIsOptional())) {
                totalPrice = totalPrice.add(lineTotal);
            }

            item.put("name",         ing != null ? ing.getName() : ri.getDisplayText());
            item.put("quantity",     qty);
            item.put("unit",         ri.getUnit());
            item.put("displayText",  ri.getDisplayText());
            item.put("isOptional",   ri.getIsOptional());
            item.put("groupName",    ri.getGroupName());
            item.put("unitPriceFcfa", unitPrice);
            item.put("totalPriceFcfa", lineTotal);
            items.add(item);
        }

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("recipeId",       recipe.getId());
        result.put("recipeTitle",    recipe.getTitle());
        result.put("baseServings",   baseServings);
        result.put("requestedServings", requestedServings);
        result.put("ingredients",    items);
        result.put("totalEstimatedPriceFcfa", totalPrice);
        return result;
    }

    // ─────────────────────────────────────────────────────────
    // WRITE Operations
    // ─────────────────────────────────────────────────────────

    /**
     * Create a new recipe.
     */
    @Transactional
    @CacheEvict(value = {"recipes", "recipeDetail"}, allEntries = true)
    public RecipeDetailDTO createRecipe(UUID authorId, CreateRecipeRequest request) {
        Recipe recipe = Recipe.builder()
            .authorId(authorId)
            .title(request.getTitle())
            .slug(generateSlug(request.getTitle()))
            .description(request.getDescription())
            .story(request.getStory())
            .continentId(request.getContinentId())
            .countryId(request.getCountryId())
            .cultureId(request.getCultureId())
            .categoryId(request.getCategoryId())
            .recipeType(request.getRecipeType() != null
                ? Recipe.RecipeType.valueOf(request.getRecipeType())
                : Recipe.RecipeType.DISH)
            .cuisineType(request.getCuisineType())
            .courseType(request.getCourseType())
            .difficultyLevel(Recipe.DifficultyLevel.valueOf(request.getDifficultyLevel()))
            .spiceLevel(request.getSpiceLevel() != null ? request.getSpiceLevel() : 1)
            .prepTimeMinutes(request.getPrepTimeMinutes())
            .cookTimeMinutes(request.getCookTimeMinutes())
            .restTimeMinutes(request.getRestTimeMinutes() != null ? request.getRestTimeMinutes() : 0)
            .servings(request.getServings())
            .servingsUnit(request.getServingsUnit() != null ? request.getServingsUnit() : "persons")
            .caloriesKcal(request.getCaloriesKcal())
            .proteinG(request.getProteinG())
            .carbsG(request.getCarbsG())
            .fatG(request.getFatG())
            .fiberG(request.getFiberG())
            .sugarG(request.getSugarG())
            .sodiumMg(request.getSodiumMg())
            .isVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false)
            .isVegan(request.getIsVegan() != null ? request.getIsVegan() : false)
            .isGlutenFree(request.getIsGlutenFree() != null ? request.getIsGlutenFree() : false)
            .isDairyFree(request.getIsDairyFree() != null ? request.getIsDairyFree() : false)
            .isHalal(request.getIsHalal() != null ? request.getIsHalal() : false)
            .isKosher(request.getIsKosher() != null ? request.getIsKosher() : false)
            .isNutFree(request.getIsNutFree() != null ? request.getIsNutFree() : false)
            .isLowCarb(request.getIsLowCarb() != null ? request.getIsLowCarb() : false)
            .coverImageUrl(request.getCoverImageUrl())
            .videoUrl(request.getVideoUrl())
            .tags(request.getTags() != null ? request.getTags() : new String[]{})
            .status(Recipe.RecipeStatus.DRAFT)
            .build();

        // Add ingredients — look up by ID or by name (auto-create if not found)
        if (request.getIngredients() != null) {
            int order = 0;
            for (CreateRecipeRequest.IngredientRequest ingReq : request.getIngredients()) {
                Ingredient ingredient = null;

                if (ingReq.getIngredientId() != null) {
                    ingredient = ingredientRepository.findById(ingReq.getIngredientId()).orElse(null);
                }

                if (ingredient == null && ingReq.getName() != null && !ingReq.getName().isBlank()) {
                    String name = ingReq.getName().trim();
                    ingredient = ingredientRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> ingredientRepository.save(
                            Ingredient.builder().name(name).build()
                        ));
                }

                if (ingredient == null) continue; // skip if no name and no id

                RecipeIngredient ri = RecipeIngredient.builder()
                    .ingredient(ingredient)
                    .quantity(ingReq.getQuantity())
                    .unit(ingReq.getUnit())
                    .displayText(ingReq.getDisplayText())
                    .isOptional(ingReq.getIsOptional() != null ? ingReq.getIsOptional() : false)
                    .groupName(ingReq.getGroupName())
                    .displayOrder(order++)
                    .build();
                recipe.addIngredient(ri);
            }
        }

        // Add steps
        if (request.getSteps() != null) {
            for (CreateRecipeRequest.StepRequest stepReq : request.getSteps()) {
                RecipeStep step = RecipeStep.builder()
                    .instruction(stepReq.getInstruction())
                    .durationMinutes(stepReq.getDurationMinutes())
                    .imageUrl(stepReq.getImageUrl())
                    .tip(stepReq.getTip())
                    .build();
                recipe.addStep(step);
            }
        }

        recipe = recipeRepository.save(recipe);
        log.info("Recipe created: {} by author {}", recipe.getId(), authorId);

        return buildDetailDTO(recipe);
    }

    /**
     * Publish a draft recipe (submit for moderation review).
     */
    @Transactional
    @CacheEvict(value = {"recipes", "recipeDetail"}, allEntries = true)
    public RecipeDetailDTO publishRecipe(UUID recipeId, UUID authorId) {
        Recipe recipe = recipeRepository.findByIdAndAuthorId(recipeId, authorId)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", recipeId));

        recipe.publish(); // domain method validates state
        recipe = recipeRepository.save(recipe);

        log.info("Recipe submitted for review: {}", recipeId);
        return buildDetailDTO(recipe);
    }

    /**
     * Approve a recipe (moderator action).
     */
    @Transactional
    @CacheEvict(value = {"recipes", "recipeDetail"}, allEntries = true)
    public RecipeDetailDTO approveRecipe(UUID recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", recipeId));

        if (recipe.getStatus() != Recipe.RecipeStatus.PENDING_REVIEW) {
            throw new BusinessException("Recipe is not pending review");
        }

        recipe.approve();
        recipe = recipeRepository.save(recipe);

        // Publish event for search indexing, social fanout, etc.
        try {
            kafkaTemplate.send("cerex.recipe.published", recipe.getId().toString(), recipe.getId());
        } catch (Exception e) {
            log.warn("Failed to publish recipe.published event: {}", e.getMessage());
        }

        log.info("Recipe approved and published: {}", recipeId);
        return buildDetailDTO(recipe);
    }

    /**
     * Reject a recipe with a moderation note.
     */
    @Transactional
    @CacheEvict(value = {"recipes", "recipeDetail"}, allEntries = true)
    public RecipeDetailDTO rejectRecipe(UUID recipeId, String reason) {
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", recipeId));

        recipe.reject(reason);
        recipe = recipeRepository.save(recipe);

        log.info("Recipe rejected: {} - reason: {}", recipeId, reason);
        return buildDetailDTO(recipe);
    }

    /**
     * Toggle like on a recipe.
     */
    @Transactional
    @CacheEvict(value = "recipeDetail", key = "#recipeId")
    public void toggleLike(UUID recipeId, UUID userId, boolean liked) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResourceNotFoundException("Recipe", "id", recipeId);
        }
        int delta = liked ? 1 : -1;
        recipeRepository.updateLikeCount(recipeId, delta);
    }

    /**
     * Soft delete a recipe (author only).
     */
    @Transactional
    @CacheEvict(value = {"recipes", "recipeDetail"}, allEntries = true)
    public void deleteRecipe(UUID recipeId, UUID authorId) {
        if (!recipeRepository.existsByIdAndAuthorId(recipeId, authorId)) {
            throw new UnauthorizedException("You can only delete your own recipes");
        }
        recipeRepository.deleteById(recipeId); // triggers @SQLDelete soft delete
        log.info("Recipe soft-deleted: {} by author {}", recipeId, authorId);
    }

    // ─────────────────────────────────────────────────────────
    // Helper Methods
    // ─────────────────────────────────────────────────────────

    @Async
    public void incrementViewCountAsync(UUID recipeId) {
        try {
            recipeRepository.incrementViewCount(recipeId);
        } catch (Exception e) {
            log.warn("Failed to increment view count for recipe {}: {}", recipeId, e.getMessage());
        }
    }

    private RecipeDetailDTO buildDetailDTO(Recipe recipe) {
        RecipeDetailDTO dto = recipeMapper.toDetailDTO(recipe);

        // Enrich with author info
        var profile = profileRepository.findByUserId(recipe.getAuthorId()).orElse(null);
        dto.setAuthor(RecipeDetailDTO.AuthorDTO.builder()
            .id(recipe.getAuthorId())
            .displayName(profile != null ? profile.getDisplayName() : "Unknown")
            .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
            .isVerifiedChef(profile != null ? profile.getIsVerifiedChef() : false)
            .recipeCount(profile != null ? profile.getRecipeCount() : 0)
            .followerCount(profile != null ? profile.getFollowerCount() : 0)
            .build());

        return dto;
    }

    private RecipeCardDTO buildCardDTO(Recipe recipe) {
        RecipeCardDTO dto = recipeMapper.toCardDTO(recipe);

        // Enrich with author info
        var profile = profileRepository.findByUserId(recipe.getAuthorId()).orElse(null);
        dto.setAuthorName(profile != null ? profile.getDisplayName() : "Unknown");
        dto.setAuthorAvatarUrl(profile != null ? profile.getAvatarUrl() : null);

        // Always expose the status so the frontend can display moderation state
        dto.setStatus(recipe.getStatus() != null ? recipe.getStatus().name() : null);

        return dto;
    }

    /**
     * Generate a URL-safe slug from a recipe title.
     * Example: "Poulet Yassa Sénégalais" → "poulet-yassa-senegalais"
     */
    private String generateSlug(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        String ascii = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        String slug = WHITESPACE.matcher(ascii).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        slug = slug.toLowerCase(Locale.ROOT).replaceAll("-{2,}", "-")
            .replaceAll("^-|-$", "");

        // Ensure uniqueness
        String baseSlug = slug;
        int counter = 1;
        while (recipeRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }
}

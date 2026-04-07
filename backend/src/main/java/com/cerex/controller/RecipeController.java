package com.cerex.controller;

import com.cerex.dto.ApiResponse;
import com.cerex.dto.recipe.CreateRecipeRequest;
import com.cerex.dto.recipe.RecipeCardDTO;
import com.cerex.dto.recipe.RecipeDetailDTO;
import com.cerex.security.CerexUserDetails;
import com.cerex.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for recipe CRUD and lifecycle management.
 *
 * <p>Base path: {@code /api/v1/recipes}
 */
@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipes", description = "Recipe CRUD, publishing, and discovery")
public class RecipeController {

    private final RecipeService recipeService;

    // ─────────────────────────────────────────────────────────
    // Public Read Endpoints
    // ─────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List published recipes with optional cuisine/difficulty filters")
    public ResponseEntity<ApiResponse<Page<RecipeCardDTO>>> listRecipes(
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        boolean hasFilters = (cuisine != null && !cuisine.isBlank())
                          || (difficulty != null && !difficulty.isBlank())
                          || (q != null && !q.isBlank());

        Page<RecipeCardDTO> recipes;
        if (hasFilters) {
            recipes = recipeService.filterRecipes(cuisine, difficulty, q, pageable);
        } else {
            recipes = recipeService.listPublishedRecipes(pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @GetMapping("/search")
    @Operation(summary = "Search recipes by keyword with optional filters")
    public ResponseEntity<ApiResponse<Page<RecipeCardDTO>>> searchRecipes(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String difficulty,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RecipeCardDTO> recipes = recipeService.filterRecipes(cuisine, difficulty, query, pageable);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending recipes by engagement score")
    public ResponseEntity<ApiResponse<Page<RecipeCardDTO>>> getTrendingRecipes(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(recipeService.getTrendingRecipes(pageable)));
    }

    @GetMapping("/user/{authorId}")
    @Operation(summary = "Get published recipes by author ID (alias for /by-author)")
    public ResponseEntity<ApiResponse<Page<RecipeCardDTO>>> getRecipesByAuthorAlias(
            @PathVariable UUID authorId,
            @PageableDefault(size = 12, sort = "publishedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<RecipeCardDTO> recipes = recipeService.getRecipesByAuthor(authorId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @GetMapping("/{idOrSlug}")
    @Operation(summary = "Get full recipe detail by ID or slug")
    public ResponseEntity<ApiResponse<RecipeDetailDTO>> getRecipe(
            @PathVariable String idOrSlug) {
        RecipeDetailDTO recipe;
        try {
            UUID id = UUID.fromString(idOrSlug);
            recipe = recipeService.getRecipeById(id);
        } catch (IllegalArgumentException e) {
            // Not a UUID — treat as slug
            recipe = recipeService.getRecipeBySlug(idOrSlug);
        }
        return ResponseEntity.ok(ApiResponse.ok(recipe));
    }

    @GetMapping("/by-author/{authorId}")
    @Operation(summary = "Get published recipes by a specific author")
    public ResponseEntity<ApiResponse<Page<RecipeCardDTO>>> getRecipesByAuthor(
            @PathVariable UUID authorId,
            @PageableDefault(size = 12, sort = "publishedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<RecipeCardDTO> recipes = recipeService.getRecipesByAuthor(authorId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @GetMapping("/explore/continent/{continentId}")
    @Operation(summary = "Explore recipes by continent with cultural filters")
    public ResponseEntity<ApiResponse<Page<RecipeCardDTO>>> exploreByCulture(
            @PathVariable UUID continentId,
            @RequestParam(required = false) UUID countryId,
            @RequestParam(required = false) UUID cultureId,
            @RequestParam(required = false) Boolean isVegan,
            @RequestParam(required = false) Boolean isGlutenFree,
            @RequestParam(required = false) Boolean isHalal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RecipeCardDTO> recipes = recipeService.filterByCulture(
            continentId, countryId, cultureId, isVegan, isGlutenFree, isHalal, pageable);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    // ─────────────────────────────────────────────────────────
    // Authenticated Write Endpoints
    // ─────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new recipe")
    public ResponseEntity<ApiResponse<RecipeDetailDTO>> createRecipe(
            @AuthenticationPrincipal CerexUserDetails currentUser,
            @Valid @RequestBody CreateRecipeRequest request) {
        RecipeDetailDTO recipe = recipeService.createRecipe(currentUser.getUserId(), request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(recipe, "Recipe created successfully"));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Submit a draft recipe for moderation review")
    public ResponseEntity<ApiResponse<RecipeDetailDTO>> publishRecipe(
            @PathVariable UUID id,
            @AuthenticationPrincipal CerexUserDetails currentUser) {
        RecipeDetailDTO recipe = recipeService.publishRecipe(id, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(recipe, "Recipe submitted for review"));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Toggle like on a recipe")
    public ResponseEntity<ApiResponse<String>> toggleLike(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "true") boolean liked,
            @AuthenticationPrincipal CerexUserDetails currentUser) {
        recipeService.toggleLike(id, currentUser.getUserId(), liked);
        return ResponseEntity.ok(ApiResponse.ok(liked ? "Recipe liked" : "Like removed"));
    }

    @GetMapping("/{id}/grocery-list")
    @Operation(summary = "Get grocery shopping list for a recipe with prices in FCFA")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getGroceryList(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "4") int servings) {
        java.util.Map<String, Object> list = recipeService.getGroceryList(id, servings);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a recipe (author only)")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable UUID id,
            @AuthenticationPrincipal CerexUserDetails currentUser) {
        recipeService.deleteRecipe(id, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get all recipes by the current user (any status)")
    public ResponseEntity<ApiResponse<Page<RecipeCardDTO>>> getMyRecipes(
            @AuthenticationPrincipal CerexUserDetails currentUser,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<RecipeCardDTO> recipes = recipeService.getMyRecipes(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    // ─────────────────────────────────────────────────────────
    // Moderation Endpoints (ADMIN / MODERATOR)
    // ─────────────────────────────────────────────────────────

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Approve a recipe for publication (moderator only)")
    public ResponseEntity<ApiResponse<RecipeDetailDTO>> approveRecipe(@PathVariable UUID id) {
        RecipeDetailDTO recipe = recipeService.approveRecipe(id);
        return ResponseEntity.ok(ApiResponse.ok(recipe, "Recipe approved and published"));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Reject a recipe with a moderation note (moderator only)")
    public ResponseEntity<ApiResponse<RecipeDetailDTO>> rejectRecipe(
            @PathVariable UUID id,
            @RequestParam String reason) {
        RecipeDetailDTO recipe = recipeService.rejectRecipe(id, reason);
        return ResponseEntity.ok(ApiResponse.ok(recipe, "Recipe rejected"));
    }
}

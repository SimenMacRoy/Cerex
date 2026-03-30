package com.cerex.mapper;

import com.cerex.domain.*;
import com.cerex.dto.recipe.RecipeCardDTO;
import com.cerex.dto.recipe.RecipeDetailDTO;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Recipe entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecipeMapper {

    // ── Recipe → RecipeCardDTO ──────────────────────────────

    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "authorAvatarUrl", ignore = true)
    @Mapping(target = "totalTimeMinutes", expression = "java(recipe.getTotalTimeMinutes())")
    RecipeCardDTO toCardDTO(Recipe recipe);

    List<RecipeCardDTO> toCardDTOList(List<Recipe> recipes);

    // ── Recipe → RecipeDetailDTO ────────────────────────────

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "nutrition", source = "recipe")
    @Mapping(target = "ingredients", source = "recipe.ingredients")
    @Mapping(target = "steps", source = "recipe.steps")
    @Mapping(target = "totalTimeMinutes", expression = "java(recipe.getTotalTimeMinutes())")
    @Mapping(target = "recipeType", expression = "java(recipe.getRecipeType().name())")
    @Mapping(target = "difficultyLevel", expression = "java(recipe.getDifficultyLevel().name())")
    @Mapping(target = "status", expression = "java(recipe.getStatus().name())")
    RecipeDetailDTO toDetailDTO(Recipe recipe);

    // ── Nutrition mapping ───────────────────────────────────

    @Mapping(target = "caloriesKcal", source = "caloriesKcal")
    @Mapping(target = "proteinG", source = "proteinG")
    @Mapping(target = "carbsG", source = "carbsG")
    @Mapping(target = "fatG", source = "fatG")
    @Mapping(target = "fiberG", source = "fiberG")
    @Mapping(target = "sugarG", source = "sugarG")
    @Mapping(target = "sodiumMg", source = "sodiumMg")
    RecipeDetailDTO.NutritionDTO toNutritionDTO(Recipe recipe);

    // ── RecipeIngredient → IngredientLineDTO ────────────────

    @Mapping(target = "name", source = "ingredient.name")
    RecipeDetailDTO.IngredientLineDTO toIngredientLineDTO(RecipeIngredient recipeIngredient);

    List<RecipeDetailDTO.IngredientLineDTO> toIngredientLineDTOList(List<RecipeIngredient> ingredients);

    // ── RecipeStep → StepDTO ────────────────────────────────

    RecipeDetailDTO.StepDTO toStepDTO(RecipeStep step);

    List<RecipeDetailDTO.StepDTO> toStepDTOList(List<RecipeStep> steps);
}

package com.cerex.mapper;

import com.cerex.domain.Ingredient;
import com.cerex.domain.Recipe;
import com.cerex.domain.RecipeIngredient;
import com.cerex.domain.RecipeStep;
import com.cerex.dto.recipe.RecipeCardDTO;
import com.cerex.dto.recipe.RecipeDetailDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-06T22:29:35-0400",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RecipeMapperImpl implements RecipeMapper {

    @Override
    public RecipeCardDTO toCardDTO(Recipe recipe) {
        if ( recipe == null ) {
            return null;
        }

        RecipeCardDTO.RecipeCardDTOBuilder recipeCardDTO = RecipeCardDTO.builder();

        recipeCardDTO.id( recipe.getId() );
        recipeCardDTO.title( recipe.getTitle() );
        recipeCardDTO.slug( recipe.getSlug() );
        recipeCardDTO.description( recipe.getDescription() );
        recipeCardDTO.coverImageUrl( recipe.getCoverImageUrl() );
        recipeCardDTO.thumbnailUrl( recipe.getThumbnailUrl() );
        recipeCardDTO.authorId( recipe.getAuthorId() );
        recipeCardDTO.cuisineType( recipe.getCuisineType() );
        recipeCardDTO.courseType( recipe.getCourseType() );
        if ( recipe.getDifficultyLevel() != null ) {
            recipeCardDTO.difficultyLevel( recipe.getDifficultyLevel().name() );
        }
        recipeCardDTO.spiceLevel( recipe.getSpiceLevel() );
        recipeCardDTO.prepTimeMinutes( recipe.getPrepTimeMinutes() );
        recipeCardDTO.cookTimeMinutes( recipe.getCookTimeMinutes() );
        recipeCardDTO.servings( recipe.getServings() );
        recipeCardDTO.avgRating( recipe.getAvgRating() );
        recipeCardDTO.ratingCount( recipe.getRatingCount() );
        recipeCardDTO.likeCount( recipe.getLikeCount() );
        if ( recipe.getViewCount() != null ) {
            recipeCardDTO.viewCount( recipe.getViewCount().intValue() );
        }
        recipeCardDTO.isVegan( recipe.getIsVegan() );
        recipeCardDTO.isVegetarian( recipe.getIsVegetarian() );
        recipeCardDTO.isGlutenFree( recipe.getIsGlutenFree() );
        recipeCardDTO.isHalal( recipe.getIsHalal() );
        recipeCardDTO.isPremium( recipe.getIsPremium() );
        recipeCardDTO.isAiGenerated( recipe.getIsAiGenerated() );
        recipeCardDTO.isFeatured( recipe.getIsFeatured() );
        String[] tags = recipe.getTags();
        if ( tags != null ) {
            recipeCardDTO.tags( Arrays.copyOf( tags, tags.length ) );
        }
        recipeCardDTO.publishedAt( recipe.getPublishedAt() );
        if ( recipe.getStatus() != null ) {
            recipeCardDTO.status( recipe.getStatus().name() );
        }

        recipeCardDTO.totalTimeMinutes( recipe.getTotalTimeMinutes() );

        return recipeCardDTO.build();
    }

    @Override
    public List<RecipeCardDTO> toCardDTOList(List<Recipe> recipes) {
        if ( recipes == null ) {
            return null;
        }

        List<RecipeCardDTO> list = new ArrayList<RecipeCardDTO>( recipes.size() );
        for ( Recipe recipe : recipes ) {
            list.add( toCardDTO( recipe ) );
        }

        return list;
    }

    @Override
    public RecipeDetailDTO toDetailDTO(Recipe recipe) {
        if ( recipe == null ) {
            return null;
        }

        RecipeDetailDTO.RecipeDetailDTOBuilder recipeDetailDTO = RecipeDetailDTO.builder();

        recipeDetailDTO.nutrition( toNutritionDTO( recipe ) );
        recipeDetailDTO.ingredients( toIngredientLineDTOList( recipe.getIngredients() ) );
        recipeDetailDTO.steps( toStepDTOList( recipe.getSteps() ) );
        recipeDetailDTO.id( recipe.getId() );
        recipeDetailDTO.title( recipe.getTitle() );
        recipeDetailDTO.slug( recipe.getSlug() );
        recipeDetailDTO.description( recipe.getDescription() );
        recipeDetailDTO.story( recipe.getStory() );
        recipeDetailDTO.coverImageUrl( recipe.getCoverImageUrl() );
        recipeDetailDTO.videoUrl( recipe.getVideoUrl() );
        recipeDetailDTO.cuisineType( recipe.getCuisineType() );
        recipeDetailDTO.courseType( recipe.getCourseType() );
        recipeDetailDTO.spiceLevel( recipe.getSpiceLevel() );
        recipeDetailDTO.prepTimeMinutes( recipe.getPrepTimeMinutes() );
        recipeDetailDTO.cookTimeMinutes( recipe.getCookTimeMinutes() );
        recipeDetailDTO.restTimeMinutes( recipe.getRestTimeMinutes() );
        recipeDetailDTO.servings( recipe.getServings() );
        recipeDetailDTO.servingsUnit( recipe.getServingsUnit() );
        recipeDetailDTO.isVegetarian( recipe.getIsVegetarian() );
        recipeDetailDTO.isVegan( recipe.getIsVegan() );
        recipeDetailDTO.isGlutenFree( recipe.getIsGlutenFree() );
        recipeDetailDTO.isDairyFree( recipe.getIsDairyFree() );
        recipeDetailDTO.isHalal( recipe.getIsHalal() );
        recipeDetailDTO.isKosher( recipe.getIsKosher() );
        recipeDetailDTO.isNutFree( recipe.getIsNutFree() );
        recipeDetailDTO.isLowCarb( recipe.getIsLowCarb() );
        String[] tags = recipe.getTags();
        if ( tags != null ) {
            recipeDetailDTO.tags( Arrays.copyOf( tags, tags.length ) );
        }
        recipeDetailDTO.viewCount( recipe.getViewCount() );
        recipeDetailDTO.likeCount( recipe.getLikeCount() );
        recipeDetailDTO.saveCount( recipe.getSaveCount() );
        recipeDetailDTO.avgRating( recipe.getAvgRating() );
        recipeDetailDTO.ratingCount( recipe.getRatingCount() );
        recipeDetailDTO.commentCount( recipe.getCommentCount() );
        recipeDetailDTO.isPremium( recipe.getIsPremium() );
        recipeDetailDTO.isAiGenerated( recipe.getIsAiGenerated() );
        recipeDetailDTO.isFeatured( recipe.getIsFeatured() );
        recipeDetailDTO.version( recipe.getVersion() );
        recipeDetailDTO.publishedAt( recipe.getPublishedAt() );
        recipeDetailDTO.createdAt( recipe.getCreatedAt() );
        recipeDetailDTO.updatedAt( recipe.getUpdatedAt() );

        recipeDetailDTO.totalTimeMinutes( recipe.getTotalTimeMinutes() );
        recipeDetailDTO.recipeType( recipe.getRecipeType().name() );
        recipeDetailDTO.difficultyLevel( recipe.getDifficultyLevel().name() );
        recipeDetailDTO.status( recipe.getStatus().name() );

        return recipeDetailDTO.build();
    }

    @Override
    public RecipeDetailDTO.NutritionDTO toNutritionDTO(Recipe recipe) {
        if ( recipe == null ) {
            return null;
        }

        RecipeDetailDTO.NutritionDTO.NutritionDTOBuilder nutritionDTO = RecipeDetailDTO.NutritionDTO.builder();

        nutritionDTO.caloriesKcal( recipe.getCaloriesKcal() );
        nutritionDTO.proteinG( recipe.getProteinG() );
        nutritionDTO.carbsG( recipe.getCarbsG() );
        nutritionDTO.fatG( recipe.getFatG() );
        nutritionDTO.fiberG( recipe.getFiberG() );
        nutritionDTO.sugarG( recipe.getSugarG() );
        nutritionDTO.sodiumMg( recipe.getSodiumMg() );

        return nutritionDTO.build();
    }

    @Override
    public RecipeDetailDTO.IngredientLineDTO toIngredientLineDTO(RecipeIngredient recipeIngredient) {
        if ( recipeIngredient == null ) {
            return null;
        }

        RecipeDetailDTO.IngredientLineDTO.IngredientLineDTOBuilder ingredientLineDTO = RecipeDetailDTO.IngredientLineDTO.builder();

        ingredientLineDTO.name( recipeIngredientIngredientName( recipeIngredient ) );
        ingredientLineDTO.id( recipeIngredient.getId() );
        ingredientLineDTO.quantity( recipeIngredient.getQuantity() );
        ingredientLineDTO.unit( recipeIngredient.getUnit() );
        ingredientLineDTO.displayText( recipeIngredient.getDisplayText() );
        ingredientLineDTO.isOptional( recipeIngredient.getIsOptional() );
        ingredientLineDTO.groupName( recipeIngredient.getGroupName() );

        return ingredientLineDTO.build();
    }

    @Override
    public List<RecipeDetailDTO.IngredientLineDTO> toIngredientLineDTOList(List<RecipeIngredient> ingredients) {
        if ( ingredients == null ) {
            return null;
        }

        List<RecipeDetailDTO.IngredientLineDTO> list = new ArrayList<RecipeDetailDTO.IngredientLineDTO>( ingredients.size() );
        for ( RecipeIngredient recipeIngredient : ingredients ) {
            list.add( toIngredientLineDTO( recipeIngredient ) );
        }

        return list;
    }

    @Override
    public RecipeDetailDTO.StepDTO toStepDTO(RecipeStep step) {
        if ( step == null ) {
            return null;
        }

        RecipeDetailDTO.StepDTO.StepDTOBuilder stepDTO = RecipeDetailDTO.StepDTO.builder();

        stepDTO.stepNumber( step.getStepNumber() );
        stepDTO.instruction( step.getInstruction() );
        stepDTO.durationMinutes( step.getDurationMinutes() );
        stepDTO.imageUrl( step.getImageUrl() );
        stepDTO.tip( step.getTip() );

        return stepDTO.build();
    }

    @Override
    public List<RecipeDetailDTO.StepDTO> toStepDTOList(List<RecipeStep> steps) {
        if ( steps == null ) {
            return null;
        }

        List<RecipeDetailDTO.StepDTO> list = new ArrayList<RecipeDetailDTO.StepDTO>( steps.size() );
        for ( RecipeStep recipeStep : steps ) {
            list.add( toStepDTO( recipeStep ) );
        }

        return list;
    }

    private String recipeIngredientIngredientName(RecipeIngredient recipeIngredient) {
        Ingredient ingredient = recipeIngredient.getIngredient();
        if ( ingredient == null ) {
            return null;
        }
        return ingredient.getName();
    }
}

package com.cerex.service;

import com.cerex.domain.Recipe;
import com.cerex.domain.Recipe.DifficultyLevel;
import com.cerex.domain.Recipe.RecipeStatus;
import com.cerex.dto.recipe.CreateRecipeRequest;
import com.cerex.dto.recipe.RecipeCardDTO;
import com.cerex.dto.recipe.RecipeDetailDTO;
import com.cerex.exception.BusinessException;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.exception.UnauthorizedException;
import com.cerex.mapper.RecipeMapper;
import com.cerex.repository.RecipeRepository;
import com.cerex.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for RecipeService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService Tests")
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private RecipeService recipeService;

    private UUID authorId;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();

        testRecipe = Recipe.builder()
            .id(UUID.randomUUID())
            .authorId(authorId)
            .title("Poulet Yassa")
            .slug("poulet-yassa")
            .description("Plat sénégalais traditionnel")
            .status(RecipeStatus.PUBLISHED)
            .difficultyLevel(DifficultyLevel.INTERMEDIATE)
            .prepTimeMinutes(30)
            .cookTimeMinutes(45)
            .servings(4)
            .averageRating(BigDecimal.valueOf(4.5))
            .viewCount(150)
            .likeCount(42)
            .publishedAt(Instant.now())
            .build();
    }

    @Nested
    @DisplayName("Get Recipe")
    class GetRecipeTests {

        @Test
        @DisplayName("Should get recipe by slug")
        void shouldGetRecipeBySlug() {
            // Given
            given(recipeRepository.findBySlugAndStatus("poulet-yassa", RecipeStatus.PUBLISHED))
                .willReturn(Optional.of(testRecipe));

            // When
            RecipeDetailDTO result = recipeService.getRecipeBySlug("poulet-yassa");

            // Then
            assertThat(result).isNotNull();
            then(recipeRepository).should().findBySlugAndStatus("poulet-yassa", RecipeStatus.PUBLISHED);
        }

        @Test
        @DisplayName("Should throw not found for unknown slug")
        void shouldThrowNotFoundForUnknownSlug() {
            // Given
            given(recipeRepository.findBySlugAndStatus("unknown", RecipeStatus.PUBLISHED))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> recipeService.getRecipeBySlug("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should list published recipes with pagination")
        void shouldListPublishedRecipes() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Recipe> recipePage = new PageImpl<>(List.of(testRecipe), pageable, 1);
            given(recipeRepository.findAll(pageable)).willReturn(recipePage);

            // When
            Page<RecipeCardDTO> result = recipeService.listPublishedRecipes(pageable);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Recipe Lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("Should publish a draft recipe")
        void shouldPublishDraftRecipe() {
            // Given
            testRecipe.setStatus(RecipeStatus.DRAFT);
            given(recipeRepository.findById(testRecipe.getId())).willReturn(Optional.of(testRecipe));
            given(recipeRepository.save(any(Recipe.class))).willAnswer(inv -> inv.getArgument(0));

            // When
            recipeService.publishRecipe(testRecipe.getId(), authorId);

            // Then
            assertThat(testRecipe.getStatus()).isEqualTo(RecipeStatus.PENDING_REVIEW);
            then(recipeRepository).should().save(testRecipe);
        }

        @Test
        @DisplayName("Should not allow non-author to publish")
        void shouldNotAllowNonAuthorToPublish() {
            // Given
            testRecipe.setStatus(RecipeStatus.DRAFT);
            given(recipeRepository.findById(testRecipe.getId())).willReturn(Optional.of(testRecipe));

            UUID otherUser = UUID.randomUUID();

            // When & Then
            assertThatThrownBy(() -> recipeService.publishRecipe(testRecipe.getId(), otherUser))
                .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Should soft-delete a recipe")
        void shouldSoftDeleteRecipe() {
            // Given
            given(recipeRepository.findById(testRecipe.getId())).willReturn(Optional.of(testRecipe));

            // When
            recipeService.deleteRecipe(testRecipe.getId(), authorId);

            // Then
            then(recipeRepository).should().save(any(Recipe.class));
        }
    }
}

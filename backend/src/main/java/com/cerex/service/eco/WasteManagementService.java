package com.cerex.service.eco;

import com.cerex.domain.EcoBadge;
import com.cerex.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for waste management, eco-badge gamification, and sustainability tracking.
 *
 * <p>Tracks:
 * <ul>
 *   <li>User waste reduction score</li>
 *   <li>Anti-waste recipe usage</li>
 *   <li>Eco-badge progression & awarding</li>
 *   <li>Community sustainability metrics</li>
 *   <li>Carbon footprint tracking per user</li>
 * </ul>
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WasteManagementService {

    private final UserProfileRepository profileRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ─────────────────────────────────────────────────────────
    // Badge System
    // ─────────────────────────────────────────────────────────

    /**
     * Badge definitions with criteria.
     */
    private static final List<BadgeDefinition> BADGE_DEFINITIONS = List.of(
        new BadgeDefinition("ECO_WARRIOR", "Éco-Guerrier", "A réduit ses déchets de 50%",
            EcoBadge.BadgeCategory.ECO, 100, 50),
        new BadgeDefinition("LOCAL_HERO", "Héros Local", "Cuisine avec 80% d'ingrédients locaux",
            EcoBadge.BadgeCategory.ECO, 80, 30),
        new BadgeDefinition("ZERO_WASTE_CHEF", "Chef Zéro Déchet", "10 recettes anti-gaspillage créées",
            EcoBadge.BadgeCategory.ECO, 150, 10),
        new BadgeDefinition("ORGANIC_ADVOCATE", "Défenseur du Bio", "50 achats bio consécutifs",
            EcoBadge.BadgeCategory.ECO, 120, 50),
        new BadgeDefinition("SEASON_MASTER", "Maître des Saisons", "Cuisine de saison pendant 3 mois",
            EcoBadge.BadgeCategory.ECO, 90, 90),

        new BadgeDefinition("CULTURE_EXPLORER", "Explorateur Culturel", "Recettes de 5 continents testées",
            EcoBadge.BadgeCategory.CULTURAL, 75, 5),
        new BadgeDefinition("WORLD_CHEF", "Chef du Monde", "Recettes de 20 pays différents",
            EcoBadge.BadgeCategory.CULTURAL, 200, 20),
        new BadgeDefinition("SPICE_MASTER", "Maître des Épices", "50 recettes épicées maîtrisées",
            EcoBadge.BadgeCategory.CULTURAL, 100, 50),

        new BadgeDefinition("SOCIAL_BUTTERFLY", "Papillon Social", "100 interactions sociales",
            EcoBadge.BadgeCategory.SOCIAL, 50, 100),
        new BadgeDefinition("INFLUENCER", "Influenceur Culinaire", "1000 followers atteints",
            EcoBadge.BadgeCategory.SOCIAL, 200, 1000),
        new BadgeDefinition("HELPFUL_CHEF", "Chef Solidaire", "50 commentaires utiles postés",
            EcoBadge.BadgeCategory.SOCIAL, 80, 50),

        new BadgeDefinition("FIRST_RECIPE", "Première Recette", "A publié sa première recette",
            EcoBadge.BadgeCategory.MILESTONE, 10, 1),
        new BadgeDefinition("RECIPE_MASTER", "Maître des Recettes", "100 recettes publiées",
            EcoBadge.BadgeCategory.MILESTONE, 300, 100),
        new BadgeDefinition("ORDER_CHAMPION", "Champion des Commandes", "50 commandes passées",
            EcoBadge.BadgeCategory.MILESTONE, 100, 50)
    );

    /**
     * Check which badges a user has earned and award new ones.
     */
    public List<BadgeAward> checkAndAwardBadges(UUID userId, UserProgress progress) {
        List<BadgeAward> newBadges = new ArrayList<>();

        for (BadgeDefinition def : BADGE_DEFINITIONS) {
            boolean earned = switch (def.code) {
                case "ECO_WARRIOR" -> progress.wasteReductionPercent >= def.threshold;
                case "LOCAL_HERO" -> progress.localIngredientPercent >= def.threshold;
                case "ZERO_WASTE_CHEF" -> progress.antiWasteRecipeCount >= def.threshold;
                case "ORGANIC_ADVOCATE" -> progress.organicPurchaseStreak >= def.threshold;
                case "SEASON_MASTER" -> progress.seasonalCookingDays >= def.threshold;
                case "CULTURE_EXPLORER" -> progress.continentsExplored >= def.threshold;
                case "WORLD_CHEF" -> progress.countriesExplored >= def.threshold;
                case "SPICE_MASTER" -> progress.spicyRecipeCount >= def.threshold;
                case "SOCIAL_BUTTERFLY" -> progress.socialInteractions >= def.threshold;
                case "INFLUENCER" -> progress.followerCount >= def.threshold;
                case "HELPFUL_CHEF" -> progress.helpfulComments >= def.threshold;
                case "FIRST_RECIPE" -> progress.recipesPublished >= def.threshold;
                case "RECIPE_MASTER" -> progress.recipesPublished >= def.threshold;
                case "ORDER_CHAMPION" -> progress.ordersPlaced >= def.threshold;
                default -> false;
            };

            if (earned && !progress.earnedBadgeCodes.contains(def.code)) {
                BadgeAward award = BadgeAward.builder()
                    .code(def.code)
                    .name(def.name)
                    .description(def.description)
                    .category(def.category.name())
                    .points(def.points)
                    .build();
                newBadges.add(award);

                // Notify via Kafka
                kafkaTemplate.send("cerex.user.badge_earned", userId.toString(),
                    Map.of("userId", userId, "badge", def.code, "points", def.points));

                log.info("Badge awarded to user [{}]: {} (+{} pts)", userId, def.code, def.points);
            }
        }

        return newBadges;
    }

    /**
     * Get total eco points for a user.
     */
    public int calculateTotalEcoPoints(UUID userId, List<String> earnedBadgeCodes) {
        return BADGE_DEFINITIONS.stream()
            .filter(d -> earnedBadgeCodes.contains(d.code))
            .mapToInt(d -> d.points)
            .sum();
    }

    /**
     * Get community sustainability stats.
     */
    public CommunitySustainabilityStats getCommunityStats() {
        // In production, these would come from aggregated database queries
        return CommunitySustainabilityStats.builder()
            .totalCarbonSavedKg(0)
            .totalWastePreventedKg(0)
            .totalLocalMeals(0)
            .activeEcoUsers(0)
            .averageEcoScore(0)
            .build();
    }

    // ─────────────────────────────────────────────────────────
    // DTOs
    // ─────────────────────────────────────────────────────────

    public record BadgeDefinition(
        String code, String name, String description,
        EcoBadge.BadgeCategory category, int points, int threshold) {}

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserProgress {
        private double wasteReductionPercent;
        private double localIngredientPercent;
        private int antiWasteRecipeCount;
        private int organicPurchaseStreak;
        private int seasonalCookingDays;
        private int continentsExplored;
        private int countriesExplored;
        private int spicyRecipeCount;
        private int socialInteractions;
        private long followerCount;
        private int helpfulComments;
        private int recipesPublished;
        private int ordersPlaced;
        private Set<String> earnedBadgeCodes;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BadgeAward {
        private String code;
        private String name;
        private String description;
        private String category;
        private int points;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CommunitySustainabilityStats {
        private long totalCarbonSavedKg;
        private long totalWastePreventedKg;
        private long totalLocalMeals;
        private long activeEcoUsers;
        private double averageEcoScore;
    }
}

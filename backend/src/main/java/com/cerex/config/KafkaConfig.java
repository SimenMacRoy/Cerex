package com.cerex.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration for the event-driven architecture.
 *
 * <p>Topics follow the pattern: {@code cerex.<domain>.<event>}
 */
@Configuration
public class KafkaConfig {

    // ── User Events ─────────────────────────────────────────────
    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name("cerex.user.created")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic userUpdatedTopic() {
        return TopicBuilder.name("cerex.user.updated")
            .partitions(3)
            .replicas(1)
            .build();
    }

    // ── Recipe Events ───────────────────────────────────────────
    @Bean
    public NewTopic recipePublishedTopic() {
        return TopicBuilder.name("cerex.recipe.published")
            .partitions(6)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic recipeLikedTopic() {
        return TopicBuilder.name("cerex.recipe.liked")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic recipeViewedTopic() {
        return TopicBuilder.name("cerex.recipe.viewed")
            .partitions(6)
            .replicas(1)
            .build();
    }

    // ── Order Events ────────────────────────────────────────────
    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("cerex.order.created")
            .partitions(6)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic orderStatusChangedTopic() {
        return TopicBuilder.name("cerex.order.status-changed")
            .partitions(6)
            .replicas(1)
            .build();
    }

    // ── Notification Events ─────────────────────────────────────
    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name("cerex.notification.send")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic notificationCreatedTopic() {
        return TopicBuilder.name("cerex.notification.created")
            .partitions(3)
            .replicas(1)
            .build();
    }

    // ── Social Events ───────────────────────────────────────────
    @Bean
    public NewTopic socialPostCreatedTopic() {
        return TopicBuilder.name("cerex.social.post_created")
            .partitions(3)
            .replicas(1)
            .build();
    }

    // ── Restaurant Events ───────────────────────────────────────
    @Bean
    public NewTopic restaurantCreatedTopic() {
        return TopicBuilder.name("cerex.restaurant.created")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic restaurantVerifiedTopic() {
        return TopicBuilder.name("cerex.restaurant.verified")
            .partitions(3)
            .replicas(1)
            .build();
    }

    // ── Grocery Events ──────────────────────────────────────────
    @Bean
    public NewTopic groceryCreatedTopic() {
        return TopicBuilder.name("cerex.grocery.created")
            .partitions(3)
            .replicas(1)
            .build();
    }

    // ── Eco Badge Events ────────────────────────────────────────
    @Bean
    public NewTopic badgeEarnedTopic() {
        return TopicBuilder.name("cerex.user.badge_earned")
            .partitions(3)
            .replicas(1)
            .build();
    }

    // ── Auth Events ─────────────────────────────────────────────
    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name("cerex.user.registered")
            .partitions(3)
            .replicas(1)
            .build();
    }
}

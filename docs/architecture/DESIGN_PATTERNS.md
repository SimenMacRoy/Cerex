# Cerex — Design Patterns Reference

> Version: 1.0.0 | Last Updated: 2026-03-30

---

## Table of Contents

1. [MVC Pattern](#1-mvc-pattern)
2. [Repository Pattern](#2-repository-pattern)
3. [Service Layer Pattern](#3-service-layer-pattern)
4. [Factory Pattern](#4-factory-pattern)
5. [Strategy Pattern](#5-strategy-pattern)
6. [Observer Pattern](#6-observer-pattern)
7. [Singleton Pattern](#7-singleton-pattern)
8. [CQRS Pattern](#8-cqrs-pattern)
9. [Clean Architecture](#9-clean-architecture)
10. [Domain-Driven Design (DDD)](#10-domain-driven-design-ddd)
11. [Pattern Interaction Map](#11-pattern-interaction-map)

---

## 1. MVC Pattern

**Context in Cerex**: The web and mobile frontends follow MVC. On the backend, Spring Boot controllers act as the View layer entry point, routing requests to Service (Controller) and returning DTOs (View Model).

**Structure in Cerex:**
```
Controller (C) → Accepts HTTP requests, validates input, delegates to Service
Service (M)    → Business logic, orchestrates domain operations
DTO / Response (V) → Data Transfer Objects returned to clients
```

### Java/Spring Boot Example

```java
// ─────────────────────────────────────────────────────────────
// MODEL: RecipeService.java (Business Logic)
// ─────────────────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    public RecipeDetailDTO getRecipeBySlug(String slug, UUID currentUserId) {
        Recipe recipe = recipeRepository.findBySlugAndStatus(slug, RecipeStatus.PUBLISHED)
            .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + slug));

        RecipeDetailDTO dto = recipeMapper.toDetailDTO(recipe);

        // Enrich with user interaction state
        if (currentUserId != null) {
            dto.setUserInteraction(buildUserInteraction(recipe.getId(), currentUserId));
        }

        return dto;
    }
}

// ─────────────────────────────────────────────────────────────
// VIEW: RecipeDetailDTO.java (Response Model)
// ─────────────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDetailDTO {
    private UUID id;
    private String title;
    private String slug;
    private AuthorDTO author;
    private GeographyDTO geography;
    private List<IngredientLineDTO> ingredients;
    private List<RecipeStepDTO> steps;
    private EngagementDTO engagement;
    private UserInteractionDTO userInteraction;
}

// ─────────────────────────────────────────────────────────────
// CONTROLLER: RecipeController.java (Request Handling)
// ─────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<RecipeDetailDTO>> getRecipe(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        UUID userId = currentUser != null ? currentUser.getId() : null;
        RecipeDetailDTO recipe = recipeService.getRecipeBySlug(slug, userId);
        return ResponseEntity.ok(ApiResponse.success(recipe));
    }
}
```

---

## 2. Repository Pattern

**Context in Cerex**: Every domain entity has a dedicated repository that abstracts all data access. Services never query the database directly — they always go through a repository interface. This enables testability (mock repositories) and database technology independence.

```java
// ─────────────────────────────────────────────────────────────
// INTERFACE: RecipeRepository.java
// Defines the contract for recipe data access
// ─────────────────────────────────────────────────────────────
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID>,
                                          JpaSpecificationExecutor<Recipe> {

    // Simple derived queries
    Optional<Recipe> findBySlugAndStatus(String slug, RecipeStatus status);

    List<Recipe> findByAuthorIdAndStatusOrderByCreatedAtDesc(UUID authorId, RecipeStatus status);

    boolean existsBySlug(String slug);

    // JPQL queries
    @Query("""
        SELECT r FROM Recipe r
        JOIN FETCH r.author a
        JOIN FETCH r.continent cont
        LEFT JOIN FETCH r.country co
        WHERE r.status = 'PUBLISHED'
          AND r.continentId = :continentId
          AND (:countryId IS NULL OR r.countryId = :countryId)
          AND (:isVegan IS NULL OR r.isVegan = :isVegan)
          AND (:isGlutenFree IS NULL OR r.isGlutenFree = :isGlutenFree)
        ORDER BY r.publishedAt DESC
        """)
    Page<Recipe> findByCulturalFilters(
        @Param("continentId") UUID continentId,
        @Param("countryId") UUID countryId,
        @Param("isVegan") Boolean isVegan,
        @Param("isGlutenFree") Boolean isGlutenFree,
        Pageable pageable
    );

    // Native SQL for complex analytics queries
    @Query(value = """
        SELECT r.id, r.title, r.avg_rating, r.view_count,
               COUNT(DISTINCT o.id) AS recent_orders
        FROM recipes_schema.recipes r
        LEFT JOIN orders_schema.order_items oi ON oi.recipe_id = r.id
        LEFT JOIN orders_schema.orders o ON o.id = oi.order_id
            AND o.created_at >= NOW() - INTERVAL '7 days'
        WHERE r.status = 'PUBLISHED'
          AND r.continent_id = :continentId
        GROUP BY r.id, r.title, r.avg_rating, r.view_count
        ORDER BY (r.avg_rating * 0.4 + LOG(r.view_count + 1) * 0.3 + recent_orders * 0.3) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTrendingByContinent(
        @Param("continentId") UUID continentId,
        @Param("limit") int limit
    );

    @Modifying
    @Query("UPDATE Recipe r SET r.viewCount = r.viewCount + 1 WHERE r.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Recipe r SET r.likeCount = r.likeCount + :delta WHERE r.id = :id")
    void updateLikeCount(@Param("id") UUID id, @Param("delta") int delta);
}

// ─────────────────────────────────────────────────────────────
// SPECIFICATION: RecipeSpecifications.java
// Dynamic query building with type-safe predicates
// ─────────────────────────────────────────────────────────────
public class RecipeSpecifications {

    public static Specification<Recipe> withFilters(RecipeFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), RecipeStatus.PUBLISHED));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter.getContinentId() != null) {
                predicates.add(cb.equal(root.get("continentId"), filter.getContinentId()));
            }
            if (filter.getDifficultyLevel() != null) {
                predicates.add(cb.equal(root.get("difficultyLevel"), filter.getDifficultyLevel()));
            }
            if (filter.getIsVegan() != null && filter.getIsVegan()) {
                predicates.add(cb.isTrue(root.get("isVegan")));
            }
            if (filter.getMaxTimeMinutes() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("totalTimeMinutes"), filter.getMaxTimeMinutes()
                ));
            }
            if (filter.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("avgRating"), filter.getMinRating()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

---

## 3. Service Layer Pattern

**Context in Cerex**: The Service Layer encapsulates all business logic, transaction management, and cross-cutting concerns. It sits between controllers and repositories, orchestrating complex operations.

```java
// ─────────────────────────────────────────────────────────────
// RecipeService.java — Full service layer implementation
// ─────────────────────────────────────────────────────────────
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;
    private final CacheService cacheService;
    private final EventPublisher eventPublisher;
    private final SlugGenerator slugGenerator;
    private final MediaService mediaService;
    private final AIService aiService;

    /**
     * Creates a new recipe draft.
     * Generates a unique slug, validates ingredients, and publishes a domain event.
     */
    @Transactional
    public RecipeDetailDTO createRecipe(CreateRecipeRequest request, UUID authorId) {
        log.info("Creating recipe for author: {}", authorId);

        // Validate author exists and has permission
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new UserNotFoundException(authorId));

        // Generate unique slug
        String slug = slugGenerator.generateUnique(request.getTitle(),
            s -> recipeRepository.existsBySlug(s));

        // Build domain entity
        Recipe recipe = recipeMapper.fromCreateRequest(request);
        recipe.setAuthorId(authorId);
        recipe.setSlug(slug);
        recipe.setStatus(RecipeStatus.DRAFT);

        // Validate and resolve ingredients
        List<RecipeIngredient> ingredients = resolveIngredients(request.getIngredients());
        recipe.setIngredients(ingredients);

        Recipe saved = recipeRepository.save(recipe);

        // Async AI enrichment (non-blocking)
        aiService.enrichRecipeAsync(saved.getId());

        // Publish domain event
        eventPublisher.publish(RecipeCreatedEvent.of(saved));

        log.info("Recipe created successfully: {} (slug: {})", saved.getId(), slug);
        return recipeMapper.toDetailDTO(saved);
    }

    /**
     * Publishes a recipe draft — triggers moderation pipeline.
     */
    @Transactional
    public RecipeDetailDTO publishRecipe(UUID recipeId, UUID requesterId) {
        Recipe recipe = findRecipeForEdit(recipeId, requesterId);

        if (recipe.getStatus() != RecipeStatus.DRAFT) {
            throw new InvalidRecipeStateException(
                "Can only publish recipes in DRAFT status. Current: " + recipe.getStatus()
            );
        }

        recipe.setStatus(RecipeStatus.PENDING_REVIEW);
        recipe.setPublishedAt(Instant.now());
        Recipe updated = recipeRepository.save(recipe);

        // Invalidate any cached drafts
        cacheService.evict("recipe:" + recipeId);

        // Trigger moderation pipeline
        eventPublisher.publish(RecipeSubmittedForReviewEvent.of(updated));

        return recipeMapper.toDetailDTO(updated);
    }

    /**
     * Retrieves a recipe with caching.
     * Cache-aside pattern: check Redis first, then DB.
     */
    public RecipeDetailDTO getRecipeBySlug(String slug, UUID currentUserId) {
        String cacheKey = "recipe:slug:" + slug;

        return cacheService.getOrCompute(cacheKey, Duration.ofMinutes(5), () -> {
            Recipe recipe = recipeRepository.findBySlugAndStatus(slug, RecipeStatus.PUBLISHED)
                .orElseThrow(() -> new RecipeNotFoundException(slug));

            // Fire-and-forget view count increment
            eventPublisher.publishAsync(RecipeViewedEvent.of(recipe.getId(), currentUserId));

            return recipeMapper.toDetailDTO(recipe);
        });
    }

    private Recipe findRecipeForEdit(UUID recipeId, UUID requesterId) {
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new RecipeNotFoundException(recipeId));

        if (!recipe.getAuthorId().equals(requesterId)) {
            throw new InsufficientPermissionsException(
                "User " + requesterId + " cannot edit recipe " + recipeId
            );
        }
        return recipe;
    }

    private List<RecipeIngredient> resolveIngredients(List<IngredientLineRequest> requests) {
        return requests.stream()
            .map(req -> {
                Ingredient ingredient = ingredientRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new IngredientNotFoundException(req.getIngredientId()));

                return RecipeIngredient.builder()
                    .ingredient(ingredient)
                    .quantity(req.getQuantity())
                    .unit(req.getUnit())
                    .preparation(req.getPreparation())
                    .isOptional(req.getIsOptional())
                    .groupName(req.getGroupName())
                    .displayOrder(req.getDisplayOrder())
                    .build();
            })
            .collect(Collectors.toList());
    }
}
```

---

## 4. Factory Pattern

**Context in Cerex**: Used extensively for creating complex objects (notifications, events, recommendations) without coupling the caller to concrete implementations.

```java
// ─────────────────────────────────────────────────────────────
// NotificationFactory.java
// Creates the correct notification type based on event
// ─────────────────────────────────────────────────────────────
@Component
@RequiredArgsConstructor
public class NotificationFactory {

    private final UserRepository userRepository;

    public Notification createOrderNotification(Order order, NotificationType type) {
        return switch (type) {
            case ORDER_CONFIRMED -> Notification.builder()
                .recipientId(order.getUserId())
                .notificationType(NotificationType.ORDER_CONFIRMED)
                .title("Order Confirmed! 🎉")
                .body(String.format("Your order #%s has been confirmed. Estimated delivery: %s",
                    order.getOrderNumber(),
                    formatEta(order.getEstimatedDelivery())))
                .actionUrl("/orders/" + order.getId() + "/track")
                .data(Map.of(
                    "orderId", order.getId().toString(),
                    "orderNumber", order.getOrderNumber()
                ))
                .channel(NotificationChannel.PUSH)
                .build();

            case ORDER_DELIVERED -> Notification.builder()
                .recipientId(order.getUserId())
                .notificationType(NotificationType.ORDER_DELIVERED)
                .title("Order Delivered!")
                .body("Your meal from " + order.getRestaurantName() + " has arrived. Enjoy!")
                .actionUrl("/orders/" + order.getId() + "/review")
                .channel(NotificationChannel.PUSH)
                .build();

            case ORDER_CANCELLED -> Notification.builder()
                .recipientId(order.getUserId())
                .notificationType(NotificationType.ORDER_CANCELLED)
                .title("Order Cancelled")
                .body("Your order has been cancelled. Refund: " + formatAmount(order.getRefundAmount()))
                .channel(NotificationChannel.EMAIL)
                .build();

            default -> throw new UnsupportedNotificationTypeException(type);
        };
    }

    public Notification createSocialNotification(SocialEvent event) {
        return switch (event.getType()) {
            case NEW_FOLLOWER -> Notification.builder()
                .recipientId(event.getTargetUserId())
                .senderId(event.getActorId())
                .notificationType(NotificationType.NEW_FOLLOWER)
                .title(event.getActorName() + " started following you")
                .body("You have a new follower!")
                .actionUrl("/users/" + event.getActorUsername())
                .channel(NotificationChannel.IN_APP)
                .build();

            case RECIPE_LIKED -> Notification.builder()
                .recipientId(event.getTargetUserId())
                .senderId(event.getActorId())
                .notificationType(NotificationType.RECIPE_LIKED)
                .title(event.getActorName() + " liked your recipe")
                .body("\"" + event.getRecipeTitle() + "\" got a new like!")
                .actionUrl("/recipes/" + event.getRecipeSlug())
                .channel(NotificationChannel.IN_APP)
                .build();

            default -> throw new UnsupportedNotificationTypeException(event.getType());
        };
    }
}

// ─────────────────────────────────────────────────────────────
// RecommendationFactory.java
// Chooses the correct recommendation algorithm
// ─────────────────────────────────────────────────────────────
@Component
@RequiredArgsConstructor
public class RecommendationFactory {

    private final CollaborativeFilteringStrategy collaborativeFiltering;
    private final ContentBasedStrategy contentBased;
    private final PopularityBasedStrategy popularityBased;
    private final HybridStrategy hybrid;

    public RecommendationStrategy getStrategy(String algorithmName) {
        return switch (algorithmName.toLowerCase()) {
            case "collaborative" -> collaborativeFiltering;
            case "content_based"  -> contentBased;
            case "popularity"     -> popularityBased;
            case "hybrid"         -> hybrid;
            default -> hybrid; // default to hybrid
        };
    }
}
```

---

## 5. Strategy Pattern

**Context in Cerex**: The recommendation engine, payment processing, and search ranking all use the Strategy pattern to swap algorithms at runtime without changing client code.

```java
// ─────────────────────────────────────────────────────────────
// INTERFACE: RecommendationStrategy.java
// ─────────────────────────────────────────────────────────────
public interface RecommendationStrategy {
    /**
     * Generate recipe recommendations for a user.
     *
     * @param userId  The target user
     * @param context Additional context (homepage, post-order, etc.)
     * @param limit   Maximum number of recommendations
     * @return Ordered list of recommendations with scores
     */
    List<RecipeRecommendation> recommend(UUID userId, RecommendationContext context, int limit);
}

// ─────────────────────────────────────────────────────────────
// IMPLEMENTATION 1: CollaborativeFilteringStrategy.java
// "Users like you also loved..."
// ─────────────────────────────────────────────────────────────
@Component
@RequiredArgsConstructor
public class CollaborativeFilteringStrategy implements RecommendationStrategy {

    private final AIUserProfileRepository aiProfileRepo;
    private final RecipeRepository recipeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<RecipeRecommendation> recommend(UUID userId, RecommendationContext ctx, int limit) {
        // Fetch user's interaction embedding from MongoDB
        AIUserProfile userProfile = aiProfileRepo.findByUserId(userId)
            .orElseThrow(() -> new UserProfileNotFoundException(userId));

        float[] userEmbedding = userProfile.getUserEmbedding();

        // Find similar users via cosine similarity (Redis or Elasticsearch)
        List<UUID> similarUserIds = findSimilarUsers(userEmbedding, 50);

        // Get top-rated recipes from similar users (excluding already-seen)
        List<UUID> alreadySeen = userProfile.getViewHistory().stream()
            .map(h -> h.getRecipeId())
            .collect(Collectors.toList());

        return recipeRepository.findTopRatedBySimilarUsers(similarUserIds, alreadySeen, limit)
            .stream()
            .map(recipe -> RecipeRecommendation.builder()
                .recipe(recipe)
                .score(calculateScore(recipe, userProfile))
                .reason("Users with similar tastes loved this")
                .reasonCode("COLLABORATIVE_FILTER")
                .build())
            .collect(Collectors.toList());
    }

    private double calculateScore(Recipe recipe, AIUserProfile profile) {
        return recipe.getAvgRating() * 0.5 + recipe.getViewCount() * 0.0001 * 0.3 + 0.2;
    }
}

// ─────────────────────────────────────────────────────────────
// IMPLEMENTATION 2: ContentBasedStrategy.java
// "Because you love West African cuisine..."
// ─────────────────────────────────────────────────────────────
@Component
@RequiredArgsConstructor
public class ContentBasedStrategy implements RecommendationStrategy {

    private final AIUserProfileRepository aiProfileRepo;
    private final ElasticsearchOperations elasticsearchOps;

    @Override
    public List<RecipeRecommendation> recommend(UUID userId, RecommendationContext ctx, int limit) {
        AIUserProfile profile = aiProfileRepo.findByUserId(userId)
            .orElseThrow(() -> new UserProfileNotFoundException(userId));

        // Build a preference vector based on cuisine affinities
        Map<String, Double> cuisineAffinities = profile.getCuisineAffinities();

        // Get top cuisine preference
        String topCuisine = cuisineAffinities.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("international");

        // Query Elasticsearch with vector similarity + cuisine filter
        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(q -> q.bool(b -> b
                .must(m -> m.match(f -> f.field("cuisineType").query(topCuisine)))
                .mustNot(mn -> mn.ids(i -> i.values(profile.getViewHistoryIds())))
            ))
            .withSort(Sort.by(Sort.Direction.DESC, "avgRating"))
            .withPageable(PageRequest.of(0, limit))
            .build();

        SearchHits<RecipeDocument> hits = elasticsearchOps.search(searchQuery, RecipeDocument.class);

        return hits.stream()
            .map(hit -> RecipeRecommendation.builder()
                .recipe(recipeMapper.fromDocument(hit.getContent()))
                .score(hit.getScore())
                .reason("Based on your love of " + topCuisine + " cuisine")
                .reasonCode("CONTENT_BASED")
                .build())
            .collect(Collectors.toList());
    }
}

// ─────────────────────────────────────────────────────────────
// PAYMENT STRATEGY: Different payment processors
// ─────────────────────────────────────────────────────────────
public interface PaymentStrategy {
    PaymentResult charge(PaymentRequest request);
    RefundResult refund(String paymentId, BigDecimal amount);
}

@Component("stripe")
public class StripePaymentStrategy implements PaymentStrategy { /* ... */ }

@Component("paypal")
public class PayPalPaymentStrategy implements PaymentStrategy { /* ... */ }
```

---

## 6. Observer Pattern

**Context in Cerex**: Domain events trigger downstream processes (indexing, notifications, analytics, AI training) via Kafka. The Observer pattern decouples producers from consumers completely.

```java
// ─────────────────────────────────────────────────────────────
// EVENT: RecipePublishedEvent.java (Subject/Observable)
// ─────────────────────────────────────────────────────────────
@Value
@Builder
public class RecipePublishedEvent {
    String eventId;
    String eventType = "recipe.published";
    Instant timestamp;
    String producerService = "recipe-service";
    int version = 1;
    RecipePublishedPayload payload;

    public static RecipePublishedEvent of(Recipe recipe) {
        return RecipePublishedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .payload(RecipePublishedPayload.builder()
                .recipeId(recipe.getId())
                .authorId(recipe.getAuthorId())
                .title(recipe.getTitle())
                .slug(recipe.getSlug())
                .continentId(recipe.getContinentId())
                .countryId(recipe.getCountryId())
                .tags(recipe.getTags())
                .build())
            .build();
    }
}

// ─────────────────────────────────────────────────────────────
// PUBLISHER: DomainEventPublisher.java
// ─────────────────────────────────────────────────────────────
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(DomainEvent event) {
        String topic = resolveTopicName(event);
        String key = event.getAggregateId(); // partition by entity ID for ordering

        kafkaTemplate.send(topic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event {}: {}", event.getEventType(), ex.getMessage());
                    // Dead letter queue handling
                } else {
                    log.debug("Published event {} to topic {} partition {}",
                        event.getEventId(), topic, result.getRecordMetadata().partition());
                }
            });
    }

    private String resolveTopicName(DomainEvent event) {
        return event.getEventType().split("\\.")[0] + ".events";
    }
}

// ─────────────────────────────────────────────────────────────
// OBSERVER 1: SearchIndexingListener.java
// Listens to recipe events and updates Elasticsearch index
// ─────────────────────────────────────────────────────────────
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchIndexingListener {

    private final ElasticsearchOperations elasticsearchOps;
    private final RecipeRepository recipeRepository;

    @KafkaListener(
        topics = "recipe.events",
        groupId = "cerex.search.indexer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onRecipeEvent(RecipePublishedEvent event, Acknowledgment ack) {
        try {
            log.info("Indexing recipe: {}", event.getPayload().getRecipeId());

            Recipe recipe = recipeRepository.findById(event.getPayload().getRecipeId())
                .orElseThrow();

            RecipeDocument doc = RecipeDocument.fromRecipe(recipe);
            elasticsearchOps.save(doc);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to index recipe {}: {}", event.getPayload().getRecipeId(), e.getMessage());
            // Will be retried by Kafka consumer group
        }
    }
}

// ─────────────────────────────────────────────────────────────
// OBSERVER 2: SocialFanoutListener.java
// Notifies followers when someone they follow publishes a recipe
// ─────────────────────────────────────────────────────────────
@Component
@RequiredArgsConstructor
@Slf4j
public class SocialFanoutListener {

    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;

    @KafkaListener(topics = "recipe.events", groupId = "cerex.social.fanout")
    public void onRecipePublished(RecipePublishedEvent event, Acknowledgment ack) {
        UUID authorId = event.getPayload().getAuthorId();

        // Get all followers of the author
        List<UUID> followerIds = followRepository.findFollowerIds(authorId);

        log.info("Fanning out recipe {} to {} followers", event.getPayload().getRecipeId(), followerIds.size());

        // Batch notification sending (chunk of 500)
        Lists.partition(followerIds, 500).forEach(batch -> {
            batch.forEach(followerId -> {
                Notification notification = notificationFactory.createNewRecipeNotification(
                    event.getPayload(), followerId
                );
                notificationService.sendAsync(notification);
            });
        });

        ack.acknowledge();
    }
}
```

---

## 7. Singleton Pattern

**Context in Cerex**: Used for expensive-to-create shared resources: database connection pools, Redis clients, HTTP clients, configuration holders.

```java
// ─────────────────────────────────────────────────────────────
// Spring's @Bean creates singletons by default.
// Shown here for clarity with thread-safety considerations.
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// RedisConfiguration.java — Singleton Redis connection
// ─────────────────────────────────────────────────────────────
@Configuration
@EnableCaching
public class RedisConfiguration {

    @Bean
    @Singleton  // Spring manages this as a singleton
    public LettuceConnectionFactory redisConnectionFactory(RedisProperties props) {
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(props.getCluster().getNodes());
        clusterConfig.setPassword(props.getPassword());
        clusterConfig.setMaxRedirects(3);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(500))
            .shutdownTimeout(Duration.ZERO)
            .clientOptions(ClusterClientOptions.builder()
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build())
            .build();

        return new LettuceConnectionFactory(clusterConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory factory) {
        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
            "recipes",        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)),
            "user_profiles",  RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)),
            "search_results", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(2)),
            "trending",       RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10))
        );

        return RedisCacheManager.builder(factory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)))
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}

// ─────────────────────────────────────────────────────────────
// FeatureFlagService.java — Singleton feature flag manager
// Thread-safe, refreshes from Redis every 60 seconds
// ─────────────────────────────────────────────────────────────
@Component
@Slf4j
public class FeatureFlagService {

    private volatile Map<String, Boolean> flags = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        refreshFlags();
    }

    @Scheduled(fixedDelay = 60_000)
    public void refreshFlags() {
        try {
            Map<Object, Object> redisFlags = redisTemplate.opsForHash()
                .entries("feature_flags");
            flags = redisFlags.entrySet().stream()
                .collect(Collectors.toConcurrentMap(
                    e -> e.getKey().toString(),
                    e -> Boolean.parseBoolean(e.getValue().toString())
                ));
        } catch (Exception e) {
            log.warn("Failed to refresh feature flags: {}", e.getMessage());
        }
    }

    public boolean isEnabled(String flagName) {
        return flags.getOrDefault(flagName, false);
    }

    public boolean isEnabled(String flagName, UUID userId) {
        // Gradual rollout: enable for specific user percentage
        if (!flags.getOrDefault(flagName, false)) return false;
        return Math.abs(userId.hashCode()) % 100 < getRolloutPercentage(flagName);
    }
}
```

---

## 8. CQRS Pattern

**Context in Cerex**: Commands (write operations) and Queries (read operations) are separated to allow independent optimization, scaling, and evolution.

```java
// ─────────────────────────────────────────────────────────────
// COMMAND side: Handles write operations
// ─────────────────────────────────────────────────────────────

// Command: CreateRecipeCommand.java
@Value
@Builder
public class CreateRecipeCommand {
    UUID commandId = UUID.randomUUID();
    UUID requesterId;
    String title;
    String description;
    UUID continentId;
    UUID countryId;
    DifficultyLevel difficultyLevel;
    List<IngredientLineRequest> ingredients;
    List<RecipeStepRequest> steps;
    List<String> tags;
}

// CommandHandler: CreateRecipeCommandHandler.java
@Component
@RequiredArgsConstructor
@Transactional
public class CreateRecipeCommandHandler implements CommandHandler<CreateRecipeCommand, RecipeId> {

    private final RecipeRepository recipeRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    public RecipeId handle(CreateRecipeCommand command) {
        Recipe recipe = Recipe.create(command);  // Domain logic in entity
        recipeRepository.save(recipe);

        recipe.getDomainEvents().forEach(eventPublisher::publish);
        recipe.clearDomainEvents();

        return new RecipeId(recipe.getId());
    }
}

// ─────────────────────────────────────────────────────────────
// QUERY side: Handles read operations (optimized for reads)
// ─────────────────────────────────────────────────────────────

// Query: GetRecipesFeedQuery.java
@Value
@Builder
public class GetRecipesFeedQuery {
    UUID userId;
    String continentCode;
    String countryCode;
    Boolean isVegan;
    Boolean isGlutenFree;
    Integer page;
    Integer size;
    String sortBy;
}

// QueryHandler: RecipeFeedQueryHandler.java
@Component
@RequiredArgsConstructor
public class RecipeFeedQueryHandler implements QueryHandler<GetRecipesFeedQuery, Page<RecipeCardDTO>> {

    private final RecipeReadRepository readRepository;  // Separate read model
    private final CacheService cacheService;

    @Override
    public Page<RecipeCardDTO> handle(GetRecipesFeedQuery query) {
        String cacheKey = buildCacheKey(query);

        return cacheService.getOrCompute(cacheKey, Duration.ofMinutes(5), () ->
            readRepository.findFeedOptimized(query)
        );
    }

    private String buildCacheKey(GetRecipesFeedQuery q) {
        return String.format("feed:%s:%s:%s:%d:%d",
            q.getContinentCode(), q.getCountryCode(),
            q.getUserId() != null ? q.getUserId() : "anonymous",
            q.getPage(), q.getSize()
        );
    }
}

// ─────────────────────────────────────────────────────────────
// Read-optimized repository (uses DB view or materialized view)
// ─────────────────────────────────────────────────────────────
@Repository
public interface RecipeReadRepository extends JpaRepository<RecipeReadModel, UUID> {

    @Query(value = """
        SELECT * FROM recipes_schema.v_recipe_feed_cards
        WHERE (:continentCode IS NULL OR continent_code = :continentCode)
          AND (:countryCode IS NULL OR country_code = :countryCode)
          AND (:isVegan IS NULL OR is_vegan = :isVegan)
        ORDER BY trending_score DESC
        """, nativeQuery = true)
    Page<RecipeReadModel> findFeedOptimized(/* params */, Pageable pageable);
}
```

---

## 9. Clean Architecture

**Context in Cerex**: Dependency inversion ensures business rules (domain) never depend on frameworks or infrastructure. This makes the core logic testable in isolation.

```
┌──────────────────────────────────────────────────────────┐
│                  CLEAN ARCHITECTURE LAYERS               │
│                                                          │
│  ┌─────────────────────────────────────────────────┐     │
│  │     FRAMEWORKS & DRIVERS (outermost)            │     │
│  │  Spring Boot, Hibernate, Redis, Kafka, REST     │     │
│  │                                                 │     │
│  │  ┌───────────────────────────────────────────┐  │     │
│  │  │     INTERFACE ADAPTERS                    │  │     │
│  │  │  Controllers, Repositories, Presenters    │  │     │
│  │  │                                           │  │     │
│  │  │  ┌─────────────────────────────────────┐  │  │     │
│  │  │  │     APPLICATION / USE CASES         │  │  │     │
│  │  │  │  RecipeService, OrderService,        │  │  │     │
│  │  │  │  CommandHandlers, QueryHandlers      │  │  │     │
│  │  │  │                                     │  │  │     │
│  │  │  │  ┌────────────────────────────────┐ │  │  │     │
│  │  │  │  │     DOMAIN (innermost)         │ │  │  │     │
│  │  │  │  │  Entities, Value Objects,      │ │  │  │     │
│  │  │  │  │  Domain Events, Aggregates     │ │  │  │     │
│  │  │  │  └────────────────────────────────┘ │  │  │     │
│  │  │  └─────────────────────────────────────┘  │  │     │
│  │  └───────────────────────────────────────────┘  │     │
│  └─────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────┘
```

```java
// DOMAIN LAYER: Recipe.java — Pure domain logic, no framework dependencies
public class Recipe {
    private RecipeId id;
    private AuthorId authorId;
    private RecipeTitle title;                      // Value Object
    private RecipeStatus status;
    private List<RecipeIngredient> ingredients;
    private List<RecipeStep> steps;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Domain method — encapsulates business rule
    public void publish() {
        if (this.status != RecipeStatus.DRAFT) {
            throw new DomainException("Recipe must be in DRAFT to publish");
        }
        if (this.ingredients.isEmpty()) {
            throw new DomainException("Recipe must have at least one ingredient");
        }
        if (this.steps.isEmpty()) {
            throw new DomainException("Recipe must have at least one step");
        }

        this.status = RecipeStatus.PENDING_REVIEW;
        registerEvent(new RecipeSubmittedForReviewEvent(this.id, this.authorId));
    }

    public void approve(ModeratorId moderatorId) {
        this.status = RecipeStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        registerEvent(new RecipePublishedEvent(this.id, this.authorId, moderatorId));
    }

    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

// VALUE OBJECT: RecipeTitle.java — Self-validating
public record RecipeTitle(String value) {
    public RecipeTitle {
        Objects.requireNonNull(value, "Title cannot be null");
        if (value.isBlank()) throw new DomainException("Title cannot be blank");
        if (value.length() > 300) throw new DomainException("Title too long (max 300 chars)");
        value = value.trim();
    }
}
```

---

## 10. Domain-Driven Design (DDD)

**Context in Cerex**: The domain is organized into bounded contexts (Recipe, Order, User, Social), each with its own ubiquitous language, aggregates, and anti-corruption layers at context boundaries.

```java
// ─────────────────────────────────────────────────────────────
// AGGREGATE ROOT: Order.java
// The Order aggregate controls all order-related state changes
// ─────────────────────────────────────────────────────────────
@Entity
@Table(name = "orders", schema = "orders_schema")
public class Order extends AggregateRoot<OrderId> {

    @EmbeddedId
    private OrderId id;

    @Embedded
    private OrderNumber orderNumber;

    @Embedded
    private CustomerId customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Embedded
    private Money total;

    @Embedded
    private DeliveryAddress deliveryAddress;

    // DOMAIN BEHAVIOR: Business rules enforced in the aggregate
    public void addItem(RecipeId recipeId, int quantity, Money unitPrice) {
        if (status != OrderStatus.DRAFT) {
            throw new OrderNotModifiableException("Cannot modify order in status: " + status);
        }
        if (quantity < 1 || quantity > 99) {
            throw new DomainException("Quantity must be between 1 and 99");
        }

        items.stream()
            .filter(item -> item.getRecipeId().equals(recipeId))
            .findFirst()
            .ifPresentOrElse(
                existing -> existing.increaseQuantity(quantity),
                () -> items.add(new OrderItem(recipeId, quantity, unitPrice))
            );

        recalculateTotal();
    }

    public void confirm(RestaurantId restaurantId) {
        ensureStatus(OrderStatus.PENDING, "confirm");
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
        registerEvent(new OrderConfirmedEvent(this.id, customerId, restaurantId, total));
    }

    public void cancel(String reason) {
        if (!isCancellable()) {
            throw new OrderCancellationNotAllowedException(
                "Order cannot be cancelled in status: " + status
            );
        }
        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = Instant.now();
        registerEvent(new OrderCancelledEvent(this.id, customerId, total));
    }

    private boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    private void ensureStatus(OrderStatus expected, String operation) {
        if (status != expected) {
            throw new InvalidOrderStateException(
                String.format("Cannot %s order in status %s (expected: %s)", operation, status, expected)
            );
        }
    }

    private void recalculateTotal() {
        this.total = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
}

// ─────────────────────────────────────────────────────────────
// DOMAIN SERVICE: RecipePricingDomainService.java
// Logic that doesn't naturally belong to any single entity
// ─────────────────────────────────────────────────────────────
@DomainService
public class RecipePricingDomainService {

    public Money calculateOrderTotal(Order order, PromoCode promoCode, DeliveryZone zone) {
        Money subtotal = order.getSubtotal();
        Money deliveryFee = calculateDeliveryFee(subtotal, zone);
        Money discount = promoCode != null ? promoCode.calculateDiscount(subtotal) : Money.ZERO;
        Money serviceFee = subtotal.multiply(0.05); // 5% service fee
        Money tax = (subtotal.subtract(discount)).multiply(zone.getTaxRate());

        return subtotal
            .add(deliveryFee)
            .add(serviceFee)
            .subtract(discount)
            .add(tax);
    }
}
```

---

## 11. Pattern Interaction Map

```
HTTP Request → RecipeController (MVC Controller)
                    │
                    ▼ delegates to
              RecipeService (Service Layer)
                    │
               ┌────┤ uses
               │    │
               │    ├──► RecipeRepository (Repository Pattern)
               │    │         │
               │    │         └──► PostgreSQL / Redis (Singleton connections)
               │    │
               │    ├──► RecommendationFactory (Factory)
               │    │         │
               │    │         └──► HybridStrategy (Strategy)
               │    │
               │    ├──► NotificationFactory (Factory)
               │    │
               │    └──► DomainEventPublisher (Observer - subject)
               │               │
               │    Kafka Topics (Observer - channel)
               │               │
               │    ┌──────────┼──────────┐
               │    ▼          ▼          ▼
               │  SearchIndexingListener  SocialFanoutListener
               │  (Observer - listener)   (Observer - listener)
               │
               └──► CQRS: Commands go to CommandHandlers
                          Queries go to QueryHandlers (cached reads)
```

---

*Document Version: 1.0.0 | Next Review: 2026-06-30 | Owner: Cerex Backend Team*

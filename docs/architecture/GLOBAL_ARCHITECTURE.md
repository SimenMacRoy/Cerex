# Cerex — Global Culinary Platform: System Architecture

> Version: 1.0.0 | Last Updated: 2026-03-30 | Status: Production

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [High-Level Architecture](#high-level-architecture)
3. [Microservices Breakdown](#microservices-breakdown)
4. [Technology Stack Justification](#technology-stack-justification)
5. [Communication Patterns](#communication-patterns)
6. [Data Flow Diagrams](#data-flow-diagrams)
7. [Deployment Architecture](#deployment-architecture)
8. [CDN and Media Strategy](#cdn-and-media-strategy)
9. [Caching Strategy](#caching-strategy)
10. [Message Queuing (Kafka)](#message-queuing-kafka)
11. [Observability and Monitoring](#observability-and-monitoring)
12. [Disaster Recovery](#disaster-recovery)

---

## 1. Executive Summary

Cerex is a next-generation global culinary platform connecting food enthusiasts, professional chefs, home cooks, and restaurants across every continent. The platform enables recipe discovery, meal ordering, cultural exploration, AI-powered personalization, and social interaction around food.

The architecture is designed around the following core principles:

- **Scalability**: Horizontal scaling at every layer, capable of handling 50M+ daily active users
- **Resilience**: No single point of failure; graceful degradation under load
- **Performance**: Sub-100ms response times for 99th percentile API calls globally
- **Security**: Zero-trust architecture, end-to-end encryption, GDPR/CCPA compliant by design
- **Observability**: Full distributed tracing, metrics, and log aggregation
- **Cultural Intelligence**: Multi-language, multi-currency, multi-timezone native support

---

## 2. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                   CEREX GLOBAL PLATFORM                             │
└─────────────────────────────────────────────────────────────────────────────────────┘

  CLIENTS
  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
  │  Web App     │  │  iOS App     │  │ Android App  │  │ Partner API  │
  │  (React)     │  │  (Swift)     │  │  (Kotlin)    │  │  (3rd Party) │
  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
         │                 │                  │                  │
         └─────────────────┴──────────────────┴──────────────────┘
                                     │
                              ┌──────▼──────┐
                              │  CloudFlare │
                              │  CDN + WAF  │
                              └──────┬──────┘
                                     │
                        ┌────────────▼────────────┐
                        │    API GATEWAY (Kong)    │
                        │  Rate Limiting | Auth    │
                        │  Load Balancing | TLS    │
                        └────────────┬────────────┘
                                     │
        ┌────────────────────────────▼────────────────────────────┐
        │                  SERVICE MESH (Istio)                    │
        │                                                          │
        │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
        │  │  User    │ │  Recipe  │ │  Order   │ │  Social  │  │
        │  │ Service  │ │ Service  │ │ Service  │ │ Service  │  │
        │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │
        │       │             │             │             │        │
        │  ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐  │
        │  │   AI/ML  │ │  Search  │ │ Notif.   │ │  Media   │  │
        │  │ Service  │ │ Service  │ │ Service  │ │ Service  │  │
        │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │
        └───────┼─────────────┼─────────────┼─────────────┼───────┘
                │             │             │             │
        ┌───────▼─────────────▼─────────────▼─────────────▼───────┐
        │                 MESSAGE BUS (Apache Kafka)               │
        │         Topics: recipes | orders | users | events        │
        └───────────────────────────────────────────────────────────┘
                │             │             │             │
        ┌───────▼──┐  ┌───────▼──┐  ┌──────▼───┐  ┌─────▼────┐
        │PostgreSQL│  │ MongoDB  │  │  Redis   │  │Elasticsr.│
        │(Primary) │  │(Metadata)│  │ (Cache)  │  │(Search)  │
        └──────────┘  └──────────┘  └──────────┘  └──────────┘
                │
        ┌───────▼──────────────────────────────────────────────┐
        │              AWS S3 / CloudFront (Media)             │
        └──────────────────────────────────────────────────────┘
```

---

## 3. Microservices Breakdown

### 3.1 User Service
**Responsibility**: Identity management, authentication, authorization, profile management, subscription handling.

| Component | Technology | Description |
|-----------|-----------|-------------|
| Auth Module | Spring Security + JWT | Token issuance, validation, refresh |
| OAuth2 Module | Spring OAuth2 Client | Google, Facebook, Apple SSO |
| Profile Module | JPA + PostgreSQL | User profile CRUD |
| Subscription Module | Stripe SDK | Premium plan management |
| GDPR Module | Custom | Data export, deletion requests |

**API Base**: `/api/v1/users`
**Port**: 8081
**DB**: PostgreSQL (users schema)

---

### 3.2 Recipe Service
**Responsibility**: Recipe lifecycle management, cultural metadata, ingredient management, versioning.

| Component | Technology | Description |
|-----------|-----------|-------------|
| Recipe CRUD | Spring Data JPA | Full recipe management |
| Ingredient Engine | Custom Algorithm | Ingredient parsing and normalization |
| Cultural Tagging | NLP + MongoDB | Continent/Country/Culture association |
| Version Control | Hibernate Envers | Recipe change history |
| Import/Export | Apache POI + JSON | Bulk recipe import |

**API Base**: `/api/v1/recipes`
**Port**: 8082
**DB**: PostgreSQL (recipes schema) + MongoDB (metadata)

---

### 3.3 Order Service
**Responsibility**: Order lifecycle, payment processing, restaurant integration, delivery tracking.

| Component | Technology | Description |
|-----------|-----------|-------------|
| Order Management | Spring State Machine | Order status transitions |
| Payment Gateway | Stripe + PayPal | Multi-currency payments |
| Restaurant Adapter | REST + Webhooks | Restaurant POS integration |
| Delivery Tracker | WebSocket + Redis | Real-time GPS tracking |
| Invoice Engine | iText7 | PDF invoice generation |

**API Base**: `/api/v1/orders`
**Port**: 8083
**DB**: PostgreSQL (orders schema)

---

### 3.4 Social Service
**Responsibility**: Social feed, posts, likes, comments, follows, sharing.

| Component | Technology | Description |
|-----------|-----------|-------------|
| Feed Engine | Redis Sorted Sets | Personalized activity feed |
| Post Management | MongoDB | Rich social content |
| Interaction Engine | Redis + PostgreSQL | Likes, comments, shares |
| Follow Graph | Neo4j | Social graph traversal |
| Notification Bus | Kafka Producer | Social event publishing |

**API Base**: `/api/v1/social`
**Port**: 8084
**DB**: MongoDB (social schema) + PostgreSQL (interactions)

---

### 3.5 AI/ML Service
**Responsibility**: Recipe generation, recommendations, translation, trend analysis, fraud detection.

| Component | Technology | Description |
|-----------|-----------|-------------|
| Recipe Generator | GPT-4o + LangChain | AI recipe creation |
| Recommender | Collaborative Filtering + DL | Personalized suggestions |
| Translator | DeepL API + custom NMT | Multi-language content |
| Trend Analyzer | Time Series ML | Culinary trend prediction |
| Fraud Detector | Isolation Forest | Restaurant fraud detection |
| Waste Optimizer | Linear Programming | Ingredient waste reduction |

**API Base**: `/api/v1/ai`
**Port**: 8085
**DB**: MongoDB (ai_models) + Redis (feature store)

---

### 3.6 Search Service
**Responsibility**: Full-text search, faceted filtering, auto-complete, geo-search.

| Component | Technology | Description |
|-----------|-----------|-------------|
| Search Engine | Elasticsearch 8.x | Full-text + vector search |
| Auto-complete | Redis Trie | Instant suggestions |
| Geo-search | Elasticsearch geo | Restaurant proximity search |
| Index Manager | Spring Batch | Periodic re-indexing |
| Vector Search | Elasticsearch ELSER | Semantic recipe search |

**API Base**: `/api/v1/search`
**Port**: 8086
**DB**: Elasticsearch

---

### 3.7 Media Service
**Responsibility**: Image/video upload, processing, CDN management, metadata extraction.

| Component | Technology | Description |
|-----------|-----------|-------------|
| Upload Handler | AWS S3 Multipart | Chunked file uploads |
| Image Processor | ImageMagick + Sharp | Resize, optimize, WebP |
| Video Processor | FFmpeg | Thumbnail, transcode |
| CDN Manager | CloudFront SDK | Cache invalidation |
| Metadata Extractor | Apache Tika | EXIF, content analysis |

**API Base**: `/api/v1/media`
**Port**: 8087
**DB**: MongoDB (media_metadata) + AWS S3

---

### 3.8 Notification Service
**Responsibility**: Push notifications, email campaigns, SMS, in-app notifications.

| Component | Technology | Description |
|-----------|-----------|-------------|
| Push Engine | Firebase FCM + APNs | Mobile push notifications |
| Email Engine | SendGrid + Mjml | Transactional emails |
| SMS Engine | Twilio | SMS notifications |
| In-App | WebSocket | Real-time in-app alerts |
| Template Engine | Thymeleaf | Notification templates |

**API Base**: `/api/v1/notifications`
**Port**: 8088
**DB**: MongoDB (notifications) + Redis (delivery tracking)

---

## 4. Technology Stack Justification

### 4.1 Backend — Java / Spring Boot

**Justification**:
- **Maturity**: Spring Boot is the most battle-tested enterprise Java framework with 15+ years of production use
- **Ecosystem**: Rich ecosystem including Spring Security, Spring Data, Spring Cloud
- **Performance**: JVM with GraalVM native compilation achieves C-like performance
- **Microservices**: Spring Cloud provides service discovery, load balancing, circuit breaking out of the box
- **Type Safety**: Strong typing prevents entire classes of runtime bugs
- **Tooling**: Excellent IDE support (IntelliJ IDEA), profiling, and APM integration

**Alternatives Considered**:
- Node.js (rejected: weaker type safety, less suitable for complex business logic)
- Go (rejected: smaller ecosystem for enterprise features, less mature ORM support)
- Python/FastAPI (retained for AI/ML microservice only)

---

### 4.2 Frontend — React / TypeScript

**Justification**:
- **Component Reusability**: 60%+ code reuse across web and React Native mobile
- **TypeScript**: Compile-time safety prevents 15-30% of production bugs
- **Ecosystem**: Largest npm ecosystem, best-in-class tooling (Vite, Turbopack)
- **Performance**: React 18 concurrent mode, Suspense, streaming SSR
- **State Management**: Redux Toolkit + React Query for optimal server state handling
- **Internationalization**: react-i18next with 50+ language support

---

### 4.3 Databases

| Database | Use Case | Justification |
|----------|----------|---------------|
| PostgreSQL 16 | Transactional data (users, orders, recipes) | ACID compliance, rich SQL, JSON support, PostGIS for geo |
| MongoDB 7.0 | Flexible documents (AI metadata, social posts, media) | Schema flexibility, horizontal sharding, aggregation pipeline |
| Redis 7.2 | Caching, sessions, real-time features | Sub-millisecond latency, pub/sub, sorted sets, streams |
| Elasticsearch 8.x | Search, analytics, log aggregation | Best-in-class full-text search, vector search, Kibana |

---

### 4.4 Infrastructure

| Component | Technology | Justification |
|-----------|-----------|---------------|
| Container Runtime | Docker + containerd | Industry standard, reproducible builds |
| Orchestration | Kubernetes (EKS) | Auto-scaling, self-healing, rolling deployments |
| Service Mesh | Istio | mTLS, circuit breaking, observability |
| API Gateway | Kong | Plugin ecosystem, rate limiting, authentication |
| Message Broker | Apache Kafka | High throughput, durability, exactly-once semantics |
| CI/CD | GitHub Actions + ArgoCD | GitOps workflow, automated deployments |
| Cloud Provider | AWS (primary) + GCP (AI) | Best-in-class managed services |

---

## 5. Communication Patterns

### 5.1 Synchronous Communication (REST + gRPC)

```
Client Request Flow:
─────────────────────────────────────────────────
Client → API Gateway → Service → Repository → DB
                    ↓
                Response ← (< 100ms target)

Inter-Service Synchronous (gRPC):
─────────────────────────────────────────────────
Recipe Service ──gRPC──→ User Service (get author info)
Order Service  ──gRPC──→ Recipe Service (get recipe details)
Social Service ──gRPC──→ User Service (get follower list)
```

**When to use REST**:
- Client-facing APIs
- External integrations
- Simple request-response patterns

**When to use gRPC**:
- Internal service-to-service communication
- High-throughput scenarios (>10K RPS)
- Streaming data requirements

---

### 5.2 Asynchronous Communication (Kafka)

```
Event-Driven Architecture:
─────────────────────────────────────────────────
Producer                Topic               Consumer(s)
────────                ─────               ──────────
User Service  ──────→  user.created    →   AI Service (build profile)
                                        →   Notification Service (welcome email)
                                        →   Analytics Service (track signup)

Recipe Service ─────→  recipe.published →  Search Service (index)
                                         →  Social Service (fan-out to followers)
                                         →  AI Service (update recommendations)

Order Service  ─────→  order.placed     →  Restaurant Service (notify kitchen)
                                         →  Notification Service (order confirmation)
                                         →  Analytics Service (revenue tracking)
```

**Kafka Topic Configuration**:

```yaml
topics:
  user.created:
    partitions: 12
    replication_factor: 3
    retention_ms: 2592000000   # 30 days

  recipe.published:
    partitions: 24
    replication_factor: 3
    retention_ms: 7776000000   # 90 days

  order.placed:
    partitions: 48             # Higher throughput for orders
    replication_factor: 3
    retention_ms: 31536000000  # 1 year (compliance)

  recommendation.generated:
    partitions: 12
    replication_factor: 3
    retention_ms: 86400000     # 24 hours
```

---

### 5.3 Real-Time Communication (WebSocket)

```
WebSocket Connections (via STOMP over WebSocket):
─────────────────────────────────────────────────
Client ←──WebSocket──→ Notification Service
         STOMP             │
                           ├── /topic/order/{orderId}/status
                           ├── /topic/user/{userId}/notifications
                           ├── /topic/recipe/{recipeId}/live-cooking
                           └── /queue/user/{userId}/chat
```

---

## 6. Data Flow Diagrams

### 6.1 Recipe Discovery Flow

```
User Opens App
      │
      ▼
API Gateway receives GET /api/v1/recipes/feed
      │
      ▼
Recipe Service checks Redis cache
      │
  ┌───┴───┐
  │ HIT   │ MISS
  ▼       ▼
Redis   AI Service
Cache   /recommend
  │         │
  │         ▼
  │    PostgreSQL query
  │    + Elasticsearch
  │         │
  │    Redis caches
  │    result (TTL: 5min)
  │         │
  └────┬────┘
       ▼
  Format response
  (RecipeCardDTO[])
       │
       ▼
  CloudFront serves
  media URLs
       │
       ▼
  Client renders
  RecipeCard grid
```

---

### 6.2 Order Placement Flow

```
User taps "Order" on Recipe
         │
         ▼
Frontend: POST /api/v1/orders
         │
         ▼
Order Service validates request
         │
         ▼
Payment Service (Stripe) charges card
         │
     ┌───┴───┐
     │Success│ Failure
     │       ▼
     │  Return 402 / retry
     ▼
Order saved to PostgreSQL
(status: PENDING_CONFIRMATION)
         │
         ▼
Kafka: order.placed event published
         │
    ┌────┴───────────────────┐
    ▼                        ▼
Restaurant Service    Notification Service
notified via webhook  sends confirmation
(POS integration)     email + push
    │
    ▼
Restaurant confirms
    │
    ▼
Kafka: order.confirmed event
    │
    ▼
Order status: CONFIRMED
    │
    ▼
WebSocket pushes real-time
status to client
```

---

### 6.3 AI Recipe Generation Flow

```
User requests AI recipe
POST /api/v1/ai/recipes/generate
         │
         ▼
AI Service validates request
(cuisine_type, dietary_restrictions,
 available_ingredients, skill_level)
         │
         ▼
Feature Engineering:
- User preference vector (Redis)
- Seasonal ingredient data (PostgreSQL)
- Cultural constraints (MongoDB)
         │
         ▼
LLM API Call (GPT-4o / Llama 3)
Prompt: [System Prompt + Context + Request]
         │
         ▼
Response parsing + validation
(JSON schema enforcement)
         │
         ▼
Nutrition enrichment (USDA API)
         │
         ▼
Translation if needed (DeepL API)
         │
         ▼
Save to MongoDB (draft)
         │
         ▼
Return RecipeGenerationResult to client
         │
         ▼
User reviews and publishes
         │
         ▼
Kafka: recipe.ai_generated event
```

---

## 7. Deployment Architecture

### 7.1 Kubernetes Cluster Layout

```
┌─────────────────────────────────────────────────────────────────┐
│                     EKS CLUSTER (AWS)                          │
│                                                                  │
│  NAMESPACE: cerex-prod                                          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   Ingress (ALB)                          │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐   │
│  │  user-svc  │ │recipe-svc  │ │ order-svc  │ │ social-svc │   │
│  │ replicas:3 │ │ replicas:5 │ │ replicas:4 │ │ replicas:3 │   │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘   │
│                                                                  │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐   │
│  │  ai-svc    │ │search-svc  │ │ media-svc  │ │ notif-svc  │   │
│  │ replicas:2 │ │ replicas:3 │ │ replicas:2 │ │ replicas:3 │   │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘   │
│                                                                  │
│  NAMESPACE: cerex-data                                          │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  PostgreSQL (RDS) | MongoDB Atlas | Redis Cluster      │     │
│  │  Elasticsearch | Kafka (MSK) | Zookeeper               │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘

MULTI-REGION DEPLOYMENT:
─────────────────────────────
us-east-1 (PRIMARY)
eu-west-1 (SECONDARY - EU users)
ap-southeast-1 (SECONDARY - Asia users)
```

### 7.2 Kubernetes Resource Definitions (Example)

```yaml
# recipe-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: recipe-service
  namespace: cerex-prod
  labels:
    app: recipe-service
    version: "1.0.0"
spec:
  replicas: 5
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 2
      maxUnavailable: 0
  selector:
    matchLabels:
      app: recipe-service
  template:
    spec:
      containers:
      - name: recipe-service
        image: cerex/recipe-service:1.0.0
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8082
          initialDelaySeconds: 20
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8082
          initialDelaySeconds: 30
          periodSeconds: 15
      - name: istio-proxy
        image: istio/proxyv2:1.19.0
```

### 7.3 Auto-Scaling Configuration

```yaml
# HorizontalPodAutoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: recipe-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: recipe-service
  minReplicas: 3
  maxReplicas: 50
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: External
    external:
      metric:
        name: kafka_consumer_lag
        selector:
          matchLabels:
            topic: recipe.events
      target:
        type: AverageValue
        averageValue: 1000
```

---

## 8. CDN and Media Strategy

### 8.1 CloudFront Distribution Architecture

```
Upload Flow:
User → Media Service → S3 Origin → CloudFront Distribution
                                         │
                     ┌───────────────────┼───────────────────┐
                     ▼                   ▼                   ▼
              Edge (US)           Edge (EU)           Edge (APAC)
              POP: 50+            POP: 30+            POP: 40+
```

### 8.2 Media Processing Pipeline

```
Original Upload
      │
      ▼
S3 Trigger → Lambda → ImageMagick Processing
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        thumbnail_80x80  card_400x300  hero_1200x800
              │               │               │
              └───────────────┴───────────────┘
                              │
                         S3 Storage
                              │
                         CloudFront
                              │
                    Content-Type: image/webp
                    Cache-Control: max-age=31536000
                    ETag: enabled
```

### 8.3 Media URL Patterns

```
Images:   https://media.cerex.com/{recipeid}/hero.webp
          https://media.cerex.com/{recipeid}/card.webp
          https://media.cerex.com/{recipeid}/thumb.webp

Videos:   https://media.cerex.com/{recipeid}/video_720p.mp4
          https://media.cerex.com/{recipeid}/video_1080p.mp4
          https://media.cerex.com/{recipeid}/thumbnail.webp

Avatars:  https://media.cerex.com/users/{userid}/avatar.webp
```

---

## 9. Caching Strategy

### 9.1 Cache Topology

```
┌─────────────────────────────────────────────────────────┐
│                   CACHING LAYERS                        │
│                                                         │
│  L1: Browser Cache (CDN headers, Service Worker)        │
│  └── Static assets: 1 year                             │
│  └── API responses: 60 seconds (stale-while-revalidate) │
│                                                         │
│  L2: CDN Cache (CloudFront)                             │
│  └── Media: 1 year (immutable)                          │
│  └── Public recipe pages: 5 minutes                     │
│                                                         │
│  L3: API Gateway Cache (Kong)                           │
│  └── Public endpoints: 30 seconds                       │
│                                                         │
│  L4: Application Cache (Redis Cluster)                  │
│  └── User sessions: 24 hours                            │
│  └── Recipe lists: 5 minutes                            │
│  └── Search results: 2 minutes                          │
│  └── User preferences: 1 hour                           │
└─────────────────────────────────────────────────────────┘
```

### 9.2 Redis Key Patterns

```
# User Session
session:{userId}                              TTL: 86400s

# Recipe Cache
recipe:{recipeId}                             TTL: 300s
recipe:list:page:{page}:size:{size}           TTL: 300s
recipe:trending:{continent}:{period}          TTL: 600s
recipe:recommended:{userId}                   TTL: 1800s

# Search Cache
search:results:{queryHash}                    TTL: 120s
search:autocomplete:{prefix}                  TTL: 3600s

# Rate Limiting
ratelimit:{userId}:{endpoint}:{window}        TTL: 60s

# Real-time
order:tracking:{orderId}                      TTL: 86400s
user:online:{userId}                          TTL: 300s
```

### 9.3 Cache Invalidation Strategy

```
Strategy: Cache-Aside with Write-Through for critical data

Recipe Update Flow:
1. Write to PostgreSQL
2. Invalidate Redis key: recipe:{recipeId}
3. Invalidate Redis pattern: recipe:list:*
4. Publish Kafka event: cache.invalidate
5. Search Service re-indexes in background

User Profile Update:
1. Write to PostgreSQL
2. Write-through to Redis: user:{userId}:profile
3. Invalidate dependent keys: recommendations, social feed
```

---

## 10. Message Queuing (Kafka)

### 10.1 Kafka Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    KAFKA CLUSTER (MSK)                       │
│                                                             │
│  Broker 1     Broker 2     Broker 3     Broker 4           │
│  (Leader)     (Follower)   (Follower)   (Follower)         │
│                                                             │
│  Topics:                                                    │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  user.events     (12 partitions, RF=3)              │   │
│  │  recipe.events   (24 partitions, RF=3)              │   │
│  │  order.events    (48 partitions, RF=3)              │   │
│  │  social.events   (12 partitions, RF=3)              │   │
│  │  ai.events       (6 partitions,  RF=3)              │   │
│  │  media.events    (6 partitions,  RF=3)              │   │
│  │  notif.events    (24 partitions, RF=3)              │   │
│  │  analytics.events(48 partitions, RF=3)              │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 10.2 Event Schema (Avro)

```json
{
  "type": "record",
  "name": "RecipePublishedEvent",
  "namespace": "com.cerex.events",
  "fields": [
    {"name": "eventId",     "type": "string"},
    {"name": "eventType",   "type": "string"},
    {"name": "timestamp",   "type": "long"},
    {"name": "version",     "type": "int", "default": 1},
    {"name": "producerService", "type": "string"},
    {"name": "payload", "type": {
      "type": "record",
      "name": "RecipePayload",
      "fields": [
        {"name": "recipeId",   "type": "string"},
        {"name": "authorId",   "type": "string"},
        {"name": "title",      "type": "string"},
        {"name": "continentId","type": "string"},
        {"name": "countryId",  "type": "string"},
        {"name": "tags",       "type": {"type": "array", "items": "string"}}
      ]
    }}
  ]
}
```

### 10.3 Consumer Groups

```
Consumer Groups per Topic:
─────────────────────────────────────────────────
recipe.events:
  - cerex.search.indexer        (Search Service)
  - cerex.ai.trainer            (AI Service)
  - cerex.social.fanout         (Social Service)
  - cerex.analytics.aggregator  (Analytics)
  - cerex.notification.sender   (Notification)

order.events:
  - cerex.restaurant.notifier   (Restaurant Service)
  - cerex.notification.sender   (Notification)
  - cerex.analytics.revenue     (Analytics)
  - cerex.inventory.updater     (Inventory Service)
```

---

## 11. Observability and Monitoring

### 11.1 Three Pillars

```
METRICS (Prometheus + Grafana)
├── Business Metrics: DAU, recipe views, orders/hour, revenue
├── Infrastructure: CPU, memory, disk, network
└── Application: Request rate, error rate, latency (p50/p95/p99)

LOGS (ELK Stack)
├── Structured JSON logging (logstash format)
├── Correlation IDs for distributed tracing
└── Log levels: ERROR (PagerDuty), WARN (Slack), INFO (Kibana)

TRACES (Jaeger + OpenTelemetry)
├── Distributed tracing across all services
├── Sampling: 100% errors, 10% normal traffic
└── Trace retention: 7 days
```

### 11.2 SLOs (Service Level Objectives)

| Service | Availability | P99 Latency | Error Rate |
|---------|-------------|-------------|------------|
| API Gateway | 99.99% | < 50ms | < 0.01% |
| Recipe Service | 99.9% | < 200ms | < 0.1% |
| Order Service | 99.99% | < 300ms | < 0.01% |
| AI Service | 99.5% | < 2000ms | < 1% |
| Search Service | 99.9% | < 150ms | < 0.1% |
| Media Service | 99.9% | < 500ms | < 0.1% |

---

## 12. Disaster Recovery

### 12.1 Recovery Strategy

| Scenario | RTO | RPO | Strategy |
|----------|-----|-----|----------|
| Service crash | < 30s | 0 | Kubernetes self-healing |
| AZ failure | < 2min | 0 | Multi-AZ deployment |
| Region failure | < 15min | < 5min | Active-passive failover |
| Data corruption | < 1hr | < 1hr | PITR (Point-in-Time Recovery) |
| Full disaster | < 4hr | < 1hr | Backup region promotion |

### 12.2 Backup Strategy

```
PostgreSQL:
  - Continuous WAL archival to S3
  - Daily full backup (retained 30 days)
  - Point-in-time recovery window: 35 days

MongoDB:
  - Continuous oplog backup (Atlas)
  - Daily snapshots (retained 7 days)
  - Cross-region replication enabled

Redis:
  - RDB snapshots every 15 minutes
  - AOF for durability (fsync: every second)
  - Redis Sentinel for automatic failover

Kafka:
  - Topics replicated across 3 brokers (RF=3)
  - MirrorMaker 2 for cross-region replication
```

---

*Document Version: 1.0.0 | Next Review: 2026-06-30 | Owner: Cerex Architecture Team*

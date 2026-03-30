# Cerex — Complete Database Schema

> Version: 1.0.0 | Last Updated: 2026-03-30 | Status: Production

---

## Table of Contents

1. [Database Strategy Overview](#1-database-strategy-overview)
2. [PostgreSQL Schema](#2-postgresql-schema)
3. [MongoDB Collections](#3-mongodb-collections)
4. [Redis Key Patterns](#4-redis-key-patterns)
5. [Elasticsearch Mappings](#5-elasticsearch-mappings)
6. [Indexing Strategy](#6-indexing-strategy)
7. [Partitioning Strategy](#7-partitioning-strategy)
8. [Entity Relationship Diagram](#8-entity-relationship-diagram)

---

## 1. Database Strategy Overview

| Database | Version | Use Case | Schema |
|----------|---------|----------|--------|
| PostgreSQL | 16.x | Transactional, relational data | users, recipes, orders, social |
| MongoDB | 7.0 | Flexible documents, AI metadata | ai_data, media_metadata, social_posts |
| Redis | 7.2 | Cache, sessions, real-time | in-memory key-value |
| Elasticsearch | 8.x | Search, analytics | recipes_index, restaurants_index |

---

## 2. PostgreSQL Schema

### 2.1 Schema Organization

```sql
-- Separate schemas for domain isolation and security
CREATE SCHEMA IF NOT EXISTS users_schema;
CREATE SCHEMA IF NOT EXISTS recipes_schema;
CREATE SCHEMA IF NOT EXISTS orders_schema;
CREATE SCHEMA IF NOT EXISTS geo_schema;
CREATE SCHEMA IF NOT EXISTS social_schema;
CREATE SCHEMA IF NOT EXISTS media_schema;
CREATE SCHEMA IF NOT EXISTS subscriptions_schema;
```

---

### 2.2 Geography Tables

```sql
-- ─────────────────────────────────────────────────────────────
-- Table: geo_schema.continents
-- Purpose: Top-level geographic classification for culinary regions
-- ─────────────────────────────────────────────────────────────
CREATE TABLE geo_schema.continents (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(2)      NOT NULL UNIQUE,          -- AF, AS, EU, NA, SA, OC, AN
    name            VARCHAR(100)    NOT NULL,
    name_fr         VARCHAR(100),
    name_es         VARCHAR(100),
    name_zh         VARCHAR(100),
    name_ar         VARCHAR(100),
    description     TEXT,
    image_url       VARCHAR(500),
    cuisine_summary TEXT,
    recipe_count    INTEGER         DEFAULT 0,
    is_active       BOOLEAN         DEFAULT TRUE,
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     DEFAULT NOW()
);

INSERT INTO geo_schema.continents (code, name) VALUES
  ('AF', 'Africa'),
  ('AS', 'Asia'),
  ('EU', 'Europe'),
  ('NA', 'North America'),
  ('SA', 'South America'),
  ('OC', 'Oceania'),
  ('AN', 'Antarctica');

-- ─────────────────────────────────────────────────────────────
-- Table: geo_schema.countries
-- Purpose: Country-level geographic and culinary metadata
-- ─────────────────────────────────────────────────────────────
CREATE TABLE geo_schema.countries (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    continent_id    UUID            NOT NULL REFERENCES geo_schema.continents(id),
    iso_code        CHAR(2)         NOT NULL UNIQUE,          -- ISO 3166-1 alpha-2
    iso_code_3      CHAR(3)         NOT NULL UNIQUE,          -- ISO 3166-1 alpha-3
    name            VARCHAR(100)    NOT NULL,
    native_name     VARCHAR(200),
    flag_emoji      VARCHAR(10),
    capital         VARCHAR(100),
    currency_code   CHAR(3),                                  -- ISO 4217
    primary_language VARCHAR(50),
    languages       VARCHAR[]       DEFAULT '{}',
    timezone        VARCHAR(100),
    phone_code      VARCHAR(10),
    cuisine_style   VARCHAR(200),
    staple_foods    VARCHAR[]       DEFAULT '{}',
    cooking_methods VARCHAR[]       DEFAULT '{}',
    description     TEXT,
    is_active       BOOLEAN         DEFAULT TRUE,
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- Table: geo_schema.cultures
-- Purpose: Sub-national cultural groups with distinct culinary traditions
-- ─────────────────────────────────────────────────────────────
CREATE TABLE geo_schema.cultures (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id      UUID            REFERENCES geo_schema.countries(id),
    continent_id    UUID            NOT NULL REFERENCES geo_schema.continents(id),
    name            VARCHAR(200)    NOT NULL,
    native_name     VARCHAR(200),
    description     TEXT,
    culinary_desc   TEXT,
    key_ingredients VARCHAR[]       DEFAULT '{}',
    spice_level     SMALLINT        CHECK (spice_level BETWEEN 1 AND 5),
    dietary_notes   VARCHAR[]       DEFAULT '{}',     -- halal, kosher, vegan-friendly
    is_regional     BOOLEAN         DEFAULT TRUE,
    parent_culture_id UUID          REFERENCES geo_schema.cultures(id),
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     DEFAULT NOW()
);
```

---

### 2.3 User Tables

```sql
-- ─────────────────────────────────────────────────────────────
-- Table: users_schema.users
-- Purpose: Core authentication and identity
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users_schema.users (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(320)    NOT NULL UNIQUE,
    email_verified      BOOLEAN         DEFAULT FALSE,
    email_verified_at   TIMESTAMPTZ,
    phone               VARCHAR(20)     UNIQUE,
    phone_verified      BOOLEAN         DEFAULT FALSE,
    password_hash       VARCHAR(255),                         -- null for OAuth-only users
    salt                VARCHAR(64),
    role                VARCHAR(30)     NOT NULL DEFAULT 'USER',
                                                              -- USER, CHEF, RESTAURANT_OWNER,
                                                              -- ADMIN, MODERATOR, SUPER_ADMIN
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
                                                              -- ACTIVE, SUSPENDED, DELETED, PENDING
    oauth_provider      VARCHAR(50),                          -- google, facebook, apple
    oauth_provider_id   VARCHAR(255),
    last_login_at       TIMESTAMPTZ,
    last_login_ip       INET,
    failed_login_count  SMALLINT        DEFAULT 0,
    locked_until        TIMESTAMPTZ,
    mfa_enabled         BOOLEAN         DEFAULT FALSE,
    mfa_secret          VARCHAR(64),
    gdpr_consent        BOOLEAN         DEFAULT FALSE,
    gdpr_consent_date   TIMESTAMPTZ,
    marketing_consent   BOOLEAN         DEFAULT FALSE,
    data_deletion_requested_at TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ,                          -- soft delete

    CONSTRAINT chk_role CHECK (role IN (
        'USER', 'CHEF', 'RESTAURANT_OWNER', 'ADMIN', 'MODERATOR', 'SUPER_ADMIN'
    )),
    CONSTRAINT chk_status CHECK (status IN (
        'ACTIVE', 'SUSPENDED', 'DELETED', 'PENDING', 'BANNED'
    ))
);

-- ─────────────────────────────────────────────────────────────
-- Table: users_schema.user_profiles
-- Purpose: Extended user profile and preferences
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users_schema.user_profiles (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL UNIQUE REFERENCES users_schema.users(id) ON DELETE CASCADE,
    username            VARCHAR(50)     UNIQUE,
    display_name        VARCHAR(100),
    first_name          VARCHAR(100),
    last_name           VARCHAR(100),
    bio                 TEXT,
    avatar_url          VARCHAR(500),
    cover_url           VARCHAR(500),
    country_id          UUID            REFERENCES geo_schema.countries(id),
    city                VARCHAR(100),
    timezone            VARCHAR(100)    DEFAULT 'UTC',
    locale              CHAR(5)         DEFAULT 'en_US',      -- BCP 47 language tag
    preferred_language  CHAR(2)         DEFAULT 'en',
    skill_level         VARCHAR(20)     DEFAULT 'BEGINNER',   -- BEGINNER, INTERMEDIATE, ADVANCED, PROFESSIONAL
    dietary_preferences VARCHAR[]       DEFAULT '{}',         -- vegan, vegetarian, halal, kosher, etc.
    allergies           VARCHAR[]       DEFAULT '{}',
    favorite_cuisines   VARCHAR[]       DEFAULT '{}',
    disliked_ingredients VARCHAR[]      DEFAULT '{}',
    cooking_equipment   VARCHAR[]       DEFAULT '{}',
    followers_count     INTEGER         DEFAULT 0,
    following_count     INTEGER         DEFAULT 0,
    recipes_count       INTEGER         DEFAULT 0,
    orders_count        INTEGER         DEFAULT 0,
    website_url         VARCHAR(500),
    instagram_handle    VARCHAR(100),
    tiktok_handle       VARCHAR(100),
    youtube_channel_url VARCHAR(500),
    is_verified_chef    BOOLEAN         DEFAULT FALSE,
    chef_certification  VARCHAR(200),
    chef_specialty      VARCHAR(200),
    years_experience    SMALLINT,
    is_public           BOOLEAN         DEFAULT TRUE,
    show_email          BOOLEAN         DEFAULT FALSE,
    show_phone          BOOLEAN         DEFAULT FALSE,
    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW()
);
```

---

### 2.4 Recipe Tables

```sql
-- ─────────────────────────────────────────────────────────────
-- Table: recipes_schema.categories
-- Purpose: Recipe classification taxonomy
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.categories (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id       UUID            REFERENCES recipes_schema.categories(id),
    slug            VARCHAR(100)    NOT NULL UNIQUE,
    name            VARCHAR(100)    NOT NULL,
    name_fr         VARCHAR(100),
    name_es         VARCHAR(100),
    name_zh         VARCHAR(100),
    name_ar         VARCHAR(100),
    description     TEXT,
    icon_name       VARCHAR(100),
    image_url       VARCHAR(500),
    display_order   SMALLINT        DEFAULT 0,
    is_active       BOOLEAN         DEFAULT TRUE,
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- Table: recipes_schema.recipes
-- Purpose: Core recipe entity — the heart of the platform
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.recipes (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id           UUID            NOT NULL REFERENCES users_schema.users(id),
    category_id         UUID            REFERENCES recipes_schema.categories(id),
    continent_id        UUID            REFERENCES geo_schema.continents(id),
    country_id          UUID            REFERENCES geo_schema.countries(id),
    culture_id          UUID            REFERENCES geo_schema.cultures(id),

    -- Basic Info
    title               VARCHAR(300)    NOT NULL,
    title_fr            VARCHAR(300),
    title_es            VARCHAR(300),
    title_zh            VARCHAR(300),
    title_ar            VARCHAR(300),
    slug                VARCHAR(400)    NOT NULL UNIQUE,
    description         TEXT            NOT NULL,
    description_fr      TEXT,
    description_es      TEXT,
    story               TEXT,                                  -- cultural story behind the recipe

    -- Culinary Details
    recipe_type         VARCHAR(30)     NOT NULL DEFAULT 'DISH',
                                                              -- DISH, BEVERAGE, DESSERT,
                                                              -- SNACK, SAUCE, BREAD, etc.
    cuisine_type        VARCHAR(100),
    course_type         VARCHAR(50),                          -- STARTER, MAIN, DESSERT, SIDE, etc.
    difficulty_level    VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM',
                                                              -- BEGINNER, EASY, MEDIUM, HARD, EXPERT
    spice_level         SMALLINT        DEFAULT 1 CHECK (spice_level BETWEEN 0 AND 5),

    -- Timing
    prep_time_minutes   INTEGER         NOT NULL DEFAULT 0,
    cook_time_minutes   INTEGER         NOT NULL DEFAULT 0,
    rest_time_minutes   INTEGER         DEFAULT 0,
    total_time_minutes  INTEGER GENERATED ALWAYS AS (
                            prep_time_minutes + cook_time_minutes + rest_time_minutes
                        ) STORED,

    -- Servings & Portions
    servings            SMALLINT        NOT NULL DEFAULT 4,
    servings_unit       VARCHAR(50)     DEFAULT 'persons',

    -- Nutrition (per serving)
    calories_kcal       DECIMAL(8,2),
    protein_g           DECIMAL(8,2),
    carbs_g             DECIMAL(8,2),
    fat_g               DECIMAL(8,2),
    fiber_g             DECIMAL(8,2),
    sugar_g             DECIMAL(8,2),
    sodium_mg           DECIMAL(8,2),

    -- Dietary Flags
    is_vegetarian       BOOLEAN         DEFAULT FALSE,
    is_vegan            BOOLEAN         DEFAULT FALSE,
    is_gluten_free      BOOLEAN         DEFAULT FALSE,
    is_dairy_free       BOOLEAN         DEFAULT FALSE,
    is_halal            BOOLEAN         DEFAULT FALSE,
    is_kosher           BOOLEAN         DEFAULT FALSE,
    is_nut_free         BOOLEAN         DEFAULT FALSE,
    is_low_carb         BOOLEAN         DEFAULT FALSE,

    -- Media
    cover_image_url     VARCHAR(500),
    video_url           VARCHAR(500),
    thumbnail_url       VARCHAR(500),

    -- Engagement Metrics
    view_count          BIGINT          DEFAULT 0,
    like_count          INTEGER         DEFAULT 0,
    save_count          INTEGER         DEFAULT 0,
    order_count         INTEGER         DEFAULT 0,
    share_count         INTEGER         DEFAULT 0,
    comment_count       INTEGER         DEFAULT 0,
    avg_rating          DECIMAL(3,2)    DEFAULT 0.0 CHECK (avg_rating BETWEEN 0 AND 5),
    rating_count        INTEGER         DEFAULT 0,

    -- Status & Moderation
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
                                                              -- DRAFT, PENDING_REVIEW,
                                                              -- PUBLISHED, ARCHIVED, REJECTED
    is_ai_generated     BOOLEAN         DEFAULT FALSE,
    ai_model_version    VARCHAR(50),
    is_featured         BOOLEAN         DEFAULT FALSE,
    is_premium          BOOLEAN         DEFAULT FALSE,
    moderation_status   VARCHAR(30)     DEFAULT 'PENDING',
    moderation_note     TEXT,

    -- SEO
    meta_title          VARCHAR(200),
    meta_description    VARCHAR(500),
    keywords            VARCHAR[]       DEFAULT '{}',
    tags                VARCHAR[]       DEFAULT '{}',

    -- Versioning
    version             INTEGER         DEFAULT 1,
    original_recipe_id  UUID            REFERENCES recipes_schema.recipes(id),
    forked_from_id      UUID            REFERENCES recipes_schema.recipes(id),

    published_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ,

    CONSTRAINT chk_status CHECK (status IN (
        'DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'ARCHIVED', 'REJECTED'
    )),
    CONSTRAINT chk_difficulty CHECK (difficulty_level IN (
        'BEGINNER', 'EASY', 'MEDIUM', 'HARD', 'EXPERT'
    ))
);

-- ─────────────────────────────────────────────────────────────
-- Table: recipes_schema.recipe_steps
-- Purpose: Ordered cooking instructions
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.recipe_steps (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id       UUID            NOT NULL REFERENCES recipes_schema.recipes(id) ON DELETE CASCADE,
    step_number     SMALLINT        NOT NULL,
    title           VARCHAR(200),
    instruction     TEXT            NOT NULL,
    duration_minutes SMALLINT,
    image_url       VARCHAR(500),
    video_url       VARCHAR(500),
    tips            TEXT,
    temperature_c   DECIMAL(5,2),
    technique       VARCHAR(100),                             -- sauté, boil, bake, etc.
    created_at      TIMESTAMPTZ     DEFAULT NOW(),

    UNIQUE(recipe_id, step_number)
);

-- ─────────────────────────────────────────────────────────────
-- Table: recipes_schema.ingredients
-- Purpose: Global ingredient master catalog
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.ingredients (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200)    NOT NULL UNIQUE,
    name_fr         VARCHAR(200),
    name_es         VARCHAR(200),
    name_zh         VARCHAR(200),
    name_ar         VARCHAR(200),
    category        VARCHAR(100),                             -- vegetable, meat, spice, grain, etc.
    sub_category    VARCHAR(100),
    description     TEXT,
    image_url       VARCHAR(500),
    season          VARCHAR[]       DEFAULT '{}',             -- spring, summer, autumn, winter
    origin_country  VARCHAR(100),
    allergens       VARCHAR[]       DEFAULT '{}',
    is_common       BOOLEAN         DEFAULT TRUE,
    is_vegan        BOOLEAN         DEFAULT FALSE,
    is_gluten_free  BOOLEAN         DEFAULT TRUE,

    -- Nutrition per 100g
    calories_kcal   DECIMAL(8,2),
    protein_g       DECIMAL(8,2),
    carbs_g         DECIMAL(8,2),
    fat_g           DECIMAL(8,2),
    fiber_g         DECIMAL(8,2),

    usda_fdc_id     VARCHAR(50),                              -- USDA FoodData Central ID
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- Table: recipes_schema.recipe_ingredients
-- Purpose: Junction table linking recipes to ingredients with quantities
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.recipe_ingredients (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id       UUID            NOT NULL REFERENCES recipes_schema.recipes(id) ON DELETE CASCADE,
    ingredient_id   UUID            NOT NULL REFERENCES recipes_schema.ingredients(id),
    quantity        DECIMAL(10,3)   NOT NULL,
    unit            VARCHAR(50)     NOT NULL,                 -- g, ml, cup, tbsp, tsp, piece, etc.
    quantity_note   VARCHAR(200),                             -- "heaped", "packed", "roughly"
    preparation     VARCHAR(200),                             -- "finely chopped", "julienned"
    is_optional     BOOLEAN         DEFAULT FALSE,
    group_name      VARCHAR(100),                             -- "For the sauce", "For garnish"
    display_order   SMALLINT        DEFAULT 0,
    created_at      TIMESTAMPTZ     DEFAULT NOW(),

    UNIQUE(recipe_id, ingredient_id)
);
```

---

### 2.5 Restaurant Tables

```sql
-- ─────────────────────────────────────────────────────────────
-- Table: recipes_schema.restaurants
-- Purpose: Restaurant profiles for recipe ordering
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.restaurants (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id            UUID            NOT NULL REFERENCES users_schema.users(id),
    name                VARCHAR(200)    NOT NULL,
    slug                VARCHAR(250)    NOT NULL UNIQUE,
    description         TEXT,
    cuisine_types       VARCHAR[]       DEFAULT '{}',
    continent_id        UUID            REFERENCES geo_schema.continents(id),
    country_id          UUID            REFERENCES geo_schema.countries(id),

    -- Location
    address_line1       VARCHAR(300)    NOT NULL,
    address_line2       VARCHAR(300),
    city                VARCHAR(100)    NOT NULL,
    state_province      VARCHAR(100),
    postal_code         VARCHAR(20),
    latitude            DECIMAL(10,7),
    longitude           DECIMAL(10,7),
    location            GEOMETRY(POINT, 4326),               -- PostGIS

    -- Contact
    phone               VARCHAR(20),
    email               VARCHAR(320),
    website_url         VARCHAR(500),

    -- Media
    logo_url            VARCHAR(500),
    cover_url           VARCHAR(500),
    gallery_urls        VARCHAR[]       DEFAULT '{}',

    -- Business
    price_range         SMALLINT        CHECK (price_range BETWEEN 1 AND 4), -- $ $$ $$$ $$$$
    min_order_amount    DECIMAL(10,2)   DEFAULT 0,
    delivery_fee        DECIMAL(10,2)   DEFAULT 0,
    free_delivery_above DECIMAL(10,2),
    delivery_radius_km  DECIMAL(5,2),
    delivery_time_min   INTEGER         DEFAULT 30,           -- estimated minutes

    -- Status
    status              VARCHAR(20)     DEFAULT 'PENDING',
                                                              -- PENDING, APPROVED, SUSPENDED, CLOSED
    is_verified         BOOLEAN         DEFAULT FALSE,
    is_featured         BOOLEAN         DEFAULT FALSE,
    is_open_now         BOOLEAN         DEFAULT FALSE,
    accepts_online_orders BOOLEAN       DEFAULT TRUE,

    -- Ratings
    avg_rating          DECIMAL(3,2)    DEFAULT 0.0,
    rating_count        INTEGER         DEFAULT 0,
    total_orders        INTEGER         DEFAULT 0,

    -- Certification
    health_cert_url     VARCHAR(500),
    health_cert_expiry  DATE,
    business_license    VARCHAR(100),

    -- Fraud Prevention
    fraud_score         DECIMAL(5,4)    DEFAULT 0.0,
    fraud_flags         VARCHAR[]       DEFAULT '{}',
    manual_review_at    TIMESTAMPTZ,

    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
```

---

### 2.6 Order Tables

```sql
-- ─────────────────────────────────────────────────────────────
-- Table: orders_schema.orders
-- Purpose: Order lifecycle management
-- ─────────────────────────────────────────────────────────────
CREATE TABLE orders_schema.orders (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number        VARCHAR(20)     NOT NULL UNIQUE,      -- CRX-2026-000001
    user_id             UUID            NOT NULL REFERENCES users_schema.users(id),
    restaurant_id       UUID            REFERENCES recipes_schema.restaurants(id),

    -- Order Type
    order_type          VARCHAR(20)     NOT NULL DEFAULT 'DELIVERY',
                                                              -- DELIVERY, PICKUP, DINE_IN,
                                                              -- INGREDIENT_KIT, CATERING
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
                                                              -- PENDING, CONFIRMED, PREPARING,
                                                              -- READY, OUT_FOR_DELIVERY,
                                                              -- DELIVERED, CANCELLED, REFUNDED

    -- Pricing
    subtotal            DECIMAL(12,2)   NOT NULL,
    discount_amount     DECIMAL(12,2)   DEFAULT 0,
    delivery_fee        DECIMAL(12,2)   DEFAULT 0,
    service_fee         DECIMAL(12,2)   DEFAULT 0,
    tax_amount          DECIMAL(12,2)   DEFAULT 0,
    tip_amount          DECIMAL(12,2)   DEFAULT 0,
    total_amount        DECIMAL(12,2)   NOT NULL,
    currency_code       CHAR(3)         NOT NULL DEFAULT 'EUR',

    -- Delivery Details
    delivery_address    JSONB,                                -- {line1, line2, city, country, lat, lng}
    delivery_notes      TEXT,
    estimated_delivery  TIMESTAMPTZ,
    actual_delivery     TIMESTAMPTZ,

    -- Payment
    payment_status      VARCHAR(20)     DEFAULT 'PENDING',
                                                              -- PENDING, AUTHORIZED, PAID, FAILED, REFUNDED
    payment_method      VARCHAR(50),                          -- stripe, paypal, card, cash
    payment_intent_id   VARCHAR(200),                         -- Stripe PaymentIntent ID
    refund_amount       DECIMAL(12,2)   DEFAULT 0,
    refund_reason       TEXT,

    -- Coupon/Promo
    promo_code          VARCHAR(50),
    promo_discount      DECIMAL(12,2)   DEFAULT 0,

    -- Tracking
    tracking_url        VARCHAR(500),
    driver_id           UUID,
    driver_name         VARCHAR(100),
    driver_phone        VARCHAR(20),
    driver_location     GEOMETRY(POINT, 4326),

    -- Timestamps
    confirmed_at        TIMESTAMPTZ,
    preparing_at        TIMESTAMPTZ,
    ready_at            TIMESTAMPTZ,
    picked_up_at        TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    cancellation_reason TEXT,

    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW()
) PARTITION BY RANGE (created_at);

-- Monthly partitions for orders (high write volume)
CREATE TABLE orders_schema.orders_2026_01 PARTITION OF orders_schema.orders
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
CREATE TABLE orders_schema.orders_2026_02 PARTITION OF orders_schema.orders
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
-- ... continue for all months

-- ─────────────────────────────────────────────────────────────
-- Table: orders_schema.order_items
-- Purpose: Individual items within an order
-- ─────────────────────────────────────────────────────────────
CREATE TABLE orders_schema.order_items (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID            NOT NULL REFERENCES orders_schema.orders(id) ON DELETE CASCADE,
    recipe_id       UUID            REFERENCES recipes_schema.recipes(id),
    item_name       VARCHAR(300)    NOT NULL,
    item_type       VARCHAR(30)     NOT NULL DEFAULT 'RECIPE',
                                                              -- RECIPE, INGREDIENT_KIT, ADDON, FEE
    quantity        SMALLINT        NOT NULL DEFAULT 1,
    unit_price      DECIMAL(12,2)   NOT NULL,
    total_price     DECIMAL(12,2)   NOT NULL,
    customizations  JSONB           DEFAULT '{}',              -- special instructions, substitutions
    notes           TEXT,
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);
```

---

### 2.7 Social & Review Tables

```sql
-- ─────────────────────────────────────────────────────────────
-- Table: social_schema.reviews
-- Purpose: Recipe and restaurant reviews with rich feedback
-- ─────────────────────────────────────────────────────────────
CREATE TABLE social_schema.reviews (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    reviewer_id     UUID            NOT NULL REFERENCES users_schema.users(id),
    entity_type     VARCHAR(30)     NOT NULL,                 -- RECIPE, RESTAURANT
    entity_id       UUID            NOT NULL,
    order_id        UUID            REFERENCES orders_schema.orders(id),

    -- Ratings (1-5)
    overall_rating  SMALLINT        NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    taste_rating    SMALLINT        CHECK (taste_rating BETWEEN 1 AND 5),
    texture_rating  SMALLINT        CHECK (texture_rating BETWEEN 1 AND 5),
    presentation_rating SMALLINT    CHECK (presentation_rating BETWEEN 1 AND 5),
    value_rating    SMALLINT        CHECK (value_rating BETWEEN 1 AND 5),
    delivery_rating SMALLINT        CHECK (delivery_rating BETWEEN 1 AND 5),

    title           VARCHAR(200),
    content         TEXT            NOT NULL,
    media_urls      VARCHAR[]       DEFAULT '{}',
    tags            VARCHAR[]       DEFAULT '{}',             -- authentic, too-spicy, perfect-portion

    -- Helpfulness
    helpful_count   INTEGER         DEFAULT 0,
    unhelpful_count INTEGER         DEFAULT 0,

    -- Moderation
    is_verified_purchase BOOLEAN    DEFAULT FALSE,
    status          VARCHAR(20)     DEFAULT 'PUBLISHED',
    moderation_flag VARCHAR(50),

    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     DEFAULT NOW(),

    UNIQUE(reviewer_id, entity_type, entity_id)
);

-- ─────────────────────────────────────────────────────────────
-- Table: social_schema.follows
-- Purpose: User follow graph
-- ─────────────────────────────────────────────────────────────
CREATE TABLE social_schema.follows (
    follower_id     UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    following_id    UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ     DEFAULT NOW(),

    PRIMARY KEY (follower_id, following_id),
    CONSTRAINT no_self_follow CHECK (follower_id != following_id)
);

-- ─────────────────────────────────────────────────────────────
-- Table: social_schema.recipe_saves
-- Purpose: User recipe bookmarks and collections
-- ─────────────────────────────────────────────────────────────
CREATE TABLE social_schema.recipe_saves (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    recipe_id       UUID            NOT NULL REFERENCES recipes_schema.recipes(id) ON DELETE CASCADE,
    collection_name VARCHAR(100)    DEFAULT 'Saved',
    notes           TEXT,
    created_at      TIMESTAMPTZ     DEFAULT NOW(),

    UNIQUE(user_id, recipe_id)
);
```

---

### 2.8 Media & Notification Tables

```sql
-- ─────────────────────────────────────────────────────────────
-- Table: media_schema.media
-- Purpose: Media asset registry with processing status
-- ─────────────────────────────────────────────────────────────
CREATE TABLE media_schema.media (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    uploader_id     UUID            NOT NULL REFERENCES users_schema.users(id),
    entity_type     VARCHAR(50),                              -- RECIPE, USER, RESTAURANT, etc.
    entity_id       UUID,
    media_type      VARCHAR(20)     NOT NULL,                 -- IMAGE, VIDEO, AUDIO, DOCUMENT
    original_url    VARCHAR(500)    NOT NULL,
    cdn_url         VARCHAR(500),
    thumbnail_url   VARCHAR(500),
    file_name       VARCHAR(300),
    mime_type       VARCHAR(100),
    file_size_bytes BIGINT,
    width_px        INTEGER,
    height_px       INTEGER,
    duration_sec    INTEGER,
    alt_text        VARCHAR(500),
    caption         TEXT,
    processing_status VARCHAR(20)   DEFAULT 'PENDING',        -- PENDING, PROCESSING, READY, FAILED
    is_primary      BOOLEAN         DEFAULT FALSE,
    display_order   SMALLINT        DEFAULT 0,
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- Table: users_schema.notifications
-- Purpose: Notification queue and delivery tracking
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users_schema.notifications (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id    UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    sender_id       UUID            REFERENCES users_schema.users(id),
    notification_type VARCHAR(50)   NOT NULL,
                                                              -- RECIPE_LIKED, NEW_FOLLOWER,
                                                              -- ORDER_CONFIRMED, etc.
    title           VARCHAR(200)    NOT NULL,
    body            TEXT            NOT NULL,
    image_url       VARCHAR(500),
    action_url      VARCHAR(500),
    data            JSONB           DEFAULT '{}',
    channel         VARCHAR(20)     DEFAULT 'IN_APP',         -- IN_APP, EMAIL, PUSH, SMS
    is_read         BOOLEAN         DEFAULT FALSE,
    read_at         TIMESTAMPTZ,
    sent_at         TIMESTAMPTZ,
    delivery_status VARCHAR(20)     DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- Table: subscriptions_schema.subscriptions
-- Purpose: Premium subscription management
-- ─────────────────────────────────────────────────────────────
CREATE TABLE subscriptions_schema.subscriptions (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL REFERENCES users_schema.users(id),
    plan                VARCHAR(30)     NOT NULL,             -- FREE, EXPLORER, CHEF_PRO, ENTERPRISE
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    billing_cycle       VARCHAR(20)     DEFAULT 'MONTHLY',   -- MONTHLY, ANNUAL
    price               DECIMAL(10,2)   NOT NULL,
    currency_code       CHAR(3)         DEFAULT 'EUR',
    stripe_subscription_id VARCHAR(200),
    stripe_customer_id  VARCHAR(200),
    current_period_start TIMESTAMPTZ,
    current_period_end  TIMESTAMPTZ,
    cancel_at_period_end BOOLEAN        DEFAULT FALSE,
    cancelled_at        TIMESTAMPTZ,
    trial_end           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- Table: users_schema.eco_badges
-- Purpose: Gamification and sustainability achievement system
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users_schema.eco_badges (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50)     NOT NULL UNIQUE,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    criteria        TEXT,
    icon_url        VARCHAR(500),
    badge_type      VARCHAR(30)     DEFAULT 'ACHIEVEMENT',    -- ACHIEVEMENT, CERTIFICATION, MILESTONE
    points_value    INTEGER         DEFAULT 0,
    is_active       BOOLEAN         DEFAULT TRUE,
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);

CREATE TABLE users_schema.user_badges (
    user_id         UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    badge_id        UUID            NOT NULL REFERENCES users_schema.eco_badges(id),
    awarded_at      TIMESTAMPTZ     DEFAULT NOW(),
    awarded_reason  TEXT,
    PRIMARY KEY (user_id, badge_id)
);
```

---

## 3. MongoDB Collections

### 3.1 AI & Personalization Data

```javascript
// Collection: ai_user_profiles
// Purpose: ML feature store for user personalization
{
  _id: ObjectId(),
  userId: UUID,                          // References PostgreSQL users.id
  updatedAt: ISODate(),

  // Behavioral Vectors
  tasteProfile: {
    spicePreference: 0.7,               // 0.0 (mild) to 1.0 (very spicy)
    sweetPreference: 0.3,
    sourPreference: 0.5,
    umamiPreference: 0.8,
    preferredTextures: ["crunchy", "creamy"]
  },

  // Embedding vector for collaborative filtering
  userEmbedding: [0.234, -0.156, 0.789, ...], // 128-dimensional

  // Interaction history
  viewHistory: [
    { recipeId: UUID, viewedAt: ISODate(), durationSeconds: 120, source: "FEED" }
  ],
  likeHistory:    [{ recipeId: UUID, likedAt: ISODate() }],
  orderHistory:   [{ recipeId: UUID, orderedAt: ISODate(), rating: 4.5 }],
  searchHistory:  [{ query: "thai curry", searchedAt: ISODate(), clickedId: UUID }],

  // Geographic preferences
  cuisineAffinities: {
    "asian": 0.85,
    "mediterranean": 0.72,
    "west_african": 0.61
  },

  // Model metadata
  modelVersion: "v2.1.0",
  lastTrainingAt: ISODate()
}

// Collection: ai_recipe_metadata
// Purpose: AI-generated insights and embeddings for recipes
{
  _id: ObjectId(),
  recipeId: UUID,
  createdAt: ISODate(),
  updatedAt: ISODate(),

  // Semantic embedding for similarity search
  embedding: [0.145, 0.782, -0.234, ...],  // 1536-dimensional (OpenAI ada-002)

  // AI-generated tags (beyond user-provided)
  aiTags: ["comfort food", "rainy day", "family gathering"],

  // Flavor profile analysis
  flavorProfile: {
    dominant: "umami",
    secondary: ["savory", "aromatic"],
    intensities: { sweet: 0.2, sour: 0.1, salty: 0.6, bitter: 0.1, umami: 0.8, spicy: 0.5 }
  },

  // Cultural authenticity score
  authenticityScore: 0.87,
  authenticityNotes: "Uses traditional spice blend ratio",

  // Similarity relationships
  similarRecipeIds: [UUID, UUID, UUID],    // top-10 most similar

  // Translation cache
  translations: {
    fr: { title: "Poulet Yassa", description: "..." },
    es: { title: "Pollo Yassa", description: "..." },
    zh: { title: "亚萨鸡", description: "..." }
  },

  // Trend data
  trendScore: 0.73,
  trendRegions: ["fr_FR", "ca_CA"],
  trendPeakDate: ISODate()
}

// Collection: ai_generated_recipes
// Purpose: Storage for AI-generated recipe drafts
{
  _id: ObjectId(),
  requestId: UUID,
  userId: UUID,
  createdAt: ISODate(),
  status: "DRAFT",                         // DRAFT, ACCEPTED, REJECTED

  prompt: {
    cuisineType: "Japanese",
    ingredients: ["salmon", "avocado", "nori"],
    dietaryRestrictions: ["gluten_free"],
    servings: 4,
    difficultyTarget: "MEDIUM"
  },

  generatedRecipe: {
    title: "Salmon Avocado Roll Bowl",
    description: "...",
    ingredients: [...],
    steps: [...],
    estimatedTime: 25,
    nutritionEstimate: { calories: 420, protein: 32, carbs: 38, fat: 15 }
  },

  modelUsed: "gpt-4o",
  promptTokens: 823,
  completionTokens: 1204,
  generationTimeMs: 3421
}
```

---

### 3.2 Social Posts Collection

```javascript
// Collection: social_posts
// Purpose: Rich social content (more flexible than SQL)
{
  _id: ObjectId(),
  postId: UUID,
  authorId: UUID,
  createdAt: ISODate(),
  updatedAt: ISODate(),

  postType: "RECIPE_SHARE",               // RECIPE_SHARE, COOKING_VIDEO, QUESTION, TIP, REVIEW

  content: {
    text: "Just made this incredible Mole Negro...",
    recipeId: UUID,                        // linked recipe (optional)
    mediaUrls: ["https://media.cerex.com/..."],
    location: { city: "Oaxaca", country: "MX" },
    tags: ["#MexicanFood", "#MoleNegro", "#homecooking"]
  },

  engagement: {
    likes: 342,
    comments: 47,
    shares: 23,
    saves: 189,
    views: 4521
  },

  visibility: "PUBLIC",                   // PUBLIC, FOLLOWERS_ONLY, PRIVATE

  comments: [
    {
      commentId: UUID,
      authorId: UUID,
      text: "This looks amazing!",
      likes: 12,
      createdAt: ISODate(),
      replies: [...]
    }
  ],

  moderationStatus: "APPROVED"
}
```

---

### 3.3 Media Metadata Collection

```javascript
// Collection: media_metadata
// Purpose: Rich media metadata beyond what PostgreSQL stores
{
  _id: ObjectId(),
  mediaId: UUID,                           // References PostgreSQL media.id
  entityType: "RECIPE",
  entityId: UUID,
  uploadedAt: ISODate(),

  // EXIF data (for photos)
  exif: {
    cameraModel: "iPhone 15 Pro",
    focalLength: 24,
    iso: 400,
    shutterSpeed: "1/120",
    colorSpace: "sRGB",
    gpsLatitude: 48.8566,
    gpsLongitude: 2.3522
  },

  // AI content analysis
  contentAnalysis: {
    labels: ["food", "cuisine", "plate", "dish"],
    dominantColors: ["#8B4513", "#FFD700", "#228B22"],
    foodDetected: true,
    detectedDishes: ["curry", "rice"],
    qualityScore: 0.87,
    isNSFW: false,
    safeSearchAnnotation: { adult: "VERY_UNLIKELY", violence: "VERY_UNLIKELY" }
  },

  // Processing results
  variants: {
    original: { url: "...", width: 4032, height: 3024, sizeBytes: 4521000 },
    hero: { url: "...", width: 1200, height: 800, sizeBytes: 245000 },
    card: { url: "...", width: 400, height: 300, sizeBytes: 45000 },
    thumb: { url: "...", width: 80, height: 80, sizeBytes: 8000 }
  }
}
```

---

## 4. Redis Key Patterns

```
# ── SESSIONS ──────────────────────────────────────────────────
session:{userId}                              TYPE: Hash    TTL: 86400s
  Fields: token, refreshToken, deviceId, createdAt, lastActive

# ── RECIPE CACHE ──────────────────────────────────────────────
recipe:{recipeId}:detail                      TYPE: String  TTL: 300s
  Value: JSON serialized RecipeDetailDTO

recipe:feed:{userId}:page:{n}                 TYPE: String  TTL: 300s
  Value: JSON array of RecipeCardDTO

recipe:trending:{continentCode}:{period}      TYPE: Sorted Set TTL: 600s
  Members: recipeId, Score: trending_score

recipe:popular:{categorySlug}                 TYPE: Sorted Set TTL: 3600s
  Members: recipeId, Score: view_count

recipe:search:{queryHash}                     TYPE: String  TTL: 120s
  Value: JSON search results

# ── USER CACHE ────────────────────────────────────────────────
user:{userId}:profile                         TYPE: Hash    TTL: 3600s
user:{userId}:preferences                     TYPE: Hash    TTL: 7200s
user:{userId}:recommendations                 TYPE: List    TTL: 1800s
user:{userId}:notifications:unread            TYPE: Integer TTL: 86400s

# ── RATE LIMITING ─────────────────────────────────────────────
ratelimit:{userId}:api:{endpoint}             TYPE: Integer TTL: 60s
ratelimit:{ip}:login                          TYPE: Integer TTL: 900s
ratelimit:{userId}:order                      TYPE: Integer TTL: 3600s

# ── ORDER TRACKING ────────────────────────────────────────────
order:{orderId}:status                        TYPE: String  TTL: 86400s
order:{orderId}:driver:location               TYPE: Hash    TTL: 86400s
  Fields: lat, lng, updatedAt

# ── SOCIAL FEED ───────────────────────────────────────────────
feed:{userId}:social                          TYPE: Sorted Set TTL: 1800s
  Members: postId, Score: published_timestamp

# ── AUTOCOMPLETE ──────────────────────────────────────────────
autocomplete:recipe:{prefix}                  TYPE: Sorted Set TTL: 3600s
autocomplete:ingredient:{prefix}              TYPE: Sorted Set TTL: 3600s

# ── PUB/SUB CHANNELS ──────────────────────────────────────────
channel:order:{orderId}:updates               TYPE: PubSub
channel:user:{userId}:notifications           TYPE: PubSub

# ── FEATURE FLAGS ─────────────────────────────────────────────
feature_flags                                 TYPE: Hash    TTL: none
  Fields: ai_recommendations, new_search, beta_ordering, etc.

# ── DISTRIBUTED LOCKS ─────────────────────────────────────────
lock:recipe:publish:{recipeId}                TYPE: String  TTL: 30s
lock:order:create:{userId}                    TYPE: String  TTL: 10s
```

---

## 5. Elasticsearch Mappings

```json
{
  "mappings": {
    "properties": {
      "recipeId":    { "type": "keyword" },
      "title":       { "type": "text", "analyzer": "multilingual_analyzer",
                       "fields": { "keyword": { "type": "keyword" } } },
      "description": { "type": "text", "analyzer": "multilingual_analyzer" },
      "tags":        { "type": "keyword" },
      "continentCode":  { "type": "keyword" },
      "countryCode":    { "type": "keyword" },
      "cuisineType":    { "type": "keyword" },
      "difficultyLevel":{ "type": "keyword" },
      "totalTimeMin":   { "type": "integer" },
      "avgRating":      { "type": "float" },
      "viewCount":      { "type": "long" },
      "isVegan":        { "type": "boolean" },
      "isGlutenFree":   { "type": "boolean" },
      "isHalal":        { "type": "boolean" },
      "publishedAt":    { "type": "date" },
      "embedding":   {
        "type": "dense_vector",
        "dims": 1536,
        "index": true,
        "similarity": "cosine"
      }
    }
  }
}
```

---

## 6. Indexing Strategy

```sql
-- ── PERFORMANCE INDEXES ──────────────────────────────────────

-- Recipes: common query patterns
CREATE INDEX CONCURRENTLY idx_recipes_status_published
    ON recipes_schema.recipes(status, published_at DESC)
    WHERE status = 'PUBLISHED';

CREATE INDEX CONCURRENTLY idx_recipes_author
    ON recipes_schema.recipes(author_id) WHERE deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_recipes_continent_country
    ON recipes_schema.recipes(continent_id, country_id)
    WHERE status = 'PUBLISHED';

CREATE INDEX CONCURRENTLY idx_recipes_dietary
    ON recipes_schema.recipes(is_vegan, is_vegetarian, is_gluten_free, is_halal)
    WHERE status = 'PUBLISHED';

CREATE INDEX CONCURRENTLY idx_recipes_tags
    ON recipes_schema.recipes USING GIN(tags);

CREATE INDEX CONCURRENTLY idx_recipes_fts
    ON recipes_schema.recipes USING GIN(
        to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(description,''))
    );

-- Orders: time-based queries (works with partitioning)
CREATE INDEX CONCURRENTLY idx_orders_user_created
    ON orders_schema.orders(user_id, created_at DESC);

CREATE INDEX CONCURRENTLY idx_orders_restaurant_status
    ON orders_schema.orders(restaurant_id, status);

-- Users: authentication queries
CREATE UNIQUE INDEX idx_users_email_lower
    ON users_schema.users(LOWER(email));

CREATE INDEX CONCURRENTLY idx_users_oauth
    ON users_schema.users(oauth_provider, oauth_provider_id)
    WHERE oauth_provider IS NOT NULL;

-- Restaurants: geo-search (PostGIS)
CREATE INDEX CONCURRENTLY idx_restaurants_location
    ON recipes_schema.restaurants USING GIST(location);

-- Reviews: entity lookups
CREATE INDEX CONCURRENTLY idx_reviews_entity
    ON social_schema.reviews(entity_type, entity_id);

CREATE INDEX CONCURRENTLY idx_reviews_reviewer
    ON social_schema.reviews(reviewer_id);
```

---

## 7. Partitioning Strategy

```sql
-- Orders table: RANGE partitioned by created_at (monthly)
-- Creates 12 partitions per year automatically with pg_partman:
SELECT partman.create_parent(
    p_parent_table => 'orders_schema.orders',
    p_control => 'created_at',
    p_type => 'native',
    p_interval => 'monthly',
    p_retention => '24 months',
    p_retention_keep_table => TRUE
);

-- Recipe views: RANGE partitioned by month for analytics
CREATE TABLE analytics_schema.recipe_views (
    id          UUID    DEFAULT gen_random_uuid(),
    recipe_id   UUID    NOT NULL,
    user_id     UUID,
    session_id  VARCHAR(100),
    viewed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source      VARCHAR(50),
    duration_s  INTEGER
) PARTITION BY RANGE (viewed_at);

-- User activity: HASH partitioned by user_id for even distribution
CREATE TABLE analytics_schema.user_events (
    id          UUID    DEFAULT gen_random_uuid(),
    user_id     UUID    NOT NULL,
    event_type  VARCHAR(50),
    event_data  JSONB,
    created_at  TIMESTAMPTZ DEFAULT NOW()
) PARTITION BY HASH (user_id);

-- Create 16 hash partitions
DO $$
BEGIN
    FOR i IN 0..15 LOOP
        EXECUTE format(
            'CREATE TABLE analytics_schema.user_events_%s
             PARTITION OF analytics_schema.user_events
             FOR VALUES WITH (modulus 16, remainder %s)', i, i
        );
    END LOOP;
END $$;
```

---

## 8. Entity Relationship Diagram

```
geo_schema
─────────────────────────────────────────────────
continents ──1:N──► countries ──1:N──► cultures

users_schema
─────────────────────────────────────────────────
users ──1:1──► user_profiles
users ──1:N──► notifications
users ──1:1──► subscriptions
users ──N:M──► eco_badges (via user_badges)

recipes_schema
─────────────────────────────────────────────────
categories ──1:N──► recipes (tree structure)
users ──1:N──► recipes (author)
continents ──1:N──► recipes
countries ──1:N──► recipes
cultures ──1:N──► recipes
recipes ──1:N──► recipe_steps
recipes ──N:M──► ingredients (via recipe_ingredients)
users ──1:N──► restaurants
restaurants ──N:M──► recipes (via restaurant_menu)

orders_schema
─────────────────────────────────────────────────
users ──1:N──► orders
restaurants ──1:N──► orders
orders ──1:N──► order_items
order_items ──N:1──► recipes

social_schema
─────────────────────────────────────────────────
users ──N:M──► users (via follows)
users ──N:M──► recipes (via recipe_saves)
users ──1:N──► reviews (reviewer)
recipes ──1:N──► reviews
restaurants ──1:N──► reviews
```

---

*Document Version: 1.0.0 | Next Review: 2026-06-30 | Owner: Cerex Data Engineering Team*

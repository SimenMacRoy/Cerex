-- ─────────────────────────────────────────────────────────────
-- V3__create_recipes_tables.sql
-- Recipe entities, ingredients, steps, categories, media
-- ─────────────────────────────────────────────────────────────

-- ─────────────────────────────────────────────────────────────
-- Categories
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.categories (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)    NOT NULL,
    slug            VARCHAR(150)    NOT NULL UNIQUE,
    description     TEXT,
    icon_url        VARCHAR(500),
    parent_id       UUID            REFERENCES recipes_schema.categories(id),
    display_order   INT             DEFAULT 0,
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- Recipes
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.recipes (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id           UUID            NOT NULL REFERENCES users_schema.users(id),
    continent_id        UUID            REFERENCES geo_schema.continents(id),
    country_id          UUID            REFERENCES geo_schema.countries(id),
    culture_id          UUID            REFERENCES geo_schema.cultures(id),
    category_id         UUID            REFERENCES recipes_schema.categories(id),
    title               VARCHAR(300)    NOT NULL,
    title_fr            VARCHAR(300),
    title_es            VARCHAR(300),
    title_zh            VARCHAR(300),
    title_ar            VARCHAR(300),
    slug                VARCHAR(400)    NOT NULL UNIQUE,
    description         TEXT            NOT NULL,
    description_fr      TEXT,
    description_es      TEXT,
    story               TEXT,
    recipe_type         VARCHAR(30)     NOT NULL DEFAULT 'DISH',
    cuisine_type        VARCHAR(100),
    course_type         VARCHAR(50),
    difficulty_level    VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM',
    spice_level         INT             DEFAULT 1,
    prep_time_minutes   INT             NOT NULL DEFAULT 0,
    cook_time_minutes   INT             NOT NULL DEFAULT 0,
    rest_time_minutes   INT             DEFAULT 0,
    servings            INT             NOT NULL DEFAULT 4,
    servings_unit       VARCHAR(50)     DEFAULT 'persons',
    -- Nutrition
    calories_kcal       DECIMAL(8, 2),
    protein_g           DECIMAL(8, 2),
    carbs_g             DECIMAL(8, 2),
    fat_g               DECIMAL(8, 2),
    fiber_g             DECIMAL(8, 2),
    sugar_g             DECIMAL(8, 2),
    sodium_mg           DECIMAL(8, 2),
    -- Dietary flags
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
    -- Engagement
    view_count          BIGINT          DEFAULT 0,
    like_count          INT             DEFAULT 0,
    save_count          INT             DEFAULT 0,
    order_count         INT             DEFAULT 0,
    share_count         INT             DEFAULT 0,
    comment_count       INT             DEFAULT 0,
    avg_rating          DECIMAL(3, 2)   DEFAULT 0.00,
    rating_count        INT             DEFAULT 0,
    -- Status
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    is_ai_generated     BOOLEAN         DEFAULT FALSE,
    ai_model_version    VARCHAR(50),
    is_featured         BOOLEAN         DEFAULT FALSE,
    is_premium          BOOLEAN         DEFAULT FALSE,
    moderation_status   VARCHAR(30)     DEFAULT 'PENDING',
    moderation_note     TEXT,
    -- SEO
    meta_title          VARCHAR(200),
    meta_description    VARCHAR(500),
    keywords            VARCHAR[],
    tags                VARCHAR[],
    -- Versioning
    version             INT             DEFAULT 1,
    original_recipe_id  UUID,
    forked_from_id      UUID,
    -- Timestamps
    published_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ,

    CONSTRAINT chk_recipes_status CHECK (status IN ('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'ARCHIVED', 'REJECTED')),
    CONSTRAINT chk_recipes_difficulty CHECK (difficulty_level IN ('BEGINNER', 'EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_recipes_type CHECK (recipe_type IN ('DISH', 'BEVERAGE', 'DESSERT', 'SNACK', 'SAUCE', 'BREAD', 'SOUP', 'SALAD', 'SIDE_DISH', 'MARINADE', 'BREAKFAST', 'STREET_FOOD'))
);

CREATE INDEX idx_recipes_author_id    ON recipes_schema.recipes(author_id);
CREATE INDEX idx_recipes_slug         ON recipes_schema.recipes(slug);
CREATE INDEX idx_recipes_status       ON recipes_schema.recipes(status);
CREATE INDEX idx_recipes_continent_id ON recipes_schema.recipes(continent_id);
CREATE INDEX idx_recipes_country_id   ON recipes_schema.recipes(country_id);
CREATE INDEX idx_recipes_culture_id   ON recipes_schema.recipes(culture_id);
CREATE INDEX idx_recipes_published_at ON recipes_schema.recipes(published_at);
CREATE INDEX idx_recipes_avg_rating   ON recipes_schema.recipes(avg_rating DESC);

-- ─────────────────────────────────────────────────────────────
-- Ingredients Master Catalog
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.ingredients (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(200)    NOT NULL,
    name_fr             VARCHAR(200),
    name_es             VARCHAR(200),
    category            VARCHAR(100),
    image_url           VARCHAR(500),
    is_allergen         BOOLEAN         DEFAULT FALSE,
    allergen_type       VARCHAR(50),
    calories_per_100g   INT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ingredients_name     ON recipes_schema.ingredients(name);
CREATE INDEX idx_ingredients_category ON recipes_schema.ingredients(category);

-- ─────────────────────────────────────────────────────────────
-- Recipe ↔ Ingredient junction
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.recipe_ingredients (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id       UUID            NOT NULL REFERENCES recipes_schema.recipes(id) ON DELETE CASCADE,
    ingredient_id   UUID            NOT NULL REFERENCES recipes_schema.ingredients(id),
    quantity        DECIMAL(10, 3),
    unit            VARCHAR(50),
    display_text    VARCHAR(300),
    is_optional     BOOLEAN         DEFAULT FALSE,
    group_name      VARCHAR(100),
    display_order   INT             DEFAULT 0,
    UNIQUE(recipe_id, ingredient_id)
);

CREATE INDEX idx_recipe_ingredients_recipe_id ON recipes_schema.recipe_ingredients(recipe_id);

-- ─────────────────────────────────────────────────────────────
-- Recipe Steps
-- ─────────────────────────────────────────────────────────────
CREATE TABLE recipes_schema.recipe_steps (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id       UUID            NOT NULL REFERENCES recipes_schema.recipes(id) ON DELETE CASCADE,
    step_number     INT             NOT NULL,
    instruction     TEXT            NOT NULL,
    duration_minutes INT,
    image_url       VARCHAR(500),
    tip             VARCHAR(500),
    UNIQUE(recipe_id, step_number)
);

CREATE INDEX idx_recipe_steps_recipe_id ON recipes_schema.recipe_steps(recipe_id);

-- ─────────────────────────────────────────────────────────────
-- Media Assets
-- ─────────────────────────────────────────────────────────────
CREATE TABLE media_schema.media (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type         VARCHAR(30)     NOT NULL,
    entity_id           UUID            NOT NULL,
    media_type          VARCHAR(20)     NOT NULL,
    original_url        VARCHAR(500)    NOT NULL,
    cdn_url             VARCHAR(500),
    thumbnail_url       VARCHAR(500),
    mime_type           VARCHAR(100),
    file_size           BIGINT,
    width               INT,
    height              INT,
    alt_text            VARCHAR(300),
    processing_status   VARCHAR(20)     DEFAULT 'PENDING',
    uploaded_by         UUID,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_media_entity    ON media_schema.media(entity_type, entity_id);
CREATE INDEX idx_media_uploader  ON media_schema.media(uploaded_by);

-- ─────────────────────────────────────────────────────────────
-- Social: Reviews, Follows, Saves
-- ─────────────────────────────────────────────────────────────
CREATE TABLE social_schema.reviews (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    reviewer_id     UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    entity_type     VARCHAR(30)     NOT NULL,
    entity_id       UUID            NOT NULL,
    overall_rating  INT             NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    taste_rating    INT             CHECK (taste_rating BETWEEN 1 AND 5),
    presentation_rating INT         CHECK (presentation_rating BETWEEN 1 AND 5),
    title           VARCHAR(200),
    body            TEXT,
    helpful_count   INT             DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE(reviewer_id, entity_type, entity_id)
);

CREATE TABLE social_schema.follows (
    follower_id     UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    following_id    UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id),
    CONSTRAINT no_self_follow CHECK (follower_id != following_id)
);

CREATE TABLE social_schema.recipe_saves (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    recipe_id       UUID            NOT NULL REFERENCES recipes_schema.recipes(id) ON DELETE CASCADE,
    collection_name VARCHAR(100)    DEFAULT 'Favorites',
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    UNIQUE(user_id, recipe_id)
);

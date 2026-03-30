-- ─────────────────────────────────────────────────────────────
-- V6__create_restaurants_tables.sql
-- Restaurant, Grocery, Menu, MenuItem tables
-- ─────────────────────────────────────────────────────────────

-- ─────────────────────────────────────────────────────────
-- Restaurants
-- ─────────────────────────────────────────────────────────
CREATE TABLE orders_schema.restaurants (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id                    UUID            NOT NULL REFERENCES users_schema.users(id),
    status                      VARCHAR(30)     NOT NULL DEFAULT 'PENDING_VERIFICATION',
    name                        VARCHAR(200)    NOT NULL,
    description                 TEXT,
    slug                        VARCHAR(300)    UNIQUE,
    cuisine_type                VARCHAR(100),
    phone                       VARCHAR(30),
    email                       VARCHAR(200),
    website                     VARCHAR(500),
    address_line1               VARCHAR(300),
    address_line2               VARCHAR(300),
    city                        VARCHAR(100),
    state_province              VARCHAR(100),
    postal_code                 VARCHAR(20),
    country_id                  UUID            REFERENCES geo_schema.countries(id),
    latitude                    NUMERIC(10,7),
    longitude                   NUMERIC(10,7),
    delivery_radius_km          DOUBLE PRECISION DEFAULT 10.0,
    timezone                    VARCHAR(50)     DEFAULT 'UTC',
    operating_hours             JSONB,
    minimum_order_amount        NUMERIC(10,2)   DEFAULT 0,
    average_preparation_time_min INT            DEFAULT 30,
    logo_url                    VARCHAR(500),
    cover_image_url             VARCHAR(500),
    average_rating              NUMERIC(3,2)    DEFAULT 0,
    total_reviews               INT             DEFAULT 0,
    total_orders                INT             DEFAULT 0,
    stripe_connect_account_id   VARCHAR(100),
    commission_rate             NUMERIC(5,4)    DEFAULT 0.15,
    is_premium_partner          BOOLEAN         DEFAULT FALSE,
    is_verified                 BOOLEAN         DEFAULT FALSE,
    eco_score                   INT             DEFAULT 0,
    supports_takeaway           BOOLEAN         DEFAULT TRUE,
    supports_delivery           BOOLEAN         DEFAULT TRUE,
    supports_dine_in            BOOLEAN         DEFAULT TRUE,
    accepts_reservations        BOOLEAN         DEFAULT FALSE,
    offers_catering             BOOLEAN         DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     DEFAULT NOW(),
    verified_at                 TIMESTAMPTZ
);

CREATE INDEX idx_restaurants_owner_id    ON orders_schema.restaurants(owner_id);
CREATE INDEX idx_restaurants_status      ON orders_schema.restaurants(status);
CREATE INDEX idx_restaurants_city        ON orders_schema.restaurants(city);
CREATE INDEX idx_restaurants_country_id  ON orders_schema.restaurants(country_id);
CREATE INDEX idx_restaurants_cuisine     ON orders_schema.restaurants(cuisine_type);
CREATE INDEX idx_restaurants_rating      ON orders_schema.restaurants(average_rating);
CREATE INDEX idx_restaurants_geo         ON orders_schema.restaurants(latitude, longitude);

-- Cuisine tags
CREATE TABLE orders_schema.restaurant_cuisine_tags (
    restaurant_id   UUID        NOT NULL REFERENCES orders_schema.restaurants(id) ON DELETE CASCADE,
    tag             VARCHAR(100) NOT NULL,
    PRIMARY KEY (restaurant_id, tag)
);

-- Gallery images
CREATE TABLE orders_schema.restaurant_gallery (
    restaurant_id   UUID        NOT NULL REFERENCES orders_schema.restaurants(id) ON DELETE CASCADE,
    image_url       VARCHAR(500) NOT NULL
);

-- ─────────────────────────────────────────────────────────
-- Menus
-- ─────────────────────────────────────────────────────────
CREATE TABLE orders_schema.menus (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID            NOT NULL REFERENCES orders_schema.restaurants(id) ON DELETE CASCADE,
    name            VARCHAR(200)    NOT NULL,
    description     TEXT,
    menu_type       VARCHAR(30)     DEFAULT 'REGULAR',
    is_active       BOOLEAN         DEFAULT TRUE,
    sort_order      INT             DEFAULT 0,
    available_from  VARCHAR(10),
    available_until VARCHAR(10),
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     DEFAULT NOW()
);

CREATE INDEX idx_menus_restaurant_id ON orders_schema.menus(restaurant_id);

-- ─────────────────────────────────────────────────────────
-- Menu Items
-- ─────────────────────────────────────────────────────────
CREATE TABLE orders_schema.menu_items (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_id             UUID            NOT NULL REFERENCES orders_schema.menus(id) ON DELETE CASCADE,
    recipe_id           UUID,
    name                VARCHAR(200)    NOT NULL,
    description         TEXT,
    category            VARCHAR(100),
    price               NUMERIC(10,2)   NOT NULL,
    discount_price      NUMERIC(10,2),
    currency_code       VARCHAR(3)      DEFAULT 'EUR',
    calories            INT,
    portion_size        VARCHAR(50),
    is_vegan            BOOLEAN         DEFAULT FALSE,
    is_vegetarian       BOOLEAN         DEFAULT FALSE,
    is_gluten_free      BOOLEAN         DEFAULT FALSE,
    is_halal            BOOLEAN         DEFAULT FALSE,
    is_kosher           BOOLEAN         DEFAULT FALSE,
    image_url           VARCHAR(500),
    is_available        BOOLEAN         DEFAULT TRUE,
    is_featured         BOOLEAN         DEFAULT FALSE,
    is_spicy            BOOLEAN         DEFAULT FALSE,
    spice_level         INT             DEFAULT 0,
    sort_order          INT             DEFAULT 0,
    preparation_time_min INT,
    eco_score           INT             DEFAULT 0,
    carbon_footprint_g  INT,
    total_orders        INT             DEFAULT 0,
    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW()
);

CREATE INDEX idx_menu_items_menu_id   ON orders_schema.menu_items(menu_id);
CREATE INDEX idx_menu_items_recipe_id ON orders_schema.menu_items(recipe_id);
CREATE INDEX idx_menu_items_category  ON orders_schema.menu_items(category);

-- Menu item allergens
CREATE TABLE orders_schema.menu_item_allergens (
    menu_item_id    UUID        NOT NULL REFERENCES orders_schema.menu_items(id) ON DELETE CASCADE,
    allergen        VARCHAR(50) NOT NULL,
    PRIMARY KEY (menu_item_id, allergen)
);

-- ─────────────────────────────────────────────────────────
-- Restaurant Reviews
-- ─────────────────────────────────────────────────────────
CREATE TABLE orders_schema.restaurant_reviews (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id       UUID            NOT NULL REFERENCES orders_schema.restaurants(id) ON DELETE CASCADE,
    user_id             UUID            NOT NULL REFERENCES users_schema.users(id),
    order_id            UUID,
    rating              INT             NOT NULL CHECK (rating BETWEEN 1 AND 5),
    food_rating         INT             CHECK (food_rating BETWEEN 1 AND 5),
    service_rating      INT             CHECK (service_rating BETWEEN 1 AND 5),
    delivery_rating     INT             CHECK (delivery_rating BETWEEN 1 AND 5),
    ambiance_rating     INT             CHECK (ambiance_rating BETWEEN 1 AND 5),
    comment             TEXT,
    is_verified_purchase BOOLEAN        DEFAULT FALSE,
    helpful_count       INT             DEFAULT 0,
    is_flagged          BOOLEAN         DEFAULT FALSE,
    restaurant_reply    TEXT,
    replied_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW(),
    UNIQUE (restaurant_id, user_id)
);

CREATE INDEX idx_rest_reviews_restaurant ON orders_schema.restaurant_reviews(restaurant_id);
CREATE INDEX idx_rest_reviews_user       ON orders_schema.restaurant_reviews(user_id);

-- ─────────────────────────────────────────────────────────
-- Groceries
-- ─────────────────────────────────────────────────────────
CREATE TABLE orders_schema.groceries (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id                UUID            NOT NULL REFERENCES users_schema.users(id),
    status                  VARCHAR(30)     NOT NULL DEFAULT 'PENDING_VERIFICATION',
    name                    VARCHAR(200)    NOT NULL,
    description             TEXT,
    slug                    VARCHAR(300)    UNIQUE,
    grocery_type            VARCHAR(30)     DEFAULT 'GENERAL',
    phone                   VARCHAR(30),
    email                   VARCHAR(200),
    website                 VARCHAR(500),
    address_line1           VARCHAR(300),
    city                    VARCHAR(100),
    state_province          VARCHAR(100),
    postal_code             VARCHAR(20),
    country_id              UUID            REFERENCES geo_schema.countries(id),
    latitude                NUMERIC(10,7),
    longitude               NUMERIC(10,7),
    delivery_radius_km      DOUBLE PRECISION DEFAULT 15.0,
    operating_hours         JSONB,
    logo_url                VARCHAR(500),
    cover_image_url         VARCHAR(500),
    average_rating          NUMERIC(3,2)    DEFAULT 0,
    total_reviews           INT             DEFAULT 0,
    supports_delivery       BOOLEAN         DEFAULT TRUE,
    supports_pickup         BOOLEAN         DEFAULT TRUE,
    minimum_order_amount    NUMERIC(10,2)   DEFAULT 0,
    is_organic_certified    BOOLEAN         DEFAULT FALSE,
    is_verified             BOOLEAN         DEFAULT FALSE,
    eco_score               INT             DEFAULT 0,
    stripe_connect_account_id VARCHAR(100),
    created_at              TIMESTAMPTZ     DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     DEFAULT NOW()
);

CREATE INDEX idx_groceries_owner_id   ON orders_schema.groceries(owner_id);
CREATE INDEX idx_groceries_status     ON orders_schema.groceries(status);
CREATE INDEX idx_groceries_city       ON orders_schema.groceries(city);
CREATE INDEX idx_groceries_country_id ON orders_schema.groceries(country_id);

-- Grocery specialty tags
CREATE TABLE orders_schema.grocery_specialty_tags (
    grocery_id  UUID        NOT NULL REFERENCES orders_schema.groceries(id) ON DELETE CASCADE,
    tag         VARCHAR(100) NOT NULL,
    PRIMARY KEY (grocery_id, tag)
);

-- ─────────────────────────────────────────────────────────
-- Grocery Products
-- ─────────────────────────────────────────────────────────
CREATE TABLE orders_schema.grocery_products (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    grocery_id          UUID            NOT NULL REFERENCES orders_schema.groceries(id) ON DELETE CASCADE,
    ingredient_id       UUID,
    name                VARCHAR(200)    NOT NULL,
    description         TEXT,
    category            VARCHAR(100),
    brand               VARCHAR(100),
    price               NUMERIC(10,2)   NOT NULL,
    price_per_unit      NUMERIC(10,2),
    unit                VARCHAR(30),
    weight_grams        INT,
    stock_quantity      INT             DEFAULT 0,
    is_in_stock         BOOLEAN         DEFAULT TRUE,
    image_url           VARCHAR(500),
    barcode             VARCHAR(50),
    origin_country      VARCHAR(100),
    is_organic          BOOLEAN         DEFAULT FALSE,
    is_local            BOOLEAN         DEFAULT FALSE,
    is_fair_trade       BOOLEAN         DEFAULT FALSE,
    eco_score           INT             DEFAULT 0,
    nutri_score         VARCHAR(2),
    carbon_footprint_g  INT,
    total_sold          INT             DEFAULT 0,
    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW()
);

CREATE INDEX idx_grocery_products_grocery    ON orders_schema.grocery_products(grocery_id);
CREATE INDEX idx_grocery_products_ingredient ON orders_schema.grocery_products(ingredient_id);
CREATE INDEX idx_grocery_products_category   ON orders_schema.grocery_products(category);

-- Grocery product allergens
CREATE TABLE orders_schema.grocery_product_allergens (
    product_id  UUID        NOT NULL REFERENCES orders_schema.grocery_products(id) ON DELETE CASCADE,
    allergen    VARCHAR(50) NOT NULL,
    PRIMARY KEY (product_id, allergen)
);

-- ─────────────────────────────────────────────────────────────
-- V4__create_orders_tables.sql
-- Order lifecycle management, order items
-- ─────────────────────────────────────────────────────────────

-- ─────────────────────────────────────────────────────────────
-- Orders (partitioned by created_at for performance)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE orders_schema.orders (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number        VARCHAR(20)     NOT NULL UNIQUE,
    user_id             UUID            NOT NULL REFERENCES users_schema.users(id),
    restaurant_id       UUID,
    order_type          VARCHAR(20)     NOT NULL DEFAULT 'DELIVERY',
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    -- Pricing
    subtotal            DECIMAL(12, 2)  NOT NULL,
    discount_amount     DECIMAL(12, 2)  DEFAULT 0.00,
    delivery_fee        DECIMAL(12, 2)  DEFAULT 0.00,
    service_fee         DECIMAL(12, 2)  DEFAULT 0.00,
    tax_amount          DECIMAL(12, 2)  DEFAULT 0.00,
    tip_amount          DECIMAL(12, 2)  DEFAULT 0.00,
    total_amount        DECIMAL(12, 2)  NOT NULL,
    currency_code       VARCHAR(3)      NOT NULL DEFAULT 'EUR',
    -- Delivery
    delivery_address    JSONB,
    delivery_notes      VARCHAR(500),
    estimated_delivery  TIMESTAMPTZ,
    actual_delivery     TIMESTAMPTZ,
    -- Payment
    payment_status      VARCHAR(20)     DEFAULT 'PENDING',
    payment_method      VARCHAR(50),
    payment_intent_id   VARCHAR(200),
    refund_amount       DECIMAL(12, 2)  DEFAULT 0.00,
    refund_reason       TEXT,
    -- Promotions
    promo_code          VARCHAR(50),
    promo_discount      DECIMAL(12, 2)  DEFAULT 0.00,
    -- Delivery Tracking
    tracking_url        VARCHAR(500),
    driver_id           UUID,
    driver_name         VARCHAR(100),
    driver_phone        VARCHAR(20),
    -- Status Timestamps
    confirmed_at        TIMESTAMPTZ,
    preparing_at        TIMESTAMPTZ,
    ready_at            TIMESTAMPTZ,
    picked_up_at        TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    cancellation_reason TEXT,
    -- Standard Timestamps
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_orders_type CHECK (order_type IN ('DELIVERY', 'PICKUP', 'DINE_IN', 'INGREDIENT_KIT', 'CATERING')),
    CONSTRAINT chk_orders_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT chk_orders_payment CHECK (payment_status IN ('PENDING', 'AUTHORIZED', 'PAID', 'FAILED', 'PARTIALLY_REFUNDED', 'REFUNDED'))
);

CREATE INDEX idx_orders_user_id        ON orders_schema.orders(user_id);
CREATE INDEX idx_orders_restaurant_id  ON orders_schema.orders(restaurant_id);
CREATE INDEX idx_orders_status         ON orders_schema.orders(status);
CREATE INDEX idx_orders_order_number   ON orders_schema.orders(order_number);
CREATE INDEX idx_orders_created_at     ON orders_schema.orders(created_at);
CREATE INDEX idx_orders_payment_intent ON orders_schema.orders(payment_intent_id);

-- ─────────────────────────────────────────────────────────────
-- Order Items
-- ─────────────────────────────────────────────────────────────
CREATE TABLE orders_schema.order_items (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID            NOT NULL REFERENCES orders_schema.orders(id) ON DELETE CASCADE,
    recipe_id               UUID            NOT NULL,
    recipe_title            VARCHAR(300)    NOT NULL,
    recipe_thumbnail_url    VARCHAR(500),
    quantity                INT             NOT NULL DEFAULT 1,
    unit_price              DECIMAL(10, 2)  NOT NULL,
    total_price             DECIMAL(10, 2)  NOT NULL,
    special_instructions    VARCHAR(500),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_items_order_id  ON orders_schema.order_items(order_id);
CREATE INDEX idx_order_items_recipe_id ON orders_schema.order_items(recipe_id);

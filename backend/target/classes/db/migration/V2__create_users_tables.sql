-- ─────────────────────────────────────────────────────────────
-- V2__create_users_tables.sql
-- User authentication, profiles, subscriptions, badges
-- ─────────────────────────────────────────────────────────────

-- ─────────────────────────────────────────────────────────────
-- Core Users Table
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users_schema.users (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    email                   VARCHAR(320)    NOT NULL UNIQUE,
    email_verified          BOOLEAN         DEFAULT FALSE,
    email_verified_at       TIMESTAMPTZ,
    phone                   VARCHAR(20)     UNIQUE,
    phone_verified          BOOLEAN         DEFAULT FALSE,
    password_hash           VARCHAR(255),
    salt                    VARCHAR(64),
    oauth_provider          VARCHAR(50),
    oauth_provider_id       VARCHAR(255),
    role                    VARCHAR(30)     NOT NULL DEFAULT 'USER',
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    last_login_at           TIMESTAMPTZ,
    last_login_ip           VARCHAR(45),
    failed_login_count      INT             DEFAULT 0,
    locked_until            TIMESTAMPTZ,
    mfa_enabled             BOOLEAN         DEFAULT FALSE,
    mfa_secret              VARCHAR(64),
    gdpr_consent            BOOLEAN         NOT NULL DEFAULT FALSE,
    gdpr_consent_date       TIMESTAMPTZ,
    marketing_consent       BOOLEAN         DEFAULT FALSE,
    data_deletion_requested_at TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at              TIMESTAMPTZ,

    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'CHEF', 'RESTAURANT_OWNER', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'PENDING', 'SUSPENDED', 'BANNED', 'PENDING_DELETION', 'DELETED'))
);

CREATE INDEX idx_users_email        ON users_schema.users(email);
CREATE INDEX idx_users_phone        ON users_schema.users(phone);
CREATE INDEX idx_users_oauth        ON users_schema.users(oauth_provider, oauth_provider_id);
CREATE INDEX idx_users_status       ON users_schema.users(status);
CREATE INDEX idx_users_created_at   ON users_schema.users(created_at);

-- ─────────────────────────────────────────────────────────────
-- User Profiles
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users_schema.user_profiles (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL UNIQUE REFERENCES users_schema.users(id) ON DELETE CASCADE,
    display_name        VARCHAR(100)    NOT NULL,
    bio                 VARCHAR(1000),
    avatar_url          VARCHAR(500),
    cover_image_url     VARCHAR(500),
    date_of_birth       DATE,
    gender              VARCHAR(10),
    country_id          UUID            REFERENCES geo_schema.countries(id),
    city                VARCHAR(100),
    timezone            VARCHAR(50)     DEFAULT 'UTC',
    preferred_language  VARCHAR(10)     DEFAULT 'en',
    preferred_currency  VARCHAR(3)      DEFAULT 'EUR',
    cooking_skill_level VARCHAR(20)     DEFAULT 'BEGINNER',
    dietary_preferences VARCHAR[],
    favorite_cuisines   VARCHAR[],
    allergens           VARCHAR[],
    spice_tolerance     INT             DEFAULT 3,
    follower_count      INT             DEFAULT 0,
    following_count     INT             DEFAULT 0,
    recipe_count        INT             DEFAULT 0,
    is_verified_chef    BOOLEAN         DEFAULT FALSE,
    verified_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_profiles_user_id ON users_schema.user_profiles(user_id);
CREATE INDEX idx_user_profiles_country ON users_schema.user_profiles(country_id);

-- ─────────────────────────────────────────────────────────────
-- Eco Badges
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users_schema.eco_badges (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(500),
    icon_url        VARCHAR(500),
    category        VARCHAR(30)     NOT NULL,
    criteria        TEXT,
    points          INT             DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE users_schema.user_badges (
    user_id         UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    badge_id        UUID            NOT NULL REFERENCES users_schema.eco_badges(id) ON DELETE CASCADE,
    earned_at       TIMESTAMPTZ     DEFAULT NOW(),
    PRIMARY KEY (user_id, badge_id)
);

-- ─────────────────────────────────────────────────────────────
-- Notifications
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users_schema.notifications (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    type            VARCHAR(50)     NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    body            TEXT,
    data            JSONB,
    is_read         BOOLEAN         DEFAULT FALSE,
    read_at         TIMESTAMPTZ,
    channel         VARCHAR(20)     DEFAULT 'IN_APP',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_id ON users_schema.notifications(user_id);
CREATE INDEX idx_notifications_read    ON users_schema.notifications(user_id, is_read);

-- ─────────────────────────────────────────────────────────────
-- Subscriptions
-- ─────────────────────────────────────────────────────────────
CREATE TABLE subscriptions_schema.subscriptions (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    plan                    VARCHAR(30)     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    billing_cycle           VARCHAR(10)     DEFAULT 'MONTHLY',
    price                   DECIMAL(10, 2),
    currency_code           VARCHAR(3)      DEFAULT 'EUR',
    stripe_subscription_id  VARCHAR(200),
    stripe_customer_id      VARCHAR(200),
    trial_ends_at           TIMESTAMPTZ,
    current_period_start    TIMESTAMPTZ,
    current_period_end      TIMESTAMPTZ,
    cancelled_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_user_id   ON subscriptions_schema.subscriptions(user_id);
CREATE INDEX idx_subscriptions_status    ON subscriptions_schema.subscriptions(status);
CREATE INDEX idx_subscriptions_stripe_id ON subscriptions_schema.subscriptions(stripe_subscription_id);

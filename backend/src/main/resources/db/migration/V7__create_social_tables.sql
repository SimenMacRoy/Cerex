-- ─────────────────────────────────────────────────────────────
-- V7__create_social_tables.sql
-- Social network: posts, comments, follows, likes, shares, notifications
-- ─────────────────────────────────────────────────────────────

-- ─────────────────────────────────────────────────────────
-- Posts
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS social_schema.posts (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id           UUID            NOT NULL REFERENCES users_schema.users(id),
    post_type           VARCHAR(30)     NOT NULL DEFAULT 'GENERAL',
    recipe_id           UUID,
    restaurant_id       UUID,
    title               VARCHAR(200),
    content             TEXT,
    video_url           VARCHAR(500),
    thumbnail_url       VARCHAR(500),
    like_count          INT             DEFAULT 0,
    comment_count       INT             DEFAULT 0,
    share_count         INT             DEFAULT 0,
    reproduce_count     INT             DEFAULT 0,
    view_count          INT             DEFAULT 0,
    save_count          INT             DEFAULT 0,
    boost_score         DOUBLE PRECISION DEFAULT 0.0,
    location_name       VARCHAR(200),
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    is_pinned           BOOLEAN         DEFAULT FALSE,
    is_featured         BOOLEAN         DEFAULT FALSE,
    report_count        INT             DEFAULT 0,
    original_post_id    UUID,
    reproduction_rating INT,
    reproduction_notes  TEXT,
    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_posts_author_id   ON social_schema.posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_post_type   ON social_schema.posts(post_type);
CREATE INDEX IF NOT EXISTS idx_posts_recipe_id   ON social_schema.posts(recipe_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at  ON social_schema.posts(created_at);
CREATE INDEX IF NOT EXISTS idx_posts_boost_score ON social_schema.posts(boost_score DESC);
CREATE INDEX IF NOT EXISTS idx_posts_status      ON social_schema.posts(status);

-- Post media
CREATE TABLE IF NOT EXISTS social_schema.post_media (
    post_id     UUID        NOT NULL REFERENCES social_schema.posts(id) ON DELETE CASCADE,
    media_url   VARCHAR(500) NOT NULL
);

-- Post hashtags
CREATE TABLE IF NOT EXISTS social_schema.post_hashtags (
    post_id     UUID        NOT NULL REFERENCES social_schema.posts(id) ON DELETE CASCADE,
    hashtag     VARCHAR(100) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_post_hashtags ON social_schema.post_hashtags(hashtag);

-- ─────────────────────────────────────────────────────────
-- Comments
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS social_schema.comments (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id             UUID            NOT NULL REFERENCES social_schema.posts(id) ON DELETE CASCADE,
    author_id           UUID            NOT NULL REFERENCES users_schema.users(id),
    content             TEXT            NOT NULL,
    parent_comment_id   UUID,
    like_count          INT             DEFAULT 0,
    reply_count         INT             DEFAULT 0,
    is_edited           BOOLEAN         DEFAULT FALSE,
    is_flagged          BOOLEAN         DEFAULT FALSE,
    created_at          TIMESTAMPTZ     DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_comments_post_id   ON social_schema.comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON social_schema.comments(author_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent_id ON social_schema.comments(parent_comment_id);

-- ─────────────────────────────────────────────────────────
-- Follows
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS social_schema.follows (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id     UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    followee_id     UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    is_muted        BOOLEAN         DEFAULT FALSE,
    is_close_friend BOOLEAN         DEFAULT FALSE,
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    UNIQUE (follower_id, followee_id),
    CHECK (follower_id != followee_id)
);

CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON social_schema.follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_followee_id ON social_schema.follows(followee_id);

-- ─────────────────────────────────────────────────────────
-- Likes (generic: post, comment, recipe, review)
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS social_schema.likes (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    entity_type     VARCHAR(20)     NOT NULL,
    entity_id       UUID            NOT NULL,
    reaction_type   VARCHAR(20)     DEFAULT 'LIKE',
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    UNIQUE (user_id, entity_type, entity_id)
);

CREATE INDEX IF NOT EXISTS idx_likes_user_id ON social_schema.likes(user_id);
CREATE INDEX IF NOT EXISTS idx_likes_entity  ON social_schema.likes(entity_type, entity_id);

-- ─────────────────────────────────────────────────────────
-- Shares
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS social_schema.shares (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    entity_type     VARCHAR(20)     NOT NULL,
    entity_id       UUID            NOT NULL,
    share_platform  VARCHAR(30)     DEFAULT 'INTERNAL',
    message         VARCHAR(500),
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shares_user_id    ON social_schema.shares(user_id);
CREATE INDEX IF NOT EXISTS idx_shares_entity     ON social_schema.shares(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_shares_created_at ON social_schema.shares(created_at);

-- ─────────────────────────────────────────────────────────
-- Notifications
-- ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS social_schema.notifications (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id        UUID            NOT NULL REFERENCES users_schema.users(id) ON DELETE CASCADE,
    sender_id           UUID            REFERENCES users_schema.users(id),
    notification_type   VARCHAR(30)     NOT NULL,
    title               VARCHAR(200),
    message             VARCHAR(500),
    entity_type         VARCHAR(30),
    entity_id           UUID,
    action_url          VARCHAR(500),
    image_url           VARCHAR(500),
    is_read             BOOLEAN         DEFAULT FALSE,
    read_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notif_recipient_id ON social_schema.notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notif_is_read      ON social_schema.notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notif_created_at   ON social_schema.notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notif_type         ON social_schema.notifications(notification_type);

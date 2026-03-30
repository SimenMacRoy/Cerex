-- ─────────────────────────────────────────────────────────────
-- V1__create_schemas_and_geo.sql
-- Creates all domain schemas + geography reference tables
-- ─────────────────────────────────────────────────────────────

-- Domain schemas
CREATE SCHEMA IF NOT EXISTS users_schema;
CREATE SCHEMA IF NOT EXISTS recipes_schema;
CREATE SCHEMA IF NOT EXISTS orders_schema;
CREATE SCHEMA IF NOT EXISTS geo_schema;
CREATE SCHEMA IF NOT EXISTS social_schema;
CREATE SCHEMA IF NOT EXISTS media_schema;
CREATE SCHEMA IF NOT EXISTS subscriptions_schema;

-- UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─────────────────────────────────────────────────────────────
-- Geography: Continents
-- ─────────────────────────────────────────────────────────────
CREATE TABLE geo_schema.continents (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(2)      NOT NULL UNIQUE,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    image_url       VARCHAR(500),
    recipe_count    INT             DEFAULT 0,
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
-- Geography: Countries
-- ─────────────────────────────────────────────────────────────
CREATE TABLE geo_schema.countries (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    continent_id    UUID            NOT NULL REFERENCES geo_schema.continents(id),
    iso_code        VARCHAR(3)      NOT NULL UNIQUE,
    name            VARCHAR(100)    NOT NULL,
    name_local      VARCHAR(100),
    flag_emoji      VARCHAR(10),
    capital         VARCHAR(100),
    currency_code   VARCHAR(3),
    phone_prefix    VARCHAR(10),
    primary_language VARCHAR(10),
    recipe_count    INT             DEFAULT 0,
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
-- Geography: Cultures (sub-national culinary groups)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE geo_schema.cultures (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id      UUID            NOT NULL REFERENCES geo_schema.countries(id),
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    culinary_highlights TEXT,
    image_url       VARCHAR(500),
    recipe_count    INT             DEFAULT 0,
    created_at      TIMESTAMPTZ     DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     DEFAULT NOW()
);

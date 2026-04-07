-- V8__add_first_last_name_to_profiles.sql
-- Add first_name and last_name columns to user_profiles

ALTER TABLE users_schema.user_profiles
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS last_name  VARCHAR(100);

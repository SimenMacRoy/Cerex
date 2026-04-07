-- ─────────────────────────────────────────────────────────────
-- V9__seed_sample_data.sql
-- Sample users, profiles and published recipes for development
-- Password for all users: Test1234  (BCrypt hash below)
-- ─────────────────────────────────────────────────────────────

-- ── Demo Users ────────────────────────────────────────────────
INSERT INTO users_schema.users (id, email, password_hash, role, status, email_verified, gdpr_consent, gdpr_consent_date, created_at, updated_at)
VALUES
  ('a0000000-0000-0000-0000-000000000001', 'chef.marie@cerex.com',
   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeIpJ7VgQYPhxEMyu',
   'CHEF', 'ACTIVE', true, true, NOW(), NOW(), NOW()),

  ('a0000000-0000-0000-0000-000000000002', 'chef.kofi@cerex.com',
   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeIpJ7VgQYPhxEMyu',
   'CHEF', 'ACTIVE', true, true, NOW(), NOW(), NOW()),

  ('a0000000-0000-0000-0000-000000000003', 'admin@cerex.com',
   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeIpJ7VgQYPhxEMyu',
   'ADMIN', 'ACTIVE', true, true, NOW(), NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- ── User Profiles ─────────────────────────────────────────────
INSERT INTO users_schema.user_profiles (id, user_id, display_name, first_name, last_name, bio, preferred_language, preferred_currency, cooking_skill_level, follower_count, following_count, recipe_count, created_at, updated_at)
VALUES
  ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001',
   'Chef Marie', 'Marie', 'Dupont',
   'French chef passionate about West African fusion cuisine. 15 years of culinary experience.',
   'fr', 'EUR', 'EXPERT', 1200, 340, 47, NOW(), NOW()),

  ('b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002',
   'Chef Kofi', 'Kofi', 'Mensah',
   'Ghanaian chef sharing the flavors of West Africa with the world.',
   'en', 'USD', 'EXPERT', 890, 210, 32, NOW(), NOW()),

  ('b0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000003',
   'Admin Cerex', 'Admin', 'Cerex',
   'Platform administrator.',
   'en', 'EUR', 'INTERMEDIATE', 0, 0, 0, NOW(), NOW())
ON CONFLICT (user_id) DO NOTHING;

-- ── Sample Categories ─────────────────────────────────────────
INSERT INTO recipes_schema.categories (id, name, slug, description, display_order)
VALUES
  ('c0000000-0000-0000-0000-000000000001', 'Main Dishes',   'main-dishes',   'Hearty main course recipes', 1),
  ('c0000000-0000-0000-0000-000000000002', 'Soups & Stews', 'soups-stews',   'Warming soups and stews',    2),
  ('c0000000-0000-0000-0000-000000000003', 'Desserts',      'desserts',       'Sweet treats and desserts',  3),
  ('c0000000-0000-0000-0000-000000000004', 'Street Food',   'street-food',   'Quick bites and street food', 4),
  ('c0000000-0000-0000-0000-000000000005', 'Beverages',     'beverages',      'Drinks and smoothies',       5)
ON CONFLICT (slug) DO NOTHING;

-- ── Published Recipes ─────────────────────────────────────────
INSERT INTO recipes_schema.recipes (
  id, author_id, category_id,
  title, slug, description,
  recipe_type, cuisine_type, course_type, difficulty_level,
  spice_level, prep_time_minutes, cook_time_minutes, servings, servings_unit,
  is_vegetarian, is_vegan, is_gluten_free, is_halal,
  avg_rating, rating_count, view_count, like_count, save_count,
  status, moderation_status,
  published_at, created_at, updated_at
) VALUES

-- 1. Jollof Rice
('d0000000-0000-0000-0000-000000000001',
 'a0000000-0000-0000-0000-000000000002',
 'c0000000-0000-0000-0000-000000000001',
 'Ghanaian Jollof Rice', 'ghanaian-jollof-rice',
 'The ultimate West African Jollof Rice — smoky, rich, and deeply flavorful. A staple at every celebration.',
 'DISH', 'AFRICAN', 'MAIN', 'MEDIUM',
 3, 20, 45, 6, 'persons',
 true, true, true, true,
 4.8, 312, 8420, 654, 203,
 'PUBLISHED', 'APPROVED',
 NOW(), NOW(), NOW()),

-- 2. Boeuf Bourguignon
('d0000000-0000-0000-0000-000000000002',
 'a0000000-0000-0000-0000-000000000001',
 'c0000000-0000-0000-0000-000000000001',
 'Boeuf Bourguignon', 'boeuf-bourguignon',
 'Classic French beef stew slow-cooked in Burgundy wine with mushrooms, pearl onions and lardons.',
 'DISH', 'French', 'MAIN', 'HARD',
 1, 30, 180, 4, 'persons',
 false, false, true, false,
 4.9, 287, 12300, 891, 445,
 'PUBLISHED', 'APPROVED',
 NOW(), NOW(), NOW()),

-- 3. Thiéboudienne
('d0000000-0000-0000-0000-000000000003',
 'a0000000-0000-0000-0000-000000000002',
 'c0000000-0000-0000-0000-000000000001',
 'Thiéboudienne (Senegalo Fish Rice)', 'thieboudienne',
 'Senegal''s national dish — fish and vegetables slow-cooked in a rich tomato sauce over broken rice.',
 'DISH', 'AFRICAN', 'MAIN', 'MEDIUM',
 2, 40, 90, 8, 'persons',
 false, false, true, true,
 4.7, 198, 6700, 432, 167,
 'PUBLISHED', 'APPROVED',
 NOW(), NOW(), NOW()),

-- 4. Croissants
('d0000000-0000-0000-0000-000000000004',
 'a0000000-0000-0000-0000-000000000001',
 'c0000000-0000-0000-0000-000000000001',
 'Croissants au Beurre', 'croissants-au-beurre',
 'Authentic French butter croissants with 27 flaky laminated layers. A weekend baking project worth every minute.',
 'BREAD', 'French', 'BREAKFAST', 'EXPERT',
 1, 60, 25, 12, 'croissants',
 true, false, false, false,
 4.9, 521, 18900, 1243, 678,
 'PUBLISHED', 'APPROVED',
 NOW(), NOW(), NOW()),

-- 5. Ndolé
('d0000000-0000-0000-0000-000000000005',
 'a0000000-0000-0000-0000-000000000002',
 'c0000000-0000-0000-0000-000000000001',
 'Ndolé Camerounais', 'ndole-camerounais',
 'Cameroon''s national dish of bitter leaves stewed with groundnuts, served with plantains and fish or beef.',
 'DISH', 'AFRICAN', 'MAIN', 'MEDIUM',
 2, 45, 60, 6, 'persons',
 false, false, true, true,
 4.6, 156, 4300, 289, 98,
 'PUBLISHED', 'APPROVED',
 NOW(), NOW(), NOW()),

-- 6. Ratatouille
('d0000000-0000-0000-0000-000000000006',
 'a0000000-0000-0000-0000-000000000001',
 'c0000000-0000-0000-0000-000000000001',
 'Ratatouille Provençale', 'ratatouille-provencale',
 'A vibrant Provençal vegetable stew with zucchini, eggplant, tomatoes and fresh herbs. Simple and stunning.',
 'DISH', 'French', 'MAIN', 'EASY',
 1, 20, 40, 4, 'persons',
 true, true, true, true,
 4.5, 243, 9800, 567, 312,
 'PUBLISHED', 'APPROVED',
 NOW(), NOW(), NOW()),

-- 7. Suya
('d0000000-0000-0000-0000-000000000007',
 'a0000000-0000-0000-0000-000000000002',
 'c0000000-0000-0000-0000-000000000004',
 'Suya (Nigerian Spiced Beef Skewers)', 'suya-nigerian-beef-skewers',
 'West Africa''s most iconic street food — thin beef strips marinated in a fiery peanut spice blend, grilled over charcoal.',
 'STREET_FOOD', 'AFRICAN', 'SNACK', 'EASY',
 4, 20, 15, 4, 'skewers',
 false, false, true, true,
 4.8, 389, 11200, 823, 401,
 'PUBLISHED', 'APPROVED',
 NOW(), NOW(), NOW()),

-- 8. Coq au Vin
('d0000000-0000-0000-0000-000000000008',
 'a0000000-0000-0000-0000-000000000001',
 'c0000000-0000-0000-0000-000000000001',
 'Coq au Vin', 'coq-au-vin',
 'Tender chicken braised in red wine with mushrooms and pearl onions. A timeless French bistro classic.',
 'DISH', 'French', 'MAIN', 'MEDIUM',
 1, 25, 90, 4, 'persons',
 false, false, true, false,
 4.7, 178, 7600, 445, 234,
 'PUBLISHED', 'APPROVED',
 NOW(), NOW(), NOW())

ON CONFLICT (slug) DO NOTHING;

-- ─────────────────────────────────────────────────────────────
-- V5__seed_reference_data.sql
-- Seeds initial countries, categories, and sample ingredients
-- ─────────────────────────────────────────────────────────────

-- ── Countries (sample — 20 major culinary nations) ──────────
INSERT INTO geo_schema.countries (continent_id, iso_code, name, flag_emoji, primary_language, currency_code) VALUES
  -- Africa
  ((SELECT id FROM geo_schema.continents WHERE code = 'AF'), 'SN', 'Senegal',       '🇸🇳', 'fr', 'XOF'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'AF'), 'NG', 'Nigeria',       '🇳🇬', 'en', 'NGN'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'AF'), 'MA', 'Morocco',       '🇲🇦', 'ar', 'MAD'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'AF'), 'ET', 'Ethiopia',      '🇪🇹', 'am', 'ETB'),
  -- Asia
  ((SELECT id FROM geo_schema.continents WHERE code = 'AS'), 'JP', 'Japan',         '🇯🇵', 'ja', 'JPY'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'AS'), 'IN', 'India',         '🇮🇳', 'hi', 'INR'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'AS'), 'TH', 'Thailand',      '🇹🇭', 'th', 'THB'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'AS'), 'CN', 'China',         '🇨🇳', 'zh', 'CNY'),
  -- Europe
  ((SELECT id FROM geo_schema.continents WHERE code = 'EU'), 'FR', 'France',        '🇫🇷', 'fr', 'EUR'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'EU'), 'IT', 'Italy',         '🇮🇹', 'it', 'EUR'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'EU'), 'ES', 'Spain',         '🇪🇸', 'es', 'EUR'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'EU'), 'GB', 'United Kingdom','🇬🇧', 'en', 'GBP'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'EU'), 'DE', 'Germany',       '🇩🇪', 'de', 'EUR'),
  -- North America
  ((SELECT id FROM geo_schema.continents WHERE code = 'NA'), 'US', 'United States', '🇺🇸', 'en', 'USD'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'NA'), 'MX', 'Mexico',        '🇲🇽', 'es', 'MXN'),
  -- South America
  ((SELECT id FROM geo_schema.continents WHERE code = 'SA'), 'BR', 'Brazil',        '🇧🇷', 'pt', 'BRL'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'SA'), 'PE', 'Peru',          '🇵🇪', 'es', 'PEN'),
  ((SELECT id FROM geo_schema.continents WHERE code = 'SA'), 'AR', 'Argentina',     '🇦🇷', 'es', 'ARS'),
  -- Oceania
  ((SELECT id FROM geo_schema.continents WHERE code = 'OC'), 'AU', 'Australia',     '🇦🇺', 'en', 'AUD');

-- ── Recipe Categories ───────────────────────────────────────
INSERT INTO recipes_schema.categories (name, slug, description, display_order) VALUES
  ('Main Course',  'main-course',  'Hearty main dishes from around the world',  1),
  ('Appetizer',    'appetizer',    'Starters and small bites',                   2),
  ('Dessert',      'dessert',      'Sweet treats and confections',               3),
  ('Soup & Stew',  'soup-stew',    'Warming soups and stews',                    4),
  ('Salad',        'salad',        'Fresh salads and cold dishes',               5),
  ('Bread & Pastry','bread-pastry','Baked goods and pastries',                   6),
  ('Beverage',     'beverage',     'Hot and cold drinks',                         7),
  ('Sauce & Condiment','sauce-condiment','Sauces, dips, and condiments',         8),
  ('Snack',        'snack',        'Quick bites and street food',                 9),
  ('Breakfast',    'breakfast',    'Morning meals and brunch dishes',             10),
  ('Side Dish',    'side-dish',    'Accompaniments and sides',                    11);

-- ── Sample Ingredients ──────────────────────────────────────
INSERT INTO recipes_schema.ingredients (name, name_fr, category) VALUES
  ('Chicken',       'Poulet',       'Protein'),
  ('Rice',          'Riz',          'Grain'),
  ('Onion',         'Oignon',       'Vegetable'),
  ('Garlic',        'Ail',          'Vegetable'),
  ('Tomato',        'Tomate',       'Vegetable'),
  ('Olive Oil',     'Huile d''olive','Oil'),
  ('Salt',          'Sel',          'Seasoning'),
  ('Black Pepper',  'Poivre noir',  'Seasoning'),
  ('Lemon',         'Citron',       'Fruit'),
  ('Ginger',        'Gingembre',    'Spice'),
  ('Cumin',         'Cumin',        'Spice'),
  ('Paprika',       'Paprika',      'Spice'),
  ('Chili Pepper',  'Piment',       'Spice'),
  ('Butter',        'Beurre',       'Dairy'),
  ('Flour',         'Farine',       'Grain'),
  ('Egg',           'Œuf',          'Protein'),
  ('Sugar',         'Sucre',        'Sweetener'),
  ('Coconut Milk',  'Lait de coco', 'Dairy Alternative'),
  ('Soy Sauce',     'Sauce soja',   'Condiment'),
  ('Fish Sauce',    'Sauce poisson','Condiment');

-- ── Sample Eco Badges ───────────────────────────────────────
INSERT INTO users_schema.eco_badges (name, description, category, criteria, points) VALUES
  ('First Recipe',     'Created your first recipe on Cerex',           'MILESTONE', 'Create 1 recipe',          10),
  ('Home Cook',        'Published 10 recipes',                         'MILESTONE', 'Publish 10 recipes',       50),
  ('Globe Trotter',    'Cooked recipes from 5 different continents',   'CULTURAL',  'Cook from 5 continents',   100),
  ('Zero Waste Hero',  'Used the waste optimizer 10 times',            'ECO',       'Use waste optimizer 10x',  75),
  ('Community Builder', 'Gained 100 followers',                        'SOCIAL',    'Reach 100 followers',      100),
  ('Vegan Explorer',   'Cooked 20 vegan recipes',                     'ECO',       'Cook 20 vegan recipes',    60),
  ('Chef''s Kiss',     'Achieved an average rating of 4.5+',          'MILESTONE', 'Average rating >= 4.5',    150);

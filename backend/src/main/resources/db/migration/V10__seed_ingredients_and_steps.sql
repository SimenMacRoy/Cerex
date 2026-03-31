-- ─────────────────────────────────────────────────────────────
-- V10__seed_ingredients_and_steps.sql
-- Master ingredients + recipe_ingredients + recipe_steps
-- for the 8 seeded recipes (V9)
-- ─────────────────────────────────────────────────────────────

-- ── Master Ingredients ─────────────────────────────────────────
INSERT INTO recipes_schema.ingredients (id, name, category, created_at, updated_at) VALUES
  -- Pantry / Grains
  ('e1000001-0000-0000-0000-000000000001', 'Long-grain white rice',    'Grains',     NOW(), NOW()),
  ('e1000001-0000-0000-0000-000000000002', 'Broken rice (thiébou)',    'Grains',     NOW(), NOW()),
  ('e1000001-0000-0000-0000-000000000003', 'All-purpose flour',        'Grains',     NOW(), NOW()),
  ('e1000001-0000-0000-0000-000000000004', 'Bread flour',              'Grains',     NOW(), NOW()),

  -- Proteins
  ('e1000002-0000-0000-0000-000000000001', 'Beef chuck (stewing)',     'Meat',       NOW(), NOW()),
  ('e1000002-0000-0000-0000-000000000002', 'Whole chicken',            'Meat',       NOW(), NOW()),
  ('e1000002-0000-0000-0000-000000000003', 'Beef sirloin (thin)',      'Meat',       NOW(), NOW()),
  ('e1000002-0000-0000-0000-000000000004', 'Smoked beef / beef jerky', 'Meat',       NOW(), NOW()),
  ('e1000002-0000-0000-0000-000000000005', 'Lardons / pancetta',       'Meat',       NOW(), NOW()),
  ('e1000002-0000-0000-0000-000000000006', 'Whole white fish (thiof)', 'Seafood',    NOW(), NOW()),
  ('e1000002-0000-0000-0000-000000000007', 'Smoked fish',              'Seafood',    NOW(), NOW()),
  ('e1000002-0000-0000-0000-000000000008', 'Dried shrimp',             'Seafood',    NOW(), NOW()),

  -- Dairy
  ('e1000003-0000-0000-0000-000000000001', 'Unsalted butter',          'Dairy',      NOW(), NOW()),
  ('e1000003-0000-0000-0000-000000000002', 'Milk (whole)',             'Dairy',      NOW(), NOW()),

  -- Vegetables
  ('e1000004-0000-0000-0000-000000000001', 'Yellow onion',             'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000002', 'Pearl onions',             'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000003', 'Garlic',                   'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000004', 'Tomatoes (fresh)',         'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000005', 'Tomato paste',             'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000006', 'Scotch bonnet pepper',     'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000007', 'Bell pepper (red)',        'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000008', 'Carrots',                  'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000009', 'Mushrooms (cremini)',      'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000010', 'Zucchini',                 'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000011', 'Eggplant (aubergine)',     'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000012', 'Cassava / yam',            'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000013', 'Bitter leaf (ndolé)',      'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000014', 'Cabbage',                  'Vegetables', NOW(), NOW()),
  ('e1000004-0000-0000-0000-000000000015', 'Plantain',                 'Vegetables', NOW(), NOW()),

  -- Pantry / Sauces
  ('e1000005-0000-0000-0000-000000000001', 'Burgundy red wine',        'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000002', 'Chicken broth',            'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000003', 'Beef broth',               'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000004', 'Vegetable broth',          'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000005', 'Tomato puree / passata',   'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000006', 'Peanuts (groundnuts)',      'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000007', 'Peanut butter (smooth)',   'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000008', 'Olive oil',                'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000009', 'Vegetable oil',            'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000010', 'Bay leaves',               'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000011', 'Fresh thyme',              'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000012', 'Fresh rosemary',           'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000013', 'Salt',                     'Pantry',     NOW(), NOW()),
  ('e1000005-0000-0000-0000-000000000014', 'Black pepper',             'Pantry',     NOW(), NOW()),

  -- Spices (suya)
  ('e1000006-0000-0000-0000-000000000001', 'Yaji / suya spice blend',  'Spices',     NOW(), NOW()),
  ('e1000006-0000-0000-0000-000000000002', 'Smoked paprika',           'Spices',     NOW(), NOW()),
  ('e1000006-0000-0000-0000-000000000003', 'Cayenne pepper',           'Spices',     NOW(), NOW()),
  ('e1000006-0000-0000-0000-000000000004', 'Ginger powder',            'Spices',     NOW(), NOW()),
  ('e1000006-0000-0000-0000-000000000005', 'Cumin',                    'Spices',     NOW(), NOW()),
  ('e1000006-0000-0000-0000-000000000006', 'Coriander powder',         'Spices',     NOW(), NOW()),
  ('e1000006-0000-0000-0000-000000000007', 'Herbs de Provence',        'Spices',     NOW(), NOW()),

  -- Croissant
  ('e1000007-0000-0000-0000-000000000001', 'Active dry yeast',         'Baking',     NOW(), NOW()),
  ('e1000007-0000-0000-0000-000000000002', 'Sugar',                    'Baking',     NOW(), NOW()),
  ('e1000007-0000-0000-0000-000000000003', 'Egg (large)',               'Dairy',      NOW(), NOW())

ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- 1. Ghanaian Jollof Rice  (d0000000-0000-0000-0000-000000000001)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO recipes_schema.recipe_ingredients
  (id, recipe_id, ingredient_id, quantity, unit, display_text, is_optional, display_order)
VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000001-0000-0000-0000-000000000001', 3,    'cups',  '3 cups long-grain rice, rinsed',             false, 1),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000004-0000-0000-0000-000000000004', 4,    'large', '4 large ripe tomatoes, chopped',              false, 2),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000004-0000-0000-0000-000000000007', 2,    'pcs',   '2 red bell peppers',                          false, 3),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000004-0000-0000-0000-000000000006', 1,    'pcs',   '1 scotch bonnet pepper (adjust to taste)',    false, 4),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000004-0000-0000-0000-000000000001', 2,    'large', '2 large onions',                              false, 5),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000004-0000-0000-0000-000000000003', 4,    'cloves','4 cloves garlic, minced',                     false, 6),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000005-0000-0000-0000-000000000005', 3,    'tbsp',  '3 tbsp tomato paste',                         false, 7),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000005-0000-0000-0000-000000000009', 4,    'tbsp',  '4 tbsp vegetable oil',                        false, 8),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000005-0000-0000-0000-000000000002', 2,    'cups',  '2 cups chicken or vegetable broth',           false, 9),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000005-0000-0000-0000-000000000011', 2,    'sprigs','2 sprigs fresh thyme',                        false, 10),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000005-0000-0000-0000-000000000010', 2,    'pcs',   '2 bay leaves',                                false, 11),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000005-0000-0000-0000-000000000013', 1.5,  'tsp',   '1½ tsp salt, or to taste',                    false, 12),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 'e1000006-0000-0000-0000-000000000002', 1,    'tsp',   '1 tsp smoked paprika',                        false, 13)
ON CONFLICT DO NOTHING;

INSERT INTO recipes_schema.recipe_steps (id, recipe_id, step_number, instruction, duration_minutes, tip) VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 1,
   'Blend the tomatoes, red bell peppers, scotch bonnet and one onion together until smooth. Set aside.',
   5, 'Use ripe tomatoes for a deeper, sweeter sauce.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 2,
   'Heat oil in a heavy-bottomed pot over medium-high heat. Fry the second onion (sliced) until golden, about 5 minutes. Add the tomato paste and fry for 2 minutes, stirring constantly.',
   7, 'Frying the tomato paste removes its raw taste and deepens the colour.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 3,
   'Pour in the blended tomato mixture. Stir, then cook uncovered on medium heat for 20–25 minutes, stirring occasionally, until the sauce has reduced and the oil floats on top.',
   25, 'The oil rising to the top is the key sign the sauce is ready — this is called "frying the stew".'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 4,
   'Add the broth, thyme, bay leaves, smoked paprika, garlic and salt. Bring to a boil. Add the rinsed rice, stir once and reduce heat to low. Cover tightly.',
   5, 'Add a piece of foil under the lid to trap more steam — this is the secret to fluffy Jollof.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 5,
   'Cook on very low heat for 30–35 minutes without lifting the lid. Check after 30 minutes — the rice should be cooked through and slightly smoky at the bottom.',
   35, 'The famous "party Jollof" burnt bottom (socarrat) is a delicacy — aim for it!'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000001', 6,
   'Remove from heat and let rest, covered, for 5 minutes. Fluff with a fork, remove bay leaves and thyme sprigs, and serve hot.',
   5, NULL)
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- 2. Boeuf Bourguignon  (d0000000-0000-0000-0000-000000000002)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO recipes_schema.recipe_ingredients
  (id, recipe_id, ingredient_id, quantity, unit, display_text, is_optional, display_order)
VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000002-0000-0000-0000-000000000001', 1.2,  'kg',   '1.2 kg beef chuck, cut into 4 cm cubes',      false, 1),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000005-0000-0000-0000-000000000001', 750,  'ml',   '750 ml Burgundy (Pinot Noir)',                 false, 2),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000005-0000-0000-0000-000000000003', 300,  'ml',   '300 ml beef broth',                           false, 3),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000002-0000-0000-0000-000000000005', 150,  'g',    '150 g lardons (smoked bacon cubes)',           false, 4),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000004-0000-0000-0000-000000000009', 250,  'g',    '250 g cremini mushrooms, quartered',           false, 5),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000004-0000-0000-0000-000000000002', 200,  'g',    '200 g pearl onions, peeled',                  false, 6),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000004-0000-0000-0000-000000000008', 2,    'large','2 large carrots, cut into rounds',             false, 7),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000004-0000-0000-0000-000000000001', 1,    'large','1 large onion, roughly chopped',               false, 8),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000004-0000-0000-0000-000000000003', 4,    'cloves','4 cloves garlic, crushed',                   false, 9),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000004-0000-0000-0000-000000000005', 2,    'tbsp', '2 tbsp tomato paste',                         false, 10),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000003-0000-0000-0000-000000000001', 30,   'g',    '30 g butter',                                 false, 11),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000005-0000-0000-0000-000000000008', 2,    'tbsp', '2 tbsp olive oil',                            false, 12),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000005-0000-0000-0000-000000000011', 4,    'sprigs','4 sprigs fresh thyme',                       false, 13),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000005-0000-0000-0000-000000000010', 2,    'pcs',  '2 bay leaves',                                false, 14),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000005-0000-0000-0000-000000000013', 1,    'tsp',  'Salt to taste',                               false, 15),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 'e1000005-0000-0000-0000-000000000014', 0.5,  'tsp',  'Freshly ground black pepper',                 false, 16)
ON CONFLICT DO NOTHING;

INSERT INTO recipes_schema.recipe_steps (id, recipe_id, step_number, instruction, duration_minutes, tip) VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 1,
   'Marinate the beef: place in a bowl with the wine, thyme, bay leaves and garlic. Cover and refrigerate for at least 4 hours (overnight is ideal). Drain, reserving the marinade. Pat the beef dry with paper towels.',
   10, 'Drying the beef is critical — wet beef steams instead of browning.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 2,
   'Preheat oven to 160 °C (325 °F). In a large Dutch oven, sauté the lardons over medium heat until golden. Remove with a slotted spoon. Brown the beef in batches in the lardon fat until deep mahogany on all sides, 8–10 minutes per batch.',
   25, 'Do not crowd the pot — work in batches for a proper sear.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 3,
   'In the same pot, sauté the chopped onion and carrots until softened, 5 minutes. Add tomato paste and cook 1 minute. Pour in the reserved marinade and bring to a boil, scraping up any browned bits.',
   8, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 4,
   'Return the beef and lardons to the pot. Add the broth — liquid should just cover the meat. Bring to a simmer, cover and transfer to the oven. Braise for 2.5 – 3 hours until the beef is fork-tender.',
   180, 'Check every 45 min — if the liquid reduces below half, add a splash of broth.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 5,
   'Meanwhile, sauté the pearl onions in butter until golden. In another pan, sauté mushrooms in butter until browned. Add both to the pot in the last 30 minutes of braising.',
   15, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000002', 6,
   'Remove the bay leaves and thyme. Taste and adjust seasoning. Serve over egg noodles, mashed potatoes or crusty bread.',
   5, 'Even better the next day — the flavours deepen overnight.')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- 3. Thiéboudienne  (d0000000-0000-0000-0000-000000000003)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO recipes_schema.recipe_ingredients
  (id, recipe_id, ingredient_id, quantity, unit, display_text, is_optional, display_order)
VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000001-0000-0000-0000-000000000002', 600,  'g',    '600 g broken rice, rinsed',                   false, 1),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000002-0000-0000-0000-000000000006', 1.5,  'kg',   '1.5 kg whole white fish (thiof or grouper), cleaned and scored', false, 2),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000002-0000-0000-0000-000000000007', 100,  'g',    '100 g smoked fish (for depth)',                true, 3),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000004-0000-0000-0000-000000000004', 4,    'large','4 large ripe tomatoes',                        false, 4),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000004-0000-0000-0000-000000000005', 3,    'tbsp', '3 tbsp tomato paste',                         false, 5),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000004-0000-0000-0000-000000000001', 2,    'large','2 large onions',                              false, 6),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000004-0000-0000-0000-000000000003', 5,    'cloves','5 cloves garlic',                            false, 7),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000004-0000-0000-0000-000000000008', 2,    'pcs',  '2 carrots, halved lengthwise',                false, 8),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000004-0000-0000-0000-000000000012', 200,  'g',    '200 g cassava, cut into chunks',              false, 9),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000004-0000-0000-0000-000000000014', 0.25, 'head', 'Quarter head of cabbage',                     false, 10),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000002-0000-0000-0000-000000000008', 50,   'g',    '50 g dried shrimp',                           false, 11),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000005-0000-0000-0000-000000000009', 5,    'tbsp', '5 tbsp vegetable oil',                        false, 12),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 'e1000005-0000-0000-0000-000000000013', 1,    'tbsp', 'Salt to taste',                               false, 13)
ON CONFLICT DO NOTHING;

INSERT INTO recipes_schema.recipe_steps (id, recipe_id, step_number, instruction, duration_minutes, tip) VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 1,
   'Season the fish inside and out with a paste of garlic, salt and a little oil. Let marinate for 15 minutes. Heat oil in a large pot and fry the fish on both sides until golden, about 4 minutes per side. Remove and set aside.',
   25, 'Do not skip frying the fish first — it adds layers of flavour to the broth.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 2,
   'In the same pot, fry the sliced onion until translucent. Blend the tomatoes and add to the pot with the tomato paste. Fry this tomato sauce for 15–20 minutes, stirring often, until deep red and the oil separates.',
   20, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 3,
   'Add 1.5 litres of water, the dried shrimp, smoked fish, carrots, cassava and cabbage. Bring to a boil, then simmer for 20 minutes. Taste and adjust salt. Remove the vegetables and set aside.',
   25, 'Keep the vegetables firm — they will cook further with the rice.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 4,
   'Add the rinsed broken rice directly to the tomato broth. Stir, reduce heat to low, cover and cook for 25–30 minutes until the rice has absorbed all the broth. Allow a slight crust to form on the bottom.',
   30, 'The "xoon" (bottom crust) is the most prized part — scrape it out and serve it separately.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000003', 5,
   'Return the fried fish to the pot in the last 5 minutes to warm through. Serve the rice on a large platter topped with the fish, vegetables and any remaining sauce.',
   10, NULL)
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- 4. Croissants au Beurre  (d0000000-0000-0000-0000-000000000004)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO recipes_schema.recipe_ingredients
  (id, recipe_id, ingredient_id, quantity, unit, display_text, is_optional, display_order)
VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 'e1000001-0000-0000-0000-000000000004', 500,  'g',    '500 g bread flour (T45 or T55)',               false, 1),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 'e1000007-0000-0000-0000-000000000001', 7,    'g',    '7 g active dry yeast (1 sachet)',              false, 2),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 'e1000007-0000-0000-0000-000000000002', 60,   'g',    '60 g sugar',                                  false, 3),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 'e1000005-0000-0000-0000-000000000013', 10,   'g',    '10 g fine salt',                              false, 4),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 'e1000003-0000-0000-0000-000000000002', 300,  'ml',   '300 ml whole milk, lukewarm',                 false, 5),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 'e1000003-0000-0000-0000-000000000001', 280,  'g',    '280 g cold unsalted butter (for lamination)',  false, 6),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 'e1000007-0000-0000-0000-000000000003', 1,    'large','1 large egg + 1 tbsp milk (for egg wash)',     false, 7)
ON CONFLICT DO NOTHING;

INSERT INTO recipes_schema.recipe_steps (id, recipe_id, step_number, instruction, duration_minutes, tip) VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 1,
   'Make the détrempe (base dough): dissolve yeast in lukewarm milk. Mix flour, sugar and salt in a stand mixer. Add the milk mixture and knead on medium speed for 5 minutes until smooth. Shape into a rectangle, wrap in cling film and refrigerate overnight (or at least 1 hour).',
   15, 'Cold dough is easier to laminate — do not rush this step.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 2,
   'Prepare the beurrage (butter block): pound the cold butter between two sheets of baking paper into a flat 15×15 cm square. Refrigerate until firm but pliable (it should bend without cracking).',
   10, 'The butter and dough should have the same consistency — this is key for even layers.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 3,
   'Roll the dough on a floured surface into a 25×25 cm square. Place the butter block in the centre at 45° (like a diamond). Fold the four dough flaps over the butter to enclose it completely. Seal the edges.',
   10, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 4,
   'First turn: roll the dough into a 20×60 cm rectangle. Fold it in thirds like a letter (a "single turn"). Wrap and refrigerate for 30 minutes. Repeat this process 2 more times (3 single turns total), resting 30 minutes between each.',
   120, 'Always roll away from you and keep the seam to one side — consistency builds layers.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 5,
   'After the final rest, roll the dough to a 5 mm thickness. Cut into long triangles (base 10 cm). Roll each triangle from the base to the tip, gently stretching as you roll. Place on baking sheets lined with parchment, curl the ends inward to form the crescent shape.',
   20, 'Do not press too hard — you want to preserve the air pockets.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 6,
   'Proof the croissants at room temperature (22–25 °C) for 2–3 hours until they look puffy and jiggle when you shake the tray. Brush gently with egg wash.',
   180, 'Under-proofed croissants will not open up in the oven; over-proofed ones will leak butter.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000004', 7,
   'Bake at 200 °C (fan 180 °C) for 16–18 minutes until deep golden brown. Transfer to a wire rack and let cool for at least 15 minutes before eating.',
   20, 'Resist cutting them immediately — the layers need to set as they cool.')
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- 5. Ndolé Camerounais  (d0000000-0000-0000-0000-000000000005)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO recipes_schema.recipe_ingredients
  (id, recipe_id, ingredient_id, quantity, unit, display_text, is_optional, display_order)
VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000004-0000-0000-0000-000000000013', 400,  'g',    '400 g fresh or frozen bitter leaf (ndolé), blanched and squeezed', false, 1),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000005-0000-0000-0000-000000000006', 200,  'g',    '200 g raw peanuts (groundnuts)',               false, 2),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000002-0000-0000-0000-000000000004', 300,  'g',    '300 g smoked beef or beef jerky',             false, 3),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000002-0000-0000-0000-000000000007', 150,  'g',    '150 g smoked fish, flaked',                   false, 4),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000002-0000-0000-0000-000000000008', 50,   'g',    '50 g dried shrimp',                           false, 5),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000004-0000-0000-0000-000000000001', 2,    'large','2 large onions',                              false, 6),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000004-0000-0000-0000-000000000003', 6,    'cloves','6 cloves garlic',                            false, 7),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000004-0000-0000-0000-000000000006', 1,    'pcs',  '1 scotch bonnet or 1 tsp cayenne',            false, 8),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000004-0000-0000-0000-000000000015', 2,    'pcs',  '2 ripe plantains, sliced and fried',          true, 9),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000005-0000-0000-0000-000000000009', 4,    'tbsp', '4 tbsp vegetable oil',                        false, 10),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 'e1000005-0000-0000-0000-000000000013', 1,    'tsp',  'Salt to taste',                               false, 11)
ON CONFLICT DO NOTHING;

INSERT INTO recipes_schema.recipe_steps (id, recipe_id, step_number, instruction, duration_minutes, tip) VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 1,
   'Blanch the bitter leaf in boiling salted water for 5 minutes. Drain, then squeeze out as much water as possible repeatedly (at least 3 times). This removes the bitterness. Chop roughly and set aside.',
   15, 'The more you squeeze, the less bitter the final dish will be.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 2,
   'Dry-roast the raw peanuts in a pan over medium heat for 5 minutes, stirring constantly, until the skins crack. Let cool, rub off the skins, then blend with a little water into a smooth paste.',
   15, 'Alternatively use smooth peanut butter — but roasted fresh peanuts give superior flavour.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 3,
   'Blend the onions, garlic, scotch bonnet and dried shrimp into a rough paste. Heat oil in a large pot and fry this paste over medium heat for 8–10 minutes until fragrant and reduced.',
   12, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 4,
   'Add the smoked beef and smoked fish to the pot. Stir and cook 5 minutes. Add the peanut paste, stir well and add 500 ml of water. Simmer for 15 minutes, stirring often to prevent the peanuts from sticking.',
   20, 'Keep stirring — peanut sauces can catch and burn.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 5,
   'Add the squeezed bitter leaf. Stir gently to combine everything. Simmer on low heat for 15–20 minutes. Taste and adjust salt. The sauce should be thick and fragrant.',
   20, 'Do not cover the pot at this stage — you want the sauce to thicken.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000005', 6,
   'Serve hot with fried plantain, boiled yam or rice. This dish tastes even better the next day.',
   5, NULL)
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- 6. Ratatouille Provençale  (d0000000-0000-0000-0000-000000000006)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO recipes_schema.recipe_ingredients
  (id, recipe_id, ingredient_id, quantity, unit, display_text, is_optional, display_order)
VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000004-0000-0000-0000-000000000011', 1,    'large','1 large eggplant (500 g), diced 2 cm',         false, 1),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000004-0000-0000-0000-000000000010', 2,    'medium','2 medium zucchini, diced 2 cm',               false, 2),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000004-0000-0000-0000-000000000007', 2,    'large','2 large red bell peppers, diced',              false, 3),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000004-0000-0000-0000-000000000004', 4,    'large','4 large ripe tomatoes, roughly chopped',       false, 4),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000004-0000-0000-0000-000000000001', 2,    'medium','2 medium onions, sliced',                     false, 5),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000004-0000-0000-0000-000000000003', 4,    'cloves','4 cloves garlic, thinly sliced',              false, 6),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000005-0000-0000-0000-000000000008', 5,    'tbsp', '5 tbsp good olive oil',                        false, 7),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000005-0000-0000-0000-000000000012', 2,    'sprigs','2 sprigs fresh rosemary',                    false, 8),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000005-0000-0000-0000-000000000011', 4,    'sprigs','4 sprigs fresh thyme',                        false, 9),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000005-0000-0000-0000-000000000010', 2,    'pcs',  '2 bay leaves',                                false, 10),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 'e1000005-0000-0000-0000-000000000013', 1,    'tsp',  'Salt and pepper to taste',                    false, 11)
ON CONFLICT DO NOTHING;

INSERT INTO recipes_schema.recipe_steps (id, recipe_id, step_number, instruction, duration_minutes, tip) VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 1,
   'Toss the diced eggplant with 1 tsp salt and let sit in a colander for 20 minutes to draw out moisture. Rinse and pat dry.',
   25, 'Salting the eggplant prevents it from absorbing too much oil during cooking.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 2,
   'Heat 2 tbsp olive oil in a large heavy pan or Dutch oven over medium-high heat. Sauté the eggplant for 5–6 minutes until golden. Remove and set aside. Repeat with the zucchini (3–4 minutes). Remove and set aside.',
   15, 'Cooking each vegetable separately prevents steaming — you want caramelisation, not mush.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 3,
   'In the same pan, heat 2 tbsp olive oil. Sauté the onions over medium heat for 8 minutes until soft. Add the garlic and bell peppers, cook 5 more minutes.',
   15, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 4,
   'Add the tomatoes, herbs (thyme, rosemary, bay leaves) and season with salt and pepper. Stir well, partially cover and simmer for 15 minutes until the tomatoes have broken down into a sauce.',
   15, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 5,
   'Return the eggplant and zucchini to the pan. Stir gently to combine. Simmer uncovered on low heat for 20–25 minutes until everything is very tender and the sauce has thickened. Drizzle with 1 tbsp fresh olive oil before serving.',
   25, 'Ratatouille is even better at room temperature, and the next day.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000006', 6,
   'Remove the bay leaves and herb sprigs. Taste and adjust seasoning. Serve warm, at room temperature or cold — as a main, side dish or on crusty bread.',
   5, NULL)
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- 7. Suya  (d0000000-0000-0000-0000-000000000007)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO recipes_schema.recipe_ingredients
  (id, recipe_id, ingredient_id, quantity, unit, display_text, is_optional, display_order)
VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000002-0000-0000-0000-000000000003', 600,  'g',    '600 g beef sirloin, sliced paper-thin across the grain', false, 1),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000006-0000-0000-0000-000000000001', 4,    'tbsp', '4 tbsp yaji (suya spice blend)',               false, 2),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000005-0000-0000-0000-000000000007', 2,    'tbsp', '2 tbsp smooth peanut butter',                 false, 3),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000006-0000-0000-0000-000000000002', 1,    'tsp',  '1 tsp smoked paprika',                        false, 4),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000006-0000-0000-0000-000000000003', 0.5,  'tsp',  '½ tsp cayenne pepper',                        false, 5),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000006-0000-0000-0000-000000000004', 1,    'tsp',  '1 tsp ginger powder',                         false, 6),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000005-0000-0000-0000-000000000009', 2,    'tbsp', '2 tbsp vegetable oil',                        false, 7),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000005-0000-0000-0000-000000000013', 1,    'tsp',  '1 tsp salt',                                  false, 8),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000004-0000-0000-0000-000000000001', 1,    'large','1 large red onion, sliced into rings (to serve)', true, 9),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 'e1000004-0000-0000-0000-000000000004', 2,    'pcs',  '2 tomatoes, sliced (to serve)',                true, 10)
ON CONFLICT DO NOTHING;

INSERT INTO recipes_schema.recipe_steps (id, recipe_id, step_number, instruction, duration_minutes, tip) VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 1,
   'Partially freeze the beef (20 minutes in the freezer) to make it easier to slice very thinly — aim for 3–4 mm slices. Thread 2–3 slices onto each skewer, weaving back and forth.',
   25, 'Ultra-thin slices are the secret to authentic suya texture — they cook fast and get crispy edges.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 2,
   'Mix together the yaji, peanut butter, paprika, cayenne, ginger, oil and salt into a paste. Coat each beef skewer generously on all sides. Marinate for at least 30 minutes (up to 4 hours in the fridge).',
   35, 'The peanut butter helps the spices adhere and adds richness.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 3,
   'Prepare a very hot charcoal grill or set your oven grill to maximum. Grill the suya skewers 3–4 minutes per side, until nicely charred at the edges and cooked through. Baste once with any remaining marinade halfway through.',
   15, 'High heat is essential — you want char, not steaming.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000007', 4,
   'Dust the cooked suya with a little more dry yaji spice. Serve immediately with sliced raw onion, tomatoes and more suya spice on the side. Eat hot off the skewer.',
   5, NULL)
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- 8. Coq au Vin  (d0000000-0000-0000-0000-000000000008)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO recipes_schema.recipe_ingredients
  (id, recipe_id, ingredient_id, quantity, unit, display_text, is_optional, display_order)
VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000002-0000-0000-0000-000000000002', 1.8,  'kg',   '1.8 kg whole chicken, cut into 8 pieces',     false, 1),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000005-0000-0000-0000-000000000001', 750,  'ml',   '750 ml red wine (Burgundy or Côtes du Rhône)', false, 2),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000005-0000-0000-0000-000000000002', 250,  'ml',   '250 ml chicken broth',                        false, 3),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000002-0000-0000-0000-000000000005', 150,  'g',    '150 g lardons',                               false, 4),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000004-0000-0000-0000-000000000009', 250,  'g',    '250 g button mushrooms, halved',              false, 5),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000004-0000-0000-0000-000000000002', 200,  'g',    '200 g pearl onions',                          false, 6),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000004-0000-0000-0000-000000000003', 3,    'cloves','3 cloves garlic, minced',                   false, 7),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000004-0000-0000-0000-000000000005', 2,    'tbsp', '2 tbsp tomato paste',                         false, 8),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000003-0000-0000-0000-000000000001', 40,   'g',    '40 g butter',                                 false, 9),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000005-0000-0000-0000-000000000008', 2,    'tbsp', '2 tbsp olive oil',                            false, 10),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000005-0000-0000-0000-000000000011', 4,    'sprigs','4 sprigs thyme',                             false, 11),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000005-0000-0000-0000-000000000010', 2,    'pcs',  '2 bay leaves',                                false, 12),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 'e1000005-0000-0000-0000-000000000013', 1,    'tsp',  'Salt and pepper to taste',                    false, 13)
ON CONFLICT DO NOTHING;

INSERT INTO recipes_schema.recipe_steps (id, recipe_id, step_number, instruction, duration_minutes, tip) VALUES
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 1,
   'Marinate the chicken pieces in the wine with thyme and bay leaves for 2 hours (or overnight). Remove the chicken, pat dry and reserve the marinade.',
   10, 'Drying the chicken ensures it browns properly — skip this and you will get grey, steamed chicken.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 2,
   'Render the lardons in a large Dutch oven over medium heat until crispy. Remove with a slotted spoon. Brown the chicken pieces in the lardon fat in batches, skin side down first, 4–5 minutes per side. Remove and set aside.',
   20, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 3,
   'In the same pot, sauté the garlic for 1 minute. Add tomato paste and cook 1 minute. Pour in the reserved wine marinade, bring to a boil and reduce by a third (about 10 minutes).',
   15, 'Reducing the wine before adding the chicken concentrates the flavour.'),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 4,
   'Return the chicken and lardons to the pot. Add the chicken broth. Liquid should nearly cover the chicken. Bring to a gentle simmer, cover and cook on low heat for 45–55 minutes until the chicken is very tender.',
   55, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 5,
   'Meanwhile, sauté the pearl onions in butter until golden, 8 minutes. Sauté the mushrooms separately until browned. Add both to the pot in the last 15 minutes.',
   15, NULL),
  (gen_random_uuid(), 'd0000000-0000-0000-0000-000000000008', 6,
   'Remove the chicken. Increase heat and boil the sauce for 5–8 minutes to reduce and thicken. Return chicken to pot, taste for seasoning and serve with crusty bread, mashed potatoes or buttered noodles.',
   10, 'For an extra glossy sauce, stir in 1 tbsp cold butter at the very end (monter au beurre).')
ON CONFLICT DO NOTHING;

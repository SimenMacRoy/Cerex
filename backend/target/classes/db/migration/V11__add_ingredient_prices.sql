-- ─────────────────────────────────────────────────────────────
-- V11__add_ingredient_prices.sql
-- Add estimated price (FCFA) to ingredients master catalog
-- ─────────────────────────────────────────────────────────────

ALTER TABLE recipes_schema.ingredients
    ADD COLUMN IF NOT EXISTS estimated_price_fcfa NUMERIC(10,2);

-- ── Seed prices for seeded ingredients (V10) ────────────────
-- Grains
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 800  WHERE id = 'e1000001-0000-0000-0000-000000000001'; -- Long-grain rice (per kg)
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 700  WHERE id = 'e1000001-0000-0000-0000-000000000002'; -- Broken rice
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 600  WHERE id = 'e1000001-0000-0000-0000-000000000003'; -- All-purpose flour
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 700  WHERE id = 'e1000001-0000-0000-0000-000000000004'; -- Bread flour

-- Proteins / Meat
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 3500 WHERE id = 'e1000002-0000-0000-0000-000000000001'; -- Beef chuck (per kg)
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 2500 WHERE id = 'e1000002-0000-0000-0000-000000000002'; -- Whole chicken (per kg)
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 4000 WHERE id = 'e1000002-0000-0000-0000-000000000003'; -- Beef sirloin (per kg)
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 3000 WHERE id = 'e1000002-0000-0000-0000-000000000004'; -- Smoked beef
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 2000 WHERE id = 'e1000002-0000-0000-0000-000000000005'; -- Lardons

-- Seafood
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 4500 WHERE id = 'e1000002-0000-0000-0000-000000000006'; -- Whole white fish
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 1500 WHERE id = 'e1000002-0000-0000-0000-000000000007'; -- Smoked fish
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 2500 WHERE id = 'e1000002-0000-0000-0000-000000000008'; -- Dried shrimp

-- Dairy
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 1500 WHERE id = 'e1000003-0000-0000-0000-000000000001'; -- Unsalted butter
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 600  WHERE id = 'e1000003-0000-0000-0000-000000000002'; -- Milk (whole)

-- Vegetables
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 200  WHERE id = 'e1000004-0000-0000-0000-000000000001'; -- Yellow onion
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 300  WHERE id = 'e1000004-0000-0000-0000-000000000002'; -- Pearl onions
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 100  WHERE id = 'e1000004-0000-0000-0000-000000000003'; -- Garlic
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 250  WHERE id = 'e1000004-0000-0000-0000-000000000004'; -- Tomatoes (fresh)
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 200  WHERE id = 'e1000004-0000-0000-0000-000000000005'; -- Tomato paste
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 150  WHERE id = 'e1000004-0000-0000-0000-000000000006'; -- Scotch bonnet pepper
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 200  WHERE id = 'e1000004-0000-0000-0000-000000000007'; -- Bell pepper
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 150  WHERE id = 'e1000004-0000-0000-0000-000000000008'; -- Carrots
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 500  WHERE id = 'e1000004-0000-0000-0000-000000000009'; -- Mushrooms
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 200  WHERE id = 'e1000004-0000-0000-0000-000000000010'; -- Zucchini
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 200  WHERE id = 'e1000004-0000-0000-0000-000000000011'; -- Eggplant
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 400  WHERE id = 'e1000004-0000-0000-0000-000000000012'; -- Cassava / yam
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 500  WHERE id = 'e1000004-0000-0000-0000-000000000013'; -- Bitter leaf (ndolé)
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 150  WHERE id = 'e1000004-0000-0000-0000-000000000014'; -- Cabbage
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 300  WHERE id = 'e1000004-0000-0000-0000-000000000015'; -- Plantain

-- Pantry / Sauces
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 3000 WHERE id = 'e1000005-0000-0000-0000-000000000001'; -- Red wine
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 300  WHERE id = 'e1000005-0000-0000-0000-000000000002'; -- Chicken broth
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 300  WHERE id = 'e1000005-0000-0000-0000-000000000003'; -- Beef broth
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 250  WHERE id = 'e1000005-0000-0000-0000-000000000004'; -- Vegetable broth
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 300  WHERE id = 'e1000005-0000-0000-0000-000000000005'; -- Tomato puree
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 800  WHERE id = 'e1000005-0000-0000-0000-000000000006'; -- Peanuts
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 1000 WHERE id = 'e1000005-0000-0000-0000-000000000007'; -- Peanut butter
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 700  WHERE id = 'e1000005-0000-0000-0000-000000000008'; -- Olive oil
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 400  WHERE id = 'e1000005-0000-0000-0000-000000000009'; -- Vegetable oil
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 100  WHERE id = 'e1000005-0000-0000-0000-000000000010'; -- Bay leaves
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 100  WHERE id = 'e1000005-0000-0000-0000-000000000011'; -- Fresh thyme
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 100  WHERE id = 'e1000005-0000-0000-0000-000000000012'; -- Fresh rosemary
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 50   WHERE id = 'e1000005-0000-0000-0000-000000000013'; -- Salt
UPDATE recipes_schema.ingredients SET estimated_price_fcfa = 75   WHERE id = 'e1000005-0000-0000-0000-000000000014'; -- Black pepper

-- Set a default price for any ingredient that was auto-created and has no price
UPDATE recipes_schema.ingredients
SET estimated_price_fcfa = 300
WHERE estimated_price_fcfa IS NULL;

-- ─────────────────────────────────────────────────────────────
-- V12__grocery_stores_and_products.sql
-- Add bulk/currency columns to grocery_products,
-- seed grocery stores and products linked to recipe ingredients
-- ─────────────────────────────────────────────────────────────

-- ── New columns on grocery_products ──────────────────────────
ALTER TABLE orders_schema.grocery_products
    ADD COLUMN IF NOT EXISTS is_bulk_only      BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS minimum_quantity   INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS currency           VARCHAR(3) DEFAULT 'XAF';

-- ── Seed Grocery Stores ──────────────────────────────────────
-- owner_id = admin user from V9
INSERT INTO orders_schema.groceries (
  id, owner_id, status, name, description, slug, grocery_type,
  phone, email, address_line1, city, state_province, postal_code,
  latitude, longitude, delivery_radius_km,
  average_rating, total_reviews,
  supports_delivery, supports_pickup, minimum_order_amount,
  is_organic_certified, is_verified, eco_score,
  created_at, updated_at
) VALUES
-- 1. Marché Frais Douala (Cameroon — XAF)
('f0000000-0000-0000-0000-000000000001',
 'a0000000-0000-0000-0000-000000000003', 'ACTIVE',
 'Marché Frais Douala',
 'Ingrédients africains frais et épices — du ndolé aux plantains. Ouvert 7/7.',
 'marche-frais-douala', 'GENERAL',
 '+237 691 234 567', 'contact@marchefraisdouala.cm',
 '123 Rue des Marchés, Akwa', 'Douala', 'Littoral', '00237',
 4.0510000, 9.7679000, 20.0,
 4.60, 287, TRUE, TRUE, 1000.00, FALSE, TRUE, 72,
 NOW(), NOW()),

-- 2. Épicerie Parisienne (France — EUR)
('f0000000-0000-0000-0000-000000000002',
 'a0000000-0000-0000-0000-000000000003', 'ACTIVE',
 'Épicerie Parisienne',
 'Ingrédients fins français : beurre, vin, farine, herbes. Livraison express Paris.',
 'epicerie-parisienne', 'SPECIALTY',
 '+33 1 42 33 44 55', 'bonjour@epicerieparisienne.fr',
 '45 Rue du Faubourg Saint-Antoine', 'Paris', 'Île-de-France', '75012',
 48.8499000, 2.3709000, 15.0,
 4.80, 521, TRUE, TRUE, 15.00, FALSE, TRUE, 85,
 NOW(), NOW()),

-- 3. Afro Market Accra (Ghana — GHS)
('f0000000-0000-0000-0000-000000000003',
 'a0000000-0000-0000-0000-000000000003', 'ACTIVE',
 'Afro Market Accra',
 'West African staples — palm oil, yam, suya spice, groundnuts. Wholesale available.',
 'afro-market-accra', 'GENERAL',
 '+233 302 123 456', 'info@afromarketaccra.gh',
 '88 Oxford Street, Osu', 'Accra', 'Greater Accra', 'GA-100',
 5.5600000, -0.1869000, 25.0,
 4.40, 198, TRUE, TRUE, 20.00, FALSE, TRUE, 65,
 NOW(), NOW()),

-- 4. BioFresh Organic (Paris suburb — EUR)
('f0000000-0000-0000-0000-000000000004',
 'a0000000-0000-0000-0000-000000000003', 'ACTIVE',
 'BioFresh Organic',
 'Produits bio certifiés — légumes, herbes, produits laitiers, farines.',
 'biofresh-organic', 'ORGANIC',
 '+33 1 55 66 77 88', 'hello@biofresh.fr',
 '12 Avenue de la République', 'Montreuil', 'Île-de-France', '93100',
 48.8637000, 2.4433000, 12.0,
 4.70, 156, TRUE, TRUE, 20.00, TRUE, TRUE, 95,
 NOW(), NOW()),

-- 5. Marché Sandaga Dakar (Senegal — XOF)
('f0000000-0000-0000-0000-000000000005',
 'a0000000-0000-0000-0000-000000000003', 'ACTIVE',
 'Marché Sandaga',
 'Le cœur du marché dakarois — poisson frais, riz brisé, légumes locaux.',
 'marche-sandaga-dakar', 'GENERAL',
 '+221 77 123 45 67', 'sandaga@dakar.sn',
 'Place Sandaga', 'Dakar', 'Dakar', '10000',
 14.6694000, -17.4381000, 18.0,
 4.30, 342, TRUE, TRUE, 500.00, FALSE, TRUE, 70,
 NOW(), NOW())

ON CONFLICT (slug) DO NOTHING;

-- ── Seed Grocery Products ────────────────────────────────────
-- Columns: id, grocery_id, ingredient_id, name, description, category,
--          price, price_per_unit, unit, stock_quantity, is_in_stock,
--          is_organic, is_local, is_bulk_only, minimum_quantity, currency,
--          eco_score, created_at, updated_at

-- ═══════════════════════════════════════════════════════════════
-- Marché Frais Douala (XAF)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO orders_schema.grocery_products (
  id, grocery_id, ingredient_id, name, description, category,
  price, price_per_unit, unit, stock_quantity, is_in_stock,
  is_organic, is_local, is_bulk_only, minimum_quantity, currency,
  eco_score, created_at, updated_at
) VALUES
  -- Rice
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000001-0000-0000-0000-000000000001',
   'Riz long grain (5kg)', 'Sac de riz long grain premium', 'Grains',
   3500, 700, 'kg', 50, TRUE, FALSE, FALSE, FALSE, 1, 'XAF', 60, NOW(), NOW()),

  -- Tomatoes
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000004-0000-0000-0000-000000000004',
   'Tomates fraîches (1kg)', 'Tomates mûres du marché', 'Légumes',
   800, 800, 'kg', 100, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 80, NOW(), NOW()),

  -- Scotch bonnet
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000004-0000-0000-0000-000000000006',
   'Piment scotch bonnet (100g)', 'Piments très forts', 'Légumes',
   300, 3000, 'kg', 80, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 85, NOW(), NOW()),

  -- Onions
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000004-0000-0000-0000-000000000001',
   'Oignons jaunes (1kg)', 'Oignons frais du marché', 'Légumes',
   500, 500, 'kg', 200, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 75, NOW(), NOW()),

  -- Garlic
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000004-0000-0000-0000-000000000003',
   'Ail frais (tête)', 'Têtes d''ail frais', 'Légumes',
   200, 200, 'pièce', 150, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 80, NOW(), NOW()),

  -- Tomato paste
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000004-0000-0000-0000-000000000005',
   'Concentré de tomates (400g)', 'Double concentré', 'Conserves',
   600, 1500, 'kg', 60, TRUE, FALSE, FALSE, FALSE, 1, 'XAF', 55, NOW(), NOW()),

  -- Vegetable oil
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000005-0000-0000-0000-000000000009',
   'Huile végétale (1L)', 'Huile de cuisine raffinée', 'Épicerie',
   1200, 1200, 'L', 40, TRUE, FALSE, FALSE, FALSE, 1, 'XAF', 50, NOW(), NOW()),

  -- Bitter leaf (ndolé)
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000004-0000-0000-0000-000000000013',
   'Feuilles de ndolé (500g)', 'Feuilles amères fraîches ou surgelées', 'Légumes',
   1500, 3000, 'kg', 30, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 90, NOW(), NOW()),

  -- Groundnuts
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000005-0000-0000-0000-000000000006',
   'Arachides crues (1kg)', 'Arachides décortiquées', 'Épicerie',
   2000, 2000, 'kg', 45, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 75, NOW(), NOW()),

  -- Plantain
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000004-0000-0000-0000-000000000015',
   'Plantains mûrs (régime)', 'Bananes plantain mûres', 'Fruits',
   1000, 1000, 'régime', 25, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 85, NOW(), NOW()),

  -- Smoked beef / jerky
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000002-0000-0000-0000-000000000004',
   'Bœuf fumé (500g)', 'Viande de bœuf fumée artisanale', 'Viandes',
   2500, 5000, 'kg', 20, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 65, NOW(), NOW()),

  -- Smoked fish
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000002-0000-0000-0000-000000000007',
   'Poisson fumé (200g)', 'Poisson fumé artisanal', 'Poissonnerie',
   1200, 6000, 'kg', 35, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 70, NOW(), NOW()),

  -- Dried shrimp
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000002-0000-0000-0000-000000000008',
   'Crevettes séchées (100g)', 'Crevettes séchées du littoral', 'Poissonnerie',
   800, 8000, 'kg', 50, TRUE, FALSE, TRUE, FALSE, 1, 'XAF', 75, NOW(), NOW()),

  -- BULK ONLY: Rice 25kg sack
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001',
   'e1000001-0000-0000-0000-000000000001',
   'Riz long grain (sac 25kg)', 'Sac de riz en gros — meilleur prix', 'Grains',
   14000, 560, 'kg', 20, TRUE, FALSE, FALSE, TRUE, 1, 'XAF', 55, NOW(), NOW())

ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- Épicerie Parisienne (EUR)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO orders_schema.grocery_products (
  id, grocery_id, ingredient_id, name, description, category,
  price, price_per_unit, unit, stock_quantity, is_in_stock,
  is_organic, is_local, is_bulk_only, minimum_quantity, currency,
  eco_score, created_at, updated_at
) VALUES
  -- Butter
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000003-0000-0000-0000-000000000001',
   'Beurre doux AOP (250g)', 'Beurre de Charentes-Poitou AOP', 'Crèmerie',
   3.50, 14.00, 'kg', 80, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 70, NOW(), NOW()),

  -- Bread flour
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000001-0000-0000-0000-000000000004',
   'Farine T55 (1kg)', 'Farine de blé type 55 pour viennoiseries', 'Farines',
   1.80, 1.80, 'kg', 60, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 65, NOW(), NOW()),

  -- Burgundy wine
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000005-0000-0000-0000-000000000001',
   'Bourgogne Pinot Noir (75cl)', 'Vin de Bourgogne pour la cuisine', 'Vins',
   8.90, 11.87, 'L', 30, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 60, NOW(), NOW()),

  -- Beef chuck
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000002-0000-0000-0000-000000000001',
   'Paleron de bœuf (1kg)', 'Viande à braiser — races françaises', 'Boucherie',
   16.90, 16.90, 'kg', 15, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 55, NOW(), NOW()),

  -- Lardons
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000002-0000-0000-0000-000000000005',
   'Lardons fumés (200g)', 'Lardons de poitrine fumée', 'Charcuterie',
   2.40, 12.00, 'kg', 50, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 50, NOW(), NOW()),

  -- Mushrooms
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000009',
   'Champignons de Paris (250g)', 'Cremini / champignons bruns', 'Légumes',
   2.10, 8.40, 'kg', 40, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 75, NOW(), NOW()),

  -- Pearl onions
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000002',
   'Oignons grelots (250g)', 'Petits oignons pour bourguignon', 'Légumes',
   2.50, 10.00, 'kg', 30, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 80, NOW(), NOW()),

  -- Whole chicken
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000002-0000-0000-0000-000000000002',
   'Poulet entier fermier (1.8kg)', 'Label Rouge — élevé en plein air', 'Volaille',
   12.90, 7.17, 'kg', 12, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 70, NOW(), NOW()),

  -- Active dry yeast
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000007-0000-0000-0000-000000000001',
   'Levure sèche (7g × 5)', 'Sachet de levure de boulangerie', 'Boulangerie',
   1.60, 45.71, 'kg', 100, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 60, NOW(), NOW()),

  -- Milk
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000003-0000-0000-0000-000000000002',
   'Lait entier (1L)', 'Lait frais entier pasteurisé', 'Crèmerie',
   1.20, 1.20, 'L', 50, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 65, NOW(), NOW()),

  -- Eggs
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000007-0000-0000-0000-000000000003',
   'Œufs frais × 6', 'Œufs de poules élevées en plein air', 'Crèmerie',
   2.80, 0.47, 'pièce', 40, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 70, NOW(), NOW()),

  -- Olive oil
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000005-0000-0000-0000-000000000008',
   'Huile d''olive vierge extra (75cl)', 'Huile d''olive de Provence', 'Épicerie',
   7.50, 10.00, 'L', 25, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 80, NOW(), NOW()),

  -- Zucchini
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000010',
   'Courgettes (500g)', 'Courgettes de saison', 'Légumes',
   1.60, 3.20, 'kg', 35, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 85, NOW(), NOW()),

  -- Eggplant
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000011',
   'Aubergine (pièce)', 'Aubergine violette de saison', 'Légumes',
   1.30, 1.30, 'pièce', 30, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 85, NOW(), NOW()),

  -- Tomatoes
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000004',
   'Tomates grappes (500g)', 'Tomates mûries sur grappe', 'Légumes',
   2.20, 4.40, 'kg', 50, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 85, NOW(), NOW()),

  -- Onions
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000001',
   'Oignons jaunes (1kg)', 'Oignons de Roscoff', 'Légumes',
   1.50, 1.50, 'kg', 80, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 80, NOW(), NOW()),

  -- Garlic
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000003',
   'Ail (tête)', 'Ail blanc de Lomagne', 'Légumes',
   0.80, 0.80, 'pièce', 60, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 80, NOW(), NOW()),

  -- Carrots
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000008',
   'Carottes (1kg)', 'Carottes de plein champ', 'Légumes',
   1.30, 1.30, 'kg', 60, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 80, NOW(), NOW()),

  -- Tomato paste
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000005',
   'Concentré de tomates (200g)', 'Double concentré de tomates', 'Conserves',
   1.40, 7.00, 'kg', 45, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 60, NOW(), NOW()),

  -- Bell pepper
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000004-0000-0000-0000-000000000007',
   'Poivron rouge (pièce)', 'Poivron rouge de saison', 'Légumes',
   1.50, 1.50, 'pièce', 40, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 80, NOW(), NOW()),

  -- Sugar
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000007-0000-0000-0000-000000000002',
   'Sucre en poudre (1kg)', 'Sucre blanc cristallisé', 'Épicerie',
   1.10, 1.10, 'kg', 70, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 55, NOW(), NOW()),

  -- Thyme
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000005-0000-0000-0000-000000000011',
   'Thym frais (botte)', 'Thym frais de Provence', 'Herbes',
   1.20, 1.20, 'botte', 40, TRUE, FALSE, TRUE, FALSE, 1, 'EUR', 90, NOW(), NOW()),

  -- Bay leaves
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000005-0000-0000-0000-000000000010',
   'Laurier (sachet 10 feuilles)', 'Feuilles de laurier séchées', 'Épices',
   1.00, 1.00, 'sachet', 50, TRUE, FALSE, FALSE, FALSE, 1, 'EUR', 70, NOW(), NOW()),

  -- Rosemary
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002',
   'e1000005-0000-0000-0000-000000000012',
   'Romarin frais (botte)', 'Romarin frais de Provence', 'Herbes',
   1.20, 1.20, 'botte', 35, TRUE, FALSE, TRUE, FALSE, 1, 'EUR', 90, NOW(), NOW())

ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- Afro Market Accra (GHS)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO orders_schema.grocery_products (
  id, grocery_id, ingredient_id, name, description, category,
  price, price_per_unit, unit, stock_quantity, is_in_stock,
  is_organic, is_local, is_bulk_only, minimum_quantity, currency,
  eco_score, created_at, updated_at
) VALUES
  -- Rice
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000001-0000-0000-0000-000000000001',
   'Long-grain rice (5kg)', 'Premium Jasmine rice', 'Grains',
   65.00, 13.00, 'kg', 100, TRUE, FALSE, FALSE, FALSE, 1, 'GHS', 60, NOW(), NOW()),

  -- Suya spice
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000006-0000-0000-0000-000000000001',
   'Yaji / Suya spice blend (200g)', 'Authentic Nigerian suya spice', 'Spices',
   25.00, 125.00, 'kg', 60, TRUE, FALSE, TRUE, FALSE, 1, 'GHS', 75, NOW(), NOW()),

  -- Beef sirloin
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000002-0000-0000-0000-000000000003',
   'Beef sirloin (1kg)', 'Thinly sliced for suya', 'Meat',
   85.00, 85.00, 'kg', 20, TRUE, FALSE, FALSE, FALSE, 1, 'GHS', 50, NOW(), NOW()),

  -- Peanut butter
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000005-0000-0000-0000-000000000007',
   'Peanut butter (500g)', 'Smooth groundnut paste', 'Pantry',
   18.00, 36.00, 'kg', 45, TRUE, FALSE, TRUE, FALSE, 1, 'GHS', 70, NOW(), NOW()),

  -- Groundnuts bulk (BULK ONLY)
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000005-0000-0000-0000-000000000006',
   'Raw groundnuts (10kg sack)', 'Bulk — wholesale only', 'Pantry',
   120.00, 12.00, 'kg', 15, TRUE, FALSE, TRUE, TRUE, 1, 'GHS', 80, NOW(), NOW()),

  -- Scotch bonnet
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000004-0000-0000-0000-000000000006',
   'Scotch bonnet peppers (100g)', 'Fresh hot peppers', 'Vegetables',
   5.00, 50.00, 'kg', 80, TRUE, FALSE, TRUE, FALSE, 1, 'GHS', 85, NOW(), NOW()),

  -- Vegetable oil
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000005-0000-0000-0000-000000000009',
   'Vegetable oil (1L)', 'Refined cooking oil', 'Pantry',
   22.00, 22.00, 'L', 35, TRUE, FALSE, FALSE, FALSE, 1, 'GHS', 50, NOW(), NOW()),

  -- Smoked paprika
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000006-0000-0000-0000-000000000002',
   'Smoked paprika (100g)', 'Spanish-style smoked paprika', 'Spices',
   12.00, 120.00, 'kg', 40, TRUE, FALSE, FALSE, FALSE, 1, 'GHS', 65, NOW(), NOW()),

  -- Ginger powder
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000006-0000-0000-0000-000000000004',
   'Ginger powder (100g)', 'Dried ground ginger', 'Spices',
   8.00, 80.00, 'kg', 50, TRUE, FALSE, TRUE, FALSE, 1, 'GHS', 70, NOW(), NOW()),

  -- Onions
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000004-0000-0000-0000-000000000001',
   'Yellow onions (1kg)', 'Fresh onions', 'Vegetables',
   8.00, 8.00, 'kg', 100, TRUE, FALSE, TRUE, FALSE, 1, 'GHS', 75, NOW(), NOW()),

  -- Tomatoes
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003',
   'e1000004-0000-0000-0000-000000000004',
   'Tomatoes (1kg)', 'Fresh ripe tomatoes', 'Vegetables',
   12.00, 12.00, 'kg', 80, TRUE, FALSE, TRUE, FALSE, 1, 'GHS', 80, NOW(), NOW())

ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════
-- Marché Sandaga Dakar (XOF)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO orders_schema.grocery_products (
  id, grocery_id, ingredient_id, name, description, category,
  price, price_per_unit, unit, stock_quantity, is_in_stock,
  is_organic, is_local, is_bulk_only, minimum_quantity, currency,
  eco_score, created_at, updated_at
) VALUES
  -- Broken rice
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000001-0000-0000-0000-000000000002',
   'Riz brisé (5kg)', 'Riz brisé de qualité pour thiéboudienne', 'Grains',
   3000, 600, 'kg', 80, TRUE, FALSE, FALSE, FALSE, 1, 'XOF', 65, NOW(), NOW()),

  -- Whole fish
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000002-0000-0000-0000-000000000006',
   'Thiof frais (1.5kg)', 'Poisson frais du jour', 'Poissonnerie',
   8000, 5333, 'kg', 10, TRUE, FALSE, TRUE, FALSE, 1, 'XOF', 85, NOW(), NOW()),

  -- Smoked fish
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000002-0000-0000-0000-000000000007',
   'Poisson fumé (200g)', 'Poisson fumé artisanal', 'Poissonnerie',
   2500, 12500, 'kg', 30, TRUE, FALSE, TRUE, FALSE, 1, 'XOF', 70, NOW(), NOW()),

  -- Cassava
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000004-0000-0000-0000-000000000012',
   'Manioc (1kg)', 'Manioc frais', 'Légumes',
   500, 500, 'kg', 40, TRUE, FALSE, TRUE, FALSE, 1, 'XOF', 80, NOW(), NOW()),

  -- Dried shrimp
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000002-0000-0000-0000-000000000008',
   'Crevettes séchées (100g)', 'Crevettes séchées locales', 'Poissonnerie',
   1500, 15000, 'kg', 50, TRUE, FALSE, TRUE, FALSE, 1, 'XOF', 75, NOW(), NOW()),

  -- Tomatoes
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000004-0000-0000-0000-000000000004',
   'Tomates fraîches (1kg)', 'Tomates mûres du marché', 'Légumes',
   500, 500, 'kg', 80, TRUE, FALSE, TRUE, FALSE, 1, 'XOF', 80, NOW(), NOW()),

  -- Onions
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000004-0000-0000-0000-000000000001',
   'Oignons (1kg)', 'Oignons frais', 'Légumes',
   400, 400, 'kg', 100, TRUE, FALSE, TRUE, FALSE, 1, 'XOF', 75, NOW(), NOW()),

  -- Garlic
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000004-0000-0000-0000-000000000003',
   'Ail (tête)', 'Ail frais', 'Légumes',
   200, 200, 'pièce', 80, TRUE, FALSE, TRUE, FALSE, 1, 'XOF', 80, NOW(), NOW()),

  -- Tomato paste
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000004-0000-0000-0000-000000000005',
   'Concentré de tomates (400g)', 'Double concentré', 'Conserves',
   600, 1500, 'kg', 50, TRUE, FALSE, FALSE, FALSE, 1, 'XOF', 55, NOW(), NOW()),

  -- Vegetable oil
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000005-0000-0000-0000-000000000009',
   'Huile végétale (1L)', 'Huile de cuisine', 'Épicerie',
   1000, 1000, 'L', 40, TRUE, FALSE, FALSE, FALSE, 1, 'XOF', 50, NOW(), NOW()),

  -- Cabbage
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000004-0000-0000-0000-000000000014',
   'Chou vert (pièce)', 'Chou frais du jardin', 'Légumes',
   300, 300, 'pièce', 30, TRUE, FALSE, TRUE, FALSE, 1, 'XOF', 80, NOW(), NOW()),

  -- Carrots
  (gen_random_uuid(), 'f0000000-0000-0000-0000-000000000005',
   'e1000004-0000-0000-0000-000000000008',
   'Carottes (1kg)', 'Carottes fraîches', 'Légumes',
   400, 400, 'kg', 50, TRUE, FALSE, TRUE, FALSE, 1, 'XOF', 75, NOW(), NOW())

ON CONFLICT DO NOTHING;

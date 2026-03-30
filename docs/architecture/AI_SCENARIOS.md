# Cerex — AI Features Specification

> Version: 1.0.0 | Last Updated: 2026-03-30

---

## Table of Contents

1. [AI Architecture Overview](#1-ai-architecture-overview)
2. [Recipe Generation Algorithm](#2-recipe-generation-algorithm)
3. [Personalized Recommendation Engine](#3-personalized-recommendation-engine)
4. [Multi-Language Translation Pipeline](#4-multi-language-translation-pipeline)
5. [Culinary Trend Analysis](#5-culinary-trend-analysis)
6. [Fraud Detection ML Model](#6-fraud-detection-ml-model)
7. [Waste Optimization Algorithm](#7-waste-optimization-algorithm)
8. [Personalized Menu Generation](#8-personalized-menu-generation)
9. [LLM API Integration](#9-llm-api-integration)

---

## 1. AI Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│                     CEREX AI PLATFORM                        │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐    │
│  │               FEATURE STORE (Redis + MongoDB)        │    │
│  │  User embeddings, recipe embeddings, trend vectors   │    │
│  └──────────────────────┬───────────────────────────────┘    │
│                         │                                    │
│  ┌──────────┐ ┌─────────┴──────────┐ ┌──────────────────┐   │
│  │  LLM     │ │   ML MODELS        │ │   EXTERNAL AI    │   │
│  │  Engine  │ │  (Python/FastAPI)  │ │   APIS           │   │
│  │ GPT-4o   │ │  ├── Recommender   │ │  ├── DeepL       │   │
│  │ Llama 3  │ │  ├── FraudDetect   │ │  ├── Google NLP  │   │
│  │ Mistral  │ │  ├── TrendAnalyzer │ │  ├── AWS Rekogn  │   │
│  └──────────┘ │  └── WasteOptimizer│ │  └── USDA API    │   │
│               └────────────────────┘ └──────────────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐    │
│  │               MODEL REGISTRY (MLflow)               │    │
│  │  Version tracking, A/B testing, performance metrics  │    │
│  └──────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
```

**Technology Stack:**
- Python 3.12 / FastAPI (AI microservice)
- PyTorch 2.3 (deep learning models)
- scikit-learn (classical ML)
- LangChain / LlamaIndex (LLM orchestration)
- MLflow (model lifecycle management)
- Hugging Face Transformers (NLP/translation)
- Apache Spark (batch feature engineering)
- Feast (feature store coordination)

---

## 2. Recipe Generation Algorithm

### 2.1 Generation Pipeline

```
User Input
    │
    ▼
Input Validation & Sanitization
    │
    ▼
Context Enrichment
├── User preference profile (Redis)
├── Seasonal ingredient availability (PostgreSQL)
├── Cultural constraints for cuisine type (MongoDB)
├── Nutrition targets based on user preferences
└── Similar published recipes (Elasticsearch vector search)
    │
    ▼
Prompt Engineering
├── System prompt: Culinary expert persona + safety rules
├── Few-shot examples: 3 high-quality recipe examples
├── Cultural context: cuisine-specific techniques and ingredients
└── User request: formatted structured input
    │
    ▼
LLM API Call (with streaming)
├── Primary: GPT-4o (best quality)
├── Fallback: Llama 3 70B (cost optimization)
└── Timeout: 30 seconds
    │
    ▼
Response Parsing & Validation
├── JSON schema validation
├── Ingredient completeness check
├── Step coherence validation
└── Safety content filtering
    │
    ▼
Post-Processing Enrichment
├── Nutrition calculation (USDA FoodData API)
├── Missing allergen detection
├── Cost estimation (ingredient price API)
└── Difficulty level calibration
    │
    ▼
Quality Scoring
├── Ingredient harmony score (ML classifier)
├── Cultural authenticity score (embedding similarity)
└── Reject if quality < 0.6 and retry
    │
    ▼
Return GeneratedRecipe to client
```

### 2.2 Prompt Engineering

```python
# recipe_generator.py

SYSTEM_PROMPT = """You are Chef Cerex, a world-class culinary expert with deep knowledge
of cuisines from every culture and continent. You create authentic, detailed, and
culturally respectful recipes.

RULES:
1. Always provide accurate measurements in both metric and imperial units
2. Respect cultural dietary restrictions (halal, kosher, vegetarian traditions, etc.)
3. Include cultural context and story behind the dish
4. Use professional culinary terminology while remaining accessible
5. Never generate recipes that could cause harm (toxic ingredients, unsafe techniques)
6. Format the output EXACTLY as JSON following the schema provided

QUALITY STANDARDS:
- Ingredient quantities must be precise and tested
- Steps must be in logical sequence with timing
- Temperature and technique specifications are required where relevant
- Nutrition estimates should be realistic
"""

def build_generation_prompt(request: RecipeGenerationRequest) -> str:
    seasonal_ingredients = get_seasonal_ingredients(request.cuisine_type)
    cultural_notes = get_cultural_constraints(request.cuisine_type)

    return f"""
Generate a {request.cuisine_type} recipe with the following specifications:

Meal Type: {request.meal_type}
Servings: {request.servings}
Target Difficulty: {request.difficulty_target}
Maximum Time: {request.max_time_minutes} minutes
Available Ingredients: {', '.join(request.available_ingredients)}
Dietary Restrictions: {', '.join(request.dietary_restrictions)}
Spice Level Preference: {request.spice_level_preference}/5
Additional Notes: {request.additional_prompt or 'None'}

Currently in Season: {', '.join(seasonal_ingredients)}
Cultural Notes: {cultural_notes}

Respond with valid JSON matching this exact schema:
{RECIPE_JSON_SCHEMA}
"""

RECIPE_JSON_SCHEMA = """
{
  "title": "string (max 200 chars)",
  "description": "string (2-3 sentences)",
  "cultural_story": "string (1 paragraph about the dish's origin/significance)",
  "ingredients": [
    {
      "name": "string",
      "quantity": "number",
      "unit": "string",
      "preparation": "string (optional)",
      "group": "string (optional)"
    }
  ],
  "steps": [
    {
      "step_number": "integer",
      "title": "string",
      "instruction": "string",
      "duration_minutes": "integer",
      "technique": "string",
      "temperature_celsius": "number (optional)",
      "tips": "string (optional)"
    }
  ],
  "total_time_minutes": "integer",
  "difficulty_level": "BEGINNER|EASY|MEDIUM|HARD|EXPERT",
  "spice_level": "0-5",
  "estimated_nutrition_per_serving": {
    "calories_kcal": "number",
    "protein_g": "number",
    "carbs_g": "number",
    "fat_g": "number"
  },
  "tags": ["array of relevant tags"],
  "chef_tips": "string (professional tips)"
}
"""
```

### 2.3 Quality Scoring Model

```python
class RecipeQualityScorer:
    """
    Scores AI-generated recipes on multiple dimensions.
    Uses a fine-tuned BERT model + rule-based checks.
    """

    def __init__(self):
        self.bert_scorer = pipeline("text-classification",
                                     model="cerex/recipe-quality-bert")
        self.ingredient_encoder = load_ingredient_embedding_model()

    def score(self, recipe: dict) -> float:
        scores = []

        # 1. Structural completeness (rule-based)
        scores.append(self._score_completeness(recipe))

        # 2. Ingredient harmony (ML - are these ingredients culturally/culinarily compatible?)
        scores.append(self._score_ingredient_harmony(recipe['ingredients']))

        # 3. Step coherence (BERT-based NLU)
        step_text = ' '.join([s['instruction'] for s in recipe['steps']])
        coherence = self.bert_scorer(step_text)[0]['score']
        scores.append(coherence)

        # 4. Cultural authenticity (embedding similarity to known recipes from that cuisine)
        scores.append(self._score_cultural_authenticity(recipe))

        # Weighted average
        weights = [0.2, 0.3, 0.25, 0.25]
        return sum(s * w for s, w in zip(scores, weights))

    def _score_completeness(self, recipe: dict) -> float:
        required_fields = ['title', 'description', 'ingredients', 'steps',
                           'total_time_minutes', 'difficulty_level']
        present = sum(1 for f in required_fields if recipe.get(f))
        return present / len(required_fields)

    def _score_ingredient_harmony(self, ingredients: list) -> float:
        # Use ingredient co-occurrence matrix trained on 500K recipes
        ingredient_names = [i['name'] for i in ingredients]
        embeddings = self.ingredient_encoder.encode(ingredient_names)
        # Calculate average pairwise cosine similarity
        similarities = cosine_similarity(embeddings)
        n = len(ingredients)
        if n < 2:
            return 0.5
        avg_sim = (similarities.sum() - n) / (n * (n - 1))
        return float(avg_sim)
```

---

## 3. Personalized Recommendation Engine

### 3.1 Hybrid Recommendation Architecture

```
User arrives at homepage
         │
         ▼
Check User Profile Completeness
         │
    ┌────┴────┐
    │<3 orders│ ≥3 orders
    ▼         ▼
Popularity-  Hybrid Engine
Based        │
    │        ├── 40% Collaborative Filtering
    │        ├── 35% Content-Based
    │        └── 25% Contextual (time, location, season)
    │         │
    └────┬────┘
         ▼
Post-Filter (apply hard constraints)
├── Remove already-seen (last 30 days)
├── Apply dietary restrictions (remove allergens)
└── Apply active promotions boost
         │
         ▼
Diversity Injection
└── Ensure ≥3 different continents in top-10
         │
         ▼
Return ranked recommendations with explanations
```

### 3.2 Matrix Factorization (Collaborative Filtering)

```python
class CerexRecommender:
    """
    Neural Collaborative Filtering model.
    Trained nightly on user interaction data.
    """

    def __init__(self, n_users: int, n_recipes: int, embedding_dim: int = 128):
        self.user_embedding  = nn.Embedding(n_users, embedding_dim)
        self.recipe_embedding = nn.Embedding(n_recipes, embedding_dim)
        self.user_bias   = nn.Embedding(n_users, 1)
        self.recipe_bias = nn.Embedding(n_recipes, 1)
        self.global_bias = nn.Parameter(torch.zeros(1))

        # Deep layers for non-linear interactions
        self.fc_layers = nn.Sequential(
            nn.Linear(embedding_dim * 2, 256),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(256, 128),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(128, 1),
            nn.Sigmoid()
        )

    def forward(self, user_ids: torch.Tensor, recipe_ids: torch.Tensor) -> torch.Tensor:
        u_emb = self.user_embedding(user_ids)
        r_emb = self.recipe_embedding(recipe_ids)

        # Concatenate user and recipe embeddings
        combined = torch.cat([u_emb, r_emb], dim=1)
        interaction_score = self.fc_layers(combined).squeeze()

        # Matrix factorization component
        mf_score = (u_emb * r_emb).sum(dim=1)

        # Biases
        u_bias = self.user_bias(user_ids).squeeze()
        r_bias = self.recipe_bias(recipe_ids).squeeze()

        return torch.sigmoid(interaction_score + mf_score + u_bias + r_bias + self.global_bias)

    def recommend(self, user_id: int, candidate_recipe_ids: list, top_k: int = 20) -> list:
        """Get top-k recommendations for a user from candidate pool."""
        user_tensor    = torch.tensor([user_id] * len(candidate_recipe_ids))
        recipes_tensor = torch.tensor(candidate_recipe_ids)

        with torch.no_grad():
            scores = self.forward(user_tensor, recipes_tensor)

        # Return top-k sorted by score
        top_k_indices = scores.topk(top_k).indices
        return [(candidate_recipe_ids[i], scores[i].item()) for i in top_k_indices]
```

### 3.3 Contextual Signals

```python
def build_contextual_features(user: dict, request_context: dict) -> dict:
    """Add time, location, and seasonal context to recommendations."""

    hour = datetime.now().hour
    month = datetime.now().month

    return {
        # Time-based signals
        "is_breakfast_time": 6 <= hour <= 10,
        "is_lunch_time":     11 <= hour <= 14,
        "is_dinner_time":    18 <= hour <= 22,
        "is_weekend":        datetime.now().weekday() >= 5,

        # Seasonal signals (Northern Hemisphere)
        "is_spring":  month in [3, 4, 5],
        "is_summer":  month in [6, 7, 8],
        "is_autumn":  month in [9, 10, 11],
        "is_winter":  month in [12, 1, 2],

        # User context
        "user_country": user.get("country_code"),
        "user_timezone": user.get("timezone"),

        # Request context
        "source": request_context.get("source"),         # "homepage", "post-order", "search"
        "weather_condition": get_weather(user.get("city")),  # "cold", "hot", "rainy"
    }
```

---

## 4. Multi-Language Translation Pipeline

### 4.1 Translation Architecture

```
Source Content (English)
         │
         ▼
Pre-Translation Processing
├── Detect culinary terms requiring special handling
├── Identify proper nouns (dish names to preserve)
└── Extract cultural references needing localization
         │
         ▼
Translation Engine Selection
├── DeepL (priority — highest quality for EU languages)
├── Google Cloud Translation (fallback + Asian languages)
└── Cerex Custom NMT (for culinary-specific vocabulary)
         │
         ▼
Post-Translation Quality Check
├── Back-translation verification (translate back, compare)
├── Terminology consistency validation
└── Cultural sensitivity review (AI + human)
         │
         ▼
Caching in MongoDB (translations collection)
```

### 4.2 Translation Service

```python
# translation_service.py

class CerexTranslationService:
    """
    Multi-provider translation with quality assurance.
    Supports 50+ languages with culinary terminology awareness.
    """

    SUPPORTED_LANGUAGES = {
        'fr': ('DeepL', 0.95),        # French - DeepL primary
        'es': ('DeepL', 0.94),        # Spanish - DeepL primary
        'de': ('DeepL', 0.93),        # German - DeepL primary
        'pt': ('DeepL', 0.91),        # Portuguese - DeepL primary
        'it': ('DeepL', 0.92),        # Italian - DeepL primary
        'zh': ('Google', 0.88),       # Chinese - Google primary
        'ja': ('Google', 0.87),       # Japanese - Google primary
        'ko': ('Google', 0.87),       # Korean - Google primary
        'ar': ('Google', 0.85),       # Arabic - Google primary (RTL)
        'hi': ('Google', 0.84),       # Hindi - Google primary
        'sw': ('CerexNMT', 0.80),    # Swahili - Custom model
        'ha': ('CerexNMT', 0.78),    # Hausa - Custom model
        'yo': ('CerexNMT', 0.77),    # Yoruba - Custom model
    }

    # Terms that should NEVER be translated (preserve original)
    CULINARY_PROPER_NOUNS = {
        "Jollof Rice", "Mole Negro", "Bibimbap", "Couscous",
        "Bouillabaisse", "Rendang", "Tagine", "Injera",
        "Mafé", "Egusi", "Fufu", "Ugali"
    }

    def translate_recipe(self, recipe: dict, target_language: str) -> dict:
        """Translate all translatable fields of a recipe."""
        provider, quality_threshold = self.SUPPORTED_LANGUAGES.get(
            target_language, ('Google', 0.75)
        )

        translated = {}

        # Translate title (preserve proper nouns)
        translated['title'] = self._translate_with_preservation(
            recipe['title'], target_language, provider
        )

        # Translate description and story
        translated['description'] = self._translate(
            recipe['description'], target_language, provider
        )
        if recipe.get('story'):
            translated['story'] = self._translate(
                recipe['story'], target_language, provider
            )

        # Translate step instructions
        translated['steps'] = [
            {**step, 'instruction': self._translate(step['instruction'], target_language, provider)}
            for step in recipe['steps']
        ]

        # Translate ingredient preparations
        translated['ingredients'] = [
            {**ing, 'preparation': self._translate(ing.get('preparation', ''), target_language, provider)
             if ing.get('preparation') else None}
            for ing in recipe['ingredients']
        ]

        # Quality validation
        quality_score = self._assess_quality(recipe['description'], translated['description'])
        if quality_score < quality_threshold:
            self.logger.warning(
                f"Translation quality {quality_score} below threshold {quality_threshold} "
                f"for language {target_language}. Flagging for human review."
            )
            translated['_quality_flag'] = True

        return translated

    def _translate_with_preservation(self, text: str, target_lang: str, provider: str) -> str:
        """Translate while preserving culinary proper nouns."""
        placeholders = {}
        protected_text = text

        for term in self.CULINARY_PROPER_NOUNS:
            if term.lower() in text.lower():
                placeholder = f"__CEREX_TERM_{len(placeholders)}__"
                placeholders[placeholder] = term
                protected_text = protected_text.replace(term, placeholder)

        translated = self._translate(protected_text, target_lang, provider)

        # Restore preserved terms
        for placeholder, original in placeholders.items():
            translated = translated.replace(placeholder, original)

        return translated
```

---

## 5. Culinary Trend Analysis

### 5.1 Trend Detection Model

```python
# trend_analyzer.py

class CulinaryTrendAnalyzer:
    """
    Detects emerging food trends using time-series analysis
    on recipe views, searches, orders, and social media signals.
    """

    def analyze_trends(self, continent_code: str, period: str = 'week') -> list[TrendResult]:
        # Collect signals from multiple sources
        recipe_views    = self._get_recipe_view_velocity(continent_code, period)
        search_queries  = self._get_search_query_trends(continent_code, period)
        order_patterns  = self._get_order_trend_data(continent_code, period)
        social_signals  = self._get_social_engagement_trends(continent_code, period)

        # Combine signals using weighted ensemble
        combined = self._combine_signals(
            recipe_views    = recipe_views,    weight=0.25,
            search_queries  = search_queries,  weight=0.30,
            order_patterns  = order_patterns,  weight=0.30,
            social_signals  = social_signals,  weight=0.15
        )

        # Apply STL decomposition to identify trend vs seasonality
        # (separates real trends from seasonal patterns like "pumpkin in October")
        trends = []
        for cuisine_type, time_series in combined.items():
            stl_result = seasonal_decompose(time_series, model='additive', period=7)
            trend_component = stl_result.trend.dropna()

            # Calculate trend slope (positive = growing trend)
            growth_rate = self._calculate_growth_rate(trend_component)
            current_score = float(trend_component.iloc[-1])

            if growth_rate > 0.05:  # Growing by > 5% = trending
                trends.append(TrendResult(
                    cuisine_type   = cuisine_type,
                    trend_score    = current_score,
                    growth_rate    = growth_rate,
                    peak_day       = self._find_peak(trend_component),
                    key_ingredients= self._extract_key_ingredients(cuisine_type, continent_code),
                    top_recipes    = self._get_top_recipes_for_trend(cuisine_type, continent_code)
                ))

        return sorted(trends, key=lambda t: t.growth_rate, reverse=True)[:10]

    def _calculate_growth_rate(self, trend_series: pd.Series) -> float:
        """Calculate week-over-week growth rate using linear regression."""
        x = np.arange(len(trend_series)).reshape(-1, 1)
        y = trend_series.values
        model = LinearRegression().fit(x, y)
        baseline = y[:7].mean() if len(y) >= 14 else y[0]
        if baseline == 0:
            return 0.0
        return float(model.coef_[0] * 7 / baseline)  # Weekly growth rate
```

---

## 6. Fraud Detection ML Model

### 6.1 Model Architecture

```python
# restaurant_fraud_detector.py

class RestaurantFraudDetector:
    """
    Ensemble model: Isolation Forest + XGBoost + Neural Network
    Detects: fake reviews, order manipulation, identity fraud.
    """

    def __init__(self):
        # Anomaly detection for unknown fraud patterns
        self.isolation_forest = IsolationForest(
            n_estimators=200,
            contamination=0.05,   # Expected 5% fraud rate
            random_state=42
        )

        # Supervised classification on labeled fraud cases
        self.xgboost_classifier = XGBClassifier(
            n_estimators=500,
            max_depth=6,
            learning_rate=0.05,
            scale_pos_weight=19,  # Handle class imbalance (5% fraud)
            use_label_encoder=False,
            eval_metric='auc'
        )

        # Neural network for complex pattern detection
        self.neural_net = self._build_neural_network()

    def _build_neural_network(self) -> tf.keras.Model:
        inputs = tf.keras.Input(shape=(25,))  # 25 fraud features
        x = tf.keras.layers.Dense(128, activation='relu')(inputs)
        x = tf.keras.layers.BatchNormalization()(x)
        x = tf.keras.layers.Dropout(0.3)(x)
        x = tf.keras.layers.Dense(64, activation='relu')(x)
        x = tf.keras.layers.BatchNormalization()(x)
        x = tf.keras.layers.Dropout(0.2)(x)
        x = tf.keras.layers.Dense(32, activation='relu')(x)
        outputs = tf.keras.layers.Dense(1, activation='sigmoid')(x)

        model = tf.keras.Model(inputs, outputs)
        model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['auc'])
        return model

    def predict(self, features: dict) -> FraudPrediction:
        feature_vector = self._extract_features(features)

        # Get predictions from each model
        if_score  = self.isolation_forest.score_samples([feature_vector])[0]
        xgb_score = self.xgboost_classifier.predict_proba([feature_vector])[0][1]
        nn_score  = self.neural_net.predict([feature_vector])[0][0]

        # Normalize isolation forest score to [0, 1]
        if_normalized = (if_score * -1 + 0.5).clip(0, 1)

        # Ensemble (weighted average)
        final_score = (if_normalized * 0.3 + xgb_score * 0.4 + nn_score * 0.3)

        return FraudPrediction(
            score=float(final_score),
            risk_level=self._score_to_risk_level(final_score),
            model_versions={
                'isolation_forest': '1.2.0',
                'xgboost': '2.1.0',
                'neural_net': '1.5.0'
            }
        )

    def _extract_features(self, raw: dict) -> np.ndarray:
        return np.array([
            # Order features (7)
            raw.get('avg_order_value', 0),
            raw.get('order_velocity_7d', 0),
            raw.get('cancellation_rate', 0),
            raw.get('refund_rate', 0),
            raw.get('order_value_std_dev', 0),
            raw.get('peak_hour_order_concentration', 0),
            raw.get('delivery_distance_anomaly_score', 0),

            # Review features (8)
            raw.get('avg_rating', 0),
            raw.get('rating_std_dev', 0),
            raw.get('review_velocity_7d', 0),
            raw.get('new_account_review_ratio', 0),
            raw.get('duplicate_text_ratio', 0),
            raw.get('burst_detected', 0),
            raw.get('reviewer_geographic_concentration', 0),
            raw.get('response_rate_to_reviews', 0),

            # Account features (5)
            raw.get('account_age_days', 0),
            raw.get('is_verified', 0),
            raw.get('has_health_cert', 0),
            raw.get('business_license_verified', 0),
            raw.get('profile_completeness_score', 0),

            # Financial features (5)
            raw.get('total_revenue_30d', 0),
            raw.get('revenue_growth_rate', 0),
            raw.get('chargeback_count', 0),
            raw.get('promo_abuse_score', 0),
            raw.get('payment_failure_rate', 0),
        ])
```

---

## 7. Waste Optimization Algorithm

### 7.1 Ingredient Waste Minimization

```python
# waste_optimizer.py

class CerexWasteOptimizer:
    """
    Recommends recipes that maximize use of ingredients a user already has,
    minimizing waste and grocery spend using linear programming.
    """

    def optimize_weekly_menu(self, user_pantry: list[IngredientStock],
                              user_preferences: dict,
                              budget: float,
                              num_meals: int = 7) -> WeeklyMenu:
        """
        Formulate as a Linear Programming problem:
        - Maximize: ingredient utilization + preference score
        - Subject to: budget constraint, nutritional balance, variety
        """

        # Get candidate recipes
        candidates = self._get_candidate_recipes(user_preferences)

        # Build pantry coverage matrix
        # pantry_coverage[i][j] = fraction of recipe[j]'s ingredients covered by pantry
        coverage_matrix = self._build_coverage_matrix(candidates, user_pantry)

        # Solve LP
        n_recipes = len(candidates)
        x = cp.Variable(n_recipes, boolean=True)  # Binary: include recipe or not

        # Objective: maximize waste reduction score + preference alignment
        waste_scores = coverage_matrix.mean(axis=0)
        pref_scores  = np.array([self._preference_score(r, user_preferences) for r in candidates])
        objective = cp.Maximize(
            0.6 * waste_scores @ x + 0.4 * pref_scores @ x
        )

        # Constraints
        constraints = [
            cp.sum(x) == num_meals,                                   # Exactly num_meals
            self._build_cost_vector(candidates) @ x <= budget,        # Within budget
            self._build_nutrition_constraint(candidates, x),           # Nutritional balance
            self._build_variety_constraint(candidates, x),             # Continental variety
        ]

        problem = cp.Problem(objective, constraints)
        problem.solve(solver=cp.CBC)

        selected_recipes = [candidates[i] for i in range(n_recipes) if x.value[i] > 0.5]

        # Calculate waste reduction metrics
        coverage = self._calculate_pantry_coverage(selected_recipes, user_pantry)
        estimated_waste_reduction = coverage.waste_reduction_percentage
        estimated_savings = coverage.estimated_cost_savings

        return WeeklyMenu(
            recipes=selected_recipes,
            shopping_list=coverage.missing_ingredients,
            estimated_cost=coverage.total_cost,
            waste_reduction_percentage=estimated_waste_reduction,
            estimated_savings=estimated_savings,
            eco_score=self._calculate_eco_score(selected_recipes, coverage)
        )
```

---

## 8. Personalized Menu Generation

### 8.1 Dynamic Menu Engine

```python
# menu_generator.py

class PersonalizedMenuGenerator:
    """
    Generates complete personalized weekly menus considering:
    nutritional goals, cultural variety, budget, skill level, time constraints.
    """

    def generate_weekly_menu(self, user_id: str,
                              preferences: UserPreferences,
                              constraints: MenuConstraints) -> PersonalizedMenu:

        # Step 1: Gather user context
        user_profile = self.ai_profile_service.get_profile(user_id)
        seasonal_availability = self.seasonal_service.get_current_season_ingredients()

        # Step 2: Build nutrition targets per day
        nutrition_targets = self.nutrition_service.calculate_targets(
            age=preferences.age,
            weight_kg=preferences.weight_kg,
            height_cm=preferences.height_cm,
            activity_level=preferences.activity_level,
            goal=preferences.dietary_goal  # LOSE_WEIGHT, MAINTAIN, BUILD_MUSCLE
        )

        # Step 3: Recommend recipes for each meal slot
        weekly_plan = {}
        for day in range(7):
            daily_plan = {}
            remaining_calories = nutrition_targets.daily_calories

            for meal_type in ['breakfast', 'lunch', 'dinner', 'snack']:
                meal_calories = nutrition_targets.get_meal_calories(meal_type)
                available_time = constraints.get_available_time(day, meal_type)

                # Get recommendations for this slot
                candidates = self.recommender.recommend_for_meal_slot(
                    user_id=user_id,
                    meal_type=meal_type,
                    target_calories=meal_calories,
                    max_time_minutes=available_time,
                    dietary_restrictions=preferences.dietary_restrictions,
                    day_of_week=day,
                    already_planned=[r for r in weekly_plan.values() if r]  # Avoid repetition
                )

                # Select best candidate
                selected = self._select_optimal_recipe(
                    candidates=candidates,
                    nutrition_remaining=remaining_calories,
                    cultural_variety=weekly_plan,
                    user_profile=user_profile
                )

                daily_plan[meal_type] = selected
                if selected:
                    remaining_calories -= selected.nutrition.calories_kcal

            weekly_plan[f"day_{day}"] = daily_plan

        return PersonalizedMenu(
            user_id=user_id,
            week_start=datetime.now().date(),
            daily_plans=weekly_plan,
            nutritional_summary=self._summarize_nutrition(weekly_plan),
            shopping_list=self._generate_shopping_list(weekly_plan),
            estimated_budget=self._estimate_budget(weekly_plan),
            eco_score=self._calculate_eco_score(weekly_plan),
            cultural_diversity_score=self._calculate_cultural_diversity(weekly_plan)
        )
```

---

## 9. LLM API Integration

### 9.1 LLM Client with Fallback

```java
// AIService.java (Spring Boot)

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final OpenAIClient openAIClient;
    private final AnthropicClient anthropicClient;
    private final OllamaClient ollamaClient;         // Self-hosted Llama 3 fallback
    private final FeatureFlagService featureFlags;
    private final MeterRegistry meterRegistry;

    private static final int MAX_RETRIES = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /**
     * Generate a recipe using LLM with automatic fallback chain.
     * Primary: GPT-4o → Fallback: Claude 3.5 Sonnet → Fallback: Llama 3 (local)
     */
    public RecipeGenerationResult generateRecipe(RecipeGenerationRequest request) {
        String prompt = buildRecipePrompt(request);
        long startTime = System.currentTimeMillis();

        try {
            // Primary: OpenAI GPT-4o
            if (featureFlags.isEnabled("ai_openai")) {
                return generateWithOpenAI(prompt, request, startTime);
            }
        } catch (OpenAIException e) {
            log.warn("OpenAI call failed, falling back to Anthropic: {}", e.getMessage());
            meterRegistry.counter("ai.provider.fallback", "from", "openai", "to", "anthropic").increment();
        }

        try {
            // Fallback 1: Anthropic Claude
            if (featureFlags.isEnabled("ai_anthropic")) {
                return generateWithAnthropic(prompt, request, startTime);
            }
        } catch (AnthropicException e) {
            log.warn("Anthropic call failed, falling back to local model: {}", e.getMessage());
            meterRegistry.counter("ai.provider.fallback", "from", "anthropic", "to", "local").increment();
        }

        // Fallback 2: Local Ollama (Llama 3)
        return generateWithOllama(prompt, request, startTime);
    }

    private RecipeGenerationResult generateWithOpenAI(String prompt,
                                                       RecipeGenerationRequest request,
                                                       long startTime) {
        ChatCompletionRequest openAIRequest = ChatCompletionRequest.builder()
            .model("gpt-4o")
            .messages(List.of(
                ChatMessage.builder().role("system").content(SYSTEM_PROMPT).build(),
                ChatMessage.builder().role("user").content(prompt).build()
            ))
            .responseFormat(ResponseFormat.jsonObject())
            .temperature(0.7)
            .maxTokens(2500)
            .build();

        ChatCompletion response = openAIClient.chatCompletions().create(openAIRequest);
        String content = response.choices().get(0).message().content();

        RecipeDto generatedRecipe = parseAndValidateRecipe(content);

        long duration = System.currentTimeMillis() - startTime;
        meterRegistry.timer("ai.generation.duration", "provider", "openai").record(Duration.ofMillis(duration));

        return RecipeGenerationResult.builder()
            .recipe(generatedRecipe)
            .modelUsed("gpt-4o")
            .promptTokens(response.usage().promptTokens())
            .completionTokens(response.usage().completionTokens())
            .generationTimeMs(duration)
            .build();
    }

    private String buildRecipePrompt(RecipeGenerationRequest request) {
        return """
            Generate a %s recipe with these requirements:
            - Servings: %d
            - Target difficulty: %s
            - Max time: %d minutes
            - Available ingredients: %s
            - Dietary restrictions: %s
            - Additional notes: %s

            Respond with valid JSON only.
            """.formatted(
            request.getCuisineType(),
            request.getServings(),
            request.getDifficultyTarget(),
            request.getMaxTimeMinutes(),
            String.join(", ", request.getAvailableIngredients()),
            String.join(", ", request.getDietaryRestrictions()),
            request.getAdditionalPrompt() != null ? request.getAdditionalPrompt() : "None"
        );
    }

    /**
     * Generate embedding vector for a recipe (for semantic search).
     * Uses text-embedding-3-small (1536 dimensions).
     */
    public float[] generateRecipeEmbedding(Recipe recipe) {
        String textToEmbed = String.format("%s. %s. Ingredients: %s. Tags: %s",
            recipe.getTitle(),
            recipe.getDescription(),
            recipe.getIngredients().stream()
                .map(ri -> ri.getIngredient().getName())
                .collect(Collectors.joining(", ")),
            String.join(", ", recipe.getTags())
        );

        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
            .model("text-embedding-3-small")
            .input(textToEmbed)
            .build();

        EmbeddingResponse response = openAIClient.embeddings().create(embeddingRequest);
        List<Float> embedding = response.data().get(0).embedding();

        return ArrayUtils.toPrimitive(embedding.toArray(new Float[0]));
    }
}
```

---

*Document Version: 1.0.0 | Next Review: 2026-06-30 | Owner: Cerex AI/ML Team*

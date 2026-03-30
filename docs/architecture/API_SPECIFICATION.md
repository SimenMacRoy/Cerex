# Cerex — REST API Specification

> Version: 1.0.0 | Base URL: `https://api.cerex.com/api/v1` | Last Updated: 2026-03-30

---

## Table of Contents

1. [API Standards](#1-api-standards)
2. [Authentication Endpoints](#2-authentication-endpoints)
3. [User Management Endpoints](#3-user-management-endpoints)
4. [Recipe CRUD Endpoints](#4-recipe-crud-endpoints)
5. [Search & Filter Endpoints](#5-search--filter-endpoints)
6. [Order Management Endpoints](#6-order-management-endpoints)
7. [Social Features Endpoints](#7-social-features-endpoints)
8. [AI Endpoints](#8-ai-endpoints)
9. [Media Endpoints](#9-media-endpoints)
10. [Admin Endpoints](#10-admin-endpoints)
11. [Error Response Standards](#11-error-response-standards)
12. [Rate Limiting Strategy](#12-rate-limiting-strategy)
13. [Versioning Strategy](#13-versioning-strategy)

---

## 1. API Standards

### Base Response Envelope

All API responses follow this standard structure:

```json
{
  "success": true,
  "data": { ... },
  "meta": {
    "requestId": "req_01JA2X3Y4Z",
    "timestamp": "2026-03-30T14:22:33.123Z",
    "version": "1.0.0",
    "duration_ms": 45
  },
  "pagination": {
    "page": 1,
    "size": 20,
    "totalElements": 542,
    "totalPages": 28,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### Pagination

All list endpoints support cursor-based or offset-based pagination:

```
?page=0&size=20&sort=created_at,desc
?cursor=eyJpZCI6Ijk4ZDFjMjMifQ==&size=20
```

### HTTP Methods

| Method | Usage |
|--------|-------|
| GET | Read operations (no side effects) |
| POST | Create new resources |
| PUT | Full update (replace) |
| PATCH | Partial update |
| DELETE | Soft delete |

### Authentication Header

```
Authorization: Bearer <JWT_ACCESS_TOKEN>
```

---

## 2. Authentication Endpoints

### POST /auth/register

Register a new user account.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "Str0ng!P@ssword",
  "firstName": "Marie",
  "lastName": "Dupont",
  "username": "marie_cooks",
  "locale": "fr_FR",
  "gdprConsent": true,
  "marketingConsent": false
}
```

**Response 201:**
```json
{
  "success": true,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "username": "marie_cooks",
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "emailVerificationRequired": true
  }
}
```

**Validation Errors 422:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": [
      { "field": "email", "message": "Email is already registered" },
      { "field": "password", "message": "Password must be at least 8 characters" }
    ]
  }
}
```

---

### POST /auth/login

Authenticate with email/password.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "Str0ng!P@ssword",
  "deviceId": "device_abc123",
  "deviceType": "WEB",
  "rememberMe": false
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "username": "marie_cooks",
      "displayName": "Marie Dupont",
      "avatarUrl": "https://media.cerex.com/users/550e8400/avatar.webp",
      "role": "USER",
      "subscription": "EXPLORER",
      "isVerifiedChef": false
    }
  }
}
```

---

### POST /auth/oauth/{provider}

OAuth2 authentication (google, facebook, apple).

**Request:**
```json
{
  "code": "4/P7q7W91a-oMsCeLvIaQm6bTrgtp7",
  "redirectUri": "https://cerex.com/auth/callback",
  "state": "random_state_token"
}
```

**Response 200:** _(same as /auth/login response)_

---

### POST /auth/refresh

Refresh access token.

**Request:**
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}
```

---

### POST /auth/logout

Invalidate current session.

**Headers:** `Authorization: Bearer <token>`

**Response 204:** _(No Content)_

---

### POST /auth/forgot-password

Initiate password reset flow.

**Request:**
```json
{ "email": "user@example.com" }
```

**Response 200:**
```json
{
  "success": true,
  "data": { "message": "Password reset email sent if account exists." }
}
```

---

### POST /auth/reset-password

Complete password reset.

**Request:**
```json
{
  "token": "reset_token_from_email",
  "newPassword": "NewStr0ng!P@ssword"
}
```

**Response 200:** _(success confirmation)_

---

### POST /auth/verify-email

Verify email address.

**Request:**
```json
{ "token": "email_verification_token" }
```

**Response 200:** _(success confirmation)_

---

## 3. User Management Endpoints

### GET /users/me

Get current authenticated user profile.

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "username": "marie_cooks",
    "displayName": "Marie Dupont",
    "firstName": "Marie",
    "lastName": "Dupont",
    "bio": "Passionate about Mediterranean cuisine 🌊",
    "avatarUrl": "https://media.cerex.com/users/550e8400/avatar.webp",
    "coverUrl": "https://media.cerex.com/users/550e8400/cover.webp",
    "role": "CHEF",
    "isVerifiedChef": true,
    "chefSpecialty": "Mediterranean & North African",
    "skillLevel": "PROFESSIONAL",
    "country": { "code": "FR", "name": "France" },
    "locale": "fr_FR",
    "dietaryPreferences": ["vegetarian"],
    "allergies": ["nuts"],
    "favoriteCuisines": ["mediterranean", "japanese", "west_african"],
    "followersCount": 12483,
    "followingCount": 342,
    "recipesCount": 87,
    "subscription": {
      "plan": "CHEF_PRO",
      "status": "ACTIVE",
      "expiresAt": "2027-03-30T00:00:00Z"
    },
    "badges": [
      { "code": "TOP_CHEF_2025", "name": "Top Chef 2025", "iconUrl": "..." }
    ],
    "createdAt": "2025-01-15T10:30:00Z"
  }
}
```

---

### PATCH /users/me

Update current user profile.

**Request:**
```json
{
  "displayName": "Marie Dupont",
  "bio": "French chef specializing in Mediterranean fusion",
  "dietaryPreferences": ["vegetarian", "gluten_free"],
  "locale": "fr_FR",
  "timezone": "Europe/Paris"
}
```

**Response 200:** _(updated user profile)_

---

### GET /users/{userId}

Get public user profile.

**Response 200:** _(public profile subset, no private fields)_

---

### GET /users/{userId}/recipes

Get recipes by a user.

**Query Params:** `?page=0&size=12&status=PUBLISHED&sort=created_at,desc`

**Response 200:** _(paginated RecipeCardDTO list)_

---

### GET /users/{userId}/followers

**Query Params:** `?page=0&size=20`

**Response 200:** _(paginated user list)_

---

### GET /users/{userId}/following

_(same structure as followers)_

---

### DELETE /users/me

Initiate GDPR account deletion request (30-day grace period).

**Response 200:**
```json
{
  "success": true,
  "data": {
    "message": "Account deletion scheduled. You have 30 days to cancel.",
    "scheduledDeletionDate": "2026-04-29T00:00:00Z",
    "cancellationToken": "cancel_abc123xyz"
  }
}
```

---

### GET /users/me/data-export

Request GDPR data export.

**Response 202:**
```json
{
  "success": true,
  "data": {
    "exportId": "export_abc123",
    "status": "PROCESSING",
    "estimatedReadyAt": "2026-03-31T14:00:00Z",
    "notificationEmail": "user@example.com"
  }
}
```

---

## 4. Recipe CRUD Endpoints

### GET /recipes

List recipes with filtering and pagination.

**Query Parameters:**
```
page          int      Page number (default: 0)
size          int      Page size (default: 20, max: 50)
sort          string   Sort field and direction (e.g., "created_at,desc")
continent     string   Continent code (AF, AS, EU, NA, SA, OC)
country       string   ISO country code (FR, JP, MX)
culture       UUID     Culture ID
category      string   Category slug
difficulty    string   BEGINNER|EASY|MEDIUM|HARD|EXPERT
minRating     float    Minimum average rating (0-5)
maxTime       int      Maximum total time in minutes
isVegan       boolean
isGlutenFree  boolean
isHalal       boolean
isPremium     boolean
author        UUID     Filter by author ID
search        string   Full-text search query
tags          string[] Comma-separated tags
```

**Response 200:**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "title": "Poulet Yassa",
      "slug": "poulet-yassa-senegalais",
      "description": "A tangy Senegalese marinated chicken dish with caramelized onions.",
      "coverImageUrl": "https://media.cerex.com/recipes/550e8400/card.webp",
      "author": {
        "id": "auth123",
        "username": "chef_diallo",
        "displayName": "Aminata Diallo",
        "avatarUrl": "https://media.cerex.com/users/auth123/avatar.webp",
        "isVerifiedChef": true
      },
      "continent": { "code": "AF", "name": "Africa" },
      "country": { "code": "SN", "name": "Senegal" },
      "culture": { "id": "cult123", "name": "Senegambian" },
      "category": { "slug": "main-course", "name": "Main Course" },
      "difficultyLevel": "MEDIUM",
      "totalTimeMinutes": 90,
      "servings": 4,
      "avgRating": 4.8,
      "ratingCount": 342,
      "viewCount": 15234,
      "likeCount": 2341,
      "isVegan": false,
      "isGlutenFree": true,
      "isHalal": true,
      "isPremium": false,
      "isAiGenerated": false,
      "tags": ["chicken", "caramelized-onions", "west-african", "lemon"],
      "publishedAt": "2026-01-15T10:00:00Z"
    }
  ],
  "meta": { ... },
  "pagination": { "page": 0, "size": 20, "totalElements": 12483, "totalPages": 625 }
}
```

---

### POST /recipes

Create a new recipe.

**Request:**
```json
{
  "title": "Mole Negro Oaxaqueño",
  "description": "The most complex and celebrated mole in Mexican cuisine...",
  "story": "This recipe has been passed down through generations in Oaxacan families...",
  "categoryId": "cat-main-course-uuid",
  "continentId": "cont-na-uuid",
  "countryId": "country-mx-uuid",
  "cultureId": "culture-oaxacan-uuid",
  "recipeType": "DISH",
  "cuisineType": "Mexican",
  "courseType": "MAIN",
  "difficultyLevel": "HARD",
  "spiceLevel": 3,
  "prepTimeMinutes": 60,
  "cookTimeMinutes": 180,
  "servings": 8,
  "isVegan": false,
  "isGlutenFree": true,
  "isHalal": false,
  "ingredients": [
    {
      "ingredientId": "ingr-mulato-chile-uuid",
      "quantity": 100,
      "unit": "g",
      "preparation": "dried, deveined, and toasted",
      "isOptional": false,
      "groupName": "For the chile paste"
    },
    {
      "ingredientId": "ingr-chocolate-uuid",
      "quantity": 50,
      "unit": "g",
      "preparation": "roughly chopped",
      "isOptional": false,
      "groupName": "For the mole"
    }
  ],
  "steps": [
    {
      "stepNumber": 1,
      "title": "Toast the chiles",
      "instruction": "On a dry comal or heavy skillet over medium heat, toast each chile variety separately...",
      "durationMinutes": 15,
      "technique": "dry-roast",
      "tips": "Be careful not to burn the chiles or the mole will be bitter"
    }
  ],
  "tags": ["mole", "mexican", "oaxacan", "complex", "chocolate"],
  "keywords": ["mole negro", "oaxacan mole", "mexican mole sauce"],
  "status": "DRAFT"
}
```

**Response 201:**
```json
{
  "success": true,
  "data": {
    "id": "recipe-uuid-new",
    "slug": "mole-negro-oaxaqueno",
    "status": "DRAFT",
    "createdAt": "2026-03-30T14:22:33Z",
    "editUrl": "/recipes/mole-negro-oaxaqueno/edit"
  }
}
```

---

### GET /recipes/{idOrSlug}

Get full recipe detail.

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Poulet Yassa",
    "slug": "poulet-yassa-senegalais",
    "description": "...",
    "story": "Yassa originates from the Casamance region...",

    "author": {
      "id": "auth123",
      "username": "chef_diallo",
      "displayName": "Aminata Diallo",
      "avatarUrl": "...",
      "isVerifiedChef": true,
      "chefSpecialty": "West African Cuisine"
    },

    "geography": {
      "continent": { "code": "AF", "name": "Africa" },
      "country": { "code": "SN", "name": "Senegal", "flagEmoji": "🇸🇳" },
      "culture": { "name": "Senegambian", "description": "..." }
    },

    "culinary": {
      "category": { "slug": "main-course", "name": "Main Course" },
      "recipeType": "DISH",
      "cuisineType": "West African",
      "courseType": "MAIN",
      "difficultyLevel": "MEDIUM",
      "spiceLevel": 2
    },

    "timing": {
      "prepTimeMinutes": 30,
      "cookTimeMinutes": 60,
      "restTimeMinutes": 0,
      "totalTimeMinutes": 90
    },

    "servings": { "amount": 4, "unit": "persons" },

    "nutrition": {
      "perServing": {
        "caloriesKcal": 485,
        "proteinG": 38.5,
        "carbsG": 42.1,
        "fatG": 16.2,
        "fiberG": 4.8
      }
    },

    "dietary": {
      "isVegan": false,
      "isVegetarian": false,
      "isGlutenFree": true,
      "isDairyFree": true,
      "isHalal": true,
      "isKosher": false
    },

    "ingredients": [
      {
        "id": "ri-uuid-1",
        "ingredient": {
          "id": "ingr-chicken-uuid",
          "name": "Whole Chicken",
          "category": "poultry",
          "imageUrl": "..."
        },
        "quantity": 1.5,
        "unit": "kg",
        "preparation": "cut into 8 pieces",
        "isOptional": false,
        "groupName": null,
        "displayOrder": 1
      }
    ],

    "steps": [
      {
        "stepNumber": 1,
        "title": "Marinate the chicken",
        "instruction": "In a large bowl, combine lemon juice, onions, mustard, garlic...",
        "durationMinutes": 30,
        "imageUrl": "https://media.cerex.com/recipes/uuid/step1.webp",
        "tips": "Marinate overnight for best results",
        "technique": "marinate"
      }
    ],

    "media": {
      "coverImageUrl": "https://media.cerex.com/recipes/uuid/hero.webp",
      "videoUrl": "https://media.cerex.com/recipes/uuid/video_720p.mp4",
      "gallery": ["https://media.cerex.com/recipes/uuid/step1.webp"]
    },

    "engagement": {
      "viewCount": 15234,
      "likeCount": 2341,
      "saveCount": 891,
      "orderCount": 543,
      "shareCount": 234,
      "commentCount": 87,
      "avgRating": 4.8,
      "ratingCount": 342
    },

    "userInteraction": {
      "isLiked": false,
      "isSaved": true,
      "collectionName": "African Favorites",
      "userRating": null
    },

    "relatedRecipes": [...],
    "orderable": {
      "isAvailable": true,
      "restaurants": [
        { "id": "rest-uuid", "name": "Dakar Kitchen", "deliveryTime": "35-45 min", "price": 18.50 }
      ]
    },

    "status": "PUBLISHED",
    "isPremium": false,
    "isAiGenerated": false,
    "tags": ["chicken", "west-african", "yassa"],
    "publishedAt": "2026-01-15T10:00:00Z",
    "updatedAt": "2026-02-01T12:00:00Z"
  }
}
```

---

### PUT /recipes/{id}

Full update of a recipe (author only).

**Request:** _(same as POST /recipes)_

**Response 200:** _(updated recipe)_

---

### PATCH /recipes/{id}

Partial update of a recipe.

**Request:**
```json
{
  "status": "PUBLISHED",
  "coverImageUrl": "https://media.cerex.com/recipes/uuid/hero.webp"
}
```

**Response 200:** _(updated recipe)_

---

### DELETE /recipes/{id}

Soft delete a recipe (author or admin only).

**Response 204:** _(No Content)_

---

### POST /recipes/{id}/publish

Publish a draft recipe (triggers moderation pipeline).

**Response 200:**
```json
{
  "success": true,
  "data": {
    "status": "PENDING_REVIEW",
    "message": "Recipe submitted for review. Expected review time: 2-4 hours.",
    "estimatedPublishAt": "2026-03-30T18:00:00Z"
  }
}
```

---

### POST /recipes/{id}/like

Toggle like on a recipe.

**Response 200:**
```json
{
  "success": true,
  "data": { "liked": true, "likeCount": 2342 }
}
```

---

### POST /recipes/{id}/save

Save recipe to collection.

**Request:**
```json
{ "collectionName": "African Favorites" }
```

**Response 200:**
```json
{
  "success": true,
  "data": { "saved": true, "collectionName": "African Favorites" }
}
```

---

## 5. Search & Filter Endpoints

### GET /search/recipes

Full-text search with advanced filtering.

**Query Parameters:**
```
q             string   Search query (full-text)
page          int      Page number
size          int      Page size
continent     string   Filter by continent code
country       string   Filter by ISO country code
culture       UUID     Filter by culture ID
category      string   Filter by category slug
difficulty    string   BEGINNER|EASY|MEDIUM|HARD|EXPERT
minRating     float    Min average rating
maxTime       int      Max total time in minutes
isVegan       boolean
isGlutenFree  boolean
isHalal       boolean
isPremium     boolean
sort          string   relevance|rating|recent|popular|trending
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "query": "spicy chicken curry",
    "totalResults": 1234,
    "searchTimeMs": 23,
    "facets": {
      "continents": [
        { "code": "AS", "name": "Asia", "count": 842 },
        { "code": "EU", "name": "Europe", "count": 312 }
      ],
      "difficulty": [
        { "level": "EASY", "count": 234 },
        { "level": "MEDIUM", "count": 543 }
      ],
      "dietary": {
        "vegan": 123,
        "glutenFree": 456,
        "halal": 789
      }
    },
    "results": [...]
  }
}
```

---

### GET /search/autocomplete

Get search suggestions as user types.

**Query Params:** `?q=chick&type=recipes,ingredients,chefs&limit=10`

**Response 200:**
```json
{
  "success": true,
  "data": {
    "recipes": [
      { "id": "...", "title": "Chicken Tikka Masala", "coverImageUrl": "..." }
    ],
    "ingredients": [
      { "id": "...", "name": "Chicken Breast", "category": "poultry" }
    ],
    "chefs": [
      { "id": "...", "username": "chef_priya", "displayName": "Priya Sharma" }
    ]
  }
}
```

---

### GET /search/trending

Get trending recipes globally or by region.

**Query Params:** `?continent=AF&period=week&limit=10`

**Response 200:** _(list of trending recipes with trend score)_

---

### GET /explore/continents/{continentCode}/recipes

Explore recipes by continent with rich cultural context.

**Response 200:**
```json
{
  "success": true,
  "data": {
    "continent": {
      "code": "AF",
      "name": "Africa",
      "cuisineSummary": "Africa's culinary landscape is as diverse as its cultures...",
      "coverImageUrl": "..."
    },
    "countries": [
      {
        "code": "NG",
        "name": "Nigeria",
        "flagEmoji": "🇳🇬",
        "featuredDish": "Jollof Rice",
        "recipeCount": 342
      }
    ],
    "featuredRecipes": [...],
    "trendingRecipes": [...]
  }
}
```

---

## 6. Order Management Endpoints

### POST /orders

Place a new order.

**Request:**
```json
{
  "restaurantId": "rest-uuid-123",
  "orderType": "DELIVERY",
  "items": [
    {
      "recipeId": "recipe-uuid-456",
      "quantity": 2,
      "customizations": {
        "spiceLevel": "extra_spicy",
        "notes": "No onions please"
      }
    }
  ],
  "deliveryAddress": {
    "line1": "15 Rue de la Paix",
    "city": "Paris",
    "postalCode": "75001",
    "country": "FR",
    "latitude": 48.8698,
    "longitude": 2.3309
  },
  "deliveryNotes": "Code: 4521",
  "paymentMethod": "stripe",
  "paymentIntentId": "pi_3Pmk5c2eZvKYlo2C1s4AH",
  "promoCode": "WELCOME20",
  "tipAmount": 2.00
}
```

**Response 201:**
```json
{
  "success": true,
  "data": {
    "orderId": "order-uuid-789",
    "orderNumber": "CRX-2026-004521",
    "status": "PENDING",
    "estimatedDelivery": "2026-03-30T15:30:00Z",
    "pricing": {
      "subtotal": 37.00,
      "deliveryFee": 2.90,
      "serviceFee": 1.50,
      "discount": 7.40,
      "tipAmount": 2.00,
      "taxAmount": 3.41,
      "total": 39.41,
      "currency": "EUR"
    },
    "paymentStatus": "AUTHORIZED",
    "trackingUrl": "https://cerex.com/orders/order-uuid-789/track"
  }
}
```

---

### GET /orders

Get user's order history.

**Query Params:** `?page=0&size=10&status=DELIVERED&sort=created_at,desc`

**Response 200:** _(paginated order list)_

---

### GET /orders/{id}

Get order details.

**Response 200:** _(full order with items, tracking, timeline)_

---

### PATCH /orders/{id}/cancel

Cancel an order (only if status is PENDING or CONFIRMED).

**Request:**
```json
{ "reason": "Changed my mind" }
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "status": "CANCELLED",
    "refundAmount": 39.41,
    "refundEstimatedAt": "2026-04-02T00:00:00Z"
  }
}
```

---

### GET /orders/{id}/track

Get real-time order tracking.

**Response 200:**
```json
{
  "success": true,
  "data": {
    "orderId": "order-uuid-789",
    "status": "OUT_FOR_DELIVERY",
    "statusTimeline": [
      { "status": "PENDING",           "timestamp": "2026-03-30T14:30:00Z" },
      { "status": "CONFIRMED",         "timestamp": "2026-03-30T14:32:00Z" },
      { "status": "PREPARING",         "timestamp": "2026-03-30T14:35:00Z" },
      { "status": "OUT_FOR_DELIVERY",  "timestamp": "2026-03-30T15:00:00Z" }
    ],
    "driver": {
      "name": "Moussa K.",
      "phone": "+33 6 XX XX XX 00",
      "avatarUrl": "...",
      "currentLocation": { "lat": 48.8610, "lng": 2.3340 }
    },
    "estimatedArrival": "2026-03-30T15:25:00Z",
    "deliveryAddress": { "lat": 48.8698, "lng": 2.3309 }
  }
}
```

---

## 7. Social Features Endpoints

### POST /social/posts

Create a social post.

**Request:**
```json
{
  "postType": "RECIPE_SHARE",
  "content": {
    "text": "Just tried making Poulet Yassa for the first time and it was amazing! 🇸🇳",
    "recipeId": "recipe-uuid-456",
    "mediaUrls": ["https://media.cerex.com/temp/upload_123.jpg"],
    "location": { "city": "Paris", "country": "FR" }
  },
  "tags": ["#WestAfricanFood", "#PouletYassa", "#homecooking"],
  "visibility": "PUBLIC"
}
```

**Response 201:** _(created post)_

---

### GET /social/feed

Get personalized social feed.

**Query Params:** `?page=0&size=20&feedType=FOLLOWING`

**Response 200:** _(paginated social posts)_

---

### POST /social/follows/{userId}

Follow a user.

**Response 200:**
```json
{
  "success": true,
  "data": { "following": true, "followersCount": 12484 }
}
```

---

### DELETE /social/follows/{userId}

Unfollow a user.

**Response 200:**
```json
{
  "success": true,
  "data": { "following": false, "followersCount": 12483 }
}
```

---

### POST /social/posts/{postId}/comments

Add a comment to a post.

**Request:**
```json
{
  "text": "This looks incredible! Did you use the whole chicken or just thighs?",
  "parentCommentId": null
}
```

**Response 201:** _(created comment)_

---

### GET /social/posts/{postId}/comments

Get comments for a post.

**Query Params:** `?page=0&size=20&sort=createdAt,desc`

**Response 200:** _(paginated comments with nested replies)_

---

### POST /reviews

Submit a review for a recipe or restaurant.

**Request:**
```json
{
  "entityType": "RECIPE",
  "entityId": "recipe-uuid-456",
  "orderId": "order-uuid-789",
  "overallRating": 5,
  "tasteRating": 5,
  "presentationRating": 4,
  "title": "Absolutely authentic!",
  "content": "This Yassa recipe is the closest I've had to what I tasted in Dakar...",
  "tags": ["authentic", "perfect-spice", "generous-portion"],
  "mediaUrls": []
}
```

**Response 201:** _(created review)_

---

## 8. AI Endpoints

### POST /ai/recipes/generate

Generate a recipe using AI.

**Request:**
```json
{
  "cuisineType": "Japanese",
  "mealType": "DINNER",
  "servings": 4,
  "difficultyTarget": "MEDIUM",
  "availableIngredients": ["salmon", "avocado", "soy sauce", "ginger", "sesame"],
  "dietaryRestrictions": ["gluten_free"],
  "maxTimeMinutes": 30,
  "spiceLevelPreference": 2,
  "additionalPrompt": "Make it colorful and visually appealing for a dinner party"
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "requestId": "ai-req-uuid-123",
    "status": "COMPLETED",
    "generatedRecipe": {
      "title": "Glazed Salmon with Avocado Sesame Rice",
      "description": "A beautiful Japanese-inspired dish combining silky glazed salmon...",
      "ingredients": [...],
      "steps": [...],
      "estimatedNutrition": { "calories": 520, "protein": 38, "carbs": 45, "fat": 18 },
      "totalTimeMinutes": 28,
      "difficultyLevel": "MEDIUM"
    },
    "modelUsed": "gpt-4o",
    "generationTimeMs": 2841,
    "saveUrl": "/ai/recipes/ai-req-uuid-123/save"
  }
}
```

---

### POST /ai/recipes/{requestId}/save

Save an AI-generated recipe to your profile.

**Response 201:** _(saved recipe with ID)_

---

### GET /ai/recommendations

Get personalized recipe recommendations.

**Query Params:** `?algorithm=hybrid&limit=10&context=homepage`

**Response 200:**
```json
{
  "success": true,
  "data": {
    "algorithm": "hybrid",
    "recommendations": [
      {
        "recipe": { ... },
        "score": 0.94,
        "reason": "Based on your love of West African cuisine",
        "reasonCode": "CUISINE_AFFINITY"
      }
    ],
    "sessionId": "sess-rec-uuid-456"
  }
}
```

---

### POST /ai/translate

Translate a recipe to another language.

**Request:**
```json
{
  "recipeId": "recipe-uuid-456",
  "targetLanguage": "fr",
  "fields": ["title", "description", "steps"]
}
```

**Response 200:** _(translated content)_

---

### GET /ai/trends

Get culinary trend analysis.

**Query Params:** `?continent=EU&period=month&limit=10`

**Response 200:**
```json
{
  "success": true,
  "data": {
    "period": "2026-03",
    "continent": "EU",
    "trends": [
      {
        "topic": "Korean Fusion",
        "trendScore": 0.89,
        "growthRate": 0.34,
        "topRecipes": [...],
        "keyIngredients": ["gochujang", "kimchi", "doenjang"]
      }
    ]
  }
}
```

---

## 9. Media Endpoints

### POST /media/upload

Upload media (image or video).

**Request:** `multipart/form-data`
```
file:       <binary>
entityType: RECIPE
entityId:   recipe-uuid-456
mediaType:  IMAGE
altText:    "Finished Poulet Yassa dish"
```

**Response 201:**
```json
{
  "success": true,
  "data": {
    "mediaId": "media-uuid-789",
    "status": "PROCESSING",
    "originalUrl": "https://media.cerex.com/temp/upload_123.jpg",
    "estimatedProcessingTime": "30 seconds"
  }
}
```

---

### GET /media/{mediaId}

Get media asset details.

**Response 200:**
```json
{
  "success": true,
  "data": {
    "mediaId": "media-uuid-789",
    "status": "READY",
    "variants": {
      "original": "https://media.cerex.com/recipes/uuid/original.jpg",
      "hero": "https://media.cerex.com/recipes/uuid/hero.webp",
      "card": "https://media.cerex.com/recipes/uuid/card.webp",
      "thumb": "https://media.cerex.com/recipes/uuid/thumb.webp"
    },
    "width": 1200,
    "height": 800,
    "sizeBytes": 245000,
    "mimeType": "image/webp"
  }
}
```

---

## 10. Admin Endpoints

All admin endpoints require `ADMIN` or `SUPER_ADMIN` role.

### GET /admin/recipes/pending

Get recipes pending moderation review.

**Response 200:** _(paginated recipe list with moderation status)_

---

### PATCH /admin/recipes/{id}/moderate

Approve or reject a recipe.

**Request:**
```json
{
  "decision": "APPROVED",
  "note": "Recipe meets all quality standards"
}
```

**Response 200:** _(moderation result)_

---

### GET /admin/users

List all users with admin filter options.

**Query Params:** `?status=ACTIVE&role=CHEF&page=0&size=50`

**Response 200:** _(paginated user list with admin fields)_

---

### PATCH /admin/users/{id}/status

Update user account status.

**Request:**
```json
{
  "status": "SUSPENDED",
  "reason": "Violation of community guidelines - spam",
  "durationHours": 72
}
```

**Response 200:** _(updated user status)_

---

### GET /admin/analytics/dashboard

Get platform-wide analytics.

**Query Params:** `?period=day&date=2026-03-30`

**Response 200:**
```json
{
  "success": true,
  "data": {
    "period": "2026-03-30",
    "users": {
      "totalActive": 2340123,
      "newRegistrations": 4521,
      "dailyActiveUsers": 342000
    },
    "recipes": {
      "totalPublished": 234521,
      "publishedToday": 234,
      "totalViews": 12340000
    },
    "orders": {
      "totalToday": 23451,
      "revenueToday": 423190.50,
      "averageOrderValue": 18.04
    },
    "topContinents": [...],
    "topRecipes": [...]
  }
}
```

---

## 11. Error Response Standards

### Error Response Format

```json
{
  "success": false,
  "error": {
    "code": "RECIPE_NOT_FOUND",
    "message": "The requested recipe could not be found.",
    "details": null,
    "requestId": "req_01JA2X3Y4Z",
    "timestamp": "2026-03-30T14:22:33.123Z",
    "path": "/api/v1/recipes/invalid-id",
    "documentation": "https://docs.cerex.com/errors/RECIPE_NOT_FOUND"
  }
}
```

### HTTP Status Code Map

| Status | Code | Usage |
|--------|------|-------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST |
| 202 | Accepted | Async operation queued |
| 204 | No Content | Successful DELETE |
| 400 | BAD_REQUEST | Malformed request |
| 401 | UNAUTHORIZED | Missing or invalid authentication |
| 402 | PAYMENT_REQUIRED | Payment processing failed |
| 403 | FORBIDDEN | Insufficient permissions |
| 404 | NOT_FOUND | Resource not found |
| 409 | CONFLICT | Resource already exists |
| 422 | VALIDATION_ERROR | Request validation failed |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests |
| 500 | INTERNAL_ERROR | Server error |
| 503 | SERVICE_UNAVAILABLE | Dependency down |

### Domain Error Codes

```
AUTH_001    INVALID_CREDENTIALS
AUTH_002    ACCOUNT_LOCKED
AUTH_003    EMAIL_NOT_VERIFIED
AUTH_004    TOKEN_EXPIRED
AUTH_005    TOKEN_INVALID
AUTH_006    MFA_REQUIRED

RECIPE_001  RECIPE_NOT_FOUND
RECIPE_002  RECIPE_NOT_PUBLISHED
RECIPE_003  RECIPE_ALREADY_EXISTS
RECIPE_004  INSUFFICIENT_PERMISSIONS
RECIPE_005  RECIPE_MODERATION_REJECTED

ORDER_001   ORDER_NOT_FOUND
ORDER_002   RESTAURANT_CLOSED
ORDER_003   DELIVERY_NOT_AVAILABLE
ORDER_004   MINIMUM_ORDER_NOT_MET
ORDER_005   PAYMENT_FAILED
ORDER_006   ORDER_ALREADY_CANCELLED
ORDER_007   CANCELLATION_WINDOW_EXPIRED

AI_001      GENERATION_FAILED
AI_002      QUOTA_EXCEEDED
AI_003      CONTENT_POLICY_VIOLATION
```

---

## 12. Rate Limiting Strategy

### Rate Limit Headers

All responses include:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 85
X-RateLimit-Reset: 1711807200
X-RateLimit-Policy: "100;w=60;comment=per-minute-user"
Retry-After: 15
```

### Rate Limits by Tier

| Tier | Endpoint Type | Limit |
|------|-------------|-------|
| Anonymous | Public GET | 30 req/min |
| Free User | All endpoints | 100 req/min |
| Explorer | All endpoints | 300 req/min |
| Chef Pro | All endpoints | 1000 req/min |
| Enterprise | All endpoints | Custom |
| Admin | All endpoints | 5000 req/min |

### Special Limits

| Endpoint | Limit | Window |
|----------|-------|--------|
| POST /auth/login | 5 attempts | 15 min |
| POST /auth/forgot-password | 3 attempts | 1 hour |
| POST /orders | 10 orders | 1 hour |
| POST /ai/recipes/generate | 5 generations | 1 hour (free) |
| POST /media/upload | 20 uploads | 1 hour |

---

## 13. Versioning Strategy

### URL Versioning

```
/api/v1/recipes    — Current stable version
/api/v2/recipes    — New version (when breaking changes occur)
/api/beta/recipes  — Beta features
```

### Deprecation Policy

1. New version announced 6 months before old version deprecation
2. Old version maintained for 12 months post-announcement
3. Deprecation headers returned:
   ```
   Deprecation: true
   Sunset: Sat, 30 Mar 2027 00:00:00 GMT
   Link: <https://docs.cerex.com/migration/v1-to-v2>; rel="successor-version"
   ```

### Backward Compatibility Rules

- New optional fields may be added without version bump
- Field removal or type changes require new version
- Endpoint removal requires new version
- New required fields require new version

---

*Document Version: 1.0.0 | Next Review: 2026-06-30 | Owner: Cerex API Team*

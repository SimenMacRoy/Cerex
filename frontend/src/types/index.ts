// ═══════════════════════════════════════════════════════════
// CEREX - TypeScript Type Definitions
// ═══════════════════════════════════════════════════════════

// ─── Auth ─────────────────────────────────────────────────
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  preferredLanguage?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserProfile;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

// ─── User ─────────────────────────────────────────────────
export type UserRole = 'USER' | 'CHEF' | 'RESTAURANT_OWNER' | 'MODERATOR' | 'ADMIN' | 'SUPER_ADMIN';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'BANNED';

export interface UserProfile {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  username?: string;
  avatarUrl?: string;
  bio?: string;
  role: UserRole;
  status: UserStatus;
  preferredLanguage: string;
  followersCount: number;
  followingCount: number;
  recipesCount: number;
  ecoBadges: EcoBadge[];
  createdAt: string;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  username?: string;
  bio?: string;
  avatarUrl?: string;
  preferredLanguage?: string;
}

// ─── Recipe ───────────────────────────────────────────────
export type DifficultyLevel = 'EASY' | 'MEDIUM' | 'HARD' | 'EXPERT';
export type RecipeStatus = 'DRAFT' | 'PENDING_REVIEW' | 'PUBLISHED' | 'ARCHIVED' | 'REJECTED';
export type RecipeType = 'APPETIZER' | 'MAIN_COURSE' | 'DESSERT' | 'SOUP' | 'SALAD' | 'SNACK' | 'BEVERAGE' | 'BREAKFAST' | 'SIDE_DISH';

export interface RecipeCard {
  id: string;
  title: string;
  slug: string;
  description: string;
  coverImageUrl?: string;
  authorId?: string;
  authorName: string;
  authorAvatarUrl?: string;
  cuisineType?: string;
  courseType?: string;
  difficultyLevel: DifficultyLevel;
  spiceLevel?: number;
  prepTimeMinutes: number;
  cookTimeMinutes: number;
  totalTimeMinutes?: number;
  servings: number;
  avgRating: number;
  ratingCount: number;
  likeCount: number;
  commentCount?: number;
  viewCount?: number;
  isVegan?: boolean;
  isVegetarian?: boolean;
  isGlutenFree?: boolean;
  isHalal?: boolean;
  isPremium?: boolean;
  isFeatured?: boolean;
  publishedAt?: string;
  // UI state (not from API)
  isLiked?: boolean;
  isSaved?: boolean;
}

export interface RecipeDetail {
  id: string;
  title: string;
  slug: string;
  description: string;
  author?: { id: string; displayName: string; avatarUrl?: string };
  recipeType?: string;
  cuisineType?: string;
  courseType?: string;
  difficultyLevel: DifficultyLevel;
  spiceLevel?: number;
  prepTimeMinutes: number;
  cookTimeMinutes: number;
  restTimeMinutes?: number;
  totalTimeMinutes?: number;
  servings: number;
  servingsUnit?: string;
  nutrition?: NutritionalInfo;
  isVegetarian?: boolean;
  isVegan?: boolean;
  isGlutenFree?: boolean;
  isDairyFree?: boolean;
  isHalal?: boolean;
  isKosher?: boolean;
  isNutFree?: boolean;
  isLowCarb?: boolean;
  ingredients?: RecipeIngredient[];
  steps?: RecipeStep[];
  viewCount?: number;
  likeCount: number;
  saveCount?: number;
  avgRating: number;
  ratingCount: number;
  commentCount?: number;
  isPremium?: boolean;
  isFeatured?: boolean;
  status: RecipeStatus;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface RecipeIngredient {
  id: string;
  name: string;
  quantity: number;
  unit: string;
  isOptional: boolean;
  substituteFor?: string;
}

export interface RecipeStep {
  stepNumber: number;
  title: string;
  description: string;
  imageUrl?: string;
  durationMinutes?: number;
  tips?: string;
}

export interface NutritionalInfo {
  calories: number;
  protein: number;
  carbohydrates: number;
  fat: number;
  fiber: number;
  sugar: number;
  sodium: number;
}

export interface DietaryFlags {
  vegetarian: boolean;
  vegan: boolean;
  glutenFree: boolean;
  dairyFree: boolean;
  nutFree: boolean;
  halal: boolean;
  kosher: boolean;
}

export interface CreateRecipeRequest {
  title: string;
  description: string;
  recipeType: RecipeType;
  cuisineOrigin: string;
  difficulty: DifficultyLevel;
  prepTimeMinutes: number;
  cookTimeMinutes: number;
  servings: number;
  ingredients: Omit<RecipeIngredient, 'id'>[];
  steps: Omit<RecipeStep, 'stepNumber'>[];
  dietaryFlags: DietaryFlags;
  tags: string[];
  imageUrl?: string;
}

// ─── Order ────────────────────────────────────────────────
export type OrderType = 'DELIVERY' | 'PICKUP' | 'DINE_IN' | 'INGREDIENT_KIT' | 'CATERING';
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED' | 'REFUNDED';

export interface Order {
  id: string;
  orderNumber: string;
  orderType: OrderType;
  status: OrderStatus;
  items: OrderItem[];
  subtotal: number;
  deliveryFee: number;
  tax: number;
  total: number;
  deliveryAddress?: string;
  restaurantName: string;
  restaurantId: string;
  estimatedDeliveryTime?: string;
  paymentMethod: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id: string;
  name: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  notes?: string;
  imageUrl?: string;
}

export interface CreateOrderRequest {
  restaurantId: string;
  orderType: OrderType;
  items: { menuItemId: string; quantity: number; notes?: string }[];
  deliveryAddress?: string;
  paymentMethod: string;
}

export interface OrderTrackingUpdate {
  orderId: string;
  status: OrderStatus;
  message: string;
  estimatedMinutes?: number;
  location?: { lat: number; lng: number };
  timestamp: string;
}

// ─── Restaurant ───────────────────────────────────────────
export type RestaurantStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
export type CuisineType = 'FRENCH' | 'ITALIAN' | 'JAPANESE' | 'CHINESE' | 'INDIAN' | 'MEXICAN' | 'THAI' | 'AMERICAN' | 'MEDITERRANEAN' | 'AFRICAN' | 'KOREAN' | 'VIETNAMESE' | 'MIDDLE_EASTERN' | 'FUSION' | 'OTHER';

export interface Restaurant {
  id: string;
  name: string;
  description: string;
  imageUrl: string;
  coverImageUrl?: string;
  cuisineType: CuisineType;
  address: string;
  city: string;
  country: string;
  latitude: number;
  longitude: number;
  phone: string;
  rating: number;
  reviewsCount: number;
  deliveryTimeMinutes: number;
  minimumOrder: number;
  deliveryFee: number;
  isOpen: boolean;
  openingHours: string;
  status: RestaurantStatus;
}

export interface Menu {
  id: string;
  name: string;
  description?: string;
  items: MenuItem[];
}

export interface MenuItem {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl?: string;
  category: string;
  isAvailable: boolean;
  isPopular: boolean;
  dietaryFlags?: Partial<DietaryFlags>;
  calories?: number;
}

export interface RestaurantReview {
  id: string;
  userId: string;
  userName: string;
  userAvatarUrl?: string;
  rating: number;
  comment: string;
  createdAt: string;
}

// ─── Grocery ──────────────────────────────────────────────
export interface Grocery {
  id: string;
  name: string;
  description: string;
  imageUrl: string;
  address: string;
  city: string;
  rating: number;
  deliveryTimeMinutes: number;
  isOpen: boolean;
}

export interface GroceryProduct {
  id: string;
  name: string;
  description: string;
  price: number;
  unit: string;
  imageUrl?: string;
  category: string;
  isOrganic: boolean;
  isLocal: boolean;
  isSeasonal: boolean;
  ecoScore?: number;
  inStock: boolean;
}

// ─── Social ───────────────────────────────────────────────
export type PostType = 'RECIPE_SHARE' | 'COOKING_TIP' | 'FOOD_PHOTO' | 'REPRODUCE' | 'REVIEW' | 'QUESTION';
export type PostStatus = 'ACTIVE' | 'HIDDEN' | 'DELETED';

export interface Post {
  id: string;
  authorId: string;
  authorName: string;
  authorAvatarUrl?: string;
  postType: PostType;
  content: string;
  imageUrls: string[];
  recipeId?: string;
  recipeName?: string;
  likesCount: number;
  commentsCount: number;
  sharesCount: number;
  isLiked: boolean;
  createdAt: string;
}

export interface Comment {
  id: string;
  authorId: string;
  authorName: string;
  authorAvatarUrl?: string;
  content: string;
  likesCount: number;
  isLiked: boolean;
  createdAt: string;
}

export interface CreatePostRequest {
  postType: PostType;
  content: string;
  imageUrls?: string[];
  recipeId?: string;
}

export type NotificationType = 'LIKE' | 'COMMENT' | 'FOLLOW' | 'SHARE' | 'ORDER_UPDATE' | 'RECIPE_APPROVED' | 'BADGE_EARNED' | 'SYSTEM';

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  fromUserId?: string;
  fromUserName?: string;
  fromUserAvatarUrl?: string;
  referenceId?: string;
  referenceType?: string;
  isRead: boolean;
  createdAt: string;
}

// ─── Eco ──────────────────────────────────────────────────
export interface EcoBadge {
  id: string;
  name: string;
  description: string;
  iconUrl: string;
  level: number;
  earnedAt: string;
}

export interface EcoScore {
  score: number;
  grade: 'A' | 'B' | 'C' | 'D' | 'E';
  carbonFootprint: number;
  seasonalPercentage: number;
  localPercentage: number;
  organicPercentage: number;
  tips: string[];
}

export interface WasteReductionTip {
  id: string;
  title: string;
  description: string;
  category: string;
  impactLevel: 'LOW' | 'MEDIUM' | 'HIGH';
}

// ─── AI ───────────────────────────────────────────────────
export interface AIRecommendation {
  recipes: RecipeCard[];
  reason: string;
  confidence: number;
}

export interface AIMealPlan {
  id: string;
  name: string;
  days: {
    day: string;
    meals: {
      type: 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK';
      recipe: RecipeCard;
    }[];
  }[];
  totalCalories: number;
  ecoScore: number;
}

// ─── API Response ─────────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
}

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface PaginatedResponse<T> {
  success: boolean;
  data: SpringPage<T>;
  pagination?: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
  timestamp: string;
}

// ─── Cart (frontend-only) ─────────────────────────────────
export interface CartItem {
  menuItem: MenuItem;
  quantity: number;
  notes?: string;
  restaurantId: string;
  restaurantName: string;
}

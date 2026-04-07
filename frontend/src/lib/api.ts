import apiClient from './apiClient';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  UserProfile,
  UpdateProfileRequest,
  RecipeCard,
  RecipeDetail,
  CreateRecipeRequest,
  Order,
  CreateOrderRequest,
  Restaurant,
  Menu,
  MenuItem,
  RestaurantReview,
  Grocery,
  GroceryProduct,
  Post,
  Comment,
  CreatePostRequest,
  Notification,
  EcoScore,
  WasteReductionTip,
  AIRecommendation,
  AIMealPlan,
  ApiResponse,
  PaginatedResponse,
} from '@/types';

// ═══════════════════════════════════════════════════════════
// AUTH API
// ═══════════════════════════════════════════════════════════
export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<ApiResponse<AuthResponse>>('/auth/login', data),

  register: (data: RegisterRequest) =>
    apiClient.post<ApiResponse<AuthResponse>>('/auth/register', data),

  refresh: (refreshToken: string) =>
    apiClient.post<ApiResponse<AuthResponse>>('/auth/refresh', { refreshToken }),

  logout: () =>
    apiClient.post('/auth/logout'),
};

// ═══════════════════════════════════════════════════════════
// USER API
// ═══════════════════════════════════════════════════════════
export const userApi = {
  getProfile: () =>
    apiClient.get<ApiResponse<UserProfile>>('/users/me'),

  updateProfile: (data: UpdateProfileRequest) =>
    apiClient.put<ApiResponse<UserProfile>>('/users/me', data),

  getUserById: (id: string) =>
    apiClient.get<ApiResponse<UserProfile>>(`/users/${id}`),

  getFollowers: (userId: string, page = 0) =>
    apiClient.get<PaginatedResponse<UserProfile>>(`/users/${userId}/followers`, { params: { page } }),

  getFollowing: (userId: string, page = 0) =>
    apiClient.get<PaginatedResponse<UserProfile>>(`/users/${userId}/following`, { params: { page } }),
};

// ═══════════════════════════════════════════════════════════
// RECIPE API
// ═══════════════════════════════════════════════════════════
export const recipeApi = {
  getAll: (params?: { page?: number; size?: number; sort?: string; cuisine?: string; difficulty?: string; diet?: string; q?: string }) =>
    apiClient.get<PaginatedResponse<RecipeCard>>('/recipes', { params }),

  getById: (id: string) =>
    apiClient.get<ApiResponse<RecipeDetail>>(`/recipes/${id}`),

  create: (data: CreateRecipeRequest) =>
    apiClient.post<ApiResponse<RecipeDetail>>('/recipes', data),

  update: (id: string, data: Partial<CreateRecipeRequest>) =>
    apiClient.put<ApiResponse<RecipeDetail>>(`/recipes/${id}`, data),

  delete: (id: string) =>
    apiClient.delete(`/recipes/${id}`),

  like: (id: string, liked: boolean) =>
    apiClient.post(`/recipes/${id}/like`, null, { params: { liked } }),

  getGroceryList: (id: string, servings: number) =>
    apiClient.get<ApiResponse<any>>(`/recipes/${id}/grocery-list`, { params: { servings } }),

  save: (id: string) =>
    apiClient.post(`/recipes/${id}/save`),

  search: (query: string, params?: { page?: number; size?: number }) =>
    apiClient.get<PaginatedResponse<RecipeCard>>('/recipes/search', { params: { q: query, ...params } }),

  getTrending: (page = 0) =>
    apiClient.get<PaginatedResponse<RecipeCard>>('/recipes/trending', { params: { page } }),

  getByUser: (userId: string, page = 0) =>
    apiClient.get<PaginatedResponse<RecipeCard>>(`/recipes/user/${userId}`, { params: { page } }),

  getMyRecipes: (page = 0) =>
    apiClient.get<PaginatedResponse<RecipeCard>>('/recipes/me', { params: { page } }),

  approve: (id: string) =>
    apiClient.post<ApiResponse<RecipeDetail>>(`/recipes/${id}/approve`),

  reject: (id: string, reason: string) =>
    apiClient.post<ApiResponse<RecipeDetail>>(`/recipes/${id}/reject`, null, { params: { reason } }),

  publish: (id: string) =>
    apiClient.post<ApiResponse<RecipeDetail>>(`/recipes/${id}/publish`),
};

// ═══════════════════════════════════════════════════════════
// ORDER API
// ═══════════════════════════════════════════════════════════
export const orderApi = {
  create: (data: CreateOrderRequest) =>
    apiClient.post<ApiResponse<Order>>('/orders', data),

  getMyOrders: (page = 0) =>
    apiClient.get<PaginatedResponse<Order>>('/orders/my', { params: { page } }),

  getById: (id: string) =>
    apiClient.get<ApiResponse<Order>>(`/orders/${id}`),

  cancel: (id: string) =>
    apiClient.post(`/orders/${id}/cancel`),
};

// ═══════════════════════════════════════════════════════════
// RESTAURANT API
// ═══════════════════════════════════════════════════════════
export const restaurantApi = {
  getAll: (params?: { page?: number; cuisine?: string; city?: string; sort?: string }) =>
    apiClient.get<PaginatedResponse<Restaurant>>('/restaurants', { params }),

  getById: (id: string) =>
    apiClient.get<ApiResponse<Restaurant>>(`/restaurants/${id}`),

  getMenu: (id: string) =>
    apiClient.get<ApiResponse<Menu[]>>(`/restaurants/${id}/menu`),

  getMenuItem: (restaurantId: string, itemId: string) =>
    apiClient.get<ApiResponse<MenuItem>>(`/restaurants/${restaurantId}/menu/items/${itemId}`),

  getReviews: (id: string, page = 0) =>
    apiClient.get<PaginatedResponse<RestaurantReview>>(`/restaurants/${id}/reviews`, { params: { page } }),

  addReview: (id: string, data: { rating: number; comment: string }) =>
    apiClient.post(`/restaurants/${id}/reviews`, data),

  search: (query: string, params?: { page?: number }) =>
    apiClient.get<PaginatedResponse<Restaurant>>('/restaurants/search', { params: { q: query, ...params } }),

  getNearby: (lat: number, lng: number, radiusKm = 10) =>
    apiClient.get<PaginatedResponse<Restaurant>>('/restaurants/nearby', { params: { lat, lng, radius: radiusKm } }),
};

// ═══════════════════════════════════════════════════════════
// GROCERY API
// ═══════════════════════════════════════════════════════════
export const groceryApi = {
  getAll: (params?: { page?: number; city?: string }) =>
    apiClient.get<PaginatedResponse<Grocery>>('/groceries', { params }),

  getById: (id: string) =>
    apiClient.get<ApiResponse<Grocery>>(`/groceries/${id}`),

  getProducts: (groceryId: string, params?: { page?: number; category?: string }) =>
    apiClient.get<PaginatedResponse<GroceryProduct>>(`/groceries/${groceryId}/products`, { params }),

  searchProducts: (query: string) =>
    apiClient.get<PaginatedResponse<GroceryProduct>>('/groceries/products/search', { params: { q: query } }),
};

// ═══════════════════════════════════════════════════════════
// SOCIAL API
// ═══════════════════════════════════════════════════════════
export const socialApi = {
  getFeed: (page = 0) =>
    apiClient.get<PaginatedResponse<Post>>('/social/feed', { params: { page } }),

  createPost: (data: CreatePostRequest) =>
    apiClient.post<ApiResponse<Post>>('/social/posts', data),

  likePost: (postId: string) =>
    apiClient.post(`/social/posts/${postId}/like`),

  getComments: (postId: string, page = 0) =>
    apiClient.get<PaginatedResponse<Comment>>(`/social/posts/${postId}/comments`, { params: { page } }),

  addComment: (postId: string, content: string) =>
    apiClient.post<ApiResponse<Comment>>(`/social/posts/${postId}/comments`, { content }),

  sharePost: (postId: string) =>
    apiClient.post(`/social/posts/${postId}/share`),

  followUser: (userId: string) =>
    apiClient.post(`/social/follow/${userId}`),

  unfollowUser: (userId: string) =>
    apiClient.delete(`/social/follow/${userId}`),

  getNotifications: (page = 0) =>
    apiClient.get<PaginatedResponse<Notification>>('/social/notifications', { params: { page } }),

  markNotificationRead: (id: string) =>
    apiClient.put(`/social/notifications/${id}/read`),

  markAllNotificationsRead: () =>
    apiClient.put('/social/notifications/read-all'),
};

// ═══════════════════════════════════════════════════════════
// ECO API
// ═══════════════════════════════════════════════════════════
export const ecoApi = {
  getRecipeEcoScore: (recipeId: string) =>
    apiClient.get<ApiResponse<EcoScore>>(`/eco/recipes/${recipeId}/score`),

  getWasteTips: () =>
    apiClient.get<ApiResponse<WasteReductionTip[]>>('/eco/waste-tips'),

  getUserEcoStats: () =>
    apiClient.get<ApiResponse<{ totalScore: number; badges: number; carbonSaved: number }>>('/eco/stats'),
};

// ═══════════════════════════════════════════════════════════
// AI API
// ═══════════════════════════════════════════════════════════
export const aiApi = {
  getRecommendations: () =>
    apiClient.get<ApiResponse<AIRecommendation>>('/ai/recommendations'),

  getMealPlan: (params: { days: number; dietary?: string; calories?: number }) =>
    apiClient.get<ApiResponse<AIMealPlan>>('/ai/meal-plan', { params }),

  analyzeImage: (imageUrl: string) =>
    apiClient.post<ApiResponse<{ ingredients: string[]; suggestedRecipes: RecipeCard[] }>>('/ai/analyze-image', { imageUrl }),

  generateRecipe: (params: {
    ingredients: string[];
    cuisine?: string;
    dietaryRestrictions?: string;
    servings?: number;
    difficultyLevel?: string;
    language?: string;
  }) =>
    apiClient.post<ApiResponse<string>>('/ai/generate-recipe', null, { params: {
      ...params,
      ingredients: params.ingredients,
      language: params.language ?? 'fr',
    }}),
};

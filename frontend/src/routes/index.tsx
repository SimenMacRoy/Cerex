import { Routes, Route, Navigate } from 'react-router-dom';
import { Suspense, lazy } from 'react';
import { useAuthStore } from '@/stores/authStore';
import MainLayout from '@/components/layout/MainLayout';
import AuthLayout from '@/components/layout/AuthLayout';
import LoadingScreen from '@/components/ui/LoadingScreen';

// ─── Lazy-loaded pages ────────────────────────────────────
const HomePage = lazy(() => import('@/pages/HomePage'));
const LoginPage = lazy(() => import('@/pages/auth/LoginPage'));
const RegisterPage = lazy(() => import('@/pages/auth/RegisterPage'));
const RecipesPage = lazy(() => import('@/pages/recipes/RecipesPage'));
const RecipeDetailPage = lazy(() => import('@/pages/recipes/RecipeDetailPage'));
const CreateRecipePage = lazy(() => import('@/pages/recipes/CreateRecipePage'));
const RestaurantsPage = lazy(() => import('@/pages/restaurants/RestaurantsPage'));
const RestaurantDetailPage = lazy(() => import('@/pages/restaurants/RestaurantDetailPage'));
const GroceryPage = lazy(() => import('@/pages/grocery/GroceryPage'));
const CartPage = lazy(() => import('@/pages/orders/CartPage'));
const CheckoutPage = lazy(() => import('@/pages/orders/CheckoutPage'));
const OrderTrackingPage = lazy(() => import('@/pages/orders/OrderTrackingPage'));
const OrderHistoryPage = lazy(() => import('@/pages/orders/OrderHistoryPage'));
const FeedPage = lazy(() => import('@/pages/social/FeedPage'));
const ProfilePage = lazy(() => import('@/pages/social/ProfilePage'));
const NotificationsPage = lazy(() => import('@/pages/social/NotificationsPage'));
const SearchPage = lazy(() => import('@/pages/SearchPage'));
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage'));

// ─── Protected Route ──────────────────────────────────────
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

// ─── Guest Route (redirect if authenticated) ──────────────
function GuestRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  if (isAuthenticated) return <Navigate to="/" replace />;
  return <>{children}</>;
}

export default function AppRoutes() {
  return (
    <Suspense fallback={<LoadingScreen />}>
      <Routes>
        {/* Auth routes */}
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<GuestRoute><LoginPage /></GuestRoute>} />
          <Route path="/register" element={<GuestRoute><RegisterPage /></GuestRoute>} />
        </Route>

        {/* Main app routes */}
        <Route element={<MainLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/search" element={<SearchPage />} />
          
          {/* Recipes */}
          <Route path="/recipes" element={<RecipesPage />} />
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
          <Route path="/recipes/create" element={<ProtectedRoute><CreateRecipePage /></ProtectedRoute>} />

          {/* Restaurants */}
          <Route path="/restaurants" element={<RestaurantsPage />} />
          <Route path="/restaurants/:id" element={<RestaurantDetailPage />} />

          {/* Grocery */}
          <Route path="/grocery" element={<GroceryPage />} />

          {/* Orders */}
          <Route path="/cart" element={<CartPage />} />
          <Route path="/checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
          <Route path="/orders" element={<ProtectedRoute><OrderHistoryPage /></ProtectedRoute>} />
          <Route path="/orders/:id/tracking" element={<ProtectedRoute><OrderTrackingPage /></ProtectedRoute>} />

          {/* Social */}
          <Route path="/feed" element={<ProtectedRoute><FeedPage /></ProtectedRoute>} />
          <Route path="/profile/:id" element={<ProfilePage />} />
          <Route path="/notifications" element={<ProtectedRoute><NotificationsPage /></ProtectedRoute>} />

          {/* 404 */}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </Suspense>
  );
}

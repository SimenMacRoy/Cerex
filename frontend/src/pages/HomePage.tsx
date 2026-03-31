import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { FiArrowRight, FiTrendingUp, FiMapPin, FiFeather } from 'react-icons/fi';
import { recipeApi, restaurantApi } from '@/lib/api';
import RecipeCard from '@/components/recipe/RecipeCard';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

export default function HomePage() {
  const { t } = useTranslation();

  const { data: trending, isLoading: trendingLoading } = useQuery({
    queryKey: ['recipes', 'trending'],
    queryFn: () => recipeApi.getTrending(0).then((r) => r.data),
  });

  const { data: restaurants, isLoading: restaurantsLoading } = useQuery({
    queryKey: ['restaurants', 'nearby'],
    queryFn: () => restaurantApi.getAll({ page: 0 }).then((r) => r.data),
  });

  return (
    <div>
      {/* ─── Hero Section ────────────────────────────────── */}
      <section className="relative bg-gradient-to-br from-primary-500 via-primary-600 to-cerex-accent overflow-hidden">
        <div className="absolute inset-0 bg-black/20" />
        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 md:py-24">
          <div className="max-w-2xl">
            <h1 className="text-4xl md:text-6xl font-display font-bold text-white mb-6 leading-tight">
              {t('home.hero.title')}
            </h1>
            <p className="text-lg md:text-xl text-white/90 mb-8">
              {t('home.hero.subtitle')}
            </p>
            <div className="flex flex-wrap gap-4">
              <Link to="/recipes" className="btn-primary bg-white text-primary-600 hover:bg-gray-100 shadow-xl text-base">
                {t('home.hero.cta')}
                <FiArrowRight className="inline ml-2" />
              </Link>
              <Link to="/restaurants" className="btn-outline border-white text-white hover:bg-white/10 text-base">
                {t('home.hero.secondary')}
              </Link>
            </div>
          </div>
        </div>
        <div className="absolute -bottom-24 -right-24 w-96 h-96 rounded-full bg-white/5" />
        <div className="absolute top-12 right-12 w-32 h-32 rounded-full bg-white/5" />
      </section>

      {/* ─── Trending Recipes ─────────────────────────────── */}
      <section className="page-container py-12">
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-2">
            <FiTrendingUp className="w-6 h-6 text-primary-500" />
            <h2 className="section-title">{t('home.trending')}</h2>
          </div>
          <Link to="/recipes" className="text-primary-500 hover:text-primary-600 font-medium text-sm flex items-center gap-1">
            {t('common.viewAll')} <FiArrowRight className="w-4 h-4" />
          </Link>
        </div>
        {trendingLoading ? (
          <div className="flex justify-center py-12"><LoadingSpinner size="lg" /></div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {trending?.data?.content?.slice(0, 8).map((recipe) => (
              <RecipeCard key={recipe.id} recipe={recipe} />
            ))}
          </div>
        )}
        {!trendingLoading && !trending?.data?.content?.length && (
          <div className="text-center py-12 text-gray-500">
            <p className="text-lg">{t('common.noResults')}</p>
            <p className="text-sm mt-2">Discover recipes from around the world soon!</p>
          </div>
        )}
      </section>

      {/* ─── Restaurants Near You ──────────────────────────── */}
      <section className="bg-gray-50 dark:bg-cerex-medium/50">
        <div className="page-container py-12">
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-2">
              <FiMapPin className="w-6 h-6 text-secondary-500" />
              <h2 className="section-title">{t('home.nearYou')}</h2>
            </div>
            <Link to="/restaurants" className="text-primary-500 hover:text-primary-600 font-medium text-sm flex items-center gap-1">
              {t('common.viewAll')} <FiArrowRight className="w-4 h-4" />
            </Link>
          </div>
          {restaurantsLoading ? (
            <div className="flex justify-center py-12"><LoadingSpinner size="lg" /></div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {restaurants?.data?.content?.slice(0, 6).map((restaurant) => (
                <Link key={restaurant.id} to={`/restaurants/${restaurant.id}`} className="card p-0 group">
                  <div className="h-40 bg-gray-200 dark:bg-gray-700 overflow-hidden">
                    {restaurant.imageUrl && (
                      <img
                        src={restaurant.imageUrl}
                        alt={restaurant.name}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                      />
                    )}
                  </div>
                  <div className="p-4">
                    <h3 className="font-semibold text-lg mb-1">{restaurant.name}</h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mb-2">{restaurant.cuisineType} • {restaurant.city}</p>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-yellow-500 font-medium">★ {restaurant.rating?.toFixed(1)}</span>
                      <span className="text-gray-400">{restaurant.deliveryTimeMinutes} min</span>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* ─── Eco Section ──────────────────────────────────── */}
      <section className="page-container py-12">
        <div className="card bg-gradient-to-r from-green-50 to-emerald-50 dark:from-green-900/20 dark:to-emerald-900/20 border-green-200 dark:border-green-800 p-8">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center shrink-0">
              <FiFeather className="w-6 h-6 text-green-600 dark:text-green-400" />
            </div>
            <div>
              <h3 className="font-display text-xl font-bold text-green-800 dark:text-green-200 mb-2">
                {t('home.ecoTip')}
              </h3>
              <p className="text-green-700 dark:text-green-300">
                Choose seasonal and local ingredients to reduce your carbon footprint by up to 30%.
                Every recipe on Cerex includes an Eco Score to help you make sustainable choices! 🌍
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

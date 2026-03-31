import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { FiSearch } from 'react-icons/fi';
import { recipeApi, restaurantApi } from '@/lib/api';
import RecipeCard from '@/components/recipe/RecipeCard';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { Link } from 'react-router-dom';

export default function SearchPage() {
  const { t } = useTranslation();
  const [query, setQuery] = useState('');
  const [activeTab, setActiveTab] = useState<'recipes' | 'restaurants'>('recipes');

  const { data: recipes, isLoading: recipesLoading } = useQuery({
    queryKey: ['search-recipes', query],
    queryFn: () => recipeApi.search(query, { page: 0, size: 12 }).then((r) => r.data),
    enabled: query.length >= 2,
  });

  const { data: restaurants, isLoading: restaurantsLoading } = useQuery({
    queryKey: ['search-restaurants', query],
    queryFn: () => restaurantApi.search(query, { page: 0 }).then((r) => r.data),
    enabled: query.length >= 2 && activeTab === 'restaurants',
  });

  return (
    <div className="page-container max-w-4xl">
      {/* Search input */}
      <div className="relative mb-8">
        <FiSearch className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={t('nav.search')}
          className="w-full pl-12 pr-4 py-4 text-lg bg-white dark:bg-cerex-medium border border-gray-200 dark:border-gray-700 rounded-2xl focus:outline-none focus:ring-2 focus:ring-primary-500"
          autoFocus
        />
      </div>

      {/* Tabs */}
      <div className="flex gap-4 mb-6 border-b border-gray-200 dark:border-gray-700">
        {(['recipes', 'restaurants'] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`pb-2 text-sm font-medium border-b-2 transition-colors ${
              activeTab === tab
                ? 'border-primary-500 text-primary-500'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab === 'recipes' ? t('nav.recipes') : t('nav.restaurants')}
          </button>
        ))}
      </div>

      {/* Results */}
      {query.length < 2 ? (
        <div className="text-center py-16 text-gray-400">
          <FiSearch className="w-16 h-16 mx-auto mb-4" />
          <p>Start typing to search...</p>
        </div>
      ) : activeTab === 'recipes' ? (
        recipesLoading ? (
          <div className="flex justify-center py-12"><LoadingSpinner size="lg" /></div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {recipes?.data?.content?.map((recipe) => (
              <RecipeCard key={recipe.id} recipe={recipe} />
            ))}
            {!recipes?.data?.content?.length && (
              <div className="col-span-full text-center py-12 text-gray-500">{t('common.noResults')}</div>
            )}
          </div>
        )
      ) : (
        restaurantsLoading ? (
          <div className="flex justify-center py-12"><LoadingSpinner size="lg" /></div>
        ) : (
          <div className="space-y-4">
            {restaurants?.data?.content?.map((r) => (
              <Link key={r.id} to={`/restaurants/${r.id}`} className="card p-4 flex items-center gap-4 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                {r.imageUrl && <img src={r.imageUrl} alt={r.name} className="w-16 h-16 rounded-xl object-cover" />}
                <div>
                  <h3 className="font-medium">{r.name}</h3>
                  <p className="text-sm text-gray-500">{r.cuisineType} • {r.city} • ★ {r.rating?.toFixed(1)}</p>
                </div>
              </Link>
            ))}
            {!restaurants?.data?.content?.length && (
              <div className="text-center py-12 text-gray-500">{t('common.noResults')}</div>
            )}
          </div>
        )
      )}
    </div>
  );
}

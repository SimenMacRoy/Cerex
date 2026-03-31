import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import { FiMapPin, FiClock, FiStar } from 'react-icons/fi';
import { restaurantApi } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import { formatCurrency } from '@/lib/utils';

export default function RestaurantsPage() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [selectedCuisine, setSelectedCuisine] = useState<string>();

  const { data, isLoading } = useQuery({
    queryKey: ['restaurants', page, selectedCuisine],
    queryFn: () => restaurantApi.getAll({ page, cuisine: selectedCuisine }).then((r) => r.data),
  });

  const cuisines = ['FRENCH', 'ITALIAN', 'JAPANESE', 'CHINESE', 'INDIAN', 'MEXICAN', 'THAI', 'AFRICAN', 'KOREAN'];

  return (
    <div className="page-container">
      <h1 className="section-title mb-6">{t('nav.restaurants')}</h1>

      {/* Cuisine filters */}
      <div className="flex flex-wrap gap-2 mb-8">
        <button
          onClick={() => setSelectedCuisine(undefined)}
          className={`badge text-xs cursor-pointer ${!selectedCuisine ? 'bg-primary-500 text-white' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'}`}
        >
          All
        </button>
        {cuisines.map((c) => (
          <button
            key={c}
            onClick={() => setSelectedCuisine(selectedCuisine === c ? undefined : c)}
            className={`badge text-xs cursor-pointer ${selectedCuisine === c ? 'bg-primary-500 text-white' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'}`}
          >
            {c}
          </button>
        ))}
      </div>

      {isLoading ? (
        <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {data?.data?.content?.map((restaurant) => (
              <Link key={restaurant.id} to={`/restaurants/${restaurant.id}`} className="card group">
                <div className="h-44 bg-gray-200 dark:bg-gray-700 overflow-hidden">
                  {restaurant.imageUrl ? (
                    <img src={restaurant.imageUrl} alt={restaurant.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                  ) : (
                    <div className="flex items-center justify-center h-full text-4xl">🍽️</div>
                  )}
                  {restaurant.isOpen && <span className="absolute top-3 right-3 badge bg-green-100 text-green-700 text-xs">Open</span>}
                </div>
                <div className="p-4">
                  <h3 className="font-semibold text-lg mb-1 group-hover:text-primary-500 transition-colors">{restaurant.name}</h3>
                  <p className="text-sm text-gray-500 dark:text-gray-400 mb-3">{restaurant.cuisineType} cuisine</p>
                  <div className="flex items-center justify-between text-sm text-gray-500">
                    <span className="flex items-center gap-1"><FiStar className="w-4 h-4 text-yellow-400" />{restaurant.rating?.toFixed(1)}</span>
                    <span className="flex items-center gap-1"><FiClock className="w-4 h-4" />{restaurant.deliveryTimeMinutes} min</span>
                    <span className="flex items-center gap-1"><FiMapPin className="w-4 h-4" />{restaurant.city}</span>
                  </div>
                  {restaurant.minimumOrder > 0 && (
                    <p className="text-xs text-gray-400 mt-2">Min. {formatCurrency(restaurant.minimumOrder)}</p>
                  )}
                </div>
              </Link>
            ))}
          </div>

          {!data?.data?.content?.length && (
            <div className="text-center py-16 text-gray-500">{t('common.noResults')}</div>
          )}

          {data?.data && data.data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-8">
              <Button variant="ghost" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>{t('common.previous')}</Button>
              <span className="text-sm text-gray-500">{page + 1} / {data.data.totalPages}</span>
              <Button variant="ghost" disabled={data.data.last} onClick={() => setPage((p) => p + 1)}>{t('common.next')}</Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

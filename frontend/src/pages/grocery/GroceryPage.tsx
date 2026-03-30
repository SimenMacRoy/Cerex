import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { FiMapPin, FiClock } from 'react-icons/fi';
import { groceryApi } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

export default function GroceryPage() {
  const { t } = useTranslation();
  const { data, isLoading } = useQuery({
    queryKey: ['groceries'],
    queryFn: () => groceryApi.getAll({ page: 0 }).then((r) => r.data),
  });

  return (
    <div className="page-container">
      <h1 className="section-title mb-8">{t('nav.grocery')}</h1>
      {isLoading ? (
        <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {data?.data?.map((grocery) => (
            <div key={grocery.id} className="card group">
              <div className="h-40 bg-gray-200 dark:bg-gray-700 overflow-hidden">
                {grocery.imageUrl && <img src={grocery.imageUrl} alt={grocery.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />}
              </div>
              <div className="p-4">
                <h3 className="font-semibold text-lg mb-1">{grocery.name}</h3>
                <p className="text-sm text-gray-500 dark:text-gray-400 mb-3 line-clamp-2">{grocery.description}</p>
                <div className="flex items-center justify-between text-sm text-gray-500">
                  <span className="flex items-center gap-1"><FiMapPin className="w-4 h-4" />{grocery.city}</span>
                  <span className="flex items-center gap-1"><FiClock className="w-4 h-4" />{grocery.deliveryTimeMinutes} min</span>
                  <span className="text-yellow-500">★ {grocery.rating?.toFixed(1)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
      {!isLoading && !data?.data?.length && (
        <div className="text-center py-16 text-gray-500">{t('common.noResults')}</div>
      )}
    </div>
  );
}

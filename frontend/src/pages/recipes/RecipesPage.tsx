import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { FiFilter, FiGrid, FiList } from 'react-icons/fi';
import { recipeApi } from '@/lib/api';
import RecipeCard from '@/components/recipe/RecipeCard';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';

export default function RecipesPage() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [filters, setFilters] = useState<{ cuisine?: string; difficulty?: string }>({});

  const { data, isLoading } = useQuery({
    queryKey: ['recipes', page, filters],
    queryFn: () => recipeApi.getAll({ page, size: 12, ...filters }).then((r) => r.data),
  });

  const cuisines = ['FRENCH', 'ITALIAN', 'JAPANESE', 'CHINESE', 'INDIAN', 'MEXICAN', 'THAI', 'AFRICAN'];
  const difficulties = ['EASY', 'MEDIUM', 'HARD', 'EXPERT'];

  return (
    <div className="page-container">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <h1 className="section-title">{t('nav.recipes')}</h1>
        <div className="flex items-center gap-2">
          <button onClick={() => setViewMode('grid')} className={`p-2 rounded-lg ${viewMode === 'grid' ? 'bg-primary-100 text-primary-600' : 'text-gray-400'}`}>
            <FiGrid className="w-5 h-5" />
          </button>
          <button onClick={() => setViewMode('list')} className={`p-2 rounded-lg ${viewMode === 'list' ? 'bg-primary-100 text-primary-600' : 'text-gray-400'}`}>
            <FiList className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-2 mb-6">
        <div className="flex items-center gap-1 text-sm text-gray-500 mr-2">
          <FiFilter className="w-4 h-4" />
          {t('common.filter')}:
        </div>
        {cuisines.map((c) => (
          <button
            key={c}
            onClick={() => setFilters((f) => ({ ...f, cuisine: f.cuisine === c ? undefined : c }))}
            className={`badge text-xs cursor-pointer ${filters.cuisine === c ? 'bg-primary-500 text-white' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'}`}
          >
            {c}
          </button>
        ))}
        <span className="text-gray-300 dark:text-gray-600">|</span>
        {difficulties.map((d) => (
          <button
            key={d}
            onClick={() => setFilters((f) => ({ ...f, difficulty: f.difficulty === d ? undefined : d }))}
            className={`badge text-xs cursor-pointer ${filters.difficulty === d ? 'bg-secondary-500 text-white' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'}`}
          >
            {t(`recipe.${d.toLowerCase()}`)}
          </button>
        ))}
      </div>

      {/* Recipe Grid */}
      {isLoading ? (
        <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
      ) : (
        <>
          <div className={viewMode === 'grid'
            ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'
            : 'space-y-4'
          }>
            {data?.data?.map((recipe) => (
              <RecipeCard key={recipe.id} recipe={recipe} />
            ))}
          </div>

          {!data?.data?.length && (
            <div className="text-center py-16 text-gray-500">
              <p className="text-lg">{t('common.noResults')}</p>
            </div>
          )}

          {/* Pagination */}
          {data?.pagination && data.pagination.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-8">
              <Button variant="ghost" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                {t('common.previous')}
              </Button>
              <span className="text-sm text-gray-500">
                {page + 1} / {data.pagination.totalPages}
              </span>
              <Button variant="ghost" disabled={data.pagination.last} onClick={() => setPage((p) => p + 1)}>
                {t('common.next')}
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

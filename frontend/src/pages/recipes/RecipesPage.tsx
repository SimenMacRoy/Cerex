import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { useState, useEffect, useCallback, useRef } from 'react';
import { FiFilter, FiGrid, FiList, FiSearch, FiX, FiPlus } from 'react-icons/fi';
import { Link } from 'react-router-dom';
import { recipeApi } from '@/lib/api';
import { useAuthStore } from '@/stores/authStore';
import RecipeCard from '@/components/recipe/RecipeCard';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';

const CUISINE_FLAGS: Record<string, string> = {
  FRENCH: '🇫🇷', ITALIAN: '🇮🇹', JAPANESE: '🇯🇵', CHINESE: '🇨🇳',
  INDIAN: '🇮🇳', MEXICAN: '🇲🇽', THAI: '🇹🇭', AFRICAN: '🌍',
  KOREAN: '🇰🇷', SPANISH: '🇪🇸', GREEK: '🇬🇷', TURKISH: '🇹🇷',
  AMERICAN: '🇺🇸', BRAZILIAN: '🇧🇷', MOROCCAN: '🇲🇦', LEBANESE: '🇱🇧',
};

export default function RecipesPage() {
  const { t } = useTranslation();
  const { isAuthenticated } = useAuthStore();
  const [page, setPage] = useState(0);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [filters, setFilters] = useState<{ cuisine?: string; difficulty?: string }>({});
  const [searchInput, setSearchInput] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout>>();

  // Debounced search: wait 400ms after user stops typing
  const handleSearchChange = useCallback((value: string) => {
    setSearchInput(value);
    if (debounceTimerRef.current) clearTimeout(debounceTimerRef.current);
    debounceTimerRef.current = setTimeout(() => {
      setSearchQuery(value);
      setPage(0);
    }, 400);
  }, []);

  // Reset page when filters change
  useEffect(() => {
    setPage(0);
  }, [filters]);

  const { data, isLoading } = useQuery({
    queryKey: ['recipes', page, filters, searchQuery],
    queryFn: () => recipeApi.getAll({
      page,
      size: 12,
      ...filters,
      ...(searchQuery ? { q: searchQuery } : {}),
    }).then((r) => r.data),
  });

  const clearSearch = () => {
    setSearchInput('');
    setSearchQuery('');
    setPage(0);
  };

  const clearAllFilters = () => {
    setFilters({});
    clearSearch();
  };

  const hasActiveFilters = filters.cuisine || filters.difficulty || searchQuery;

  const cuisines = Object.keys(CUISINE_FLAGS);
  const difficulties = ['EASY', 'MEDIUM', 'HARD', 'EXPERT'];

  return (
    <div className="page-container">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <h1 className="section-title">{t('nav.recipes')}</h1>
        <div className="flex items-center gap-2">
          {isAuthenticated && (
            <Link
              to="/recipes/create"
              className="flex items-center gap-1.5 px-4 py-2 bg-primary-500 hover:bg-primary-600 text-white rounded-xl text-sm font-medium transition-colors"
            >
              <FiPlus className="w-4 h-4" />
              Ajouter
            </Link>
          )}
          <button onClick={() => setViewMode('grid')} className={`p-2 rounded-lg transition-colors ${viewMode === 'grid' ? 'bg-primary-100 text-primary-600' : 'text-gray-400 hover:text-gray-600'}`}>
            <FiGrid className="w-5 h-5" />
          </button>
          <button onClick={() => setViewMode('list')} className={`p-2 rounded-lg transition-colors ${viewMode === 'list' ? 'bg-primary-100 text-primary-600' : 'text-gray-400 hover:text-gray-600'}`}>
            <FiList className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Search bar */}
      <div className="relative mb-6">
        <FiSearch className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
        <input
          type="text"
          value={searchInput}
          onChange={(e) => handleSearchChange(e.target.value)}
          placeholder={t('common.search') + '...'}
          className="input-field pl-12 pr-10 w-full"
        />
        {searchInput && (
          <button onClick={clearSearch} className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
            <FiX className="w-5 h-5" />
          </button>
        )}
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
            className={`badge text-xs cursor-pointer transition-colors ${filters.cuisine === c ? 'bg-primary-500 text-white' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'}`}
          >
            {CUISINE_FLAGS[c]} {c}
          </button>
        ))}
        <span className="text-gray-300 dark:text-gray-600">|</span>
        {difficulties.map((d) => (
          <button
            key={d}
            onClick={() => setFilters((f) => ({ ...f, difficulty: f.difficulty === d ? undefined : d }))}
            className={`badge text-xs cursor-pointer transition-colors ${filters.difficulty === d ? 'bg-secondary-500 text-white' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'}`}
          >
            {t(`recipe.${d.toLowerCase()}`)}
          </button>
        ))}
        {hasActiveFilters && (
          <>
            <span className="text-gray-300 dark:text-gray-600">|</span>
            <button onClick={clearAllFilters} className="badge text-xs cursor-pointer bg-red-100 text-red-600 hover:bg-red-200 dark:bg-red-900/30 dark:text-red-400">
              <FiX className="w-3 h-3 inline mr-1" />{t('common.clearAll') || 'Clear all'}
            </button>
          </>
        )}
      </div>

      {/* Active filter summary */}
      {hasActiveFilters && (
        <div className="mb-4 text-sm text-gray-500 dark:text-gray-400">
          {searchQuery && <span>🔍 &quot;{searchQuery}&quot; </span>}
          {filters.cuisine && <span>• {CUISINE_FLAGS[filters.cuisine]} {filters.cuisine} </span>}
          {filters.difficulty && <span>• {t(`recipe.${filters.difficulty.toLowerCase()}`)} </span>}
        </div>
      )}

      {/* Recipe Grid */}
      {isLoading ? (
        <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
      ) : (
        <>
          <div className={viewMode === 'grid'
            ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'
            : 'space-y-4'
          }>
            {data?.data?.content?.map((recipe) => (
              <RecipeCard key={recipe.id} recipe={recipe} />
            ))}
          </div>

          {!data?.data?.content?.length && (
            <div className="text-center py-16 text-gray-500">
              <p className="text-lg">🍽️</p>
              <p className="text-lg mt-2">{t('common.noResults')}</p>
              {hasActiveFilters && (
                <Button variant="ghost" className="mt-4" onClick={clearAllFilters}>
                  {t('common.clearAll') || 'Clear filters'}
                </Button>
              )}
            </div>
          )}

          {/* Pagination */}
          {data?.data && data.data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-8">
              <Button variant="ghost" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                {t('common.previous')}
              </Button>
              <span className="text-sm text-gray-500">
                {page + 1} / {data.data.totalPages}
              </span>
              <Button variant="ghost" disabled={data.data.last} onClick={() => setPage((p) => p + 1)}>
                {t('common.next')}
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

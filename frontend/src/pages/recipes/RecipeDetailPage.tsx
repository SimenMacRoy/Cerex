import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { FiClock, FiUsers, FiHeart, FiShare2, FiBookmark, FiShoppingCart } from 'react-icons/fi';
import { recipeApi } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import StarRating from '@/components/ui/StarRating';
import EcoScoreBadge from '@/components/ui/EcoScoreBadge';
import Avatar from '@/components/ui/Avatar';
import Button from '@/components/ui/Button';
import { formatDuration, getDifficultyColor, cn } from '@/lib/utils';

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();

  const { data, isLoading } = useQuery({
    queryKey: ['recipe', id],
    queryFn: () => recipeApi.getById(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  if (isLoading) return <div className="flex justify-center py-24"><LoadingSpinner size="lg" /></div>;
  if (!data) return <div className="page-container text-center py-16">{t('common.error')}</div>;

  const recipe = data;

  return (
    <div className="page-container max-w-4xl">
      {/* Hero image */}
      <div className="relative h-64 md:h-96 rounded-2xl overflow-hidden mb-8 bg-gray-200 dark:bg-gray-700">
        {recipe.imageUrl && (
          <img src={recipe.imageUrl} alt={recipe.title} className="w-full h-full object-cover" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
        <div className="absolute bottom-6 left-6 right-6">
          <span className={cn('badge mb-2', getDifficultyColor(recipe.difficulty))}>
            {t(`recipe.${recipe.difficulty.toLowerCase()}`)}
          </span>
          <h1 className="text-3xl md:text-4xl font-display font-bold text-white">{recipe.title}</h1>
        </div>
      </div>

      {/* Meta row */}
      <div className="flex flex-wrap items-center gap-4 mb-8">
        <div className="flex items-center gap-2">
          <Avatar src={recipe.authorAvatarUrl} name={recipe.authorName} size="sm" />
          <span className="text-sm font-medium">{recipe.authorName}</span>
        </div>
        <StarRating rating={recipe.rating} />
        <span className="text-sm text-gray-500">({recipe.ratingsCount} {t('recipe.reviews')})</span>
        <div className="flex items-center gap-1 text-sm text-gray-500">
          <FiClock className="w-4 h-4" />
          {formatDuration(recipe.prepTimeMinutes + recipe.cookTimeMinutes)}
        </div>
        <div className="flex items-center gap-1 text-sm text-gray-500">
          <FiUsers className="w-4 h-4" />
          {recipe.servings} {t('recipe.servings')}
        </div>
        {recipe.ecoScore && <EcoScoreBadge score={recipe.ecoScore} grade={recipe.ecoScore >= 80 ? 'A' : recipe.ecoScore >= 60 ? 'B' : 'C'} size="sm" />}
      </div>

      {/* Action buttons */}
      <div className="flex flex-wrap gap-3 mb-8">
        <Button variant="primary" leftIcon={<FiHeart className="w-4 h-4" />}>
          {t('recipe.like')} ({recipe.likesCount})
        </Button>
        <Button variant="outline" leftIcon={<FiBookmark className="w-4 h-4" />}>
          {t('recipe.save')}
        </Button>
        <Button variant="outline" leftIcon={<FiShare2 className="w-4 h-4" />}>
          {t('recipe.share')}
        </Button>
        <Button variant="secondary" leftIcon={<FiShoppingCart className="w-4 h-4" />}>
          {t('recipe.orderIngredients')}
        </Button>
      </div>

      {/* Description */}
      <p className="text-gray-600 dark:text-gray-400 mb-8 text-lg leading-relaxed">{recipe.description}</p>

      <div className="grid md:grid-cols-3 gap-8">
        {/* Ingredients */}
        <div className="md:col-span-1">
          <h2 className="text-xl font-display font-bold mb-4">{t('recipe.ingredients')}</h2>
          <div className="card p-4 space-y-3">
            {recipe.ingredients?.map((ing) => (
              <div key={ing.id} className="flex items-center justify-between py-1 border-b border-gray-100 dark:border-gray-700 last:border-0">
                <span className={cn('text-sm', ing.isOptional && 'italic text-gray-400')}>{ing.name}</span>
                <span className="text-sm font-medium text-gray-500">{ing.quantity} {ing.unit}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Steps */}
        <div className="md:col-span-2">
          <h2 className="text-xl font-display font-bold mb-4">{t('recipe.steps')}</h2>
          <div className="space-y-4">
            {recipe.steps?.map((step) => (
              <div key={step.stepNumber} className="card p-4">
                <div className="flex items-start gap-4">
                  <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center text-primary-600 dark:text-primary-400 font-bold text-sm shrink-0">
                    {step.stepNumber}
                  </div>
                  <div>
                    <h3 className="font-semibold mb-1">{step.title}</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-400">{step.description}</p>
                    {step.tips && (
                      <p className="text-sm text-primary-500 mt-2 italic">💡 {step.tips}</p>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Nutritional Info */}
      {recipe.nutritionalInfo && (
        <div className="mt-8">
          <h2 className="text-xl font-display font-bold mb-4">{t('recipe.nutrition')}</h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3">
            {Object.entries(recipe.nutritionalInfo).map(([key, value]) => (
              <div key={key} className="card p-3 text-center">
                <div className="text-lg font-bold text-primary-500">{value as number}</div>
                <div className="text-xs text-gray-500 capitalize">{key}</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

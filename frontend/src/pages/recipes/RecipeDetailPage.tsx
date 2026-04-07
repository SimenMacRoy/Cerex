import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useState } from 'react';
import { FiClock, FiUsers, FiHeart, FiShare2, FiBookmark, FiShoppingCart, FiX, FiMinus, FiPlus } from 'react-icons/fi';
import { recipeApi } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import StarRating from '@/components/ui/StarRating';
import Avatar from '@/components/ui/Avatar';
import Button from '@/components/ui/Button';
import { formatDuration, getDifficultyColor, cn } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [liked, setLiked] = useState(false);
  const [saved, setSaved] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [groceryOpen, setGroceryOpen] = useState(false);
  const [servings, setServings] = useState(4);

  const { data, isLoading } = useQuery({
    queryKey: ['recipe', id],
    queryFn: () => recipeApi.getById(id!).then((r) => {
      const recipe = r.data.data;
      setLikeCount(recipe.likeCount ?? 0);
      return recipe;
    }),
    enabled: !!id,
  });

  const likeMutation = useMutation({
    mutationFn: () => recipeApi.like(id!, !liked),
    onMutate: () => {
      setLiked((prev) => !prev);
      setLikeCount((prev) => liked ? prev - 1 : prev + 1);
    },
    onError: () => {
      setLiked((prev) => !prev);
      setLikeCount((prev) => liked ? prev + 1 : prev - 1);
      toast.error(t('common.error'));
    },
  });

  const { data: groceryData, isFetching: groceryLoading } = useQuery({
    queryKey: ['grocery-list', id, servings],
    queryFn: () => recipeApi.getGroceryList(id!, servings).then((r) => r.data.data),
    enabled: groceryOpen && !!id,
  });

  const saveMutation = useMutation({
    mutationFn: () => recipeApi.save(id!),
    onMutate: () => {
      setSaved((prev) => !prev);
      toast.success(saved ? t('recipe.unsaved') : t('recipe.saved'));
    },
    onError: () => {
      setSaved((prev) => !prev);
      toast.error(t('common.error'));
    },
  });

  const handleShare = async () => {
    try {
      await navigator.clipboard.writeText(window.location.href);
      toast.success(t('recipe.linkCopied') || 'Link copied!');
    } catch {
      toast.error(t('common.error'));
    }
  };

  if (isLoading) return <div className="flex justify-center py-24"><LoadingSpinner size="lg" /></div>;
  if (!data) return <div className="page-container text-center py-16">{t('common.error')}</div>;

  const recipe = data;

  return (
    <div className="page-container max-w-4xl">
      {/* Hero image */}
      <div className="relative h-64 md:h-96 rounded-2xl overflow-hidden mb-8 bg-gray-200 dark:bg-gray-700">
        {recipe.coverImageUrl && (
          <img src={recipe.coverImageUrl} alt={recipe.title} className="w-full h-full object-cover" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
        <div className="absolute bottom-6 left-6 right-6">
          <span className={cn('badge mb-2', getDifficultyColor(recipe.difficultyLevel))}>
            {t(`recipe.${recipe.difficultyLevel?.toLowerCase() ?? 'medium'}`, { defaultValue: recipe.difficultyLevel })}
          </span>
          <h1 className="text-3xl md:text-4xl font-display font-bold text-white">{recipe.title}</h1>
        </div>
      </div>

      {/* Meta row */}
      <div className="flex flex-wrap items-center gap-4 mb-8">
        <div className="flex items-center gap-2">
          <Avatar src={recipe.author?.avatarUrl} name={recipe.author?.displayName ?? ''} size="sm" />
          <span className="text-sm font-medium">{recipe.author?.displayName}</span>
        </div>
        <StarRating rating={recipe.avgRating} />
        <span className="text-sm text-gray-500">({recipe.ratingCount} {t('recipe.reviews')})</span>
        <div className="flex items-center gap-1 text-sm text-gray-500">
          <FiClock className="w-4 h-4" />
          {formatDuration(recipe.prepTimeMinutes + recipe.cookTimeMinutes)}
        </div>
        <div className="flex items-center gap-1 text-sm text-gray-500">
          <FiUsers className="w-4 h-4" />
          {recipe.servings} {t('recipe.servings')}
        </div>
      </div>

      {/* Action buttons */}
      <div className="flex flex-wrap gap-3 mb-8">
        <Button
          variant={liked ? 'primary' : 'outline'}
          leftIcon={<FiHeart className={cn('w-4 h-4', liked && 'fill-current')} />}
          onClick={() => likeMutation.mutate()}
        >
          {t('recipe.like')} ({likeCount})
        </Button>
        <Button
          variant={saved ? 'primary' : 'outline'}
          leftIcon={<FiBookmark className={cn('w-4 h-4', saved && 'fill-current')} />}
          onClick={() => saveMutation.mutate()}
        >
          {saved ? t('recipe.saved') || 'Saved' : t('recipe.save')}
        </Button>
        <Button variant="outline" leftIcon={<FiShare2 className="w-4 h-4" />} onClick={handleShare}>
          {t('recipe.share')}
        </Button>
        <Button
          variant="secondary"
          leftIcon={<FiShoppingCart className="w-4 h-4" />}
          onClick={() => { setServings(recipe.servings ?? 4); setGroceryOpen(true); }}
        >
          Commander les ingrédients
        </Button>
      </div>

      {/* Grocery Modal */}
      {groceryOpen && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-cerex-medium rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] flex flex-col">
            {/* Modal header */}
            <div className="flex items-center justify-between p-5 border-b border-gray-200 dark:border-gray-700">
              <div>
                <h3 className="font-display font-bold text-lg">Liste de courses</h3>
                <p className="text-xs text-gray-500 mt-0.5">{recipe.title}</p>
              </div>
              <button onClick={() => setGroceryOpen(false)} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors">
                <FiX className="w-5 h-5" />
              </button>
            </div>

            {/* Servings adjuster */}
            <div className="flex items-center justify-between px-5 py-3 bg-gray-50 dark:bg-gray-800/50">
              <span className="text-sm font-medium">Nombre de portions</span>
              <div className="flex items-center gap-3">
                <button
                  onClick={() => setServings((s) => Math.max(1, s - 1))}
                  className="w-8 h-8 rounded-full border border-gray-300 dark:border-gray-600 flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                >
                  <FiMinus className="w-3.5 h-3.5" />
                </button>
                <span className="w-6 text-center font-bold">{servings}</span>
                <button
                  onClick={() => setServings((s) => s + 1)}
                  className="w-8 h-8 rounded-full border border-gray-300 dark:border-gray-600 flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                >
                  <FiPlus className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>

            {/* Ingredient list */}
            <div className="flex-1 overflow-y-auto px-5 py-3">
              {groceryLoading ? (
                <div className="flex justify-center py-8"><LoadingSpinner /></div>
              ) : (
                <div className="space-y-2">
                  {groceryData?.ingredients?.map((item: any, i: number) => (
                    <div
                      key={i}
                      className={cn(
                        'flex items-center justify-between py-2.5 border-b border-gray-100 dark:border-gray-700 last:border-0',
                        item.isOptional && 'opacity-60'
                      )}
                    >
                      <div className="flex-1 min-w-0">
                        <span className="text-sm font-medium">{item.name}</span>
                        {item.isOptional && <span className="ml-1 text-xs text-gray-400">(optionnel)</span>}
                        {item.groupName && <div className="text-xs text-gray-400">{item.groupName}</div>}
                      </div>
                      <div className="flex items-center gap-4 ml-3 shrink-0">
                        <span className="text-sm text-gray-500 min-w-[80px] text-right">
                          {item.quantity} {item.unit}
                        </span>
                        <span className="text-sm font-semibold text-primary-600 dark:text-primary-400 min-w-[70px] text-right">
                          {Number(item.totalPriceFcfa).toLocaleString('fr-FR')} F
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Total + CTA */}
            <div className="p-5 border-t border-gray-200 dark:border-gray-700">
              <div className="flex items-center justify-between mb-4">
                <span className="font-semibold">Total estimé</span>
                <span className="text-xl font-bold text-primary-600 dark:text-primary-400">
                  {groceryData ? Number(groceryData.totalEstimatedPriceFcfa).toLocaleString('fr-FR') : '—'} FCFA
                </span>
              </div>
              <p className="text-xs text-gray-400 mb-4">
                * Prix indicatifs du marché. Les ingrédients optionnels ne sont pas inclus dans le total.
              </p>
              <Button
                className="w-full"
                leftIcon={<FiShoppingCart className="w-4 h-4" />}
                onClick={() => {
                  setGroceryOpen(false);
                  navigate(`/recipes/${id}/order-ingredients?servings=${servings}`);
                }}
              >
                Trouver les épiceries proches
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Description */}
      <p className="text-gray-600 dark:text-gray-400 mb-8 text-lg leading-relaxed">{recipe.description}</p>

      <div className="grid md:grid-cols-3 gap-8">
        {/* Ingredients */}
        <div className="md:col-span-1">
          <h2 className="text-xl font-display font-bold mb-4">{t('recipe.ingredients')}</h2>
          <div className="card p-4 space-y-3">
            {recipe.ingredients?.map((ing) => (
              <div key={ing.id} className="flex items-start justify-between py-1 border-b border-gray-100 dark:border-gray-700 last:border-0">
                <span className={cn('text-sm flex-1', ing.isOptional && 'italic text-gray-400')}>
                  {ing.displayText || ing.name}
                </span>
                <span className="text-sm font-medium text-gray-500 ml-2 whitespace-nowrap">
                  {ing.quantity} {ing.unit}
                </span>
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
                  <div className="flex-1">
                    <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed">{step.instruction}</p>
                    {step.durationMinutes && (
                      <span className="inline-flex items-center gap-1 text-xs text-gray-400 mt-2">
                        <FiClock className="w-3 h-3" /> {formatDuration(step.durationMinutes)}
                      </span>
                    )}
                    {step.tip && (
                      <p className="text-sm text-primary-500 mt-2 italic">💡 {step.tip}</p>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Nutritional Info */}
      {recipe.nutrition && (
        <div className="mt-8">
          <h2 className="text-xl font-display font-bold mb-4">{t('recipe.nutrition')}</h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3">
            {Object.entries(recipe.nutrition).map(([key, value]) => (
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

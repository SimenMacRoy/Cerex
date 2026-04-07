import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FiPlus, FiTrash2, FiCheck, FiX, FiClock, FiEye } from 'react-icons/fi';
import { recipeApi } from '@/lib/api';
import { useAuthStore } from '@/stores/authStore';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import { formatDuration, getDifficultyColor, getCuisineFlag, cn } from '@/lib/utils';
import toast from 'react-hot-toast';
import type { RecipeCard } from '@/types';

type Tab = 'all' | 'mine' | 'pending';

const STATUS_BADGE: Record<string, { label: string; cls: string }> = {
  PUBLISHED:      { label: 'Publiée',       cls: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400' },
  DRAFT:          { label: 'Brouillon',     cls: 'bg-gray-100  text-gray-600  dark:bg-gray-800 dark:text-gray-400' },
  PENDING_REVIEW: { label: 'En révision',   cls: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400' },
  REJECTED:       { label: 'Rejetée',       cls: 'bg-red-100   text-red-600   dark:bg-red-900/30 dark:text-red-400' },
};

export default function MyRecipesPage() {
  const { user } = useAuthStore();
  const isAdmin = user?.role === 'ADMIN' || user?.role === 'MODERATOR' || user?.role === 'SUPER_ADMIN';
  const [tab, setTab] = useState<Tab>('mine');
  const [rejectId, setRejectId] = useState<string | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const queryClient = useQueryClient();

  // All public recipes (admin view)
  const { data: allData, isLoading: allLoading } = useQuery({
    queryKey: ['recipes-all'],
    queryFn: () => recipeApi.getAll({ size: 50 }).then((r) => r.data),
    enabled: tab === 'all',
  });

  // My own recipes (any status)
  const { data: mineData, isLoading: mineLoading } = useQuery({
    queryKey: ['my-recipes'],
    queryFn: () => recipeApi.getMyRecipes(0).then((r) => r.data),
    enabled: tab === 'mine',
  });

  // Pending review (admin only)
  const { data: pendingData, isLoading: pendingLoading } = useQuery({
    queryKey: ['recipes-pending'],
    queryFn: () => recipeApi.getAll({ size: 50 }).then((r) => r.data),
    enabled: tab === 'pending' && isAdmin,
  });

  const approveMutation = useMutation({
    mutationFn: (id: string) => recipeApi.approve(id),
    onSuccess: () => {
      toast.success('Recette approuvée et publiée !');
      queryClient.invalidateQueries({ queryKey: ['recipes-pending'] });
      queryClient.invalidateQueries({ queryKey: ['recipes-all'] });
    },
    onError: () => toast.error('Erreur lors de l\'approbation'),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      recipeApi.reject(id, reason),
    onSuccess: () => {
      toast.success('Recette rejetée.');
      setRejectId(null);
      setRejectReason('');
      queryClient.invalidateQueries({ queryKey: ['recipes-pending'] });
    },
    onError: () => toast.error('Erreur'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => recipeApi.delete(id),
    onSuccess: () => {
      toast.success('Recette supprimée.');
      queryClient.invalidateQueries({ queryKey: ['my-recipes'] });
    },
    onError: () => toast.error('Erreur lors de la suppression'),
  });

  const publishMutation = useMutation({
    mutationFn: (id: string) => recipeApi.publish(id),
    onSuccess: () => {
      toast.success('Recette soumise pour révision !');
      queryClient.invalidateQueries({ queryKey: ['my-recipes'] });
    },
    onError: () => toast.error('Erreur'),
  });

  const tabs: { key: Tab; label: string; adminOnly?: boolean }[] = [
    { key: 'mine',    label: 'Mes recettes' },
    { key: 'all',     label: 'Toutes les recettes' },
    ...(isAdmin ? [{ key: 'pending' as Tab, label: '⏳ En attente de révision', adminOnly: true }] : []),
  ];

  const isLoading = (tab === 'all' && allLoading) || (tab === 'mine' && mineLoading) || (tab === 'pending' && pendingLoading);

  const rawRecipes: RecipeCard[] =
    tab === 'all'     ? (allData?.data?.content ?? []) :
    tab === 'mine'    ? (mineData?.data?.content ?? []) :
    tab === 'pending' ? ((pendingData?.data?.content ?? []).filter((r: any) => r.status === 'PENDING_REVIEW')) :
    [];

  return (
    <div className="page-container max-w-5xl">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <h1 className="section-title">Recettes</h1>
        <Link
          to="/recipes/create"
          className="flex items-center gap-2 px-4 py-2 bg-primary-500 hover:bg-primary-600 text-white rounded-xl text-sm font-medium transition-colors"
        >
          <FiPlus className="w-4 h-4" />
          Nouvelle recette
        </Link>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 border-b border-gray-200 dark:border-gray-700">
        {tabs.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={cn(
              'px-4 py-2.5 text-sm font-medium border-b-2 transition-colors -mb-px',
              tab === t.key
                ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'
            )}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Reject modal */}
      {rejectId && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-cerex-medium rounded-2xl p-6 max-w-md w-full shadow-xl">
            <h3 className="font-semibold text-lg mb-3">Motif du rejet</h3>
            <textarea
              className="input-field min-h-[100px] resize-y w-full mb-4"
              placeholder="Expliquez pourquoi la recette est rejetée..."
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
            />
            <div className="flex gap-2">
              <Button variant="outline" className="flex-1" onClick={() => setRejectId(null)}>
                Annuler
              </Button>
              <Button
                variant="primary"
                className="flex-1 bg-red-500 hover:bg-red-600"
                isLoading={rejectMutation.isPending}
                onClick={() => rejectId && rejectMutation.mutate({ id: rejectId, reason: rejectReason })}
                disabled={!rejectReason.trim()}
              >
                Rejeter
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Content */}
      {isLoading ? (
        <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
      ) : rawRecipes.length === 0 ? (
        <div className="text-center py-16 text-gray-500">
          <p className="text-4xl mb-4">🍽️</p>
          <p className="text-lg font-medium mb-2">Aucune recette ici</p>
          {tab === 'mine' && (
            <Link to="/recipes/create" className="text-primary-500 underline text-sm">
              Créer ma première recette →
            </Link>
          )}
        </div>
      ) : (
        <div className="space-y-3">
          {rawRecipes.map((recipe: any) => (
            <div
              key={recipe.id}
              className="card p-4 flex items-center gap-4 hover:shadow-md transition-shadow"
            >
              {/* Image */}
              <div className="w-16 h-16 rounded-xl bg-gray-100 dark:bg-gray-800 overflow-hidden shrink-0">
                {recipe.coverImageUrl ? (
                  <img src={recipe.coverImageUrl} alt={recipe.title} className="w-full h-full object-cover" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-2xl">🍽️</div>
                )}
              </div>

              {/* Info */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-semibold text-sm truncate">{recipe.title}</h3>
                  {recipe.cuisineType && (
                    <span className="text-xs text-gray-400">
                      {getCuisineFlag(recipe.cuisineType)} {recipe.cuisineType}
                    </span>
                  )}
                </div>
                <div className="flex items-center gap-3 mt-1 text-xs text-gray-500 flex-wrap">
                  <span className={cn('badge text-xs', getDifficultyColor(recipe.difficultyLevel))}>
                    {recipe.difficultyLevel}
                  </span>
                  <span className="flex items-center gap-1">
                    <FiClock className="w-3 h-3" />
                    {formatDuration((recipe.prepTimeMinutes ?? 0) + (recipe.cookTimeMinutes ?? 0))}
                  </span>
                  {recipe.authorName && tab !== 'mine' && (
                    <span className="text-gray-400">par {recipe.authorName}</span>
                  )}
                  {recipe.status && (
                    <span className={cn('badge text-xs', STATUS_BADGE[recipe.status]?.cls)}>
                      {STATUS_BADGE[recipe.status]?.label ?? recipe.status}
                    </span>
                  )}
                </div>
              </div>

              {/* Actions */}
              <div className="flex items-center gap-1 shrink-0">
                {/* View */}
                {recipe.status === 'PUBLISHED' && (
                  <Link
                    to={`/recipes/${recipe.id}`}
                    className="p-2 text-gray-400 hover:text-primary-500 transition-colors"
                    title="Voir"
                  >
                    <FiEye className="w-4 h-4" />
                  </Link>
                )}

                {/* Submit for review (own draft) */}
                {tab === 'mine' && recipe.status === 'DRAFT' && (
                  <Button
                    size="sm"
                    variant="ghost"
                    isLoading={publishMutation.isPending}
                    onClick={() => publishMutation.mutate(recipe.id)}
                    title="Soumettre pour révision"
                  >
                    Soumettre
                  </Button>
                )}

                {/* Admin: Approve */}
                {isAdmin && (tab === 'pending' || tab === 'all') && recipe.status === 'PENDING_REVIEW' && (
                  <button
                    onClick={() => approveMutation.mutate(recipe.id)}
                    className="p-2 text-green-500 hover:text-green-700 hover:bg-green-50 dark:hover:bg-green-900/20 rounded-lg transition-colors"
                    title="Approuver"
                  >
                    <FiCheck className="w-4 h-4" />
                  </button>
                )}

                {/* Admin: Reject */}
                {isAdmin && (tab === 'pending' || tab === 'all') && recipe.status === 'PENDING_REVIEW' && (
                  <button
                    onClick={() => setRejectId(recipe.id)}
                    className="p-2 text-red-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                    title="Rejeter"
                  >
                    <FiX className="w-4 h-4" />
                  </button>
                )}

                {/* Delete (own recipe) */}
                {tab === 'mine' && (
                  <button
                    onClick={() => {
                      if (confirm('Supprimer cette recette ?')) deleteMutation.mutate(recipe.id);
                    }}
                    className="p-2 text-red-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                    title="Supprimer"
                  >
                    <FiTrash2 className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

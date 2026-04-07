import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FiUsers, FiBookOpen, FiCheck, FiX, FiSlash, FiTrash2,
  FiShield, FiActivity, FiClock, FiEye, FiAlertCircle, FiZap,
} from 'react-icons/fi';
import apiClient from '@/lib/apiClient';
import { useAuthStore } from '@/stores/authStore';
import { Navigate } from 'react-router-dom';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import { cn } from '@/lib/utils';
import toast from 'react-hot-toast';

// ── API helpers ─────────────────────────────────────────────
const adminApi = {
  stats: ()                             => apiClient.get('/admin/stats'),
  users: (page = 0)                     => apiClient.get('/admin/users', { params: { page, size: 15 } }),
  changeRole:   (id: string, role: string)   => apiClient.patch(`/admin/users/${id}/role`,   null, { params: { role } }),
  changeStatus: (id: string, status: string) => apiClient.patch(`/admin/users/${id}/status`, null, { params: { status } }),
  deleteUser:   (id: string)            => apiClient.delete(`/admin/users/${id}`),
  recipes: (page = 0, status?: string)  => apiClient.get('/admin/recipes', { params: { page, size: 15, status } }),
  approveRecipe: (id: string)           => apiClient.post(`/recipes/${id}/approve`),
  rejectRecipe:  (id: string, reason: string) => apiClient.post(`/recipes/${id}/reject`, null, { params: { reason } }),
};

type Tab = 'overview' | 'users' | 'recipes' | 'pending' | 'ai';

const STATUS_COLORS: Record<string, string> = {
  PUBLISHED:      'bg-green-100 text-green-700',
  PENDING_REVIEW: 'bg-yellow-100 text-yellow-700',
  DRAFT:          'bg-gray-100  text-gray-600',
  REJECTED:       'bg-red-100   text-red-600',
  ACTIVE:         'bg-green-100 text-green-700',
  SUSPENDED:      'bg-orange-100 text-orange-700',
  BANNED:         'bg-red-100   text-red-700',
  INACTIVE:       'bg-gray-100  text-gray-500',
};

const ROLES = ['USER', 'CHEF', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN'];
const USER_STATUSES = ['ACTIVE', 'SUSPENDED', 'BANNED', 'INACTIVE'];

export default function AdminDashboard() {
  const { user } = useAuthStore();
  const isAdmin = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN' || user?.role === 'MODERATOR';
  if (!isAdmin) return <Navigate to="/" replace />;

  const [tab, setTab]               = useState<Tab>('overview');
  const [userPage, setUserPage]     = useState(0);
  const [recipePage, setRecipePage] = useState(0);
  const [rejectModal, setRejectModal] = useState<{ id: string; title: string } | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const qc = useQueryClient();

  // AI Recipe Generation state
  const [aiName, setAiName]           = useState('');
  const [aiResult, setAiResult]       = useState<any>(null);
  const [aiGenerating, setAiGenerating] = useState(false);

  const { data: statsData } = useQuery({
    queryKey: ['admin-stats'],
    queryFn: () => adminApi.stats().then((r: any) => r.data.data),
  });

  const { data: usersData, isLoading: usersLoading } = useQuery({
    queryKey: ['admin-users', userPage],
    queryFn: () => adminApi.users(userPage).then((r: any) => r.data.data),
    enabled: tab === 'users',
  });

  const { data: recipesData, isLoading: recipesLoading } = useQuery({
    queryKey: ['admin-recipes', recipePage, null],
    queryFn: () => adminApi.recipes(recipePage).then((r: any) => r.data.data),
    enabled: tab === 'recipes',
  });

  const { data: pendingData, isLoading: pendingLoading } = useQuery({
    queryKey: ['admin-pending'],
    queryFn: () => adminApi.recipes(0, 'PENDING_REVIEW').then((r: any) => r.data.data),
    enabled: tab === 'pending',
  });

  const roleMut = useMutation({
    mutationFn: ({ id, role }: { id: string; role: string }) => adminApi.changeRole(id, role),
    onSuccess: () => { toast.success('Rôle mis à jour'); qc.invalidateQueries({ queryKey: ['admin-users'] }); },
    onError: () => toast.error('Erreur'),
  });

  const statusMut = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => adminApi.changeStatus(id, status),
    onSuccess: () => { toast.success('Statut mis à jour'); qc.invalidateQueries({ queryKey: ['admin-users'] }); },
    onError: () => toast.error('Erreur'),
  });

  const deleteUserMut = useMutation({
    mutationFn: (id: string) => adminApi.deleteUser(id),
    onSuccess: () => { toast.success('Utilisateur supprimé'); qc.invalidateQueries({ queryKey: ['admin-users'] }); },
    onError: () => toast.error('Erreur'),
  });

  const approveMut = useMutation({
    mutationFn: (id: string) => adminApi.approveRecipe(id),
    onSuccess: () => {
      toast.success('Recette approuvée et publiée !');
      qc.invalidateQueries({ queryKey: ['admin-pending'] });
      qc.invalidateQueries({ queryKey: ['admin-recipes'] });
      qc.invalidateQueries({ queryKey: ['admin-stats'] });
    },
    onError: () => toast.error('Erreur lors de l\'approbation'),
  });

  const rejectMut = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => adminApi.rejectRecipe(id, reason),
    onSuccess: () => {
      toast.success('Recette rejetée.');
      setRejectModal(null);
      setRejectReason('');
      qc.invalidateQueries({ queryKey: ['admin-pending'] });
      qc.invalidateQueries({ queryKey: ['admin-stats'] });
    },
    onError: () => toast.error('Erreur'),
  });

  const tabs = [
    { key: 'overview' as Tab, label: 'Vue d\'ensemble',    icon: FiActivity },
    { key: 'users'    as Tab, label: 'Utilisateurs',       icon: FiUsers },
    { key: 'pending'  as Tab, label: 'En attente',         icon: FiClock,
      badge: statsData?.pending ?? 0 },
    { key: 'recipes'  as Tab, label: 'Toutes les recettes', icon: FiBookOpen },
    { key: 'ai'       as Tab, label: 'IA — Créer recette', icon: FiZap },
  ];

  return (
    <div className="page-container max-w-6xl">
      {/* Header */}
      <div className="flex items-center gap-3 mb-8">
        <div className="w-10 h-10 rounded-xl bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
          <FiShield className="w-5 h-5 text-primary-600 dark:text-primary-400" />
        </div>
        <div>
          <h1 className="text-2xl font-display font-bold">Dashboard Admin</h1>
          <p className="text-sm text-gray-500">Gestion de la plateforme Cerex</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 border-b border-gray-200 dark:border-gray-700 overflow-x-auto">
        {tabs.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={cn(
              'flex items-center gap-2 px-4 py-2.5 text-sm font-medium border-b-2 transition-colors -mb-px whitespace-nowrap shrink-0',
              tab === t.key
                ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'
            )}
          >
            <t.icon className="w-4 h-4" />
            {t.label}
            {t.badge > 0 && (
              <span className="ml-1 px-1.5 py-0.5 text-xs bg-yellow-100 text-yellow-700 rounded-full font-bold">
                {t.badge}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Reject modal */}
      {rejectModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-cerex-medium rounded-2xl p-6 max-w-md w-full shadow-xl">
            <h3 className="font-semibold text-lg mb-1">Rejeter la recette</h3>
            <p className="text-sm text-gray-500 mb-3 truncate">{rejectModal.title}</p>
            <textarea
              className="input-field min-h-[100px] resize-y w-full mb-4"
              placeholder="Motif du rejet..."
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
            />
            <div className="flex gap-2">
              <Button variant="outline" className="flex-1" onClick={() => setRejectModal(null)}>Annuler</Button>
              <Button
                className="flex-1 bg-red-500 hover:bg-red-600 text-white"
                isLoading={rejectMut.isPending}
                disabled={!rejectReason.trim()}
                onClick={() => rejectMut.mutate({ id: rejectModal.id, reason: rejectReason })}
              >
                Rejeter
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ── Overview ── */}
      {tab === 'overview' && (
        <div className="space-y-6">
          {/* Stat cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              { label: 'Utilisateurs',    value: statsData?.totalUsers,   sub: `+${statsData?.newUsers7d ?? 0} cette semaine`,  icon: FiUsers,    color: 'text-blue-500' },
              { label: 'Recettes totales',value: statsData?.totalRecipes,  sub: `+${statsData?.newRecipes7d ?? 0} cette semaine`, icon: FiBookOpen, color: 'text-primary-500' },
              { label: 'Publiées',        value: statsData?.published,     sub: 'Visibles publiquement',                          icon: FiCheck,    color: 'text-green-500' },
              { label: 'En attente',      value: statsData?.pending,       sub: 'À réviser',                                      icon: FiClock,    color: 'text-yellow-500' },
            ].map((s) => (
              <div key={s.label} className="card p-5">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-sm text-gray-500 font-medium">{s.label}</span>
                  <s.icon className={cn('w-5 h-5', s.color)} />
                </div>
                <div className="text-3xl font-bold">{s.value ?? '—'}</div>
                <div className="text-xs text-gray-400 mt-1">{s.sub}</div>
              </div>
            ))}
          </div>

          {/* Extra stats */}
          <div className="grid grid-cols-3 gap-4">
            {[
              { label: 'Brouillons', value: statsData?.drafts,   color: 'text-gray-500' },
              { label: 'Rejetées',   value: statsData?.rejected, color: 'text-red-500'  },
              { label: 'Publiées %', value: statsData?.totalRecipes
                  ? `${Math.round((statsData.published / statsData.totalRecipes) * 100)}%`
                  : '—', color: 'text-green-500' },
            ].map((s) => (
              <div key={s.label} className="card p-4 text-center">
                <div className={cn('text-2xl font-bold', s.color)}>{s.value ?? '—'}</div>
                <div className="text-xs text-gray-400 mt-1">{s.label}</div>
              </div>
            ))}
          </div>

          {/* Quick action */}
          {(statsData?.pending ?? 0) > 0 && (
            <div className="card p-4 border-l-4 border-yellow-400 flex items-center gap-3">
              <FiAlertCircle className="w-5 h-5 text-yellow-500 shrink-0" />
              <div className="flex-1">
                <p className="font-medium text-sm">
                  {statsData.pending} recette{statsData.pending > 1 ? 's' : ''} en attente de révision
                </p>
                <p className="text-xs text-gray-500">Cliquez sur l'onglet "En attente" pour les examiner</p>
              </div>
              <Button size="sm" onClick={() => setTab('pending')}>Réviser</Button>
            </div>
          )}
        </div>
      )}

      {/* ── Users ── */}
      {tab === 'users' && (
        <div>
          {usersLoading ? (
            <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
          ) : (
            <>
              <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-gray-700">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 dark:bg-gray-800">
                    <tr>
                      {['Utilisateur', 'Email', 'Rôle', 'Statut', 'Inscrit le', 'Actions'].map((h) => (
                        <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                    {usersData?.content?.map((u: any) => (
                      <tr key={u.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center text-xs font-bold text-primary-600">
                              {(u.displayName || u.email)?.[0]?.toUpperCase()}
                            </div>
                            <span className="font-medium truncate max-w-[120px]">{u.displayName || '—'}</span>
                          </div>
                        </td>
                        <td className="px-4 py-3 text-gray-500 truncate max-w-[160px]">{u.email}</td>
                        <td className="px-4 py-3">
                          <select
                            value={u.role}
                            onChange={(e) => roleMut.mutate({ id: u.id, role: e.target.value })}
                            className="text-xs border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1 bg-white dark:bg-gray-800 focus:outline-none focus:ring-1 focus:ring-primary-400"
                          >
                            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
                          </select>
                        </td>
                        <td className="px-4 py-3">
                          <select
                            value={u.status}
                            onChange={(e) => statusMut.mutate({ id: u.id, status: e.target.value })}
                            className="text-xs border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1 bg-white dark:bg-gray-800 focus:outline-none focus:ring-1 focus:ring-primary-400"
                          >
                            {USER_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                          </select>
                        </td>
                        <td className="px-4 py-3 text-gray-400 text-xs">
                          {u.createdAt ? new Date(u.createdAt).toLocaleDateString('fr-FR') : '—'}
                        </td>
                        <td className="px-4 py-3">
                          <button
                            onClick={() => {
                              if (confirm(`Supprimer ${u.email} ?`)) deleteUserMut.mutate(u.id);
                            }}
                            className="p-1.5 text-red-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                            title="Supprimer"
                          >
                            <FiTrash2 className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {usersData?.totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-4">
                  <Button variant="ghost" disabled={userPage === 0} onClick={() => setUserPage(p => p - 1)}>Précédent</Button>
                  <span className="flex items-center text-sm text-gray-500">{userPage + 1} / {usersData.totalPages}</span>
                  <Button variant="ghost" disabled={userPage >= usersData.totalPages - 1} onClick={() => setUserPage(p => p + 1)}>Suivant</Button>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {/* ── Pending review ── */}
      {tab === 'pending' && (
        <div>
          {pendingLoading ? (
            <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
          ) : pendingData?.content?.length === 0 ? (
            <div className="text-center py-16 text-gray-500">
              <FiCheck className="w-12 h-12 mx-auto mb-3 text-green-400" />
              <p className="font-medium">Aucune recette en attente</p>
              <p className="text-sm mt-1">Tout est à jour !</p>
            </div>
          ) : (
            <div className="space-y-3">
              {pendingData?.content?.map((r: any) => (
                <div key={r.id} className="card p-4 flex items-center gap-4">
                  <div className="w-16 h-16 rounded-xl overflow-hidden bg-gray-100 dark:bg-gray-800 shrink-0">
                    {r.coverImageUrl
                      ? <img src={r.coverImageUrl} alt={r.title} className="w-full h-full object-cover" />
                      : <div className="w-full h-full flex items-center justify-center text-2xl">🍽️</div>
                    }
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold truncate">{r.title}</p>
                    <div className="flex items-center gap-3 mt-1 text-xs text-gray-500 flex-wrap">
                      {r.authorName && <span>par <strong>{r.authorName}</strong></span>}
                      {r.cuisineType && <span>🌍 {r.cuisineType}</span>}
                      {r.difficultyLevel && <span>{r.difficultyLevel}</span>}
                      <span className="text-gray-400">
                        {r.createdAt ? new Date(r.createdAt).toLocaleDateString('fr-FR') : ''}
                      </span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <a
                      href={`/recipes/${r.id}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="p-2 text-gray-400 hover:text-primary-500 transition-colors"
                      title="Prévisualiser"
                    >
                      <FiEye className="w-4 h-4" />
                    </a>
                    <button
                      onClick={() => approveMut.mutate(r.id)}
                      className="flex items-center gap-1.5 px-3 py-1.5 bg-green-500 hover:bg-green-600 text-white rounded-lg text-xs font-medium transition-colors"
                    >
                      <FiCheck className="w-3.5 h-3.5" /> Approuver
                    </button>
                    <button
                      onClick={() => { setRejectModal({ id: r.id, title: r.title }); setRejectReason(''); }}
                      className="flex items-center gap-1.5 px-3 py-1.5 bg-red-100 hover:bg-red-200 text-red-600 rounded-lg text-xs font-medium transition-colors"
                    >
                      <FiX className="w-3.5 h-3.5" /> Rejeter
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ── AI Recipe Generation ── */}
      {tab === 'ai' && (
        <div className="max-w-xl space-y-6">
          <div className="card p-6">
            <div className="flex items-center gap-2 mb-2">
              <FiZap className="w-5 h-5 text-primary-500" />
              <h2 className="font-display font-bold text-lg">Créer une recette avec l'IA</h2>
            </div>
            <p className="text-sm text-gray-500 mb-5">
              Entrez uniquement le nom de la recette. L'IA génère automatiquement les ingrédients,
              les étapes, la nutrition et toutes les données, puis la sauvegarde comme brouillon.
            </p>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">Nom de la recette</label>
                <input
                  className="input-field w-full text-base"
                  placeholder="ex: Ndolé camerounais, Poulet Yassa, Tiramisu..."
                  value={aiName}
                  onChange={(e) => setAiName(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter' && aiName.trim()) e.currentTarget.blur(); }}
                />
              </div>

              <Button
                className="w-full"
                isLoading={aiGenerating}
                disabled={!aiName.trim()}
                leftIcon={<FiZap className="w-4 h-4" />}
                onClick={async () => {
                  setAiGenerating(true);
                  setAiResult(null);
                  try {
                    const res = await apiClient.post(
                      `/admin/ai/generate-recipe`,
                      null,
                      { params: { recipeName: aiName.trim() } }
                    );
                    const recipe = (res as any).data?.data;
                    setAiResult(recipe);
                    toast.success(`Recette "${recipe?.title ?? aiName}" créée et sauvegardée !`);
                    qc.invalidateQueries({ queryKey: ['admin-stats'] });
                    setAiName('');
                  } catch (e: any) {
                    const msg = e?.response?.data?.message ?? e?.message ?? 'Erreur inconnue';
                    toast.error('Erreur : ' + msg);
                  } finally {
                    setAiGenerating(false);
                  }
                }}
              >
                {aiGenerating ? 'Génération en cours...' : 'Générer et sauvegarder'}
              </Button>
            </div>
          </div>

          {/* Result preview */}
          {aiResult && (
            <div className="card p-5">
              <div className="flex items-center gap-2 mb-4">
                <FiCheck className="w-4 h-4 text-green-500" />
                <h3 className="font-display font-bold text-green-700 dark:text-green-400">
                  Recette sauvegardée comme brouillon
                </h3>
              </div>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Titre</span>
                  <span className="font-medium">{aiResult.title}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Cuisine</span>
                  <span>{aiResult.cuisineType ?? '—'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Difficulté</span>
                  <span>{aiResult.difficultyLevel ?? '—'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Temps total</span>
                  <span>{(aiResult.prepTimeMinutes ?? 0) + (aiResult.cookTimeMinutes ?? 0)} min</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Ingrédients</span>
                  <span>{aiResult.ingredients?.length ?? 0}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Étapes</span>
                  <span>{aiResult.steps?.length ?? 0}</span>
                </div>
              </div>
              <p className="text-xs text-gray-400 mt-4">
                Retrouvez-la dans "Toutes les recettes" → statut DRAFT. Soumettez-la à la révision pour la publier.
              </p>
              <Button
                size="sm"
                variant="outline"
                className="mt-3 w-full"
                onClick={() => { setTab('recipes'); setAiResult(null); }}
              >
                Voir dans les recettes
              </Button>
            </div>
          )}
        </div>
      )}

      {/* ── All Recipes ── */}
      {tab === 'recipes' && (
        <div>
          {recipesLoading ? (
            <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
          ) : (
            <>
              <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-gray-700">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 dark:bg-gray-800">
                    <tr>
                      {['Recette', 'Auteur', 'Cuisine', 'Statut', 'Note', 'Vues', 'Créée le', 'Actions'].map((h) => (
                        <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                    {recipesData?.content?.map((r: any) => (
                      <tr key={r.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                        <td className="px-4 py-3 font-medium truncate max-w-[180px]">{r.title}</td>
                        <td className="px-4 py-3 text-gray-500 text-xs">{r.authorName ?? '—'}</td>
                        <td className="px-4 py-3 text-gray-500 text-xs">{r.cuisineType ?? '—'}</td>
                        <td className="px-4 py-3">
                          <span className={cn('badge text-xs', STATUS_COLORS[r.status] ?? '')}>
                            {r.status}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-xs">{r.avgRating ? Number(r.avgRating).toFixed(1) : '—'}</td>
                        <td className="px-4 py-3 text-xs text-gray-400">{r.viewCount ?? 0}</td>
                        <td className="px-4 py-3 text-xs text-gray-400">
                          {r.createdAt ? new Date(r.createdAt).toLocaleDateString('fr-FR') : '—'}
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-1">
                            <a
                              href={`/recipes/${r.id}`}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="p-1.5 text-gray-400 hover:text-primary-500 transition-colors"
                            >
                              <FiEye className="w-3.5 h-3.5" />
                            </a>
                            {r.status === 'PENDING_REVIEW' && (
                              <>
                                <button
                                  onClick={() => approveMut.mutate(r.id)}
                                  className="p-1.5 text-green-500 hover:text-green-700 hover:bg-green-50 dark:hover:bg-green-900/20 rounded-lg transition-colors"
                                  title="Approuver"
                                >
                                  <FiCheck className="w-3.5 h-3.5" />
                                </button>
                                <button
                                  onClick={() => { setRejectModal({ id: r.id, title: r.title }); setRejectReason(''); }}
                                  className="p-1.5 text-red-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                                  title="Rejeter"
                                >
                                  <FiSlash className="w-3.5 h-3.5" />
                                </button>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {recipesData?.totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-4">
                  <Button variant="ghost" disabled={recipePage === 0} onClick={() => setRecipePage(p => p - 1)}>Précédent</Button>
                  <span className="flex items-center text-sm text-gray-500">{recipePage + 1} / {recipesData.totalPages}</span>
                  <Button variant="ghost" disabled={recipePage >= recipesData.totalPages - 1} onClick={() => setRecipePage(p => p + 1)}>Suivant</Button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}

import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { FiCalendar, FiUsers } from 'react-icons/fi';
import { userApi, recipeApi, socialApi } from '@/lib/api';
import { useAuthStore } from '@/stores/authStore';
import Avatar from '@/components/ui/Avatar';
import Button from '@/components/ui/Button';
import RecipeCard from '@/components/recipe/RecipeCard';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { formatDate } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function ProfilePage() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const { user: currentUser } = useAuthStore();
  const isOwnProfile = currentUser?.id === id;

  const { data: profile, isLoading } = useQuery({
    queryKey: ['user', id],
    queryFn: () => userApi.getUserById(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const { data: recipes } = useQuery({
    queryKey: ['user-recipes', id],
    queryFn: () => recipeApi.getByUser(id!, 0).then((r) => r.data),
    enabled: !!id,
  });

  const handleFollow = async () => {
    try {
      await socialApi.followUser(id!);
      toast.success(`Following ${profile?.firstName}!`);
    } catch {
      toast.error('Failed');
    }
  };

  if (isLoading) return <div className="flex justify-center py-24"><LoadingSpinner size="lg" /></div>;
  if (!profile) return <div className="page-container text-center py-16">{t('common.error')}</div>;

  return (
    <div className="page-container max-w-4xl">
      {/* Profile header */}
      <div className="card p-6 md:p-8 mb-8">
        <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
          <Avatar src={profile.avatarUrl} name={`${profile.firstName} ${profile.lastName}`} size="xl" />
          <div className="flex-1 text-center md:text-left">
            <h1 className="text-2xl font-display font-bold">{profile.firstName} {profile.lastName}</h1>
            {profile.username && <p className="text-gray-500">@{profile.username}</p>}
            {profile.bio && <p className="text-sm text-gray-600 dark:text-gray-400 mt-2">{profile.bio}</p>}
            <div className="flex items-center justify-center md:justify-start gap-6 mt-4 text-sm">
              <div><span className="font-bold">{profile.recipesCount}</span> <span className="text-gray-500">{t('nav.recipes')}</span></div>
              <div><span className="font-bold">{profile.followersCount}</span> <span className="text-gray-500">{t('social.followers')}</span></div>
              <div><span className="font-bold">{profile.followingCount}</span> <span className="text-gray-500">{t('social.following')}</span></div>
            </div>
            <div className="flex items-center justify-center md:justify-start gap-2 mt-2 text-xs text-gray-400">
              <FiCalendar className="w-3 h-3" />
              Joined {formatDate(profile.createdAt)}
            </div>
            {/* Eco badges */}
            {profile.ecoBadges?.length > 0 && (
              <div className="flex items-center gap-2 mt-3">
                {profile.ecoBadges.map((badge) => (
                  <span key={badge.id} className="badge bg-green-100 text-green-700 text-xs" title={badge.description}>
                    🌱 {badge.name}
                  </span>
                ))}
              </div>
            )}
          </div>
          {!isOwnProfile && (
            <Button onClick={handleFollow} leftIcon={<FiUsers className="w-4 h-4" />}>
              {t('social.follow')}
            </Button>
          )}
        </div>
      </div>

      {/* User's recipes */}
      <h2 className="section-title mb-6">{t('nav.recipes')}</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {recipes?.data?.content?.map((recipe) => (
          <RecipeCard key={recipe.id} recipe={recipe} />
        ))}
      </div>
      {!recipes?.data?.content?.length && (
        <div className="text-center py-12 text-gray-500">{t('common.noResults')}</div>
      )}
    </div>
  );
}

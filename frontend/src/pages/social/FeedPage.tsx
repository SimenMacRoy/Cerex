import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { FiHeart, FiMessageCircle, FiShare2, FiSend, FiImage } from 'react-icons/fi';
import { socialApi } from '@/lib/api';
import { useAuthStore } from '@/stores/authStore';
import Avatar from '@/components/ui/Avatar';
import Button from '@/components/ui/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { formatRelativeTime, cn } from '@/lib/utils';
import toast from 'react-hot-toast';
import type { CreatePostRequest } from '@/types';

export default function FeedPage() {
  const { t } = useTranslation();
  const { user } = useAuthStore();
  const [newPostContent, setNewPostContent] = useState('');
  const [isPosting, setIsPosting] = useState(false);

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['social-feed'],
    queryFn: () => socialApi.getFeed(0).then((r) => r.data),
  });

  const handleCreatePost = async () => {
    if (!newPostContent.trim()) return;
    setIsPosting(true);
    try {
      const postData: CreatePostRequest = { postType: 'FOOD_PHOTO', content: newPostContent };
      await socialApi.createPost(postData);
      setNewPostContent('');
      refetch();
      toast.success('Post published! 🎉');
    } catch {
      toast.error('Failed to post');
    } finally {
      setIsPosting(false);
    }
  };

  const handleLike = async (postId: string) => {
    try {
      await socialApi.likePost(postId);
      refetch();
    } catch {
      toast.error('Failed');
    }
  };

  return (
    <div className="page-container max-w-2xl">
      <h1 className="section-title mb-8">{t('social.feed')}</h1>

      {/* Create post */}
      <div className="card p-4 mb-6">
        <div className="flex gap-3">
          <Avatar src={user?.avatarUrl} name={user?.firstName || 'U'} />
          <div className="flex-1">
            <textarea
              value={newPostContent}
              onChange={(e) => setNewPostContent(e.target.value)}
              placeholder={t('social.newPost')}
              className="input-field min-h-[80px] resize-none"
            />
            <div className="flex items-center justify-between mt-3">
              <button className="p-2 text-gray-400 hover:text-primary-500"><FiImage className="w-5 h-5" /></button>
              <Button size="sm" onClick={handleCreatePost} isLoading={isPosting} leftIcon={<FiSend className="w-4 h-4" />}>
                {t('social.post')}
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Feed */}
      {isLoading ? (
        <div className="flex justify-center py-12"><LoadingSpinner size="lg" /></div>
      ) : (
        <div className="space-y-4">
          {data?.data?.content?.map((post) => (
            <div key={post.id} className="card p-4">
              {/* Post header */}
              <div className="flex items-center gap-3 mb-3">
                <Avatar src={post.authorAvatarUrl} name={post.authorName} />
                <div>
                  <span className="font-medium text-sm">{post.authorName}</span>
                  <p className="text-xs text-gray-500">{formatRelativeTime(post.createdAt)}</p>
                </div>
              </div>

              {/* Content */}
              <p className="text-sm mb-3 leading-relaxed">{post.content}</p>

              {/* Images */}
              {post.imageUrls?.length > 0 && (
                <div className="grid grid-cols-2 gap-2 mb-3 rounded-xl overflow-hidden">
                  {post.imageUrls.slice(0, 4).map((url, i) => (
                    <img key={i} src={url} alt="" className="w-full h-48 object-cover" />
                  ))}
                </div>
              )}

              {/* Actions */}
              <div className="flex items-center gap-6 pt-3 border-t border-gray-100 dark:border-gray-700">
                <button onClick={() => handleLike(post.id)} className={cn('flex items-center gap-1.5 text-sm', post.isLiked ? 'text-red-500' : 'text-gray-500 hover:text-red-500')}>
                  <FiHeart className={cn('w-4 h-4', post.isLiked && 'fill-red-500')} />
                  {post.likesCount}
                </button>
                <button className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-primary-500">
                  <FiMessageCircle className="w-4 h-4" />
                  {post.commentsCount}
                </button>
                <button className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-primary-500">
                  <FiShare2 className="w-4 h-4" />
                  {post.sharesCount}
                </button>
              </div>
            </div>
          ))}
          {!data?.data?.content?.length && (
            <div className="text-center py-16 text-gray-500">
              <p className="text-lg">{t('common.noResults')}</p>
              <p className="text-sm mt-2">Follow users to see their posts here!</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

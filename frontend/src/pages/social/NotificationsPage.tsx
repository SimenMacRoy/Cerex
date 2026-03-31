import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FiBell, FiCheck } from 'react-icons/fi';
import { socialApi } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Avatar from '@/components/ui/Avatar';
import Button from '@/components/ui/Button';
import { formatRelativeTime, cn } from '@/lib/utils';

export default function NotificationsPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => socialApi.getNotifications(0).then((r) => r.data),
  });

  const markAllRead = useMutation({
    mutationFn: () => socialApi.markAllNotificationsRead(),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  });

  const markRead = useMutation({
    mutationFn: (id: string) => socialApi.markNotificationRead(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  });

  return (
    <div className="page-container max-w-2xl">
      <div className="flex items-center justify-between mb-8">
        <h1 className="section-title">{t('social.notifications')}</h1>
        <Button variant="ghost" size="sm" onClick={() => markAllRead.mutate()} leftIcon={<FiCheck className="w-4 h-4" />}>
          Mark all read
        </Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
      ) : (
        <div className="space-y-2">
          {data?.data?.content?.map((notif) => (
            <div
              key={notif.id}
              onClick={() => !notif.isRead && markRead.mutate(notif.id)}
              className={cn(
                'card p-4 flex items-start gap-3 cursor-pointer transition-colors',
                !notif.isRead && 'bg-primary-50/50 dark:bg-primary-900/10 border-primary-200 dark:border-primary-800',
              )}
            >
              {notif.fromUserAvatarUrl ? (
                <Avatar src={notif.fromUserAvatarUrl} name={notif.fromUserName || 'U'} size="sm" />
              ) : (
                <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center shrink-0">
                  <FiBell className="w-4 h-4 text-primary-500" />
                </div>
              )}
              <div className="flex-1 min-w-0">
                <p className="text-sm"><span className="font-medium">{notif.fromUserName}</span> {notif.message}</p>
                <p className="text-xs text-gray-400 mt-1">{formatRelativeTime(notif.createdAt)}</p>
              </div>
              {!notif.isRead && <div className="w-2 h-2 rounded-full bg-primary-500 shrink-0 mt-2" />}
            </div>
          ))}
          {!data?.data?.content?.length && (
            <div className="text-center py-16 text-gray-500">
              <FiBell className="w-12 h-12 mx-auto text-gray-300 dark:text-gray-600 mb-3" />
              <p>{t('social.noNotifications')}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

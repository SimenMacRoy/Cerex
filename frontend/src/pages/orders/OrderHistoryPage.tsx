import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { orderApi } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { getOrderStatusColor, formatDate, formatCurrency, cn } from '@/lib/utils';

export default function OrderHistoryPage() {
  const { t } = useTranslation();
  const { data, isLoading } = useQuery({
    queryKey: ['my-orders'],
    queryFn: () => orderApi.getMyOrders(0).then((r) => r.data),
  });

  return (
    <div className="page-container max-w-3xl">
      <h1 className="section-title mb-8">{t('order.orderHistory')}</h1>
      {isLoading ? (
        <div className="flex justify-center py-16"><LoadingSpinner size="lg" /></div>
      ) : (
        <div className="space-y-4">
          {data?.data?.content?.map((order) => (
            <Link key={order.id} to={`/orders/${order.id}/tracking`} className="card p-4 flex items-center justify-between hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-semibold">#{order.orderNumber}</span>
                  <span className={cn('badge text-xs', getOrderStatusColor(order.status))}>{order.status.replace(/_/g, ' ')}</span>
                </div>
                <p className="text-sm text-gray-500">{order.restaurantName} • {order.items?.length} items</p>
                <p className="text-xs text-gray-400 mt-1">{formatDate(order.createdAt)}</p>
              </div>
              <span className="font-bold text-lg">{formatCurrency(order.total)}</span>
            </Link>
          ))}
          {!data?.data?.content?.length && (
            <div className="text-center py-16 text-gray-500">
              <p className="text-lg">No orders yet</p>
              <p className="text-sm mt-2">{t('common.noResults')}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

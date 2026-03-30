import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { FiCheckCircle, FiClock, FiTruck, FiPackage } from 'react-icons/fi';
import { orderApi } from '@/lib/api';
import { useAuthStore } from '@/stores/authStore';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { cn, getOrderStatusColor } from '@/lib/utils';
import type { OrderTrackingUpdate } from '@/types';

export default function OrderTrackingPage() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const { accessToken } = useAuthStore();
  const [liveUpdate, setLiveUpdate] = useState<OrderTrackingUpdate | null>(null);

  const { data: order, isLoading } = useQuery({
    queryKey: ['order', id],
    queryFn: () => orderApi.getById(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  // WebSocket connection for live tracking
  useEffect(() => {
    if (!id || !accessToken) return;

    const client = new Client({
      brokerURL: `${import.meta.env.VITE_WS_URL?.replace('http', 'ws')}/ws`,
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      onConnect: () => {
        client.subscribe(`/topic/orders/${id}`, (message) => {
          const update: OrderTrackingUpdate = JSON.parse(message.body);
          setLiveUpdate(update);
        });
      },
    });

    client.activate();
    return () => { client.deactivate(); };
  }, [id, accessToken]);

  const currentStatus = liveUpdate?.status || order?.status || 'PENDING';

  const statusSteps = [
    { key: 'CONFIRMED', icon: FiCheckCircle, label: t('order.status.confirmed') },
    { key: 'PREPARING', icon: FiPackage, label: t('order.status.preparing') },
    { key: 'OUT_FOR_DELIVERY', icon: FiTruck, label: t('order.status.delivering') },
    { key: 'DELIVERED', icon: FiCheckCircle, label: t('order.status.delivered') },
  ];

  const currentStepIndex = statusSteps.findIndex((s) => s.key === currentStatus);

  if (isLoading) return <div className="flex justify-center py-24"><LoadingSpinner size="lg" /></div>;

  return (
    <div className="page-container max-w-2xl">
      <h1 className="section-title mb-2">{t('order.tracking')}</h1>
      <p className="text-sm text-gray-500 mb-8">Order #{order?.orderNumber}</p>

      {/* Status badge */}
      <div className="text-center mb-8">
        <span className={cn('badge text-lg px-6 py-2', getOrderStatusColor(currentStatus))}>
          {currentStatus.replace(/_/g, ' ')}
        </span>
        {liveUpdate?.message && (
          <p className="text-sm text-gray-500 mt-3">{liveUpdate.message}</p>
        )}
        {liveUpdate?.estimatedMinutes && (
          <p className="text-sm text-primary-500 mt-1 flex items-center justify-center gap-1">
            <FiClock className="w-4 h-4" />
            ~{liveUpdate.estimatedMinutes} min remaining
          </p>
        )}
      </div>

      {/* Progress steps */}
      <div className="flex items-center justify-between mb-12">
        {statusSteps.map((step, i) => {
          const isActive = i <= currentStepIndex;
          const Icon = step.icon;
          return (
            <div key={step.key} className="flex flex-col items-center flex-1">
              <div className={cn(
                'w-12 h-12 rounded-full flex items-center justify-center mb-2 transition-colors',
                isActive ? 'bg-primary-500 text-white' : 'bg-gray-200 dark:bg-gray-700 text-gray-400',
              )}>
                <Icon className="w-6 h-6" />
              </div>
              <span className={cn('text-xs font-medium text-center', isActive ? 'text-primary-600' : 'text-gray-400')}>
                {step.label}
              </span>
              {i < statusSteps.length - 1 && (
                <div className={cn('h-0.5 w-full mt-[-30px] mb-6', i < currentStepIndex ? 'bg-primary-500' : 'bg-gray-200 dark:bg-gray-700')} />
              )}
            </div>
          );
        })}
      </div>

      {/* Order details */}
      {order && (
        <div className="card p-6">
          <h2 className="font-semibold mb-4">Order Details</h2>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between"><span className="text-gray-500">Restaurant</span><span>{order.restaurantName}</span></div>
            <div className="flex justify-between"><span className="text-gray-500">Type</span><span>{order.orderType}</span></div>
            {order.deliveryAddress && <div className="flex justify-between"><span className="text-gray-500">Address</span><span>{order.deliveryAddress}</span></div>}
            <hr className="border-gray-200 dark:border-gray-700" />
            {order.items?.map((item) => (
              <div key={item.id} className="flex justify-between">
                <span>{item.quantity}x {item.name}</span>
                <span className="font-medium">{item.totalPrice?.toFixed(2)} €</span>
              </div>
            ))}
            <hr className="border-gray-200 dark:border-gray-700" />
            <div className="flex justify-between font-bold"><span>{t('order.total')}</span><span>{order.total?.toFixed(2)} €</span></div>
          </div>
        </div>
      )}
    </div>
  );
}

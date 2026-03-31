import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { FiClock, FiMapPin, FiPhone, FiPlus, FiMinus, FiStar } from 'react-icons/fi';
import { restaurantApi } from '@/lib/api';
import { useCartStore } from '@/stores/cartStore';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import StarRating from '@/components/ui/StarRating';
import Button from '@/components/ui/Button';
import { formatCurrency, cn } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function RestaurantDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const { addItem, items } = useCartStore();

  const { data: restaurant, isLoading } = useQuery({
    queryKey: ['restaurant', id],
    queryFn: () => restaurantApi.getById(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const { data: menus } = useQuery({
    queryKey: ['restaurant-menu', id],
    queryFn: () => restaurantApi.getMenu(id!).then((r) => r.data.data),
    enabled: !!id,
  });

  const { data: reviews } = useQuery({
    queryKey: ['restaurant-reviews', id],
    queryFn: () => restaurantApi.getReviews(id!, 0).then((r) => r.data),
    enabled: !!id,
  });

  if (isLoading) return <div className="flex justify-center py-24"><LoadingSpinner size="lg" /></div>;
  if (!restaurant) return <div className="page-container text-center py-16">{t('common.error')}</div>;

  const handleAddToCart = (item: { id: string; name: string; description: string; price: number; imageUrl?: string; category: string; isAvailable: boolean; isPopular: boolean }) => {
    addItem(item as import('@/types').MenuItem, restaurant.id, restaurant.name);
    toast.success(`${item.name} added to cart!`);
  };

  return (
    <div className="page-container max-w-5xl">
      {/* Header */}
      <div className="relative h-56 md:h-72 rounded-2xl overflow-hidden mb-8 bg-gray-200 dark:bg-gray-700">
        {restaurant.coverImageUrl && <img src={restaurant.coverImageUrl} alt={restaurant.name} className="w-full h-full object-cover" />}
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
        <div className="absolute bottom-6 left-6">
          {restaurant.isOpen && <span className="badge bg-green-500 text-white text-xs mb-2">Open Now</span>}
          <h1 className="text-3xl font-display font-bold text-white">{restaurant.name}</h1>
          <p className="text-white/80 text-sm mt-1">{restaurant.cuisineType} cuisine • {restaurant.address}</p>
        </div>
      </div>

      {/* Info row */}
      <div className="flex flex-wrap items-center gap-6 mb-8">
        <StarRating rating={restaurant.rating} />
        <span className="text-sm text-gray-500">({restaurant.reviewsCount} {t('restaurant.reviews')})</span>
        <span className="flex items-center gap-1 text-sm text-gray-500"><FiClock className="w-4 h-4" />{restaurant.deliveryTimeMinutes} min</span>
        <span className="flex items-center gap-1 text-sm text-gray-500"><FiMapPin className="w-4 h-4" />{restaurant.city}</span>
        <span className="flex items-center gap-1 text-sm text-gray-500"><FiPhone className="w-4 h-4" />{restaurant.phone}</span>
      </div>

      {/* Menu */}
      <h2 className="text-xl font-display font-bold mb-6">{t('restaurant.menu')}</h2>
      {menus?.map((menu) => (
        <div key={menu.id} className="mb-8">
          <h3 className="text-lg font-semibold mb-4 text-primary-500">{menu.name}</h3>
          <div className="grid md:grid-cols-2 gap-4">
            {menu.items?.map((item) => {
              const cartQty = items.find((ci) => ci.menuItem.id === item.id)?.quantity || 0;
              return (
                <div key={item.id} className={cn('card p-4 flex gap-4', !item.isAvailable && 'opacity-50')}>
                  {item.imageUrl && <img src={item.imageUrl} alt={item.name} className="w-24 h-24 rounded-xl object-cover shrink-0" />}
                  <div className="flex-1">
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="font-medium">{item.name}</h4>
                        {item.isPopular && <span className="badge-accent text-xs">🔥 Popular</span>}
                      </div>
                      <span className="font-bold text-primary-500">{formatCurrency(item.price)}</span>
                    </div>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-1 line-clamp-2">{item.description}</p>
                    <div className="flex items-center justify-between mt-3">
                      {item.calories && <span className="text-xs text-gray-400">{item.calories} cal</span>}
                      {item.isAvailable ? (
                        cartQty > 0 ? (
                          <div className="flex items-center gap-2">
                            <button onClick={() => useCartStore.getState().updateQuantity(item.id, cartQty - 1)} className="p-1 rounded bg-gray-100 dark:bg-gray-800"><FiMinus className="w-3 h-3" /></button>
                            <span className="text-sm font-medium w-6 text-center">{cartQty}</span>
                            <button onClick={() => handleAddToCart(item)} className="p-1 rounded bg-primary-100 text-primary-600"><FiPlus className="w-3 h-3" /></button>
                          </div>
                        ) : (
                          <Button size="sm" variant="outline" leftIcon={<FiPlus className="w-3 h-3" />} onClick={() => handleAddToCart(item)}>
                            {t('restaurant.addToCart')}
                          </Button>
                        )
                      ) : (
                        <span className="text-xs text-red-400">Unavailable</span>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      ))}

      {/* Reviews */}
      <h2 className="text-xl font-display font-bold mt-12 mb-6">{t('restaurant.reviews')}</h2>
      <div className="space-y-4">
        {reviews?.data?.content?.map((review) => (
          <div key={review.id} className="card p-4">
            <div className="flex items-center gap-3 mb-2">
              <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center text-sm font-bold text-primary-600">
                {review.userName[0]}
              </div>
              <div>
                <span className="font-medium text-sm">{review.userName}</span>
                <div className="flex items-center gap-1">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <FiStar key={i} className={cn('w-3 h-3', i < review.rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300')} />
                  ))}
                </div>
              </div>
            </div>
            <p className="text-sm text-gray-600 dark:text-gray-400">{review.comment}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

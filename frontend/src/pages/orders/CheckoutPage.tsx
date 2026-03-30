import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { FiCreditCard, FiMapPin } from 'react-icons/fi';
import { useCartStore } from '@/stores/cartStore';
import { orderApi } from '@/lib/api';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { formatCurrency } from '@/lib/utils';

export default function CheckoutPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { items, restaurantId, getTotal, clearCart } = useCartStore();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [address, setAddress] = useState('');
  const total = getTotal();

  const handlePlaceOrder = async () => {
    if (!restaurantId || items.length === 0) return;
    setIsSubmitting(true);
    try {
      const response = await orderApi.create({
        restaurantId,
        orderType: 'DELIVERY',
        items: items.map((i) => ({ menuItemId: i.menuItem.id, quantity: i.quantity, notes: i.notes })),
        deliveryAddress: address,
        paymentMethod: 'CARD',
      });
      clearCart();
      toast.success('Order placed! 🎉');
      navigate(`/orders/${response.data.data.id}/tracking`);
    } catch {
      toast.error('Failed to place order');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="page-container max-w-2xl">
      <h1 className="section-title mb-8">{t('order.checkout')}</h1>

      <div className="space-y-6">
        {/* Delivery Address */}
        <div className="card p-6">
          <h2 className="font-semibold mb-4 flex items-center gap-2"><FiMapPin className="w-5 h-5 text-primary-500" />{t('order.deliveryAddress')}</h2>
          <Input placeholder="123 Main Street, City" value={address} onChange={(e) => setAddress(e.target.value)} />
        </div>

        {/* Payment */}
        <div className="card p-6">
          <h2 className="font-semibold mb-4 flex items-center gap-2"><FiCreditCard className="w-5 h-5 text-primary-500" />{t('order.paymentMethod')}</h2>
          <div className="space-y-3">
            <label className="flex items-center gap-3 p-3 border border-primary-500 rounded-xl cursor-pointer bg-primary-50 dark:bg-primary-900/20">
              <input type="radio" name="payment" defaultChecked className="text-primary-500" />
              <span className="text-sm font-medium">Credit Card</span>
            </label>
            <label className="flex items-center gap-3 p-3 border border-gray-200 dark:border-gray-700 rounded-xl cursor-pointer">
              <input type="radio" name="payment" className="text-primary-500" />
              <span className="text-sm font-medium">Cash on Delivery</span>
            </label>
          </div>
        </div>

        {/* Order Summary */}
        <div className="card p-6">
          <h2 className="font-semibold mb-4">Order Summary</h2>
          <div className="space-y-2">
            {items.map((item) => (
              <div key={item.menuItem.id} className="flex justify-between text-sm">
                <span>{item.quantity}x {item.menuItem.name}</span>
                <span>{formatCurrency(item.menuItem.price * item.quantity)}</span>
              </div>
            ))}
            <hr className="border-gray-200 dark:border-gray-700 my-3" />
            <div className="flex justify-between text-sm"><span>{t('order.subtotal')}</span><span>{formatCurrency(total)}</span></div>
            <div className="flex justify-between text-sm"><span>{t('order.delivery_fee')}</span><span>{formatCurrency(2.99)}</span></div>
            <div className="flex justify-between font-bold text-lg mt-2"><span>{t('order.total')}</span><span>{formatCurrency(total + 2.99 + total * 0.1)}</span></div>
          </div>
        </div>

        <Button onClick={handlePlaceOrder} isLoading={isSubmitting} size="lg" className="w-full">
          {t('order.placeOrder')}
        </Button>
      </div>
    </div>
  );
}

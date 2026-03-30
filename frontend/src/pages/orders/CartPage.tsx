import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { FiTrash2, FiPlus, FiMinus, FiShoppingBag } from 'react-icons/fi';
import { useCartStore } from '@/stores/cartStore';
import Button from '@/components/ui/Button';
import { formatCurrency } from '@/lib/utils';

export default function CartPage() {
  const { t } = useTranslation();
  const { items, restaurantName, updateQuantity, removeItem, clearCart, getTotal } = useCartStore();
  const total = getTotal();

  if (items.length === 0) {
    return (
      <div className="page-container text-center py-20">
        <FiShoppingBag className="w-16 h-16 mx-auto text-gray-300 dark:text-gray-600 mb-4" />
        <h2 className="text-xl font-semibold mb-2">{t('order.cart')} is empty</h2>
        <p className="text-gray-500 dark:text-gray-400 mb-6">Add items from a restaurant to get started!</p>
        <Link to="/restaurants"><Button variant="primary">Browse Restaurants</Button></Link>
      </div>
    );
  }

  return (
    <div className="page-container max-w-3xl">
      <h1 className="section-title mb-2">{t('order.cart')}</h1>
      <p className="text-sm text-gray-500 dark:text-gray-400 mb-8">from {restaurantName}</p>

      <div className="space-y-4 mb-8">
        {items.map((item) => (
          <div key={item.menuItem.id} className="card p-4 flex items-center gap-4">
            {item.menuItem.imageUrl && <img src={item.menuItem.imageUrl} alt={item.menuItem.name} className="w-16 h-16 rounded-xl object-cover" />}
            <div className="flex-1">
              <h3 className="font-medium">{item.menuItem.name}</h3>
              <p className="text-sm text-primary-500 font-semibold">{formatCurrency(item.menuItem.price)}</p>
            </div>
            <div className="flex items-center gap-2">
              <button onClick={() => updateQuantity(item.menuItem.id, item.quantity - 1)} className="p-1.5 rounded-lg bg-gray-100 dark:bg-gray-800 hover:bg-gray-200"><FiMinus className="w-4 h-4" /></button>
              <span className="w-8 text-center font-medium">{item.quantity}</span>
              <button onClick={() => updateQuantity(item.menuItem.id, item.quantity + 1)} className="p-1.5 rounded-lg bg-gray-100 dark:bg-gray-800 hover:bg-gray-200"><FiPlus className="w-4 h-4" /></button>
            </div>
            <span className="font-semibold w-20 text-right">{formatCurrency(item.menuItem.price * item.quantity)}</span>
            <button onClick={() => removeItem(item.menuItem.id)} className="p-2 text-red-400 hover:text-red-600"><FiTrash2 className="w-4 h-4" /></button>
          </div>
        ))}
      </div>

      {/* Summary */}
      <div className="card p-6 space-y-3">
        <div className="flex justify-between text-sm"><span>{t('order.subtotal')}</span><span>{formatCurrency(total)}</span></div>
        <div className="flex justify-between text-sm"><span>{t('order.delivery_fee')}</span><span>{formatCurrency(2.99)}</span></div>
        <div className="flex justify-between text-sm"><span>{t('order.tax')}</span><span>{formatCurrency(total * 0.1)}</span></div>
        <hr className="border-gray-200 dark:border-gray-700" />
        <div className="flex justify-between font-bold text-lg"><span>{t('order.total')}</span><span>{formatCurrency(total + 2.99 + total * 0.1)}</span></div>
        <div className="flex gap-3 pt-3">
          <Button variant="ghost" onClick={clearCart}>{t('common.delete')} All</Button>
          <Link to="/checkout" className="flex-1"><Button className="w-full" size="lg">{t('order.checkout')}</Button></Link>
        </div>
      </div>
    </div>
  );
}

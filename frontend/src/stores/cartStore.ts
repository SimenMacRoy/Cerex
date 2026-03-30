import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { CartItem, MenuItem } from '@/types';

interface CartState {
  items: CartItem[];
  restaurantId: string | null;
  restaurantName: string | null;

  // Actions
  addItem: (item: MenuItem, restaurantId: string, restaurantName: string) => void;
  removeItem: (menuItemId: string) => void;
  updateQuantity: (menuItemId: string, quantity: number) => void;
  clearCart: () => void;
  getTotal: () => number;
  getItemCount: () => number;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      restaurantId: null,
      restaurantName: null,

      addItem: (menuItem, restaurantId, restaurantName) => {
        const { items, restaurantId: currentRestId } = get();

        // If adding from a different restaurant, clear cart first
        if (currentRestId && currentRestId !== restaurantId) {
          set({
            items: [{ menuItem, quantity: 1, restaurantId, restaurantName }],
            restaurantId,
            restaurantName,
          });
          return;
        }

        const existingIndex = items.findIndex((i) => i.menuItem.id === menuItem.id);
        if (existingIndex >= 0) {
          const updated = [...items];
          updated[existingIndex] = {
            ...updated[existingIndex],
            quantity: updated[existingIndex].quantity + 1,
          };
          set({ items: updated });
        } else {
          set({
            items: [...items, { menuItem, quantity: 1, restaurantId, restaurantName }],
            restaurantId,
            restaurantName,
          });
        }
      },

      removeItem: (menuItemId) => {
        const newItems = get().items.filter((i) => i.menuItem.id !== menuItemId);
        if (newItems.length === 0) {
          set({ items: [], restaurantId: null, restaurantName: null });
        } else {
          set({ items: newItems });
        }
      },

      updateQuantity: (menuItemId, quantity) => {
        if (quantity <= 0) {
          get().removeItem(menuItemId);
          return;
        }
        const updated = get().items.map((item) =>
          item.menuItem.id === menuItemId ? { ...item, quantity } : item,
        );
        set({ items: updated });
      },

      clearCart: () => set({ items: [], restaurantId: null, restaurantName: null }),

      getTotal: () =>
        get().items.reduce((sum, item) => sum + item.menuItem.price * item.quantity, 0),

      getItemCount: () =>
        get().items.reduce((sum, item) => sum + item.quantity, 0),
    }),
    {
      name: 'cerex-cart',
    },
  ),
);

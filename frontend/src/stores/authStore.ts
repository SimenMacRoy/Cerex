import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UserProfile, AuthResponse } from '@/types';
import { authApi, userApi } from '@/lib/api';

interface AuthState {
  user: UserProfile | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (email: string, password: string) => Promise<void>;
  register: (firstName: string, lastName: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  fetchProfile: () => Promise<void>;
  updateProfile: (data: Partial<UserProfile>) => void;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      login: async (email, password) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authApi.login({ email, password });
          const data: AuthResponse = response.data.data;
          set({
            user: data.user,
            accessToken: data.accessToken,
            refreshToken: data.refreshToken,
            isAuthenticated: true,
            isLoading: false,
          });
        } catch (err: unknown) {
          const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Login failed';
          set({ error: message, isLoading: false });
          throw err;
        }
      },

      register: async (firstName, lastName, email, password) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authApi.register({ firstName, lastName, email, password });
          const data: AuthResponse = response.data.data;
          set({
            user: data.user,
            accessToken: data.accessToken,
            refreshToken: data.refreshToken,
            isAuthenticated: true,
            isLoading: false,
          });
        } catch (err: unknown) {
          const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Registration failed';
          set({ error: message, isLoading: false });
          throw err;
        }
      },

      logout: () => {
        authApi.logout().catch(() => {});
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
          error: null,
        });
      },

      setTokens: (accessToken, refreshToken) => {
        set({ accessToken, refreshToken });
      },

      fetchProfile: async () => {
        if (!get().accessToken) return;
        try {
          const response = await userApi.getProfile();
          set({ user: response.data.data });
        } catch {
          // Token might be expired, logout
          get().logout();
        }
      },

      updateProfile: (data) => {
        const current = get().user;
        if (current) {
          set({ user: { ...current, ...data } });
        }
      },

      clearError: () => set({ error: null }),
    }),
    {
      name: 'cerex-auth',
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);

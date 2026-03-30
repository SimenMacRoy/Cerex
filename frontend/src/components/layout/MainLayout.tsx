import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import {
  FiHome, FiSearch, FiShoppingCart, FiBell, FiUser, FiMenu, FiX,
  FiSun, FiMoon, FiGlobe, FiLogOut, FiBookOpen, FiShoppingBag,
  FiUsers, FiMapPin,
} from 'react-icons/fi';
import { useAuthStore } from '@/stores/authStore';
import { useThemeStore } from '@/stores/themeStore';
import { useCartStore } from '@/stores/cartStore';
import Avatar from '@/components/ui/Avatar';
import { cn } from '@/lib/utils';

export default function MainLayout() {
  const { t, i18n } = useTranslation();
  const { user, isAuthenticated, logout } = useAuthStore();
  const { isDark, toggleTheme } = useThemeStore();
  const itemCount = useCartStore((s) => s.getItemCount());
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);

  const toggleLanguage = () => {
    i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr');
  };

  const navLinks = [
    { to: '/', label: t('nav.home'), icon: FiHome },
    { to: '/recipes', label: t('nav.recipes'), icon: FiBookOpen },
    { to: '/restaurants', label: t('nav.restaurants'), icon: FiMapPin },
    { to: '/grocery', label: t('nav.grocery'), icon: FiShoppingBag },
    { to: '/feed', label: t('nav.social'), icon: FiUsers, auth: true },
  ];

  return (
    <div className="min-h-screen flex flex-col">
      {/* ─── Top Navbar ─────────────────────────────────── */}
      <header className="sticky top-0 z-50 bg-white/80 dark:bg-cerex-dark/80 backdrop-blur-lg border-b border-gray-200 dark:border-gray-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <Link to="/" className="flex items-center gap-2">
              <span className="text-2xl font-display font-bold text-primary-500">🍳 Cerex</span>
            </Link>

            {/* Desktop Navigation */}
            <nav className="hidden md:flex items-center gap-1">
              {navLinks
                .filter((l) => !l.auth || isAuthenticated)
                .map((link) => (
                  <Link
                    key={link.to}
                    to={link.to}
                    className="flex items-center gap-1.5 px-3 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 hover:text-primary-500 dark:hover:text-primary-400 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                  >
                    <link.icon className="w-4 h-4" />
                    {link.label}
                  </Link>
                ))}
            </nav>

            {/* Search bar (desktop) */}
            <div className="hidden lg:flex items-center flex-1 max-w-md mx-6">
              <div
                className="w-full relative cursor-pointer"
                onClick={() => navigate('/search')}
              >
                <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                <div className="w-full pl-10 pr-4 py-2 bg-gray-100 dark:bg-gray-800 rounded-xl text-sm text-gray-400 dark:text-gray-500">
                  {t('nav.search')}
                </div>
              </div>
            </div>

            {/* Right actions */}
            <div className="flex items-center gap-2">
              {/* Theme toggle */}
              <button
                onClick={toggleTheme}
                className="p-2 text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
                aria-label="Toggle theme"
              >
                {isDark ? <FiSun className="w-5 h-5" /> : <FiMoon className="w-5 h-5" />}
              </button>

              {/* Language toggle */}
              <button
                onClick={toggleLanguage}
                className="p-2 text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors text-xs font-bold"
                aria-label="Toggle language"
              >
                <FiGlobe className="w-5 h-5" />
              </button>

              {/* Cart */}
              <Link to="/cart" className="relative p-2 text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors">
                <FiShoppingCart className="w-5 h-5" />
                {itemCount > 0 && (
                  <span className="absolute -top-0.5 -right-0.5 w-5 h-5 flex items-center justify-center bg-primary-500 text-white text-xs font-bold rounded-full">
                    {itemCount}
                  </span>
                )}
              </Link>

              {isAuthenticated ? (
                <>
                  {/* Notifications */}
                  <Link to="/notifications" className="p-2 text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors">
                    <FiBell className="w-5 h-5" />
                  </Link>

                  {/* Profile dropdown */}
                  <div className="relative">
                    <button
                      onClick={() => setProfileMenuOpen(!profileMenuOpen)}
                      className="flex items-center gap-2 p-1 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                    >
                      <Avatar src={user?.avatarUrl} name={user?.firstName || 'U'} size="sm" />
                    </button>
                    {profileMenuOpen && (
                      <div className="absolute right-0 mt-2 w-56 bg-white dark:bg-cerex-medium rounded-xl shadow-xl border border-gray-200 dark:border-gray-700 py-2 animate-scale-in z-50">
                        <div className="px-4 py-2 border-b border-gray-100 dark:border-gray-700">
                          <p className="font-medium text-sm">{user?.firstName} {user?.lastName}</p>
                          <p className="text-xs text-gray-500 dark:text-gray-400">{user?.email}</p>
                        </div>
                        <Link
                          to={`/profile/${user?.id}`}
                          className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700"
                          onClick={() => setProfileMenuOpen(false)}
                        >
                          <FiUser className="w-4 h-4" />
                          {t('nav.profile')}
                        </Link>
                        <Link
                          to="/orders"
                          className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700"
                          onClick={() => setProfileMenuOpen(false)}
                        >
                          <FiShoppingBag className="w-4 h-4" />
                          {t('nav.orders')}
                        </Link>
                        <hr className="my-1 border-gray-100 dark:border-gray-700" />
                        <button
                          onClick={() => { logout(); setProfileMenuOpen(false); navigate('/'); }}
                          className="flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 w-full"
                        >
                          <FiLogOut className="w-4 h-4" />
                          {t('auth.logout')}
                        </button>
                      </div>
                    )}
                  </div>
                </>
              ) : (
                <div className="hidden sm:flex items-center gap-2">
                  <Link to="/login" className="btn-ghost text-sm">{t('auth.login')}</Link>
                  <Link to="/register" className="btn-primary text-sm">{t('auth.register')}</Link>
                </div>
              )}

              {/* Mobile menu toggle */}
              <button
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="md:hidden p-2 text-gray-500 dark:text-gray-400"
              >
                {mobileMenuOpen ? <FiX className="w-6 h-6" /> : <FiMenu className="w-6 h-6" />}
              </button>
            </div>
          </div>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-cerex-dark animate-slide-down">
            <nav className="px-4 py-3 space-y-1">
              {navLinks
                .filter((l) => !l.auth || isAuthenticated)
                .map((link) => (
                  <Link
                    key={link.to}
                    to={link.to}
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center gap-3 px-3 py-2.5 text-sm font-medium text-gray-600 dark:text-gray-300 hover:text-primary-500 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800"
                  >
                    <link.icon className="w-5 h-5" />
                    {link.label}
                  </Link>
                ))}
              {!isAuthenticated && (
                <div className="pt-3 border-t border-gray-200 dark:border-gray-700 flex gap-2">
                  <Link to="/login" className="btn-outline flex-1 text-center text-sm" onClick={() => setMobileMenuOpen(false)}>{t('auth.login')}</Link>
                  <Link to="/register" className="btn-primary flex-1 text-center text-sm" onClick={() => setMobileMenuOpen(false)}>{t('auth.register')}</Link>
                </div>
              )}
            </nav>
          </div>
        )}
      </header>

      {/* ─── Main Content ───────────────────────────────── */}
      <main className="flex-1">
        <Outlet />
      </main>

      {/* ─── Footer ─────────────────────────────────────── */}
      <footer className="bg-white dark:bg-cerex-medium border-t border-gray-200 dark:border-gray-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            <div>
              <h3 className="font-display font-bold text-lg text-primary-500 mb-3">🍳 Cerex</h3>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                {t('app.description')}
              </p>
            </div>
            <div>
              <h4 className="font-semibold text-sm mb-3">{t('nav.recipes')}</h4>
              <div className="space-y-2">
                <Link to="/recipes" className="block text-sm text-gray-500 dark:text-gray-400 hover:text-primary-500">{t('home.trending')}</Link>
                <Link to="/recipes/create" className="block text-sm text-gray-500 dark:text-gray-400 hover:text-primary-500">{t('recipe.create')}</Link>
              </div>
            </div>
            <div>
              <h4 className="font-semibold text-sm mb-3">{t('nav.restaurants')}</h4>
              <div className="space-y-2">
                <Link to="/restaurants" className="block text-sm text-gray-500 dark:text-gray-400 hover:text-primary-500">{t('home.nearYou')}</Link>
                <Link to="/grocery" className="block text-sm text-gray-500 dark:text-gray-400 hover:text-primary-500">{t('nav.grocery')}</Link>
              </div>
            </div>
            <div>
              <h4 className="font-semibold text-sm mb-3">{t('nav.social')}</h4>
              <div className="space-y-2">
                <Link to="/feed" className="block text-sm text-gray-500 dark:text-gray-400 hover:text-primary-500">{t('social.feed')}</Link>
                <Link to="/notifications" className="block text-sm text-gray-500 dark:text-gray-400 hover:text-primary-500">{t('social.notifications')}</Link>
              </div>
            </div>
          </div>
          <div className={cn('mt-8 pt-6 border-t border-gray-200 dark:border-gray-700 text-center text-sm text-gray-500 dark:text-gray-400')}>
            © {new Date().getFullYear()} Cerex. All rights reserved.
          </div>
        </div>
      </footer>

      {/* ─── Mobile Bottom Navigation ───────────────────── */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white dark:bg-cerex-dark border-t border-gray-200 dark:border-gray-700 z-50">
        <div className="flex items-center justify-around py-2">
          {[
            { to: '/', icon: FiHome, label: t('nav.home') },
            { to: '/search', icon: FiSearch, label: t('common.search') },
            { to: '/recipes', icon: FiBookOpen, label: t('nav.recipes') },
            { to: '/cart', icon: FiShoppingCart, label: t('nav.cart') },
            { to: isAuthenticated ? `/profile/${user?.id}` : '/login', icon: FiUser, label: t('nav.profile') },
          ].map((item) => (
            <Link
              key={item.to}
              to={item.to}
              className="flex flex-col items-center gap-0.5 px-3 py-1 text-gray-500 dark:text-gray-400 hover:text-primary-500 transition-colors"
            >
              <item.icon className="w-5 h-5" />
              <span className="text-[10px] font-medium">{item.label}</span>
            </Link>
          ))}
        </div>
      </nav>
    </div>
  );
}

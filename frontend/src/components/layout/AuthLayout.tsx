import { Outlet, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { FiGlobe } from 'react-icons/fi';

export default function AuthLayout() {
  const { i18n } = useTranslation();

  const toggleLanguage = () => {
    i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr');
  };

  return (
    <div className="min-h-screen flex">
      {/* Left side - branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-primary-500 via-primary-600 to-cerex-accent relative overflow-hidden">
        <div className="absolute inset-0 bg-black/10" />
        <div className="relative z-10 flex flex-col justify-center items-center p-12 text-white">
          <h1 className="text-5xl font-display font-bold mb-4">🍳 Cerex</h1>
          <p className="text-xl text-white/90 text-center max-w-md font-light">
            La plateforme mondiale de recettes, gastronomie et commandes alimentaires intelligentes
          </p>
          <div className="mt-12 grid grid-cols-3 gap-8 text-center">
            <div>
              <div className="text-3xl font-bold">1M+</div>
              <div className="text-sm text-white/70">Recettes</div>
            </div>
            <div>
              <div className="text-3xl font-bold">50K+</div>
              <div className="text-sm text-white/70">Restaurants</div>
            </div>
            <div>
              <div className="text-3xl font-bold">200+</div>
              <div className="text-sm text-white/70">Pays</div>
            </div>
          </div>
        </div>
        {/* Decorative circles */}
        <div className="absolute -bottom-24 -left-24 w-96 h-96 rounded-full bg-white/5" />
        <div className="absolute -top-12 -right-12 w-64 h-64 rounded-full bg-white/5" />
      </div>

      {/* Right side - form */}
      <div className="flex-1 flex flex-col">
        <div className="flex items-center justify-between p-6">
          <Link to="/" className="text-2xl font-display font-bold text-primary-500 lg:hidden">
            🍳 Cerex
          </Link>
          <button
            onClick={toggleLanguage}
            className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-gray-500 hover:text-gray-700 dark:text-gray-400 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          >
            <FiGlobe className="w-4 h-4" />
            {i18n.language === 'fr' ? 'EN' : 'FR'}
          </button>
        </div>
        <div className="flex-1 flex items-center justify-center p-6">
          <div className="w-full max-w-md">
            <Outlet />
          </div>
        </div>
      </div>
    </div>
  );
}

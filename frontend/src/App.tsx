import { Toaster } from 'react-hot-toast';
import { useThemeStore } from '@/stores/themeStore';
import AppRoutes from '@/routes';

function App() {
  const { isDark } = useThemeStore();

  return (
    <div className={isDark ? 'dark' : ''}>
      <div className="min-h-screen bg-cerex-cream dark:bg-cerex-dark text-cerex-dark dark:text-cerex-cream transition-colors duration-300">
        <AppRoutes />
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              borderRadius: '12px',
              padding: '12px 16px',
              fontSize: '14px',
            },
          }}
        />
      </div>
    </div>
  );
}

export default App;

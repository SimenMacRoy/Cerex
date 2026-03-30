export default function LoadingScreen() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-cerex-cream dark:bg-cerex-dark">
      <div className="text-center">
        <div className="relative w-16 h-16 mx-auto mb-4">
          <div className="absolute inset-0 rounded-full border-4 border-primary-200 dark:border-primary-800"></div>
          <div className="absolute inset-0 rounded-full border-4 border-transparent border-t-primary-500 animate-spin"></div>
        </div>
        <p className="text-lg font-display text-primary-600 dark:text-primary-400 animate-pulse">
          Cerex
        </p>
      </div>
    </div>
  );
}

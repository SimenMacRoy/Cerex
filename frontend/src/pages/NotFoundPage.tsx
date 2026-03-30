import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Button from '@/components/ui/Button';

export default function NotFoundPage() {
  const { t } = useTranslation();

  return (
    <div className="page-container text-center py-24">
      <div className="text-8xl mb-4">🍽️</div>
      <h1 className="text-4xl font-display font-bold mb-4">404</h1>
      <p className="text-lg text-gray-500 dark:text-gray-400 mb-8">
        Oops! This page seems to have been eaten...
      </p>
      <Link to="/">
        <Button size="lg">{t('common.back')} to Home</Button>
      </Link>
    </div>
  );
}

import { cn } from '@/lib/utils';
import { FiStar } from 'react-icons/fi';
import { FaStar, FaStarHalfAlt } from 'react-icons/fa';

interface StarRatingProps {
  rating: number;
  maxStars?: number;
  size?: 'sm' | 'md' | 'lg';
  showValue?: boolean;
  className?: string;
}

export default function StarRating({ rating, maxStars = 5, size = 'md', showValue = true, className }: StarRatingProps) {
  const sizes = {
    sm: 'w-3.5 h-3.5',
    md: 'w-4.5 h-4.5',
    lg: 'w-6 h-6',
  };

  const stars = [];
  for (let i = 1; i <= maxStars; i++) {
    if (i <= Math.floor(rating)) {
      stars.push(<FaStar key={i} className={cn(sizes[size], 'text-yellow-400')} />);
    } else if (i === Math.ceil(rating) && rating % 1 >= 0.5) {
      stars.push(<FaStarHalfAlt key={i} className={cn(sizes[size], 'text-yellow-400')} />);
    } else {
      stars.push(<FiStar key={i} className={cn(sizes[size], 'text-gray-300 dark:text-gray-600')} />);
    }
  }

  return (
    <div className={cn('flex items-center gap-0.5', className)}>
      {stars}
      {showValue && (
        <span className="ml-1 text-sm font-medium text-gray-600 dark:text-gray-400">
          {rating.toFixed(1)}
        </span>
      )}
    </div>
  );
}

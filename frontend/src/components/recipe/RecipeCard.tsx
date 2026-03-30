import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { FiClock, FiHeart, FiMessageCircle, FiUser } from 'react-icons/fi';
import type { RecipeCard as RecipeCardType } from '@/types';
import { cn, formatDuration, getDifficultyColor, truncate } from '@/lib/utils';
import StarRating from '@/components/ui/StarRating';

interface RecipeCardProps {
  recipe: RecipeCardType;
  className?: string;
}

export default function RecipeCard({ recipe, className }: RecipeCardProps) {
  const { t } = useTranslation();

  return (
    <Link to={`/recipes/${recipe.id}`} className={cn('card group', className)}>
      {/* Image */}
      <div className="relative h-48 bg-gray-200 dark:bg-gray-700 overflow-hidden">
        {recipe.imageUrl ? (
          <img
            src={recipe.imageUrl}
            alt={recipe.title}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            loading="lazy"
          />
        ) : (
          <div className="flex items-center justify-center h-full text-4xl">🍽️</div>
        )}
        {/* Difficulty badge */}
        <span className={cn('absolute top-3 left-3 badge text-xs', getDifficultyColor(recipe.difficulty))}>
          {t(`recipe.${recipe.difficulty.toLowerCase()}`)}
        </span>
        {/* Eco score */}
        {recipe.ecoScore && recipe.ecoScore > 0 && (
          <span className="absolute top-3 right-3 badge bg-green-100 text-green-700 text-xs">
            🌱 {recipe.ecoScore}
          </span>
        )}
      </div>

      {/* Content */}
      <div className="p-4">
        <h3 className="font-semibold text-base mb-1 group-hover:text-primary-500 transition-colors line-clamp-1">
          {recipe.title}
        </h3>
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-3 line-clamp-2">
          {truncate(recipe.description, 80)}
        </p>

        {/* Author */}
        <div className="flex items-center gap-2 mb-3">
          <div className="w-6 h-6 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
            <FiUser className="w-3 h-3 text-primary-600 dark:text-primary-400" />
          </div>
          <span className="text-xs text-gray-500 dark:text-gray-400">{recipe.authorName}</span>
        </div>

        {/* Meta */}
        <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
          <div className="flex items-center gap-3">
            <span className="flex items-center gap-1">
              <FiClock className="w-3.5 h-3.5" />
              {formatDuration(recipe.prepTimeMinutes + recipe.cookTimeMinutes)}
            </span>
            <StarRating rating={recipe.rating} size="sm" showValue={false} />
          </div>
          <div className="flex items-center gap-3">
            <span className="flex items-center gap-1">
              <FiHeart className={cn('w-3.5 h-3.5', recipe.isLiked && 'text-red-500 fill-red-500')} />
              {recipe.likesCount}
            </span>
            <span className="flex items-center gap-1">
              <FiMessageCircle className="w-3.5 h-3.5" />
              {recipe.commentsCount}
            </span>
          </div>
        </div>
      </div>
    </Link>
  );
}

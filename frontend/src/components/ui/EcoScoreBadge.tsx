import { cn } from '@/lib/utils';
import { getEcoGradeColor } from '@/lib/utils';

interface EcoScoreBadgeProps {
  score: number;
  grade: string;
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
  className?: string;
}

export default function EcoScoreBadge({ score, grade, size = 'md', showLabel = true, className }: EcoScoreBadgeProps) {
  const sizes = {
    sm: 'w-8 h-8 text-xs',
    md: 'w-11 h-11 text-sm',
    lg: 'w-16 h-16 text-lg',
  };

  return (
    <div className={cn('flex items-center gap-2', className)}>
      <div
        className={cn(
          'rounded-full flex items-center justify-center font-bold',
          getEcoGradeColor(grade),
          sizes[size],
        )}
      >
        {grade}
      </div>
      {showLabel && (
        <div className="text-xs">
          <div className="font-medium text-gray-700 dark:text-gray-300">Eco Score</div>
          <div className="text-gray-500 dark:text-gray-400">{score}/100</div>
        </div>
      )}
    </div>
  );
}

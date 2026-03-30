import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Merge Tailwind CSS classes with clsx
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Format a date string to a human-readable format
 */
export function formatDate(dateString: string, locale = 'en'): string {
  const date = new Date(dateString);
  return date.toLocaleDateString(locale === 'fr' ? 'fr-FR' : 'en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

/**
 * Format a date to a relative time string (e.g., "2 hours ago")
 */
export function formatRelativeTime(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffSec < 60) return 'just now';
  if (diffMin < 60) return `${diffMin}m ago`;
  if (diffHour < 24) return `${diffHour}h ago`;
  if (diffDay < 7) return `${diffDay}d ago`;
  return formatDate(dateString);
}

/**
 * Format a number as currency
 */
export function formatCurrency(amount: number, currency = 'EUR'): string {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency,
  }).format(amount);
}

/**
 * Format minutes into a human-readable duration
 */
export function formatDuration(minutes: number): string {
  if (minutes < 60) return `${minutes} min`;
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return mins > 0 ? `${hours}h ${mins}min` : `${hours}h`;
}

/**
 * Truncate text with ellipsis
 */
export function truncate(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength).trimEnd() + '...';
}

/**
 * Generate initials from a name
 */
export function getInitials(name: string): string {
  return name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}

/**
 * Debounce a function
 */
export function debounce<T extends (...args: unknown[]) => unknown>(
  fn: T,
  delay: number,
): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout>;
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn(...args), delay);
  };
}

/**
 * Get eco score grade color
 */
export function getEcoGradeColor(grade: string): string {
  const colors: Record<string, string> = {
    A: 'text-green-600 bg-green-100',
    B: 'text-lime-600 bg-lime-100',
    C: 'text-yellow-600 bg-yellow-100',
    D: 'text-orange-600 bg-orange-100',
    E: 'text-red-600 bg-red-100',
  };
  return colors[grade] || colors.C;
}

/**
 * Get difficulty color
 */
export function getDifficultyColor(difficulty: string): string {
  const colors: Record<string, string> = {
    EASY: 'text-green-600 bg-green-100',
    MEDIUM: 'text-yellow-600 bg-yellow-100',
    HARD: 'text-orange-600 bg-orange-100',
    EXPERT: 'text-red-600 bg-red-100',
  };
  return colors[difficulty] || colors.MEDIUM;
}

/**
 * Get order status color
 */
export function getOrderStatusColor(status: string): string {
  const colors: Record<string, string> = {
    PENDING: 'text-gray-600 bg-gray-100',
    CONFIRMED: 'text-blue-600 bg-blue-100',
    PREPARING: 'text-yellow-600 bg-yellow-100',
    READY: 'text-green-600 bg-green-100',
    OUT_FOR_DELIVERY: 'text-purple-600 bg-purple-100',
    DELIVERED: 'text-green-700 bg-green-200',
    CANCELLED: 'text-red-600 bg-red-100',
    REFUNDED: 'text-gray-600 bg-gray-200',
  };
  return colors[status] || colors.PENDING;
}

/**
 * Generate a random placeholder image URL
 */
export function getPlaceholderImage(category: string, width = 400, height = 300): string {
  return `https://source.unsplash.com/${width}x${height}/?${category},food`;
}

/**
 * Calculate star rating display
 */
export function getStarRating(rating: number): { full: number; half: boolean; empty: number } {
  const full = Math.floor(rating);
  const half = rating % 1 >= 0.5;
  const empty = 5 - full - (half ? 1 : 0);
  return { full, half, empty };
}

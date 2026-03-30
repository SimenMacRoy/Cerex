import { cn } from '@/lib/utils';
import { forwardRef, type ButtonHTMLAttributes } from 'react';
import LoadingSpinner from './LoadingSpinner';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ variant = 'primary', size = 'md', isLoading, leftIcon, rightIcon, className, children, disabled, ...props }, ref) => {
    const variants = {
      primary: 'bg-primary-500 hover:bg-primary-600 text-white shadow-md hover:shadow-lg focus:ring-primary-500',
      secondary: 'bg-secondary-500 hover:bg-secondary-600 text-white shadow-md hover:shadow-lg focus:ring-secondary-500',
      outline: 'border-2 border-primary-500 text-primary-600 hover:bg-primary-50 dark:hover:bg-primary-950 focus:ring-primary-500',
      ghost: 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 focus:ring-gray-400',
      danger: 'bg-red-500 hover:bg-red-600 text-white shadow-md hover:shadow-lg focus:ring-red-500',
    };

    const sizes = {
      sm: 'py-1.5 px-3 text-sm rounded-lg',
      md: 'py-2.5 px-5 text-sm rounded-xl',
      lg: 'py-3 px-7 text-base rounded-xl',
    };

    return (
      <button
        ref={ref}
        className={cn(
          'inline-flex items-center justify-center gap-2 font-semibold transition-all duration-200',
          'active:scale-[0.98] focus:outline-none focus:ring-2 focus:ring-offset-2',
          'disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100',
          variants[variant],
          sizes[size],
          className,
        )}
        disabled={disabled || isLoading}
        {...props}
      >
        {isLoading ? (
          <LoadingSpinner size="sm" />
        ) : leftIcon ? (
          <span className="shrink-0">{leftIcon}</span>
        ) : null}
        {children}
        {rightIcon && !isLoading && <span className="shrink-0">{rightIcon}</span>}
      </button>
    );
  },
);

Button.displayName = 'Button';
export default Button;

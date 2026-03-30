import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { FiMail, FiLock, FiEye, FiEyeOff } from 'react-icons/fi';
import { useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';

const loginSchema = z.object({
  email: z.string().email('Invalid email'),
  password: z.string().min(6, 'Min 6 characters'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { login, isLoading, error } = useAuthStore();
  const [showPassword, setShowPassword] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      await login(data.email, data.password);
      toast.success('Welcome back! 🎉');
      navigate('/');
    } catch {
      toast.error(error || 'Login failed');
    }
  };

  return (
    <div className="animate-fade-in">
      <h1 className="text-3xl font-display font-bold mb-2">{t('auth.loginTitle')}</h1>
      <p className="text-gray-500 dark:text-gray-400 mb-8">{t('auth.loginSubtitle')}</p>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <Input
          label={t('auth.email')}
          type="email"
          placeholder="you@example.com"
          leftIcon={<FiMail className="w-4 h-4" />}
          error={errors.email?.message}
          {...register('email')}
        />

        <Input
          label={t('auth.password')}
          type={showPassword ? 'text' : 'password'}
          placeholder="••••••••"
          leftIcon={<FiLock className="w-4 h-4" />}
          rightIcon={
            <button type="button" onClick={() => setShowPassword(!showPassword)} className="focus:outline-none">
              {showPassword ? <FiEyeOff className="w-4 h-4" /> : <FiEye className="w-4 h-4" />}
            </button>
          }
          error={errors.password?.message}
          {...register('password')}
        />

        <div className="flex items-center justify-between">
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" className="rounded border-gray-300 text-primary-500 focus:ring-primary-500" />
            <span className="text-gray-600 dark:text-gray-400">Remember me</span>
          </label>
          <Link to="/forgot-password" className="text-sm text-primary-500 hover:text-primary-600">
            {t('auth.forgotPassword')}
          </Link>
        </div>

        {error && (
          <div className="p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-sm text-red-600 dark:text-red-400">
            {error}
          </div>
        )}

        <Button type="submit" isLoading={isLoading} className="w-full" size="lg">
          {t('auth.login')}
        </Button>
      </form>

      <div className="mt-6">
        <div className="relative">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-gray-200 dark:border-gray-700" />
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="px-3 bg-white dark:bg-cerex-dark text-gray-500">{t('auth.orContinueWith')}</span>
          </div>
        </div>
        <div className="mt-4 grid grid-cols-2 gap-3">
          <button className="btn-outline text-sm py-2.5">🔵 {t('auth.google')}</button>
          <button className="btn-outline text-sm py-2.5">🔵 {t('auth.facebook')}</button>
        </div>
      </div>

      <p className="mt-8 text-center text-sm text-gray-500 dark:text-gray-400">
        {t('auth.noAccount')}{' '}
        <Link to="/register" className="text-primary-500 hover:text-primary-600 font-medium">
          {t('auth.register')}
        </Link>
      </p>
    </div>
  );
}

import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { FiMail, FiLock, FiUser, FiEye, FiEyeOff } from 'react-icons/fi';
import { useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';

const registerSchema = z.object({
  firstName: z.string().min(2, 'Min 2 characters'),
  lastName: z.string().min(2, 'Min 2 characters'),
  email: z.string().email('Invalid email'),
  password: z.string().min(8, 'Min 8 characters').regex(/[A-Z]/, 'Must contain uppercase').regex(/[0-9]/, 'Must contain a number'),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});

type RegisterFormData = z.infer<typeof registerSchema>;

export default function RegisterPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { register: registerUser, isLoading, error } = useAuthStore();
  const [showPassword, setShowPassword] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormData) => {
    try {
      await registerUser(data.firstName, data.lastName, data.email, data.password);
      toast.success('Account created! Welcome to Cerex! 🎉');
      navigate('/');
    } catch {
      toast.error(error || 'Registration failed');
    }
  };

  return (
    <div className="animate-fade-in">
      <h1 className="text-3xl font-display font-bold mb-2">{t('auth.registerTitle')}</h1>
      <p className="text-gray-500 dark:text-gray-400 mb-8">{t('auth.registerSubtitle')}</p>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input
            label={t('auth.firstName')}
            placeholder="John"
            leftIcon={<FiUser className="w-4 h-4" />}
            error={errors.firstName?.message}
            {...register('firstName')}
          />
          <Input
            label={t('auth.lastName')}
            placeholder="Doe"
            error={errors.lastName?.message}
            {...register('lastName')}
          />
        </div>

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
          helperText="Min 8 characters, 1 uppercase, 1 number"
          {...register('password')}
        />

        <Input
          label={t('auth.confirmPassword')}
          type="password"
          placeholder="••••••••"
          leftIcon={<FiLock className="w-4 h-4" />}
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />

        {error && (
          <div className="p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-sm text-red-600 dark:text-red-400">
            {error}
          </div>
        )}

        <Button type="submit" isLoading={isLoading} className="w-full" size="lg">
          {t('auth.register')}
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-500 dark:text-gray-400">
        {t('auth.hasAccount')}{' '}
        <Link to="/login" className="text-primary-500 hover:text-primary-600 font-medium">
          {t('auth.login')}
        </Link>
      </p>
    </div>
  );
}

import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import toast from 'react-hot-toast';
import { FiPlus, FiTrash2 } from 'react-icons/fi';
import { recipeApi } from '@/lib/api';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import type { CreateRecipeRequest } from '@/types';
import { useState } from 'react';

export default function CreateRecipePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register, handleSubmit, control, formState: { errors } } = useForm<CreateRecipeRequest>({
    defaultValues: {
      ingredients: [{ name: '', quantity: 1, unit: 'g', isOptional: false }],
      steps: [{ instruction: '', tip: '' }],
      dietaryFlags: { vegetarian: false, vegan: false, glutenFree: false, dairyFree: false, nutFree: false, halal: false, kosher: false },
      tags: [],
    },
  });

  const { fields: ingredientFields, append: addIngredient, remove: removeIngredient } = useFieldArray({ control, name: 'ingredients' });
  const { fields: stepFields, append: addStep, remove: removeStep } = useFieldArray({ control, name: 'steps' });

  const onSubmit = async (data: CreateRecipeRequest) => {
    setIsSubmitting(true);
    try {
      await recipeApi.create(data);
      toast.success('Recipe created! 🎉');
      navigate('/recipes');
    } catch {
      toast.error('Failed to create recipe');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="page-container max-w-3xl">
      <h1 className="section-title mb-8">{t('recipe.create')}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
        {/* Basic info */}
        <div className="card p-6 space-y-4">
          <Input label={t('recipe.title')} placeholder="My amazing recipe..." error={errors.title?.message} {...register('title', { required: 'Title is required' })} />
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">{t('recipe.description')}</label>
            <textarea className="input-field min-h-[100px] resize-y" placeholder="Describe your recipe..." {...register('description', { required: 'Description is required' })} />
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">{t('recipe.difficulty')}</label>
              <select className="input-field" {...register('difficulty')}>
                <option value="EASY">{t('recipe.easy')}</option>
                <option value="MEDIUM">{t('recipe.medium')}</option>
                <option value="HARD">{t('recipe.hard')}</option>
                <option value="EXPERT">{t('recipe.expert')}</option>
              </select>
            </div>
            <Input label={t('recipe.prepTime')} type="number" {...register('prepTimeMinutes', { valueAsNumber: true })} />
            <Input label={t('recipe.cookTime')} type="number" {...register('cookTimeMinutes', { valueAsNumber: true })} />
            <Input label={t('recipe.servings')} type="number" {...register('servings', { valueAsNumber: true })} />
          </div>
        </div>

        {/* Ingredients */}
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">{t('recipe.ingredients')}</h2>
            <Button type="button" variant="ghost" size="sm" leftIcon={<FiPlus />} onClick={() => addIngredient({ name: '', quantity: 1, unit: 'g', isOptional: false })}>
              Add
            </Button>
          </div>
          <div className="space-y-3">
            {ingredientFields.map((field, index) => (
              <div key={field.id} className="flex items-center gap-2">
                <Input placeholder="Ingredient name" {...register(`ingredients.${index}.name`)} className="flex-1" />
                <Input type="number" {...register(`ingredients.${index}.quantity`, { valueAsNumber: true })} className="w-20" />
                <Input placeholder="unit" {...register(`ingredients.${index}.unit`)} className="w-20" />
                <button type="button" onClick={() => removeIngredient(index)} className="p-2 text-red-400 hover:text-red-600">
                  <FiTrash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Steps */}
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">{t('recipe.steps')}</h2>
            <Button type="button" variant="ghost" size="sm" leftIcon={<FiPlus />} onClick={() => addStep({ instruction: '', tip: '' })}>
              Add Step
            </Button>
          </div>
          <div className="space-y-4">
            {stepFields.map((field, index) => (
              <div key={field.id} className="flex gap-3">
                <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center text-primary-600 font-bold text-sm shrink-0 mt-1">
                  {index + 1}
                </div>
                <div className="flex-1 space-y-2">
                  <textarea className="input-field min-h-[80px] resize-y text-sm" placeholder="Describe this step..." {...register(`steps.${index}.instruction`)} />
                  <Input placeholder="💡 Tip (optional)" {...register(`steps.${index}.tip`)} />
                </div>
                <button type="button" onClick={() => removeStep(index)} className="p-2 text-red-400 hover:text-red-600 mt-1">
                  <FiTrash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Dietary Flags */}
        <div className="card p-6">
          <h2 className="text-lg font-semibold mb-4">Dietary Options</h2>
          <div className="flex flex-wrap gap-3">
            {(['vegetarian', 'vegan', 'glutenFree', 'dairyFree', 'nutFree', 'halal', 'kosher'] as const).map((flag) => (
              <label key={flag} className="flex items-center gap-2 badge bg-gray-100 dark:bg-gray-800 cursor-pointer px-3 py-1.5">
                <input type="checkbox" {...register(`dietaryFlags.${flag}`)} className="rounded border-gray-300" />
                <span className="text-sm">{t(`recipe.dietary.${flag}`)}</span>
              </label>
            ))}
          </div>
        </div>

        <Button type="submit" isLoading={isSubmitting} size="lg" className="w-full">
          {t('recipe.create')}
        </Button>
      </form>
    </div>
  );
}

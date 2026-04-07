import { useNavigate } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import toast from 'react-hot-toast';
import { FiPlus, FiTrash2, FiArrowLeft } from 'react-icons/fi';
import { recipeApi } from '@/lib/api';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { useState } from 'react';

const CUISINES = [
  'AFRICAN', 'FRENCH', 'ITALIAN', 'JAPANESE', 'CHINESE',
  'INDIAN', 'MEXICAN', 'THAI', 'AMERICAN', 'MEDITERRANEAN',
  'MOROCCAN', 'SENEGALESE', 'GHANAIAN', 'NIGERIAN', 'CAMEROONIAN',
  'LEBANESE', 'TURKISH', 'GREEK', 'SPANISH', 'VIETNAMESE',
];

const RECIPE_TYPES = [
  { value: 'DISH',      label: 'Plat principal' },
  { value: 'SOUP',      label: 'Soupe / Ragoût' },
  { value: 'SALAD',     label: 'Salade' },
  { value: 'DESSERT',   label: 'Dessert' },
  { value: 'BEVERAGE',  label: 'Boisson' },
  { value: 'SNACK',     label: 'Collation' },
  { value: 'BREAD',     label: 'Pain / Pâtisserie' },
  { value: 'SAUCE',     label: 'Sauce / Condiment' },
  { value: 'SIDE_DISH', label: 'Accompagnement' },
];

interface IngredientField { name: string; quantity: string; unit: string; isOptional: boolean }
interface StepField       { instruction: string; durationMinutes: string; tip: string }

interface FormValues {
  title: string;
  description: string;
  recipeType: string;
  cuisineType: string;
  difficultyLevel: string;
  prepTimeMinutes: number;
  cookTimeMinutes: number;
  servings: number;
  coverImageUrl: string;
  isVegetarian: boolean;
  isVegan: boolean;
  isGlutenFree: boolean;
  isHalal: boolean;
  isDairyFree: boolean;
  ingredients: IngredientField[];
  steps: StepField[];
}

export default function CreateRecipePage() {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register, handleSubmit, control, formState: { errors } } = useForm<FormValues>({
    defaultValues: {
      recipeType: 'DISH',
      difficultyLevel: 'MEDIUM',
      cuisineType: '',
      prepTimeMinutes: 15,
      cookTimeMinutes: 30,
      servings: 4,
      ingredients: [{ name: '', quantity: '', unit: '', isOptional: false }],
      steps: [{ instruction: '', durationMinutes: '', tip: '' }],
      isVegetarian: false,
      isVegan: false,
      isGlutenFree: false,
      isHalal: false,
      isDairyFree: false,
    },
  });

  const { fields: ingFields, append: addIng, remove: removeIng } =
    useFieldArray({ control, name: 'ingredients' });

  const { fields: stepFields, append: addStep, remove: removeStep } =
    useFieldArray({ control, name: 'steps' });

  const onSubmit = async (data: FormValues) => {
    const validIngredients = data.ingredients.filter((i) => i.name.trim());
    if (validIngredients.length === 0) {
      toast.error('Ajoutez au moins un ingrédient.');
      return;
    }
    const validSteps = data.steps.filter((s) => s.instruction.trim());
    if (validSteps.length === 0) {
      toast.error('Ajoutez au moins une étape.');
      return;
    }

    setIsSubmitting(true);
    try {
      const payload = {
        title: data.title,
        description: data.description,
        recipeType: data.recipeType,
        cuisineType: data.cuisineType || undefined,
        difficultyLevel: data.difficultyLevel,
        prepTimeMinutes: Number(data.prepTimeMinutes),
        cookTimeMinutes: Number(data.cookTimeMinutes),
        servings: Number(data.servings),
        coverImageUrl: data.coverImageUrl || undefined,
        isVegetarian: data.isVegetarian,
        isVegan: data.isVegan,
        isGlutenFree: data.isGlutenFree,
        isHalal: data.isHalal,
        isDairyFree: data.isDairyFree,
        ingredients: validIngredients.map((i) => ({
          name: i.name.trim(),
          quantity: i.quantity ? Number(i.quantity) : undefined,
          unit: i.unit.trim() || undefined,
          isOptional: i.isOptional,
        })),
        steps: validSteps.map((s) => ({
          instruction: s.instruction.trim(),
          durationMinutes: s.durationMinutes ? Number(s.durationMinutes) : undefined,
          tip: s.tip.trim() || undefined,
        })),
      };

      await recipeApi.create(payload as any);
      toast.success('Recette créée ! Elle sera visible après validation.');
      navigate('/recipes');
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? 'Erreur lors de la création';
      toast.error(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="page-container max-w-3xl">
      {/* Header */}
      <div className="flex items-center gap-3 mb-8">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="p-2 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
        >
          <FiArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="section-title">Ajouter une recette</h1>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">

        {/* ── Informations générales ── */}
        <div className="card p-6 space-y-4">
          <h2 className="font-semibold text-lg border-b border-gray-100 dark:border-gray-700 pb-2">
            Informations générales
          </h2>

          <Input
            label="Titre *"
            placeholder="Ex : Jollof Rice au poulet..."
            error={errors.title?.message}
            {...register('title', { required: 'Le titre est requis' })}
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              Description *
            </label>
            <textarea
              className="input-field min-h-[100px] resize-y"
              placeholder="Décrivez brièvement votre recette..."
              {...register('description', {
                required: 'La description est requise',
                minLength: { value: 10, message: 'Au moins 10 caractères' },
              })}
            />
            {errors.description && (
              <p className="text-red-500 text-xs mt-1">{errors.description.message}</p>
            )}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                Type de plat
              </label>
              <select className="input-field" {...register('recipeType')}>
                {RECIPE_TYPES.map((rt) => (
                  <option key={rt.value} value={rt.value}>{rt.label}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                Cuisine / Origine
              </label>
              <select className="input-field" {...register('cuisineType')}>
                <option value="">-- Sélectionner --</option>
                {CUISINES.map((c) => (
                  <option key={c} value={c}>{c.charAt(0) + c.slice(1).toLowerCase()}</option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              Difficulté *
            </label>
            <select className="input-field" {...register('difficultyLevel', { required: true })}>
              <option value="EASY">Facile</option>
              <option value="MEDIUM">Moyen</option>
              <option value="HARD">Difficile</option>
              <option value="EXPERT">Expert</option>
            </select>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <Input
              label="Préparation (min) *"
              type="number"
              min={0}
              error={errors.prepTimeMinutes?.message}
              {...register('prepTimeMinutes', { required: true, min: 0, valueAsNumber: true })}
            />
            <Input
              label="Cuisson (min) *"
              type="number"
              min={0}
              error={errors.cookTimeMinutes?.message}
              {...register('cookTimeMinutes', { required: true, min: 0, valueAsNumber: true })}
            />
            <Input
              label="Portions *"
              type="number"
              min={1}
              error={errors.servings?.message}
              {...register('servings', { required: true, min: 1, valueAsNumber: true })}
            />
          </div>

          <Input
            label="Photo (URL)"
            placeholder="https://..."
            {...register('coverImageUrl')}
          />
        </div>

        {/* ── Ingrédients ── */}
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-lg">Ingrédients *</h2>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              leftIcon={<FiPlus className="w-4 h-4" />}
              onClick={() => addIng({ name: '', quantity: '', unit: '', isOptional: false })}
            >
              Ajouter
            </Button>
          </div>

          <div className="space-y-2">
            <div className="grid grid-cols-[1fr_80px_80px_48px_36px] gap-2 text-xs text-gray-400 px-1">
              <span>Ingrédient</span><span>Quantité</span><span>Unité</span><span>Opt.</span><span />
            </div>
            {ingFields.map((field, idx) => (
              <div key={field.id} className="grid grid-cols-[1fr_80px_80px_48px_36px] gap-2 items-center">
                <input
                  className="input-field text-sm"
                  placeholder="Ex : tomates"
                  {...register(`ingredients.${idx}.name`)}
                />
                <input
                  className="input-field text-sm"
                  type="number"
                  min={0}
                  step="any"
                  placeholder="1"
                  {...register(`ingredients.${idx}.quantity`)}
                />
                <input
                  className="input-field text-sm"
                  placeholder="g / ml / pcs"
                  {...register(`ingredients.${idx}.unit`)}
                />
                <div className="flex justify-center">
                  <input type="checkbox" {...register(`ingredients.${idx}.isOptional`)} />
                </div>
                <button
                  type="button"
                  onClick={() => removeIng(idx)}
                  disabled={ingFields.length === 1}
                  className="p-1 text-red-400 hover:text-red-600 disabled:opacity-30"
                >
                  <FiTrash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* ── Étapes ── */}
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-lg">Étapes de préparation *</h2>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              leftIcon={<FiPlus className="w-4 h-4" />}
              onClick={() => addStep({ instruction: '', durationMinutes: '', tip: '' })}
            >
              Ajouter une étape
            </Button>
          </div>

          <div className="space-y-4">
            {stepFields.map((field, idx) => (
              <div key={field.id} className="flex gap-3">
                <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center text-primary-600 dark:text-primary-400 font-bold text-sm shrink-0 mt-1">
                  {idx + 1}
                </div>
                <div className="flex-1 space-y-2">
                  <textarea
                    className="input-field min-h-[80px] resize-y text-sm w-full"
                    placeholder="Décrivez cette étape en détail..."
                    {...register(`steps.${idx}.instruction`, { required: true })}
                  />
                  <div className="flex gap-2">
                    <input
                      className="input-field w-36 text-sm"
                      type="number"
                      min={0}
                      placeholder="Durée (min)"
                      {...register(`steps.${idx}.durationMinutes`)}
                    />
                    <input
                      className="input-field flex-1 text-sm"
                      placeholder="💡 Astuce (optionnel)"
                      {...register(`steps.${idx}.tip`)}
                    />
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => removeStep(idx)}
                  disabled={stepFields.length === 1}
                  className="p-1 text-red-400 hover:text-red-600 disabled:opacity-30 mt-1"
                >
                  <FiTrash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* ── Options alimentaires ── */}
        <div className="card p-6">
          <h2 className="font-semibold text-lg mb-4">Régimes alimentaires</h2>
          <div className="flex flex-wrap gap-3">
            {[
              { key: 'isVegetarian', label: '🥦 Végétarien' },
              { key: 'isVegan',      label: '🌱 Vegan' },
              { key: 'isGlutenFree', label: '🌾 Sans gluten' },
              { key: 'isHalal',      label: '☪️ Halal' },
              { key: 'isDairyFree',  label: '🥛 Sans lactose' },
            ].map(({ key, label }) => (
              <label
                key={key}
                className="flex items-center gap-2 px-3 py-1.5 rounded-full border border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 text-sm transition-colors"
              >
                <input type="checkbox" {...register(key as keyof FormValues)} className="rounded" />
                {label}
              </label>
            ))}
          </div>
        </div>

        {/* Submit */}
        <div className="flex gap-3">
          <Button type="button" variant="outline" className="flex-1" onClick={() => navigate(-1)}>
            Annuler
          </Button>
          <Button type="submit" isLoading={isSubmitting} className="flex-1">
            Publier ma recette
          </Button>
        </div>
      </form>
    </div>
  );
}

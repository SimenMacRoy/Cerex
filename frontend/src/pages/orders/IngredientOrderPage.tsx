import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { useState, useEffect, useRef } from 'react';
import {
  FiMapPin, FiTruck, FiShoppingBag, FiPrinter, FiAlertTriangle,
  FiChevronDown, FiChevronUp, FiStar, FiPackage, FiX,
  FiNavigation, FiShoppingCart, FiArrowLeft, FiFeather, FiGlobe,
} from 'react-icons/fi';
import { ingredientOrderApi } from '@/lib/api';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import { cn } from '@/lib/utils';
import toast from 'react-hot-toast';
import type { StorePlan, SourcingItem, UnavailableItem } from '@/types';

/** Format price with currency symbol */
function formatPrice(amount: number, currency: string): string {
  const fmt: Record<string, { locale: string; symbol: string; after?: boolean }> = {
    XAF: { locale: 'fr-FR', symbol: ' FCFA', after: true },
    XOF: { locale: 'fr-FR', symbol: ' FCFA', after: true },
    EUR: { locale: 'fr-FR', symbol: ' €', after: true },
    GHS: { locale: 'en-GH', symbol: 'GH₵ ' },
    NGN: { locale: 'en-NG', symbol: '₦ ' },
    USD: { locale: 'en-US', symbol: '$ ' },
    GBP: { locale: 'en-GB', symbol: '£ ' },
  };
  const c = fmt[currency] || fmt.XAF;
  const num = Number(amount).toLocaleString(c.locale, { minimumFractionDigits: 0, maximumFractionDigits: 2 });
  return c.after ? `${num}${c.symbol}` : `${c.symbol}${num}`;
}

export default function IngredientOrderPage() {
  const { id } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const printRef = useRef<HTMLDivElement>(null);

  const servings = parseInt(searchParams.get('servings') || '4', 10);

  // ── Geolocation ──────────────────────────────────────────
  const [userLat, setUserLat] = useState<number | null>(null);
  const [userLng, setUserLng] = useState<number | null>(null);
  const [geoError, setGeoError] = useState<string | null>(null);
  const [geoLoading, setGeoLoading] = useState(true);

  useEffect(() => {
    if (!navigator.geolocation) {
      setGeoError(t('ingredientOrder.geoNotSupported'));
      setGeoLoading(false);
      // Default: Douala
      setUserLat(4.0510);
      setUserLng(9.7679);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setUserLat(pos.coords.latitude);
        setUserLng(pos.coords.longitude);
        setGeoLoading(false);
      },
      () => {
        setGeoError(t('ingredientOrder.geoBlocked'));
        setGeoLoading(false);
        // Fallback: Douala
        setUserLat(4.0510);
        setUserLng(9.7679);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  }, [t]);

  // ── Fetch sourcing quote ─────────────────────────────────
  const {
    data: quote,
    isLoading: quoteLoading,
    error: quoteError,
  } = useQuery({
    queryKey: ['ingredient-quote', id, servings, userLat, userLng],
    queryFn: () =>
      ingredientOrderApi
        .getQuote({
          recipeId: id!,
          servings,
          latitude: userLat!,
          longitude: userLng!,
          radiusKm: 50,
        })
        .then((r) => r.data.data),
    enabled: !!id && userLat !== null && userLng !== null,
  });

  // ── UI state ─────────────────────────────────────────────
  const [expandedStore, setExpandedStore] = useState<string | null>(null);
  const [selectedMode, setSelectedMode] = useState<'delivery' | 'pickup' | 'print'>('delivery');

  // Auto-expand first store
  useEffect(() => {
    if (quote?.storePlans?.length && !expandedStore) {
      setExpandedStore(quote.storePlans[0].groceryId);
    }
  }, [quote, expandedStore]);

  // ── Print handler ────────────────────────────────────────
  const handlePrint = () => {
    window.print();
  };

  // ── Loading / Error states ───────────────────────────────
  if (geoLoading) {
    return (
      <div className="page-container max-w-4xl flex flex-col items-center justify-center py-24 gap-4">
        <FiNavigation className="w-10 h-10 text-primary-500 animate-pulse" />
        <p className="text-lg font-medium">{t('ingredientOrder.locating')}</p>
        <p className="text-sm text-gray-500">{t('ingredientOrder.locatingDesc')}</p>
      </div>
    );
  }

  if (quoteLoading) {
    return (
      <div className="page-container max-w-4xl flex flex-col items-center justify-center py-24 gap-4">
        <LoadingSpinner size="lg" />
        <p className="text-lg font-medium">{t('ingredientOrder.searching')}</p>
        <p className="text-sm text-gray-500">{t('ingredientOrder.searchingDesc')}</p>
      </div>
    );
  }

  if (quoteError || !quote) {
    return (
      <div className="page-container max-w-4xl text-center py-16">
        <FiAlertTriangle className="w-12 h-12 text-red-400 mx-auto mb-4" />
        <h2 className="text-xl font-bold mb-2">{t('ingredientOrder.errorTitle')}</h2>
        <p className="text-gray-500 mb-6">{t('ingredientOrder.errorDesc')}</p>
        <Button onClick={() => navigate(-1)} leftIcon={<FiArrowLeft className="w-4 h-4" />}>
          {t('common.back')}
        </Button>
      </div>
    );
  }

  const totalStores = quote.storePlans.length;
  const totalCovered = quote.storePlans.reduce((s, p) => s + p.coverageCount, 0);
  const totalNeeded = quote.storePlans[0]?.totalNeeded ?? 0;
  const coveragePct = totalNeeded > 0 ? Math.round((totalCovered / totalNeeded) * 100) : 0;

  return (
    <div className="page-container max-w-4xl print:max-w-full" ref={printRef}>
      {/* ── Back + Header ───────────────────────────────────── */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-primary-500 transition-colors mb-4 print:hidden"
      >
        <FiArrowLeft className="w-4 h-4" />
        {t('ingredientOrder.backToRecipe')}
      </button>

      <div className="mb-8">
        <h1 className="text-2xl md:text-3xl font-display font-bold mb-2">
          {t('ingredientOrder.title')}
        </h1>
        <p className="text-gray-500">
          <span className="font-semibold text-gray-700 dark:text-gray-300">{quote.recipeTitle}</span>
          {' · '}
          {quote.requestedServings} {t('recipe.servings')}
        </p>
      </div>

      {/* ── Geo info ────────────────────────────────────────── */}
      {geoError && (
        <div className="flex items-center gap-2 p-3 mb-6 rounded-xl bg-amber-50 dark:bg-amber-900/20 text-amber-700 dark:text-amber-300 text-sm print:hidden">
          <FiAlertTriangle className="w-4 h-4 shrink-0" />
          <p>{geoError} {t('ingredientOrder.geoFallback')}</p>
        </div>
      )}

      {/* ── Summary Cards ───────────────────────────────────── */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
        <div className="card p-4 text-center">
          <FiMapPin className="w-5 h-5 mx-auto text-primary-500 mb-1" />
          <div className="text-2xl font-bold">{totalStores}</div>
          <div className="text-xs text-gray-500">{t('ingredientOrder.storesFound')}</div>
        </div>
        <div className="card p-4 text-center">
          <FiShoppingBag className="w-5 h-5 mx-auto text-green-500 mb-1" />
          <div className="text-2xl font-bold">{coveragePct}%</div>
          <div className="text-xs text-gray-500">{t('ingredientOrder.coverage')}</div>
        </div>
        <div className="card p-4 text-center">
          <FiPackage className="w-5 h-5 mx-auto text-blue-500 mb-1" />
          <div className="text-2xl font-bold">{totalCovered}/{totalNeeded}</div>
          <div className="text-xs text-gray-500">{t('ingredientOrder.ingredientsAvailable')}</div>
        </div>
        <div className="card p-4 text-center">
          <FiGlobe className="w-5 h-5 mx-auto text-orange-500 mb-1" />
          <div className="text-2xl font-bold">{formatPrice(quote.grandTotal, quote.currency)}</div>
          <div className="text-xs text-gray-500">{t('ingredientOrder.estimatedTotal')}</div>
        </div>
      </div>

      {/* ── Bulk Warning ────────────────────────────────────── */}
      {quote.hasBulkItems && (
        <div className="flex items-start gap-3 p-4 mb-6 rounded-xl bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 print:border-amber-400">
          <FiAlertTriangle className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />
          <div>
            <h4 className="font-semibold text-amber-800 dark:text-amber-200 text-sm">
              {t('ingredientOrder.bulkWarningTitle')}
            </h4>
            <p className="text-sm text-amber-700 dark:text-amber-300 mt-0.5">
              {t('ingredientOrder.bulkWarningDesc')}
            </p>
          </div>
        </div>
      )}

      {/* ── Delivery Mode Selector ──────────────────────────── */}
      <div className="flex gap-2 mb-6 print:hidden">
        {[
          { key: 'delivery' as const, icon: FiTruck, label: t('ingredientOrder.delivery') },
          { key: 'pickup' as const, icon: FiShoppingBag, label: t('ingredientOrder.pickup') },
          { key: 'print' as const, icon: FiPrinter, label: t('ingredientOrder.printList') },
        ].map(({ key, icon: Icon, label }) => (
          <button
            key={key}
            onClick={() => setSelectedMode(key)}
            className={cn(
              'flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-medium border transition-all',
              selectedMode === key
                ? 'bg-primary-50 dark:bg-primary-900/30 border-primary-300 dark:border-primary-700 text-primary-700 dark:text-primary-300'
                : 'bg-white dark:bg-cerex-medium border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 hover:border-gray-300'
            )}
          >
            <Icon className="w-4 h-4" />
            {label}
          </button>
        ))}
      </div>

      {/* ── Store Plans ─────────────────────────────────────── */}
      <div className="space-y-4 mb-8">
        {quote.storePlans.map((store, idx) => (
          <StoreCard
            key={store.groceryId}
            store={store}
            index={idx}
            isExpanded={expandedStore === store.groceryId}
            onToggle={() => setExpandedStore(expandedStore === store.groceryId ? null : store.groceryId)}
            selectedMode={selectedMode}
          />
        ))}
      </div>

      {/* ── Unavailable Items ───────────────────────────────── */}
      {quote.unavailableItems.length > 0 && (
        <div className="mb-8">
          <h3 className="flex items-center gap-2 font-display font-bold text-lg mb-3">
            <FiX className="w-5 h-5 text-red-400" />
            {t('ingredientOrder.unavailableTitle')} ({quote.unavailableItems.length})
          </h3>
          <div className="card divide-y divide-gray-100 dark:divide-gray-700">
            {quote.unavailableItems.map((item) => (
              <UnavailableRow key={item.ingredientId} item={item} />
            ))}
          </div>
        </div>
      )}

      {/* ── Grand Total + CTA ───────────────────────────────── */}
      <div className="sticky bottom-0 bg-white/95 dark:bg-cerex-dark/95 backdrop-blur border-t border-gray-200 dark:border-gray-700 -mx-4 px-4 py-4 print:static print:border-t-2 print:border-gray-400">
        <div className="max-w-4xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
          <div>
            <p className="text-sm text-gray-500">{t('ingredientOrder.grandTotal')}</p>
            <p className="text-2xl font-bold text-primary-600 dark:text-primary-400">
              {formatPrice(quote.grandTotal, quote.currency)}
            </p>
            {totalStores > 1 && (
              <p className="text-xs text-gray-400">
                {t('ingredientOrder.acrossStores', { count: totalStores })}
              </p>
            )}
          </div>
          <div className="flex gap-3 print:hidden">
            {selectedMode === 'print' ? (
              <Button
                leftIcon={<FiPrinter className="w-4 h-4" />}
                onClick={handlePrint}
              >
                {t('ingredientOrder.printNow')}
              </Button>
            ) : (
              <Button
                leftIcon={<FiShoppingCart className="w-4 h-4" />}
                onClick={() => {
                  toast.success(t('ingredientOrder.orderPlaced'));
                }}
              >
                {selectedMode === 'delivery'
                  ? t('ingredientOrder.orderDelivery')
                  : t('ingredientOrder.confirmPickup')}
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Store Card Component
// ─────────────────────────────────────────────────────────────
function StoreCard({
  store,
  index,
  isExpanded,
  onToggle,
  selectedMode,
}: {
  store: StorePlan;
  index: number;
  isExpanded: boolean;
  onToggle: () => void;
  selectedMode: string;
}) {
  const { t } = useTranslation();
  const isPrimary = index === 0;

  return (
    <div
      className={cn(
        'card overflow-hidden transition-shadow',
        isPrimary && 'ring-2 ring-primary-300 dark:ring-primary-700'
      )}
    >
      {/* Header */}
      <button
        onClick={onToggle}
        className="w-full flex items-center justify-between p-4 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors text-left"
      >
        <div className="flex items-center gap-3">
          <div
            className={cn(
              'w-10 h-10 rounded-xl flex items-center justify-center text-white font-bold text-sm shrink-0',
              isPrimary ? 'bg-primary-500' : 'bg-gray-400 dark:bg-gray-600'
            )}
          >
            {index + 1}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-bold">{store.groceryName}</h3>
              {isPrimary && (
                <span className="px-2 py-0.5 text-xs font-semibold bg-primary-100 dark:bg-primary-900/40 text-primary-700 dark:text-primary-300 rounded-full">
                  {t('ingredientOrder.bestMatch')}
                </span>
              )}
            </div>
            <div className="flex items-center gap-3 mt-0.5 text-xs text-gray-500">
              <span className="flex items-center gap-1">
                <FiMapPin className="w-3 h-3" /> {store.distanceKm} km · {store.city}
              </span>
              {store.averageRating > 0 && (
                <span className="flex items-center gap-1">
                  <FiStar className="w-3 h-3 text-yellow-400" /> {store.averageRating}
                </span>
              )}
              <span>{store.coverageCount}/{store.totalNeeded} {t('ingredientOrder.items')}</span>
            </div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <div className="text-right">
            <div className="font-bold text-primary-600 dark:text-primary-400">
              {formatPrice(store.storeTotal, store.currency)}
            </div>
            <div className="flex items-center gap-1 text-xs text-gray-400 mt-0.5">
              {store.supportsDelivery && selectedMode === 'delivery' && (
                <span className="flex items-center gap-0.5 text-green-500">
                  <FiTruck className="w-3 h-3" /> {t('ingredientOrder.delivers')}
                </span>
              )}
              {store.supportsPickup && selectedMode === 'pickup' && (
                <span className="flex items-center gap-0.5 text-blue-500">
                  <FiShoppingBag className="w-3 h-3" /> {t('ingredientOrder.pickupAvailable')}
                </span>
              )}
            </div>
          </div>
          {isExpanded ? <FiChevronUp className="w-5 h-5 text-gray-400" /> : <FiChevronDown className="w-5 h-5 text-gray-400" />}
        </div>
      </button>

      {/* Expanded items */}
      {isExpanded && (
        <div className="border-t border-gray-100 dark:border-gray-700">
          {/* Minimum order warning */}
          {store.minimumOrderAmount > 0 && store.storeTotal < store.minimumOrderAmount && (
            <div className="flex items-center gap-2 px-4 py-2 bg-amber-50 dark:bg-amber-900/20 text-amber-700 dark:text-amber-300 text-xs">
              <FiAlertTriangle className="w-3.5 h-3.5 shrink-0" />
              {t('ingredientOrder.minOrderWarning', {
                min: formatPrice(store.minimumOrderAmount, store.currency),
              })}
            </div>
          )}

          <div className="divide-y divide-gray-50 dark:divide-gray-800">
            {store.items.map((item) => (
              <ItemRow key={item.productId} item={item} />
            ))}
          </div>

          {/* Store subtotal */}
          <div className="flex items-center justify-between px-4 py-3 bg-gray-50 dark:bg-gray-800/50 font-semibold text-sm">
            <span>{t('ingredientOrder.storeSubtotal')}</span>
            <span className="text-primary-600 dark:text-primary-400">
              {formatPrice(store.storeTotal, store.currency)}
            </span>
          </div>
        </div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Item Row Component
// ─────────────────────────────────────────────────────────────
function ItemRow({ item }: { item: SourcingItem }) {
  const { t } = useTranslation();

  return (
    <div
      className={cn(
        'flex items-center justify-between px-4 py-3',
        item.isOptional && 'opacity-60'
      )}
    >
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium truncate">{item.productName}</span>
          {item.bulkOnly && (
            <span className="px-1.5 py-0.5 text-[10px] font-bold bg-orange-100 dark:bg-orange-900/40 text-orange-700 dark:text-orange-300 rounded uppercase">
              {t('ingredientOrder.bulk')}
            </span>
          )}
          {item.organic && (
            <FiFeather className="w-3.5 h-3.5 text-green-500 shrink-0" title="Organic" />
          )}
          {item.isOptional && (
            <span className="text-[10px] text-gray-400 uppercase">{t('ingredientOrder.optional')}</span>
          )}
        </div>
        <p className="text-xs text-gray-500 mt-0.5">
          {item.displayText || `${item.recipeQuantity} ${item.recipeUnit}`}
        </p>
      </div>
      <div className="flex items-center gap-4 ml-3 shrink-0">
        <span className="text-xs text-gray-400 text-right min-w-[60px]">
          {formatPrice(item.price, item.currency)}/{item.productUnit}
        </span>
        <span className="text-sm font-bold text-right min-w-[80px]">
          {formatPrice(item.lineTotal, item.currency)}
        </span>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Unavailable Row Component
// ─────────────────────────────────────────────────────────────
function UnavailableRow({ item }: { item: UnavailableItem }) {
  const { t } = useTranslation();

  return (
    <div className="flex items-center justify-between px-4 py-3">
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <FiX className="w-4 h-4 text-red-400 shrink-0" />
          <span className="text-sm font-medium">{item.ingredientName}</span>
          {item.isOptional && (
            <span className="text-[10px] text-gray-400 uppercase">{t('ingredientOrder.optional')}</span>
          )}
        </div>
        <p className="text-xs text-gray-500 mt-0.5 ml-6">
          {item.displayText || `${item.recipeQuantity} ${item.recipeUnit}`}
        </p>
      </div>
      <div className="text-right shrink-0">
        <span className="text-xs text-gray-400 line-through">
          ~{formatPrice(item.estimatedPriceFcfa, 'XAF')}
        </span>
        <p className="text-[10px] text-red-400">{t('ingredientOrder.notInStock')}</p>
      </div>
    </div>
  );
}

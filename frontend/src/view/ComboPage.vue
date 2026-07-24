<template>
  <div class="combo-page">
    <!-- ── Header ── -->
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Combo &amp; Đồ ăn</span>
      <ThemeToggle />
    </header>

    <!-- ── Order context strip ── -->
    <div class="context-strip">
      <div class="ctx-item">
        <span class="ctx-label">Ghế đã chọn</span>
        <span class="ctx-value">
          {{ bookingStore.selectedSeats.map(s => `${(s.hangGhe||'').trim()}${s.soGhe}`).join(', ') || '—' }}
        </span>
      </div>
      <div class="ctx-sep"></div>
      <div class="ctx-item">
        <span class="ctx-label">Tiền vé</span>
        <span class="ctx-value ctx-value--gold">{{ fmtPrice(bookingStore.totalSeatPrice) }}</span>
      </div>
    </div>

    <!-- ── Loading ── -->
    <div v-if="loading" class="state-box">
      <div class="spinner"></div>
      <p>Đang tải sản phẩm...</p>
    </div>

    <!-- ── Error ── -->
    <div v-else-if="loadError" class="state-box state-box--error">
      <svg class="state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <p>{{ loadError }}</p>
      <button class="btn-retry" @click="loadProducts">Thử lại</button>
    </div>

    <!-- ── Product groups ── -->
    <div v-else class="content">
      <template v-if="combos.length > 0">
        <div class="group-title">🍿 Combo</div>
        <div class="product-grid">
          <div v-for="p in combos" :key="p.id" class="product-card">
            <ProductCard :product="p" />
          </div>
        </div>
      </template>

      <template v-if="foods.length > 0">
        <div class="group-title">🍟 Đồ ăn</div>
        <div class="product-grid">
          <div v-for="p in foods" :key="p.id" class="product-card">
            <ProductCard :product="p" />
          </div>
        </div>
      </template>

      <template v-if="drinks.length > 0">
        <div class="group-title">🥤 Nước uống</div>
        <div class="product-grid">
          <div v-for="p in drinks" :key="p.id" class="product-card">
            <ProductCard :product="p" />
          </div>
        </div>
      </template>

      <div v-if="!loading && products.length === 0" class="empty">
        <p>Không có sản phẩm nào.</p>
      </div>
    </div>

    <!-- ── Bottom bar ── -->
    <div class="bottom-bar">
      <div class="bottom-bar__totals">
        <div class="total-row">
          <span>Tiền vé:</span>
          <span>{{ fmtPrice(bookingStore.totalSeatPrice) }}</span>
        </div>
        <div v-if="bookingStore.totalComboPrice > 0" class="total-row">
          <span>Combo ({{ totalItems }} sp):</span>
          <span>{{ fmtPrice(bookingStore.totalComboPrice) }}</span>
        </div>
        <div class="total-row total-row--main">
          <span>Tổng cộng:</span>
          <span class="total-gold">{{ fmtPrice(bookingStore.subtotal) }}</span>
        </div>
      </div>
      <div class="bottom-bar__actions">
        <button class="btn-skip" @click="skip">Bỏ qua</button>
        <button class="btn-next" @click="goCheckout">Tiếp tục</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, defineComponent, h } from 'vue'
import { useRouter } from 'vue-router'
import { useBookingStore } from '@/stores/bookingStore'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router       = useRouter()
const bookingStore = useBookingStore()
const authStore    = useAuthStore()

const products  = ref([])
const loading   = ref(false)
const loadError = ref('')

// ── Grouping ────────────────────────────────────────────────
const combos = computed(() => products.value.filter(p => p.loaiSanPham === 'combo'))
const foods  = computed(() => products.value.filter(p => p.loaiSanPham === 'food'))
const drinks = computed(() => products.value.filter(p => p.loaiSanPham === 'drink'))

const totalItems = computed(() =>
  bookingStore.selectedCombos.reduce((s, c) => s + c.soLuong, 0)
)

// ── Load ────────────────────────────────────────────────────
async function loadProducts() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await api.get('/san-pham')
    products.value = (Array.isArray(res.data) ? res.data : []).filter(p => p.dangHoatDong !== false)
  } catch (e) {
    loadError.value = e.response?.data?.message || 'Không tải được sản phẩm'
  } finally {
    loading.value = false
  }
}

// ── Navigation ──────────────────────────────────────────────
function skip() { router.push('/checkout') }
function goCheckout() { router.push('/checkout') }

function fmtPrice(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(v)
}

onMounted(() => {
  if (!authStore.isLoggedIn) { router.push('/auth'); return }
  loadProducts()
})

// ── Inline child component — avoids separate file ──────────
const ProductCard = defineComponent({
  name: 'ProductCard',
  props: { product: Object },
  setup(props) {
    const qty = computed(() => {
      const c = bookingStore.selectedCombos.find(c => c.id === props.product.id)
      return c ? c.soLuong : 0
    })

    function inc() {
      if (qty.value === 0) {
        bookingStore.addCombo(props.product, 1)
      } else {
        bookingStore.updateComboQuantity(props.product.id, qty.value + 1)
      }
    }
    function dec() {
      if (qty.value > 0) bookingStore.updateComboQuantity(props.product.id, qty.value - 1)
    }

    const fmtP = (v) => new Intl.NumberFormat('vi-VN',{ style:'currency', currency:'VND' }).format(v || 0)

    return () => h('div', { class: 'pcard' }, [
      h('div', { class: 'pcard__img' }, [
        props.product.anhUrl
          ? h('img', { src: props.product.anhUrl, alt: props.product.tenSanPham, class: 'pcard__photo' })
          : h('div', { class: 'pcard__img-placeholder' }, ['🍿'])
      ]),
      h('div', { class: 'pcard__body' }, [
        h('p', { class: 'pcard__name' }, props.product.tenSanPham),
        props.product.moTa ? h('p', { class: 'pcard__desc' }, props.product.moTa) : null,
        h('p', { class: 'pcard__price' }, fmtP(props.product.gia)),
      ]),
      h('div', { class: 'pcard__ctrl' }, [
        h('button', { class: 'ctrl-btn', onClick: dec, disabled: qty.value === 0, 'aria-label': 'Giảm' }, '−'),
        h('span', { class: qty.value > 0 ? 'ctrl-qty ctrl-qty--active' : 'ctrl-qty' }, String(qty.value)),
        h('button', { class: 'ctrl-btn ctrl-btn--plus', onClick: inc, 'aria-label': 'Tăng' }, '+'),
      ]),
    ])
  }
})
</script>

<style scoped>
/* ── base ──────────────────────────────────────────────────── */
.combo-page {
  background: var(--void, #050508);
  color: var(--text-secondary, #94a3b8);
  min-height: 100vh;
  font-family: var(--font-ui, 'Inter', sans-serif);
  display: flex;
  flex-direction: column;
  padding-bottom: 200px;
}

/* ── top bar ──────────────────────────────────────────────── */
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px;
  background: rgba(5,5,8,0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  position: sticky; top: 0; z-index: 60;
}
.top-bar__title {
  font-size: 16px; font-weight: 700;
  color: var(--text-primary, #f1f5f9);
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
}
.icon-btn {
  width: 44px; height: 44px;
  border-radius: var(--radius-sm, 6px);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--text-secondary, #94a3b8);
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: background 0.2s, border-color 0.2s;
}
.icon-btn:hover {
  background: var(--glass-bg-heavy, rgba(255,255,255,0.08));
  border-color: var(--gold, #C9A84C);
  color: var(--gold, #C9A84C);
}
.icon-btn svg { width: 18px; height: 18px; }

/* ── context strip ────────────────────────────────────────── */
.context-strip {
  display: flex; align-items: center;
  padding: 12px 20px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  backdrop-filter: blur(8px);
  gap: 0; overflow-x: auto;
}
.ctx-item { display: flex; flex-direction: column; gap: 2px; padding: 0 16px; flex-shrink: 0; }
.ctx-item:first-child { padding-left: 0; }
.ctx-label { font-size: 10px; text-transform: uppercase; color: var(--text-ghost, rgba(241,245,249,0.45)); font-weight: 700; letter-spacing: 0.4px; }
.ctx-value { font-size: 13px; font-weight: 700; color: var(--text-primary, #f1f5f9); }
.ctx-value--gold { color: var(--gold, #C9A84C); }
.ctx-sep { width: 1px; background: var(--glass-border, rgba(255,255,255,0.08)); align-self: stretch; flex-shrink: 0; }

/* ── states ───────────────────────────────────────────────── */
.state-box {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 16px; padding: 40px; text-align: center;
  color: var(--text-secondary, #94a3b8);
}
.state-box--error { color: #fca5a5; }
.state-icon { width: 48px; height: 48px; color: #ef4444; }
.spinner {
  width: 46px; height: 46px;
  border: 3px solid var(--glass-border, rgba(255,255,255,0.08));
  border-top-color: var(--gold, #C9A84C);
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.btn-retry {
  padding: 10px 26px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--text-primary, #f1f5f9);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-sm, 6px);
  font-weight: 700; cursor: pointer;
  transition: border-color 0.2s;
}
.btn-retry:hover { border-color: var(--gold, #C9A84C); color: var(--gold, #C9A84C); }

/* ── content ──────────────────────────────────────────────── */
.content { flex: 1; padding: 24px 20px; max-width: 1100px; margin: 0 auto; width: 100%; }

.group-title {
  font-size: 16px; font-weight: 700;
  color: var(--text-primary, #f1f5f9);
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  border-left: 3px solid var(--gold, #C9A84C);
  padding-left: 12px;
  margin: 0 0 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px,1fr));
  gap: 14px; margin-bottom: 32px;
}
.empty { text-align: center; padding: 60px; color: var(--text-secondary, #94a3b8); }

/* ── product card (styles for the inline component) ─────── */
:deep(.pcard) {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 12px);
  backdrop-filter: blur(16px);
  overflow: hidden;
  display: flex; flex-direction: column;
  transition: border-color 0.2s, transform 0.2s;
}
:deep(.pcard:hover) {
  border-color: rgba(201,168,76,0.4);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.4);
}
:deep(.pcard__img) {
  width: 100%; aspect-ratio: 1;
  background: rgba(201,168,76,0.04);
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
:deep(.pcard__photo) { width: 100%; height: 100%; object-fit: cover; }
:deep(.pcard__img-placeholder) { font-size: 48px; }
:deep(.pcard__body) { padding: 12px 14px; flex: 1; display: flex; flex-direction: column; gap: 4px; }
:deep(.pcard__name) {
  font-size: 14px; font-weight: 700; margin: 0; line-height: 1.3;
  color: var(--text-primary, #f1f5f9);
}
:deep(.pcard__desc) { font-size: 11px; color: var(--text-secondary, #94a3b8); margin: 0; flex: 1; }
:deep(.pcard__price) { font-size: 16px; font-weight: 900; color: var(--gold, #C9A84C); margin: 6px 0 0; }
:deep(.pcard__ctrl) {
  display: flex; align-items: center;
  padding: 10px 14px;
  border-top: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  gap: 10px;
}
:deep(.ctrl-btn) {
  width: 32px; height: 32px; border-radius: 7px;
  border: 1px solid rgba(201,168,76,0.3);
  background: rgba(201,168,76,0.1);
  color: var(--gold, #C9A84C);
  font-size: 18px; font-weight: 700; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s;
}
:deep(.ctrl-btn:hover:not([disabled])) { background: rgba(201,168,76,0.2); }
:deep(.ctrl-btn[disabled]) { opacity: 0.35; cursor: not-allowed; }
:deep(.ctrl-btn--plus) { background: rgba(201,168,76,0.15); }
:deep(.ctrl-btn--plus:hover) { background: rgba(201,168,76,0.25); }
:deep(.ctrl-qty) {
  flex: 1; text-align: center; font-size: 16px; font-weight: 900;
  color: var(--text-secondary, #94a3b8);
}
:deep(.ctrl-qty--active) { color: var(--gold, #C9A84C); }

/* ── bottom bar ───────────────────────────────────────────── */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 55;
  padding: 14px 20px;
  background: rgba(5,5,8,0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
}
.bottom-bar__totals { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.total-row {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 13px; color: var(--text-secondary, #94a3b8);
}
.total-row--main {
  font-size: 15px; font-weight: 700;
  color: var(--text-primary, #f1f5f9);
  margin-top: 4px;
}
.total-gold { font-size: 18px; color: var(--gold, #C9A84C); font-weight: 900; }

.bottom-bar__actions { display: flex; gap: 10px; }
.btn-skip {
  padding: 11px 20px;
  border-radius: var(--radius-sm, 6px);
  background: transparent;
  color: var(--text-secondary, #94a3b8);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  font-size: 14px; font-weight: 700; cursor: pointer;
  transition: all 0.2s;
}
.btn-skip:hover { border-color: var(--gold, #C9A84C); color: var(--gold, #C9A84C); }
.btn-next {
  padding: 11px 28px;
  border-radius: var(--radius-sm, 6px);
  background: var(--gold, #C9A84C);
  color: #0D0D0D; border: none;
  font-size: 14px; font-weight: 600; cursor: pointer;
  outline: 1.5px solid rgba(201,168,76,0.45); outline-offset: 3px;
  transition: transform 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1)),
              box-shadow 0.3s var(--ease-out, cubic-bezier(0.4,0,0.2,1)),
              outline-offset 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1));
  will-change: transform;
}
.btn-next:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--gold-glow, rgba(201,168,76,0.35));
  outline-offset: 5px;
}

/* ── responsive ───────────────────────────────────────────── */
@media (max-width: 640px) {
  .bottom-bar { flex-direction: column; gap: 10px; }
  .bottom-bar__actions { width: 100%; }
  .btn-skip, .btn-next { flex: 1; text-align: center; }
  .product-grid { grid-template-columns: repeat(auto-fill, minmax(160px,1fr)); }
}
</style>

<template>
  <div class="combo-page">
    <!-- ── Header ── -->
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Combo & Đồ ăn</span>
      <div style="width:38px"></div>
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
        <span class="ctx-value gold">{{ fmtPrice(bookingStore.totalSeatPrice) }}</span>
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
          <span class="gold">{{ fmtPrice(bookingStore.subtotal) }}</span>
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
  background: linear-gradient(160deg,#0b1120 0%,#0f172a 50%,#1a1f35 100%);
  color: #f1f5f9; min-height: 100vh;
  display: flex; flex-direction: column;
  padding-bottom: 200px;
}

/* ── top bar ──────────────────────────────────────────────── */
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px;
  background: rgba(11,17,32,.9); backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255,215,0,.12);
  position: sticky; top: 0; z-index: 60;
}
.top-bar__title { font-size: 16px; font-weight: 700; }
.icon-btn {
  width: 38px; height: 38px; border-radius: 8px;
  border: 1px solid rgba(255,215,0,.25);
  background: rgba(255,215,0,.07); color: #ffd700;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: background .2s;
}
.icon-btn:hover { background: rgba(255,215,0,.15); }
.icon-btn svg { width: 18px; height: 18px; }

/* ── context strip ────────────────────────────────────────── */
.context-strip {
  display: flex; align-items: center;
  padding: 12px 20px;
  background: rgba(30,41,55,.5);
  border-bottom: 1px solid rgba(255,215,0,.1);
  gap: 0; overflow-x: auto;
}
.ctx-item { display: flex; flex-direction: column; gap: 2px; padding: 0 16px; flex-shrink: 0; }
.ctx-item:first-child { padding-left: 0; }
.ctx-label { font-size: 10px; text-transform: uppercase; color: #64748b; font-weight: 700; }
.ctx-value { font-size: 13px; font-weight: 700; }
.ctx-value.gold { color: #ffd700; }
.ctx-sep { width: 1px; background: rgba(255,215,0,.12); align-self: stretch; flex-shrink: 0; }

/* ── states ───────────────────────────────────────────────── */
.state-box {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 16px; padding: 40px; text-align: center;
}
.state-box--error { color: #fca5a5; }
.state-icon { width: 48px; height: 48px; color: #ef4444; }
.spinner {
  width: 46px; height: 46px;
  border: 4px solid rgba(255,215,0,.15);
  border-top-color: #ffd700; border-radius: 50%;
  animation: spin .9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.btn-retry {
  padding: 10px 26px; background: #ffd700; color: #0f172a;
  border: none; border-radius: 8px; font-weight: 800; cursor: pointer;
}

/* ── content ──────────────────────────────────────────────── */
.content { flex: 1; padding: 24px 20px; max-width: 1100px; margin: 0 auto; width: 100%; }
.group-title { font-size: 16px; font-weight: 800; color: #ffd700; margin: 0 0 16px; }
.product-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(220px,1fr));
  gap: 14px; margin-bottom: 32px;
}
.empty { text-align: center; padding: 60px; color: #64748b; }

/* ── product card (styles for the inline component) ─────── */
:deep(.pcard) {
  background: #1e2937; border: 1px solid rgba(255,215,0,.1);
  border-radius: 12px; overflow: hidden;
  display: flex; flex-direction: column;
  transition: border-color .2s, transform .2s;
}
:deep(.pcard:hover) { border-color: rgba(255,215,0,.35); transform: translateY(-3px); }
:deep(.pcard__img) {
  width: 100%; aspect-ratio: 1; background: rgba(255,215,0,.04);
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
:deep(.pcard__photo) { width: 100%; height: 100%; object-fit: cover; }
:deep(.pcard__img-placeholder) { font-size: 48px; }
:deep(.pcard__body) { padding: 12px 14px; flex: 1; display: flex; flex-direction: column; gap: 4px; }
:deep(.pcard__name) { font-size: 14px; font-weight: 800; margin: 0; line-height: 1.3; }
:deep(.pcard__desc) { font-size: 11px; color: #94a3b8; margin: 0; flex: 1; }
:deep(.pcard__price) { font-size: 16px; font-weight: 900; color: #ffd700; margin: 6px 0 0; }
:deep(.pcard__ctrl) {
  display: flex; align-items: center;
  padding: 10px 14px;
  border-top: 1px solid rgba(255,215,0,.07);
  gap: 10px;
}
:deep(.ctrl-btn) {
  width: 32px; height: 32px; border-radius: 7px;
  border: 1px solid rgba(255,215,0,.25);
  background: rgba(255,215,0,.07); color: #ffd700;
  font-size: 18px; font-weight: 700; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background .15s;
}
:deep(.ctrl-btn:hover:not([disabled])) { background: rgba(255,215,0,.18); }
:deep(.ctrl-btn[disabled]) { opacity: .35; cursor: not-allowed; }
:deep(.ctrl-btn--plus) { background: rgba(255,215,0,.12); }
:deep(.ctrl-btn--plus:hover) { background: rgba(255,215,0,.22); }
:deep(.ctrl-qty) {
  flex: 1; text-align: center; font-size: 16px; font-weight: 900;
  color: #94a3b8;
}
:deep(.ctrl-qty--active) { color: #ffd700; }

/* ── bottom bar ───────────────────────────────────────────── */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 55;
  padding: 14px 20px;
  background: rgba(11,17,32,.97); backdrop-filter: blur(16px);
  border-top: 1px solid rgba(255,215,0,.2);
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
}
.bottom-bar__totals { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.total-row {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 13px; color: #94a3b8;
}
.total-row--main { font-size: 15px; font-weight: 800; color: #f1f5f9; margin-top: 4px; }
.total-row--main .gold { font-size: 18px; color: #ffd700; }
.gold { color: #ffd700; }

.bottom-bar__actions { display: flex; gap: 10px; }
.btn-skip {
  padding: 11px 20px; border-radius: 8px;
  background: transparent; color: #94a3b8;
  border: 1px solid rgba(255,255,255,.15);
  font-size: 14px; font-weight: 700; cursor: pointer; transition: all .2s;
}
.btn-skip:hover { border-color: #ffd700; color: #ffd700; }
.btn-next {
  padding: 11px 26px; border-radius: 8px;
  background: linear-gradient(135deg,#ffd700,#ffed4e);
  color: #0f172a; border: none;
  font-size: 14px; font-weight: 900; cursor: pointer; transition: all .2s;
  box-shadow: 0 4px 16px rgba(255,215,0,.3);
}
.btn-next:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(255,215,0,.4); }

/* ── responsive ───────────────────────────────────────────── */
@media (max-width: 640px) {
  .bottom-bar { flex-direction: column; gap: 10px; }
  .bottom-bar__actions { width: 100%; }
  .btn-skip, .btn-next { flex: 1; text-align: center; }
  .product-grid { grid-template-columns: repeat(auto-fill, minmax(160px,1fr)); }
}
</style>

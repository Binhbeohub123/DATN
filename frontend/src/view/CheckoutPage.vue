<template>
  <div class="checkout-page">
    <!-- ── Header ── -->
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Thanh toán</span>
      <ThemeToggle />
    </header>

    <div class="page-body">
      <!-- ── Left / main column ── -->
      <div class="main-col">

        <!-- Order summary -->
        <section class="card">
          <h2 class="card__title">📋 Tóm tắt đơn hàng</h2>

          <div class="summary-block">
            <div class="sum-row">
              <span class="sum-label">Phim</span>
              <span class="sum-val">{{ bookingStore.selectedMovie?.title || '-' }}</span>
            </div>
            <div class="sum-row">
              <span class="sum-label">Suất chiếu</span>
              <span class="sum-val">{{ fmtDatetime(bookingStore.selectedShowtime?.thoiGianBatDau) }}</span>
            </div>
            <div class="sum-row">
              <span class="sum-label">Phòng</span>
              <span class="sum-val">
                {{ bookingStore.selectedShowtime?.tenPhong || bookingStore.selectedShowtime?.phongChieu?.tenPhong || '-' }}
                <span class="badge-type">{{ bookingStore.selectedShowtime?.loaiPhong || bookingStore.selectedShowtime?.phongChieu?.loaiPhong || '' }}</span>
              </span>
            </div>
            <div class="divider"></div>

            <!-- Seats -->
            <div v-for="s in bookingStore.selectedSeats" :key="s.id" class="sum-row sum-row--seat">
              <span class="sum-label seat-tag">Ghế {{ (s.hangGhe||'').trim() }}{{ s.soGhe }} <span :class="['type-pip', typeClass(s.loaiGhe)]">{{ typeLabel(s.loaiGhe) }}</span></span>
              <span class="sum-val">{{ fmtPrice(s.giaTien) }}</span>
            </div>
            <div class="sum-row sum-row--sub">
              <span class="sum-label">Tổng tiền vé ({{ bookingStore.selectedSeats.length }} ghế)</span>
              <span class="sum-val">{{ fmtPrice(bookingStore.totalSeatPrice) }}</span>
            </div>

            <!-- Combos -->
            <template v-if="bookingStore.selectedCombos.length > 0">
              <div class="divider"></div>
              <div v-for="c in bookingStore.selectedCombos" :key="c.id" class="sum-row">
                <span class="sum-label">{{ c.tenSanPham }} × {{ c.soLuong }}</span>
                <span class="sum-val">{{ fmtPrice(c.giaTien * c.soLuong) }}</span>
              </div>
              <div class="sum-row sum-row--sub">
                <span class="sum-label">Tổng combo</span>
                <span class="sum-val">{{ fmtPrice(bookingStore.totalComboPrice) }}</span>
              </div>
            </template>
          </div>
        </section>

        <!-- Promo -->
        <section class="card">
          <h2 class="card__title">🎟️ Mã khuyến mãi</h2>
          <div class="promo-row">
            <input
              v-model="promoInput"
              class="promo-input"
              placeholder="Nhập mã (VD: POLY10)"
              :disabled="!!bookingStore.promoData || checkingPromo"
              @keyup.enter="applyPromo"
            />
            <button
              v-if="!bookingStore.promoData"
              class="btn-apply"
              :disabled="!promoInput.trim() || checkingPromo"
              @click="applyPromo"
            >{{ checkingPromo ? '...' : 'Áp dụng' }}</button>
            <button v-else class="btn-remove" @click="removePromo">✕ Xóa</button>
          </div>
          <p v-if="promoError" class="promo-error">{{ promoError }}</p>
          <div v-if="bookingStore.promoData" class="promo-ok">
            <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>
            <span>{{ bookingStore.promoData.tenKhuyenMai }} — Giảm {{ fmtPrice(bookingStore.promoDiscount) }}</span>
          </div>
          <p class="promo-hint">Mã thử: <strong>POLY10</strong> (−10%), <strong>WELCOME50K</strong> (−50.000đ)</p>
        </section>

        <!-- Loyalty points -->
        <section class="card">
          <h2 class="card__title">⭐ Điểm tích lũy</h2>
          <div class="loyalty-info">
            <span>Số dư: <strong class="gold">{{ authStore.user?.diemTichLuy || 0 }} điểm</strong></span>
            <span class="loyalty-eq">(= {{ fmtPrice((authStore.user?.diemTichLuy || 0) * 1000) }})</span>
          </div>
          <label class="toggle-row">
            <span class="toggle-wrap">
              <input type="checkbox" v-model="useLoyalty" class="toggle-cb" @change="onLoyaltyToggle" />
              <span class="toggle-slider"></span>
            </span>
            <span class="toggle-label">Dùng điểm tích lũy để giảm giá</span>
          </label>
          <div v-if="useLoyalty" class="loyalty-ctrl">
            <input
              type="number" v-model.number="loyaltyPoints"
              :max="maxLoyaltyPoints" min="0"
              class="loyalty-input"
              @change="onLoyaltyToggle"
            />
            <span class="loyalty-eq-inline">= {{ fmtPrice(loyaltyPoints * 1000) }}</span>
          </div>
        </section>

        <!-- Payment method -->
        <section class="card">
          <h2 class="card__title">💳 Phương thức thanh toán</h2>
          <div class="pay-methods">
            <label v-for="m in methods" :key="m.value" :class="['pay-card', { 'pay-card--active': payMethod === m.value }]">
              <input type="radio" v-model="payMethod" :value="m.value" class="pay-radio" />
              <span class="pay-icon">{{ m.icon }}</span>
              <span class="pay-body">
                <span class="pay-name">{{ m.name }}</span>
                <span class="pay-desc">{{ m.desc }}</span>
              </span>
              <span v-if="payMethod === m.value" class="pay-check">✓</span>
            </label>
          </div>
        </section>
      </div>

      <!-- ── Right / price summary ── -->
      <div class="side-col">
        <div class="price-card">
          <h2 class="price-card__title">Tổng thanh toán</h2>
          <div class="price-row">
            <span>Tiền vé</span>
            <span>{{ fmtPrice(bookingStore.totalSeatPrice) }}</span>
          </div>
          <div v-if="bookingStore.totalComboPrice > 0" class="price-row">
            <span>Combo</span>
            <span>{{ fmtPrice(bookingStore.totalComboPrice) }}</span>
          </div>
          <div v-if="bookingStore.promoDiscount > 0" class="price-row price-row--disc">
            <span>Khuyến mãi</span>
            <span>−{{ fmtPrice(bookingStore.promoDiscount) }}</span>
          </div>
          <div v-if="bookingStore.pointsDiscount > 0" class="price-row price-row--disc">
            <span>Điểm tích lũy</span>
            <span>−{{ fmtPrice(bookingStore.pointsDiscount) }}</span>
          </div>
          <div class="price-divider"></div>
          <div class="price-row price-row--total">
            <span>Thành tiền</span>
            <span>{{ fmtPrice(bookingStore.totalPrice) }}</span>
          </div>

          <button
            class="btn-confirm"
            :disabled="confirming || !payMethod"
            @click="confirm"
          >
            <span v-if="confirming" class="btn-spinner"></span>
            <span v-else>Xác nhận thanh toán</span>
          </button>

          <p v-if="confirmError" class="confirm-error">{{ confirmError }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useBookingStore } from '@/stores/bookingStore'
import api from '@/services/api'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router       = useRouter()
const authStore    = useAuthStore()
const bookingStore = useBookingStore()

// ── promo ────────────────────────────────────────────────────
const promoInput   = ref(bookingStore.promoCode || '')
const checkingPromo = ref(false)
const promoError   = ref('')

async function applyPromo() {
  if (!promoInput.value.trim()) return
  checkingPromo.value = true
  promoError.value = ''
  const ok = await bookingStore.validatePromo(promoInput.value.trim().toUpperCase())
  if (!ok) promoError.value = bookingStore.error.promo || 'Mã không hợp lệ'
  checkingPromo.value = false
}
function removePromo() {
  bookingStore.clearPromo()
  promoInput.value = ''
  promoError.value = ''
}

// ── loyalty ──────────────────────────────────────────────────
const useLoyalty   = ref(bookingStore.useLoyaltyPoints)
const loyaltyPoints = ref(bookingStore.loyaltyPointsToUse)

const maxLoyaltyPoints = computed(() => {
  const userPts = authStore.user?.diemTichLuy || 0
  const maxFromOrder = Math.floor(bookingStore.subtotal / 1000)
  return Math.min(userPts, maxFromOrder)
})

function onLoyaltyToggle() {
  if (!useLoyalty.value) {
    loyaltyPoints.value = 0
    bookingStore.setUseLoyaltyPoints(false, 0)
  } else {
    loyaltyPoints.value = Math.min(loyaltyPoints.value, maxLoyaltyPoints.value)
    bookingStore.setUseLoyaltyPoints(true, loyaltyPoints.value)
  }
}

// ── payment method ───────────────────────────────────────────
const payMethod = ref(bookingStore.paymentMethod || 'VNPay')
const methods = [
  { value: 'VNPay', icon: '🏦', name: 'VNPay', desc: 'Thanh toán qua cổng VNPay' },
  { value: 'Momo',  icon: '📱', name: 'MoMo',  desc: 'Ví điện tử MoMo' },
  { value: 'Cash',  icon: '💵', name: 'Tiền mặt', desc: 'Thanh toán tại quầy' },
]

// ── confirm ──────────────────────────────────────────────────
const confirming   = ref(false)
const confirmError = ref('')

async function confirm() {
  if (confirming.value) return
  confirmError.value = ''
  confirming.value = true

  bookingStore.setPaymentMethod(payMethod.value)
  if (useLoyalty.value) bookingStore.setUseLoyaltyPoints(true, loyaltyPoints.value)

  try {
    const booking = await bookingStore.createBooking()
    if (!booking || !booking.id) {
      confirmError.value = bookingStore.error.booking || 'Không tạo được đơn đặt vé'
      return
    }

    if (payMethod.value === 'Cash') {
      router.push({ name: 'payment-result', params: { bookingId: String(booking.id) } })
      return
    }

    const endpoint = payMethod.value === 'VNPay' ? '/thanh-toan/vnpay' : '/thanh-toan/momo'
    const payRes = await api.post(endpoint, { datVeId: booking.id })
    const url = payRes.data?.paymentUrl
    if (url) {
      window.location.href = url
    } else {
      router.push({ name: 'payment-result', params: { bookingId: String(booking.id) } })
    }
  } catch (e) {
    confirmError.value = e.response?.data?.message || e.response?.data || 'Lỗi khi xử lý thanh toán'
  } finally {
    confirming.value = false
  }
}

// ── helpers ──────────────────────────────────────────────────
function fmtPrice(v) {
  if (v == null) return '-'
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(v)
}
function fmtDatetime(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('vi-VN',{ day:'2-digit', month:'2-digit', hour:'2-digit', minute:'2-digit' })
}
function typeClass(t) {
  if (!t) return ''
  const l = t.toLowerCase()
  if (l === 'vip')    return 'pip-vip'
  if (l.includes('cặp') || l.includes('couple')) return 'pip-couple'
  return ''
}
function typeLabel(t) {
  if (!t) return ''
  const l = t.toLowerCase()
  if (l === 'vip') return 'VIP'
  if (l.includes('cặp') || l.includes('couple')) return 'Cặp đôi'
  return ''
}

onMounted(() => {
  if (!authStore.isLoggedIn) { router.push('/auth'); return }
})
</script>

<style scoped>
/* ── base ──────────────────────────────────────────────────── */
.checkout-page {
  background: var(--void, #050508);
  color: var(--text-secondary, #94a3b8);
  min-height: 100vh;
  font-family: var(--font-ui, 'Inter', sans-serif);
}

/* ── top bar ──────────────────────────────────────────────── */
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px;
  background: rgba(5,5,8,0.85);
  backdrop-filter: var(--glass-blur, blur(20px));
  -webkit-backdrop-filter: var(--glass-blur, blur(20px));
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  position: sticky; top: 0; z-index: 20;
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
  color: var(--electric, #29bcea);
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: background 0.2s, border-color 0.2s;
}
.icon-btn:hover {
  background: var(--glass-bg-heavy, rgba(255,255,255,0.08));
  border-color: var(--electric, #29bcea);
}
.icon-btn svg { width: 18px; height: 18px; }

/* ── layout ───────────────────────────────────────────────── */
.page-body {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  padding: 24px 20px;
  max-width: 1200px;
  margin: 0 auto;
  align-items: start;
}
.main-col { display: flex; flex-direction: column; gap: 16px; }

/* ── glass cards ──────────────────────────────────────────── */
.card {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: var(--radius-md, 12px);
  padding: 20px;
}
.card__title {
  font-size: 15px; font-weight: 700;
  color: var(--text-primary, #f1f5f9); margin: 0 0 16px;
  font-family: var(--font-ui, 'Inter', sans-serif);
}

/* ── order summary ────────────────────────────────────────── */
.summary-block { display: flex; flex-direction: column; gap: 0; }
.sum-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 0; font-size: 14px;
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.sum-row:last-child { border-bottom: none; }
.sum-label { color: var(--text-secondary, #94a3b8); font-weight: 600; }
.sum-val { color: var(--text-primary, #f1f5f9); font-weight: 400; text-align: right; }
.sum-row--sub .sum-val { font-weight: 700; color: var(--electric, #29bcea); }
.seat-tag { display: flex; align-items: center; gap: 6px; }
.badge-type {
  padding: 2px 8px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--glass-bg, rgba(255,255,255,0.04));
  font-size: 12px; font-weight: 700; color: var(--electric, #29bcea);
  border-radius: var(--radius-sm, 6px);
}
.type-pip {
  padding: 2px 8px; font-size: 11px; font-weight: 700;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--text-secondary, #94a3b8);
  border-radius: var(--radius-sm, 6px);
}
.divider { height: 1px; background: var(--glass-border, rgba(255,255,255,0.08)); margin: 8px 0; }

/* ── promo ────────────────────────────────────────────────── */
.promo-row { display: flex; gap: 8px; margin-bottom: 10px; }
.promo-input {
  flex: 1; min-height: 44px; padding: 10px 12px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-sm, 6px);
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--text-primary, #f1f5f9);
  font-size: 14px; font-family: var(--font-ui, 'Inter', sans-serif);
  transition: border-color 0.3s;
}
.promo-input:focus {
  outline: none;
  border-color: var(--electric, #29bcea);
  box-shadow: 0 0 0 2px var(--electric-soft, rgba(41,188,234,0.08));
}
.promo-input::placeholder { color: var(--text-ghost, rgba(241,245,249,0.45)); }
.promo-input:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-apply {
  min-height: 44px; padding: 10px 16px;
  background: var(--electric, #29bcea); color: var(--on-accent, #ffffff);
  border: none; border-radius: var(--radius-sm, 6px);
  font-size: 14px; font-weight: 700; cursor: pointer;
  transition: background 0.2s;
}
.btn-apply:hover:not(:disabled) { background: var(--electric-hover, #1a9fbd); }
.btn-apply:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-remove {
  min-height: 44px; padding: 10px 14px;
  background: transparent; color: var(--electric, #29bcea);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-sm, 6px);
  font-size: 14px; font-weight: 700; cursor: pointer;
  transition: border-color 0.2s;
}
.btn-remove:hover { border-color: var(--electric, #29bcea); }
.promo-error { font-size: 12px; color: #f87171; margin: 0 0 6px; }
.promo-ok {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 12px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid rgba(52,211,153,0.25);
  border-radius: var(--radius-sm, 6px);
  font-size: 14px; color: #34d399; font-weight: 700; margin-bottom: 8px;
}
.promo-hint { font-size: 12px; color: var(--text-ghost, rgba(241,245,249,0.45)); margin: 0; }

/* ── loyalty ──────────────────────────────────────────────── */
.loyalty-info { font-size: 14px; color: var(--text-secondary, #94a3b8); margin-bottom: 14px; }
.gold { color: var(--gold-bright, #F5D17E); font-weight: 700; }
.loyalty-eq { font-size: 12px; color: var(--text-ghost, rgba(241,245,249,0.45)); margin-left: 6px; }
.toggle-row { display: flex; align-items: center; gap: 12px; cursor: pointer; margin-bottom: 12px; }
.toggle-wrap { position: relative; width: 44px; height: 24px; flex-shrink: 0; }
.toggle-cb { position: absolute; opacity: 0; width: 0; height: 0; }
.toggle-slider {
  position: absolute; inset: 0;
  background: var(--surface-3, #1a1a28);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-pill, 999px);
  cursor: pointer;
  transition: background 0.2s;
}
.toggle-slider::after {
  content: ''; position: absolute;
  width: 18px; height: 18px; background: var(--text-secondary, #94a3b8);
  top: 2px; left: 2px;
  border-radius: 50%;
  transition: transform 0.2s, background 0.2s;
}
.toggle-cb:checked ~ .toggle-slider { background: var(--electric-soft, rgba(41,188,234,0.08)); border-color: var(--electric, #29bcea); }
.toggle-cb:checked ~ .toggle-slider::after { transform: translateX(20px); background: var(--electric, #29bcea); }
.toggle-label { font-size: 14px; font-weight: 700; color: var(--text-primary, #f1f5f9); }
.loyalty-ctrl { display: flex; align-items: center; gap: 10px; }
.loyalty-input {
  width: 120px; min-height: 44px; padding: 8px 10px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-sm, 6px);
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--text-primary, #f1f5f9);
  font-size: 14px;
}
.loyalty-eq-inline { font-size: 13px; color: var(--text-ghost, rgba(241,245,249,0.45)); }

/* ── payment methods ──────────────────────────────────────── */
.pay-methods { display: flex; flex-direction: column; gap: 10px; }
.pay-card {
  display: flex; align-items: center; gap: 12px; padding: 14px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 12px);
  background: var(--glass-bg, rgba(255,255,255,0.04));
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.pay-card--active {
  border-color: var(--electric, #29bcea);
  box-shadow: 0 0 0 1px var(--electric-soft, rgba(41,188,234,0.08));
  background: var(--glass-bg-heavy, rgba(255,255,255,0.08));
}
.pay-radio { display: none; }
.pay-icon { font-size: 24px; width: 36px; text-align: center; flex-shrink: 0; }
.pay-body { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.pay-name { font-size: 14px; font-weight: 700; color: var(--text-primary, #f1f5f9); }
.pay-desc { font-size: 12px; color: var(--text-secondary, #94a3b8); }
.pay-check { font-size: 18px; color: var(--electric, #29bcea); font-weight: 700; }

/* ── price card (double-bezel) ────────────────────────────── */
.side-col { position: sticky; top: 84px; }
.price-card {
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 12px);
  padding: 4px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}
.price-card > * {
  /* Inner bezel wrapper — rendered as direct children inside the price-card padding */
  border-radius: calc(var(--radius-md, 12px) - 4px);
}
.price-card__title {
  font-size: 15px; font-weight: 700;
  color: var(--text-primary, #f1f5f9); margin: 0 0 16px;
  padding: 16px 16px 0;
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
}
.price-row {
  display: flex; justify-content: space-between;
  font-size: 14px; color: var(--text-secondary, #94a3b8); padding: 8px 16px;
}
.price-row--disc { color: #34d399; }
.price-divider {
  height: 1px;
  background: var(--glass-border, rgba(255,255,255,0.08));
  margin: 12px 16px;
}
.price-row--total {
  font-size: 18px; font-weight: 700;
  color: var(--electric, #29bcea); padding: 8px 16px;
}

/* ── confirm button (BiB) ─────────────────────────────────── */
.btn-confirm {
  width: calc(100% - 32px); min-height: 44px; margin: 16px 16px;
  padding: 12px; border: none;
  background: var(--electric, #29bcea); color: var(--on-accent, #ffffff);
  font-size: 14px; font-weight: 700; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 10px;
  border-radius: var(--radius-sm, 6px);
  outline: 1.5px solid rgba(41,188,234,0.45); outline-offset: 3px;
  transition: transform 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1)),
              box-shadow 0.3s var(--ease-out, cubic-bezier(0.4,0,0.2,1)),
              outline-offset 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1));
  will-change: transform;
}
.btn-confirm:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--electric-glow, rgba(41,188,234,0.30));
  outline-offset: 5px;
}
.btn-confirm:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-spinner {
  width: 20px; height: 20px;
  border: 3px solid rgba(255,255,255,0.4); border-top-color: #fff;
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.confirm-error {
  margin: 0 16px 12px; font-size: 13px; color: #f87171; text-align: center;
  padding: 10px 12px;
  background: rgba(248,113,113,0.08);
  border: 1px solid rgba(248,113,113,0.25);
  border-radius: var(--radius-sm, 6px);
}

/* ── responsive ───────────────────────────────────────────── */
@media (max-width: 900px) {
  .page-body { grid-template-columns: 1fr; }
  .side-col { position: static; }
}
@media (max-width: 640px) {
  .page-body { padding: 16px; }
}
</style>

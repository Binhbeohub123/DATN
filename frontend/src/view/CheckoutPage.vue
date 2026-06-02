<template>
  <div class="checkout-page">
    <!-- ── Header ── -->
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Thanh toán</span>
      <div style="width:38px"></div>
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
              <span class="sum-val">{{ bookingStore.selectedMovie?.title || '—' }}</span>
            </div>
            <div class="sum-row">
              <span class="sum-label">Suất chiếu</span>
              <span class="sum-val">{{ fmtDatetime(bookingStore.selectedShowtime?.thoiGianBatDau) }}</span>
            </div>
            <div class="sum-row">
              <span class="sum-label">Phòng</span>
              <span class="sum-val">
                {{ bookingStore.selectedShowtime?.tenPhong || bookingStore.selectedShowtime?.phongChieu?.tenPhong || '—' }}
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
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(v)
}
function fmtDatetime(dt) {
  if (!dt) return '—'
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
  background: linear-gradient(160deg,#0b1120 0%,#0f172a 50%,#1a1f35 100%);
  color: #f1f5f9; min-height: 100vh;
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

/* ── layout ───────────────────────────────────────────────── */
.page-body {
  display: grid; grid-template-columns: 1fr 360px;
  gap: 24px; padding: 28px 24px;
  max-width: 1100px; margin: 0 auto;
  align-items: start;
}
.main-col { display: flex; flex-direction: column; gap: 20px; }

/* ── card ─────────────────────────────────────────────────── */
.card {
  background: rgba(30,41,55,.5);
  border: 1px solid rgba(255,215,0,.1);
  border-radius: 14px; padding: 20px;
}
.card__title { font-size: 16px; font-weight: 800; color: #ffd700; margin: 0 0 16px; }

/* ── summary ──────────────────────────────────────────────── */
.summary-block { display: flex; flex-direction: column; gap: 0; }
.sum-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; font-size: 13px;
  border-bottom: 1px solid rgba(255,215,0,.04);
}
.sum-row:last-child { border-bottom: none; }
.sum-label { color: #94a3b8; font-weight: 600; }
.sum-val   { color: #f1f5f9; font-weight: 700; text-align: right; }
.sum-row--seat .sum-val { color: #ffd700; }
.sum-row--sub .sum-val  { font-weight: 800; }
.seat-tag { display: flex; align-items: center; gap: 6px; }
.badge-type {
  padding: 1px 7px; border-radius: 4px;
  background: rgba(255,215,0,.15); color: #ffd700;
  font-size: 11px; font-weight: 800; text-transform: uppercase;
}
.type-pip {
  padding: 1px 6px; border-radius: 4px;
  font-size: 10px; font-weight: 800;
}
.pip-vip    { background: rgba(124,58,237,.25); color: #c4b5fd; }
.pip-couple { background: rgba(236,72,153,.25); color: #f9a8d4; }
.divider { height: 1px; background: rgba(255,215,0,.1); margin: 8px 0; }

/* ── promo ────────────────────────────────────────────────── */
.promo-row { display: flex; gap: 8px; margin-bottom: 10px; }
.promo-input {
  flex: 1; padding: 10px 14px;
  border: 1px solid rgba(255,215,0,.2); border-radius: 8px;
  background: rgba(255,215,0,.04); color: #f1f5f9; font-size: 14px;
  transition: border-color .2s;
}
.promo-input:focus { outline: none; border-color: #ffd700; }
.promo-input:disabled { opacity: .6; }
.btn-apply {
  padding: 10px 18px; border-radius: 8px;
  background: rgba(255,215,0,.12); border: 1px solid rgba(255,215,0,.3);
  color: #ffd700; font-weight: 800; font-size: 13px; cursor: pointer; transition: all .2s;
}
.btn-apply:hover:not(:disabled) { background: rgba(255,215,0,.22); }
.btn-apply:disabled { opacity: .4; cursor: not-allowed; }
.btn-remove {
  padding: 10px 14px; border-radius: 8px;
  background: rgba(239,68,68,.1); border: 1px solid rgba(239,68,68,.25);
  color: #fca5a5; font-weight: 700; font-size: 13px; cursor: pointer; transition: all .2s;
}
.btn-remove:hover { background: rgba(239,68,68,.2); }
.promo-error { font-size: 12px; color: #fca5a5; margin: 0 0 6px; }
.promo-ok {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 14px;
  background: rgba(34,197,94,.1); border: 1px solid rgba(34,197,94,.25); border-radius: 8px;
  font-size: 13px; color: #86efac; font-weight: 700; margin-bottom: 8px;
}
.promo-ok svg { flex-shrink: 0; color: #4ade80; }
.promo-hint { font-size: 11px; color: #64748b; margin: 0; }
.promo-hint strong { color: #94a3b8; }

/* ── loyalty ──────────────────────────────────────────────── */
.loyalty-info { font-size: 13px; color: #94a3b8; margin-bottom: 14px; }
.gold { color: #ffd700; }
.loyalty-eq { font-size: 11px; color: #64748b; margin-left: 6px; }

.toggle-row { display: flex; align-items: center; gap: 12px; cursor: pointer; margin-bottom: 12px; }
.toggle-wrap { position: relative; width: 44px; height: 24px; flex-shrink: 0; }
.toggle-cb { position: absolute; opacity: 0; width: 0; height: 0; }
.toggle-slider {
  position: absolute; inset: 0; border-radius: 12px;
  background: rgba(255,255,255,.15); transition: background .2s;
  cursor: pointer;
}
.toggle-slider::after {
  content: ''; position: absolute;
  width: 18px; height: 18px; border-radius: 50%;
  background: #fff; top: 3px; left: 3px; transition: transform .2s;
}
.toggle-cb:checked ~ .toggle-slider { background: #ffd700; }
.toggle-cb:checked ~ .toggle-slider::after { transform: translateX(20px); }
.toggle-label { font-size: 13px; font-weight: 700; color: #f1f5f9; }

.loyalty-ctrl { display: flex; align-items: center; gap: 10px; }
.loyalty-input {
  width: 100px; padding: 8px 12px;
  border: 1px solid rgba(255,215,0,.2); border-radius: 8px;
  background: rgba(255,215,0,.06); color: #ffd700;
  font-size: 14px; font-weight: 700;
}
.loyalty-input:focus { outline: none; border-color: #ffd700; }
.loyalty-eq-inline { font-size: 13px; color: #94a3b8; }

/* ── payment methods ──────────────────────────────────────── */
.pay-methods { display: flex; flex-direction: column; gap: 10px; }
.pay-card {
  display: flex; align-items: center; gap: 14px; padding: 14px 16px;
  border: 2px solid rgba(255,215,0,.1); border-radius: 12px;
  background: rgba(255,215,0,.03); cursor: pointer; transition: all .2s;
}
.pay-card:hover { border-color: rgba(255,215,0,.3); background: rgba(255,215,0,.07); }
.pay-card--active { border-color: #ffd700; background: rgba(255,215,0,.1); }
.pay-radio { display: none; }
.pay-icon  { font-size: 28px; width: 40px; text-align: center; flex-shrink: 0; }
.pay-body  { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.pay-name  { font-size: 14px; font-weight: 800; color: #f1f5f9; }
.pay-desc  { font-size: 12px; color: #64748b; }
.pay-check { font-size: 18px; color: #ffd700; font-weight: 900; }

/* ── side price card ──────────────────────────────────────── */
.side-col { position: sticky; top: 80px; }
.price-card {
  background: rgba(30,41,55,.6);
  border: 1px solid rgba(255,215,0,.2);
  border-radius: 16px; padding: 24px;
}
.price-card__title { font-size: 16px; font-weight: 800; color: #ffd700; margin: 0 0 16px; }
.price-row {
  display: flex; justify-content: space-between;
  font-size: 13px; color: #94a3b8; padding: 6px 0;
}
.price-row--disc { color: #4ade80; }
.price-divider { height: 1px; background: rgba(255,215,0,.15); margin: 12px 0; }
.price-row--total {
  font-size: 18px; font-weight: 900; color: #ffd700; padding: 8px 0;
}
.btn-confirm {
  width: 100%; margin-top: 16px; padding: 15px;
  background: linear-gradient(135deg,#ffd700,#ffed4e);
  color: #0f172a; border: none; border-radius: 10px;
  font-size: 15px; font-weight: 900; cursor: pointer; transition: all .2s;
  display: flex; align-items: center; justify-content: center; gap: 10px;
  box-shadow: 0 6px 20px rgba(255,215,0,.3);
}
.btn-confirm:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 10px 28px rgba(255,215,0,.4); }
.btn-confirm:disabled { opacity: .5; cursor: not-allowed; box-shadow: none; }
.btn-spinner {
  width: 20px; height: 20px; border: 3px solid rgba(15,23,42,.3);
  border-top-color: #0f172a; border-radius: 50%;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.confirm-error {
  margin-top: 12px; font-size: 13px; color: #fca5a5;
  text-align: center; padding: 10px;
  background: rgba(239,68,68,.1); border-radius: 8px;
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

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
.checkout-page { background: #ffffff; color: #7f7e7f; min-height: 100vh; }
.top-bar { display: flex; align-items: center; justify-content: space-between; padding: 20px; border-bottom: 1px solid #efefef; background: #ffffff; position: sticky; top: 0; z-index: 20; }
.top-bar__title { font-size: 16.8px; font-weight: 700; color: #29bcea; }
.icon-btn { width: 44px; height: 44px; border: 1px solid #efefef; background: #f7f7f7; color: #29bcea; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.icon-btn svg { width: 18px; height: 18px; }
.page-body { display: grid; grid-template-columns: 1fr 360px; gap: 20px; padding: 24px 20px; max-width: 1200px; margin: 0 auto; align-items: start; }
.main-col { display: flex; flex-direction: column; gap: 16px; }
.card { background: #f7f7f7; border: 1px solid #efefef; padding: 20px; }
.card__title { font-size: 16.8px; font-weight: 700; color: #29bcea; margin: 0 0 16px; }
.summary-block { display: flex; flex-direction: column; gap: 0; }
.sum-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; font-size: 14.4px; border-bottom: 1px solid #efefef; }
.sum-row:last-child { border-bottom: none; }
.sum-label { color: #7f7e7f; font-weight: 700; }
.sum-val { color: #000000; font-weight: 400; text-align: right; }
.sum-row--sub .sum-val { font-weight: 700; color: #29bcea; }
.seat-tag { display: flex; align-items: center; gap: 6px; }
.badge-type { padding: 2px 8px; border: 1px solid #efefef; background: #ffffff; font-size: 12px; font-weight: 700; color: #29bcea; }
.type-pip { padding: 2px 8px; font-size: 11px; font-weight: 700; border: 1px solid #efefef; background: #fff; color: #767676; }
.divider { height: 1px; background: #efefef; margin: 8px 0; }
.promo-row { display: flex; gap: 8px; margin-bottom: 10px; }
.promo-input { flex: 1; min-height: 44px; padding: 10px 12px; border: 1px solid #efefef; background: #fff; color: #000; font-size: 14.4px; }
.promo-input:focus { outline: none; border-color: #29bcea; box-shadow: 0 0 0 2px rgba(41,188,234,.15); }
.btn-apply,.btn-remove { min-height: 44px; padding: 10px 14px; background: #29bcea; color: #fff; border: none; font-size: 14.4px; font-weight: 700; cursor: pointer; }
.btn-apply:disabled { opacity: .6; cursor: not-allowed; }
.btn-remove { background: transparent; color: #29bcea; border: 1px solid #29bcea; }
.promo-error { font-size: 12px; color: #8f2a2a; margin: 0 0 6px; }
.promo-ok { display: flex; align-items: center; gap: 6px; padding: 10px 12px; background: #ffffff; border: 1px solid #efefef; font-size: 14.4px; color: #177245; font-weight: 700; margin-bottom: 8px; }
.promo-hint { font-size: 12px; color: #767676; margin: 0; }
.loyalty-info { font-size: 14.4px; color: #7f7e7f; margin-bottom: 14px; }
.gold { color: #29bcea; font-weight: 700; }
.loyalty-eq { font-size: 12px; color: #767676; margin-left: 6px; }
.toggle-row { display: flex; align-items: center; gap: 12px; cursor: pointer; margin-bottom: 12px; }
.toggle-wrap { position: relative; width: 44px; height: 24px; flex-shrink: 0; }
.toggle-cb { position: absolute; opacity: 0; width: 0; height: 0; }
.toggle-slider { position: absolute; inset: 0; background: #d9d9d9; cursor: pointer; }
.toggle-slider::after { content: ''; position: absolute; width: 18px; height: 18px; background: #fff; top: 3px; left: 3px; transition: transform .2s; border: 1px solid #efefef; }
.toggle-cb:checked ~ .toggle-slider { background: #29bcea; }
.toggle-cb:checked ~ .toggle-slider::after { transform: translateX(20px); }
.toggle-label { font-size: 14.4px; font-weight: 700; color: #000; }
.loyalty-ctrl { display: flex; align-items: center; gap: 10px; }
.loyalty-input { width: 120px; min-height: 44px; padding: 8px 10px; border: 1px solid #efefef; background: #fff; color: #000; font-size: 14.4px; }
.loyalty-eq-inline { font-size: 13px; color: #7f7e7f; }
.pay-methods { display: flex; flex-direction: column; gap: 10px; }
.pay-card { display: flex; align-items: center; gap: 12px; padding: 12px; border: 1px solid #efefef; background: #fff; cursor: pointer; }
.pay-card--active { border-color: #29bcea; background: #f7fcfe; }
.pay-radio { display: none; }
.pay-icon { font-size: 24px; width: 36px; text-align: center; flex-shrink: 0; }
.pay-body { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.pay-name { font-size: 14.4px; font-weight: 700; color: #000; }
.pay-desc { font-size: 12px; color: #7f7e7f; }
.pay-check { font-size: 18px; color: #29bcea; font-weight: 700; }
.side-col { position: sticky; top: 84px; }
.price-card { background: #ffffff; border: 1px solid #efefef; padding: 20px; }
.price-card__title { font-size: 16.8px; font-weight: 700; color: #29bcea; margin: 0 0 16px; }
.price-row { display: flex; justify-content: space-between; font-size: 14.4px; color: #7f7e7f; padding: 8px 0; }
.price-row--disc { color: #177245; }
.price-divider { height: 1px; background: #efefef; margin: 12px 0; }
.price-row--total { font-size: 18px; font-weight: 700; color: #29bcea; padding: 8px 0; }
.btn-confirm { width: 100%; min-height: 44px; margin-top: 16px; padding: 12px; background: #29bcea; color: #fff; border: none; font-size: 14.4px; font-weight: 700; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 10px; }
.btn-confirm:hover:not(:disabled) { background: #1a9fbd; }
.btn-confirm:disabled { opacity: .6; cursor: not-allowed; }
.btn-spinner { width: 20px; height: 20px; border: 3px solid rgba(255,255,255,.4); border-top-color: #fff; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.confirm-error { margin-top: 12px; font-size: 13px; color: #8f2a2a; text-align: center; padding: 10px; background: #fff5f5; border: 1px solid #f0d4d4; }
@media (max-width: 900px) { .page-body { grid-template-columns: 1fr; } .side-col { position: static; } }
@media (max-width: 640px) { .page-body { padding: 16px; } }
</style>

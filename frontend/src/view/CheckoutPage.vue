<template>
  <div class="checkout-page">
    <!-- ── Booking expiry countdown banner ── -->
    <transition name="slide-down">
      <div v-if="countdownDisplay" :class="['countdown-banner', countdownClass]">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        Vui lòng thanh toán trong
        <strong>{{ countdownDisplay }}</strong>
      </div>
    </transition>

    <!-- ── Booking expired modal ── -->
    <transition name="fade">
      <div v-if="showExpiredModal" class="expired-overlay">
        <div class="expired-modal">
          <div class="expired-icon">⏰</div>
          <h2 class="expired-title">Đơn hàng đã hết hạn</h2>
          <p class="expired-desc">Ghế đã được trả lại. Đang chuyển về trang phim...</p>
        </div>
      </div>
    </transition>

    <!-- ── Header ── -->
    <header class="top-bar">
      <button class="icon-btn" @click="goBack" aria-label="Quay lại">
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
              <span class="sum-val">{{ displayMovieName }}</span>
            </div>
            <div class="sum-row">
              <span class="sum-label">Suất chiếu</span>
              <span class="sum-val">{{ displayShowtime }}</span>
            </div>
            <div class="sum-row">
              <span class="sum-label">Phòng</span>
              <span class="sum-val">
                {{ displayRoom }}
                <span v-if="displayRoomType" class="badge-type">{{ displayRoomType }}</span>
              </span>
            </div>
            <div class="divider"></div>

            <!-- Seats -->
            <template v-if="retryBookingId && retryBooking">
              <!-- Retry mode: seats from ChiTietDatGhe on the fetched booking -->
              <div v-for="ct in (retryBooking.chiTietDatGhe || [])" :key="ct.gheNgoi?.id" class="sum-row sum-row--seat">
                <span class="sum-label seat-tag">
                  Ghế {{ (ct.gheNgoi?.hangGhe || '').trim() }}{{ ct.gheNgoi?.soGhe }}
                  <span v-if="ct.gheNgoi?.loaiGhe && ct.gheNgoi.loaiGhe !== 'thường'" :class="['type-pip', typeClass(ct.gheNgoi.loaiGhe)]">{{ typeLabel(ct.gheNgoi.loaiGhe) }}</span>
                </span>
                <span class="sum-val">{{ fmtPrice(ct.giaTien) }}</span>
              </div>
              <div class="sum-row sum-row--sub">
                <span class="sum-label">Tổng tiền vé ({{ (retryBooking.chiTietDatGhe || []).length }} ghế)</span>
                <span class="sum-val">{{ fmtPrice(retryTotalSeatPrice) }}</span>
              </div>
              <!-- Combos in retry mode -->
              <template v-if="(retryBooking.chiTietDatSanPham || []).length > 0">
                <div class="divider"></div>
                <div v-for="c in (retryBooking.chiTietDatSanPham || [])" :key="c.sanPham?.id" class="sum-row">
                  <span class="sum-label">{{ c.sanPham?.tenSanPham || '?' }} × {{ c.soLuong }}</span>
                  <span class="sum-val">{{ fmtPrice((c.giaLucMua || 0) * c.soLuong) }}</span>
                </div>
                <div class="sum-row sum-row--sub">
                  <span class="sum-label">Tổng combo</span>
                  <span class="sum-val">{{ fmtPrice(retryTotalComboPrice) }}</span>
                </div>
              </template>
            </template>
            <template v-else>
              <!-- Normal mode: seats from bookingStore -->
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
            <span>
              {{ bookingStore.promoData.tenKhuyenMai }}
              <template v-if="bookingStore.promoDiscount > 0">
                — Giảm <strong>{{ fmtPrice(bookingStore.promoDiscount) }}</strong>
              </template>
              <template v-else>
                — Đang tính...
              </template>
            </span>
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
            <span>{{ retryBookingId && retryBooking ? fmtPrice(retryTotalSeatPrice) : fmtPrice(bookingStore.totalSeatPrice) }}</span>
          </div>
          <template v-if="retryBookingId && retryBooking">
            <div v-if="retryTotalComboPrice > 0" class="price-row">
              <span>Combo</span>
              <span>{{ fmtPrice(retryTotalComboPrice) }}</span>
            </div>
          </template>
          <template v-else>
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
            <div v-if="bookingStore.promoDiscount > 0 || bookingStore.pointsDiscount > 0" class="price-row price-row--original">
              <span>Giá gốc</span>
              <span class="price-strikethrough">{{ fmtPrice(bookingStore.subtotal) }}</span>
            </div>
          </template>
          <div class="price-divider"></div>
          <div class="price-row price-row--total">
            <span>Thành tiền</span>
            <span>{{ retryBookingId && retryBooking ? fmtPrice(retryBooking.tongTienThanhToan) : fmtPrice(bookingStore.totalPrice) }}</span>
          </div>

          <button
            class="btn-confirm"
            :disabled="confirming || !payMethod || isExpired"
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
import { ref, computed, onMounted, onUnmounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useBookingStore } from '@/stores/bookingStore'
import api from '@/services/api'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router       = useRouter()
const route        = useRoute()
const authStore    = useAuthStore()
const bookingStore = useBookingStore()

// ── Back navigation — always go somewhere safe ───────────────
function goBack() {
  if (bookingStore.selectedShowtime?.id) {
    router.push(`/seat-selection/${bookingStore.selectedShowtime.id}`)
  } else {
    router.push('/movies')
  }
}

// ── Retry-payment mode (coming from MyTicketsPage) ───────────
const retryBookingId  = ref(null)
const retryBooking    = ref(null)
const loadingRetry    = ref(false)
const retryLoadError  = ref('')

async function loadRetryBooking(id) {
  loadingRetry.value = true
  retryLoadError.value = ''
  try {
    const res = await api.get(`/dat-ve/${id}`)
    retryBooking.value = res.data
  } catch (e) {
    retryLoadError.value = e.response?.data?.message || 'Không tải được đơn đặt vé'
  } finally {
    loadingRetry.value = false
  }
}

// ── Display computeds — show retry booking data or store data ─
const displayMovieName = computed(() => {
  if (retryBookingId.value && retryBooking.value) {
    return retryBooking.value.lichChieu?.phim?.tenPhim || '-'
  }
  return bookingStore.selectedMovie?.title || '-'
})

const displayShowtime = computed(() => {
  if (retryBookingId.value && retryBooking.value) {
    return fmtDatetime(retryBooking.value.lichChieu?.thoiGianBatDau)
  }
  return fmtDatetime(bookingStore.selectedShowtime?.thoiGianBatDau)
})

const displayRoom = computed(() => {
  if (retryBookingId.value && retryBooking.value) {
    return retryBooking.value.lichChieu?.phongChieu?.tenPhong || '-'
  }
  return bookingStore.selectedShowtime?.tenPhong
      || bookingStore.selectedShowtime?.phongChieu?.tenPhong
      || '-'
})

const displayRoomType = computed(() => {
  if (retryBookingId.value && retryBooking.value) {
    return retryBooking.value.lichChieu?.phongChieu?.loaiPhong || ''
  }
  return bookingStore.selectedShowtime?.loaiPhong
      || bookingStore.selectedShowtime?.phongChieu?.loaiPhong
      || ''
})

// ── Retry mode price breakdowns ───────────────────────────────
const retryTotalSeatPrice = computed(() => {
  if (!retryBooking.value?.chiTietDatGhe?.length) return 0
  return retryBooking.value.chiTietDatGhe.reduce((sum, ct) => sum + Number(ct.giaTien || 0), 0)
})

const retryTotalComboPrice = computed(() => {
  if (!retryBooking.value?.chiTietDatSanPham?.length) return 0
  return retryBooking.value.chiTietDatSanPham.reduce(
    (sum, c) => sum + Number(c.giaLucMua || 0) * (c.soLuong || 0), 0
  )
})

// ── Booking expiry countdown ─────────────────────────────────
const EXPIRY_MINUTES   = 10
const countdownSecs    = ref(null)   // null = no active booking yet
const showExpiredModal = ref(false)
let   countdownTimer   = null

function startBookingCountdown(ngayTao) {
  const created  = new Date(ngayTao).getTime()
  const expiryMs = EXPIRY_MINUTES * 60 * 1000

  // Check immediately — stale tab: already expired on load
  const leftOnLoad = Math.floor((created + expiryMs - Date.now()) / 1000)
  if (leftOnLoad <= 0) {
    countdownSecs.value = 0
    showExpiredModal.value = true
    setTimeout(() => router.push('/movies'), 3000)
    return
  }

  function tick() {
    const left = Math.floor((created + expiryMs - Date.now()) / 1000)
    if (left <= 0) {
      countdownSecs.value = 0
      clearInterval(countdownTimer)
      showExpiredModal.value = true
      setTimeout(() => router.push('/movies'), 3000)
    } else {
      countdownSecs.value = left
    }
  }
  tick()
  countdownTimer = setInterval(tick, 1000)
}

// True when countdown has reached zero
const isExpired = computed(() => countdownSecs.value !== null && countdownSecs.value <= 0)

const countdownDisplay = computed(() => {
  if (countdownSecs.value == null) return null
  const m = Math.floor(countdownSecs.value / 60)
  const s = countdownSecs.value % 60
  return `${m}:${String(s).padStart(2, '0')}`
})

const countdownClass = computed(() => {
  if (countdownSecs.value == null) return ''
  if (countdownSecs.value <= 60)  return 'countdown--red'
  if (countdownSecs.value <= 180) return 'countdown--amber'
  return 'countdown--normal'
})

// ── promo ────────────────────────────────────────────────────
const promoInput    = ref(bookingStore.promoCode || '')
const checkingPromo = ref(false)
const promoError    = ref('')

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
const useLoyalty    = ref(bookingStore.useLoyaltyPoints)
const loyaltyPoints = ref(bookingStore.loyaltyPointsToUse)

const maxLoyaltyPoints = computed(() => {
  const userPts     = authStore.user?.diemTichLuy || 0
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
  { value: 'VNPay',   icon: '🏦', name: 'VNPay',   desc: 'Thanh toán qua cổng VNPay' },
  { value: 'PayOS',   icon: '💳', name: 'PayOS',    desc: 'Thanh toán qua PayOS' },
  { value: 'ZaloPay', icon: '💚', name: 'ZaloPay',  desc: 'Thanh toán qua ZaloPay' },
  { value: 'Cash',    icon: '💵', name: 'Tiền mặt', desc: 'Thanh toán tại quầy' },
]

// ── confirm ──────────────────────────────────────────────────
const confirming        = ref(false)
const confirmError      = ref('')
// Set to true when user is redirected to a payment gateway so we
// do NOT release the seat locks on component unmount.
const paymentInitiated  = ref(false)

async function confirm() {
  if (confirming.value) return
  if (isExpired.value) {
    showExpiredModal.value = true
    return
  }
  confirmError.value = ''
  confirming.value = true

  bookingStore.setPaymentMethod(payMethod.value)
  if (useLoyalty.value) bookingStore.setUseLoyaltyPoints(true, loyaltyPoints.value)

  try {
    let bookingId, ngayTao

    if (retryBookingId.value) {
      // ── Retry mode: validate the loaded booking, then use its ID ──
      if (!retryBooking.value) {
        confirmError.value = 'Không tải được thông tin đơn đặt vé. Vui lòng thử lại.'
        return
      }
      const amount = Number(retryBooking.value.tongTienThanhToan)
      if (!amount || amount <= 0) {
        confirmError.value = 'Không thể thanh toán: tổng tiền không hợp lệ'
        return
      }
      bookingId = retryBookingId.value
      ngayTao   = retryBooking.value?.ngayTao
    } else {
      // ── Normal mode: create a new booking ──
      const booking = await bookingStore.createBooking()
      if (!booking || !booking.id) {
        confirmError.value = bookingStore.error.booking || 'Không tạo được đơn đặt vé'
        return
      }
      bookingId = booking.id
      ngayTao   = booking.ngayTao

      // Zero-total: backend already confirmed/paid — skip gateway
      if (booking.trangThaiThanhToan === 'paid'
          || Number(booking.tongTienThanhToan) === 0) {
        router.push({ name: 'payment-result', params: { bookingId: String(bookingId) } })
        return
      }
    }

    // Start countdown from booking creation time
    if (ngayTao) startBookingCountdown(ngayTao)

    if (payMethod.value === 'Cash') {
      router.push({ name: 'payment-result', params: { bookingId: String(bookingId) } })
      return
    }

    let endpoint, urlField
    if (payMethod.value === 'VNPay') {
      endpoint = '/thanh-toan/vnpay'
      urlField  = 'paymentUrl'
    } else if (payMethod.value === 'ZaloPay') {
      endpoint = '/thanh-toan/zalopay/create'
      urlField  = 'orderUrl'
    } else {
      // PayOS
      endpoint = '/thanh-toan/payos/create'
      urlField  = 'checkoutUrl'
    }

    const payRes = await api.post(endpoint, { datVeId: bookingId })
    const url = payRes.data?.[urlField] || payRes.data?.paymentUrl || payRes.data?.orderUrl || payRes.data?.checkoutUrl
    if (url) {
      paymentInitiated.value = true   // do NOT release seats on unmount
      window.location.href = url
    } else {
      // URL field missing — log what we got and show error
      confirmError.value = `Không nhận được URL thanh toán. Phản hồi: ${JSON.stringify(payRes.data)}`
    }
  } catch (e) {
    confirmError.value = e.response?.data?.message
      || (typeof e.response?.data === 'string' ? e.response.data : null)
      || e.message
      || 'Lỗi khi xử lý thanh toán'
  } finally {
    confirming.value = false
  }
}

// ── helpers ──────────────────────────────────────────────────
function fmtPrice(v) {
  if (v == null || isNaN(Number(v))) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(Number(v))
}
function fmtDatetime(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('vi-VN',{ day:'2-digit', month:'2-digit', hour:'2-digit', minute:'2-digit' })
}
function typeClass(t) {
  if (!t) return ''
  const l = t.toLowerCase()
  if (l === 'vip') return 'pip-vip'
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

// ── Seat release on navigation away ─────────────────────────
/**
 * Release all SeatLock records for the currently selected seats when the user
 * leaves CheckoutPage without paying.
 * Guard: skip if payment has already been initiated (window.location.href fired).
 * This matches the same pattern used in SeatSelectionPage.vue.
 */
async function releaseSeatsOnLeave() {
  if (paymentInitiated.value) return           // payment in progress — keep locks
  if (retryBookingId.value) return              // retry mode — no new locks were created

  const seats = bookingStore.selectedSeats
  if (!seats?.length) return

  const lichChieuId = bookingStore.selectedShowtime?.id
  if (!lichChieuId) return                      // no showtime — nothing to release

  console.log('[Checkout] releasing seats on leave:', seats, lichChieuId)

  await Promise.allSettled(seats.map(seat =>
    api.delete('/dat-ve/release-seat', {
      data: { gheNgoiId: seat.id, lichChieuId }
    }).catch(() => { /* non-fatal */ })
  ))
}

function handleBeforeUnload() {
  if (paymentInitiated.value) return
  if (retryBookingId.value) return

  const seats = bookingStore.selectedSeats
  if (!seats?.length) return

  const lichChieuId = bookingStore.selectedShowtime?.id
  if (!lichChieuId) return                      // no showtime — nothing to release

  // sendBeacon is fire-and-forget — best effort on tab close / hard navigate
  for (const seat of seats) {
    navigator.sendBeacon(
      '/api/dat-ve/release-seat',
      JSON.stringify({ gheNgoiId: seat.id, lichChieuId })
    )
  }
}

onMounted(async () => {
  if (!authStore.isLoggedIn) { router.push('/auth'); return }

  // ── Retry-payment mode (?bookingId=X from MyTicketsPage) ─────
  const qBookingId = route.query?.bookingId
  if (qBookingId) {
    retryBookingId.value = Number(qBookingId)
    await loadRetryBooking(qBookingId)
  }

  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  // Async release — fire and forget (Vue route navigation)
  releaseSeatsOnLeave()
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  window.removeEventListener('beforeunload', handleBeforeUnload)
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
.price-row--original { color: var(--text-ghost, rgba(241,245,249,0.45)); font-size: 13px; }
.price-strikethrough { text-decoration: line-through; color: var(--text-ghost, rgba(241,245,249,0.45)); }
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

/* ── countdown banner ─────────────────────────────────────── */
.countdown-banner {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  padding: 10px 20px;
  font-size: 14px; font-weight: 700;
  transition: background 0.4s, color 0.4s;
}
.countdown--normal {
  background: rgba(201,168,76,0.12);
  border-bottom: 1px solid rgba(201,168,76,0.25);
  color: var(--gold, #C9A84C);
}
.countdown--amber {
  background: rgba(245,158,11,0.15);
  border-bottom: 1px solid rgba(245,158,11,0.35);
  color: #f59e0b;
}
.countdown--red {
  background: rgba(239,68,68,0.15);
  border-bottom: 1px solid rgba(239,68,68,0.35);
  color: #f87171;
  animation: pulse-red 1s ease-in-out infinite;
}
@keyframes pulse-red {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.7; }
}
.slide-down-enter-active, .slide-down-leave-active { transition: all 0.3s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-100%); }

/* ── expired modal ────────────────────────────────────────── */
.expired-overlay {
  position: fixed; inset: 0; z-index: 500;
  background: rgba(5,5,8,0.85);
  backdrop-filter: blur(8px);
  display: flex; align-items: center; justify-content: center;
}
.expired-modal {
  background: var(--surface-1, #0f0f17);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 12px);
  padding: 40px 48px;
  text-align: center;
  max-width: 380px;
}
.expired-icon { font-size: 48px; margin-bottom: 16px; }
.expired-title {
  font-size: 22px; font-weight: 700;
  color: var(--text-primary, #f1f5f9); margin: 0 0 12px;
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
}
.expired-desc { font-size: 14px; color: var(--text-secondary, #94a3b8); margin: 0; line-height: 1.6; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>

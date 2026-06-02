<template>
  <div class="result-page">
    <!-- ── Loading ── -->
    <div v-if="phase === 'loading'" class="centered">
      <div class="spinner"></div>
      <p class="status-text">Đang xác nhận thanh toán...</p>
      <p class="status-sub">Vui lòng không đóng trang này</p>
    </div>

    <!-- ── Success ── -->
    <div v-else-if="phase === 'success'" class="centered success-panel">
      <!-- Confetti dots -->
      <div class="confetti" aria-hidden="true">
        <span v-for="n in 12" :key="n" :class="`dot dot-${n}`"></span>
      </div>

      <div class="status-icon status-icon--success">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
      </div>
      <h1 class="status-title">Đặt vé thành công!</h1>
      <p class="status-sub">Mã đặt vé của bạn đã được xác nhận</p>

      <!-- Booking card -->
      <div class="booking-card" v-if="booking">
        <div class="booking-card__code">{{ booking.maDatVe }}</div>

        <div class="booking-card__rows">
          <div class="brow">
            <span class="brow-label">Phim</span>
            <span class="brow-val">{{ booking.lichChieu?.phim?.tenPhim || '-' }}</span>
          </div>
          <div class="brow">
            <span class="brow-label">Suất chiếu</span>
            <span class="brow-val">{{ fmtDatetime(booking.lichChieu?.thoiGianBatDau) }}</span>
          </div>
          <div class="brow">
            <span class="brow-label">Phòng</span>
            <span class="brow-val">{{ booking.lichChieu?.phongChieu?.tenPhong || '-' }}</span>
          </div>
          <div class="brow">
            <span class="brow-label">Ghế</span>
            <span class="brow-val">{{ seatList }}</span>
          </div>
          <div class="brow">
            <span class="brow-label">Tổng thanh toán</span>
            <span class="brow-val gold">{{ fmtPrice(booking.tongTienThanhToan) }}</span>
          </div>
          <div class="brow">
            <span class="brow-label">Trạng thái</span>
            <span class="brow-val">
              <span class="status-badge status-badge--ok">{{ payStatusLabel(booking.trangThaiThanhToan) }}</span>
            </span>
          </div>
        </div>

        <!-- QR -->
        <div class="qr-section">
          <p class="qr-label">Mã QR vé</p>
          <div class="qr-box" ref="qrBoxRef">
            <img
              v-if="booking.maQR && booking.maQR.startsWith('http')"
              :src="booking.maQR"
              :alt="booking.maDatVe"
              class="qr-img"
            />
            <div v-else class="qr-placeholder">
              <!-- Inline SVG QR-style grid generated from booking code -->
              <svg viewBox="0 0 9 9" fill="#000000" xmlns="http://www.w3.org/2000/svg" class="qr-svg">
                <rect x="0" y="0" width="4" height="4" fill="#000000"/>
                <rect x="1" y="1" width="2" height="2" fill="white"/>
                <rect x="5" y="0" width="4" height="4" fill="#000000"/>
                <rect x="6" y="1" width="2" height="2" fill="white"/>
                <rect x="0" y="5" width="4" height="4" fill="#000000"/>
                <rect x="1" y="6" width="2" height="2" fill="white"/>
                <rect x="4" y="4" width="1" height="1" fill="#000000"/>
                <rect x="5" y="5" width="1" height="1" fill="#000000"/>
                <rect x="7" y="5" width="1" height="1" fill="#000000"/>
                <rect x="5" y="7" width="3" height="1" fill="#000000"/>
              </svg>
              <p class="qr-code-text">{{ booking.maDatVe }}</p>
            </div>
          </div>
          <p class="qr-hint">Xuất trình mã QR tại quầy để nhận vé</p>
        </div>
      </div>

      <!-- Actions -->
      <div class="actions">
        <button class="btn btn--primary" @click="goHome">🏠 Về trang chủ</button>
        <button class="btn btn--outline" @click="downloadTicket">⬇ Tải vé</button>
        <button class="btn btn--ghost" @click="viewTickets">📋 Vé của tôi</button>
      </div>
    </div>

    <!-- ── Failure ── -->
    <div v-else-if="phase === 'fail'" class="centered fail-panel">
      <div class="status-icon status-icon--fail">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </div>
      <h1 class="status-title">Thanh toán thất bại</h1>
      <p class="status-sub">{{ failMessage }}</p>
      <div v-if="failCode" class="fail-code">Mã lỗi: <code>{{ failCode }}</code></div>

      <div class="actions">
        <button class="btn btn--primary" @click="retry">↩ Thử lại</button>
        <button class="btn btn--ghost" @click="goHome">🏠 Về trang chủ</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useBookingStore } from '@/stores/bookingStore'
import api from '@/services/api'

const router       = useRouter()
const route        = useRoute()
const authStore    = useAuthStore()
const bookingStore = useBookingStore()

// ── State ────────────────────────────────────────────────────
const phase       = ref('loading')   // 'loading' | 'success' | 'fail'
const booking     = ref(null)
const failMessage = ref('')
const failCode    = ref('')

// ── Computed ─────────────────────────────────────────────────
const seatList = computed(() => {
  if (!booking.value?.chiTietDatGhe?.length) return '-'
  return booking.value.chiTietDatGhe
    .map(c => `${(c.gheNgoi?.hangGhe || '').trim()}${c.gheNgoi?.soGhe}`)
    .sort()
    .join(', ')
})

// ── On mount ─────────────────────────────────────────────────
onMounted(async () => {
  if (!authStore.isLoggedIn) { router.push('/auth'); return }
  await processResult()
})

async function processResult() {
  const q = route.query
  const bookingId = route.params.bookingId || q.bookingId

  // ── Cash / direct path ─────────────────────────────────────
  if (bookingId && !q.vnp_ResponseCode && !q.resultCode) {
    await fetchBooking(bookingId)
    return
  }

  // ── VNPay callback ─────────────────────────────────────────
  if (q.vnp_ResponseCode !== undefined) {
    if (q.vnp_ResponseCode === '00') {
      const id = q.vnp_TxnRef || bookingId
      await fetchBooking(id)
    } else {
      failCode.value    = q.vnp_ResponseCode
      failMessage.value = vnpayMessage(q.vnp_ResponseCode)
      phase.value = 'fail'
    }
    return
  }

  // ── MoMo callback ──────────────────────────────────────────
  if (q.resultCode !== undefined) {
    if (q.resultCode === '0') {
      const id = q.orderId?.split('_')[0] || bookingId
      await fetchBooking(id)
    } else {
      failCode.value    = q.resultCode
      failMessage.value = momoMessage(q.resultCode)
      phase.value = 'fail'
    }
    return
  }

  // ── Fallback ───────────────────────────────────────────────
  failMessage.value = 'Không tìm thấy thông tin thanh toán'
  phase.value = 'fail'
}

async function fetchBooking(id) {
  if (!id) {
    failMessage.value = 'Không xác định được đơn đặt vé'
    phase.value = 'fail'
    return
  }
  try {
    const res = await api.get(`/dat-ve/${id}`)
    booking.value = res.data
    phase.value   = 'success'
    // Clear booking store after success
    bookingStore.clearBooking()
  } catch (e) {
    failMessage.value = e.response?.data?.message || 'Không lấy được thông tin đơn đặt vé'
    failCode.value    = e.response?.status ? String(e.response.status) : ''
    phase.value = 'fail'
  }
}

// ── Actions ──────────────────────────────────────────────────
function goHome()    { router.push('/') }
function viewTickets() { router.push('/my-tickets') }
function retry()     { router.push({ name: 'checkout' }) }
function downloadTicket() {
  if (!booking.value) return
  // Build a simple text representation and trigger download
  const lines = [
    `PolyCinema — Vé xem phim`,
    `Mã đặt vé : ${booking.value.maDatVe}`,
    `Phim      : ${booking.value.lichChieu?.phim?.tenPhim || '-'}`,
    `Suất chiếu: ${fmtDatetime(booking.value.lichChieu?.thoiGianBatDau)}`,
    `Phòng     : ${booking.value.lichChieu?.phongChieu?.tenPhong || '-'}`,
    `Ghế       : ${seatList.value}`,
    `Thanh toán: ${fmtPrice(booking.value.tongTienThanhToan)}`,
  ]
  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const url  = URL.createObjectURL(blob)
  const a    = document.createElement('a')
  a.href = url; a.download = `ve-${booking.value.maDatVe}.txt`
  a.click(); URL.revokeObjectURL(url)
}

// ── Helpers ───────────────────────────────────────────────────
function fmtPrice(v) {
  if (v == null) return '-'
  return new Intl.NumberFormat('vi-VN',{ style:'currency', currency:'VND' }).format(v)
}
function fmtDatetime(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('vi-VN',{ day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' })
}
function payStatusLabel(s) {
  if (s === 'paid') return 'Đã thanh toán'
  if (s === 'unpaid') return 'Chờ thanh toán'
  return s || '-'
}
function vnpayMessage(code) {
  const map = {
    '07':'Giao dịch bị nghi ngờ gian lận', '09':'Thẻ/tài khoản chưa đăng ký Internet Banking',
    '10':'Xác thực thông tin thẻ quá 3 lần', '11':'Đã hết hạn chờ thanh toán',
    '12':'Thẻ/tài khoản bị khóa', '13':'Sai mật khẩu OTP',
    '24':'Giao dịch bị hủy', '51':'Tài khoản không đủ số dư',
    '65':'Vượt quá hạn mức giao dịch trong ngày', '75':'Ngân hàng thanh toán đang bảo trì',
    '79':'Sai mật khẩu thanh toán quá số lần', '99':'Lỗi không xác định',
  }
  return map[code] || `Giao dịch thất bại (mã ${code})`
}
function momoMessage(code) {
  const map = {
    '1':'Giao dịch thất bại', '2':'Tài khoản bị khóa', '3':'Không đủ số dư',
    '4':'Quá hạn thanh toán', '5':'Thông tin giao dịch không hợp lệ',
    '6':'Lỗi hệ thống MoMo', '7':'Giao dịch bị từ chối',
    '8':'Lỗi kết nối',
  }
  return map[code] || `Thanh toán MoMo thất bại (mã ${code})`
}
</script>

<style scoped>
.result-page { background: #ffffff; color: #7f7e7f; min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px; }
.centered { width: 100%; max-width: 640px; display: flex; flex-direction: column; align-items: center; gap: 20px; position: relative; }
.spinner { width: 56px; height: 56px; border: 5px solid #dff4fb; border-top-color: #29bcea; border-radius: 50%; animation: spin .9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.status-text { font-size: 18px; font-weight: 700; margin: 0; color: #29bcea; }
.status-sub { font-size: 14.4px; color: #7f7e7f; margin: 0; text-align: center; }
.confetti { display: none; }
.status-icon { width: 72px; height: 72px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; border: 1px solid #efefef; background: #f7f7f7; }
.status-icon svg { width: 32px; height: 32px; }
.status-icon--success { color: #177245; }
.status-icon--fail { color: #8f2a2a; }
.status-title { font-size: clamp(22px,5vw,32px); font-weight: 700; margin: 0; text-align: center; color: #29bcea; }
.gold { color: #29bcea; }
.booking-card { width: 100%; background: #f7f7f7; border: 1px solid #efefef; overflow: hidden; }
.booking-card__code { text-align: center; padding: 14px; background: #ffffff; font-size: 22px; font-weight: 700; color: #29bcea; letter-spacing: 1px; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; border-bottom: 1px solid #efefef; }
.booking-card__rows { padding: 16px; display: flex; flex-direction: column; gap: 0; }
.brow { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; font-size: 14.4px; border-bottom: 1px solid #efefef; }
.brow:last-child { border-bottom: none; }
.brow-label { color: #7f7e7f; font-weight: 700; }
.brow-val { color: #000000; font-weight: 400; text-align: right; max-width: 60%; }
.brow-val.gold { color: #29bcea; font-size: 15px; font-weight: 700; }
.status-badge { padding: 4px 10px; font-size: 12px; font-weight: 700; border: 1px solid #efefef; background: #fff; }
.status-badge--ok { color: #177245; }
.qr-section { padding: 20px; text-align: center; border-top: 1px solid #efefef; }
.qr-label { font-size: 12px; font-weight: 700; color: #7f7e7f; text-transform: uppercase; letter-spacing: .5px; margin: 0 0 12px; }
.qr-box { background: white; border: 1px solid #efefef; padding: 16px; display: flex; align-items: center; justify-content: center; min-height: 180px; margin-bottom: 12px; }
.qr-img { max-width: 160px; max-height: 160px; }
.qr-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.qr-svg { width: 140px; height: 140px; }
.qr-code-text { font-size: 11px; font-weight: 700; color: #000; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; letter-spacing: 1px; margin: 0; }
.qr-hint { font-size: 12px; color: #767676; margin: 0; }
.fail-code { padding: 10px 18px; background: #fff5f5; border: 1px solid #f0d4d4; font-size: 13px; color: #8f2a2a; }
.fail-code code { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-weight: 700; }
.actions { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; width: 100%; }
.btn { min-height: 44px; padding: 12px 22px; font-size: 14.4px; font-weight: 700; cursor: pointer; border: 1px solid #29bcea; background: transparent; color: #29bcea; white-space: nowrap; }
.btn--primary { background: #29bcea; color: #ffffff; border-color: #29bcea; }
.btn--outline:hover, .btn--ghost:hover { background: #29bcea; color: #ffffff; }
.btn--ghost { border-color: #efefef; color: #7f7e7f; }
@media (max-width: 640px) { .result-page { padding: 16px; align-items: flex-start; } .actions { flex-direction: column; } .btn { width: 100%; text-align: center; } }
</style>

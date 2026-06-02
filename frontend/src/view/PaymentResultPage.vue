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
            <span class="brow-val">{{ booking.lichChieu?.phim?.tenPhim || '—' }}</span>
          </div>
          <div class="brow">
            <span class="brow-label">Suất chiếu</span>
            <span class="brow-val">{{ fmtDatetime(booking.lichChieu?.thoiGianBatDau) }}</span>
          </div>
          <div class="brow">
            <span class="brow-label">Phòng</span>
            <span class="brow-val">{{ booking.lichChieu?.phongChieu?.tenPhong || '—' }}</span>
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
              <svg viewBox="0 0 9 9" fill="#0f172a" xmlns="http://www.w3.org/2000/svg" class="qr-svg">
                <rect x="0" y="0" width="4" height="4" fill="#0f172a"/>
                <rect x="1" y="1" width="2" height="2" fill="white"/>
                <rect x="5" y="0" width="4" height="4" fill="#0f172a"/>
                <rect x="6" y="1" width="2" height="2" fill="white"/>
                <rect x="0" y="5" width="4" height="4" fill="#0f172a"/>
                <rect x="1" y="6" width="2" height="2" fill="white"/>
                <rect x="4" y="4" width="1" height="1" fill="#0f172a"/>
                <rect x="5" y="5" width="1" height="1" fill="#0f172a"/>
                <rect x="7" y="5" width="1" height="1" fill="#0f172a"/>
                <rect x="5" y="7" width="3" height="1" fill="#0f172a"/>
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
  if (!booking.value?.chiTietDatGhe?.length) return '—'
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
    `Phim      : ${booking.value.lichChieu?.phim?.tenPhim || '—'}`,
    `Suất chiếu: ${fmtDatetime(booking.value.lichChieu?.thoiGianBatDau)}`,
    `Phòng     : ${booking.value.lichChieu?.phongChieu?.tenPhong || '—'}`,
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
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN',{ style:'currency', currency:'VND' }).format(v)
}
function fmtDatetime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN',{ day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' })
}
function payStatusLabel(s) {
  if (s === 'paid') return 'Đã thanh toán'
  if (s === 'unpaid') return 'Chờ thanh toán'
  return s || '—'
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
/* ── base ──────────────────────────────────────────────────── */
.result-page {
  background: linear-gradient(160deg,#0b1120 0%,#0f172a 50%,#1a1f35 100%);
  color: #f1f5f9; min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
}
.centered {
  width: 100%; max-width: 580px;
  display: flex; flex-direction: column; align-items: center; gap: 20px;
  position: relative;
}

/* ── spinner ──────────────────────────────────────────────── */
.spinner {
  width: 56px; height: 56px;
  border: 5px solid rgba(255,215,0,.15);
  border-top-color: #ffd700; border-radius: 50%;
  animation: spin .9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.status-text { font-size: 18px; font-weight: 700; margin: 0; }
.status-sub  { font-size: 13px; color: #64748b; margin: 0; text-align: center; }

/* ── confetti ─────────────────────────────────────────────── */
.confetti { position: absolute; inset: -20px; pointer-events: none; overflow: hidden; }
.dot { position: absolute; width: 8px; height: 8px; border-radius: 50%; animation: pop 1s ease-out forwards; }
.dot-1  { background:#ffd700; top:5%;  left:20%; animation-delay:.0s; }
.dot-2  { background:#4ade80; top:10%; left:60%; animation-delay:.1s; }
.dot-3  { background:#f472b6; top:15%; left:80%; animation-delay:.2s; }
.dot-4  { background:#60a5fa; top:25%; left:10%; animation-delay:.15s; }
.dot-5  { background:#ffd700; top:5%;  left:45%; animation-delay:.05s; }
.dot-6  { background:#a78bfa; top:20%; left:90%; animation-delay:.25s; }
.dot-7  { background:#fb923c; top:30%; left:35%; animation-delay:.08s; }
.dot-8  { background:#4ade80; top:8%;  left:70%; animation-delay:.18s; }
.dot-9  { background:#ffd700; top:35%; left:55%; animation-delay:.3s; }
.dot-10 { background:#f472b6; top:12%; left:30%; animation-delay:.12s; }
.dot-11 { background:#60a5fa; top:22%; left:50%; animation-delay:.22s; }
.dot-12 { background:#a78bfa; top:18%; left:5%;  animation-delay:.04s; }
@keyframes pop { 0%{transform:scale(0) translateY(0);opacity:1} 100%{transform:scale(1) translateY(-40px);opacity:0} }

/* ── status icons ─────────────────────────────────────────── */
.status-icon {
  width: 72px; height: 72px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.status-icon svg { width: 32px; height: 32px; }
.status-icon--success { background: rgba(74,222,128,.12); border: 2px solid rgba(74,222,128,.4); color: #4ade80; }
.status-icon--fail    { background: rgba(248,113,113,.12); border: 2px solid rgba(248,113,113,.4); color: #f87171; }

.status-title { font-size: clamp(22px,5vw,32px); font-weight: 900; margin: 0; text-align: center; }
.gold { color: #ffd700; }

/* ── booking card ─────────────────────────────────────────── */
.booking-card {
  width: 100%;
  background: rgba(30,41,55,.6);
  border: 1px solid rgba(255,215,0,.2);
  border-radius: 16px; overflow: hidden;
}
.booking-card__code {
  text-align: center; padding: 14px;
  background: rgba(255,215,0,.1);
  font-size: 22px; font-weight: 900; color: #ffd700;
  letter-spacing: 2px; font-family: monospace;
  border-bottom: 1px dashed rgba(255,215,0,.25);
}
.booking-card__rows { padding: 16px; display: flex; flex-direction: column; gap: 0; }
.brow {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; font-size: 13px;
  border-bottom: 1px solid rgba(255,215,0,.05);
}
.brow:last-child { border-bottom: none; }
.brow-label { color: #94a3b8; font-weight: 600; }
.brow-val   { color: #f1f5f9; font-weight: 700; text-align: right; max-width: 60%; }
.brow-val.gold { color: #ffd700; font-size: 15px; }

.status-badge { padding: 3px 10px; border-radius: 999px; font-size: 11px; font-weight: 800; }
.status-badge--ok { background: rgba(74,222,128,.15); color: #4ade80; }

/* ── QR ───────────────────────────────────────────────────── */
.qr-section {
  padding: 20px; text-align: center;
  border-top: 1px dashed rgba(255,215,0,.2);
}
.qr-label { font-size: 12px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: .5px; margin: 0 0 12px; }
.qr-box {
  background: white; border-radius: 12px; padding: 16px;
  display: flex; align-items: center; justify-content: center;
  min-height: 180px; margin-bottom: 12px;
}
.qr-img { max-width: 160px; max-height: 160px; }
.qr-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.qr-svg { width: 140px; height: 140px; }
.qr-code-text { font-size: 11px; font-weight: 800; color: #0f172a; font-family: monospace; letter-spacing: 1px; margin: 0; }
.qr-hint { font-size: 11px; color: #64748b; margin: 0; }

/* ── fail ─────────────────────────────────────────────────── */
.fail-code {
  padding: 10px 18px;
  background: rgba(248,113,113,.1); border: 1px solid rgba(248,113,113,.25);
  border-radius: 8px; font-size: 13px; color: #fca5a5;
}
.fail-code code { font-family: monospace; font-weight: 800; }

/* ── actions ──────────────────────────────────────────────── */
.actions { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; width: 100%; }
.btn {
  padding: 12px 22px; border-radius: 10px;
  font-size: 14px; font-weight: 800; cursor: pointer; transition: all .2s;
  border: none; white-space: nowrap;
}
.btn--primary {
  background: linear-gradient(135deg,#ffd700,#ffed4e);
  color: #0f172a; box-shadow: 0 4px 16px rgba(255,215,0,.3);
}
.btn--primary:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(255,215,0,.4); }
.btn--outline {
  background: transparent; color: #ffd700;
  border: 2px solid rgba(255,215,0,.4);
}
.btn--outline:hover { background: rgba(255,215,0,.08); }
.btn--ghost {
  background: rgba(255,255,255,.06); color: #94a3b8;
  border: 1px solid rgba(255,255,255,.1);
}
.btn--ghost:hover { background: rgba(255,255,255,.1); color: #f1f5f9; }

/* ── responsive ───────────────────────────────────────────── */
@media (max-width: 640px) {
  .result-page { padding: 16px; align-items: flex-start; }
  .actions { flex-direction: column; }
  .btn { width: 100%; text-align: center; }
}
</style>

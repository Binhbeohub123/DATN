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
            <span class="brow-val brow-val--gold">{{ fmtPrice(booking.tongTienThanhToan) }}</span>
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
          <div class="qr-box">
            <!-- Real QR PNG from /ve/{maVe}/qr -->
            <img
              v-if="qrObjectUrl"
              :src="qrObjectUrl"
              :alt="'QR vé ' + booking.maDatVe"
              class="qr-img"
            />
            <!-- Loading state -->
            <div v-else-if="qrLoading" class="qr-placeholder">
              <div class="qr-spinner"></div>
            </div>
            <!-- Error / placeholder fallback -->
            <div v-else class="qr-placeholder">
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
        <button class="btn btn--outline" v-if="qrObjectUrl" @click="downloadQr">🖼️ Tải mã QR</button>
        <button class="btn btn--outline" @click="downloadTicket">⬇ Tải vé</button>
        <button class="btn btn--ghost" @click="viewTickets">📋 Vé của tôi</button>
      </div>
    </div>

    <!-- ── Failure ── -->
    <div v-else-if="phase === 'fail'" class="centered fail-panel">
      <div class="status-icon status-icon--fail">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </div>
      <h1 class="status-title status-title--fail">Thanh toán thất bại</h1>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useBookingStore } from '@/stores/bookingStore'
import api from '@/services/api'
import { fmtDateTime12 } from '@/utils/homeHelpers'

const router       = useRouter()
const route        = useRoute()
const authStore    = useAuthStore()
const bookingStore = useBookingStore()

// ── State ────────────────────────────────────────────────────
const phase       = ref('loading')   // 'loading' | 'success' | 'fail'
const booking     = ref(null)
const failMessage = ref('')
const failCode    = ref('')

// ── QR image ─────────────────────────────────────────────────
const qrObjectUrl  = ref(null)   // blob URL for the PNG returned by /ve/{maVe}/qr
const qrLoading    = ref(false)
const qrError      = ref(false)

async function loadQr(maVe) {
  if (!maVe) return
  qrLoading.value = true
  qrError.value   = false
  try {
    const res = await api.get(`/ve/${maVe}/qr`, { responseType: 'blob', headers: { Accept: 'image/png' } })
    if (qrObjectUrl.value) URL.revokeObjectURL(qrObjectUrl.value)
    qrObjectUrl.value = URL.createObjectURL(res.data)
  } catch {
    qrError.value = true
  } finally {
    qrLoading.value = false
  }
}

function downloadQr() {
  if (!qrObjectUrl.value || !booking.value) return
  const a = document.createElement('a')
  a.href = qrObjectUrl.value
  a.download = `ve-${booking.value.maDatVe}-qr.png`
  a.click()
}

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

  // ── PayOS callback ──────────────────────────────────────────
  // PayOS appends orderCode (+code/status/id) to the returnUrl. Re-query PayOS
  // for the authoritative status and reconcile the booking server-side.
  if (q.orderCode !== undefined) {
    try {
      const res = await api.post('/thanh-toan/payos/confirm', { orderCode: q.orderCode, datVeId: bookingId })
      await fetchBooking(res.data?.datVeId)
    } catch (e) {
      failMessage.value = e.response?.data?.message || 'Không xác nhận được thanh toán PayOS'
      failCode.value    = e.response?.status ? String(e.response.status) : ''
      phase.value = 'fail'
    }
    return
  }

  // ── ZaloPay callback ─────────────────────────────────────────
  // ZaloPay redirects to /payment-result/{id}?apptransid=...&status=...&checksum=...
  // Must be checked BEFORE the cash/direct path — ZaloPay redirects also carry
  // the bookingId path param.
  if (q.apptransid !== undefined) {
    try {
      const res = await api.post('/thanh-toan/zalopay/redirect', {
        datVeId:        bookingId,
        appid:          q.appid,
        apptransid:     q.apptransid,
        pmcid:          q.pmcid,
        bankcode:       q.bankcode,
        amount:         q.amount,
        discountamount: q.discountamount,
        status:         q.status,
        checksum:       q.checksum,
      })
      if (String(q.status) === '1' || res.data?.trangThaiThanhToan === 'paid') {
        await fetchBooking(res.data?.datVeId || bookingId)
      } else if (String(q.status) === '-49') {
        // User cancelled on the ZaloPay page — booking was released server-side
        router.replace('/payment-cancel')
      } else {
        failCode.value    = q.status
        failMessage.value = zaloStatusMessage(q.status)
        phase.value = 'fail'
      }
    } catch (e) {
      failMessage.value = e.response?.data?.message || 'Không xác nhận được thanh toán ZaloPay'
      failCode.value    = e.response?.status ? String(e.response.status) : ''
      phase.value = 'fail'
    }
    return
  }

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
    // Load QR image
    if (res.data?.maDatVe) loadQr(res.data.maDatVe)
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
  return fmtDateTime12(dt, '-')
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
onUnmounted(() => {
  if (qrObjectUrl.value) URL.revokeObjectURL(qrObjectUrl.value)
})

function momoMessage(code) {
  const map = {
    '1':'Giao dịch thất bại', '2':'Tài khoản bị khóa', '3':'Không đủ số dư',
    '4':'Quá hạn thanh toán', '5':'Thông tin giao dịch không hợp lệ',
    '6':'Lỗi hệ thống MoMo', '7':'Giao dịch bị từ chối',
    '8':'Lỗi kết nối',
  }
  return map[code] || `Thanh toán MoMo thất bại (mã ${code})`
}
function zaloStatusMessage(code) {
  const map = {
    '1':'Giao dịch thành công', '0':'Giao dịch đang xử lý',
    '-49':'Giao dịch bị hủy bởi người dùng',
  }
  return map[code] || `Thanh toán ZaloPay thất bại (mã ${code})`
}
</script>

<style scoped>
/* ── base ──────────────────────────────────────────────────── */
.result-page {
  background: var(--void, #050508);
  color: var(--text-secondary, #94a3b8);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  font-family: var(--font-ui, 'Inter', sans-serif);
}

.centered {
  width: 100%;
  max-width: 640px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  position: relative;
}

/* ── loading ───────────────────────────────────────────────── */
.spinner {
  width: 56px; height: 56px;
  border: 4px solid var(--glass-border, rgba(255,255,255,0.08));
  border-top-color: var(--gold, #C9A84C);
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.status-text {
  font-size: 18px; font-weight: 700; margin: 0;
  color: var(--text-primary, #f1f5f9);
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
}
.status-sub { font-size: 14px; color: var(--text-secondary, #94a3b8); margin: 0; text-align: center; }

/* ── status icon ───────────────────────────────────────────── */
.status-icon {
  width: 80px; height: 80px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  border-radius: var(--radius-md, 12px);
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  backdrop-filter: blur(16px);
}
.status-icon svg { width: 36px; height: 36px; }
.status-icon--success {
  border-color: rgba(16,185,129,0.35);
  background: rgba(16,185,129,0.08);
  color: #10B981;
  box-shadow: 0 0 24px rgba(16,185,129,0.15);
}
.status-icon--fail {
  border-color: rgba(239,68,68,0.35);
  background: rgba(239,68,68,0.08);
  color: #EF4444;
  box-shadow: 0 0 24px rgba(239,68,68,0.15);
}

.status-title {
  font-size: clamp(22px,5vw,32px);
  font-weight: 700; margin: 0; text-align: center;
  color: var(--text-primary, #f1f5f9);
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
}
.status-title--fail { color: #EF4444; }

/* ── booking card ──────────────────────────────────────────── */
.booking-card {
  width: 100%;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--gold, #C9A84C);
  border-radius: var(--radius-md, 12px);
  backdrop-filter: blur(16px);
  overflow: hidden;
}
.booking-card__code {
  text-align: center; padding: 16px;
  background: rgba(201,168,76,0.06);
  font-size: 22px; font-weight: 700;
  color: var(--gold, #C9A84C);
  letter-spacing: 2px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  border-bottom: 1px solid rgba(201,168,76,0.2);
}
.booking-card__rows { padding: 16px; display: flex; flex-direction: column; gap: 0; }
.brow {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 0; font-size: 14px;
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.brow:last-child { border-bottom: none; }
.brow-label { color: var(--text-secondary, #94a3b8); font-weight: 600; }
.brow-val { color: var(--text-primary, #f1f5f9); font-weight: 400; text-align: right; max-width: 60%; }
.brow-val--gold { color: var(--gold, #C9A84C); font-size: 15px; font-weight: 700; }

/* ── status badges ─────────────────────────────────────────── */
.status-badge {
  padding: 4px 10px;
  border-radius: var(--radius-pill, 999px);
  font-size: 12px; font-weight: 700;
}
.status-badge--ok {
  background: rgba(16,185,129,0.15);
  color: #10B981;
  border: 1px solid rgba(16,185,129,0.25);
}

/* ── QR section ────────────────────────────────────────────── */
.qr-section { padding: 20px; text-align: center; border-top: 1px solid var(--glass-border, rgba(255,255,255,0.08)); }
.qr-label {
  font-size: 12px; font-weight: 700;
  color: var(--text-secondary, #94a3b8);
  text-transform: uppercase; letter-spacing: 0.5px; margin: 0 0 12px;
}
.qr-box {
  background: rgba(255,255,255,0.95);
  border-radius: var(--radius-md, 12px);
  padding: 16px;
  display: flex; align-items: center; justify-content: center;
  min-height: 180px; margin-bottom: 12px;
  max-width: 220px; margin-left: auto; margin-right: auto;
}
.qr-img { max-width: 160px; max-height: 160px; }
.qr-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.qr-svg { width: 140px; height: 140px; }
.qr-code-text {
  font-size: 11px; font-weight: 700; color: #000000;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  letter-spacing: 1px; margin: 0;
}
.qr-hint { font-size: 12px; color: var(--text-secondary, #94a3b8); margin: 0; }

.qr-spinner {
  width: 40px; height: 40px;
  border: 3px solid rgba(255,255,255,0.1);
  border-top-color: var(--gold, #C9A84C);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* ── fail code ─────────────────────────────────────────────── */
.fail-code {
  padding: 10px 18px;
  background: rgba(239,68,68,0.08);
  border: 1px solid rgba(239,68,68,0.25);
  border-radius: var(--radius-sm, 6px);
  font-size: 13px; color: #fca5a5;
}
.fail-code code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-weight: 700; color: #EF4444;
}

/* ── actions ───────────────────────────────────────────────── */
.actions { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; width: 100%; }
.btn {
  min-height: 44px; padding: 12px 22px;
  font-size: 14px; font-weight: 700; cursor: pointer;
  border-radius: var(--radius-sm, 6px);
  white-space: nowrap;
  transition: all 0.2s;
}
.btn--primary {
  background: var(--gold, #C9A84C);
  color: #0D0D0D;
  border: none;
  outline: 1.5px solid rgba(201,168,76,0.45); outline-offset: 3px;
}
.btn--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--gold-glow, rgba(201,168,76,0.35));
  outline-offset: 5px;
}
.btn--outline {
  background: transparent;
  color: var(--text-primary, #f1f5f9);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.btn--outline:hover {
  border-color: var(--gold, #C9A84C);
  color: var(--gold, #C9A84C);
}
.btn--ghost {
  background: transparent;
  color: var(--text-secondary, #94a3b8);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.btn--ghost:hover {
  border-color: var(--gold, #C9A84C);
  color: var(--gold, #C9A84C);
}

@media (max-width: 640px) {
  .result-page { padding: 16px; align-items: flex-start; }
  .actions { flex-direction: column; }
  .btn { width: 100%; text-align: center; }
}
</style>

<template>
  <div class="seat-page">
    <!-- ── Expired-seat toast ── -->
    <transition name="toast">
      <div v-if="expiredToast" class="expired-toast">{{ expiredToast }}</div>
    </transition>

    <!-- ── Header ── -->
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Chọn ghế</span>
      <ThemeToggle />
    </header>

    <!-- ── Showtime strip ── -->
    <div class="showtime-strip" v-if="bookingStore.selectedShowtime">
      <div class="strip-item">
        <span class="strip-label">Phim</span>
        <span class="strip-value">{{ bookingStore.selectedMovie?.title || '—' }}</span>
      </div>
      <div class="strip-sep"></div>
      <div class="strip-item">
        <span class="strip-label">Suất chiếu</span>
        <span class="strip-value">{{ fmtTime(bookingStore.selectedShowtime?.thoiGianBatDau) }}</span>
      </div>
      <div class="strip-sep"></div>
      <div class="strip-item">
        <span class="strip-label">Phòng</span>
        <span class="strip-value">{{ bookingStore.selectedShowtime?.tenPhong || bookingStore.selectedShowtime?.phongChieu?.tenPhong || '—' }} · {{ bookingStore.selectedShowtime?.loaiPhong || bookingStore.selectedShowtime?.phongChieu?.loaiPhong || '' }}</span>
      </div>
    </div>

    <!-- ── Legend ── -->
    <div class="legend">
      <div class="legend-item"><div class="seat-sample seat-sample--booked"></div><span>Đã đặt</span></div>
      <div class="legend-item"><div class="seat-sample seat-sample--locked"></div><span>Đang giữ</span></div>
      <div class="legend-item"><div class="seat-sample seat-sample--selected"></div><span>Đang chọn</span></div>
      <div class="legend-item"><div class="seat-sample seat-sample--vip"></div><span>VIP</span></div>
      <div class="legend-item"><div class="seat-sample seat-sample--couple"></div><span>Cặp đôi</span></div>
    </div>

    <!-- ── Loading ── -->
    <div v-if="loading" class="state-box">
      <div class="spinner"></div>
      <p>Đang tải sơ đồ ghế...</p>
    </div>

    <!-- ── Error ── -->
    <div v-else-if="loadError" class="state-box state-box--error">
      <svg class="state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <p>{{ loadError }}</p>
      <button class="btn-retry" @click="loadSeats">Thử lại</button>
    </div>

    <!-- ── Seat grid ── -->
    <div v-else class="screen-wrap">
      <div class="screen-label">MÀN HÌNH</div>
      <div class="screen-bar"></div>

      <div class="grid-scroll">
        <div class="rows-wrap">
          <div v-for="row in rows" :key="row.label" class="seat-row">
            <span class="row-label">{{ row.label }}</span>
            <div class="row-seats">
              <button
                v-for="seat in row.seats"
                :key="seat.id"
                :class="['seat', seatClass(seat)]"
                :disabled="isBooked(seat) || isLockedByOther(seat) || (isMaxReached && !isSelected(seat))"
                :title="seatTitle(seat)"
                @click="toggle(seat)"
                :aria-label="seatTitle(seat)"
              >
                <template v-if="isSelected(seat) && seatCountdowns[seat.id]">
                  <span class="seat-countdown">{{ fmtCountdown(seat.id) }}</span>
                </template>
                <template v-else>{{ seat.soGhe }}</template>
              </button>
            </div>
            <span class="row-label">{{ row.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Bottom bar ── -->
    <div class="bottom-bar">
      <div class="bottom-bar__info">
        <div class="info-row">
          <span class="info-label">Ghế đã chọn:</span>
          <span class="info-chips">
            <span v-if="bookingStore.selectedSeats.length === 0" class="chip-none">Chưa chọn</span>
            <span v-for="s in bookingStore.selectedSeats" :key="s.id" class="chip-seat">
              {{ s.hangGhe?.trim() }}{{ s.soGhe }}
            </span>
          </span>
        </div>
        <div class="info-row">
          <span class="info-label">Tổng tiền:</span>
          <span class="info-price">{{ fmtPrice(bookingStore.totalSeatPrice) }}</span>
        </div>
        <p v-if="isMaxReached" class="max-warn">Tối đa 8 ghế mỗi lần đặt</p>
      </div>
      <div class="bottom-bar__actions">
        <button class="btn-clear" @click="bookingStore.clearSeats()" :disabled="bookingStore.selectedSeats.length === 0">
          Xóa
        </button>
        <button
          class="btn-next"
          :disabled="bookingStore.selectedSeats.length === 0"
          @click="goCombo"
        >
          Tiếp tục ({{ bookingStore.selectedSeats.length }})
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useBookingStore } from '@/stores/bookingStore'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { fmtDateTime12 } from '@/utils/homeHelpers'
import { useSeatWebSocket } from '@/composables/useSeatWebSocket'

const router       = useRouter()
const route        = useRoute()
const bookingStore = useBookingStore()
const authStore    = useAuthStore()

const allSeats      = ref([])
const lockedSeatIds = ref(new Set())   // seats locked by OTHER users
const loading       = ref(false)
const loadError     = ref('')

// ── Real-time seat lock updates via WebSocket ──────────────────
let seatWs = null   // { disconnect }

function handleSeatWsMessage(msg) {
  const seatId = msg.gheNgoiId
  if (!seatId) return
  if (msg.event === 'locked') {
    const seat = allSeats.value.find(s => s.id === seatId)
    if (seat && !isSelected(seat)) {
      const next = new Set(lockedSeatIds.value)
      next.add(seatId)
      lockedSeatIds.value = next
    }
  } else if (msg.event === 'unlocked') {
    const next = new Set(lockedSeatIds.value)
    next.delete(seatId)
    lockedSeatIds.value = next
  }
}

// Countdown state: seatId → { timer (setInterval), secondsLeft }
const seatCountdowns = ref({})         // seatId → secondsLeft
let   lockDurationSecs = 10 * 60       // default 10 min; updated on first lock response

// Toast for expired-seat notices
const expiredToast = ref('')
let   expiredToastTimer = null

function showExpiredToast(msg) {
  clearTimeout(expiredToastTimer)
  expiredToast.value = msg
  expiredToastTimer = setTimeout(() => { expiredToast.value = '' }, 4000)
}

// ── Group seats by row ─────────────────────────────────────
const rows = computed(() => {
  const map = {}
  allSeats.value.forEach(s => {
    const key = (s.hangGhe || '?').trim()
    if (!map[key]) map[key] = { label: key, seats: [] }
    map[key].seats.push(s)
  })
  return Object.values(map)
    .sort((a, b) => a.label.localeCompare(b.label))
    .map(r => ({ ...r, seats: r.seats.sort((a, b) => a.soGhe - b.soGhe) }))
})

const isMaxReached = computed(() => bookingStore.selectedSeats.length >= 8)

// ── Seat helpers ────────────────────────────────────────────
function isBooked(seat) {
  return seat.trangThai === 'booked' || seat.trangThai === 'reserved'
}
function isLockedByOther(seat) {
  return lockedSeatIds.value.has(seat.id) && !isSelected(seat)
}
function isSelected(seat) {
  return bookingStore.selectedSeats.some(s => s.id === seat.id)
}
function seatClass(seat) {
  if (isSelected(seat))      return 'seat--selected'
  if (isBooked(seat))        return 'seat--booked'
  if (isLockedByOther(seat)) return 'seat--locked'
  const t = (seat.loaiGhe || '').toLowerCase()
  if (t === 'vip')      return 'seat--vip'
  if (t.includes('cặp') || t.includes('couple')) return 'seat--couple'
  return ''
}
function seatTitle(seat) {
  const label = `${(seat.hangGhe||'').trim()}${seat.soGhe}`
  if (isBooked(seat))        return `${label} — Đã đặt`
  if (isLockedByOther(seat)) return `${label} — Đang được giữ`
  return `${label} — ${fmtPrice(seat.giaTien)}`
}

// ── Countdown management ───────────────────────────────────
function startCountdown(seatId, durationSecs) {
  stopCountdown(seatId)
  seatCountdowns.value[seatId] = durationSecs
  const interval = setInterval(() => {
    const left = (seatCountdowns.value[seatId] || 1) - 1
    if (left <= 0) {
      stopCountdown(seatId)
      delete seatCountdowns.value[seatId]
      // Auto-deselect expired seat
      bookingStore.removeSeat(seatId)
      showExpiredToast('Ghế đã hết thời gian giữ. Vui lòng chọn lại.')
    } else {
      seatCountdowns.value[seatId] = left
    }
  }, 1000)
  // Store interval id on the object itself
  seatCountdowns.value[`__timer_${seatId}`] = interval
}
function stopCountdown(seatId) {
  const timerId = seatCountdowns.value[`__timer_${seatId}`]
  if (timerId) { clearInterval(timerId); delete seatCountdowns.value[`__timer_${seatId}`] }
}
function fmtCountdown(seatId) {
  const secs = seatCountdowns.value[seatId]
  if (!secs) return ''
  const m = Math.floor(secs / 60)
  const s = secs % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

// ── Toggle seat selection ──────────────────────────────────
async function toggle(seat) {
  if (isBooked(seat) || isLockedByOther(seat)) return

  if (isSelected(seat)) {
    // Deselect → release lock
    bookingStore.removeSeat(seat.id)
    stopCountdown(seat.id)
    delete seatCountdowns.value[seat.id]
    try {
      await api.delete('/dat-ve/release-seat', {
        data: { gheNgoiId: seat.id, lichChieuId: bookingStore.selectedShowtime?.id }
      })
    } catch { /* non-fatal — lock will expire anyway */ }
  } else {
    // Select → try to lock
    try {
      const res = await api.post('/dat-ve/lock-seat', {
        gheNgoiId:   seat.id,
        lichChieuId: bookingStore.selectedShowtime?.id
      })
      const durationSecs = (res.data?.lockDurationMinutes ?? 10) * 60
      lockDurationSecs = durationSecs
      bookingStore.addSeat(seat)
      startCountdown(seat.id, durationSecs)
    } catch (e) {
      const msg = e.response?.data?.message || e.response?.data || 'Ghế đã bị giữ. Vui lòng chọn ghế khác.'
      showExpiredToast(msg)
    }
  }
}

// ── Load seats + locked seats ──────────────────────────────
async function loadSeats() {
  const showtimeId = route.params.showtimeId || bookingStore.selectedShowtime?.id
  if (!showtimeId) { loadError.value = 'Không xác định được suất chiếu. Vui lòng quay lại.'; return }
  loading.value = true
  loadError.value = ''
  try {
    const [seatsRes, lockedRes] = await Promise.all([
      api.get(`/lich-chieu/${showtimeId}/ghe-trong`),
      api.get(`/lich-chieu/${showtimeId}/locked-seats`).catch(() => ({ data: [] }))
    ])
    allSeats.value = Array.isArray(seatsRes.data) ? seatsRes.data : []
    lockedSeatIds.value = new Set(Array.isArray(lockedRes.data) ? lockedRes.data : [])
  } catch (e) {
    loadError.value = e.response?.data?.message || 'Không tải được sơ đồ ghế'
  } finally {
    loading.value = false
  }
}

// ── Release all locked seats on page leave ─────────────────
async function releaseAllLocks() {
  const showtimeId = bookingStore.selectedShowtime?.id
  if (!showtimeId) return
  const seated = [...bookingStore.selectedSeats]
  await Promise.allSettled(seated.map(s =>
    api.delete('/dat-ve/release-seat', { data: { gheNgoiId: s.id, lichChieuId: showtimeId } })
  ))
}

function goCombo() {
  if (bookingStore.selectedSeats.length === 0) return
  router.push('/combo')
}

function fmtTime(dt) {
  return fmtDateTime12(dt)
}
function fmtPrice(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(v)
}

// beforeunload: release locks if user navigates away
function handleBeforeUnload(e) {
  const showtimeId = bookingStore.selectedShowtime?.id
  if (!showtimeId || bookingStore.selectedSeats.length === 0) return
  // Fire-and-forget best-effort release (navigator.sendBeacon would be ideal but API is JSON)
  bookingStore.selectedSeats.forEach(s => {
    navigator.sendBeacon
      ? navigator.sendBeacon('/api/dat-ve/release-seat', JSON.stringify({ gheNgoiId: s.id, lichChieuId: showtimeId }))
      : null
  })
}

onMounted(() => {
  if (!authStore.isLoggedIn) { router.push('/auth'); return }
  const currentLichChieuId = Number(route.params.showtimeId)
  if (bookingStore.selectedShowtime?.id !== currentLichChieuId) {
    bookingStore.clearSeats()
  }
  loadSeats()
  window.addEventListener('beforeunload', handleBeforeUnload)

  // Connect WebSocket for real-time seat lock updates
  if (currentLichChieuId) {
    seatWs = useSeatWebSocket(currentLichChieuId, handleSeatWsMessage)
  }
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  if (seatWs) { seatWs.disconnect(); seatWs = null }
  Object.keys(seatCountdowns.value)
    .filter(k => k.startsWith('__timer_'))
    .forEach(k => clearInterval(seatCountdowns.value[k]))
})
</script>

<style scoped>
/* ── base ──────────────────────────────────────────────────── */
.seat-page {
  background: var(--void, #050508);
  color: var(--text-secondary, #94a3b8);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-bottom: 180px;
  font-family: var(--font-ui, 'Inter', sans-serif);
}

/* ── top bar ──────────────────────────────────────────────── */
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px;
  background: rgba(5,5,8,0.85);
  backdrop-filter: var(--glass-blur, blur(20px));
  -webkit-backdrop-filter: var(--glass-blur, blur(20px));
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
  color: var(--electric, #29bcea); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s, border-color 0.2s;
}
.icon-btn:hover {
  background: var(--glass-bg-heavy, rgba(255,255,255,0.08));
  border-color: var(--electric, #29bcea);
}
.icon-btn svg { width: 18px; height: 18px; }

/* ── strip ────────────────────────────────────────────────── */
.showtime-strip {
  display: flex; align-items: center; gap: 0;
  padding: 12px 20px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  backdrop-filter: blur(8px);
  overflow-x: auto;
}
.strip-item { display: flex; flex-direction: column; gap: 2px; flex-shrink: 0; padding: 0 16px; }
.strip-item:first-child { padding-left: 0; }
.strip-label { font-size: 10px; text-transform: uppercase; color: var(--text-ghost, rgba(241,245,249,0.45)); font-weight: 700; letter-spacing: 0.4px; }
.strip-value { font-size: 13px; font-weight: 700; color: var(--text-primary, #f1f5f9); }
.strip-sep { width: 1px; background: var(--glass-border, rgba(255,255,255,0.08)); align-self: stretch; flex-shrink: 0; }

/* ── legend ───────────────────────────────────────────────── */
.legend {
  display: flex; gap: 12px; align-items: center;
  padding: 12px 20px; flex-wrap: wrap;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  justify-content: center;
}
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--text-secondary, #94a3b8); }
.seat-sample {
  width: 20px; height: 20px; border-radius: 4px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.seat-sample--booked  { background: #ef4444; }
.seat-sample--locked  { background: #f59e0b; }
.seat-sample--selected { background: var(--electric, #29bcea); box-shadow: 0 0 8px var(--electric-glow, rgba(41,188,234,0.30)); }
.seat-sample--vip     { background: var(--gold, #C9A84C); box-shadow: 0 0 8px var(--gold-glow, rgba(201,168,76,0.35)); }
.seat-sample--couple  { background: #ec4899; }

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
  border-top-color: var(--electric, #29bcea); border-radius: 50%;
  animation: spin 0.9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.btn-retry {
  padding: 10px 26px;
  background: var(--electric, #29bcea); color: var(--on-accent, #ffffff);
  border: none; border-radius: var(--radius-sm, 6px);
  font-weight: 700; cursor: pointer;
}

/* ── screen ───────────────────────────────────────────────── */
.screen-wrap { flex: 1; padding: 28px 16px 0; max-width: 900px; margin: 0 auto; width: 100%; }
.screen-label {
  text-align: center; font-size: 10px; font-weight: 900;
  letter-spacing: 3px; color: var(--text-ghost, rgba(241,245,249,0.45));
  text-transform: uppercase; margin-bottom: 6px;
}
.screen-bar {
  height: 6px;
  border-radius: var(--radius-pill, 999px);
  background: linear-gradient(90deg, transparent 0%, var(--electric, #29bcea) 50%, transparent 100%);
  margin-bottom: 32px;
  transform: perspective(800px) rotateX(-8deg);
  box-shadow: 0 4px 32px var(--electric-glow, rgba(41,188,234,0.30));
}

/* ── grid ─────────────────────────────────────────────────── */
.grid-scroll { overflow-x: auto; padding-bottom: 16px; }
.rows-wrap { display: flex; flex-direction: column; gap: 10px; min-width: fit-content; }
.seat-row { display: flex; align-items: center; gap: 10px; }
.row-label { width: 24px; text-align: center; font-size: 12px; font-weight: 800; color: var(--text-ghost, rgba(241,245,249,0.45)); flex-shrink: 0; }
.row-seats { display: flex; gap: 6px; }

/* ── seats ────────────────────────────────────────────────── */
.seat {
  width: 36px; height: 36px; border-radius: 8px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  font-size: 11px; font-weight: 700;
  cursor: pointer;
  transition: transform 0.15s ease-out, box-shadow 0.2s ease-out;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-secondary, #94a3b8); flex-shrink: 0;
  background: var(--surface-3, #1a1a28);
}
.seat:hover:not(:disabled) {
  transform: scale(1.12) translateY(-2px);
  border-color: var(--electric, #29bcea);
  color: var(--electric, #29bcea);
}
.seat:disabled { cursor: not-allowed; opacity: 0.5; }

.seat--booked {
  background: rgba(239,68,68,0.2);
  border-color: rgba(239,68,68,0.4);
  color: #ef4444; opacity: 0.7;
}
.seat--selected {
  background: var(--electric, #29bcea);
  border-color: var(--electric, #29bcea);
  color: var(--on-accent, #ffffff);
  transform: scale(1.06);
  box-shadow: 0 0 12px var(--electric-glow, rgba(41,188,234,0.30));
}
.seat--vip {
  background: var(--gold, #C9A84C);
  border-color: var(--gold, #C9A84C);
  color: var(--on-accent, #ffffff);
}
.seat--vip:hover:not(:disabled) {
  background: var(--gold-bright, #F5D17E);
  box-shadow: 0 0 8px var(--gold-glow, rgba(201,168,76,0.35));
}
.seat--couple {
  background: #ec4899;
  border-color: #db2777;
  color: #ffffff;
}
.seat--couple:hover:not(:disabled) {
  background: #f472b6;
}
/* Locked by another user — amber */
.seat--locked {
  background: rgba(245,158,11,0.25);
  border-color: rgba(245,158,11,0.5);
  color: #f59e0b;
  cursor: not-allowed;
}
/* Countdown label inside selected seat */
.seat-countdown {
  font-size: 9px;
  font-weight: 900;
  line-height: 1;
  color: #ffffff;
  letter-spacing: -0.3px;
}

/* ── Expired-seat toast ──────────────────────────────────── */
.expired-toast {
  position: fixed;
  top: 72px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 200;
  padding: 12px 20px;
  background: rgba(245,158,11,0.15);
  border: 1px solid rgba(245,158,11,0.4);
  border-radius: var(--radius-sm, 6px);
  color: #fbbf24;
  font-size: 14px;
  font-weight: 700;
  backdrop-filter: blur(12px);
  white-space: nowrap;
}
.toast-enter-active, .toast-leave-active { transition: opacity 0.25s, transform 0.25s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(-8px); }

/* ── bottom bar ───────────────────────────────────────────── */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 55;
  padding: 14px 20px;
  background: rgba(5,5,8,0.9);
  backdrop-filter: var(--glass-blur, blur(20px));
  -webkit-backdrop-filter: var(--glass-blur, blur(20px));
  border-top: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
}
.bottom-bar__info { flex: 1; display: flex; flex-direction: column; gap: 6px; }
.info-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.info-label { font-size: 12px; color: var(--text-ghost, rgba(241,245,249,0.45)); font-weight: 600; white-space: nowrap; }
.info-chips { display: flex; gap: 4px; flex-wrap: wrap; }
.chip-none { font-size: 12px; color: var(--text-ghost, rgba(241,245,249,0.45)); }
.chip-seat {
  padding: 3px 10px;
  background: var(--electric-soft, rgba(41,188,234,0.08));
  border: 1px solid rgba(41,188,234,0.25);
  border-radius: var(--radius-pill, 999px);
  font-size: 11px; font-weight: 700; color: var(--electric, #29bcea);
}
.info-price { font-size: 18px; font-weight: 900; color: var(--electric, #29bcea); }
.max-warn { font-size: 11px; color: #fbbf24; margin: 0; }

.bottom-bar__actions { display: flex; gap: 10px; }
.btn-clear {
  padding: 10px 16px; border-radius: var(--radius-sm, 6px);
  background: transparent; color: var(--text-secondary, #94a3b8);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s;
}
.btn-clear:hover:not(:disabled) { border-color: var(--electric, #29bcea); color: var(--electric, #29bcea); }
.btn-clear:disabled { opacity: 0.4; cursor: not-allowed; }

.btn-next {
  padding: 11px 28px;
  border-radius: var(--radius-sm, 6px);
  background: var(--electric, #29bcea);
  color: var(--on-accent, #ffffff); border: none;
  font-size: 14px; font-weight: 700; cursor: pointer;
  outline: 1.5px solid rgba(41,188,234,0.45); outline-offset: 3px;
  transition: transform 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1)),
              box-shadow 0.3s var(--ease-out, cubic-bezier(0.4,0,0.2,1)),
              outline-offset 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1));
  will-change: transform;
}
.btn-next:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--electric-glow, rgba(41,188,234,0.30));
  outline-offset: 5px;
}
.btn-next:disabled { opacity: 0.4; cursor: not-allowed; box-shadow: none; }

/* ── responsive ───────────────────────────────────────────── */
@media (max-width: 640px) {
  .bottom-bar { flex-direction: column; gap: 12px; padding: 12px 16px; }
  .bottom-bar__actions { width: 100%; }
  .btn-clear, .btn-next { flex: 1; text-align: center; }
  .seat { width: 30px; height: 30px; font-size: 10px; }
  .row-seats { gap: 5px; }
}
</style>

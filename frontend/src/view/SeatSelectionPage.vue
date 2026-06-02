<template>
  <div class="seat-page">
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
      <div class="legend-item"><div class="seat-sample seat-sample--avail"></div><span>Trống</span></div>
      <div class="legend-item"><div class="seat-sample seat-sample--booked"></div><span>Đã đặt</span></div>
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
                :disabled="isBooked(seat) || (isMaxReached && !isSelected(seat))"
                :title="seatTitle(seat)"
                @click="toggle(seat)"
                :aria-label="seatTitle(seat)"
              >
                {{ seat.soGhe }}
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useBookingStore } from '@/stores/bookingStore'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router       = useRouter()
const route        = useRoute()
const bookingStore = useBookingStore()
const authStore    = useAuthStore()

const allSeats  = ref([])
const loading   = ref(false)
const loadError = ref('')

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
function isSelected(seat) {
  return bookingStore.selectedSeats.some(s => s.id === seat.id)
}
function seatClass(seat) {
  if (isSelected(seat)) return 'seat--selected'
  if (isBooked(seat))   return 'seat--booked'
  const t = (seat.loaiGhe || '').toLowerCase()
  if (t === 'vip')      return 'seat--vip'
  if (t.includes('cặp') || t.includes('couple')) return 'seat--couple'
  return 'seat--avail'
}
function seatTitle(seat) {
  const label = `${(seat.hangGhe||'').trim()}${seat.soGhe}`
  return `${label} — ${fmtPrice(seat.giaTien)}${isBooked(seat) ? ' (Đã đặt)' : ''}`
}

function toggle(seat) {
  if (isBooked(seat)) return
  if (isSelected(seat)) {
    bookingStore.removeSeat(seat.id)
  } else {
    bookingStore.addSeat(seat)
  }
}

// ── Load seats ──────────────────────────────────────────────
async function loadSeats() {
  const showtimeId = route.params.showtimeId || bookingStore.selectedShowtime?.id
  if (!showtimeId) {
    loadError.value = 'Không xác định được suất chiếu. Vui lòng quay lại.'
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    const res = await api.get(`/lich-chieu/${showtimeId}/ghe-trong`)
    allSeats.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    loadError.value = e.response?.data?.message || 'Không tải được sơ đồ ghế'
  } finally {
    loading.value = false
  }
}

function goCombo() {
  if (bookingStore.selectedSeats.length === 0) return
  router.push('/combo')
}

function fmtTime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN', { day:'2-digit', month:'2-digit', hour:'2-digit', minute:'2-digit' })
}
function fmtPrice(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(v)
}

onMounted(() => {
  if (!authStore.isLoggedIn) { router.push('/auth'); return }
  bookingStore.clearSeats()
  loadSeats()
})
</script>

<style scoped>
/* ── base ──────────────────────────────────────────────────── */
.seat-page {
  background: #ffffff;
  color: #7f7e7f;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-bottom: 180px;
  font-family: 'Raleway', sans-serif;
}

/* ── top bar ──────────────────────────────────────────────── */
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px;
  background: #ffffff;
  border-bottom: 1px solid #efefef;
  position: sticky; top: 0; z-index: 60;
}
.top-bar__title { font-size: 16px; font-weight: 700; color: #29bcea; }
.icon-btn {
  width: 44px; height: 44px; border-radius: 4px;
  border: 1px solid #efefef;
  background: #f7f7f7; color: #29bcea;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: background .2s;
}
.icon-btn:hover { background: rgba(41,188,234,.15); }
.icon-btn svg { width: 18px; height: 18px; }

/* ── strip ────────────────────────────────────────────────── */
.showtime-strip {
  display: flex; align-items: center; gap: 0;
  padding: 12px 20px;
  background: #f7f7f7;
  border-bottom: 1px solid #efefef;
  overflow-x: auto;
}
.strip-item { display: flex; flex-direction: column; gap: 2px; flex-shrink: 0; padding: 0 16px; }
.strip-item:first-child { padding-left: 0; }
.strip-label { font-size: 10px; text-transform: uppercase; color: #64748b; font-weight: 700; letter-spacing: .4px; }
.strip-value { font-size: 13px; font-weight: 700; color: #000000; }
.strip-sep { width: 1px; background: rgba(41,188,234,.12); align-self: stretch; flex-shrink: 0; }

/* ── legend ───────────────────────────────────────────────── */
.legend {
  display: flex; gap: 16px; align-items: center;
  padding: 12px 20px; flex-wrap: wrap;
  background: #ffffff;
  border-bottom: 1px solid #efefef;
  justify-content: center;
}
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #94a3b8; }
.seat-sample {
  width: 20px; height: 20px; border-radius: 4px;
  border: 1px solid rgba(255,255,255,.1);
}
.seat-sample--avail  { background: #374151; }
.seat-sample--booked { background: #ef4444; }
.seat-sample--selected { background: #29bcea; }
.seat-sample--vip    { background: #7c3aed; }
.seat-sample--couple { background: #ec4899; }

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
  border: 4px solid rgba(41,188,234,.15);
  border-top-color: #29bcea; border-radius: 50%;
  animation: spin .9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.btn-retry {
  padding: 10px 26px; background: #29bcea; color: #ffffff;
  border: none; border-radius: 8px; font-weight: 800; cursor: pointer;
}

/* ── screen ───────────────────────────────────────────────── */
.screen-wrap { flex: 1; padding: 28px 16px 0; max-width: 900px; margin: 0 auto; width: 100%; }
.screen-label { text-align: center; font-size: 10px; font-weight: 900; letter-spacing: 3px; color: #64748b; text-transform: uppercase; margin-bottom: 6px; }
.screen-bar {
  height: 4px;
  border-radius: 0;
  background: #29bcea;
  margin-bottom: 32px;
  opacity: 0.85;
}

/* ── grid ─────────────────────────────────────────────────── */
.grid-scroll { overflow-x: auto; padding-bottom: 16px; }
.rows-wrap { display: flex; flex-direction: column; gap: 10px; min-width: fit-content; }
.seat-row { display: flex; align-items: center; gap: 10px; }
.row-label { width: 24px; text-align: center; font-size: 12px; font-weight: 800; color: #64748b; flex-shrink: 0; }
.row-seats { display: flex; gap: 6px; }

/* ── seats ────────────────────────────────────────────────── */
.seat {
  width: 34px; height: 34px; border-radius: 5px;
  border: 1px solid rgba(255,255,255,.1);
  font-size: 11px; font-weight: 700;
  cursor: pointer; transition: all .15s;
  display: flex; align-items: center; justify-content: center;
  color: #7f7e7f; flex-shrink: 0;
}
.seat:hover:not(:disabled) { transform: scale(1.12); border-color: #29bcea; }
.seat:disabled { cursor: not-allowed; opacity: .6; }

.seat--avail   { background: #f7f7f7; border-color: #efefef; color: #7f7e7f; }
.seat--avail:hover:not(:disabled) { border-color: #29bcea; color: #29bcea; }
.seat--booked  { background: #ef4444; border-color: #dc2626; opacity: .7; }
.seat--selected { background: #29bcea; border-color: #29bcea; color: #ffffff; transform: scale(1.06); }
.seat--vip     { background: #7c3aed; border-color: #6d28d9; }
.seat--vip:hover:not(:disabled) { background: #8b5cf6; }
.seat--couple  { background: #ec4899; border-color: #db2777; }
.seat--couple:hover:not(:disabled) { background: #f472b6; }

/* ── bottom bar ───────────────────────────────────────────── */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 55;
  padding: 14px 20px;
  background: #ffffff;
  border-top: 1px solid #efefef;
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
}
.bottom-bar__info { flex: 1; display: flex; flex-direction: column; gap: 6px; }
.info-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.info-label { font-size: 12px; color: #64748b; font-weight: 600; white-space: nowrap; }
.info-chips { display: flex; gap: 4px; flex-wrap: wrap; }
.chip-none { font-size: 12px; color: #64748b; }
.chip-seat {
  padding: 2px 8px; background: rgba(41,188,234,.15); border: 1px solid rgba(41,188,234,.3);
  border-radius: 5px; font-size: 11px; font-weight: 800; color: #29bcea;
}
.info-price { font-size: 18px; font-weight: 900; color: #29bcea; }
.max-warn { font-size: 11px; color: #fbbf24; margin: 0; }

.bottom-bar__actions { display: flex; gap: 10px; }
.btn-clear {
  padding: 10px 16px; border-radius: 8px;
  background: transparent; color: #94a3b8;
  border: 1px solid rgba(255,255,255,.15);
  font-size: 13px; font-weight: 700; cursor: pointer; transition: all .2s;
}
.btn-clear:hover:not(:disabled) { border-color: #29bcea; color: #29bcea; }
.btn-clear:disabled { opacity: .4; cursor: not-allowed; }
.btn-next {
  padding: 11px 24px; border-radius: 4px;
  background: #29bcea;
  color: #ffffff; border: none;
  font-size: 14px; font-weight: 900; cursor: pointer; transition: all .2s;
  box-shadow: 0 4px 16px rgba(41,188,234,.3);
}
.btn-next:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(41,188,234,.4); }
.btn-next:disabled { opacity: .4; cursor: not-allowed; box-shadow: none; }

/* ── responsive ───────────────────────────────────────────── */
@media (max-width: 640px) {
  .bottom-bar { flex-direction: column; gap: 12px; padding: 12px 16px; }
  .bottom-bar__actions { width: 100%; }
  .btn-clear, .btn-next { flex: 1; text-align: center; }
  .seat { width: 30px; height: 30px; font-size: 10px; }
  .row-seats { gap: 5px; }
}
</style>

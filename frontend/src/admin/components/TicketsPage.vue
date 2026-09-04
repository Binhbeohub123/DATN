<template>
  <div class="tickets-page">
    <div class="toolbar">
      <select v-model="statusFilter" class="filter-select" @change="load">
        <option value="">Tất cả trạng thái</option>
        <option value="confirmed">Đã xác nhận</option>
        <option value="pending">Chờ thanh toán</option>
        <option value="cancelled">Đã hủy</option>
      </select>
    </div>

    <div class="card">
      <div v-if="loading" class="loading-text">Đang tải...</div>
      <div v-else-if="tickets.length === 0" class="empty-text">Không có vé nào</div>
      <table v-else>
        <thead>
          <tr>
            <th>Mã vé</th>
            <th>Khách hàng</th>
            <th>Phim</th>
            <th>Suất chiếu</th>
            <th>Ghế</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in tickets" :key="t.id">
            <td class="mono">{{ t.maDatVe }}</td>
            <td>{{ t.email || t.hoTen || '—' }}</td>
            <td>{{ t.tenPhim }}</td>
            <td>{{ fmtShowtime(t) }}</td>
            <td>
              <div v-if="t.seats && t.seats.length > 0" class="seat-chips">
                <span
                  v-for="s in t.seats"
                  :key="(s.hangGhe || '') + s.soGhe"
                  :class="['seat-chip', seatChipClass(s.loaiGhe)]"
                  :title="s.loaiGhe"
                >{{ (s.hangGhe || '').trim() }}{{ s.soGheHienThi ?? s.soGhe }}</span>
              </div>
              <span v-else class="no-seats">—</span>
            </td>
            <td class="price">{{ fmtPrice(t.tongTien) }}</td>
            <td><span :class="['badge', badgeClass(t.trangThai)]">{{ statusLabel(t.trangThai) }}</span></td>
            <td>
              <button
                v-if="t.trangThai !== 'cancelled' && t.trangThai !== 'refunded'"
                class="btn-cancel-ticket"
                @click="cancelTicket(t)"
              >Hủy vé</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="pagination">
        <button :disabled="page === 0" @click="page--; load()">← Trước</button>
        <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="page++; load()">Sau →</button>
      </div>
    </div>

    <!-- ── Seat Lock Management ── -->
    <div class="card lock-section">
      <div class="lock-header">
        <h3 class="lock-title">Quản lý ghế đang khóa</h3>
        <div class="lock-config-row">
          <label class="lock-config-label">Thời gian khóa ghế (phút):</label>
          <input v-model.number="lockMinutes" type="number" min="1" max="60" class="lock-min-input" />
          <button class="btn-save-lock" @click="saveLockMinutes">Lưu</button>
          <span v-if="lockSaveMsg" class="lock-save-msg">{{ lockSaveMsg }}</span>
        </div>
      </div>

      <div class="lock-filter-row">
        <select v-model="lockLichChieuId" class="filter-select" style="min-width:200px" @change="onLichChieuChange">
          <option value="">-- Chọn suất chiếu --</option>
          <option v-for="lc in lichChieuList" :key="lc.id" :value="lc.id">
            {{ fmtLichChieu(lc) }}
          </option>
        </select>
      </div>

      <!-- ── Seat map (replaces old table) ── -->
      <div v-if="loadingLocks" class="loading-text">Đang tải sơ đồ ghế...</div>
      <template v-else-if="seatMapData.length > 0">
        <!-- Stats -->
        <div class="seat-stats">
          <span>Tổng: <strong>{{ seatMapData.length }}</strong> ghế</span>
          <span class="stat-avail">Trống: <strong>{{ seatMapData.filter(s => s.status === 'available').length }}</strong></span>
          <span class="stat-booked">Đã đặt: <strong>{{ seatMapData.filter(s => s.status === 'booked').length }}</strong></span>
          <span class="stat-locked">Đang khóa: <strong>{{ seatMapData.filter(s => s.status === 'locked').length }}</strong></span>
        </div>

        <!-- Legend -->
        <div class="map-legend">
          <span class="legend-item"><div class="legend-dot" style="background:#374151"></div> Trống</span>
          <span class="legend-item"><div class="legend-dot legend-dot--aisle"></div> Lối đi</span>
          <span class="legend-item"><div class="legend-dot" style="background:#EF4444"></div> Đã đặt</span>
          <span class="legend-item"><div class="legend-dot" style="background:#F59E0B"></div> Đang giữ</span>
          <span class="legend-item"><div class="legend-dot" style="background:#C9A84C"></div> VIP</span>
          <span class="legend-item"><div class="legend-dot" style="background:#EC4899"></div> Cặp đôi</span>
        </div>

        <SeatGrid
          :rows="seatMapRows"
          :total-cols="smTotalCols"
          :show-screen="true"
          :seat-key-fn="s => s.gheNgoiId"
          :seat-title-fn="seatMapTitle"
          :seat-class-fn="ticketSeatClass"
        >
          <template #seat-content="{ seat }">
            <span class="sm-seat-label">{{ (seat.loaiGhe || '').trim() === 'trống' ? '' : (seat.soGheHienThi ?? seat.soGhe) }}</span>
            <button
              v-if="seat.status === 'locked' && seat.lockId"
              class="sm-release-btn"
              :title="`Mở khóa ghế ${(seat.hangGhe || '').trim()}${seat.soGheHienThi ?? seat.soGhe}`"
              @click.stop="forceReleaseSeat(seat)"
            >&#x2715;</button>
          </template>
        </SeatGrid>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import api from '@/services/api'
import { useAdminShellStore } from '@/stores/adminShellStore'
import { fmtTime12, fmtDateTime12 } from '@/utils/homeHelpers'
import { fmtDate } from '@/utils/dateFmt'
import { useSeatWebSocket } from '@/composables/useSeatWebSocket'
import SeatGrid from '@/components/SeatGrid.vue'

const shell = useAdminShellStore()

const tickets = ref([])
const loading = ref(false)
const search = ref('')
const statusFilter = ref('')
const page = ref(0)
const totalPages = ref(1)
let debounceTimer = null

// ── Seat lock management state ──────────────────────────────
const lockMinutes     = ref(10)
const lockSaveMsg     = ref('')
const lockLichChieuId = ref('')
const lichChieuList   = ref([])
const activeLocks     = ref([])
const loadingLocks    = ref(false)

// ── Seat map state ──────────────────────────────────────────
const seatMapData  = ref([])   // [{ gheNgoiId, hangGhe, soGhe, loaiGhe, status, lockId?, expiresAt? }]
let   seatMapWs    = null      // WebSocket connection (replaced 30s polling)

const seatMapRows = computed(() => {
  const map = {}
  seatMapData.value.forEach(s => {
    const key = (s.hangGhe || '?').trim()
    if (!map[key]) map[key] = { label: key, seats: [] }
    map[key].seats.push(s)
  })
  return Object.values(map)
    .sort((a, b) => a.label.localeCompare(b.label))
    .map(r => ({ ...r, seats: r.seats.sort((a, b) => a.soGhe - b.soGhe) }))
})

// Số cột vật lý rộng nhất — grid-column giữ vị trí thật của khe lối đi
const smTotalCols = computed(() =>
  seatMapData.value.reduce((m, s) => Math.max(m, s.soGhe || 0), 0)
)

function seatMapTitle(seat) {
  const label = `${(seat.hangGhe || '').trim()}${seat.soGheHienThi ?? seat.soGhe}`
  if (seat.status === 'locked' && seat.expiresAt) {
    const exp = new Date(seat.expiresAt)
    const hhmm = exp.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
    return `Ghế ${label} — Hết khóa lúc ${hhmm}`
  }
  if (seat.status === 'booked') return `Ghế ${label} — Đã đặt`
  const loai = seat.loaiGhe ? ` (${seat.loaiGhe})` : ''
  return `Ghế ${label}${loai} — Trống`
}

function ticketSeatClass(seat) {
  if (seat.status === 'locked') return 'seat--locked'
  if (seat.status === 'booked') return 'seat--booked'
  return ''
}

async function loadSeatMap() {
  if (!lockLichChieuId.value) return
  loadingLocks.value = true
  try {
    const res = await api.get('/admin/seat-map', { params: { lichChieuId: lockLichChieuId.value } })
    seatMapData.value = Array.isArray(res.data) ? res.data : []
    // Also keep activeLocks in sync for the old force-release helper
    activeLocks.value = seatMapData.value.filter(s => s.status === 'locked')
  } catch { seatMapData.value = [] }
  finally { loadingLocks.value = false }
}

/** Called by @change on the showtime dropdown — loads map immediately or resets. */
function onLichChieuChange() {
  stopSeatMapWs()
  if (!lockLichChieuId.value) {
    seatMapData.value = []
    activeLocks.value = []
    return
  }
  loadSeatMap().then(() => {
    // Connect WebSocket for real-time updates (replaces 30s polling)
    startSeatMapWs(lockLichChieuId.value)
  })
}

function startSeatMapWs(lichChieuId) {
  stopSeatMapWs()
  if (!lichChieuId) return
  seatMapWs = useSeatWebSocket(lichChieuId, () => {
    // On any seat lock/unlock event, reload the full seat map for admin detail
    if (lockLichChieuId.value) loadSeatMap()
  })
}

function stopSeatMapWs() {
  if (seatMapWs) { seatMapWs.disconnect(); seatMapWs = null }
}

async function forceReleaseSeat(seat) {
  if (!seat.lockId) return
  const label = `${(seat.hangGhe || '').trim()}${seat.soGheHienThi ?? seat.soGhe}`
  if (!confirm(`Mở khóa ghế ${label}?`)) return
  try {
    await api.delete(`/admin/seat-locks/${seat.lockId}`)
    await loadSeatMap()
  } catch (e) {
    alert(e.response?.data?.message || 'Lỗi mở khóa ghế')
  }
}

function fmtPrice(n) {
  if (n == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

function fmtShowtime(t) {
  if (!t.ngayChieu) return '—'
  return `${fmtDate(t.ngayChieu)} ${t.gioChieu || ''}`
}

function statusLabel(s) {
  const map = { confirmed: 'Đã xác nhận', pending: 'Chờ TT', cancelled: 'Đã hủy', paid: 'Đã thanh toán' }
  return map[s] || s || '—'
}

function badgeClass(s) {
  if (s === 'confirmed' || s === 'paid') return 'badge-green'
  if (s === 'pending') return 'badge-yellow'
  if (s === 'cancelled') return 'badge-red'
  return 'badge-gray'
}

/**
 * Seat chip color matches SeatSelectionPage seat colors.
 * VIP → gold, cặp đôi → pink, thường/default → gray
 */
function seatChipClass(loaiGhe) {
  if (!loaiGhe) return 'chip-thuong'
  const l = loaiGhe.toLowerCase()
  if (l === 'vip') return 'chip-vip'
  if (l.includes('cặp') || l.includes('couple')) return 'chip-couple'
  return 'chip-thuong'
}

function mapBooking(b) {
  const start = b.lichChieu?.thoiGianBatDau
  // Map seat details from chiTietDatGhe if available in the response
  const seats = Array.isArray(b.chiTietDatGhe)
    ? b.chiTietDatGhe.map(ct => ({
        id:      ct.gheNgoi?.id,
        hangGhe: (ct.gheNgoi?.hangGhe || '').trim(),
        soGhe:   ct.gheNgoi?.soGheHienThi ?? ct.gheNgoi?.soGhe,
        loaiGhe: ct.gheNgoi?.loaiGhe || 'thường',
      })).filter(s => s.hangGhe || s.soGhe)
    : []
  return {
    id: b.id,
    maDatVe: b.maDatVe,
    email: b.nguoiDung?.email,
    hoTen: b.nguoiDung?.hoTen,
    tenPhim: b.lichChieu?.phim?.tenPhim,
    ngayChieu: start,
    gioChieu: start
      ? fmtTime12(start, '')
      : '',
    tongTien: b.tongTienThanhToan,
    trangThai: b.trangThai,
    seats,
  }
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: 15 }
    if (search.value.trim()) params.q = search.value.trim()
    if (statusFilter.value) params.trangThai = statusFilter.value
    const { data } = await api.get('/admin/dat-ve', { params })
    const rows = data.content || data || []
    tickets.value = Array.isArray(rows) ? rows.map(mapBooking) : []
    totalPages.value = data.totalPages ?? 1
  } catch {
    tickets.value = []
  } finally {
    loading.value = false
  }
}

function syncFromShell() {
  if (shell.searchTargetPage && shell.searchTargetPage !== 'tickets') return
  if (shell.searchQuery) search.value = shell.searchQuery
  if (shell.ticketStatusFilter) statusFilter.value = shell.ticketStatusFilter
  page.value = 0
  load()
}

watch(() => shell.searchTick, syncFromShell)

function debouncedLoad() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => { page.value = 0; load() }, 400)
}

// ── Cancel ticket (admin) ───────────────────────────────────
async function cancelTicket(ticket) {
  if (!confirm(`Xác nhận hủy vé ${ticket.maDatVe}?`)) return
  try {
    await api.put(`/admin/dat-ve/${ticket.maDatVe}/cancel`)
    await load()
  } catch (e) {
    alert(e.response?.data?.message || 'Lỗi hủy vé')
  }
}

// ── Seat lock config ────────────────────────────────────────
async function loadLockConfig() {
  try {
    const res = await api.get('/admin/config/seat-lock-minutes')
    lockMinutes.value = res.data?.minutes ?? 10
  } catch { /* use default */ }
}

async function saveLockMinutes() {
  try {
    await api.put('/admin/config/seat-lock-minutes', { minutes: lockMinutes.value })
    lockSaveMsg.value = '✓ Đã lưu'
    setTimeout(() => { lockSaveMsg.value = '' }, 2000)
  } catch (e) {
    lockSaveMsg.value = e.response?.data?.message || 'Lỗi lưu cấu hình'
  }
}

// ── Seat lock list for a showtime ───────────────────────────
async function loadLichChieuList() {
  try {
    const res = await api.get('/admin/lich-chieu', { params: { page: 0, size: 100 } })
    lichChieuList.value = res.data?.content ?? res.data ?? []
  } catch { lichChieuList.value = [] }
}

async function loadLocks() {
  if (!lockLichChieuId.value) return
  loadingLocks.value = true
  try {
    const res = await api.get('/admin/seat-locks', { params: { lichChieuId: lockLichChieuId.value } })
    activeLocks.value = Array.isArray(res.data) ? res.data : []
  } catch { activeLocks.value = [] }
  finally { loadingLocks.value = false }
}

async function forceRelease(lockId) {
  try {
    await api.delete(`/admin/seat-locks/${lockId}`)
    await loadLocks()
  } catch (e) {
    alert(e.response?.data?.message || 'Lỗi mở khóa ghế')
  }
}

function fmtLichChieu(lc) {
  if (!lc) return ''
  const phim  = lc.phim?.tenPhim || ''
  const start = lc.thoiGianBatDau ? fmtDateTime12(lc.thoiGianBatDau, '') : ''
  return `${phim} — ${start}`
}

function fmtDt(dt) {
  if (!dt) return '—'
  return fmtDateTime12(dt)
}

function timeLeft(expiresAt) {
  if (!expiresAt) return '—'
  const diffMs = new Date(expiresAt) - Date.now()
  if (diffMs <= 0) return 'Hết hạn'
  const m = Math.floor(diffMs / 60000)
  const s = Math.floor((diffMs % 60000) / 1000)
  return `${m}:${String(s).padStart(2, '0')}`
}

onMounted(() => {
  syncFromShell()
  load()
  loadLockConfig()
  loadLichChieuList()
})

onUnmounted(() => {
  stopSeatMapWs()
})
</script>



<style scoped>
/* ── Page root ── */
.tickets-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--admin-bg);
  color: var(--admin-text);
}

/* ── Toolbar row ── */
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

/* ── Search / filter inputs ── */
.search-input, .filter-select {
  min-height: 40px;
  padding: 9px 14px;
  border: 1px solid var(--admin-border);
  border-radius: 8px;
  background: var(--admin-surface);
  color: var(--admin-text);
  font-size: 14px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  transition: border-color 150ms ease;
  -webkit-appearance: none;
  appearance: none;
}
.search-input { flex: 1; min-width: 200px; }
.search-input::placeholder { color: var(--admin-text-muted); }
.search-input:focus, .filter-select:focus {
  outline: none;
  border-color: var(--admin-accent);
  box-shadow: 0 0 0 2px rgba(255,255,255,0.15);
}

/* ── Table wrapper ── */
.card {
  background: var(--admin-surface);
  border: 1px solid var(--admin-border);
  border-radius: 12px;
  overflow: hidden;
}

/* ── Table ── */
table { width: 100%; border-collapse: collapse; }
thead tr { background: var(--admin-bg); }
th {
  padding: 12px 16px;
  text-align: left;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--admin-text-muted);
  border-bottom: 2px solid var(--admin-border);
  white-space: nowrap;
}
tbody tr { border-bottom: 1px solid var(--admin-divider); transition: background 150ms ease; }
tbody tr:last-child { border-bottom: none; }
tbody tr:hover { background: var(--admin-surface-hover); }
td {
  padding: 14px 16px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--admin-text);
  vertical-align: middle;
}

/* ── Cell helpers ── */
.mono  { font-family: 'Courier New', monospace; font-size: 13px; color: var(--admin-accent); font-weight: 600; }
.price { font-weight: 700; color: var(--admin-accent); }

/* ── Status badges ── */
.badge {
  display: inline-flex;
  align-items: center;
  border-radius: 9999px;
  padding: 3px 12px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}
.badge-green  { background: rgba(16,185,129,0.15); color: #10B981; }
.badge-yellow { background: rgba(245,158,11,0.15);  color: #F59E0B; }
.badge-red    { background: rgba(239,68,68,0.15);   color: #EF4444; }
.badge-gray   { background: rgba(156,163,175,0.15); color: var(--admin-text-muted); }

/* ── Loading / empty ── */
.loading-text, .empty-text {
  text-align: center;
  padding: 32px;
  color: var(--admin-text-muted);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
}

/* ── Pagination ── */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 14px 20px;
  border-top: 1px solid var(--admin-divider);
}
.pagination button {
  min-height: 36px;
  padding: 7px 16px;
  border: 1px solid var(--admin-border);
  border-radius: 9999px;
  background: transparent;
  color: var(--admin-text);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-weight: 600;
  cursor: pointer;
  transition: border-color 150ms ease, color 150ms ease;
}
.pagination button:hover:not(:disabled) { border-color: var(--admin-accent); color: var(--admin-accent); }
.pagination button:disabled { opacity: 0.35; cursor: not-allowed; }
.pagination span { font-size: 13px; color: var(--admin-text-muted); font-weight: 600; }

/* ── Seat lock section ── */
.lock-section { margin-top: 20px; }
.lock-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; padding: 16px 20px 12px; border-bottom: 1px solid var(--admin-divider); }
.lock-title { font-size: 14px; font-weight: 700; color: var(--admin-text); margin: 0; }
.lock-config-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.lock-config-label { font-size: 13px; color: var(--admin-text-muted); }
.lock-min-input { width: 72px; min-height: 36px; padding: 6px 10px; border: 1px solid var(--admin-border); border-radius: 6px; background: var(--admin-bg); color: var(--admin-text); font-size: 14px; text-align: center; }
.btn-save-lock { padding: 7px 14px; background: #C9A84C; color: #0D0D0D; border: none; border-radius: 6px; font-weight: 700; font-size: 13px; cursor: pointer; }
.btn-save-lock:hover { background: #F5D17E; }
.lock-save-msg { font-size: 12px; color: #34d399; }
.lock-filter-row { display: flex; gap: 10px; align-items: center; padding: 12px 20px; flex-wrap: wrap; }

/* ── Cancel / unlock ticket button ── */
.btn-cancel-ticket {
  padding: 5px 12px;
  background: transparent;
  color: #f87171;
  border: 1px solid rgba(248,113,113,0.35);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-cancel-ticket:hover { background: rgba(248,113,113,0.1); }

/* ── Seat chips in booking rows ── */
.seat-chips { display: flex; flex-wrap: wrap; gap: 4px; }
.no-seats { font-size: 12px; color: var(--admin-text-muted); }

.seat-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 28px;
  padding: 0 6px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
  border: 1px solid;
}
/* Matches SeatSelectionPage seat colors */
.chip-thuong {
  background: var(--admin-surface-hover);
  border-color: var(--admin-border);
  color: var(--admin-text);
}
.chip-vip {
  background: rgba(201,168,76,0.18);
  border-color: rgba(201,168,76,0.4);
  color: #C9A84C;
}
.chip-couple {
  background: rgba(236,72,153,0.18);
  border-color: rgba(236,72,153,0.4);
  color: #ec4899;
}

/* ── Seat map (admin seat status view) ── */
.seat-stats {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  padding: 12px 20px;
  font-size: 13px;
  color: var(--admin-text-muted);
  border-bottom: 1px solid var(--admin-divider);
}
.seat-stats strong { color: var(--admin-text); }
.stat-avail  strong { color: var(--admin-text-muted); }
.stat-booked strong { color: #EF4444; }
.stat-locked strong { color: #F59E0B; }

.map-legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
  padding: 10px 20px;
  border-bottom: 1px solid var(--admin-divider);
  font-size: 12px;
  color: var(--admin-text-muted);
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  flex-shrink: 0;
}
.legend-dot--aisle {
  background: transparent;
  border: 1px dashed rgba(148,163,184,.7);
}

/* Force-release button overlay on locked seats */
.sm-seat-label { line-height: 1; }

.sm-release-btn {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #EF4444;
  color: var(--admin-accent);
  border: none;
  font-size: 8px;
  font-weight: 900;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  padding: 0;
  z-index: 5;
}
.sm-release-btn:hover { background: #DC2626; }
</style>

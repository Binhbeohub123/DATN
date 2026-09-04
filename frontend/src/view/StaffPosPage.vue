<template>
  <div class="staff-pos">
    <header class="page-header">
      <router-link to="/staff/dashboard" class="btn-back">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
        Thoát POS
      </router-link>
      <h2>Bán vé tại quầy (POS)</h2>
      <div class="header-actions">
        <router-link v-if="authStore.isAdmin" to="/" class="btn-nav">🏠 Home</router-link>
        <router-link v-if="authStore.isAdmin" to="/admin/dashboard" class="btn-nav">⚙️ Admin Panel</router-link>
        <ThemeToggle />
      </div>
    </header>

    <main class="pos-main">
      <div class="pos-container">

        <!-- STEP 1: Chọn rạp → suất chiếu -->
        <div v-if="step === 1" class="pos-step">
          <h3>1. Chọn Suất Chiếu</h3>

          <!-- Cinema picker — custom dropdown (native <select> popup ignores CSS on Windows Chrome) -->
          <div class="field-group">
            <label>Rạp chiếu *</label>
            <div class="custom-select" :class="{ open: rapDropdownOpen }">
              <button
                class="custom-select__trigger"
                type="button"
                @click="rapDropdownOpen = !rapDropdownOpen"
                @blur="rapDropdownOpen = false"
              >
                <span>{{ selectedRapLabel || '— Chọn rạp —' }}</span>
                <svg class="custom-select__arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
              </button>
              <div class="custom-select__options" v-if="rapDropdownOpen">
                <div
                  class="custom-select__option"
                  :class="{ selected: selectedRapId === '' }"
                  @mousedown="selectRap(''); rapDropdownOpen = false"
                >— Chọn rạp —</div>
                <div
                  v-for="r in raps"
                  :key="r.id"
                  class="custom-select__option"
                  :class="{ selected: selectedRapId === r.id }"
                  @mousedown="selectRap(r.id); rapDropdownOpen = false"
                >{{ r.tenRap }}</div>
              </div>
            </div>
          </div>

          <div v-if="!selectedRapId" class="empty">Hãy chọn rạp chiếu để xem suất chiếu hôm nay.</div>
          <div v-else-if="loadingShowtimes" class="loading">Đang tải suất chiếu...</div>
          <div v-else-if="movies.length === 0" class="empty">Không có suất chiếu nào còn lại hôm nay.</div>

          <div v-else class="movie-list">
            <div v-for="movie in movies" :key="movie.id" class="movie-item">
              <div class="movie-info">
                <h4>{{ movie.title }}</h4>
              </div>
              <div class="showtime-list">
                <button
                  v-for="st in movie.showtimes"
                  :key="st.id"
                  class="btn-showtime"
                  @click="selectShowtime(st)"
                >
                  {{ formatTime(st.thoiGianBatDau) }}
                  <small>{{ formatCurrency(st.giaCoBan) }}</small>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- STEP 2: Chọn ghế — matched to SeatSelectionPage layout -->
        <div v-if="step === 2" class="pos-step">
          <div class="step-header">
            <h3>2. Chọn Ghế</h3>
            <button class="btn-text" @click="step = 1">Thay đổi suất chiếu</button>
          </div>

          <div v-if="loadingSeats" class="loading">Đang tải sơ đồ ghế...</div>
          <div v-else class="seat-map-container">
            <!-- Seat grid — shared SeatGrid component (same layout as the 3 synced pages) -->
            <SeatGrid
              :rows="seatRows"
              :total-cols="totalCols"
              :show-screen="true"
              :seat-size="36"
              :seat-gap="6"
              :row-gap="10"
              :disabled-ids="disabledSeatIds"
              :seat-class-fn="seatClassFn"
              :seat-title-fn="seatTitleFn"
              @seat-click="toggleSeat"
            />

            <!-- Legend -->
            <div class="seat-legend">
              <div class="legend-item"><div class="seat-sample seat-sample--avail"></div><span>Trống</span></div>
              <div class="legend-item"><div class="seat-sample seat-sample--booked"></div><span>Đã đặt</span></div>
              <div class="legend-item"><div class="seat-sample seat-sample--selected"></div><span>Đang chọn</span></div>
              <div class="legend-item"><div class="seat-sample seat-sample--vip"></div><span>VIP</span></div>
              <div class="legend-item"><div class="seat-sample seat-sample--couple"></div><span>Cặp đôi</span></div>
            </div>

            <div class="step-footer">
              <div class="summary">
                <p>Ghế đã chọn: <strong>{{ selectedSeats.map(s => s.hangGhe.trim() + (s.soGheHienThi ?? s.soGhe)).join(', ') || 'Chưa chọn' }}</strong></p>
                <p>Tạm tính: <strong>{{ formatCurrency(totalSeatPrice) }}</strong></p>
              </div>
              <button class="btn-next" :disabled="selectedSeats.length === 0" @click="step = 3">Tiếp tục</button>
            </div>
          </div>
        </div>

        <!-- STEP 3: Combo -->
        <div v-if="step === 3" class="pos-step">
          <div class="step-header">
            <h3>3. Combo &amp; Đồ ăn</h3>
            <button class="btn-text" @click="step = 2">Quay lại chọn ghế</button>
          </div>

          <div v-if="loadingCombos" class="loading">Đang tải...</div>
          <div v-else class="combo-list">
            <div v-for="product in products" :key="product.id" class="combo-item">
              <div class="combo-info">
                <h4>{{ product.tenSanPham }}</h4>
                <p class="price">{{ formatCurrency(product.gia) }}</p>
                <p class="stock" :class="{ 'stock--out': (product.tonKho ?? 0) <= 0 }">
                  {{ (product.tonKho ?? 0) <= 0 ? 'Hết hàng' : `Còn ${product.tonKho}` }}
                </p>
              </div>
              <div class="combo-actions">
                <button @click="updateCombo(product, -1)" :disabled="getComboQty(product.id) === 0">-</button>
                <span>{{ getComboQty(product.id) }}</span>
                <button @click="updateCombo(product, 1)"
                        :disabled="(product.tonKho ?? 0) <= 0 || getComboQty(product.id) >= (product.tonKho ?? 0)">+</button>
              </div>
            </div>

            <div class="step-footer">
              <div class="summary">
                <p>Tiền combo: <strong>{{ formatCurrency(totalComboPrice) }}</strong></p>
              </div>
              <button class="btn-next" @click="step = 4">Thanh toán</button>
            </div>
          </div>
        </div>

        <!-- STEP 4: Thanh toán -->
        <div v-if="step === 4" class="pos-step">
          <div class="step-header">
            <h3>4. Tổng kết &amp; Thanh toán</h3>
            <button class="btn-text" @click="step = 3">Sửa đơn</button>
          </div>

          <div class="checkout-summary">
            <div class="summary-section">
              <h4>Vé xem phim</h4>
              <p>Phim: <strong>{{ selectedShowtime?.phim?.tenPhim }}</strong></p>
              <p>Suất chiếu: <strong>{{ formatDateTime(selectedShowtime?.thoiGianBatDau) }}</strong></p>
              <p>Ghế: <strong>{{ selectedSeats.map(s => s.hangGhe.trim() + (s.soGheHienThi ?? s.soGhe)).join(', ') }}</strong></p>
              <p>Tiền vé: <strong>{{ formatCurrency(totalSeatPrice) }}</strong></p>
            </div>

            <div class="summary-section" v-if="selectedCombos.length > 0">
              <h4>Combo &amp; Đồ ăn</h4>
              <ul>
                <li v-for="c in selectedCombos" :key="c.sanPhamId">
                  {{ c.name }} x{{ c.soLuong }} = {{ formatCurrency(c.price * c.soLuong) }}
                </li>
              </ul>
            </div>

            <div class="summary-section options">
              <label>Email khách hàng (Tùy chọn, hỏi khách để tích điểm):
                <input type="email" v-model="khachHangEmail" placeholder="vd: khach@gmail.com" />
              </label>
            </div>

            <div class="total-section">
              <h3>Tổng cộng: {{ formatCurrency(totalPrice) }}</h3>
              <p>Hình thức: Tiền mặt</p>
            </div>

            <button class="btn-submit" :disabled="submitting" @click="submitBooking">
              {{ submitting ? 'Đang xử lý...' : 'Xác nhận Thanh toán' }}
            </button>
            <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
          </div>
        </div>

        <!-- Popup thành công -->
        <div v-if="successData" class="success-popup">
          <div class="popup-content">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#4ade80" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M8 12l3 3 5-5"/>
            </svg>
            <h2>Đặt vé thành công!</h2>
            <p><strong>Mã vé:</strong> {{ successData.maDatVe }}</p>
            <p><strong>Tổng tiền:</strong> {{ formatCurrency(successData.tongTienThanhToan) }}</p>

            <!-- QR image — same pattern as MyTicketsPage & PaymentResultPage -->
            <div class="pos-qr-box">
              <div v-if="posQrLoading" class="pos-qr-spinner"></div>
              <img
                v-else-if="posQrObjectUrl"
                :src="posQrObjectUrl"
                :alt="`QR vé ${successData.maDatVe}`"
                class="pos-qr-img"
              />
              <div v-else class="pos-qr-fallback">
                <svg viewBox="0 0 9 9" fill="#000" xmlns="http://www.w3.org/2000/svg" class="pos-qr-svg">
                  <rect x="0" y="0" width="4" height="4"/><rect x="1" y="1" width="2" height="2" fill="white"/>
                  <rect x="5" y="0" width="4" height="4"/><rect x="6" y="1" width="2" height="2" fill="white"/>
                  <rect x="0" y="5" width="4" height="4"/><rect x="1" y="6" width="2" height="2" fill="white"/>
                  <rect x="4" y="4" width="1" height="1"/><rect x="5" y="5" width="1" height="1"/>
                  <rect x="7" y="5" width="1" height="1"/><rect x="5" y="7" width="3" height="1"/>
                </svg>
                <p class="pos-qr-code-text">{{ successData.maDatVe }}</p>
              </div>
            </div>
            <p class="pos-qr-hint">Xuất trình mã QR tại cửa ra vào</p>

            <div class="popup-actions">
              <button class="btn-print" @click="printTicket">In vé</button>
              <button class="btn-new" @click="resetPOS">Đơn mới</button>
            </div>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import api from '@/services/api'
import ThemeToggle from '@/components/ThemeToggle.vue'
import SeatGrid from '@/components/SeatGrid.vue'
import { fmtTime12 } from '@/utils/homeHelpers'
import { fmtDateTime12 } from '@/utils/dateFmt'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

const step = ref(1)

// Cinemas
const raps = ref([])
const selectedRapId = ref('')
const rapDropdownOpen = ref(false)

const selectedRapLabel = computed(() =>
  raps.value.find(r => r.id === selectedRapId.value)?.tenRap ?? ''
)

function selectRap(id) {
  selectedRapId.value = id
  onRapChange()
}

// Step 1: Showtimes
const loadingShowtimes = ref(false)
const rawShowtimes = ref([])
const movies = computed(() => {
  const map = new Map()
  rawShowtimes.value.forEach(st => {
    if (!st.phim) return
    if (!map.has(st.phim.id)) {
      map.set(st.phim.id, { id: st.phim.id, title: st.phim.tenPhim, showtimes: [] })
    }
    map.get(st.phim.id).showtimes.push(st)
  })
  return Array.from(map.values())
})
const selectedShowtime = ref(null)

// Step 2: Seats
const loadingSeats = ref(false)
const seats = ref([])
const selectedSeats = ref([])

// Group seats into rows for the row-label layout
const seatRows = computed(() => {
  const map = new Map()
  seats.value.forEach(s => {
    const key = (s.hangGhe || '').trim()
    if (!map.has(key)) map.set(key, [])
    map.get(key).push(s)
  })
  return Array.from(map.entries())
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([label, seatList]) => ({
      label,
      seats: seatList.sort((a, b) => (a.soGhe || 0) - (b.soGhe || 0))
    }))
})

// Physical column width of the room — widest soGhe. SeatGrid keeps each seat at
// its true grid-column (grid-column = soGhe), so filtered-out 'trống' cells leave
// a visible gap instead of shifting neighbours — identical to the synced pages.
const totalCols = computed(() =>
  seats.value.reduce((m, s) => Math.max(m, s.soGhe || 0), 0)
)

// Booked seats are disabled (no lock/WebSocket on POS — staff sells at the counter).
const disabledSeatIds = computed(() =>
  new Set(seats.value.filter(s => s.trangThai === 'booked').map(s => s.id))
)

const seatClassFn = (seat) => {
  if (selectedSeats.value.some(s => s.id === seat.id)) return 'seat--selected'
  if (seat.trangThai === 'booked') return 'seat--booked'
  const t = (seat.loaiGhe || '').toLowerCase()
  if (t === 'vip') return 'seat--vip'
  if (t.includes('cặp') || t.includes('couple')) return 'seat--couple'
  return ''
}

const seatTitleFn = (seat) =>
  `${(seat.hangGhe || '').trim()}${seat.soGheHienThi ?? seat.soGhe} — ${seat.loaiGhe || 'thường'}`

const totalSeatPrice = computed(() =>
  selectedSeats.value.reduce((t, s) => t + (Number(s.giaTien) || 0), 0)
)

// Step 3: Combos
const loadingCombos = ref(false)
const products = ref([])
const selectedCombos = ref([])
const totalComboPrice = computed(() =>
  selectedCombos.value.reduce((t, c) => t + (c.price * c.soLuong), 0)
)

// Step 4
const khachHangEmail = ref('')
const totalPrice = computed(() => totalSeatPrice.value + totalComboPrice.value)
const submitting = ref(false)
const errorMsg = ref('')
const successData = ref(null)
const posQrObjectUrl = ref(null)   // blob URL for the POS success QR image
const posQrLoading  = ref(false)

onMounted(async () => {
  await fetchRaps()
  fetchProducts()
})

async function fetchRaps() {
  try {
    const res = await api.get('/rap-chieu')
    raps.value = res.data || []
  } catch (err) { console.error(err) }
}

function onRapChange() {
  rawShowtimes.value = []
  selectedShowtime.value = null
  selectedSeats.value = []
  if (selectedRapId.value) fetchShowtimes()
}

async function fetchShowtimes() {
  if (!selectedRapId.value) return
  loadingShowtimes.value = true
  try {
    const res = await api.get('/staff/pos/lich-chieu', { params: { rapChieuId: selectedRapId.value } })
    rawShowtimes.value = res.data || []
  } catch (err) { console.error(err) }
  finally { loadingShowtimes.value = false }
}

async function fetchProducts() {
  loadingCombos.value = true
  try {
    const res = await api.get('/san-pham')
    products.value = (res.data || []).filter(p => p.dangHoatDong !== false)
  } catch (err) { console.error(err) }
  finally { loadingCombos.value = false }
}

function selectShowtime(st) {
  selectedShowtime.value = st
  selectedSeats.value = []
  fetchSeats(st.id)
  step.value = 2
}

async function fetchSeats(lichChieuId) {
  loadingSeats.value = true
  try {
    const res = await api.get(`/staff/pos/ghe/${lichChieuId}`)
    seats.value = res.data || []
  } catch (err) { console.error(err) }
  finally { loadingSeats.value = false }
}

function toggleSeat(seat) {
  if (seat.trangThai === 'booked') return
  const idx = selectedSeats.value.findIndex(s => s.id === seat.id)
  if (idx > -1) selectedSeats.value.splice(idx, 1)
  else selectedSeats.value.push(seat)
}

function getComboQty(id) {
  return selectedCombos.value.find(c => c.sanPhamId === id)?.soLuong || 0
}

function updateCombo(product, delta) {
  const stock = product.tonKho ?? 0
  const idx = selectedCombos.value.findIndex(c => c.sanPhamId === product.id)
  const current = idx > -1 ? selectedCombos.value[idx].soLuong : 0
  if (delta > 0 && current >= stock) return
  if (idx > -1) {
    const newQty = selectedCombos.value[idx].soLuong + delta
    if (newQty <= 0) selectedCombos.value.splice(idx, 1)
    else selectedCombos.value[idx].soLuong = newQty
  } else if (delta > 0) {
    selectedCombos.value.push({ sanPhamId: product.id, name: product.tenSanPham, price: product.gia, soLuong: 1 })
  }
}

async function submitBooking() {
  submitting.value = true
  errorMsg.value = ''
  try {
    const payload = {
      lichChieuId: selectedShowtime.value.id,
      gheIds: selectedSeats.value.map(s => s.id),
      comboData: selectedCombos.value.map(c => ({ sanPhamId: c.sanPhamId, soLuong: c.soLuong })),
      khachHangEmail: khachHangEmail.value.trim() || null
    }
    const res = await api.post('/staff/pos/dat-ve', payload)
    successData.value = res.data
    // Fetch the scannable QR image for the new ticket
    if (res.data?.maDatVe) fetchPosQr(res.data.maDatVe)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Có lỗi xảy ra khi tạo đơn'
  } finally { submitting.value = false }
}

function printTicket() { window.print() }

async function fetchPosQr(maDatVe) {
  if (posQrObjectUrl.value) { URL.revokeObjectURL(posQrObjectUrl.value); posQrObjectUrl.value = null }
  posQrLoading.value = true
  try {
    const res = await api.get(`/ve/${maDatVe}/qr`, { responseType: 'blob', headers: { Accept: 'image/png' } })
    posQrObjectUrl.value = URL.createObjectURL(res.data)
  } catch {
    posQrObjectUrl.value = null   // fallback SVG shown instead
  } finally {
    posQrLoading.value = false
  }
}

onUnmounted(() => {
  if (posQrObjectUrl.value) URL.revokeObjectURL(posQrObjectUrl.value)
})

function resetPOS() {
  step.value = 1
  selectedShowtime.value = null
  selectedSeats.value = []
  selectedCombos.value = []
  khachHangEmail.value = ''
  successData.value = null
  if (posQrObjectUrl.value) { URL.revokeObjectURL(posQrObjectUrl.value); posQrObjectUrl.value = null }
  errorMsg.value = ''
  rawShowtimes.value = []
}

function formatCurrency(val) {
  if (val == null) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}
function formatTime(val) {
  if (!val) return ''
  return fmtTime12(val, '')
}
function formatDateTime(val) {
  if (!val) return ''
  return fmtDateTime12(val, '')
}
</script>

<style scoped>
/* ── Page root ── */
.staff-pos {
  min-height: 100vh;
  background: var(--void, #050508);
  color: var(--text-primary, #f1f5f9);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem 2rem;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.btn-back {
  display: flex; align-items: center; gap: 0.5rem;
  color: var(--text-secondary, #94a3b8); text-decoration: none; font-size: 0.9rem;
  transition: color 0.2s; flex-shrink: 0;
}
.btn-back:hover { color: var(--text-primary, #f1f5f9); }
.page-header h2 { margin: 0; font-size: 1.4rem; font-weight: 600; }
.header-actions { display: flex; align-items: center; gap: 0.75rem; margin-left: auto; }
.btn-nav {
  padding: 0.4rem 0.9rem;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.15));
  border-radius: 6px;
  background: transparent;
  color: var(--text-secondary, #94a3b8);
  font-size: 0.82rem;
  text-decoration: none;
  white-space: nowrap;
  transition: all 0.2s;
}
.btn-nav:hover { border-color: var(--electric, #29bcea); color: var(--electric, #29bcea); }

.pos-main { max-width: 900px; margin: 2rem auto; padding: 0 1.5rem; }

.pos-step {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: 12px; padding: 2rem;
}

/* ── Cinema picker ── */
.field-group { display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 1.5rem; }
.field-group label { font-size: 0.85rem; font-weight: 600; color: var(--text-secondary, #94a3b8); text-transform: uppercase; letter-spacing: 0.05em; }

/* ── Custom select (replaces native <select> to prevent white OS popup) ── */
.custom-select { position: relative; width: 100%; }
.custom-select__trigger {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  background: var(--surface-2, #14141f);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.12));
  border-radius: 8px;
  color: var(--text-primary, #f1f5f9);
  font-size: 0.95rem;
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s;
}
.custom-select__trigger:focus,
.custom-select.open .custom-select__trigger {
  outline: none;
  border-color: var(--electric, #29bcea);
}
.custom-select__arrow {
  width: 16px; height: 16px;
  color: var(--text-secondary, #94a3b8);
  transition: transform 0.2s;
  flex-shrink: 0;
}
.custom-select.open .custom-select__arrow { transform: rotate(180deg); }
.custom-select__options {
  position: absolute;
  top: calc(100% + 4px);
  left: 0; right: 0;
  background: var(--surface-2, #14141f);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.12));
  border-radius: 8px;
  overflow-y: auto;
  max-height: 240px;
  z-index: 100;
  box-shadow: 0 8px 24px rgba(0,0,0,0.4);
}
.custom-select__option {
  padding: 0.65rem 1rem;
  color: var(--text-primary, #f1f5f9);
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.15s;
}
.custom-select__option:hover { background: var(--surface-3, #1a1a28); }
.custom-select__option.selected { color: var(--electric, #29bcea); background: var(--surface-3, #1a1a28); }

.step-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;
}
.step-header h3 { margin: 0; }
.btn-text { background: transparent; border: none; color: var(--electric, #29bcea); cursor: pointer; }

/* ── Movie/showtime list ── */
.movie-item {
  margin-bottom: 1.5rem; padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.movie-item h4 { margin: 0 0 1rem; color: var(--electric, #29bcea); }
.showtime-list { display: flex; flex-wrap: wrap; gap: 0.8rem; }
.btn-showtime {
  padding: 0.8rem 1.2rem;
  background: rgba(255,255,255,0.04);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.12));
  border-radius: 8px; color: var(--text-primary, #f1f5f9);
  cursor: pointer; display: flex; flex-direction: column; align-items: center; gap: 0.3rem;
  transition: background 0.2s, border-color 0.2s;
}
.btn-showtime:hover { background: rgba(41,188,234,0.08); border-color: var(--electric, #29bcea); }
.btn-showtime small { color: var(--text-ghost, rgba(241,245,249,0.45)); }

/* ── Screen bar — supplied by shared SeatGrid (show-screen) ── */
.seat-map-container { margin-top: 0.5rem; }

/* ── Legend ── */
.seat-legend {
  display: flex; justify-content: center; gap: 1.5rem;
  margin: 1.5rem 0;
  padding-top: 1rem;
  border-top: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.legend-item { display: flex; align-items: center; gap: 0.5rem; font-size: 12px; color: var(--text-secondary, #94a3b8); }
.seat-sample {
  width: 20px; height: 20px; border-radius: 4px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.seat-sample--avail   { background: var(--surface-3, #1a1a28); }
.seat-sample--booked  { background: #ef4444; }
.seat-sample--selected { background: var(--electric, #29bcea); box-shadow: 0 0 8px rgba(41,188,234,0.30); }
.seat-sample--vip     { background: var(--gold, #C9A84C); box-shadow: 0 0 8px var(--gold-glow, rgba(201,168,76,0.35)); }
.seat-sample--couple  { background: #ec4899; }

/* ── Footer / navigation ── */
.step-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 2rem; padding-top: 1.5rem;
  border-top: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.btn-next, .btn-submit {
  padding: 0.8rem 2rem;
  background: var(--electric, #29bcea);
  color: #fff; border: none; border-radius: 8px;
  font-weight: 600; cursor: pointer; transition: filter 0.2s;
}
.btn-next:hover:not(:disabled), .btn-submit:hover:not(:disabled) { filter: brightness(1.1); }
.btn-next:disabled, .btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

/* ── Combos ── */
.combo-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 1rem;
  background: rgba(255,255,255,0.03);
  margin-bottom: 0.8rem; border-radius: 8px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.06));
}
.combo-info h4 { margin: 0 0 0.3rem; }
.combo-info .price { color: var(--electric, #29bcea); font-weight: 600; margin: 0; }
.combo-info .stock { font-size: 0.8rem; margin: 0.2rem 0 0; color: #34d399; font-weight: 600; }
.combo-info .stock--out { color: #f87171; }
.combo-actions { display: flex; align-items: center; gap: 1rem; }
.combo-actions button:disabled { opacity: 0.4; cursor: not-allowed; }
.combo-actions button {
  width: 32px; height: 32px; border-radius: 6px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.2));
  background: transparent; color: var(--text-primary, #f1f5f9); cursor: pointer;
  transition: background 0.2s;
}
.combo-actions button:hover:not(:disabled) { background: var(--glass-bg-heavy, rgba(255,255,255,0.08)); }

/* ── Payment ── */
.checkout-summary { display: flex; flex-direction: column; gap: 1.5rem; }
.summary-section {
  background: var(--surface-2, #14141f); padding: 1.5rem; border-radius: 8px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.06));
}
.summary-section h4 { margin: 0 0 1rem; color: var(--electric, #29bcea); }
.summary-section p { margin: 0.3rem 0; }
.total-section { text-align: right; margin: 1rem 0; }
.total-section h3 { margin: 0; color: var(--electric, #29bcea); font-size: 1.8rem; }
.options input {
  width: 100%; padding: 0.8rem; margin-top: 0.5rem;
  background: var(--surface-2, #14141f); border: 1px solid var(--glass-border, rgba(255,255,255,0.15));
  color: var(--text-primary, #f1f5f9); border-radius: 6px;
}

/* ── Success popup ── */
.success-popup {
  position: fixed; inset: 0;
  background: var(--overlay, rgba(0,0,0,0.75));
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.popup-content {
  background: var(--surface-2, #14141f); padding: 3rem; border-radius: 12px;
  text-align: center; max-width: 400px; width: 90%;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.popup-content h2 { color: #4ade80; margin: 1rem 0; }

/* ── POS ticket QR ── */
.pos-qr-box {
  background: rgba(255,255,255,0.95);
  border-radius: 10px; padding: 14px;
  width: 180px; height: 180px; margin: 1rem auto 0;
  display: flex; align-items: center; justify-content: center;
}
.pos-qr-img { width: 150px; height: 150px; display: block; }
.pos-qr-spinner {
  width: 36px; height: 36px;
  border: 3px solid rgba(0,0,0,0.1); border-top-color: #29bcea;
  border-radius: 50%; animation: pos-spin 0.8s linear infinite;
}
@keyframes pos-spin { to { transform: rotate(360deg); } }
.pos-qr-fallback { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.pos-qr-svg { width: 130px; height: 130px; }
.pos-qr-code-text {
  font-size: 10px; font-weight: 700; color: #000;
  font-family: monospace; letter-spacing: 1px; margin: 0;
}
.pos-qr-hint { font-size: 12px; color: var(--text-secondary, #94a3b8); margin: 0.5rem 0 1rem; }
.popup-actions { display: flex; gap: 1rem; justify-content: center; margin-top: 2rem; }
.popup-actions button { padding: 0.8rem 1.5rem; border: none; border-radius: 6px; font-weight: 600; cursor: pointer; }
.btn-print { background: #3b82f6; color: #fff; }
.btn-new { background: var(--surface-3, #1a1a28); color: var(--text-primary, #f1f5f9); }

.error-msg { color: #ef4444; margin-top: 1rem; }
.loading, .empty { padding: 2rem; text-align: center; color: var(--text-secondary, #94a3b8); }
</style>

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
    </header>

    <main class="pos-main">
      <div class="pos-container">
        
        <!-- STEP 1: Chọn phim & suất chiếu -->
        <div v-if="step === 1" class="pos-step">
          <h3>1. Chọn Suất Chiếu</h3>
          <div v-if="loadingShowtimes" class="loading">Đang tải suất chiếu...</div>
          <div v-else-if="movies.length === 0" class="empty">Không có suất chiếu nào hôm nay.</div>
          
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

        <!-- STEP 2: Chọn ghế -->
        <div v-if="step === 2" class="pos-step">
          <div class="step-header">
            <h3>2. Chọn Ghế</h3>
            <button class="btn-text" @click="step = 1">Thay đổi suất chiếu</button>
          </div>
          
          <div v-if="loadingSeats" class="loading">Đang tải sơ đồ ghế...</div>
          <div v-else class="seat-map-container">
            <div class="screen-indicator">Màn Hình</div>
            <div class="seat-grid">
              <button 
                v-for="seat in seats" 
                :key="seat.id"
                :class="['seat-btn', seat.trangThai, { selected: selectedSeats.includes(seat) }]"
                :disabled="seat.trangThai === 'booked'"
                @click="toggleSeat(seat)"
              >
                {{ seat.hangGhe }}{{ seat.soGhe }}
              </button>
            </div>
            
            <div class="seat-legend">
              <div class="legend-item"><span class="seat-btn available"></span> Trống</div>
              <div class="legend-item"><span class="seat-btn booked"></span> Đã đặt</div>
              <div class="legend-item"><span class="seat-btn selected"></span> Đang chọn</div>
            </div>

            <div class="step-footer">
              <div class="summary">
                <p>Ghế đã chọn: <strong>{{ selectedSeats.map(s => s.hangGhe + s.soGhe).join(', ') || 'Chưa chọn' }}</strong></p>
                <p>Tạm tính: <strong>{{ formatCurrency(totalSeatPrice) }}</strong></p>
              </div>
              <button class="btn-next" :disabled="selectedSeats.length === 0" @click="step = 3">Tiếp tục</button>
            </div>
          </div>
        </div>

        <!-- STEP 3: Chọn Combo -->
        <div v-if="step === 3" class="pos-step">
          <div class="step-header">
            <h3>3. Combo & Đồ ăn</h3>
            <button class="btn-text" @click="step = 2">Quay lại chọn ghế</button>
          </div>

          <div v-if="loadingCombos" class="loading">Đang tải...</div>
          <div v-else class="combo-list">
            <div v-for="product in products" :key="product.id" class="combo-item">
              <div class="combo-info">
                <h4>{{ product.tenSanPham }}</h4>
                <p class="price">{{ formatCurrency(product.gia) }}</p>
              </div>
              <div class="combo-actions">
                <button @click="updateCombo(product, -1)" :disabled="getComboQty(product.id) === 0">-</button>
                <span>{{ getComboQty(product.id) }}</span>
                <button @click="updateCombo(product, 1)">+</button>
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
            <h3>4. Tổng kết & Thanh toán</h3>
            <button class="btn-text" @click="step = 3">Sửa đơn</button>
          </div>

          <div class="checkout-summary">
            <div class="summary-section">
              <h4>Vé xem phim</h4>
              <p>Phim: <strong>{{ selectedShowtime?.phim?.tenPhim }}</strong></p>
              <p>Suất chiếu: <strong>{{ formatDateTime(selectedShowtime?.thoiGianBatDau) }}</strong></p>
              <p>Ghế: <strong>{{ selectedSeats.map(s => s.hangGhe + s.soGhe).join(', ') }}</strong></p>
              <p>Tiền vé: <strong>{{ formatCurrency(totalSeatPrice) }}</strong></p>
            </div>

            <div class="summary-section" v-if="selectedCombos.length > 0">
              <h4>Combo & Đồ ăn</h4>
              <ul>
                <li v-for="c in selectedCombos" :key="c.sanPhamId">
                  {{ c.name }} x{{ c.soLuong }} = {{ formatCurrency(c.price * c.soLuong) }}
                </li>
              </ul>
            </div>

            <div class="summary-section options">
              <label>ID Khách hàng (Tùy chọn):
                <input type="number" v-model="khachHangId" placeholder="Nhập ID khách hàng" />
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

        <!-- Popup Thành công -->
        <div v-if="successData" class="success-popup">
          <div class="popup-content">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#4ade80" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M8 12l3 3 5-5"/>
            </svg>
            <h2>Đặt vé thành công!</h2>
            <p><strong>Mã vé:</strong> {{ successData.maDatVe }}</p>
            <p><strong>Mã QR:</strong> {{ successData.maQR }}</p>
            <p><strong>Tổng tiền:</strong> {{ formatCurrency(successData.tongTienThanhToan) }}</p>
            
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
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'

const step = ref(1)

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
const totalSeatPrice = computed(() => {
  return selectedSeats.value.reduce((total, seat) => total + (seat.giaTien || 0), 0)
})

// Step 3: Combos
const loadingCombos = ref(false)
const products = ref([])
const selectedCombos = ref([])
const totalComboPrice = computed(() => {
  return selectedCombos.value.reduce((total, c) => total + (c.price * c.soLuong), 0)
})

// Step 4: Payment
const khachHangId = ref('')
const totalPrice = computed(() => totalSeatPrice.value + totalComboPrice.value)
const submitting = ref(false)
const errorMsg = ref('')
const successData = ref(null)

onMounted(() => {
  fetchShowtimes()
  fetchProducts()
})

async function fetchShowtimes() {
  loadingShowtimes.value = true
  try {
    const res = await api.get('/staff/pos/lich-chieu')
    rawShowtimes.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    loadingShowtimes.value = false
  }
}

async function fetchProducts() {
  loadingCombos.value = true
  try {
    const res = await api.get('/san-pham')
    products.value = res.data.filter(p => p.dangHoatDong !== false)
  } catch (err) {
    console.error(err)
  } finally {
    loadingCombos.value = false
  }
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
    seats.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    loadingSeats.value = false
  }
}

function toggleSeat(seat) {
  if (seat.trangThai === 'booked') return
  const idx = selectedSeats.value.findIndex(s => s.id === seat.id)
  if (idx > -1) {
    selectedSeats.value.splice(idx, 1)
  } else {
    selectedSeats.value.push(seat)
  }
}

function getComboQty(id) {
  const item = selectedCombos.value.find(c => c.sanPhamId === id)
  return item ? item.soLuong : 0
}

function updateCombo(product, delta) {
  const idx = selectedCombos.value.findIndex(c => c.sanPhamId === product.id)
  if (idx > -1) {
    const newQty = selectedCombos.value[idx].soLuong + delta
    if (newQty <= 0) {
      selectedCombos.value.splice(idx, 1)
    } else {
      selectedCombos.value[idx].soLuong = newQty
    }
  } else if (delta > 0) {
    selectedCombos.value.push({
      sanPhamId: product.id,
      name: product.tenSanPham,
      price: product.gia,
      soLuong: 1
    })
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
      khachHangId: khachHangId.value ? parseInt(khachHangId.value) : null
    }
    
    const res = await api.post('/staff/pos/dat-ve', payload)
    successData.value = res.data
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Có lỗi xảy ra khi tạo đơn'
  } finally {
    submitting.value = false
  }
}

function printTicket() {
  window.print()
}

function resetPOS() {
  step.value = 1
  selectedShowtime.value = null
  selectedSeats.value = []
  selectedCombos.value = []
  khachHangId.value = ''
  successData.value = null
  errorMsg.value = ''
  fetchShowtimes()
}

// Formatters
function formatCurrency(val) {
  if (val == null) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}
function formatTime(val) {
  if (!val) return ''
  const d = new Date(val)
  return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}
function formatDateTime(val) {
  if (!val) return ''
  const d = new Date(val)
  return d.toLocaleString('vi-VN')
}
</script>

<style scoped>
.staff-pos {
  min-height: 100vh;
  background: #0f1923;
  color: #fff;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 1.5rem 2rem;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.2s;
}
.btn-back:hover { color: #fff; }
.page-header h2 { margin: 0; font-size: 1.4rem; font-weight: 600; }

.pos-main {
  max-width: 900px;
  margin: 2rem auto;
  padding: 0 1.5rem;
}

.pos-step {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 2rem;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.step-header h3 { margin: 0; }
.btn-text {
  background: transparent;
  border: none;
  color: #29bcea;
  cursor: pointer;
}

.movie-item {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.movie-item h4 { margin: 0 0 1rem; color: #e94560; }
.showtime-list { display: flex; flex-wrap: wrap; gap: 0.8rem; }
.btn-showtime {
  padding: 0.8rem 1.2rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  color: #fff;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  transition: background 0.2s;
}
.btn-showtime:hover { background: rgba(255, 255, 255, 0.1); border-color: #e94560; }
.btn-showtime small { color: rgba(255, 255, 255, 0.5); }

/* Seats */
.screen-indicator {
  text-align: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.5rem;
  margin-bottom: 2rem;
  border-radius: 4px;
}
.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(40px, 1fr));
  gap: 8px;
  margin-bottom: 2rem;
}
.seat-btn {
  height: 40px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: #fff;
  cursor: pointer;
  font-size: 0.8rem;
}
.seat-btn.available:hover { background: rgba(255, 255, 255, 0.1); }
.seat-btn.booked { background: #ef4444; border-color: #ef4444; cursor: not-allowed; opacity: 0.5; }
.seat-btn.selected { background: #e94560; border-color: #e94560; }

.seat-legend {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
}
.legend-item { display: flex; align-items: center; gap: 0.5rem; font-size: 0.9rem; }
.legend-item .seat-btn { width: 24px; height: 24px; cursor: default; }

.step-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}
.btn-next, .btn-submit {
  padding: 0.8rem 2rem;
  background: #e94560;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}
.btn-next:disabled, .btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

/* Combos */
.combo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.03);
  margin-bottom: 0.8rem;
  border-radius: 6px;
}
.combo-info h4 { margin: 0 0 0.3rem; }
.combo-info .price { color: #e94560; font-weight: 600; margin: 0; }
.combo-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.combo-actions button {
  width: 32px; height: 32px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: transparent; color: #fff;
  cursor: pointer;
}

/* Payment */
.checkout-summary { display: flex; flex-direction: column; gap: 1.5rem; }
.summary-section { background: rgba(0, 0, 0, 0.2); padding: 1.5rem; border-radius: 8px; }
.summary-section h4 { margin: 0 0 1rem; color: #29bcea; }
.summary-section p { margin: 0.3rem 0; }
.total-section { text-align: right; margin: 1rem 0; }
.total-section h3 { margin: 0; color: #e94560; font-size: 1.8rem; }
.options input {
  width: 100%; padding: 0.8rem; margin-top: 0.5rem;
  background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff; border-radius: 4px;
}

/* Popup */
.success-popup {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.popup-content {
  background: #1f2937; padding: 3rem; border-radius: 12px;
  text-align: center; max-width: 400px; width: 90%;
}
.popup-content h2 { color: #4ade80; margin: 1rem 0; }
.popup-actions { display: flex; gap: 1rem; justify-content: center; margin-top: 2rem; }
.popup-actions button {
  padding: 0.8rem 1.5rem; border: none; border-radius: 6px; font-weight: 600; cursor: pointer;
}
.btn-print { background: #3b82f6; color: #fff; }
.btn-new { background: #4b5563; color: #fff; }

.error-msg { color: #ef4444; margin-top: 1rem; }
</style>
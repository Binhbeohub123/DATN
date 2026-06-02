<template>
  <div class="tickets-page">
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Vé của tôi</span>
      <ThemeToggle />
    </header>

    <!-- Filter tabs -->
    <div class="tabs">
      <button v-for="t in TABS" :key="t.val" :class="['tab', { 'tab--active': activeTab === t.val }]" @click="activeTab = t.val">
        {{ t.label }}
        <span v-if="counts[t.val] > 0" class="tab-count">{{ counts[t.val] }}</span>
      </button>
    </div>

    <!-- Skeleton -->
    <div v-if="loading" class="list-wrap">
      <div v-for="n in 3" :key="n" class="skel-card">
        <div class="skel skel--poster"></div>
        <div class="skel-body">
          <div class="skel skel--line"></div>
          <div class="skel skel--line skel--w70"></div>
          <div class="skel skel--line skel--w50"></div>
        </div>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="state-box">
      <svg class="state-icon state-icon--err" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <p>{{ loadError }}</p>
      <button class="btn-retry" @click="loadTickets">Thử lại</button>
    </div>

    <!-- Empty -->
    <div v-else-if="visible.length === 0" class="state-box">
      <div class="empty-icon">🎟️</div>
      <p class="empty-text">{{ activeTab === 'all' ? 'Bạn chưa có vé nào' : 'Không có vé trong mục này' }}</p>
      <button class="btn-book" @click="router.push('/')">Đặt vé ngay</button>
    </div>

    <!-- Ticket list -->
    <div v-else class="list-wrap">
      <article v-for="tk in visible" :key="tk.id" class="ticket-card" @click="openDetail(tk)">
        <!-- Left tear -->
        <div class="ticket-left">
          <div class="poster-box">
            <img v-if="tk.lichChieu?.phim?.posterUrl" :src="tk.lichChieu.phim.posterUrl" :alt="tk.lichChieu.phim.tenPhim" class="poster-img" />
            <div v-else class="poster-fallback">🎬</div>
          </div>
        </div>
        <!-- Right content -->
        <div class="ticket-right">
          <div class="tk-header">
            <h3 class="tk-title">{{ tk.lichChieu?.phim?.tenPhim || 'Phim' }}</h3>
            <span :class="['status-badge', statusClass(tk)]">{{ statusLabel(tk) }}</span>
          </div>
          <p class="tk-meta">📅 {{ fmtDt(tk.lichChieu?.thoiGianBatDau) }}</p>
          <p class="tk-meta">🏢 {{ tk.lichChieu?.phongChieu?.tenPhong || '-' }}</p>
          <div class="tk-footer">
            <span class="tk-code">{{ tk.maDatVe }}</span>
            <span class="tk-seats">{{ seatList(tk) }}</span>
            <span class="tk-price">{{ fmtPrice(tk.tongTienThanhToan) }}</span>
          </div>
          <!-- Actions -->
          <div class="tk-actions" @click.stop>
            <button v-if="canRepay(tk)" class="btn-pay" @click.stop="repayTicket(tk)" :disabled="repaying === tk.id">
              {{ repaying === tk.id ? 'Đang mở cổng...' : 'Thanh toán lại' }}
            </button>
            <button v-if="tk.trangThai === 'confirmed'" class="btn-qr" @click.stop="openQR(tk)">📱 Xem QR</button>
            <button v-if="canCancel(tk)" class="btn-cancel" @click.stop="cancelTicket(tk)" :disabled="cancelling === tk.id">
              {{ cancelling === tk.id ? '...' : 'Hủy vé' }}
            </button>
          </div>
        </div>
        <!-- Perforation -->
        <div class="ticket-perf"></div>
      </article>
    </div>

    <!-- QR Modal -->
    <transition name="fade">
      <div v-if="qrTicket" class="modal-bg" @click.self="qrTicket = null">
        <div class="qr-modal">
          <button class="modal-close" @click="qrTicket = null">✕</button>
          <h2 class="qr-title">{{ qrTicket.lichChieu?.phim?.tenPhim }}</h2>
          <p class="qr-sub">{{ fmtDt(qrTicket.lichChieu?.thoiGianBatDau) }} · {{ qrTicket.lichChieu?.phongChieu?.tenPhong }}</p>
          <div class="qr-box">
            <div v-if="qrLoading" class="qr-spin-wrap"><div class="spinner"></div></div>
            <img v-else-if="qrSrc" :src="qrSrc" alt="QR vé" class="qr-img" />
            <div v-else class="qr-fallback">
              <svg viewBox="0 0 9 9" fill="#000000" class="qr-svg"><rect x="0" y="0" width="4" height="4"/><rect x="1" y="1" width="2" height="2" fill="white"/><rect x="5" y="0" width="4" height="4"/><rect x="6" y="1" width="2" height="2" fill="white"/><rect x="0" y="5" width="4" height="4"/><rect x="1" y="6" width="2" height="2" fill="white"/><rect x="4" y="4" width="1" height="1"/><rect x="5" y="5" width="1" height="1"/><rect x="7" y="5" width="1" height="1"/><rect x="5" y="7" width="3" height="1"/></svg>
              <p class="qr-code-txt">{{ qrTicket.maDatVe }}</p>
            </div>
          </div>
          <p class="qr-hint">Xuất trình mã QR tại quầy để nhận vé</p>
          <div class="qr-rows">
            <div class="qr-row"><span>Ghế</span><span>{{ seatList(qrTicket) }}</span></div>
            <div class="qr-row"><span>Tổng tiền</span><span class="gold">{{ fmtPrice(qrTicket.tongTienThanhToan) }}</span></div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router    = useRouter()
const authStore = useAuthStore()

const tickets    = ref([])
const loading    = ref(false)
const loadError  = ref('')
const activeTab  = ref('all')
const cancelling = ref(null)
const repaying   = ref(null)
const qrTicket   = ref(null)
const qrSrc      = ref('')
const qrLoading  = ref(false)

const TABS = [
  { val: 'all',       label: 'Tất cả' },
  { val: 'confirmed', label: 'Đã xác nhận' },
  { val: 'pending',   label: 'Chờ thanh toán' },
  { val: 'cancelled', label: 'Đã hủy' },
]

const counts = computed(() => ({
  all:       tickets.value.length,
  confirmed: tickets.value.filter(t => t.trangThai === 'confirmed').length,
  pending:   tickets.value.filter(t => t.trangThai === 'pending').length,
  cancelled: tickets.value.filter(t => t.trangThai === 'cancelled').length,
}))

const visible = computed(() => {
  if (activeTab.value === 'all') return tickets.value
  return tickets.value.filter(t => t.trangThai === activeTab.value)
})

function statusClass(tk) {
  if (tk.trangThai === 'confirmed') return 'badge--green'
  if (tk.trangThai === 'cancelled') return 'badge--red'
  if (tk.trangThaiThanhToan === 'unpaid') return 'badge--yellow'
  return 'badge--gray'
}
function statusLabel(tk) {
  if (tk.trangThai === 'confirmed') return 'Đã xác nhận'
  if (tk.trangThai === 'cancelled') return 'Đã hủy'
  if (tk.trangThaiThanhToan === 'unpaid') return 'Chờ thanh toán'
  return tk.trangThai
}
function canCancel(tk) {
  return tk.trangThai === 'pending' && tk.trangThaiThanhToan === 'unpaid'
}
function canRepay(tk) {
  return tk.trangThai === 'pending' && tk.trangThaiThanhToan === 'unpaid'
}
function seatList(tk) {
  if (!tk.chiTietDatGhe?.length) return '-'
  return tk.chiTietDatGhe.map(c => `${(c.gheNgoi?.hangGhe || '').trim()}${c.gheNgoi?.soGhe}`).sort().join(', ')
}
function fmtDt(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('vi-VN', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' })
}
function fmtPrice(v) {
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(v || 0)
}

function openDetail(tk) {
  if (tk.trangThai === 'confirmed') openQR(tk)
}

async function repayTicket(tk) {
  if (!canRepay(tk) || repaying.value === tk.id) return
  repaying.value = tk.id
  try {
    const payRes = await api.post('/thanh-toan/vnpay', { datVeId: tk.id })
    const url = payRes.data?.paymentUrl
    if (!url) throw new Error('Không tạo được liên kết thanh toán')
    window.location.href = url
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.data || 'Không thể thanh toán lại vé này'
    alert(String(msg))
  } finally {
    repaying.value = null
  }
}

async function openQR(tk) {
  qrTicket.value = tk
  qrSrc.value    = ''
  // Use maQR field directly — it may be a URL or base64
  if (tk.maQR) {
    qrSrc.value = tk.maQR
    return
  }
  // Fetch fresh booking detail to get maQR
  qrLoading.value = true
  try {
    const res = await api.get(`/dat-ve/${tk.id}`)
    qrSrc.value = res.data?.maQR || ''
  } catch { qrSrc.value = '' }
  finally { qrLoading.value = false }
}

async function cancelTicket(tk) {
  if (!confirm(`Hủy vé "${tk.maDatVe}"?\nThao tác này không thể hoàn tác.`)) return
  cancelling.value = tk.id
  try {
    await api.put(`/dat-ve/${tk.id}/cancel`)
    await loadTickets()
  } catch (e) {
    alert(e.response?.data || 'Không thể hủy vé')
  } finally {
    cancelling.value = null
  }
}

async function loadTickets() {
  loading.value  = true
  loadError.value = ''
  try {
    const res = await api.get('/dat-ve')
    tickets.value = (Array.isArray(res.data) ? res.data : [])
      .sort((a, b) => (b.id || 0) - (a.id || 0))
  } catch (e) {
    loadError.value = e.response?.data?.message || 'Không tải được danh sách vé'
  } finally {
    loading.value = false
  }
}

onMounted(loadTickets)
</script>

<style scoped>
.tickets-page {
  background: #ffffff;
  color: #7f7e7f;
  min-height: 100vh;
  padding-bottom: 40px;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #efefef;
  background: #ffffff;
  position: sticky;
  top: 0;
  z-index: 20;
}

.top-bar__title {
  font-size: 16.8px;
  font-weight: 700;
  color: #29bcea;
}

.icon-btn {
  width: 44px;
  height: 44px;
  border-radius: 4px;
  border: 1px solid #efefef;
  background: #f7f7f7;
  color: #29bcea;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn svg { width: 18px; height: 18px; }

.tabs {
  display: flex;
  gap: 8px;
  padding: 16px 20px;
  overflow-x: auto;
  border-bottom: 1px solid #efefef;
}

.tab {
  min-height: 44px;
  padding: 10px 16px;
  border-radius: 4px;
  border: 1px solid #efefef;
  background: #ffffff;
  color: #7f7e7f;
  font-size: 14.4px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab--active {
  background: #29bcea;
  border-color: #29bcea;
  color: #ffffff;
}

.tab-count {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 8px;
  background: #efefef;
  color: #000000;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab--active .tab-count { background: rgba(255, 255, 255, 0.92); }

.list-wrap {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skel-card {
  display: flex;
  gap: 14px;
  background: #f7f7f7;
  border: 1px solid #efefef;
  border-radius: 0;
  padding: 20px;
}

.skel-body { flex: 1; display: flex; flex-direction: column; gap: 10px; }
.skel { background: linear-gradient(90deg, #f0f0f0 25%, #e7e7e7 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; border-radius: 2px; }
.skel--poster { width: 56px; height: 78px; flex-shrink: 0; }
.skel--line { height: 12px; width: 100%; }
.skel--w70 { width: 70%; }
.skel--w50 { width: 50%; }
@keyframes shimmer { to { background-position: -200% 0; } }

.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 340px;
  gap: 16px;
  padding: 40px 20px;
  text-align: center;
}

.state-icon { width: 48px; height: 48px; }
.state-icon--err { color: #29bcea; }
.empty-icon { font-size: 52px; }
.empty-text { color: #7f7e7f; font-size: 14.4px; }

.btn-retry,.btn-book {
  min-height: 44px;
  padding: 12px 28px;
  background: #29bcea;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14.4px;
  font-weight: 700;
  cursor: pointer;
}

.btn-retry:hover,.btn-book:hover { background: #1a9fbd; }

.ticket-card {
  display: flex;
  background: #f7f7f7;
  border: 1px solid #efefef;
  border-radius: 0;
  overflow: hidden;
  cursor: pointer;
  position: relative;
}

.ticket-left {
  flex-shrink: 0;
  width: 76px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 10px;
}

.poster-box {
  width: 56px;
  height: 78px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #efefef;
}

.poster-img { width: 100%; height: 100%; object-fit: cover; }
.poster-fallback { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 22px; }
.ticket-perf { position: absolute; left: 76px; top: 0; bottom: 0; width: 1px; background: #efefef; }

.ticket-right {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tk-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; }
.tk-title { font-size: 16.8px; font-weight: 700; margin: 0; color: #29bcea; line-height: 1.5; }

.status-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
}

.badge--green  { background: #e9f8ef; color: #177245; }
.badge--yellow { background: #f8f3de; color: #7e6517; }
.badge--red    { background: #fdecec; color: #8f2a2a; }
.badge--gray   { background: #efefef; color: #767676; }

.tk-meta   { font-size: 14.4px; color: #7f7e7f; margin: 0; }
.tk-footer { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-top: 4px; }
.tk-code   { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size: 12px; font-weight: 400; color: #000000; background: #ffffff; border: 1px solid #efefef; padding: 4px 8px; border-radius: 2px; }
.tk-seats  { font-size: 12px; color: #767676; }
.tk-price  { margin-left: auto; font-size: 16px; font-weight: 700; color: #29bcea; }
.tk-actions { display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap; }

.btn-pay,.btn-qr,.btn-cancel {
  min-height: 44px;
  padding: 10px 16px;
  border-radius: 4px;
  font-size: 14.4px;
  font-weight: 700;
  cursor: pointer;
  border: 1px solid #29bcea;
  background: transparent;
  color: #29bcea;
}

.btn-pay:hover:not(:disabled), .btn-qr:hover:not(:disabled), .btn-cancel:hover:not(:disabled) {
  background: #29bcea;
  color: #ffffff;
}

.btn-pay:disabled,.btn-cancel:disabled { opacity: .5; cursor: not-allowed; }

.modal-bg {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 20px;
}

.qr-modal {
  background: #ffffff;
  border: 1px solid #efefef;
  border-radius: 0;
  padding: 28px;
  max-width: 420px;
  width: 100%;
  text-align: center;
  position: relative;
}

.modal-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 32px;
  height: 32px;
  background: #f7f7f7;
  border: 1px solid #efefef;
  color: #7f7e7f;
  font-size: 14px;
  cursor: pointer;
}

.qr-title { font-size: 16.8px; font-weight: 700; color: #29bcea; margin: 0 0 4px; }
.qr-sub   { font-size: 14.4px; color: #7f7e7f; margin: 0 0 16px; }
.qr-box   { background: #f7f7f7; border: 1px solid #efefef; border-radius: 0; padding: 20px; display: flex; align-items: center; justify-content: center; min-height: 180px; margin-bottom: 12px; }
.qr-img   { max-width: 160px; max-height: 160px; }
.qr-fallback { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.qr-svg   { width: 140px; height: 140px; }
.qr-code-txt { font-size: 11px; font-weight: 700; color: #000000; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; margin: 0; letter-spacing: 1px; }
.qr-spin-wrap { display: flex; align-items: center; justify-content: center; width: 160px; height: 160px; }
.spinner  { width: 40px; height: 40px; border: 4px solid #dff4fb; border-top-color: #29bcea; border-radius: 50%; animation: spin .9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.qr-hint  { font-size: 12px; color: #767676; margin: 0 0 16px; }
.qr-rows  { text-align: left; }
.qr-row   { display: flex; justify-content: space-between; font-size: 14.4px; padding: 8px 0; border-bottom: 1px solid #efefef; }
.qr-row:last-child { border-bottom: none; }
.gold     { color: #29bcea; font-weight: 700; }

.fade-enter-active,.fade-leave-active { transition: opacity .2s; }
.fade-enter-from,.fade-leave-to { opacity: 0; }

@media (max-width: 767px) {
  .list-wrap { padding: 24px 20px; }
  .ticket-card { flex-direction: column; }
  .ticket-left { width: 100%; justify-content: flex-start; padding: 16px; }
  .ticket-perf { display: none; }
  .tk-footer { align-items: flex-start; }
  .tk-price { margin-left: 0; }
}
</style>

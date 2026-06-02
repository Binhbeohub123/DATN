<template>
  <div class="tickets-page">
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Vé của tôi</span>
      <div style="width:38px"></div>
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
          <p class="tk-meta">🏢 {{ tk.lichChieu?.phongChieu?.tenPhong || '—' }}</p>
          <div class="tk-footer">
            <span class="tk-code">{{ tk.maDatVe }}</span>
            <span class="tk-seats">{{ seatList(tk) }}</span>
            <span class="tk-price">{{ fmtPrice(tk.tongTienThanhToan) }}</span>
          </div>
          <!-- Actions -->
          <div class="tk-actions" @click.stop>
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
              <svg viewBox="0 0 9 9" fill="#0f172a" class="qr-svg"><rect x="0" y="0" width="4" height="4"/><rect x="1" y="1" width="2" height="2" fill="white"/><rect x="5" y="0" width="4" height="4"/><rect x="6" y="1" width="2" height="2" fill="white"/><rect x="0" y="5" width="4" height="4"/><rect x="1" y="6" width="2" height="2" fill="white"/><rect x="4" y="4" width="1" height="1"/><rect x="5" y="5" width="1" height="1"/><rect x="7" y="5" width="1" height="1"/><rect x="5" y="7" width="3" height="1"/></svg>
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

const router    = useRouter()
const authStore = useAuthStore()

const tickets    = ref([])
const loading    = ref(false)
const loadError  = ref('')
const activeTab  = ref('all')
const cancelling = ref(null)
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
function seatList(tk) {
  if (!tk.chiTietDatGhe?.length) return '—'
  return tk.chiTietDatGhe.map(c => `${(c.gheNgoi?.hangGhe || '').trim()}${c.gheNgoi?.soGhe}`).sort().join(', ')
}
function fmtDt(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' })
}
function fmtPrice(v) {
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(v || 0)
}

function openDetail(tk) {
  if (tk.trangThai === 'confirmed') openQR(tk)
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
.tickets-page { background:linear-gradient(160deg,#0b1120 0%,#0f172a 50%,#1a1f35 100%); color:#f1f5f9; min-height:100vh; padding-bottom:40px; }

/* top bar */
.top-bar { display:flex; align-items:center; justify-content:space-between; padding:14px 20px; background:rgba(11,17,32,.9); backdrop-filter:blur(12px); border-bottom:1px solid rgba(255,215,0,.12); position:sticky; top:0; z-index:60; }
.top-bar__title { font-size:16px; font-weight:700; }
.icon-btn { width:38px; height:38px; border-radius:8px; border:1px solid rgba(255,215,0,.25); background:rgba(255,215,0,.07); color:#ffd700; cursor:pointer; display:flex; align-items:center; justify-content:center; transition:background .2s; }
.icon-btn:hover { background:rgba(255,215,0,.15); }
.icon-btn svg { width:18px; height:18px; }

/* tabs */
.tabs { display:flex; gap:6px; padding:14px 20px; overflow-x:auto; border-bottom:1px solid rgba(255,215,0,.08); }
.tab { padding:7px 14px; border-radius:999px; border:1px solid rgba(255,215,0,.2); background:transparent; color:#64748b; font-size:13px; font-weight:700; cursor:pointer; white-space:nowrap; transition:all .2s; display:flex; align-items:center; gap:6px; }
.tab:hover { border-color:#ffd700; color:#ffd700; }
.tab--active { background:#ffd700; color:#0f172a; border-color:#ffd700; }
.tab-count { min-width:18px; height:18px; padding:0 5px; border-radius:999px; background:rgba(15,23,42,.3); font-size:10px; font-weight:900; display:flex; align-items:center; justify-content:center; }
.tab--active .tab-count { background:rgba(15,23,42,.2); }

/* skeleton */
.list-wrap { max-width:720px; margin:0 auto; padding:20px; display:flex; flex-direction:column; gap:14px; }
.skel-card { display:flex; gap:14px; background:rgba(30,41,55,.5); border:1px solid rgba(255,215,0,.07); border-radius:14px; padding:16px; }
.skel-body { flex:1; display:flex; flex-direction:column; gap:10px; }
.skel { background:linear-gradient(90deg,rgba(255,255,255,.05) 25%,rgba(255,255,255,.1) 50%,rgba(255,255,255,.05) 75%); background-size:200% 100%; animation:shimmer 1.4s infinite; border-radius:5px; }
.skel--poster { width:56px; height:78px; border-radius:8px; flex-shrink:0; }
.skel--line { height:13px; width:100%; }
.skel--w70 { width:70%; }
.skel--w50 { width:50%; }
@keyframes shimmer { to { background-position:-200% 0; } }

/* state boxes */
.state-box { display:flex; flex-direction:column; align-items:center; justify-content:center; min-height:380px; gap:16px; padding:40px; text-align:center; }
.state-icon { width:48px; height:48px; }
.state-icon--err { color:#ef4444; }
.empty-icon { font-size:64px; }
.empty-text { color:#64748b; font-size:15px; }
.btn-retry,.btn-book { padding:10px 24px; background:#ffd700; color:#0f172a; border:none; border-radius:8px; font-weight:800; cursor:pointer; transition:all .2s; }
.btn-retry:hover,.btn-book:hover { transform:translateY(-2px); box-shadow:0 8px 20px rgba(255,215,0,.3); }

/* ticket card — torn ticket style */
.ticket-card { display:flex; background:rgba(30,41,55,.6); border:1px solid rgba(255,215,0,.12); border-radius:14px; overflow:hidden; cursor:pointer; transition:border-color .2s, transform .15s; position:relative; }
.ticket-card:hover { border-color:rgba(255,215,0,.35); transform:translateY(-2px); }
.ticket-left { flex-shrink:0; width:76px; background:rgba(15,23,42,.4); display:flex; align-items:center; justify-content:center; padding:14px 10px; }
.poster-box { width:56px; height:78px; border-radius:7px; overflow:hidden; background:rgba(255,215,0,.05); }
.poster-img { width:100%; height:100%; object-fit:cover; }
.poster-fallback { width:100%; height:100%; display:flex; align-items:center; justify-content:center; font-size:22px; }
/* perforation line */
.ticket-perf { position:absolute; left:76px; top:0; bottom:0; width:1px; background:repeating-linear-gradient(to bottom,transparent 0,transparent 4px,rgba(255,215,0,.2) 4px,rgba(255,215,0,.2) 8px); }
.ticket-right { flex:1; padding:14px 16px; display:flex; flex-direction:column; gap:6px; }
.tk-header { display:flex; align-items:flex-start; justify-content:space-between; gap:8px; }
.tk-title { font-size:15px; font-weight:800; margin:0; line-height:1.3; }

/* badges */
.status-badge { padding:3px 9px; border-radius:999px; font-size:10px; font-weight:900; white-space:nowrap; flex-shrink:0; }
.badge--green  { background:rgba(74,222,128,.15); color:#4ade80; }
.badge--yellow { background:rgba(234,179,8,.15); color:#fde047; }
.badge--red    { background:rgba(239,68,68,.15); color:#fca5a5; }
.badge--gray   { background:rgba(148,163,184,.15); color:#94a3b8; }

.tk-meta   { font-size:12px; color:#94a3b8; margin:0; }
.tk-footer { display:flex; align-items:center; gap:10px; flex-wrap:wrap; margin-top:4px; }
.tk-code   { font-family:monospace; font-size:11px; font-weight:800; color:#ffd700; background:rgba(255,215,0,.1); padding:2px 7px; border-radius:4px; }
.tk-seats  { font-size:11px; color:#94a3b8; }
.tk-price  { margin-left:auto; font-size:14px; font-weight:900; color:#ffd700; }
.tk-actions { display:flex; gap:8px; margin-top:4px; }
.btn-qr,.btn-cancel { padding:6px 14px; border-radius:7px; font-size:12px; font-weight:700; cursor:pointer; border:none; transition:all .2s; }
.btn-qr { background:rgba(255,215,0,.1); color:#ffd700; border:1px solid rgba(255,215,0,.25); }
.btn-qr:hover { background:rgba(255,215,0,.2); }
.btn-cancel { background:rgba(239,68,68,.1); color:#fca5a5; border:1px solid rgba(239,68,68,.2); }
.btn-cancel:hover:not(:disabled) { background:rgba(239,68,68,.2); }
.btn-cancel:disabled { opacity:.5; cursor:not-allowed; }

/* QR modal */
.modal-bg { position:fixed; inset:0; background:rgba(0,0,0,.75); display:flex; align-items:center; justify-content:center; z-index:200; padding:20px; }
.qr-modal { background:#0f172a; border:1px solid rgba(255,215,0,.25); border-radius:18px; padding:28px; max-width:360px; width:100%; text-align:center; position:relative; }
.modal-close { position:absolute; top:14px; right:14px; width:30px; height:30px; background:rgba(255,255,255,.07); border:none; border-radius:50%; color:#94a3b8; font-size:14px; cursor:pointer; }
.modal-close:hover { background:rgba(255,255,255,.14); color:#f1f5f9; }
.qr-title { font-size:15px; font-weight:800; color:#ffd700; margin:0 0 4px; }
.qr-sub   { font-size:12px; color:#64748b; margin:0 0 16px; }
.qr-box   { background:white; border-radius:12px; padding:20px; display:flex; align-items:center; justify-content:center; min-height:180px; margin-bottom:12px; }
.qr-img   { max-width:160px; max-height:160px; }
.qr-fallback { display:flex; flex-direction:column; align-items:center; gap:8px; }
.qr-svg   { width:140px; height:140px; }
.qr-code-txt { font-size:11px; font-weight:800; color:#0f172a; font-family:monospace; margin:0; letter-spacing:1px; }
.qr-spin-wrap { display:flex; align-items:center; justify-content:center; width:160px; height:160px; }
.spinner  { width:40px; height:40px; border:4px solid rgba(255,215,0,.2); border-top-color:#ffd700; border-radius:50%; animation:spin .9s linear infinite; }
@keyframes spin { to { transform:rotate(360deg); } }
.qr-hint  { font-size:11px; color:#64748b; margin:0 0 16px; }
.qr-rows  { text-align:left; }
.qr-row   { display:flex; justify-content:space-between; font-size:13px; padding:6px 0; border-bottom:1px solid rgba(255,215,0,.07); }
.qr-row:last-child { border-bottom:none; }
.gold     { color:#ffd700; font-weight:800; }

.fade-enter-active,.fade-leave-active { transition:opacity .2s; }
.fade-enter-from,.fade-leave-to { opacity:0; }
</style>

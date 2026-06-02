<template>
  <div class="dashboard">
    <!-- Toast -->
    <transition name="toast"><div v-if="toast.show" :class="['toast',`toast--${toast.type}`]">{{ toast.msg }}</div></transition>

    <!-- KPI cards -->
    <div class="kpi-grid">
      <div v-for="k in kpis" :key="k.label" class="kpi-card">
        <div class="kpi-left">
          <div class="kpi-icon">{{ k.icon }}</div>
          <div :class="['kpi-trend', k.up ? 'up' : 'down']" v-if="k.trend != null">
            {{ k.up ? '▲' : '▼' }} {{ k.trend }}
          </div>
        </div>
        <div class="kpi-body">
          <div class="kpi-value">{{ k.value }}</div>
          <div class="kpi-label">{{ k.label }}</div>
        </div>
      </div>
    </div>

    <!-- Revenue line chart (SVG) + Top movies -->
    <div class="two-col">
      <div class="card chart-card">
        <div class="card-head">
          <h3>Doanh Thu 30 Ngày</h3>
          <div class="period-btns">
            <button :class="['pbtn', period===7?'pbtn--active':'']"  @click="period=7;loadRevenue()">7N</button>
            <button :class="['pbtn', period===30?'pbtn--active':'']" @click="period=30;loadRevenue()">30N</button>
          </div>
        </div>
        <div v-if="loadingRev" class="chart-placeholder"><div class="spinner"></div></div>
        <div v-else-if="revData.length===0" class="empty-text">Chưa có dữ liệu doanh thu</div>
        <svg v-else class="line-chart" :viewBox="`0 0 ${SVG_W} ${SVG_H}`" preserveAspectRatio="none">
          <defs>
            <linearGradient id="revGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#ff6b00" stop-opacity="0.4"/>
              <stop offset="100%" stop-color="#ff6b00" stop-opacity="0"/>
            </linearGradient>
          </defs>
          <!-- grid lines -->
          <line v-for="y in yLines" :key="y" :x1="PAD" :y1="y" :x2="SVG_W-PAD/2" :y2="y" stroke="rgba(17,24,39,0.12)" stroke-width="1"/>
          <!-- area fill -->
          <path :d="areaPath" fill="url(#revGrad)"/>
          <!-- line -->
          <path :d="linePath" fill="none" stroke="#ff6b00" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          <!-- dots -->
          <circle v-for="(p,i) in chartPts" :key="i" :cx="p.x" :cy="p.y" r="4" fill="#ff6b00" stroke="white" stroke-width="2"/>
          <!-- x labels -->
          <text v-for="(p,i) in chartPts.filter((_,i)=>i%(Math.ceil(chartPts.length/6))===0)" :key="'l'+i" :x="p.x" :y="SVG_H-4" text-anchor="middle" font-size="10" fill="#9ca3af">{{ fmtDateShort(p.label) }}</text>
        </svg>
      </div>

      <div class="card">
        <h3>Top 5 Phim Doanh Thu</h3>
        <div v-if="loadingTop" class="loading-text">Đang tải...</div>
        <div v-else-if="topMovies.length===0" class="empty-text">Chưa có dữ liệu</div>
        <table v-else class="top-table">
          <thead><tr><th>#</th><th>Phim</th><th>Vé</th><th>Doanh thu</th></tr></thead>
          <tbody>
            <tr v-for="(m,i) in topMovies" :key="m.id">
              <td><span class="rank" :class="i<3?`rank--${i+1}`:''">{{i+1}}</span></td>
              <td class="td-movie">{{ m.tenPhim }}</td>
              <td>{{ m.tickets }}</td>
              <td class="td-rev">{{ fmtPrice(m.revenue) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Recent bookings -->
    <div class="card">
      <div class="card-head"><h3>Đặt Vé Gần Đây</h3><span class="badge-count">{{ recentBookings.length }}</span></div>
      <div v-if="loadingBook" class="loading-text">Đang tải...</div>
      <div v-else-if="recentBookings.length===0" class="empty-text">Chưa có đặt vé nào</div>
      <table v-else class="data-table">
        <thead><tr><th>Mã</th><th>Khách hàng</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th></tr></thead>
        <tbody>
          <tr v-for="bk in recentBookings" :key="bk.id">
            <td class="mono">{{ bk.maDatVe }}</td>
            <td>{{ bk.nguoiDung?.hoTen || bk.nguoiDung?.email || '—' }}</td>
            <td class="td-rev">{{ fmtPrice(bk.tongTienThanhToan) }}</td>
            <td>{{ bk.trangThaiThanhToan }}</td>
            <td><span :class="['sbadge', bk.trangThai==='confirmed'?'sbadge--green':bk.trangThai==='cancelled'?'sbadge--red':'sbadge--yellow']">{{ bk.trangThai }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import api from '@/services/api'

const SVG_W = 600, SVG_H = 180, PAD = 40

const toast = reactive({ show:false, msg:'', type:'success' })
let toastTimer = null
function showToast(msg, type='error') {
  clearTimeout(toastTimer); toast.msg=msg; toast.type=type; toast.show=true
  toastTimer = setTimeout(() => toast.show=false, 3500)
}

const stats = ref({})
const revData = ref([])
const topMovies = ref([])
const recentBookings = ref([])
const period = ref(30)
const loadingRev = ref(false), loadingBook = ref(false), loadingTop = ref(false)

const kpis = computed(() => [
  { icon:'💰', label:'Doanh thu hôm nay', value: fmtPrice(stats.value.todayRevenue), trend:null },
  { icon:'🎟️', label:'Vé bán hôm nay',   value: stats.value.todayTickets ?? '—', trend:null },
  { icon:'👥', label:'Tổng người dùng',   value: stats.value.totalUsers ?? '—', trend:null },
  { icon:'🎬', label:'Phim đang chiếu',   value: stats.value.totalMovies ?? '—', trend:null },
])

// SVG chart
const yLines = computed(() => {
  if (!chartPts.value.length) return []
  return [1,2,3].map(i => PAD + ((SVG_H-PAD*2)*i/3))
})

const chartPts = computed(() => {
  if (!revData.value.length) return []
  const vals = revData.value.map(d => Number(d.doanhThu)||0)
  const max = Math.max(...vals,1)
  const step = (SVG_W - PAD*2) / Math.max(vals.length-1, 1)
  return revData.value.map((d,i) => ({
    x: PAD + i * step,
    y: PAD + (1 - vals[i]/max) * (SVG_H - PAD*2),
    label: d.ngay,
    val: vals[i],
  }))
})

const linePath = computed(() => {
  const pts = chartPts.value
  if (!pts.length) return ''
  return pts.map((p,i) => `${i===0?'M':'L'}${p.x},${p.y}`).join(' ')
})
const areaPath = computed(() => {
  const pts = chartPts.value; if (!pts.length) return ''
  const bottom = SVG_H - PAD
  return linePath.value + ` L${pts.at(-1).x},${bottom} L${pts[0].x},${bottom} Z`
})

function fmtPrice(v) {
  if (v == null || v === undefined) return '—'
  return new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(v)
}
function fmtDateShort(s) {
  if (!s) return ''
  const p = String(s).split('-'); return p.length>=3 ? `${p[2]}/${p[1]}` : s
}

async function loadStats() {
  try { const r = await api.get('/admin/stats'); stats.value = r.data }
  catch { showToast('Không tải được thống kê') }
}

async function loadRevenue() {
  loadingRev.value = true
  try {
    const to = new Date().toISOString().slice(0,10)
    const from = new Date(Date.now()-period.value*86400000).toISOString().slice(0,10)
    const r = await api.get('/admin/doanh-thu', { params:{from,to} })
    revData.value = Array.isArray(r.data) ? r.data : []
  } catch { showToast('Không tải được dữ liệu doanh thu') }
  finally { loadingRev.value = false }
}

async function loadTopMovies() {
  loadingTop.value = true
  try {
    const [movRes, lcRes] = await Promise.all([
      api.get('/admin/phim'),
      api.get('/admin/dat-ve?page=0&size=200')
    ])
    const movies = movRes.data || []
    const bookings = lcRes.data?.content || []
    // aggregate tickets & revenue per phimId
    const map = {}
    bookings.forEach(bk => {
      if (bk.trangThaiThanhToan !== 'paid') return
      const id = bk.lichChieu?.phim?.id
      if (!id) return
      if (!map[id]) map[id] = { id, tenPhim: bk.lichChieu.phim.tenPhim, tickets:0, revenue:0 }
      map[id].tickets += (bk.chiTietDatGhe?.length || 0)
      map[id].revenue += Number(bk.tongTienThanhToan)||0
    })
    topMovies.value = Object.values(map).sort((a,b) => b.revenue-a.revenue).slice(0,5)
  } catch { showToast('Không tải được top phim') }
  finally { loadingTop.value = false }
}

async function loadBookings() {
  loadingBook.value = true
  try {
    const r = await api.get('/admin/dat-ve?page=0&size=10')
    recentBookings.value = r.data?.content || []
  } catch { showToast('Không tải được đặt vé') }
  finally { loadingBook.value = false }
}

onMounted(() => { loadStats(); loadRevenue(); loadTopMovies(); loadBookings() })
</script>

<style scoped>
.dashboard { display:flex; flex-direction:column; gap:24px; }
.toast { position:fixed; top:20px; right:20px; z-index:999; padding:12px 20px; border-radius:10px; font-size:13px; font-weight:700; }
.toast--error   { background:#7f1d1d; color:#fecaca; }
.toast--success { background:#14532d; color:#bbf7d0; }
.toast-enter-active,.toast-leave-active{transition:opacity .3s}
.toast-enter-from,.toast-leave-to{opacity:0}

.kpi-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(200px,1fr)); gap:16px; }
.kpi-card { display:flex; align-items:center; justify-content:space-between; gap:12px; padding:20px; border-radius:18px; background:rgba(255,255,255,0.84); border:1px solid rgba(255,255,255,0.7); box-shadow:0 8px 24px rgba(15,23,42,0.07); }
.kpi-icon { font-size:32px; }
.kpi-value { font-size:20px; font-weight:900; color:#111827; }
.kpi-label { font-size:12px; color:#6b7280; font-weight:700; margin-top:2px; }
.kpi-trend { font-size:11px; font-weight:700; }
.kpi-trend.up { color:#16a34a; } .kpi-trend.down { color:#dc2626; }

.two-col { display:grid; grid-template-columns:1fr 360px; gap:20px; }
.card { padding:20px; border-radius:18px; background:rgba(255,255,255,0.84); border:1px solid rgba(255,255,255,0.7); box-shadow:0 8px 24px rgba(15,23,42,0.07); }
.card-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.card-head h3,.card > h3 { font-size:15px; font-weight:800; color:#111827; margin:0 0 16px; }
.period-btns { display:flex; gap:4px; }
.pbtn { padding:4px 12px; border-radius:6px; border:1px solid #e5e7eb; background:white; font-size:12px; font-weight:700; cursor:pointer; }
.pbtn--active { background:#ff6b00; color:white; border-color:#ff6b00; }
.badge-count { background:#f3f4f6; padding:2px 8px; border-radius:999px; font-size:12px; font-weight:800; color:#6b7280; }

.chart-card { padding:20px; }
.chart-placeholder { height:180px; display:flex; align-items:center; justify-content:center; }
.spinner { width:36px; height:36px; border:4px solid rgba(255,107,0,.2); border-top-color:#ff6b00; border-radius:50%; animation:spin .9s linear infinite; }
@keyframes spin { to{transform:rotate(360deg)} }
.line-chart { width:100%; height:180px; overflow:visible; }

.top-table,.data-table { width:100%; border-collapse:collapse; font-size:13px; }
.top-table th,.data-table th { padding:8px 10px; text-align:left; font-size:11px; font-weight:800; color:#6b7280; text-transform:uppercase; border-bottom:2px solid #f3f4f6; }
.top-table td,.data-table td { padding:10px; border-bottom:1px solid #f3f4f6; }
.top-table tr:last-child td,.data-table tr:last-child td { border-bottom:none; }
.top-table tr:hover td,.data-table tr:hover td { background:#fff7ed; }
.td-movie { font-weight:700; max-width:160px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.td-rev { font-weight:800; color:#ff6b00; white-space:nowrap; }
.rank { display:inline-flex; width:22px; height:22px; border-radius:50%; align-items:center; justify-content:center; font-size:11px; font-weight:900; background:#f3f4f6; color:#6b7280; }
.rank--1 { background:#ffd700; color:#92400e; }
.rank--2 { background:#e2e8f0; color:#475569; }
.rank--3 { background:#fed7aa; color:#9a3412; }
.mono { font-family:monospace; font-size:12px; font-weight:700; color:#ff6b00; }
.sbadge { padding:3px 8px; border-radius:999px; font-size:10px; font-weight:800; }
.sbadge--green  { background:#dcfce7; color:#166534; }
.sbadge--yellow { background:#fef9c3; color:#854d0e; }
.sbadge--red    { background:#fee2e2; color:#991b1b; }
.loading-text,.empty-text { text-align:center; padding:24px; color:#9ca3af; font-size:13px; }

@media(max-width:900px) { .two-col { grid-template-columns:1fr; } }
</style>

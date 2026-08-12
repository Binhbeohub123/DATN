<template>
  <div class="db-page">
    <!-- Toast -->
    <transition name="toast">
      <div v-if="toast.show" :class="['db-toast', `db-toast--${toast.type}`]">{{ toast.msg }}</div>
    </transition>

    <!-- KPI row -->
    <div class="db-kpi-row reveal is-visible">
      <div v-for="k in kpis" :key="k.label" class="db-kpi glass-card">
        <div class="db-kpi__icon">
          <!-- Revenue -->
          <svg v-if="k.label.includes('thu')" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
          <!-- Tickets -->
          <svg v-else-if="k.label.includes('Vé')" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v1a2 2 0 0 0 0 4v1a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2v-1a2 2 0 0 0 0-4V9z"/><line x1="9" y1="12" x2="9.01" y2="12"/><line x1="13" y1="12" x2="15" y2="12"/></svg>
          <!-- Users -->
          <svg v-else-if="k.label.includes('người')" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="7" r="4"/><path d="M3 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/><path d="M21 21v-2a4 4 0 0 0-3-3.85"/></svg>
          <!-- Movies -->
          <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M7 4v16M17 4v16M2 9h5M17 9h5M2 15h5M17 15h5"/></svg>
        </div>
        <div class="db-kpi__body">
          <div class="db-kpi__value">{{ k.value }}</div>
          <div class="db-kpi__label">{{ k.label }}</div>
          <span v-if="k.trend != null" :class="['db-kpi__trend', k.up ? 'db-kpi__trend--up' : 'db-kpi__trend--down']">
            {{ k.up ? '▲' : '▼' }} {{ k.trend }}
          </span>
        </div>
      </div>
    </div>

    <!-- Chart + Top movies -->
    <div class="db-two-col">
      <!-- Revenue chart -->
      <div class="db-chart-card glass-card">
        <div class="db-card-head">
          <h3 class="db-section-title">Doanh Thu</h3>
          <div class="db-period-btns">
            <button :class="['db-pbtn', period===7 ? 'db-pbtn--active' : '']" @click="period=7; loadRevenue()">7N</button>
            <button :class="['db-pbtn', period===30 ? 'db-pbtn--active' : '']" @click="period=30; loadRevenue()">30N</button>
          </div>
        </div>
        <div v-if="loadingRev" class="db-chart-placeholder"><div class="db-spinner"></div></div>
        <div v-else-if="revData.length === 0" class="db-empty">Chưa có dữ liệu doanh thu</div>
        <svg v-else class="db-line-chart" :viewBox="`0 0 ${SVG_W} ${SVG_H}`" preserveAspectRatio="none">
          <defs>
            <linearGradient id="dbRevGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" :stop-color="chartColors.accent" stop-opacity="0.28"/>
              <stop offset="100%" :stop-color="chartColors.accent" stop-opacity="0"/>
            </linearGradient>
          </defs>
          <line v-for="y in yLines" :key="y" :x1="PAD" :y1="y" :x2="SVG_W - PAD/2" :y2="y" :stroke="chartColors.accentMuted" stroke-width="1"/>
          <path :d="areaPath" fill="url(#dbRevGrad)"/>
          <path :d="linePath" fill="none" :stroke="chartColors.accent" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle v-for="(p, i) in chartPts" :key="i" :cx="p.x" :cy="p.y" r="3" :fill="chartColors.accent" stroke="#0a0a0f" stroke-width="2"/>
          <text
            v-for="(p, i) in chartPts.filter((_, i) => i % (Math.ceil(chartPts.length / 6)) === 0)"
            :key="'l' + i"
            :x="p.x" :y="SVG_H - 4"
            text-anchor="middle" font-size="10" :fill="chartColors.muted"
          >{{ fmtDateShort(p.label) }}</text>
        </svg>
      </div>

      <!-- Top 5 movies -->
      <div class="db-top-card glass-card">
        <h3 class="db-section-title">Top 5 Phim Doanh Thu</h3>
        <div v-if="loadingTop" class="db-loading">Đang tải...</div>
        <div v-else-if="topMovies.length === 0" class="db-empty">Chưa có dữ liệu</div>
        <div v-else class="db-table-wrap">
          <table class="db-table">
            <thead>
              <tr>
                <th>#</th><th>Phim</th><th>Vé</th><th>Doanh thu</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(m, i) in topMovies" :key="m.id">
                <td><span class="db-rank" :class="i < 3 ? `db-rank--${i+1}` : ''">{{ i + 1 }}</span></td>
                <td class="db-td-movie">{{ m.tenPhim }}</td>
                <td>{{ m.tickets }}</td>
                <td class="db-td-rev">{{ fmtPrice(m.revenue) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Recent bookings -->
    <div class="db-bookings-card glass-card reveal is-visible">
      <div class="db-card-head">
        <h3 class="db-section-title">Đặt Vé Gần Đây</h3>
        <span class="db-count-badge">{{ recentBookings.length }}</span>
      </div>
      <div v-if="loadingBook" class="db-loading">Đang tải...</div>
      <div v-else-if="recentBookings.length === 0" class="db-empty">Chưa có đặt vé nào</div>
      <div v-else class="db-table-wrap">
        <table class="db-table">
          <thead>
            <tr>
              <th>Mã</th><th>Khách hàng</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="bk in recentBookings" :key="bk.id">
              <td class="db-td-mono">{{ bk.maDatVe }}</td>
              <td>{{ bk.nguoiDung?.hoTen || bk.nguoiDung?.email || '—' }}</td>
              <td class="db-td-rev">{{ fmtPrice(bk.tongTienThanhToan) }}</td>
              <td>{{ bk.trangThaiThanhToan }}</td>
              <td>
                <span :class="[
                  'db-badge',
                  bk.trangThai === 'confirmed' ? 'db-badge--green' :
                  bk.trangThai === 'cancelled' ? 'db-badge--red' : 'db-badge--yellow'
                ]">{{ bk.trangThai }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import api from '@/services/api'

const chartColors = computed(() => {
  const style = getComputedStyle(document.documentElement)
  return {
    accent:      style.getPropertyValue('--admin-accent').trim()      || '#FFFFFF',
    accentMuted: style.getPropertyValue('--admin-accent-muted').trim() || 'rgba(255,255,255,0.07)',
    muted:       style.getPropertyValue('--admin-text-muted').trim()  || '#9CA3AF',
    surface:     style.getPropertyValue('--admin-surface').trim()     || '#111827',
  }
})

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
    // Use local-date arithmetic — toISOString() converts to UTC and gives wrong
    // date string in timezones ahead of UTC (e.g. UTC+7 between 00:00–07:00 local).
    const now  = new Date()
    const to   = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')}`
    const past = new Date(now.getFullYear(), now.getMonth(), now.getDate() - period.value)
    const from = `${past.getFullYear()}-${String(past.getMonth()+1).padStart(2,'0')}-${String(past.getDate()).padStart(2,'0')}`
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
/* ── Page wrapper ── */
.db-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 100%;
  background: var(--admin-bg);
  color: var(--admin-text);
  font-family: var(--font-ui);
}

/* ── Toast ── */
.toast-enter-active, .toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; }
.db-toast {
  position: fixed;
  top: 20px; right: 20px;
  z-index: 9999;
  padding: 12px 20px;
  border-radius: 10px;
  font-family: var(--font-ui);
  font-size: 13px;
  font-weight: 600;
  border: 1px solid var(--glass-border);
  backdrop-filter: var(--glass-blur);
}
.db-toast--error   { background: rgba(127,29,29,0.92); color: #fecaca; border-color: #ef4444; }
.db-toast--success { background: rgba(20,83,45,0.92);  color: #bbf7d0; border-color: #22c55e; }

/* ── KPI row ── */
.db-kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 1100px) { .db-kpi-row { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px)  { .db-kpi-row { grid-template-columns: 1fr; } }

.db-kpi {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-left: 3px solid var(--admin-accent);
  transition: box-shadow 200ms var(--ease-out);
}
.db-kpi:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.04); border-color: rgba(255,255,255,0.20); }

.db-kpi__icon {
  flex-shrink: 0;
  color: var(--admin-accent);
}

.db-kpi__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.db-kpi__value {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.db-kpi__label {
  font-family: var(--font-ui);
  font-size: 13px;
  color: var(--text-secondary);
}

.db-kpi__trend {
  font-family: var(--font-ui);
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: var(--radius-pill);
  display: inline-block;
  width: fit-content;
}
.db-kpi__trend--up   { background: rgba(16,185,129,0.12);  color: #10B981; }
.db-kpi__trend--down { background: rgba(239,68,68,0.12);  color: #EF4444; }

/* ── Two-column layout ── */
.db-two-col {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
}
@media (max-width: 960px) { .db-two-col { grid-template-columns: 1fr; } }

/* ── Card common ── */
.db-chart-card,
.db-top-card,
.db-bookings-card {
  padding: 20px 24px;
  overflow: hidden;
}

.db-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 12px;
}

/* ── Section title ── */
.db-section-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: var(--admin-accent);
  margin: 0 0 16px;
  padding-left: 12px;
  border-left: 3px solid var(--admin-accent);
  line-height: 1.3;
}
.db-card-head .db-section-title { margin-bottom: 0; }

/* ── Period buttons ── */
.db-period-btns { display: flex; gap: 4px; }
.db-pbtn {
  padding: 5px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-secondary);
  font-family: var(--font-ui);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 200ms var(--ease-out);
}
.db-pbtn:hover       { color: var(--text-primary); border-color: var(--glass-border); }
.db-pbtn--active     { background: var(--admin-accent-muted); color: var(--admin-accent); border-color: var(--admin-accent); }

/* ── Chart ── */
.db-line-chart {
  display: block;
  width: 100%;
  height: 180px;
  overflow: visible;
}
.db-chart-placeholder {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.db-spinner {
  width: 32px; height: 32px;
  border: 3px solid var(--glass-border);
  border-top-color: var(--admin-accent);
  border-radius: 50%;
  animation: db-spin 0.8s linear infinite;
}
@keyframes db-spin { to { transform: rotate(360deg); } }

/* ── Table pattern ── */
.db-table-wrap { overflow-x: auto; }

.db-table {
  width: 100%;
  border-collapse: collapse;
}

.db-table thead tr {
  background: var(--admin-bg);
}

.db-table th {
  padding: 12px 16px;
  text-align: left;
  font-family: var(--font-ui);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-ghost);
  border-bottom: 1px solid var(--glass-border);
  white-space: nowrap;
}

.db-table tbody tr {
  border-bottom: 1px solid var(--glass-border);
  transition: background 150ms var(--ease-out);
}
.db-table tbody tr:last-child { border-bottom: none; }
.db-table tbody tr:hover { background: var(--admin-surface-hover); }

.db-table td {
  padding: 14px 16px;
  font-family: var(--font-ui);
  font-size: 14px;
  color: var(--text-primary);
  vertical-align: middle;
}

/* ── Table cell helpers ── */
.db-td-movie {
  max-width: 160px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 600;
}
.db-td-rev  { font-weight: 700; color: var(--admin-text); }
.db-td-mono { font-family: 'Courier New', monospace; font-size: 13px; color: var(--admin-text); }

/* ── Rank badges ── */
.db-rank {
  display: inline-flex;
  width: 22px; height: 22px;
  border-radius: 50%;
  align-items: center;
  justify-content: center;
  font-family: var(--font-ui);
  font-size: 11px;
  font-weight: 700;
  background: var(--glass-bg);
  color: var(--text-secondary);
}
.db-rank--1 { background: var(--admin-accent); color: var(--admin-bg); }
.db-rank--2 { background: var(--glass-bg-heavy);   color: var(--text-primary); }
.db-rank--3 { background: rgba(245,158,11,0.2);    color: #f59e0b; }

/* ── Status badges ── */
.db-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: var(--radius-pill);
  font-family: var(--font-ui);
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
.db-badge--green  { background: rgba(16,185,129,0.15); color: #10B981; }
.db-badge--red    { background: rgba(239,68,68,0.15);  color: #EF4444; }
.db-badge--yellow { background: rgba(245,158,11,0.15); color: #F59E0B; }

/* ── Count badge ── */
.db-count-badge {
  padding: 3px 10px;
  border-radius: var(--radius-pill);
  background: var(--admin-accent-muted);
  color: var(--admin-text);
  font-family: var(--font-ui);
  font-size: 12px;
  font-weight: 600;
}

/* ── Empty / loading ── */
.db-empty,
.db-loading {
  padding: 32px;
  text-align: center;
  font-family: var(--font-ui);
  font-size: 14px;
  color: var(--text-secondary);
}
</style>

<template>
  <div class="history-page">
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Lịch sử giao dịch</span>
      <div style="width:38px"></div>
    </header>

    <!-- Summary card (always visible) -->
    <div class="summary-bar">
      <div class="sum-item">
        <span class="sum-val">{{ fmtPrice(authStore.user?.tongTienDaChi || 0) }}</span>
        <span class="sum-lbl">Tổng chi tiêu</span>
      </div>
      <div class="sum-div"></div>
      <div class="sum-item">
        <span class="sum-val">{{ authStore.user?.diemTichLuy || 0 }}</span>
        <span class="sum-lbl">Điểm tích lũy</span>
      </div>
      <div class="sum-div"></div>
      <div class="sum-item">
        <span :class="['sum-val', 'sum-val--level', memberClass]">{{ authStore.user?.capDoThanhVien || 'Thường' }}</span>
        <span class="sum-lbl">Hạng thành viên</span>
      </div>
    </div>

    <!-- Skeleton -->
    <div v-if="loading" class="content">
      <div v-for="n in 3" :key="n" class="skel-row">
        <div class="skel skel--sm"></div>
        <div class="skel-lines">
          <div class="skel skel--line"></div>
          <div class="skel skel--line skel--w60"></div>
        </div>
        <div class="skel skel--sm"></div>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="state-box">
      <svg class="state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <p>{{ loadError }}</p>
      <button class="btn-retry" @click="load">Thử lại</button>
    </div>

    <!-- Empty -->
    <div v-else-if="txs.length === 0" class="state-box">
      <div class="empty-icon">📋</div>
      <p class="empty-txt">Chưa có giao dịch nào</p>
      <button class="btn-retry" @click="router.push('/')">Đặt vé ngay</button>
    </div>

    <!-- Table on desktop / cards on mobile -->
    <div v-else class="content">
      <!-- Desktop table -->
      <div class="table-wrap">
        <table class="tx-table">
          <thead>
            <tr>
              <th>Ngày</th>
              <th>Mã</th>
              <th>Phim</th>
              <th>Ghế</th>
              <th>Combo</th>
              <th>Tiền gốc</th>
              <th>Giảm</th>
              <th>Thanh toán</th>
              <th>Điểm</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tx in txs" :key="tx.id" :class="rowClass(tx)">
              <td class="td-date">{{ fmtDate(tx.ngayTao) }}</td>
              <td class="td-code">{{ tx.maDatVe }}</td>
              <td class="td-movie">{{ tx.lichChieu?.phim?.tenPhim || '—' }}</td>
              <td>{{ seatList(tx) }}</td>
              <td>{{ comboSummary(tx) }}</td>
              <td>{{ fmtPrice(tx.tongTienGoc) }}</td>
              <td class="td-disc">
                <span v-if="totalDisc(tx) > 0" class="disc-val">−{{ fmtPrice(totalDisc(tx)) }}</span>
                <span v-else>—</span>
              </td>
              <td class="td-total">{{ fmtPrice(tx.tongTienThanhToan) }}</td>
              <td class="td-pts">
                <span v-if="tx.trangThaiThanhToan === 'paid'" class="pts-badge">+{{ earnedPoints(tx) }}</span>
                <span v-else>—</span>
              </td>
              <td><span :class="['status-badge', statusClass(tx)]">{{ statusLabel(tx) }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile cards -->
      <div class="mobile-cards">
        <div v-for="tx in txs" :key="tx.id" class="tx-card">
          <div class="tx-card__top">
            <div>
              <p class="tx-movie">{{ tx.lichChieu?.phim?.tenPhim || 'Phim' }}</p>
              <p class="tx-date">{{ fmtDate(tx.ngayTao) }}</p>
            </div>
            <span :class="['status-badge', statusClass(tx)]">{{ statusLabel(tx) }}</span>
          </div>
          <div class="tx-card__rows">
            <div class="tx-row"><span>Mã đặt vé</span><span class="mono">{{ tx.maDatVe }}</span></div>
            <div class="tx-row"><span>Ghế</span><span>{{ seatList(tx) }}</span></div>
            <div class="tx-row" v-if="comboSummary(tx) !== '—'"><span>Combo</span><span>{{ comboSummary(tx) }}</span></div>
            <div class="tx-row" v-if="totalDisc(tx) > 0"><span>Giảm giá</span><span class="green">−{{ fmtPrice(totalDisc(tx)) }}</span></div>
            <div class="tx-row tx-row--total"><span>Thành tiền</span><span class="gold">{{ fmtPrice(tx.tongTienThanhToan) }}</span></div>
            <div class="tx-row" v-if="tx.trangThaiThanhToan === 'paid'"><span>Điểm nhận được</span><span class="gold">+{{ earnedPoints(tx) }} điểm</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'

const router    = useRouter()
const authStore = useAuthStore()

const txs       = ref([])
const loading   = ref(false)
const loadError = ref('')

const memberClass = computed(() => {
  const l = authStore.user?.capDoThanhVien || 'Thường'
  if (l === 'Kim Cương') return 'level--diamond'
  if (l === 'Vàng')      return 'level--gold'
  if (l === 'Bạc')       return 'level--silver'
  return ''
})

function fmtPrice(v) {
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(v || 0)
}
function fmtDate(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' })
}
function seatList(tx) {
  if (!tx.chiTietDatGhe?.length) return '—'
  return tx.chiTietDatGhe.map(c => `${(c.gheNgoi?.hangGhe||'').trim()}${c.gheNgoi?.soGhe}`).sort().join(', ')
}
function comboSummary(tx) {
  if (!tx.chiTietDatSanPham?.length) return '—'
  return tx.chiTietDatSanPham.map(c => `${c.sanPham?.tenSanPham || '?'} x${c.soLuong}`).join(', ')
}
function totalDisc(tx) {
  return (Number(tx.tienGiamKhuyenMai) || 0) + (Number(tx.tienGiamTuDiem) || 0)
}
function earnedPoints(tx) {
  return Math.floor((Number(tx.tongTienThanhToan) || 0) / 1000)
}
function statusClass(tx) {
  if (tx.trangThai === 'confirmed') return 'badge--green'
  if (tx.trangThai === 'cancelled') return 'badge--red'
  if (tx.trangThaiThanhToan === 'unpaid') return 'badge--yellow'
  return 'badge--gray'
}
function statusLabel(tx) {
  if (tx.trangThai === 'confirmed') return 'Đã xác nhận'
  if (tx.trangThai === 'cancelled') return 'Đã hủy'
  if (tx.trangThaiThanhToan === 'paid') return 'Đã thanh toán'
  return 'Chờ thanh toán'
}
function rowClass(tx) {
  if (tx.trangThai === 'cancelled') return 'row--cancelled'
  if (tx.trangThaiThanhToan === 'paid') return 'row--paid'
  return ''
}

async function load() {
  loading.value   = true
  loadError.value = ''
  try {
    if (!authStore.user) await authStore.fetchProfile()
    const res = await api.get('/dat-ve')
    txs.value = (Array.isArray(res.data) ? res.data : []).sort((a,b) => (b.id||0)-(a.id||0))
  } catch (e) {
    loadError.value = e.response?.data?.message || 'Không tải được lịch sử giao dịch'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.history-page { background:linear-gradient(160deg,#0b1120 0%,#0f172a 50%,#1a1f35 100%); color:#f1f5f9; min-height:100vh; padding-bottom:60px; }

/* top bar */
.top-bar { display:flex; align-items:center; justify-content:space-between; padding:14px 20px; background:rgba(11,17,32,.9); backdrop-filter:blur(12px); border-bottom:1px solid rgba(255,215,0,.12); position:sticky; top:0; z-index:60; }
.top-bar__title { font-size:16px; font-weight:700; }
.icon-btn { width:38px; height:38px; border-radius:8px; border:1px solid rgba(255,215,0,.25); background:rgba(255,215,0,.07); color:#ffd700; cursor:pointer; display:flex; align-items:center; justify-content:center; transition:background .2s; }
.icon-btn:hover { background:rgba(255,215,0,.15); }
.icon-btn svg { width:18px; height:18px; }

/* summary */
.summary-bar { display:flex; align-items:center; justify-content:space-around; padding:18px 20px; background:linear-gradient(135deg,rgba(255,215,0,.08),rgba(255,107,0,.08)); border-bottom:1px solid rgba(255,215,0,.15); }
.sum-item { text-align:center; }
.sum-val  { display:block; font-size:18px; font-weight:900; color:#ffd700; }
.sum-val--level { font-size:14px; }
.sum-lbl  { font-size:11px; color:#64748b; font-weight:600; margin-top:2px; display:block; }
.sum-div  { width:1px; height:36px; background:rgba(255,215,0,.15); }
.level--silver  { color:#cbd5e1; }
.level--gold    { color:#ffd700; }
.level--diamond { color:#c4b5fd; }

/* skeleton */
.content { max-width:1100px; margin:0 auto; padding:20px; }
.skel-row { display:flex; gap:12px; align-items:center; background:rgba(30,41,55,.4); border-radius:10px; padding:14px; margin-bottom:10px; }
.skel-lines { flex:1; display:flex; flex-direction:column; gap:8px; }
.skel { background:linear-gradient(90deg,rgba(255,255,255,.05) 25%,rgba(255,255,255,.1) 50%,rgba(255,255,255,.05) 75%); background-size:200% 100%; animation:shimmer 1.4s infinite; border-radius:5px; }
.skel--sm   { width:64px; height:40px; border-radius:7px; }
.skel--line { height:12px; width:100%; }
.skel--w60  { width:60%; }
@keyframes shimmer { to { background-position:-200% 0; } }

/* state box */
.state-box { display:flex; flex-direction:column; align-items:center; justify-content:center; min-height:360px; gap:16px; padding:40px; text-align:center; }
.state-icon { width:48px; height:48px; color:#ef4444; }
.empty-icon { font-size:64px; }
.empty-txt  { font-size:15px; color:#64748b; }
.btn-retry  { padding:10px 24px; background:#ffd700; color:#0f172a; border:none; border-radius:8px; font-weight:800; cursor:pointer; }
.btn-retry:hover { transform:translateY(-2px); box-shadow:0 8px 20px rgba(255,215,0,.3); }

/* desktop table */
.table-wrap { overflow-x:auto; border-radius:12px; border:1px solid rgba(255,215,0,.1); }
.tx-table { width:100%; border-collapse:collapse; font-size:13px; }
.tx-table th { padding:11px 14px; text-align:left; font-size:11px; font-weight:800; color:#64748b; text-transform:uppercase; letter-spacing:.4px; background:rgba(30,41,55,.6); border-bottom:1px solid rgba(255,215,0,.12); white-space:nowrap; }
.tx-table td { padding:12px 14px; border-bottom:1px solid rgba(255,215,0,.05); vertical-align:middle; }
.tx-table tr:last-child td { border-bottom:none; }
.tx-table tr:hover td { background:rgba(255,215,0,.03); }
.row--cancelled td { opacity:.5; }
.td-code  { font-family:monospace; color:#ffd700; font-weight:800; font-size:12px; }
.td-movie { font-weight:700; color:#f1f5f9; max-width:180px; }
.td-date  { color:#64748b; white-space:nowrap; }
.td-disc  .disc-val { color:#4ade80; font-weight:700; }
.td-total { font-weight:900; color:#ffd700; white-space:nowrap; }
.td-pts   .pts-badge { color:#ffd700; font-weight:800; font-size:12px; }

/* status badges */
.status-badge { padding:3px 9px; border-radius:999px; font-size:10px; font-weight:900; white-space:nowrap; }
.badge--green  { background:rgba(74,222,128,.15); color:#4ade80; }
.badge--yellow { background:rgba(234,179,8,.15); color:#fde047; }
.badge--red    { background:rgba(239,68,68,.15); color:#fca5a5; }
.badge--gray   { background:rgba(148,163,184,.15); color:#94a3b8; }

/* mobile cards — hidden above 768px */
.mobile-cards { display:none; flex-direction:column; gap:12px; }
.tx-card { background:rgba(30,41,55,.5); border:1px solid rgba(255,215,0,.1); border-radius:12px; overflow:hidden; }
.tx-card__top { display:flex; justify-content:space-between; align-items:flex-start; padding:14px 16px 8px; }
.tx-movie { font-size:14px; font-weight:800; margin:0 0 2px; }
.tx-date  { font-size:11px; color:#64748b; margin:0; }
.tx-card__rows { padding:8px 16px 14px; display:flex; flex-direction:column; gap:0; }
.tx-row { display:flex; justify-content:space-between; font-size:13px; padding:6px 0; border-bottom:1px solid rgba(255,215,0,.05); }
.tx-row:last-child { border-bottom:none; }
.tx-row span:first-child { color:#94a3b8; font-weight:600; }
.tx-row--total span:last-child { font-weight:900; }
.mono  { font-family:monospace; font-weight:800; font-size:12px; }
.gold  { color:#ffd700; font-weight:800; }
.green { color:#4ade80; }

@media (max-width:768px) {
  .table-wrap   { display:none; }
  .mobile-cards { display:flex; }
}
</style>

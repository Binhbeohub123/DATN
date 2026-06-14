<template>
  <div class="tickets-page">
    <div class="toolbar">
      <input v-model="search" class="search-input" placeholder="Tìm theo mã vé, email..." @input="debouncedLoad" />
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
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in tickets" :key="t.id">
            <td class="mono">{{ t.maDatVe }}</td>
            <td>{{ t.email || t.hoTen || '—' }}</td>
            <td>{{ t.tenPhim }}</td>
            <td>{{ fmtShowtime(t) }}</td>
            <td class="price">{{ fmtPrice(t.tongTien) }}</td>
            <td><span :class="['badge', badgeClass(t.trangThai)]">{{ statusLabel(t.trangThai) }}</span></td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="pagination">
        <button :disabled="page === 0" @click="page--; load()">← Trước</button>
        <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="page++; load()">Sau →</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import api from '@/services/api'
import { useAdminShellStore } from '@/stores/adminShellStore'

const shell = useAdminShellStore()

const tickets = ref([])
const loading = ref(false)
const search = ref('')
const statusFilter = ref('')
const page = ref(0)
const totalPages = ref(1)
let debounceTimer = null

function fmtPrice(n) {
  if (n == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n)
}

function fmtShowtime(t) {
  if (!t.ngayChieu) return '—'
  const d = new Date(t.ngayChieu)
  return `${d.toLocaleDateString('vi-VN')} ${t.gioChieu || ''}`
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

function mapBooking(b) {
  const start = b.lichChieu?.thoiGianBatDau
  return {
    id: b.id,
    maDatVe: b.maDatVe,
    email: b.nguoiDung?.email,
    hoTen: b.nguoiDung?.hoTen,
    tenPhim: b.lichChieu?.phim?.tenPhim,
    ngayChieu: start,
    gioChieu: start
      ? new Date(start).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
      : '',
    tongTien: b.tongTienThanhToan,
    trangThai: b.trangThai,
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

onMounted(() => {
  syncFromShell()
  load()
})
</script>



<style scoped>
/* ── Page root ── */
.tickets-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: #0D0D0D;
  color: #E5E5E5;
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
  border: 1px solid #374151;
  border-radius: 8px;
  background: #111827;
  color: #E5E5E5;
  font-size: 14px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  transition: border-color 150ms ease;
  -webkit-appearance: none;
  appearance: none;
}
.search-input { flex: 1; min-width: 200px; }
.search-input::placeholder { color: #9CA3AF; }
.search-input:focus, .filter-select:focus {
  outline: none;
  border-color: #FFFFFF;
  box-shadow: 0 0 0 2px rgba(255,255,255,0.15);
}

/* ── Table wrapper ── */
.card {
  background: #111827;
  border: 1px solid #374151;
  border-radius: 12px;
  overflow: hidden;
}

/* ── Table ── */
table { width: 100%; border-collapse: collapse; }
thead tr { background: #0D0D0D; }
th {
  padding: 12px 16px;
  text-align: left;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #9CA3AF;
  border-bottom: 2px solid #374151;
  white-space: nowrap;
}
tbody tr { border-bottom: 1px solid #1F2937; transition: background 150ms ease; }
tbody tr:last-child { border-bottom: none; }
tbody tr:hover { background: #1F2937; }
td {
  padding: 14px 16px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  color: #E5E5E5;
  vertical-align: middle;
}

/* ── Cell helpers ── */
.mono  { font-family: 'Courier New', monospace; font-size: 13px; color: #FFFFFF; font-weight: 600; }
.price { font-weight: 700; color: #FFFFFF; }

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
.badge-gray   { background: rgba(156,163,175,0.15); color: #9CA3AF; }

/* ── Loading / empty ── */
.loading-text, .empty-text {
  text-align: center;
  padding: 32px;
  color: #9CA3AF;
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
  border-top: 1px solid #1F2937;
}
.pagination button {
  min-height: 36px;
  padding: 7px 16px;
  border: 1px solid #374151;
  border-radius: 9999px;
  background: transparent;
  color: #E5E5E5;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-weight: 600;
  cursor: pointer;
  transition: border-color 150ms ease, color 150ms ease;
}
.pagination button:hover:not(:disabled) { border-color: #FFFFFF; color: #FFFFFF; }
.pagination button:disabled { opacity: 0.35; cursor: not-allowed; }
.pagination span { font-size: 13px; color: #9CA3AF; font-weight: 600; }
</style>

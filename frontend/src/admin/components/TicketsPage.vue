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



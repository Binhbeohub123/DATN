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
import { ref, onMounted } from 'vue'
import api from '@/services/api'

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

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: 15 }
    if (search.value.trim()) params.q = search.value.trim()
    if (statusFilter.value) params.trangThai = statusFilter.value
    const { data } = await api.get('/admin/dat-ve', { params })
    tickets.value = data.content || data || []
    totalPages.value = data.totalPages ?? 1
  } catch {
    tickets.value = []
  } finally {
    loading.value = false
  }
}

function debouncedLoad() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => { page.value = 0; load() }, 400)
}

onMounted(load)
</script>

<style scoped>
.tickets-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  font-family: 'Raleway', sans-serif;
}

.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input,
.filter-select {
  min-height: 44px;
  padding: 10px 14px;
  border: 1px solid #efefef;
  border-radius: 4px;
  font-size: 14.4px;
  background: #ffffff;
  font-family: inherit;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.card {
  border-radius: 0;
  background: #f7f7f7;
  border: 1px solid #efefef;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  text-align: left;
  padding: 14px 16px;
  font-size: 11px;
  font-weight: 700;
  color: #767676;
  text-transform: uppercase;
  background: #ffffff;
  border-bottom: 1px solid #efefef;
}

td {
  padding: 12px 16px;
  font-size: 14.4px;
  color: #7f7e7f;
  border-bottom: 1px solid #efefef;
}

tr:last-child td {
  border-bottom: none;
}

tr:hover td {
  background: #f7fcfe;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-weight: 700;
  color: #29bcea;
}

.price {
  font-weight: 700;
  color: #29bcea;
}

.badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
}

.badge-green {
  background: #e8f7ef;
  color: #166534;
}

.badge-yellow {
  background: #fef9e8;
  color: #854d0e;
}

.badge-red {
  background: #fef2f2;
  color: #991b1b;
}

.badge-gray {
  background: #f7f7f7;
  color: #767676;
  border: 1px solid #efefef;
}

.loading-text,
.empty-text {
  text-align: center;
  padding: 40px;
  color: #767676;
  font-size: 14.4px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid #efefef;
  background: #ffffff;
}

.pagination button {
  min-height: 44px;
  padding: 8px 16px;
  border: 1px solid #29bcea;
  border-radius: 4px;
  background: transparent;
  color: #29bcea;
  cursor: pointer;
  font-weight: 700;
  font-family: inherit;
}

.pagination button:hover:not(:disabled) {
  background: #29bcea;
  color: #ffffff;
}

.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  border-color: #efefef;
  color: #767676;
}

.pagination span {
  font-size: 13px;
  font-weight: 700;
  color: #7f7e7f;
}
</style>

<template>
  <div class="tickets-page">
    <div class="toolbar">
      <input v-model="search" placeholder="🔍 Tìm mã đặt vé, khách hàng..." class="search-input" />
      <select v-model="filterStatus" class="filter-select">
        <option value="">Tất cả</option>
        <option value="pending">Chờ thanh toán</option>
        <option value="confirmed">Đã xác nhận</option>
        <option value="cancelled">Đã hủy</option>
      </select>
    </div>

    <div class="card">
      <div v-if="loading" class="loading-text">Đang tải...</div>
      <table v-else>
        <thead>
          <tr>
            <th>Mã đặt vé</th>
            <th>Khách hàng</th>
            <th>Phim</th>
            <th>Suất chiếu</th>
            <th>Tổng tiền</th>
            <th>Thanh toán</th>
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="bk in filteredBookings" :key="bk.id">
            <td><strong>{{ bk.maDatVe }}</strong></td>
            <td>{{ bk.nguoiDung?.hoTen || bk.nguoiDung?.email || '—' }}</td>
            <td>{{ bk.lichChieu?.phim?.tenPhim || '—' }}</td>
            <td>{{ formatDatetime(bk.lichChieu?.thoiGianBatDau) }}</td>
            <td>{{ formatPrice(bk.tongTienThanhToan) }}</td>
            <td><span :class="['badge', payClass(bk.trangThaiThanhToan)]">{{ bk.trangThaiThanhToan }}</span></td>
            <td><span :class="['badge', statusClass(bk.trangThai)]">{{ bk.trangThai }}</span></td>
          </tr>
          <tr v-if="filteredBookings.length === 0">
            <td colspan="7" class="empty-text">Không có đặt vé</td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div class="pagination">
        <button :disabled="page === 0" @click="page--; load()">‹ Trước</button>
        <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="page++; load()">Sau ›</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'

const bookings = ref([])
const loading = ref(false)
const search = ref('')
const filterStatus = ref('')
const page = ref(0)
const totalPages = ref(1)

const filteredBookings = computed(() => {
  return bookings.value.filter(bk => {
    const matchSearch = !search.value ||
      bk.maDatVe?.toLowerCase().includes(search.value.toLowerCase()) ||
      bk.nguoiDung?.hoTen?.toLowerCase().includes(search.value.toLowerCase()) ||
      bk.nguoiDung?.email?.toLowerCase().includes(search.value.toLowerCase())
    const matchStatus = !filterStatus.value || bk.trangThai === filterStatus.value
    return matchSearch && matchStatus
  })
})

function formatDatetime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function formatPrice(val) {
  if (!val && val !== 0) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

function payClass(s) {
  if (s === 'paid') return 'badge-green'
  if (s === 'unpaid') return 'badge-yellow'
  return 'badge-gray'
}

function statusClass(s) {
  if (s === 'confirmed') return 'badge-green'
  if (s === 'pending') return 'badge-yellow'
  if (s === 'cancelled') return 'badge-red'
  return 'badge-gray'
}

async function load() {
  loading.value = true
  try {
    const res = await api.get(`/admin/dat-ve?page=${page.value}&size=20`)
    bookings.value = res.data?.content || []
    totalPages.value = res.data?.totalPages || 1
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.tickets-page { display: flex; flex-direction: column; gap: 20px; }
.toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.search-input { flex: 1; min-width: 200px; padding: 10px 14px; border: 1px solid #e5e7eb; border-radius: 10px; font-size: 14px; }
.filter-select { padding: 10px 14px; border: 1px solid #e5e7eb; border-radius: 10px; font-size: 14px; background: white; }
.card { border-radius: 20px; background: rgba(255,255,255,0.84); border: 1px solid rgba(255,255,255,0.7); box-shadow: 0 8px 24px rgba(15,23,42,0.07); overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 14px 16px; font-size: 12px; font-weight: 800; color: #6b7280; text-transform: uppercase; background: #f9fafb; border-bottom: 1px solid #e5e7eb; }
td { padding: 12px 16px; font-size: 13px; border-bottom: 1px solid #f3f4f6; }
tr:last-child td { border-bottom: none; }
tr:hover td { background: #fff7ed; }
.badge { padding: 4px 10px; border-radius: 999px; font-size: 11px; font-weight: 800; }
.badge-green { background: #dcfce7; color: #166534; }
.badge-yellow { background: #fef9c3; color: #854d0e; }
.badge-red { background: #fee2e2; color: #991b1b; }
.badge-gray { background: #f3f4f6; color: #6b7280; }
.loading-text, .empty-text { text-align: center; padding: 40px; color: #9ca3af; font-size: 14px; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; padding: 16px; border-top: 1px solid #f3f4f6; }
.pagination button { padding: 8px 16px; border: 1px solid #e5e7eb; border-radius: 8px; background: white; cursor: pointer; font-weight: 700; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
.pagination span { font-size: 13px; font-weight: 700; color: #6b7280; }
</style>

<template>
  <div class="staff-report">
    <header class="page-header">
      <router-link to="/staff/dashboard" class="btn-back">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
        Trở về Dashboard
      </router-link>
      <h2>Báo cáo ca làm việc</h2>
    </header>

    <main class="report-main">
      <div class="filter-box">
        <div class="form-group">
          <label>Từ thời gian:</label>
          <input type="datetime-local" v-model="filters.from" />
        </div>
        <div class="form-group">
          <label>Đến thời gian:</label>
          <input type="datetime-local" v-model="filters.to" />
        </div>
        <button class="btn-filter" @click="fetchReport" :disabled="loading">
          {{ loading ? 'Đang tải...' : 'Xem báo cáo' }}
        </button>
      </div>

      <div v-if="errorMsg" class="error-msg">
        {{ errorMsg }}
      </div>

      <div v-if="reportData && !loading" class="report-grid">
        <div class="stat-card">
          <div class="stat-icon revenue-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 1v22M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-label">Tổng doanh thu bán vé (Tại quầy)</span>
            <span class="stat-value">{{ formatCurrency(reportData.tongDoanhThu) }}</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon ticket-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="2" y="3" width="20" height="18" rx="2" />
              <path d="M2 9h20M8 15h2M14 15h2" />
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-label">Số vé đã bán</span>
            <span class="stat-value">{{ reportData.tongVeBan }}</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon checkin-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-label">Số vé đã Check-in</span>
            <span class="stat-value">{{ reportData.soVeCheckIn }}</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'

const loading = ref(false)
const errorMsg = ref('')
const reportData = ref(null)

// Mặc định lấy từ đầu ngày đến hiện tại
const now = new Date()
const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate())

// Format yyyy-MM-ddThh:mm để gắn vào thẻ input type="datetime-local"
const formatDateTimeLocal = (date) => {
  const tzOffset = (new Date()).getTimezoneOffset() * 60000;
  return (new Date(date - tzOffset)).toISOString().slice(0, 16);
}

const filters = ref({
  from: formatDateTimeLocal(startOfDay),
  to: formatDateTimeLocal(now)
})

const formatCurrency = (value) => {
  if (value == null) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

async function fetchReport() {
  loading.value = true
  errorMsg.value = ''
  
  try {
    const params = {}
    if (filters.value.from) {
      params.from = new Date(filters.value.from).toISOString()
    }
    if (filters.value.to) {
      params.to = new Date(filters.value.to).toISOString()
    }
    
    const res = await api.get('/staff/report/shift', { params })
    reportData.value = res.data
  } catch (err) {
    errorMsg.value = 'Lỗi tải báo cáo: ' + (err.response?.data?.message || err.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchReport()
})
</script>

<style scoped>
.staff-report {
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

.btn-back:hover {
  color: #fff;
}

.page-header h2 {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
}

.report-main {
  max-width: 900px;
  margin: 2rem auto;
  padding: 0 1.5rem;
}

.filter-box {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  align-items: flex-end;
  background: rgba(255, 255, 255, 0.05);
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.form-group input {
  padding: 0.6rem 1rem;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  color: #fff;
  font-family: inherit;
}

.btn-filter {
  padding: 0.65rem 1.5rem;
  background: #e94560;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-filter:hover:not(:disabled) {
  background: #d63d56;
}

.report-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1.2rem;
  background: rgba(255, 255, 255, 0.05);
  padding: 1.5rem;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.revenue-icon {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.ticket-icon {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}

.checkin-icon {
  background: rgba(168, 85, 247, 0.2);
  color: #a855f7;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.stat-label {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
}

.error-msg {
  color: #ef4444;
  padding: 1rem;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 8px;
  margin-bottom: 2rem;
}
</style>
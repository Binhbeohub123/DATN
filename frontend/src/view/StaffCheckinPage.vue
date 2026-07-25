<template>
  <div class="staff-checkin">
    <header class="page-header">
      <router-link to="/staff/dashboard" class="btn-back">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
        Trở về Dashboard
      </router-link>
      <h2>Quét QR Check-in</h2>
    </header>

    <main class="checkin-main">
      <div class="checkin-box">
        <h3>Nhập Mã QR (hoặc dùng máy quét)</h3>
        <p class="subtitle">Focus vào ô bên dưới và dùng máy quét QR cầm tay để tự động nhập.</p>

        <form @submit.prevent="handleCheckin" class="checkin-form">
          <input 
            type="text" 
            v-model="maQR" 
            placeholder="Ví dụ: QR-12345678" 
            required
            ref="qrInput"
            :disabled="loading"
          />
          <button type="submit" class="btn-submit" :disabled="loading || !maQR.trim()">
            {{ loading ? 'Đang xử lý...' : 'Check-in' }}
          </button>
        </form>

        <div v-if="errorMsg" class="error-msg">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
          </svg>
          {{ errorMsg }}
        </div>
      </div>

      <div v-if="result" class="result-box success-msg">
        <div class="success-header">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#4ade80" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M8 12l3 3 5-5"/>
          </svg>
          <h3>{{ result.message }}</h3>
        </div>
        <div class="result-details">
          <p><strong>Mã vé:</strong> {{ result.maDatVe }}</p>
          <p><strong>Phim:</strong> {{ result.phim }}</p>
          <p><strong>Suất chiếu:</strong> {{ result.suatChieu ? new Date(result.suatChieu).toLocaleString('vi-VN') : '' }}</p>
          <p><strong>Khách hàng:</strong> {{ result.khachHang }}</p>
        </div>
        <button class="btn-next" @click="resetForm">Quét vé tiếp theo</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import api from '@/services/api'

const maQR = ref('')
const loading = ref(false)
const errorMsg = ref('')
const result = ref(null)
const qrInput = ref(null)

onMounted(() => {
  // Tự động focus vào ô nhập liệu để sẵn sàng quét
  nextTick(() => {
    qrInput.value?.focus()
  })
})

async function handleCheckin() {
  if (!maQR.value.trim()) return
  
  errorMsg.value = ''
  result.value = null
  loading.value = true
  
  try {
    const res = await api.post('/staff/checkin', { maQR: maQR.value.trim() })
    result.value = res.data
    maQR.value = '' // Clear input cho lần quét tới
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Có lỗi xảy ra khi check-in'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  result.value = null
  errorMsg.value = ''
  maQR.value = ''
  nextTick(() => {
    qrInput.value?.focus()
  })
}
</script>

<style scoped>
.staff-checkin {
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

.checkin-main {
  max-width: 600px;
  margin: 3rem auto;
  padding: 0 1.5rem;
}

.checkin-box, .result-box {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.checkin-box h3 {
  margin: 0 0 0.5rem;
  font-size: 1.2rem;
}

.subtitle {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.9rem;
  margin: 0 0 1.5rem;
}

.checkin-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.checkin-form input {
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #fff;
  font-size: 1.1rem;
  text-align: center;
}

.checkin-form input:focus {
  outline: none;
  border-color: #e94560;
  background: rgba(255, 255, 255, 0.1);
}

.btn-submit, .btn-next {
  padding: 1rem;
  background: #e94560;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-submit:hover:not(:disabled), .btn-next:hover {
  background: #d63d56;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  padding: 1rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #fca5a5;
}

.success-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.success-header h3 {
  margin: 0;
  color: #4ade80;
  font-size: 1.3rem;
}

.result-details {
  background: rgba(0, 0, 0, 0.2);
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.result-details p {
  margin: 0.5rem 0;
  font-size: 1.05rem;
}

.result-details strong {
  color: rgba(255, 255, 255, 0.7);
  display: inline-block;
  width: 100px;
}

.btn-next {
  width: 100%;
}
</style>
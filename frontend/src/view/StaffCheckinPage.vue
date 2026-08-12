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
      <div class="header-actions">
        <router-link to="/" class="btn-nav">🏠 Home</router-link>
        <router-link v-if="authStore.isAdmin" to="/admin/dashboard" class="btn-nav">⚙️ Admin Panel</router-link>
        <ThemeToggle />
      </div>
    </header>

    <main class="checkin-main">

      <!-- Manual / USB scanner input -->
      <div class="checkin-box">
        <h3>Nhập Mã Vé (hoặc dùng máy quét USB)</h3>
        <p class="subtitle">Nhập mã đặt vé hoặc đưa máy quét USB vào ô bên dưới và quét QR trên vé.</p>

        <form @submit.prevent="handleCheckin" class="checkin-form">
          <input
            type="text"
            v-model="maDatVe"
            placeholder="Ví dụ: BK345678"
            required
            ref="qrInput"
            :disabled="loading"
          />
          <button type="submit" class="btn-submit" :disabled="loading || !maDatVe.trim()">
            {{ loading ? 'Đang xử lý...' : 'Check-in' }}
          </button>
        </form>

        <!-- Camera toggle button -->
        <button
          class="btn-camera-toggle"
          type="button"
          @click="toggleCamera"
          :disabled="loading"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
            <circle cx="12" cy="13" r="4"/>
          </svg>
          {{ cameraActive ? 'Tắt camera' : 'Quét bằng camera' }}
        </button>

        <!-- Camera viewfinder -->
        <div v-if="cameraActive" class="camera-section">
          <div id="qr-reader" class="qr-reader-container"></div>
          <p class="camera-hint">Hướng camera vào mã QR trên vé</p>
          <p v-if="cameraError" class="camera-error">
            ⚠️ {{ cameraError }}<br>
            <small>Vui lòng dùng nhập thủ công hoặc máy quét USB.</small>
          </p>
        </div>

        <div v-if="errorMsg" class="error-msg">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
          </svg>
          {{ errorMsg }}
        </div>
      </div>

      <!-- Success result -->
      <div v-if="result" class="result-box" :class="{ 'result-box--warn': errorTicket }">
        <div class="success-header" :class="{ 'success-header--warn': errorTicket }">
          <svg v-if="!errorTicket" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#4ade80" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M8 12l3 3 5-5"/>
          </svg>
          <svg v-else width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
          </svg>
          <h3 :class="{ 'warn-title': errorTicket }">{{ result.message }}</h3>
        </div>
        <div class="result-details">
          <div v-if="result.ticket.trangThaiQuet" class="checkin-status" :class="result.ticket.canCheckIn ? 'checkin-status--ok' : 'checkin-status--warn'">
            <span class="checkin-status__dot"></span>
            {{ result.ticket.trangThaiQuet }}
          </div>
          <p><strong>Mã vé:</strong> {{ result.ticket.maDatVe }}</p>
          <p><strong>Khách hàng:</strong> {{ result.ticket.khachHang }}
            <span v-if="result.ticket.email" class="muted">({{ result.ticket.email }})</span>
          </p>
          <p><strong>Phim:</strong> {{ result.ticket.phim }}</p>
          <p><strong>Rạp chiếu:</strong> {{ result.ticket.rapChieu }}</p>
          <p><strong>Phòng chiếu:</strong> {{ result.ticket.phongChieu }}</p>
          <p><strong>Suất chiếu:</strong> {{ formatDateTime(result.ticket.thoiGianBatDau) }}</p>
          <p><strong>Ghế:</strong> {{ result.ticket.ghe && result.ticket.ghe.length ? result.ticket.ghe.join(', ') : '—' }}</p>
          <p v-if="result.ticket.combo && result.ticket.combo.length">
            <strong>Combo:</strong>
            <span v-for="(c, i) in result.ticket.combo" :key="i">
              {{ c.tenSanPham }} x{{ c.soLuong }}<span v-if="i < result.ticket.combo.length - 1">, </span>
            </span>
          </p>
          <p v-if="result.ticket.tongTienGoc > 0">
            <strong>Tiền vé gốc:</strong> {{ formatCurrency(result.ticket.tongTienGoc) }}
          </p>
          <p v-if="result.ticket.tienGiamKhuyenMai > 0">
            <strong>KM:</strong> -{{ formatCurrency(result.ticket.tienGiamKhuyenMai) }}
          </p>
          <p v-if="result.ticket.tienGiamTuDiem > 0">
            <strong>Điểm:</strong> -{{ formatCurrency(result.ticket.tienGiamTuDiem) }} ({{ result.ticket.diemSuDung }} điểm)
          </p>
          <p class="total-line"><strong>Tổng thanh toán:</strong> {{ formatCurrency(result.ticket.tongTienThanhToan) }}</p>
          <p><strong>Thanh toán:</strong> {{ paymentStatusText(result.ticket.trangThaiThanhToan) }}</p>
          <p v-if="result.ticket.thoiGianCheckIn">
            <strong>Check-in lúc:</strong> {{ formatDateTime(result.ticket.thoiGianCheckIn) }}
            <span v-if="result.ticket.nhanVienCheckIn"> bởi {{ result.ticket.nhanVienCheckIn }}</span>
          </p>
        </div>
        <button class="btn-next" @click="resetForm">Quét vé tiếp theo</button>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import api from '@/services/api'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

const maDatVe   = ref('')
const loading   = ref(false)
const errorMsg  = ref('')
const result    = ref(null)
const errorTicket = ref(false)
const qrInput   = ref(null)

// Camera state
const cameraActive = ref(false)
const cameraError  = ref('')
let html5QrScanner = null

onMounted(() => {
  nextTick(() => { qrInput.value?.focus() })
})

onUnmounted(() => {
  stopCamera()
})

// ── Camera toggle ─────────────────────────────────────────────
async function toggleCamera() {
  if (cameraActive.value) {
    stopCamera()
  } else {
    cameraActive.value = true
    cameraError.value = ''
    await nextTick()
    startCamera()
  }
}

function startCamera() {
  import('html5-qrcode').then(({ Html5Qrcode }) => {
    html5QrScanner = new Html5Qrcode('qr-reader')
    html5QrScanner.start(
      { facingMode: 'environment' },        // rear camera
      { fps: 10, qrbox: { width: 250, height: 250 } },
      onScanSuccess,
      () => {}                              // suppress per-frame errors
    ).catch(err => {
      cameraError.value = 'Không thể truy cập camera. Vui lòng cấp quyền camera cho trình duyệt.'
      console.warn('[QR camera] start failed:', err)
    })
  }).catch(() => {
    cameraError.value = 'Không tải được thư viện quét QR.'
  })
}

function stopCamera() {
  if (html5QrScanner) {
    html5QrScanner.stop().catch(() => {}).finally(() => {
      html5QrScanner = null
    })
  }
  cameraActive.value = false
}

// ── Camera scan success ───────────────────────────────────────
function onScanSuccess(decodedText) {
  const code = decodedText.trim()
  if (!code || loading.value) return
  stopCamera()
  maDatVe.value = code
  submitCheckin(code)
}

// ── Checkin submit ────────────────────────────────────────────
async function handleCheckin() {
  if (!maDatVe.value.trim()) return
  await submitCheckin(maDatVe.value.trim())
}

async function submitCheckin(code) {
  errorMsg.value = ''
  result.value   = null
  errorTicket.value = false
  loading.value  = true
  try {
    const res = await api.post('/staff/checkin', { maDatVe: code })
    result.value = res.data
    errorTicket.value = false
    maDatVe.value = ''
  } catch (err) {
    const data = err.response?.data
    if (data && data.ticket) {
      result.value = data
      errorTicket.value = true
      maDatVe.value = ''
    } else {
      errorMsg.value = data?.message || 'Có lỗi xảy ra khi check-in'
    }
  } finally {
    loading.value = false
  }
}

function resetForm() {
  result.value   = null
  errorTicket.value = false
  errorMsg.value = ''
  maDatVe.value  = ''
  nextTick(() => { qrInput.value?.focus() })
}

function formatDateTime(val) {
  if (!val) return ''
  const d = new Date(val)
  return d.toLocaleString('vi-VN', {
    weekday: 'short', year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

function formatCurrency(val) {
  if (val == null) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

function paymentStatusText(status) {
  if (status === 'paid') return 'Đã thanh toán'
  if (status === 'unpaid') return 'Chưa thanh toán'
  return status || ''
}
</script>

<style scoped>
.staff-checkin {
  min-height: 100vh;
  background: var(--void, #050508);
  color: var(--text-primary, #f1f5f9);
}
.page-header {
  display: flex; align-items: center; gap: 1.5rem; padding: 1.5rem 2rem;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.btn-back {
  display: flex; align-items: center; gap: 0.5rem; flex-shrink: 0;
  color: var(--text-secondary, #94a3b8); text-decoration: none; font-size: 0.9rem; transition: color 0.2s;
}
.btn-back:hover { color: var(--text-primary, #f1f5f9); }
.page-header h2 { margin: 0; font-size: 1.4rem; font-weight: 600; }
.header-actions { display: flex; align-items: center; gap: 0.75rem; margin-left: auto; }
.btn-nav {
  padding: 0.4rem 0.9rem; border: 1px solid var(--glass-border, rgba(255,255,255,0.15));
  border-radius: 6px; background: transparent; color: var(--text-secondary, #94a3b8);
  font-size: 0.82rem; text-decoration: none; white-space: nowrap; transition: all 0.2s;
}
.btn-nav:hover { border-color: var(--electric, #29bcea); color: var(--electric, #29bcea); }

.checkin-main { max-width: 600px; margin: 3rem auto; padding: 0 1.5rem; }

.checkin-box, .result-box {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: 12px; padding: 2rem; margin-bottom: 2rem;
}
.checkin-box h3 { margin: 0 0 0.5rem; font-size: 1.2rem; }
.subtitle { color: var(--text-secondary, #94a3b8); font-size: 0.9rem; margin: 0 0 1.5rem; }

.checkin-form { display: flex; flex-direction: column; gap: 1rem; }
.checkin-form input {
  padding: 1rem;
  background: var(--surface-2, #14141f);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.15));
  border-radius: 8px; color: var(--text-primary, #f1f5f9); font-size: 1.1rem; text-align: center;
}
.checkin-form input:focus { outline: none; border-color: var(--electric, #29bcea); }

/* Camera toggle button */
.btn-camera-toggle {
  display: inline-flex; align-items: center; gap: 0.5rem;
  margin-top: 1rem; padding: 0.65rem 1.25rem;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.2));
  border-radius: 8px; background: transparent;
  color: var(--text-secondary, #94a3b8);
  font-size: 0.9rem; font-weight: 600; cursor: pointer; transition: all 0.2s;
  width: 100%;
  justify-content: center;
}
.btn-camera-toggle:hover:not(:disabled) {
  border-color: var(--electric, #29bcea);
  color: var(--electric, #29bcea);
}
.btn-camera-toggle:disabled { opacity: 0.5; cursor: not-allowed; }

/* Camera viewfinder */
.camera-section { margin-top: 1.5rem; }
.qr-reader-container {
  width: 100%; max-width: 320px; margin: 0 auto;
  border-radius: 12px; overflow: hidden;
  border: 2px solid var(--electric, #29bcea);
}
/* html5-qrcode injects its own video/canvas — limit their size */
.qr-reader-container :deep(video),
.qr-reader-container :deep(canvas) {
  width: 100% !important; height: auto !important; border-radius: 10px;
}
.camera-hint {
  text-align: center; font-size: 0.85rem;
  color: var(--text-secondary, #94a3b8); margin: 0.75rem 0 0;
}
.camera-error {
  margin-top: 0.75rem; padding: 0.75rem;
  background: rgba(239,68,68,0.08); border: 1px solid rgba(239,68,68,0.25);
  border-radius: 8px; color: #fca5a5; font-size: 0.85rem; text-align: center;
}

.btn-submit, .btn-next {
  padding: 1rem; background: var(--electric, #29bcea); color: #fff;
  border: none; border-radius: 8px; font-size: 1.05rem; font-weight: 600;
  cursor: pointer; transition: filter 0.2s;
}
.btn-submit:hover:not(:disabled), .btn-next:hover { filter: brightness(1.1); }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.error-msg {
  display: flex; align-items: center; gap: 0.5rem; margin-top: 1.5rem;
  padding: 1rem; background: rgba(239,68,68,0.1); border: 1px solid rgba(239,68,68,0.3);
  border-radius: 8px; color: #fca5a5;
}

.success-header { display: flex; flex-direction: column; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }
.success-header h3 { margin: 0; color: #4ade80; font-size: 1.3rem; }
.success-header--warn h3.warn-title { color: #f59e0b; }
.result-box--warn { border-color: rgba(245,158,11,0.4); }
.muted { color: var(--text-secondary, #94a3b8); font-size: 0.9rem; }
.result-details {
  background: var(--surface-2, #14141f); padding: 1.5rem; border-radius: 8px; margin-bottom: 1.5rem;
}
.result-details p { margin: 0.5rem 0; font-size: 1.05rem; }
.result-details strong { color: var(--text-secondary, #94a3b8); display: inline-block; width: 100px; }
.checkin-status {
  display: flex; align-items: center; gap: 0.6rem;
  padding: 0.85rem 1rem; border-radius: 8px;
  font-size: 1rem; font-weight: 600; margin-bottom: 1.25rem;
}
.checkin-status__dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.checkin-status--ok {
  background: rgba(74,222,128,0.12); border: 1px solid rgba(74,222,128,0.4); color: #4ade80;
}
.checkin-status--ok .checkin-status__dot { background: #4ade80; box-shadow: 0 0 8px rgba(74,222,128,0.8); }
.checkin-status--warn {
  background: rgba(245,158,11,0.12); border: 1px solid rgba(245,158,11,0.4); color: #fbbf24;
}
.checkin-status--warn .checkin-status__dot { background: #f59e0b; box-shadow: 0 0 8px rgba(245,158,11,0.8); }
.result-details .total-line strong { color: var(--electric, #29bcea); }
.result-details .total-line {
  margin-top: 0.9rem; padding-top: 0.9rem; border-top: 1px dashed var(--glass-border, rgba(255,255,255,0.15));
  font-size: 1.15rem;
}
.btn-next { width: 100%; }
</style>

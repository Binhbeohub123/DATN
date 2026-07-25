<template>
  <div class="cancel-page">
    <div class="cancel-card">
      <div class="cancel-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
             width="48" height="48" aria-hidden="true">
          <circle cx="12" cy="12" r="10"/>
          <line x1="15" y1="9" x2="9" y2="15"/>
          <line x1="9" y1="9" x2="15" y2="15"/>
        </svg>
      </div>
      <h2 class="cancel-title">Đã hủy thanh toán</h2>
      <p class="cancel-body">Đơn hàng của bạn đã được hủy và ghế đã được trả lại.</p>
      <p class="redirect-note">Đang chuyển về trang chủ...</p>
      <div class="cancel-spinner"></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'

const route  = useRoute()
const router = useRouter()

onMounted(async () => {
  const maDatVe = route.query?.maDatVe
  if (maDatVe) {
    try {
      await api.post('/thanh-toan/payos/cancel', { maDatVe })
    } catch (e) {
      // Non-fatal — booking may have been auto-cancelled already
      console.error('[PaymentCancelPage] Cancel error:', e?.response?.data || e?.message)
    }
  }
  // Redirect to movies after 2 seconds whether cancel succeeded or not
  setTimeout(() => router.push('/movies'), 2000)
})
</script>

<style scoped>
.cancel-page {
  min-height: 100vh;
  background: var(--void, #050508);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  font-family: var(--font-ui, 'Inter', sans-serif);
}

.cancel-card {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: var(--radius-md, 12px);
  padding: 48px 40px;
  text-align: center;
  max-width: 420px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.cancel-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md, 12px);
  background: rgba(239,68,68,0.08);
  border: 1px solid rgba(239,68,68,0.25);
  color: #EF4444;
  margin-bottom: 4px;
}

.cancel-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary, #f1f5f9);
  margin: 0;
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
}

.cancel-body {
  font-size: 14px;
  color: var(--text-secondary, #94a3b8);
  margin: 0;
  line-height: 1.6;
}

.redirect-note {
  font-size: 13px;
  color: var(--text-ghost, rgba(241,245,249,0.45));
  margin: 0;
}

.cancel-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid var(--glass-border, rgba(255,255,255,0.08));
  border-top-color: var(--text-secondary, #94a3b8);
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
  margin-top: 8px;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>

<template>
  <div class="app">
    <div v-if="hasError" class="error-screen">
      <h1>⚠️ Lỗi tải ứng dụng</h1>
      <p>{{ errorMessage }}</p>
      <button @click="reloadPage">Tải lại trang</button>
    </div>
    <router-view v-else />
    <ThemeToggle v-if="showFabTheme" class="app-theme-fab" />
    <ToastContainer />

    <!-- ── Account locked overlay (blocks everything, any tab/page) ── -->
    <div v-if="authStore.locked" class="locked-overlay">
      <div class="locked-card">
        <div class="locked-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="40" height="40"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
        </div>
        <h1 class="locked-title">Tài khoản của bạn đã bị khóa</h1>
        <p class="locked-desc">Bạn không thể sử dụng các tính năng của hệ thống cho đến khi tài khoản được mở khóa.</p>
        <div v-if="authStore.lockReason" class="locked-reason">
          <span class="locked-reason__label">Lý do:</span>
          <span class="locked-reason__text">{{ authStore.lockReason }}</span>
        </div>
        <button class="locked-logout" @click="handleLockedLogout">Đăng xuất</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onErrorCaptured } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import ToastContainer from '@/components/ToastContainer.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const showFabTheme = computed(() => {
  const path = route.path
  if (path === '/' || path.startsWith('/admin') || path.startsWith('/staff')) return false
  const withBar = ['/phim', '/rap', '/seat-selection', '/combo', '/checkout', '/profile', '/transaction-history', '/my-tickets']
  return !withBar.some((p) => path.startsWith(p))
})

const hasError = ref(false)
const errorMessage = ref('')

onErrorCaptured((err) => {
  console.error('App error:', err)
  hasError.value = true
  errorMessage.value = err.message || 'Có lỗi không xác định xảy ra'
  return false
})

const reloadPage = () => window.location.reload()

function handleLockedLogout() {
  authStore.logout()
  router.replace('/auth').catch(() => {})
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app { width: 100%; height: 100%; }

.app {
  font-family: 'Raleway', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: var(--page-bg);
  color: var(--text-secondary);
  min-height: 100vh;
  transition: background 0.25s ease, color 0.25s ease;
}

.error-screen {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  background: var(--page-bg);
  color: var(--text-secondary);
  padding: 20px;
  text-align: center;
}

.error-screen h1 {
  font-size: 40px;
  line-height: 1.2;
  color: var(--accent);
  font-weight: 700;
}

.error-screen p {
  font-size: 14.4px;
  line-height: 1.5;
  color: var(--text-secondary);
  max-width: 500px;
  text-align: center;
}

.error-screen button {
  padding: 12px 28px;
  background: var(--accent);
  color: var(--on-accent);
  border: none;
  border-radius: 4px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.error-screen button:hover {
  background: var(--accent-hover);
}

.app-theme-fab {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 90;
  box-shadow: var(--shadow-sm);
}

/* ── Account locked overlay ─────────────────────────────────── */
.locked-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: var(--page-bg, #050508);
}

.locked-card {
  width: 100%;
  max-width: 460px;
  padding: 40px 36px;
  text-align: center;
  background: var(--glass-bg-heavy, rgba(255,255,255,0.08));
  border: 1px solid rgba(239,68,68,0.3);
  border-radius: var(--radius-md, 12px);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.locked-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #EF4444;
  background: rgba(239,68,68,0.12);
  border: 1px solid rgba(239,68,68,0.35);
}

.locked-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary, #f1f5f9);
  margin: 0 0 12px;
  line-height: 1.3;
}

.locked-desc {
  font-size: 14px;
  color: var(--text-secondary, #94a3b8);
  line-height: 1.6;
  margin: 0 0 20px;
}

.locked-reason {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  margin-bottom: 24px;
  text-align: left;
  background: rgba(239,68,68,0.08);
  border: 1px dashed rgba(239,68,68,0.35);
  border-radius: var(--radius-sm, 6px);
}

.locked-reason__label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(239,68,68,0.9);
}

.locked-reason__text {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #f1f5f9);
  line-height: 1.5;
}

.locked-logout {
  padding: 12px 32px;
  background: #EF4444;
  color: #fff;
  border: none;
  border-radius: var(--radius-pill, 999px);
  font-size: 14px;
  font-weight: 700;
  font-family: var(--font-ui, 'Inter', sans-serif);
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
}

.locked-logout:hover {
  background: #dc2626;
  transform: translateY(-1px);
}
</style>

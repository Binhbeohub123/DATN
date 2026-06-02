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
  </div>
</template>

<script setup>
import { ref, computed, onErrorCaptured } from 'vue'
import { useRoute } from 'vue-router'
import ToastContainer from '@/components/ToastContainer.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'

const route = useRoute()
const showFabTheme = computed(() => {
  const path = route.path
  if (path === '/' || path.startsWith('/admin')) return false
  const withBar = ['/movie', '/seats', '/combo', '/checkout', '/profile', '/transaction-history', '/my-tickets']
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
</style>

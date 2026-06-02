<template>
  <div class="app" :class="{ dark: isDarkMode }">
    <div v-if="hasError" class="error-screen">
      <h1>⚠️ Lỗi tải ứng dụng</h1>
      <p>{{ errorMessage }}</p>
      <button @click="reloadPage">Tải lại trang</button>
    </div>
    <router-view v-else />
    <!-- Global toast notifications -->
    <ToastContainer />
  </div>
</template>

<script setup>
import { ref, onMounted, onErrorCaptured } from 'vue'
import ToastContainer from '@/components/ToastContainer.vue'

const isDarkMode = ref(true)
const hasError = ref(false)
const errorMessage = ref('')

onMounted(() => {
  const saved = localStorage.getItem('poly_theme')
  if (saved) {
    isDarkMode.value = saved === 'dark'
  }
})

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
  font-family: 'Be Vietnam Pro', 'Noto Sans Vietnamese', 'Nunito', system-ui, -apple-system, sans-serif;
  background: #f8fafc; color: #1e2937;
  transition: background 0.3s, color 0.3s;
}
.app.dark { background: #0f172a; color: #f1f5f9; }

.error-screen {
  width: 100%; height: 100vh;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 24px; background: #f8fafc; color: #1e2937;
}
.error-screen h1 { font-size: 32px; }
.error-screen p  { font-size: 16px; color: #ef4444; max-width: 500px; text-align: center; }
.error-screen button {
  padding: 12px 24px; background: #ffd700; color: #0f172a;
  border: none; border-radius: 8px; font-weight: 700; cursor: pointer; transition: all .2s;
}
.error-screen button:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(255,215,0,.3); }
</style>

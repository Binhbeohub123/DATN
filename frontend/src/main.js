import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/themeStore'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
useThemeStore().init()
app.use(router)

// Global error handler — prevents uncaught errors from showing blank screen
app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue Error]', err, info)
}

// Suppress unhandled promise rejections from API calls during init
window.addEventListener('unhandledrejection', (event) => {
  // Suppress network errors when backend is not running — they are non-fatal
  const msg = event.reason?.message || ''
  if (msg.includes('Network Error') || msg.includes('ERR_CONNECTION_REFUSED')) {
    event.preventDefault()
    console.warn('[App] Backend not reachable — running in offline mode')
  }
})

app.mount('#app')

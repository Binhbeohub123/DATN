import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json; charset=utf-8',
    'Accept': 'application/json; charset=utf-8',
  },
})

// ── Request interceptor: inject JWT ──────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error) => Promise.reject(error)
)

// ── Response interceptor: show global toast on 4xx/5xx ───────
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response) {
      const { status } = error.response

      if (status === 401) {
        const url = error.config?.url || ''
        const isAuthRequest = url.includes('/auth/login') || url.includes('/auth/register')
        const isProfileFetch = url.includes('/auth/profile')
        if (!isAuthRequest) {
          localStorage.removeItem('token')
          localStorage.removeItem('user')
        }
        if (!isProfileFetch && !window.location.pathname.startsWith('/auth')) {
          // Lazy-import to avoid circular dep at module load time
          const { default: router } = await import('@/router/index.js')
          // Pass ?expired=true so AuthPage can show the session-expired message
          router.push({ path: '/auth', query: { expired: 'true' } }).catch(() => {})
        }
      }

      if (status === 403) {
        const body = error.response?.data
        // Locked account — show the full-screen "bị khóa" notice everywhere
        if (body?.code === 'ACCOUNT_LOCKED') {
          const { useAuthStore } = await import('@/stores/authStore')
          useAuthStore().setAccountLocked(body.lyDoKhoa || body.message || '')
        } else {
          // Show toast for forbidden
          _showErrorToast('Bạn không có quyền thực hiện thao tác này')
        }
      }

      if (status >= 500) {
        _showErrorToast('Lỗi máy chủ. Vui lòng thử lại sau.')
        console.error('[API] Server error:', error.response?.data)
      }

      // 4xx (except 401 handled above) — caller decides whether to show message
    } else if (error.request) {
      console.warn('[API] Network error – backend may not be running')
    }

    return Promise.reject(error)
  }
)

/** Lazy helper — avoids importing useToast at module load time */
function _showErrorToast(msg) {
  try {
    // Dynamic import to avoid circular dependency
    import('@/composables/useToast.js').then(({ useToast }) => {
      useToast().error(msg)
    })
  } catch { /* non-fatal */ }
}

export default api

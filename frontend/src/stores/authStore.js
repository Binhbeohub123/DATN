import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  // ── State ──
  const token        = ref(localStorage.getItem('token') || null)
  const user         = ref(null)
  const loading      = ref(false)
  const error        = ref('')
  // Stores the path the user tried to visit before being redirected to /auth
  const redirectPath = ref(null)

  // ── Getters ──
  const isLoggedIn = computed(() => !!token.value)

  const userRole = computed(() => {
    if (!token.value) return ''
    try {
      const payload    = token.value.split('.')[1]
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
      const decoded    = JSON.parse(decodeURIComponent(escape(atob(normalized))))
      return (decoded?.role || '').replace('ROLE_', '').toUpperCase()
    } catch {
      return ''
    }
  })

  const isAdmin = computed(() => userRole.value === 'ADMIN')

  const memberLevel = computed(() => user.value?.capDoThanhVien || 'Thường')

  const userInitials = computed(() => {
    const name = user.value?.hoTen || user.value?.email || '?'
    return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
  })

  // ── Actions ──
  async function fetchProfile() {
    if (!token.value) { user.value = null; return }
    loading.value = true
    try {
      const res  = await api.get('/auth/profile')
      user.value = res.data
    } catch (err) {
      if (err.response?.status === 401) logout()
      else user.value = null
    } finally {
      loading.value = false
    }
  }

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function logout() {
    token.value    = null
    user.value     = null
    redirectPath.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  /** Save where the user was trying to go before auth redirect */
  function setRedirectPath(path) {
    redirectPath.value = path
  }

  /** Consume the redirect path (clears it after reading) */
  function popRedirectPath() {
    const p = redirectPath.value
    redirectPath.value = null
    return p
  }

  async function login(email, password) {
    loading.value = true
    error.value   = ''
    try {
      const res = await api.post('/auth/login', { email, password })
      const jwt = typeof res.data === 'string' ? res.data : res.data?.token
      if (!jwt) throw new Error('No token received')
      setToken(jwt)
      await fetchProfile()
      return true
    } catch (err) {
      error.value = err.response?.data?.message || err.response?.data || 'Đăng nhập thất bại'
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(data) {
    loading.value = true
    error.value   = ''
    try {
      const res = await api.post('/auth/register', data)
      return res.data
    } catch (err) {
      error.value = err.response?.data?.message || 'Đăng ký thất bại'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Initialise on store creation — silently restore session
  if (token.value) {
    fetchProfile().catch(() => {})
  }

  return {
    token, user, loading, error, redirectPath,
    isLoggedIn, userRole, isAdmin, memberLevel, userInitials,
    fetchProfile, setToken, logout, setRedirectPath, popRedirectPath,
    login, register,
  }
})

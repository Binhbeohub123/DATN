import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  function normalizeToken(rawToken) {
    if (typeof rawToken !== 'string') return null
    const trimmed = rawToken.trim()
    if (!trimmed) return null
    // Handle backend responses that may wrap JWT in quotes.
    return trimmed.replace(/^"+|"+$/g, '')
  }

  // ── State ──
  const token        = ref(localStorage.getItem('token') || null)
  const user         = ref(null)
  const loading      = ref(false)
  const error        = ref('')
  // Stores the path the user tried to visit before being redirected to /auth
  const redirectPath = ref(null)

  // ── Account-lock state (set when backend returns ACCOUNT_LOCKED) ──
  const locked     = ref(false)
  const lockReason = ref('')

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

  const isAdmin  = computed(() => userRole.value === 'ADMIN')
  const isStaff  = computed(() => userRole.value === 'STAFF')

  const memberLevel = computed(() => user.value?.capDoThanhVien || 'Thường')

  const userInitials = computed(() => {
    // Avatar mặc định theo role: ADMIN → AD, STAFF → NV, khách → chữ cái đầu tên
    const role = userRole.value
    if (role === 'ADMIN') return 'AD'
    if (role === 'STAFF') return 'NV'
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
      // Guard: even if a locked profile somehow returns, show the lock screen
      if (user.value && user.value.trangThai === false) {
        setAccountLocked(user.value.lyDoKhoa)
      }
    } catch (err) {
      if (err.response?.status === 401) logout()
      else user.value = null
    } finally {
      loading.value = false
    }
  }

  function setToken(newToken) {
    const normalized = normalizeToken(newToken)
    if (!normalized) {
      token.value = null
      localStorage.removeItem('token')
      return
    }
    token.value = normalized
    localStorage.setItem('token', normalized)
  }

  /** Mark the account as locked and store the reason shown to the user. */
  function setAccountLocked(reason) {
    locked.value = true
    lockReason.value = reason || ''
  }

  function logout() {
    token.value    = null
    user.value     = null
    redirectPath.value = null
    locked.value   = false
    lockReason.value = ''
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
    locked, lockReason,
    isLoggedIn, userRole, isAdmin, isStaff, memberLevel, userInitials,
    fetchProfile, setToken, setAccountLocked, logout, setRedirectPath, popRedirectPath,
    login, register,
  }
})

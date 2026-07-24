import { createRouter, createWebHistory } from 'vue-router'

// ── Helpers ──────────────────────────────────────────────────
function decodeToken(token) {
  try {
    const payload    = token.split('.')[1]
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(decodeURIComponent(escape(atob(normalized))))
  } catch {
    return null
  }
}

function getUserRole() {
  const token   = localStorage.getItem('token')
  const payload = token ? decodeToken(token) : null
  return (payload?.role || '').replace('ROLE_', '').toUpperCase()
}

// ── Routes ────────────────────────────────────────────────────
const routes = [
  { path: '/',        name: 'home',         component: () => import('@/view/home.vue'),                    meta: { requiresAuth: false } },
  { path: '/movies',  name: 'movies',       component: () => import('@/view/home.vue'),                    meta: { requiresAuth: false } },
  { path: '/phim/:id',name: 'movie-detail', component: () => import('@/view/MovieDetailPage.vue'),         meta: { requiresAuth: false }, props: true },
  { path: '/rap-chieu/:id', name: 'cinema-detail', component: () => import('@/view/CinemaDetailPage.vue'), meta: { requiresAuth: false }, props: true },
  { path: '/auth',    name: 'auth',         component: () => import('@/Auth/AuthPage.vue'),                meta: { requiresAuth: false } },
  { path: '/seat-selection/:showtimeId', name: 'seat-selection', component: () => import('@/view/SeatSelectionPage.vue'), meta: { requiresAuth: true }, props: true },
  { path: '/combo',   name: 'combo',        component: () => import('@/view/ComboPage.vue'),               meta: { requiresAuth: true } },
  {
    path: '/checkout',
    name: 'checkout',
    component: () => import('@/view/CheckoutPage.vue'),
    meta: { requiresAuth: true },
    beforeEnter: async (to, from, next) => {
      // Allow retry-payment mode (bookingId query param present)
      if (to.query?.bookingId) { next(); return }
      // Allow normal flow only if seats have been selected
      const { useBookingStore } = await import('@/stores/bookingStore')
      const bookingStore = useBookingStore()
      if (!bookingStore.selectedSeats || bookingStore.selectedSeats.length === 0) {
        // No booking data — redirect to movies rather than showing a broken checkout
        next('/movies')
        return
      }
      next()
    },
  },
  { path: '/payment-result/:bookingId', name: 'payment-result', component: () => import('@/view/PaymentResultPage.vue'), meta: { requiresAuth: true }, props: true },
  { path: '/payment-cancel', name: 'payment-cancel', component: () => import('@/view/PaymentCancelPage.vue'), meta: { requiresAuth: false } },
  { path: '/profile', name: 'profile',      component: () => import('@/view/UserProfilePage.vue'),         meta: { requiresAuth: true } },
  { path: '/my-tickets', name: 'my-tickets', component: () => import('@/view/MyTicketsPage.vue'),          meta: { requiresAuth: true } },
  { path: '/transaction-history', name: 'transaction-history', component: () => import('@/view/TransactionHistoryPage.vue'), meta: { requiresAuth: true } },
  { path: '/admin', redirect: '/admin/dashboard' },
  {
    path: '/admin/:tab',
    name: 'admin',
    component: () => import('@/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  // ── 404 catch-all ────────────────────────────────────────────
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/view/NotFoundPage.vue'), meta: { requiresAuth: false } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0, behavior: 'smooth' }
  },
})

// ── Navigation guard ──────────────────────────────────────────
router.beforeEach(async (to, from, next) => {
  // ── OAuth callback: token in query ──
  const oauthToken = to.query?.token
  if (oauthToken) {
    localStorage.setItem('token', oauthToken)
    const role = getUserRole()
    next({ path: role === 'ADMIN' ? '/admin/dashboard' : '/', replace: true })
    return
  }

  const token       = localStorage.getItem('token')
  const requiresAuth = to.meta?.requiresAuth !== false
  const role        = getUserRole()

  // ── JWT expiry check ──
  if (token) {
    const payload = decodeToken(token)
    if (payload && payload.exp && Math.floor(Date.now() / 1000) > payload.exp) {
      // Token expired — clear credentials and redirect to auth with notice
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (to.path !== '/auth') {
        next({ path: '/auth', query: { expired: 'true' }, replace: true })
        return
      }
    }
  }

  // ── Unauthenticated user hitting a protected route ──
  if (requiresAuth && !localStorage.getItem('token')) {
    // Save intended destination so AuthPage can redirect back after login
    const { useAuthStore } = await import('@/stores/authStore')
    const authStore = useAuthStore()
    if (to.fullPath !== '/auth') authStore.setRedirectPath(to.fullPath)
    next('/auth')
    return
  }

  // ── Wrong role for route (e.g. non-admin hitting /admin) ──
  if (to.meta?.role && role !== to.meta.role) {
    // Show toast then redirect home
    import('@/composables/useToast.js').then(({ useToast }) => {
      useToast().error('Bạn không có quyền truy cập trang này')
    })
    next('/')
    return
  }

  // ── Logged-in user hitting /auth ──
  if (!requiresAuth && localStorage.getItem('token') && to.path === '/auth') {
    next(role === 'ADMIN' ? '/admin/dashboard' : '/')
    return
  }

  next()
})

export default router

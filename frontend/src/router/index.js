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
  { path: '/phim-dangchieu', name: 'phim-dangchieu', component: () => import('@/view/MovieListPage.vue'), meta: { requiresAuth: false } },
  { path: '/phim-sapchieu',  name: 'phim-sapchieu',  component: () => import('@/view/MovieListPage.vue'), meta: { requiresAuth: false } },
  { path: '/rap/:id', name: 'cinema-detail', component: () => import('@/view/CinemaDetailPage.vue'),        meta: { requiresAuth: false }, props: true },
  { path: '/auth',    name: 'auth',         component: () => import('@/Auth/AuthPage.vue'),                meta: { requiresAuth: false } },
  { path: '/seat-selection/:showtimeId', name: 'seat-selection', component: () => import('@/view/SeatSelectionPage.vue'), meta: { requiresAuth: true }, props: true },
  { path: '/combo',   name: 'combo',        component: () => import('@/view/ComboPage.vue'),               meta: { requiresAuth: true } },
  {
    path: '/checkout',
    name: 'checkout',
    component: () => import('@/view/CheckoutPage.vue'),
    meta: { requiresAuth: true },
    beforeEnter: async (to, from, next) => {
      // Allow retry-payment mode (bookingId query param present) — không đụng vào
      if (to.query?.bookingId) { next(); return }
      const [{ useBookingStore }, apiMod, toastMod, storeMod] = await Promise.all([
        import('@/stores/bookingStore'),
        import('@/services/api'),
        import('@/composables/useToast'),
        import('@/stores/bookingStore'),
      ])
      const bookingStore = useBookingStore()
      // Allow normal flow only if seats have been selected
      if (bookingStore.selectedSeats && bookingStore.selectedSeats.length > 0) {
        next()
        return
      }

      // F5 recovery: store rỗng nhưng sessionStorage snapshot còn →
      // verify SERVER-SIDE rằng toàn bộ ghế vẫn đang bị CHÍNH user này khoá,
      // rồi mới populate lại store và cho vào trang.
      const hadSnapshot = storeMod.hasBookingSnapshot()
      const kept = await bookingStore.hydrateFromSnapshot({ verify: true })
      if (kept && kept.length > 0) {
        next()
        return
      }

      // Không snapshot (tab mới/đóng hẳn) → im lặng như hành vi cũ;
      // Snapshot có mà lock đã hết → thông báo rõ ràng rồi về chọn lại.
      bookingStore.clearSnapshot()
      if (hadSnapshot) {
        toastMod.useToast().error('Phiên giữ ghế đã hết hạn, vui lòng chọn lại')
      }
      next('/movies')
    },
  },
  { path: '/payment-result/:bookingId', name: 'payment-result', component: () => import('@/view/PaymentResultPage.vue'), meta: { requiresAuth: true }, props: true },
  { path: '/payment-cancel/:maDatVe?', name: 'payment-cancel', component: () => import('@/view/PaymentCancelPage.vue'), meta: { requiresAuth: false } },
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
  // ── Staff routes ────────────────────────────────────────────
  { path: '/staff', redirect: '/staff/dashboard' },
  { path: '/staff/dashboard', name: 'staff-dashboard', component: () => import('@/view/StaffDashboardPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/staff/pos', name: 'staff-pos', component: () => import('@/view/StaffPosPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/staff/checkin', name: 'staff-checkin', component: () => import('@/view/StaffCheckinPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/staff/report', name: 'staff-report', component: () => import('@/view/StaffReportPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
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
    // Update the reactive store (ref + localStorage), otherwise isLoggedIn
    // stays false and the header keeps showing "Đăng nhập/Đăng ký" until F5.
    const { useAuthStore } = await import('@/stores/authStore')
    const authStore = useAuthStore()
    authStore.setToken(oauthToken)
    authStore.fetchProfile().catch(() => {})
    const role = getUserRole()
    const dest = role === 'ADMIN' ? '/admin/dashboard' : role === 'STAFF' ? '/staff/dashboard' : '/'
    next({ path: dest, replace: true })
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
    // Staff routes redirect to shared /auth, not a staff-specific page
    if (to.path.startsWith('/staff')) {
      next('/auth')
      return
    }
    // Save intended destination so AuthPage can redirect back after login
    const { useAuthStore } = await import('@/stores/authStore')
    const authStore = useAuthStore()
    if (to.fullPath !== '/auth') authStore.setRedirectPath(to.fullPath)
    next('/auth')
    return
  }

  // ── Wrong role for route (e.g. non-admin hitting /admin) ──
  if (to.meta?.role) {
    const requiredRole = to.meta.role
    const hasAccess = role === requiredRole || (requiredRole === 'STAFF' && role === 'ADMIN')
    if (!hasAccess) {
      // Show toast then redirect
      import('@/composables/useToast.js').then(({ useToast }) => {
        useToast().error('Bạn không có quyền truy cập trang này')
      })
      if (to.path.startsWith('/staff')) { next('/auth'); return }
      next('/')
      return
    }
  }

  // ── Logged-in user hitting /auth ──
  if (!requiresAuth && localStorage.getItem('token') && to.path === '/auth') {
    const dest = role === 'ADMIN' ? '/admin/dashboard' : role === 'STAFF' ? '/staff/dashboard' : '/'
    next(dest)
    return
  }

  next()
})

export default router

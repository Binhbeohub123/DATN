<template>
  <div class="admin-container">
    <!-- SIDEBAR -->
    <aside class="sidebar">
      <!-- Logo -->
      <div class="sidebar-logo">
        <svg class="logo-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="22" height="22">
          <rect x="2" y="2" width="20" height="20" rx="2.5"/>
          <path d="M7 2v20M17 2v20M2 12h20M2 7h5M17 7h5M2 17h5M17 17h5"/>
        </svg>
        <span class="logo-text">PolyCinema</span>
        <span class="logo-badge chip-glass chip-glass--gold">ADMIN</span>
      </div>

      <!-- Nav -->
      <nav class="sidebar-menu">
        <div class="menu-section">TỔNG QUAN</div>
        <RouterLink to="/admin/dashboard" class="menu-item" active-class="active" @click="onNavClick">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
          <span>Tổng Quan</span>
        </RouterLink>

        <div class="menu-section">QUẢN LÝ</div>
        <RouterLink to="/admin/movies" class="menu-item" active-class="active" @click="onNavClick">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M7 4v16M17 4v16M2 9h5M17 9h5M2 15h5M17 15h5"/></svg>
          <span>Quản Lý Phim</span>
        </RouterLink>
        <RouterLink to="/admin/schedule" class="menu-item" active-class="active" @click="onNavClick">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>
          <span>Lịch Chiếu</span>
        </RouterLink>
        <RouterLink to="/admin/tickets" class="menu-item" active-class="active" @click="onNavClick">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v1a2 2 0 0 0 0 4v1a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2v-1a2 2 0 0 0 0-4V9z"/></svg>
          <span>Đặt Vé</span>
          <span class="menu-badge" v-if="pendingTickets > 0">{{ pendingTickets }}</span>
        </RouterLink>
        <RouterLink to="/admin/customers" class="menu-item" active-class="active" @click="onNavClick">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="8" r="3"/><path d="M2 20c0-4 2.7-7 6-7h4c3.3 0 6 3 6 7"/><circle cx="17" cy="6" r="2.5"/><path d="M21 18c0-3-1.8-5-4-5"/></svg>
          <span>Khách Hàng</span>
        </RouterLink>
        <RouterLink to="/admin/cinemas" class="menu-item" active-class="active" @click="onNavClick">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          <span>Rạp Chiếu</span>
        </RouterLink>

        <div class="menu-section">HỆ THỐNG</div>
        <RouterLink to="/admin/promo" class="menu-item" active-class="active" @click="onNavClick">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
          <span>Khuyến Mãi</span>
        </RouterLink>
        <RouterLink to="/admin/reports" class="menu-item" active-class="active" @click="onNavClick">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
          <span>Báo Cáo</span>
        </RouterLink>
        <RouterLink to="/admin/settings" class="menu-item" active-class="active" @click="onNavClick">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
          <span>Cài Đặt</span>
        </RouterLink>
      </nav>

      <!-- Footer user card -->
      <div class="sidebar-footer">
        <div class="admin-info glass-card">
          <div class="admin-avatar">AD</div>
          <div class="admin-info-text">
            <div class="admin-name">Admin PolyCinema</div>
            <div class="admin-role">Quản Trị Viên</div>
          </div>
        </div>
        <button type="button" class="btn-ghost home-btn" @click="goHome">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
          Trang chủ
        </button>
        <button type="button" class="btn-ghost logout-btn" @click="logout">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          Đăng Xuất
        </button>
      </div>
    </aside>

    <!-- MAIN CONTENT -->
    <div class="main">
      <div
        v-if="shell.showDatePanel || shell.showNotifPanel"
        class="admin-panel-backdrop"
        aria-hidden="true"
        @click="shell.closePanels()"
      />

      <!-- Topbar -->
      <header class="topbar">
        <div class="topbar-head">
          <p class="eyebrow">{{ currentPage }}</p>
          <h1>{{ pageTitles[currentPage] }}</h1>
        </div>

        <div class="topbar-toolbar" ref="topbarToolbarRef">
          <div class="topbar-toolbar-start">
            <button type="button" class="btn-ghost home-btn--topbar" title="Về trang chủ" @click="goHome">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
              <span class="home-btn__label">Trang chủ</span>
            </button>
            <ThemeToggle />
            <div class="topbar-search-wrap">
              <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              <input
                v-model="shell.searchQuery"
                type="search"
                class="search-input"
                placeholder="Tìm phim, vé, khách..."
                aria-label="Tìm kiếm"
                @keydown.enter.prevent="runSearch"
              />
              <button type="button" class="search-go" title="Tìm" @click="runSearch">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14"><polyline points="9 18 15 12 9 6"/></svg>
              </button>
            </div>
          </div>

          <div class="topbar-toolbar-end">
            <div class="topbar-popover-wrap">
              <button type="button" class="date-tag chip-glass" @click.stop="shell.toggleDatePanel()">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="13" height="13"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>
                <span class="date-tag__text">{{ shell.formattedFilterDate }}</span>
              </button>
              <div v-if="shell.showDatePanel" class="topbar-popover date-popover glass-card" @click.stop>
                <p class="popover-title">Chọn ngày lọc lịch chiếu</p>
                <input v-model="shell.filterDate" type="date" class="popover-date-input" />
                <div class="popover-actions">
                  <button type="button" class="btn-ghost popover-btn" @click.stop="shell.showDatePanel = false">Đóng</button>
                  <button type="button" class="btn-primary popover-btn" @click.stop="applyDateFilter">Áp dụng</button>
                  <button type="button" class="btn-ghost popover-btn" @click.stop="setTodayAndApply">Hôm nay</button>
                </div>
              </div>
            </div>

            <div class="topbar-popover-wrap">
              <button type="button" class="notif" aria-label="Thông báo" @click.stop="shell.toggleNotifPanel()">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="18" height="18"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
                <span v-if="shell.pendingCount > 0" class="notif-dot">{{ shell.pendingCount > 9 ? '9+' : shell.pendingCount }}</span>
              </button>
              <div v-if="shell.showNotifPanel" class="topbar-popover notif-popover glass-card" @click.stop>
                <div class="popover-head">
                  <p class="popover-title">Thông báo</p>
                  <button type="button" class="popover-link" @click="goPendingTickets">Chờ TT ({{ shell.pendingCount }})</button>
                </div>
                <div v-if="shell.loadingNotif" class="popover-empty">Đang tải...</div>
                <div v-else-if="shell.notifications.length === 0" class="popover-empty">Không có thông báo mới</div>
                <ul v-else class="notif-list">
                  <li v-for="n in shell.notifications" :key="n.id">
                    <button type="button" class="notif-item" @click="openNotif(n)">
                      <span :class="['notif-item__badge', n.pending ? 'notif-item__badge--pending' : '']">
                        {{ n.pending ? '!' : '✓' }}
                      </span>
                      <span class="notif-item__body">
                        <span class="notif-item__title">{{ n.title }} · {{ n.maDatVe }}</span>
                        <span class="notif-item__sub">{{ n.subtitle }}</span>
                      </span>
                    </button>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </header>

      <div class="content">
        <DashboardPage v-if="currentPage === 'dashboard'" />
        <MoviesPage    v-if="currentPage === 'movies'" />
        <SchedulePage  v-if="currentPage === 'schedule'" />
        <TicketsPage   v-if="currentPage === 'tickets'" />
        <CustomersPage v-if="currentPage === 'customers'" />
        <CinemasPage   v-if="currentPage === 'cinemas'" />
        <PromoPage     v-if="currentPage === 'promo'" />
        <ReportsPage   v-if="currentPage === 'reports'" />
        <SettingsPage  v-if="currentPage === 'settings'" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { useAdminShellStore } from '@/stores/adminShellStore'
import { useAuthStore } from '@/stores/authStore'
import { useToast } from '@/composables/useToast'
import DashboardPage  from './components/DashboardPage.vue'
import MoviesPage     from './components/MoviesPage.vue'
import SchedulePage   from './components/SchedulePage.vue'
import TicketsPage    from './components/TicketsPage.vue'
import CustomersPage  from './components/CustomersPage.vue'
import CinemasPage    from './components/CinemasPage.vue'
import PromoPage      from './components/PromoPage.vue'
import ReportsPage    from './components/ReportsPage.vue'
import SettingsPage   from './components/SettingsPage.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const shell = useAdminShellStore()
const toast = useToast()
const topbarToolbarRef = ref(null)

const pendingTickets = ref(0)

const pageTitles = {
  dashboard: 'Tổng Quan',
  movies:    'Quản Lý Phim',
  schedule:  'Lịch Chiếu',
  tickets:   'Quản Lý Đặt Vé',
  customers: 'Khách Hàng',
  cinemas:   'Quản Lý Rạp Chiếu',
  promo:     'Khuyến Mãi',
  reports:   'Báo Cáo',
  settings:  'Cài Đặt',
}

const VALID_PAGES = Object.keys(pageTitles)

const currentPage = computed(() => {
  const tab = String(route.params.tab || 'dashboard')
  return VALID_PAGES.includes(tab) ? tab : 'dashboard'
})

function onNavClick() {
  shell.closePanels()
}

function switchPage(page) {
  if (!VALID_PAGES.includes(page)) return
  shell.closePanels()
  const target = `/admin/${page}`
  if (route.path !== target) {
    router.push(target)
  }
}

const pageLabels = {
  movies: 'Quản lý phim',
  schedule: 'Lịch chiếu',
  tickets: 'Đặt vé',
  customers: 'Khách hàng',
}

function runSearch() {
  const q = shell.searchQuery.trim()
  if (!q) {
    toast.info('Nhập từ khóa (tên phim, email, mã vé...) rồi nhấn Enter hoặc ⌕')
    return
  }
  const page = shell.applyGlobalSearch()
  if (page) {
    switchPage(page)
    toast.success(`Đang tìm trên ${pageLabels[page] || page}`)
  }
}

function applyDateFilter() {
  const page = shell.applyFilterDate(shell.filterDate)
  switchPage(page)
  toast.success(`Đã lọc lịch chiếu ngày ${shell.formattedFilterDate}`)
}

function setTodayAndApply() {
  shell.filterDate = shell.toIsoDate(new Date())
  applyDateFilter()
}

function openNotif(n) {
  shell.openTicketFromNotif(n)
  switchPage('tickets')
}

function goPendingTickets() {
  shell.goToPendingTickets()
  switchPage('tickets')
}

function goHome() {
  shell.closePanels()
  router.push('/')
}

function logout() {
  authStore.logout()
  router.push('/auth')
}

function onEscapeKey(e) {
  if (e.key === 'Escape') shell.closePanels()
}

watch(() => shell.pendingCount, (n) => {
  pendingTickets.value = n
})

watch(
  () => route.params.tab,
  (tab) => {
    if (tab && !VALID_PAGES.includes(String(tab))) {
      router.replace('/admin/dashboard')
    }
    shell.closePanels()
  },
)

onMounted(async () => {
  shell.closePanels()
  if (!VALID_PAGES.includes(String(route.params.tab || 'dashboard'))) {
    router.replace('/admin/dashboard')
  }
  document.addEventListener('keydown', onEscapeKey)
  await shell.refreshPendingCount()
  pendingTickets.value = shell.pendingCount
})

onUnmounted(() => {
  document.removeEventListener('keydown', onEscapeKey)
})
</script>

<style>
@import '../assets/tokens.css';
@import '../assets/cinema.css';
@import '../assets/admin-gold.css';

/* ── Shell layout variables ── */
:root { --admin-sidebar: 240px; }

/* ── Grid layout ── */
.admin-container {
  width: 100%;
  min-height: 100vh;
  display: grid;
  grid-template-columns: var(--admin-sidebar) minmax(0, 1fr);
  align-items: start;
  background: var(--void);
}

/* ── Sidebar ── */
.sidebar {
  grid-column: 1;
  grid-row: 1;
  position: sticky;
  top: 0;
  z-index: 500;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--void);
  border-right: 1px solid var(--glass-border);
  overflow: hidden;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 20px;
  border-bottom: 1px solid var(--glass-border);
  flex-shrink: 0;
}
.logo-icon-svg { color: var(--gold); flex-shrink: 0; }
.logo-text {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
}
.logo-badge { margin-left: auto; font-size: 10px; letter-spacing: 0.1em; }

.sidebar-menu {
  flex: 1;
  padding: 12px 8px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--glass-border) transparent;
}
.menu-section {
  padding: 20px 12px 6px;
  font-family: var(--font-ui);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--text-ghost);
}
.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 40px;
  padding: 10px 16px;
  margin: 2px 0;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-family: var(--font-ui);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: all 300ms var(--ease-out);
}
.menu-item:hover { background: var(--glass-bg); color: var(--text-primary); }
.menu-item.active { background: var(--gold-soft); color: var(--gold); font-weight: 600; }
.menu-item > svg { flex-shrink: 0; opacity: 0.75; }
.menu-item.active > svg,
.menu-item:hover > svg { opacity: 1; }
.menu-badge {
  margin-left: auto;
  min-width: 20px;
  padding: 2px 6px;
  border-radius: var(--radius-pill);
  background: #ef4444;
  color: #fff;
  font-family: var(--font-ui);
  font-size: 10px;
  font-weight: 700;
  text-align: center;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--glass-border);
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}
.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  margin-bottom: 4px;
}
.admin-avatar {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--gold);
  color: #000;
  font-family: var(--font-display);
  font-size: 13px;
  font-weight: 700;
}
.admin-info-text { min-width: 0; }
.admin-name {
  font-family: var(--font-ui);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.admin-role {
  font-family: var(--font-ui);
  font-size: 11px;
  color: var(--text-secondary);
}
.home-btn, .logout-btn {
  width: 100%;
  justify-content: center;
}

/* ── Main column ── */
.main {
  grid-column: 2;
  grid-row: 1;
  min-width: 0;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}
.admin-panel-backdrop {
  position: absolute;
  inset: 0;
  z-index: 40;
  background: transparent;
  cursor: default;
}

/* ── Topbar ── */
.topbar {
  position: sticky;
  top: 0;
  z-index: 45;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 24px;
  background: color-mix(in srgb, var(--void) 80%, transparent);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--glass-border);
}
.topbar-head { min-width: 0; }
.eyebrow {
  margin: 0 0 2px;
  font-family: var(--font-ui);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--text-ghost);
}
.topbar h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}
.topbar-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.topbar-toolbar-start {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  flex: 1 1 240px;
  min-width: 0;
}
.topbar-toolbar-end {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.topbar-popover-wrap { position: relative; flex-shrink: 0; }
.home-btn--topbar { white-space: nowrap; }
.home-btn__label { font-size: 12px; }

/* Search bar */
.topbar-search-wrap {
  display: flex;
  align-items: center;
  flex: 1 1 180px;
  min-width: 0;
  width: min(300px, 100%);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-pill);
  overflow: hidden;
  transition: border-color 200ms var(--ease-out);
}
.topbar-search-wrap:focus-within { border-color: var(--gold); }
.search-icon { margin-left: 12px; color: var(--text-secondary); flex-shrink: 0; }
.search-input {
  flex: 1;
  min-width: 0;
  min-height: 38px;
  padding: 8px 10px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-family: var(--font-ui);
  font-size: 13px;
  outline: none;
}
.search-input::placeholder { color: var(--text-ghost); }
.search-go {
  min-width: 38px;
  min-height: 38px;
  border: none;
  border-left: 1px solid var(--glass-border);
  background: transparent;
  color: var(--gold);
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: background 200ms var(--ease-out);
}
.search-go:hover { background: var(--gold-soft); }

/* Date tag */
.date-tag {
  cursor: pointer;
  max-width: min(220px, 42vw);
  min-height: 36px;
  border: none;
  background: transparent;
}
.date-tag__text { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Notif bell */
.notif {
  position: relative;
  min-width: 38px;
  min-height: 38px;
  padding: 0 9px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-sm);
  border: 1px solid var(--glass-border);
  background: var(--glass-bg);
  cursor: pointer;
  color: var(--text-secondary);
  transition: border-color 200ms var(--ease-out), color 200ms var(--ease-out);
}
.notif:hover { border-color: var(--gold); color: var(--gold); }
.notif-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 3px;
  border-radius: var(--radius-pill);
  background: #ef4444;
  color: #fff;
  font-family: var(--font-ui);
  font-size: 9px;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
}

/* Popovers */
.topbar-popover {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 60;
  min-width: 280px;
  max-width: min(340px, calc(100vw - 40px));
  padding: 16px;
  box-shadow: var(--shadow-lg);
  pointer-events: auto;
}
.notif-popover { max-height: 380px; overflow-y: auto; }
.popover-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}
.popover-title {
  margin: 0;
  font-family: var(--font-ui);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.popover-link {
  border: none;
  background: none;
  color: var(--gold);
  font-family: var(--font-ui);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.popover-date-input {
  width: 100%;
  margin-bottom: 10px;
}
.popover-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 12px; }
.popover-btn { padding: 8px 14px; min-height: 36px; font-size: 13px; }
.popover-empty {
  padding: 16px 8px;
  text-align: center;
  color: var(--text-secondary);
  font-family: var(--font-ui);
  font-size: 13px;
}
.notif-list { list-style: none; margin: 0; padding: 0; }
.notif-item {
  display: flex;
  width: 100%;
  gap: 10px;
  padding: 10px 8px;
  border: none;
  border-bottom: 1px solid var(--glass-border);
  background: transparent;
  text-align: left;
  cursor: pointer;
  font-family: var(--font-ui);
  transition: background 150ms var(--ease-out);
}
.notif-item:last-child { border-bottom: none; }
.notif-item:hover { background: var(--glass-bg); }
.notif-item__badge {
  width: 26px; height: 26px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: var(--radius-sm);
  background: var(--glass-bg);
  color: var(--text-secondary);
  font-size: 11px;
  font-weight: 700;
}
.notif-item__badge--pending { background: rgba(245,158,11,0.15); color: #f59e0b; }
.notif-item__body { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.notif-item__title { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.notif-item__sub { font-size: 11px; color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Content area */
.content { flex: 1; padding: 24px; }

/* ── Responsive ── */
@media (max-width: 1100px) {
  :root { --admin-sidebar: 200px; }
}
@media (max-width: 900px) {
  :root { --admin-sidebar: 68px; }
  .logo-text, .logo-badge, .menu-section,
  .menu-item > span, .admin-info-text,
  .home-btn > span:not(.logo-badge), .logout-btn > span { display: none; }
  .sidebar-logo { justify-content: center; padding: 0 12px; }
  .menu-item { justify-content: center; padding: 10px 12px; }
  .menu-badge { position: absolute; top: 6px; right: 6px; }
  .sidebar-footer { padding: 10px; align-items: center; }
  .admin-info { padding: 8px; justify-content: center; }
  .home-btn, .logout-btn { justify-content: center; padding: 10px; min-width: 38px; }
}
@media (max-width: 640px) {
  .admin-container { display: flex; flex-direction: column; }
  .sidebar { position: fixed; bottom: 0; left: 0; right: 0; height: 60px; width: 100%; z-index: 120; flex-direction: row; border-right: none; border-top: 1px solid var(--glass-border); }
  .sidebar-logo, .sidebar-footer, .menu-section { display: none; }
  .sidebar-menu { display: flex; flex-direction: row; padding: 6px; overflow-x: auto; gap: 4px; }
  .menu-item { min-width: 44px; min-height: 44px; flex: 0 0 auto; border-radius: var(--radius-sm); }
  .main { padding-bottom: 68px; }
  .topbar { padding: 12px 16px; }
  .content { padding: 16px; }
}
</style>
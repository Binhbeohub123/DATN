<template>
  <div class="admin-container">
    <!-- SIDEBAR -->
    <div class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24"><path d="M18 3v2h-2V3H8v2H6V3H4v18h2v-2h2v2h8v-2h2v2h2V3h-2zM8 17H6v-2h2v2zm0-4H6v-2h2v2zm0-4H6V7h2v2zm10 8h-2v-2h2v2zm0-4h-2v-2h2v2zm0-4h-2V7h2v2z"/></svg>
        </div>
        <span class="logo-text">Poly<span>Cinema</span></span>
        <span class="logo-admin">ADMIN</span>
      </div>

      <div class="sidebar-menu">
        <div class="menu-section">TỔNG QUAN</div>
        <RouterLink to="/admin/dashboard" class="menu-item" active-class="active" @click="onNavClick">
          <span class="icon">📊</span><span>Tổng Quan</span>
        </RouterLink>

        <div class="menu-section">QUẢN LÝ</div>
        <RouterLink to="/admin/movies" class="menu-item" active-class="active" @click="onNavClick">
          <span class="icon">🎬</span><span>Quản Lý Phim</span>
        </RouterLink>
        <RouterLink to="/admin/schedule" class="menu-item" active-class="active" @click="onNavClick">
          <span class="icon">📅</span><span>Lịch Chiếu</span>
        </RouterLink>
        <RouterLink to="/admin/tickets" class="menu-item" active-class="active" @click="onNavClick">
          <span class="icon">🎟️</span><span>Đặt Vé</span>
          <span class="menu-badge" v-if="pendingTickets > 0">{{ pendingTickets }}</span>
        </RouterLink>
        <RouterLink to="/admin/customers" class="menu-item" active-class="active" @click="onNavClick">
          <span class="icon">👥</span><span>Khách Hàng</span>
        </RouterLink>
        <RouterLink to="/admin/cinemas" class="menu-item" active-class="active" @click="onNavClick">
          <span class="icon">🏢</span><span>Rạp Chiếu</span>
        </RouterLink>

        <div class="menu-section">HỆ THỐNG</div>
        <RouterLink to="/admin/promo" class="menu-item" active-class="active" @click="onNavClick">
          <span class="icon">🎁</span><span>Khuyến Mãi</span>
        </RouterLink>
        <RouterLink to="/admin/reports" class="menu-item" active-class="active" @click="onNavClick">
          <span class="icon">📈</span><span>Báo Cáo</span>
        </RouterLink>
        <RouterLink to="/admin/settings" class="menu-item" active-class="active" @click="onNavClick">
          <span class="icon">⚙️</span><span>Cài Đặt</span>
        </RouterLink>
      </div>

      <div class="sidebar-footer">
        <div class="admin-info">
          <div class="admin-avatar">AD</div>
          <div>
            <div class="admin-name">Admin PolyCinema</div>
            <div class="admin-role">Quản Trị Viên</div>
          </div>
        </div>
        <button type="button" class="home-btn" @click="goHome">🏠 Về trang chủ</button>
        <a href="#" class="logout-btn" @click.prevent="logout">🔓 Đăng Xuất</a>
      </div>
    </div>

    <!-- MAIN CONTENT -->
    <div class="main">
      <div
        v-if="shell.showDatePanel || shell.showNotifPanel"
        class="admin-panel-backdrop"
        aria-hidden="true"
        @click="shell.closePanels()"
      />

      <div class="topbar">
        <div class="topbar-head">
          <p class="eyebrow">Control room</p>
          <h1>{{ pageTitles[currentPage] }}</h1>
        </div>

        <div class="topbar-toolbar" ref="topbarToolbarRef">
          <div class="topbar-toolbar-start">
            <button type="button" class="home-btn home-btn--topbar" title="Về trang chủ" @click="goHome">
              🏠 <span class="home-btn__label">Trang chủ</span>
            </button>
            <ThemeToggle />
            <div class="topbar-search-wrap">
              <input
                v-model="shell.searchQuery"
                type="search"
                class="search-input"
                placeholder="Tìm phim, vé, khách..."
                aria-label="Tìm kiếm"
                @keydown.enter.prevent="runSearch"
              />
              <button type="button" class="search-go" title="Tìm" @click="runSearch">⌕</button>
            </div>
          </div>

          <div class="topbar-toolbar-end">
            <div class="topbar-popover-wrap">
              <button type="button" class="date-tag" @click.stop="shell.toggleDatePanel()">
                📅 <span class="date-tag__text">{{ shell.formattedFilterDate }}</span>
              </button>
              <div v-if="shell.showDatePanel" class="topbar-popover date-popover" @click.stop>
                <p class="popover-title">Chọn ngày lọc lịch chiếu</p>
                <input v-model="shell.filterDate" type="date" class="popover-date-input" />
                <div class="popover-actions">
                  <button type="button" class="popover-btn" @click.stop="shell.showDatePanel = false">Đóng</button>
                  <button type="button" class="popover-btn popover-btn--primary" @click.stop="applyDateFilter">Áp dụng</button>
                  <button type="button" class="popover-btn" @click.stop="setTodayAndApply">Hôm nay</button>
                </div>
              </div>
            </div>

            <div class="topbar-popover-wrap">
              <button
                type="button"
                class="notif"
                aria-label="Thông báo"
                @click.stop="shell.toggleNotifPanel()"
              >
                🔔
                <span v-if="shell.pendingCount > 0" class="notif-dot">{{ shell.pendingCount > 9 ? '9+' : shell.pendingCount }}</span>
              </button>
              <div v-if="shell.showNotifPanel" class="topbar-popover notif-popover" @click.stop>
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
      </div>

      <div class="content">
        <!-- Dashboard -->
        <DashboardPage v-if="currentPage === 'dashboard'" />
        <!-- Movies -->
        <MoviesPage v-if="currentPage === 'movies'" />
        <!-- Schedule -->
        <SchedulePage v-if="currentPage === 'schedule'" />
        <!-- Tickets -->
        <TicketsPage v-if="currentPage === 'tickets'" />
        <!-- Customers -->
        <CustomersPage v-if="currentPage === 'customers'" />
        <!-- Cinemas -->
        <CinemasPage v-if="currentPage === 'cinemas'" />
        <!-- Promo -->
        <PromoPage v-if="currentPage === 'promo'" />
        <!-- Reports -->
        <ReportsPage v-if="currentPage === 'reports'" />
        <!-- Settings -->
        <SettingsPage v-if="currentPage === 'settings'" />
      </div>
    </div>

    <!-- Modals sẽ được quản lý qua composable hoặc event bus sau này -->
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
@import '@/assets/admin-gold.css';

:root {
  --admin-accent: #ffd700;
  --admin-ink: #e5e5e5;
  --admin-muted: #9ca3af;
  --admin-line: #374151;
  --admin-card: #111827;
  --admin-sidebar: 280px;
}

.admin-container,
.admin-container * {
  box-sizing: border-box;
}

.admin-container {
  width: 100%;
  min-height: 100vh;
  display: grid;
  grid-template-columns: var(--admin-sidebar) minmax(0, 1fr);
  column-gap: 18px;
  align-items: start;
  padding: 18px;
  color: var(--admin-ink);
  font-family: 'Raleway', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: #0d0d0d;
  transition: background 0.25s ease, color 0.25s ease;
}

.sidebar {
  grid-column: 1;
  grid-row: 1;
  position: sticky;
  top: 18px;
  z-index: 500;
  width: 100%;
  min-width: 0;
  height: calc(100vh - 36px);
  max-height: calc(100vh - 36px);
  display: flex;
  flex-direction: column;
  border: 1px solid var(--admin-line);
  border-radius: 0;
  background: var(--admin-card);
  box-shadow: none;
  overflow: hidden;
  isolation: isolate;
  pointer-events: auto;
}

.sidebar-logo,
.sidebar-footer {
  position: relative;
  z-index: 1;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 22px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 4px;
  background: var(--admin-accent);
  box-shadow: none;
}

.logo-icon svg {
  width: 24px;
  height: 24px;
  fill: #0d0d0d;
}

.logo-text {
  color: var(--admin-accent);
  font-size: 20px;
  font-weight: 900;
  letter-spacing: -0.6px;
}

.logo-text span {
  color: var(--admin-ink);
}

.logo-admin {
  margin-left: auto;
  padding: 5px 8px;
  border-radius: 2px;
  background: #1f2937;
  color: var(--admin-accent);
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 1px;
}

.sidebar-menu {
  position: relative;
  z-index: 2;
  flex: 1;
  padding: 18px 14px;
  overflow-y: auto;
  pointer-events: auto;
}

.menu-section {
  padding: 18px 14px 8px;
  color: #9ca3af;
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 1.4px;
}

a.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 46px;
  padding: 12px 14px;
  border-radius: 4px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--admin-muted);
  font-size: 14px;
  font-weight: 800;
  font-family: inherit;
  text-align: left;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.22s ease, background 0.22s ease, color 0.22s ease;
  -webkit-tap-highlight-color: transparent;
}

.menu-item:hover {
  color: #e5e5e5;
  background: #1f2937;
  border-color: transparent;
  transform: none;
}

.menu-item.active {
  color: #ffd700;
  background: rgba(255, 215, 0, 0.1);
  border-color: transparent;
  border-left: 2px solid #ffd700;
  box-shadow: none;
}

.menu-item .icon {
  width: 24px;
  text-align: center;
  font-size: 18px;
}

.menu-badge {
  margin-left: auto;
  min-width: 22px;
  padding: 2px 7px;
  border-radius: 999px;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 900;
  text-align: center;
}

.sidebar-footer {
  padding: 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 12px;
  border-radius: 8px;
  background: #1f2937;
  border: 1px solid #374151;
}

.admin-avatar {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: rgba(255, 215, 0, 0.15);
  color: #ffd700;
  font-size: 14px;
  font-weight: 900;
}

.admin-name {
  color: var(--admin-ink);
  font-size: 13px;
  font-weight: 900;
}

.admin-role {
  color: var(--text-tertiary);
  font-size: 11px;
  font-weight: 700;
}

.home-btn {
  display: block;
  width: 100%;
  margin-bottom: 10px;
  padding: 11px 12px;
  border: 1px solid var(--admin-line);
  border-radius: 4px;
  background: var(--surface);
  color: var(--admin-ink);
  font-size: 13px;
  font-weight: 800;
  font-family: inherit;
  text-align: center;
  cursor: pointer;
  transition: 0.22s ease;
}

.home-btn:hover {
  border-color: var(--admin-accent);
  color: var(--admin-accent);
  background: var(--accent-soft);
}

.home-btn--topbar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: auto;
  margin-bottom: 0;
  min-height: 44px;
  padding: 10px 14px;
  border-radius: 999px;
  background: var(--surface-plain);
  white-space: nowrap;
}

.home-btn__label {
  font-size: 12px;
  font-weight: 700;
}

.logout-btn {
  display: block;
  padding: 11px 12px;
  border: 1px solid #374151;
  border-radius: 8px;
  color: #e5e5e5;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
  text-decoration: none;
  transition: 0.22s ease;
}

.logout-btn:hover {
  color: #ffd700;
  border-color: #ffd700;
  background: rgba(255, 215, 0, 0.08);
}

.main {
  grid-column: 2;
  grid-row: 1;
  min-width: 0;
  min-height: calc(100vh - 36px);
  position: relative;
  z-index: 1;
  isolation: isolate;
}

.admin-panel-backdrop {
  position: absolute;
  inset: 0;
  z-index: 40;
  background: transparent;
  cursor: default;
}

.topbar {
  position: sticky;
  top: 18px;
  z-index: 45;
  width: 100%;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 22px;
  border: 1px solid var(--admin-line);
  border-radius: 12px;
  background: #111827;
  box-shadow: none;
  overflow: visible;
}

.topbar-head {
  min-width: 0;
}

.eyebrow {
  margin: 0 0 3px;
  color: var(--admin-accent);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 1.8px;
  text-transform: uppercase;
}

.topbar h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #e5e5e5;
  letter-spacing: -0.02em;
}

.topbar-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  min-width: 0;
}

.topbar-toolbar-start {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  flex: 1 1 240px;
  min-width: 0;
}

.topbar-toolbar-end {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.topbar-popover-wrap {
  position: relative;
  flex-shrink: 0;
}

.topbar-search-wrap {
  display: flex;
  align-items: center;
  min-width: 0;
  width: min(320px, 100%);
  max-width: 100%;
  flex: 1 1 180px;
  border: 1px solid var(--admin-line);
  border-radius: 999px;
  background: #111827;
  overflow: hidden;
}

.search-input {
  flex: 1;
  min-width: 0;
  min-height: 44px;
  padding: 10px 12px 10px 16px;
  border: none;
  background: transparent;
  color: var(--admin-ink);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  outline: none;
}

.search-input::placeholder {
  color: var(--admin-muted);
}

.search-go {
  min-width: 44px;
  min-height: 44px;
  border: none;
  border-left: 1px solid var(--admin-line);
  background: var(--surface);
  color: var(--admin-accent);
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.search-go:hover {
  background: rgba(255, 215, 0, 0.1);
}

.date-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 44px;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid var(--admin-line);
  background: #111827;
  color: var(--admin-muted);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  max-width: min(240px, 42vw);
}

.date-tag__text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date-tag:hover {
  border-color: var(--admin-accent);
  color: var(--admin-accent);
}

.notif {
  position: relative;
  min-width: 44px;
  min-height: 44px;
  padding: 0 10px;
  display: grid;
  place-items: center;
  border-radius: 4px;
  border: 1px solid var(--admin-line);
  background: #111827;
  cursor: pointer;
  font-size: 18px;
  font-family: inherit;
}

.notif:hover {
  border-color: var(--admin-accent);
}

.notif-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border: 2px solid var(--surface-plain);
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 800;
  line-height: 14px;
  text-align: center;
}

.topbar-popover {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 60;
  min-width: 280px;
  max-width: min(360px, calc(100vw - 40px));
  padding: 14px;
  border: 1px solid var(--admin-line);
  background: #111827;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.45);
  pointer-events: auto;
}

.date-popover {
  right: 0;
}

.notif-popover {
  right: 0;
  max-height: 400px;
  overflow-y: auto;
}

.popover-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.popover-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--admin-ink);
}

.popover-link {
  border: none;
  background: none;
  color: var(--admin-accent);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
}

.popover-date-input {
  width: 100%;
  min-height: 44px;
  padding: 10px 12px;
  border: 1px solid var(--admin-line);
  border-radius: 4px;
  background: var(--surface);
  color: var(--admin-ink);
  font-family: inherit;
  font-size: 14px;
}

.popover-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.popover-btn {
  min-height: 40px;
  padding: 8px 14px;
  border: 1px solid var(--admin-line);
  border-radius: 4px;
  background: transparent;
  color: var(--admin-muted);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
}

.popover-btn--primary {
  background: var(--admin-accent);
  border-color: var(--admin-accent);
  color: #0d0d0d;
}

.popover-btn--primary:hover {
  filter: brightness(1.1);
}

.popover-empty {
  padding: 16px 8px;
  text-align: center;
  color: var(--admin-muted);
  font-size: 13px;
}

.notif-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notif-item {
  display: flex;
  width: 100%;
  gap: 10px;
  padding: 10px 8px;
  border: none;
  border-bottom: 1px solid var(--admin-line);
  background: transparent;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
}

.notif-item:last-child {
  border-bottom: none;
}

.notif-item:hover {
  background: var(--hover-row);
}

.notif-item__badge {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 4px;
  background: var(--surface);
  color: var(--admin-muted);
  font-size: 12px;
  font-weight: 800;
}

.notif-item__badge--pending {
  background: #fef9e8;
  color: #854d0e;
}

.notif-item__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.notif-item__title {
  font-size: 13px;
  font-weight: 700;
  color: var(--admin-ink);
}

.notif-item__sub {
  font-size: 11px;
  color: var(--admin-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.35); opacity: 0.64; }
}

.content {
  padding: 24px 0 0;
}

.stat-card,
.card {
  border: 1px solid var(--admin-line);
  border-radius: 0;
  background: var(--admin-card);
  box-shadow: none;
  transition: border-color 0.22s ease;
}

.stat-card:hover,
.card:hover {
  transform: none;
  border-color: var(--admin-accent);
  box-shadow: none;
}

table {
  width: 100%;
  border-collapse: collapse;
}

tr {
  transition: background-color 0.15s ease;
}

tr:hover td {
  background: var(--hover-row);
}

@media (max-width: 1180px) {
  :root { --admin-sidebar: 242px; }
  .topbar-toolbar-start {
    flex: 1 1 100%;
  }
  .topbar-search-wrap {
    flex: 1 1 100%;
    width: 100%;
    max-width: 100%;
  }
  .topbar-toolbar-end {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 900px) {
  :root { --admin-sidebar: 76px; }
  .admin-container {
    grid-template-columns: 76px minmax(0, 1fr);
    column-gap: 12px;
    padding: 12px;
  }
  .sidebar {
    top: 12px;
    height: calc(100vh - 24px);
    max-height: calc(100vh - 24px);
    border-radius: 24px;
  }
  .sidebar-logo { justify-content: center; padding: 18px 10px; }
  .logo-text,
  .logo-admin,
  .menu-section,
  .menu-item span:not(.icon),
  .admin-info > div,
  .home-btn:not(.home-btn--topbar),
  .logout-btn { display: none; }
  .menu-item { justify-content: center; padding: 14px 10px; }
  .menu-item:hover { transform: translateX(0) scale(1.04); }
  .menu-badge { position: absolute; top: 7px; right: 8px; }
  .sidebar-footer { padding: 12px; }
  .admin-info { justify-content: center; padding: 8px; }
  .topbar { top: 12px; border-radius: 22px; }
}

@media (max-width: 640px) {
  .admin-container {
    display: block;
    padding: 10px;
    padding-bottom: 88px;
  }
  .sidebar,
  .main {
    grid-column: auto;
    grid-row: auto;
  }
  .sidebar {
    position: fixed;
    top: auto;
    right: 10px;
    bottom: 10px;
    left: 10px;
    z-index: 120;
    width: auto;
    min-height: 0;
    height: 66px;
    max-height: none;
    border-radius: 22px;
  }
  .sidebar-logo,
  .sidebar-footer,
  .menu-section { display: none; }
  .sidebar-menu {
    display: flex;
    gap: 6px;
    padding: 8px;
    overflow-x: auto;
  }
  .menu-item {
    min-width: 48px;
    min-height: 48px;
    border-radius: 16px;
    flex: 0 0 auto;
  }
  .main {
    min-height: 0;
  }
  .topbar {
    position: relative;
    top: 0;
    z-index: 20;
    min-height: auto;
    padding: 16px;
  }
  .topbar-toolbar { gap: 8px; }
  .topbar-search-wrap { width: 100%; min-width: 0; max-width: none; }
  .home-btn--topbar .home-btn__label { display: none; }
  .date-tag { max-width: min(200px, 50vw); }
  .content { padding-top: 14px; }
}

.topbar .search-input {
  border: none !important;
  border-radius: 0 !important;
  background: transparent !important;
  color: #e5e5e5 !important;
  box-shadow: none !important;
}

.topbar-search-wrap {
  background: #111827;
  border-color: #374151;
}

.search-go {
  color: #ffd700;
  background: #1f2937;
  border-left-color: #374151;
}

.search-go:hover {
  background: rgba(255, 215, 0, 0.1);
}

.date-tag,
.notif {
  background: #111827;
  border-color: #374151;
  color: #9ca3af;
}

.date-tag:hover,
.notif:hover {
  border-color: #ffd700;
  color: #ffd700;
}

.topbar-popover {
  background: #111827;
  border-color: #374151;
}

.popover-title,
.notif-item__title {
  color: #e5e5e5;
}

.popover-link {
  color: #ffd700;
}

.popover-empty,
.notif-item__sub {
  color: #9ca3af;
}

.home-btn--topbar {
  background: #111827;
  border-color: #374151;
  color: #e5e5e5;
}

.home-btn--topbar:hover {
  border-color: #ffd700;
  color: #ffd700;
  background: rgba(255, 215, 0, 0.08);
}

.logo-admin {
  background: #1f2937;
  color: #ffd700;
}

.logo-text span {
  color: #e5e5e5;
}

.sidebar-logo {
  border-bottom-color: #374151;
}

.sidebar-footer {
  border-top-color: #374151;
}
</style>
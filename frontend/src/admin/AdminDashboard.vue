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
        <span class="logo-badge admin-role-badge">ADMIN</span>
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
            <div class="topbar-search-wrap" @click="openPalette">
              <svg class="admin-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15" aria-hidden="true"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              <input
                ref="paletteInputRef"
                v-model="shell.searchQuery"
                type="search"
                class="admin-search-input"
                placeholder="Tìm phim, vé, khách... (Enter)"
                aria-label="Tìm kiếm toàn cục"
                autocomplete="off"
                @input="debouncedPalette"
                @keydown.enter.prevent="runSearch"
                @keydown.escape="closePalette"
                @keydown.down.prevent="paletteMoveDown"
                @keydown.up.prevent="paletteMoveUp"
                @focus="onSearchFocus"
              />
              <button v-if="shell.searchQuery" class="admin-search-clear" @click.stop="clearSearch" aria-label="Xóa tìm kiếm">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" width="12" height="12"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>

            <!-- Command Palette dropdown -->
            <transition name="palette-drop">
              <div v-if="paletteOpen && shell.searchQuery.trim()" class="admin-palette" role="listbox" @mousedown.prevent>
                <div v-if="paletteLoading" class="palette-state">
                  <div class="palette-spinner"></div><span>Đang tìm...</span>
                </div>
                <template v-else-if="paletteResults.length > 0">
                  <div
                    v-for="(item, idx) in paletteResults"
                    :key="item._key"
                    :class="['palette-item', { 'palette-item--active': idx === paletteActiveIdx }]"
                    role="option"
                    :aria-selected="idx === paletteActiveIdx"
                    @click="paletteSelect(item)"
                    @mouseenter="paletteActiveIdx = idx"
                  >
                    <span :class="['palette-badge', `palette-badge--${item._section}`]">{{ item._label }}</span>
                    <span class="palette-title">{{ item._title }}</span>
                    <span v-if="item._sub" class="palette-sub">{{ item._sub }}</span>
                  </div>
                </template>
                <div v-else class="palette-state palette-empty">
                  Không tìm thấy kết quả cho "<strong>{{ shell.searchQuery }}</strong>"
                </div>
              </div>
            </transition>
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
import api from '@/services/api'
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

// ── Admin Command Palette ─────────────────────────────────────
const paletteInputRef = ref(null)
const paletteOpen     = ref(false)
const paletteLoading  = ref(false)
const paletteResults  = ref([])
const paletteActiveIdx = ref(0)
let _paletteTimer = null

// Static settings items for client-side matching
// subTab maps to SettingsPage's internal tabs: banners | products | gioi_thieu | null
const SETTINGS_ITEMS = [
  { key: 'settings-info',    label: 'Cài Đặt', title: 'Thông tin rạp',       sub: 'Settings → Thông tin',    tab: 'settings', subTab: null },
  { key: 'settings-about',   label: 'Cài Đặt', title: 'Giới thiệu',          sub: 'Settings → Giới thiệu',  tab: 'settings', subTab: 'gioi_thieu' },
  { key: 'settings-banner',  label: 'Cài Đặt', title: 'Banner quảng cáo',    sub: 'Settings → Banner',      tab: 'settings', subTab: 'banners' },
  { key: 'settings-product', label: 'Cài Đặt', title: 'Sản phẩm combo',      sub: 'Settings → Sản phẩm',    tab: 'settings', subTab: 'products' },
  { key: 'settings-format',  label: 'Cài Đặt', title: 'Định dạng chiếu phim',sub: 'Settings → Định dạng',   tab: 'settings', subTab: null },
  { key: 'settings-config',  label: 'Cài Đặt', title: 'Cấu hình hệ thống',  sub: 'Settings → Cấu hình',    tab: 'settings', subTab: null },
]

function openPalette() { paletteOpen.value = true }
function onSearchFocus() { if (shell.searchQuery.trim()) debouncedPalette() }
function closePalette() { paletteOpen.value = false; paletteActiveIdx.value = 0 }

function clearSearch() {
  shell.searchQuery = ''
  paletteResults.value = []
  closePalette()
  paletteInputRef.value?.focus()
}

async function runPalette() {
  const q = shell.searchQuery.trim()
  if (!q) { paletteResults.value = []; closePalette(); return }
  paletteOpen.value = true
  paletteLoading.value = true
  paletteActiveIdx.value = 0
  const ql = q.toLowerCase()

  try {
    // All 6 sections in parallel — server-side for users/tickets, client-side for the rest
    const [moviesRes, usersRes, ticketsRes] = await Promise.allSettled([
      api.get('/admin/phim'),
      api.get('/admin/users', { params: { page: 0, size: 8, search: q } }),
      api.get('/admin/dat-ve', { params: { page: 0, size: 8, q } }),
    ])

    const results = []

    // ── Movies (client-side filter of full list) ──────────────
    const allMovies = moviesRes.status === 'fulfilled' ? (moviesRes.value.data || []) : []
    allMovies
      .filter(m => !m.isDeleted && (m.tenPhim || '').toLowerCase().includes(ql))
      .slice(0, 5)
      .forEach(m => results.push({
        _key: 'movie-' + m.id, _section: 'movies', _label: '🎬 Phim',
        _title: m.tenPhim, _sub: m.trangThai === 'dang_chieu' ? 'Đang chiếu' : m.trangThai === 'sap_chieu' ? 'Sắp chiếu' : m.trangThai === 'da_ket_thuc' ? 'Đã kết thúc' : 'Chưa chiếu',
        _action: () => { switchPage('movies'); shell.searchQuery = m.tenPhim; shell.searchTargetPage = 'movies'; shell.searchTick++ },
      }))

    // ── Customers ─────────────────────────────────────────────
    const usersData = usersRes.status === 'fulfilled' ? (usersRes.value.data?.content || usersRes.value.data || []) : []
    ;(Array.isArray(usersData) ? usersData : []).slice(0, 5).forEach(u => results.push({
      _key: 'user-' + u.id, _section: 'customers', _label: '👤 Khách hàng',
      _title: u.hoTen || u.email, _sub: u.email,
      _action: () => { switchPage('customers'); shell.searchQuery = u.hoTen || u.email; shell.searchTargetPage = 'customers'; shell.searchTick++ },
    }))

    // ── Tickets ────────────────────────────────────────────────
    const ticketRows = ticketsRes.status === 'fulfilled' ? (ticketsRes.value.data?.content || ticketsRes.value.data || []) : []
    ;(Array.isArray(ticketRows) ? ticketRows : []).slice(0, 5).forEach(t => results.push({
      _key: 'ticket-' + t.id, _section: 'tickets', _label: '🎟️ Vé',
      _title: t.maDatVe || t.maDatVe, _sub: t.nguoiDung?.email || t.email || '',
      _action: () => { switchPage('tickets'); shell.searchQuery = t.maDatVe; shell.searchTargetPage = 'tickets'; shell.ticketStatusFilter = ''; shell.searchTick++ },
    }))

    // ── Cinemas (client-side via /api/rap-chieu — small dataset) ─
    try {
      const cinemasRes = await api.get('/rap-chieu')
      ;(cinemasRes.data || [])
        .filter(c => (c.tenRap || '').toLowerCase().includes(ql) || (c.diaChi || '').toLowerCase().includes(ql))
        .slice(0, 4)
        .forEach(c => results.push({
          _key: 'cinema-' + c.id, _section: 'cinemas', _label: '🏠 Rạp chiếu',
          _title: c.tenRap, _sub: c.diaChi || '',
          _action: () => { switchPage('cinemas'); shell.searchQuery = c.tenRap; shell.searchTargetPage = 'cinemas'; shell.searchTick++ },
        }))
    } catch {}

    // ── Promos (client-side via /api/khuyen-mai/all — admin endpoint) ─
    try {
      const promosRes = await api.get('/khuyen-mai/all')
      ;(promosRes.data || [])
        .filter(p => (p.tenKhuyenMai || '').toLowerCase().includes(ql) || (p.maKhuyenMai || '').toLowerCase().includes(ql))
        .slice(0, 4)
        .forEach(p => results.push({
          _key: 'promo-' + p.id, _section: 'promo', _label: '🎁 Khuyến mãi',
          _title: p.tenKhuyenMai, _sub: p.maKhuyenMai ? `Mã: ${p.maKhuyenMai}` : '',
          _action: () => { switchPage('promo'); shell.searchQuery = p.tenKhuyenMai; shell.searchTargetPage = 'promo'; shell.searchTick++ },
        }))
    } catch {}

    // ── Settings (static keyword match + live banner/product data) ─────
    // 1. Static tab-label matches (kept as fallback)
    SETTINGS_ITEMS
      .filter(s => s.title.toLowerCase().includes(ql) || s.sub.toLowerCase().includes(ql))
      .forEach(s => results.push({
        _key: s.key, _section: 'settings', _label: '⚙️ Cài đặt',
        _title: s.title, _sub: s.sub,
        _action: () => {
          switchPage('settings')
          shell.searchTargetPage = 'settings'
          shell.settingsSubTab = s.subTab || null
          shell.searchQuery = (s.subTab === 'banners' || s.subTab === 'products') ? q : ''
          shell.searchTick++
        },
      }))

    // 2. Live banner content
    try {
      const bannersRes = await api.get('/admin/banner')
      ;(bannersRes.data || [])
        .filter(b => (b.tieuDe || '').toLowerCase().includes(ql) || (b.moTa || '').toLowerCase().includes(ql))
        .slice(0, 4)
        .forEach(b => results.push({
          _key: 'banner-' + b.id, _section: 'banner', _label: '🖼️ Banner',
          _title: b.tieuDe || '(Banner)',
          _sub: b.dangHoatDong ? 'Đang hiển thị' : 'Đã ẩn',
          _action: () => { switchPage('settings'); shell.searchQuery = b.tieuDe || q; shell.searchTargetPage = 'settings'; shell.settingsSubTab = 'banners'; shell.searchTick++ },
        }))
    } catch {}

    // 3. Live combo/product content
    try {
      const prodsRes = await api.get('/admin/san-pham')
      ;(prodsRes.data || [])
        .filter(p => (p.tenSanPham || '').toLowerCase().includes(ql) || (p.moTa || '').toLowerCase().includes(ql))
        .slice(0, 4)
        .forEach(p => results.push({
          _key: 'product-' + p.id, _section: 'product', _label: '🍿 Combo',
          _title: p.tenSanPham || '(Sản phẩm)',
          _sub: p.gia ? `${Number(p.gia).toLocaleString('vi-VN')} ₫` : '',
          _action: () => { switchPage('settings'); shell.searchQuery = p.tenSanPham || q; shell.searchTargetPage = 'settings'; shell.settingsSubTab = 'products'; shell.searchTick++ },
        }))
    } catch {}

    paletteResults.value = results
  } catch {}
  paletteLoading.value = false
}

function debouncedPalette() {
  clearTimeout(_paletteTimer)
  _paletteTimer = setTimeout(() => runPalette(), 320)
}

// Keep legacy runSearch for Enter key — just runs palette immediately
function runSearch() {
  clearTimeout(_paletteTimer)
  runPalette()
}

// Keep old debouncedSearch alias so any leftover references don't break
const debouncedSearch = debouncedPalette

function paletteMoveDown() {
  if (!paletteResults.value.length) return
  paletteActiveIdx.value = (paletteActiveIdx.value + 1) % paletteResults.value.length
}
function paletteMoveUp() {
  if (!paletteResults.value.length) return
  paletteActiveIdx.value = (paletteActiveIdx.value - 1 + paletteResults.value.length) % paletteResults.value.length
}
function paletteSelect(item) {
  closePalette()
  item._action?.()
}

// Close palette when clicking outside
function onDocClick(e) {
  if (!e.target.closest('.topbar-search-wrap') && !e.target.closest('.admin-palette')) {
    closePalette()
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
  document.addEventListener('click', onDocClick)
  await shell.refreshPendingCount()
  pendingTickets.value = shell.pendingCount
})

onUnmounted(() => {
  document.removeEventListener('keydown', onEscapeKey)
  document.removeEventListener('click', onDocClick)
})
</script>

<style>
@import '../assets/tokens.css';
@import '../assets/cinema.css';
@import '../assets/admin-theme.css';

/* ── Admin role badge (sidebar logo) ── */
.admin-role-badge {
  margin-left: auto; padding: 2px 8px; border-radius: 9999px;
  background: var(--admin-accent-muted); color: var(--admin-accent);
  font-family: var(--font-ui); font-size: 10px; font-weight: 600;
  letter-spacing: 0.12em; text-transform: uppercase;
}

/* ── Shell layout variables ── */
:root { --admin-sidebar: 240px; }

/* ── Grid layout ── */
.admin-container {
  width: 100%;
  min-height: 100vh;
  display: grid;
  grid-template-columns: var(--admin-sidebar) minmax(0, 1fr);
  align-items: start;
  background: var(--admin-bg);
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
  background: var(--admin-bg);
  border-right: 1px solid var(--admin-border);
  overflow: hidden;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 20px;
  border-bottom: 1px solid var(--admin-border);
  flex-shrink: 0;
}
.logo-icon-svg { color: var(--admin-accent); flex-shrink: 0; }
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
.menu-item.active { background: var(--admin-accent-muted); color: var(--admin-accent); font-weight: 600; }
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
  border-top: 1px solid var(--admin-border);
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
  background: var(--admin-accent);
  color: var(--admin-bg);
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
  position: sticky; top: 0; z-index: 45;
  display: flex; flex-direction: column; gap: 12px; padding: 16px 24px;
  background: color-mix(in srgb, var(--admin-bg) 85%, transparent);
  backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--admin-border);
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
.home-btn--topbar {
  white-space: nowrap;
  border: 1px solid var(--admin-border); border-radius: 8px; padding: 6px 12px;
  color: var(--admin-text); background: transparent; font-size: 12px;
  transition: border-color 150ms, color 150ms;
}
.home-btn--topbar:hover { border-color: var(--admin-accent); color: var(--admin-accent); }
.home-btn__label { font-size: 12px; }

/* Search bar */
.topbar-search-wrap {
  display: flex; align-items: center; flex: 1; min-width: 0;
  background: var(--admin-surface);
  border: 1px solid var(--admin-border); border-radius: 10px; overflow: visible;
  transition: border-color 200ms var(--ease-out), box-shadow 200ms var(--ease-out);
  position: relative;   /* palette anchors to this */
}
.topbar-search-wrap:focus-within {
  border-color: var(--admin-accent);
  box-shadow: 0 0 0 2px var(--admin-accent-border);
}
.admin-search-icon {
  /* Icon sits inside the rounded bar — transparent bg, vertically centred by flex parent */
  margin-left: 14px; flex-shrink: 0; display: block;
  color: var(--admin-text-muted);
  background: transparent; border: none; padding: 0; min-height: unset;
}
.admin-search-input {
  flex: 1 !important; min-width: 0;
  min-height: 42px !important; padding: 10px 14px !important;
  border: none !important; background: transparent !important;
  box-shadow: none !important; border-radius: 0 !important;
  color: var(--admin-text) !important; font-family: var(--font-ui) !important;
  font-size: 13px !important; outline: none !important;
}
.admin-search-input::placeholder { color: var(--admin-text-muted) !important; }

/* Clear button inside search bar */
.admin-search-clear {
  display: flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; margin-right: 8px; flex-shrink: 0;
  background: transparent; border: none; cursor: pointer;
  color: var(--admin-text-muted); border-radius: 4px;
  transition: color 150ms, background 150ms;
}
.admin-search-clear:hover { color: var(--admin-text); background: var(--admin-accent-muted); }

/* ── Admin Command Palette Dropdown ── */
.palette-drop-enter-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.palette-drop-leave-active { transition: opacity 0.1s ease, transform 0.1s ease; }
.palette-drop-enter-from, .palette-drop-leave-to { opacity: 0; transform: translateY(-6px); }

.admin-palette {
  position: absolute;
  top: calc(100% + 6px);
  left: 0; right: 0;
  z-index: 6000;
  background: var(--admin-surface);
  border: 1px solid var(--admin-border);
  border-radius: 10px;
  box-shadow: 0 12px 32px rgba(0,0,0,0.45);
  overflow: hidden;
  max-height: 400px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--admin-border) transparent;
}

.palette-state {
  display: flex; align-items: center; gap: 10px;
  padding: 20px 16px;
  color: var(--admin-text-muted);
  font-size: 13px;
  font-family: var(--font-ui);
}
.palette-empty { font-size: 13px; }
.palette-empty strong { color: var(--admin-accent); font-weight: 600; }

.palette-spinner {
  width: 18px; height: 18px; flex-shrink: 0;
  border: 2px solid var(--admin-border);
  border-top-color: var(--admin-accent);
  border-radius: 50%;
  animation: pal-spin 0.65s linear infinite;
}
@keyframes pal-spin { to { transform: rotate(360deg); } }

.palette-item {
  display: flex; align-items: center; gap: 9px;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--admin-divider);
  transition: background 100ms;
  font-family: var(--font-ui);
}
.palette-item:last-child { border-bottom: none; }
.palette-item--active,
.palette-item:hover { background: var(--admin-surface-hover); }

.palette-badge {
  flex-shrink: 0; font-size: 11px; font-weight: 600;
  padding: 2px 7px; border-radius: 9999px; white-space: nowrap;
}
.palette-badge--movies   { background: rgba(41,188,234,0.12); color: #29bcea; }
.palette-badge--customers { background: rgba(16,185,129,0.12); color: #10B981; }
.palette-badge--tickets  { background: rgba(245,158,11,0.12);  color: #F59E0B; }
.palette-badge--cinemas  { background: rgba(201,168,76,0.12);  color: #C9A84C; }
.palette-badge--promo    { background: rgba(236,72,153,0.12);  color: #EC4899; }
.palette-badge--settings { background: rgba(156,163,175,0.12); color: #9CA3AF; }
.palette-badge--banner   { background: rgba(139,92,246,0.12);  color: #a78bfa; }
.palette-badge--product  { background: rgba(249,115,22,0.12);  color: #fb923c; }

.palette-title {
  flex: 1; font-size: 13px; font-weight: 500; color: var(--admin-text);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.palette-sub {
  font-size: 12px; color: var(--admin-text-muted);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  max-width: 180px;
}

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
.notif:hover { border-color: var(--admin-accent); color: var(--admin-accent); }
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
  border: none; background: none; color: var(--admin-accent);
  font-family: var(--font-ui); font-size: 12px; font-weight: 600; cursor: pointer;
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

/* Spacing: toolbar row → content below */
.topbar-toolbar { margin-bottom: 4px; }

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
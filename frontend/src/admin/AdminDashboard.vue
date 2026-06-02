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
        <a class="menu-item" :class="{ active: currentPage === 'dashboard' }" @click="switchPage('dashboard')">
          <span class="icon">📊</span><span>Tổng Quan</span>
        </a>

        <div class="menu-section">QUẢN LÝ</div>
        <a class="menu-item" :class="{ active: currentPage === 'movies' }" @click="switchPage('movies')">
          <span class="icon">🎬</span><span>Quản Lý Phim</span>
        </a>
        <a class="menu-item" :class="{ active: currentPage === 'schedule' }" @click="switchPage('schedule')">
          <span class="icon">📅</span><span>Lịch Chiếu</span>
        </a>
        <a class="menu-item" :class="{ active: currentPage === 'tickets' }" @click="switchPage('tickets')">
          <span class="icon">🎟️</span><span>Đặt Vé</span>
          <span class="menu-badge" v-if="pendingTickets > 0">{{ pendingTickets }}</span>

        </a>
        <a class="menu-item" :class="{ active: currentPage === 'customers' }" @click="switchPage('customers')">
          <span class="icon">👥</span><span>Khách Hàng</span>
        </a>
        <a class="menu-item" :class="{ active: currentPage === 'cinemas' }" @click="switchPage('cinemas')">
          <span class="icon">🏢</span><span>Rạp Chiếu</span>
        </a>

        <div class="menu-section">HỆ THỐNG</div>
        <a class="menu-item" :class="{ active: currentPage === 'promo' }" @click="switchPage('promo')">
          <span class="icon">🎁</span><span>Khuyến Mãi</span>
        </a>
        <a class="menu-item" :class="{ active: currentPage === 'reports' }" @click="switchPage('reports')">
          <span class="icon">📈</span><span>Báo Cáo</span>
        </a>
        <a class="menu-item" :class="{ active: currentPage === 'settings' }" @click="switchPage('settings')">
          <span class="icon">⚙️</span><span>Cài Đặt</span>
        </a>
      </div>

      <div class="sidebar-footer">
        <div class="admin-info">
          <div class="admin-avatar">AD</div>
          <div>
            <div class="admin-name">Admin PolyCinema</div>
            <div class="admin-role">Quản Trị Viên</div>
          </div>
        </div>
        <a href="#" class="logout-btn">🔓 Đăng Xuất</a>
      </div>
    </div>

    <!-- MAIN CONTENT -->
    <div class="main">
      <div class="topbar">
        <div>
          <p class="eyebrow">Control room</p>
          <h1>{{ pageTitles[currentPage] }}</h1>
        </div>
        <div class="topbar-right">
          <ThemeToggle />
          <div class="search-box">⌕ Tìm phim, vé, khách...</div>
          <span class="date-tag">📅 {{ currentDate }}</span>
          <span class="notif">🔔<span class="notif-dot"></span></span>
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
import { ref } from 'vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import DashboardPage  from './components/DashboardPage.vue'
import MoviesPage     from './components/MoviesPage.vue'
import SchedulePage   from './components/SchedulePage.vue'
import TicketsPage    from './components/TicketsPage.vue'
import CustomersPage  from './components/CustomersPage.vue'
import CinemasPage    from './components/CinemasPage.vue'
import PromoPage      from './components/PromoPage.vue'
import ReportsPage    from './components/ReportsPage.vue'
import SettingsPage   from './components/SettingsPage.vue'

const currentPage = ref('dashboard')
const pendingTickets = ref(0)
const currentDate = new Date().toLocaleDateString('vi-VN', {
  weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric'
})
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

function switchPage(page) { currentPage.value = page }
</script>

<style>
:root {
  --admin-accent: var(--accent);
  --admin-ink: var(--text-primary);
  --admin-muted: var(--text-secondary);
  --admin-line: var(--border);
  --admin-card: var(--surface);
  --admin-sidebar: 280px;
}

.admin-container,
.admin-container * {
  box-sizing: border-box;
}

.admin-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  color: var(--admin-ink);
  font-family: 'Raleway', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: var(--page-bg);
  overflow-x: clip;
  transition: background 0.25s ease, color 0.25s ease;
}

.sidebar {
  position: fixed;
  inset: 18px auto 18px 18px;
  z-index: 50;
  width: var(--admin-sidebar);
  display: flex;
  flex-direction: column;
  border: 1px solid var(--admin-line);
  border-radius: 0;
  background: var(--admin-card);
  box-shadow: none;
  overflow: hidden;
}

.sidebar-logo,
.sidebar-menu,
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
  fill: white;
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
  background: var(--surface-plain);
  color: var(--admin-accent);
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 1px;
}

.sidebar-menu {
  flex: 1;
  padding: 18px 14px;
  overflow-y: auto;
}

.menu-section {
  padding: 18px 14px 8px;
  color: #767676;
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 1.4px;
}

.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 46px;
  padding: 12px 14px;
  border-radius: 4px;
  border: 1px solid transparent;
  color: var(--admin-muted);
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.22s ease, background 0.22s ease, color 0.22s ease;
}

.menu-item:hover {
  color: var(--admin-accent);
  background: var(--surface-plain);
  border-color: var(--admin-line);
  transform: none;
}

.menu-item.active {
  color: var(--on-accent);
  background: var(--admin-accent);
  border-color: var(--admin-accent);
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
  border-radius: 0;
  background: #ffffff;
}

.admin-avatar {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: #e8f7fc;
  color: var(--admin-accent);
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

.logout-btn {
  display: block;
  padding: 11px 12px;
  border: 1px solid #29bcea;
  border-radius: 4px;
  color: #29bcea;
  font-size: 13px;
  font-weight: 900;
  text-align: center;
  text-decoration: none;
  transition: 0.22s ease;
}

.logout-btn:hover {
  color: #ffffff;
  border-color: #29bcea;
  background: #29bcea;
}

.main {
  flex: 1;
  min-width: 0;
  min-height: 100vh;
  margin-left: calc(var(--admin-sidebar) + 36px);
  padding: 18px 18px 18px 0;
}

.topbar {
  position: sticky;
  top: 18px;
  z-index: 40;
  min-height: 84px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 16px 22px;
  border: 1px solid var(--admin-line);
  border-radius: 0;
  background: var(--surface-plain);
  backdrop-filter: none;
  box-shadow: none;
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
  font-size: clamp(24px, 3vw, 34px);
  font-weight: 900;
  letter-spacing: -1px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.search-box,
.date-tag,
.notif {
  border: 1px solid var(--admin-line);
  background: var(--surface-plain);
  box-shadow: none;
}

.search-box {
  min-width: 250px;
  padding: 12px 16px;
  border-radius: 999px;
  color: #9ca3af;
  font-size: 13px;
  font-weight: 800;
}

.date-tag {
  padding: 12px 14px;
  border-radius: 999px;
  color: var(--admin-muted);
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
}

.notif {
  position: relative;
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  cursor: pointer;
}

.notif-dot {
  position: absolute;
  top: 9px;
  right: 10px;
  width: 9px;
  height: 9px;
  border: 2px solid white;
  border-radius: 50%;
  background: #ef4444;
  animation: pulse 1.7s infinite;
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
  .search-box { min-width: 190px; }
  .topbar { align-items: flex-start; flex-direction: column; }
  .topbar-right { width: 100%; flex-wrap: wrap; }
}

@media (max-width: 900px) {
  :root { --admin-sidebar: 76px; }
  .sidebar { inset: 12px auto 12px 12px; border-radius: 24px; }
  .sidebar-logo { justify-content: center; padding: 18px 10px; }
  .logo-text,
  .logo-admin,
  .menu-section,
  .menu-item span:not(.icon),
  .admin-info > div,
  .logout-btn { display: none; }
  .menu-item { justify-content: center; padding: 14px 10px; }
  .menu-item:hover { transform: translateX(0) scale(1.04); }
  .menu-badge { position: absolute; top: 7px; right: 8px; }
  .sidebar-footer { padding: 12px; }
  .admin-info { justify-content: center; padding: 8px; }
  .main { margin-left: 100px; padding: 12px 12px 12px 0; }
  .topbar { top: 12px; border-radius: 22px; }
}

@media (max-width: 640px) {
  .admin-container { display: block; padding-bottom: 78px; }
  .sidebar {
    inset: auto 10px 10px 10px;
    width: auto;
    min-height: 0;
    height: 66px;
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
    margin-left: 0;
    padding: 10px;
  }
  .topbar {
    position: relative;
    top: 0;
    min-height: auto;
    padding: 16px;
  }
  .topbar-right { gap: 8px; }
  .search-box { order: 3; width: 100%; min-width: 0; }
  .date-tag { max-width: calc(100% - 56px); overflow: hidden; text-overflow: ellipsis; }
  .content { padding-top: 14px; }
}

/* Child admin pages — shared controls */
.admin-container .btn-primary,
.admin-container .btn-add,
.admin-container .btn-save,
.admin-container .btn-confirm {
  min-height: 44px;
  padding: 10px 18px;
  border: none;
  border-radius: 4px;
  background: #29bcea !important;
  color: #ffffff !important;
  font-weight: 700;
  cursor: pointer;
  box-shadow: none !important;
}

.admin-container .btn-primary:hover,
.admin-container .btn-add:hover,
.admin-container .btn-save:hover {
  background: #1a9fbd !important;
}

.admin-container .btn-secondary,
.admin-container .btn-cancel {
  min-height: 44px;
  padding: 10px 18px;
  border: 1px solid #29bcea !important;
  border-radius: 4px;
  background: transparent !important;
  color: #29bcea !important;
  font-weight: 700;
  cursor: pointer;
}

.admin-container .filter-select,
.admin-container .search-input,
.admin-container input[type="text"],
.admin-container input[type="number"],
.admin-container input[type="date"],
.admin-container input[type="time"],
.admin-container select,
.admin-container textarea {
  min-height: 44px;
  border: 1px solid #efefef !important;
  border-radius: 4px !important;
  background: #ffffff !important;
  color: #000000 !important;
}

.admin-container .data-table th {
  background: #f7f7f7 !important;
  color: #767676 !important;
  border-bottom: 1px solid #efefef !important;
}

.admin-container .data-table td {
  border-bottom: 1px solid #efefef !important;
  color: #7f7e7f !important;
}

.admin-container .modal-overlay {
  background: rgba(0, 0, 0, 0.35) !important;
}

.admin-container .modal {
  border: 1px solid #efefef !important;
  border-radius: 0 !important;
  background: #ffffff !important;
  color: #000000 !important;
  box-shadow: none !important;
}

.admin-container .modal h2,
.admin-container .modal h3 {
  color: #000000 !important;
}

.admin-container .badge,
.admin-container .status-badge {
  border-radius: 4px !important;
}

.admin-container .page-toolbar h2 {
  color: #000000 !important;
}

.admin-container .page-toolbar h2 span,
.admin-container .accent,
.admin-container .highlight {
  color: #29bcea !important;
}

.admin-container .card,
.admin-container .table-card {
  border-radius: 0 !important;
  background: #f7f7f7 !important;
  border: 1px solid #efefef !important;
  box-shadow: none !important;
}

.admin-container tr:hover td,
.admin-container .data-table tr:hover td,
.admin-container .top-table tr:hover td {
  background: #f7fcfe !important;
}

.admin-container .btn-primary,
.admin-container .tab.active {
  background: #29bcea !important;
  background-image: none !important;
}

.admin-container .filter-select,
.admin-container .search-input,
.admin-container .ctrl-input {
  border-color: #efefef !important;
  border-radius: 4px !important;
}

.admin-container .admin-avatar {
  background: #e8f7fc !important;
}

.admin-container .toast--success {
  background: #ffffff !important;
  color: #166534 !important;
  border: 1px solid #86efac !important;
}

.admin-container .toast--error {
  background: #ffffff !important;
  color: #991b1b !important;
  border: 1px solid #fca5a5 !important;
}
</style>
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
  --admin-orange: #ff6b00;
  --admin-gold: #ffb000;
  --admin-ink: #111827;
  --admin-muted: #6b7280;
  --admin-line: rgba(17, 24, 39, 0.1);
  --admin-card: rgba(255, 255, 255, 0.84);
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
  font-family: 'Be Vietnam Pro', 'Noto Sans Vietnamese', 'Nunito', 'Segoe UI', sans-serif;
  background:
    radial-gradient(circle at 12% 8%, rgba(255, 107, 0, 0.18), transparent 32%),
    radial-gradient(circle at 88% 10%, rgba(255, 176, 0, 0.16), transparent 28%),
    linear-gradient(135deg, #fff7ed 0%, #f8fafc 42%, #e5e7eb 100%);
  overflow-x: clip;
}

.sidebar {
  position: fixed;
  inset: 18px auto 18px 18px;
  z-index: 50;
  width: var(--admin-sidebar);
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 28px;
  background: linear-gradient(180deg, #111827 0%, #171717 58%, #2a1204 100%);
  box-shadow: 0 28px 70px rgba(17, 24, 39, 0.28);
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 0%, rgba(255, 107, 0, 0.28), transparent 34%);
  pointer-events: none;
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
  border-radius: 14px;
  background: linear-gradient(135deg, var(--admin-orange), var(--admin-gold));
  box-shadow: 0 12px 26px rgba(255, 107, 0, 0.36);
}

.logo-icon svg {
  width: 24px;
  height: 24px;
  fill: white;
}

.logo-text {
  color: var(--admin-orange);
  font-size: 20px;
  font-weight: 900;
  letter-spacing: -0.6px;
}

.logo-text span {
  color: #fff;
}

.logo-admin {
  margin-left: auto;
  padding: 5px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #fed7aa;
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
  color: rgba(255, 255, 255, 0.38);
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
  border-radius: 16px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.22s ease, background 0.22s ease, color 0.22s ease;
}

.menu-item:hover {
  color: white;
  background: rgba(255, 255, 255, 0.08);
  transform: translateX(4px);
}

.menu-item.active {
  color: white;
  background: linear-gradient(135deg, var(--admin-orange), #f97316);
  box-shadow: 0 14px 28px rgba(255, 107, 0, 0.26);
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
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.07);
}

.admin-avatar {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: #fff7ed;
  color: var(--admin-orange);
  font-size: 14px;
  font-weight: 900;
}

.admin-name {
  color: white;
  font-size: 13px;
  font-weight: 900;
}

.admin-role {
  color: rgba(255, 255, 255, 0.48);
  font-size: 11px;
  font-weight: 700;
}

.logout-btn {
  display: block;
  padding: 11px 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  color: #fed7aa;
  font-size: 13px;
  font-weight: 900;
  text-align: center;
  text-decoration: none;
  transition: 0.22s ease;
}

.logout-btn:hover {
  color: white;
  border-color: rgba(255, 107, 0, 0.7);
  background: rgba(255, 107, 0, 0.14);
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
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(22px);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.11);
}

.eyebrow {
  margin: 0 0 3px;
  color: var(--admin-orange);
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
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 8px 22px rgba(17, 24, 39, 0.06);
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
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 24px;
  background: var(--admin-card);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.08);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.stat-card:hover,
.card:hover {
  transform: translateY(-5px);
  border-color: rgba(255, 107, 0, 0.32);
  box-shadow: 0 26px 58px rgba(255, 107, 0, 0.14);
}

table {
  width: 100%;
  border-collapse: collapse;
}

tr {
  transition: background-color 0.15s ease;
}

tr:hover td {
  background: #fff7ed;
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
</style>
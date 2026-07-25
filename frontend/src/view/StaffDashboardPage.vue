<template>
  <div class="staff-dashboard">
    <header class="staff-header">
      <div class="header-left">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
          stroke-linecap="round" stroke-linejoin="round">
          <path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z" />
        </svg>
        <span class="header-title">PolyCinema Staff</span>
      </div>
      <div class="header-right">
        <span class="staff-name">{{ staffName }}</span>
        <button class="btn-logout" @click="handleLogout">Đăng xuất</button>
      </div>
    </header>

    <main class="dashboard-main">
      <h2 class="welcome">Xin chào, {{ staffName }}!</h2>
      <p class="welcome-sub">Chọn chức năng bên dưới để bắt đầu làm việc.</p>

      <div class="menu-grid">
        <router-link to="/staff/pos" class="menu-card menu-card--pos">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="2" y="3" width="20" height="18" rx="2" />
            <path d="M2 9h20M8 15h2M14 15h2" />
          </svg>
          <span class="card-title">Bán vé tại quầy</span>
          <span class="card-desc">Đặt vé, chọn ghế, combo cho khách tại rạp</span>
          <span class="card-badge">Phase 2</span>
        </router-link>

        <router-link to="/staff/checkin" class="menu-card menu-card--checkin">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2" />
            <path d="M7 7h3v3H7zM14 7h3v3h-3zM7 14h3v3H7z" />
            <path d="M14 14h3v3h-3z" />
          </svg>
          <span class="card-title">Quét QR Check-in</span>
          <span class="card-desc">Quét mã QR vé để check-in khách vào phòng chiếu</span>
          <span class="card-badge">Phase 3</span>
        </router-link>

        <router-link to="/staff/report" class="menu-card menu-card--report">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M3 3v18h18" />
            <path d="M7 16l4-4 4 4 5-6" />
          </svg>
          <span class="card-title">Báo cáo ca</span>
          <span class="card-desc">Xem tổng kết doanh thu, số vé trong ca làm việc</span>
          <span class="card-badge">Phase 3</span>
        </router-link>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const staffName = computed(() => {
  return authStore.user?.hoTen || authStore.user?.email || 'Nhân viên'
})

function handleLogout() {
  authStore.logout()
  router.replace('/staff/login')
}
</script>

<style scoped>
.staff-dashboard {
  min-height: 100vh;
  background: #0f1923;
  color: #fff;
}

.staff-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 2rem;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.header-left svg {
  color: #e94560;
}

.header-title {
  font-weight: 700;
  font-size: 1.1rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.staff-name {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.btn-logout {
  padding: 0.45rem 1rem;
  border: 1px solid rgba(233, 69, 96, 0.5);
  border-radius: 6px;
  background: transparent;
  color: #e94560;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: #e94560;
  color: #fff;
}

.dashboard-main {
  max-width: 900px;
  margin: 0 auto;
  padding: 3rem 1.5rem;
}

.welcome {
  font-size: 1.6rem;
  font-weight: 600;
  margin: 0 0 0.4rem;
}

.welcome-sub {
  color: rgba(255, 255, 255, 0.5);
  margin: 0 0 2.5rem;
  font-size: 0.95rem;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.25rem;
}

.menu-card {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1.5rem;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  text-decoration: none;
  color: #fff;
  transition: all 0.25s;
  position: relative;
}

.menu-card:hover {
  border-color: rgba(233, 69, 96, 0.4);
  background: rgba(255, 255, 255, 0.07);
  transform: translateY(-2px);
}

.menu-card svg {
  color: #e94560;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.card-desc {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.4;
}

.card-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 0.7rem;
  color: rgba(255, 255, 255, 0.4);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 600px) {
  .staff-header {
    padding: 0.8rem 1rem;
  }
  .dashboard-main {
    padding: 2rem 1rem;
  }
  .menu-grid {
    grid-template-columns: 1fr;
  }
}
</style>
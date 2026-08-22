<template>
    <nav class="nav">
      <div class="nav-island">
        <!-- Logo -->
        <router-link to="/" class="logo">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z"/></svg>
          <span class="logo-text">Poly<span class="logo-accent">Cinema</span></span>
        </router-link>

        <!-- Desktop tabs -->
        <nav class="nav-tabs" aria-label="Chọn nội dung" role="tablist">
          <slot name="tabs" />
        </nav>

        <!-- Nav actions -->
        <div class="nav-actions">
          <ThemeToggle />
          <button class="icon-btn gs-trigger" @click="$emit('search-click')" aria-label="Tìm kiếm" title="Tìm kiếm (/)">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          </button>
          <button class="icon-btn lang-toggle" @click="$emit('toggle-lang')" :aria-label="lang === 'vi' ? 'Switch to English' : 'Chuyển sang Tiếng Việt'" :title="lang === 'vi' ? 'Switch to English' : 'Chuyển sang Tiếng Việt'">
            <span class="lang-flag" v-if="lang === 'vi'">
              <svg width="22" height="15" viewBox="0 0 60 40" aria-hidden="true">
                <rect width="60" height="40" fill="#FFFFFF"/>
                <rect y="0" width="60" height="4" fill="#B22234"/><rect y="8" width="60" height="4" fill="#B22234"/>
                <rect y="16" width="60" height="4" fill="#B22234"/><rect y="24" width="60" height="4" fill="#B22234"/>
                <rect y="32" width="60" height="4" fill="#B22234"/>
                <rect width="26" height="22" fill="#3C3B6E"/>
              </svg>
            </span>
            <span class="lang-flag" v-else>
              <svg width="22" height="15" viewBox="0 0 30 20" aria-hidden="true">
                <rect width="30" height="20" fill="#DA251D"/>
                <path d="M15 3.2l1.5 4.6h4.9l-4 2.9 1.5 4.6L15 12.4l-4 2.9 1.5-4.6-4-2.9h4.9z" fill="#FFD200"/>
              </svg>
            </span>
          </button>
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/auth" class="btn btn-ghost">{{ t('login') }}</router-link>
            <router-link to="/auth?mode=register" class="btn btn-primary btn-bib">{{ t('register') }}</router-link>
          </template>
          <template v-else>
            <div class="user-menu-wrapper">
              <button class="user-menu" @click.stop="showDropdown = !showDropdown" :aria-expanded="showDropdown" aria-haspopup="true">
                <div class="avatar">
                  <img v-if="authStore.user?.anhDaiDien" :src="authStore.user.anhDaiDien" :alt="authStore.userInitials" class="avatar-img" @error="e => e.target.style.display='none'" />
                  <span v-else>{{ authStore.userInitials }}</span>
                </div>
                <span class="user-name">{{ authStore.user?.hoTen || authStore.user?.email }}</span>
                <span class="chevron" aria-hidden="true">{{ showDropdown ? '\u25B2' : '\u25BC' }}</span>
              </button>
              <div v-if="showDropdown" class="dropdown glass-card" role="menu">
                <router-link to="/profile" class="dropdown-item" @click="showDropdown=false" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  {{ t('profile') }}
                </router-link>
                <router-link v-if="authStore.userRole !== 'STAFF'" to="/my-tickets" class="dropdown-item" @click="showDropdown=false" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v2z"/><path d="M13 5v2M13 17v2M13 11v2"/></svg>
                  {{ t('tickets') }}
                </router-link>
                <router-link v-if="authStore.userRole !== 'STAFF'" to="/transaction-history" class="dropdown-item" @click="showDropdown=false" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1" ry="1"/><path d="M9 12h6M9 16h4"/></svg>
                  Lịch sử GD
                </router-link>
                <a v-if="authStore.isAdmin" href="/admin" class="dropdown-item" @click="showDropdown=false" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/><circle cx="12" cy="12" r="8"/></svg>
                  Admin Panel
                </a>
                <router-link v-if="authStore.userRole === 'STAFF' || authStore.isAdmin" to="/staff/dashboard" class="dropdown-item" @click="showDropdown=false" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z"/></svg>
                  Khu vực nhân viên
                </router-link>
                <hr class="dropdown-hr" />
                <a href="#" @click.prevent="authStore.logout(); showDropdown=false" class="dropdown-item logout" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                  {{ t('logout') }}
                </a>
              </div>
            </div>
          </template>
        </div>

        <!-- Mobile hamburger -->
        <button class="hamburger" @click.stop="showMobileMenu = !showMobileMenu" :aria-expanded="showMobileMenu" aria-label="Menu">
          <span :class="['ham-line', { 'ham-line--open1': showMobileMenu }]"></span>
          <span :class="['ham-line', { 'ham-line--open2': showMobileMenu }]"></span>
          <span :class="['ham-line', { 'ham-line--open3': showMobileMenu }]"></span>
        </button>
      </div>
    </nav>

    <!-- Mobile drawer -->
    <transition name="drawer">
      <div v-if="showMobileMenu" class="mobile-drawer" @click.self="showMobileMenu=false">
        <div class="drawer-panel">
          <div v-if="authStore.isLoggedIn" class="drawer-user">
            <div class="drawer-avatar">{{ authStore.userInitials }}</div>
            <div>
              <p class="drawer-name">{{ authStore.user?.hoTen || authStore.user?.email }}</p>
              <p class="drawer-level">{{ authStore.user?.capDoThanhVien || 'Thường' }}</p>
            </div>
          </div>
          <div class="drawer-links">
            <div class="drawer-theme-row">
              <ThemeToggle show-label />
            </div>
            <button class="icon-btn drawer-theme-btn" @click="$emit('toggle-lang')">🌐 {{ lang === 'vi' ? 'Switch to English' : 'Chuyển sang Tiếng Việt' }}</button>
          </div>
          <div class="drawer-tabs" @click="showMobileMenu = false">
            <slot name="drawer-tabs" />
          </div>
          <hr class="drawer-hr" />
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/auth" class="drawer-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
              {{ t('login') }}
            </router-link>
            <router-link to="/auth?mode=register" class="drawer-item drawer-item--primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg>
              {{ t('register') }}
            </router-link>
          </template>
          <template v-else>
            <router-link to="/profile" class="drawer-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              {{ t('profile') }}
            </router-link>
            <router-link v-if="authStore.userRole !== 'STAFF'" to="/my-tickets" class="drawer-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v2z"/><path d="M13 5v2M13 17v2M13 11v2"/></svg>
              {{ t('tickets') }}
            </router-link>
            <router-link v-if="authStore.userRole !== 'STAFF'" to="/transaction-history" class="drawer-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1" ry="1"/><path d="M9 12h6M9 16h4"/></svg>
              Lịch sử GD
            </router-link>
            <a v-if="authStore.isAdmin" href="/admin" class="drawer-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/><circle cx="12" cy="12" r="8"/></svg>
              Admin Panel
            </a>
            <router-link v-if="authStore.userRole === 'STAFF' || authStore.isAdmin" to="/staff/dashboard" class="drawer-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z"/></svg>
              Khu vực nhân viên
            </router-link>
            <hr class="drawer-hr" />
            <a href="#" @click.prevent="authStore.logout()" class="drawer-item drawer-item--danger">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
              {{ t('logout') }}
            </a>
          </template>
        </div>
      </div>
    </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import ThemeToggle from '@/components/ThemeToggle.vue'

defineProps({
  t: { type: Function, required: true },
  lang: { type: String, default: 'vi' }
})

defineEmits(['search-click', 'toggle-lang'])

const authStore = useAuthStore()
const showDropdown = ref(false)
const showMobileMenu = ref(false)

const closeDropdown = (e) => {
  if (!e.target.closest('.user-menu-wrapper') && !e.target.closest('.dropdown')) {
    showDropdown.value = false
  }
  if (!e.target.closest('.hamburger') && !e.target.closest('.drawer-panel')) {
    showMobileMenu.value = false
  }
}

onMounted(() => document.addEventListener('click', closeDropdown))
onUnmounted(() => document.removeEventListener('click', closeDropdown))

function closeAll() {
  showDropdown.value = false
  showMobileMenu.value = false
}

defineExpose({ closeAll })
</script>

<style>
/* ── Nav bar ── */
.nav {
  position: sticky; top: 0; z-index: 50;
  background: var(--nav-bg, rgba(5,5,8,0.82));
  backdrop-filter: blur(20px) saturate(1.4);
  -webkit-backdrop-filter: blur(20px) saturate(1.4);
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.06));
}
.nav-island {
  max-width: 1400px; margin: 0 auto;
  display: flex; align-items: center;
  padding: 0 40px; height: 64px;
}
.logo { display: flex; align-items: center; gap: 10px; text-decoration: none; flex-shrink: 0; }
.logo-text { font-family: var(--font-display, 'Playfair Display', Georgia, serif); font-size: 20px; font-weight: 700; color: #fff; letter-spacing: -0.3px; }
.logo-accent { color: var(--electric, #29bcea); }

/* ── Nav tabs ── */
.nav-tabs { display: flex; align-items: center; gap: 4px; margin-left: 20px; }
.nav-tabs .main-tab {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 16px; background: none; border: none;
  border-radius: var(--radius-pill, 999px);
  color: var(--text-secondary, #94a3b8);
  font-size: 14px; font-weight: 700;
  font-family: var(--font-ui, 'Inter', sans-serif);
  cursor: pointer; white-space: nowrap;
  transition: color 0.2s, background 0.2s;
}
.nav-tabs .main-tab:hover { color: var(--text-primary, #f1f5f9); background: var(--glass-bg, rgba(255,255,255,0.06)); }
.nav-tabs .main-tab.active { color: var(--electric, #29bcea); background: rgba(41,188,234,0.12); }

/* ── Phim dropdown ── */
.nav-item-dropdown { position: relative; }
.nav-dropdown {
  position: absolute; top: 100%; left: 50%; transform: translateX(-50%);
  margin-top: 4px; min-width: 150px;
  background: #1a1a2e; border: 1px solid rgba(255,255,255,0.12);
  border-radius: var(--radius-md, 10px);
  padding: 6px; z-index: 100;
  box-shadow: 0 12px 40px rgba(0,0,0,0.6);
}
.nav-dropdown__item {
  display: block; padding: 8px 14px; border-radius: 6px;
  color: #cbd5e1; font-size: 13px; font-weight: 600;
  text-decoration: none; white-space: nowrap;
  transition: background 0.15s, color 0.15s;
}
.nav-dropdown__item:hover { background: rgba(41,188,234,0.15); color: #fff; }
.dd-fade-enter-active, .dd-fade-leave-active { transition: opacity 0.15s, transform 0.15s; }
.dd-fade-enter-from, .dd-fade-leave-to { opacity: 0; transform: translateX(-50%) translateY(-4px); }

/* ── Nav actions ── */
.nav-actions { display: flex; align-items: center; gap: 12px; margin-left: auto; }
.btn {
  min-height: 44px; padding: 10px 20px;
  border-radius: var(--radius-pill, 999px);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px; font-weight: 700;
  text-decoration: none; cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center;
  transition: transform 0.25s var(--ease-out, cubic-bezier(0.4,0,0.2,1)), box-shadow 0.25s, background 0.25s, color 0.25s, border-color 0.25s;
}
.btn-ghost {
  background: transparent; color: var(--text-secondary, #94a3b8);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.10));
}
.btn-ghost:hover { color: #fff; border-color: rgba(255,255,255,0.25); }
.btn-primary { background: var(--electric, #29bcea); color: #fff; border: 1px solid transparent; box-shadow: var(--glow-elec, 0 0 16px rgba(41,188,234,0.30)); }
.btn-primary:hover { background: var(--electric-hover, #1a9fbd); transform: translateY(-1px); }
.icon-btn {
  width: 40px; height: 40px; display: flex; align-items: center; justify-content: center;
  background: none; border: none; border-radius: 50%;
  color: var(--text-secondary, #94a3b8); cursor: pointer;
  transition: color 0.2s, background 0.2s;
}
.icon-btn:hover { color: #fff; background: var(--glass-bg, rgba(255,255,255,0.06)); }
.gs-trigger { flex-shrink: 0; }
.lang-flag { display: flex; align-items: center; justify-content: center; line-height: 0; }

/* ── User menu ── */
.user-menu-wrapper { position: relative; }
.user-menu {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 12px 6px 6px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-pill, 999px);
  cursor: pointer; color: var(--text-primary, #f1f5f9);
  transition: border-color 0.2s, background 0.2s;
}
.user-menu:hover { border-color: rgba(255,255,255,0.18); background: rgba(255,255,255,0.06); }
.avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, var(--electric, #29bcea), var(--gold, #C9A84C));
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; color: #fff; overflow: hidden; flex-shrink: 0;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.user-name { font-size: 13px; font-weight: 600; max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chevron { font-size: 10px; color: var(--text-ghost, rgba(241,245,249,0.45)); margin-left: 2px; }
.dropdown.glass-card {
  position: absolute; top: calc(100% + 8px); right: 0; min-width: 200px;
  background: var(--glass-bg-heavy, rgba(15,15,30,0.96));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 10px);
  padding: 6px; z-index: 100;
  backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.5);
}
.dropdown-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px; border-radius: 6px;
  color: var(--text-secondary, #94a3b8);
  font-size: 13px; font-weight: 600;
  text-decoration: none; white-space: nowrap;
  transition: background 0.15s, color 0.15s;
}
.dropdown-item:hover { background: rgba(41,188,234,0.12); color: var(--electric, #29bcea); }
.dropdown-item.logout { color: #ef4444; }
.dropdown-item.logout:hover { background: rgba(239,68,68,0.12); color: #ef4444; }
.dropdown-hr { border: none; border-top: 1px solid rgba(255,255,255,0.06); margin: 4px 0; }

/* ── Hamburger ── */
.hamburger { display: none; flex-direction: column; gap: 5px; background: none; border: none; cursor: pointer; padding: 8px; z-index: 60; }
.ham-line { display: block; width: 22px; height: 2px; background: var(--text-primary, #f1f5f9); border-radius: 2px; transition: transform 0.3s, opacity 0.3s; }
.ham-line--open1 { transform: translateY(7px) rotate(45deg); }
.ham-line--open2 { opacity: 0; }
.ham-line--open3 { transform: translateY(-7px) rotate(-45deg); }

/* ── Mobile drawer ── */
.mobile-drawer {
  position: fixed; inset: 0; z-index: 200;
  background: rgba(0,0,0,0.5);
}
.drawer-panel {
  position: absolute; top: 0; right: 0; bottom: 0;
  width: 280px; max-width: 85vw;
  background: var(--glass-bg-heavy, rgba(15,15,30,0.98));
  border-left: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  padding: 20px; display: flex; flex-direction: column; gap: 4px;
  overflow-y: auto; overscroll-behavior: contain;
}
.drawer-user { display: flex; align-items: center; gap: 12px; padding: 12px 8px; margin-bottom: 8px; }
.drawer-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: linear-gradient(135deg, var(--electric, #29bcea), var(--gold, #C9A84C));
  display: flex; align-items: center; justify-content: center;
  font-size: 15px; font-weight: 700; color: #fff; flex-shrink: 0;
}
.drawer-name { font-size: 14px; font-weight: 700; color: var(--text-primary, #f1f5f9); margin: 0; }
.drawer-level { font-size: 12px; color: var(--text-ghost, rgba(241,245,249,0.45)); margin: 2px 0 0; }
.drawer-links { padding: 4px 8px; }
.drawer-theme-row { margin-bottom: 8px; }
.drawer-theme-btn { font-size: 13px; color: var(--text-secondary, #94a3b8); }
.drawer-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 8px; border-radius: 8px;
  color: var(--text-secondary, #94a3b8);
  font-size: 14px; font-weight: 600;
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}
.drawer-item:hover { background: rgba(41,188,234,0.12); color: var(--electric, #29bcea); }
.drawer-item--primary { color: var(--electric, #29bcea); }
.drawer-item--danger { color: #ef4444; }
.drawer-item--danger:hover { background: rgba(239,68,68,0.12); color: #ef4444; }
.drawer-hr { border: none; border-top: 1px solid rgba(255,255,255,0.06); margin: 8px 0; }

/* ── Transitions ── */
.drawer-enter-active, .drawer-leave-active { transition: opacity 0.25s; }
.drawer-enter-active .drawer-panel, .drawer-leave-active .drawer-panel { transition: transform 0.3s var(--ease-out, cubic-bezier(0.4,0,0.2,1)); }
.drawer-enter-from, .drawer-leave-to { opacity: 0; }
.drawer-enter-from .drawer-panel, .drawer-leave-to .drawer-panel { transform: translateX(100%); }

/* ── Responsive ── */
@media (max-width: 900px) {
  .nav-tabs { display: none; }
  .nav-actions .btn { display: none; }
  .hamburger { display: flex; }
  .nav-island { padding: 0 16px; }
}
@media (min-width: 901px) {
  .mobile-drawer { display: none; }
}
</style>

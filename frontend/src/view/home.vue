<template>
  <div class="home">
    <!-- NAV -->
    <nav class="nav">
      <div class="nav-island">
        <router-link to="/" class="logo">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z"/></svg>
          <span class="logo-text">Poly<span class="logo-accent">Cinema</span></span>
        </router-link>

        <!-- Desktop nav-actions -->
        <div class="nav-actions">
          <ThemeToggle />
          <button class="icon-btn" @click="toggleLang">{{ lang === 'vi' ? 'EN' : 'VI' }}</button>
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
                <span class="chevron" aria-hidden="true">{{ showDropdown ? '▲' : '▼' }}</span>
              </button>
              <div v-if="showDropdown" class="dropdown glass-card" role="menu">
                <router-link to="/profile" class="dropdown-item" @click="showDropdown=false" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  {{ t('profile') }}
                </router-link>
                <router-link to="/my-tickets" class="dropdown-item" @click="showDropdown=false" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v2z"/><path d="M13 5v2M13 17v2M13 11v2"/></svg>
                  {{ t('tickets') }}
                </router-link>
                <router-link to="/transaction-history" class="dropdown-item" @click="showDropdown=false" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1" ry="1"/><path d="M9 12h6M9 16h4"/></svg>
                  Lịch sử GD
                </router-link>
                <a v-if="authStore.isAdmin" href="/admin" class="dropdown-item" @click="showDropdown=false" role="menuitem">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/><circle cx="12" cy="12" r="8"/></svg>
                  Admin Panel
                </a>
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
            <button class="icon-btn drawer-theme-btn" @click="toggleLang">🌐 {{ lang === 'vi' ? 'Switch to English' : 'Chuyển sang Tiếng Việt' }}</button>
          </div>
          <hr class="drawer-hr" />
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/auth" class="drawer-item" @click="showMobileMenu=false">🔑 Đăng nhập</router-link>
            <router-link to="/auth?mode=register" class="drawer-item drawer-item--primary" @click="showMobileMenu=false">📝 Đăng ký</router-link>
          </template>
          <template v-else>
            <router-link to="/profile" class="drawer-item" @click="showMobileMenu=false">👤 Hồ sơ</router-link>
            <router-link to="/my-tickets" class="drawer-item" @click="showMobileMenu=false">🎟️ Vé của tôi</router-link>
            <router-link to="/transaction-history" class="drawer-item" @click="showMobileMenu=false">📋 Lịch sử GD</router-link>
            <a v-if="authStore.isAdmin" href="/admin" class="drawer-item" @click="showMobileMenu=false">⚙️ Admin Panel</a>
            <hr class="drawer-hr" />
            <a href="#" @click.prevent="authStore.logout(); showMobileMenu=false" class="drawer-item drawer-item--danger">🔓 Đăng xuất</a>
          </template>
        </div>
      </div>
    </transition>

    <!-- HERO STAGE -->
    <div class="hero-stage">
      <div
        class="hero-bg"
        :style="currentBanner ? { backgroundImage: `url(${currentBanner.hinhAnh})` } : {}"
      ></div>
      <div class="hero-vignette"></div>
      <div class="hero-fade"></div>
      <div class="hero-grain"></div>
      <div class="hero-content" v-if="currentBanner">
        <h1 class="hero-title">{{ currentBanner.tieuDe }}</h1>
        <p class="hero-desc">{{ currentBanner.moTa }}</p>
        <button class="btn-bib btn-hero" @click="bookNow">{{ t('bookNow') }}</button>
      </div>
      <div class="hero-content" v-else>
        <p class="hero-loading">{{ t('loading') }}...</p>
      </div>
      <div v-if="movieStore.banners.length > 1" class="banner-dots">
        <button v-for="(_, i) in movieStore.banners" :key="i" :class="['dot', { active: bannerIndex === i }]" @click="bannerIndex = i"></button>
      </div>
    </div>

    <!-- MOVIES SECTION -->
    <section class="section">
      <h2 class="section-title">{{ t('movies') }} <span>{{ t('now') }}</span></h2>
      <div class="tabs">
        <button :class="['tab', { active: activeTab === 'dang_chieu' }]" @click="activeTab = 'dang_chieu'">{{ t('nowShowing') }}</button>
        <button :class="['tab', { active: activeTab === 'sap_chieu' }]" @click="activeTab = 'sap_chieu'">{{ t('comingSoon') }}</button>
      </div>
      <div v-if="isLoading" class="loading">{{ t('loading') }}...</div>
      <div v-else-if="isError" class="error">{{ isError }}</div>
      <div v-else-if="displayMovies.length === 0" class="error">{{ t('noMovies') || 'Không có phim' }}</div>
      <div v-else class="movie-grid">
        <div 
          v-for="(movie, index) in displayMovies" 
          :key="movie.id" 
          class="movie-card"
          :style="{ '--card-index': index }"
          @click="goToMovie(movie.id)"
          role="button"
          tabindex="0"
          @keypress.enter="goToMovie(movie.id)"
          @mousemove="(e) => { const r=e.currentTarget.getBoundingClientRect(); const x=(e.clientX-r.left)/r.width-0.5; const y=(e.clientY-r.top)/r.height-0.5; e.currentTarget.style.transform=`perspective(1000px) rotateY(${x*16}deg) rotateX(${-y*16}deg)`; }"
          @mouseleave="(e) => { e.currentTarget.style.transform=''; }"
        >
          <div class="movie-poster">
            <img
              v-if="movie.poster"
              :src="movie.poster"
              :alt="movie.title"
              @error="(e) => { e.target.style.display='none'; e.target.nextElementSibling.style.display='flex' }"
            />
            <div class="poster-placeholder" :style="movie.poster ? 'display:none' : ''">
              <div class="poster-gradient">
                <span class="poster-icon">🎬</span>
                <span class="poster-title">{{ movie.title }}</span>
              </div>
            </div>
            <div class="movie-overlay">
              <svg viewBox="0 0 24 24" fill="currentColor" width="48" height="48">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </div>
          </div>
          <div class="movie-info">
            <h3>{{ movie.title }}</h3>
            <p class="movie-meta"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> {{ movie.duration }} phút</p>
            <p class="movie-meta rating"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="none"><polygon fill="currentColor" points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg> {{ movie.rating ? Number(movie.rating).toFixed(1) : 'Chưa có' }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- PROMO -->
    <section class="promo">
      <h2>{{ t('promoTitle') }}</h2>
      <p>{{ t('promoDesc') }}</p>
      <button class="btn btn-white">{{ t('joinNow') }}</button>
    </section>

    <!-- FOOTER -->
    <footer class="footer">
      <p>&copy; 2026 PolyCinema. {{ t('allRights') }}</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useMovieStore } from '@/stores/movieStore'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router = useRouter()
const authStore = useAuthStore()
const movieStore = useMovieStore()

const lang = ref(localStorage.getItem('poly_lang') || 'vi')
const activeTab = ref('dang_chieu')
const showDropdown = ref(false)
const showMobileMenu = ref(false)
const bannerIndex = ref(0)
let bannerTimer = null

// Close dropdown when clicking outside
const closeDropdown = (e) => {
  if (!e.target.closest('.user-menu-wrapper') && !e.target.closest('.dropdown')) {
    showDropdown.value = false
  }
  if (!e.target.closest('.hamburger') && !e.target.closest('.drawer-panel')) {
    showMobileMenu.value = false
  }
}

const translations = {
  vi: { login: 'Đăng nhập', register: 'Đăng ký', profile: 'Hồ sơ', tickets: 'Vé của tôi', logout: 'Đăng xuất', movies: 'Phim', now: 'Nổi Bật', nowShowing: 'Đang chiếu', comingSoon: 'Sắp chiếu', bookNow: 'Đặt vé ngay', loading: 'Đang tải', promoTitle: 'Ưu Đãi Thứ 3', promoDesc: 'Giảm 30% vé xem phim vào thứ 3', joinNow: 'Tham Gia', allRights: 'All rights reserved.' },
  en: { login: 'Login', register: 'Register', profile: 'Profile', tickets: 'My Tickets', logout: 'Logout', movies: 'Movies', now: 'Featured', nowShowing: 'Now Showing', comingSoon: 'Coming Soon', bookNow: 'Book Now', loading: 'Loading', promoTitle: 'Tuesday Offer', promoDesc: '30% off all tickets on Tuesday', joinNow: 'Join Now', allRights: 'All rights reserved.' }
}

const t = (key) => translations[lang.value][key] || key
const displayMovies = computed(() => activeTab.value === 'dang_chieu' ? movieStore.phimDangChieu : movieStore.phimSapChieu)
const isLoading = computed(() => activeTab.value === 'dang_chieu' ? movieStore.loading.dangChieu : movieStore.loading.sapChieu)
const isError = computed(() => activeTab.value === 'dang_chieu' ? movieStore.error.dangChieu : movieStore.error.sapChieu)
const currentBanner = computed(() => movieStore.banners[bannerIndex.value])

function startBannerTimer() {
  if (bannerTimer) clearInterval(bannerTimer)
  if (movieStore.banners.length > 1) {
    bannerTimer = setInterval(() => {
      bannerIndex.value = (bannerIndex.value + 1) % movieStore.banners.length
    }, 5000)
  }
}

// Start timer once banners are loaded
watch(() => movieStore.banners.length, (len) => {
  if (len > 1) startBannerTimer()
})

const toggleLang = () => { lang.value = lang.value === 'vi' ? 'en' : 'vi'; localStorage.setItem('poly_lang', lang.value) }
const bookNow = () => { if (!authStore.isLoggedIn) router.push('/auth'); else router.push('/') }
const goToMovie = (id) => router.push({ name: 'movie-detail', params: { id } })

onMounted(() => {
  movieStore.fetchDangChieu()
  movieStore.fetchSapChieu()
  movieStore.fetchBanners()
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
  document.removeEventListener('click', closeDropdown)
})
</script>

<style scoped>
.home {
  background: var(--page-bg);
  color: var(--text-secondary);
  min-height: 100vh;
  font-family: 'Raleway', sans-serif;
  transition: background 0.25s ease, color 0.25s ease;
}

.nav {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 40px;
  background: transparent;
}

.nav-island {
  display: flex;
  align-items: center;
  gap: 24px;
  width: 100%;
  max-width: 1400px;
  border-radius: var(--radius-pill);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  background: rgba(5, 5, 8, 0.85);
  padding: 8px 20px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}

.logo {
  text-decoration: none;
  color: var(--text-primary, #f1f5f9);
  font-weight: 700;
  font-size: 20px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.logo-text {
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary, #f1f5f9);
  letter-spacing: -0.02em;
}

.logo-accent { color: var(--electric, #29bcea); }

.logo span { color: var(--accent); }

.drawer-theme-row {
  margin-bottom: 4px;
}

.nav-actions { display: flex; align-items: center; gap: 12px; margin-left: auto; }

.icon-btn {
  min-width: 44px;
  min-height: 44px;
  border-radius: var(--radius-pill, 999px);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--electric, #29bcea);
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  transition: background 0.2s, border-color 0.2s;
}

.icon-btn:hover { border-color: var(--electric, #29bcea); background: var(--glass-bg-heavy, rgba(255,255,255,0.08)); }

.btn {
  min-height: 44px;
  padding: 10px 18px;
  border-radius: 4px;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, border-color 0.2s;
}

.btn-ghost {
  background: transparent;
  color: var(--text-secondary, #94a3b8);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  transition: background 0.2s, color 0.2s, border-color 0.2s;
}

.btn-ghost:hover { background: var(--glass-bg, rgba(255,255,255,0.04)); color: var(--text-primary, #f1f5f9); border-color: var(--electric, #29bcea); }

.btn-primary {
  background: var(--electric, #29bcea);
  color: var(--on-accent, #ffffff);
  border: none;
}

.btn-primary:hover { background: var(--electric-hover, #1a9fbd); }

/* .btn-bib styles are provided by cinema.css; these supplement in nav context */
.btn-bib {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 20px;
  background: var(--electric, #29bcea);
  color: var(--on-accent, #ffffff);
  border: none;
  border-radius: var(--radius-sm, 6px);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  outline: 1.5px solid rgba(41,188,234,0.45);
  outline-offset: 3px;
  transition: transform 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1)),
              box-shadow 0.3s var(--ease-out, cubic-bezier(0.4,0,0.2,1)),
              outline-offset 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1));
  will-change: transform;
  text-decoration: none;
}

.btn-bib:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--electric-glow, rgba(41,188,234,0.30));
  outline-offset: 5px;
}

.btn-white {
  background: #ffffff;
  color: #29bcea;
  border: 1px solid #ffffff;
}

.btn-white:hover { background: #f7f7f7; }

.user-menu-wrapper { position: relative; }

.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-pill, 999px);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--glass-bg, rgba(255,255,255,0.04));
  user-select: none;
  transition: background 0.2s, border-color 0.2s;
}

.user-menu:hover { border-color: var(--electric, #29bcea); background: var(--glass-bg-heavy, rgba(255,255,255,0.08)); }

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #f1f5f9);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chevron { font-size: 10px; color: var(--text-ghost, rgba(241,245,249,0.45)); }

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #29bcea;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img { width: 100%; height: 100%; object-fit: cover; }

.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  backdrop-filter: var(--glass-blur, blur(20px));
  -webkit-backdrop-filter: var(--glass-blur, blur(20px));
  border-radius: var(--radius-md, 12px);
  min-width: 220px;
  z-index: 200;
  overflow: hidden;
  box-shadow: var(--shadow-lg, 0 12px 40px rgba(0,0,0,0.55));
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: var(--text-secondary, #94a3b8);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-ui, 'Inter', sans-serif);
  transition: background 0.15s, color 0.15s;
  cursor: pointer;
}

.dropdown-item:not(:last-child) { border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08)); }

.dropdown-item:hover { background: var(--glass-bg-heavy, rgba(255,255,255,0.08)); color: var(--text-primary, #f1f5f9); }

.dropdown-item.logout { color: #f87171; }

.dropdown-hr { border: none; border-top: 1px solid var(--glass-border, rgba(255,255,255,0.08)); margin: 0; }

.hamburger {
  display: none;
  flex-direction: column;
  gap: 5px;
  padding: 8px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-primary, #f1f5f9);
  margin-left: auto;
}

.ham-line {
  display: block;
  width: 22px;
  height: 2px;
  background: currentColor;
  border-radius: 2px;
  transition: transform 0.25s, opacity 0.25s;
}

.ham-line--open1 { transform: translateY(7px) rotate(45deg); }
.ham-line--open2 { opacity: 0; }
.ham-line--open3 { transform: translateY(-7px) rotate(-45deg); }

.mobile-drawer {
  position: fixed;
  inset: 0;
  z-index: 300;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  justify-content: flex-end;
}

.drawer-panel {
  width: 280px;
  background: #ffffff;
  height: 100%;
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
  border-left: 1px solid #efefef;
}

.drawer-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0 16px;
  border-bottom: 1px solid #efefef;
  margin-bottom: 4px;
}

.drawer-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #29bcea;
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.drawer-name { font-size: 14px; font-weight: 700; color: #000000; margin: 0; }
.drawer-level { font-size: 11px; color: #767676; margin: 0; }
.drawer-links { display: flex; flex-direction: column; gap: 4px; }

.drawer-theme-btn {
  width: 100%;
  text-align: left;
  padding: 10px 12px;
  font-size: 13px;
  background: #f7f7f7;
  border-radius: 4px;
  color: #7f7e7f;
  border: 1px solid #efefef;
  cursor: pointer;
}

.drawer-hr { border: none; border-top: 1px solid #efefef; margin: 6px 0; }

.drawer-item {
  display: block;
  padding: 12px 14px;
  border-radius: 4px;
  color: #7f7e7f;
  text-decoration: none;
  font-size: 14px;
  font-weight: 700;
  transition: background 0.15s;
  cursor: pointer;
}

.drawer-item:hover { background: #f7f7f7; color: #29bcea; }

.drawer-item--primary {
  background: #29bcea;
  color: #ffffff;
}

.drawer-item--danger { color: #dc2626; }

.drawer-enter-active, .drawer-leave-active { transition: opacity 0.2s; }
.drawer-enter-active .drawer-panel, .drawer-leave-active .drawer-panel { transition: transform 0.25s; }
.drawer-enter-from, .drawer-leave-to { opacity: 0; }
.drawer-enter-from .drawer-panel, .drawer-leave-to .drawer-panel { transform: translateX(100%); }

.hero-stage {
  position: relative;
  height: 100svh;
  min-height: 600px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 120%;
  top: -10%;
  background-size: cover;
  background-position: center;
  will-change: transform;
}

.hero-vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at center, transparent 40%, rgba(5,5,8,0.85) 100%);
  z-index: 1;
}

.hero-fade {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 50%, var(--void, #050508) 100%);
  z-index: 2;
}

.hero-grain {
  position: absolute;
  inset: 0;
  opacity: 0.03;
  mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  z-index: 3;
}

.hero-content {
  position: absolute;
  bottom: 15%;
  left: 5%;
  z-index: 10;
  color: var(--text-primary, #f1f5f9);
  max-width: 600px;
}

.hero-title {
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  font-size: clamp(40px, 7vw, 88px);
  font-weight: 700;
  letter-spacing: -0.04em;
  line-height: 1.1;
  margin-bottom: 16px;
  color: var(--text-primary, #f1f5f9);
}

.hero-desc {
  font-size: clamp(14px, 1.5vw, 18px);
  color: var(--text-secondary, #94a3b8);
  margin-bottom: 28px;
  line-height: 1.6;
  max-width: 480px;
}

.hero-loading {
  color: var(--text-ghost, rgba(241,245,249,0.45));
  font-size: 18px;
}

.btn-hero {
  padding: 14px 36px;
  font-size: 15px;
}

.banner-dots {
  position: absolute;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 10;
}

.dot {
  width: 8px;
  height: 8px;
  min-width: 8px;
  min-height: 8px;
  padding: 0;
  border-radius: 50%;
  background: rgba(255,255,255,0.35);
  border: none;
  cursor: pointer;
  transition: background 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1)), width 0.4s var(--spring, cubic-bezier(0.34,1.56,0.64,1));
  will-change: width;
}

.dot.active {
  background: var(--electric, #29bcea);
  width: 24px;
  border-radius: var(--radius-pill, 999px);
}

.section { padding: 60px 40px; max-width: 1400px; margin: 0 auto; }

.section-title {
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 40px;
  color: var(--text-primary, #f1f5f9);
}

.section-title span { color: #29bcea; }

.tabs {
  display: flex;
  gap: 8px;
  border-bottom: 1px solid #efefef;
  margin-bottom: 32px;
}

.tab {
  min-height: 44px;
  padding: 12px 20px;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  color: var(--text-secondary, #94a3b8);
  cursor: pointer;
  font-weight: 700;
  margin-bottom: -1px;
  transition: color 0.2s, border-color 0.2s;
}

.tab:hover { color: var(--text-primary, #f1f5f9); }

.tab.active { color: var(--electric, #29bcea); border-bottom-color: var(--electric, #29bcea); }

.movie-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 24px;
}

.movie-card {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-radius: var(--radius-md, 12px);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  transform-style: preserve-3d;
  transition: transform 0.15s ease-out, box-shadow 0.3s ease-out;
  animation: card-fade-in 0.5s var(--ease-out, cubic-bezier(0.4,0,0.2,1)) both;
  animation-delay: calc(var(--card-index, 0) * 50ms);
}
@keyframes card-fade-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.movie-card:hover { box-shadow: var(--shadow-md, 0 4px 16px rgba(0,0,0,0.45)); }

.movie-poster {
  position: relative;
  aspect-ratio: 2/3;
  overflow: hidden;
  background: #efefef;
}

.movie-poster img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }

.poster-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f7f7;
}

.poster-gradient {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  text-align: center;
  width: 100%;
  height: 100%;
}

.poster-icon { font-size: 36px; }
.poster-title { font-size: 12px; font-weight: 700; color: var(--text-tertiary); line-height: 1.3; max-width: 90%; }

.movie-card:hover .movie-poster img { transform: scale(1.05); }

.movie-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(5,5,8,0.92) 0%, rgba(5,5,8,0.4) 50%, transparent 100%);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 0.3s var(--ease-out, cubic-bezier(0.4,0,0.2,1)), transform 0.3s var(--ease-out);
  color: #ffffff;
}
.movie-card:hover .movie-overlay { opacity: 1; transform: translateY(0); }

.movie-info { padding: 16px; background: var(--surface-2, #14141f); }

.movie-info h3 {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 8px;
  line-height: 1.3;
  color: var(--text-primary);
}

.movie-info p { font-size: 12px; color: var(--text-tertiary); margin: 4px 0; }
.movie-meta {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-secondary, #94a3b8);
  margin: 3px 0;
}
.rating { color: var(--gold-bright, #F5D17E); font-weight: 700; }

.loading, .error { text-align: center; padding: 40px; color: #767676; }
.error { color: #dc2626; }

.promo {
  background: #29bcea;
  padding: 60px 40px;
  text-align: center;
  color: #ffffff;
  margin: 60px 40px;
  border-radius: 0;
}

.promo h2 { font-size: 32px; font-weight: 700; margin-bottom: 16px; }
.promo p { font-size: 16px; margin-bottom: 24px; opacity: 0.95; }

.footer {
  background: #f7f7f7;
  border-top: 1px solid #efefef;
  padding: 40px;
  text-align: center;
  color: #767676;
}

@media (max-width: 768px) {
  .nav { padding: 8px 16px; }
  .nav-island { padding: 6px 14px; border-radius: var(--radius-lg, 20px); }
  .nav-actions { display: none; }
  .hamburger { display: flex; }
  .hero-stage {
    min-height: 480px;
  }
  .hero-content {
    left: 5%;
    right: 5%;
    max-width: 100%;
  }
  .section { padding: 40px 20px; }
  .movie-grid { grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 12px; }
  .promo { margin: 40px 20px; }
}

@media (max-width: 480px) {
  .movie-grid { grid-template-columns: repeat(2, 1fr); }
  .section-title { font-size: 24px; }
}
</style>

<template>
  <div class="home" :class="{ dark: darkMode }">
    <!-- NAV -->
    <nav class="nav">
      <router-link to="/" class="logo">🎬 Poly<span>Cinema</span></router-link>

      <!-- Desktop nav-actions -->
      <div class="nav-actions">
        <button class="icon-btn" @click="toggleTheme" :title="darkMode ? 'Light mode' : 'Dark mode'">{{ darkMode ? '☀️' : '🌙' }}</button>
        <button class="icon-btn" @click="toggleLang">{{ lang === 'vi' ? 'EN' : 'VI' }}</button>
        <template v-if="!authStore.isLoggedIn">
          <router-link to="/auth" class="btn btn-ghost">{{ t('login') }}</router-link>
          <router-link to="/auth?mode=register" class="btn btn-primary">{{ t('register') }}</router-link>
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
            <div v-if="showDropdown" class="dropdown" role="menu">
              <router-link to="/profile" class="dropdown-item" @click="showDropdown=false" role="menuitem">👤 {{ t('profile') }}</router-link>
              <router-link to="/my-tickets" class="dropdown-item" @click="showDropdown=false" role="menuitem">🎟️ {{ t('tickets') }}</router-link>
              <router-link to="/transaction-history" class="dropdown-item" @click="showDropdown=false" role="menuitem">📋 Lịch sử GD</router-link>
              <a v-if="authStore.isAdmin" href="/admin" class="dropdown-item" @click="showDropdown=false" role="menuitem">⚙️ Admin Panel</a>
              <hr class="dropdown-hr" />
              <a href="#" @click.prevent="authStore.logout(); showDropdown=false" class="dropdown-item logout" role="menuitem">🔓 {{ t('logout') }}</a>
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
            <button class="icon-btn drawer-theme-btn" @click="toggleTheme">{{ darkMode ? '☀️ Light mode' : '🌙 Dark mode' }}</button>
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

    <!-- BANNER SLIDER -->
    <div v-if="currentBanner" class="banner-slider" :style="{ backgroundImage: `url(${currentBanner.hinhAnh})` }">
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <h1>{{ currentBanner.tieuDe }}</h1>
        <p>{{ currentBanner.moTa }}</p>
        <button class="btn btn-primary btn-lg" @click="bookNow">{{ t('bookNow') }}</button>
      </div>
      <div v-if="movieStore.banners.length > 1" class="banner-dots">
        <button v-for="(_, i) in movieStore.banners" :key="i" :class="['dot', { active: bannerIndex === i }]" @click="bannerIndex = i"></button>
      </div>
    </div>
    <div v-else class="banner-placeholder">
      <p>{{ t('loading') }}...</p>
    </div>

    <!-- MOVIES SECTION -->
    <section class="section">
      <h2 class="section-title">{{ t('movies') }} <span>{{ t('now') }}</span></h2>
      <div class="tabs">
        <button :class="['tab', { active: activeTab === 'dangChieu' }]" @click="activeTab = 'dangChieu'">{{ t('nowShowing') }}</button>
        <button :class="['tab', { active: activeTab === 'sapChieu' }]" @click="activeTab = 'sapChieu'">{{ t('comingSoon') }}</button>
      </div>
      <div v-if="isLoading" class="loading">{{ t('loading') }}...</div>
      <div v-else-if="isError" class="error">{{ isError }}</div>
      <div v-else-if="displayMovies.length === 0" class="error">{{ t('noMovies') || 'Không có phim' }}</div>
      <div v-else class="movie-grid">
        <div 
          v-for="movie in displayMovies" 
          :key="movie.id" 
          class="movie-card" 
          @click="goToMovie(movie.id)"
          role="button"
          tabindex="0"
          @keypress.enter="goToMovie(movie.id)"
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
            <p>⏱ {{ movie.duration }} phút</p>
            <p class="rating">⭐ {{ movie.rating ? Number(movie.rating).toFixed(1) : 'Chưa có' }}</p>
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

const router = useRouter()
const authStore = useAuthStore()
const movieStore = useMovieStore()

const lang = ref(localStorage.getItem('poly_lang') || 'vi')
const darkMode = ref(localStorage.getItem('poly_theme') !== 'light')
const activeTab = ref('dangChieu')
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
const displayMovies = computed(() => activeTab.value === 'dangChieu' ? movieStore.phimDangChieu : movieStore.phimSapChieu)
const isLoading = computed(() => activeTab.value === 'dangChieu' ? movieStore.loading.dangChieu : movieStore.loading.sapChieu)
const isError = computed(() => activeTab.value === 'dangChieu' ? movieStore.error.dangChieu : movieStore.error.sapChieu)
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

const toggleTheme = () => { darkMode.value = !darkMode.value; localStorage.setItem('poly_theme', darkMode.value ? 'dark' : 'light') }
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
.home { background: #f8fafc; color: #1e2937; min-height: 100vh; transition: all 0.3s }
.home.dark { background: #0f172a; color: #f1f5f9 }

.nav { display: flex; justify-content: space-between; align-items: center; padding: 16px 40px; background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); border-bottom: 1px solid #e2e8f0; position: sticky; top: 0; z-index: 100 }
.home.dark .nav { background: rgba(15,23,42,0.95); border-bottom-color: #334155 }

.logo { text-decoration: none; color: inherit; font-weight: 900; font-size: 20px; display: flex; align-items: center; gap: 8px }
.logo span { color: #ffd700 }

.nav-actions { display: flex; align-items: center; gap: 12px }
.icon-btn { width: 36px; height: 36px; border-radius: 8px; border: 1px solid #e2e8f0; background: white; cursor: pointer; font-size: 16px; transition: all 0.2s }
.home.dark .icon-btn { background: #1e2937; border-color: #334155 }
.icon-btn:hover { border-color: #ffd700; color: #ffd700 }

.btn { padding: 8px 16px; border-radius: 8px; border: none; font-weight: 700; cursor: pointer; transition: all 0.2s; text-decoration: none; display: inline-block }
.btn-ghost { background: transparent; color: inherit; border: 1px solid #e2e8f0 }
.home.dark .btn-ghost { border-color: #334155 }
.btn-ghost:hover { border-color: #ffd700; color: #ffd700 }
.btn-primary { background: linear-gradient(135deg, #ffd700, #ffed4e); color: #0f172a; font-weight: 800 }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(255,215,0,0.3) }
.btn-lg { padding: 12px 28px; font-size: 15px }
.btn-white { background: white; color: #ffd700; font-weight: 800 }
.btn-white:hover { transform: translateY(-2px); box-shadow: 0 8px 16px rgba(0,0,0,0.2) }

.user-menu-wrapper { position: relative; }
.user-menu { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 5px 10px; border-radius: 50px; border: 1px solid #e2e8f0; background: white; user-select: none; }
.home.dark .user-menu { background: #1e2937; border-color: #334155 }
.user-menu:hover { border-color: #ffd700; }
.user-name { font-size: 14px; font-weight: 600; max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chevron { font-size: 10px; color: #94a3b8; }
.avatar { width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg, #ffd700, #ffed4e); color: #0f172a; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 12px; flex-shrink: 0; overflow: hidden; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.dropdown { position: absolute; top: calc(100% + 8px); right: 0; background: white; border: 1px solid #e2e8f0; border-radius: 12px; min-width: 200px; z-index: 200; box-shadow: 0 8px 24px rgba(0,0,0,0.12); overflow: hidden; }
.home.dark .dropdown { background: #1e2937; border-color: #334155 }
.dropdown-item { display: block; padding: 11px 16px; color: inherit; text-decoration: none; font-size: 14px; font-weight: 600; transition: background 0.15s; cursor: pointer; }
.dropdown-item:not(:last-child) { border-bottom: 1px solid #f1f5f9; }
.home.dark .dropdown-item:not(:last-child) { border-bottom-color: #263348; }
.dropdown-item:hover { background: #f8fafc; }
.home.dark .dropdown-item:hover { background: #263348; }
.dropdown-item.logout { color: #ef4444; }
.dropdown-hr { border: none; border-top: 1px solid #e2e8f0; margin: 0; }
.home.dark .dropdown-hr { border-top-color: #263348; }

/* hamburger — hidden on desktop */
.hamburger { display: none; flex-direction: column; gap: 5px; padding: 8px; background: none; border: none; cursor: pointer; }
.ham-line { display: block; width: 22px; height: 2px; background: currentColor; border-radius: 2px; transition: transform .25s, opacity .25s; }
.ham-line--open1 { transform: translateY(7px) rotate(45deg); }
.ham-line--open2 { opacity: 0; }
.ham-line--open3 { transform: translateY(-7px) rotate(-45deg); }

/* mobile drawer */
.mobile-drawer { position: fixed; inset: 0; z-index: 300; background: rgba(0,0,0,.55); display: flex; justify-content: flex-end; }
.drawer-panel { width: 280px; background: #0f172a; height: 100%; padding: 24px 20px; display: flex; flex-direction: column; gap: 8px; overflow-y: auto; border-left: 1px solid rgba(255,215,0,.15); }
.home.dark .drawer-panel { background: #0b1120; }
.drawer-user { display: flex; align-items: center; gap: 12px; padding: 12px 0 16px; border-bottom: 1px solid rgba(255,215,0,.12); margin-bottom: 4px; }
.drawer-avatar { width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg,#ffd700,#ffed4e); color: #0f172a; font-size: 16px; font-weight: 900; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.drawer-name  { font-size: 14px; font-weight: 800; color: #f1f5f9; margin: 0; }
.drawer-level { font-size: 11px; color: #64748b; margin: 0; }
.drawer-links { display: flex; flex-direction: column; gap: 4px; }
.drawer-theme-btn { width: 100%; text-align: left; padding: 10px 12px; font-size: 13px; background: rgba(255,255,255,.05); border-radius: 8px; color: #f1f5f9; border: none; cursor: pointer; }
.drawer-hr { border: none; border-top: 1px solid rgba(255,215,0,.1); margin: 6px 0; }
.drawer-item { display: block; padding: 12px 14px; border-radius: 9px; color: #f1f5f9; text-decoration: none; font-size: 14px; font-weight: 700; transition: background .15s; cursor: pointer; }
.drawer-item:hover { background: rgba(255,215,0,.08); }
.drawer-item--primary { background: linear-gradient(135deg,rgba(255,215,0,.15),rgba(255,215,0,.08)); color: #ffd700; }
.drawer-item--danger  { color: #fca5a5; }
.drawer-item--danger:hover { background: rgba(239,68,68,.08); }
.drawer-enter-active, .drawer-leave-active { transition: opacity .2s; }
.drawer-enter-active .drawer-panel, .drawer-leave-active .drawer-panel { transition: transform .25s; }
.drawer-enter-from, .drawer-leave-to { opacity: 0; }
.drawer-enter-from .drawer-panel, .drawer-leave-to .drawer-panel { transform: translateX(100%); }

.banner-slider { height: 400px; background-size: cover; background-position: center; display: flex; align-items: center; justify-content: center; position: relative }
.banner-placeholder { height: 400px; background: linear-gradient(135deg, #e2e8f0, #cbd5e1); display: flex; align-items: center; justify-content: center; color: #64748b; font-size: 18px }
.home.dark .banner-placeholder { background: linear-gradient(135deg, #1e2937, #263348) }
.banner-overlay { position: absolute; inset: 0; background: linear-gradient(to right, rgba(0,0,0,0.6), rgba(0,0,0,0.2)) }
.banner-content { position: relative; z-index: 2; color: white; text-align: left; max-width: 600px; padding: 40px }
.banner-content h1 { font-size: 48px; font-weight: 900; margin-bottom: 16px }
.banner-content p { font-size: 16px; margin-bottom: 24px; opacity: 0.9 }
.banner-dots { position: absolute; bottom: 20px; left: 50%; transform: translateX(-50%); display: flex; gap: 8px; z-index: 10 }
.dot { width: 10px; height: 10px; border-radius: 50%; background: rgba(255,255,255,0.5); border: none; cursor: pointer; transition: all 0.3s }
.dot.active { background: #ffd700; width: 28px; border-radius: 5px }

.section { padding: 60px 40px; max-width: 1400px; margin: 0 auto }
.section-title { font-size: 32px; font-weight: 800; margin-bottom: 40px }
.section-title span { color: #ffd700 }

.tabs { display: flex; gap: 4px; border-bottom: 2px solid #e2e8f0; margin-bottom: 32px }
.home.dark .tabs { border-bottom-color: #334155 }
.tab { padding: 12px 20px; background: none; border: none; color: #94a3b8; cursor: pointer; font-weight: 700; border-bottom: 3px solid transparent; margin-bottom: -2px; transition: all 0.2s }
.tab:hover { color: inherit }
.tab.active { color: #ffd700; border-bottom-color: #ffd700 }

.movie-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 24px }
.movie-card { background: white; border-radius: 12px; overflow: hidden; cursor: pointer; transition: all 0.3s; border: 1px solid #e2e8f0 }
.home.dark .movie-card { background: #1e2937; border-color: #334155 }
.movie-card:hover { transform: translateY(-8px); box-shadow: 0 20px 40px rgba(255,215,0,0.15); border-color: #ffd700 }

.movie-poster { position: relative; aspect-ratio: 2/3; overflow: hidden; background: #e2e8f0 }
.home.dark .movie-poster { background: #334155 }
.movie-poster img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s }
.poster-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1a2540, #0f172a); }
.poster-gradient { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; padding: 16px; text-align: center; width: 100%; height: 100%; }
.poster-icon { font-size: 36px; }
.poster-title { font-size: 12px; font-weight: 700; color: #94a3b8; line-height: 1.3; max-width: 90%; }
.movie-card:hover .movie-poster img { transform: scale(1.08) }
.movie-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s; color: #ffd700; }
.movie-overlay svg { filter: drop-shadow(0 2px 8px rgba(0,0,0,0.5)); }
.movie-card:hover .movie-overlay { opacity: 1 }

.movie-info { padding: 16px }
.movie-info h3 { font-size: 14px; font-weight: 800; margin-bottom: 8px; line-height: 1.3 }
.movie-info p { font-size: 12px; color: #94a3b8; margin: 4px 0 }
.home.dark .movie-info p { color: #64748b }
.rating { color: #ffd700; font-weight: 700 }

.loading, .error { text-align: center; padding: 40px; color: #94a3b8 }
.error { color: #ef4444 }

.promo { background: linear-gradient(135deg, #c2410c, #ffd700, #f97316); padding: 60px 40px; text-align: center; color: white; margin: 60px 40px; border-radius: 20px }
.promo h2 { font-size: 32px; font-weight: 800; margin-bottom: 16px }
.promo p { font-size: 16px; margin-bottom: 24px; opacity: 0.9 }

.footer { background: #f8fafc; border-top: 1px solid #e2e8f0; padding: 40px; text-align: center; color: #94a3b8 }
.home.dark .footer { background: #060d1a; border-top-color: #334155; color: #6b7280 }

@media (max-width: 768px) {
  .nav { padding: 12px 20px }
  .nav-actions { display: none }
  .hamburger { display: flex; color: #1e2937 }
  .home.dark .hamburger { color: #f1f5f9 }
  .banner-content { padding: 20px }
  .banner-content h1 { font-size: 32px }
  .section { padding: 40px 20px }
  .movie-grid { grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 12px }
  .promo { margin: 40px 20px }
}

@media (max-width: 480px) {
  .movie-grid { grid-template-columns: repeat(2, 1fr) }
  .banner-content h1 { font-size: 24px }
  .section-title { font-size: 24px }
}
</style>

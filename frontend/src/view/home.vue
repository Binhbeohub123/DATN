<template>
  <div class="home">
    <!-- NAV -->
    <nav class="nav">
      <router-link to="/" class="logo">🎬 Poly<span>Cinema</span></router-link>

      <!-- Desktop nav-actions -->
      <div class="nav-actions">
        <ThemeToggle />
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
        <button :class="['tab', { active: activeTab === 'dang_chieu' }]" @click="activeTab = 'dang_chieu'">{{ t('nowShowing') }}</button>
        <button :class="['tab', { active: activeTab === 'sap_chieu' }]" @click="activeTab = 'sap_chieu'">{{ t('comingSoon') }}</button>
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 40px;
  background: var(--surface-plain);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  text-decoration: none;
  color: #000000;
  font-weight: 700;
  font-size: 20px;
}

.logo span { color: #29bcea; }

.nav-actions { display: flex; align-items: center; gap: 12px; }

.icon-btn {
  min-width: 44px;
  min-height: 44px;
  border-radius: 4px;
  border: 1px solid #efefef;
  background: #f7f7f7;
  color: #29bcea;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
}

.icon-btn:hover { border-color: #29bcea; background: #ffffff; }

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
  color: #29bcea;
  border: 1px solid #29bcea;
}

.btn-ghost:hover { background: #29bcea; color: #ffffff; }

.btn-primary {
  background: #29bcea;
  color: #ffffff;
  border: none;
}

.btn-primary:hover { background: #1a9fbd; }

.banner-content .btn-lg {
  padding: 10px 22px;
  font-size: 14.4px;
  min-height: 44px;
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
  border-radius: 4px;
  border: 1px solid #efefef;
  background: #f7f7f7;
  user-select: none;
}

.user-menu:hover { border-color: #29bcea; }

.user-name {
  font-size: 14px;
  font-weight: 700;
  color: #000000;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chevron { font-size: 10px; color: #767676; }

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
  background: #ffffff;
  border: 1px solid #efefef;
  border-radius: 4px;
  min-width: 220px;
  z-index: 200;
  overflow: hidden;
}

.dropdown-item {
  display: block;
  padding: 12px 16px;
  color: #7f7e7f;
  text-decoration: none;
  font-size: 14px;
  font-weight: 700;
  transition: background 0.15s;
  cursor: pointer;
}

.dropdown-item:not(:last-child) { border-bottom: 1px solid #efefef; }

.dropdown-item:hover { background: #f7f7f7; color: #29bcea; }

.dropdown-item.logout { color: #dc2626; }

.dropdown-hr { border: none; border-top: 1px solid #efefef; margin: 0; }

.hamburger {
  display: none;
  flex-direction: column;
  gap: 5px;
  padding: 8px;
  background: none;
  border: none;
  cursor: pointer;
  color: #000000;
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

.banner-slider {
  height: clamp(200px, 32vh, 300px);
  max-height: 300px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

.banner-placeholder {
  height: clamp(200px, 32vh, 300px);
  max-height: 300px;
  background: #f7f7f7;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #767676;
  font-size: 18px;
  flex-shrink: 0;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, rgba(0, 0, 0, 0.55), rgba(0, 0, 0, 0.15));
}

.banner-content {
  position: relative;
  z-index: 2;
  color: #ffffff;
  text-align: left;
  max-width: 520px;
  padding: 20px 32px;
}

.banner-content h1 {
  font-size: clamp(22px, 3.5vw, 32px);
  font-weight: 700;
  margin-bottom: 10px;
  color: #ffffff;
  line-height: 1.2;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.banner-content p {
  font-size: 14.4px;
  margin-bottom: 16px;
  opacity: 0.95;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.banner-dots {
  position: absolute;
  bottom: 12px;
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
  background: rgba(255, 255, 255, 0.45);
  border: none;
  cursor: pointer;
  transition: background 0.2s, width 0.2s;
}

.dot.active {
  background: #29bcea;
  width: 20px;
  border-radius: 4px;
}

.section { padding: 60px 40px; max-width: 1400px; margin: 0 auto; }

.section-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 40px;
  color: #000000;
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
  color: #7f7e7f;
  cursor: pointer;
  font-weight: 700;
  margin-bottom: -1px;
  transition: color 0.2s, border-color 0.2s;
}

.tab:hover { color: #000000; }

.tab.active { color: #29bcea; border-bottom-color: #29bcea; }

.movie-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 24px;
}

.movie-card {
  background: #f7f7f7;
  border-radius: 0;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s;
  border: 1px solid #efefef;
}

.movie-card:hover { border-color: #29bcea; }

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
.poster-title { font-size: 12px; font-weight: 700; color: #767676; line-height: 1.3; max-width: 90%; }

.movie-card:hover .movie-poster img { transform: scale(1.05); }

.movie-overlay {
  position: absolute;
  inset: 0;
  background: rgba(41, 188, 234, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: #ffffff;
}

.movie-card:hover .movie-overlay { opacity: 1; }

.movie-info { padding: 16px; background: #ffffff; }

.movie-info h3 {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 8px;
  line-height: 1.3;
  color: #000000;
}

.movie-info p { font-size: 12px; color: #767676; margin: 4px 0; }
.rating { color: #29bcea; font-weight: 700; }

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
  .nav { padding: 12px 20px; }
  .nav-actions { display: none; }
  .hamburger { display: flex; }
  .banner-slider,
  .banner-placeholder {
    height: clamp(180px, 28vh, 240px);
    max-height: 240px;
  }
  .banner-content { padding: 16px 20px; }
  .section { padding: 40px 20px; }
  .movie-grid { grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 12px; }
  .promo { margin: 40px 20px; }
}

@media (max-width: 480px) {
  .movie-grid { grid-template-columns: repeat(2, 1fr); }
  .section-title { font-size: 24px; }
}
</style>

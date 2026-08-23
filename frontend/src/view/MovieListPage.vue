<template>
  <div class="movie-list-page">
    <!-- NAV -->
    <SiteHeader ref="siteHeaderRef" :t="t" :lang="lang" @search-click="globalSearchRef?.open()" @toggle-lang="toggleLang">
      <template #tabs>
        <div class="nav-item-dropdown" @mouseenter="phimDropdownOpen = true" @mouseleave="phimDropdownOpen = false">
          <button class="main-tab active" role="button">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
            <span class="main-tab-label">Phim</span>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
          </button>
          <transition name="dd-fade">
            <div v-show="phimDropdownOpen" class="nav-dropdown">
              <router-link to="/phim-dangchieu" class="nav-dropdown__item" @click="phimDropdownOpen = false">Đang Chiếu</router-link>
              <router-link to="/phim-sapchieu" class="nav-dropdown__item" @click="phimDropdownOpen = false">Sắp Chiếu</router-link>
            </div>
          </transition>
        </div>
        <router-link to="/" class="main-tab">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          <span class="main-tab-label">Rạp Chiếu</span>
        </router-link>
        <router-link to="/#khuyen-mai" class="main-tab">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          <span class="main-tab-label">Khuyến Mãi</span>
        </router-link>
        <router-link to="/#gioi-thieu" class="main-tab">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <span class="main-tab-label">Giới Thiệu</span>
        </router-link>
      </template>
      <template #drawer-tabs>
        <router-link to="/phim-dangchieu" class="drawer-item">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
          Đang Chiếu
        </router-link>
        <router-link to="/phim-sapchieu" class="drawer-item">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
          Sắp Chiếu
        </router-link>
        <router-link to="/" class="drawer-item">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          Rạp Chiếu
        </router-link>
        <router-link to="/#khuyen-mai" class="drawer-item">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          Khuyến Mãi
        </router-link>
        <router-link to="/#gioi-thieu" class="drawer-item">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          Giới Thiệu
        </router-link>
      </template>
    </SiteHeader>

    <!-- MAIN CONTENT -->
    <div class="ml-content">
      <div class="ml-header">
        <h1 class="ml-title">{{ pageTitle }}</h1>
        <p class="ml-subtitle">Tổng cộng {{ movies.length }} phim</p>
      </div>

      <div v-if="loading" class="ml-state">
        <div class="spinner"></div>
        <p>Đang tải...</p>
      </div>
      <div v-else-if="movies.length === 0" class="ml-state ml-empty">
        <p>Không có phim nào</p>
      </div>

      <div v-else class="ml-grid">
        <div v-for="m in movies" :key="m.id" class="ml-card">
          <div class="ml-card__poster" @click="goDetail(m.id)">
            <img v-if="m.poster" :src="m.poster" :alt="m.title" loading="lazy"
                 @error="(e) => { e.target.style.display='none'; e.target.nextElementSibling.style.display='flex' }" />
            <div class="ml-poster-fallback" :style="m.poster ? 'display:none' : ''">
              <span>&#x1F3AC;</span>
              <span>{{ m.title }}</span>
            </div>
            <div v-if="m.trailer" class="ml-card__trailer-btn" @click.stop="openTrailer(m.trailer)">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
            </div>
          </div>
          <div class="ml-card__info">
            <h3 class="ml-card__title" @click="goDetail(m.id)">{{ m.title }}</h3>
            <div class="ml-card__meta">
              <span v-if="m.genre" class="ml-tag ml-tag--genre">{{ m.genre }}</span>
              <span v-if="m.duration" class="ml-tag ml-tag--dur">{{ m.duration }} phút</span>
            </div>

          </div>
        </div>
      </div>
    </div>

    <!-- FOOTER -->
    <SiteFooter :t="t" :lang="lang" :cinemas="movieStore.cinemas" @toggle-lang="setLang" @footer-nav="handleFooterNav" />

    <VideoModal :visible="showModal" :url="trailerUrl" @close="showModal = false" />

    <GlobalSearchOverlay ref="globalSearchRef" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMovieStore } from '@/stores/movieStore'
import { useAuthStore } from '@/stores/authStore'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import VideoModal from '@/components/VideoModal.vue'
import GlobalSearchOverlay from '@/components/GlobalSearchOverlay.vue'

const route = useRoute()
const router = useRouter()
const movieStore = useMovieStore()
const authStore = useAuthStore()

const showModal = ref(false)
const trailerUrl = ref('')
const siteHeaderRef = ref(null)
const phimDropdownOpen = ref(false)
const lang = ref(localStorage.getItem('poly_lang') || 'vi')

const translations = {
  vi: { login: 'Đăng nhập', register: 'Đăng ký', profile: 'Hồ sơ', tickets: 'Vé của tôi', logout: 'Đăng xuất', tabPhim: 'Phim', tabRap: 'Rạp Chiếu', tabKhuyenMai: 'Khuyến Mãi', tabGioiThieu: 'Giới Thiệu', footerTagline: 'Đặt vé nhanh, combo ngon, giải trí trọn vẹn', footerBuyTickets: 'Mua Vé', footerBuyPopcorn: 'Combo Bắp Nước', footerMovies: 'Phim', footerAccount: 'Tài Khoản', footerExplore: 'Khám Phá', footerTheaters: 'Hệ Thống Rạp', footerAllTheaters: 'Tất cả hệ thống rạp', nowShowing: 'Đang chiếu', comingSoon: 'Sắp chiếu', bookNow: 'Đặt vé', allRights: 'All rights reserved.' },
  en: { login: 'Login', register: 'Register', profile: 'Profile', tickets: 'My Tickets', logout: 'Logout', tabPhim: 'Movies', tabRap: 'Cinemas', tabKhuyenMai: 'Promotions', tabGioiThieu: 'About Us', footerTagline: 'Book fast, tasty combos, complete entertainment', footerBuyTickets: 'Buy Tickets', footerBuyPopcorn: 'Popcorn Combos', footerMovies: 'Movies', footerAccount: 'Account', footerExplore: 'Explore', footerTheaters: 'Cinema System', footerAllTheaters: 'All cinemas', nowShowing: 'Now Showing', comingSoon: 'Coming Soon', bookNow: 'Book Now', allRights: 'All rights reserved.' }
}
const t = (key) => translations[lang.value]?.[key] || key

const isDangChieu = computed(() => route.path === '/phim-dangchieu')
const pageTitle = computed(() => isDangChieu.value ? 'Phim Đang Chiếu' : 'Phim Sắp Chiếu')
const movies = computed(() => isDangChieu.value ? movieStore.phimDangChieu : movieStore.phimSapChieu)
const loading = computed(() => isDangChieu.value ? movieStore.loading.dangChieu : movieStore.loading.sapChieu)

function toggleLang() {
  lang.value = lang.value === 'vi' ? 'en' : 'vi'
  localStorage.setItem('poly_lang', lang.value)
  window.location.reload()
}
function setLang(v) {
  if (lang.value === v) return
  lang.value = v
  localStorage.setItem('poly_lang', v)
  window.location.reload()
}
function openTrailer(url) { trailerUrl.value = url; showModal.value = true }
function goDetail(id) { router.push(`/phim/${id}`) }

function handleFooterNav({ action, tab }) {
  siteHeaderRef.value?.closeAll()
  if (action === 'movies' || action === 'movies-tab') {
    router.push('/phim-dangchieu')
  } else if (action === 'tab') {
    if (tab === 'rap_chieu') router.push('/')
    else if (tab === 'khuyen_mai') router.push('/#khuyen-mai')
    else if (tab === 'gioi_thieu') router.push('/#gioi-thieu')
  }
}

const globalSearchRef = ref(null)

onMounted(() => {
  movieStore.fetchCinemas()
  if (isDangChieu.value) movieStore.fetchDangChieu()
  else movieStore.fetchSapChieu()
})

watch(() => route.path, (p) => {
  if (p === '/phim-dangchieu') movieStore.fetchDangChieu()
  else if (p === '/phim-sapchieu') movieStore.fetchSapChieu()
})
</script>

<style scoped>
.movie-list-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.ml-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 80px;
  width: 100%;
  flex: 1;
}

.ml-header { text-align: center; margin-bottom: 36px; }
.ml-title { font-size: 28px; font-weight: 700; color: #fff; margin-bottom: 8px; }
.ml-subtitle { color: rgba(255,255,255,0.5); font-size: 14px; }

.ml-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 80px 0; color: rgba(255,255,255,0.6); }
.ml-empty p { font-size: 16px; }

.spinner {
  width: 36px; height: 36px; border: 3px solid rgba(255,255,255,0.15);
  border-top-color: #e50914; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.ml-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
@media (max-width: 900px) { .ml-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 640px) { .ml-grid { grid-template-columns: repeat(2, 1fr); gap: 14px; } }

.ml-card { display: flex; flex-direction: column; }

.ml-card__poster {
  position: relative; aspect-ratio: 2/3; border-radius: 12px; overflow: hidden;
  background: #1a1a2e; cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.ml-card__poster:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,0.5); }
.ml-card__poster img { width: 100%; height: 100%; object-fit: cover; display: block; }

.ml-poster-fallback {
  width: 100%; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; font-size: 13px; color: rgba(255,255,255,0.5);
  background: linear-gradient(135deg, #1a1a2e, #16213e);
}
.ml-poster-fallback span:first-child { font-size: 36px; }

.ml-card__trailer-btn {
  position: absolute; bottom: 12px; right: 12px;
  width: 44px; height: 44px; border-radius: 50%;
  background: rgba(229,9,20,0.9); color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: transform 0.2s, background 0.2s;
  backdrop-filter: blur(4px);
}
.ml-card__trailer-btn:hover { transform: scale(1.1); background: #e50914; }

.ml-card__info { padding: 10px 2px 0; }
.ml-card__title {
  font-size: 15px; font-weight: 600; color: #fff;
  margin: 0 0 6px; cursor: pointer;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  line-height: 1.35;
}
.ml-card__title:hover { color: #e50914; }

.ml-card__meta { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
.ml-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 4px;
  background: rgba(255,255,255,0.08); color: rgba(255,255,255,0.6);
}
.ml-tag--genre { max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ml-tag--dur { color: rgba(255,255,255,0.45); }

</style>

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
                <router-link v-if="authStore.userRole !== 'STAFF'" to="/profile" class="dropdown-item" @click="showDropdown=false" role="menuitem">
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
            <button class="icon-btn drawer-theme-btn" @click="toggleLang">🌐 {{ lang === 'vi' ? 'Switch to English' : 'Chuyển sang Tiếng Việt' }}</button>
          </div>
          <hr class="drawer-hr" />
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/auth" class="drawer-item" @click="showMobileMenu=false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
              Đăng nhập
            </router-link>
            <router-link to="/auth?mode=register" class="drawer-item drawer-item--primary" @click="showMobileMenu=false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg>
              Đăng ký
            </router-link>
          </template>
          <template v-else>
            <router-link v-if="authStore.userRole !== 'STAFF'" to="/profile" class="drawer-item" @click="showMobileMenu=false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              Hồ sơ
            </router-link>
            <router-link v-if="authStore.userRole !== 'STAFF'" to="/my-tickets" class="drawer-item" @click="showMobileMenu=false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v2z"/><path d="M13 5v2M13 17v2M13 11v2"/></svg>
              Vé của tôi
            </router-link>
            <router-link v-if="authStore.userRole !== 'STAFF'" to="/transaction-history" class="drawer-item" @click="showMobileMenu=false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1" ry="1"/><path d="M9 12h6M9 16h4"/></svg>
              Lịch sử GD
            </router-link>
            <a v-if="authStore.isAdmin" href="/admin" class="drawer-item" @click="showMobileMenu=false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/><circle cx="12" cy="12" r="8"/></svg>
              Admin Panel
            </a>
            <router-link v-if="authStore.userRole === 'STAFF' || authStore.isAdmin" to="/staff/dashboard" class="drawer-item" @click="showMobileMenu=false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z"/></svg>
              Khu vực nhân viên
            </router-link>
            <hr class="drawer-hr" />
            <a href="#" @click.prevent="authStore.logout(); showMobileMenu=false" class="drawer-item drawer-item--danger">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
              Đăng xuất
            </a>
          </template>
        </div>
      </div>
    </transition>

    <!-- HERO STAGE -->
    <div
      class="hero-stage"
      :class="{ 'hero-stage--clickable': currentBanner?.linkUrl }"
      @click="currentBanner?.linkUrl ? onBannerClick() : null"
      :role="currentBanner?.linkUrl ? 'button' : null"
      :tabindex="currentBanner?.linkUrl ? 0 : null"
      :aria-label="currentBanner?.linkUrl ? 'Xem thêm: ' + currentBanner.tieuDe : null"
      @keypress.enter="currentBanner?.linkUrl ? onBannerClick() : null"
    >
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
        <button
          class="btn-bib btn-hero"
          @click="onBannerCta"
        >{{ t('bookNow') }}</button>
      </div>
      <div class="hero-content" v-else>
        <p class="hero-loading">{{ t('loading') }}...</p>
      </div>
      <div v-if="movieStore.banners.length > 1" class="banner-dots">
        <button
          class="banner-arrow banner-arrow--left"
          @click.stop="prevBanner"
          :aria-label="'Banner trước'"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
        <button v-for="(_, i) in movieStore.banners" :key="i" :class="['dot', { active: bannerIndex === i }]" @click.stop="bannerIndex = i; resetBannerTimer()"></button>
        <button
          class="banner-arrow banner-arrow--right"
          @click.stop="nextBanner"
          :aria-label="'Banner tiếp theo'"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
      </div>
    </div>

    <!-- TOP-LEVEL TAB SWITCHER -->
    <nav class="main-tabs-bar" aria-label="Chọn nội dung">
      <div class="main-tabs">
        <button
          :class="['main-tab', { active: mainTab === 'phim' }]"
          @click="mainTab = 'phim'"
          :aria-selected="mainTab === 'phim'"
          role="tab"
          aria-controls="main-panel-phim"
          id="main-tab-phim"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
          {{ t('tabPhim') }}
        </button>
        <button
          :class="['main-tab', { active: mainTab === 'rap_chieu' }]"
          @click="mainTab = 'rap_chieu'"
          :aria-selected="mainTab === 'rap_chieu'"
          role="tab"
          aria-controls="main-panel-rap"
          id="main-tab-rap"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          {{ t('tabRap') }}
        </button>
        <button
          :class="['main-tab', { active: mainTab === 'khuyen_mai' }]"
          @click="mainTab = 'khuyen_mai'"
          :aria-selected="mainTab === 'khuyen_mai'"
          role="tab"
          aria-controls="main-panel-khuyen-mai"
          id="main-tab-khuyen-mai"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          {{ t('tabKhuyenMai') }}
        </button>
        <button
          :class="['main-tab', { active: mainTab === 'gioi_thieu' }]"
          @click="mainTab = 'gioi_thieu'"
          :aria-selected="mainTab === 'gioi_thieu'"
          role="tab"
          aria-controls="main-panel-gioi-thieu"
          id="main-tab-gioi-thieu"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ t('tabGioiThieu') }}
        </button>
      </div>
    </nav>

    <!-- PANEL: PHIM -->
    <div v-show="mainTab === 'phim'" id="main-panel-phim" role="tabpanel" aria-labelledby="main-tab-phim">

    <!-- FEATURED MOVIES -->
    <section class="section section--featured" aria-labelledby="featured-title">
      <h2 class="section-title" id="featured-title">{{ t('featured') }} <span>{{ t('topRated') }}</span></h2>
      <div v-if="movieStore.loading.noiBat" class="loading" role="status">{{ t('loading') }}...</div>
      <div v-else-if="movieStore.error.noiBat" class="error" role="alert">{{ movieStore.error.noiBat }}</div>
      <div v-else-if="movieStore.phimNoiBat.length === 0" class="loading">{{ t('noMovies') }}</div>
      <div v-else class="carousel" ref="featuredCarouselRef" role="list" aria-label="Phim nổi bật">
        <div
          v-for="(movie, index) in movieStore.phimNoiBat"
          :key="movie.id"
          class="carousel-item movie-card"
          :style="{ '--card-index': index }"
          role="listitem"
          @click="goToMovie(movie.id)"
          tabindex="0"
          @keypress.enter="goToMovie(movie.id)"
          @mousemove="tiltCard"
          @mouseleave="resetTilt"
        >
          <div class="featured-rank" aria-hidden="true">{{ index + 1 }}</div>
          <div class="movie-poster">
            <img
              v-if="movie.poster"
              :src="movie.poster"
              :alt="movie.title"
              loading="lazy"
              @error="(e) => { e.target.style.display='none'; e.target.nextElementSibling.style.display='flex' }"
            />
            <div class="poster-placeholder" :style="movie.poster ? 'display:none' : ''">
              <div class="poster-gradient">
                <span class="poster-icon" aria-hidden="true">🎬</span>
                <span class="poster-title">{{ movie.title }}</span>
              </div>
            </div>
            <div class="movie-overlay" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="currentColor" width="48" height="48"><path d="M8 5v14l11-7z"/></svg>
            </div>
          </div>
          <div class="movie-info">
            <h3>{{ movie.title }}</h3>
            <p class="movie-meta"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> {{ movie.duration }} phút</p>
            <p class="movie-meta rating"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="none" aria-hidden="true"><polygon fill="currentColor" points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg> {{ movie.rating ? Number(movie.rating).toFixed(1) : 'Chưa có' }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- MOVIES SECTION -->
    <section class="section" aria-labelledby="movies-title">
      <div class="section-header">
        <h2 class="section-title" id="movies-title">{{ t('movies') }} <span>{{ t('schedule') }}</span></h2>
        <!-- Search bar -->
        <div class="search-bar" role="search">
          <label for="movie-search" class="sr-only">{{ t('searchPlaceholder') }}</label>
          <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input
            id="movie-search"
            type="search"
            class="search-input"
            :placeholder="t('searchPlaceholder')"
            v-model="searchQuery"
            @input="onSearch"
            autocomplete="off"
          />
          <button v-if="searchQuery" class="search-clear" @click="clearSearch" :aria-label="t('clearSearch')">✕</button>
        </div>
      </div>

      <div v-if="searchQuery && searchLoading" class="loading" role="status">{{ t('loading') }}...</div>
      <div v-else-if="searchQuery && searchResults.length === 0 && !searchLoading" class="error" role="alert">{{ t('noResults') }}</div>

      <template v-if="!searchQuery">
        <div class="tabs" role="tablist" :aria-label="t('movieTabs')">
          <button
            :class="['tab', { active: activeTab === 'dang_chieu' }]"
            @click="activeTab = 'dang_chieu'"
            role="tab"
            :aria-selected="activeTab === 'dang_chieu'"
            id="tab-dang-chieu"
            aria-controls="panel-dang-chieu"
          >{{ t('nowShowing') }}</button>
          <button
            :class="['tab', { active: activeTab === 'sap_chieu' }]"
            @click="activeTab = 'sap_chieu'"
            role="tab"
            :aria-selected="activeTab === 'sap_chieu'"
            id="tab-sap-chieu"
            aria-controls="panel-sap-chieu"
          >{{ t('comingSoon') }}</button>
        </div>
        <div v-if="isLoading" class="loading" role="status">{{ t('loading') }}...</div>
        <div v-else-if="isError" class="error" role="alert">{{ isError }}</div>
        <div v-else-if="displayMovies.length === 0" class="error">{{ t('noMovies') }}</div>
        <div v-else class="carousel-wrapper">
          <button
            class="carousel-arrow carousel-arrow--left"
            @click="scrollCarousel('main', -1)"
            :aria-label="t('scrollLeft')"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="15 18 9 12 15 6"/></svg>
          </button>
          <div
            ref="mainCarouselRef"
            class="carousel carousel--peek"
            :id="activeTab === 'dang_chieu' ? 'panel-dang-chieu' : 'panel-sap-chieu'"
            role="tabpanel"
            :aria-labelledby="activeTab === 'dang_chieu' ? 'tab-dang-chieu' : 'tab-sap-chieu'"
          >
            <div
              v-for="(movie, index) in displayMovies"
              :key="movie.id"
              class="carousel-item movie-card"
              :style="{ '--card-index': index }"
              @click="goToMovie(movie.id)"
              role="button"
              tabindex="0"
              @keypress.enter="goToMovie(movie.id)"
              @mousemove="tiltCard"
              @mouseleave="resetTilt"
            >
              <div class="movie-poster">
                <img
                  v-if="movie.poster"
                  :src="movie.poster"
                  :alt="movie.title"
                  loading="lazy"
                  @error="(e) => { e.target.style.display='none'; e.target.nextElementSibling.style.display='flex' }"
                />
                <div class="poster-placeholder" :style="movie.poster ? 'display:none' : ''">
                  <div class="poster-gradient">
                    <span class="poster-icon" aria-hidden="true">🎬</span>
                    <span class="poster-title">{{ movie.title }}</span>
                  </div>
                </div>
                <div class="movie-overlay" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="currentColor" width="48" height="48"><path d="M8 5v14l11-7z"/></svg>
                </div>
              </div>
              <div class="movie-info">
                <h3>{{ movie.title }}</h3>
                <p class="movie-meta"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> {{ movie.duration }} phút</p>
                <p class="movie-meta rating"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="none" aria-hidden="true"><polygon fill="currentColor" points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg> {{ movie.rating ? Number(movie.rating).toFixed(1) : 'Chưa có' }}</p>
              </div>
            </div>
          </div>
          <button
            class="carousel-arrow carousel-arrow--right"
            @click="scrollCarousel('main', 1)"
            :aria-label="t('scrollRight')"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="9 18 15 12 9 6"/></svg>
          </button>
        </div>
      </template>

      <!-- Search results carousel -->
      <template v-else-if="searchResults.length > 0">
        <div class="carousel" role="list" :aria-label="t('searchResults')">
          <div
            v-for="(movie, index) in searchResults"
            :key="movie.id"
            class="carousel-item movie-card"
            :style="{ '--card-index': index }"
            role="listitem"
            @click="goToMovie(movie.id)"
            tabindex="0"
            @keypress.enter="goToMovie(movie.id)"
            @mousemove="tiltCard"
            @mouseleave="resetTilt"
          >
            <div class="movie-poster">
              <img
                v-if="movie.poster"
                :src="movie.poster"
                :alt="movie.title"
                loading="lazy"
                @error="(e) => { e.target.style.display='none'; e.target.nextElementSibling.style.display='flex' }"
              />
              <div class="poster-placeholder" :style="movie.poster ? 'display:none' : ''">
                <div class="poster-gradient">
                  <span class="poster-icon" aria-hidden="true">🎬</span>
                  <span class="poster-title">{{ movie.title }}</span>
                </div>
              </div>
              <div class="movie-overlay" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="currentColor" width="48" height="48"><path d="M8 5v14l11-7z"/></svg>
              </div>
            </div>
            <div class="movie-info">
              <h3>{{ movie.title }}</h3>
              <p class="movie-meta"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> {{ movie.duration }} phút</p>
              <p class="movie-meta rating"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="none" aria-hidden="true"><polygon fill="currentColor" points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg> {{ movie.rating ? Number(movie.rating).toFixed(1) : 'Chưa có' }}</p>
            </div>
          </div>
        </div>
      </template>
    </section>

    <!-- PROMO -->
    <section class="promo">
      <h2>{{ t('promoTitle') }}</h2>
      <p>{{ t('promoDesc') }}</p>
      <button class="btn btn-white">{{ t('joinNow') }}</button>
    </section>

    </div><!-- /PANEL: PHIM -->

    <!-- PANEL: RẠP CHIẾU -->
    <div v-show="mainTab === 'rap_chieu'" id="main-panel-rap" role="tabpanel" aria-labelledby="main-tab-rap">

    <!-- CINEMA LISTING -->
    <section class="section section--cinemas" aria-labelledby="cinemas-title">
      <h2 class="section-title" id="cinemas-title">{{ t('tabRap') }} <span>PolyCinema</span></h2>
      <div v-if="movieStore.loading.cinemas" class="loading" role="status">{{ t('loading') }}...</div>
      <div v-else-if="movieStore.error.cinemas" class="error" role="alert">{{ movieStore.error.cinemas }}</div>
      <div v-else-if="movieStore.cinemas.length === 0" class="loading">{{ t('noCinemas') }}</div>
      <div v-else class="cinema-grid" role="list">
        <article
          v-for="cinema in movieStore.cinemas"
          :key="cinema.id"
          class="cinema-card cinema-card--clickable"
          role="button"
          tabindex="0"
          :aria-label="`Xem chi tiết rạp ${cinema.tenRap}`"
          @click="router.push({ name: 'cinema-detail', params: { id: cinema.id } })"
          @keypress.enter="router.push({ name: 'cinema-detail', params: { id: cinema.id } })"
        >
          <div class="cinema-img-wrap">
            <img
              v-if="cinema.hinhAnh"
              :src="cinema.hinhAnh"
              :alt="cinema.tenRap"
              class="cinema-img"
              loading="lazy"
              @error="(e) => { e.target.style.display='none'; e.target.nextElementSibling.style.display='flex' }"
            />
            <div class="cinema-img-placeholder" :style="cinema.hinhAnh ? 'display:none' : ''">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="7" width="20" height="15" rx="2"/><path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/><line x1="12" y1="12" x2="12" y2="16"/><line x1="10" y1="14" x2="14" y2="14"/></svg>
            </div>
          </div>
          <div class="cinema-info">
            <h3 class="cinema-name">{{ cinema.tenRap }}</h3>
            <p v-if="cinema.thanhPho" class="cinema-city">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              {{ cinema.thanhPho }}
            </p>
            <p class="cinema-address">{{ cinema.diaChi }}</p>
            <a
              v-if="cinema.latitude != null && cinema.longitude != null"
              :href="`https://maps.google.com/?q=${cinema.latitude},${cinema.longitude}`"
              class="cinema-map-link"
              target="_blank"
              rel="noopener noreferrer"
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polygon points="3 11 22 2 13 21 11 13 3 11"/></svg>
              {{ t('viewMap') }}
            </a>
          </div>
        </article>
      </div>
    </section>

    </div><!-- /PANEL: RẠP CHIẾU -->

    <!-- PANEL: KHUYẾN MÃI -->
    <div v-show="mainTab === 'khuyen_mai'" id="main-panel-khuyen-mai" role="tabpanel" aria-labelledby="main-tab-khuyen-mai">
      <section class="section section--promos" aria-labelledby="promos-title">
        <h2 class="section-title" id="promos-title">{{ t('tabKhuyenMai') }} <span>{{ t('promoActive') }}</span></h2>
        <div v-if="promoLoading" class="loading" role="status">{{ t('loading') }}...</div>
        <div v-else-if="promoError" class="error" role="alert">{{ promoError }}</div>
        <div v-else-if="promos.length === 0" class="loading">{{ t('noPromos') }}</div>
        <div v-else class="promo-grid" role="list">
          <article
            v-for="promo in promos"
            :key="promo.id"
            class="promo-card"
            role="listitem"
          >
            <!-- Header: name + discount badge -->
            <div class="promo-card__header">
              <h3 class="promo-card__name">{{ promo.tenKhuyenMai }}</h3>
              <span class="promo-card__badge" :class="promo.loaiGiamGia === 'percent' ? 'badge--percent' : 'badge--fixed'">
                <template v-if="promo.loaiGiamGia === 'percent'">-{{ promo.giaTriGiam }}%</template>
                <template v-else>-{{ formatCurrency(promo.giaTriGiam) }}₫</template>
              </span>
            </div>
            <!-- Promo code -->
            <p class="promo-card__code">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
              {{ promo.maKhuyenMai }}
            </p>
            <!-- Description -->
            <p v-if="promo.moTa" class="promo-card__desc">{{ promo.moTa }}</p>
            <!-- Date range -->
            <p class="promo-card__dates">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              {{ formatDate(promo.ngayBatDau) }} – {{ formatDate(promo.ngayKetThuc) }}
            </p>
            <!-- Min order -->
            <p v-if="promo.donHangToiThieu" class="promo-card__min">
              {{ t('promoMin') }}: {{ formatCurrency(promo.donHangToiThieu) }}₫
            </p>
            <!-- Applicable movies or system-wide CTA -->
            <div class="promo-card__movies" v-if="promo.phims && promo.phims.length > 0">
              <p class="promo-card__movies-label">{{ t('promoAppliesTo') }}:</p>
              <div class="promo-movies-list">
                <button
                  v-for="phim in promo.phims"
                  :key="phim.id"
                  class="promo-movie-chip"
                  @click="goToMovie(phim.id)"
                  :title="phim.tenPhim"
                >
                  <img
                    v-if="phim.posterUrl"
                    :src="phim.posterUrl"
                    :alt="phim.tenPhim"
                    class="promo-movie-chip__poster"
                    loading="lazy"
                    @error="(e) => e.target.style.display='none'"
                  />
                  <span class="promo-movie-chip__name">{{ phim.tenPhim }}</span>
                </button>
              </div>
            </div>
            <div v-else class="promo-card__cta">
              <router-link to="/" @click="mainTab = 'phim'" class="promo-card__btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
                {{ t('chooseMovie') }}
              </router-link>
            </div>
          </article>
        </div>
      </section>
    </div><!-- /PANEL: KHUYẾN MÃI -->

    <!-- PANEL: GIỚI THIỆU -->
    <div v-show="mainTab === 'gioi_thieu'" id="main-panel-gioi-thieu" role="tabpanel" aria-labelledby="main-tab-gioi-thieu">
      <div
        class="gioi-thieu-stage"
        :style="gioiThieu.hinhAnhUrl ? { backgroundImage: `url(${gioiThieu.hinhAnhUrl})` } : {}"
        aria-label="Giới thiệu PolyCinema"
      >
        <div class="gioi-thieu-overlay"></div>
        <div class="gioi-thieu-body" v-if="!gioiThieuLoading">
          <h2 class="gioi-thieu-title">{{ gioiThieu.tieuDe || t('tabGioiThieu') }}</h2>
          <p
            v-for="(para, i) in (gioiThieu.noiDung || '').split('\n\n').filter(Boolean)"
            :key="i"
            class="gioi-thieu-para"
          >{{ para }}</p>
        </div>
        <div v-else class="gioi-thieu-body">
          <p class="gioi-thieu-para">{{ t('loading') }}...</p>
        </div>
      </div>
    </div><!-- /PANEL: GIỚI THIỆU -->

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
const mainTab = ref('phim')    // top-level: 'phim' | 'rap_chieu'
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
  vi: { login: 'Đăng nhập', register: 'Đăng ký', profile: 'Hồ sơ', tickets: 'Vé của tôi', logout: 'Đăng xuất', movies: 'Lịch Chiếu', schedule: 'Phim', now: 'Nổi Bật', nowShowing: 'Đang chiếu', comingSoon: 'Sắp chiếu', bookNow: 'Đặt vé ngay', loading: 'Đang tải', promoTitle: 'Ưu Đãi Thứ 3', promoDesc: 'Giảm 30% vé xem phim vào thứ 3', joinNow: 'Tham Gia', allRights: 'All rights reserved.', featured: 'Phim', topRated: 'Đánh Giá Cao', noMovies: 'Không có phim', searchPlaceholder: 'Tìm phim...', clearSearch: 'Xóa tìm kiếm', noResults: 'Không tìm thấy phim phù hợp', searchResults: 'Kết quả tìm kiếm', movieTabs: 'Danh mục phim', cinemaSystem: 'Hệ Thống', cinemaNetwork: 'Rạp Chiếu', noCinemas: 'Chưa có thông tin rạp', viewMap: 'Xem bản đồ', scrollLeft: 'Cuộn trái', scrollRight: 'Cuộn phải', tabPhim: 'Phim', tabRap: 'Rạp Chiếu', tabKhuyenMai: 'Khuyến Mãi', promoActive: 'Đang Áp Dụng', noPromos: 'Hiện không có khuyến mãi', promoAppliesTo: 'Áp dụng cho phim', promoMin: 'Đơn hàng tối thiểu', chooseMovie: 'Chọn phim', tabGioiThieu: 'Giới Thiệu' },
  en: { login: 'Login', register: 'Register', profile: 'Profile', tickets: 'My Tickets', logout: 'Logout', movies: 'Movie', schedule: 'Schedule', now: 'Featured', nowShowing: 'Now Showing', comingSoon: 'Coming Soon', bookNow: 'Book Now', loading: 'Loading', promoTitle: 'Tuesday Offer', promoDesc: '30% off all tickets on Tuesday', joinNow: 'Join Now', allRights: 'All rights reserved.', featured: 'Top', topRated: 'Rated Movies', noMovies: 'No movies available', searchPlaceholder: 'Search movies...', clearSearch: 'Clear search', noResults: 'No movies found', searchResults: 'Search results', movieTabs: 'Movie categories', cinemaSystem: 'Cinema', cinemaNetwork: 'Network', noCinemas: 'No cinemas available', viewMap: 'View map', scrollLeft: 'Scroll left', scrollRight: 'Scroll right', tabPhim: 'Movies', tabRap: 'Cinemas', tabKhuyenMai: 'Promotions', promoActive: 'Active', noPromos: 'No active promotions', promoAppliesTo: 'Applies to', promoMin: 'Min. order', chooseMovie: 'Browse movies', tabGioiThieu: 'About Us' }
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

function resetBannerTimer() { startBannerTimer() }

function prevBanner() {
  const len = movieStore.banners.length
  if (!len) return
  bannerIndex.value = (bannerIndex.value - 1 + len) % len
  resetBannerTimer()
}

function nextBanner() {
  const len = movieStore.banners.length
  if (!len) return
  bannerIndex.value = (bannerIndex.value + 1) % len
  resetBannerTimer()
}

// Start timer once banners are loaded
watch(() => movieStore.banners.length, (len) => {
  if (len > 1) startBannerTimer()
})

const toggleLang = () => { lang.value = lang.value === 'vi' ? 'en' : 'vi'; localStorage.setItem('poly_lang', lang.value) }
const bookNow = () => { if (!authStore.isLoggedIn) router.push('/auth'); else router.push('/') }
const goToMovie = (id) => router.push({ name: 'movie-detail', params: { id } })

/**
 * Navigate using the current banner's linkUrl (internal router path like "/phim/1").
 * Falls back to bookNow() if linkUrl is absent.
 */
function onBannerClick() {
  const url = currentBanner.value?.linkUrl
  if (url && url.trim()) {
    router.push(url.trim())
  } else {
    bookNow()
  }
}

function onBannerCta() {
  onBannerClick()
}

// ── Search ────────────────────────────────────────────────────
const searchQuery   = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
let   searchTimer   = null

function onSearch() {
  clearTimeout(searchTimer)
  if (!searchQuery.value.trim()) { searchResults.value = []; return }
  searchLoading.value = true
  searchTimer = setTimeout(async () => {
    searchResults.value = await movieStore.searchMovies(searchQuery.value.trim())
    searchLoading.value = false
  }, 300)
}

function clearSearch() {
  searchQuery.value = ''
  searchResults.value = []
}

// ── Card tilt ─────────────────────────────────────────────────
function tiltCard(e) {
  const r = e.currentTarget.getBoundingClientRect()
  const x = (e.clientX - r.left) / r.width  - 0.5
  const y = (e.clientY - r.top)  / r.height - 0.5
  e.currentTarget.style.transform = `perspective(1000px) rotateY(${x * 16}deg) rotateX(${-y * 16}deg)`
}
function resetTilt(e) { e.currentTarget.style.transform = '' }

// ── Carousel arrow scroll + wheel-to-horizontal ───────────────
const mainCarouselRef     = ref(null)
const featuredCarouselRef = ref(null)

function scrollCarousel(which, direction) {
  const el = which === 'main' ? mainCarouselRef.value : featuredCarouselRef.value
  if (!el) return
  // Scroll by the width of one card (200px) plus the gap (20px)
  el.scrollBy({ left: direction * 220, behavior: 'smooth' })
}

/** Non-passive wheel handler: converts vertical scroll into horizontal scroll.
 *  Attached imperatively so we can pass { passive: false } — Vue's @wheel
 *  binding is passive by default and cannot call preventDefault(). */
const wheelHandlers = new WeakMap()

function attachWheelScroll(el) {
  if (!el) return
  const handler = (e) => {
    if (e.deltaY === 0) return
    e.preventDefault()
    el.scrollLeft += e.deltaY
  }
  wheelHandlers.set(el, handler)
  el.addEventListener('wheel', handler, { passive: false })
}

function detachWheelScroll(el) {
  if (!el) return
  const handler = wheelHandlers.get(el)
  if (handler) el.removeEventListener('wheel', handler)
}

// ── Giới Thiệu (About tab) ───────────────────────────────────
const gioiThieu       = ref({ tieuDe: '', noiDung: '', hinhAnhUrl: null })
const gioiThieuLoading = ref(false)

async function fetchGioiThieu() {
  gioiThieuLoading.value = true
  try {
    const res = await api.get('/gioi-thieu')
    gioiThieu.value = res.data || {}
  } catch { /* use defaults already set */ }
  finally { gioiThieuLoading.value = false }
}

// ── Promotions (Khuyến Mãi tab) ───────────────────────────────
import api from '@/services/api'

const promos      = ref([])
const promoLoading = ref(false)
const promoError  = ref('')

async function fetchPromos() {
  promoLoading.value = true
  promoError.value   = ''
  try {
    const res = await api.get('/khuyen-mai/active')
    promos.value = res.data || []
  } catch {
    promoError.value = 'Không tải được khuyến mãi'
  } finally {
    promoLoading.value = false
  }
}

function formatCurrency(val) {
  if (val == null) return '0'
  return Number(val).toLocaleString('vi-VN')
}

function formatDate(d) {
  if (!d) return '—'
  // d is 'yyyy-MM-dd' string from LocalDate
  const [y, m, day] = String(d).split('-')
  return `${day}/${m}/${y}`
}

onMounted(() => {
  movieStore.fetchDangChieu()
  movieStore.fetchSapChieu()
  movieStore.fetchNoiBat()
  movieStore.fetchCinemas()
  movieStore.fetchBanners()
  fetchPromos()
  fetchGioiThieu()
  document.addEventListener('click', closeDropdown)
  // Attempt immediate attach (works if data was already cached)
  setTimeout(() => {
    attachWheelScroll(mainCarouselRef.value)
    attachWheelScroll(featuredCarouselRef.value)
  }, 0)
})

// Re-attach whenever the ref element appears (data loads asynchronously)
watch(mainCarouselRef,     (el) => attachWheelScroll(el))
watch(featuredCarouselRef, (el) => attachWheelScroll(el))

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
  document.removeEventListener('click', closeDropdown)
  detachWheelScroll(mainCarouselRef.value)
  detachWheelScroll(featuredCarouselRef.value)
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
  background: var(--surface-1, #0f0f17);
  border-left: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  height: 100%;
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
}

.drawer-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0 16px;
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  margin-bottom: 4px;
}

.drawer-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--gold, #C9A84C);
  color: #0D0D0D;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.drawer-name { font-size: 14px; font-weight: 700; color: var(--text-primary, #f1f5f9); margin: 0; }
.drawer-level { font-size: 11px; color: var(--text-secondary, #94a3b8); margin: 0; }
.drawer-links { display: flex; flex-direction: column; gap: 4px; }

.drawer-theme-btn {
  width: 100%;
  text-align: left;
  padding: 10px 12px;
  font-size: 13px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-radius: var(--radius-sm, 6px);
  color: var(--text-secondary, #94a3b8);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  cursor: pointer;
}

.drawer-hr { border: none; border-top: 1px solid var(--glass-border, rgba(255,255,255,0.08)); margin: 6px 0; }

.drawer-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: var(--radius-sm, 6px);
  color: var(--text-secondary, #94a3b8);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-ui, 'Inter', sans-serif);
  transition: background 0.15s, color 0.15s;
  cursor: pointer;
  border: none;
  background: none;
  width: 100%;
}

.drawer-item:hover { background: var(--glass-bg, rgba(255,255,255,0.04)); color: var(--text-primary, #f1f5f9); }

.drawer-item--primary {
  background: var(--gold, #C9A84C);
  color: #0D0D0D;
}
.drawer-item--primary:hover { background: var(--gold-bright, #F5D17E); color: #0D0D0D; }

.drawer-item--danger { color: #f87171; }
.drawer-item--danger:hover { background: rgba(248,113,113,0.08); color: #f87171; }

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

/* When banner has a linked movie — show pointer and subtle focus ring */
.hero-stage--clickable { cursor: pointer; }
.hero-stage--clickable:focus-visible {
  outline: 3px solid var(--electric, #29bcea);
  outline-offset: -3px;
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

/* Prev / next arrows in banner dot bar */
.banner-arrow {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.25);
  background: rgba(5,5,8,0.55);
  backdrop-filter: blur(8px);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, transform 0.15s;
  flex-shrink: 0;
}
.banner-arrow:hover {
  background: rgba(41,188,234,0.45);
  border-color: var(--electric, #29bcea);
  transform: scale(1.08);
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

/* ── Top-level tab bar (Phim | Rạp Chiếu) ─────────────────── */
.main-tabs-bar {
  display: flex;
  justify-content: center;
  padding: 0 40px;
  border-bottom: 2px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--void, #050508);
  position: sticky;
  top: 64px;   /* sits just below the sticky nav (~64px tall) */
  z-index: 50;
}

.main-tabs {
  display: flex;
  gap: 0;
  max-width: 1400px;
  width: 100%;
}

.main-tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 16px 28px;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  color: var(--text-secondary, #94a3b8);
  font-size: 15px;
  font-weight: 700;
  font-family: var(--font-ui, 'Inter', sans-serif);
  cursor: pointer;
  margin-bottom: -2px;   /* overlap the bar's border-bottom */
  transition: color 0.2s, border-color 0.2s;
  min-height: 52px;
  white-space: nowrap;
}

.main-tab:hover { color: var(--text-primary, #f1f5f9); }

.main-tab.active {
  color: var(--electric, #29bcea);
  border-bottom-color: var(--electric, #29bcea);
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 40px;
}

.section-header .section-title { margin-bottom: 0; }

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

/* Search bar */
.search-bar {
  position: relative;
  display: flex;
  align-items: center;
  min-width: 220px;
  max-width: 340px;
  flex: 1 1 220px;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--text-ghost, rgba(241,245,249,0.45));
  pointer-events: none;
  flex-shrink: 0;
}

.search-input {
  width: 100%;
  padding: 10px 36px 10px 36px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-pill, 999px);
  color: var(--text-primary, #f1f5f9);
  font-size: 14px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  outline: none;
  transition: border-color 0.2s;
}

.search-input::placeholder { color: var(--text-ghost, rgba(241,245,249,0.45)); }
.search-input:focus { border-color: var(--electric, #29bcea); }
/* Remove browser default clear button on search inputs */
.search-input::-webkit-search-cancel-button { display: none; }

.search-clear {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  color: var(--text-ghost, rgba(241,245,249,0.45));
  cursor: pointer;
  font-size: 13px;
  padding: 4px;
  line-height: 1;
  min-width: 24px;
  min-height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: color 0.2s, background 0.2s;
}
.search-clear:hover { color: var(--text-primary, #f1f5f9); background: var(--glass-bg-heavy, rgba(255,255,255,0.08)); }

/* Screen-reader only */
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; border: 0; }

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

/* ── Carousel (replaces .movie-grid) ─────────────────────── */
.carousel-wrapper {
  position: relative;
}

/* Fade-out peek at the right edge so users see more content exists */
.carousel-wrapper::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  bottom: 12px; /* aligns with scrollbar padding */
  width: 60px;
  background: linear-gradient(to right, transparent, var(--page-bg, #050508));
  pointer-events: none;
  z-index: 2;
  border-radius: 0 var(--radius-md, 12px) var(--radius-md, 12px) 0;
}

.carousel {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 12px;
  /* hide scrollbar but keep scrollable */
  scrollbar-width: thin;
  scrollbar-color: var(--glass-border, rgba(255,255,255,0.1)) transparent;
}

.carousel::-webkit-scrollbar { height: 4px; }
.carousel::-webkit-scrollbar-track { background: transparent; }
.carousel::-webkit-scrollbar-thumb { background: var(--glass-border, rgba(255,255,255,0.12)); border-radius: 2px; }

/* Peek: last visible card is intentionally half-cut to signal more content */
.carousel--peek {
  /* Right padding creates the "cut-off" illusion — last card bleeds 40px out */
  padding-right: 40px;
}

.carousel-item {
  flex: 0 0 200px;
  scroll-snap-align: start;
}

/* ── Arrow buttons ────────────────────────────────────────── */
.carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(calc(-50% - 8px)); /* offset for scrollbar */
  z-index: 10;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.15));
  background: var(--surface-1, #0f0f17);
  color: var(--text-primary, #f1f5f9);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--shadow-md, 0 4px 16px rgba(0,0,0,0.55));
  opacity: 0;
  transition: opacity 0.2s, background 0.2s, border-color 0.2s;
}

/* Always visible on mobile, hover-reveal on desktop */
.carousel-wrapper:hover .carousel-arrow,
.carousel-arrow:focus-visible {
  opacity: 1;
}

.carousel-arrow:hover {
  background: var(--electric, #29bcea);
  border-color: var(--electric, #29bcea);
  color: #fff;
}

.carousel-arrow--left  { left:  -20px; }
.carousel-arrow--right { right: -20px; }

/* Featured rank badge */
.featured-rank {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 5;
  width: 28px;
  height: 28px;
  background: var(--electric, #29bcea);
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-ui, 'Inter', sans-serif);
  box-shadow: 0 2px 8px rgba(41,188,234,0.45);
}

.movie-card {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-radius: var(--radius-md, 12px);
  overflow: hidden;
  cursor: pointer;
  position: relative;
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

/* ── Cinema listing ──────────────────────────────────────── */
.section--cinemas { background: var(--surface-1, #0f0f17); border-radius: var(--radius-lg, 20px); max-width: 100%; padding: 60px 40px; }

.cinema-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.cinema-card {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 12px);
  overflow: hidden;
  transition: box-shadow 0.25s;
}

.cinema-card--clickable {
  cursor: pointer;
}
.cinema-card--clickable:focus-visible {
  outline: 2px solid rgba(255,255,255,0.5);
  outline-offset: 2px;
}

.cinema-card:hover { box-shadow: var(--shadow-md, 0 4px 16px rgba(0,0,0,0.45)); }

.cinema-img-wrap {
  width: 100%;
  aspect-ratio: 16/9;
  overflow: hidden;
  background: var(--surface-2, #14141f);
}

.cinema-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }
.cinema-card:hover .cinema-img { transform: scale(1.04); }

.cinema-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-ghost, rgba(241,245,249,0.25));
}

.cinema-info { padding: 16px; }

.cinema-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary, #f1f5f9);
  margin-bottom: 6px;
  line-height: 1.3;
}

.cinema-city {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--electric, #29bcea);
  font-weight: 600;
  margin-bottom: 4px;
}

.cinema-address {
  font-size: 12px;
  color: var(--text-secondary, #94a3b8);
  line-height: 1.5;
  margin-bottom: 10px;
}

.cinema-map-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  color: var(--electric, #29bcea);
  text-decoration: none;
  padding: 5px 10px;
  border: 1px solid var(--electric, #29bcea);
  border-radius: var(--radius-pill, 999px);
  transition: background 0.2s, color 0.2s;
}

.cinema-map-link:hover { background: var(--electric, #29bcea); color: #fff; }

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
  .hero-stage { min-height: 480px; }
  .hero-content { left: 5%; right: 5%; max-width: 100%; }
  .main-tabs-bar { padding: 0 16px; top: 56px; }
  .main-tab { padding: 12px 16px; font-size: 14px; }
  .section { padding: 40px 20px; }
  .section--cinemas { padding: 40px 20px; }
  .section-header { flex-direction: column; align-items: flex-start; }
  .search-bar { max-width: 100%; min-width: 0; width: 100%; }
  .carousel-item { flex: 0 0 160px; }
  /* Always show arrows on touch devices */
  .carousel-arrow { opacity: 1; }
  .carousel-arrow--left  { left:  -12px; }
  .carousel-arrow--right { right: -12px; }
  .cinema-grid { grid-template-columns: 1fr; }
  .promo { margin: 40px 20px; }
}

@media (max-width: 480px) {
  .carousel-item { flex: 0 0 140px; }
  .section-title { font-size: 24px; }
}/* ── Promotions section ─────────────────────────────────────── */
.section--promos { max-width: 1400px; margin: 0 auto; padding: 60px 40px; }

.promo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.promo-card {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 12px);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: box-shadow 0.25s;
}

.promo-card:hover { box-shadow: var(--shadow-md, 0 4px 16px rgba(0,0,0,0.45)); }

.promo-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.promo-card__name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary, #f1f5f9);
  line-height: 1.3;
  margin: 0;
}

.promo-card__badge {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: var(--radius-pill, 999px);
  font-size: 13px;
  font-weight: 800;
  font-family: var(--font-ui, 'Inter', sans-serif);
  white-space: nowrap;
}

.badge--percent {
  background: rgba(41,188,234,0.15);
  color: var(--electric, #29bcea);
  border: 1px solid rgba(41,188,234,0.3);
}

.badge--fixed {
  background: rgba(201,168,76,0.15);
  color: var(--gold, #C9A84C);
  border: 1px solid rgba(201,168,76,0.3);
}

.promo-card__code {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  font-family: monospace;
  color: var(--electric, #29bcea);
  background: rgba(41,188,234,0.07);
  border: 1px dashed rgba(41,188,234,0.3);
  border-radius: var(--radius-sm, 6px);
  padding: 5px 10px;
  width: fit-content;
  margin: 0;
}

.promo-card__desc {
  font-size: 13px;
  color: var(--text-secondary, #94a3b8);
  line-height: 1.5;
  margin: 0;
}

.promo-card__dates,
.promo-card__min {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary, #94a3b8);
  margin: 0;
}

.promo-card__movies { margin-top: 4px; }

.promo-card__movies-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-ghost, rgba(241,245,249,0.45));
  margin: 0 0 8px;
}

.promo-movies-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.promo-movie-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 10px 5px 5px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-pill, 999px);
  cursor: pointer;
  color: var(--text-primary, #f1f5f9);
  font-size: 13px;
  font-weight: 600;
  font-family: var(--font-ui, 'Inter', sans-serif);
  transition: background 0.2s, border-color 0.2s, color 0.2s;
  text-align: left;
  max-width: 220px;
}

.promo-movie-chip:hover {
  border-color: var(--electric, #29bcea);
  color: var(--electric, #29bcea);
  background: rgba(41,188,234,0.07);
}

.promo-movie-chip__poster {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.promo-movie-chip__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* System-wide promo CTA */
.promo-card__cta { margin-top: 4px; }

.promo-card__btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 16px;
  background: var(--electric, #29bcea);
  color: #fff;
  border-radius: var(--radius-pill, 999px);
  font-size: 13px;
  font-weight: 700;
  font-family: var(--font-ui, 'Inter', sans-serif);
  text-decoration: none;
  transition: background 0.2s, transform 0.15s;
}

.promo-card__btn:hover {
  background: var(--electric-hover, #1a9fbd);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .section--promos { padding: 40px 20px; }
  .promo-grid { grid-template-columns: 1fr; }
}
/* ── Giới Thiệu (About) panel ───────────────────────────────── */
.gioi-thieu-stage {
  position: relative;
  min-height: 70vh;
  background: var(--void, #050508) center/cover no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gioi-thieu-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(5,5,8,0.55) 0%,
    rgba(5,5,8,0.75) 100%
  );
  z-index: 1;
}

.gioi-thieu-body {
  position: relative;
  z-index: 2;
  max-width: 760px;
  width: 100%;
  padding: 80px 40px;
  text-align: center;
}

.gioi-thieu-title {
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  font-size: clamp(28px, 5vw, 52px);
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--text-primary, #f1f5f9);
  margin: 0 0 28px;
  text-transform: uppercase;
}

.gioi-thieu-para {
  font-size: clamp(14px, 1.6vw, 17px);
  line-height: 1.85;
  color: rgba(241,245,249,0.82);
  margin: 0 0 18px;
  font-family: var(--font-ui, 'Inter', sans-serif);
}
.gioi-thieu-para:last-child { margin-bottom: 0; }

@media (max-width: 640px) {
  .gioi-thieu-body { padding: 60px 20px; }
}

</style>

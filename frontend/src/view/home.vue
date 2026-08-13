<template>
  <div class="home">
    <!-- NAV -->
    <nav class="nav">
      <div class="nav-island">
        <router-link to="/" class="logo">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z"/></svg>
          <span class="logo-text">Poly<span class="logo-accent">Cinema</span></span>
        </router-link>

        <!-- Desktop tabs -->
        <nav class="nav-tabs" aria-label="Chọn nội dung" role="tablist">
          <button
            :class="['main-tab', { active: mainTab === 'phim' }]"
            @click="mainTab = 'phim'"
            :aria-selected="mainTab === 'phim'"
            role="tab"
            aria-controls="main-panel-phim"
            id="main-tab-phim"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
            <span class="main-tab-label">{{ t('tabPhim') }}</span>
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
            <span class="main-tab-label">{{ t('tabRap') }}</span>
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
            <span class="main-tab-label">{{ t('tabKhuyenMai') }}</span>
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
            <span class="main-tab-label">{{ t('tabGioiThieu') }}</span>
          </button>
        </nav>

        <!-- Desktop nav-actions -->
        <div class="nav-actions">
          <ThemeToggle />
          <!-- Global search trigger -->
          <button class="icon-btn gs-trigger" @click="openGlobalSearch" :aria-label="t('searchPlaceholder')" title="Tìm kiếm (/)">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          </button>
          <button class="icon-btn lang-toggle" @click="toggleLang" :aria-label="lang === 'vi' ? 'Switch to English' : 'Chuyển sang Tiếng Việt'" :title="lang === 'vi' ? 'Switch to English' : 'Chuyển sang Tiếng Việt'">
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
                <span class="chevron" aria-hidden="true">{{ showDropdown ? '▲' : '▼' }}</span>
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
            <button class="icon-btn drawer-theme-btn" @click="toggleLang">🌐 {{ lang === 'vi' ? 'Switch to English' : 'Chuyển sang Tiếng Việt' }}</button>
          </div>
          <div class="drawer-tabs">
            <button
              :class="['drawer-item', { active: mainTab === 'phim' }]"
              @click="mainTab = 'phim'; showMobileMenu = false"
              role="tab"
              aria-selected="mainTab === 'phim'"
              aria-controls="main-panel-phim"
              id="drawer-tab-phim"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
              {{ t('tabPhim') }}
            </button>
            <button
              :class="['drawer-item', { active: mainTab === 'rap_chieu' }]"
              @click="mainTab = 'rap_chieu'; showMobileMenu = false"
              role="tab"
              aria-selected="mainTab === 'rap_chieu'"
              aria-controls="main-panel-rap"
              id="drawer-tab-rap"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              {{ t('tabRap') }}
            </button>
            <button
              :class="['drawer-item', { active: mainTab === 'khuyen_mai' }]"
              @click="mainTab = 'khuyen_mai'; showMobileMenu = false"
              role="tab"
              aria-selected="mainTab === 'khuyen_mai'"
              aria-controls="main-panel-khuyen-mai"
              id="drawer-tab-khuyen-mai"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
              {{ t('tabKhuyenMai') }}
            </button>
            <button
              :class="['drawer-item', { active: mainTab === 'gioi_thieu' }]"
              @click="mainTab = 'gioi_thieu'; showMobileMenu = false"
              role="tab"
              aria-selected="mainTab === 'gioi_thieu'"
              aria-controls="main-panel-gioi-thieu"
              id="drawer-tab-gioi-thieu"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ t('tabGioiThieu') }}
            </button>
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
            <router-link to="/profile" class="drawer-item" @click="showMobileMenu=false">
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

    <!-- ── Global Search Overlay ───────────────────────────────────── -->
    <transition name="gs-fade">
      <div v-if="gsOpen" class="gs-overlay" role="dialog" aria-modal="true" aria-label="Tìm kiếm toàn cục" @click.self="closeGlobalSearch">
        <div class="gs-panel">
          <!-- Input row -->
          <div class="gs-input-row">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="gs-icon" aria-hidden="true"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input
              ref="gsInputRef"
              v-model="gsQuery"
              type="search"
              class="gs-input"
              placeholder="Tìm phim, rạp chiếu, khuyến mãi, giới thiệu..."
              autocomplete="off"
              @input="onGsInput"
              @keydown.escape="closeGlobalSearch"
              @keydown.down.prevent="gsMoveDown"
              @keydown.up.prevent="gsMoveUp"
              @keydown.enter.prevent="gsSelectActive"
            />
            <button class="gs-close" @click="closeGlobalSearch" aria-label="Đóng tìm kiếm">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              <kbd>Esc</kbd>
            </button>
          </div>

          <!-- Results -->
          <div class="gs-body" role="listbox">
            <div v-if="gsLoading" class="gs-state">
              <div class="gs-spinner"></div>
              <span>Đang tìm...</span>
            </div>
            <div v-else-if="gsQuery.trim() && gsAllResults.length === 0" class="gs-state gs-empty">
              Không tìm thấy kết quả cho "<strong>{{ gsQuery }}</strong>"
            </div>
            <template v-else-if="gsAllResults.length > 0">
              <!-- Movies-only toggle -->
              <div class="gs-filter-row" v-if="gsMovieResults.length > 0">
                <button :class="['gs-filter-btn', { active: gsOnlyMovies }]" @click="gsOnlyMovies = !gsOnlyMovies">
                  🎬 Chỉ xem phim ({{ gsMovieResults.length }})
                </button>
              </div>

              <div
                v-for="(item, idx) in gsDisplayResults"
                :key="item._key"
                :class="['gs-item', { 'gs-item--active': idx === gsActiveIdx }]"
                role="option"
                :aria-selected="idx === gsActiveIdx"
                @click="gsSelectItem(item)"
                @mouseenter="gsActiveIdx = idx"
              >
                <span class="gs-item__section" :class="`gs-section--${item._section}`">{{ item._label }}</span>
                <span class="gs-item__title">{{ item._title }}</span>
                <span v-if="item._sub" class="gs-item__sub">{{ item._sub }}</span>
                <svg v-if="item._section === 'phim'" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="gs-item__go" aria-hidden="true"><polyline points="9 18 15 12 9 6"/></svg>
              </div>
            </template>
            <div v-else-if="!gsQuery.trim()" class="gs-state gs-hint">
              Gõ để tìm phim, rạp chiếu, khuyến mãi...
              <span class="gs-kbd-hint"><kbd>/</kbd> để mở · <kbd>Esc</kbd> để đóng</span>
            </div>
          </div>
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
        <p v-if="bannerDateRange" class="hero-date-range" aria-label="Thời gian diễn ra">{{ bannerDateRange }}</p>
        <p class="hero-desc">{{ currentBanner.moTa }}</p>
        <button
          v-if="bannerShowCta"
          class="btn-bib btn-hero"
          @click="onBannerCta"
        >{{ bannerCtaLabel }}</button>
      </div>
      <div class="hero-content" v-else>
        <p class="hero-loading">{{ t('loading') }}...</p>
      </div>
      <button
        v-if="movieStore.banners.length > 1"
        class="banner-arrow banner-arrow--left"
        @click.stop="prevBanner"
        :aria-label="'Banner trước'"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <button
        v-if="movieStore.banners.length > 1"
        class="banner-arrow banner-arrow--right"
        @click.stop="nextBanner"
        :aria-label="'Banner tiếp theo'"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
      <div v-if="movieStore.banners.length > 1" class="banner-dots">
        <button v-for="(_, i) in movieStore.banners" :key="i" :class="['dot', { active: bannerIndex === i }]" @click.stop="bannerIndex = i; resetBannerTimer()"></button>
      </div>
    </div>

    <!-- PANEL: PHIM -->
    <div v-show="mainTab === 'phim'" id="main-panel-phim" role="tabpanel" aria-labelledby="main-tab-phim">

    <!-- QUICK BOOKING -->
    <section class="quickbook" aria-labelledby="quickbook-title">
      <div class="quickbook__inner">
        <h2 class="quickbook__title" id="quickbook-title">Đặt vé nhanh</h2>

        <div class="quickbook__row">
          <div class="quickbook__field">
            <label class="quickbook__label" for="qb-cinema">Chọn rạp</label>
            <select id="qb-cinema" v-model="qbCinemaId" class="quickbook__select" @change="onQbCinemaChange">
              <option value="">-- Chọn rạp --</option>
              <option v-for="c in movieStore.cinemas" :key="c.id" :value="c.id">{{ c.tenRap }}</option>
            </select>
          </div>
          <div class="quickbook__field">
            <label class="quickbook__label" for="qb-movie">Chọn phim</label>
            <select id="qb-movie" v-model="qbMovieId" class="quickbook__select" :disabled="!qbCinemaId || qbLoading || qbMovies.length === 0" @change="onQbMovieChange">
              <option value="">-- Chọn phim --</option>
              <option v-for="m in qbMovies" :key="m.phimId" :value="m.phimId">{{ m.tenPhim }}</option>
            </select>
          </div>
        </div>

        <div v-if="qbError" class="quickbook__error" role="alert">{{ qbError }}</div>

        <div v-if="qbDays.length" class="quickbook__days" role="tablist" aria-label="Chọn ngày có suất chiếu">
          <DayChip
            v-for="d in qbDays"
            :key="d.iso"
            :num="d.num"
            :mo="d.mo"
            :dow="d.dow"
            :active="qbDate === d.iso"
            @select="qbDate = d.iso; qbSelected = null"
          />
        </div>

        <template v-if="qbShowtimesForDate.length">
          <div v-for="g in qbShowtimesByBuoi" :key="g.label" class="qb-buoi">
            <div v-if="g.list.length" class="qb-buoi__label">{{ g.label }}</div>
            <div class="quickbook__shows">
              <button
                v-for="s in g.list"
                :key="s.lichChieuId"
                :class="['qb-show', { 'qb-show--active': qbSelected?.lichChieuId === s.lichChieuId }]"
                @click="qbSelected = s"
              >
                <span class="qb-show__time">{{ fmtQbTime(s.thoiGianBatDau) }}</span>
                <span class="qb-show__room">{{ s.tenPhong }}</span>
                <span class="qb-show__format">{{ s.tenDinhDang || s.loaiPhong }}</span>
                <span class="qb-show__price">{{ fmtQbPrice(s.giaCoBan) }}</span>
              </button>
            </div>
          </div>
        </template>
        <div v-else-if="qbMovieId && !qbLoading" class="quickbook__empty">Không có suất chiếu</div>

        <div class="quickbook__bar">
          <span v-if="qbSelected" class="quickbook__hint">{{ qbSelected.tenPhong }} · {{ fmtQbTime(qbSelected.thoiGianBatDau) }}</span>
          <span v-else class="quickbook__hint">Vui lòng chọn suất chiếu</span>
          <button class="qb-book-btn" :disabled="!qbSelected" @click="qbBook">Đặt vé</button>
        </div>
      </div>
    </section>

    <!-- MOVIES SECTION -->
    <section class="section" aria-labelledby="movies-title">
      <div class="section-header">
        <h2 class="section-title" id="movies-title">{{ t('movies') }} <span>{{ t('schedule') }}</span></h2>
      </div>

      <!-- Tab bar always visible, regardless of search state -->
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

      <!-- Default grid (no search) -->
      <template v-if="!searchQuery">
        <div v-if="isLoading" class="loading" role="status">{{ t('loading') }}...</div>
        <div v-else-if="isError" class="error" role="alert">{{ isError }}</div>
        <div v-else-if="displayMovies.length === 0" class="error">{{ t('noMovies') }}</div>
        <div v-else class="carousel-wrapper">
          <button
            class="carousel-arrow carousel-arrow--left"
            @click="scrollCarousel(-1)"
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
                <div class="format-badges">
                  <span v-for="f in movie.formats" :key="f.id" class="format-badge">{{ f.tenDinhDang }}</span>
                </div>
                <div class="movie-overlay">
                  <div class="overlay-inner">
                    <h3 class="overlay-title">{{ movie.title }}</h3>
                    <p class="overlay-meta">
                      <span v-if="movie.duration" class="ov-chip">{{ movie.duration }} phút</span>
                      <span v-if="movie.genre" class="ov-genre">{{ movie.genre }}</span>
                    </p>
                    <p v-if="movie.rating" class="overlay-rating">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="none" aria-hidden="true"><polygon fill="currentColor" points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                      {{ Number(movie.rating).toFixed(1) }}
                    </p>
                    <p v-if="movie.language" class="overlay-lang">🌐 {{ movie.language }}</p>
                    <div class="overlay-actions">
                      <button class="ov-btn ov-btn--detail" @click.stop="goToMovie(movie.id)">{{ t('viewDetail') }}</button>
                      <button v-if="isBookable(movie)" class="ov-btn ov-btn--book" @click.stop="goToMovie(movie.id)">{{ t('bookNow') }}</button>
                    </div>
                  </div>
                </div>
              </div>
              <div class="movie-info">
                <span class="age-badge" :class="'age-badge--' + ageClass(movie.ageRating)">{{ movie.ageRating }}</span>
                <h3 class="movie-info-title">{{ movie.title }}</h3>
              </div>
            </div>
          </div>
          <button
            class="carousel-arrow carousel-arrow--right"
            @click="scrollCarousel(1)"
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
              <div class="format-badges">
                <span v-for="f in movie.formats" :key="f.id" class="format-badge">{{ f.tenDinhDang }}</span>
              </div>
              <div class="movie-overlay">
                <div class="overlay-inner">
                  <h3 class="overlay-title">{{ movie.title }}</h3>
                  <p class="overlay-meta">
                    <span v-if="movie.duration" class="ov-chip">{{ movie.duration }} phút</span>
                    <span v-if="movie.genre" class="ov-genre">{{ movie.genre }}</span>
                  </p>
                  <p v-if="movie.rating" class="overlay-rating">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="none" aria-hidden="true"><polygon fill="currentColor" points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                    {{ Number(movie.rating).toFixed(1) }}
                  </p>
                  <p v-if="movie.language" class="overlay-lang">🌐 {{ movie.language }}</p>
                  <div class="overlay-actions">
                    <button class="ov-btn ov-btn--detail" @click.stop="goToMovie(movie.id)">{{ t('viewDetail') }}</button>
                    <button v-if="isBookable(movie)" class="ov-btn ov-btn--book" @click.stop="goToMovie(movie.id)">{{ t('bookNow') }}</button>
                  </div>
                </div>
              </div>
            </div>
            <div class="movie-info">
              <span class="age-badge" :class="'age-badge--' + ageClass(movie.ageRating)">{{ movie.ageRating }}</span>
              <h3 class="movie-info-title">{{ movie.title }}</h3>
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
          class="cinema-card"
          role="link"
          tabindex="0"
          @click="router.push('/rap/' + cinema.id)"
          @keypress.enter="router.push('/rap/' + cinema.id)"
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
            <p class="cinema-schedule-hint">📅 Xem lịch chiếu</p>
            <div class="cinema-card-actions">
              <a
                v-if="cinema.latitude != null && cinema.longitude != null"
                :href="`https://maps.google.com/?q=${cinema.latitude},${cinema.longitude}`"
                class="cinema-map-link"
                target="_blank"
                rel="noopener noreferrer"
                @click.stop
              >
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polygon points="3 11 22 2 13 21 11 13 3 11"/></svg>
                {{ t('viewMap') }}
              </a>
              <button
                v-if="cinema.banDoUrl"
                class="btn-directions-card"
                @click.stop="handleDirectionsCard(cinema)"
              >
                🗺️ Đường đi
              </button>
            </div>
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
            <!-- Banner: poster phim + badge giảm giá + tên phim -->
            <div class="promo-card__banner">
              <img
                v-if="bannerPoster(promo)"
                :src="bannerPoster(promo)"
                :alt="promo.tenKhuyenMai"
                class="promo-card__banner-img"
                loading="lazy"
                @error="(e) => e.target.style.display='none'"
              />
              <div v-else class="promo-card__banner-fallback">
                <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z"/></svg>
              </div>
              <div class="promo-card__banner-overlay"></div>
              <span class="promo-card__badge" :class="promo.loaiGiamGia === 'percent' ? 'badge--percent' : 'badge--fixed'">
                <template v-if="promo.loaiGiamGia === 'percent'">-{{ promo.giaTriGiam }}%</template>
                <template v-else>-{{ formatCurrency(promo.giaTriGiam) }}₫</template>
              </span>
              <div class="promo-card__banner-movies">
                <template v-if="promo.phims && promo.phims.length > 0">
                  <span v-for="(phim, i) in promo.phims.slice(0, 2)" :key="phim.id" class="promo-card__banner-movie">
                    {{ phim.tenPhim }}<template v-if="i < Math.min(promo.phims.length, 2) - 1">, </template>
                  </span>
                  <span v-if="promo.phims.length > 2" class="promo-card__banner-movie">+{{ promo.phims.length - 2 }} phim khác</span>
                </template>
                <span v-else class="promo-card__banner-movie">{{ t('promoSystemWide') }}</span>
              </div>
            </div>

            <!-- Body -->
            <div class="promo-card__body">
              <h3 class="promo-card__name">{{ promo.tenKhuyenMai }}</h3>

              <!-- Mã + nút copy -->
              <div class="promo-card__code-row">
                <span class="promo-card__code">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                  {{ promo.maKhuyenMai }}
                </span>
                <button
                  class="promo-card__copy"
                  :class="{ copied: copiedCode === promo.maKhuyenMai }"
                  @click="copyPromo(promo)"
                >
                  <svg v-if="copiedCode !== promo.maKhuyenMai" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
                  <svg v-else width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 6L9 17l-5-5"/></svg>
                  {{ copiedCode === promo.maKhuyenMai ? 'Đã sao chép!' : 'Copy' }}
                </button>
              </div>

              <p v-if="promo.moTa" class="promo-card__desc">{{ promo.moTa }}</p>

              <div class="promo-card__meta">
                <span v-if="promo.donHangToiThieu" class="promo-card__meta-item">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
                  {{ t('promoMin') }}: {{ formatCurrency(promo.donHangToiThieu) }}₫
                </span>
                <span class="promo-card__meta-item">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  {{ formatDate(promo.ngayBatDau) }} – {{ formatDate(promo.ngayKetThuc) }}
                </span>
              </div>

              <!-- Phim áp dụng / CTA toàn hệ thống -->
              <div v-if="promo.phims && promo.phims.length > 0" class="promo-card__movies">
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
          <h2 class="gioi-thieu-title" id="gioi-thieu-title">{{ gioiThieu.tieuDe || t('tabGioiThieu') }}</h2>
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
      <!-- Hero CTA -->
      <div class="footer-hero">
        <div class="footer-hero__copy">
          <p class="footer-hero__tag">BE HAPPY, BE A STAR</p>
          <p class="footer-hero__sub">{{ t('footerTagline') }}</p>
        </div>
        <div class="footer-hero__actions">
          <button class="footer-btn footer-btn--primary" @click="goFooterMovies">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
            {{ t('footerBuyTickets') }}
          </button>
          <button class="footer-btn footer-btn--ghost" @click="goFooterMovies">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M8.5 2h7l-1 7h-5l-1-7z"/><path d="M5 3h14M7 9h10l1.5 4.5-1 4H6.5l-1-4L7 9z"/><path d="M9 17.5V22M15 17.5V22"/></svg>
            {{ t('footerBuyPopcorn') }}
          </button>
        </div>
      </div>

      <!-- Link columns -->
      <div class="footer-grid">
        <div class="footer-col">
          <h4 class="footer-col__title">{{ t('footerMovies') }}</h4>
          <button class="footer-link" @click="goFooterMoviesTab('dang_chieu')">{{ t('nowShowing') }}</button>
          <button class="footer-link" @click="goFooterMoviesTab('sap_chieu')">{{ t('comingSoon') }}</button>
          <button class="footer-link" @click="goFooterMovies">{{ t('bookNow') }}</button>
        </div>

        <div class="footer-col">
          <h4 class="footer-col__title">{{ t('footerAccount') }}</h4>
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/auth" class="footer-link" @click="closeFooterMenus">{{ t('login') }}</router-link>
            <router-link to="/auth?mode=register" class="footer-link" @click="closeFooterMenus">{{ t('register') }}</router-link>
          </template>
          <template v-else>
            <router-link to="/profile" class="footer-link" @click="closeFooterMenus">{{ t('profile') }}</router-link>
            <router-link v-if="authStore.userRole !== 'STAFF'" to="/my-tickets" class="footer-link" @click="closeFooterMenus">{{ t('tickets') }}</router-link>
            <router-link v-if="authStore.userRole !== 'STAFF'" to="/transaction-history" class="footer-link" @click="closeFooterMenus">Lịch sử GD</router-link>
          </template>
        </div>

        <div class="footer-col">
          <h4 class="footer-col__title">{{ t('footerExplore') }}</h4>
          <button class="footer-link" @click="goFooterTab('rap_chieu')">{{ t('tabRap') }}</button>
          <button class="footer-link" @click="goFooterTab('khuyen_mai')">{{ t('tabKhuyenMai') }}</button>
          <button class="footer-link" @click="goFooterTab('gioi_thieu')">{{ t('tabGioiThieu') }}</button>
        </div>

        <div class="footer-col">
          <h4 class="footer-col__title">{{ t('footerTheaters') }}</h4>
          <template v-if="movieStore.cinemas.length">
            <button
              v-for="cinema in movieStore.cinemas.slice(0, 5)"
              :key="cinema.id"
              class="footer-link footer-link--truncate"
              :title="cinema.tenRap"
              @click="router.push('/rap/' + cinema.id)"
            >{{ cinema.tenRap }}</button>
          </template>
          <button class="footer-link" @click="goFooterTab('rap_chieu')">{{ t('footerAllTheaters') }}</button>
        </div>
      </div>

      <!-- Language + legal -->
      <div class="footer-bottom">
        <div class="footer-lang">
          <button :class="['footer-lang__btn', { active: lang === 'vi' }]" @click="switchLang('vi')">VI</button>
          <span class="footer-lang__sep">|</span>
          <button :class="['footer-lang__btn', { active: lang === 'en' }]" @click="switchLang('en')">EN</button>
        </div>
        <p class="footer-copy">&copy; 2026 PolyCinema. {{ t('allRights') }}</p>
      </div>
    </footer>
  </div>

  <!-- Confirm modal (teleported to body, shared with cinema cards) -->
  <ConfirmModal />
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useMovieStore } from '@/stores/movieStore'
import { useBookingStore } from '@/stores/bookingStore'
import ThemeToggle from '@/components/ThemeToggle.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import DayChip from '@/components/DayChip.vue'
import { useConfirmModal } from '@/composables/useConfirmModal'
import { fmtTime12 } from '@/utils/homeHelpers'

const router = useRouter()
const authStore = useAuthStore()
const movieStore = useMovieStore()
const bookingStore = useBookingStore()
const { open: openConfirmModal } = useConfirmModal()

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

// ── Footer helpers ─────────────────────────────────────────────
const closeFooterMenus = () => {
  showDropdown.value = false
  showMobileMenu.value = false
}

function goFooterMovies() {
  closeFooterMenus()
  mainTab.value = 'phim'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function goFooterMoviesTab(tab) {
  closeFooterMenus()
  mainTab.value = 'phim'
  activeTab.value = tab
  nextTick(() => {
    const el = document.getElementById('movies-title')
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

function goFooterTab(tab) {
  closeFooterMenus()
  mainTab.value = tab
  const targetId = {
    rap_chieu: 'cinemas-title',
    khuyen_mai: 'promos-title',
    gioi_thieu: 'gioi-thieu-title'
  }[tab]
  const panelId = {
    rap_chieu: 'main-panel-rap',
    khuyen_mai: 'main-panel-khuyen-mai',
    gioi_thieu: 'main-panel-gioi-thieu'
  }[tab]
  nextTick(() => {
    const el = (targetId && document.getElementById(targetId)) || (panelId && document.getElementById(panelId))
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

function switchLang(next) {
  lang.value = next
  localStorage.setItem('poly_lang', next)
}

const translations = {
  vi: { login: 'Đăng nhập', register: 'Đăng ký', profile: 'Hồ sơ', tickets: 'Vé của tôi', logout: 'Đăng xuất', movies: 'Lịch Chiếu', schedule: 'Phim', now: 'Nổi Bật', nowShowing: 'Đang chiếu', comingSoon: 'Sắp chiếu', bookNow: 'Đặt vé', bookNowCta: 'Đặt vé ngay', viewDetail: 'Xem chi tiết', loading: 'Đang tải', promoTitle: 'Ưu Đãi Thứ 3', promoDesc: 'Giảm 30% vé xem phim vào thứ 3', joinNow: 'Tham Gia', allRights: 'All rights reserved.', featured: 'Phim', topRated: 'Đánh Giá Cao', noMovies: 'Không có phim', searchPlaceholder: 'Tìm phim...', clearSearch: 'Xóa tìm kiếm', noResults: 'Không tìm thấy phim phù hợp', searchResults: 'Kết quả tìm kiếm', movieTabs: 'Danh mục phim', cinemaSystem: 'Hệ Thống', cinemaNetwork: 'Rạp Chiếu', noCinemas: 'Chưa có thông tin rạp', viewMap: 'Xem bản đồ', scrollLeft: 'Cuộn trái', scrollRight: 'Cuộn phải', tabPhim: 'Phim', tabRap: 'Rạp Chiếu', tabKhuyenMai: 'Khuyến Mãi', promoActive: 'Đang Áp Dụng', noPromos: 'Hiện không có khuyến mãi', promoAppliesTo: 'Áp dụng cho phim', promoMin: 'Đơn hàng tối thiểu', promoSystemWide: 'Áp dụng toàn hệ thống', chooseMovie: 'Chọn phim', tabGioiThieu: 'Giới Thiệu', footerTagline: 'Đặt vé nhanh, combo ngon, giải trí trọn vẹn', footerBuyTickets: 'Mua Vé', footerBuyPopcorn: 'Combo Bắp Nước', footerMovies: 'Phim', footerAccount: 'Tài Khoản', footerExplore: 'Khám Phá', footerTheaters: 'Hệ Thống Rạp', footerAllTheaters: 'Tất cả hệ thống rạp' },
  en: { login: 'Login', register: 'Register', profile: 'Profile', tickets: 'My Tickets', logout: 'Logout', movies: 'Movie', schedule: 'Schedule', now: 'Featured', nowShowing: 'Now Showing', comingSoon: 'Coming Soon', bookNow: 'Book Now', bookNowCta: 'Book Now', viewDetail: 'View Details', loading: 'Loading', promoTitle: 'Tuesday Offer', promoDesc: '30% off all tickets on Tuesday', joinNow: 'Join Now', allRights: 'All rights reserved.', featured: 'Top', topRated: 'Rated Movies', noMovies: 'No movies available', searchPlaceholder: 'Search movies...', clearSearch: 'Clear search', noResults: 'No movies found', searchResults: 'Search results', movieTabs: 'Movie categories', cinemaSystem: 'Cinema', cinemaNetwork: 'Network', noCinemas: 'No cinemas available', viewMap: 'View map', scrollLeft: 'Scroll left', scrollRight: 'Scroll right', tabPhim: 'Movies', tabRap: 'Cinemas', tabKhuyenMai: 'Promotions', promoActive: 'Active', noPromos: 'No active promotions', promoAppliesTo: 'Applies to', promoMin: 'Min. order', promoSystemWide: 'System-wide', chooseMovie: 'Browse movies', tabGioiThieu: 'About Us', footerTagline: 'Book fast, tasty combos, complete entertainment', footerBuyTickets: 'Buy Tickets', footerBuyPopcorn: 'Popcorn Combos', footerMovies: 'Movies', footerAccount: 'Account', footerExplore: 'Explore', footerTheaters: 'Cinema System', footerAllTheaters: 'All cinemas' }
}

const t = (key) => translations[lang.value][key] || key
const displayMovies = computed(() => activeTab.value === 'dang_chieu' ? movieStore.phimDangChieu : movieStore.phimSapChieu)
const isLoading = computed(() => activeTab.value === 'dang_chieu' ? movieStore.loading.dangChieu : movieStore.loading.sapChieu)
const isError = computed(() => activeTab.value === 'dang_chieu' ? movieStore.error.dangChieu : movieStore.error.sapChieu)
const currentBanner = computed(() => movieStore.banners[bannerIndex.value])

// CTA button only for movie banners; combo & promotion banners show no button
const bannerShowCta = computed(() => currentBanner.value?.loaiBanner === 'Phim')
const bannerCtaLabel = computed(() => t('bookNowCta'))

/**
 * Formatted date-range string for the current hero banner.
 * Uses the existing formatDate() helper (returns 'dd/MM/yyyy').
 * Returns '' when no dates are set so the element stays hidden.
 */
const bannerDateRange = computed(() => {
  const b = currentBanner.value
  if (!b) return ''
  const start = b.ngayBatDau  || null
  const end   = b.ngayKetThuc || null
  if (start && end)   return `Diễn ra từ ${formatDate(start)} - ${formatDate(end)}`
  if (start)          return `Bắt đầu từ ${formatDate(start)}`
  if (end)            return `Đến hết ${formatDate(end)}`
  return ''
})

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

// Card age-rating badge → color class
function ageClass(a) {
  const map = { P: 'p', K: 'k', T13: 't13', T16: 't16', T18: 't18' }
  return map[a] || 'na'
}
// "Đặt vé" only for movies whose tickets are on sale
function isBookable(m) {
  if (!m) return false
  return m.status === 'dang_chieu' || m.status === 'sap_chieu'
}

function handleDirectionsCard(cinema) {
  openConfirmModal({
    title: 'Mở đường đi?',
    description: `Bạn sẽ được chuyển đến Google Maps để xem đường đi đến ${cinema.tenRap}`,
    confirmLabel: 'Mở Google Maps',
    cancelLabel: 'Hủy',
    onConfirm: () => window.open(cinema.banDoUrl, '_blank', 'noopener,noreferrer'),
  })
}

/**
 * Navigate based on the current banner's typed target (loaiBanner).
 * - Phim → movie detail page /phim/:id
 * - Khac → no button / no navigation (plain banner)
 */
function onBannerClick() {
  const b = currentBanner.value
  if (!b) return
  if (b.loaiBanner === 'Phim' && b.phimId) {
    router.push('/phim/' + b.phimId)
  }
}

function onBannerCta() {
  onBannerClick()
}

// ── Global Search ─────────────────────────────────────────────
const gsOpen      = ref(false)
const gsQuery     = ref('')
const gsLoading   = ref(false)
const gsOnlyMovies = ref(false)
const gsActiveIdx = ref(0)
const gsInputRef  = ref(null)
const gsMovieResults  = ref([])   // from API
const gsCinemaResults = ref([])   // client-side from movieStore.cinemas
const gsPromoResults  = ref([])   // client-side from promos ref
const gsAboutResult   = ref(null) // static match
let   gsTimer = null

const gsAllResults = computed(() => {
  if (!gsQuery.value.trim()) return []
  const results = []
  const q = gsQuery.value.trim().toLowerCase()

  // Movies (from API, already filtered)
  gsMovieResults.value.forEach(m => {
    results.push({
      _key:     'phim-' + m.id,
      _section: 'phim',
      _label:   '🎬 Phim',
      _title:   m.tenPhim || m.title || '',
      _sub:     m.trangThai === 'dang_chieu' ? 'Đang chiếu' : m.trangThai === 'sap_chieu' ? 'Sắp chiếu' : '',
      _data:    m,
    })
  })

  // Cinemas (client-side)
  gsCinemaResults.value.forEach(c => {
    results.push({
      _key:     'rap-' + c.id,
      _section: 'rap',
      _label:   '🏠 Rạp Chiếu',
      _title:   c.tenRap || '',
      _sub:     c.diaChi || c.thanhPho || '',
      _data:    c,
    })
  })

  // Promos (client-side)
  gsPromoResults.value.forEach(p => {
    results.push({
      _key:     'km-' + p.id,
      _section: 'km',
      _label:   '🎁 Khuyến Mãi',
      _title:   p.tenKhuyenMai || '',
      _sub:     p.maKhuyenMai ? `Mã: ${p.maKhuyenMai}` : '',
      _data:    p,
    })
  })

  // About (static)
  if (gsAboutResult.value) {
    results.push({
      _key:     'about',
      _section: 'about',
      _label:   'ℹ️ Giới Thiệu',
      _title:   gsAboutResult.value,
      _sub:     '',
      _data:    null,
    })
  }

  return results
})

const gsDisplayResults = computed(() =>
  gsOnlyMovies.value
    ? gsAllResults.value.filter(r => r._section === 'phim')
    : gsAllResults.value
)

function openGlobalSearch() {
  gsOpen.value = true
  gsQuery.value = ''
  gsMovieResults.value = []
  gsCinemaResults.value = []
  gsPromoResults.value = []
  gsAboutResult.value = null
  gsOnlyMovies.value = false
  gsActiveIdx.value = 0
  nextTick(() => gsInputRef.value?.focus())
}

function closeGlobalSearch() {
  gsOpen.value = false
  gsQuery.value = ''
}

function onGsInput() {
  clearTimeout(gsTimer)
  gsActiveIdx.value = 0
  const q = gsQuery.value.trim()
  if (!q) {
    gsMovieResults.value = []
    gsCinemaResults.value = []
    gsPromoResults.value = []
    gsAboutResult.value = null
    return
  }
  gsLoading.value = true
  gsTimer = setTimeout(async () => {
    const ql = q.toLowerCase()
    try {
      // Movies: use existing API
      const [moviesRes] = await Promise.allSettled([
        movieStore.searchMovies(q)
      ])
      gsMovieResults.value = moviesRes.status === 'fulfilled' ? (moviesRes.value || []) : []

      // Cinemas: client-side filter from already-loaded store data
      gsCinemaResults.value = (movieStore.cinemas || []).filter(c =>
        (c.tenRap || '').toLowerCase().includes(ql) ||
        (c.diaChi || '').toLowerCase().includes(ql) ||
        (c.thanhPho || '').toLowerCase().includes(ql)
      )

      // Promos: client-side filter from local promos ref
      gsPromoResults.value = promos.value.filter(p =>
        (p.tenKhuyenMai || '').toLowerCase().includes(ql) ||
        (p.maKhuyenMai || '').toLowerCase().includes(ql) ||
        (p.moTa || '').toLowerCase().includes(ql)
      )

      // About: static keyword match
      const aboutKw = ['giới thiệu', 'about', 'gioi thieu', 'liên hệ', 'lien he', 'polycinema', 'hệ thống', 'he thong', 'thông tin']
      gsAboutResult.value = aboutKw.some(kw => ql.includes(kw) || kw.includes(ql))
        ? 'Trang Giới Thiệu PolyCinema' : null
    } catch {}
    gsLoading.value = false
  }, 300)
}

function gsMoveDown() {
  if (gsDisplayResults.value.length === 0) return
  gsActiveIdx.value = (gsActiveIdx.value + 1) % gsDisplayResults.value.length
}

function gsMoveUp() {
  if (gsDisplayResults.value.length === 0) return
  gsActiveIdx.value = (gsActiveIdx.value - 1 + gsDisplayResults.value.length) % gsDisplayResults.value.length
}

function gsSelectActive() {
  const item = gsDisplayResults.value[gsActiveIdx.value]
  if (item) gsSelectItem(item)
}

function gsSelectItem(item) {
  closeGlobalSearch()
  switch (item._section) {
    case 'phim':
      if (item._data?.id) {
        router.push(`/phim/${item._data.id}`)
      } else {
        mainTab.value = 'phim'
      }
      break
    case 'rap':
      mainTab.value = 'rap_chieu'
      break
    case 'km':
      mainTab.value = 'khuyen_mai'
      break
    case 'about':
      mainTab.value = 'gioi_thieu'
      break
  }
}

// Keyboard shortcut: "/" opens global search when not already in an input
function onKeySlash(e) {
  if (e.key === '/' && !['INPUT','TEXTAREA','SELECT'].includes(e.target.tagName)) {
    e.preventDefault()
    openGlobalSearch()
  }
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
const mainCarouselRef = ref(null)

function scrollCarousel(direction) {
  const el = mainCarouselRef.value
  if (!el) return
  // Scroll by one card width plus the gap (matches .carousel-item width)
  const card = el.querySelector('.carousel-item')
  const step = card ? card.getBoundingClientRect().width + 20 : 240
  el.scrollBy({ left: direction * step, behavior: 'smooth' })
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

// ── Promo card: banner + copy code ────────────────────────────
const copiedCode = ref('')

function bannerPoster(promo) {
  return promo?.phims?.length ? (promo.phims[0].posterUrl || '') : ''
}

async function copyPromo(promo) {
  try {
    await navigator.clipboard.writeText(promo.maKhuyenMai)
    copiedCode.value = promo.maKhuyenMai
    setTimeout(() => { if (copiedCode.value === promo.maKhuyenMai) copiedCode.value = '' }, 2000)
  } catch (e) {
    /* clipboard bị chặn — bỏ qua */
  }
}

// ── Quick booking ───────────────────────────────────────────
const qbCinemaId = ref('')
const qbMovieId = ref('')
const qbDate = ref('')
const qbSelected = ref(null)
const qbShowtimes = ref([])
const qbLoading = ref(false)
const qbError = ref('')

const DOW = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7']

const qbDays = computed(() => {
  if (!qbMovieId.value) return []
  const now = Date.now()
  const set = new Set()
  for (const s of qbShowtimes.value) {
    if (String(s.phimId) !== String(qbMovieId.value)) continue
    if (new Date(s.thoiGianBatDau).getTime() <= now) continue
    const iso = String(s.thoiGianBatDau || '').slice(0, 10)
    if (iso) set.add(iso)
  }
  return [...set].sort().map(iso => {
    const d = new Date(iso + 'T00:00:00')
    return { iso, num: d.getDate(), mo: d.getMonth() + 1, dow: DOW[d.getDay()] }
  })
})

const qbMovies = computed(() => {
  const map = new Map()
  for (const s of qbShowtimes.value) {
    if (!map.has(String(s.phimId))) {
      map.set(String(s.phimId), { phimId: s.phimId, tenPhim: s.tenPhim })
    }
  }
  return [...map.values()].sort((a, b) => a.tenPhim.localeCompare(b.tenPhim))
})

const qbShowtimesForDate = computed(() => {
  if (!qbMovieId.value || !qbDate.value) return []
  const now = Date.now()
  return qbShowtimes.value.filter(s =>
    String(s.phimId) === String(qbMovieId.value) &&
    String(s.thoiGianBatDau || '').slice(0, 10) === qbDate.value &&
    new Date(s.thoiGianBatDau).getTime() > now
  )
})

const qbShowtimesByBuoi = computed(() => {
  const morning = []
  const evening = []
  for (const s of qbShowtimesForDate.value) {
    const h = new Date(s.thoiGianBatDau).getHours()
    if (h >= 1 && h <= 12) morning.push(s)
    else evening.push(s)
  }
  return [
    { label: 'Buổi sáng', list: morning },
    { label: 'Buổi chiều / tối', list: evening },
  ].filter(g => g.list.length)
})

async function onQbCinemaChange() {
  qbMovieId.value = ''
  qbDate.value = ''
  qbSelected.value = null
  qbShowtimes.value = []
  qbError.value = ''
  if (!qbCinemaId.value) return
  qbLoading.value = true
  try {
    const res = await api.get(`/lich-chieu/rap/${qbCinemaId.value}`)
    qbShowtimes.value = Array.isArray(res.data) ? res.data : []
    if (qbMovies.value.length) {
      qbMovieId.value = qbMovies.value[0].phimId
      qbDate.value = qbDays.value[0]?.iso || ''
    }
  } catch {
    qbError.value = 'Không tải được lịch chiếu'
  } finally {
    qbLoading.value = false
  }
}

function onQbMovieChange() {
  qbDate.value = qbDays.value[0]?.iso || ''
  qbSelected.value = null
}

function fmtQbTime(dt) {
  return fmtTime12(dt)
}

function fmtQbPrice(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}

function qbBook() {
  const s = qbSelected.value
  if (!s) return
  const cinema = movieStore.cinemas.find(c => String(c.id) === String(qbCinemaId.value))
  if (!authStore.isLoggedIn) {
    authStore.setRedirectPath(`/seat-selection/${s.lichChieuId}`)
    router.push('/auth')
    return
  }
  bookingStore.setMovie({
    id: s.phimId, title: s.tenPhim, poster: s.posterUrl,
    duration: s.thoiLuong, ageRating: s.phanLoaiDoTuoi, language: s.ngonNgu,
  })
  bookingStore.setShowtime({
    id: s.lichChieuId, phimId: s.phimId,
    phongChieuId: s.phongChieuId, tenPhong: s.tenPhong, loaiPhong: s.loaiPhong,
    rapChieuId: cinema?.id, tenRap: cinema?.tenRap, thanhPho: cinema?.thanhPho,
    tenDinhDang: s.tenDinhDang,
    thoiGianBatDau: s.thoiGianBatDau, thoiGianKetThuc: s.thoiGianKetThuc,
    giaCoBan: s.giaCoBan,
    phongChieu: { id: s.phongChieuId, tenPhong: s.tenPhong, loaiPhong: s.loaiPhong },
  })
  router.push(`/seat-selection/${s.lichChieuId}`)
}

watch(() => qbDate.value, () => { qbSelected.value = null })

onMounted(() => {
  movieStore.fetchDangChieu()
  movieStore.fetchSapChieu()
  movieStore.fetchCinemas()
  movieStore.fetchBanners()
  fetchPromos()
  fetchGioiThieu()
  document.addEventListener('click', closeDropdown)
  document.addEventListener('keydown', onKeySlash)
  // Attempt immediate attach (works if data was already cached)
  setTimeout(() => {
    attachWheelScroll(mainCarouselRef.value)
  }, 0)
})

// Re-attach whenever the ref element appears (data loads asynchronously)
watch(mainCarouselRef, (el) => attachWheelScroll(el))

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
  document.removeEventListener('click', closeDropdown)
  document.removeEventListener('keydown', onKeySlash)
  detachWheelScroll(mainCarouselRef.value)
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

.lang-toggle { display: inline-flex; align-items: center; justify-content: center; padding: 0 10px; }
.lang-flag { display: inline-flex; border-radius: 3px; overflow: hidden; }
.lang-flag svg { display: block; }

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

.drawer-tabs {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 8px;
}

.drawer-tabs .drawer-item.active {
  background: rgba(41,188,234,0.12);
  color: var(--electric, #29bcea);
}

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
  height: 50svh;
  min-height: 300px;
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
  font-size: clamp(30px, 5.25vw, 66px);
  font-weight: 700;
  letter-spacing: -0.04em;
  line-height: 1.1;
  margin-bottom: 12px;
  color: var(--text-primary, #f1f5f9);
}

.hero-desc {
  font-size: clamp(10.5px, 1.125vw, 13.5px);
  color: var(--text-secondary, #94a3b8);
  margin-bottom: 21px;
  line-height: 1.6;
  max-width: 480px;
}

.hero-date-range {
  display: inline-flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary, #94a3b8);
  opacity: 0.75;
}

.hero-loading {
  color: var(--text-ghost, rgba(241,245,249,0.45));
  font-size: 13.5px;
}

.btn-hero {
  padding: 10.5px 27px;
  font-size: 11.25px;
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

/* Prev / next arrows on left/right edges */
.banner-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 11;
  width: 42px;
  height: 42px;
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
}
.banner-arrow--left  { left: 16px; }
.banner-arrow--right { right: 16px; }
.banner-arrow:hover {
  background: rgba(41,188,234,0.45);
  border-color: var(--electric, #29bcea);
  transform: translateY(-50%) scale(1.08);
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

/* ── Top-level tabs inside the nav bar (Phim | Rạp Chiếu | ...) ── */
.nav-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 20px;
}

.nav-tabs .main-tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: none;
  border: none;
  border-radius: var(--radius-pill, 999px);
  color: var(--text-secondary, #94a3b8);
  font-size: 14px;
  font-weight: 700;
  font-family: var(--font-ui, 'Inter', sans-serif);
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.2s, background 0.2s;
}

.nav-tabs .main-tab:hover { color: var(--text-primary, #f1f5f9); background: var(--glass-bg, rgba(255,255,255,0.06)); }

.nav-tabs .main-tab.active {
  color: var(--electric, #29bcea);
  background: rgba(41,188,234,0.12);
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
  gap: 28px;
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
  flex: 0 0 330px;
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

/* ── Age rating badge (P, K, T13, T16, T18) ───────────────── */
.age-badge {
  flex: 0 0 auto;
  min-width: 26px;
  height: 26px;
  padding: 0 6px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.3px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.35);
}
.age-badge--p    { background: #16a34a; }
.age-badge--k    { background: #2563eb; }
.age-badge--t13  { background: #d97706; }
.age-badge--t16  { background: #ea580c; }
.age-badge--t18  { background: #dc2626; }
.age-badge--na   { background: #475569; }

/* ── Title bar under poster: age badge + movie name ────────── */
.movie-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: var(--surface-2, #14141f);
  border-top: 1px solid var(--glass-border, rgba(255,255,255,0.06));
}
.movie-info-title {
  flex: 1;
  min-width: 0;
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── Format badges (2D, 3D, IMAX…) ─────────────────────────── */
.format-badges {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 6;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  max-width: calc(100% - 16px);
}
.format-badge {
  background: rgba(5,5,8,0.72);
  border: 1px solid rgba(255,255,255,0.18);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 4px;
  letter-spacing: 0.4px;
  backdrop-filter: blur(3px);
}

/* ── Hover overlay: quick info + actions ───────────────────── */
.movie-overlay {
  position: absolute;
  inset: 0;
  z-index: 5;
  background: linear-gradient(to top, rgba(5,5,8,0.95) 0%, rgba(5,5,8,0.72) 45%, rgba(5,5,8,0.45) 100%);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 12px;
  opacity: 0;
  transform: translateY(10px);
  transition: opacity 0.32s var(--ease-out, cubic-bezier(0.4,0,0.2,1)), transform 0.32s var(--ease-out);
  color: #fff;
}
.movie-card:hover .movie-overlay { opacity: 1; transform: translateY(0); }

.overlay-inner {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 100%;
  overflow: hidden;
}
.overlay-title {
  font-size: 14px;
  font-weight: 800;
  line-height: 1.25;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.overlay-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin: 0;
}
.ov-chip {
  font-size: 11px;
  font-weight: 700;
  background: rgba(255,255,255,0.14);
  padding: 2px 7px;
  border-radius: 4px;
}
.ov-genre {
  font-size: 11px;
  color: rgba(255,255,255,0.82);
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}
.overlay-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0;
  color: var(--gold-bright, #F5D17E);
  font-size: 12px;
  font-weight: 800;
}
.overlay-lang {
  align-self: flex-start;
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: rgba(41,188,234,0.22);
  border: 1px solid rgba(41,188,234,0.45);
  padding: 3px 8px;
  border-radius: 6px;
  letter-spacing: 0.3px;
}
.overlay-actions {
  display: flex;
  gap: 6px;
  margin-top: 2px;
}
.ov-btn {
  flex: 1;
  font-size: 11px;
  font-weight: 800;
  padding: 7px 6px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
  white-space: nowrap;
}
.ov-btn:active { transform: scale(0.97); }
.ov-btn--detail {
  background: rgba(255,255,255,0.16);
  color: #fff;
}
.ov-btn--detail:hover { background: rgba(255,255,255,0.28); }
.ov-btn--book {
  background: var(--electric, #29bcea);
  color: #fff;
}
.ov-btn--book:hover { background: #21a9d3; }

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
  cursor: pointer;
  transition: box-shadow 0.25s;
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

.cinema-card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  margin-top: 0;
}

.btn-directions-card {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary, #94a3b8);
  background: transparent;
  padding: 5px 10px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.2));
  border-radius: var(--radius-pill, 999px);
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
}
.btn-directions-card:hover {
  background: rgba(255,255,255,0.08);
  color: var(--text-primary, #f1f5f9);
  border-color: rgba(255,255,255,0.4);
}

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

.quickbook { padding: 0 40px; margin: 8px auto 8px; max-width: 1400px; }
.quickbook__inner {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 14px);
  padding: 20px;
}
.quickbook__title {
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary, #f1f5f9);
  margin: 0 0 16px;
}
.quickbook__row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.quickbook__field { display: flex; flex-direction: column; gap: 6px; }
.quickbook__label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-ghost, rgba(241,245,249,0.45)); }
.quickbook__select {
  padding: 10px 14px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: 8px;
  background: var(--surface-1, #0f0f17);
  color: var(--text-primary, #f1f5f9);
  font-size: 14px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  outline: none;
  min-height: 42px;
}
.quickbook__select:focus { border-color: var(--electric, #29bcea); }
.quickbook__select:disabled { opacity: 0.5; cursor: not-allowed; }
.quickbook__error { margin-top: 12px; font-size: 13px; color: #fca5a5; }
.quickbook__days { display: flex; gap: 10px; overflow-x: auto; margin-top: 16px; padding-bottom: 12px; scrollbar-width: thin; scrollbar-color: var(--electric-soft, rgba(41,188,234,0.08)) transparent; }
.quickbook__days::-webkit-scrollbar { height: 4px; }
.quickbook__days::-webkit-scrollbar-thumb { background: var(--electric-soft, rgba(41,188,234,0.08)); border-radius: 2px; }
.qb-buoi { margin-top: 16px; }
.qb-buoi__label { font-size: 12px; font-weight: 700; color: var(--electric, #29bcea); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 10px; }
.quickbook__shows { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 12px; margin-top: 16px; }
.qb-show {
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: center;
  padding: 14px 10px;
  border-radius: 12px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--text-secondary, #94a3b8);
  cursor: pointer;
  transition: all 0.2s;
  backdrop-filter: blur(8px);
}
.qb-show:hover { border-color: var(--electric, #29bcea); background: var(--glass-bg-heavy, rgba(255,255,255,0.08)); transform: translateY(-3px); }
.qb-show--active { border-color: var(--electric, #29bcea); background: rgba(41,188,234,0.08); }
.qb-show__time { font-size: 22px; font-weight: 900; color: var(--electric, #29bcea); }
.qb-show__room { font-size: 11px; color: var(--text-ghost, rgba(241,245,249,0.45)); }
.qb-show__format { font-size: 11px; font-weight: 700; color: var(--text-secondary, #94a3b8); }
.qb-show__price { font-size: 13px; font-weight: 800; color: var(--gold-bright, #F5D17E); }
.quickbook__empty { margin-top: 16px; padding: 20px; text-align: center; color: var(--text-ghost, rgba(241,245,249,0.45)); font-size: 14px; }
.quickbook__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding: 14px 18px;
  background: rgba(41,188,234,0.06);
  border: 1px solid rgba(41,188,234,0.18);
  border-radius: 10px;
}
.quickbook__hint { font-size: 13px; color: var(--text-secondary, #94a3b8); }
.qb-book-btn {
  padding: 10px 24px;
  background: var(--electric, #29bcea);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s;
}
.qb-book-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.qb-book-btn:hover:not(:disabled) { background: var(--electric-hover, #1a9fbd); }

.footer {
  background: #0a0a0f;
  border-top: 1px solid rgba(255,255,255,0.06);
  color: var(--text-secondary, #94a3b8);
  padding: 0;
}

#movies-title {
  scroll-margin-top: 110px;
}
#cinemas-title,
#promos-title,
.gioi-thieu-title {
  scroll-margin-top: 110px;
}

/* ── Footer hero (Tầng 1) ─────────────────────────────────── */
.footer-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  max-width: 1400px;
  margin: 0 auto;
  padding: 56px 40px 40px;
}
.footer-hero__tag {
  margin: 0 0 8px;
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  font-size: clamp(28px, 4vw, 44px);
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: 0.5px;
  color: #fff;
}
.footer-hero__sub {
  margin: 0;
  font-size: 15px;
  color: var(--text-secondary, #94a3b8);
}
.footer-hero__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.footer-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 22px;
  border: 1px solid transparent;
  border-radius: var(--radius-pill, 999px);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.25s var(--ease-out, cubic-bezier(0.4,0,0.2,1)), box-shadow 0.25s, background 0.25s, border-color 0.25s, color 0.25s;
}
.footer-btn--primary {
  background: var(--electric, #29bcea);
  color: #fff;
  box-shadow: var(--glow-elec, 0 0 16px rgba(41,188,234,0.30));
}
.footer-btn--primary:hover {
  background: var(--electric-hover, #1a9fbd);
  transform: translateY(-2px);
  box-shadow: 0 0 28px var(--electric-glow, rgba(41,188,234,0.30));
}
.footer-btn--ghost {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-color: var(--glass-border, rgba(255,255,255,0.08));
  color: #fff;
}
.footer-btn--ghost:hover {
  background: var(--gold-soft, rgba(201,168,76,0.10));
  border-color: var(--gold, #C9A84C);
  color: var(--gold-bright, #F5D17E);
  transform: translateY(-2px);
}

/* ── Link columns (Tầng 2) ───────────────────────────────── */
.footer-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 40px 48px;
  border-top: 1px solid rgba(255,255,255,0.06);
  text-align: left;
}
.footer-col__title {
  margin: 0 0 14px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  color: var(--text-primary, #f1f5f9);
}
.footer-link {
  display: block;
  width: 100%;
  margin: 0;
  padding: 6px 0;
  background: none;
  border: none;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--text-secondary, #94a3b8);
  text-align: left;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s;
}
.footer-link:hover {
  color: var(--electric, #29bcea);
}
.footer-link--truncate {
  max-width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ── Bottom bar (Tầng 3) ─────────────────────────────────── */
.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 40px 28px;
  border-top: 1px solid rgba(255,255,255,0.06);
}
.footer-lang {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.footer-lang__btn {
  padding: 5px 10px;
  background: none;
  border: 1px solid transparent;
  border-radius: var(--radius-sm, 6px);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.6px;
  color: var(--text-ghost, rgba(241,245,249,0.45));
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s, background 0.2s;
}
.footer-lang__btn:hover {
  color: var(--text-primary, #f1f5f9);
}
.footer-lang__btn.active {
  color: var(--electric, #29bcea);
  border-color: rgba(41,188,234,0.35);
  background: var(--electric-soft, rgba(41,188,234,0.08));
}
.footer-lang__sep {
  color: var(--text-ghost, rgba(241,245,249,0.45));
}
.footer-copy {
  margin: 0;
  font-size: 13px;
  color: var(--text-ghost, rgba(241,245,249,0.45));
}

@media (max-width: 768px) {
  .footer-hero { flex-direction: column; align-items: flex-start; padding: 40px 20px 32px; }
  .footer-grid { grid-template-columns: 1fr; gap: 24px; padding: 32px 20px 36px; }
  .footer-bottom { padding: 18px 20px 24px; }
}

@media (max-width: 768px) {
  .nav { padding: 8px 16px; }
  .nav-island { padding: 6px 14px; border-radius: var(--radius-lg, 20px); }
  .nav-actions { display: none; }
  .nav-tabs { margin-left: 8px; gap: 2px; }
  .nav-tabs .main-tab { padding: 8px 12px; font-size: 13px; gap: 6px; }
  .hamburger { display: flex; }
  .hero-stage { min-height: 280px; }
  .hero-content { left: 5%; right: 5%; max-width: 100%; }
  .section { padding: 40px 20px; }
  .section--cinemas { padding: 40px 20px; }
  .section-header { flex-direction: column; align-items: flex-start; }
  .search-bar { max-width: 100%; min-width: 0; width: 100%; }
  .carousel-item { flex: 0 0 270px; }
  /* Always show arrows on touch devices */
  .carousel-arrow { opacity: 1; }
  .carousel-arrow--left  { left:  -12px; }
  .carousel-arrow--right { right: -12px; }
  .cinema-grid { grid-template-columns: 1fr; }
  .promo { margin: 40px 20px; }
  .quickbook { padding: 0 20px; }
  .quickbook__row { grid-template-columns: 1fr; }
}

@media (max-width: 480px) {
  .carousel-item { flex: 0 0 225px; }
  .section-title { font-size: 24px; }
}/* ── Promotions section ─────────────────────────────────────── */
.section--promos { max-width: 1400px; margin: 0 auto; padding: 60px 40px; }

.promo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
  gap: 22px;
}

.promo-card {
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 12px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.25s, box-shadow 0.25s, border-color 0.25s;
}

.promo-card:hover {
  transform: translateY(-3px);
  border-color: rgba(41,188,234,0.35);
  box-shadow: var(--shadow-md, 0 8px 24px rgba(0,0,0,0.45));
}

/* ── Banner ──────────────────────────────────────────────── */
.promo-card__banner {
  position: relative;
  height: 150px;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #0b1a26 0%, #10202e 55%, #0a1420 100%);
}
.promo-card__banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.45s var(--ease-out, cubic-bezier(0.4,0,0.2,1));
}
.promo-card:hover .promo-card__banner-img { transform: scale(1.06); }
.promo-card__banner-fallback {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  color: rgba(41,188,234,0.55);
  background:
    radial-gradient(circle at 22% 18%, rgba(41,188,234,0.18), transparent 42%),
    radial-gradient(circle at 80% 75%, rgba(201,168,76,0.16), transparent 45%),
    linear-gradient(135deg, #0b1a26 0%, #10202e 55%, #0a1420 100%);
}
.promo-card__banner-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(to bottom, rgba(5,5,8,0.10) 0%, rgba(5,5,8,0.55) 62%, rgba(5,5,8,0.88) 100%);
}

.promo-card__badge {
  position: absolute; top: 12px; left: 12px;
  z-index: 1;
  padding: 5px 13px;
  border-radius: var(--radius-pill, 999px);
  font-size: 14px; font-weight: 800;
  font-family: var(--font-ui, 'Inter', sans-serif);
  letter-spacing: 0.3px;
  backdrop-filter: blur(4px);
}

.badge--percent {
  background: rgba(41,188,234,0.22);
  color: #7ee0ff;
  border: 1px solid rgba(41,188,234,0.5);
  box-shadow: 0 2px 12px rgba(41,188,234,0.25);
}

.badge--fixed {
  background: rgba(201,168,76,0.22);
  color: #e6c56f;
  border: 1px solid rgba(201,168,76,0.5);
  box-shadow: 0 2px 12px rgba(201,168,76,0.25);
}

.promo-card__banner-movies {
  position: absolute; left: 12px; right: 12px; bottom: 10px;
  z-index: 1;
  display: flex; flex-wrap: wrap; gap: 3px 8px;
}
.promo-card__banner-movie {
  font-size: 12.5px; font-weight: 700; color: #fff;
  text-shadow: 0 1px 4px rgba(0,0,0,0.9);
}

/* ── Body ────────────────────────────────────────────────── */
.promo-card__body {
  padding: 16px 18px 18px;
  display: flex; flex-direction: column; gap: 12px;
  flex: 1;
}

.promo-card__name {
  font-size: 17px; font-weight: 800;
  color: var(--text-primary, #f1f5f9);
  line-height: 1.3;
  margin: 0;
}

.promo-card__code-row {
  display: flex; align-items: stretch; gap: 8px;
}

.promo-card__code {
  flex: 1;
  display: inline-flex; align-items: center; gap: 7px;
  min-width: 0;
  font-size: 13px; font-weight: 800; font-family: monospace;
  letter-spacing: 1px;
  color: var(--electric, #29bcea);
  background: rgba(41,188,234,0.08);
  border: 1px dashed rgba(41,188,234,0.35);
  border-radius: var(--radius-sm, 6px);
  padding: 8px 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.promo-card__copy {
  display: inline-flex; align-items: center; justify-content: center; gap: 5px;
  flex-shrink: 0;
  padding: 0 13px;
  border-radius: var(--radius-sm, 6px);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.15));
  background: var(--glass-bg, rgba(255,255,255,0.05));
  color: var(--text-secondary, #94a3b8);
  font-size: 12px; font-weight: 700;
  font-family: var(--font-ui, 'Inter', sans-serif);
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.promo-card__copy:hover { border-color: var(--electric, #29bcea); color: var(--electric, #29bcea); }
.promo-card__copy:active { transform: scale(0.97); }
.promo-card__copy.copied {
  border-color: rgba(74,222,128,0.5);
  color: #4ade80;
  background: rgba(74,222,128,0.1);
}

.promo-card__desc {
  font-size: 13px;
  color: var(--text-secondary, #94a3b8);
  line-height: 1.55;
  margin: 0;
}

.promo-card__meta {
  display: flex; flex-wrap: wrap; gap: 8px 14px;
}

.promo-card__meta-item {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 12px;
  color: var(--text-secondary, #94a3b8);
}

.promo-card__movies { margin-top: 2px; }

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
.promo-card__cta { margin-top: auto; padding-top: 4px; }

.promo-card__btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
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

/* ── Nav: global search trigger button ── */
.gs-trigger {
  flex-shrink: 0;
}

/* ── Global Search Overlay ── */
.gs-fade-enter-active, .gs-fade-leave-active { transition: opacity 0.18s ease; }
.gs-fade-enter-from, .gs-fade-leave-to { opacity: 0; }

.gs-overlay {
  position: fixed;
  inset: 0;
  z-index: 9000;
  background: rgba(0,0,0,0.72);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 80px 20px 20px;
}

.gs-panel {
  width: 100%;
  max-width: 640px;
  background: var(--surface-2, #14141f);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.1));
  border-radius: var(--radius-lg, 20px);
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(0,0,0,0.6);
  display: flex;
  flex-direction: column;
  max-height: 70vh;
}

.gs-input-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  flex-shrink: 0;
}

.gs-icon {
  color: var(--text-secondary, #94a3b8);
  flex-shrink: 0;
}

.gs-input {
  flex: 1;
  min-width: 0;
  background: transparent;
  border: none;
  outline: none;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 16px;
  color: var(--text-primary, #f1f5f9);
  caret-color: var(--electric, #29bcea);
}
.gs-input::placeholder { color: var(--text-secondary, #94a3b8); }

.gs-close {
  display: flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.1));
  border-radius: var(--radius-sm, 6px);
  color: var(--text-secondary, #94a3b8);
  cursor: pointer;
  padding: 4px 8px;
  font-size: 11px;
  transition: color 150ms, border-color 150ms;
  white-space: nowrap;
}
.gs-close:hover { color: var(--text-primary, #f1f5f9); border-color: var(--text-secondary, #94a3b8); }
kbd {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 10px;
  opacity: 0.7;
}

.gs-body {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--glass-border) transparent;
}

.gs-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 20px;
  color: var(--text-secondary, #94a3b8);
  font-size: 14px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  text-align: center;
}
.gs-empty strong { color: var(--electric, #29bcea); }
.gs-hint { font-size: 13px; }
.gs-kbd-hint { display: flex; gap: 8px; margin-top: 6px; color: var(--text-ghost, rgba(241,245,249,0.45)); font-size: 12px; }
.gs-kbd-hint kbd { background: var(--surface-3, #1a1a28); padding: 2px 6px; border-radius: 4px; opacity: 1; font-size: 11px; }

.gs-spinner {
  width: 24px; height: 24px;
  border: 2px solid var(--glass-border, rgba(255,255,255,0.1));
  border-top-color: var(--electric, #29bcea);
  border-radius: 50%;
  animation: gs-spin 0.7s linear infinite;
}
@keyframes gs-spin { to { transform: rotate(360deg); } }

.gs-filter-row {
  display: flex;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.06));
}
.gs-filter-btn {
  padding: 4px 12px;
  border-radius: var(--radius-pill, 999px);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.1));
  background: transparent;
  color: var(--text-secondary, #94a3b8);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 150ms;
  font-family: var(--font-ui, 'Inter', sans-serif);
}
.gs-filter-btn:hover,
.gs-filter-btn.active {
  background: var(--electric-soft, rgba(41,188,234,0.08));
  border-color: var(--electric, #29bcea);
  color: var(--electric, #29bcea);
}

.gs-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 16px;
  cursor: pointer;
  transition: background 120ms;
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.04));
  font-family: var(--font-ui, 'Inter', sans-serif);
}
.gs-item:last-child { border-bottom: none; }
.gs-item--active,
.gs-item:hover { background: var(--surface-3, #1a1a28); }

.gs-item__section {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-pill, 999px);
  white-space: nowrap;
}
.gs-section--phim  { background: rgba(41,188,234,0.12); color: #29bcea; }
.gs-section--rap   { background: rgba(201,168,76,0.12);  color: #C9A84C; }
.gs-section--km    { background: rgba(16,185,129,0.12);  color: #10B981; }
.gs-section--about { background: rgba(156,163,175,0.12); color: #9CA3AF; }

.gs-item__title {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary, #f1f5f9);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.gs-item__sub {
  font-size: 12px;
  color: var(--text-secondary, #94a3b8);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 160px;
}
.gs-item__go {
  color: var(--text-ghost, rgba(241,245,249,0.45));
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .gs-overlay { padding: 60px 12px 12px; }
  .gs-panel { max-height: 80vh; }
}

</style>

<template>
  <div class="home">
    <!-- NAV -->
    <SiteHeader ref="siteHeaderRef" :t="t" :lang="lang" @search-click="openGlobalSearch" @toggle-lang="toggleLang">
      <template #tabs>
        <div class="nav-item-dropdown" @mouseenter="phimDropdownOpen = true" @mouseleave="phimDropdownOpen = false">
          <button class="main-tab" role="button">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
            <span class="main-tab-label">{{ t('tabPhim') }}</span>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
          </button>
          <transition name="dd-fade">
            <div v-show="phimDropdownOpen" class="nav-dropdown">
              <router-link to="/phim-dangchieu" class="nav-dropdown__item" @click="phimDropdownOpen = false">Đang Chiếu</router-link>
              <router-link to="/phim-sapchieu" class="nav-dropdown__item" @click="phimDropdownOpen = false">Sắp Chiếu</router-link>
            </div>
          </transition>
        </div>
        <button :class="['main-tab', { active: mainTab === 'rap_chieu' }]" @click="mainTab = 'rap_chieu'" role="tab">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          <span class="main-tab-label">{{ t('tabRap') }}</span>
        </button>
        <button :class="['main-tab', { active: mainTab === 'khuyen_mai' }]" @click="mainTab = 'khuyen_mai'" role="tab">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          <span class="main-tab-label">{{ t('tabKhuyenMai') }}</span>
        </button>
        <button :class="['main-tab', { active: mainTab === 'gioi_thieu' }]" @click="mainTab = 'gioi_thieu'" role="tab">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <span class="main-tab-label">{{ t('tabGioiThieu') }}</span>
        </button>
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
        <button :class="['drawer-item', { active: mainTab === 'rap_chieu' }]" @click="mainTab = 'rap_chieu'">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          {{ t('tabRap') }}
        </button>
        <button :class="['drawer-item', { active: mainTab === 'khuyen_mai' }]" @click="mainTab = 'khuyen_mai'">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          {{ t('tabKhuyenMai') }}
        </button>
        <button :class="['drawer-item', { active: mainTab === 'gioi_thieu' }]" @click="mainTab = 'gioi_thieu'">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ t('tabGioiThieu') }}
        </button>
      </template>
    </SiteHeader>

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
    <SiteFooter :t="t" :lang="lang" :cinemas="movieStore.cinemas" @toggle-lang="switchLang" @footer-nav="handleFooterNav" />
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
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import DayChip from '@/components/DayChip.vue'
import { useConfirmModal } from '@/composables/useConfirmModal'
import { fmtTime12 } from '@/utils/homeHelpers'

const router = useRouter()
const authStore = useAuthStore()
const movieStore = useMovieStore()
const bookingStore = useBookingStore()
const { open: openConfirmModal } = useConfirmModal()

const lang = ref((() => { const v = localStorage.getItem('poly_lang'); return (v === 'vi' || v === 'en') ? v : 'vi' })())
const activeTab = ref('dang_chieu')
const mainTab = ref('phim')    // top-level: 'phim' | 'rap_chieu'
const phimDropdownOpen = ref(false)
const siteHeaderRef = ref(null)
const bannerIndex = ref(0)
let bannerTimer = null

// ── Footer helpers ─────────────────────────────────────────────
function handleFooterNav({ action, tab }) {
  siteHeaderRef.value?.closeAll()
  if (action === 'movies') {
    mainTab.value = 'phim'
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } else if (action === 'movies-tab') {
    if (tab === 'sap_chieu') {
      router.push('/phim-sapchieu')
    } else {
      mainTab.value = 'phim'
      activeTab.value = tab
      nextTick(() => {
        const el = document.getElementById('movies-title')
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
      })
    }
  } else if (action === 'tab') {
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
}

function switchLang(next) {
  lang.value = next
  localStorage.setItem('poly_lang', next)
}

const translations = {
  vi: { login: 'Đăng nhập', register: 'Đăng ký', profile: 'Hồ sơ', tickets: 'Vé của tôi', logout: 'Đăng xuất', movies: 'Lịch Chiếu', schedule: 'Phim', now: 'Nổi Bật', nowShowing: 'Đang chiếu', comingSoon: 'Sắp chiếu', bookNow: 'Đặt vé', bookNowCta: 'Đặt vé ngay', viewDetail: 'Xem chi tiết', loading: 'Đang tải', promoTitle: 'Ưu Đãi Thứ 3', promoDesc: 'Giảm 30% vé xem phim vào thứ 3', joinNow: 'Tham Gia', allRights: 'All rights reserved.', featured: 'Phim', topRated: 'Đánh Giá Cao', noMovies: 'Không có phim', searchPlaceholder: 'Tìm phim...', clearSearch: 'Xóa tìm kiếm', noResults: 'Không tìm thấy phim phù hợp', searchResults: 'Kết quả tìm kiếm', movieTabs: 'Danh mục phim', cinemaSystem: 'Hệ Thống', cinemaNetwork: 'Rạp Chiếu', noCinemas: 'Chưa có thông tin rạp', viewMap: 'Xem bản đồ', scrollLeft: 'Cuộn trái', scrollRight: 'Cuộn phải', tabPhim: 'Phim', tabRap: 'Rạp Chiếu', tabKhuyenMai: 'Khuyến Mãi', promoActive: 'Đang Áp Dụng', noPromos: 'Hiện không có khuyến mãi', promoAppliesTo: 'Áp dụng cho phim', promoMin: 'Đơn hàng tối thiểu', promoSystemWide: 'Áp dụng toàn hệ thống', chooseMovie: 'Chọn phim', tabGioiThieu: 'Giới Thiệu', footerTagline: 'Đặt vé nhanh, combo ngon, giải trí trọn vẹn', footerBuyTickets: 'Mua Vé', footerBuyPopcorn: 'Combo Bắp Nước', footerMovies: 'Phim', footerAccount: 'Tài Khoản', footerExplore: 'Khám Phá', footerTheaters: 'Hệ Thống Rạp', footerAllTheaters: 'Tất cả hệ thống rạp' },
  en: { login: 'Login', register: 'Register', profile: 'Profile', tickets: 'My Tickets', logout: 'Logout', movies: 'Movie', schedule: 'Schedule', now: 'Featured', nowShowing: 'Now Showing', comingSoon: 'Coming Soon', bookNow: 'Book Now', bookNowCta: 'Book Now', viewDetail: 'View Details', loading: 'Loading', promoTitle: 'Tuesday Offer', promoDesc: '30% off all tickets on Tuesday', joinNow: 'Join Now', allRights: 'All rights reserved.', featured: 'Top', topRated: 'Rated Movies', noMovies: 'No movies available', searchPlaceholder: 'Search movies...', clearSearch: 'Clear search', noResults: 'No movies found', searchResults: 'Search results', movieTabs: 'Movie categories', cinemaSystem: 'Cinema', cinemaNetwork: 'Network', noCinemas: 'No cinemas available', viewMap: 'View map', scrollLeft: 'Scroll left', scrollRight: 'Scroll right', tabPhim: 'Movies', tabRap: 'Cinemas', tabKhuyenMai: 'Promotions', promoActive: 'Active', noPromos: 'No active promotions', promoAppliesTo: 'Applies to', promoMin: 'Min. order', promoSystemWide: 'System-wide', chooseMovie: 'Browse movies', tabGioiThieu: 'About Us', footerTagline: 'Book fast, tasty combos, complete entertainment', footerBuyTickets: 'Buy Tickets', footerBuyPopcorn: 'Popcorn Combos', footerMovies: 'Movies', footerAccount: 'Account', footerExplore: 'Explore', footerTheaters: 'Cinema System', footerAllTheaters: 'All cinemas' }
}

const langSafe = computed(() => translations[lang.value] ? lang.value : 'vi')
const t = (key) => translations[langSafe.value]?.[key] || key
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
.tab--link { text-decoration: none; font-size: inherit; font-weight: inherit; font-family: inherit; }
.tab--link:hover { color: var(--text-primary, #f1f5f9); }

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

#movies-title {
  scroll-margin-top: 110px;
}
#cinemas-title,
#promos-title,
.gioi-thieu-title {
  scroll-margin-top: 110px;
}

@media (max-width: 768px) {
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

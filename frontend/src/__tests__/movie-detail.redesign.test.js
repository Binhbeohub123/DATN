/**
 * Fix Verification Unit Tests — Task 9.2
 *
 * These assertions check that MovieDetailPage.vue renders correctly after the
 * premium-ui redesign. All tests are expected to PASS on the fixed code.
 *
 * Assertions:
 *  1. `.hero` element is present in rendered DOM (hero section rendered when movie ref is populated)
 *  2. `.btn-bib` or `.btn-book` CTA is present (book-ticket button rendered)
 *  3. `.movie-title` element contains the mocked movie title text
 *  4. Poster `<img>` has the correct `src` attribute from mocked data
 *  5. Rating meta (`.rating-value`) renders the correct value from mocked data
 *
 * Validates: Requirements from premium-ui-redesign spec (task 9.1, 9.2)
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import MovieDetailPage from '../view/MovieDetailPage.vue'

// ── Fixtures ──────────────────────────────────────────────────────────────────

/** Mocked movie data returned by api.get('/phim/1') */
const MOCK_MOVIE = {
  id: 1,
  tenPhim: 'Inception',
  tenPhimTiengAnh: 'Inception',
  theLoai: 'Sci-Fi',
  daoDien: 'Christopher Nolan',
  dienVienChinh: 'Leonardo DiCaprio',
  thoiLuong: 148,
  ngonNgu: 'Tiếng Anh',
  phanLoaiDoTuoi: 'T13',
  posterUrl: 'https://example.com/inception.jpg',
  trailerUrl: '',
  moTa: 'A thief who steals corporate secrets.',
  diemDanhGia: 8.8,
  soLuongDanhGia: 2500000,
  trangThai: 'dang_chieu',
  ngayCongChieu: '2010-07-16',
}

// ── Mock the API module so no real HTTP calls go out during mount ─────────────
vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn((url) => {
      if (url === '/phim/1') {
        return Promise.resolve({ data: MOCK_MOVIE })
      }
      // Return empty schedule for any other endpoint (e.g. /phim/1/lich-chieu)
      return Promise.resolve({ data: [] })
    }),
    post: vi.fn().mockResolvedValue({ data: {} }),
  },
}))

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Create a minimal router that provides route.params.id = '1' */
function buildRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/movie/:id', component: MovieDetailPage },
      { path: '/auth', component: { template: '<div/>' } },
      { path: '/seat-selection/:id', component: { template: '<div/>' } },
    ],
  })
}

// ── Test Suite ────────────────────────────────────────────────────────────────

describe('Fix Verification — MovieDetailPage.vue redesign (EXPECTED TO PASS on fixed code)', () => {
  let wrapper
  let pinia
  let router

  beforeEach(async () => {
    pinia = createPinia()
    setActivePinia(pinia)
    router = buildRouter()

    // Navigate to the movie detail route so route.params.id === '1'
    await router.push('/movie/1')
    await router.isReady()

    wrapper = mount(MovieDetailPage, {
      global: {
        plugins: [pinia, router],
        stubs: {
          // Stub ThemeToggle to avoid themeStore localStorage side-effects in jsdom
          ThemeToggle: { template: '<button class="theme-toggle-stub" />' },
        },
      },
    })

    // Wait for onMounted async operations (loadMovie + loadSchedules) to complete
    await flushPromises()
    await wrapper.vm.$nextTick()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  // ── 1. Hero section is present ────────────────────────────────────────────

  it('1. .hero element is present in rendered DOM when movie data is loaded', () => {
    const hero = wrapper.find('.hero')
    expect(hero.exists()).toBe(true)
  })

  // ── 2. Book-ticket CTA button is present ─────────────────────────────────

  it('2. book-ticket CTA button (.btn-bib or .btn-book) is present in rendered DOM', () => {
    // After the redesign (task 9.1) the class becomes .btn-bib.
    // The pre-redesign class is .btn-book. Accept either to support both states.
    const btnBib = wrapper.find('.btn-bib')
    const btnBook = wrapper.find('.btn-book')

    const ctaExists = btnBib.exists() || btnBook.exists()
    expect(ctaExists).toBe(true)
  })

  // ── 3. Movie title binds correctly from store data ────────────────────────

  it('3. .movie-title element contains the mocked movie title text', () => {
    const titleEl = wrapper.find('.movie-title')
    expect(titleEl.exists()).toBe(true)
    expect(titleEl.text()).toContain(MOCK_MOVIE.tenPhim)
  })

  // ── 4. Poster <img> src attribute binds correctly ─────────────────────────

  it('4. poster <img> has the correct src attribute from mocked data', () => {
    const img = wrapper.find('img.poster-img')
    expect(img.exists()).toBe(true)
    expect(img.attributes('src')).toBe(MOCK_MOVIE.posterUrl)
  })

  // ── 5. Rating meta binds correctly ────────────────────────────────────────

  it('5. .rating-value element renders the correct rating from mocked data', () => {
    const ratingEl = wrapper.find('.rating-value')
    expect(ratingEl.exists()).toBe(true)
    // The component formats via Number(movie.rating).toFixed(1)
    expect(ratingEl.text()).toContain(Number(MOCK_MOVIE.diemDanhGia).toFixed(1))
  })

  // ── Composite: all bindings correct in a single assertion pass ────────────

  it('COMPOSITE: hero, CTA, title, poster src, and rating all bind correctly', () => {
    // Hero exists
    expect(wrapper.find('.hero').exists()).toBe(true)

    // CTA exists (either .btn-bib or .btn-book)
    const ctaExists = wrapper.find('.btn-bib').exists() || wrapper.find('.btn-book').exists()
    expect(ctaExists).toBe(true)

    // Movie title
    const titleEl = wrapper.find('.movie-title')
    expect(titleEl.exists()).toBe(true)
    expect(titleEl.text()).toContain(MOCK_MOVIE.tenPhim)

    // Poster src
    const img = wrapper.find('img.poster-img')
    expect(img.exists()).toBe(true)
    expect(img.attributes('src')).toBe(MOCK_MOVIE.posterUrl)

    // Rating value
    const ratingEl = wrapper.find('.rating-value')
    expect(ratingEl.exists()).toBe(true)
    expect(ratingEl.text()).toContain(Number(MOCK_MOVIE.diemDanhGia).toFixed(1))
  })
})

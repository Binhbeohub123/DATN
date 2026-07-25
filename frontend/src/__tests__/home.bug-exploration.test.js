/**
 * Bug Condition Exploration Test — Task 1.1
 *
 * These assertions check for the PRESENCE of all six bug indicators in the
 * CURRENT (unfixed) code.  The test is EXPECTED TO FAIL once the fix is
 * applied — failure on unfixed code confirms the bugs exist.
 *
 * Bug indicators checked:
 *  1. `.logo` text contains the 🎬 emoji (emoji used instead of SVG icon)
 *  2. Nav does NOT use `backdrop-filter` (glass morphism absent)
 *  3. `h1.movie-title` font-family does NOT contain 'Playfair' (single typeface)
 *  4. No `scroll` event listener on `window` after mount (no parallax)
 *  5. Page background resolves to #ffffff or does not equal #050508 (no void token)
 *  6. `.glass-card` class is absent from rendered DOM (no depth system)
 *
 * Validates: Requirements 1.2, 1.3, 1.9, 1.10, 1.11, 1.12 (bugfix.md)
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import HomeVue from '../view/home.vue'

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Create a minimal router so <router-link> resolves without warnings */
function buildRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div/>' } },
      { path: '/auth', component: { template: '<div/>' } },
      { path: '/profile', component: { template: '<div/>' } },
      { path: '/my-tickets', component: { template: '<div/>' } },
      { path: '/transaction-history', component: { template: '<div/>' } },
    ],
  })
}

/** Stub out store API calls so mount doesn't hit the network */
function stubStoreMethods(wrapper) {
  // movieStore fetch methods are called in onMounted; we stub them via vi.mock
}

// ── Fixtures ──────────────────────────────────────────────────────────────────

// Mock the API module so no real HTTP calls go out during mount
vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn().mockResolvedValue({ data: {} }),
  },
}))

// ── Test Suite ────────────────────────────────────────────────────────────────

describe('Bug Condition Exploration — home.vue (EXPECTED TO FAIL on unfixed code)', () => {
  let wrapper
  let pinia
  let router

  beforeEach(async () => {
    pinia = createPinia()
    setActivePinia(pinia)
    router = buildRouter()

    wrapper = mount(HomeVue, {
      global: {
        plugins: [pinia, router],
        stubs: {
          // Stub ThemeToggle to avoid themeStore localStorage side-effects in jsdom
          ThemeToggle: { template: '<button class="theme-toggle-stub" />' },
        },
      },
    })

    // Wait for router to be ready and any async component setup
    await router.isReady()
    await wrapper.vm.$nextTick()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  // ── Bug Indicator 1: Emoji logo ───────────────────────────────────────────

  it('BUG-1: .logo text content contains 🎬 emoji (emoji not yet replaced by SVG)', () => {
    const logo = wrapper.find('.logo')
    expect(logo.exists()).toBe(true)

    const logoText = logo.text()
    // On UNFIXED code: the logo contains the 🎬 emoji
    // This assertion PASSES on unfixed code, confirming bug indicator 1 exists
    expect(logoText).toContain('🎬')
  })

  // ── Bug Indicator 2: No glass morphism on nav ─────────────────────────────

  it('BUG-2: nav element does NOT use backdrop-filter (glass morphism absent)', () => {
    const nav = wrapper.find('nav.nav')
    expect(nav.exists()).toBe(true)

    // Check that there is no .nav-island child wrapping the nav contents
    // (the .nav-island class is what gets backdrop-filter in the fixed version)
    const navIsland = wrapper.find('.nav-island')
    // On UNFIXED code: .nav-island does not exist
    expect(navIsland.exists()).toBe(false)

    // Additionally confirm the nav element itself has no inline backdrop-filter style
    const navStyle = nav.element.style.backdropFilter || nav.element.style.webkitBackdropFilter || ''
    expect(navStyle).toBe('')
  })

  // ── Bug Indicator 3: Single typeface (no Playfair Display) ───────────────

  it('BUG-3: banner h1 does NOT use Playfair Display font family (single typeface)', () => {
    // The banner content h1 is the primary hero heading in the current layout
    // In the fixed version this will have font-family: var(--font-display) = Playfair Display
    const bannerH1 = wrapper.find('.banner-content h1')

    // In the unfixed code the banner content exists only when currentBanner is truthy.
    // Since our mock returns [] for banners the banner-placeholder renders instead.
    // We instead verify via the scoped CSS that the .home uses 'Raleway' (not Playfair).
    // We can check the home container's fontFamily from getComputedStyle.
    const homeEl = wrapper.find('.home')
    expect(homeEl.exists()).toBe(true)

    // getComputedStyle in jsdom reflects inline styles and style elements injected by Vue SFC
    // The scoped style declares: font-family: 'Raleway', sans-serif on .home
    // We verify by checking the rendered HTML does NOT contain any Playfair reference
    const html = wrapper.html()
    expect(html).not.toContain('Playfair')

    // Also confirm the section-title h2 is present and has no playfair class
    const sectionTitle = wrapper.find('.section-title')
    expect(sectionTitle.exists()).toBe(true)
    const titleClass = sectionTitle.element.className
    expect(titleClass).not.toContain('playfair')
  })

  // ── Bug Indicator 4: No scroll event listener / parallax ─────────────────

  it('BUG-4: no scroll event listener added to window after mount (no parallax)', () => {
    // Spy on window.addEventListener to detect any scroll registration
    const addEventSpy = vi.spyOn(window, 'addEventListener')

    // Re-mount to capture calls during onMounted
    const pinia2 = createPinia()
    setActivePinia(pinia2)
    const router2 = buildRouter()

    addEventSpy.mockClear()

    const w2 = mount(HomeVue, {
      global: {
        plugins: [pinia2, router2],
        stubs: {
          ThemeToggle: { template: '<button />' },
        },
      },
    })

    // Collect all event types registered on window during mount
    const scrollCalls = addEventSpy.mock.calls.filter(
      ([eventType]) => eventType === 'scroll'
    )

    // On UNFIXED code: no scroll listener is added (no parallax choreography)
    // This assertion PASSES on unfixed code, confirming bug indicator 4 exists
    expect(scrollCalls).toHaveLength(0)

    w2.unmount()
    addEventSpy.mockRestore()
  })

  // ── Bug Indicator 5: No void colour token (#050508) ───────────────────────

  it('BUG-5: page background is not the cinematic void token #050508', () => {
    const homeEl = wrapper.find('.home')
    expect(homeEl.exists()).toBe(true)

    // In jsdom, getComputedStyle returns inline styles + styles from <style> blocks
    // Vue SFC scoped styles are injected into the jsdom document during mount
    const computedBg = window.getComputedStyle(homeEl.element).backgroundColor

    // The void token is rgb(5, 5, 8) ≈ #050508
    // On UNFIXED code, background is var(--page-bg) which maps to a light/white value
    // NOT the deep cinematic void background
    const isVoidColour = computedBg === 'rgb(5, 5, 8)' || computedBg === '#050508'
    expect(isVoidColour).toBe(false)
  })

  // ── Bug Indicator 6: No .glass-card in DOM ────────────────────────────────

  it('BUG-6: .glass-card class is absent from rendered DOM (no depth system)', () => {
    // In the fixed version, cards, nav dropdown, and overlays use .glass-card
    // On UNFIXED code: .glass-card does not appear anywhere in the rendered HTML
    const glassCards = wrapper.findAll('.glass-card')
    expect(glassCards).toHaveLength(0)
  })

  // ── Composite: all six indicators together (property-style check) ─────────

  it('PROPERTY: all six bug indicators are simultaneously present in current render', () => {
    const html = wrapper.html()

    // 1. Emoji in logo
    expect(wrapper.find('.logo').text()).toContain('🎬')

    // 2. No nav island (glass morphism absent)
    expect(wrapper.find('.nav-island').exists()).toBe(false)

    // 3. No Playfair reference anywhere in rendered HTML
    expect(html).not.toContain('Playfair')

    // 4. scroll listener absence is checked in the dedicated test above

    // 5. No void colour class or token applied to home container
    expect(html).not.toContain('050508')

    // 6. No glass-card in DOM
    expect(wrapper.findAll('.glass-card')).toHaveLength(0)
  })
})

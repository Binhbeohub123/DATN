/**
 * Fix Verification Property-Based Tests — Task 12.1
 *
 * These tests assert that the cinematic design system is fully applied.
 * They are EXPECTED TO PASS on the FIXED code.
 *
 * Assertions:
 *  1.  tokens.css --void === #050508
 *  2.  tokens.css defines >= 38 custom properties in :root
 *  3.  cinema.css .glass-card has backdrop-filter
 *  4.  cinema.css .btn-bib has cubic-bezier(0.34,1.56,0.64,1) spring easing
 *  5.  cinema.css .reveal has translateY(24px) in initial state
 *  6.  home.vue nav does NOT contain 🎬 emoji
 *  7.  home.vue nav logo area contains <svg
 *  8.  home.vue style has .hero-stage with 100svh
 *  9.  MovieDetailPage.vue style has .hero with 100svh
 *  10. SeatSelectionPage.vue style .seat-page has --void
 *  11. CheckoutPage.vue style .checkout-page has --void
 *  12. AuthPage.vue style .page has --void
 *  13. .nav-island exists in home.vue rendered DOM
 *  14. .hero-stage exists in home.vue rendered DOM
 *
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12
 */

import { describe, it, expect, vi, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// Helper: read a source file relative to the __tests__ dir
const src = (relPath) => fs.readFileSync(path.resolve(__dirname, relPath), 'utf-8')

// ── Mock API so no real HTTP calls go out ─────────────────────────────────────
vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn().mockResolvedValue({ data: {} }),
  },
}))

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Parse all CSS custom-property declarations from the first :root block.
 * Returns a Map of { '--name' => 'value' } with trimmed values.
 */
function parseRootTokens(cssText) {
  const rootMatch = cssText.match(/:root\s*\{([^}]+)\}/)
  if (!rootMatch) return new Map()
  const map = new Map()
  const lineRe = /(-{2}[\w-]+)\s*:\s*([^;]+);/g
  let m
  while ((m = lineRe.exec(rootMatch[1])) !== null) {
    map.set(m[1].trim(), m[2].trim())
  }
  return map
}

/**
 * Extract the body of a specific CSS rule (first occurrence of `.selector { … }`).
 * The regex handles multi-line rule bodies by matching up to the closing brace.
 */
function extractRuleBody(cssText, selector) {
  // Escape special regex chars in selector
  const escaped = selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const re = new RegExp(escaped + '\\s*\\{([^}]+)\\}')
  const m = cssText.match(re)
  return m ? m[1] : ''
}

/**
 * Extract the <template> section from a Vue SFC source string.
 */
function extractTemplate(vueSource) {
  const m = vueSource.match(/<template>([\s\S]*?)<\/template>/)
  return m ? m[1] : ''
}

/**
 * Extract the <style …> section from a Vue SFC source string.
 */
function extractStyle(vueSource) {
  const m = vueSource.match(/<style[\s\S]*?>([\s\S]*?)<\/style>/)
  return m ? m[1] : ''
}

/**
 * Build a minimal router so <router-link> resolves without warnings during mount.
 */
function buildRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div/>' } },
      { path: '/auth', component: { template: '<div/>' } },
      { path: '/profile', component: { template: '<div/>' } },
      { path: '/my-tickets', component: { template: '<div/>' } },
      { path: '/transaction-history', component: { template: '<div/>' } },
      { path: '/movie/:id', name: 'movie-detail', component: { template: '<div/>' } },
    ],
  })
}

// ── Pre-load source files once ────────────────────────────────────────────────

let tokensCss, cinemaCss, homeVue, movieDetailVue, seatSelectionVue, checkoutVue, authVue

beforeAll(() => {
  tokensCss       = src('../assets/tokens.css')
  cinemaCss       = src('../assets/cinema.css')
  homeVue         = src('../view/home.vue')
  movieDetailVue  = src('../view/MovieDetailPage.vue')
  seatSelectionVue = src('../view/SeatSelectionPage.vue')
  checkoutVue     = src('../view/CheckoutPage.vue')
  authVue         = src('../Auth/AuthPage.vue')
})

// ═════════════════════════════════════════════════════════════════════════════
// GROUP 1 — tokens.css assertions
// ═════════════════════════════════════════════════════════════════════════════

describe('Fix Verification — tokens.css', () => {
  it('1. tokens.css --void === #050508 (cinematic void background token)', () => {
    const tokens = parseRootTokens(tokensCss)
    expect(tokens.has('--void')).toBe(true)
    expect(tokens.get('--void')).toBe('#050508')
  })

  it('2. tokens.css defines >= 38 custom properties in :root block', () => {
    const tokens = parseRootTokens(tokensCss)
    expect(tokens.size).toBeGreaterThanOrEqual(38)
  })
})

// ═════════════════════════════════════════════════════════════════════════════
// GROUP 2 — cinema.css assertions
// ═════════════════════════════════════════════════════════════════════════════

describe('Fix Verification — cinema.css', () => {
  it('3. cinema.css .glass-card rule contains backdrop-filter declaration', () => {
    const ruleBody = extractRuleBody(cinemaCss, '.glass-card')
    expect(ruleBody).toContain('backdrop-filter')
  })

  it('4. cinema.css .btn-bib rule references var(--spring) and tokens.css --spring is cubic-bezier(0.34,1.56,0.64,1)', () => {
    // .btn-bib uses the var(--spring) token in its transition declaration
    const ruleBody = extractRuleBody(cinemaCss, '.btn-bib')
    expect(ruleBody).toContain('var(--spring)')

    // The --spring token must resolve to the correct spring cubic-bezier
    const tokens = parseRootTokens(tokensCss)
    expect(tokens.get('--spring')).toBe('cubic-bezier(0.34,1.56,0.64,1)')
  })

  it('5. cinema.css .reveal rule contains translateY(24px) in initial state', () => {
    const ruleBody = extractRuleBody(cinemaCss, '.reveal')
    expect(ruleBody).toContain('translateY(24px)')
  })
})

// ═════════════════════════════════════════════════════════════════════════════
// GROUP 3 — home.vue static source assertions
// ═════════════════════════════════════════════════════════════════════════════

describe('Fix Verification — home.vue (source file checks)', () => {
  it('6. home.vue nav template does NOT contain the 🎬 emoji', () => {
    const template = extractTemplate(homeVue)
    // Locate the nav block
    const navStart = template.indexOf('<nav')
    const navEnd   = template.indexOf('</nav>', navStart) + '</nav>'.length
    expect(navStart).toBeGreaterThan(-1)
    const navBlock = template.slice(navStart, navEnd)
    expect(navBlock).not.toContain('🎬')
  })

  it('7. home.vue nav logo area contains an <svg element (SVG icon replaces emoji)', () => {
    const template = extractTemplate(homeVue)
    // Find the .logo area — from class="logo" to next closing anchor/element
    const logoIdx = template.indexOf('class="logo"')
    expect(logoIdx).toBeGreaterThan(-1)
    // The <svg should appear within a reasonable window after the logo class
    const excerpt = template.slice(logoIdx, logoIdx + 600)
    expect(excerpt).toContain('<svg')
  })

  it('8. home.vue style block has .hero-stage rule with 100svh height', () => {
    const style = extractStyle(homeVue)
    expect(style).toContain('.hero-stage')
    expect(style).toContain('100svh')
  })
})

// ═════════════════════════════════════════════════════════════════════════════
// GROUP 4 — other page source assertions
// ═════════════════════════════════════════════════════════════════════════════

describe('Fix Verification — page-level style checks', () => {
  it('9. MovieDetailPage.vue style has .hero rule with 100svh height', () => {
    const style = extractStyle(movieDetailVue)
    expect(style).toContain('.hero')
    expect(style).toContain('100svh')
  })

  it('10. SeatSelectionPage.vue style .seat-page references --void token', () => {
    const style = extractStyle(seatSelectionVue)
    expect(style).toContain('.seat-page')
    expect(style).toContain('--void')
  })

  it('11. CheckoutPage.vue style .checkout-page references --void token', () => {
    const style = extractStyle(checkoutVue)
    expect(style).toContain('.checkout-page')
    expect(style).toContain('--void')
  })

  it('12. AuthPage.vue style .page references --void token', () => {
    const style = extractStyle(authVue)
    expect(style).toContain('.page')
    expect(style).toContain('--void')
  })
})

// ═════════════════════════════════════════════════════════════════════════════
// GROUP 5 — home.vue DOM mount assertions
// ═════════════════════════════════════════════════════════════════════════════

describe('Fix Verification — home.vue rendered DOM', () => {
  let wrapper

  beforeAll(async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const router = buildRouter()

    // Dynamically import to avoid top-level import issues with vi.mock
    const { default: HomeVue } = await import('../view/home.vue')

    wrapper = mount(HomeVue, {
      global: {
        plugins: [pinia, router],
        stubs: {
          ThemeToggle: { template: '<button class="theme-toggle-stub" />' },
        },
      },
    })

    await router.isReady()
    await wrapper.vm.$nextTick()
  })

  it('13. .nav-island exists in rendered DOM (fluid island nav applied)', () => {
    const island = wrapper.find('.nav-island')
    expect(island.exists()).toBe(true)
  })

  it('14. .hero-stage exists in rendered DOM (cinematic hero stage applied)', () => {
    const stage = wrapper.find('.hero-stage')
    expect(stage.exists()).toBe(true)
  })
})

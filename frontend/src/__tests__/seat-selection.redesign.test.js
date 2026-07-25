/**
 * Fix Verification Unit Tests — Task 10.2
 *
 * These assertions check that SeatSelectionPage.vue renders correctly after the
 * premium-ui redesign. All tests are expected to PASS on the fixed code.
 *
 * Assertions:
 *  1. Page root (.seat-page) background references the `--void` CSS token
 *  2. Adding 8 seats causes isMaxReached to be true and the 9th seat button is disabled
 *  3. The proceed CTA button (.btn-next or .btn-bib) is present in the rendered template
 *
 * Validates: Requirements from premium-ui-redesign spec (task 10.1, 10.2)
 */

import { describe, it, expect, vi, beforeEach, afterEach, beforeAll } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import { useBookingStore } from '../stores/bookingStore'
import { useAuthStore } from '../stores/authStore'
import SeatSelectionPage from '../view/SeatSelectionPage.vue'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

// ── Source file path (read from disk to inspect CSS declarations) ─────────────
const __dirname = path.dirname(fileURLToPath(import.meta.url))
const COMPONENT_PATH = path.resolve(__dirname, '../view/SeatSelectionPage.vue')

// ── Mock API module — no real HTTP calls ──────────────────────────────────────
vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn((url) => {
      // Return an array of 10 available seats for any showtime seat endpoint
      if (url && url.includes('ghe-trong')) {
        const seats = Array.from({ length: 10 }, (_, i) => ({
          id: i + 1,
          soGhe: i + 1,
          hangGhe: 'A',
          loaiGhe: 'normal',
          giaTien: 90000,
          trangThai: 'available',
        }))
        return Promise.resolve({ data: seats })
      }
      return Promise.resolve({ data: [] })
    }),
    post: vi.fn().mockResolvedValue({ data: {} }),
  },
}))

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Create a minimal router with a showtimeId param so loadSeats resolves cleanly */
function buildRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/seat-selection/:showtimeId', component: SeatSelectionPage },
      { path: '/auth', component: { template: '<div/>' } },
      { path: '/combo', component: { template: '<div/>' } },
    ],
  })
}

/** Build N distinct seat objects so addSeat deduplication never trips */
function makeSeats(count = 8) {
  return Array.from({ length: count }, (_, i) => ({
    id: 100 + i,
    soGhe: 100 + i,
    hangGhe: 'B',
    loaiGhe: 'normal',
    giaTien: 90000,
    trangThai: 'available',
  }))
}

// ── Test Suite ────────────────────────────────────────────────────────────────

describe('Fix Verification — SeatSelectionPage.vue redesign (Task 10.2)', () => {
  let wrapper
  let pinia
  let router
  let componentSource

  beforeAll(() => {
    // Read the Vue SFC source from disk once for CSS inspection tests.
    // In jsdom + Vitest, Vue SFC scoped styles are NOT injected as <style> DOM
    // elements — they are processed at transform time only.  Reading from disk
    // is the reliable approach used throughout this project (see tokens.test.js).
    componentSource = fs.readFileSync(COMPONENT_PATH, 'utf-8')
  })

  beforeEach(async () => {
    pinia = createPinia()
    setActivePinia(pinia)
    router = buildRouter()

    // Navigate to a route that provides the showtimeId param
    await router.push('/seat-selection/42')
    await router.isReady()

    // Set auth token so authStore.isLoggedIn === true, preventing redirect to /auth.
    // isLoggedIn is computed(() => !!token.value), so setToken() is the correct way.
    const authStore = useAuthStore()
    authStore.setToken(
      'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfVVNFUiJ9.test'
    )

    wrapper = mount(SeatSelectionPage, {
      global: {
        plugins: [pinia, router],
        stubs: {
          ThemeToggle: { template: '<button class="theme-toggle-stub" />' },
        },
      },
    })

    // Wait for onMounted async operations (loadSeats API call) to complete
    await flushPromises()
    await wrapper.vm.$nextTick()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  // ── Test 1: Page root background references --void token ──────────────────

  it('1. .seat-page background style references the --void CSS token', () => {
    // Verify the element is rendered
    const seatPage = wrapper.find('.seat-page')
    expect(seatPage.exists()).toBe(true)

    // Extract the <style> block from the SFC source file.
    // The redesigned component must declare:
    //   .seat-page { background: var(--void, #050508); ... }
    const styleBlockMatch = componentSource.match(/<style[\s\S]*?>([\s\S]*?)<\/style>/)
    expect(styleBlockMatch).not.toBeNull()

    const styleText = styleBlockMatch[1]

    // Both the rule selector and the token must be present
    expect(styleText).toContain('seat-page')
    expect(styleText).toContain('--void')

    // They must appear in proximity (within the same rule block)
    const seatPageIdx = styleText.indexOf('seat-page')
    const nearbyCSS   = styleText.slice(seatPageIdx, seatPageIdx + 400)
    expect(nearbyCSS).toContain('--void')
  })

  // ── Test 2: Adding 8 seats → isMaxReached true, 9th button disabled ───────

  it('2. adding 8 seats makes isMaxReached true and all unselected seat buttons are disabled', async () => {
    const bookingStore = useBookingStore()

    // onMounted calls bookingStore.clearSeats(), so we start clean
    expect(bookingStore.selectedSeats.length).toBe(0)

    // Add 8 distinct seats via the store (ids 100–107, different from API mock ids 1–10)
    makeSeats(8).forEach(seat => bookingStore.addSeat(seat))

    // Allow Vue reactivity to propagate
    await wrapper.vm.$nextTick()

    // Store state: 8 seats selected
    expect(bookingStore.selectedSeats.length).toBe(8)

    // DOM should show the max-reached warning (v-if="isMaxReached")
    const maxWarn = wrapper.find('.max-warn')
    expect(maxWarn.exists()).toBe(true)

    // All rendered seat buttons that are not selected must now be disabled.
    // The template uses: :disabled="isBooked(seat) || (isMaxReached && !isSelected(seat))"
    // Our mock seats (ids 1–10) are not in selectedSeats, so they hit the isMaxReached branch.
    const seatButtons      = wrapper.findAll('button.seat')
    const unselectedButtons = seatButtons.filter(
      btn => !btn.classes().includes('seat--selected')
    )

    expect(unselectedButtons.length).toBeGreaterThan(0)
    unselectedButtons.forEach(btn => {
      expect(btn.element.disabled).toBe(true)
    })
  })

  // ── Test 3: Proceed CTA button is present ─────────────────────────────────

  it('3. proceed CTA button (.btn-next or .btn-bib) is present in the rendered template', () => {
    // Task 10.1 renames .btn-next to .btn-bib as part of the redesign.
    // Accept either class so the test works both before and after that rename.
    const btnNext = wrapper.find('.btn-next')
    const btnBib  = wrapper.find('.btn-bib')

    expect(btnNext.exists() || btnBib.exists()).toBe(true)
  })

  // ── Composite: all three assertions together ──────────────────────────────

  it('COMPOSITE: background token, max seat logic, and CTA all pass together', async () => {
    const bookingStore = useBookingStore()

    // 1. .seat-page exists and CSS source references --void
    expect(wrapper.find('.seat-page').exists()).toBe(true)

    const styleBlockMatch = componentSource.match(/<style[\s\S]*?>([\s\S]*?)<\/style>/)
    expect(styleBlockMatch).not.toBeNull()
    const styleText   = styleBlockMatch[1]
    const seatPageIdx = styleText.indexOf('seat-page')
    const nearbyCSS   = styleText.slice(seatPageIdx, seatPageIdx + 400)
    expect(nearbyCSS).toContain('--void')

    // 2. Max seat logic
    makeSeats(8).forEach(seat => bookingStore.addSeat(seat))
    await wrapper.vm.$nextTick()

    expect(bookingStore.selectedSeats.length).toBe(8)
    expect(wrapper.find('.max-warn').exists()).toBe(true)

    const unselected = wrapper
      .findAll('button.seat')
      .filter(b => !b.classes().includes('seat--selected'))
    unselected.forEach(btn => expect(btn.element.disabled).toBe(true))

    // 3. CTA button
    expect(
      wrapper.find('.btn-next').exists() || wrapper.find('.btn-bib').exists()
    ).toBe(true)
  })
})

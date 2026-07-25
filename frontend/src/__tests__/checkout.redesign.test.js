/**
 * Fix Verification Unit Tests — Task 11.2
 *
 * These assertions check that CheckoutPage.vue renders correctly after the
 * premium-ui redesign. All tests are expected to PASS on the fixed code.
 *
 * Assertions:
 *  1. `.price-card` element exists and has child elements (proving double-bezel pattern exists)
 *  2. The CSS source file has `--void` in the `.checkout-page` rule (read from disk)
 *  3. Confirm button (`.btn-confirm`) exists and is present in the rendered template
 *  4. `bookingStore.validatePromo` is called with `'POLY10'` when promo code is submitted
 *  5. Confirm button is still wired to `bookingStore.createBooking`
 *
 * Validates: Requirements from premium-ui-redesign spec (task 11.1, 11.2)
 */

import { describe, it, expect, vi, beforeEach, afterEach, beforeAll } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import { useBookingStore } from '../stores/bookingStore'
import { useAuthStore } from '../stores/authStore'
import CheckoutPage from '../view/CheckoutPage.vue'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

// ── Source file path (read from disk to inspect CSS declarations) ─────────────
const __dirname = path.dirname(fileURLToPath(import.meta.url))
const COMPONENT_PATH = path.resolve(__dirname, '../view/CheckoutPage.vue')

// ── Mock API module — no real HTTP calls ──────────────────────────────────────
vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn((url) => {
      // Mock promo validation endpoint
      if (url && url.includes('khuyen-mai/validate')) {
        return Promise.resolve({
          data: {
            tenKhuyenMai: 'POLY10',
            loaiGiamGia: 'percent',
            giaTriGiam: 10,
            giaTriGiamToiDa: 100000,
          },
        })
      }
      // Mock booking creation endpoint
      if (url && url.includes('dat-ve')) {
        return Promise.resolve({
          data: {
            id: 12345,
            tongTien: 180000,
          },
        })
      }
      return Promise.resolve({ data: {} })
    }),
  },
}))

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Create a minimal router with checkout route */
function buildRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/checkout', component: CheckoutPage },
      { path: '/auth', component: { template: '<div/>' } },
      { path: '/payment-result/:bookingId', component: { template: '<div/>' }, name: 'payment-result' },
    ],
  })
}

// ── Test Suite ────────────────────────────────────────────────────────────────

describe('Fix Verification — CheckoutPage.vue redesign (Task 11.2)', () => {
  let wrapper
  let pinia
  let router
  let componentSource

  beforeAll(() => {
    // Read the Vue SFC source from disk once for CSS inspection tests.
    // In jsdom + Vitest, Vue SFC scoped styles are NOT injected as <style> DOM
    // elements — they are processed at transform time only. Reading from disk
    // is the reliable approach used throughout this project (see tokens.test.js).
    componentSource = fs.readFileSync(COMPONENT_PATH, 'utf-8')
  })

  beforeEach(async () => {
    pinia = createPinia()
    setActivePinia(pinia)
    router = buildRouter()

    // Navigate to checkout route
    await router.push('/checkout')
    await router.isReady()

    // Set auth token so authStore.isLoggedIn === true, preventing redirect to /auth.
    const authStore = useAuthStore()
    authStore.setToken(
      'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfVVNFUiJ9.test'
    )

    // Mock user with loyalty points
    authStore.user = {
      id: 1,
      email: 'test@example.com',
      hoTen: 'Test User',
      diemTichLuy: 100,
    }

    // Set up booking store with minimal booking data
    const bookingStore = useBookingStore()
    bookingStore.setMovie({
      id: 1,
      title: 'Test Movie',
      tenPhim: 'Test Movie',
    })
    bookingStore.setShowtime({
      id: 1,
      thoiGianBatDau: '2024-01-15T19:30:00',
      tenPhong: 'Phòng 1',
      loaiPhong: '2D',
    })
    bookingStore.addSeat({
      id: 1,
      soGhe: 1,
      hangGhe: 'A',
      loaiGhe: 'normal',
      giaTien: 90000,
    })
    bookingStore.addSeat({
      id: 2,
      soGhe: 2,
      hangGhe: 'A',
      loaiGhe: 'normal',
      giaTien: 90000,
    })

    wrapper = mount(CheckoutPage, {
      global: {
        plugins: [pinia, router],
        stubs: {
          ThemeToggle: { template: '<button class="theme-toggle-stub" />' },
        },
      },
    })

    // Wait for onMounted async operations to complete
    await flushPromises()
    await wrapper.vm.$nextTick()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  // ── Test 1: .price-card element exists and has child elements ─────────────

  it('1. .price-card element exists and has child elements (double-bezel pattern present)', () => {
    const priceCard = wrapper.find('.price-card')
    expect(priceCard.exists()).toBe(true)

    // Verify it has child elements (proves nested structure)
    const children = priceCard.findAll('*')
    expect(children.length).toBeGreaterThan(0)

    // Verify it contains the title element
    const title = priceCard.find('.price-card__title')
    expect(title.exists()).toBe(true)

    // Verify it contains price rows
    const priceRows = priceCard.findAll('.price-row')
    expect(priceRows.length).toBeGreaterThan(0)

    // Verify padding is present (double-bezel outer ring)
    // The CSS declares: .price-card { padding: 4px; }
    const styleBlockMatch = componentSource.match(/<style[\s\S]*?>([\s\S]*?)<\/style>/)
    expect(styleBlockMatch).not.toBeNull()
    const styleText = styleBlockMatch[1]

    // Find .price-card rule and verify it has padding
    const priceCardIdx = styleText.indexOf('.price-card {')
    expect(priceCardIdx).toBeGreaterThan(-1)
    const priceCardRule = styleText.slice(priceCardIdx, priceCardIdx + 500)
    expect(priceCardRule).toContain('padding')
  })

  // ── Test 2: CSS source file has --void in .checkout-page rule ─────────────

  it('2. CSS source file has --void token in the .checkout-page rule', () => {
    // Extract the <style> block from the SFC source file
    const styleBlockMatch = componentSource.match(/<style[\s\S]*?>([\s\S]*?)<\/style>/)
    expect(styleBlockMatch).not.toBeNull()

    const styleText = styleBlockMatch[1]

    // Verify .checkout-page class is present
    expect(styleText).toContain('.checkout-page')

    // Find the .checkout-page rule and verify it references --void
    const checkoutPageIdx = styleText.indexOf('.checkout-page')
    expect(checkoutPageIdx).toBeGreaterThan(-1)

    // Extract nearby CSS (the rule block)
    const nearbyCSS = styleText.slice(checkoutPageIdx, checkoutPageIdx + 300)
    expect(nearbyCSS).toContain('--void')
  })

  // ── Test 3: .btn-confirm CTA is present ───────────────────────────────────

  it('3. confirm button (.btn-confirm) is present in rendered template', () => {
    const btnConfirm = wrapper.find('.btn-confirm')
    expect(btnConfirm.exists()).toBe(true)

    // Verify button text content
    expect(btnConfirm.text()).toContain('Xác nhận thanh toán')
  })

  // ── Test 4: bookingStore.validatePromo called with 'POLY10' ───────────────

  it("4. bookingStore.validatePromo is called with 'POLY10' when promo code is submitted", async () => {
    const bookingStore = useBookingStore()

    // Spy on validatePromo method
    const validatePromoSpy = vi.spyOn(bookingStore, 'validatePromo')

    // Find promo input and button
    const promoInput = wrapper.find('.promo-input')
    const applyBtn = wrapper.find('.btn-apply')

    expect(promoInput.exists()).toBe(true)
    expect(applyBtn.exists()).toBe(true)

    // Type 'POLY10' into the promo input
    await promoInput.setValue('POLY10')
    await wrapper.vm.$nextTick()

    // Click the apply button
    await applyBtn.trigger('click')
    await flushPromises()

    // Verify validatePromo was called with 'POLY10'
    expect(validatePromoSpy).toHaveBeenCalled()
    expect(validatePromoSpy).toHaveBeenCalledWith('POLY10')
  })

  // ── Test 5: Confirm button wired to bookingStore.createBooking ────────────

  it('5. confirm button is wired to bookingStore.createBooking', async () => {
    const bookingStore = useBookingStore()

    // Spy on createBooking method
    const createBookingSpy = vi.spyOn(bookingStore, 'createBooking')

    // Find and click the confirm button
    const btnConfirm = wrapper.find('.btn-confirm')
    expect(btnConfirm.exists()).toBe(true)

    await btnConfirm.trigger('click')
    await flushPromises()

    // Verify createBooking was called
    expect(createBookingSpy).toHaveBeenCalled()
  })

  // ── Composite: all five assertions together ───────────────────────────────

  it('COMPOSITE: price-card structure, void token, btn-confirm, promo validation, and booking creation all pass', async () => {
    const bookingStore = useBookingStore()

    // 1. .price-card exists and has children
    const priceCard = wrapper.find('.price-card')
    expect(priceCard.exists()).toBe(true)
    expect(priceCard.findAll('*').length).toBeGreaterThan(0)

    // 2. CSS references --void in .checkout-page rule
    const styleBlockMatch = componentSource.match(/<style[\s\S]*?>([\s\S]*?)<\/style>/)
    expect(styleBlockMatch).not.toBeNull()
    const styleText = styleBlockMatch[1]
    const checkoutPageIdx = styleText.indexOf('.checkout-page')
    const nearbyCSS = styleText.slice(checkoutPageIdx, checkoutPageIdx + 300)
    expect(nearbyCSS).toContain('--void')

    // 3. .btn-confirm exists
    const btnConfirm = wrapper.find('.btn-confirm')
    expect(btnConfirm.exists()).toBe(true)

    // 4. validatePromo is called with 'POLY10'
    const validatePromoSpy = vi.spyOn(bookingStore, 'validatePromo')
    const promoInput = wrapper.find('.promo-input')
    const applyBtn = wrapper.find('.btn-apply')
    await promoInput.setValue('POLY10')
    await applyBtn.trigger('click')
    await flushPromises()
    expect(validatePromoSpy).toHaveBeenCalledWith('POLY10')

    // 5. createBooking is called when confirm button is clicked
    const createBookingSpy = vi.spyOn(bookingStore, 'createBooking')
    await btnConfirm.trigger('click')
    await flushPromises()
    expect(createBookingSpy).toHaveBeenCalled()
  })
})

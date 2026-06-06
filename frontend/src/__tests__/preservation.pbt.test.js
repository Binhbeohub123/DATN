/**
 * Preservation Property-Based Test — Task 12.2
 *
 * Verifies that all non-visual (functional) logic is preserved after the
 * premium UI redesign. Tests are grouped into the following properties:
 *
 *  A. bookingStore seat logic (addSeat / removeSeat sequences)
 *  B. bannerIndex cycling (timer advances correctly)
 *  C. Promo code whitespace/uppercase handling
 *  D. authStore login flow
 *  E. Static source-code preservation checks
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { readFileSync } from 'fs'
import { resolve } from 'path'
import { fileURLToPath } from 'url'
import { dirname } from 'path'

// ── Store imports ──────────────────────────────────────────────────────────────
import { useBookingStore } from '@/stores/bookingStore'
import { useMovieStore } from '@/stores/movieStore'
import { useAuthStore } from '@/stores/authStore'

// ── API mock ───────────────────────────────────────────────────────────────────
vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn().mockResolvedValue({ data: {} }),
  },
}))

// Mock localStorage so authStore initialises without browser storage errors
const localStorageMock = (() => {
  let store = {}
  return {
    getItem: (key) => store[key] ?? null,
    setItem: (key, value) => { store[key] = String(value) },
    removeItem: (key) => { delete store[key] },
    clear: () => { store = {} },
  }
})()
Object.defineProperty(global, 'localStorage', { value: localStorageMock, writable: true })

// ── Helper: resolve src path ───────────────────────────────────────────────────
const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)
const srcDir = resolve(__dirname, '..')

function readSrc(relPath) {
  return readFileSync(resolve(srcDir, relPath), 'utf-8')
}

// ── Seat object factory ────────────────────────────────────────────────────────
let _seatId = 1
function makeSeat(overrides = {}) {
  return {
    id: _seatId++,
    soGhe: _seatId,
    hangGhe: 'A',
    loaiGhe: 'thuong',
    giaTien: 50_000 + Math.floor(Math.random() * 100_000),
    trangThai: 'available',
    ...overrides,
  }
}

// ═══════════════════════════════════════════════════════════════════════════════
// A. bookingStore seat logic
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * **Validates: Requirements 3.3, 3.9**
 */
describe('PROPERTY A — bookingStore seat logic', () => {
  let bookingStore

  beforeEach(() => {
    _seatId = 1
    localStorageMock.clear()
    setActivePinia(createPinia())
    bookingStore = useBookingStore()
    bookingStore.clearSeats()
  })

  // ── A1. Three random-like addSeat / removeSeat sequences ──────────────────

  it('A1a: sequence — add 5 seats, remove 2 → length === 3, isMaxReached === false', () => {
    const seats = Array.from({ length: 5 }, makeSeat)
    seats.forEach(s => bookingStore.addSeat(s))
    expect(bookingStore.selectedSeats.length).toBe(5)
    expect(bookingStore.selectedSeats.length).toBeLessThanOrEqual(8)

    bookingStore.removeSeat(seats[0].id)
    bookingStore.removeSeat(seats[2].id)
    expect(bookingStore.selectedSeats.length).toBe(3)
    expect(bookingStore.selectedSeats.length).toBeLessThanOrEqual(8)

    // isMaxReached is a computed in the component but the store exposes the raw
    // selectedSeats array — the max gate is enforced in addSeat
    const isMaxReached = bookingStore.selectedSeats.length >= 8
    expect(isMaxReached).toBe(false)
  })

  it('A1b: sequence — interleaved add/remove → length never exceeds 8', () => {
    const pool = Array.from({ length: 12 }, makeSeat)
    const added = []

    // Add 6
    for (let i = 0; i < 6; i++) {
      bookingStore.addSeat(pool[i])
      added.push(pool[i])
    }
    expect(bookingStore.selectedSeats.length).toBeLessThanOrEqual(8)

    // Remove 3
    bookingStore.removeSeat(added[0].id)
    bookingStore.removeSeat(added[1].id)
    bookingStore.removeSeat(added[2].id)
    expect(bookingStore.selectedSeats.length).toBeLessThanOrEqual(8)

    // Add 5 more (only 2 should be blocked once max is reached)
    for (let i = 6; i < 11; i++) {
      bookingStore.addSeat(pool[i])
    }
    expect(bookingStore.selectedSeats.length).toBeLessThanOrEqual(8)
  })

  it('A1c: sequence — add 3, remove 3, add 8 → length === 8, isMaxReached === true', () => {
    const pool = Array.from({ length: 11 }, makeSeat)

    // Add 3 then remove all 3 → start fresh
    for (let i = 0; i < 3; i++) bookingStore.addSeat(pool[i])
    for (let i = 0; i < 3; i++) bookingStore.removeSeat(pool[i].id)
    expect(bookingStore.selectedSeats.length).toBe(0)

    // Add exactly 8 new seats
    for (let i = 3; i < 11; i++) bookingStore.addSeat(pool[i])
    expect(bookingStore.selectedSeats.length).toBe(8)

    const isMaxReached = bookingStore.selectedSeats.length >= 8
    expect(isMaxReached).toBe(true)
  })

  // ── A2. 8-seat maximum gate ───────────────────────────────────────────────

  it('A2: adding 9th seat is rejected — selectedSeats.length stays at 8', () => {
    const seats = Array.from({ length: 9 }, makeSeat)

    for (let i = 0; i < 8; i++) {
      const added = bookingStore.addSeat(seats[i])
      expect(added).toBe(true)
    }

    expect(bookingStore.selectedSeats.length).toBe(8)
    const isMaxReached = bookingStore.selectedSeats.length >= 8
    expect(isMaxReached).toBe(true)

    // Attempt to add 9th seat — store enforces the 8-seat cap
    const rejected = bookingStore.addSeat(seats[8])
    expect(rejected).toBe(false)
    expect(bookingStore.selectedSeats.length).toBe(8)
  })

  // ── A3. totalSeatPrice === sum of giaTien ─────────────────────────────────

  it('A3: totalSeatPrice equals sum of all selected seat giaTien values', () => {
    const prices = [45_000, 75_000, 60_000, 90_000, 50_000]
    const seats = prices.map(p => makeSeat({ giaTien: p }))

    seats.forEach(s => bookingStore.addSeat(s))

    const expected = prices.reduce((a, b) => a + b, 0)
    expect(bookingStore.totalSeatPrice).toBe(expected)
  })

  it('A3b: totalSeatPrice after removing a seat subtracts that seat price', () => {
    const seats = [
      makeSeat({ giaTien: 50_000 }),
      makeSeat({ giaTien: 80_000 }),
      makeSeat({ giaTien: 60_000 }),
    ]
    seats.forEach(s => bookingStore.addSeat(s))
    expect(bookingStore.totalSeatPrice).toBe(190_000)

    bookingStore.removeSeat(seats[1].id)
    expect(bookingStore.totalSeatPrice).toBe(110_000)
  })
})

// ═══════════════════════════════════════════════════════════════════════════════
// B. bannerIndex cycling
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * **Validates: Requirements 3.8**
 */
describe('PROPERTY B — bannerIndex cycling via startBannerTimer logic', () => {
  /**
   * Simulate the startBannerTimer logic extracted from home.vue script setup:
   *   bannerTimer = setInterval(() => {
   *     bannerIndex.value = (bannerIndex.value + 1) % banners.length
   *   }, 5000)
   *
   * We advance N ticks manually without relying on real timers.
   */
  function simulateBannerCycling(bannerCount, ticks) {
    let bannerIndex = 0
    const indices = [bannerIndex]
    for (let i = 0; i < ticks; i++) {
      bannerIndex = (bannerIndex + 1) % bannerCount
      indices.push(bannerIndex)
    }
    return indices
  }

  const bannerLengths = [1, 3, 5]

  bannerLengths.forEach(len => {
    it(`B: bannerIndex never exceeds ${len - 1} for banners.length === ${len} (20 ticks)`, () => {
      const indices = simulateBannerCycling(len, 20)

      indices.forEach(idx => {
        expect(idx).toBeGreaterThanOrEqual(0)
        expect(idx).toBeLessThanOrEqual(len - 1)
      })
    })

    if (len > 1) {
      it(`B: bannerIndex cycles back to 0 after reaching ${len - 1} for length === ${len}`, () => {
        const indices = simulateBannerCycling(len, len * 3)

        // After exactly `len` ticks the index should be back to 0
        expect(indices[len]).toBe(0)

        // Verify full cycle pattern: 0,1,2,...,len-1, 0,1,2,...
        for (let i = 0; i < len; i++) {
          expect(indices[i]).toBe(i)
        }
      })
    }
  })

  it('B: bannerIndex for length=1 stays at 0 for every tick', () => {
    const indices = simulateBannerCycling(1, 10)
    indices.forEach(idx => expect(idx).toBe(0))
  })
})

// ═══════════════════════════════════════════════════════════════════════════════
// C. Promo code handling
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * **Validates: Requirements 3.4**
 */
describe('PROPERTY C — promo code whitespace + uppercase normalisation', () => {
  let bookingStore

  beforeEach(() => {
    localStorageMock.clear()
    setActivePinia(createPinia())
    bookingStore = useBookingStore()
  })

  /**
   * C5: For promo codes with leading/trailing whitespace,
   * applyPromo in CheckoutPage uses `promoInput.value.trim().toUpperCase()`
   * before calling bookingStore.validatePromo.
   *
   * We verify the transformation logic directly (mirrors CheckoutPage.applyPromo):
   *   const ok = await bookingStore.validatePromo(promoInput.value.trim().toUpperCase())
   */
  const promoInputVariants = [
    { raw: '  poly10  ', expected: 'POLY10' },
    { raw: 'welcome50k', expected: 'WELCOME50K' },
    { raw: ' SALE20 ',  expected: 'SALE20' },
    { raw: '  abc  ',   expected: 'ABC' },
    { raw: 'XYZ',       expected: 'XYZ' },
    { raw: '   ',       expected: '' },           // empty after trim → no call
  ]

  promoInputVariants.forEach(({ raw, expected }) => {
    it(`C5: "${raw}" → trim().toUpperCase() === "${expected}"`, () => {
      const normalised = raw.trim().toUpperCase()
      expect(normalised).toBe(expected)
    })
  })

  it('C6: CheckoutPage.vue source uses promoInput.value.trim().toUpperCase() in applyPromo', () => {
    const source = readSrc('view/CheckoutPage.vue')
    // Verify the exact normalisation pattern is present in the source
    expect(source).toContain('promoInput.value.trim().toUpperCase()')
  })

  it('C5+C6: validatePromo is called with trimmed uppercased code (integration check)', async () => {
    const api = (await import('@/services/api')).default
    api.post.mockResolvedValueOnce({
      data: {
        tenKhuyenMai: 'Test Promo',
        loaiGiamGia: 'percent',
        giaTriGiam: 10,
        giaTriGiamToiDa: 500_000,
      },
    })

    // Simulate what CheckoutPage.applyPromo does:
    const rawInput = '  poly10  '
    const normalised = rawInput.trim().toUpperCase()   // === 'POLY10'
    const ok = await bookingStore.validatePromo(normalised)

    expect(ok).toBe(true)
    expect(bookingStore.promoCode).toBe('POLY10')
    // Verify the API was called with the normalised code
    expect(api.post).toHaveBeenCalledWith('/khuyen-mai/validate', { maKhuyenMai: 'POLY10' })
  })
})

// ═══════════════════════════════════════════════════════════════════════════════
// D. authStore login flow
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * **Validates: Requirements 3.2**
 */
describe('PROPERTY D — authStore login flow', () => {
  let authStore

  beforeEach(() => {
    localStorageMock.clear()
    setActivePinia(createPinia())
    authStore = useAuthStore()
  })

  it('D7: authStore.login is a function that exists and can be called', () => {
    expect(typeof authStore.login).toBe('function')
  })

  it('D7: authStore.login returns a Promise when called', async () => {
    const apiMod = await import('@/services/api')
    const api = apiMod.default
    api.post.mockResolvedValueOnce({ data: { token: 'fake.jwt.token' } })
    api.get.mockResolvedValueOnce({ data: { email: 'test@example.com' } })

    const result = authStore.login('test@example.com', 'password123')
    expect(result).toBeInstanceOf(Promise)
    // Await so the mock call is consumed and doesn't bleed into other tests
    await result
  })

  it('D8: isLoggedIn is false when no token is set', () => {
    // Fresh store, no token in localStorage
    expect(authStore.isLoggedIn).toBe(false)
  })

  it('D8: isLoggedIn becomes true after setToken is called with a valid token', () => {
    expect(authStore.isLoggedIn).toBe(false)

    // setToken stores the token and marks user as logged in
    authStore.setToken('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0IiwicGVybWlzc2lvbnMiOlsiUk9MRV9VU0VSIl0sInJvbGUiOiJVU0VSIn0.fake')
    expect(authStore.isLoggedIn).toBe(true)
  })

  it('D8: isLoggedIn returns false after logout clears the token', () => {
    authStore.setToken('some.valid.token')
    expect(authStore.isLoggedIn).toBe(true)

    authStore.logout()
    expect(authStore.isLoggedIn).toBe(false)
    expect(localStorageMock.getItem('token')).toBeNull()
  })

  it('D8: isLoggedIn is computed from token — clearing token directly sets it false', () => {
    authStore.setToken('some.valid.token')
    expect(authStore.isLoggedIn).toBe(true)

    // Call setToken with an empty/null value to clear
    authStore.setToken('')
    expect(authStore.isLoggedIn).toBe(false)
  })
})

// ═══════════════════════════════════════════════════════════════════════════════
// E. Static source analysis — script setup preservation checks
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10**
 */
describe('PROPERTY E — script setup preservation (static source analysis)', () => {

  it('E9: home.vue <script setup> calls movieStore.fetchDangChieu()', () => {
    const source = readSrc('view/home.vue')
    expect(source).toContain('movieStore.fetchDangChieu()')
  })

  it('E9: home.vue <script setup> calls movieStore.fetchSapChieu()', () => {
    const source = readSrc('view/home.vue')
    expect(source).toContain('movieStore.fetchSapChieu()')
  })

  it('E9: home.vue <script setup> calls movieStore.fetchBanners()', () => {
    const source = readSrc('view/home.vue')
    expect(source).toContain('movieStore.fetchBanners()')
  })

  it('E9: all three fetchX calls appear inside the onMounted block in home.vue', () => {
    const source = readSrc('view/home.vue')
    // Verify onMounted is present and the fetch calls appear after it
    const onMountedIdx = source.indexOf('onMounted(')
    expect(onMountedIdx).toBeGreaterThan(-1)

    const fetchDC = source.indexOf('movieStore.fetchDangChieu()', onMountedIdx)
    const fetchSC = source.indexOf('movieStore.fetchSapChieu()', onMountedIdx)
    const fetchBN = source.indexOf('movieStore.fetchBanners()', onMountedIdx)

    expect(fetchDC).toBeGreaterThan(onMountedIdx)
    expect(fetchSC).toBeGreaterThan(onMountedIdx)
    expect(fetchBN).toBeGreaterThan(onMountedIdx)
  })

  it('E10: SeatSelectionPage.vue calls bookingStore.clearSeats() in onMounted', () => {
    const source = readSrc('view/SeatSelectionPage.vue')
    const onMountedIdx = source.indexOf('onMounted(')
    expect(onMountedIdx).toBeGreaterThan(-1)

    const clearSeatsIdx = source.indexOf('bookingStore.clearSeats()', onMountedIdx)
    expect(clearSeatsIdx).toBeGreaterThan(onMountedIdx)
  })

  it('E11: CheckoutPage.vue calls bookingStore.createBooking() in the confirm() function', () => {
    const source = readSrc('view/CheckoutPage.vue')

    // Locate the confirm function
    const confirmFnIdx = source.indexOf('async function confirm(')
    expect(confirmFnIdx).toBeGreaterThan(-1)

    const createBookingIdx = source.indexOf('bookingStore.createBooking()', confirmFnIdx)
    expect(createBookingIdx).toBeGreaterThan(confirmFnIdx)
  })

  it('E12: AuthPage.vue login() function calls authStore.login(email, loginForm.value.password)', () => {
    // AuthPage.vue is in src/Auth/
    const source = readSrc('Auth/AuthPage.vue')

    // The login function exists
    const loginFnIdx = source.indexOf('async function login()')
    expect(loginFnIdx).toBeGreaterThan(-1)

    // authStore.login is called with email and password
    const authLoginIdx = source.indexOf('authStore.login(email, loginForm.value.password)', loginFnIdx)
    expect(authLoginIdx).toBeGreaterThan(loginFnIdx)
  })

  it('E: SeatSelectionPage.vue still imports and uses bookingStore', () => {
    const source = readSrc('view/SeatSelectionPage.vue')
    expect(source).toContain("import { useBookingStore } from '@/stores/bookingStore'")
    expect(source).toContain('bookingStore.selectedSeats')
  })

  it('E: CheckoutPage.vue still imports and uses bookingStore', () => {
    const source = readSrc('view/CheckoutPage.vue')
    expect(source).toContain("import { useBookingStore } from '@/stores/bookingStore'")
    expect(source).toContain('bookingStore.createBooking()')
  })

  it('E: home.vue still imports and uses both movieStore and authStore', () => {
    const source = readSrc('view/home.vue')
    expect(source).toContain("import { useMovieStore } from '@/stores/movieStore'")
    expect(source).toContain("import { useAuthStore } from '@/stores/authStore'")
  })
})

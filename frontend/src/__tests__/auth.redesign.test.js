/**
 * Fix Verification Unit Tests — Task 8.3
 *
 * These assertions check for the REDESIGNED state (post-fix) of AuthPage.vue.
 * All tests are expected to PASS on the fixed code.
 *
 * Assertions:
 *  1. Each `.input-wrap` element contains both an `<input>` and a `<label>` sibling
 *     (floating-label DOM structure present)
 *  2. Rendered HTML does NOT contain any emoji characters
 *     (🎬, 👤, 💳, ⚙️, 🎟️, 🔓, 📋)
 *  3. `.btn-bib` class is present on the login submit button
 *  4. `authStore.login` is called with trimmed + lowercased email on form submit
 *
 * Validates: Requirements 2.1, 2.2, 2.3, 3.4 (premium-ui-redesign spec)
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import AuthPage from '../Auth/AuthPage.vue'

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Create a minimal router so <router-link> and useRouter() resolve without warnings */
function buildRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/auth', component: { template: '<div/>' } },
      { path: '/', component: { template: '<div/>' } },
      { path: '/admin', component: { template: '<div/>' } },
    ],
  })
}

// ── Mock the API module so no real HTTP calls go out during mount ─────────────
vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: {} }),
    post: vi.fn().mockResolvedValue({ data: { token: 'fake.jwt.token' } }),
  },
}))

// ── Test Suite ────────────────────────────────────────────────────────────────

describe('Fix Verification — AuthPage.vue redesign (EXPECTED TO PASS on fixed code)', () => {
  let wrapper
  let pinia
  let router

  beforeEach(async () => {
    pinia = createPinia()
    setActivePinia(pinia)
    router = buildRouter()

    wrapper = mount(AuthPage, {
      global: {
        plugins: [pinia, router],
        stubs: {
          // Stub ThemeToggle to avoid themeStore localStorage side-effects in jsdom
          ThemeToggle: { template: '<button />' },
        },
      },
    })

    await router.isReady()
    await wrapper.vm.$nextTick()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  // ── 1. Floating-label DOM structure ──────────────────────────────────────

  it('1. each .input-wrap contains both an <input> and a <label> sibling', () => {
    const inputWraps = wrapper.findAll('.input-wrap')

    // Should have at least the email and password inputs in login mode
    expect(inputWraps.length).toBeGreaterThan(0)

    inputWraps.forEach((wrap, i) => {
      const input = wrap.find('input')
      const label = wrap.find('label')

      expect(input.exists(), `input-wrap[${i}] should contain an <input>`).toBe(true)
      expect(label.exists(), `input-wrap[${i}] should contain a <label>`).toBe(true)
    })
  })

  // ── 2. No emoji in rendered template ──────────────────────────────────────

  it('2. rendered HTML does NOT contain any emoji characters', () => {
    const html = wrapper.html()

    const emojis = ['🎬', '👤', '💳', '⚙️', '🎟️', '🔓', '📋']
    emojis.forEach((emoji) => {
      expect(html, `rendered HTML should not contain emoji: ${emoji}`).not.toContain(emoji)
    })
  })

  // ── 3. .btn-bib class on login button ─────────────────────────────────────

  it('3. login submit button has .btn-bib class (Button-in-Button CTA applied)', () => {
    // Default mount is in login mode (isRegister = false, isForgot = false, isVerify = false)
    const loginBtn = wrapper.find('.btn-bib')
    expect(loginBtn.exists()).toBe(true)
  })

  // ── 4. authStore.login called with trimmed + lowercased email ─────────────

  it('4. authStore.login is called with trimmed and lowercased email on form submit', async () => {
    // Import the store and spy on the login method
    const { useAuthStore } = await import('../stores/authStore')

    const pinia2 = createPinia()
    setActivePinia(pinia2)
    const router2 = buildRouter()

    const authStore = useAuthStore()
    const loginSpy = vi.spyOn(authStore, 'login').mockResolvedValue(true)

    const w = mount(AuthPage, {
      global: {
        plugins: [pinia2, router2],
        stubs: {
          ThemeToggle: { template: '<button />' },
        },
      },
    })

    await router2.isReady()
    await w.vm.$nextTick()

    // Set email with surrounding whitespace and mixed case, and a valid password
    const emailInput = w.find('input[type="email"]')
    const passwordInput = w.find('input[type="password"]')

    expect(emailInput.exists()).toBe(true)
    expect(passwordInput.exists()).toBe(true)

    // Simulate user typing an email with leading/trailing spaces and uppercase characters
    await emailInput.setValue('  Test.User@Example.COM  ')
    await passwordInput.setValue('password123')

    // Click the login button
    const loginBtn = w.find('.btn-bib')
    expect(loginBtn.exists()).toBe(true)
    await loginBtn.trigger('click')

    // Wait for async handler
    await w.vm.$nextTick()

    // Verify authStore.login was called with trimmed + lowercased email
    expect(loginSpy).toHaveBeenCalledOnce()
    const [calledEmail] = loginSpy.mock.calls[0]
    expect(calledEmail).toBe('test.user@example.com')

    w.unmount()
  })
})

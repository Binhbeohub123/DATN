/**
 * Staff route guard (C) — STAFF role is restricted to staff-only routes.
 *
 * Simulates the navigation guard by driving the real router with a JWT that
 * carries a given role. Asserts redirect targets for all three roles across
 * customer / staff / admin paths.
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest'

// Encode a tiny JWT payload with the given role (no expiry check concerns).
function makeToken(role) {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const payload = btoa(
    JSON.stringify({
      sub: '1',
      role: role ? `ROLE_${role}` : role,
      exp: Math.floor(Date.now() / 1000) + 60 * 60,
    })
  )
  return `${header}.${payload}.sig`
}

// Capture which path the guard ultimately allows for a navigation target.
// Uses the app's real router (imported here) so the beforeEach guard runs.
describe('STAFF route whitelist guard', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  async function resolveRedirect(token, targetPath) {
    localStorage.setItem('token', token)
    // Lazy import the router so the guard is fresh per test.
    const { default: router } = await import('@/router/index.js')
    let resolved = targetPath
    try {
      await router.push(targetPath)
      resolved = router.currentRoute.value.path
    } catch (e) {
      // Redirection guards reject the navigation promise — read the actual path.
      resolved = router.currentRoute.value.path
    }
    return resolved
  }

  it('STAFF is redirected from customer home / to /staff/dashboard', async () => {
    const final = await resolveRedirect(makeToken('STAFF'), '/')
    expect(final).toBe('/staff/dashboard')
  })

  it('STAFF is redirected from /movies to /staff/dashboard', async () => {
    const final = await resolveRedirect(makeToken('STAFF'), '/movies')
    expect(final).toBe('/staff/dashboard')
  })

  it('STAFF is redirected from /profile to /staff/dashboard', async () => {
    const final = await resolveRedirect(makeToken('STAFF'), '/profile')
    expect(final).toBe('/staff/dashboard')
  })

  it('STAFF is redirected from /admin/dashboard to /staff/dashboard', async () => {
    const final = await resolveRedirect(makeToken('STAFF'), '/admin/dashboard')
    expect(final).toBe('/staff/dashboard')
  })

  it('STAFF CAN access all 4 whitelisted staff routes', async () => {
    for (const allowed of ['/staff/dashboard', '/staff/pos', '/staff/checkin', '/staff/report']) {
      const final = await resolveRedirect(makeToken('STAFF'), allowed)
      expect(final, `STAFF should reach ${allowed}`).toBe(allowed)
    }
  })

  it('ADMIN can still access customer home and admin dashboard (unaffected)', async () => {
    const home = await resolveRedirect(makeToken('ADMIN'), '/')
    expect(home).toBe('/')
    const admin = await resolveRedirect(makeToken('ADMIN'), '/admin/dashboard')
    expect(admin).toBe('/admin/dashboard')
    const staff = await resolveRedirect(makeToken('ADMIN'), '/staff/dashboard')
    expect(staff).toBe('/staff/dashboard')
  })

  it('CUSTOMER cannot reach staff routes (existing guard B unchanged)', async () => {
    const final = await resolveRedirect(makeToken('CUSTOMER'), '/staff/pos')
    expect(final).not.toBe('/staff/pos')
  })
})

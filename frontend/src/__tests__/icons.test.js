/**
 * Task 6.15 — Unit tests for SVG icon components
 *
 * For each of the 14 icon components, verifies:
 *   1. The component renders an <svg> element
 *   2. The <svg> has width="24" and height="24" by default (size prop defaults to 24)
 *   3. The <svg> has stroke-width="1.5" attribute
 *   4. When size prop is passed (e.g. 32), the svg has width="32" and height="32"
 *
 * Note: IconStar and IconPlay use fill="currentColor" on their inner shapes and
 * stroke="none" on the svg root, but stroke-width="1.5" is still present on the SVG.
 *
 * Validates: Task 6.1–6.14 (all icon components)
 */

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

// Registry of all 14 icon components under test
// Each entry: [displayName, asyncImport]
const iconModules = [
  ['IconFilm',     () => import('../components/icons/IconFilm.vue')],
  ['IconTicket',   () => import('../components/icons/IconTicket.vue')],
  ['IconUser',     () => import('../components/icons/IconUser.vue')],
  ['IconCard',     () => import('../components/icons/IconCard.vue')],
  ['IconSettings', () => import('../components/icons/IconSettings.vue')],
  ['IconBack',     () => import('../components/icons/IconBack.vue')],
  ['IconStar',     () => import('../components/icons/IconStar.vue')],
  ['IconClock',    () => import('../components/icons/IconClock.vue')],
  ['IconPlay',     () => import('../components/icons/IconPlay.vue')],
  ['IconCheck',    () => import('../components/icons/IconCheck.vue')],
  ['IconLogout',   () => import('../components/icons/IconLogout.vue')],
  ['IconHistory',  () => import('../components/icons/IconHistory.vue')],
  ['IconGlobe',    () => import('../components/icons/IconGlobe.vue')],
  ['IconKey',      () => import('../components/icons/IconKey.vue')],
]

describe('SVG Icon Components', () => {
  for (const [name, importFn] of iconModules) {
    describe(name, () => {
      it('renders an <svg> element', async () => {
        const mod = await importFn()
        const component = mod.default
        const wrapper = mount(component)

        const svg = wrapper.find('svg')
        expect(svg.exists(), `${name} should render an <svg> element`).toBe(true)
      })

      it('has width="24" and height="24" by default', async () => {
        const mod = await importFn()
        const component = mod.default
        const wrapper = mount(component)

        const svg = wrapper.find('svg')
        expect(svg.attributes('width'), `${name} default width should be "24"`).toBe('24')
        expect(svg.attributes('height'), `${name} default height should be "24"`).toBe('24')
      })

      it('has stroke-width="1.5" attribute on the svg root', async () => {
        const mod = await importFn()
        const component = mod.default
        const wrapper = mount(component)

        const svg = wrapper.find('svg')
        expect(
          svg.attributes('stroke-width'),
          `${name} svg should have stroke-width="1.5" even when stroke="none"`
        ).toBe('1.5')
      })

      it('uses width and height equal to the size prop when size=32', async () => {
        const mod = await importFn()
        const component = mod.default
        const wrapper = mount(component, { props: { size: 32 } })

        const svg = wrapper.find('svg')
        expect(svg.attributes('width'), `${name} width should reflect size prop "32"`).toBe('32')
        expect(svg.attributes('height'), `${name} height should reflect size prop "32"`).toBe('32')
      })
    })
  }
})

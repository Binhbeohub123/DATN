/**
 * Task 5.3 — Unit tests for motion composables
 *
 * Tests:
 *   - useReveal: attaches IntersectionObserver, adds `is-visible` class on intersection,
 *     calls unobserve after intersection, disconnects on unmount
 *   - useMagnetic: applies translate() transform on mousemove, clears on mouseleave,
 *     cleanup function removes both event listeners
 *
 * Validates: Task 5.1 (useReveal) and Task 5.2 (useMagnetic)
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, ref, h } from 'vue'
import { useReveal } from '@/composables/useReveal'
import { useMagnetic } from '@/composables/useMagnetic'

// ─── useReveal tests ──────────────────────────────────────────────────────────

describe('useReveal', () => {
  let mockObserve
  let mockUnobserve
  let mockDisconnect
  let capturedCallback
  let capturedOptions
  let constructorCallCount

  beforeEach(() => {
    mockObserve = vi.fn()
    mockUnobserve = vi.fn()
    mockDisconnect = vi.fn()
    capturedCallback = null
    capturedOptions = null
    constructorCallCount = 0

    // `vi.fn()` produces an arrow function — not constructable with `new`.
    // Use a regular class so `new IntersectionObserver(...)` works in jsdom.
    class MockIO {
      constructor(callback, options) {
        constructorCallCount++
        capturedCallback = callback
        capturedOptions = options
        this.observe = mockObserve
        this.unobserve = mockUnobserve
        this.disconnect = mockDisconnect
      }
    }

    // Inject mock into jsdom global (IntersectionObserver is not available by default)
    global.IntersectionObserver = MockIO
  })

  afterEach(() => {
    delete global.IntersectionObserver
    vi.restoreAllMocks()
  })

  it('calls IntersectionObserver constructor with threshold 0.12', async () => {
    // A minimal Vue component that mounts useReveal on a container div
    const TestComponent = defineComponent({
      setup() {
        const container = ref(null)
        useReveal(container)
        return { container }
      },
      render() {
        return h('div', { ref: 'container' }, [
          h('div', { class: 'reveal' }),
        ])
      },
    })

    const wrapper = mount(TestComponent, { attachTo: document.body })
    await wrapper.vm.$nextTick()

    expect(constructorCallCount).toBe(1)
    expect(capturedOptions).toMatchObject({ threshold: 0.12 })

    wrapper.unmount()
  })

  it('adds is-visible class to target element when it intersects', async () => {
    const TestComponent = defineComponent({
      setup() {
        const container = ref(null)
        useReveal(container)
        return { container }
      },
      render() {
        return h('div', { ref: 'container' }, [
          h('div', { class: 'reveal', 'data-testid': 'target' }),
        ])
      },
    })

    const wrapper = mount(TestComponent, { attachTo: document.body })
    await wrapper.vm.$nextTick()

    // Grab the actual DOM element being observed
    const targetEl = wrapper.find('[data-testid="target"]').element

    // Simulate intersection observer firing with isIntersecting = true
    capturedCallback([{ isIntersecting: true, target: targetEl }])

    expect(targetEl.classList.contains('is-visible')).toBe(true)

    wrapper.unmount()
  })

  it('calls unobserve on the target after it intersects', async () => {
    const TestComponent = defineComponent({
      setup() {
        const container = ref(null)
        useReveal(container)
        return { container }
      },
      render() {
        return h('div', { ref: 'container' }, [
          h('div', { class: 'reveal', 'data-testid': 'target' }),
        ])
      },
    })

    const wrapper = mount(TestComponent, { attachTo: document.body })
    await wrapper.vm.$nextTick()

    const targetEl = wrapper.find('[data-testid="target"]').element

    capturedCallback([{ isIntersecting: true, target: targetEl }])

    expect(mockUnobserve).toHaveBeenCalledWith(targetEl)

    wrapper.unmount()
  })

  it('does NOT add is-visible or call unobserve when isIntersecting is false', async () => {
    const TestComponent = defineComponent({
      setup() {
        const container = ref(null)
        useReveal(container)
        return { container }
      },
      render() {
        return h('div', { ref: 'container' }, [
          h('div', { class: 'reveal', 'data-testid': 'target' }),
        ])
      },
    })

    const wrapper = mount(TestComponent, { attachTo: document.body })
    await wrapper.vm.$nextTick()

    const targetEl = wrapper.find('[data-testid="target"]').element

    capturedCallback([{ isIntersecting: false, target: targetEl }])

    expect(targetEl.classList.contains('is-visible')).toBe(false)
    expect(mockUnobserve).not.toHaveBeenCalled()

    wrapper.unmount()
  })

  it('disconnects observer when component is unmounted', async () => {
    const TestComponent = defineComponent({
      setup() {
        const container = ref(null)
        useReveal(container)
        return { container }
      },
      render() {
        return h('div', { ref: 'container' }, [
          h('div', { class: 'reveal' }),
        ])
      },
    })

    const wrapper = mount(TestComponent, { attachTo: document.body })
    await wrapper.vm.$nextTick()

    expect(mockDisconnect).not.toHaveBeenCalled()

    wrapper.unmount()

    expect(mockDisconnect).toHaveBeenCalledOnce()
  })

  it('observes each .reveal element found inside the container', async () => {
    const TestComponent = defineComponent({
      setup() {
        const container = ref(null)
        useReveal(container)
        return { container }
      },
      render() {
        return h('div', { ref: 'container' }, [
          h('div', { class: 'reveal' }),
          h('div', { class: 'reveal' }),
          h('div', { class: 'reveal' }),
        ])
      },
    })

    const wrapper = mount(TestComponent, { attachTo: document.body })
    await wrapper.vm.$nextTick()

    // All three elements should have been passed to observe()
    expect(mockObserve).toHaveBeenCalledTimes(3)

    wrapper.unmount()
  })
})

// ─── useMagnetic tests ────────────────────────────────────────────────────────

describe('useMagnetic', () => {
  let el

  beforeEach(() => {
    el = document.createElement('div')
    // Give the element a bounding box so getBoundingClientRect returns real-ish values
    el.getBoundingClientRect = vi.fn(() => ({
      left: 100,
      top: 100,
      width: 200,
      height: 100,
      right: 300,
      bottom: 200,
    }))
    document.body.appendChild(el)
  })

  afterEach(() => {
    document.body.removeChild(el)
    vi.restoreAllMocks()
  })

  it('applies translate() transform on mousemove', () => {
    useMagnetic(el)

    // Centre of element: cx = 100 + 200/2 = 200, cy = 100 + 100/2 = 150
    const mousemove = new MouseEvent('mousemove', { clientX: 250, clientY: 200, bubbles: true })
    el.dispatchEvent(mousemove)

    // dx = (250 - 200) * 0.3 = 15, dy = (200 - 150) * 0.3 = 15
    expect(el.style.transform).toBe('translate(15px, 15px)')
  })

  it('clears transform on mouseleave', () => {
    useMagnetic(el)

    // First fire mousemove so transform is set
    el.dispatchEvent(new MouseEvent('mousemove', { clientX: 250, clientY: 200, bubbles: true }))
    expect(el.style.transform).not.toBe('')

    // Now fire mouseleave
    el.dispatchEvent(new MouseEvent('mouseleave', { bubbles: true }))
    expect(el.style.transform).toBe('')
  })

  it('transform reflects strength parameter', () => {
    useMagnetic(el, 0.5)

    // cx = 200, cy = 150; clientX=260, clientY=210
    // dx = (260 - 200) * 0.5 = 30, dy = (210 - 150) * 0.5 = 30
    el.dispatchEvent(new MouseEvent('mousemove', { clientX: 260, clientY: 210, bubbles: true }))

    expect(el.style.transform).toBe('translate(30px, 30px)')
  })

  it('cleanup function removes mousemove listener', () => {
    const cleanup = useMagnetic(el)
    cleanup()

    el.dispatchEvent(new MouseEvent('mousemove', { clientX: 250, clientY: 200, bubbles: true }))

    // After cleanup the transform should not be updated
    expect(el.style.transform).toBe('')
  })

  it('cleanup function removes mouseleave listener', () => {
    const cleanup = useMagnetic(el)

    // Set a transform by firing mousemove BEFORE cleanup
    el.dispatchEvent(new MouseEvent('mousemove', { clientX: 250, clientY: 200, bubbles: true }))
    const transformAfterMove = el.style.transform
    expect(transformAfterMove).not.toBe('')

    // Remove listeners
    cleanup()

    // Manually set transform to simulate a non-empty state
    el.style.transform = 'translate(10px, 10px)'

    // mouseleave should no longer clear it
    el.dispatchEvent(new MouseEvent('mouseleave', { bubbles: true }))
    expect(el.style.transform).toBe('translate(10px, 10px)')
  })
})

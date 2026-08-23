import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import GlobalSearchOverlay from '@/components/GlobalSearchOverlay.vue'

vi.mock('@/services/api', () => ({
  default: { get: vi.fn() },
}))
import api from '@/services/api'

const movies = [
  { id: 10, tenPhim: 'Avatar 2', trangThai: 'dang_chieu' },
]
const raps = [
  { id: 2, tenRap: 'Lotte Cinema Thủ Đức', diaChi: 'Landmark 81', thanhPho: 'TP.HCM' },
]

describe('GlobalSearchOverlay', () => {
  let router
  let pinia

  beforeEach(async () => {
    vi.clearAllMocks()
    pinia = createPinia()
    setActivePinia(pinia)
    const { useMovieStore } = await import('@/stores/movieStore')
    vi.spyOn(useMovieStore(), 'searchMovies').mockResolvedValue(movies)
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/phim/:id', component: { template: '<div/>' } },
        { path: '/rap/:id', component: { template: '<div/>' } },
      ],
    })
    api.get.mockResolvedValue({ data: raps })
  })

  function mountOverlay() {
    return mount(GlobalSearchOverlay, {
      global: {
        plugins: [pinia, router],
        stubs: { Teleport: true },
      },
    })
  }

  async function openAndType(wrapper, term) {
    await wrapper.vm.open()
    await flushPromises()
    const input = wrapper.find('.gs-input')
    await input.setValue(term)
    // debounce 300ms
    await new Promise(r => setTimeout(r, 400))
    await flushPromises()
  }

  it('opens and closes via exposed API', async () => {
    const wrapper = mountOverlay()
    expect(wrapper.find('.gs-overlay').exists()).toBe(false)
    wrapper.vm.open()
    await flushPromises()
    expect(wrapper.find('.gs-overlay').exists()).toBe(true)
    expect(wrapper.find('.gs-input').exists()).toBe(true)
    wrapper.vm.close()
    await flushPromises()
    expect(wrapper.find('.gs-overlay').exists()).toBe(false)
  })

  it('searches both movies and cinemas with section labels', async () => {
    const wrapper = mountOverlay()
    await openAndType(wrapper, 'lotte')

    const items = wrapper.findAll('.gs-item')
    const labels = wrapper.findAll('.gs-section-label').map(n => n.text())
    expect(labels.some(l => l.includes('Phim'))).toBe(true)
    expect(labels.some(l => l.includes('Rạp'))).toBe(true)
    expect(items.length).toBeGreaterThanOrEqual(2)
    expect(api.get).toHaveBeenCalledWith('/rap-chieu/search', { params: { q: 'lotte' } })
  })

  it('navigates to /rap/:id on cinema click', async () => {
    const wrapper = mountOverlay()
    await openAndType(wrapper, 'lotte')
    const rapItem = wrapper.findAll('.gs-item').find(n => n.text().includes('Lotte'))
    expect(rapItem).toBeTruthy()
    await rapItem.trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.fullPath).toBe('/rap/2')
  })

  it('shows empty state for unknown query', async () => {
    const wrapper = mountOverlay()
    const { useMovieStore } = await import('@/stores/movieStore')
    useMovieStore().searchMovies.mockResolvedValue([])
    api.get.mockResolvedValue({ data: [] })

    await openAndType(wrapper, 'zzzzz')
    expect(wrapper.find('.gs-empty').exists()).toBe(true)
  })
})

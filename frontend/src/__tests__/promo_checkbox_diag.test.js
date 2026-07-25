/**
 * Vitest unit test that replicates the exact PromoPage checkbox behavior.
 * This runs in jsdom — same as a real browser DOM.
 * 
 * Tests: given form.phimIds = [1, 2] and allMovies with ids [1,2,3,...],
 * do the checkboxes with :value="m.id" v-model="form.phimIds" render as checked?
 */
import { describe, it, expect } from 'vitest'
import { ref, nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'

// Minimal component that replicates the exact PromoPage checkbox pattern
const CheckboxTest = defineComponent({
  setup() {
    const form = ref({
      phimIds: []
    })
    const allMovies = ref([])
    const showModal = ref(false)

    return { form, allMovies, showModal }
  },
  template: `
    <div>
      <div v-if="showModal" class="modal">
        <div v-for="m in allMovies" :key="m.id" class="movie-row">
          <input type="checkbox" :value="m.id" v-model="form.phimIds" :data-movie-id="m.id" />
          <span>{{ m.tenPhim }}</span>
        </div>
        <div class="debug">phimIds={{ JSON.stringify(form.phimIds) }}</div>
      </div>
    </div>
  `
})

describe('Promo edit modal checkbox binding', () => {
  
  it('should check boxes when form.phimIds contains matching IDs (numbers)', async () => {
    const wrapper = mount(CheckboxTest)
    
    // Simulate openEdit: set form.phimIds THEN set allMovies THEN show modal
    wrapper.vm.form = { phimIds: [1, 2] }
    wrapper.vm.allMovies = [
      { id: 1, tenPhim: 'Movie 1' },
      { id: 2, tenPhim: 'Movie 2' },
      { id: 3, tenPhim: 'Movie 3' },
    ]
    wrapper.vm.showModal = true
    await nextTick()
    
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    expect(checkboxes).toHaveLength(3)
    
    console.log('Test 1: form.phimIds = [1, 2], allMovies ids = [1, 2, 3]')
    for (const cb of checkboxes) {
      const id = Number(cb.attributes('data-movie-id'))
      const isChecked = cb.element.checked
      console.log(`  Movie id=${id}: checked=${isChecked}`)
    }
    
    expect(checkboxes[0].element.checked).toBe(true)  // id=1 should be checked
    expect(checkboxes[1].element.checked).toBe(true)  // id=2 should be checked
    expect(checkboxes[2].element.checked).toBe(false) // id=3 should NOT be checked
  })

  it('should check boxes after simulated save-reload cycle', async () => {
    const wrapper = mount(CheckboxTest)
    
    // Step 1: Simulate the km object from API (like what load() returns)
    const km = {
      id: 2,
      maKhuyenMai: 'POLY10',
      phims: [
        { id: 2, tenPhim: 'Mắt Biếc' }
      ]
    }
    
    // Step 2: Simulate openEdit(km) — exactly as the real code does
    const phimIds = km.phims?.map(p => p.id) || []
    wrapper.vm.form = { phimIds }
    
    // Step 3: Simulate loadMovies() 
    wrapper.vm.allMovies = [
      { id: 1, tenPhim: 'Đệ Nhất Pháo Thủ' },
      { id: 2, tenPhim: 'Mắt Biếc' },
      { id: 3, tenPhim: 'Nhà Bà Nữ' },
    ]
    
    // Step 4: Show modal
    wrapper.vm.showModal = true
    await nextTick()
    
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    console.log('\nTest 2: km.phims=[{id:2}], form.phimIds =', JSON.stringify(wrapper.vm.form.phimIds))
    for (const cb of checkboxes) {
      const id = Number(cb.attributes('data-movie-id'))
      console.log(`  Movie id=${id}: checked=${cb.element.checked}`)
    }
    
    expect(checkboxes[0].element.checked).toBe(false)  // id=1
    expect(checkboxes[1].element.checked).toBe(true)   // id=2 should be checked!
    expect(checkboxes[2].element.checked).toBe(false)  // id=3
  })

  it('should handle the exact sequence: form set → await loadMovies → showModal', async () => {
    const wrapper = mount(CheckboxTest)
    
    // Simulate: form is set first (synchronous)
    wrapper.vm.form = { phimIds: [1, 3] }
    await nextTick()  // Let Vue react (but modal is hidden, nothing visible)
    
    // Simulate: loadMovies() resolves (async, but we simulate the result)
    wrapper.vm.allMovies = [
      { id: 1, tenPhim: 'Movie A' },
      { id: 2, tenPhim: 'Movie B' },
      { id: 3, tenPhim: 'Movie C' },
    ]
    await nextTick()  // Let Vue react (still hidden)
    
    // NOW show the modal
    wrapper.vm.showModal = true
    await nextTick()
    
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    console.log('\nTest 3: Exact timing simulation. form.phimIds =', JSON.stringify(wrapper.vm.form.phimIds))
    for (const cb of checkboxes) {
      const id = Number(cb.attributes('data-movie-id'))
      console.log(`  Movie id=${id}: checked=${cb.element.checked}`)
    }
    
    expect(checkboxes[0].element.checked).toBe(true)   // id=1 ✓
    expect(checkboxes[1].element.checked).toBe(false)  // id=2 ✗
    expect(checkboxes[2].element.checked).toBe(true)   // id=3 ✓
  })

  it('BUG SCENARIO: form.phimIds set AFTER allMovies but BEFORE showModal', async () => {
    const wrapper = mount(CheckboxTest)
    
    // What if allMovies was already loaded from a previous open?
    wrapper.vm.allMovies = [
      { id: 1, tenPhim: 'Movie A' },
      { id: 2, tenPhim: 'Movie B' },
    ]
    wrapper.vm.showModal = false
    wrapper.vm.form = { phimIds: [] }
    await nextTick()
    
    // Now openEdit is called:
    // 1. Set form with phimIds
    wrapper.vm.form = { phimIds: [2] }
    // 2. loadMovies (re-assigns allMovies)
    wrapper.vm.allMovies = [
      { id: 1, tenPhim: 'Movie A' },
      { id: 2, tenPhim: 'Movie B' },
    ]
    // 3. showModal
    wrapper.vm.showModal = true
    await nextTick()
    
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    console.log('\nTest 4 (Bug scenario): form.phimIds =', JSON.stringify(wrapper.vm.form.phimIds))
    for (const cb of checkboxes) {
      const id = Number(cb.attributes('data-movie-id'))
      console.log(`  Movie id=${id}: checked=${cb.element.checked}`)
    }
    
    expect(checkboxes[0].element.checked).toBe(false) // id=1
    expect(checkboxes[1].element.checked).toBe(true)  // id=2 should be checked!
  })

  it('BUG SCENARIO 2: Reopen modal (close then reopen with different data)', async () => {
    const wrapper = mount(CheckboxTest)
    
    // First open: show modal with phimIds=[1]
    wrapper.vm.form = { phimIds: [1] }
    wrapper.vm.allMovies = [
      { id: 1, tenPhim: 'Movie A' },
      { id: 2, tenPhim: 'Movie B' },
    ]
    wrapper.vm.showModal = true
    await nextTick()
    
    let cbs = wrapper.findAll('input[type="checkbox"]')
    console.log('\nTest 5a (first open): form.phimIds =', JSON.stringify(wrapper.vm.form.phimIds))
    for (const cb of cbs) {
      console.log(`  Movie id=${cb.attributes('data-movie-id')}: checked=${cb.element.checked}`)
    }
    expect(cbs[0].element.checked).toBe(true)
    
    // Close modal
    wrapper.vm.showModal = false
    await nextTick()
    
    // Reopen with different data: phimIds=[2]
    wrapper.vm.form = { phimIds: [2] }
    wrapper.vm.allMovies = [
      { id: 1, tenPhim: 'Movie A' },
      { id: 2, tenPhim: 'Movie B' },
    ]
    wrapper.vm.showModal = true
    await nextTick()
    
    cbs = wrapper.findAll('input[type="checkbox"]')
    console.log('Test 5b (reopen): form.phimIds =', JSON.stringify(wrapper.vm.form.phimIds))
    for (const cb of cbs) {
      console.log(`  Movie id=${cb.attributes('data-movie-id')}: checked=${cb.element.checked}`)
    }
    expect(cbs[0].element.checked).toBe(false) // id=1 should NOT be checked
    expect(cbs[1].element.checked).toBe(true)  // id=2 should be checked
  })
})

<template>
  <div class="settings-page">
    <transition name="toast"><div v-if="toast.show" :class="['toast',`toast--${toast.type}`]">{{ toast.msg }}</div></transition>

    <!-- Tabs -->
    <div class="tab-bar">
      <button :class="['tab', tab==='banners'?'tab--active':'']" @click="tab='banners'">Banners</button>
      <button :class="['tab', tab==='products'?'tab--active':'']" @click="tab='products'">Sản phẩm</button>
      <button :class="['tab', tab==='gioi_thieu'?'tab--active':'']" @click="tab='gioi_thieu'; loadGioiThieu()">Giới Thiệu</button>
    </div>

    <!-- ═══════════════ BANNERS TAB ═══════════════ -->
    <div v-if="tab==='banners'">
      <div class="toolbar">
        <h3 class="section-title">Quản lý Banner</h3>
        <button class="btn-primary" @click="openBannerAdd">+ Thêm banner</button>
      </div>

      <div class="card table-card">
        <div v-if="loadingB" class="state-center"><div class="spinner"></div></div>
        <div v-else-if="banners.length===0" class="state-center empty-text">Chưa có banner</div>
        <div v-else class="banner-list">
          <div v-if="filteredBanners.length === 0" class="state-center empty-text">Không tìm thấy banner phù hợp</div>
          <div v-for="b in filteredBanners" :key="b.id" class="banner-row" :data-row-id="b.id">
            <div class="banner-preview">
              <img v-if="b.hinhAnh" :src="b.hinhAnh" :alt="b.tieuDe" class="banner-thumb" @error="e=>e.target.style.opacity='.3'" />
              <div v-else class="banner-no-img">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="20" height="20"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              </div>
            </div>
            <div class="banner-info">
              <p class="banner-title">{{ b.tieuDe }}</p>
              <p class="banner-meta">Thứ tự: {{ b.thuTu }} &nbsp;|&nbsp; {{ fmtDate(b.ngayBatDau) }} → {{ fmtDate(b.ngayKetThuc) }}</p>
              <p class="banner-url">
                <span v-if="b.loaiBanner === 'Phim'">🎬 Phim #{{ b.phimId }}</span>
                <span v-else-if="b.loaiBanner === 'Khac'">🖼️ Banner đơn thuần</span>
                <span v-else style="opacity:.5">—</span>
              </p>
            </div>
            <div class="banner-actions">
              <button :class="['toggle-btn', b.dangHoatDong?'toggle-btn--on':'toggle-btn--off']" @click="toggleBanner(b)">
                {{ b.dangHoatDong ? '✓ Bật' : '✗ Tắt' }}
              </button>
              <button class="btn-icon btn-edit" @click="openBannerEdit(b)" title="Sửa">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
              <button class="btn-icon btn-del" @click="delBanner(b)" title="Xóa">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Banner modal -->
      <div v-if="bannerModal" class="modal-overlay" @click.self="bannerModal=false">
        <div class="modal">
          <div class="modal-head">
            <h2>{{ editingBanner ? 'Sửa banner' : 'Thêm banner' }}</h2>
            <button class="modal-close" @click="bannerModal=false">✕</button>
          </div>
          <div class="form-grid">
            <div class="field field-full"><label>Tiêu đề *</label><input v-model="bForm.tieuDe" placeholder="Tiêu đề banner"/></div>
            <div class="field field-full"><label>URL hình ảnh *</label><input v-model="bForm.hinhAnh" placeholder="https://..."/>
              <img v-if="bForm.hinhAnh" :src="bForm.hinhAnh" class="preview-img" @error="e=>e.target.style.display='none'" />
            </div>

            <!-- Loại banner — segmented control -->
            <div class="field field-full">
              <label>Loại banner *</label>
              <div class="banner-type-row">
                <button type="button"
                  :class="['banner-type-btn', bForm.loaiBanner==='Phim' ? 'banner-type-btn--active' : '']"
                  @click="setBannerType('Phim')">🎬 Phim</button>
                <button type="button"
                  :class="['banner-type-btn', bForm.loaiBanner==='Khac' ? 'banner-type-btn--active' : '']"
                  @click="setBannerType('Khac')">🖼️ Khác</button>
              </div>
            </div>

            <!-- Dependent combobox -->
            <div class="field field-full" v-if="bForm.loaiBanner === 'Phim'">
              <label>Chọn phim *</label>
              <select v-model="bForm.phimId">
                <option :value="null">— Chọn phim —</option>
                <option v-for="m in movies" :key="m.id" :value="m.id">{{ m.tenPhim }}</option>
              </select>
            </div>
            <p v-if="bForm.loaiBanner === 'Khac'" class="field-note">Banner đơn thuần — hiển thị hình ảnh quảng cáo không có nút đặt vé.</p>

            <div class="field"><label>Ngày bắt đầu</label><input v-model="bForm.ngayBatDau" type="date"/></div>
            <div class="field"><label>Ngày kết thúc</label><input v-model="bForm.ngayKetThuc" type="date"/></div>
            <div class="field"><label>Thứ tự hiển thị</label><input v-model.number="bForm.thuTu" type="number" min="0"/></div>
            <div class="field"><label>Mô tả</label><input v-model="bForm.moTa" placeholder="Mô tả phụ"/></div>
          </div>
          <p v-if="bFormErr" class="form-err">{{ bFormErr }}</p>
          <div class="modal-footer">
            <button class="btn-ghost" @click="bannerModal=false">Hủy</button>
            <button class="btn-primary" :disabled="bSaving" @click="saveBanner">{{ bSaving?'Đang lưu...':'Lưu' }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══════════════ PRODUCTS TAB ═══════════════ -->
    <div v-if="tab==='products'">
      <div class="toolbar">
        <h3 class="section-title">Quản lý Sản phẩm</h3>
        <button class="btn-primary" @click="openProdAdd">+ Thêm sản phẩm</button>
      </div>

      <div class="card table-card">
        <div v-if="loadingP" class="state-center"><div class="spinner"></div></div>
        <div v-else-if="products.length===0" class="state-center empty-text">Chưa có sản phẩm</div>
        <div v-else class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Tên</th><th>Loại</th><th>Giá</th><th>Tồn kho</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
              <tr v-if="filteredProducts.length === 0"><td colspan="6" class="empty-text">Không tìm thấy sản phẩm phù hợp</td></tr>
              <tr v-for="p in filteredProducts" :key="p.id" :data-row-id="p.id">
                <td class="td-name">
                  <div class="prod-name-wrap">
                    <img v-if="p.anhUrl" :src="p.anhUrl" class="prod-thumb" :alt="p.tenSanPham" @error="e=>e.target.style.opacity='.2'" />
                    <div v-else class="prod-no-img">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="16" height="16"><path d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2 9M17 13l2 9M9 21h6"/></svg>
                    </div>
                    {{ p.tenSanPham }}
                  </div>
                </td>
                <td><span :class="['tbadge', typeClass(p.loaiSanPham)]">{{ p.loaiSanPham }}</span></td>
                <td class="td-price">{{ fmtPrice(p.gia) }}</td>
                <td>{{ p.tonKho }}</td>
                <td><span :class="['sbadge', p.dangHoatDong?'sbadge--green':'sbadge--gray']">{{ p.dangHoatDong?'Bán':'Ngừng' }}</span></td>
                <td>
                  <div class="act-btns">
                    <button class="btn-icon btn-edit" @click="openProdEdit(p)" title="Sửa">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    </button>
                    <button class="btn-icon btn-del" @click="delProd(p)" title="Xóa">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Product modal -->
      <div v-if="prodModal" class="modal-overlay" @click.self="prodModal=false">
        <div class="modal">
          <div class="modal-head">
            <h2>{{ editingProd ? 'Sửa sản phẩm' : 'Thêm sản phẩm' }}</h2>
            <button class="modal-close" @click="prodModal=false">✕</button>
          </div>
          <div class="form-grid">
            <div class="field field-full"><label>Tên sản phẩm *</label><input v-model="pForm.tenSanPham" placeholder="Tên sản phẩm"/></div>
            <div class="field"><label>Loại sản phẩm *</label>
              <select v-model="pForm.loaiSanPham">
                <option value="combo">Combo</option>
                <option value="food">Đồ ăn</option>
                <option value="drink">Đồ uống</option>
              </select>
            </div>
            <div class="field"><label>Giá (VND) *</label><input v-model.number="pForm.gia" type="number" min="0" step="1000"/></div>
            <div class="field"><label>Tồn kho</label><input v-model.number="pForm.tonKho" type="number" min="0"/></div>
            <div class="field field-full"><label>URL ảnh</label><input v-model="pForm.anhUrl" placeholder="https://..."/>
              <img v-if="pForm.anhUrl" :src="pForm.anhUrl" class="preview-img" @error="e=>e.target.style.display='none'" />
            </div>
            <div class="field field-full"><label>Mô tả</label><input v-model="pForm.moTa" placeholder="Mô tả ngắn"/></div>
            <div class="field"><label>Đang bán</label>
              <label class="switch-label">
                <input type="checkbox" v-model="pForm.dangHoatDong" class="switch-cb" />
                <span class="switch-slider"></span>
                <span>{{ pForm.dangHoatDong ? 'Có' : 'Không' }}</span>
              </label>
            </div>
          </div>
          <p v-if="pFormErr" class="form-err">{{ pFormErr }}</p>
          <div class="modal-footer">
            <button class="btn-ghost" @click="prodModal=false">Hủy</button>
            <button class="btn-primary" :disabled="pSaving" @click="saveProd">{{ pSaving?'Đang lưu...':'Lưu' }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══════════════ GIỚI THIỆU TAB ═══════════════ -->
    <div v-if="tab==='gioi_thieu'">
      <div class="toolbar">
        <h3 class="section-title">Nội dung trang Giới Thiệu</h3>
      </div>
      <div class="card" style="padding:24px">
        <div v-if="gtLoading" class="state-center"><div class="spinner"></div></div>
        <div v-else>
          <div class="field">
            <label>Hình nền (URL)</label>
            <input v-model="gtForm.hinhAnhUrl" placeholder="https://... (để trống = nền tối mặc định)" />
            <div v-if="gtForm.hinhAnhUrl" style="margin-top:8px">
              <img :src="gtForm.hinhAnhUrl" style="max-height:120px;border-radius:8px;object-fit:cover;width:100%;" @error="e=>e.target.style.display='none'" />
            </div>
          </div>
          <div class="field">
            <label>Tiêu đề</label>
            <input v-model="gtForm.tieuDe" placeholder="VD: HỆ THỐNG CỤM RẠP POLYCINEMA" />
          </div>
          <div class="field">
            <label>Nội dung (phân đoạn bằng dòng trống)</label>
            <textarea v-model="gtForm.noiDung" rows="8" placeholder="Nhập nội dung giới thiệu..."></textarea>
          </div>
          <p v-if="gtErr" class="form-err">{{ gtErr }}</p>
          <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:16px">
            <button class="btn-primary" :disabled="gtSaving" @click="saveGioiThieu">{{ gtSaving ? 'Đang lưu...' : 'Lưu nội dung' }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch, nextTick } from 'vue'
import api from '@/services/api'
import { useAdminShellStore } from '@/stores/adminShellStore'
import { flashRow } from '@/utils/flashRow'
import { fmtDate } from '@/utils/dateFmt'

const shell = useAdminShellStore()

const toast = reactive({ show:false, msg:'', type:'success' })
let toastTimer = null
function showToast(msg, type='error') {
  clearTimeout(toastTimer); toast.msg=msg; toast.type=type; toast.show=true
  toastTimer = setTimeout(() => toast.show=false, 3500)
}

const tab = ref('banners')

// ── BANNERS ──────────────────────────────────────────────────
const banners      = ref([])
const loadingB     = ref(false)
const bannerModal  = ref(false)
const editingBanner = ref(null)
const bSaving      = ref(false)
const bFormErr     = ref('')

// Movies for Phim-type banner picker
const movies = ref([])
async function loadMovies() {
  if (movies.value.length > 0) return
  try {
    const r = await api.get('/admin/phim')
    movies.value = (r.data || []).filter(m => !m.isDeleted)
  } catch {}
}

const blankBanner = () => ({
  tieuDe: '', hinhAnh: '', moTa: '', thuTu: 0,
  ngayBatDau: '', ngayKetThuc: '', dangHoatDong: true,
  loaiBanner: null, phimId: null,
})
const bForm = ref(blankBanner())

function setBannerType(type) {
  bForm.value.loaiBanner = type
  bForm.value.phimId = null
  if (type === 'Phim') loadMovies()
}

function openBannerAdd() {
  editingBanner.value = null
  bForm.value = blankBanner()
  bFormErr.value = ''
  bannerModal.value = true
}

function openBannerEdit(b) {
  editingBanner.value = b
  bForm.value = {
    tieuDe: b.tieuDe || '', hinhAnh: b.hinhAnh || '', moTa: b.moTa || '',
    thuTu: b.thuTu ?? 0, ngayBatDau: b.ngayBatDau || '', ngayKetThuc: b.ngayKetThuc || '',
    dangHoatDong: b.dangHoatDong ?? true,
    loaiBanner: b.loaiBanner || null,
    phimId: b.phimId ?? null,
  }
  bFormErr.value = ''
  // Pre-load the relevant picker list
  if (b.loaiBanner === 'Phim') loadMovies()
  bannerModal.value = true
}

async function saveBanner() {
  if (!bForm.value.tieuDe.trim() || !bForm.value.hinhAnh.trim()) {
    bFormErr.value = 'Tiêu đề và URL hình ảnh là bắt buộc'; return
  }
  if (!bForm.value.loaiBanner) {
    bFormErr.value = 'Vui lòng chọn loại banner'; return
  }
  bSaving.value = true; bFormErr.value = ''
  try {
    const payload = { ...bForm.value }
    if (editingBanner.value) { await api.put(`/admin/banner/${editingBanner.value.id}`, payload) }
    else                     { await api.post('/admin/banner', payload) }
    showToast('Đã lưu banner', 'success')
    bannerModal.value = false
    await loadBanners()
  } catch(e) { bFormErr.value = e.response?.data?.message || 'Lỗi lưu banner' }
  finally { bSaving.value = false }
}

async function toggleBanner(b) {
  try {
    await api.put(`/admin/banner/${b.id}`, { dangHoatDong:!b.dangHoatDong })
    b.dangHoatDong = !b.dangHoatDong
    showToast(b.dangHoatDong?'Đã bật banner':'Đã tắt banner', 'success')
  } catch(e) { showToast('Lỗi thay đổi trạng thái') }
}

async function delBanner(b) {
  if (!confirm(`Xóa banner "${b.tieuDe}"?`)) return
  try {
    await api.delete(`/admin/banner/${b.id}`)
    showToast('Đã xóa banner', 'success')
    await loadBanners()
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi xóa banner') }
}

async function loadBanners() {
  loadingB.value=true
  try { const r = await api.get('/admin/banner'); banners.value = r.data||[] }
  catch(e) { showToast('Không tải được banner') }
  finally { loadingB.value=false }
}

// ── PRODUCTS ─────────────────────────────────────────────────
const products     = ref([])
const loadingP     = ref(false)
const prodModal    = ref(false)
const editingProd  = ref(null)
const pSaving      = ref(false)
const pFormErr     = ref('')

const blankProd = () => ({ tenSanPham:'', loaiSanPham:'combo', gia:50000, tonKho:100, anhUrl:'', moTa:'', dangHoatDong:true })
const pForm = ref(blankProd())

function openProdAdd()   { editingProd.value=null; pForm.value=blankProd(); pFormErr.value=''; prodModal.value=true }
function openProdEdit(p) { editingProd.value=p; pForm.value={...p}; pFormErr.value=''; prodModal.value=true }

function typeClass(t) {
  if (t==='combo') return 'tbadge--orange'
  if (t==='food')  return 'tbadge--green'
  return 'tbadge--blue'
}
function fmtPrice(v) {
  return new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(v||0)
}

async function saveProd() {
  if (!pForm.value.tenSanPham.trim()) { pFormErr.value='Tên sản phẩm không được để trống'; return }
  pSaving.value=true; pFormErr.value=''
  try {
    if (editingProd.value) { await api.put(`/admin/san-pham/${editingProd.value.id}`, pForm.value) }
    else                   { await api.post('/admin/san-pham', pForm.value) }
    showToast('Đã lưu sản phẩm', 'success')
    prodModal.value=false
    await loadProducts()
  } catch(e) { pFormErr.value = e.response?.data?.message || 'Lỗi lưu sản phẩm' }
  finally { pSaving.value=false }
}

async function delProd(p) {
  if (!confirm(`Xóa sản phẩm "${p.tenSanPham}"?`)) return
  try {
    await api.delete(`/admin/san-pham/${p.id}`)
    showToast('Đã xóa sản phẩm', 'success')
    await loadProducts()
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi xóa sản phẩm') }
}

async function loadProducts() {
  loadingP.value=true
  try { const r = await api.get('/admin/san-pham'); products.value = r.data||[] }
  catch(e) { showToast('Không tải được sản phẩm') }
  finally { loadingP.value=false }
}

// ── Command-palette targeting ─────────────────────────────────
// Declared AFTER banners/products refs to avoid TDZ (computed reads .value at registration)
const bannerSearch  = ref('')
const productSearch = ref('')
const flashQueued   = ref(false)

const filteredBanners = computed(() => {
  const q = bannerSearch.value.toLowerCase()
  if (!q) return banners.value
  return banners.value.filter(b =>
    b.tieuDe?.toLowerCase().includes(q) || b.moTa?.toLowerCase().includes(q)
  )
})

const filteredProducts = computed(() => {
  const q = productSearch.value.toLowerCase()
  if (!q) return products.value
  return products.value.filter(p =>
    p.tenSanPham?.toLowerCase().includes(q) || p.moTa?.toLowerCase().includes(q)
  )
})

function syncFromShell() {
  if (shell.searchTargetPage !== 'settings') return
  if (shell.settingsSubTab) tab.value = shell.settingsSubTab
  const q = shell.searchQuery
  if (q && tab.value === 'banners')  { bannerSearch.value = q; flashQueued.value = true }
  if (q && tab.value === 'products') { productSearch.value = q; flashQueued.value = true }
}

watch(() => shell.searchTick, syncFromShell)

watch(filteredBanners, (list) => {
  if (flashQueued.value && tab.value === 'banners' && list.length > 0) {
    flashQueued.value = false
    nextTick(() => flashRow(document.querySelector(`[data-row-id="${list[0].id}"]`)))
  }
})
watch(filteredProducts, (list) => {
  if (flashQueued.value && tab.value === 'products' && list.length > 0) {
    flashQueued.value = false
    nextTick(() => flashRow(document.querySelector(`[data-row-id="${list[0].id}"]`)))
  }
})

// load data when switching tabs
watch(tab, t => {
  if (t==='banners') loadBanners()
  else if (t==='products') loadProducts()
  else if (t==='gioi_thieu') loadGioiThieu()
})



onMounted(() => {
  syncFromShell()
  if (tab.value === 'banners') loadBanners()
})

// ── GIỚI THIỆU ──────────────────────────────────────────────────
const gtForm    = ref({ tieuDe: '', noiDung: '', hinhAnhUrl: '' })
const gtLoading = ref(false)
const gtSaving  = ref(false)
const gtErr     = ref('')

async function loadGioiThieu() {
  gtLoading.value = true
  try {
    const r = await api.get('/gioi-thieu')
    gtForm.value = {
      tieuDe:    r.data?.tieuDe    || '',
      noiDung:   r.data?.noiDung   || '',
      hinhAnhUrl: r.data?.hinhAnhUrl || '',
    }
  } catch { /* use empty defaults */ }
  finally { gtLoading.value = false }
}

async function saveGioiThieu() {
  gtSaving.value = true; gtErr.value = ''
  try {
    await api.put('/gioi-thieu', gtForm.value)
    showToast('Đã lưu nội dung Giới Thiệu', 'success')
  } catch(e) { gtErr.value = e.response?.data?.message || 'Lỗi lưu nội dung' }
  finally { gtSaving.value = false }
}
</script>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; }

/* ── Page root ── */
.settings-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--admin-bg);
  color: var(--admin-text);
}

/* ── Tab bar ── */
.tab-bar {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--admin-border);
  margin-bottom: 20px;
}
.tab {
  padding: 10px 20px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 600;
  color: var(--admin-text-muted);
  cursor: pointer;
  transition: color 150ms ease, border-color 150ms ease;
}
.tab:hover { color: var(--admin-text); }
.tab--active { color: var(--admin-accent); border-bottom-color: var(--admin-accent); }

/* ── Toolbar ── */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

/* ── Section title ── */
.section-title {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 600;
  color: var(--admin-accent);
  margin: 0;
  padding-left: 12px;
  border-left: 3px solid var(--admin-accent);
}

/* ── Buttons ── */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 40px;
  padding: 9px 18px;
  border-radius: 8px;
  border: none;
  background: var(--admin-accent);
  color: var(--admin-bg);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: filter 150ms ease;
}
.btn-primary:hover:not(:disabled) { filter: brightness(1.1); }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 40px;
  padding: 9px 18px;
  border-radius: 8px;
  border: 1px solid var(--admin-border);
  background: transparent;
  color: var(--admin-text);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 150ms ease, color 150ms ease;
}
.btn-ghost:hover { border-color: var(--admin-accent); color: var(--admin-accent); }

/* ── Card / table card ── */
.card, .table-card {
  background: var(--admin-surface);
  border: 1px solid var(--admin-border);
  border-radius: 12px;
  overflow: hidden;
}

/* ── Table ── */
.data-table { width: 100%; border-collapse: collapse; }
.table-scroll { overflow-x: auto; }
.data-table thead tr { background: var(--admin-bg); }
.data-table th {
  padding: 12px 16px;
  text-align: left;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--admin-text-muted);
  border-bottom: 2px solid var(--admin-border);
  white-space: nowrap;
}
.data-table tbody tr { border-bottom: 1px solid var(--admin-divider); transition: background 150ms ease; }
.data-table tbody tr:last-child { border-bottom: none; }
.data-table tbody tr:hover { background: var(--admin-surface-hover); }
.data-table td {
  padding: 14px 16px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--admin-text);
  vertical-align: middle;
}
.td-name { font-weight: 600; }
.td-price { font-weight: 700; color: var(--admin-accent); }

/* ── Action buttons ── */
.act-btns { display: flex; gap: 4px; align-items: center; }
.btn-icon {
  width: 32px; height: 32px;
  border: none;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: color 150ms ease, background 150ms ease;
}
.btn-edit:hover  { color: var(--admin-accent); background: rgba(255,255,255,0.10); }
.btn-del:hover   { color: #EF4444; background: rgba(239,68,68,0.1); }

/* ── Badges ── */
.sbadge, .tbadge {
  display: inline-flex;
  align-items: center;
  border-radius: 9999px;
  padding: 3px 12px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  min-width: fit-content;
}
.sbadge--green { background: rgba(16,185,129,0.15); color: #10B981; }
.sbadge--gray  { background: rgba(156,163,175,0.15); color: var(--admin-text-muted); }
.tbadge--orange { background: rgba(255,255,255,0.10); color: var(--admin-accent); }
.tbadge--green  { background: rgba(16,185,129,0.15); color: #10B981; }
.tbadge--blue   { background: rgba(255,255,255,0.10); color: var(--admin-accent); }

/* ── Toggle button ── */
.toggle-btn {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  min-width: fit-content;
  padding: 4px 12px;
  border: none;
  border-radius: 9999px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.toggle-btn--on  { background: rgba(16,185,129,0.15); color: #10B981; }
.toggle-btn--off { background: rgba(156,163,175,0.15); color: #9CA3AF; }

/* ── Banner list ── */
.banner-list { padding: 0; }
.banner-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--admin-divider);
  transition: background 150ms ease;
}
.banner-row:last-child { border-bottom: none; }
.banner-row:hover { background: var(--admin-surface-hover); }
.banner-preview {
  width: 80px; height: 50px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  background: var(--admin-surface-hover);
  border: 1px solid var(--admin-border);
}
.banner-thumb { width: 100%; height: 100%; object-fit: cover; }
.banner-no-img {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; color: var(--admin-text-muted);
}
.banner-info { flex: 1; min-width: 0; }
.banner-title { font-family: var(--font-ui, 'Inter', sans-serif); font-weight: 600; color: var(--admin-text); margin: 0 0 3px; font-size: 14px; }
.banner-meta, .banner-url {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 12px; color: var(--admin-text-muted); margin: 0;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.banner-actions { display: flex; align-items: center; gap: 6px; flex-shrink: 0; }

.banner-phim {
  display: flex; align-items: center; gap: 4px;
  font-size: 11px; color: #29bcea; margin: 2px 0 0;
  font-family: var(--font-ui, 'Inter', sans-serif);
}
.field-hint {
  font-size: 11px; color: var(--admin-text-muted); margin: 4px 0 0;
  font-family: var(--font-ui, 'Inter', sans-serif);
}
.field-optional {
  font-size: 10px; color: var(--admin-text-muted); font-weight: 400;
  text-transform: none; letter-spacing: 0; margin-left: 4px;
}

/* ── Product name wrap ── */
.prod-name-wrap { display: flex; align-items: center; gap: 8px; }
.prod-thumb {
  width: 36px; height: 36px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--admin-border);
  flex-shrink: 0;
}
.prod-no-img {
  width: 36px; height: 36px;
  background: var(--admin-surface-hover); border-radius: 6px;
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 16px; flex-shrink: 0;
}

/* ── Modal ── */
.modal-overlay {
  position: fixed;
  inset: 0; z-index: 200;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.65);
  backdrop-filter: blur(8px);
  padding: 20px;
}
.modal {
  width: min(560px, 100%);
  max-height: 90vh;
  overflow-y: auto;
  background: var(--admin-surface-hover);
  border: 1px solid var(--admin-border);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 24px 48px rgba(0,0,0,0.5);
  color: var(--admin-text);
}
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.modal-head h2 {
  font-family: var(--font-display, 'Playfair Display', serif);
  font-size: 18px;
  font-weight: 700;
  color: var(--admin-accent);
  margin: 0;
  padding-left: 12px;
  border-left: 3px solid var(--admin-accent);
}
.modal-close {
  width: 32px; height: 32px;
  background: var(--admin-surface-hover); border: none; border-radius: 50%;
  color: var(--admin-text-muted); cursor: pointer; font-size: 14px;
  display: grid; place-items: center;
  transition: color 150ms ease;
}
.modal-close:hover { color: var(--admin-text); }
.modal-footer { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }

/* ── Form grid ── */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.field-full { grid-column: 1 / -1; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 11px; font-weight: 600; color: var(--admin-text-muted);
  text-transform: uppercase; letter-spacing: 0.06em;
}
.field-note { font-size: 12px; color: var(--admin-text-muted); margin: 0; line-height: 1.5; }
.field input, .field select, .field textarea {
  min-height: 40px;
  padding: 9px 14px;
  border: 1px solid var(--admin-border);
  border-radius: 8px;
  background: var(--admin-surface);
  color: var(--admin-text);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  -webkit-appearance: none;
  appearance: none;
  transition: border-color 150ms ease;
}
.field input:focus, .field select:focus, .field textarea:focus {
  outline: none; border-color: var(--admin-accent);
  box-shadow: 0 0 0 2px rgba(255,255,255,0.15);
}
.field input::placeholder, .field textarea::placeholder { color: var(--admin-text-muted); }

/* Preview image in modal ── */
.preview-img {
  width: 80px; height: 50px;
  object-fit: cover;
  border-radius: 6px;
  margin-top: 6px;
  border: 1px solid var(--admin-border);
}
.form-err { color: #EF4444; font-size: 13px; margin-top: 4px; }

/* ── Switch toggle ── */
.switch-label { display: flex; align-items: center; gap: 10px; cursor: pointer; font-size: 14px; font-weight: 600; color: var(--admin-text); }
.switch-cb { display: none; }
.switch-slider {
  position: relative; width: 40px; height: 22px;
  background: var(--admin-border); border-radius: 11px;
  flex-shrink: 0; transition: background 200ms ease;
}
.switch-slider::after {
  content: ''; position: absolute;
  width: 16px; height: 16px; border-radius: 50%;
  background: var(--admin-bg); top: 3px; left: 3px;
  transition: transform 200ms ease;
}
.switch-cb:checked ~ .switch-slider { background: var(--admin-accent); }
.switch-cb:checked ~ .switch-slider::after { transform: translateX(18px); background: var(--admin-bg); }

/* ── Loading / empty ── */
.state-center { display: flex; justify-content: center; align-items: center; padding: 40px; }
.empty-text, .loading-text { text-align: center; padding: 24px; color: var(--admin-text-muted); font-size: 14px; }
.spinner {
  width: 32px; height: 32px;
  border: 3px solid var(--admin-border); border-top-color: var(--admin-accent);
  border-radius: 50%; animation: sp-spin 0.8s linear infinite;
}
@keyframes sp-spin { to { transform: rotate(360deg); } }

/* ── Palette target row flash ── */
.row-flash {
  animation: row-flash-pop 1.6s ease;
}
@keyframes row-flash-pop {
  0%, 100% { background-color: transparent; }
  20%, 60% { background-color: rgba(41, 188, 234, 0.18); }
}

/* ── Banner type segmented control ── */
.banner-type-row {
  display: flex; gap: 8px; flex-wrap: wrap;
}
.banner-type-btn {
  flex: 1; min-width: 90px; padding: 8px 12px;
  border: 1px solid var(--admin-border); border-radius: 8px;
  background: transparent; color: var(--admin-text-muted);
  font-family: var(--font-ui); font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 150ms;
}
.banner-type-btn:hover { border-color: var(--admin-accent); color: var(--admin-text); }
.banner-type-btn--active {
  border-color: var(--admin-accent);
  background: var(--admin-accent-muted);
  color: var(--admin-accent);
}
</style>

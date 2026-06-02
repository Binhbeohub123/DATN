<template>
  <div class="settings-page">
    <transition name="toast"><div v-if="toast.show" :class="['toast',`toast--${toast.type}`]">{{ toast.msg }}</div></transition>

    <!-- Tabs -->
    <div class="tab-bar">
      <button :class="['tab', tab==='banners'?'tab--active':'']" @click="tab='banners'">🖼️ Banners</button>
      <button :class="['tab', tab==='products'?'tab--active':'']" @click="tab='products'">🛍️ Sản phẩm</button>
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
          <div v-for="b in banners" :key="b.id" class="banner-row">
            <div class="banner-preview">
              <img v-if="b.hinhAnh" :src="b.hinhAnh" :alt="b.tieuDe" class="banner-thumb" @error="e=>e.target.style.opacity='.3'" />
              <div v-else class="banner-no-img">🖼️</div>
            </div>
            <div class="banner-info">
              <p class="banner-title">{{ b.tieuDe }}</p>
              <p class="banner-meta">Thứ tự: {{ b.thuTu }} &nbsp;|&nbsp; {{ b.ngayBatDau||'—' }} → {{ b.ngayKetThuc||'—' }}</p>
              <p class="banner-url">{{ b.linkUrl }}</p>
            </div>
            <div class="banner-actions">
              <button :class="['toggle-btn', b.dangHoatDong?'toggle-btn--on':'toggle-btn--off']" @click="toggleBanner(b)">
                {{ b.dangHoatDong ? '✓ Bật' : '✗ Tắt' }}
              </button>
              <button class="btn-icon btn-edit" @click="openBannerEdit(b)">✏️</button>
              <button class="btn-icon btn-del" @click="delBanner(b)">🗑️</button>
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
            <div class="field field-full"><label>Link khi click</label><input v-model="bForm.linkUrl" placeholder="/phim/1"/></div>
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
              <tr v-for="p in products" :key="p.id">
                <td class="td-name">
                  <div class="prod-name-wrap">
                    <img v-if="p.anhUrl" :src="p.anhUrl" class="prod-thumb" :alt="p.tenSanPham" @error="e=>e.target.style.opacity='.2'" />
                    <div v-else class="prod-no-img">🍿</div>
                    {{ p.tenSanPham }}
                  </div>
                </td>
                <td><span :class="['tbadge', typeClass(p.loaiSanPham)]">{{ p.loaiSanPham }}</span></td>
                <td class="td-price">{{ fmtPrice(p.gia) }}</td>
                <td>{{ p.tonKho }}</td>
                <td><span :class="['sbadge', p.dangHoatDong?'sbadge--green':'sbadge--gray']">{{ p.dangHoatDong?'Bán':'Ngừng' }}</span></td>
                <td>
                  <div class="act-btns">
                    <button class="btn-icon btn-edit" @click="openProdEdit(p)">✏️</button>
                    <button class="btn-icon btn-del" @click="delProd(p)">🗑️</button>
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
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import api from '@/services/api'

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

const blankBanner = () => ({ tieuDe:'', hinhAnh:'', linkUrl:'', ngayBatDau:'', ngayKetThuc:'', thuTu:0, moTa:'', dangHoatDong:true })
const bForm = ref(blankBanner())

function openBannerAdd()  { editingBanner.value=null; bForm.value=blankBanner(); bFormErr.value=''; bannerModal.value=true }
function openBannerEdit(b){ editingBanner.value=b; bForm.value={...b}; bFormErr.value=''; bannerModal.value=true }

async function saveBanner() {
  if (!bForm.value.tieuDe.trim() || !bForm.value.hinhAnh.trim()) { bFormErr.value='Tiêu đề và URL hình ảnh là bắt buộc'; return }
  bSaving.value=true; bFormErr.value=''
  try {
    if (editingBanner.value) { await api.put(`/admin/banner/${editingBanner.value.id}`, bForm.value) }
    else                     { await api.post('/admin/banner', bForm.value) }
    showToast('Đã lưu banner', 'success')
    bannerModal.value=false
    await loadBanners()
  } catch(e) { bFormErr.value = e.response?.data?.message || 'Lỗi lưu banner' }
  finally { bSaving.value=false }
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
  try { const r = await api.get('/banner'); banners.value = r.data||[] }
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
  try { const r = await api.get('/san-pham'); products.value = r.data||[] }
  catch(e) { showToast('Không tải được sản phẩm') }
  finally { loadingP.value=false }
}

// load data when switching tabs
watch(tab, t => { if (t==='banners') loadBanners(); else loadProducts() })
onMounted(() => { loadBanners() })
</script>

<style scoped>
.settings-page { display:flex; flex-direction:column; gap:20px; }
.toast{position:fixed;top:20px;right:20px;z-index:999;padding:12px 20px;border-radius:10px;font-size:13px;font-weight:700}
.toast--error{background:#7f1d1d;color:#fecaca}.toast--success{background:#14532d;color:#bbf7d0}
.toast-enter-active,.toast-leave-active{transition:opacity .3s}.toast-enter-from,.toast-leave-to{opacity:0}

/* tabs */
.tab-bar { display:flex; gap:4px; border-bottom:2px solid #e5e7eb; }
.tab { padding:10px 20px; background:none; border:none; font-size:14px; font-weight:700; color:#6b7280; cursor:pointer; border-bottom:3px solid transparent; margin-bottom:-2px; transition:all .2s; }
.tab:hover { color:#29bcea; }
.tab--active { color:#29bcea; border-bottom-color:#29bcea; }

.toolbar { display:flex; align-items:center; justify-content:space-between; }
.section-title { font-size:16px; font-weight:800; color:#111827; margin:0; }
.btn-primary { padding:9px 18px; background:#29bcea; color:white; border:none; border-radius:9px; font-weight:800; cursor:pointer; font-size:14px; transition:all .2s; }
.btn-primary:hover:not(:disabled){transform:translateY(-1px)} .btn-primary:disabled{opacity:.6;cursor:not-allowed}
.btn-ghost{padding:9px 18px;background:transparent;border:1px solid #e5e7eb;border-radius:9px;font-weight:700;cursor:pointer;font-size:14px}
.btn-ghost:hover{border-color:#29bcea;color:#29bcea}
.card{border-radius:18px;background:rgba(255,255,255,0.84);border:1px solid rgba(255,255,255,0.7);box-shadow:0 8px 24px rgba(15,23,42,0.07)}
.table-card{overflow:hidden}.table-scroll{overflow-x:auto}
.state-center{display:flex;justify-content:center;align-items:center;padding:40px}
.empty-text{color:#9ca3af;font-size:14px}
.spinner{width:36px;height:36px;border:4px solid rgba(255,107,0,.2);border-top-color:#29bcea;border-radius:50%;animation:spin .9s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}

/* banner list */
.banner-list { display:flex; flex-direction:column; gap:0; }
.banner-row { display:flex; align-items:center; gap:16px; padding:14px 18px; border-bottom:1px solid #f3f4f6; transition:background .15s; }
.banner-row:last-child { border-bottom:none; }
.banner-row:hover { background:#f7fcfe; }
.banner-preview { flex-shrink:0; width:140px; height:52px; border-radius:8px; overflow:hidden; background:#f3f4f6; }
.banner-thumb { width:100%; height:100%; object-fit:cover; }
.banner-no-img { width:100%; height:100%; display:flex; align-items:center; justify-content:center; font-size:24px; }
.banner-info { flex:1; min-width:0; }
.banner-title { font-size:14px; font-weight:700; color:#111827; margin:0 0 2px; }
.banner-meta  { font-size:11px; color:#9ca3af; margin:0 0 2px; }
.banner-url   { font-size:11px; color:#6b7280; margin:0; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; max-width:240px; }
.banner-actions { display:flex; gap:8px; align-items:center; flex-shrink:0; }
.toggle-btn{padding:4px 12px;border-radius:6px;font-size:12px;font-weight:800;cursor:pointer;border:none;transition:all .2s}
.toggle-btn--on{background:#dcfce7;color:#166534}.toggle-btn--on:hover{background:#bbf7d0}
.toggle-btn--off{background:#f3f4f6;color:#6b7280}.toggle-btn--off:hover{background:#e5e7eb}
.btn-icon{width:30px;height:30px;border:none;border-radius:7px;cursor:pointer;font-size:13px;transition:all .2s}
.btn-edit{background:#dbeafe}.btn-edit:hover{background:#bfdbfe}
.btn-del{background:#fee2e2}.btn-del:hover{background:#fecaca}

/* product table */
.data-table{width:100%;border-collapse:collapse;font-size:13px}
.data-table th{padding:11px 14px;text-align:left;font-size:11px;font-weight:800;color:#6b7280;text-transform:uppercase;background:#f9fafb;border-bottom:1px solid #e5e7eb;white-space:nowrap}
.data-table td{padding:11px 14px;border-bottom:1px solid #f3f4f6;vertical-align:middle}
.data-table tr:last-child td{border-bottom:none}
.data-table tr:hover td{background:#f7fcfe}
.td-name { font-weight:700; }
.td-price { font-weight:700; color:#29bcea; white-space:nowrap; }
.prod-name-wrap { display:flex; align-items:center; gap:8px; }
.prod-thumb { width:32px; height:32px; border-radius:6px; object-fit:cover; }
.prod-no-img { width:32px; height:32px; border-radius:6px; background:#f3f4f6; display:flex; align-items:center; justify-content:center; font-size:16px; }
.act-btns{display:flex;gap:6px}
.tbadge{padding:3px 9px;border-radius:999px;font-size:10px;font-weight:800}
.tbadge--orange{background:#f7fcfe;color:#c2410c}
.tbadge--green{background:#f0fdf4;color:#166534}
.tbadge--blue{background:#dbeafe;color:#1e40af}
.sbadge{padding:3px 9px;border-radius:999px;font-size:11px;font-weight:800}
.sbadge--green{background:#dcfce7;color:#166534}
.sbadge--gray{background:#f3f4f6;color:#6b7280}

/* modal */
.modal-overlay{position:fixed;inset:0;background:rgba(0,0,0,.45);display:flex;align-items:center;justify-content:center;z-index:500;padding:20px}
.modal{background:white;border-radius:18px;padding:28px;width:100%;max-width:580px;max-height:90vh;overflow-y:auto}
.modal-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}
.modal-head h2{font-size:18px;font-weight:900;margin:0}
.modal-close{width:30px;height:30px;background:#f3f4f6;border:none;border-radius:50%;cursor:pointer;font-size:14px}
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:14px}
.field{display:flex;flex-direction:column;gap:5px}
.field-full{grid-column:1/-1}
.field label{font-size:11px;font-weight:800;color:#6b7280;text-transform:uppercase}
.field input,.field select{padding:9px 12px;border:1px solid #e5e7eb;border-radius:8px;font-size:14px;font-family:inherit}
.field input:focus,.field select:focus{outline:none;border-color:#29bcea}
.preview-img{width:100%;max-height:80px;object-fit:cover;border-radius:8px;margin-top:6px}
.form-err{color:#ef4444;font-size:13px;margin-top:8px}
.modal-footer{display:flex;gap:10px;justify-content:flex-end;margin-top:20px}

/* switch toggle */
.switch-label{display:flex;align-items:center;gap:10px;cursor:pointer;font-size:14px;font-weight:600;color:#374151}
.switch-cb{display:none}
.switch-slider{position:relative;width:40px;height:22px;background:#d1d5db;border-radius:11px;transition:background .2s;flex-shrink:0}
.switch-slider::after{content:'';position:absolute;width:16px;height:16px;border-radius:50%;background:white;top:3px;left:3px;transition:transform .2s}
.switch-cb:checked~.switch-slider{background:#29bcea}
.switch-cb:checked~.switch-slider::after{transform:translateX(18px)}
</style>

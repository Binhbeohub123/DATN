<template>
  <div class="promo-page">
    <transition name="toast"><div v-if="toast.show" :class="['toast',`toast--${toast.type}`]">{{ toast.msg }}</div></transition>

    <div class="toolbar">
      <h3 class="section-title">Mã Khuyến Mãi</h3>
      <button class="btn-primary" @click="openCreate">+ Thêm mã</button>
    </div>

    <div class="card table-card">
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>
      <div v-else-if="promos.length===0" class="state-center empty-text">Chưa có mã khuyến mãi</div>
      <div v-else class="table-scroll">
        <table class="data-table">
          <thead><tr>
            <th>Mã</th><th>Tên</th><th>Loại</th><th>Giá trị</th>
            <th>Đơn tối thiểu</th><th>Đã dùng / Max</th><th>Hết hạn</th><th>Phim áp dụng</th><th>Trạng thái</th><th>Thao tác</th>
          </tr></thead>
          <tbody>
            <tr v-if="filteredPromos.length === 0"><td colspan="10" class="empty-text">Không tìm thấy mã phù hợp</td></tr>
            <tr v-for="km in filteredPromos" :key="km.id" :data-row-id="km.id">
              <td class="td-code">{{ km.maKhuyenMai }}</td>
              <td class="td-name">{{ km.tenKhuyenMai }}</td>
              <td><span :class="['tbadge', km.loaiGiamGia==='percent'?'tbadge--blue':'tbadge--purple']">{{ km.loaiGiamGia==='percent'?'%':'Cố định' }}</span></td>
              <td class="td-val">{{ km.loaiGiamGia==='percent' ? `${km.giaTriGiam}%` : fmtPrice(km.giaTriGiam) }}</td>
              <td>{{ fmtPrice(km.donHangToiThieu) }}</td>
              <td>{{ km.daSuDung }} / {{ km.gioiHanSuDung||'∞' }}</td>
              <td class="td-date">{{ km.ngayKetThuc || '—' }}</td>
              <td class="td-phim">
                <span v-if="!km.phims || km.phims.length===0" class="phim-all">Tất cả phim</span>
                <span v-else class="phim-list">{{ km.phims.map(p=>p.tenPhim).join(', ') }}</span>
              </td>
              <td>
                <button :class="['toggle-btn', km.dangHoatDong?'toggle-btn--on':'toggle-btn--off']" @click="toggleActive(km)">
                  {{ km.dangHoatDong ? '✓ Bật' : '✗ Tắt' }}
                </button>
              </td>
              <td>
                <div class="act-btns">
                  <button class="btn-icon btn-edit" @click="openEdit(km)" title="Sửa">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="btn-icon btn-del"  @click="del(km)" title="Xóa">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
      <div class="modal">
        <div class="modal-head">
          <h2>{{ editing ? 'Sửa mã' : 'Thêm mã khuyến mãi' }}</h2>
          <button class="modal-close" @click="showModal=false">✕</button>
        </div>
        <div class="form-grid">
          <div class="field"><label>Mã khuyến mãi *</label><input v-model="form.maKhuyenMai" placeholder="VD: SUMMER20" :disabled="!!editing" /></div>
          <div class="field"><label>Tên hiển thị *</label><input v-model="form.tenKhuyenMai" placeholder="Tên chương trình"/></div>
          <div class="field"><label>Loại giảm giá *</label>
            <select v-model="form.loaiGiamGia">
              <option value="percent">Phần trăm (%)</option>
              <option value="fixed">Cố định (VND)</option>
            </select>
          </div>
          <div class="field"><label>Giá trị giảm *</label><input v-model.number="form.giaTriGiam" type="number" min="0"/></div>
          <div class="field" v-if="form.loaiGiamGia==='percent'"><label>Giảm tối đa (VND)</label><input v-model.number="form.giaTriGiamToiDa" type="number" min="0"/></div>
          <div class="field"><label>Đơn hàng tối thiểu</label><input v-model.number="form.donHangToiThieu" type="number" min="0"/></div>
          <div class="field"><label>Số lần dùng tối đa</label><input v-model.number="form.gioiHanSuDung" type="number" min="1"/></div>
          <div class="field"><label>Ngày bắt đầu</label><input v-model="form.ngayBatDau" type="date"/></div>
          <div class="field"><label>Ngày kết thúc</label><input v-model="form.ngayKetThuc" type="date"/></div>
          <div class="field"><label>Mô tả</label><input v-model="form.moTa" placeholder="Mô tả ngắn (không bắt buộc)"/></div>
          <div class="field field--full"><label>Áp dụng cho phim (bỏ trống = áp dụng tất cả)</label>
            <div style="background:#1a1a2e;color:#0ff;font-size:11px;padding:6px 8px;border-radius:4px;margin-bottom:4px;font-family:monospace;">
              [DEBUG] form.phimIds = {{ JSON.stringify(form.phimIds) }}<br>
              [DEBUG] types = {{ form.phimIds.map(id => typeof id) }}<br>
              [DEBUG] allMovies IDs = {{ allMovies.map(m => m.id) }}<br>
              [DEBUG] allMovies ID types = {{ allMovies.slice(0,3).map(m => typeof m.id) }}
            </div>
            <div class="movie-select-box">
              <label v-for="m in allMovies" :key="m.id" class="movie-select-row">
                <input type="checkbox" :value="m.id" v-model="form.phimIds" />
                <span>{{ m.tenPhim }} <small style="color:#888">(id={{ m.id }}, inArray={{ form.phimIds.includes(m.id) }})</small></span>
              </label>
              <div v-if="allMovies.length === 0" class="movie-select-empty">Đang tải phim...</div>
            </div>
          </div>
        </div>
        <p v-if="formErr" class="form-err">{{ formErr }}</p>
        <div class="modal-footer">
          <button class="btn-ghost" @click="showModal=false">Hủy</button>
          <button class="btn-primary" :disabled="saving" @click="save">{{ saving?'Đang lưu...':'Lưu' }}</button>
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

const shell = useAdminShellStore()

const toast = reactive({ show:false, msg:'', type:'success' })
let toastTimer = null
function showToast(msg, type='error') {
  clearTimeout(toastTimer); toast.msg=msg; toast.type=type; toast.show=true
  toastTimer = setTimeout(() => toast.show=false, 3500)
}

const promos    = ref([])
const loading   = ref(false)
const showModal = ref(false)
const editing   = ref(null)
const saving    = ref(false)
const formErr   = ref('')

const allMovies = ref([])

// ── Command-palette targeting (mirrors MoviesPage syncFromShell) ──
const search = ref('')
const flashQueued = ref(false)

const filteredPromos = computed(() => {
  const q = search.value.toLowerCase()
  if (!q) return promos.value
  return promos.value.filter(p =>
    p.tenKhuyenMai?.toLowerCase().includes(q) ||
    p.maKhuyenMai?.toLowerCase().includes(q)
  )
})

function syncFromShell() {
  if (shell.searchTargetPage !== 'promo') return
  if (shell.searchQuery) {
    search.value = shell.searchQuery
    flashQueued.value = true
  }
}

watch(() => shell.searchTick, syncFromShell)

// When the palette-narrowed list renders, flash + scroll the matched row.
watch(filteredPromos, (list) => {
  if (flashQueued.value && list.length > 0) {
    flashQueued.value = false
    nextTick(() => flashRow(document.querySelector(`[data-row-id="${list[0].id}"]`)))
  }
})

const blankForm = () => ({
  maKhuyenMai:'', tenKhuyenMai:'', moTa:'', loaiGiamGia:'percent',
  giaTriGiam:10, giaTriGiamToiDa:null, donHangToiThieu:100000,
  gioiHanSuDung:100, ngayBatDau:'', ngayKetThuc:'', dangHoatDong:true,
  phimIds: []
})
const form = ref(blankForm())

function fmtPrice(v) {
  if (!v && v!==0) return '—'
  return new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(v)
}

async function openCreate() { editing.value=null; form.value=blankForm(); formErr.value=''; await loadMovies(); showModal.value=true }
async function openEdit(km) {
  console.log('[DEBUG openEdit] km.maKhuyenMai:', km.maKhuyenMai)
  console.log('[DEBUG openEdit] km.phims raw:', JSON.stringify(km.phims))
  console.log('[DEBUG openEdit] km.phims?.map(p => p.id):', km.phims?.map(p => p.id))
  editing.value=km
  form.value = {
    maKhuyenMai: km.maKhuyenMai, tenKhuyenMai: km.tenKhuyenMai||'', moTa: km.moTa||'',
    loaiGiamGia: km.loaiGiamGia||'percent', giaTriGiam: km.giaTriGiam||0,
    giaTriGiamToiDa: km.giaTriGiamToiDa||null, donHangToiThieu: km.donHangToiThieu||0,
    gioiHanSuDung: km.gioiHanSuDung||null, ngayBatDau: km.ngayBatDau||'',
    ngayKetThuc: km.ngayKetThuc||'', dangHoatDong: km.dangHoatDong!==false,
    phimIds: km.phims?.map(p => p.id) || []
  }
  console.log('[DEBUG openEdit] form.phimIds after assignment:', JSON.stringify(form.value.phimIds))
  console.log('[DEBUG openEdit] form.phimIds types:', form.value.phimIds.map(id => typeof id))
  formErr.value=''; await loadMovies();
  console.log('[DEBUG openEdit] form.phimIds BEFORE showModal:', JSON.stringify(form.value.phimIds))
  console.log('[DEBUG openEdit] allMovies IDs:', allMovies.value.map(m => ({ id: m.id, type: typeof m.id })))
  showModal.value=true
}

async function save() {
  if (!form.value.maKhuyenMai.trim() || !form.value.tenKhuyenMai.trim()) { formErr.value='Vui lòng điền mã và tên'; return }
  saving.value=true; formErr.value=''
  try {
    const payload = { ...form.value, phimIds: form.value.phimIds }
    if (!payload.giaTriGiamToiDa) delete payload.giaTriGiamToiDa
    if (editing.value) { await api.put(`/khuyen-mai/${editing.value.id}`, payload) }
    else               { await api.post('/khuyen-mai', payload) }
    showToast(editing.value?'Đã cập nhật':'Đã thêm mã khuyến mãi', 'success')
    showModal.value=false
    await load()
  } catch(e) { formErr.value = e.response?.data?.message || 'Lỗi lưu khuyến mãi' }
  finally { saving.value=false }
}

async function toggleActive(km) {
  try {
    await api.put(`/khuyen-mai/${km.id}`, { dangHoatDong: !km.dangHoatDong })
    km.dangHoatDong = !km.dangHoatDong
    showToast(km.dangHoatDong?'Đã bật mã':'Đã tắt mã', 'success')
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi thay đổi trạng thái') }
}

async function del(km) {
  if (!confirm(`Xóa mã "${km.maKhuyenMai}"?`)) return
  try {
    await api.delete(`/khuyen-mai/${km.id}`)
    showToast('Đã xóa mã', 'success')
    await load()
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi xóa mã') }
}

async function loadMovies() {
  try {
    const r = await api.get('/phim');
    allMovies.value = r.data||[]
    console.log('[DEBUG loadMovies] movie count:', allMovies.value.length)
    console.log('[DEBUG loadMovies] first 3 movie IDs + types:', allMovies.value.slice(0,3).map(m => ({ id: m.id, type: typeof m.id })))
  }
  catch(e) { allMovies.value = [] }
}

async function load() {
  loading.value=true
  try {
    const r = await api.get('/khuyen-mai/all');
    promos.value = r.data||[]
    console.log('[DEBUG load] promos count:', promos.value.length)
    promos.value.forEach(km => {
      console.log(`[DEBUG load] promo "${km.maKhuyenMai}" (id=${km.id}): phims=`, JSON.stringify(km.phims?.map(p => ({ id: p.id, type: typeof p.id, tenPhim: p.tenPhim }))))
    })
  }
  catch(e) { showToast('Không tải được mã khuyến mãi') }
  finally { loading.value=false }
}
onMounted(() => {
  syncFromShell()
  load()
})
</script>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; }

/* ── Page root ── */
.promo-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--admin-bg);
  color: var(--admin-text);
}

/* ── Cell helpers ── */
.td-code { font-family: 'Courier New', monospace; font-size: 13px; color: var(--admin-accent); font-weight: 600; }
.td-name { font-weight: 600; font-size: 14px; color: var(--admin-text); }
.td-date { font-size: 12px; color: var(--admin-text-muted); }
.td-val  { font-weight: 600; color: var(--admin-text); }

/* ── FIX: badge clipping on Trạng thái ── */
.toggle-btn {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  min-width: fit-content;
  padding: 4px 12px;
  border: none;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  font-family: var(--font-ui, 'Inter', sans-serif);
}
.toggle-btn--on  { background: rgba(16,185,129,0.15); color: #10B981; }
.toggle-btn--off { background: rgba(156,163,175,0.15); color: #9CA3AF; }

/* ── Type badges ── */
.tbadge {
  display: inline-flex;
  align-items: center;
  border-radius: 9999px;
  padding: 3px 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  min-width: fit-content;
}
.tbadge--blue   { background: rgba(255,255,255,0.10); color: var(--admin-accent); }
.tbadge--purple { background: rgba(255,255,255,0.10); color: var(--admin-accent); }

/* ── Movie multi-select ── */
.field--full { grid-column: 1 / -1; }
.movie-select-box {
  max-height: 160px;
  overflow-y: auto;
  background: var(--admin-bg);
  border: 1px solid var(--admin-accent-muted);
  border-radius: 6px;
  padding: 6px 8px;
}
.movie-select-box::-webkit-scrollbar { width: 6px; }
.movie-select-box::-webkit-scrollbar-track { background: transparent; }
.movie-select-box::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.15); border-radius: 3px; }
.movie-select-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 2px;
  cursor: pointer;
  color: var(--admin-text);
  font-size: 13px;
  border-radius: 4px;
  transition: background 0.15s;
}
.movie-select-row:hover { background: var(--admin-accent-muted); }
.movie-select-row input[type="checkbox"] { accent-color: var(--admin-text); width: 14px; height: 14px; flex-shrink: 0; cursor: pointer; }
.movie-select-empty { color: var(--admin-text-muted); font-size: 13px; padding: 4px 2px; }

/* ── Phim column ── */
.td-phim { font-size: 12px; max-width: 200px; }
.phim-all  { color: var(--admin-text-muted); font-style: italic; }
.phim-list { color: #29bcea; line-height: 1.5; }

/* ── Palette target row flash ── */
.row-flash {
  animation: row-flash-pop 1.6s ease;
}
@keyframes row-flash-pop {
  0%, 100% { background-color: transparent; }
  20%, 60% { background-color: rgba(41, 188, 234, 0.18); }
}
</style>

<template>
  <div class="movies-page">
    <transition name="toast">
      <div v-if="toast.show" :class="['toast', `toast--${toast.type}`]">{{ toast.msg }}</div>
    </transition>

    <!-- Toolbar -->
    <div class="toolbar">
      <select v-model="statusFilter" class="filter-select">
        <option value="">Tất cả trạng thái</option>
        <option value="dang_chieu">Đang chiếu</option>
        <option value="sap_chieu">Sắp chiếu</option>
        <option value="chua_chieu">Chưa chiếu</option>
        <option value="da_ket_thuc">Đã kết thúc</option>
      </select>
      <button class="btn-primary" @click="openCreate">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        Thêm phim
      </button>
    </div>

    <!-- Table -->
    <div class="card table-card">
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>
      <div v-else-if="filteredMovies.length===0" class="state-center empty-text">Không có phim nào</div>
      <div v-else class="table-scroll">
        <table class="data-table">
          <thead>
            <tr>
              <th>Poster</th><th>Tên phim</th><th>Thể loại</th><th>TL</th>
              <th>Trạng thái</th><th>Điểm</th><th>Ngày tạo</th><th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in filteredMovies" :key="m.id">
              <td>
                <img v-if="m.posterUrl" :src="m.posterUrl" class="poster-thumb" :alt="m.tenPhim" />
                <div v-else class="poster-fallback">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="18" height="18"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M7 4v16M17 4v16M2 9h5M17 9h5M2 15h5M17 15h5"/></svg>
                </div>
              </td>
              <td><div class="movie-name">{{ m.tenPhim }}</div><div class="movie-en">{{ m.tenPhimTiengAnh }}</div></td>
              <td class="td-genre">
                <span v-if="m.theLoais && m.theLoais.length > 0" class="genre-chips">
                  <span v-for="t in m.theLoais" :key="t.id" class="genre-chip">{{ t.tenTheLoai }}</span>
                </span>
                <span v-else class="td-no-genre">—</span>
              </td>
              <td class="td-dur">{{ m.thoiLuong }}p</td>
              <td>
                <span :class="['sbadge', statusClass(m.trangThai)]">{{ statusLabel(m.trangThai) }}</span>
              </td>
              <td>{{ m.diemDanhGia?.toFixed(1) || '—' }}</td>
              <td class="td-date">{{ fmtDate(m.ngayTao) }}</td>
              <td>
                <div class="act-btns">
                  <button class="btn-icon btn-edit" @click="openEdit(m)" title="Sửa">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="btn-icon btn-del" @click="del(m)" title="Xóa">
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
          <h2>{{ editing ? 'Sửa phim' : 'Thêm phim mới' }}</h2>
          <button class="modal-close" @click="showModal=false">✕</button>
        </div>
        <div class="form-grid">
          <div class="field"><label>Tên phim *</label><input v-model="form.tenPhim" placeholder="Tên tiếng Việt"/></div>
          <div class="field"><label>Tên tiếng Anh</label><input v-model="form.tenPhimTiengAnh" placeholder="English title"/></div>
          <div class="field field-full">
            <label>Thể loại</label>
            <div v-if="loadingGenres" class="genre-loading">Đang tải thể loại...</div>
            <div v-else class="genre-grid">
              <div
                v-for="g in allGenres"
                :key="g.id"
                class="genre-item"
                :class="{ 'genre-selected': selectedGenreIds.includes(Number(g.id)) }"
                @click="toggleGenre(Number(g.id))"
              >
                <input
                  type="checkbox"
                  :value="Number(g.id)"
                  :checked="selectedGenreIds.includes(Number(g.id))"
                  class="genre-checkbox"
                  @click.stop
                  @change="toggleGenre(Number(g.id))"
                />
                <span>{{ g.tenTheLoai }}</span>
              </div>
              <span v-if="allGenres.length === 0" class="genre-empty">Chưa có thể loại nào</span>
            </div>
          </div>
          <div class="field"><label>Thời lượng (phút)</label><input v-model.number="form.thoiLuong" type="number" min="1"/></div>
          <div class="field"><label>Ngôn ngữ</label><input v-model="form.ngonNgu" placeholder="Tiếng Việt"/></div>
          <div class="field"><label>Giới hạn tuổi</label>
            <select v-model="form.phanLoaiDoTuoi">
              <option value="P">P — Mọi lứa tuổi</option>
              <option value="K">K — Có phụ huynh hướng dẫn</option>
              <option value="T13">T13 — Từ 13 tuổi</option>
              <option value="T16">T16 — Từ 16 tuổi</option>
              <option value="T18">T18 — Từ 18 tuổi</option>
            </select>
          </div>
          <div class="field"><label>Đạo diễn</label><input v-model="form.daoDien"/></div>
          <div class="field"><label>Diễn viên chính</label><input v-model="form.dienVienChinh"/></div>
          <div class="field"><label>Ngày khởi chiếu</label><input v-model="form.ngayCongChieu" type="date"/></div>
          <div class="field field-full"><label>URL Poster</label><input v-model="form.posterUrl" placeholder="https://..."/></div>
          <div class="field field-full"><label>URL Trailer (YouTube)</label><input v-model="form.trailerUrl" placeholder="https://youtube.com/..."/></div>
          <div class="field field-full"><label>Mô tả</label><textarea v-model="form.moTa" rows="4" placeholder="Nội dung phim..."></textarea></div>
          <div class="field field-full">
            <label>Định dạng chiếu</label>
            <div v-if="loadingFormats" class="genre-loading">Đang tải định dạng...</div>
            <div v-else class="genre-grid">
              <div
                v-for="f in allFormats"
                :key="f.id"
                class="genre-item"
                :class="{ 'genre-selected': selectedFormatIds.includes(Number(f.id)) }"
                @click="toggleFormat(Number(f.id))"
              >
                <input
                  type="checkbox"
                  :value="Number(f.id)"
                  :checked="selectedFormatIds.includes(Number(f.id))"
                  class="genre-checkbox"
                  @click.stop
                  @change="toggleFormat(Number(f.id))"
                />
                <span>{{ f.tenDinhDang }}</span>
              </div>
              <span v-if="allFormats.length === 0" class="genre-empty">Chưa có định dạng nào</span>
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
import { ref, computed, onMounted, reactive, watch } from 'vue'
import api from '@/services/api'
import { useAdminShellStore } from '@/stores/adminShellStore'
import { fmtDate } from '@/utils/dateFmt'

const shell = useAdminShellStore()

const toast = reactive({ show:false, msg:'', type:'success' })
let toastTimer = null
function showToast(msg, type='error') {
  clearTimeout(toastTimer); toast.msg=msg; toast.type=type; toast.show=true
  toastTimer = setTimeout(() => toast.show=false, 3500)
}

const movies = ref([])
const loading = ref(false)
const search = ref('')
const statusFilter = ref('')
const showModal = ref(false)
const editing = ref(null)
const saving = ref(false)
const formErr = ref('')

// ── Genre multi-select ──────────────────────────────────────
const allGenres       = ref([])   // [{ id, tenTheLoai }] from GET /api/the-loai
const loadingGenres   = ref(false)
const selectedGenreIds = ref([])  // array of selected IDs (Numbers)

async function loadGenres() {
  loadingGenres.value = true
  try {
    const r = await api.get('/the-loai')
    allGenres.value = r.data || []
  } catch {
    allGenres.value = []
  } finally {
    loadingGenres.value = false
  }
}

function toggleGenre(id) {
  const idx = selectedGenreIds.value.indexOf(id)
  if (idx === -1) {
    selectedGenreIds.value = [...selectedGenreIds.value, id]
  } else {
    selectedGenreIds.value = selectedGenreIds.value.filter(x => x !== id)
  }
}

// ── Format (DinhDang) multi-select ──────────────────────────
const allFormats        = ref([])   // [{ id, tenDinhDang }] from GET /api/dinh-dang
const loadingFormats    = ref(false)
const selectedFormatIds = ref([])   // array of selected IDs (Numbers)

async function loadFormats() {
  loadingFormats.value = true
  try {
    const r = await api.get('/dinh-dang')
    allFormats.value = r.data || []
  } catch {
    allFormats.value = []
  } finally {
    loadingFormats.value = false
  }
}

function toggleFormat(id) {
  const idx = selectedFormatIds.value.indexOf(id)
  if (idx === -1) {
    selectedFormatIds.value = [...selectedFormatIds.value, id]
  } else {
    selectedFormatIds.value = selectedFormatIds.value.filter(x => x !== id)
  }
}

const blankForm = () => ({
  tenPhim:'', tenPhimTiengAnh:'', thoiLuong:90, ngonNgu:'Tiếng Việt',
  phanLoaiDoTuoi:'P', daoDien:'', dienVienChinh:'', moTa:'', posterUrl:'',
  trailerUrl:'', ngayCongChieu:''
})
const form = ref(blankForm())

const filteredMovies = computed(() => {
  let list = movies.value.filter(m => !m.isDeleted)
  if (search.value) list = list.filter(m => m.tenPhim?.toLowerCase().includes(search.value.toLowerCase()))
  if (statusFilter.value) list = list.filter(m => m.trangThai === statusFilter.value)
  return list
})

function statusClass(s) {
  if (s==='dang_chieu')  return 'sbadge--green'
  if (s==='sap_chieu')   return 'sbadge--amber'
  if (s==='da_ket_thuc') return 'sbadge--gray'
  return 'sbadge--gray'  // chua_chieu + fallback
}
function statusLabel(s) {
  if (s==='dang_chieu')  return 'Đang chiếu'
  if (s==='sap_chieu')   return 'Sắp chiếu'
  if (s==='chua_chieu')  return 'Chưa chiếu'
  if (s==='da_ket_thuc') return 'Đã kết thúc'
  return s || 'Chưa chiếu'
}
function openCreate() {
  editing.value = null
  form.value = blankForm()
  selectedGenreIds.value = []
  selectedFormatIds.value = []
  formErr.value = ''
  showModal.value = true
}
function openEdit(m) {
  editing.value = m
  form.value = {
    tenPhim: m.tenPhim||'', tenPhimTiengAnh: m.tenPhimTiengAnh||'',
    thoiLuong: m.thoiLuong||90, ngonNgu: m.ngonNgu||'', phanLoaiDoTuoi: m.phanLoaiDoTuoi||'P',
    daoDien: m.daoDien||'', dienVienChinh: m.dienVienChinh||'', moTa: m.moTa||'',
    posterUrl: m.posterUrl||'', trailerUrl: m.trailerUrl||'', ngayCongChieu: m.ngayCongChieu||'',
  }
  selectedGenreIds.value = Array.isArray(m.theLoais)
    ? m.theLoais.map(t => Number(t.id))
    : []
  selectedFormatIds.value = Array.isArray(m.dinhDangs)
    ? m.dinhDangs.map(d => Number(d.id))
    : []
  formErr.value = ''
  showModal.value = true
}

async function save() {
  if (!form.value.tenPhim.trim()) { formErr.value='Tên phim không được để trống'; return }
  saving.value = true; formErr.value = ''
  try {
    // Send theLoaiIds alongside the standard form fields
    const payload = {
      ...form.value,
      // Empty date/empty duration must go as null, not "", or backend 500s on LocalDate.parse("")
      ngayCongChieu: form.value.ngayCongChieu || null,
      thoiLuong: form.value.thoiLuong || null,
      theLoaiIds: selectedGenreIds.value,
      dinhDangIds: selectedFormatIds.value,
    }
    if (editing.value) { await api.put(`/admin/phim/${editing.value.id}`, payload) }
    else               { await api.post('/admin/phim', payload) }
    showToast(editing.value?'Đã cập nhật phim':'Đã thêm phim mới', 'success')
    showModal.value = false
    await load()
  } catch(e) { formErr.value = e.response?.data?.message || 'Lỗi lưu phim' }
  finally { saving.value = false }
}

async function del(m) {
  if (!confirm(`Xóa phim "${m.tenPhim}"?`)) return
  try {
    await api.delete(`/admin/phim/${m.id}`)
    showToast('Đã xóa phim', 'success')
    await load()
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi xóa phim') }
}

async function load() {
  loading.value=true
  try { const r = await api.get('/admin/phim'); movies.value = r.data||[] }
  catch(e) { showToast('Không tải được danh sách phim') }
  finally { loading.value=false }
}

function syncFromShell() {
  if (shell.searchTargetPage === 'movies' && shell.searchQuery) {
    search.value = shell.searchQuery
  }
}

watch(() => shell.searchTick, syncFromShell)

onMounted(() => {
  syncFromShell()
  load()
  loadGenres()
  loadFormats()
})
</script>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; }

/* ── Page root ── */
.movies-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--admin-bg);
  color: var(--admin-text);
}

/* ── Poster cell ── */
.poster-thumb {
  width: 48px;
  height: 68px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--admin-border);
}
.poster-fallback {
  width: 48px;
  height: 68px;
  background: var(--admin-surface-hover);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--admin-text-muted);
}

/* ── Movie name stacked ── */
.movie-name { font-weight: 600; color: var(--admin-text); font-size: 14px; }
.movie-en   { font-size: 12px; color: var(--admin-text-muted); margin-top: 2px; }

/* ── Inline cell utilities ── */
.td-genre, .td-dur, .td-date { font-size: 12px; color: var(--admin-text-muted); }

/* ── Genre chips in table cell ── */
.genre-chips { display: flex; gap: 4px; flex-wrap: wrap; }
.genre-chip {
  padding: 2px 8px;
  background: rgba(41,188,234,0.1);
  border: 1px solid rgba(41,188,234,0.25);
  border-radius: 9999px;
  font-size: 11px;
  font-weight: 600;
  color: #29bcea;
  white-space: nowrap;
}
.td-no-genre { font-size: 12px; color: var(--admin-text-muted); }

/* ── Genre multi-select in modal ── */
.genre-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  background: var(--admin-surface);
  border: 1px solid var(--admin-border);
  border-radius: 8px;
  max-height: 200px;
  overflow-y: auto;
}
.genre-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--admin-border);
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
  font-size: 13px;
  color: var(--admin-text);
  background: var(--admin-surface);
  transition: all 0.15s;
}
.genre-item:hover {
  border-color: var(--admin-accent);
  color: var(--admin-accent);
}
.genre-selected {
  border-color: var(--admin-accent) !important;
  background: var(--admin-accent-muted) !important;
  color: var(--admin-accent) !important;
}
.genre-checkbox {
  width: 14px;
  height: 14px;
  accent-color: var(--admin-accent);
  cursor: pointer;
  pointer-events: none; /* click is handled by the parent div */
}
.genre-loading { font-size: 13px; color: var(--admin-text-muted); padding: 8px 0; }
.genre-empty   { font-size: 13px; color: var(--admin-text-muted); }
</style>

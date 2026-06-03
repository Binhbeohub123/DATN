<template>
  <div class="movies-page">
    <transition name="toast"><div v-if="toast.show" :class="['toast',`toast--${toast.type}`]">{{ toast.msg }}</div></transition>

    <!-- Toolbar -->
    <div class="toolbar">
      <input v-model="search" placeholder="🔍 Tìm phim..." class="search-input" />
      <select v-model="statusFilter" class="filter-select">
        <option value="">Tất cả trạng thái</option>
        <option value="dang_chieu">Đang chiếu</option>
        <option value="sap_chieu">Sắp chiếu</option>
        <option value="ngung_chieu">Ngừng chiếu</option>
      </select>
      <button class="btn-primary" @click="openCreate">+ Thêm phim</button>
    </div>

    <!-- Table -->
    <div class="card table-card">
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>
      <div v-else-if="filteredMovies.length===0" class="state-center empty-text">Không có phim nào</div>
      <div v-else class="table-scroll">
        <table class="data-table">
          <thead><tr>
            <th>Poster</th><th>Tên phim</th><th>Thể loại</th><th>TL</th>
            <th>Trạng thái</th><th>Điểm</th><th>Ngày tạo</th><th>Thao tác</th>
          </tr></thead>
          <tbody>
            <tr v-for="m in filteredMovies" :key="m.id">
              <td><img v-if="m.posterUrl" :src="m.posterUrl" class="poster-thumb" :alt="m.tenPhim" /><div v-else class="poster-fallback">🎬</div></td>
              <td><div class="movie-name">{{ m.tenPhim }}</div><div class="movie-en">{{ m.tenPhimTiengAnh }}</div></td>
              <td class="td-genre">{{ m.theLoai }}</td>
              <td class="td-dur">{{ m.thoiLuong }}p</td>
              <td><span :class="['sbadge',statusClass(m.trangThai)]">{{ statusLabel(m.trangThai) }}</span></td>
              <td>{{ m.diemDanhGia?.toFixed(1) || '—' }}</td>
              <td class="td-date">{{ fmtDate(m.ngayTao) }}</td>
              <td>
                <div class="act-btns">
                  <button class="btn-icon btn-edit" @click="openEdit(m)" title="Sửa">✏️</button>
                  <button class="btn-icon btn-del"  @click="del(m)"     title="Xóa">🗑️</button>
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
          <div class="field"><label>Thể loại</label><input v-model="form.theLoai" placeholder="Hành động, Tình cảm..."/></div>
          <div class="field"><label>Thời lượng (phút)</label><input v-model.number="form.thoiLuong" type="number" min="1"/></div>
          <div class="field"><label>Ngôn ngữ</label><input v-model="form.ngonNgu" placeholder="Tiếng Việt"/></div>
          <div class="field"><label>Giới hạn tuổi</label>
            <select v-model="form.phanLoaiDoTuoi">
              <option value="P">P — Mọi lứa tuổi</option>
              <option value="C13">C13 — Từ 13 tuổi</option>
              <option value="C16">C16 — Từ 16 tuổi</option>
              <option value="C18">C18 — Từ 18 tuổi</option>
            </select>
          </div>
          <div class="field"><label>Đạo diễn</label><input v-model="form.daoDien"/></div>
          <div class="field"><label>Diễn viên chính</label><input v-model="form.dienVienChinh"/></div>
          <div class="field"><label>Ngày khởi chiếu</label><input v-model="form.ngayCongChieu" type="date"/></div>
          <div class="field"><label>Trạng thái</label>
            <select v-model="form.trangThai">
              <option value="sap_chieu">Sắp chiếu</option>
              <option value="dang_chieu">Đang chiếu</option>
              <option value="ngung_chieu">Ngừng chiếu</option>
            </select>
          </div>
          <div class="field field-full"><label>URL Poster</label><input v-model="form.posterUrl" placeholder="https://..."/></div>
          <div class="field field-full"><label>URL Trailer (YouTube)</label><input v-model="form.trailerUrl" placeholder="https://youtube.com/..."/></div>
          <div class="field field-full"><label>Mô tả</label><textarea v-model="form.moTa" rows="4" placeholder="Nội dung phim..."></textarea></div>
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

const blankForm = () => ({
  tenPhim:'', tenPhimTiengAnh:'', theLoai:'', thoiLuong:90, ngonNgu:'Tiếng Việt',
  phanLoaiDoTuoi:'P', daoDien:'', dienVienChinh:'', moTa:'', posterUrl:'',
  trailerUrl:'', ngayCongChieu:'', trangThai:'sap_chieu'
})
const form = ref(blankForm())

const filteredMovies = computed(() => {
  let list = movies.value.filter(m => !m.isDeleted)
  if (search.value) list = list.filter(m => m.tenPhim?.toLowerCase().includes(search.value.toLowerCase()))
  if (statusFilter.value) list = list.filter(m => m.trangThai === statusFilter.value)
  return list
})

function statusClass(s) {
  if (s==='dang_chieu') return 'sbadge--green'
  if (s==='sap_chieu')  return 'sbadge--amber'
  return 'sbadge--gray'
}
function statusLabel(s) {
  if (s==='dang_chieu') return 'Đang chiếu'
  if (s==='sap_chieu')  return 'Sắp chiếu'
  return 'Ngừng chiếu'
}
function fmtDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('vi-VN')
}

function openCreate() { editing.value=null; form.value=blankForm(); formErr.value=''; showModal.value=true }
function openEdit(m) {
  editing.value=m
  form.value = {
    tenPhim: m.tenPhim||'', tenPhimTiengAnh: m.tenPhimTiengAnh||'', theLoai: m.theLoai||'',
    thoiLuong: m.thoiLuong||90, ngonNgu: m.ngonNgu||'', phanLoaiDoTuoi: m.phanLoaiDoTuoi||'P',
    daoDien: m.daoDien||'', dienVienChinh: m.dienVienChinh||'', moTa: m.moTa||'',
    posterUrl: m.posterUrl||'', trailerUrl: m.trailerUrl||'', ngayCongChieu: m.ngayCongChieu||'',
    trangThai: m.trangThai||'sap_chieu'
  }
  formErr.value=''; showModal.value=true
}

async function save() {
  if (!form.value.tenPhim.trim()) { formErr.value='Tên phim không được để trống'; return }
  saving.value=true; formErr.value=''
  try {
    if (editing.value) { await api.put(`/admin/phim/${editing.value.id}`, form.value) }
    else               { await api.post('/admin/phim', form.value) }
    showToast(editing.value?'Đã cập nhật phim':'Đã thêm phim mới', 'success')
    showModal.value=false
    await load()
  } catch(e) { formErr.value = e.response?.data?.message || 'Lỗi lưu phim' }
  finally { saving.value=false }
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
})
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from,
.toast-leave-to { opacity: 0; }
</style>

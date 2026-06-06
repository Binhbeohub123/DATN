<template>
  <div class="schedule-page">
    <transition name="toast">
      <div v-if="toast.show" :class="['toast', `toast--${toast.type}`]">{{ toast.msg }}</div>
    </transition>

    <!-- Toolbar -->
    <div class="toolbar">
      <select v-model="filterMovie" class="filter-select">
        <option value="">Tất cả phim</option>
        <option v-for="m in movies" :key="m.id" :value="m.id">{{ m.tenPhim }}</option>
      </select>
      <input v-model="filterDate" type="date" class="filter-select" />
      <button class="btn-primary" @click="openAdd">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        Thêm lịch chiếu
      </button>
    </div>

    <!-- Table -->
    <div class="card table-card">
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>
      <div v-else-if="filtered.length===0" class="state-center empty-text">Không có lịch chiếu</div>
      <div v-else class="table-scroll">
        <table class="data-table">
          <thead>
            <tr>
              <th>Phim</th><th>Phòng chiếu</th><th>Ngày</th>
              <th>Giờ bắt đầu</th><th>Giờ kết thúc</th><th>Giá</th><th>Trạng thái</th><th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="lc in filtered" :key="lc.id">
              <td class="td-movie">{{ lc.phim?.tenPhim || '—' }}</td>
              <td>
                {{ lc.phongChieu?.tenPhong || '—' }}
                <span :class="['sbadge', lc.phongChieu?.loaiPhong==='VIP'?'sbadge--amber':'sbadge--gray']" style="margin-left:6px">{{ lc.phongChieu?.loaiPhong }}</span>
              </td>
              <td class="td-date">{{ fmtDate(lc.thoiGianBatDau) }}</td>
              <td class="td-time">{{ fmtTime(lc.thoiGianBatDau) }}</td>
              <td class="td-time">{{ fmtTime(lc.thoiGianKetThuc) }}</td>
              <td class="td-price">{{ fmtPrice(lc.giaCoBan) }}</td>
              <td>
                <span :class="['sbadge', lc.trangThai==='active'?'sbadge--green':'sbadge--gray']">{{ lc.trangThai }}</span>
              </td>
              <td>
                <div class="act-btns">
                  <button class="btn-icon btn-edit" @click="openAdd" title="Sửa">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="btn-icon btn-del" @click="del(lc)" title="Xóa">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Add Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
      <div class="modal">
        <div class="modal-head">
          <h2>Thêm lịch chiếu</h2>
          <button class="modal-close" @click="showModal=false">✕</button>
        </div>
        <div class="form-grid">
          <div class="field field-full">
            <label>Phim *</label>
            <select v-model="form.phimId">
              <option value="">-- Chọn phim --</option>
              <option v-for="m in movies" :key="m.id" :value="m.id">{{ m.tenPhim }}</option>
            </select>
          </div>
          <div class="field field-full">
            <label>Phòng chiếu *</label>
            <select v-model="form.phongChieuId">
              <option value="">-- Chọn phòng --</option>
              <option v-for="p in phongs" :key="p.id" :value="p.id">{{ p.tenPhong }} ({{ p.loaiPhong }}) — {{ p.rapChieu?.tenRap }}</option>
            </select>
          </div>
          <div class="field"><label>Ngày chiếu *</label><input v-model="form.date" type="date" /></div>
          <div class="field"><label>Giờ bắt đầu *</label><input v-model="form.startTime" type="time" /></div>
          <div class="field"><label>Thời lượng (phút)</label><input v-model.number="form.durationMin" type="number" min="30" /></div>
          <div class="field"><label>Giá cơ bản (VND) *</label><input v-model.number="form.giaCoBan" type="number" min="0" step="1000" /></div>
        </div>
        <div v-if="conflict" class="conflict-warn">⚠️ {{ conflict }}</div>
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
import { useAdminShellStore } from '@/stores/adminShellStore'

const shell = useAdminShellStore()
import api from '@/services/api'

const toast = reactive({ show:false, msg:'', type:'success' })
let toastTimer = null
function showToast(msg, type='error') {
  clearTimeout(toastTimer); toast.msg=msg; toast.type=type; toast.show=true
  toastTimer = setTimeout(() => toast.show=false, 3500)
}

const schedules = ref([])
const movies    = ref([])
const phongs    = ref([])
const loading   = ref(false)
const showModal = ref(false)
const saving    = ref(false)
const formErr   = ref('')
const conflict  = ref('')
const filterMovie = ref('')
const filterDate  = ref('')

const form = ref({ phimId:'', phongChieuId:'', date:'', startTime:'', durationMin:120, giaCoBan:80000 })

function scheduleOnDate(dt, dateStr) {
  if (!dateStr) return true
  if (!dt) return false
  const raw = typeof dt === 'string' ? dt : new Date(dt).toISOString()
  return raw.slice(0, 10) === dateStr
}

const filtered = computed(() => {
  let list = schedules.value
  if (filterMovie.value) list = list.filter(lc => lc.phim?.id == filterMovie.value)
  if (filterDate.value) list = list.filter(lc => scheduleOnDate(lc.thoiGianBatDau, filterDate.value))
  return list
})

function fmtDate(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString('vi-VN')
}
function fmtTime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleTimeString('vi-VN', { hour:'2-digit', minute:'2-digit' })
}
function fmtPrice(v) {
  return new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(v||0)
}

function openAdd() {
  form.value = { phimId:'', phongChieuId:'', date:'', startTime:'', durationMin:120, giaCoBan:80000 }
  formErr.value=''; conflict.value=''; showModal.value=true
}

async function save() {
  const { phimId, phongChieuId, date, startTime, durationMin, giaCoBan } = form.value
  if (!phimId || !phongChieuId || !date || !startTime) { formErr.value='Vui lòng điền đầy đủ thông tin *'; return }
  saving.value=true; formErr.value=''; conflict.value=''
  try {
    const start = new Date(`${date}T${startTime}`)
    const end   = new Date(start.getTime() + durationMin*60000)
    await api.post('/admin/lich-chieu', {
      phim:       { id: Number(phimId) },
      phongChieu: { id: Number(phongChieuId) },
      thoiGianBatDau:  start.toISOString().slice(0,19),
      thoiGianKetThuc: end.toISOString().slice(0,19),
      giaCoBan,
    })
    showToast('Đã thêm lịch chiếu', 'success')
    showModal.value=false
    await load()
  } catch(e) {
    const status = e.response?.status
    const msg = e.response?.data?.message || e.response?.data || ''
    if (status===409 || msg.toLowerCase().includes('conflict') || msg.includes('khung giờ')) {
      conflict.value = msg || 'Phòng chiếu đã có lịch chiếu trong khung giờ này'
    } else {
      formErr.value = msg || 'Lỗi tạo lịch chiếu'
    }
  } finally { saving.value=false }
}

async function del(lc) {
  if (!confirm(`Xóa lịch chiếu ${fmtDate(lc.thoiGianBatDau)} ${fmtTime(lc.thoiGianBatDau)}?`)) return
  try {
    await api.delete(`/admin/lich-chieu/${lc.id}`)
    showToast('Đã xóa lịch chiếu', 'success')
    await load()
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi xóa lịch chiếu') }
}

async function load() {
  loading.value=true
  try {
    const [scRes, mvRes, rapsRes] = await Promise.all([
      api.get('/admin/lich-chieu'),
      api.get('/admin/phim'),
      api.get('/rap-chieu'),
    ])
    schedules.value = scRes.data || []
    movies.value    = (mvRes.data || []).filter(m => !m.isDeleted)
    // flatten all phongChieu from all raps
    const raps = Array.isArray(rapsRes.data) ? rapsRes.data : []
    // fetch phòng theo từng rạp từ public rap-chieu endpoint
    const phongList = []
    await Promise.all(raps.map(async rap => {
      try {
        const pRes = await api.get(`/rap-chieu/${rap.id}/phong`)
        ;(pRes.data || []).forEach(p => phongList.push({ ...p, rapChieu: rap }))
      } catch {
        // try fetching all phong from schedule data
      }
    }))
    // fallback: extract phongChieu from schedules if phongList empty
    if (phongList.length === 0) {
      const seen = new Set()
      schedules.value.forEach(lc => {
        if (lc.phongChieu && !seen.has(lc.phongChieu.id)) {
          seen.add(lc.phongChieu.id)
          phongList.push({ ...lc.phongChieu, rapChieu: lc.phongChieu.rapChieu })
        }
      })
    }
    phongs.value = phongList
  } catch(e) { showToast('Không tải được dữ liệu lịch chiếu') }
  finally { loading.value=false }
}

function syncFromShell() {
  if (shell.filterDate) filterDate.value = shell.filterDate
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

<template>
  <div class="schedule-page">
    <transition name="toast"><div v-if="toast.show" :class="['toast',`toast--${toast.type}`]">{{ toast.msg }}</div></transition>

    <!-- Toolbar -->
    <div class="toolbar">
      <select v-model="filterMovie" class="filter-select">
        <option value="">Tất cả phim</option>
        <option v-for="m in movies" :key="m.id" :value="m.id">{{ m.tenPhim }}</option>
      </select>
      <input v-model="filterDate" type="date" class="filter-select" />
      <button class="btn-primary" @click="openAdd">+ Thêm lịch chiếu</button>
    </div>

    <!-- Table -->
    <div class="card table-card">
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>
      <div v-else-if="filtered.length===0" class="state-center empty-text">Không có lịch chiếu</div>
      <div v-else class="table-scroll">
        <table class="data-table">
          <thead><tr>
            <th>Phim</th><th>Phòng chiếu</th><th>Ngày</th>
            <th>Giờ bắt đầu</th><th>Giờ kết thúc</th><th>Giá</th><th>Trạng thái</th><th>Thao tác</th>
          </tr></thead>
          <tbody>
            <tr v-for="lc in filtered" :key="lc.id">
              <td class="td-movie">{{ lc.phim?.tenPhim || '—' }}</td>
              <td>{{ lc.phongChieu?.tenPhong || '—' }}<span class="type-chip">{{ lc.phongChieu?.loaiPhong }}</span></td>
              <td class="td-date">{{ fmtDate(lc.thoiGianBatDau) }}</td>
              <td class="td-time">{{ fmtTime(lc.thoiGianBatDau) }}</td>
              <td class="td-time">{{ fmtTime(lc.thoiGianKetThuc) }}</td>
              <td class="td-price">{{ fmtPrice(lc.giaCoBan) }}</td>
              <td><span :class="['sbadge', lc.trangThai==='active'?'sbadge--green':'sbadge--gray']">{{ lc.trangThai }}</span></td>
              <td><button class="btn-icon btn-del" @click="del(lc)" title="Xóa">🗑️</button></td>
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
          <div class="field">
            <label>Ngày chiếu *</label>
            <input v-model="form.date" type="date" />
          </div>
          <div class="field">
            <label>Giờ bắt đầu *</label>
            <input v-model="form.startTime" type="time" />
          </div>
          <div class="field">
            <label>Thời lượng (phút)</label>
            <input v-model.number="form.durationMin" type="number" min="30" />
          </div>
          <div class="field">
            <label>Giá cơ bản (VND) *</label>
            <input v-model.number="form.giaCoBan" type="number" min="0" step="1000" />
          </div>
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
import { ref, computed, onMounted, reactive } from 'vue'
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

const filtered = computed(() => {
  let list = schedules.value
  if (filterMovie.value) list = list.filter(lc => lc.phim?.id == filterMovie.value)
  if (filterDate.value)  list = list.filter(lc => lc.thoiGianBatDau?.startsWith(filterDate.value))
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

onMounted(load)
</script>

<style scoped>
.schedule-page { display:flex; flex-direction:column; gap:20px; }
.toast { position:fixed; top:20px; right:20px; z-index:999; padding:12px 20px; border-radius:10px; font-size:13px; font-weight:700; }
.toast--error   { background:#7f1d1d; color:#fecaca; }
.toast--success { background:#14532d; color:#bbf7d0; }
.toast-enter-active,.toast-leave-active{transition:opacity .3s}.toast-enter-from,.toast-leave-to{opacity:0}
.toolbar { display:flex; gap:10px; align-items:center; flex-wrap:wrap; }
.filter-select { padding:9px 12px; border:1px solid #e5e7eb; border-radius:9px; font-size:13px; background:white; cursor:pointer; }
.filter-select:focus { outline:none; border-color:#29bcea; }
.btn-primary { padding:9px 18px; background:#29bcea; color:white; border:none; border-radius:9px; font-weight:800; cursor:pointer; font-size:14px; white-space:nowrap; transition:all .2s; }
.btn-primary:hover:not(:disabled) { transform:translateY(-1px); }
.btn-primary:disabled { opacity:.6; cursor:not-allowed; }
.btn-ghost { padding:9px 18px; background:transparent; border:1px solid #e5e7eb; border-radius:9px; font-weight:700; cursor:pointer; font-size:14px; }
.btn-ghost:hover { border-color:#29bcea; color:#29bcea; }
.card { border-radius:18px; background:rgba(255,255,255,0.84); border:1px solid rgba(255,255,255,0.7); box-shadow:0 8px 24px rgba(15,23,42,0.07); }
.table-card { overflow:hidden; }
.table-scroll { overflow-x:auto; }
.state-center { display:flex; justify-content:center; align-items:center; padding:40px; }
.empty-text { color:#9ca3af; font-size:14px; }
.spinner { width:36px; height:36px; border:4px solid rgba(255,107,0,.2); border-top-color:#29bcea; border-radius:50%; animation:spin .9s linear infinite; }
@keyframes spin { to{transform:rotate(360deg)} }
.data-table { width:100%; border-collapse:collapse; font-size:13px; }
.data-table th { padding:11px 14px; text-align:left; font-size:11px; font-weight:800; color:#6b7280; text-transform:uppercase; background:#f9fafb; border-bottom:1px solid #e5e7eb; white-space:nowrap; }
.data-table td { padding:11px 14px; border-bottom:1px solid #f3f4f6; vertical-align:middle; }
.data-table tr:last-child td { border-bottom:none; }
.data-table tr:hover td { background:#f7fcfe; }
.td-movie { font-weight:700; max-width:180px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.td-date,.td-time { white-space:nowrap; color:#374151; }
.td-price { font-weight:700; color:#29bcea; white-space:nowrap; }
.type-chip { margin-left:6px; padding:2px 7px; background:#dbeafe; color:#1e40af; border-radius:4px; font-size:10px; font-weight:800; }
.sbadge { padding:3px 9px; border-radius:999px; font-size:11px; font-weight:800; }
.sbadge--green { background:#dcfce7; color:#166534; }
.sbadge--gray  { background:#f3f4f6; color:#6b7280; }
.btn-icon { width:30px; height:30px; border:none; border-radius:7px; cursor:pointer; font-size:13px; transition:all .2s; }
.btn-del { background:#fee2e2; } .btn-del:hover { background:#fecaca; }
.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,.45); display:flex; align-items:center; justify-content:center; z-index:500; padding:20px; }
.modal { background:white; border-radius:18px; padding:28px; width:100%; max-width:560px; max-height:90vh; overflow-y:auto; }
.modal-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:20px; }
.modal-head h2 { font-size:18px; font-weight:900; margin:0; }
.modal-close { width:30px; height:30px; background:#f3f4f6; border:none; border-radius:50%; cursor:pointer; font-size:14px; }
.form-grid { display:grid; grid-template-columns:1fr 1fr; gap:14px; }
.field { display:flex; flex-direction:column; gap:5px; }
.field-full { grid-column:1/-1; }
.field label { font-size:11px; font-weight:800; color:#6b7280; text-transform:uppercase; }
.field input,.field select { padding:9px 12px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px; font-family:inherit; }
.field input:focus,.field select:focus { outline:none; border-color:#29bcea; }
.conflict-warn { background:#fef3c7; border:1px solid #fcd34d; border-radius:8px; padding:10px 14px; font-size:13px; color:#92400e; font-weight:700; margin-top:12px; }
.form-err { color:#ef4444; font-size:13px; margin-top:8px; }
.modal-footer { display:flex; gap:10px; justify-content:flex-end; margin-top:20px; }
</style>

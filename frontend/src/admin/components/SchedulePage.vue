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
      <div class="date-nav" role="group" aria-label="Điều hướng ngày">
        <button class="date-nav__arrow" @click="shiftDate(-1)" :aria-label="'Ngày trước'" title="Ngày trước">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" width="14" height="14" aria-hidden="true"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
        <input v-model="filterDate" type="date" class="filter-select date-nav__input" :aria-label="'Lọc theo ngày'" />
        <button class="date-nav__arrow" @click="shiftDate(1)" :aria-label="'Ngày sau'" title="Ngày sau">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" width="14" height="14" aria-hidden="true"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
      </div>
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
              <th>Phim</th><th>Tên rạp</th><th>Phòng chiếu</th><th>Ngày</th>
              <th>Giờ bắt đầu</th><th>Giờ kết thúc</th><th>Giá</th><th>Trạng thái</th><th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="lc in filtered" :key="lc.id">
              <td class="td-movie">{{ lc.phim?.tenPhim || '—' }}</td>
              <td class="td-rap">{{ lc.phongChieu?.rapChieu?.tenRap || '—' }}</td>
              <td>
                {{ lc.phongChieu?.tenPhong || '—' }}
                <span class="sbadge sbadge--gray" style="margin-left:6px">{{ lc.phongChieu?.loaiPhong }}</span>
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
                  <button class="btn-icon btn-edit" @click="openEdit(lc)" title="Sửa">
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

    <!-- Add / Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
      <div class="modal modal--wide">
        <div class="modal-head">
          <h2>{{ editing ? 'Sửa lịch chiếu' : 'Thêm lịch chiếu' }}</h2>
          <button class="modal-close" @click="showModal=false">✕</button>
        </div>

        <!-- Mode toggle (only for new showtimes, not edit) -->
        <div v-if="!editing" class="mode-toggle" role="tablist">
          <button
            :class="['mode-btn', { 'mode-btn--active': mode === 'manual' }]"
            role="tab" :aria-selected="mode === 'manual'"
            @click="mode = 'manual'; formErr = ''; importPreviewRows = null; importResult = null"
          >✏️ Nhập thủ công</button>
          <button
            :class="['mode-btn', { 'mode-btn--active': mode === 'import' }]"
            role="tab" :aria-selected="mode === 'import'"
            @click="mode = 'import'; formErr = ''; batchResult = null"
          >📥 Nhập từ Excel</button>
        </div>

        <!-- ── MANUAL MODE ── -->
        <template v-if="editing || mode === 'manual'">
          <div class="form-grid">
            <div class="field field-full">
              <label>Phim *</label>
              <select v-model="form.phimId">
                <option value="">-- Chọn phim --</option>
                <option v-for="m in movies" :key="m.id" :value="m.id">{{ m.tenPhim }}</option>
              </select>
            </div>
            <div class="field field-full">
              <label>Rạp chiếu *</label>
              <select v-model="form.rapChieuId">
                <option value="">-- Chọn rạp --</option>
                <option v-for="r in raps" :key="r.id" :value="r.id">{{ r.tenRap }}</option>
              </select>
            </div>
            <div class="field field-full">
              <label>Phòng chiếu *</label>
              <select v-model="form.phongChieuId" :disabled="!form.rapChieuId">
                <option value="">{{ form.rapChieuId ? '-- Chọn phòng --' : '-- Chọn rạp trước --' }}</option>
                <option v-for="p in filteredPhongs" :key="p.id" :value="p.id">{{ p.tenPhong }} ({{ p.loaiPhong }})</option>
              </select>
            </div>
            <div class="field field-full">
              <label>Ngày chiếu * <span class="hint">(chọn một hoặc nhiều ngày)</span></label>
              <div class="date-chip-grid">
                <button
                  v-for="d in availableDays" :key="d.iso" type="button"
                  :class="['date-chip', { 'date-chip--selected': form.dates.includes(d.iso) }]"
                  @click="toggleDate(d.iso)"
                >
                  <span class="date-chip__num">{{ d.num }}</span>
                  <span class="date-chip__dow">{{ d.dow }}</span>
                </button>
              </div>
              <div v-if="form.dates.length > 0" class="selected-dates-summary">
                {{ form.dates.length }} ngày đã chọn: {{ form.dates.slice(0,3).join(', ') }}{{ form.dates.length > 3 ? ' ...' : '' }}
                <button type="button" class="btn-clear-dates" @click="form.dates = []">Xóa tất cả</button>
              </div>
            </div>
            <div class="field"><label>Giờ bắt đầu *</label><input v-model="form.startTime" type="time" /></div>
            <div class="field"><label>Thời lượng (phút)</label><input v-model.number="form.durationMin" type="number" min="30" /></div>
            <div class="field"><label>Giá cơ bản (VND) *</label><input v-model.number="form.giaCoBan" type="number" min="0" step="1000" /></div>
          </div>
          <div v-if="conflict" class="conflict-warn">⚠️ {{ conflict }}</div>
          <p v-if="formErr" class="form-err">{{ formErr }}</p>
          <div v-if="batchResult" class="batch-result">
            <p v-if="batchResult.totalSucceeded > 0" class="batch-ok">
              ✅ Tạo thành công {{ batchResult.totalSucceeded }}/{{ batchResult.totalRequested }} ngày:
              {{ batchResult.succeeded.map(s => s.date).join(', ') }}
            </p>
            <div v-if="batchResult.failed.length > 0" class="batch-fail">
              <p>❌ {{ batchResult.failed.length }} ngày thất bại:</p>
              <ul>
                <li v-for="f in batchResult.failed" :key="f.date">
                  <strong>{{ f.date }}</strong>: {{ f.reason }}
                </li>
              </ul>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-ghost" @click="showModal=false">Hủy</button>
            <button class="btn-primary" :disabled="saving" @click="save">{{ saving ? 'Đang lưu...' : (editing ? 'Cập nhật' : 'Lưu') }}</button>
          </div>
        </template>

        <!-- ── IMPORT FROM EXCEL MODE ── -->
        <template v-else-if="mode === 'import'">
          <div class="import-hint">
            <p class="import-format-note">
              📋 <strong>Định dạng file Excel (.xlsx):</strong>
              Cột A: Tên rạp chiếu | Cột B: Tên phim | Cột C: Tên phòng chiếu | Cột D: Ngày chiếu (dd/MM/yyyy) | Cột E: Giờ bắt đầu (HH:mm) | Cột F: Giá cơ bản
            </p>
          </div>

          <div class="import-upload-row">
            <label class="upload-label">
              <input ref="fileInputRef" type="file" accept=".xlsx" class="file-input-hidden" @change="onFileChange" />
              <span class="upload-btn">📂 Chọn file .xlsx</span>
              <span class="upload-filename">{{ importFile ? importFile.name : 'Chưa chọn file' }}</span>
            </label>
            <button class="btn-primary" :disabled="!importFile || importPreviewing" @click="runPreview">
              {{ importPreviewing ? 'Đang kiểm tra...' : 'Xem trước' }}
            </button>
          </div>

          <p v-if="formErr" class="form-err">{{ formErr }}</p>

          <!-- Preview table -->
          <div v-if="importPreviewRows" class="import-preview-wrap">
            <div class="import-preview-summary">
              <span class="preview-ok">✅ {{ importPreviewRows.filter(r=>r.valid).length }} hợp lệ</span>
              <span class="preview-fail">❌ {{ importPreviewRows.filter(r=>!r.valid).length }} lỗi</span>
              <span class="preview-total">/ {{ importPreviewRows.length }} dòng</span>
            </div>
            <div class="table-scroll">
              <table class="data-table import-table">
                <thead>
                  <tr>
                    <th>Dòng</th><th>Phim</th><th>Phòng</th><th>Ngày</th><th>Giờ</th><th>Giá</th><th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in importPreviewRows" :key="r.row" :class="r.valid ? '' : 'row-invalid'">
                    <td class="td-rownum">#{{ r.row }}</td>
                    <td>{{ r.tenPhim }}</td>
                    <td>{{ r.tenPhong }}</td>
                    <td class="td-date">{{ r.ngayChieu }}</td>
                    <td class="td-time">{{ r.gioChieu }}</td>
                    <td class="td-price">{{ r.giaCoBan }}</td>
                    <td class="td-status">
                      <span v-if="r.valid" class="sbadge sbadge--green">✅ Hợp lệ</span>
                      <span v-else class="import-err-msg">❌ {{ r.reason }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Import result after confirm -->
          <div v-if="importResult" class="batch-result">
            <p v-if="importResult.totalSucceeded > 0" class="batch-ok">
              ✅ Đã lưu thành công {{ importResult.totalSucceeded }}/{{ importResult.totalRequested }} lịch chiếu.
            </p>
            <div v-if="importResult.failed && importResult.failed.length > 0" class="batch-fail">
              <p>❌ {{ importResult.failed.length }} dòng thất bại:</p>
              <ul>
                <li v-for="f in importResult.failed" :key="f.row">
                  <strong>Dòng #{{ f.row }}</strong>: {{ f.reason }}
                </li>
              </ul>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-ghost" @click="showModal=false">Đóng</button>
            <button
              class="btn-primary"
              :disabled="!importPreviewRows || importPreviewRows.filter(r=>r.valid).length === 0 || saving"
              @click="runConfirm"
            >{{ saving ? 'Đang lưu...' : 'Lưu các dòng hợp lệ' }}</button>
          </div>
        </template>

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

const schedules  = ref([])
const movies     = ref([])
const phongs     = ref([])
const loading    = ref(false)
const showModal  = ref(false)
const editing    = ref(null)
const saving     = ref(false)
const formErr    = ref('')
const conflict   = ref('')
const batchResult = ref(null)
const filterMovie = ref('')
const filterDate  = ref('')

// Mode toggle: 'manual' | 'import'
const mode = ref('manual')

// Cinema + room refs for manual mode
const raps = ref([])  // all cinemas — populated by load()

// ── Manual form state ─────────────────────────────────────────
const form = ref({ phimId:'', rapChieuId:'', phongChieuId:'', dates:[], startTime:'', durationMin:120, giaCoBan:80000 })

// Filtered rooms for the manual mode dropdown (only rooms of selected cinema)
const filteredPhongs = computed(() =>
  form.value.rapChieuId
    ? phongs.value.filter(p => p.rapChieu?.id === Number(form.value.rapChieuId))
    : []
)

// When cinema changes, clear selected room (stale room from another cinema)
watch(() => form.value.rapChieuId, () => {
  form.value.phongChieuId = ''
})

// ── Excel import state ───────────────────────────────────────
const fileInputRef      = ref(null)
const importFile        = ref(null)
const importPreviewing  = ref(false)
const importPreviewRows = ref(null)  // array from /import-preview
const importResult      = ref(null)  // { succeeded, failed, ... } from /import-confirm

function onFileChange(e) {
  importFile.value = e.target.files[0] || null
  importPreviewRows.value = null
  importResult.value = null
  formErr.value = ''
}

async function runPreview() {
  if (!importFile.value) return
  importPreviewing.value = true
  importPreviewRows.value = null
  importResult.value = null
  formErr.value = ''
  try {
    const fd = new FormData()
    fd.append('file', importFile.value)
    const res = await api.post('/admin/lich-chieu/import-preview', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    importPreviewRows.value = res.data
  } catch(e) {
    formErr.value = e.response?.data?.message || 'Lỗi kiểm tra file Excel'
  } finally {
    importPreviewing.value = false
  }
}

async function runConfirm() {
  if (!importPreviewRows.value) return
  const validRows = importPreviewRows.value.filter(r => r.valid)
  if (!validRows.length) return
  saving.value = true
  importResult.value = null
  formErr.value = ''
  try {
    const res = await api.post('/admin/lich-chieu/import-confirm', validRows)
    importResult.value = res.data
    if (res.data.totalSucceeded > 0) {
      showToast(`Đã lưu ${res.data.totalSucceeded}/${res.data.totalRequested} lịch chiếu`, 'success')
      await load()
    }
    if (res.data.totalFailed === 0) showModal.value = false
  } catch(e) {
    formErr.value = e.response?.data?.message || 'Lỗi lưu lịch chiếu từ Excel'
  } finally {
    saving.value = false
  }
}

const availableDays = computed(() => {
  const out = [], DOW = ['CN','T2','T3','T4','T5','T6','T7']
  const now = new Date()
  const baseY = now.getFullYear(), baseM = now.getMonth(), baseD = now.getDate()
  for (let i = 0; i < 30; i++) {
    const d = new Date(baseY, baseM, baseD + i)
    const y = d.getFullYear(), mo = String(d.getMonth()+1).padStart(2,'0'), dy = String(d.getDate()).padStart(2,'0')
    out.push({ iso: `${y}-${mo}-${dy}`, num: d.getDate(), dow: DOW[d.getDay()] })
  }
  return out
})

function toggleDate(iso) {
  const idx = form.value.dates.indexOf(iso)
  if (idx === -1) form.value.dates = [...form.value.dates, iso].sort()
  else form.value.dates = form.value.dates.filter(d => d !== iso)
}

function scheduleOnDate(dt, dateStr) {
  if (!dateStr) return true
  if (!dt) return false
  const raw = typeof dt === 'string' ? dt : new Date(dt).toISOString()
  return raw.slice(0, 10) === dateStr
}

const filtered = computed(() => {
  let list = schedules.value
  if (filterMovie.value) list = list.filter(lc => lc.phim?.id == filterMovie.value)
  if (filterDate.value)  list = list.filter(lc => scheduleOnDate(lc.thoiGianBatDau, filterDate.value))
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
  editing.value = null
  mode.value = 'manual'
  form.value = { phimId:'', rapChieuId:'', phongChieuId:'', dates:[], startTime:'', durationMin:120, giaCoBan:80000 }
  formErr.value=''; conflict.value=''; batchResult.value=null
  importFile.value=null; importPreviewRows.value=null; importResult.value=null
  if (fileInputRef.value) fileInputRef.value.value = ''
  showModal.value=true
}

function openEdit(lc) {
  editing.value = lc
  formErr.value = ''; conflict.value = ''

  const parseISO = (iso) => {
    if (!iso) return null
    const [datePart, timePart] = iso.split('T')
    return { date: datePart, time: timePart ? timePart.slice(0, 5) : '' }
  }

  const startParsed = parseISO(lc.thoiGianBatDau)
  const endParsed   = parseISO(lc.thoiGianKetThuc)

  let durationMin = 120
  if (startParsed && endParsed) {
    const toMin = (hhmm) => { const [h, m] = hhmm.split(':').map(Number); return h * 60 + m }
    const dayDiff = startParsed.date !== endParsed.date
      ? (new Date(endParsed.date) - new Date(startParsed.date)) / 86400000 : 0
    durationMin = toMin(endParsed.time) - toMin(startParsed.time) + dayDiff * 1440
  }

  form.value = {
    phimId:       lc.phim?.id       || '',
    rapChieuId:   lc.phongChieu?.rapChieu?.id || '',
    phongChieuId: lc.phongChieu?.id || '',
    dates:        startParsed?.date ? [startParsed.date] : [],
    startTime:    startParsed?.time  || '',
    durationMin:  Math.max(30, durationMin),
    giaCoBan:     lc.giaCoBan        || 80000,
  }
  batchResult.value = null
  showModal.value = true
}

async function save() {
  const { phimId, phongChieuId, dates, startTime, durationMin, giaCoBan } = form.value
  if (!phimId || !phongChieuId || !startTime) { formErr.value='Vui lòng điền đầy đủ thông tin *'; return }

  saving.value=true; formErr.value=''; conflict.value=''; batchResult.value=null

  const [startH, startM] = startTime.split(':').map(Number)
  const totalEndMin = startH * 60 + startM + durationMin
  const endH = Math.floor(totalEndMin / 60) % 24
  const endMin = totalEndMin % 60
  const endTime = `${String(endH).padStart(2,'0')}:${String(endMin).padStart(2,'0')}`

  try {
    if (editing.value) {
      if (!dates.length) { formErr.value='Vui lòng chọn ít nhất một ngày'; saving.value=false; return }
      const date = dates[0]
      const [sH, sM] = startTime.split(':').map(Number)
      const totalEnd = sH * 60 + sM + durationMin
      let edH = Math.floor(totalEnd / 60) % 24, edM = totalEnd % 60
      let endDate = date
      if (totalEnd >= 1440) {
        const d = new Date(date + 'T00:00:00'); d.setDate(d.getDate() + Math.floor(totalEnd / 1440))
        endDate = d.toISOString().slice(0, 10)
        edH = Math.floor((totalEnd % 1440) / 60); edM = totalEnd % 60
      }
      const pad = n => String(n).padStart(2,'0')
      const payload = {
        phim: { id: Number(phimId) }, phongChieu: { id: Number(phongChieuId) },
        thoiGianBatDau:  `${date}T${pad(sH)}:${pad(sM)}:00`,
        thoiGianKetThuc: `${endDate}T${pad(edH)}:${pad(edM)}:00`,
        giaCoBan,
      }
      await api.put(`/admin/lich-chieu/${editing.value.id}`, payload)
      showToast('Đã cập nhật lịch chiếu', 'success')
      showModal.value = false
      await load()
    } else {
      if (!dates.length) { formErr.value='Vui lòng chọn ít nhất một ngày'; saving.value=false; return }
      const payload = { phimId: Number(phimId), phongChieuId: Number(phongChieuId), startTime, endTime, giaCoBan, dates }
      const res = await api.post('/admin/lich-chieu/batch', payload)
      batchResult.value = res.data
      if (res.data.totalSucceeded > 0) {
        showToast(`Đã tạo ${res.data.totalSucceeded}/${res.data.totalRequested} lịch chiếu`, 'success')
        await load()
      }
      if (res.data.totalFailed === 0) showModal.value = false
    }
  } catch(e) {
    const status = e.response?.status
    const msg = e.response?.data?.message || e.response?.data || ''
    if (status===409 || msg.toLowerCase().includes('conflict') || msg.includes('khung giờ')) {
      conflict.value = msg || 'Phòng chiếu đã có lịch chiếu trong khung giờ này'
    } else {
      formErr.value = msg || 'Lỗi lưu lịch chiếu'
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
    const scheduleParams = filterDate.value
      ? { dateFrom: filterDate.value, dateTo: filterDate.value, page: 0, size: 200 }
      : { page: 0, size: 20 }

    const [scRes, mvRes, rapsRes] = await Promise.all([
      api.get('/admin/lich-chieu', { params: scheduleParams }),
      api.get('/admin/phim'),
      api.get('/rap-chieu'),
    ])
    schedules.value = scRes.data?.content ?? scRes.data ?? []
    if (!Array.isArray(schedules.value)) schedules.value = []

    movies.value = (mvRes.data || []).filter(m => !m.isDeleted)
    const rapsFetched = Array.isArray(rapsRes.data) ? rapsRes.data : []
    raps.value = rapsFetched   // store for manual-mode cinema dropdown
    const phongList = []
    await Promise.all(rapsFetched.map(async rap => {
      try {
        const pRes = await api.get(`/rap-chieu/${rap.id}/phong`)
        ;(pRes.data || []).forEach(p => phongList.push({ ...p, rapChieu: rap }))
      } catch { /* ignore */ }
    }))
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

async function shiftDate(delta) {
  let base
  if (filterDate.value) {
    const [y, m, d] = filterDate.value.split('-').map(Number)
    base = new Date(y, m - 1, d)
  } else {
    const now = new Date()
    base = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  }
  const next = new Date(base.getFullYear(), base.getMonth(), base.getDate() + delta)
  filterDate.value = `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}-${String(next.getDate()).padStart(2, '0')}`
}

watch(filterDate, () => { load() })
watch(() => shell.searchTick, syncFromShell)

onMounted(() => {
  syncFromShell()
  load()
})
</script>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; }

.schedule-page { display: flex; flex-direction: column; gap: 0; background: #0D0D0D; color: #E5E5E5; }

/* ── Cell utilities ── */
.td-movie  { font-weight: 600; font-size: 14px; color: #E5E5E5; max-width: 160px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.td-rap    { font-size: 13px; color: #29bcea; font-weight: 600; max-width: 140px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.td-date   { font-size: 12px; color: #9CA3AF; }
.td-time   { font-size: 13px; color: #E5E5E5; font-variant-numeric: tabular-nums; }
.td-price  { font-weight: 700; color: #FFFFFF; }
.td-rownum { font-size: 12px; color: #6B7280; font-weight: 700; }
.td-status { max-width: 260px; }

/* ── Multi-date chip selector ── */
.hint { font-size: 11px; color: #6B7280; font-weight: 400; margin-left: 4px; }

.date-chip-grid {
  display: flex; flex-wrap: wrap; gap: 6px; padding: 8px;
  background: #111827; border: 1px solid #374151; border-radius: 8px;
  max-height: 160px; overflow-y: auto;
}
.date-chip-grid::-webkit-scrollbar { width: 4px; }
.date-chip-grid::-webkit-scrollbar-thumb { background: #374151; border-radius: 2px; }

.date-chip {
  display: flex; flex-direction: column; align-items: center; gap: 1px;
  width: 44px; padding: 6px 4px;
  border: 1px solid #374151; border-radius: 6px; background: transparent; color: #9CA3AF;
  cursor: pointer; font-family: var(--font-ui, 'Inter', sans-serif);
  transition: all 150ms ease; flex-shrink: 0;
}
.date-chip:hover { border-color: #FFFFFF; color: #FFFFFF; background: rgba(255,255,255,0.04); }
.date-chip--selected { border-color: #FFFFFF; background: rgba(255,255,255,0.12); color: #FFFFFF; }
.date-chip__num { font-size: 15px; font-weight: 700; line-height: 1; }
.date-chip__dow { font-size: 9px; font-weight: 600; text-transform: uppercase; }

.selected-dates-summary {
  margin-top: 6px; font-size: 12px; color: #9CA3AF;
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
}
.btn-clear-dates {
  padding: 2px 8px; border: 1px solid #374151; border-radius: 4px;
  background: transparent; color: #9CA3AF; font-size: 11px; cursor: pointer;
  transition: color 150ms, border-color 150ms;
}
.btn-clear-dates:hover { color: #EF4444; border-color: #EF4444; }

/* ── Batch result display ── */
.batch-result {
  margin-top: 12px; padding: 12px; border-radius: 8px;
  background: rgba(255,255,255,0.03); border: 1px solid #374151; font-size: 13px;
}
.batch-ok { color: #10B981; margin: 0 0 6px; font-weight: 600; }
.batch-fail { color: #FCA5A5; }
.batch-fail p { margin: 0 0 4px; font-weight: 600; }
.batch-fail ul { margin: 0; padding-left: 16px; }
.batch-fail li { margin-bottom: 2px; color: #E5E5E5; }
.batch-fail li strong { color: #FCA5A5; }

/* ── Mode toggle ── */
.modal--wide { width: min(720px, 100%); }
.mode-toggle {
  display: flex; gap: 0; border: 1px solid #374151; border-radius: 8px;
  overflow: hidden; margin-bottom: 20px;
}
.mode-btn {
  flex: 1; padding: 10px 14px; border: none; border-right: 1px solid #374151;
  background: transparent; color: #9CA3AF;
  font-family: var(--font-ui, 'Inter', sans-serif); font-size: 13px; font-weight: 600;
  cursor: pointer; transition: background 150ms, color 150ms; text-align: center;
}
.mode-btn:last-child { border-right: none; }
.mode-btn:hover { color: #E5E5E5; }
.mode-btn--active { background: rgba(255,255,255,0.08); color: #FFFFFF; }

/* ── Excel import ── */
.import-hint { margin-bottom: 16px; }
.import-format-note {
  font-size: 12px; color: #9CA3AF; background: #111827; border: 1px solid #374151;
  border-radius: 8px; padding: 10px 14px; line-height: 1.6; margin: 0;
}
.import-format-note strong { color: #E5E5E5; }

.import-upload-row {
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 16px;
}
.upload-label {
  display: flex; align-items: center; gap: 8px; cursor: pointer; flex: 1; min-width: 0;
}
.file-input-hidden { display: none; }
.upload-btn {
  display: inline-flex; align-items: center; padding: 8px 16px;
  border: 1px solid #374151; border-radius: 8px; background: #1F2937;
  color: #E5E5E5; font-size: 13px; font-weight: 600; cursor: pointer; white-space: nowrap;
  transition: border-color 150ms, background 150ms;
}
.upload-btn:hover { border-color: #FFFFFF; background: #374151; }
.upload-filename {
  font-size: 12px; color: #6B7280; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.import-preview-wrap { margin-top: 12px; }
.import-preview-summary {
  display: flex; align-items: center; gap: 12px; padding: 8px 0; margin-bottom: 8px;
  font-size: 13px; font-weight: 600;
}
.preview-ok   { color: #10B981; }
.preview-fail { color: #F87171; }
.preview-total { color: #9CA3AF; font-weight: 400; }

.import-table { font-size: 12px; }
.row-invalid { background: rgba(239,68,68,0.06); }
.import-err-msg { color: #FCA5A5; font-size: 11px; line-height: 1.4; }

/* ── Date nav ── */
.date-nav {
  display: inline-flex; align-items: center; gap: 0;
  border: 1px solid #374151; border-radius: 8px; overflow: hidden; height: 40px;
}
.date-nav__arrow {
  display: flex; align-items: center; justify-content: center;
  width: 32px; height: 100%; border: none; background: #111827; color: #9CA3AF;
  cursor: pointer; flex-shrink: 0; transition: background 150ms ease, color 150ms ease;
}
.date-nav__arrow:hover { background: #1F2937; color: #FFFFFF; }
.date-nav__arrow:active { background: #374151; }
.date-nav__input {
  border: none !important; border-left: 1px solid #374151 !important;
  border-right: 1px solid #374151 !important; border-radius: 0 !important;
  height: 38px; min-height: unset; padding: 0 10px; margin: 0; width: 148px; flex-shrink: 0;
}
</style>

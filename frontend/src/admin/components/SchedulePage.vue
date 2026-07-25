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
        <button
          class="date-nav__arrow"
          @click="shiftDate(-1)"
          :aria-label="'Ngày trước'"
          title="Ngày trước"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" width="14" height="14" aria-hidden="true"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
        <input v-model="filterDate" type="date" class="filter-select date-nav__input" :aria-label="'Lọc theo ngày'" />
        <button
          class="date-nav__arrow"
          @click="shiftDate(1)"
          :aria-label="'Ngày sau'"
          title="Ngày sau"
        >
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
                <span :class="['sbadge', lc.phongChieu?.loaiPhong==='VIP'?'sbadge--gray':'sbadge--gray']" style="margin-left:6px">{{ lc.phongChieu?.loaiPhong }}</span>
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

    <!-- Add Modal -->
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
            @click="mode = 'manual'; formErr = ''; autoResult = null"
          >✏️ Nhập thủ công</button>
          <button
            :class="['mode-btn', { 'mode-btn--active': mode === 'auto' }]"
            role="tab" :aria-selected="mode === 'auto'"
            @click="mode = 'auto'; formErr = ''; batchResult = null"
          >⚡ Tự động xếp lịch theo số suất</button>
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
            <label>Phòng chiếu *</label>
            <select v-model="form.phongChieuId">
              <option value="">-- Chọn phòng --</option>
              <option v-for="p in phongs" :key="p.id" :value="p.id">{{ p.tenPhong }} ({{ p.loaiPhong }}) — {{ p.rapChieu?.tenRap }}</option>
            </select>
          </div>
          <!-- Multi-date selector (replaces single date input) -->
          <div class="field field-full">
            <label>Ngày chiếu * <span class="hint">(chọn một hoặc nhiều ngày)</span></label>
            <div class="date-chip-grid">
              <button
                v-for="d in availableDays"
                :key="d.iso"
                type="button"
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
        <!-- Batch result summary -->
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

        <!-- ── AUTO MODE ── -->
        <template v-else-if="mode === 'auto'">
        <div class="form-grid">
          <div class="field field-full">
            <label>Phim *</label>
            <select v-model="autoForm.phimId">
              <option value="">-- Chọn phim --</option>
              <option v-for="m in movies" :key="m.id" :value="m.id">{{ m.tenPhim }} ({{ m.thoiLuong || '?' }}p)</option>
            </select>
          </div>
          <div class="field field-full">
            <label>Phòng chiếu * <span class="hint">(chọn một hoặc nhiều phòng)</span></label>
            <div class="room-check-list">
              <label v-for="p in phongs" :key="p.id" class="room-check-row">
                <input type="checkbox" :value="p.id" v-model="autoForm.phongChieuIds" class="room-checkbox" />
                <span>{{ p.tenPhong }} ({{ p.loaiPhong }}) — {{ p.rapChieu?.tenRap }}</span>
              </label>
              <span v-if="phongs.length === 0" class="hint">Đang tải phòng...</span>
            </div>
          </div>
          <div class="field field-full">
            <label>Ngày chiếu *</label>
            <div class="date-chip-grid">
              <button
                v-for="d in availableDays"
                :key="d.iso"
                type="button"
                :class="['date-chip', { 'date-chip--selected': autoForm.date === d.iso }]"
                @click="autoForm.date = (autoForm.date === d.iso ? '' : d.iso)"
              >
                <span class="date-chip__num">{{ d.num }}</span>
                <span class="date-chip__dow">{{ d.dow }}</span>
              </button>
            </div>
          </div>
          <div class="field"><label>Số suất muốn tạo *</label><input v-model.number="autoForm.soSuat" type="number" min="1" max="20" /></div>
          <div class="field"><label>Buffer giữa các suất (phút)</label><input v-model.number="autoForm.bufferMinutes" type="number" min="0" max="60" /></div>
          <div class="field"><label>Khung giờ mở cửa</label><input v-model="autoForm.openTime" type="time" /></div>
          <div class="field"><label>Khung giờ đóng cửa</label><input v-model="autoForm.closeTime" type="time" /></div>
          <div class="field"><label>Giá cơ bản (VND)</label><input v-model.number="autoForm.giaCoBan" type="number" min="0" step="1000" /></div>
        </div>
        <p v-if="formErr" class="form-err">{{ formErr }}</p>
        <!-- Auto result -->
        <div v-if="autoResult" class="batch-result">
          <div v-if="autoResult.success" class="batch-ok">
            ✅ Đã tạo {{ autoResult.totalCreated }} suất cho phim "{{ autoResult.phim }}" ngày {{ autoResult.date }}:
            <ul>
              <li v-for="s in autoResult.succeeded" :key="s.id">
                Suất #{{ s.id }} — {{ s.tenPhong }} {{ s.thoiGianBatDau.slice(11,16) }}–{{ s.thoiGianKetThuc.slice(11,16) }}
              </li>
            </ul>
          </div>
          <div v-else class="batch-fail">
            <p>⚠️ {{ autoResult.message }}</p>
            <p>Có thể xếp tối đa <strong>{{ autoResult.canFit }}</strong> suất trong khung giờ này (yêu cầu {{ autoResult.requested }}).</p>
            <p>Không có suất nào được tạo (all-or-nothing).</p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-ghost" @click="showModal=false">Hủy</button>
          <button class="btn-primary" :disabled="saving" @click="saveAuto">{{ saving ? 'Đang xử lý...' : 'Tự động xếp lịch' }}</button>
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

const schedules = ref([])
const movies    = ref([])
const phongs    = ref([])
const loading   = ref(false)
const showModal = ref(false)
const editing   = ref(null)   // holds the LichChieu being edited, or null when adding
const saving    = ref(false)
const formErr   = ref('')
const conflict  = ref('')
const batchResult = ref(null)  // { succeeded, failed } from batch endpoint
const autoResult  = ref(null)  // { succeeded, totalCreated } or shortfall from auto-generate
const filterMovie = ref('')
const filterDate  = ref('')

// ── Mode toggle: 'manual' | 'auto' ───────────────────────────
const mode = ref('manual')

// Auto-generate form state
const autoForm = ref({
  phimId: '',
  phongChieuIds: [],  // multi-select room IDs
  date: '',
  soSuat: 4,
  openTime: '09:00',
  closeTime: '22:00',
  bufferMinutes: 15,
  giaCoBan: 80000,
})

const form = ref({ phimId:'', phongChieuId:'', dates:[], startTime:'', durationMin:120, giaCoBan:80000 })

// ── 30-day chip list for the multi-date selector ──────────────
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
  // filterMovie: client-side (endpoint has no phimId param)
  if (filterMovie.value) list = list.filter(lc => lc.phim?.id == filterMovie.value)
  // filterDate: server-side when load() is called; keep as safety fallback for
  // any stale data still in schedules.value from a previous unfocused load.
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
  editing.value = null
  mode.value = 'manual'
  form.value = { phimId:'', phongChieuId:'', dates:[], startTime:'', durationMin:120, giaCoBan:80000 }
  autoForm.value = { phimId:'', phongChieuIds:[], date:'', soSuat:4, openTime:'09:00', closeTime:'22:00', bufferMinutes:15, giaCoBan:80000 }
  formErr.value=''; conflict.value=''; batchResult.value=null; autoResult.value=null; showModal.value=true
}

/**
 * Open modal in EDIT mode pre-populated from an existing LichChieu row.
 * The form uses date+startTime+durationMin instead of raw ISO timestamps,
 * so we reverse-compute those from thoiGianBatDau / thoiGianKetThuc.
 */
function openEdit(lc) {
  editing.value = lc
  formErr.value = ''; conflict.value = ''

  // Parse ISO datetime strings directly (no Date constructor — avoids UTC→local shift).
  // thoiGianBatDau is stored and returned as "YYYY-MM-DDTHH:mm:ss" with no timezone suffix.
  // Using new Date() on such a string treats it as UTC in most browsers, which would display
  // the wrong local time in the form fields and produce a shifted time on save.
  const parseISO = (iso) => {
    if (!iso) return null
    // "2026-07-23T09:00:00" → { date: "2026-07-23", time: "09:00" }
    const [datePart, timePart] = iso.split('T')
    return { date: datePart, time: timePart ? timePart.slice(0, 5) : '' }
  }

  const startParsed = parseISO(lc.thoiGianBatDau)
  const endParsed   = parseISO(lc.thoiGianKetThuc)

  let durationMin = 120
  if (startParsed && endParsed) {
    // Calculate duration by converting HH:mm to minutes
    const toMin = (hhmm) => {
      const [h, m] = hhmm.split(':').map(Number)
      return h * 60 + m
    }
    // Handle date boundary: if end is on a later date, add 24h * dayDiff
    const dayDiff = startParsed.date !== endParsed.date
      ? (new Date(endParsed.date) - new Date(startParsed.date)) / 86400000
      : 0
    durationMin = toMin(endParsed.time) - toMin(startParsed.time) + dayDiff * 1440
  }

  form.value = {
    phimId:       lc.phim?.id       || '',
    phongChieuId: lc.phongChieu?.id || '',
    dates:        startParsed?.date ? [startParsed.date] : [],
    startTime:    startParsed?.time     || '',
    durationMin:  Math.max(30, durationMin),
    giaCoBan:     lc.giaCoBan           || 80000,
  }

  batchResult.value = null
  showModal.value = true
}

async function saveAuto() {
  const { phimId, phongChieuIds, date, soSuat, openTime, closeTime, bufferMinutes, giaCoBan } = autoForm.value
  if (!phimId || !phongChieuIds.length || !date || !soSuat || !openTime || !closeTime) {
    formErr.value = 'Vui lòng điền đầy đủ thông tin *'; return
  }
  saving.value = true; formErr.value = ''; autoResult.value = null
  try {
    const payload = { phimId: Number(phimId), phongChieuIds: phongChieuIds.map(Number), date, soSuat, openTime, closeTime, bufferMinutes, giaCoBan }
    const res = await api.post('/admin/lich-chieu/auto-generate', payload)
    autoResult.value = { ...res.data, success: true }
    showToast(`Đã tạo ${res.data.totalCreated} suất chiếu`, 'success')
    await load()
    showModal.value = false
  } catch(e) {
    const d = e.response?.data
    if (e.response?.status === 422 && d?.canFit !== undefined) {
      autoResult.value = { ...d, success: false }
    } else {
      formErr.value = d?.message || 'Lỗi tự động xếp lịch'
    }
  } finally { saving.value = false }
}

async function save() {
  const { phimId, phongChieuId, dates, startTime, durationMin, giaCoBan } = form.value
  if (!phimId || !phongChieuId || !startTime) { formErr.value='Vui lòng điền đầy đủ thông tin *'; return }

  saving.value=true; formErr.value=''; conflict.value=''; batchResult.value=null

  // Compute endTime string from startTime + durationMin
  const [startH, startM] = startTime.split(':').map(Number)
  const totalEndMin = startH * 60 + startM + durationMin
  const endH = Math.floor(totalEndMin / 60) % 24
  const endMin = totalEndMin % 60
  const endTime = `${String(endH).padStart(2,'0')}:${String(endMin).padStart(2,'0')}`

  try {
    if (editing.value) {
      // Single-showtime edit — use original single endpoint
      if (!dates.length) { formErr.value='Vui lòng chọn ít nhất một ngày'; saving.value=false; return }
      const date = dates[0]
      const [sH, sM] = startTime.split(':').map(Number)
      const totalEnd = sH * 60 + sM + durationMin
      let edH = Math.floor(totalEnd / 60) % 24, edM = totalEnd % 60
      let endDate = date
      if (totalEnd >= 1440) {
        const d = new Date(date + 'T00:00:00'); d.setDate(d.getDate() + Math.floor(totalEnd / 1440)); endDate = d.toISOString().slice(0, 10)
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
      // Batch create across selected dates
      if (!dates.length) { formErr.value='Vui lòng chọn ít nhất một ngày'; saving.value=false; return }
      const payload = { phimId: Number(phimId), phongChieuId: Number(phongChieuId), startTime, endTime, giaCoBan, dates }
      const res = await api.post('/admin/lich-chieu/batch', payload)
      batchResult.value = res.data
      if (res.data.totalSucceeded > 0) {
        showToast(`Đã tạo ${res.data.totalSucceeded}/${res.data.totalRequested} lịch chiếu`, 'success')
        await load()
      }
      // Keep modal open to show partial failure details
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
    // Build query params for the schedule fetch.
    // When filterDate is set: pass dateFrom/dateTo to get ALL rows for that day
    // server-side (avoids the 20-row page limit silently truncating results).
    // Use size=200 for date-filtered requests — a single cinema day rarely
    // exceeds 100 slots even across all rooms.
    // When no date filter: fall back to page 0/size=20 (most-recent 20 across all dates).
    const scheduleParams = filterDate.value
      ? { dateFrom: filterDate.value, dateTo: filterDate.value, page: 0, size: 200 }
      : { page: 0, size: 20 }

    const [scRes, mvRes, rapsRes] = await Promise.all([
      api.get('/admin/lich-chieu', { params: scheduleParams }),
      api.get('/admin/phim'),
      api.get('/rap-chieu'),
    ])
    // Phase 4b changed /api/admin/lich-chieu to return a paginated object
    // { content: [...], totalElements, totalPages, ... } instead of a plain array.
    // Extract the array with a fallback so it works either way.
    schedules.value = scRes.data?.content ?? scRes.data ?? []
    if (!Array.isArray(schedules.value)) schedules.value = []

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
    // TODO: add pagination UI if schedule count exceeds default page size (20)
  } catch(e) { showToast('Không tải được dữ liệu lịch chiếu') }
  finally { loading.value=false }
}

function syncFromShell() {
  if (shell.filterDate) filterDate.value = shell.filterDate
}

/**
 * Move filterDate ±1 day using local-date construction (no toISOString/UTC shift).
 * If filterDate is empty, starts from today.
 * @param {number} delta — +1 (next day) or -1 (previous day)
 */
async function shiftDate(delta) {
  let base
  if (filterDate.value) {
    // Parse "YYYY-MM-DD" as local midnight — do NOT use new Date(str) which parses as UTC
    const [y, m, d] = filterDate.value.split('-').map(Number)
    base = new Date(y, m - 1, d)
  } else {
    const now = new Date()
    base = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  }
  const next = new Date(base.getFullYear(), base.getMonth(), base.getDate() + delta)
  filterDate.value = `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}-${String(next.getDate()).padStart(2, '0')}`
  // load() is triggered by the filterDate watcher below — no explicit call needed here
}

// Re-fetch from server when filterDate changes (picker input, arrow nav, or shell sync).
// This is what sends dateFrom/dateTo to the backend for server-side filtering.
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

/* ── Page root ── */
.schedule-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: #0D0D0D;
  color: #E5E5E5;
}

/* ── Cell utilities ── */
.td-movie  { font-weight: 600; font-size: 14px; color: #E5E5E5; max-width: 160px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.td-rap    { font-size: 13px; color: #29bcea; font-weight: 600; max-width: 140px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.td-date   { font-size: 12px; color: #9CA3AF; }
.td-time   { font-size: 13px; color: #E5E5E5; font-variant-numeric: tabular-nums; }
.td-price  { font-weight: 700; color: #FFFFFF; }

/* ── Multi-date chip selector ── */
.hint { font-size: 11px; color: #6B7280; font-weight: 400; margin-left: 4px; }

.date-chip-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px;
  background: #111827;
  border: 1px solid #374151;
  border-radius: 8px;
  max-height: 160px;
  overflow-y: auto;
}
.date-chip-grid::-webkit-scrollbar { width: 4px; }
.date-chip-grid::-webkit-scrollbar-thumb { background: #374151; border-radius: 2px; }

.date-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  width: 44px;
  padding: 6px 4px;
  border: 1px solid #374151;
  border-radius: 6px;
  background: transparent;
  color: #9CA3AF;
  cursor: pointer;
  font-family: var(--font-ui, 'Inter', sans-serif);
  transition: all 150ms ease;
  flex-shrink: 0;
}
.date-chip:hover { border-color: #FFFFFF; color: #FFFFFF; background: rgba(255,255,255,0.04); }
.date-chip--selected { border-color: #FFFFFF; background: rgba(255,255,255,0.12); color: #FFFFFF; }

.date-chip__num { font-size: 15px; font-weight: 700; line-height: 1; }
.date-chip__dow { font-size: 9px; font-weight: 600; text-transform: uppercase; }

.selected-dates-summary {
  margin-top: 6px;
  font-size: 12px;
  color: #9CA3AF;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.btn-clear-dates {
  padding: 2px 8px;
  border: 1px solid #374151;
  border-radius: 4px;
  background: transparent;
  color: #9CA3AF;
  font-size: 11px;
  cursor: pointer;
  transition: color 150ms, border-color 150ms;
}
.btn-clear-dates:hover { color: #EF4444; border-color: #EF4444; }

/* ── Batch result display ── */
.batch-result {
  margin-top: 12px;
  padding: 12px;
  border-radius: 8px;
  background: rgba(255,255,255,0.03);
  border: 1px solid #374151;
  font-size: 13px;
}
.batch-ok { color: #10B981; margin: 0 0 6px; font-weight: 600; }
.batch-fail { color: #FCA5A5; }
.batch-fail p { margin: 0 0 4px; font-weight: 600; }
.batch-fail ul { margin: 0; padding-left: 16px; }
.batch-fail li { margin-bottom: 2px; color: #E5E5E5; }
.batch-fail li strong { color: #FCA5A5; }

/* ── Mode toggle ── */
.modal--wide { width: min(680px, 100%); }

.mode-toggle {
  display: flex;
  gap: 0;
  border: 1px solid #374151;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}
.mode-btn {
  flex: 1;
  padding: 10px 14px;
  border: none;
  border-right: 1px solid #374151;
  background: transparent;
  color: #9CA3AF;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 150ms, color 150ms;
  text-align: center;
}
.mode-btn:last-child { border-right: none; }
.mode-btn:hover { color: #E5E5E5; }
.mode-btn--active { background: rgba(255,255,255,0.08); color: #FFFFFF; }

/* ── Room multi-checkbox list ── */
.room-check-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 140px;
  overflow-y: auto;
  padding: 8px;
  background: #111827;
  border: 1px solid #374151;
  border-radius: 8px;
}
.room-check-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #E5E5E5;
  cursor: pointer;
}
.room-checkbox {
  width: 14px; height: 14px;
  accent-color: #FFFFFF;
  cursor: pointer;
  flex-shrink: 0;
}
.date-nav {
  display: inline-flex;
  align-items: center;
  gap: 0;
  border: 1px solid #374151;
  border-radius: 8px;
  overflow: hidden;
  height: 40px;
}

.date-nav__arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 100%;
  border: none;
  background: #111827;
  color: #9CA3AF;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 150ms ease, color 150ms ease;
}
.date-nav__arrow:hover {
  background: #1F2937;
  color: #FFFFFF;
}
.date-nav__arrow:active {
  background: #374151;
}

.date-nav__input {
  /* Override the standalone filter-select border/radius — it lives inside date-nav */
  border: none !important;
  border-left: 1px solid #374151 !important;
  border-right: 1px solid #374151 !important;
  border-radius: 0 !important;
  height: 38px;
  min-height: unset;
  padding: 0 10px;
  margin: 0;
  width: 148px;
  flex-shrink: 0;
}
</style>

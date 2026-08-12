<template>
  <div class="schedule-page">
    <transition name="toast">
      <div v-if="toast.show" :class="['toast', `toast--${toast.type}`]">{{ toast.msg }}</div>
    </transition>

    <!-- Toolbar -->
    <div class="toolbar">
      <select v-model="filterRap" class="filter-select" @change="page=0">
        <option value="">Tất cả rạp</option>
        <option v-for="r in raps" :key="r.id" :value="r.id">{{ r.tenRap }}</option>
      </select>
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

    <!-- Table — flat when no cinema filter, grouped by room when cinema selected -->
    <div class="card table-card">
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>

      <!-- ── FLAT VIEW (no cinema filter) ── -->
      <template v-else-if="!filterRap">
        <div v-if="filtered.length===0" class="state-center empty-text">Không có lịch chiếu</div>
        <div v-else class="table-scroll">
          <table class="data-table">
            <thead>
              <tr>
                <th>Phim</th><th>Tên rạp</th><th>Phòng chiếu</th><th>Định dạng</th><th>Ngày</th>
                <th>Giờ bắt đầu</th><th>Giờ kết thúc</th><th>Nghỉ (p)</th><th>Giá</th><th>Trạng thái</th><th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="lc in filtered" :key="lc.id">
                <td class="td-movie">{{ lc.phim?.tenPhim || '—' }}</td>
                <td class="td-rap">{{ lc.phongChieu?.rapChieu?.tenRap || '—' }}</td>
                <td>{{ lc.phongChieu?.tenPhong || '—' }}</td>
                <td><span class="sbadge sbadge--gray">{{ lc.phongChieu?.dinhDang?.tenDinhDang || lc.phongChieu?.loaiPhong || '—' }}</span></td>
                <td class="td-date">{{ fmtDate(lc.thoiGianBatDau) }}</td>
                <td class="td-time">{{ fmtTime(lc.thoiGianBatDau) }}</td>
                <td class="td-time">{{ fmtTime(lc.thoiGianKetThuc) }}</td>
                <td class="td-time">{{ lc.thoiGianNghi ?? 15 }} p</td>
                <td class="td-price">{{ fmtPrice(lc.giaCoBan) }}</td>
                <td><span :class="['sbadge', lc.trangThai==='active'?'sbadge--green':'sbadge--gray']">{{ lc.trangThai }}</span></td>
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
      </template>

      <!-- ── GROUPED VIEW (cinema filter active) ── -->
      <template v-else>
        <div v-if="groupedByPhong.length === 0" class="state-center empty-text">Không có lịch chiếu</div>
        <div v-else>
          <div v-for="group in groupedByPhong" :key="group.phongId" class="room-group">
            <div class="room-group__header">
              <span class="room-group__name">{{ group.tenPhong }}</span>
              <span class="sbadge sbadge--gray room-group__badge">{{ group.dinhDang || '—' }}</span>
              <span class="room-group__count">{{ group.lichChieus.length }} suất</span>
            </div>
            <div v-if="group.lichChieus.length === 0" class="room-group__empty">Không có lịch chiếu phù hợp</div>
            <div v-else class="table-scroll">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Phim</th><th>Ngày</th><th>Giờ bắt đầu</th><th>Giờ kết thúc</th>
                    <th>Nghỉ (p)</th><th>Giá</th><th>Trạng thái</th><th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="lc in group.lichChieus" :key="lc.id">
                    <td class="td-movie">{{ lc.phim?.tenPhim || '—' }}</td>
                    <td class="td-date">{{ fmtDate(lc.thoiGianBatDau) }}</td>
                    <td class="td-time">{{ fmtTime(lc.thoiGianBatDau) }}</td>
                    <td class="td-time">{{ fmtTime(lc.thoiGianKetThuc) }}</td>
                    <td class="td-time">{{ lc.thoiGianNghi ?? 15 }} p</td>
                    <td class="td-price">{{ fmtPrice(lc.giaCoBan) }}</td>
                    <td><span :class="['sbadge', lc.trangThai==='active'?'sbadge--green':'sbadge--gray']">{{ lc.trangThai }}</span></td>
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
        </div>
      </template>
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
              <select v-model="form.phimId" @change="onPhimChange">
                <option value="">-- Chọn phim --</option>
                <option v-for="m in movies" :key="m.id" :value="m.id">{{ m.tenPhim }}</option>
              </select>
            </div>
            <div class="field field-full">
              <label>Rạp chiếu * <span v-if="!form.phimId" class="hint">(chọn phim trước)</span></label>
              <select v-model="form.rapChieuId" :disabled="!form.phimId">
                <option value="">-- Chọn rạp --</option>
                <option v-for="r in filteredRaps" :key="r.id" :value="r.id">{{ r.tenRap }}</option>
              </select>
            </div>
            <div class="field field-full">
              <label>Phòng chiếu * <span v-if="!form.rapChieuId" class="hint">(chọn rạp trước)</span></label>
              <select v-model="form.phongChieuId" :disabled="!form.rapChieuId">
                <option value="">{{ form.rapChieuId ? '-- Chọn phòng --' : '-- Chọn rạp trước --' }}</option>
                <option v-for="p in filteredPhongs" :key="p.id" :value="p.id">{{ p.tenPhong }} ({{ p.dinhDang?.tenDinhDang || p.loaiPhong }})</option>
              </select>
            </div>
            <!-- Edit mode: date is read-only -->
            <div v-if="editing" class="field field-full">
              <label>Ngày chiếu <span class="hint">(không thể thay đổi)</span></label>
              <input :value="form.dates[0] || ''" type="date" readonly disabled />
              <p class="field-hint">Ngày chiếu không thể thay đổi khi chỉnh sửa lịch chiếu.</p>
            </div>
            <!-- Add mode: multi-date chip selector with validation indicators -->
            <div v-else class="field field-full">
              <label>Ngày chiếu * <span class="hint">(chọn một hoặc nhiều ngày)</span></label>
              <div class="date-chip-grid">
                <button
                  v-for="d in availableDays" :key="d.iso" type="button"
                  :class="['date-chip',
                    { 'date-chip--selected': form.dates.includes(d.iso) },
                    { 'date-chip--valid':   dateValidation[d.iso] === true },
                    { 'date-chip--invalid': dateValidation[d.iso] === false }
                  ]"
                  :title="dateValidMsg[d.iso] || ''"
                  @click="toggleDate(d.iso)"
                >
                  <span v-if="form.dates.includes(d.iso)" class="date-chip__check">✓</span>
                  <span class="date-chip__num">{{ d.num }}</span>
                  <span class="date-chip__dow">{{ d.dow }}</span>
                </button>
              </div>
              <div v-if="form.dates.length > 0" class="selected-dates-summary">
                {{ form.dates.length }} ngày đã chọn: {{ form.dates.slice(0,3).join(', ') }}{{ form.dates.length > 3 ? ' ...' : '' }}
                <button type="button" class="btn-clear-dates" @click="form.dates = []">Xóa tất cả</button>
              </div>
            </div>
            <div v-if="!editing" class="field"><label>Giờ bắt đầu *</label><input v-model="form.startTime" type="time" @change="onFirstRowTimeChange" /></div>
            <div v-if="!editing" class="field"><label>Thời lượng (phút)</label><input v-model.number="form.durationMin" type="number" min="30" disabled title="Thời lượng được lấy tự động từ phim" /></div>
            <div v-if="!editing" class="field">
              <label>Thời gian nghỉ (phút)</label>
              <input v-model.number="form.thoiGianNghi" type="number" min="10" max="60" @change="onBreakTimeChange" />
              <p class="field-hint">Khoảng nghỉ sau phim trước suất tiếp theo. Mặc định: 15 phút.</p>
            </div>

            <!-- ── CONSECUTIVE SHOWTIME ROWS (add mode only) ── -->
            <div v-if="!editing" class="field field-full show-rows-section">
              <div class="show-rows-header">
                <label>Danh sách suất chiếu *</label>
                <span class="show-rows-hint">Mỗi suất = {{ form.durationMin }} p phim + {{ form.thoiGianNghi }} p nghỉ</span>
              </div>
              <div class="show-rows-list">
                <div v-for="(row, idx) in showRows" :key="idx" class="show-row">
                  <span class="show-row__idx">{{ idx + 1 }}</span>
                  <div class="show-row__time">
                    <input
                      type="time"
                      :value="row.startTime"
                      @change="onRowTimeEdit(idx, $event.target.value)"
                      class="show-row__input"
                    />
                  </div>
                  <span class="show-row__arrow">→</span>
                  <span class="show-row__end">{{ computeEndTime(row.startTime) }}</span>
                  <button
                    v-if="showRows.length > 1"
                    type="button"
                    class="show-row__del"
                    @click="removeShowRow(idx)"
                    title="Xóa suất này"
                  >🗑</button>
                  <span v-if="row.err" class="show-row__err">⚠ {{ row.err }}</span>
                </div>
              </div>
              <button type="button" class="btn-add-show" @click="addShowRow" :disabled="!form.startTime">
                ＋ Thêm suất chiếu
              </button>
            </div>

            <!-- Edit mode: single time field (unchanged) -->
            <div v-if="editing" class="field"><label>Giờ bắt đầu *</label><input v-model="form.startTime" type="time" @change="validateDates" /></div>
            <div v-if="editing" class="field"><label>Thời lượng (phút)</label><input v-model.number="form.durationMin" type="number" min="30" disabled /></div>
            <div v-if="editing" class="field">
              <label>Thời gian nghỉ (phút)</label>
              <input v-model.number="form.thoiGianNghi" type="number" min="10" max="60" @change="validateDates" />
            </div>

            <div class="field"><label>Giá cơ bản (VND) *</label><input v-model.number="form.giaCoBan" type="number" min="0" step="1000" /></div>
          </div>
          <div v-if="conflict" class="conflict-warn">⚠️ {{ conflict }}</div>
          <p v-if="formErr" class="form-err">{{ formErr }}</p>
          <div v-if="batchResult" class="batch-result">
            <p v-if="batchResult.totalSucceeded > 0" class="batch-ok">
              ✅ Tạo thành công {{ batchResult.totalSucceeded }}/{{ batchResult.totalRequested }} suất:
              {{ batchResult.succeeded.map(s => s.date + (s.startTime ? ' ' + s.startTime : '')).slice(0,4).join(', ') }}{{ batchResult.succeeded.length > 4 ? ' ...' : '' }}
            </p>
            <div v-if="batchResult.failed.length > 0" class="batch-fail">
              <p>❌ {{ batchResult.failed.length }} suất thất bại:</p>
              <ul>
                <li v-for="f in batchResult.failed" :key="f.date + (f.startTime||'')">
                  <strong>{{ f.date }}{{ f.startTime ? ' ' + f.startTime : '' }}</strong>: {{ f.reason }}
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
import { ref, computed, onMounted, reactive, watch, nextTick } from 'vue'
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
const filterRap   = ref('')

// Mode toggle: 'manual' | 'import'
const mode = ref('manual')

// Cinema + room refs for manual mode
const raps = ref([])  // all cinemas — populated by load()

// ── Consecutive showtime rows (add mode) ──────────────────────
// Each row: { startTime: 'HH:mm', manuallyEdited: false, err: '' }
const showRows = ref([{ startTime: '', manuallyEdited: false, err: '' }])

/** Compute end time string for a given start time using current form duration + break */
function computeEndTime(st) {
  if (!st || !form.value.durationMin) return '—'
  const [h, m] = st.split(':').map(Number)
  const totalMin = h * 60 + m + (form.value.durationMin || 120) + (form.value.thoiGianNghi || 15)
  const endH = Math.floor(totalMin / 60) % 24
  const endM = totalMin % 60
  return `${String(endH).padStart(2,'0')}:${String(endM).padStart(2,'0')}`
}

/** Add next consecutive row based on the last row's suggested end time */
function addShowRow() {
  const last = showRows.value[showRows.value.length - 1]
  const nextStart = computeEndTime(last.startTime)  // next start = previous end
  showRows.value.push({ startTime: nextStart === '—' ? '' : nextStart, manuallyEdited: false, err: '' })
}

function removeShowRow(idx) {
  showRows.value.splice(idx, 1)
}

function onRowTimeEdit(idx, val) {
  showRows.value[idx].startTime = val
  showRows.value[idx].manuallyEdited = true
  showRows.value[idx].err = ''
  // Also keep form.startTime in sync with row 0
  if (idx === 0) form.value.startTime = val
  validateDates()
}

/** When the top-level "Giờ bắt đầu" input changes, update row 0 unless manually edited */
function onFirstRowTimeChange() {
  if (!showRows.value[0].manuallyEdited) {
    showRows.value[0].startTime = form.value.startTime
  }
  validateDates()
}

/** When break time changes, re-suggest times for non-manually-edited rows */
function onBreakTimeChange() {
  // Recompute suggested starts for rows > 0 that were not manually edited
  for (let i = 1; i < showRows.value.length; i++) {
    if (!showRows.value[i].manuallyEdited) {
      showRows.value[i].startTime = computeEndTime(showRows.value[i - 1].startTime)
    }
  }
  validateDates()
}

// ── Manual form state ─────────────────────────────────────────
const form = ref({ phimId:'', rapChieuId:'', phongChieuId:'', dates:[], startTime:'', durationMin:120, thoiGianNghi:15, giaCoBan:80000 })

// Keep row 0 in sync when form.startTime is set from openAdd/openEdit
watch(() => form.value.startTime, (val) => {
  if (showRows.value.length > 0 && !showRows.value[0].manuallyEdited) {
    showRows.value[0].startTime = val || ''
  }
})

// Date validation state: { 'yyyy-MM-dd': true|false|null }
const dateValidation = ref({})
const dateValidMsg   = ref({})
let _validateTimer = null

// Format-filtered raps and rooms
const selectedPhimDinhDangIds = ref([])  // format IDs for the selected movie

const filteredRaps = computed(() => {
  if (!selectedPhimDinhDangIds.value.length) return raps.value
  // Only show cinemas that have at least one room matching any of the movie's formats
  const matchIds = new Set(selectedPhimDinhDangIds.value)
  return raps.value.filter(rap =>
    phongs.value.some(p => p.rapChieu?.id === rap.id && p.dinhDang?.id && matchIds.has(p.dinhDang.id))
  )
})

// Filtered rooms for the manual mode dropdown (cinema + format)
const filteredPhongs = computed(() => {
  if (!form.value.rapChieuId) return []
  let list = phongs.value.filter(p => p.rapChieu?.id === Number(form.value.rapChieuId))
  if (selectedPhimDinhDangIds.value.length) {
    const matchIds = new Set(selectedPhimDinhDangIds.value)
    list = list.filter(p => p.dinhDang?.id && matchIds.has(p.dinhDang.id))
  }
  return list
})

// When cinema changes, clear selected room
watch(() => form.value.rapChieuId, () => {
  form.value.phongChieuId = ''
  dateValidation.value = {}
})

// When room/time/break-time changes, re-validate dates
watch(() => form.value.phongChieuId, () => validateDates())

function onPhimChange() {
  // Extract dinhDang IDs from the selected movie
  const m = movies.value.find(mv => mv.id == form.value.phimId)
  selectedPhimDinhDangIds.value = m?.dinhDangs?.map(d => d.id) ?? []
  // Set duration from movie
  if (m?.thoiLuong) form.value.durationMin = m.thoiLuong
  form.value.rapChieuId = ''
  form.value.phongChieuId = ''
  dateValidation.value = {}
}

async function validateDates() {
  const { phongChieuId, phimId, startTime, durationMin, thoiGianNghi } = form.value
  if (!phongChieuId || !phimId || !startTime) return
  clearTimeout(_validateTimer)
  _validateTimer = setTimeout(async () => {
    const results = {}
    const messages = {}
    await Promise.allSettled(
      availableDays.value.map(async d => {
        try {
          const res = await api.get('/admin/lich-chieu/validate-date', {
            params: {
              phongChieuId: Number(phongChieuId),
              ngay: d.iso,
              thoiGianBatDau: startTime,
              thoiGianNghi: thoiGianNghi ?? 15,
              phimId: Number(phimId),
              excludeId: editing.value?.id ?? undefined,
            }
          })
          results[d.iso] = res.data.valid
          messages[d.iso] = res.data.reason || ''
        } catch {
          results[d.iso] = null
        }
      })
    )
    dateValidation.value = results
    dateValidMsg.value = messages
  }, 400)
}

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

// Groups filtered showtimes by room — used in the cinema-filter view
const groupedByPhong = computed(() => {
  const map = new Map()
  for (const lc of filtered.value) {
    const pid  = lc.phongChieu?.id ?? 0
    if (!map.has(pid)) {
      map.set(pid, {
        phongId:   pid,
        tenPhong:  lc.phongChieu?.tenPhong || '(Phòng không xác định)',
        dinhDang:  lc.phongChieu?.dinhDang?.tenDinhDang || lc.phongChieu?.loaiPhong || '',
        lichChieus: [],
      })
    }
    map.get(pid).lichChieus.push(lc)
  }
  // Sort rooms by tenPhong, showtimes ascending by start time
  const groups = [...map.values()].sort((a, b) => a.tenPhong.localeCompare(b.tenPhong))
  groups.forEach(g => g.lichChieus.sort((a, b) => {
    const ta = a.thoiGianBatDau ? new Date(a.thoiGianBatDau).getTime() : 0
    const tb = b.thoiGianBatDau ? new Date(b.thoiGianBatDau).getTime() : 0
    return ta - tb
  }))
  return groups
})

function fmtDate(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString('vi-VN')
}
function fmtTime(dt) {
  if (!dt) return '—'
  const d = new Date(dt)
  const m = String(d.getMinutes()).padStart(2, '0')
  let h = d.getHours()
  const ampm = h < 12 ? 'AM' : 'PM'
  h = h % 12 || 12
  return `${String(h).padStart(2, '0')}:${m} ${ampm}`
}
function fmtPrice(v) {
  return new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(v||0)
}

function openAdd() {
  editing.value = null
  mode.value = 'manual'
  form.value = { phimId:'', rapChieuId:'', phongChieuId:'', dates:[], startTime:'', durationMin:120, thoiGianNghi:15, giaCoBan:80000 }
  selectedPhimDinhDangIds.value = []
  showRows.value = [{ startTime: '', manuallyEdited: false, err: '' }]
  formErr.value=''; conflict.value=''; batchResult.value=null
  dateValidation.value={}; dateValidMsg.value={}
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

  // Duration = endTime - startTime - breakTime (or full slot if breakTime absent)
  const breakMin = lc.thoiGianNghi ?? 15
  let durationMin = 120
  if (startParsed && endParsed) {
    const toMin = (hhmm) => { const [h, m] = hhmm.split(':').map(Number); return h * 60 + m }
    const dayDiff = startParsed.date !== endParsed.date
      ? (new Date(endParsed.date) - new Date(startParsed.date)) / 86400000 : 0
    durationMin = Math.max(30, toMin(endParsed.time) - toMin(startParsed.time) + dayDiff * 1440 - breakMin)
  }

  // Detect movie formats for format-filtered dropdowns
  const m = movies.value.find(mv => mv.id == lc.phim?.id)
  selectedPhimDinhDangIds.value = m?.dinhDangs?.map(d => d.id) ?? []

  const targetPhongChieuId = lc.phongChieu?.id || ''

  form.value = {
    phimId:       lc.phim?.id               || '',
    rapChieuId:   lc.phongChieu?.rapChieu?.id || '',
    phongChieuId: '',
    dates:        startParsed?.date ? [startParsed.date] : [],
    startTime:    startParsed?.time  || '',
    durationMin:  durationMin,
    thoiGianNghi: breakMin,
    giaCoBan:     lc.giaCoBan        || 80000,
  }
  batchResult.value = null
  dateValidation.value = {}; dateValidMsg.value = {}
  showModal.value = true
  nextTick(() => { form.value.phongChieuId = targetPhongChieuId })
}

async function save() {
  const { phimId, phongChieuId, dates, startTime, durationMin, thoiGianNghi, giaCoBan } = form.value
  if (!phimId || !phongChieuId || !startTime) { formErr.value='Vui lòng điền đầy đủ thông tin *'; return }

  saving.value=true; formErr.value=''; conflict.value=''; batchResult.value=null

  const breakMin = thoiGianNghi ?? 15
  const totalEndMin = parseInt(startTime.split(':')[0])*60 + parseInt(startTime.split(':')[1]) + durationMin + breakMin
  const endH = Math.floor(totalEndMin / 60) % 24
  const endMin = totalEndMin % 60
  const endTime = `${String(endH).padStart(2,'0')}:${String(endMin).padStart(2,'0')}`

  try {
    if (editing.value) {
      if (!dates.length) { formErr.value='Vui lòng chọn ít nhất một ngày'; saving.value=false; return }
      const date = dates[0]
      const [sH, sM] = startTime.split(':').map(Number)
      const totalEnd = sH * 60 + sM + durationMin + breakMin
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
        thoiGianNghi: breakMin,
        giaCoBan,
      }
      await api.put(`/admin/lich-chieu/${editing.value.id}`, payload)
      showToast('Đã cập nhật lịch chiếu', 'success')
      showModal.value = false
      await load()
    } else {
      if (!dates.length) { formErr.value='Vui lòng chọn ít nhất một ngày'; saving.value=false; return }
      // Build shows array from showRows — use all rows that have a startTime
      const validRows = showRows.value.filter(r => r.startTime)
      if (!validRows.length) { formErr.value='Vui lòng nhập giờ bắt đầu cho ít nhất một suất'; saving.value=false; return }

      // Build shows payload: each row gets its own startTime/endTime/thoiGianNghi/giaCoBan
      const shows = validRows.map(row => {
        const [sH, sM] = row.startTime.split(':').map(Number)
        const totalEnd = sH * 60 + sM + durationMin + breakMin
        const edH = Math.floor(totalEnd / 60) % 24
        const edM = totalEnd % 60
        return {
          startTime: row.startTime,
          endTime: `${String(edH).padStart(2,'0')}:${String(edM).padStart(2,'0')}`,
          thoiGianNghi: breakMin,
          giaCoBan,
        }
      })

      // Use single-show legacy path when there's only one row (keeps backwards compat for date-chip validation display)
      let payload
      if (shows.length === 1) {
        payload = { phimId: Number(phimId), phongChieuId: Number(phongChieuId), startTime: shows[0].startTime, endTime: shows[0].endTime, thoiGianNghi: breakMin, giaCoBan, dates }
      } else {
        payload = { phimId: Number(phimId), phongChieuId: Number(phongChieuId), shows, dates }
      }
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

    // Pass rapChieuId to backend when cinema filter is active (also raises size to 500)
    if (filterRap.value) scheduleParams.rapChieuId = filterRap.value

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
watch(filterRap,  () => { load() })
watch(() => shell.searchTick, syncFromShell)

onMounted(() => {
  syncFromShell()
  load()
})
</script>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; }

.schedule-page { display: flex; flex-direction: column; gap: 0; background: var(--admin-bg); color: var(--admin-text); }

/* ── Cell utilities ── */
.td-movie  { font-weight: 600; font-size: 14px; color: var(--admin-text); max-width: 160px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.td-rap    { font-size: 13px; color: #29bcea; font-weight: 600; max-width: 140px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.td-date   { font-size: 12px; color: var(--admin-text-muted); }
.td-time   { font-size: 13px; color: var(--admin-text); font-variant-numeric: tabular-nums; }
.td-price  { font-weight: 700; color: var(--admin-accent); }
.td-rownum { font-size: 12px; color: var(--admin-text-muted); font-weight: 700; }
.td-status { max-width: 260px; }

/* ── Multi-date chip selector ── */
.hint { font-size: 11px; color: var(--admin-text-muted); font-weight: 400; margin-left: 4px; }

.date-chip-grid {
  display: flex; flex-wrap: wrap; gap: 6px; padding: 8px;
  background: var(--admin-surface); border: 1px solid var(--admin-border); border-radius: 8px;
  max-height: 160px; overflow-y: auto;
}
.date-chip-grid::-webkit-scrollbar { width: 4px; }
.date-chip-grid::-webkit-scrollbar-thumb { background: var(--admin-border); border-radius: 2px; }

.date-chip {
  position: relative;
  display: flex; flex-direction: column; align-items: center; gap: 1px;
  width: 44px; padding: 6px 4px;
  border: 1px solid var(--admin-border); border-radius: 6px; background: transparent; color: var(--admin-text-muted);
  cursor: pointer; font-family: var(--font-ui, 'Inter', sans-serif);
  transition: all 150ms ease; flex-shrink: 0;
}
.date-chip:hover { border-color: var(--admin-accent); color: var(--admin-accent); background: var(--admin-accent-muted); }
.date-chip--selected { border-color: var(--admin-accent); background: rgba(255,255,255,0.12); color: var(--admin-accent); }
/* Date validation indicators */
.date-chip--valid   { border-color: #10B981; background: rgba(16,185,129,0.10); color: #10B981; }
.date-chip--invalid { border-color: #EF4444; background: rgba(239,68,68,0.08);  color: #EF4444; }
/* Selected + valid/invalid: selection wins visually */
.date-chip--selected.date-chip--valid   { background: rgba(16,185,129,0.20); }
.date-chip--selected.date-chip--invalid { background: rgba(239,68,68,0.18); }
.date-chip__num { font-size: 15px; font-weight: 700; line-height: 1; }
.date-chip__dow { font-size: 9px; font-weight: 600; text-transform: uppercase; }
.date-chip__check {
  position: absolute; top: 2px; right: 4px;
  font-size: 10px; font-weight: 900; line-height: 1;
  color: #fff; background: #10B981;
  border-radius: 50%; width: 13px; height: 13px;
  display: flex; align-items: center; justify-content: center;
}

.selected-dates-summary {
  margin-top: 6px; font-size: 12px; color: var(--admin-text-muted);
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
}
.btn-clear-dates {
  padding: 2px 8px; border: 1px solid var(--admin-border); border-radius: 4px;
  background: transparent; color: var(--admin-text-muted); font-size: 11px; cursor: pointer;
  transition: color 150ms, border-color 150ms;
}
.btn-clear-dates:hover { color: #EF4444; border-color: #EF4444; }

/* ── Batch result display ── */
.batch-result {
  margin-top: 12px; padding: 12px; border-radius: 8px;
  background: var(--admin-accent-muted); border: 1px solid var(--admin-border); font-size: 13px;
}
.batch-ok { color: #10B981; margin: 0 0 6px; font-weight: 600; }
.batch-fail { color: #FCA5A5; }
.batch-fail p { margin: 0 0 4px; font-weight: 600; }
.batch-fail ul { margin: 0; padding-left: 16px; }
.batch-fail li { margin-bottom: 2px; color: var(--admin-text); }
.batch-fail li strong { color: #FCA5A5; }

/* ── Mode toggle ── */
.modal--wide { width: min(720px, 100%); }
.mode-toggle {
  display: flex; gap: 0; border: 1px solid var(--admin-border); border-radius: 8px;
  overflow: hidden; margin-bottom: 20px;
}
.mode-btn {
  flex: 1; padding: 10px 14px; border: none; border-right: 1px solid var(--admin-border);
  background: transparent; color: var(--admin-text-muted);
  font-family: var(--font-ui, 'Inter', sans-serif); font-size: 13px; font-weight: 600;
  cursor: pointer; transition: background 150ms, color 150ms; text-align: center;
}
.mode-btn:last-child { border-right: none; }
.mode-btn:hover { color: var(--admin-text); }
.mode-btn--active { background: var(--admin-accent-muted); color: var(--admin-accent); }

/* ── Excel import ── */
.import-hint { margin-bottom: 16px; }
.import-format-note {
  font-size: 12px; color: var(--admin-text-muted); background: var(--admin-surface); border: 1px solid var(--admin-border);
  border-radius: 8px; padding: 10px 14px; line-height: 1.6; margin: 0;
}
.import-format-note strong { color: var(--admin-text); }

.import-upload-row {
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 16px;
}
.upload-label {
  display: flex; align-items: center; gap: 8px; cursor: pointer; flex: 1; min-width: 0;
}
.file-input-hidden { display: none; }
.upload-btn {
  display: inline-flex; align-items: center; padding: 8px 16px;
  border: 1px solid var(--admin-border); border-radius: 8px; background: var(--admin-surface-hover);
  color: var(--admin-text); font-size: 13px; font-weight: 600; cursor: pointer; white-space: nowrap;
  transition: border-color 150ms, background 150ms;
}
.upload-btn:hover { border-color: var(--admin-accent); background: var(--admin-surface-hover); }
.upload-filename {
  font-size: 12px; color: var(--admin-text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.import-preview-wrap { margin-top: 12px; }
.import-preview-summary {
  display: flex; align-items: center; gap: 12px; padding: 8px 0; margin-bottom: 8px;
  font-size: 13px; font-weight: 600;
}
.preview-ok   { color: #10B981; }
.preview-fail { color: #F87171; }
.preview-total { color: var(--admin-text-muted); font-weight: 400; }

.import-table { font-size: 12px; }
.row-invalid { background: rgba(239,68,68,0.06); }
.import-err-msg { color: #FCA5A5; font-size: 11px; line-height: 1.4; }

/* ── Consecutive showtime rows ── */
.show-rows-section { border: 1px solid var(--admin-border); border-radius: 8px; padding: 12px 14px; background: var(--admin-surface); }
.show-rows-header  { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.show-rows-header label { margin-bottom: 0; }
.show-rows-hint    { font-size: 11px; color: var(--admin-text-muted); font-family: var(--font-ui); }
.show-rows-list    { display: flex; flex-direction: column; gap: 6px; margin-bottom: 10px; }
.show-row          { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.show-row__idx     { width: 20px; text-align: right; font-size: 12px; font-weight: 700; color: var(--admin-text-muted); flex-shrink: 0; }
.show-row__time    { flex-shrink: 0; }
.show-row__input   {
  width: 100px; min-height: 36px; padding: 6px 10px;
  border: 1px solid var(--admin-border) !important; border-radius: 6px !important;
  background: var(--admin-bg) !important; color: var(--admin-text) !important;
  font-family: var(--font-ui); font-size: 13px;
}
.show-row__input:focus { border-color: var(--admin-accent) !important; outline: none; }
.show-row__arrow   { color: var(--admin-text-muted); font-size: 12px; flex-shrink: 0; }
.show-row__end     { min-width: 50px; font-size: 13px; font-weight: 600; color: var(--admin-text); font-variant-numeric: tabular-nums; }
.show-row__del     {
  background: transparent; border: none; cursor: pointer;
  color: var(--admin-text-muted); font-size: 14px; padding: 2px 4px;
  border-radius: 4px; transition: color 120ms, background 120ms;
}
.show-row__del:hover { color: #EF4444; background: rgba(239,68,68,0.08); }
.show-row__err     { font-size: 11px; color: #EF4444; flex-basis: 100%; padding-left: 28px; }
.btn-add-show {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 6px 12px; border-radius: 6px;
  border: 1px dashed var(--admin-border); background: transparent;
  color: var(--admin-text-muted); font-size: 12px; font-weight: 600;
  font-family: var(--font-ui); cursor: pointer;
  transition: border-color 150ms, color 150ms, background 150ms;
}
.btn-add-show:hover:not(:disabled) { border-color: var(--admin-accent); color: var(--admin-accent); background: var(--admin-accent-muted); }
.btn-add-show:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── Room grouped view ── */
.room-group {
  border-bottom: 1px solid var(--admin-border);
}
.room-group:last-child { border-bottom: none; }

.room-group__header {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 20px 10px;
  background: var(--admin-surface-hover);
  border-bottom: 1px solid var(--admin-border);
}
.room-group__name {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px; font-weight: 700; color: var(--admin-text);
}
.room-group__badge { flex-shrink: 0; }
.room-group__count {
  margin-left: auto; font-size: 12px; color: var(--admin-text-muted); font-weight: 600;
}
.room-group__empty {
  padding: 16px 20px; font-size: 13px; color: var(--admin-text-muted);
  font-family: var(--font-ui, 'Inter', sans-serif);
}

/* ── Date nav ── */
.date-nav {  display: inline-flex; align-items: center; gap: 0;
  border: 1px solid var(--admin-border); border-radius: 8px; overflow: hidden; height: 40px;
}
.date-nav__arrow {
  display: flex; align-items: center; justify-content: center;
  width: 32px; height: 100%; border: none; background: var(--admin-surface); color: var(--admin-text-muted);
  cursor: pointer; flex-shrink: 0; transition: background 150ms ease, color 150ms ease;
}
.date-nav__arrow:hover { background: var(--admin-surface-hover); color: var(--admin-accent); }
.date-nav__arrow:active { background: var(--admin-surface-hover); }
.date-nav__input {
  border: none !important; border-left: 1px solid var(--admin-border) !important;
  border-right: 1px solid var(--admin-border) !important; border-radius: 0 !important;
  height: 38px; min-height: unset; padding: 0 10px; margin: 0; width: 148px; flex-shrink: 0;
}
</style>

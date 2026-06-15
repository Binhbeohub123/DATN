<template>
  <div class="cinemas-page">
    <!-- Tabs -->
    <div class="tabs">
      <button :class="['tab', { active: activeTab === 'rap' }]" @click="activeTab = 'rap'">Rạp chiếu</button>
      <button :class="['tab', { active: activeTab === 'phong' }]" @click="activeTab = 'phong'">Phòng chiếu</button>
      <button :class="['tab', { active: activeTab === 'ghe' }]" @click="activeTab = 'ghe'">Ghế ngồi</button>
    </div>

    <!-- RAP CHIEU TAB -->
    <div v-if="activeTab === 'rap'">
      <div class="toolbar">
        <h3>Danh sách rạp</h3>
        <button class="btn-primary" @click="openRapModal()">+ Thêm rạp</button>
      </div>
      <div class="card">
        <div v-if="loadingRap" class="loading-text">Đang tải...</div>
        <table v-else>
          <thead>
            <tr><th>ID</th><th>Tên rạp</th><th>Địa chỉ</th><th>Trạng thái</th><th>Thao tác</th></tr>
          </thead>
          <tbody>
            <tr v-for="rap in rapList" :key="rap.id">
              <td>#{{ rap.id }}</td>
              <td class="font-bold">{{ rap.tenRap }}</td>
              <td>{{ rap.diaChi }}</td>
              <td><span :class="['badge', rap.trangThai ? 'badge-green' : 'badge-gray']">{{ rap.trangThai ? 'Hoạt động' : 'Dừng' }}</span></td>
              <td>
                <div class="action-btns">
                  <button class="btn-edit" @click="openRapModal(rap)" title="Sửa">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="btn-delete" @click="deleteRap(rap)" title="Xóa">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="rapList.length === 0"><td colspan="5" class="empty-text">Chưa có rạp nào</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- PHONG CHIEU TAB -->
    <div v-if="activeTab === 'phong'">
      <div class="toolbar">
        <div class="toolbar-left">
          <h3>Danh sách phòng</h3>
          <select v-model="selectedRapId" class="filter-select" @change="loadPhong">
            <option value="">-- Chọn rạp --</option>
            <option v-for="r in rapList" :key="r.id" :value="r.id">{{ r.tenRap }}</option>
          </select>
        </div>
        <button class="btn-primary" @click="openPhongModal()">+ Thêm phòng</button>
      </div>
      <div class="card">
        <div v-if="loadingPhong" class="loading-text">Đang tải...</div>
        <table v-else>
          <thead>
            <tr><th>ID</th><th>Tên phòng</th><th>Loại</th><th>Sức chứa</th><th>Trạng thái</th><th>Thao tác</th></tr>
          </thead>
          <tbody>
            <tr v-for="phong in phongList" :key="phong.id">
              <td>#{{ phong.id }}</td>
              <td class="font-bold">{{ phong.tenPhong }}</td>
              <td><span class="badge badge-blue">{{ phong.loaiPhong }}</span></td>
              <td>{{ phong.sucChua }} ghế</td>
              <td><span :class="['badge', phong.trangThai ? 'badge-green' : 'badge-gray']">{{ phong.trangThai ? 'Hoạt động' : 'Dừng' }}</span></td>
              <td>
                <div class="action-btns">
                  <button class="btn-edit" @click="openPhongModal(phong)" title="Sửa">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="btn-delete" @click="deactivatePhong(phong)" :title="phong.trangThai ? 'Vô hiệu hóa phòng' : 'Phòng đã ngừng'">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="15" height="15"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="phongList.length === 0"><td colspan="6" class="empty-text">{{ selectedRapId ? 'Chưa có phòng nào' : 'Chọn rạp để xem phòng' }}</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- GHE NGOI TAB -->
    <div v-if="activeTab === 'ghe'">
      <div class="toolbar">
        <div class="toolbar-left">
          <h3>Sơ đồ ghế</h3>
          <select v-model="selectedPhongId" class="filter-select" @change="loadGhe">
            <option value="">-- Chọn phòng --</option>
            <option v-for="p in phongList" :key="p.id" :value="p.id">{{ p.tenPhong }}</option>
          </select>
        </div>
      </div>

      <div class="card">
        <div v-if="loadingGhe" class="loading-text">Đang tải...</div>
        <div v-else-if="gheList.length === 0" class="empty-text">Chọn phòng để xem sơ đồ ghế</div>
        <div v-else class="seat-map">

          <!-- STEP 5: Bulk action toolbar — only visible when seats are selected -->
          <div v-if="selectedSeatIds.size > 0" class="bulk-toolbar">
            <span class="bulk-count">✓ Đã chọn <strong>{{ selectedSeatIds.size }}</strong> ghế</span>
            <div class="bulk-actions">
              <span class="bulk-label">Đổi thành:</span>
              <select v-model="bulkLoaiGhe" class="bulk-select">
                <option value="thường">Thường</option>
                <option value="vip">VIP</option>
                <option value="cặp đôi">Cặp đôi</option>
              </select>
              <button class="btn-bulk-apply" @click="applyBulkEdit">Áp dụng</button>
              <button class="btn-bulk-clear" @click="selectedSeatIds = new Set()">Bỏ chọn</button>
            </div>
          </div>

          <div class="screen-label">— Màn hình —</div>

          <!-- STEP 4: Row label is now a clickable button to select/deselect entire row -->
          <div v-for="row in groupedGhe" :key="row.hang" class="seat-row">
            <button
              class="row-label-btn"
              :class="isRowFullySelected(row.hang) ? 'row-label-btn--active' : ''"
              :title="`Chọn/bỏ cả hàng ${row.hang.trim()}`"
              @click="toggleRow(row.hang)"
            >{{ row.hang.trim() }}</button>

            <div class="seats">
              <!-- STEP 2: :style for type colors, :class for selected ring indicator -->
              <button
                v-for="seat in row.seats"
                :key="seat.id"
                class="seat-chip"
                :class="selectedSeatIds.has(seat.id) ? 'seat-chip--selected' : ''"
                :style="seatStyle(seat.loaiGhe)"
                :title="`${seat.hangGhe?.trim()}${seat.soGhe} — ${seat.loaiGhe}`"
                @click="toggleSeat(seat)"
              >{{ seat.soGhe }}</button>
            </div>
          </div>

          <!-- STEP 2: Legend with inline style colors -->
          <div class="seat-legend">
            <span class="legend-item">
              <span class="chip chip--thuong"></span>Thường
            </span>
            <span class="legend-item">
              <span class="chip chip--vip"></span>VIP
            </span>
            <span class="legend-item">
              <span class="chip chip--doi"></span>Cặp đôi
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- RAP MODAL -->
    <div v-if="showRapModal" class="modal-overlay" @click.self="showRapModal = false">
      <div class="modal">
        <h2>{{ editingRap ? 'Sửa rạp' : 'Thêm rạp mới' }}</h2>
        <div class="form-group"><label>Tên rạp *</label><input v-model="rapForm.tenRap" placeholder="Tên rạp chiếu" /></div>
        <div class="form-group"><label>Địa chỉ</label><input v-model="rapForm.diaChi" placeholder="Địa chỉ rạp" /></div>
        <div class="form-group">
          <label>Trạng thái</label>
          <select v-model="rapForm.trangThai">
            <option :value="true">Hoạt động</option>
            <option :value="false">Dừng hoạt động</option>
          </select>
        </div>
        <div v-if="modalError" class="form-error">{{ modalError }}</div>
        <div class="modal-actions">
          <button class="btn-ghost" @click="showRapModal = false">Hủy</button>
          <button class="btn-primary" @click="saveRap" :disabled="savingRap">{{ savingRap ? 'Đang lưu...' : 'Lưu' }}</button>
        </div>
      </div>
    </div>

    <!-- PHONG MODAL -->
    <div v-if="showPhongModal" class="modal-overlay" @click.self="showPhongModal = false">
      <div class="modal">
        <h2>{{ editingPhong ? 'Sửa phòng' : 'Thêm phòng mới' }}</h2>
        <div class="form-group"><label>Tên phòng *</label><input v-model="phongForm.tenPhong" placeholder="Phòng 1, Hall A..." /></div>
        <div class="form-group">
          <label>Loại phòng</label>
          <select v-model="phongForm.loaiPhong">
            <option value="2D">2D</option><option value="3D">3D</option><option value="IMAX">IMAX</option><option value="4DX">4DX</option>
          </select>
        </div>
        <div class="form-group"><label>Sức chứa</label><input v-model.number="phongForm.sucChua" type="number" min="1" /></div>
        <div class="form-group">
          <label>Rạp chiếu</label>
          <select v-model="phongForm.rapChieuId">
            <option v-for="r in rapList" :key="r.id" :value="r.id">{{ r.tenRap }}</option>
          </select>
        </div>
        <div v-if="modalError" class="form-error">{{ modalError }}</div>
        <div class="modal-actions">
          <button class="btn-ghost" @click="showPhongModal = false">Hủy</button>
          <button class="btn-primary" @click="savePhong" :disabled="savingPhong">{{ savingPhong ? 'Đang lưu...' : 'Lưu' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '@/services/api'

const activeTab = ref('rap')

// ── Rạp chiếu ──────────────────────────────
const rapList = ref([])
const loadingRap = ref(false)
const showRapModal = ref(false)
const editingRap = ref(null)
const savingRap = ref(false)
const modalError = ref('')
const rapForm = ref({ tenRap: '', diaChi: '', trangThai: true })

async function loadRap() {
  loadingRap.value = true
  try {
    const res = await api.get('/rap-chieu')
    rapList.value = res.data || []
  } catch (e) { console.error(e) } finally { loadingRap.value = false }
}

function openRapModal(rap = null) {
  editingRap.value = rap
  modalError.value = ''
  if (rap) rapForm.value = { tenRap: rap.tenRap || '', diaChi: rap.diaChi || '', trangThai: rap.trangThai ?? true }
  else rapForm.value = { tenRap: '', diaChi: '', trangThai: true }
  showRapModal.value = true
}

async function saveRap() {
  if (!rapForm.value.tenRap.trim()) { modalError.value = 'Tên rạp không được để trống'; return }
  savingRap.value = true; modalError.value = ''
  try {
    if (editingRap.value) await api.put(`/admin/rap-chieu/${editingRap.value.id}`, rapForm.value)
    else await api.post('/admin/rap-chieu', rapForm.value)
    await loadRap(); showRapModal.value = false
  } catch (e) { modalError.value = e.response?.data?.message || 'Lỗi lưu rạp' }
  finally { savingRap.value = false }
}

async function deleteRap(rap) {
  if (!confirm(`Vô hiệu hóa rạp "${rap.tenRap}"?`)) return
  try { await api.delete(`/admin/rap-chieu/${rap.id}`); await loadRap() }
  catch (e) { alert(e.response?.data?.message || 'Lỗi xóa rạp') }
}

// ── Phòng chiếu ─────────────────────────────
const phongList = ref([])
const loadingPhong = ref(false)
const selectedRapId = ref('')
const showPhongModal = ref(false)
const editingPhong = ref(null)
const savingPhong = ref(false)
const phongForm = ref({ tenPhong: '', loaiPhong: '2D', sucChua: 100, rapChieuId: '' })

async function loadPhong() {
  if (!selectedRapId.value) { phongList.value = []; return }
  loadingPhong.value = true
  try {
    const res = await api.get(`/rap-chieu/${selectedRapId.value}/phong`)
    phongList.value = res.data || []
  } catch (e) { console.error(e) } finally { loadingPhong.value = false }
}

function openPhongModal(phong = null) {
  editingPhong.value = phong; modalError.value = ''
  if (phong) phongForm.value = { tenPhong: phong.tenPhong || '', loaiPhong: phong.loaiPhong || '2D', sucChua: phong.sucChua || 100, rapChieuId: selectedRapId.value }
  else phongForm.value = { tenPhong: '', loaiPhong: '2D', sucChua: 100, rapChieuId: selectedRapId.value }
  showPhongModal.value = true
}

async function savePhong() {
  if (!phongForm.value.tenPhong.trim()) { modalError.value = 'Tên phòng không được để trống'; return }
  savingPhong.value = true; modalError.value = ''
  try {
    const payload = { ...phongForm.value, rapChieu: { id: phongForm.value.rapChieuId } }
    if (editingPhong.value) await api.put(`/admin/phong-chieu/${editingPhong.value.id}`, payload)
    else await api.post('/admin/phong-chieu', payload)
    await loadPhong(); showPhongModal.value = false
  } catch (e) { modalError.value = e.response?.data?.message || 'Lỗi lưu phòng' }
  finally { savingPhong.value = false }
}

async function deactivatePhong(phong) {
  if (!phong.trangThai) { alert('Phòng này đã được vô hiệu hóa rồi.'); return }
  if (!confirm(`Vô hiệu hóa phòng "${phong.tenPhong}"?\nPhòng sẽ không thể dùng để xếp lịch chiếu mới.`)) return
  try {
    await api.delete(`/admin/phong-chieu/${phong.id}`)
    await loadPhong()
  } catch (e) { alert(e.response?.data?.message || 'Lỗi vô hiệu hóa phòng') }
}

// ── Ghế ngồi ────────────────────────────────
const gheList = ref([])
const loadingGhe = ref(false)
const selectedPhongId = ref('')

// STEP 3: Multi-select state
const selectedSeatIds = ref(new Set())
const bulkLoaiGhe = ref('thường')

// STEP 2: Inline style function — exact colors from SeatSelectionPage.vue
function seatStyle(loaiGhe) {
  if (loaiGhe === 'vip') return {
    background: '#C9A84C',
    borderColor: '#C9A84C',
    color: '#ffffff'
  }
  if (loaiGhe === 'cặp đôi') return {
    background: '#ec4899',
    borderColor: '#db2777',
    color: '#ffffff'
  }
  // 'thường' — default dark style, no inline override
  return {}
}

// STEP 3: Toggle individual seat selection
function toggleSeat(seat) {
  if (selectedSeatIds.value.has(seat.id)) {
    selectedSeatIds.value.delete(seat.id)
  } else {
    selectedSeatIds.value.add(seat.id)
  }
  selectedSeatIds.value = new Set(selectedSeatIds.value)
}

// STEP 4: Toggle entire row
function toggleRow(hangGhe) {
  const rowSeats = gheList.value.filter(g => g.hangGhe.trim() === hangGhe.trim())
  const allSelected = rowSeats.every(g => selectedSeatIds.value.has(g.id))
  if (allSelected) {
    rowSeats.forEach(g => selectedSeatIds.value.delete(g.id))
  } else {
    rowSeats.forEach(g => selectedSeatIds.value.add(g.id))
  }
  selectedSeatIds.value = new Set(selectedSeatIds.value)
}

// Helper: is every seat in a row selected?
function isRowFullySelected(hangGhe) {
  const rowSeats = gheList.value.filter(g => g.hangGhe.trim() === hangGhe.trim())
  return rowSeats.length > 0 && rowSeats.every(g => selectedSeatIds.value.has(g.id))
}

// STEP 6: Bulk apply
async function applyBulkEdit() {
  if (selectedSeatIds.value.size === 0) return
  const ids = [...selectedSeatIds.value]
  try {
    await Promise.all(ids.map(id =>
      api.put(`/admin/ghe-ngoi/${id}`, { loaiGhe: bulkLoaiGhe.value })
    ))
    // Update local state without re-fetching
    gheList.value = gheList.value.map(g =>
      selectedSeatIds.value.has(g.id) ? { ...g, loaiGhe: bulkLoaiGhe.value } : g
    )
    selectedSeatIds.value = new Set()
  } catch (err) {
    console.error('Bulk update failed', err)
    alert('Lỗi khi cập nhật ghế. Vui lòng thử lại.')
  }
}

const groupedGhe = computed(() => {
  const groups = {}
  gheList.value.forEach(g => {
    if (!groups[g.hangGhe]) groups[g.hangGhe] = []
    groups[g.hangGhe].push(g)
  })
  return Object.entries(groups)
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([hang, seats]) => ({ hang, seats: seats.sort((a, b) => a.soGhe - b.soGhe) }))
})

async function loadGhe() {
  if (!selectedPhongId.value) { gheList.value = []; return }
  selectedSeatIds.value = new Set()   // clear selection when room changes
  loadingGhe.value = true
  try {
    const res = await api.get(`/admin/ghe-ngoi?phongChieuId=${selectedPhongId.value}`)
    gheList.value = res.data || []
  } catch (e) { gheList.value = [] } finally { loadingGhe.value = false }
}

watch(activeTab, async (tab) => {
  if (tab === 'ghe') {
    if (rapList.value.length === 0) await loadRap()
    if (phongList.value.length === 0 && rapList.value.length > 0) {
      selectedRapId.value = rapList.value[0].id
      await loadPhong()
    }
  }
})

watch(selectedPhongId, (newId) => {
  if (newId) loadGhe()
})

onMounted(loadRap)
</script>

<style scoped>
/* ── Page root ── */
.cinemas-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: #0D0D0D;
  color: #E5E5E5;
}

/* ── Toolbar ── */
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-bottom: 16px;
}
.toolbar h3 {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 600;
  color: #FFFFFF;
  padding-left: 12px;
  border-left: 3px solid #FFFFFF;
  margin: 0;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* ── Filter select ── */
.filter-select {
  min-height: 40px;
  padding: 9px 36px 9px 14px;
  border: 1px solid #374151;
  border-radius: 8px;
  background: #111827;
  color: #E5E5E5;
  font-size: 14px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  -webkit-appearance: none;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%239CA3AF' stroke-width='1.5' fill='none' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  cursor: pointer;
  transition: border-color 150ms ease;
}
.filter-select:focus { outline: none; border-color: #FFFFFF; box-shadow: 0 0 0 2px rgba(255,255,255,0.15); }

/* ── Card ── */
.card {
  background: #111827;
  border: 1px solid #374151;
  border-radius: 12px;
  overflow: hidden;
}

/* ── Table ── */
table { width: 100%; border-collapse: collapse; }
thead tr { background: #0D0D0D; }
th {
  padding: 12px 16px;
  text-align: left;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #9CA3AF;
  border-bottom: 2px solid #374151;
  white-space: nowrap;
}
tbody tr { border-bottom: 1px solid #1F2937; transition: background 150ms ease; }
tbody tr:last-child { border-bottom: none; }
tbody tr:hover { background: #1F2937; }
td {
  padding: 14px 16px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  color: #E5E5E5;
  vertical-align: middle;
}
.font-bold { font-weight: 600; }

/* ── Badges ── */
.badge {
  display: inline-flex;
  align-items: center;
  border-radius: 9999px;
  padding: 3px 12px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}
.badge-green { background: rgba(16,185,129,0.15); color: #10B981; }
.badge-gray  { background: rgba(156,163,175,0.15); color: #9CA3AF; }
.badge-blue  { background: rgba(255,255,255,0.10); color: #FFFFFF; }

/* ── Action buttons ── */
.action-btns { display: flex; gap: 4px; align-items: center; }
.btn-edit, .btn-delete {
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
.btn-edit:hover   { color: #FFFFFF; background: rgba(255,255,255,0.10); }
.btn-delete:hover { color: #EF4444; background: rgba(239,68,68,0.1); }

/* ── Primary / ghost buttons ── */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 40px;
  padding: 9px 18px;
  border-radius: 8px;
  border: none;
  background: #FFFFFF;
  color: #0D0D0D;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: filter 150ms ease;
}
.btn-primary:hover { filter: brightness(1.1); }
.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 40px;
  padding: 9px 18px;
  border-radius: 8px;
  border: 1px solid #374151;
  background: transparent;
  color: #E5E5E5;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 150ms ease, color 150ms ease;
}
.btn-ghost:hover { border-color: #FFFFFF; color: #FFFFFF; }

/* ── Modal ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.65);
  backdrop-filter: blur(8px);
  padding: 20px;
}
.modal {
  width: min(480px, 100%);
  max-height: 90vh;
  overflow-y: auto;
  background: #1F2937;
  border: 1px solid #374151;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 24px 48px rgba(0,0,0,0.5);
  color: #E5E5E5;
}
.modal h2 {
  font-family: var(--font-display, 'Playfair Display', serif);
  font-size: 18px;
  font-weight: 700;
  color: #FFFFFF;
  margin: 0 0 20px;
  padding-left: 12px;
  border-left: 3px solid #FFFFFF;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}
.form-group label {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 600;
  color: #9CA3AF;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.form-group input, .form-group select {
  min-height: 40px;
  padding: 9px 14px;
  border: 1px solid #374151;
  border-radius: 8px;
  background: #111827;
  color: #E5E5E5;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  -webkit-appearance: none;
  appearance: none;
  transition: border-color 150ms ease;
}
.form-group input:focus, .form-group select:focus { outline: none; border-color: #FFFFFF; box-shadow: 0 0 0 2px rgba(255,255,255,0.15); }
.form-error { color: #EF4444; font-size: 13px; margin-top: 8px; }
.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

/* ── Loading / empty ── */
.loading-text, .empty-text {
  text-align: center;
  padding: 32px;
  color: #9CA3AF;
  font-size: 14px;
}

/* ── Bulk action toolbar ── */
.bulk-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  padding: 10px 16px;
  background: rgba(55,65,81,0.80);
  border: 1px solid #4B5563;
  border-radius: 10px;
}
.bulk-count {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 13px;
  color: #FFFFFF;
}
.bulk-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex-wrap: wrap;
}
.bulk-label {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 13px;
  color: #9CA3AF;
}
.bulk-select {
  min-height: 34px;
  padding: 5px 30px 5px 10px;
  border: 1px solid #4B5563;
  border-radius: 8px;
  background: #374151;
  color: #E5E5E5;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 13px;
  -webkit-appearance: none;
  appearance: none;
  cursor: pointer;
}
.btn-bulk-apply {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 5px 16px;
  border: none;
  border-radius: 8px;
  background: #2563EB;
  color: #FFFFFF;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 150ms ease;
}
.btn-bulk-apply:hover { background: #1D4ED8; }
.btn-bulk-clear {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 5px 12px;
  border: none;
  border-radius: 8px;
  background: #4B5563;
  color: #E5E5E5;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 13px;
  cursor: pointer;
  transition: background 150ms ease;
}
.btn-bulk-clear:hover { background: #6B7280; }

/* ── Seat map ── */
.seat-map { padding: 24px; }
.screen-label {
  text-align: center;
  padding: 8px;
  margin-bottom: 20px;
  background: #1F2937;
  border: 1px solid #374151;
  border-radius: 6px;
  font-size: 12px;
  color: #9CA3AF;
}
.seat-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }

/* Row label is now a button — STEP 4 */
.row-label-btn {
  width: 24px;
  min-width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 4px;
  background: transparent;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 12px;
  font-weight: 700;
  color: #9CA3AF;
  cursor: pointer;
  transition: color 150ms ease, background 150ms ease;
  flex-shrink: 0;
}
.row-label-btn:hover { color: #FFFFFF; background: rgba(255,255,255,0.06); }
.row-label-btn--active { color: #60A5FA; }

.seats { display: flex; flex-wrap: wrap; gap: 4px; }

/* STEP 2: Base seat chip — type colors come from :style, not :class */
.seat-chip {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  border: 1px solid #374151;
  background: #1F2937;
  color: #E5E5E5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: filter 150ms ease, outline 100ms ease;
  outline: 2px solid transparent;
  outline-offset: 1px;
}
.seat-chip:hover { filter: brightness(1.25); }

/* STEP 3: Selected state — blue ring */
.seat-chip--selected {
  outline: 2px solid #60A5FA;
  outline-offset: 2px;
}

/* ── Legend ── */
.seat-legend { display: flex; gap: 20px; margin-top: 20px; padding-top: 16px; border-top: 1px solid #374151; flex-wrap: wrap; }
.legend-item { display: flex; align-items: center; gap: 6px; font-family: var(--font-ui, 'Inter', sans-serif); font-size: 12px; font-weight: 600; color: #9CA3AF; }
.chip { width: 16px; height: 16px; border-radius: 3px; border: 1px solid transparent; }
.chip--thuong { background: #1F2937; border-color: #374151; }
.chip--vip    { background: #C9A84C; border-color: #C9A84C; }
.chip--doi    { background: #ec4899; border-color: #db2777; }
</style>

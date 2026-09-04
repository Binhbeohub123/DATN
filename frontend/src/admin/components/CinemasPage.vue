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
            <tr><th>ID</th><th>Tên rạp</th><th>Địa chỉ</th><th>Thành phố</th><th>Trạng thái</th><th>Thao tác</th></tr>
          </thead>
          <tbody>
            <tr v-for="rap in filteredRapList" :key="rap.id" :data-row-id="rap.id">
              <td>#{{ rap.id }}</td>
              <td class="font-bold">{{ rap.tenRap }}</td>
              <td>{{ rap.diaChi }}</td>
              <td class="td-city">{{ normalizeCity(rap.thanhPho) || '—' }}</td>
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
            <tr v-if="filteredRapList.length === 0"><td colspan="6" class="empty-text">{{ rapList.length === 0 ? 'Chưa có rạp nào' : 'Không tìm thấy rạp phù hợp' }}</td></tr>
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
            <tr><th>ID</th><th>Tên phòng</th><th>Loại</th><th>Định dạng</th><th>Sức chứa</th><th>Trạng thái</th><th>Thao tác</th></tr>
          </thead>
          <tbody>
            <tr v-for="phong in phongList" :key="phong.id">
              <td>#{{ phong.id }}</td>
              <td class="font-bold">{{ phong.tenPhong }}</td>
              <td><span class="badge badge-blue">{{ phong.loaiPhong }}</span></td>
              <td>
                <span v-if="phong.dinhDang" class="badge badge-green">{{ phong.dinhDang }}</span>
                <span v-else class="badge badge-gray">Chưa đặt</span>
              </td>
              <td>{{ phong.soGheThucTe ?? phong.sucChua }} ghế</td>
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
            <tr v-if="phongList.length === 0"><td colspan="7" class="empty-text">{{ selectedRapId ? 'Chưa có phòng nào' : 'Chọn rạp để xem phòng' }}</td></tr>
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
        <button v-if="selectedPhongId" class="btn-primary" @click="openAddRowModal">+ Thêm dãy ghế</button>
      </div>

      <div class="card">
        <div v-if="loadingGhe" class="loading-text">Đang tải...</div>
        <div v-else-if="gheList.length === 0" class="empty-text">Chọn phòng để xem sơ đồ ghế</div>
        <div v-else class="seat-map">

          <!-- STEP 5: Bulk action toolbar — only visible when seats are selected -->
          <div v-if="selectedSeatIds.size > 0" class="bulk-toolbar">
            <span class="bulk-count">&check; Đã chọn <strong>{{ selectedSeatIds.size }}</strong> ghế</span>
            <div class="bulk-actions">
              <span class="bulk-label">Đổi thành:</span>
              <select v-model="bulkLoaiGhe" class="bulk-select">
                <option value="thường">Thường</option>
                <option value="vip">VIP</option>
                <option value="cặp đôi">Cặp đôi</option>
                <option value="trống">Trống (lối đi)</option>
              </select>
              <button class="btn-bulk-apply" @click="applyBulkEdit">Áp dụng</button>
              <button class="btn-bulk-danger" @click="deleteSelectedSeats">Xóa ghế</button>
              <button class="btn-bulk-clear" @click="selectedSeatIds = new Set()">Bỏ chọn</button>
            </div>
          </div>

          <SeatGrid
            :rows="seatGridRows"
            :total-cols="roomMaxCols"
            :show-screen="true"
            :seat-class-fn="s => selectedSeatIds.has(s.id) ? 'seat--admin-selected' : ''"
            :seat-title-fn="s => `${s.hangGhe?.trim()}${s.soGheHienThi ?? s.soGhe} — ${s.loaiGhe}`"
            :seat-key-fn="s => s.id"
            @seat-mousedown="startDragSeat"
            @seat-mouseenter="dragSeatHover"
          >
            <template #row-left="{ row }">
              <button
                class="row-label-btn"
                :class="isRowFullySelected(row.label) ? 'row-label-btn--active' : ''"
                :title="`Chọn/bỏ cả hàng ${row.label}`"
                @click="toggleRow(row.label)"
              >{{ row.label }}</button>
            </template>
            <template #row-right="{ row }">
              <div class="row-right-actions">
                <button class="row-add-btn" :title="`Thêm ghế vào dãy ${row.label}`" @click.stop="openAddGheModal(row.label)">＋</button>
                <button class="row-delete-btn" :title="`Xóa cả dãy ${row.label}`" @click="deleteRow(row.label)">&times;</button>
              </div>
            </template>
          </SeatGrid>

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
            <span class="legend-item">
              <span class="chip chip--trong"></span>Trống / lối đi
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
          <label>Thành phố</label>
          <select v-model="rapForm.thanhPho">
            <option value="">-- Chọn thành phố --</option>
            <option value="TPHCM">TPHCM</option>
            <option value="Hà Nội">Hà Nội</option>
          </select>
        </div>
        <div class="form-group"><label>Hình ảnh (URL)</label><input v-model="rapForm.hinhAnh" placeholder="https://..." /></div>
        <div class="form-group">
          <label>Google Maps URL</label>
          <input v-model="rapForm.banDoUrl" type="url" placeholder="https://maps.app.goo.gl/..." />
          <small class="form-hint">Dán link Google Maps chia sẻ vào đây</small>
        </div>
        <div class="form-row">
          <div class="form-group form-group--half"><label>Latitude</label><input v-model="rapForm.latitude" type="text" inputmode="decimal" placeholder="10.7769" /></div>
          <div class="form-group form-group--half"><label>Longitude</label><input v-model="rapForm.longitude" type="text" inputmode="decimal" placeholder="106.7009" /></div>
        </div>
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
        <div class="form-group">
          <label>Định dạng *</label>
          <select v-model="phongForm.dinhDangId">
            <option value="">-- Chọn định dạng --</option>
            <option v-for="dd in dinhDangList" :key="dd.id" :value="dd.id">{{ dd.tenDinhDang }}</option>
          </select>
          <small class="form-hint">Bắt buộc — phòng cần có định dạng để xuất hiện trong "Thêm lịch chiếu".</small>
        </div>
        <div class="form-group">
          <label>Sức chứa</label>
          <input v-model.number="phongForm.sucChua" type="number" min="1" />
          <small class="form-hint">Tự động cập nhật theo tổng số ghế đã tạo ở tab "Ghế ngồi".</small>
        </div>
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

    <!-- THEM DAY GHE MODAL -->
    <div v-if="showAddRowModal" class="modal-overlay" @click.self="showAddRowModal = false">
      <div class="modal">
        <h2>Thêm dãy ghế</h2>
        <div class="form-group">
          <label>Phòng</label>
          <input :value="selectedPhongName" disabled />
        </div>
        <div class="form-group">
          <label>Tên dãy</label>
          <input v-model="addRowForm.hangGhe" maxlength="2" placeholder="Ví dụ: F" />
        </div>
        <div class="form-row">
          <div class="form-group form-group--half"><label>Số ghế bắt đầu</label><input v-model.number="addRowForm.soGheTu" type="number" min="1" /></div>
          <div class="form-group form-group--half"><label>Số ghế kết thúc</label><input v-model.number="addRowForm.soGheDen" type="number" min="1" /></div>
        </div>
        <div class="form-group">
          <label>Loại ghế</label>
          <select v-model="addRowForm.loaiGhe">
            <option value="thường">Thường</option>
            <option value="vip">VIP</option>
            <option value="cặp đôi">Cặp đôi</option>
            <option value="trống">Trống (lối đi)</option>
          </select>
        </div>
        <div v-if="modalError" class="form-error">{{ modalError }}</div>
        <div class="modal-actions">
          <button class="btn-ghost" @click="showAddRowModal = false">Hủy</button>
          <button class="btn-primary" @click="saveGheRow" :disabled="savingGheRow">{{ savingGheRow ? 'Đang thêm...' : 'Thêm dãy' }}</button>
        </div>
      </div>
    </div>

    <!-- THEM GHE VAO DAY DA TON TAI MODAL -->
    <div v-if="showAddGheModal" class="modal-overlay" @click.self="showAddGheModal = false">
      <div class="modal">
        <h2>Thêm ghế vào hàng {{ addGheForm.hangGhe }}</h2>
        <div class="form-group">
          <label>Phòng</label>
          <input :value="selectedPhongName" disabled />
        </div>
        <div class="form-group">
          <label>Hàng</label>
          <input :value="addGheForm.hangGhe" disabled />
        </div>
        <div class="form-row">
          <div class="form-group form-group--half"><label>Thêm từ số</label><input v-model.number="addGheForm.soGheTu" type="number" min="1" /></div>
          <div class="form-group form-group--half"><label>đến số</label><input v-model.number="addGheForm.soGheDen" type="number" min="1" /></div>
        </div>
        <div class="form-group">
          <label>Loại ghế</label>
          <select v-model="addGheForm.loaiGhe">
            <option value="thường">Thường</option>
            <option value="vip">VIP</option>
            <option value="cặp đôi">Cặp đôi</option>
            <option value="trống">Trống (lối đi)</option>
          </select>
        </div>
        <div v-if="modalError" class="form-error">{{ modalError }}</div>
        <div class="modal-actions">
          <button class="btn-ghost" @click="showAddGheModal = false">Hủy</button>
          <button class="btn-primary" @click="saveAddGhe" :disabled="savingGheRow">{{ savingGheRow ? 'Đang thêm...' : 'Thêm ghế' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import api from '@/services/api'
import { useAdminShellStore } from '@/stores/adminShellStore'
import { flashRow } from '@/utils/flashRow'
import SeatGrid from '@/components/SeatGrid.vue'

const shell = useAdminShellStore()

const activeTab = ref('rap')

// ── Rạp chiếu ──────────────────────────────
const rapList = ref([])
const loadingRap = ref(false)
const showRapModal = ref(false)
const editingRap = ref(null)
const savingRap = ref(false)
const modalError = ref('')
const rapForm = ref({ tenRap: '', diaChi: '', trangThai: true })

// Normalize legacy city strings ("TP.HCM", "TP hcm", "Tp. Hồ Chí Minh") → "TPHCM"
function normalizeCity(v) {
  if (!v) return ''
  const t = v.trim().toLowerCase()
  if (t.includes('hcm') || t.includes('hồ chí minh') || t.includes('ho chi minh')) return 'TPHCM'
  if (t.includes('hà nội') || t.includes('ha noi')) return 'Hà Nội'
  return v
}

async function loadRap() {
  loadingRap.value = true
  try {
    const res = await api.get('/admin/rap-chieu')
    rapList.value = res.data || []
  } catch (e) { console.error(e) } finally { loadingRap.value = false }
}

// ── Command-palette targeting (mirrors MoviesPage syncFromShell) ──
const search = ref('')
const flashQueued = ref(false)

const filteredRapList = computed(() => {
  const q = search.value.toLowerCase()
  if (!q) return rapList.value
  return rapList.value.filter(r =>
    r.tenRap?.toLowerCase().includes(q) ||
    r.diaChi?.toLowerCase().includes(q) ||
    normalizeCity(r.thanhPho)?.toLowerCase().includes(q)
  )
})

function syncFromShell() {
  if (shell.searchTargetPage !== 'cinemas') return
  if (shell.searchQuery) {
    activeTab.value = 'rap'
    search.value = shell.searchQuery
    flashQueued.value = true
  }
}

watch(() => shell.searchTick, syncFromShell)

// When the palette-narrowed list renders, flash + scroll the matched row.
watch(filteredRapList, (list) => {
  if (flashQueued.value && list.length > 0) {
    flashQueued.value = false
    nextTick(() => flashRow(document.querySelector(`[data-row-id="${list[0].id}"]`)))
  }
})

function openRapModal(rap = null) {
  editingRap.value = rap
  modalError.value = ''
  if (rap) rapForm.value = {
    tenRap:    rap.tenRap    || '',
    diaChi:    rap.diaChi    || '',
    trangThai: rap.trangThai ?? true,
    thanhPho:  normalizeCity(rap.thanhPho),
    latitude:  rap.latitude  != null ? Number(rap.latitude).toFixed(7).replace(/\.?0+$/, '') : '',
    longitude: rap.longitude != null ? Number(rap.longitude).toFixed(7).replace(/\.?0+$/, '') : '',
    hinhAnh:   rap.hinhAnh   || '',
    banDoUrl:  rap.banDoUrl  || '',
  }
  else rapForm.value = { tenRap: '', diaChi: '', trangThai: true, thanhPho: '', latitude: '', longitude: '', hinhAnh: '', banDoUrl: '' }
  showRapModal.value = true
}

async function saveRap() {
  if (!rapForm.value.tenRap.trim()) { modalError.value = 'Tên rạp không được để trống'; return }
  savingRap.value = true; modalError.value = ''
  try {
    // Parse lat/lng strings → numbers (null if blank or not a valid number)
    const latRaw = rapForm.value.latitude !== '' ? parseFloat(rapForm.value.latitude) : null
    const lngRaw = rapForm.value.longitude !== '' ? parseFloat(rapForm.value.longitude) : null
    const payload = {
      ...rapForm.value,
      latitude:  latRaw != null && !isNaN(latRaw)  ? latRaw  : null,
      longitude: lngRaw != null && !isNaN(lngRaw) ? lngRaw : null,
    }
    if (editingRap.value) await api.put(`/admin/rap-chieu/${editingRap.value.id}`, payload)
    else await api.post('/admin/rap-chieu', payload)
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
const phongForm = ref({ tenPhong: '', loaiPhong: '2D', dinhDangId: '', sucChua: 100, rapChieuId: '' })

// ── Định dạng (DinhDang) list ───────────────
const dinhDangList = ref([])
async function loadDinhDang() {
  try {
    const res = await api.get('/dinh-dang')
    dinhDangList.value = res.data || []
  } catch (e) { console.error(e) }
}

async function loadPhong() {
  if (!selectedRapId.value) { phongList.value = []; return }
  loadingPhong.value = true
  try {
    const res = await api.get(`/admin/phong-chieu?rapChieuId=${selectedRapId.value}`)
    phongList.value = res.data || []
  } catch (e) { console.error(e) } finally { loadingPhong.value = false }
}

function openPhongModal(phong = null) {
  editingPhong.value = phong; modalError.value = ''
  if (phong) phongForm.value = { tenPhong: phong.tenPhong || '', loaiPhong: phong.loaiPhong || '2D', dinhDangId: phong.dinhDangId != null ? Number(phong.dinhDangId) : '', sucChua: phong.soGheThucTe ?? phong.sucChua ?? 100, rapChieuId: selectedRapId.value }
  else phongForm.value = { tenPhong: '', loaiPhong: '2D', dinhDangId: '', sucChua: 100, rapChieuId: selectedRapId.value }
  showPhongModal.value = true
}

async function savePhong() {
  if (!phongForm.value.tenPhong.trim()) { modalError.value = 'Tên phòng không được để trống'; return }
  if (!phongForm.value.dinhDangId) { modalError.value = 'Vui lòng chọn định dạng cho phòng chiếu'; return }
  savingPhong.value = true; modalError.value = ''
  try {
    const payload = { ...phongForm.value, rapChieu: { id: phongForm.value.rapChieuId }, dinhDang: { id: phongForm.value.dinhDangId } }
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

// Drag-select state: mousedown on a seat starts the drag; the mode ('select'
// or 'deselect') is decided by the first seat's state and applied to every
// seat the cursor passes over until mouseup anywhere.
const isDragSelecting = ref(false)
const dragMode = ref('select')

function applySeatMode(seatId, mode) {
  if (mode === 'select') selectedSeatIds.value.add(seatId)
  else selectedSeatIds.value.delete(seatId)
  selectedSeatIds.value = new Set(selectedSeatIds.value)
}

function startDragSeat(seat) {
  dragMode.value = selectedSeatIds.value.has(seat.id) ? 'deselect' : 'select'
  isDragSelecting.value = true
  applySeatMode(seat.id, dragMode.value)
}

function dragSeatHover(seat) {
  if (!isDragSelecting.value) return
  applySeatMode(seat.id, dragMode.value)
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
  const label = g => `${(g.hangGhe || '?').trim()}${g.soGhe}`
  try {
    const results = await Promise.allSettled(
      ids.map(id =>
        api.put(`/admin/ghe-ngoi/${id}`, { loaiGhe: bulkLoaiGhe.value }).then(() => id)
      )
    )
    const okIds = results.filter(r => r.status === 'fulfilled').map(r => r.value)
    const okSet = new Set(okIds)
    const failedIds = ids.filter(id => !okSet.has(id))
    const failedLabels = failedIds.map(id => {
      const g = gheList.value.find(x => x.id === id)
      return g ? label(g) : `#${id}`
    })

    // Update local state only for seats whose PUT succeeded
    gheList.value = gheList.value.map(g =>
      okSet.has(g.id) ? { ...g, loaiGhe: bulkLoaiGhe.value } : g
    )
    // Keep failed seats selected so the user sees which ones didn't apply
    selectedSeatIds.value = new Set(failedIds)

    if (failedIds.length > 0) {
      alert(`Đã đổi ${okIds.length} ghế thành "${bulkLoaiGhe.value}". ` +
        `Lỗi ${failedIds.length} ghế chưa đổi được: ${failedLabels.join(', ')}. ` +
        `Các ghế lỗi vẫn đang được chọn để bạn thử lại.`)
    }
  } catch (err) {
    console.error('Bulk update failed', err)
    alert('Lỗi khi cập nhật ghế. Vui lòng thử lại.')
  } finally {
    // Always refresh the room list so "Sức chứa" reflects the change immediately
    await refreshPhongCapacity()
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

const seatGridRows = computed(() =>
  groupedGhe.value.map(r => ({ label: r.hang.trim(), seats: r.seats }))
)

// Số cột vật lý rộng nhất của phòng — giữ khe lối đi đúng vị trí (grid-column = soGhe)
const roomMaxCols = computed(() =>
  gheList.value.reduce((m, g) => Math.max(m, g.soGhe || 0), 0)
)

async function loadGhe() {
  if (!selectedPhongId.value) { gheList.value = []; return }
  selectedSeatIds.value = new Set()   // clear selection when room changes
  loadingGhe.value = true
  try {
    const res = await api.get(`/admin/ghe-ngoi?phongChieuId=${selectedPhongId.value}`)
    gheList.value = res.data || []
  } catch (e) { gheList.value = [] } finally { loadingGhe.value = false }
}

// ── Thêm dãy ghế ─────────────────────────────
const showAddRowModal = ref(false)
const savingGheRow = ref(false)
const addRowForm = ref({ hangGhe: '', soGheTu: 1, soGheDen: 10, loaiGhe: 'thường' })

const selectedPhongName = computed(() => {
  const p = phongList.value.find(x => String(x.id) === String(selectedPhongId.value))
  return p ? p.tenPhong : ''
})

function openAddRowModal() {
  modalError.value = ''
  // Suggest next row letter after the last existing one
  const rows = gheList.value.map(g => g.hangGhe?.trim()).filter(Boolean)
  const unique = [...new Set(rows)]
  let nextLetter = 'A'
  if (unique.length > 0) {
    const last = unique.map(r => r.charCodeAt(r.length - 1)).sort((a, b) => a - b).pop()
    nextLetter = String.fromCharCode(last + 1)
  }
  addRowForm.value = { hangGhe: nextLetter, soGheTu: 1, soGheDen: 10, loaiGhe: 'thường' }
  showAddRowModal.value = true
}

async function saveGheRow() {
  if (!addRowForm.value.hangGhe.trim()) { modalError.value = 'Vui lòng nhập tên dãy'; return }
  if (!addRowForm.value.soGheTu || !addRowForm.value.soGheDen || addRowForm.value.soGheDen < addRowForm.value.soGheTu) {
    modalError.value = 'Khoảng số ghế không hợp lệ'; return
  }
  savingGheRow.value = true; modalError.value = ''
  try {
    await api.post('/admin/ghe-ngoi/row', {
      phongChieuId: selectedPhongId.value,
      hangGhe: addRowForm.value.hangGhe,
      soGheTu: addRowForm.value.soGheTu,
      soGheDen: addRowForm.value.soGheDen,
      loaiGhe: addRowForm.value.loaiGhe,
    })
    showAddRowModal.value = false
    await loadGhe()
    await refreshPhongCapacity()
  } catch (e) {
    modalError.value = e.response?.data?.message || 'Lỗi thêm dãy ghế'
  } finally { savingGheRow.value = false }
}

// ── Thêm ghế vào dãy đã tồn tại ────────────────
const showAddGheModal = ref(false)
const addGheForm = ref({ hangGhe: '', soGheTu: 1, soGheDen: 1, loaiGhe: 'thường' })

function openAddGheModal(hang) {
  modalError.value = ''
  const rowSeats = gheList.value
    .filter(g => (g.hangGhe || '').trim() === String(hang).trim())
    .sort((a, b) => a.soGhe - b.soGhe)
  // loaiGhe mặc định = loại ghế cuối cùng của hàng (không hardcode 'thường')
  const last = rowSeats[rowSeats.length - 1]
  const maxSoGhe = rowSeats.reduce((m, g) => Math.max(m, Number(g.soGhe) || 0), 0)
  addGheForm.value = {
    hangGhe: String(hang).trim(),
    soGheTu: maxSoGhe + 1,
    soGheDen: maxSoGhe + 1,
    loaiGhe: last?.loaiGhe || 'thường',
  }
  showAddGheModal.value = true
}

async function saveAddGhe() {
  const hang = addGheForm.value.hangGhe.trim()
  const tu = Number(addGheForm.value.soGheTu)
  const den = Number(addGheForm.value.soGheDen)
  if (!tu || !den || den < tu) { modalError.value = 'Khoảng số ghế không hợp lệ'; return }
  // Validate FE: soGheTu phải > số ghế lớn nhất hiện có của hàng (tránh nhập trùng bị skip âm thầm)
  const maxSoGhe = gheList.value
    .filter(g => (g.hangGhe || '').trim() === hang)
    .reduce((m, g) => Math.max(m, Number(g.soGhe) || 0), 0)
  if (tu <= maxSoGhe) {
    modalError.value = `Hàng ${hang} đã có ghế đến số ${maxSoGhe}. Vui lòng thêm từ số ${maxSoGhe + 1} trở lên`
    return
  }
  savingGheRow.value = true; modalError.value = ''
  try {
    await api.post('/admin/ghe-ngoi/row', {
      phongChieuId: selectedPhongId.value,
      hangGhe: hang,
      soGheTu: tu,
      soGheDen: den,
      loaiGhe: addGheForm.value.loaiGhe,
    })
    showAddGheModal.value = false
    await loadGhe()
    await refreshPhongCapacity()
  } catch (e) {
    modalError.value = e.response?.data?.message || 'Lỗi thêm ghế vào dãy'
  } finally { savingGheRow.value = false }
}

async function deleteRow(hangGhe) {
  if (!confirm(`Xóa toàn bộ dãy ${hangGhe.trim()}?`)) return
  try {
    await api.delete('/admin/ghe-ngoi/row', {
      params: { phongChieuId: selectedPhongId.value, hangGhe }
    })
    selectedSeatIds.value = new Set()
    await loadGhe()
    await refreshPhongCapacity()
  } catch (e) {
    alert(e.response?.data?.message || 'Lỗi xóa dãy ghế')
  }
}

async function deleteSelectedSeats() {
  const ids = [...selectedSeatIds.value]
  if (ids.length === 0) return
  if (!confirm(`Xóa ${ids.length} ghế đã chọn?`)) return
  try {
    await Promise.all(ids.map(id => api.delete(`/admin/ghe-ngoi/${id}`)))
    selectedSeatIds.value = new Set()
    await loadGhe()
    await refreshPhongCapacity()
  } catch (e) {
    alert(e.response?.data?.message || 'Lỗi xóa ghế')
  }
}

// Reload the phong list (from the room dropdown in the ghe tab AND the phong tab)
// so "Sức chứa" stays in sync with the actual seat count.
async function refreshPhongCapacity() {
  if (!selectedRapId.value) return
  try {
    const res = await api.get(`/admin/phong-chieu?rapChieuId=${selectedRapId.value}`)
    phongList.value = res.data || []
  } catch (e) { console.error(e) }
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

onMounted(() => {
  syncFromShell()
  loadRap()
  loadDinhDang()
  // End any in-progress drag-select when the button is released anywhere
  window.addEventListener('mouseup', stopDragSelect)
})

onUnmounted(() => {
  window.removeEventListener('mouseup', stopDragSelect)
})

function stopDragSelect() {
  isDragSelecting.value = false
}
</script>

<style scoped>
/* ── Page root ── */
.cinemas-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--admin-bg);
  color: var(--admin-text);
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
  color: var(--admin-accent);
  padding-left: 12px;
  border-left: 3px solid var(--admin-accent);
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
  border: 1px solid var(--admin-border);
  border-radius: 8px;
  background: var(--admin-surface);
  color: var(--admin-text);
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
.filter-select:focus { outline: none; border-color: var(--admin-accent); box-shadow: 0 0 0 2px rgba(255,255,255,0.15); }

/* ── Card ── */
.card {
  background: var(--admin-surface);
  border: 1px solid var(--admin-border);
  border-radius: 12px;
  overflow: hidden;
}

/* ── Table ── */
table { width: 100%; border-collapse: collapse; }
thead tr { background: var(--admin-bg); }
th {
  padding: 12px 16px;
  text-align: left;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--admin-text-muted);
  border-bottom: 2px solid var(--admin-border);
  white-space: nowrap;
}
tbody tr { border-bottom: 1px solid var(--admin-divider); transition: background 150ms ease; }
tbody tr:last-child { border-bottom: none; }
tbody tr:hover { background: var(--admin-surface-hover); }
td {
  padding: 14px 16px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  color: var(--admin-text);
  vertical-align: middle;
}
.font-bold { font-weight: 600; }

.td-city { font-size: 13px; color: #29bcea; font-weight: 600; }

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
.badge-gray  { background: rgba(156,163,175,0.15); color: var(--admin-text-muted); }
.badge-blue  { background: rgba(255,255,255,0.10); color: var(--admin-accent); }

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
.btn-edit:hover   { color: var(--admin-accent); background: rgba(255,255,255,0.10); }
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
  background: var(--admin-accent);
  color: var(--admin-bg);
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
  border: 1px solid var(--admin-border);
  background: transparent;
  color: var(--admin-text);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 150ms ease, color 150ms ease;
}
.btn-ghost:hover { border-color: var(--admin-accent); color: var(--admin-accent); }

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
  background: var(--admin-surface-hover);
  border: 1px solid var(--admin-border);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 24px 48px rgba(0,0,0,0.5);
  color: var(--admin-text);
}
.modal h2 {
  font-family: var(--font-display, 'Playfair Display', serif);
  font-size: 18px;
  font-weight: 700;
  color: var(--admin-accent);
  margin: 0 0 20px;
  padding-left: 12px;
  border-left: 3px solid var(--admin-accent);
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}
.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 0;
}
.form-group--half {
  flex: 1;
  min-width: 0;
}
.form-group label {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 11px;
  font-weight: 600;
  color: var(--admin-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.form-group input, .form-group select {
  min-height: 40px;
  padding: 9px 14px;
  border: 1px solid var(--admin-border);
  border-radius: 8px;
  background: var(--admin-surface);
  color: var(--admin-text);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  -webkit-appearance: none;
  appearance: none;
  transition: border-color 150ms ease;
}
.form-group input:focus, .form-group select:focus { outline: none; border-color: var(--admin-accent); box-shadow: 0 0 0 2px rgba(255,255,255,0.15); }
.form-error { color: #EF4444; font-size: 13px; margin-top: 8px; }
.form-hint {
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 11px;
  color: var(--admin-text-muted);
  margin-top: 4px;
}
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
  color: var(--admin-text-muted);
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
  color: var(--admin-accent);
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
  color: var(--admin-text-muted);
}
.bulk-select {
  min-height: 34px;
  padding: 5px 30px 5px 10px;
  border: 1px solid #4B5563;
  border-radius: 8px;
  background: var(--admin-surface-hover);
  color: var(--admin-text);
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
  color: var(--admin-accent);
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
  color: var(--admin-text);
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 13px;
  cursor: pointer;
  transition: background 150ms ease;
}
.btn-bulk-clear:hover { background: #6B7280; }
.btn-bulk-danger {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 5px 12px;
  border: none;
  border-radius: 8px;
  background: #DC2626;
  color: #ffffff;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 150ms ease;
}
.btn-bulk-danger:hover { background: #B91C1C; }

/* ── Seat map ── */
.seat-map { padding: 24px; user-select: none; -webkit-user-select: none; }

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
  color: var(--admin-text-muted);
  cursor: pointer;
  transition: color 150ms ease, background 150ms ease;
  flex-shrink: 0;
}
.row-label-btn:hover { color: var(--admin-accent); background: var(--admin-accent-muted); }
.row-label-btn--active { color: #60A5FA; }

.row-delete-btn {
  width: 22px;
  min-width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--admin-text-muted);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  flex-shrink: 0;
  margin-left: 4px;
  transition: color 150ms ease, background 150ms ease;
}
.row-delete-btn:hover { color: #EF4444; background: rgba(239,68,68,0.15); }

.row-right-actions {
  display: flex;
  align-items: center;
  margin-left: 4px;
  flex-shrink: 0;
}
.row-add-btn {
  width: 22px;
  min-width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--admin-text-muted);
  font-size: 15px;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
  flex-shrink: 0;
  margin-right: 2px;
  transition: color 150ms ease, background 150ms ease;
}
.row-add-btn:hover { color: #34D399; background: rgba(52,211,153,0.15); }

/* ── Legend ── */
.seat-legend { display: flex; gap: 20px; margin-top: 20px; padding-top: 16px; border-top: 1px solid var(--admin-border); flex-wrap: wrap; }
.legend-item { display: flex; align-items: center; gap: 6px; font-family: var(--font-ui, 'Inter', sans-serif); font-size: 12px; font-weight: 600; color: var(--admin-text-muted); }
.chip { width: 16px; height: 16px; border-radius: 3px; border: 1px solid transparent; }
.chip--thuong { background: var(--admin-surface-hover); border-color: var(--admin-border); }
.chip--vip    { background: #C9A84C; border-color: #C9A84C; }
.chip--doi    { background: #ec4899; border-color: #db2777; }
.chip--trong  { background: transparent; border: 1px dashed var(--admin-border); }

/* ── Palette target row flash ── */
.row-flash {
  animation: row-flash-pop 1.6s ease;
}
@keyframes row-flash-pop {
  0%, 100% { background-color: transparent; }
  20%, 60% { background-color: rgba(41, 188, 234, 0.18); }
}
</style>

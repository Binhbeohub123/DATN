<template>
  <div class="cinemas-page">
    <!-- Tabs -->
    <div class="tabs">
      <button :class="['tab', { active: activeTab === 'rap' }]" @click="activeTab = 'rap'">🏢 Rạp chiếu</button>
      <button :class="['tab', { active: activeTab === 'phong' }]" @click="activeTab = 'phong'">🎭 Phòng chiếu</button>
      <button :class="['tab', { active: activeTab === 'ghe' }]" @click="activeTab = 'ghe'">💺 Ghế ngồi</button>
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
                  <button class="btn-edit" @click="openRapModal(rap)">✏️</button>
                  <button class="btn-delete" @click="deleteRap(rap)">🗑️</button>
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
                  <button class="btn-edit" @click="openPhongModal(phong)">✏️</button>
                  <button class="btn-delete" @click="deactivatePhong(phong)" :title="phong.trangThai ? 'Vô hiệu hóa phòng' : 'Phòng đã ngừng'">🗑️</button>
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
          <div class="screen-label">— Màn hình —</div>
          <div v-for="row in groupedGhe" :key="row.hang" class="seat-row">
            <div class="row-label">{{ row.hang }}</div>
            <div class="seats">
              <div v-for="seat in row.seats" :key="seat.id" :class="['seat-chip', seatTypeClass(seat.loaiGhe)]" :title="`${seat.hangGhe}${seat.soGhe} - ${seat.loaiGhe}`">
                {{ seat.soGhe }}
              </div>
            </div>
          </div>
          <div class="seat-legend">
            <span class="legend-item"><span class="chip thuong"></span>Thường</span>
            <span class="legend-item"><span class="chip vip"></span>VIP</span>
            <span class="legend-item"><span class="chip doi"></span>Đôi</span>
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
import { ref, computed, onMounted } from 'vue'
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

const groupedGhe = computed(() => {
  const groups = {}
  gheList.value.forEach(g => {
    if (!groups[g.hangGhe]) groups[g.hangGhe] = []
    groups[g.hangGhe].push(g)
  })
  return Object.entries(groups).sort((a,b) => a[0].localeCompare(b[0])).map(([hang, seats]) => ({ hang, seats: seats.sort((a,b) => a.soGhe - b.soGhe) }))
})

function seatTypeClass(loai) {
  if (!loai) return 'thuong'
  const l = loai.toLowerCase()
  if (l.includes('vip')) return 'vip'
  if (l.includes('đôi') || l.includes('doi') || l.includes('couple')) return 'doi'
  return 'thuong'
}

async function loadGhe() {
  if (!selectedPhongId.value) { gheList.value = []; return }
  loadingGhe.value = true
  try {
    const res = await api.get(`/lich-chieu/0/ghe-trong`)
    gheList.value = res.data || []
  } catch (e) { gheList.value = [] } finally { loadingGhe.value = false }
}

onMounted(loadRap)
</script>

<style scoped>
.cinemas-page { display: flex; flex-direction: column; gap: 20px; }
.tabs { display: flex; gap: 4px; }
.tab { padding: 10px 20px; border-radius: 10px; border: 1px solid #e5e7eb; background: white; font-size: 14px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.tab:hover { border-color: #29bcea; color: #29bcea; }
.tab.active { background: #29bcea; color: white; border-color: transparent; }

.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.toolbar h3 { font-size: 16px; font-weight: 800; margin: 0; }
.toolbar-left { display: flex; align-items: center; gap: 12px; }
.filter-select { padding: 8px 12px; border: 1px solid #e5e7eb; border-radius: 8px; font-size: 14px; background: white; }

.card { border-radius: 16px; background: rgba(255,255,255,0.9); border: 1px solid rgba(255,255,255,0.7); box-shadow: 0 4px 16px rgba(15,23,42,0.07); overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 800; color: #6b7280; text-transform: uppercase; background: #f9fafb; border-bottom: 1px solid #e5e7eb; }
td { padding: 12px 16px; font-size: 13px; border-bottom: 1px solid #f3f4f6; vertical-align: middle; }
tr:last-child td { border-bottom: none; }
tr:hover td { background: #f7fcfe; }
.font-bold { font-weight: 700; color: #111827; }
.empty-text, .loading-text { text-align: center; padding: 40px; color: #9ca3af; font-size: 14px; }

.badge { padding: 4px 10px; border-radius: 999px; font-size: 11px; font-weight: 800; }
.badge-green { background: #dcfce7; color: #166534; }
.badge-blue { background: #dbeafe; color: #1e40af; }
.badge-gray { background: #f3f4f6; color: #6b7280; }

.action-btns { display: flex; gap: 6px; }
.btn-edit, .btn-delete { width: 32px; height: 32px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; }
.btn-edit { background: #dbeafe; }
.btn-edit:hover { background: #bfdbfe; }
.btn-delete { background: #fee2e2; }
.btn-delete:hover { background: #fecaca; }

.btn-primary { padding: 10px 20px; background: #29bcea; color: white; border: none; border-radius: 10px; font-weight: 800; cursor: pointer; font-size: 14px; transition: all 0.2s; white-space: nowrap; }
.btn-primary:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 16px rgba(255,107,0,0.3); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-ghost { padding: 10px 20px; background: transparent; border: 1px solid #e5e7eb; border-radius: 10px; font-weight: 700; cursor: pointer; font-size: 14px; }
.btn-ghost:hover { border-color: #29bcea; color: #29bcea; }

/* Seat map */
.seat-map { padding: 24px; }
.screen-label { text-align: center; padding: 8px; background: #f3f4f6; border-radius: 6px; font-size: 12px; font-weight: 700; color: #6b7280; margin-bottom: 24px; }
.seat-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.row-label { width: 24px; text-align: center; font-size: 12px; font-weight: 800; color: #6b7280; }
.seats { display: flex; flex-wrap: wrap; gap: 4px; }
.seat-chip { width: 28px; height: 28px; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: 700; cursor: default; }
.seat-chip.thuong { background: #e2e8f0; color: #475569; }
.seat-chip.vip { background: #f3e8ff; color: #7c3aed; }
.seat-chip.doi { background: #fce7f3; color: #be185d; }
.seat-legend { display: flex; gap: 20px; margin-top: 20px; padding-top: 16px; border-top: 1px solid #e5e7eb; }
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 700; color: #6b7280; }
.chip { width: 16px; height: 16px; border-radius: 3px; }
.chip.thuong { background: #e2e8f0; }
.chip.vip { background: #f3e8ff; }
.chip.doi { background: #fce7f3; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 20px; }
.modal { background: white; border-radius: 20px; padding: 32px; width: 100%; max-width: 480px; }
.modal h2 { font-size: 18px; font-weight: 900; margin: 0 0 20px; }
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.form-group label { font-size: 12px; font-weight: 800; color: #6b7280; text-transform: uppercase; }
.form-group input, .form-group select { padding: 10px 12px; border: 1px solid #e5e7eb; border-radius: 8px; font-size: 14px; }
.form-group input:focus, .form-group select:focus { outline: none; border-color: #29bcea; }
.form-error { color: #ef4444; font-size: 13px; margin-bottom: 12px; }
.modal-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 20px; }
</style>

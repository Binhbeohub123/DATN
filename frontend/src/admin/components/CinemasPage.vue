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



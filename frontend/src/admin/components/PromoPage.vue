<template>
  <div class="promo-page">
    <transition name="toast"><div v-if="toast.show" :class="['toast',`toast--${toast.type}`]">{{ toast.msg }}</div></transition>

    <div class="toolbar">
      <h3 class="section-title">Mã Khuyến Mãi</h3>
      <button class="btn-primary" @click="openCreate">+ Thêm mã</button>
    </div>

    <div class="card table-card">
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>
      <div v-else-if="promos.length===0" class="state-center empty-text">Chưa có mã khuyến mãi</div>
      <div v-else class="table-scroll">
        <table class="data-table">
          <thead><tr>
            <th>Mã</th><th>Tên</th><th>Loại</th><th>Giá trị</th>
            <th>Đơn tối thiểu</th><th>Đã dùng / Max</th><th>Hết hạn</th><th>Trạng thái</th><th>Thao tác</th>
          </tr></thead>
          <tbody>
            <tr v-for="km in promos" :key="km.id">
              <td class="td-code">{{ km.maKhuyenMai }}</td>
              <td class="td-name">{{ km.tenKhuyenMai }}</td>
              <td><span :class="['tbadge', km.loaiGiamGia==='percent'?'tbadge--blue':'tbadge--purple']">{{ km.loaiGiamGia==='percent'?'%':'Cố định' }}</span></td>
              <td class="td-val">{{ km.loaiGiamGia==='percent' ? `${km.giaTriGiam}%` : fmtPrice(km.giaTriGiam) }}</td>
              <td>{{ fmtPrice(km.donHangToiThieu) }}</td>
              <td>{{ km.daSuDung }} / {{ km.gioiHanSuDung||'∞' }}</td>
              <td class="td-date">{{ km.ngayKetThuc || '—' }}</td>
              <td>
                <button :class="['toggle-btn', km.dangHoatDong?'toggle-btn--on':'toggle-btn--off']" @click="toggleActive(km)">
                  {{ km.dangHoatDong ? '✓ Bật' : '✗ Tắt' }}
                </button>
              </td>
              <td>
                <div class="act-btns">
                  <button class="btn-icon btn-edit" @click="openEdit(km)" title="Sửa">✏️</button>
                  <button class="btn-icon btn-del"  @click="del(km)"     title="Xóa">🗑️</button>
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
          <h2>{{ editing ? 'Sửa mã' : 'Thêm mã khuyến mãi' }}</h2>
          <button class="modal-close" @click="showModal=false">✕</button>
        </div>
        <div class="form-grid">
          <div class="field"><label>Mã khuyến mãi *</label><input v-model="form.maKhuyenMai" placeholder="VD: SUMMER20" :disabled="!!editing" /></div>
          <div class="field"><label>Tên hiển thị *</label><input v-model="form.tenKhuyenMai" placeholder="Tên chương trình"/></div>
          <div class="field"><label>Loại giảm giá *</label>
            <select v-model="form.loaiGiamGia">
              <option value="percent">Phần trăm (%)</option>
              <option value="fixed">Cố định (VND)</option>
            </select>
          </div>
          <div class="field"><label>Giá trị giảm *</label><input v-model.number="form.giaTriGiam" type="number" min="0"/></div>
          <div class="field" v-if="form.loaiGiamGia==='percent'"><label>Giảm tối đa (VND)</label><input v-model.number="form.giaTriGiamToiDa" type="number" min="0"/></div>
          <div class="field"><label>Đơn hàng tối thiểu</label><input v-model.number="form.donHangToiThieu" type="number" min="0"/></div>
          <div class="field"><label>Số lần dùng tối đa</label><input v-model.number="form.gioiHanSuDung" type="number" min="1"/></div>
          <div class="field"><label>Ngày bắt đầu</label><input v-model="form.ngayBatDau" type="date"/></div>
          <div class="field"><label>Ngày kết thúc</label><input v-model="form.ngayKetThuc" type="date"/></div>
          <div class="field"><label>Mô tả</label><input v-model="form.moTa" placeholder="Mô tả ngắn (không bắt buộc)"/></div>
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
import { ref, onMounted, reactive } from 'vue'
import api from '@/services/api'

const toast = reactive({ show:false, msg:'', type:'success' })
let toastTimer = null
function showToast(msg, type='error') {
  clearTimeout(toastTimer); toast.msg=msg; toast.type=type; toast.show=true
  toastTimer = setTimeout(() => toast.show=false, 3500)
}

const promos    = ref([])
const loading   = ref(false)
const showModal = ref(false)
const editing   = ref(null)
const saving    = ref(false)
const formErr   = ref('')

const blankForm = () => ({
  maKhuyenMai:'', tenKhuyenMai:'', moTa:'', loaiGiamGia:'percent',
  giaTriGiam:10, giaTriGiamToiDa:null, donHangToiThieu:100000,
  gioiHanSuDung:100, ngayBatDau:'', ngayKetThuc:'', dangHoatDong:true
})
const form = ref(blankForm())

function fmtPrice(v) {
  if (!v && v!==0) return '—'
  return new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(v)
}

function openCreate() { editing.value=null; form.value=blankForm(); formErr.value=''; showModal.value=true }
function openEdit(km) {
  editing.value=km
  form.value = {
    maKhuyenMai: km.maKhuyenMai, tenKhuyenMai: km.tenKhuyenMai||'', moTa: km.moTa||'',
    loaiGiamGia: km.loaiGiamGia||'percent', giaTriGiam: km.giaTriGiam||0,
    giaTriGiamToiDa: km.giaTriGiamToiDa||null, donHangToiThieu: km.donHangToiThieu||0,
    gioiHanSuDung: km.gioiHanSuDung||null, ngayBatDau: km.ngayBatDau||'',
    ngayKetThuc: km.ngayKetThuc||'', dangHoatDong: km.dangHoatDong!==false
  }
  formErr.value=''; showModal.value=true
}

async function save() {
  if (!form.value.maKhuyenMai.trim() || !form.value.tenKhuyenMai.trim()) { formErr.value='Vui lòng điền mã và tên'; return }
  saving.value=true; formErr.value=''
  try {
    const payload = { ...form.value }
    if (!payload.giaTriGiamToiDa) delete payload.giaTriGiamToiDa
    if (editing.value) { await api.put(`/khuyen-mai/${editing.value.id}`, payload) }
    else               { await api.post('/khuyen-mai', payload) }
    showToast(editing.value?'Đã cập nhật':'Đã thêm mã khuyến mãi', 'success')
    showModal.value=false
    await load()
  } catch(e) { formErr.value = e.response?.data?.message || 'Lỗi lưu khuyến mãi' }
  finally { saving.value=false }
}

async function toggleActive(km) {
  try {
    await api.put(`/khuyen-mai/${km.id}`, { dangHoatDong: !km.dangHoatDong })
    km.dangHoatDong = !km.dangHoatDong
    showToast(km.dangHoatDong?'Đã bật mã':'Đã tắt mã', 'success')
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi thay đổi trạng thái') }
}

async function del(km) {
  if (!confirm(`Xóa mã "${km.maKhuyenMai}"?`)) return
  try {
    await api.delete(`/khuyen-mai/${km.id}`)
    showToast('Đã xóa mã', 'success')
    await load()
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi xóa mã') }
}

async function load() {
  loading.value=true
  try { const r = await api.get('/khuyen-mai'); promos.value = r.data||[] }
  catch(e) { showToast('Không tải được mã khuyến mãi') }
  finally { loading.value=false }
}
onMounted(load)
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from,
.toast-leave-to { opacity: 0; }
</style>

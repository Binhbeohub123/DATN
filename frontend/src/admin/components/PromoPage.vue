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
.promo-page { display:flex; flex-direction:column; gap:20px; }
.toast{position:fixed;top:20px;right:20px;z-index:999;padding:12px 20px;border-radius:10px;font-size:13px;font-weight:700}
.toast--error{background:#7f1d1d;color:#fecaca}.toast--success{background:#14532d;color:#bbf7d0}
.toast-enter-active,.toast-leave-active{transition:opacity .3s}.toast-enter-from,.toast-leave-to{opacity:0}
.toolbar { display:flex; align-items:center; justify-content:space-between; }
.section-title { font-size:16px; font-weight:800; color:#111827; margin:0; }
.btn-primary { padding:9px 18px; background:linear-gradient(135deg,#ff6b00,#f97316); color:white; border:none; border-radius:9px; font-weight:800; cursor:pointer; font-size:14px; transition:all .2s; }
.btn-primary:hover:not(:disabled){transform:translateY(-1px)} .btn-primary:disabled{opacity:.6;cursor:not-allowed}
.btn-ghost{padding:9px 18px;background:transparent;border:1px solid #e5e7eb;border-radius:9px;font-weight:700;cursor:pointer;font-size:14px}
.btn-ghost:hover{border-color:#ff6b00;color:#ff6b00}
.card{border-radius:18px;background:rgba(255,255,255,0.84);border:1px solid rgba(255,255,255,0.7);box-shadow:0 8px 24px rgba(15,23,42,0.07)}
.table-card{overflow:hidden}.table-scroll{overflow-x:auto}
.state-center{display:flex;justify-content:center;align-items:center;padding:40px}
.empty-text{color:#9ca3af;font-size:14px}
.spinner{width:36px;height:36px;border:4px solid rgba(255,107,0,.2);border-top-color:#ff6b00;border-radius:50%;animation:spin .9s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
.data-table{width:100%;border-collapse:collapse;font-size:13px}
.data-table th{padding:11px 14px;text-align:left;font-size:11px;font-weight:800;color:#6b7280;text-transform:uppercase;background:#f9fafb;border-bottom:1px solid #e5e7eb;white-space:nowrap}
.data-table td{padding:11px 14px;border-bottom:1px solid #f3f4f6;vertical-align:middle}
.data-table tr:last-child td{border-bottom:none}
.data-table tr:hover td{background:#fff7ed}
.td-code{font-family:monospace;font-weight:800;color:#ff6b00;font-size:13px}
.td-name{font-weight:700;color:#111827;max-width:160px}
.td-val{font-weight:700}
.td-date{font-size:12px;color:#9ca3af;white-space:nowrap}
.tbadge{padding:3px 9px;border-radius:999px;font-size:10px;font-weight:800}
.tbadge--blue{background:#dbeafe;color:#1e40af}.tbadge--purple{background:#ede9fe;color:#5b21b6}
.toggle-btn{padding:4px 12px;border-radius:6px;font-size:12px;font-weight:800;cursor:pointer;border:none;transition:all .2s}
.toggle-btn--on{background:#dcfce7;color:#166534}.toggle-btn--on:hover{background:#bbf7d0}
.toggle-btn--off{background:#f3f4f6;color:#6b7280}.toggle-btn--off:hover{background:#e5e7eb}
.act-btns{display:flex;gap:6px}
.btn-icon{width:30px;height:30px;border:none;border-radius:7px;cursor:pointer;font-size:13px;transition:all .2s}
.btn-edit{background:#dbeafe}.btn-edit:hover{background:#bfdbfe}
.btn-del{background:#fee2e2}.btn-del:hover{background:#fecaca}
.modal-overlay{position:fixed;inset:0;background:rgba(0,0,0,.45);display:flex;align-items:center;justify-content:center;z-index:500;padding:20px}
.modal{background:white;border-radius:18px;padding:28px;width:100%;max-width:640px;max-height:90vh;overflow-y:auto}
.modal-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}
.modal-head h2{font-size:18px;font-weight:900;margin:0}
.modal-close{width:30px;height:30px;background:#f3f4f6;border:none;border-radius:50%;cursor:pointer;font-size:14px}
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:14px}
.field{display:flex;flex-direction:column;gap:5px}
.field label{font-size:11px;font-weight:800;color:#6b7280;text-transform:uppercase}
.field input,.field select{padding:9px 12px;border:1px solid #e5e7eb;border-radius:8px;font-size:14px;font-family:inherit}
.field input:focus,.field select:focus{outline:none;border-color:#ff6b00}
.field input:disabled{background:#f9fafb;color:#9ca3af;cursor:not-allowed}
.form-err{color:#ef4444;font-size:13px;margin-top:8px}
.modal-footer{display:flex;gap:10px;justify-content:flex-end;margin-top:20px}
</style>

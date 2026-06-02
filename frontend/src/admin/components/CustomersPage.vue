<template>
  <div class="customers-page">
    <transition name="toast"><div v-if="toast.show" :class="['toast',`toast--${toast.type}`]">{{ toast.msg }}</div></transition>

    <!-- Toolbar -->
    <div class="toolbar">
      <input v-model="search" placeholder="🔍 Tìm tên, email..." class="search-input" @input="page=1" />
      <div class="total-tag">{{ total }} người dùng</div>
    </div>

    <!-- Table -->
    <div class="card table-card">
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>
      <div v-else-if="pageData.length===0" class="state-center empty-text">Không tìm thấy người dùng</div>
      <div v-else class="table-scroll">
        <table class="data-table">
          <thead><tr>
            <th>Họ tên</th><th>Email</th><th>Vai trò</th><th>Hạng</th>
            <th>Chi tiêu</th><th>Trạng thái</th><th>Ngày tạo</th><th>Thao tác</th>
          </tr></thead>
          <tbody>
            <tr v-for="u in pageData" :key="u.id">
              <td class="td-name">{{ u.hoTen || '—' }}</td>
              <td class="td-email">{{ u.email }}</td>
              <td><span :class="['rbadge', u.vaiTro==='admin'?'rbadge--admin':'']">{{ u.vaiTro }}</span></td>
              <td><span :class="['lbadge', levelClass(u.capDoThanhVien)]">{{ u.capDoThanhVien||'Thường' }}</span></td>
              <td class="td-spent">{{ fmtPrice(u.tongTienDaChi) }}</td>
              <td>
                <span :class="['sbadge', u.trangThai?'sbadge--green':'sbadge--red']">
                  {{ u.trangThai ? 'Hoạt động' : 'Bị khóa' }}
                </span>
              </td>
              <td class="td-date">{{ fmtDate(u.ngayTao) }}</td>
              <td>
                <div class="act-btns">
                  <button v-if="u.trangThai" class="btn-sm btn-lock" @click="openLock(u)">🔒 Khóa</button>
                  <button v-else class="btn-sm btn-unlock" @click="unlock(u)">🔓 Mở</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Pagination -->
      <div class="pagination" v-if="totalPages > 1">
        <button class="pg-btn" :disabled="page===1" @click="page--;loadUsers()">‹ Trước</button>
        <span class="pg-info">{{ page }} / {{ totalPages }}</span>
        <button class="pg-btn" :disabled="page>=totalPages" @click="page++;loadUsers()">Sau ›</button>
      </div>
    </div>

    <!-- Lock modal -->
    <div v-if="lockModal" class="modal-overlay" @click.self="lockModal=false">
      <div class="modal modal--sm">
        <div class="modal-head">
          <h2>Khóa tài khoản</h2>
          <button class="modal-close" @click="lockModal=false">✕</button>
        </div>
        <p class="lock-user-info">{{ lockTarget?.hoTen || lockTarget?.email }}</p>
        <div class="field">
          <label>Lý do khóa *</label>
          <textarea v-model="lockReason" rows="4" placeholder="Nhập lý do khóa tài khoản..." class="textarea"></textarea>
        </div>
        <p v-if="lockErr" class="form-err">{{ lockErr }}</p>
        <div class="modal-footer">
          <button class="btn-ghost" @click="lockModal=false">Hủy</button>
          <button class="btn-danger" :disabled="locking" @click="doLock">{{ locking?'Đang xử lý...':'Xác nhận khóa' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive } from 'vue'
import api from '@/services/api'

const toast = reactive({ show:false, msg:'', type:'success' })
let toastTimer = null
function showToast(msg, type='error') {
  clearTimeout(toastTimer); toast.msg=msg; toast.type=type; toast.show=true
  toastTimer = setTimeout(() => toast.show=false, 3500)
}

const allUsers   = ref([])
const loading    = ref(false)
const search     = ref('')
const page       = ref(1)
const PAGE_SIZE  = 20
const lockModal  = ref(false)
const lockTarget = ref(null)
const lockReason = ref('')
const lockErr    = ref('')
const locking    = ref(false)

const filteredUsers = computed(() => {
  const q = search.value.toLowerCase()
  if (!q) return allUsers.value
  return allUsers.value.filter(u =>
    u.hoTen?.toLowerCase().includes(q) || u.email?.toLowerCase().includes(q)
  )
})
const total      = computed(() => filteredUsers.value.length)
const totalPages = computed(() => Math.ceil(total.value / PAGE_SIZE))
const pageData   = computed(() => {
  const s = (page.value-1)*PAGE_SIZE
  return filteredUsers.value.slice(s, s+PAGE_SIZE)
})

watch(search, () => { page.value=1 })

function levelClass(l) {
  if (l==='Kim Cương') return 'lbadge--diamond'
  if (l==='Vàng')      return 'lbadge--gold'
  if (l==='Bạc')       return 'lbadge--silver'
  return ''
}
function fmtPrice(v) {
  return new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(v||0)
}
function fmtDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('vi-VN')
}

function openLock(u) {
  lockTarget.value=u; lockReason.value=''; lockErr.value=''; lockModal.value=true
}
async function doLock() {
  if (!lockReason.value.trim()) { lockErr.value='Vui lòng nhập lý do'; return }
  locking.value=true; lockErr.value=''
  try {
    await api.put(`/admin/users/${lockTarget.value.id}/lock`, { reason: lockReason.value })
    const u = allUsers.value.find(x => x.id===lockTarget.value.id)
    if (u) { u.trangThai=false; u.lyDoKhoa=lockReason.value }
    showToast('Đã khóa tài khoản', 'success')
    lockModal.value=false
  } catch(e) { lockErr.value = e.response?.data?.message || 'Lỗi khóa tài khoản' }
  finally { locking.value=false }
}
async function unlock(u) {
  if (!confirm(`Mở khóa tài khoản "${u.hoTen||u.email}"?`)) return
  try {
    await api.put(`/admin/users/${u.id}/unlock`)
    u.trangThai=true; u.lyDoKhoa=null
    showToast('Đã mở khóa tài khoản', 'success')
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi mở khóa') }
}

async function loadUsers() {
  loading.value=true
  try {
    const r = await api.get(`/admin/users?page=0&size=1000`)
    allUsers.value = r.data?.content || r.data || []
  } catch(e) { showToast('Không tải được danh sách người dùng') }
  finally { loading.value=false }
}
onMounted(loadUsers)
</script>

<style scoped>
.customers-page { display:flex; flex-direction:column; gap:20px; }
.toast { position:fixed; top:20px; right:20px; z-index:999; padding:12px 20px; border-radius:10px; font-size:13px; font-weight:700; }
.toast--error   { background:#7f1d1d; color:#fecaca; }
.toast--success { background:#14532d; color:#bbf7d0; }
.toast-enter-active,.toast-leave-active{transition:opacity .3s}.toast-enter-from,.toast-leave-to{opacity:0}
.toolbar { display:flex; gap:10px; align-items:center; flex-wrap:wrap; }
.search-input { flex:1; min-width:220px; padding:9px 14px; border:1px solid #e5e7eb; border-radius:9px; font-size:14px; }
.search-input:focus { outline:none; border-color:#29bcea; }
.total-tag { padding:6px 14px; background:#f3f4f6; border-radius:999px; font-size:13px; font-weight:700; color:#6b7280; white-space:nowrap; }
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
.td-name  { font-weight:700; color:#111827; }
.td-email { color:#6b7280; font-size:12px; }
.td-spent { font-weight:700; color:#29bcea; white-space:nowrap; }
.td-date  { font-size:12px; color:#9ca3af; white-space:nowrap; }
.rbadge { padding:3px 9px; border-radius:999px; font-size:10px; font-weight:800; background:#f3f4f6; color:#6b7280; }
.rbadge--admin { background:#fee2e2; color:#991b1b; }
.lbadge { padding:3px 9px; border-radius:999px; font-size:10px; font-weight:800; background:#f3f4f6; color:#6b7280; }
.lbadge--silver  { background:#e2e8f0; color:#475569; }
.lbadge--gold    { background:#fef3c7; color:#92400e; }
.lbadge--diamond { background:#ede9fe; color:#5b21b6; }
.sbadge { padding:3px 9px; border-radius:999px; font-size:11px; font-weight:800; }
.sbadge--green { background:#dcfce7; color:#166534; }
.sbadge--red   { background:#fee2e2; color:#991b1b; }
.act-btns { display:flex; gap:6px; }
.btn-sm { padding:5px 10px; border:none; border-radius:7px; font-size:12px; font-weight:700; cursor:pointer; transition:all .2s; }
.btn-lock   { background:#fee2e2; color:#991b1b; } .btn-lock:hover   { background:#fecaca; }
.btn-unlock { background:#dcfce7; color:#166534; } .btn-unlock:hover { background:#bbf7d0; }
.pagination { display:flex; align-items:center; justify-content:center; gap:14px; padding:14px 20px; border-top:1px solid #f3f4f6; }
.pg-btn { padding:7px 16px; border:1px solid #e5e7eb; border-radius:8px; background:white; font-weight:700; cursor:pointer; }
.pg-btn:hover:not(:disabled) { border-color:#29bcea; color:#29bcea; }
.pg-btn:disabled { opacity:.4; cursor:not-allowed; }
.pg-info { font-size:13px; color:#6b7280; font-weight:700; }
.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,.45); display:flex; align-items:center; justify-content:center; z-index:500; padding:20px; }
.modal { background:white; border-radius:18px; padding:28px; width:100%; max-width:480px; }
.modal--sm { max-width:420px; }
.modal-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.modal-head h2 { font-size:17px; font-weight:900; margin:0; }
.modal-close { width:30px; height:30px; background:#f3f4f6; border:none; border-radius:50%; cursor:pointer; font-size:14px; }
.lock-user-info { font-size:14px; font-weight:700; color:#374151; margin:0 0 14px; padding:10px 14px; background:#f9fafb; border-radius:8px; }
.field { display:flex; flex-direction:column; gap:5px; margin-bottom:4px; }
.field label { font-size:11px; font-weight:800; color:#6b7280; text-transform:uppercase; }
.textarea { padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px; font-family:inherit; resize:vertical; }
.textarea:focus { outline:none; border-color:#29bcea; }
.form-err { color:#ef4444; font-size:13px; margin-top:8px; }
.modal-footer { display:flex; gap:10px; justify-content:flex-end; margin-top:20px; }
.btn-ghost  { padding:9px 18px; background:transparent; border:1px solid #e5e7eb; border-radius:9px; font-weight:700; cursor:pointer; font-size:14px; }
.btn-ghost:hover { border-color:#29bcea; color:#29bcea; }
.btn-danger { padding:9px 18px; background:#dc2626; color:white; border:none; border-radius:9px; font-weight:800; cursor:pointer; font-size:14px; }
.btn-danger:disabled { opacity:.6; cursor:not-allowed; }
.btn-danger:hover:not(:disabled) { background:#b91c1c; }
</style>

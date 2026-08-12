<template>
  <div class="customers-page">
    <transition name="toast">
      <div v-if="toast.show" :class="['toast', `toast--${toast.type}`]">{{ toast.msg }}</div>
    </transition>

    <!-- Toolbar -->
    <div class="toolbar">
      <span class="badge-count">{{ total }} người dùng</span>
      <button class="btn-primary" @click="openCreateModal">+ Tạo tài khoản</button>
    </div>

    <!-- Table -->
    <div class="card table-card">
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>
      <div v-else-if="pageData.length===0" class="state-center empty-text">Không tìm thấy người dùng</div>
      <div v-else class="table-scroll">
        <table class="data-table">
          <thead>
            <tr>
              <th>Họ tên</th><th>Email</th><th>Vai trò</th><th>Hạng</th>
              <th>Điểm tích lũy</th><th>Chi tiêu</th><th>Trạng thái</th><th>Ngày tạo</th><th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in pageData" :key="u.id">
              <td class="td-name">
                <input
                  class="text-inline"
                  type="text"
                  :value="u.hoTen || ''"
                  placeholder="-"
                  @keydown.enter.prevent="inlineEditProfile(u, 'hoTen', $event.target)"
                  @blur="revertIfUnchanged(u, 'hoTen', $event.target)"
                  title="Nhấn Enter để lưu tên mới"
                />
              </td>
              <td class="td-email">
                <input
                  class="text-inline text-inline--email"
                  type="email"
                  :value="u.email"
                  @keydown.enter.prevent="inlineEditProfile(u, 'email', $event.target)"
                  @blur="revertIfUnchanged(u, 'email', $event.target)"
                  title="Nhấn Enter để lưu email mới"
                />
              </td>
              <td>
                <select
                  class="role-select"
                  :value="u.vaiTro"
                  @change="changeRole(u, $event.target.value)"
                  title="Thay đổi vai trò"
                >
                  <option value="customer">customer</option>
                  <option value="staff">staff</option>
                  <option value="admin">admin</option>
                </select>
              </td>
              <td>
                <span :class="['lbadge', levelClass(u.capDoThanhVien)]">{{ u.capDoThanhVien || 'Thường' }}</span>
              </td>
              <td class="td-points">
                <input
                  :class="['points-inline', Number(u.diemTichLuy) > 0 ? 'points-pos' : 'points-zero']"
                  type="number"
                  :value="u.diemTichLuy || 0"
                  min="0"
                  @keydown.enter.prevent="inlineAdjustPoints(u, $event.target)"
                  @blur="$event.target.value = u.diemTichLuy || 0"
                  title="Nhập điểm mới và nhấn Enter để điều chỉnh"
                />
              </td>
              <td class="td-spent">{{ fmtPrice(u.tongTienDaChi) }}</td>
              <td>
                <span :class="['sbadge', u.trangThai ? 'sbadge--green' : 'sbadge--red']">
                  {{ u.trangThai ? 'Hoạt động' : 'Bị khóa' }}
                </span>
              </td>
              <td class="td-date">{{ fmtDate(u.ngayTao) }}</td>
              <td>
                <div class="act-btns">
                  <button v-if="u.trangThai" class="btn-sm btn-lock" @click="openLock(u)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="13" height="13"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                    Khóa
                  </button>
                  <button v-else class="btn-sm btn-unlock" @click="unlock(u)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="13" height="13"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 9.9-1"/></svg>
                    Mở
                  </button>
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
          <textarea v-model="lockReason" rows="4" placeholder="Nhập lý do khóa tài khoản..."></textarea>
        </div>
        <p v-if="lockErr" class="form-err">{{ lockErr }}</p>
        <div class="modal-footer">
          <button class="btn-ghost" @click="lockModal=false">Hủy</button>
          <button class="btn-danger" :disabled="locking" @click="doLock">{{ locking?'Đang xử lý...':'Xác nhận khóa' }}</button>
        </div>
      </div>
    </div>

    <!-- Create account modal -->
    <div v-if="createModal" class="modal-overlay" @click.self="createModal=false">
      <div class="modal">
        <div class="modal-head">
          <h2>Tạo tài khoản mới</h2>
          <button class="modal-close" @click="createModal=false">✕</button>
        </div>
        <div class="form-grid">
          <div class="field field-full">
            <label>Họ tên *</label>
            <input v-model="createForm.hoTen" placeholder="Nguyễn Văn A" />
          </div>
          <div class="field field-full">
            <label>Email *</label>
            <input v-model="createForm.email" type="email" placeholder="nhanvien@cinema.com" />
          </div>
          <div class="field">
            <label>Số điện thoại</label>
            <input v-model="createForm.soDienThoai" placeholder="0901234567" />
          </div>
          <div class="field">
            <label>Vai trò *</label>
            <select v-model="createForm.vaiTro">
              <option value="staff">Staff (Nhân viên)</option>
              <option value="admin">Admin (Quản trị)</option>
            </select>
          </div>
          <div class="field field-full">
            <label>Mật khẩu * (tối thiểu 8 ký tự)</label>
            <input v-model="createForm.password" type="password" placeholder="••••••••" autocomplete="new-password" />
          </div>
        </div>
        <p v-if="createErr" class="form-err">{{ createErr }}</p>
        <div class="modal-footer">
          <button class="btn-ghost" @click="createModal=false">Hủy</button>
          <button class="btn-primary" :disabled="creating" @click="doCreate">{{ creating ? 'Đang tạo...' : 'Tạo tài khoản' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive } from 'vue'
import api from '@/services/api'
import { useAdminShellStore } from '@/stores/adminShellStore'

const shell = useAdminShellStore()

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

// Create account state
const createModal = ref(false)
const createForm  = ref({ hoTen: '', email: '', soDienThoai: '', password: '', vaiTro: 'staff' })
const createErr   = ref('')
const creating    = ref(false)

function openCreateModal() {
  createForm.value = { hoTen: '', email: '', soDienThoai: '', password: '', vaiTro: 'staff' }
  createErr.value = ''
  createModal.value = true
}

async function doCreate() {
  const { hoTen, email, password, vaiTro } = createForm.value
  if (!hoTen.trim() || !email.trim() || !password.trim()) {
    createErr.value = 'Vui lòng điền đầy đủ Họ tên, Email và Mật khẩu'
    return
  }
  creating.value = true; createErr.value = ''
  try {
    const res = await api.post('/admin/users', {
      hoTen: hoTen.trim(),
      email: email.trim().toLowerCase(),
      soDienThoai: createForm.value.soDienThoai?.trim() || null,
      password,
      vaiTro,
    })
    allUsers.value.unshift(res.data)
    createModal.value = false
    showToast(`Đã tạo tài khoản ${vaiTro} cho ${email}`, 'success')
  } catch(e) {
    createErr.value = e.response?.data?.message || 'Lỗi tạo tài khoản'
  } finally {
    creating.value = false
  }
}

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

// Inline points editing — Enter key handler
async function inlineAdjustPoints(u, inputEl) {
  const newVal = parseInt(inputEl.value, 10)
  if (isNaN(newVal) || newVal < 0) { inputEl.value = u.diemTichLuy || 0; return }
  const oldVal = u.diemTichLuy || 0
  if (newVal === oldVal) return
  const delta = newVal - oldVal
  const lyDo = prompt(
    `Xác nhận đổi điểm tích lũy của "${u.hoTen || u.email}" từ ${oldVal} → ${newVal}?\n\nLý do điều chỉnh (tuỳ chọn):`,
    ''
  )
  if (lyDo === null) { inputEl.value = oldVal; return }   // user clicked Cancel
  try {
    const res = await api.put(`/admin/users/${u.id}/points`, { delta, lyDo: lyDo.trim() || null })
    u.diemTichLuy = res.data.diemTichLuy
    inputEl.value = res.data.diemTichLuy
    showToast(`Điểm đã cập nhật: ${res.data.diemTichLuy}`, 'success')
  } catch(e) {
    inputEl.value = oldVal
    showToast(e.response?.data?.message || 'Lỗi điều chỉnh điểm')
  }
}

// Inline profile editing — hoTen and email
// NOTE: Changing a user's email invalidates their existing JWT (the old email principal
// no longer matches the DB record). The user must log in again with the new email.
async function inlineEditProfile(u, field, inputEl) {
  const newVal = inputEl.value.trim()
  const oldVal = field === 'hoTen' ? (u.hoTen || '') : u.email

  if (newVal === oldVal) return   // nothing changed

  // Client-side validation before hitting the server
  if (field === 'hoTen' && !newVal) {
    showToast('Họ tên không được để trống')
    inputEl.value = oldVal
    return
  }
  if (field === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(newVal)) {
    showToast('Email không hợp lệ')
    inputEl.value = oldVal
    return
  }

  const label = field === 'hoTen' ? 'họ tên' : 'email'
  const emailWarning = field === 'email'
    ? '\n\n⚠️ Người dùng sẽ cần đăng nhập lại và các email xác thực sẽ được gửi tới địa chỉ mới.'
    : ''
  const confirmed = confirm(
    `Xác nhận đổi ${label} của "${u.hoTen || u.email}"?\n\nCũ: ${oldVal}\nMới: ${newVal}${emailWarning}`
  )
  if (!confirmed) { inputEl.value = oldVal; return }

  try {
    const res = await api.put(`/admin/users/${u.id}/profile`, { [field]: newVal })
    u.hoTen = res.data.hoTen
    u.email = res.data.email
    inputEl.value = newVal
    const notifNote = res.data.emailChanged ? ' Email thông báo đã được gửi tới cả hai địa chỉ.' : ''
    showToast(`Đã cập nhật ${label}.${notifNote}`, 'success')
  } catch(e) {
    inputEl.value = oldVal
    showToast(e.response?.data?.message || `Lỗi cập nhật ${label}`)
  }
}

// Revert field to the stored value on blur if value matches original (no Enter pressed)
function revertIfUnchanged(u, field, inputEl) {
  const storedVal = field === 'hoTen' ? (u.hoTen || '') : u.email
  // Only revert if the user blurred without pressing Enter — i.e. value now differs from stored
  // but we don't want to trigger a save on blur. Simply restore the stored value.
  inputEl.value = storedVal
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

async function changeRole(u, newRole) {
  if (newRole === u.vaiTro) return
  if (!confirm(`Thay đổi vai trò của "${u.hoTen||u.email}" từ "${u.vaiTro}" → "${newRole}"?`)) return
  try {
    await api.put(`/admin/users/${u.id}/role`, { vaiTro: newRole })
    u.vaiTro = newRole
    showToast(`Đã cập nhật vai trò thành "${newRole}"`, 'success')
  } catch(e) { showToast(e.response?.data?.message || 'Lỗi thay đổi vai trò') }
}

async function loadUsers() {
  loading.value=true
  try {
    const r = await api.get(`/admin/users?page=0&size=1000`)
    allUsers.value = r.data?.content || r.data || []
  } catch(e) { showToast('Không tải được danh sách người dùng') }
  finally { loading.value=false }
}
function syncFromShell() {
  if (shell.searchTargetPage === 'customers' && shell.searchQuery) {
    search.value = shell.searchQuery
    page.value = 1
    loadUsers()
  }
}

watch(() => shell.searchTick, syncFromShell)

onMounted(() => {
  syncFromShell()
  loadUsers()
})
</script>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; }

/* ── Page root ── */
.customers-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--admin-bg);
  color: var(--admin-text);
}

/* ── Cell helpers ── */
.td-name  { font-weight: 600; font-size: 14px; color: var(--admin-text); }
.td-email { font-size: 12px; color: var(--admin-text-muted); }
.td-date  { font-size: 12px; color: var(--admin-text-muted); }
.td-spent { font-weight: 700; color: var(--admin-accent); }
.td-points { white-space: nowrap; }

/* Inline text inputs for hoTen and email — same visual language as points-inline */
.text-inline {
  width: 100%;
  min-width: 80px;
  max-width: 180px;
  padding: 2px 4px;
  background: transparent !important;
  border: none !important;
  border-bottom: 1px dashed var(--admin-border) !important;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 14px;
  font-weight: 600;
  color: var(--admin-text) !important;
  cursor: text;
  outline: none;
  min-height: unset !important;
  transition: border-bottom-color 150ms;
}
.text-inline:focus {
  border-bottom-color: var(--admin-accent) !important;
  background: var(--admin-accent-muted) !important;
}
.text-inline--email {
  font-size: 12px !important;
  font-weight: 400 !important;
  color: var(--admin-text-muted) !important;
  max-width: 200px;
}
.text-inline--email:focus { color: var(--admin-text) !important; }


/* Color is set via scoped class + important. Specificity analysis:
   admin-theme: .admin-container input-type-number = (0,2,1)
   Our rule: input.points-zero.points-inline[data-v-X] = (0,3,1) wins. */
.points-inline {
  width: 72px;
  padding: 2px 4px;
  background: transparent !important;
  border: none !important;
  border-bottom: 1px dashed var(--admin-border) !important;
  font-family: var(--font-ui, 'Inter', sans-serif);
  font-size: 13px;
  font-weight: 700;
  text-align: right;
  cursor: text;
  outline: none;
  min-height: unset !important;
  transition: border-bottom-color 150ms;
  -moz-appearance: textfield;
}
.points-inline::-webkit-outer-spin-button,
.points-inline::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
.points-inline:focus { border-bottom-color: var(--admin-accent) !important; background: var(--admin-accent-muted) !important; }
/* Specificity analysis:
   admin-theme: .admin-container input[type='number'] = (0,2,1) — class + attr + element
   Previous attempt: .points-zero[data-v-xxx]          = (0,2,0) — LOSES to (0,2,1)
   This fix: .points-zero.points-inline[data-v-xxx]    = (0,3,0) — WINS over (0,2,1) */
input.points-zero.points-inline { color: #f87171 !important; }
input.points-pos.points-inline  { color: #4ade80 !important; }

/* Role dropdown — scoped !important overrides the global .admin-container select rule
   (which sets background-image, padding-right, etc. with !important).
   Scoped [data-v-*] attribute makes our selector more specific, so our !important wins. */
.role-select {
  display: block !important;
  width: 100% !important;
  max-width: 110px !important;
  padding: 5px 28px 5px 10px !important;
  background: var(--admin-accent-muted) !important;
  border: 1px solid rgba(255,255,255,0.12) !important;
  border-radius: 8px !important;
  color: var(--admin-text) !important;
  font-family: var(--font-ui, 'Inter', sans-serif) !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  cursor: pointer !important;
  -webkit-appearance: none !important;
  appearance: none !important;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='6' viewBox='0 0 10 6'%3E%3Cpath d='M1 1l4 4 4-4' stroke='%239CA3AF' stroke-width='1.5' fill='none' stroke-linecap='round'/%3E%3C/svg%3E") !important;
  background-repeat: no-repeat !important;
  background-position: right 8px center !important;
  transition: border-color 150ms, background 150ms;
  min-height: 30px !important;
}
.role-select:hover {
  border-color: rgba(255,255,255,0.3) !important;
  background: var(--admin-accent-muted) !important;
}
.role-select:focus {
  outline: none !important;
  border-color: #29bcea !important;
  box-shadow: 0 0 0 1px rgba(41,188,234,0.2) !important;
}

/* Native <option> popup — must be :global because Vue scoped [data-v-*] attributes
   are NOT injected into OS-rendered native option popups. Without :global this rule
   would compile to `.role-select option[data-v-xxx]` which the browser never matches
   for the dropdown popup. The :global wrapper emits an unscoped rule that the OS
   popup CAN pick up on Chromium-based browsers. */
:global(.role-select option) {
  background: var(--admin-surface-hover);
  color: var(--admin-text);
}
</style>

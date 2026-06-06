<template>
  <div class="customers-page">
    <transition name="toast">
      <div v-if="toast.show" :class="['toast', `toast--${toast.type}`]">{{ toast.msg }}</div>
    </transition>

    <!-- Toolbar -->
    <div class="toolbar">
      <input v-model="search" placeholder="Tìm tên, email..." class="search-input" @input="page=1" />
      <span class="badge-count">{{ total }} người dùng</span>
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
              <th>Chi tiêu</th><th>Trạng thái</th><th>Ngày tạo</th><th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in pageData" :key="u.id">
              <td class="td-name">{{ u.hoTen || '—' }}</td>
              <td class="td-email">{{ u.email }}</td>
              <td>
                <span :class="['sbadge', u.vaiTro==='admin' ? 'sbadge--amber' : 'sbadge--gray']">{{ u.vaiTro }}</span>
              </td>
              <td>
                <span :class="['lbadge', levelClass(u.capDoThanhVien)]">{{ u.capDoThanhVien || 'Thường' }}</span>
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
.toast-enter-active,
.toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from,
.toast-leave-to { opacity: 0; }
</style>

<template>
  <div class="profile-page">
    <header class="top-bar">
      <button class="icon-btn" @click="router.push('/')" aria-label="Về trang chủ">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Hồ sơ cá nhân</span>
      <div style="width:38px"></div>
    </header>

    <!-- Toast -->
    <transition name="toast">
      <div v-if="toast.show" :class="['toast', `toast--${toast.type}`]">
        {{ toast.msg }}
      </div>
    </transition>

    <!-- Loading skeleton -->
    <div v-if="loadingProfile" class="content">
      <div class="skeleton-card">
        <div class="skel skel--circle"></div>
        <div class="skel-lines">
          <div class="skel skel--line skel--w60"></div>
          <div class="skel skel--line skel--w40"></div>
        </div>
      </div>
      <div class="skeleton-card">
        <div class="skel skel--line"></div>
        <div class="skel skel--line skel--w80"></div>
        <div class="skel skel--line skel--w60"></div>
      </div>
    </div>

    <div v-else class="content">
      <!-- Avatar + Member -->
      <div class="hero-card">
        <div class="avatar-wrap">
          <img v-if="authStore.user?.anhDaiDien" :src="authStore.user.anhDaiDien" :alt="authStore.user.hoTen" class="avatar-img" @error="e => e.target.style.display='none'" />
          <div v-else class="avatar-initials">{{ initials }}</div>
          <span :class="['member-badge', memberClass]">{{ memberLevel }}</span>
        </div>
        <div class="hero-info">
          <h1 class="hero-name">{{ authStore.user?.hoTen || authStore.user?.email }}</h1>
          <p class="hero-email">{{ authStore.user?.email }}</p>
          <div class="stats-row">
            <div class="stat">
              <span class="stat-val">{{ authStore.user?.diemTichLuy || 0 }}</span>
              <span class="stat-lbl">Điểm tích lũy</span>
            </div>
            <div class="stat-div"></div>
            <div class="stat">
              <span class="stat-val">{{ fmtPrice(authStore.user?.tongTienDaChi || 0) }}</span>
              <span class="stat-lbl">Tổng chi tiêu</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick nav -->
      <div class="quick-nav">
        <button class="qnav-btn" @click="router.push('/my-tickets')">🎟️ Vé của tôi</button>
        <button class="qnav-btn" @click="router.push('/transaction-history')">📋 Lịch sử GD</button>
        <button class="qnav-btn qnav-btn--danger" @click="doLogout">🔓 Đăng xuất</button>
      </div>

      <!-- Edit profile form -->
      <div class="card">
        <h2 class="card-title">Thông tin cá nhân</h2>
        <div class="form-grid">
          <div class="field">
            <label>Họ tên</label>
            <input v-model="form.hoTen" placeholder="Họ và tên" />
          </div>
          <div class="field">
            <label>Email</label>
            <input :value="authStore.user?.email" disabled class="field__input--disabled" />
          </div>
          <div class="field">
            <label>Số điện thoại</label>
            <input v-model="form.soDienThoai" placeholder="0xxxxxxxxx" />
          </div>
          <div class="field">
            <label>Ngày sinh</label>
            <input v-model="form.ngaySinh" type="date" />
          </div>
          <div class="field field--full">
            <label>Ảnh đại diện (URL)</label>
            <input v-model="form.anhDaiDien" placeholder="https://..." />
          </div>
        </div>
        <button class="btn-primary" :disabled="savingProfile" @click="saveProfile">
          <span v-if="savingProfile" class="btn-spin"></span>
          <span v-else>Lưu thay đổi</span>
        </button>
      </div>

      <!-- Change password -->
      <div class="card">
        <h2 class="card-title">Đổi mật khẩu</h2>
        <p class="card-hint">Nhập mật khẩu hiện tại và mật khẩu mới để thay đổi.</p>
        <div class="form-grid">
          <div class="field field--full">
            <label>Mật khẩu hiện tại</label>
            <div class="pw-wrap">
              <input :type="showPw.current ? 'text' : 'password'" v-model="pw.current" placeholder="Mật khẩu hiện tại" />
              <button class="pw-toggle" @click="showPw.current = !showPw.current" type="button">{{ showPw.current ? '🙈' : '👁️' }}</button>
            </div>
          </div>
          <div class="field">
            <label>Mật khẩu mới</label>
            <div class="pw-wrap">
              <input :type="showPw.new ? 'text' : 'password'" v-model="pw.new" placeholder="Tối thiểu 6 ký tự" />
              <button class="pw-toggle" @click="showPw.new = !showPw.new" type="button">{{ showPw.new ? '🙈' : '👁️' }}</button>
            </div>
          </div>
          <div class="field">
            <label>Xác nhận mật khẩu mới</label>
            <div class="pw-wrap">
              <input :type="showPw.confirm ? 'text' : 'password'" v-model="pw.confirm" placeholder="Nhập lại mật khẩu mới" />
              <button class="pw-toggle" @click="showPw.confirm = !showPw.confirm" type="button">{{ showPw.confirm ? '🙈' : '👁️' }}</button>
            </div>
          </div>
        </div>
        <p v-if="pw.new && pw.confirm && pw.new !== pw.confirm" class="field-err">Mật khẩu xác nhận không khớp</p>
        <button class="btn-primary" :disabled="savingPw || !pwValid" @click="changePw">
          <span v-if="savingPw" class="btn-spin"></span>
          <span v-else>Đổi mật khẩu</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'

const router    = useRouter()
const authStore = useAuthStore()

// ── loading ─────────────────────────────────────────────────
const loadingProfile = ref(true)

// ── toast ────────────────────────────────────────────────────
const toast = reactive({ show: false, msg: '', type: 'success' })
let toastTimer = null
function showToast(msg, type = 'success') {
  clearTimeout(toastTimer)
  toast.msg = msg; toast.type = type; toast.show = true
  toastTimer = setTimeout(() => { toast.show = false }, 3200)
}

// ── profile form ─────────────────────────────────────────────
const form = ref({ hoTen: '', soDienThoai: '', ngaySinh: '', anhDaiDien: '' })
const savingProfile = ref(false)

async function saveProfile() {
  savingProfile.value = true
  try {
    await api.put('/auth/profile', form.value)
    await authStore.fetchProfile()
    // Re-sync form after fetch
    syncForm()
    showToast('Cập nhật thông tin thành công')
  } catch (e) {
    showToast(e.response?.data || 'Lỗi cập nhật thông tin', 'error')
  } finally {
    savingProfile.value = false
  }
}

// ── password change ──────────────────────────────────────────
const pw = reactive({ current: '', new: '', confirm: '' })
const showPw = reactive({ current: false, new: false, confirm: false })
const savingPw = ref(false)

const pwValid = computed(() =>
  pw.current.length >= 1 &&
  pw.new.length >= 6 &&
  pw.new === pw.confirm
)

async function changePw() {
  if (!pwValid.value) return
  savingPw.value = true
  try {
    // POST /api/auth/change-password — backend may return 404 if not implemented yet;
    // fall back gracefully with a user-visible message
    await api.post('/auth/change-password', {
      currentPassword: pw.current,
      newPassword: pw.new,
    })
    pw.current = ''; pw.new = ''; pw.confirm = ''
    showToast('Đổi mật khẩu thành công')
  } catch (e) {
    const status = e.response?.status
    if (status === 404) {
      showToast('Tính năng đổi mật khẩu chưa được hỗ trợ. Dùng "Quên mật khẩu" tại trang đăng nhập.', 'error')
    } else {
      showToast(e.response?.data?.message || e.response?.data || 'Mật khẩu hiện tại không đúng', 'error')
    }
  } finally {
    savingPw.value = false
  }
}

// ── helpers ───────────────────────────────────────────────────
const initials = computed(() => {
  const name = authStore.user?.hoTen || authStore.user?.email || '?'
  return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
})

const memberLevel = computed(() => authStore.user?.capDoThanhVien || 'Thường')

const memberClass = computed(() => {
  const l = memberLevel.value
  if (l === 'Kim Cương') return 'badge--diamond'
  if (l === 'Vàng')      return 'badge--gold'
  if (l === 'Bạc')       return 'badge--silver'
  return 'badge--normal'
})

function fmtPrice(v) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0)
}

function syncForm() {
  const u = authStore.user
  if (!u) return
  form.value.hoTen       = u.hoTen        || ''
  form.value.soDienThoai = u.soDienThoai  || ''
  form.value.ngaySinh    = u.ngaySinh     || ''
  form.value.anhDaiDien  = u.anhDaiDien   || ''
}

function doLogout() {
  authStore.logout()
  router.push('/')
}

onMounted(async () => {
  if (!authStore.user) await authStore.fetchProfile()
  syncForm()
  loadingProfile.value = false
})
</script>

<style scoped>
.profile-page { background: linear-gradient(160deg,#0b1120 0%,#0f172a 50%,#1a1f35 100%); color: #f1f5f9; min-height: 100vh; padding-bottom: 60px; }

/* top bar */
.top-bar { display:flex; align-items:center; justify-content:space-between; padding:14px 20px; background:rgba(11,17,32,.9); backdrop-filter:blur(12px); border-bottom:1px solid rgba(255,215,0,.12); position:sticky; top:0; z-index:60; }
.top-bar__title { font-size:16px; font-weight:700; }
.icon-btn { width:38px; height:38px; border-radius:8px; border:1px solid rgba(255,215,0,.25); background:rgba(255,215,0,.07); color:#ffd700; cursor:pointer; display:flex; align-items:center; justify-content:center; transition:background .2s; }
.icon-btn:hover { background:rgba(255,215,0,.15); }
.icon-btn svg { width:18px; height:18px; }

/* toast */
.toast { position:fixed; top:72px; left:50%; transform:translateX(-50%); z-index:200; padding:12px 24px; border-radius:10px; font-size:14px; font-weight:700; pointer-events:none; white-space:nowrap; box-shadow:0 8px 24px rgba(0,0,0,.3); }
.toast--success { background:#166534; color:#bbf7d0; border:1px solid rgba(74,222,128,.3); }
.toast--error   { background:#7f1d1d; color:#fecaca; border:1px solid rgba(239,68,68,.3); }
.toast-enter-active,.toast-leave-active { transition:all .3s; }
.toast-enter-from,.toast-leave-to { opacity:0; transform:translateX(-50%) translateY(-12px); }

/* skeleton */
.content { max-width:680px; margin:0 auto; padding:28px 20px; display:flex; flex-direction:column; gap:20px; }
.skeleton-card { background:rgba(30,41,55,.5); border:1px solid rgba(255,215,0,.07); border-radius:14px; padding:24px; display:flex; gap:16px; align-items:center; }
.skel-lines { flex:1; display:flex; flex-direction:column; gap:10px; }
.skel { background:linear-gradient(90deg,rgba(255,255,255,.06) 25%,rgba(255,255,255,.12) 50%,rgba(255,255,255,.06) 75%); background-size:200% 100%; animation:shimmer 1.4s infinite; border-radius:6px; }
.skel--circle { width:64px; height:64px; border-radius:50%; flex-shrink:0; }
.skel--line { height:14px; width:100%; }
.skel--w80 { width:80%; }
.skel--w60 { width:60%; }
.skel--w40 { width:40%; }
@keyframes shimmer { to { background-position:-200% 0; } }

/* hero card */
.hero-card { background:rgba(30,41,55,.5); border:1px solid rgba(255,215,0,.15); border-radius:16px; padding:24px; display:flex; gap:20px; align-items:flex-start; }
.avatar-wrap { position:relative; flex-shrink:0; }
.avatar-img { width:72px; height:72px; border-radius:50%; object-fit:cover; border:3px solid rgba(255,215,0,.4); }
.avatar-initials { width:72px; height:72px; border-radius:50%; background:linear-gradient(135deg,#ffd700,#ffed4e); color:#0f172a; font-size:24px; font-weight:900; display:flex; align-items:center; justify-content:center; }
.member-badge { position:absolute; bottom:-6px; left:50%; transform:translateX(-50%); padding:2px 10px; border-radius:999px; font-size:10px; font-weight:900; white-space:nowrap; }
.badge--normal  { background:rgba(148,163,184,.2); color:#94a3b8; }
.badge--silver  { background:rgba(203,213,225,.25); color:#cbd5e1; }
.badge--gold    { background:rgba(255,215,0,.2); color:#ffd700; }
.badge--diamond { background:rgba(139,92,246,.2); color:#c4b5fd; }
.hero-info { flex:1; }
.hero-name  { font-size:20px; font-weight:900; margin:0 0 4px; }
.hero-email { font-size:13px; color:#64748b; margin:0 0 14px; }
.stats-row  { display:flex; align-items:center; gap:20px; }
.stat { display:flex; flex-direction:column; gap:2px; }
.stat-val   { font-size:16px; font-weight:900; color:#ffd700; }
.stat-lbl   { font-size:11px; color:#64748b; font-weight:600; }
.stat-div   { width:1px; height:32px; background:rgba(255,215,0,.15); }

/* quick nav */
.quick-nav { display:flex; gap:8px; flex-wrap:wrap; }
.qnav-btn { flex:1; min-width:130px; padding:11px 14px; border-radius:10px; background:rgba(255,215,0,.07); border:1px solid rgba(255,215,0,.15); color:#f1f5f9; font-size:13px; font-weight:700; cursor:pointer; transition:all .2s; }
.qnav-btn:hover { border-color:#ffd700; background:rgba(255,215,0,.12); }
.qnav-btn--danger { border-color:rgba(239,68,68,.2); color:#fca5a5; }
.qnav-btn--danger:hover { background:rgba(239,68,68,.08); border-color:rgba(239,68,68,.4); }

/* card */
.card { background:rgba(30,41,55,.5); border:1px solid rgba(255,215,0,.1); border-radius:14px; padding:24px; }
.card-title { font-size:16px; font-weight:800; color:#ffd700; margin:0 0 16px; }
.card-hint  { font-size:13px; color:#64748b; margin:-8px 0 16px; }

/* form */
.form-grid { display:grid; grid-template-columns:1fr 1fr; gap:14px; margin-bottom:18px; }
.field { display:flex; flex-direction:column; gap:5px; }
.field--full { grid-column:1/-1; }
.field label { font-size:11px; font-weight:700; color:#64748b; text-transform:uppercase; letter-spacing:.4px; }
.field input { padding:10px 12px; border:1px solid rgba(255,215,0,.2); border-radius:8px; background:rgba(255,215,0,.04); color:#f1f5f9; font-size:14px; transition:border-color .2s; }
.field input:focus { outline:none; border-color:#ffd700; background:rgba(255,215,0,.08); }
.field__input--disabled { opacity:.45; cursor:not-allowed; }
.field-err { font-size:12px; color:#fca5a5; margin:-6px 0 10px; }

/* password field */
.pw-wrap { position:relative; }
.pw-wrap input { width:100%; padding-right:40px; }
.pw-toggle { position:absolute; right:10px; top:50%; transform:translateY(-50%); background:none; border:none; cursor:pointer; font-size:16px; line-height:1; }

/* buttons */
.btn-primary { display:flex; align-items:center; justify-content:center; gap:8px; padding:12px 24px; background:linear-gradient(135deg,#ffd700,#ffed4e); color:#0f172a; border:none; border-radius:9px; font-size:14px; font-weight:900; cursor:pointer; transition:all .2s; width:100%; box-shadow:0 4px 16px rgba(255,215,0,.25); }
.btn-primary:hover:not(:disabled) { transform:translateY(-2px); box-shadow:0 8px 24px rgba(255,215,0,.35); }
.btn-primary:disabled { opacity:.45; cursor:not-allowed; box-shadow:none; }
.btn-spin { width:18px; height:18px; border:3px solid rgba(15,23,42,.3); border-top-color:#0f172a; border-radius:50%; animation:spin .7s linear infinite; }
@keyframes spin { to { transform:rotate(360deg); } }

@media (max-width:600px) {
  .form-grid { grid-template-columns:1fr; }
  .hero-card { flex-direction:column; align-items:center; text-align:center; }
  .stats-row { justify-content:center; }
  .quick-nav { flex-direction:column; }
}
</style>

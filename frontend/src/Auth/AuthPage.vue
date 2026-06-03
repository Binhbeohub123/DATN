<template>
  <div class="page">
    <div class="auth-theme">
      <ThemeToggle />
    </div>
    <div class="auth-card">

      <!-- LEFT -->
      <div class="left">
        <div>
          <h1>🎬 PolyCinema</h1>
          <p class="sub">Đặt vé phim nhanh chóng và tiện lợi</p>
          <div class="feature-list">
            <div class="feature">🎟 Đặt vé online</div>
            <div class="feature">💺 Chọn ghế trực tiếp</div>
            <div class="feature">🕒 Lịch sử đặt vé</div>
            <div class="feature">🎁 Voucher ưu đãi</div>
          </div>
        </div>
      </div>

      <!-- RIGHT -->
      <div class="right">
        <div class="form-box">

          <h2>
            {{
              isRegister ? 'Tạo tài khoản' :
              isVerify ? 'Xác thực Email' :
              isForgot ? 'Quên mật khẩu' : 'Đăng nhập'
            }}
          </h2>

          <!-- ================= LOGIN ================= -->
          <div v-if="!isRegister && !isForgot && !isVerify">
            <div class="field-group">
              <input
                v-model="loginForm.email"
                placeholder="Email"
                type="email"
                :class="{ 'input-err': fieldErrors.loginEmail }"
                @input="fieldErrors.loginEmail = ''"
                @keyup.enter="login"
              />
              <span v-if="fieldErrors.loginEmail" class="field-err-msg">{{ fieldErrors.loginEmail }}</span>
            </div>
            <div class="field-group">
              <input
                v-model="loginForm.password"
                type="password"
                placeholder="Mật khẩu"
                :class="{ 'input-err': fieldErrors.loginPw }"
                @input="fieldErrors.loginPw = ''"
                @keyup.enter="login"
              />
              <span v-if="fieldErrors.loginPw" class="field-err-msg">{{ fieldErrors.loginPw }}</span>
            </div>

            <button @click="login" :disabled="isLoading.login">
              {{ isLoading.login ? 'Đang đăng nhập...' : 'Đăng nhập' }}
            </button>

            <div class="social">
              <button class="google" @click="loginGoogle" :disabled="isLoading.google">
                <span class="google-icon">🔐</span> Google
              </button>
              <button class="discord" @click="loginDiscord" :disabled="isLoading.discord">
                <span class="discord-icon">🎮</span> Discord
              </button>
            </div>

            <div class="bottom-link">
              <span @click="switchToForgot">Quên mật khẩu?</span>
            </div>
            <div class="bottom-link">
              Chưa có tài khoản?
              <span @click="switchToRegister">Đăng ký</span>
            </div>
          </div>

          <!-- ================= REGISTER ================= -->
          <div v-if="isRegister">
            <div class="field-group">
              <input
                v-model="registerForm.hoTen"
                placeholder="Họ và tên đầy đủ"
                :class="{ 'input-err': fieldErrors.hoTen }"
                @input="fieldErrors.hoTen = ''"
              />
              <span v-if="fieldErrors.hoTen" class="field-err-msg">{{ fieldErrors.hoTen }}</span>
            </div>
            <div class="field-group">
              <input
                v-model="registerForm.email"
                placeholder="Email"
                type="email"
                :class="{ 'input-err': fieldErrors.regEmail }"
                @input="fieldErrors.regEmail = ''"
              />
              <span v-if="fieldErrors.regEmail" class="field-err-msg">{{ fieldErrors.regEmail }}</span>
            </div>
            <div class="field-group">
              <input
                v-model="registerForm.soDienThoai"
                placeholder="Số điện thoại (0xxxxxxxxx)"
                :class="{ 'input-err': fieldErrors.phone }"
                @input="fieldErrors.phone = ''"
              />
              <span v-if="fieldErrors.phone" class="field-err-msg">{{ fieldErrors.phone }}</span>
            </div>
            <div class="field-group">
              <input
                v-model="registerForm.password"
                type="password"
                placeholder="Mật khẩu (tối thiểu 8 ký tự)"
                :class="{ 'input-err': fieldErrors.regPw }"
                @input="fieldErrors.regPw = ''"
              />
              <span v-if="fieldErrors.regPw" class="field-err-msg">{{ fieldErrors.regPw }}</span>
            </div>
            <div class="field-group">
              <input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="Xác nhận mật khẩu"
                :class="{ 'input-err': fieldErrors.confirmPw }"
                @input="fieldErrors.confirmPw = ''"
              />
              <span v-if="fieldErrors.confirmPw" class="field-err-msg">{{ fieldErrors.confirmPw }}</span>
            </div>

            <button @click="register" :disabled="isLoading.register">
              {{ isLoading.register ? 'Đang xử lý...' : 'Đăng ký' }}
            </button>

            <div class="bottom-link">
              Đã có tài khoản?
              <span @click="backToLogin">Đăng nhập</span>
            </div>
          </div>

          <!-- ================= VERIFY EMAIL ================= -->
          <div v-if="isVerify">
            <p class="info-text">
              Chúng tôi đã gửi mã OTP đến email:<br>
              <strong>{{ verifyForm.email }}</strong>
            </p>
            
            <input 
              v-model="verifyForm.otp" 
              placeholder="Nhập mã OTP 6 số" 
              maxlength="6"
              type="number"
            />
            
            <button @click="verifyOtp" :disabled="isLoading.verify">Xác thực Email</button>
            <button class="secondary" @click="resendOtp" :disabled="isLoading.resend">
              {{ isLoading.resend ? 'Đang gửi...' : 'Gửi lại OTP' }}
            </button>

            <div class="bottom-link">
              <span @click="backToLogin">Quay lại đăng nhập</span>
            </div>
          </div>

          <!-- ================= FORGOT PASSWORD ================= -->
          <div v-if="isForgot">
            <!-- STEP 1: email input -->
            <div v-if="!forgotEmailSent">
              <div class="field-group">
                <input
                  v-model="forgotForm.email"
                  placeholder="Nhập email của bạn"
                  type="email"
                  :class="{ 'input-err': fieldErrors.forgotEmail }"
                  @input="fieldErrors.forgotEmail = ''"
                  @keyup.enter="sendOtp"
                />
                <span v-if="fieldErrors.forgotEmail" class="field-err-msg">{{ fieldErrors.forgotEmail }}</span>
              </div>
              <button @click="sendOtp" :disabled="isLoading.forgot">
                {{ isLoading.forgot ? 'Đang gửi...' : 'Gửi OTP đặt lại mật khẩu' }}
              </button>
            </div>
            <!-- STEP 2: "Check your email" + OTP reset form -->
            <div v-else>
              <div class="check-email-box">
                <div class="check-email-icon">📧</div>
                <p class="check-email-title">Kiểm tra email của bạn</p>
                <p class="check-email-sub">
                  Chúng tôi đã gửi mã OTP đến<br>
                  <strong>{{ forgotForm.email }}</strong>
                </p>
              </div>
              <input v-model="forgotForm.otp" placeholder="Nhập mã OTP 6 số" maxlength="6" type="text" inputmode="numeric" />
              <input v-model="forgotForm.newPassword" type="password" placeholder="Mật khẩu mới (tối thiểu 8 ký tự)" />
              <button @click="resetPassword" :disabled="isLoading.reset">
                {{ isLoading.reset ? 'Đang đổi...' : 'Đặt lại mật khẩu' }}
              </button>
              <button class="secondary" @click="forgotEmailSent = false; forgotForm.otp = ''">
                ← Dùng email khác
              </button>
            </div>

            <div class="bottom-link">
              <span @click="backToLogin">Quay lại đăng nhập</span>
            </div>
          </div>

          <!-- ================= MESSAGE ================= -->
          <transition name="slide">
            <div v-if="error" class="msg error">
              <span class="icon">⚠️</span>
              <span class="text">{{ error }}</span>
            </div>
          </transition>
          <transition name="slide">
            <div v-if="success" class="msg success">
              <span class="icon">✓</span>
              <span class="text">{{ success }}</span>
            </div>
          </transition>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import ThemeToggle from '@/components/ThemeToggle.vue'

const router = useRouter()
const route  = useRoute()
const authStore = useAuthStore()
const API = 'http://localhost:8080/api/auth'

// ================= STATE =================
const isRegister = ref(false)
const isForgot   = ref(false)
const isVerify   = ref(false)
const forgotEmailSent = ref(false)   // replaces otpSent

const error   = ref('')
const success = ref('')

// Inline field-level error map
const fieldErrors = reactive({
  loginEmail: '', loginPw: '',
  hoTen: '', regEmail: '', phone: '', regPw: '', confirmPw: '',
  forgotEmail: '',
})

const isLoading = ref({
  login: false, register: false, verify: false, resend: false,
  forgot: false, reset: false, google: false, discord: false,
})

// ================= FORM =================
const loginForm = ref({
  email: '',
  password: ''
})

const registerForm = ref({
  hoTen: '',
  email: '',
  soDienThoai: '',
  password: '',
  confirmPassword: '',
})

const forgotForm = ref({
  email: '',
  otp: '',
  newPassword: ''
})

const verifyForm = ref({
  email: '',
  otp: ''
})

async function getMessage(res) {
  const text = await res.text()

  try {
    const data = JSON.parse(text)
    return data.message || text
  } catch {
    return text
  }
}

// ================= VALIDATION FUNCTIONS =================
function isValidEmail(email) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

function isValidPhone(phone) {
  // Định dạng: 10 chữ số bắt đầu từ 0, hoặc +84
  const regex = /^(0\d{9}|\+84\d{9})$/
  return regex.test(phone.replace(/\s/g, ''))
}

function hasNoNumbers(text) {
  return !/\d/.test(text)
}

function clearMsg() {
  error.value = ''
  success.value = ''
}

function clearFieldErrors() {
  Object.keys(fieldErrors).forEach(k => { fieldErrors[k] = '' })
}

function backToLogin() {
  isRegister.value = false
  isForgot.value   = false
  isVerify.value   = false
  forgotEmailSent.value = false
  clearMsg()
  clearFieldErrors()

  // Reset forms
  loginForm.value    = { email: '', password: '' }
  registerForm.value = { hoTen: '', email: '', soDienThoai: '', password: '', confirmPassword: '' }
  forgotForm.value   = { email: '', otp: '', newPassword: '' }
  verifyForm.value   = { email: '', otp: '' }
}

function switchToRegister() {
  backToLogin()
  isRegister.value = true
}

function switchToForgot() {
  backToLogin()
  isForgot.value = true
}

// ================= LOGIN =================
async function login() {
  clearMsg()
  clearFieldErrors()

  // Inline field validation
  let hasErr = false
  if (!loginForm.value.email.trim()) { fieldErrors.loginEmail = 'Vui lòng nhập email'; hasErr = true }
  else if (!isValidEmail(loginForm.value.email)) { fieldErrors.loginEmail = 'Email không đúng định dạng'; hasErr = true }
  if (!loginForm.value.password.trim()) { fieldErrors.loginPw = 'Vui lòng nhập mật khẩu'; hasErr = true }
  if (hasErr) return

  isLoading.value.login = true
  try {
    const email = loginForm.value.email.trim().toLowerCase()
    const ok = await authStore.login(email, loginForm.value.password)

    if (!ok) {
      const loginError = String(authStore.error || '')
      if (loginError.includes('Email chưa xác thực')) {
        verifyForm.value.email = email
        isVerify.value = true
        error.value = 'Email của bạn chưa được xác thực. Vui lòng xác thực ngay.'
      } else {
        error.value = loginError || 'Sai email hoặc mật khẩu'
      }
      return
    }

    success.value = 'Đăng nhập thành công!'

    setTimeout(() => {
      // Honour the intended destination saved before auth redirect
      const intended = authStore.popRedirectPath()
      if (intended && intended !== '/auth') {
        router.push(intended)
      } else if (route.query?.redirect) {
        router.push(route.query.redirect)
      } else {
        router.push(authStore.userRole === 'ADMIN' ? '/admin' : '/')
      }
    }, 600)

  } catch {
    error.value = 'Không kết nối được server'
  } finally {
    isLoading.value.login = false
  }
}

// ================= REGISTER =================
async function register() {
  clearMsg()
  clearFieldErrors()

  // Inline field validation — collect all errors at once
  let hasErr = false

  if (!registerForm.value.hoTen.trim()) {
    fieldErrors.hoTen = 'Vui lòng nhập họ tên'; hasErr = true
  } else if (!hasNoNumbers(registerForm.value.hoTen)) {
    fieldErrors.hoTen = 'Họ tên không được chứa số'; hasErr = true
  }

  if (!registerForm.value.email.trim()) {
    fieldErrors.regEmail = 'Vui lòng nhập email'; hasErr = true
  } else if (!isValidEmail(registerForm.value.email)) {
    fieldErrors.regEmail = 'Email không đúng định dạng'; hasErr = true
  }

  if (!registerForm.value.soDienThoai.trim()) {
    fieldErrors.phone = 'Vui lòng nhập số điện thoại'; hasErr = true
  } else if (!isValidPhone(registerForm.value.soDienThoai)) {
    fieldErrors.phone = 'Số điện thoại không đúng định dạng (0xxxxxxxxx)'; hasErr = true
  }

  if (!registerForm.value.password.trim()) {
    fieldErrors.regPw = 'Vui lòng nhập mật khẩu'; hasErr = true
  } else if (registerForm.value.password.length < 8) {
    fieldErrors.regPw = 'Mật khẩu phải tối thiểu 8 ký tự'; hasErr = true
  }

  if (!registerForm.value.confirmPassword.trim()) {
    fieldErrors.confirmPw = 'Vui lòng xác nhận mật khẩu'; hasErr = true
  } else if (registerForm.value.confirmPassword !== registerForm.value.password) {
    fieldErrors.confirmPw = 'Mật khẩu xác nhận không khớp'; hasErr = true
  }

  if (hasErr) return

  isLoading.value.register = true
  try {
    const payload = {
      hoTen: registerForm.value.hoTen.trim(),
      email: registerForm.value.email.trim().toLowerCase(),
      soDienThoai: registerForm.value.soDienThoai.trim(),
      password: registerForm.value.password,
    }
    const res = await fetch(`${API}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    const text = await getMessage(res)

    if (!res.ok) {
      error.value = text || 'Đăng ký thất bại'
      return
    }

    success.value = text || 'Đăng ký thành công. Vui lòng xác thực email'
    verifyForm.value.email = registerForm.value.email.trim().toLowerCase()
    isRegister.value = false
    isVerify.value = true

  } catch {
    error.value = 'Không kết nối được server'
  } finally {
    isLoading.value.register = false
  }
}

// ================= VERIFY EMAIL =================
async function verifyOtp() {
  clearMsg()
  verifyForm.value.email = verifyForm.value.email.trim().toLowerCase()
  verifyForm.value.otp = String(verifyForm.value.otp).trim()

  if (!verifyForm.value.email) {
    error.value = 'Không tìm thấy email cần xác thực. Vui lòng đăng nhập hoặc đăng ký lại để nhận OTP.'
    return
  }

  if (!verifyForm.value.otp) {
    error.value = 'OTP không được để trống'
    return
  }

  if (verifyForm.value.otp.length !== 6) {
    error.value = 'OTP phải có 6 chữ số'
    return
  }

  isLoading.value.verify = true

  try {
    const res = await fetch(`${API}/verify`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: verifyForm.value.email,
        otp: verifyForm.value.otp
      })
    })

    const text = await getMessage(res)

    if (!res.ok) {
      error.value = text || 'Xác thực thất bại'
      if (text === 'OTP hết hạn' || text === 'Không tìm thấy OTP') {
        verifyForm.value.otp = ''
      }
      return
    }

    success.value = 'Xác thực email thành công!'
    
    setTimeout(() => {
      backToLogin()
    }, 1800)

  } catch {
    error.value = 'Không kết nối được server'
  } finally {
    isLoading.value.verify = false
  }
}

// ================= RESEND OTP =================
async function resendOtp() {
  clearMsg()
  verifyForm.value.email = verifyForm.value.email.trim().toLowerCase()

  if (!verifyForm.value.email) {
    error.value = 'Không tìm thấy email cần xác thực. Vui lòng đăng nhập hoặc đăng ký lại để nhận OTP.'
    return
  }

  isLoading.value.resend = true

  try {
    const res = await fetch(`${API}/resend-verify`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: verifyForm.value.email
      })
    })

    const text = await getMessage(res)

    if (!res.ok) {
      error.value = text || 'Không gửi được mã OTP, vui lòng thử lại'
    } else {
      verifyForm.value.otp = ''
      success.value = text || 'OTP đã gửi'
    }

  } catch {
    error.value = 'Không kết nối được server'
  } finally {
    isLoading.value.resend = false
  }
}

// ================= FORGOT PASSWORD =================
async function sendOtp() {
  clearMsg()
  clearFieldErrors()
  forgotForm.value.email = forgotForm.value.email.trim().toLowerCase()

  if (!forgotForm.value.email) {
    fieldErrors.forgotEmail = 'Vui lòng nhập email'; return
  }
  if (!isValidEmail(forgotForm.value.email)) {
    fieldErrors.forgotEmail = 'Email không đúng định dạng'; return
  }

  isLoading.value.forgot = true
  try {
    const res = await fetch(`${API}/forgot`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: forgotForm.value.email }),
    })
    const text = await getMessage(res)
    if (!res.ok) {
      error.value = text || 'Không gửi được mã OTP, vui lòng thử lại'
      return
    }
    // Show "Check your email" step
    forgotEmailSent.value = true
    success.value = ''
  } catch {
    error.value = 'Không kết nối được server'
  } finally {
    isLoading.value.forgot = false
  }
}

async function resetPassword() {
  clearMsg()
  forgotForm.value.otp = String(forgotForm.value.otp).trim()

  if (!forgotForm.value.otp) { error.value = 'OTP không được để trống'; return }
  if (forgotForm.value.otp.length !== 6) { error.value = 'OTP phải có 6 chữ số'; return }
  if (!forgotForm.value.newPassword.trim()) { error.value = 'Mật khẩu mới không được để trống'; return }
  if (forgotForm.value.newPassword.length < 8) { error.value = 'Mật khẩu mới phải tối thiểu 8 ký tự'; return }

  isLoading.value.reset = true
  try {
    const res = await fetch(`${API}/reset`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: forgotForm.value.email,
        otp: forgotForm.value.otp,
        newPassword: forgotForm.value.newPassword,
      }),
    })
    const text = await getMessage(res)
    if (!res.ok) {
      error.value = text || 'Đổi mật khẩu thất bại'
      if (text === 'OTP hết hạn' || text === 'Không tìm thấy OTP') forgotForm.value.otp = ''
      return
    }
    success.value = 'Đổi mật khẩu thành công! Vui lòng đăng nhập.'
    setTimeout(() => backToLogin(), 1500)
  } catch {
    error.value = 'Không kết nối được server'
  } finally {
    isLoading.value.reset = false
  }
}

// ================= SOCIAL LOGIN =================
function loginGoogle() {
  clearMsg()
  isLoading.value.google = true
  window.location.href = 'http://localhost:8080/oauth2/authorization/google'
}

function loginDiscord() {
  clearMsg()
  isLoading.value.discord = true
  window.location.href = 'http://localhost:8080/oauth2/authorization/discord'
}
</script>

<style scoped>
* { box-sizing: border-box; }
.page { position: relative; min-height: 100vh; display: flex; justify-content: center; align-items: center; background: var(--page-bg); padding: 20px; }
.auth-theme { position: fixed; top: 16px; right: 16px; z-index: 20; }
.auth-card { width: 1000px; min-height: 650px; background: #fff; border: 1px solid #efefef; display: flex; overflow: hidden; }
.left { width: 42%; color: #29bcea; padding: 40px; background: #f7f7f7; border-right: 1px solid #efefef; display: flex; align-items: center; }
.left h1 { font-size: 40px; margin-bottom: 10px; color: #29bcea; font-weight: 700; }
.sub { color: #7f7e7f; margin-bottom: 24px; font-size: 14.4px; line-height: 1.5; }
.feature-list { display: flex; flex-direction: column; gap: 12px; }
.feature { background: #ffffff; padding: 12px; border: 1px solid #efefef; font-size: 14.4px; color: #7f7e7f; }
.right { flex: 1; display: flex; justify-content: center; align-items: center; padding: 40px; background: #ffffff; }
.form-box { width: 100%; max-width: 420px; }
h2 { font-size: 32px; margin-bottom: 24px; color: #29bcea; font-weight: 700; text-align: center; }
input { width: 100%; min-height: 44px; padding: 12px; margin-bottom: 12px; border: 1px solid #efefef; font-size: 14.4px; font-family: inherit; color: #000; background: #fff; }
input:focus { outline: none; border-color: #29bcea; box-shadow: 0 0 0 2px rgba(41,188,234,.15); }
input::placeholder { color: #767676; }
.field-group { position: relative; margin-bottom: 4px; }
.field-group input { margin-bottom: 0; }
.input-err { border-color: #c63b3b !important; background: #fff9f9 !important; }
.field-err-msg { display: block; font-size: 12px; color: #c63b3b; font-weight: 700; margin: 4px 0 8px 2px; line-height: 1.4; }
.check-email-box { text-align: center; padding: 16px 12px; background: #f7f7f7; border: 1px solid #efefef; margin-bottom: 16px; }
.check-email-icon { font-size: 34px; margin-bottom: 8px; }
.check-email-title { font-size: 16.8px; font-weight: 700; color: #29bcea; margin: 0 0 6px; }
.check-email-sub { font-size: 14.4px; color: #7f7e7f; margin: 0; line-height: 1.5; }
button { width: 100%; min-height: 44px; padding: 12px; border: none; background: #29bcea; color: #fff; font-weight: 700; font-size: 14.4px; cursor: pointer; margin-bottom: 10px; transition: background-color .2s ease; }
button:hover:not(:disabled) { background: #1a9fbd; }
button:disabled { opacity: .7; cursor: not-allowed; }
.secondary { background: transparent; color: #29bcea; border: 1px solid #29bcea; }
.secondary:hover:not(:disabled) { background: #29bcea; color: #fff; }
.social { display: flex; gap: 10px; margin-top: 12px; }
.google,.discord { background: #ffffff; color: #29bcea; border: 1px solid #29bcea; display: flex; align-items: center; justify-content: center; gap: 6px; }
.google:hover:not(:disabled),.discord:hover:not(:disabled) { background: #29bcea; color: #fff; }
.google-icon,.discord-icon { font-size: 16px; }
.bottom-link { margin-top: 12px; text-align: center; font-size: 14.4px; color: #7f7e7f; }
.bottom-link span { color: #29bcea; cursor: pointer; font-weight: 700; }
.bottom-link span:hover { text-decoration: underline; }
.info-text { text-align: center; margin-bottom: 15px; color: #7f7e7f; font-size: 14.4px; line-height: 1.5; }
.msg { margin-top: 14px; padding: 12px; text-align: left; display: flex; align-items: center; gap: 10px; font-size: 14.4px; }
.msg .icon { font-size: 16px; flex-shrink: 0; }
.msg .text { flex: 1; word-break: break-word; }
.error { background: #fff5f5; color: #8f2a2a; border: 1px solid #f0d4d4; }
.success { background: #f0fcf7; color: #177245; border: 1px solid #d2efdf; }
.slide-enter-active, .slide-leave-active { transition: all .2s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-6px); }
@media(max-width:900px){
  .auth-card { flex-direction: column; width: 100%; min-height: auto; }
  .left { width: 100%; padding: 28px; }
  .right { padding: 28px 20px; }
}
@media(max-width:640px){
  .page { padding: 10px; }
  .left h1 { font-size: 28px; }
  h2 { font-size: 24px; }
  .feature { font-size: 13px; }
}
</style>
<template>
  <div class="page">
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
    const res = await fetch(`${API}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: loginForm.value.email.trim(), password: loginForm.value.password }),
    })

    const text = await getMessage(res)

    if (!res.ok) {
      if (text === 'Email chưa xác thực') {
        verifyForm.value.email = loginForm.value.email.trim().toLowerCase()
        isVerify.value = true
        error.value = 'Email của bạn chưa được xác thực. Vui lòng xác thực ngay.'
      } else {
        error.value = text || 'Sai email hoặc mật khẩu'
      }
      return
    }

    // Backend returns raw JWT string
    authStore.setToken(text)
    await authStore.fetchProfile()
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

.page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0f172a, #1e293b);
  padding: 20px;
}

.auth-card {
  width: 1000px;
  min-height: 650px;
  background: #fff;
  border-radius: 25px;
  display: flex;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0,0,0,.3);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.auth-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 30px 60px rgba(0,0,0,.4);
}

.left {
  width: 42%;
  color: #fff;
  padding: 50px;
  background: linear-gradient(180deg, #ff7300, #ff4d00);
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.left::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 500px;
  height: 500px;
  background: rgba(255,255,255,.1);
  border-radius: 50%;
}

.left > div {
  position: relative;
  z-index: 1;
}

.left h1 { 
  font-size: 42px; 
  margin-bottom: 10px;
  text-shadow: 0 2px 4px rgba(0,0,0,.1);
}

.sub { 
  opacity: .95; 
  margin-bottom: 30px;
  font-size: 16px;
}

.feature-list { 
  display: flex; 
  flex-direction: column; 
  gap: 14px; 
}

.feature {
  background: rgba(255,255,255,.15);
  padding: 14px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,.2);
  transition: all 0.3s ease;
  font-size: 15px;
}

.feature:hover {
  background: rgba(255,255,255,.25);
  transform: translateX(5px);
}

.right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  background: #f9fafb;
}

.form-box { 
  width: 100%; 
  max-width: 400px; 
}

h2 { 
  font-size: 32px; 
  margin-bottom: 25px;
  color: #1f2937;
  font-weight: 700;
}

input {
  width: 100%;
  padding: 12px 14px;
  margin-bottom: 12px;
  border-radius: 10px;
  border: 2px solid #e5e7eb;
  font-size: 14px;
  transition: all 0.3s ease;
  font-family: inherit;
}

input:focus {
  outline: none;
  border-color: #ff7300;
  box-shadow: 0 0 0 3px rgba(255, 115, 0, 0.1);
  background: #fffbf7;
}

input::placeholder {
  color: #9ca3af;
}

/* ── Inline field error styles ── */
.field-group {
  position: relative;
  margin-bottom: 4px;
}
.field-group input {
  margin-bottom: 0;
}
.input-err {
  border-color: #ef4444 !important;
  background: #fef2f2 !important;
}
.input-err:focus {
  border-color: #ef4444 !important;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1) !important;
}
.field-err-msg {
  display: block;
  font-size: 11.5px;
  color: #dc2626;
  font-weight: 600;
  margin: 3px 0 8px 2px;
  line-height: 1.3;
}

/* ── Check-your-email box ── */
.check-email-box {
  text-align: center;
  padding: 20px 12px 16px;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border: 1px solid #93c5fd;
  border-radius: 12px;
  margin-bottom: 16px;
}
.check-email-icon {
  font-size: 42px;
  margin-bottom: 8px;
}
.check-email-title {
  font-size: 16px;
  font-weight: 800;
  color: #1e40af;
  margin: 0 0 6px;
}
.check-email-sub {
  font-size: 13px;
  color: #3b82f6;
  margin: 0;
  line-height: 1.5;
}

button {
  width: 100%;
  padding: 12px 14px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff7300, #ff6b00);
  color: white;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  margin-bottom: 10px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 115, 0, 0.3);
}

button:hover:not(:disabled) { 
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 115, 0, 0.4);
}

button:active:not(:disabled) {
  transform: translateY(0);
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.secondary {
  background: linear-gradient(135deg, #6b7280, #4b5563);
  box-shadow: 0 4px 12px rgba(107, 114, 128, 0.3);
}

.secondary:hover:not(:disabled) {
  box-shadow: 0 6px 16px rgba(107, 114, 128, 0.4);
}

.social {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.google {
  background: linear-gradient(135deg, #ea4335, #d33c27);
  box-shadow: 0 4px 12px rgba(234, 67, 53, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.google:hover:not(:disabled) {
  box-shadow: 0 6px 16px rgba(234, 67, 53, 0.4);
}

.google-icon {
  font-size: 16px;
}

.discord {
  background: linear-gradient(135deg, #5865f2, #404eed);
  box-shadow: 0 4px 12px rgba(88, 101, 242, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.discord:hover:not(:disabled) {
  box-shadow: 0 6px 16px rgba(88, 101, 242, 0.4);
}

.discord-icon {
  font-size: 16px;
}

.bottom-link {
  margin-top: 15px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.bottom-link span {
  color: #ff7300;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.bottom-link span:hover {
  color: #ff6b00;
  text-decoration: underline;
}

.info-text {
  text-align: center;
  margin-bottom: 15px;
  color: #555;
  font-size: 14px;
  line-height: 1.6;
}

/* ================= MESSAGE STYLES ================= */
.msg {
  margin-top: 15px;
  padding: 12px 14px;
  border-radius: 10px;
  text-align: left;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 500;
  animation: slideIn 0.3s ease-out;
}

.msg .icon {
  font-size: 18px;
  flex-shrink: 0;
}

.msg .text {
  flex: 1;
  word-break: break-word;
}

.error {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  color: #991b1b;
  border: 1px solid #fca5a5;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.1);
}

.success {
  background: linear-gradient(135deg, #dcfce7, #bbf7d0);
  color: #166534;
  border: 1px solid #86efac;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.1);
}

/* ================= ANIMATIONS ================= */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-enter-active, .slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ================= RESPONSIVE ================= */
@media(max-width:1024px){
  .auth-card {
    width: 95%;
    min-height: 600px;
  }
  
  h2 {
    font-size: 28px;
  }
  
  .left h1 {
    font-size: 36px;
  }
}

@media(max-width:900px){
  .auth-card { 
    flex-direction: column;
    min-height: auto;
  }
  
  .left { 
    width: 100%; 
    padding: 30px;
    min-height: 200px;
  }
  
  .right {
    padding: 30px 20px;
  }
  
  .left h1 {
    font-size: 32px;
    margin-bottom: 8px;
  }
  
  .sub {
    font-size: 14px;
  }
  
  .feature-list {
    gap: 10px;
  }
  
  .feature {
    padding: 10px 12px;
    font-size: 13px;
  }
}

@media(max-width:640px){
  .page {
    padding: 10px;
  }
  
  .auth-card {
    border-radius: 15px;
    width: 100%;
  }
  
  .left {
    padding: 20px;
    min-height: 150px;
  }
  
  .left h1 {
    font-size: 24px;
    margin-bottom: 5px;
  }
  
  .sub {
    font-size: 12px;
    margin-bottom: 15px;
  }
  
  .feature-list {
    gap: 8px;
  }
  
  .feature {
    padding: 8px 10px;
    font-size: 12px;
  }
  
  .right {
    padding: 20px 15px;
  }
  
  .form-box {
    max-width: 100%;
  }
  
  h2 {
    font-size: 22px;
    margin-bottom: 15px;
  }
  
  input {
    padding: 10px 12px;
    font-size: 13px;
  }
  
  button {
    padding: 10px 12px;
    font-size: 14px;
  }
  
  .social {
    gap: 8px;
    margin-top: 10px;
  }
  
  .msg {
    padding: 10px 12px;
    font-size: 12px;
    gap: 8px;
  }
  
  .msg .icon {
    font-size: 16px;
  }
}

@media(max-width:480px){
  .left h1 {
    font-size: 20px;
  }
  
  h2 {
    font-size: 20px;
  }
  
  button {
    padding: 9px 10px;
    font-size: 13px;
  }
}
</style>
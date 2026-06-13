<template>
  <div class="page">
    <div class="auth-theme">
      <ThemeToggle />
    </div>
    <div class="auth-card glass-card--heavy">

      <!-- LEFT -->
      <div class="left">
        <div class="left-inner">
          <h1 class="brand-title">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z"/></svg>
            PolyCinema
          </h1>
          <p class="sub">Đặt vé phim nhanh chóng và tiện lợi</p>
          <div class="feature-list">
            <span class="chip-glass"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v2z"/></svg> Đặt vé online</span>
            <span class="chip-glass"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg> Chọn ghế trực tiếp</span>
            <span class="chip-glass"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> Lịch sử đặt vé</span>
            <span class="chip-glass"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="none"><polygon fill="currentColor" points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg> Voucher ưu đãi</span>
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
              <div class="input-wrap">
                <input
                  v-model="loginForm.email"
                  placeholder=" "
                  type="email"
                  :class="{ 'input-err': fieldErrors.loginEmail }"
                  @input="fieldErrors.loginEmail = ''"
                  @keyup.enter="login"
                />
                <label>Email</label>
              </div>
              <span v-if="fieldErrors.loginEmail" class="field-err-msg">{{ fieldErrors.loginEmail }}</span>
            </div>
            <div class="field-group">
              <div class="input-wrap">
                <input
                  v-model="loginForm.password"
                  type="password"
                  placeholder=" "
                  :class="{ 'input-err': fieldErrors.loginPw }"
                  @input="fieldErrors.loginPw = ''"
                  @keyup.enter="login"
                />
                <label>Mật khẩu</label>
              </div>
              <span v-if="fieldErrors.loginPw" class="field-err-msg">{{ fieldErrors.loginPw }}</span>
            </div>

            <button class="btn-bib" @click="login" :disabled="isLoading.login">
              {{ isLoading.login ? 'Đang đăng nhập...' : 'Đăng nhập' }}
            </button>

            <div class="social">
              <button class="btn-social google" @click="loginGoogle" :disabled="isLoading.google">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Google
              </button>
              <button class="btn-social discord" @click="loginDiscord" :disabled="isLoading.discord">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057c.002.022.015.043.033.054a19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028c.462-.63.874-1.295 1.226-1.994a.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03z"/>
                </svg>
                Discord
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
              <div class="input-wrap">
                <input
                  v-model="registerForm.hoTen"
                  placeholder=" "
                  :class="{ 'input-err': fieldErrors.hoTen }"
                  @input="fieldErrors.hoTen = ''"
                />
                <label>Họ và tên đầy đủ</label>
              </div>
              <span v-if="fieldErrors.hoTen" class="field-err-msg">{{ fieldErrors.hoTen }}</span>
            </div>
            <div class="field-group">
              <div class="input-wrap">
                <input
                  v-model="registerForm.email"
                  placeholder=" "
                  type="email"
                  :class="{ 'input-err': fieldErrors.regEmail }"
                  @input="fieldErrors.regEmail = ''"
                />
                <label>Email</label>
              </div>
              <span v-if="fieldErrors.regEmail" class="field-err-msg">{{ fieldErrors.regEmail }}</span>
            </div>
            <div class="field-group">
              <div class="input-wrap">
                <input
                  v-model="registerForm.soDienThoai"
                  placeholder=" "
                  :class="{ 'input-err': fieldErrors.phone }"
                  @input="fieldErrors.phone = ''"
                />
                <label>Số điện thoại (0xxxxxxxxx)</label>
              </div>
              <span v-if="fieldErrors.phone" class="field-err-msg">{{ fieldErrors.phone }}</span>
            </div>
            <div class="field-group">
              <div class="input-wrap">
                <input
                  v-model="registerForm.password"
                  type="password"
                  placeholder=" "
                  :class="{ 'input-err': fieldErrors.regPw }"
                  @input="fieldErrors.regPw = ''"
                />
                <label>Mật khẩu (tối thiểu 8 ký tự)</label>
              </div>
              <span v-if="fieldErrors.regPw" class="field-err-msg">{{ fieldErrors.regPw }}</span>
            </div>
            <div class="field-group">
              <div class="input-wrap">
                <input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder=" "
                  :class="{ 'input-err': fieldErrors.confirmPw }"
                  @input="fieldErrors.confirmPw = ''"
                />
                <label>Xác nhận mật khẩu</label>
              </div>
              <span v-if="fieldErrors.confirmPw" class="field-err-msg">{{ fieldErrors.confirmPw }}</span>
            </div>

            <button class="btn-bib" @click="register" :disabled="isLoading.register">
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

            <div class="input-wrap">
              <input
                v-model="verifyForm.otp"
                placeholder=" "
                maxlength="6"
                type="number"
              />
              <label>Nhập mã OTP 6 số</label>
            </div>

            <button class="btn-bib" @click="verifyOtp" :disabled="isLoading.verify">Xác thực Email</button>
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
                <div class="input-wrap">
                  <input
                    v-model="forgotForm.email"
                    placeholder=" "
                    type="email"
                    :class="{ 'input-err': fieldErrors.forgotEmail }"
                    @input="fieldErrors.forgotEmail = ''"
                    @keyup.enter="sendOtp"
                  />
                  <label>Nhập email của bạn</label>
                </div>
                <span v-if="fieldErrors.forgotEmail" class="field-err-msg">{{ fieldErrors.forgotEmail }}</span>
              </div>
              <button class="btn-bib" @click="sendOtp" :disabled="isLoading.forgot">
                {{ isLoading.forgot ? 'Đang gửi...' : 'Gửi OTP đặt lại mật khẩu' }}
              </button>
            </div>
            <!-- STEP 2: "Check your email" + OTP reset form -->
            <div v-else>
              <div class="check-email-box glass-card">
                <div class="check-email-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="34" height="34">
                    <rect x="2" y="4" width="20" height="16" rx="2" />
                    <path d="m2 7 10 7 10-7" />
                  </svg>
                </div>
                <p class="check-email-title">Kiểm tra email của bạn</p>
                <p class="check-email-sub">
                  Chúng tôi đã gửi mã OTP đến<br>
                  <strong>{{ forgotForm.email }}</strong>
                </p>
              </div>
              <div class="input-wrap">
                <input v-model="forgotForm.otp" placeholder=" " maxlength="6" type="text" inputmode="numeric" />
                <label>Nhập mã OTP 6 số</label>
              </div>
              <div class="input-wrap">
                <input v-model="forgotForm.newPassword" type="password" placeholder=" " />
                <label>Mật khẩu mới (tối thiểu 8 ký tự)</label>
              </div>
              <button class="btn-bib" @click="resetPassword" :disabled="isLoading.reset">
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
            <div v-if="error" class="msg error glass-card">
              <span class="icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="16" height="16">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <circle cx="12" cy="16" r="0.5" fill="currentColor" />
                </svg>
              </span>
              <span class="text">{{ error }}</span>
            </div>
          </transition>
          <transition name="slide">
            <div v-if="success" class="msg success glass-card">
              <span class="icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" width="16" height="16">
                  <circle cx="12" cy="12" r="10" />
                  <path d="m8 12 3 3 5-5" />
                </svg>
              </span>
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
const API = (import.meta.env.VITE_API_BASE_URL || '/api') + '/auth'

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
  window.location.href = (import.meta.env.VITE_API_BASE_URL ? import.meta.env.VITE_API_BASE_URL.replace('/api', '') : '') + '/oauth2/authorization/google'
}

function loginDiscord() {
  clearMsg()
  isLoading.value.discord = true
  window.location.href = (import.meta.env.VITE_API_BASE_URL ? import.meta.env.VITE_API_BASE_URL.replace('/api', '') : '') + '/oauth2/authorization/discord'
}
</script>

<style scoped>
* { box-sizing: border-box; }

.page {
  position: relative;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--void, #050508);
  padding: 20px;
}

.auth-theme { position: fixed; top: 16px; right: 16px; z-index: 20; }

.auth-card {
  width: 1000px;
  min-height: 650px;
  display: flex;
  overflow: hidden;
  border-radius: var(--radius-md, 12px);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  box-shadow: var(--shadow-lg, 0 12px 40px rgba(0,0,0,0.55));
}

/* ── LEFT PANEL ── */
.left {
  position: relative;
  width: 42%;
  padding: 48px 40px;
  background: linear-gradient(135deg, var(--deep, #0a0a0f) 0%, var(--surface-1, #0f0f17) 100%);
  border-right: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  display: flex;
  align-items: center;
  overflow: hidden;
}

.left::before {
  content: '';
  position: absolute;
  top: 30%;
  left: 40%;
  width: 320px;
  height: 320px;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, var(--electric-glow, rgba(41,188,234,0.25)) 0%, transparent 70%);
  animation: glow-pulse 4s ease-in-out infinite alternate;
  pointer-events: none;
}

@keyframes glow-pulse {
  from { opacity: 0.4; transform: translate(-50%, -50%) scale(1); }
  to   { opacity: 0.8; transform: translate(-50%, -50%) scale(1.15); }
}

.brand-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  font-size: clamp(28px, 3vw, 40px);
  letter-spacing: -0.03em;
  color: var(--text-primary, #f1f5f9);
  margin-bottom: 12px;
  font-weight: 700;
}

.sub {
  color: var(--text-secondary, #94a3b8);
  margin-bottom: 28px;
  font-size: 14px;
  line-height: 1.6;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.left-inner {
  position: relative;
  z-index: 1;
  width: 100%;
}

.feature-list .chip-glass {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-pill, 999px);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary, #94a3b8);
  width: fit-content;
}

/* ── RIGHT PANEL ── */
.right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 48px 40px;
  background: var(--surface-1, #0f0f17);
}

.form-box {
  width: 100%;
  max-width: 420px;
}

h2 {
  font-size: clamp(22px, 3vw, 32px);
  margin-bottom: 28px;
  color: var(--text-primary, #f1f5f9);
  font-weight: 700;
  text-align: center;
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
}

/* ── Inputs ── */
input {
  width: 100%;
  min-height: 44px;
  padding: 12px 14px;
  margin-bottom: 12px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-sm, 6px);
  font-size: 14px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  color: var(--text-primary, #f1f5f9);
  background: var(--glass-bg, rgba(255,255,255,0.04));
  transition: border-color 0.3s var(--ease-out, cubic-bezier(0.4,0,0.2,1)),
              box-shadow 0.3s var(--ease-out, cubic-bezier(0.4,0,0.2,1));
}

input:focus {
  outline: none;
  border-color: var(--electric, #29bcea);
  box-shadow: 0 0 0 2px var(--electric-soft, rgba(41,188,234,0.08));
}

input::placeholder { color: var(--text-ghost, rgba(241,245,249,0.45)); }

.field-group { position: relative; margin-bottom: 4px; }
.field-group input { margin-bottom: 0; }
.input-err { border-color: #f87171 !important; }
.field-err-msg { display: block; font-size: 12px; color: #f87171; font-weight: 600; margin: 4px 0 8px 2px; line-height: 1.4; }

.check-email-box {
  text-align: center;
  padding: 20px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 12px);
  margin-bottom: 20px;
}
.check-email-icon { font-size: 34px; margin-bottom: 8px; }
.check-email-title { font-size: 16px; font-weight: 700; color: var(--electric, #29bcea); margin: 0 0 6px; }
.check-email-sub { font-size: 14px; color: var(--text-secondary, #94a3b8); margin: 0; line-height: 1.5; }

/* ── Buttons ── */
button {
  width: 100%;
  min-height: 44px;
  padding: 12px;
  border: none;
  background: var(--electric, #29bcea);
  color: var(--on-accent, #ffffff);
  font-weight: 700;
  font-size: 14px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  cursor: pointer;
  margin-bottom: 10px;
  border-radius: var(--radius-sm, 6px);
  transition: background 0.2s, transform 0.2s var(--spring, cubic-bezier(0.34,1.56,0.64,1));
}

button:hover:not(:disabled) { background: var(--electric-hover, #1a9fbd); transform: translateY(-1px); }
button:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-bib {
  outline: 1.5px solid rgba(41,188,234,0.45);
  outline-offset: 3px;
  will-change: transform;
}

.btn-bib:hover:not(:disabled) {
  box-shadow: 0 6px 20px var(--electric-glow, rgba(41,188,234,0.30));
  outline-offset: 5px;
}

.secondary {
  background: transparent;
  color: var(--electric, #29bcea);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.secondary:hover:not(:disabled) { background: var(--glass-bg, rgba(255,255,255,0.04)); transform: none; }

.social { display: flex; gap: 10px; margin-top: 12px; }
.google, .discord {
  background: transparent;
  color: var(--text-secondary, #94a3b8);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.google:hover:not(:disabled), .discord:hover:not(:disabled) {
  border-color: var(--electric, #29bcea);
  color: var(--electric, #29bcea);
  background: var(--glass-bg, rgba(255,255,255,0.04));
  transform: none;
}
.google-icon, .discord-icon { font-size: 16px; }

.bottom-link { margin-top: 12px; text-align: center; font-size: 14px; color: var(--text-secondary, #94a3b8); }
.bottom-link span { color: var(--electric, #29bcea); cursor: pointer; font-weight: 700; }
.bottom-link span:hover { text-decoration: underline; }

.info-text { text-align: center; margin-bottom: 15px; color: var(--text-secondary, #94a3b8); font-size: 14px; line-height: 1.5; }

.msg {
  margin-top: 14px;
  padding: 12px 14px;
  text-align: left;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-sm, 6px);
  backdrop-filter: blur(8px);
}
.msg .icon { font-size: 16px; flex-shrink: 0; }
.msg .text { flex: 1; word-break: break-word; }
.error { border-color: rgba(248,113,113,0.3); color: #f87171; }
.success { border-color: rgba(52,211,153,0.3); color: #34d399; }

.slide-enter-active, .slide-leave-active { transition: all 0.2s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-6px); }

@media (max-width: 900px) {
  .auth-card { flex-direction: column; width: 100%; min-height: auto; }
  .left { width: 100%; padding: 32px 24px; }
  .right { padding: 32px 24px; }
}

@media (max-width: 640px) {
  .page { padding: 10px; }
  .brand-title { font-size: 26px; }
  h2 { font-size: 22px; }
}
</style>

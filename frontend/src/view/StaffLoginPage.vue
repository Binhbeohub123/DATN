<template>
  <div class="staff-login-page">
    <div class="staff-login-card">
      <div class="staff-login-header">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
          stroke-linecap="round" stroke-linejoin="round">
          <path d="M7 4v16M17 4v16M3 8h4m10 0h4M3 16h4m10 0h4M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z" />
        </svg>
        <h1>PolyCinema</h1>
        <p class="subtitle">Đăng nhập nhân viên</p>
      </div>

      <form @submit.prevent="handleLogin" class="staff-login-form">
        <div class="field-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            placeholder="Nhập email nhân viên"
            :class="{ 'input-error': errorField === 'email' }"
            @input="clearError"
            autocomplete="username"
          />
        </div>

        <div class="field-group">
          <label for="password">Mật khẩu</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="Nhập mật khẩu"
            :class="{ 'input-error': errorField === 'password' }"
            @input="clearError"
            @keyup.enter="handleLogin"
            autocomplete="current-password"
          />
        </div>

        <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
        </button>
      </form>

      <div class="back-link">
        <router-link to="/">← Về trang chủ</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ email: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')
const errorField = ref('')

function clearError() {
  errorMessage.value = ''
  errorField.value = ''
}

async function handleLogin() {
  clearError()

  if (!form.value.email) {
    errorMessage.value = 'Email không được để trống'
    errorField.value = 'email'
    return
  }
  if (!form.value.password) {
    errorMessage.value = 'Mật khẩu không được để trống'
    errorField.value = 'password'
    return
  }

  loading.value = true
  try {
    const res = await api.post('/staff/login', {
      email: form.value.email,
      password: form.value.password
    })

    const token = res.data?.token
    if (!token) throw new Error('No token received')

    // Store token using existing authStore pattern
    authStore.setToken(token)
    await authStore.fetchProfile()

    // Redirect to staff dashboard (placeholder: /staff/dashboard)
    router.push('/staff/dashboard')
  } catch (err) {
    const msg = err.response?.data?.message || err.response?.data || 'Đăng nhập thất bại'
    errorMessage.value = typeof msg === 'string' ? msg : 'Đăng nhập thất bại'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.staff-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 1rem;
}

.staff-login-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 2.5rem;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.staff-login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.staff-login-header svg {
  color: #e94560;
  margin-bottom: 0.5rem;
}

.staff-login-header h1 {
  color: #fff;
  font-size: 1.75rem;
  margin: 0.25rem 0;
}

.staff-login-header .subtitle {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
  margin: 0;
}

.staff-login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-group label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.85rem;
  font-weight: 500;
}

.field-group input {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}

.field-group input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.field-group input:focus {
  border-color: #e94560;
}

.field-group input.input-error {
  border-color: #ff4757;
}

.error-msg {
  color: #ff4757;
  font-size: 0.85rem;
  margin: 0;
  text-align: center;
}

.btn-login {
  padding: 0.8rem;
  border: none;
  border-radius: 8px;
  background: #e94560;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, opacity 0.2s;
}

.btn-login:hover:not(:disabled) {
  background: #d63851;
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.back-link {
  text-align: center;
  margin-top: 1.5rem;
}

.back-link a {
  color: rgba(255, 255, 255, 0.5);
  text-decoration: none;
  font-size: 0.85rem;
  transition: color 0.2s;
}

.back-link a:hover {
  color: #e94560;
}
</style>
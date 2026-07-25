<template>
  <div class="not-found">
    <div class="content">
      <div class="code">404</div>
      <h1>Trang không tồn tại</h1>
      <p>Trang bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
      <div class="actions">
        <button class="btn-back" @click="router.back()">← Quay lại</button>
        <router-link to="/" class="btn-home">Về trang chủ</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7f7e7f;
  padding: 24px;
  font-family: 'Raleway', sans-serif;
}

.content {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.code {
  font-size: clamp(80px, 20vw, 160px);
  font-weight: 700;
  color: #29bcea;
  line-height: 1;
}

h1 {
  font-size: clamp(22px, 5vw, 36px);
  font-weight: 700;
  margin: 0;
  color: #000000;
}

p {
  font-size: 15px;
  color: #767676;
  margin: 0;
  max-width: 380px;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 8px;
}

.btn-back,
.btn-home {
  min-height: 44px;
  padding: 12px 28px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-back {
  background: transparent;
  border: 1px solid #29bcea;
  color: #29bcea;
}

.btn-back:hover {
  background: #29bcea;
  color: #ffffff;
}

.btn-home {
  background: #29bcea;
  color: #ffffff;
  border: none;
}

.btn-home:hover {
  background: #1a9fbd;
}
</style>

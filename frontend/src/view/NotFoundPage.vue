<template>
  <div class="not-found">
    <div class="content">
      <div class="code">404</div>
      <h1>Trang không tồn tại</h1>
      <p>Trang bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
      <div class="actions">
        <button class="btn-back" @click="router.back()">← Quay lại</button>
        <router-link to="/" class="btn-home">🏠 Về trang chủ</router-link>
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
  background: linear-gradient(160deg, #0b1120, #0f172a, #1a1f35);
  display: flex; align-items: center; justify-content: center;
  color: #f1f5f9; padding: 24px;
}
.content { text-align: center; display: flex; flex-direction: column; align-items: center; gap: 16px; }
.code { font-size: clamp(80px,20vw,160px); font-weight: 900; color: #ffd700; line-height: 1;
  text-shadow: 0 0 60px rgba(255,215,0,.3); }
h1 { font-size: clamp(22px,5vw,36px); font-weight: 900; margin: 0; }
p  { font-size: 15px; color: #64748b; margin: 0; max-width: 380px; }
.actions { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; margin-top: 8px; }
.btn-back, .btn-home {
  padding: 12px 28px; border-radius: 10px; font-size: 14px; font-weight: 800;
  cursor: pointer; transition: all .2s; text-decoration: none; display: inline-block;
}
.btn-back { background: rgba(255,255,255,.08); border: 1px solid rgba(255,255,255,.15); color: #f1f5f9; }
.btn-back:hover { border-color: #ffd700; color: #ffd700; }
.btn-home { background: linear-gradient(135deg,#ffd700,#ffed4e); color: #0f172a; border: none;
  box-shadow: 0 4px 16px rgba(255,215,0,.3); }
.btn-home:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(255,215,0,.4); }
</style>

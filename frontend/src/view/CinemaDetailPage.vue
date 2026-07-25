<template>
  <div class="cinema-detail-page">
    <!-- Back nav -->
    <nav class="cinema-detail-nav">
      <button class="back-btn" @click="router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="15 18 9 12 15 6"/></svg>
        Quay lại
      </button>
    </nav>

    <!-- Loading / Error -->
    <div v-if="loading" class="cdp-state">
      <div class="cdp-spinner"></div>
      <p>Đang tải thông tin rạp...</p>
    </div>
    <div v-else-if="error" class="cdp-state cdp-state--error">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <p>{{ error }}</p>
      <button class="btn-back-home" @click="router.push('/')">Về trang chủ</button>
    </div>

    <!-- Cinema detail content -->
    <div v-else-if="cinema" class="cdp-content">
      <!-- Hero image -->
      <div class="cdp-hero">
        <img
          v-if="cinema.hinhAnh"
          :src="cinema.hinhAnh"
          :alt="cinema.tenRap"
          class="cdp-hero-img"
          @error="e => e.target.style.display = 'none'"
        />
        <div v-else class="cdp-hero-placeholder">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="7" width="20" height="15" rx="2"/><path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/><line x1="12" y1="12" x2="12" y2="16"/><line x1="10" y1="14" x2="14" y2="14"/></svg>
        </div>
        <div class="cdp-hero-overlay">
          <div class="cdp-hero-badge" v-if="cinema.thanhPho">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            {{ cinema.thanhPho }}
          </div>
        </div>
      </div>

      <!-- Info card -->
      <div class="cdp-card">
        <div class="cdp-card-header">
          <h1 class="cdp-name">{{ cinema.tenRap }}</h1>
          <span :class="['cdp-status', cinema.trangThai ? 'cdp-status--active' : 'cdp-status--inactive']">
            {{ cinema.trangThai ? 'Đang hoạt động' : 'Dừng hoạt động' }}
          </span>
        </div>

        <div class="cdp-info-list">
          <div v-if="cinema.diaChi" class="cdp-info-row">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            <span>{{ cinema.diaChi }}</span>
          </div>
          <div v-if="cinema.thanhPho" class="cdp-info-row">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
            <span>{{ cinema.thanhPho }}</span>
          </div>
          <div v-if="cinema.soDienThoai" class="cdp-info-row">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 13a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
            <span>{{ cinema.soDienThoai }}</span>
          </div>
        </div>

        <!-- Map button -->
        <a
          v-if="hasCoords"
          :href="`https://maps.google.com/?q=${cinema.latitude},${cinema.longitude}`"
          class="cdp-map-btn"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="`Xem bản đồ rạp ${cinema.tenRap} trên Google Maps`"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polygon points="3 11 22 2 13 21 11 13 3 11"/></svg>
          Xem bản đồ trên Google Maps
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'

const router = useRouter()
const route  = useRoute()

const cinema = ref(null)
const loading = ref(true)
const error   = ref('')

const hasCoords = computed(() =>
  cinema.value &&
  cinema.value.latitude  != null &&
  cinema.value.longitude != null
)

onMounted(async () => {
  try {
    const res = await api.get(`/rap-chieu/${route.params.id}`)
    cinema.value = res.data
  } catch (e) {
    error.value = e.response?.status === 404
      ? 'Không tìm thấy rạp chiếu này.'
      : 'Không thể tải thông tin rạp. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* ── Page root ── */
.cinema-detail-page {
  min-height: 100vh;
  background: var(--bg, #0a0a12);
  color: var(--text, #e5e5e5);
  padding-bottom: 60px;
}

/* ── Back nav ── */
.cinema-detail-nav {
  padding: 20px 24px 0;
  max-width: 720px;
  margin: 0 auto;
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 8px;
  color: #9ca3af;
  font-size: 13px;
  font-weight: 500;
  padding: 7px 14px;
  cursor: pointer;
  transition: color 150ms ease, border-color 150ms ease;
}
.back-btn:hover {
  color: #fff;
  border-color: rgba(255,255,255,0.4);
}

/* ── Loading / Error states ── */
.cdp-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 80px 24px;
  color: #9ca3af;
  text-align: center;
}
.cdp-state--error { color: #f87171; }
.cdp-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(255,255,255,0.1);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.btn-back-home {
  padding: 9px 20px;
  border-radius: 8px;
  border: none;
  background: #fff;
  color: #0d0d0d;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

/* ── Content wrapper ── */
.cdp-content {
  max-width: 720px;
  margin: 24px auto 0;
  padding: 0 24px;
}

/* ── Hero ── */
.cdp-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: #1a1a2e;
  aspect-ratio: 16 / 7;
}
.cdp-hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.cdp-hero-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255,255,255,0.15);
  background: linear-gradient(135deg, #1a1a2e 0%, #0d1117 100%);
}
.cdp-hero-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
}
.cdp-hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

/* ── Info card ── */
.cdp-card {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 16px;
  padding: 28px;
  margin-top: 20px;
}
.cdp-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.cdp-name {
  font-size: clamp(20px, 4vw, 26px);
  font-weight: 700;
  color: #fff;
  margin: 0;
  line-height: 1.3;
}
.cdp-status {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  border-radius: 20px;
  padding: 4px 14px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.cdp-status--active   { background: rgba(16,185,129,0.15); color: #10b981; }
.cdp-status--inactive { background: rgba(156,163,175,0.15); color: #9ca3af; }

/* ── Info rows ── */
.cdp-info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}
.cdp-info-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #9ca3af;
  font-size: 14px;
  line-height: 1.5;
}
.cdp-info-row svg { flex-shrink: 0; margin-top: 2px; color: rgba(255,255,255,0.35); }
.cdp-info-row span { color: #d1d5db; }

/* ── Map button ── */
.cdp-map-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 22px;
  border-radius: 10px;
  background: #fff;
  color: #0d0d0d;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  transition: filter 150ms ease, transform 150ms ease;
}
.cdp-map-btn:hover {
  filter: brightness(0.92);
  transform: translateY(-1px);
}

/* ── Responsive ── */
@media (max-width: 600px) {
  .cdp-content { padding: 0 16px; }
  .cdp-card { padding: 20px; }
  .cinema-detail-nav { padding: 16px 16px 0; }
}
</style>

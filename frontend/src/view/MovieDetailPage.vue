<template>
  <div class="movie-detail" :class="{ 'no-scroll': false }">
    <!-- ── Sticky Header ── -->
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Chi tiết phim</span>
      <div style="width:40px"></div>
    </header>

    <!-- ── Loading ── -->
    <div v-if="loadingMovie" class="state-box">
      <div class="spinner"></div>
      <p>Đang tải thông tin phim...</p>
    </div>

    <!-- ── Error ── -->
    <div v-else-if="movieError" class="state-box state-box--error">
      <svg class="state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <p>{{ movieError }}</p>
      <button class="btn-retry" @click="loadMovie">Thử lại</button>
    </div>

    <!-- ── Content ── -->
    <div v-else-if="movie" class="detail-wrap">

      <!-- Hero banner -->
      <div class="hero" :style="movie.poster ? `background-image:url(${movie.poster})` : ''">
        <div class="hero__overlay"></div>
        <div class="hero__body">
          <div class="poster-wrap">
            <img v-if="movie.poster" :src="movie.poster" :alt="movie.title" class="poster-img" />
            <div v-else class="poster-fallback"><span>🎬</span><span class="poster-fallback__title">{{ movie.title }}</span></div>
            <span class="age-badge" :class="ageBadgeClass">{{ movie.ageRating }}</span>
          </div>
          <div class="hero__info">
            <h1 class="movie-title">{{ movie.title }}</h1>
            <p v-if="movie.titleEn" class="movie-title-en">{{ movie.titleEn }}</p>
            <div class="rating-row">
              <svg class="star-icon" viewBox="0 0 24 24" fill="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
              <span class="rating-value">{{ movie.rating ? Number(movie.rating).toFixed(1) : 'N/A' }}</span>
              <span class="rating-count">({{ fmtCount(movie.ratingCount) }})</span>
            </div>
            <div class="meta-chips">
              <span class="chip">⏱ {{ movie.duration }} phút</span>
              <span class="chip">🌐 {{ movie.language || '—' }}</span>
              <span class="chip chip--genre">{{ movie.genre || '—' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Meta grid -->
      <div class="section">
        <div class="meta-grid">
          <div class="meta-cell">
            <span class="meta-label">Đạo diễn</span>
            <span class="meta-value">{{ movie.director || '—' }}</span>
          </div>
          <div class="meta-cell">
            <span class="meta-label">Diễn viên</span>
            <span class="meta-value">{{ movie.cast || '—' }}</span>
          </div>
          <div class="meta-cell">
            <span class="meta-label">Ngày chiếu</span>
            <span class="meta-value">{{ fmtDate(movie.releaseDate) }}</span>
          </div>
          <div class="meta-cell">
            <span class="meta-label">Trạng thái</span>
            <span class="meta-value" :class="movie.status === 'dang_chieu' ? 'text-green' : 'text-yellow'">
              {{ movie.status === 'dang_chieu' ? 'Đang chiếu' : movie.status === 'sap_chieu' ? 'Sắp chiếu' : movie.status }}
            </span>
          </div>
        </div>
      </div>

      <!-- Description -->
      <div class="section">
        <h2 class="section-title">Nội dung</h2>
        <p class="description">{{ movie.description || 'Không có mô tả.' }}</p>
      </div>

      <!-- Trailer -->
      <div v-if="movie.trailer" class="section">
        <h2 class="section-title">Trailer</h2>
        <div class="trailer-wrap">
          <iframe :src="embedUrl(movie.trailer)" frameborder="0" allowfullscreen title="Trailer"></iframe>
        </div>
      </div>

      <!-- ── Schedule Section ── -->
      <div class="section schedule-section" id="schedule">
        <h2 class="section-title">Chọn suất chiếu</h2>

        <!-- Date tabs -->
        <div class="date-scroll">
          <button
            v-for="d in days"
            :key="d.iso"
            :class="['date-btn', { 'date-btn--active': selectedDate === d.iso }]"
            @click="selectDate(d.iso)"
          >
            <span class="date-btn__num">{{ d.num }}</span>
            <span class="date-btn__dow">{{ d.dow }}</span>
          </button>
        </div>

        <!-- Loading showtimes -->
        <div v-if="loadingSchedules" class="state-box state-box--small">
          <div class="spinner spinner--sm"></div>
          <p>Đang tải lịch chiếu...</p>
        </div>

        <!-- No showtimes -->
        <div v-else-if="schedulesOnDate.length === 0" class="no-show">
          <p>Không có suất chiếu ngày này</p>
        </div>

        <!-- Showtime cards -->
        <div v-else class="show-grid">
          <button
            v-for="s in schedulesOnDate"
            :key="s.id"
            :class="['show-card', { 'show-card--selected': selectedShowtime?.id === s.id }]"
            @click="pickShowtime(s)"
          >
            <span class="show-time">{{ fmtTime(s.thoiGianBatDau) }}</span>
            <span class="show-room">{{ s.tenPhong }}</span>
            <span class="show-type" :class="typeClass(s.loaiPhong)">{{ s.loaiPhong }}</span>
            <span class="show-price">{{ fmtPrice(s.giaCoBan) }}</span>
          </button>
        </div>
      </div>

      <!-- ── Book Button ── -->
      <div class="book-bar">
        <div class="book-bar__info">
          <template v-if="selectedShowtime">
            <span class="book-bar__label">{{ fmtTime(selectedShowtime.thoiGianBatDau) }} · {{ selectedShowtime.tenPhong }}</span>
            <span class="book-bar__price">{{ fmtPrice(selectedShowtime.giaCoBan) }}/ghế</span>
          </template>
          <span v-else class="book-bar__hint">Vui lòng chọn suất chiếu</span>
        </div>
        <button
          class="btn-book"
          :disabled="!selectedShowtime"
          @click="goBook"
        >
          Đặt vé
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMovieStore } from '@/stores/movieStore'
import { useBookingStore } from '@/stores/bookingStore'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'

const router = useRouter()
const route  = useRoute()
const movieStore   = useMovieStore()
const bookingStore = useBookingStore()
const authStore    = useAuthStore()

// ── reactive state ──────────────────────────────────────────
const movie           = ref(null)
const loadingMovie    = ref(false)
const movieError      = ref('')
const schedules       = ref([])
const loadingSchedules = ref(false)
const schedulesError  = ref('')
const selectedDate    = ref('')
const selectedShowtime = ref(null)

// ── 7-day tab list ──────────────────────────────────────────
const days = computed(() => {
  const out = []
  const DOW = ['CN','T2','T3','T4','T5','T6','T7']
  for (let i = 0; i < 7; i++) {
    const d = new Date()
    d.setDate(d.getDate() + i)
    out.push({
      iso: d.toISOString().slice(0, 10),
      num: d.getDate(),
      dow: DOW[d.getDay()],
    })
  }
  return out
})

// ── schedules filtered to selected date ────────────────────
const schedulesOnDate = computed(() => {
  if (!selectedDate.value) return []
  return schedules.value.filter(s => {
    if (!s.thoiGianBatDau) return false
    return s.thoiGianBatDau.slice(0, 10) === selectedDate.value
  })
})

// ── age badge class ─────────────────────────────────────────
const ageBadgeClass = computed(() => {
  const r = movie.value?.ageRating || 'P'
  if (r === 'P' || r === 'G') return 'age-badge--green'
  if (r.includes('13')) return 'age-badge--yellow'
  return 'age-badge--red'
})

// ── load movie ──────────────────────────────────────────────
async function loadMovie() {
  const id = route.params.id
  if (!id) return
  loadingMovie.value = true
  movieError.value = ''
  try {
    const res = await api.get(`/phim/${id}`)
    const m = res.data
    movie.value = {
      id: m.id,
      title: m.tenPhim || '',
      titleEn: m.tenPhimTiengAnh || '',
      genre: m.theLoai || '',
      director: m.daoDien || '',
      cast: m.dienVienChinh || '',
      duration: m.thoiLuong || 0,
      language: m.ngonNgu || '',
      ageRating: m.phanLoaiDoTuoi || 'P',
      poster: m.posterUrl || '',
      trailer: m.trailerUrl || '',
      description: m.moTa || '',
      rating: m.diemDanhGia || 0,
      ratingCount: m.soLuongDanhGia || 0,
      status: m.trangThai || '',
      releaseDate: m.ngayCongChieu || '',
    }
    // also push to bookingStore
    bookingStore.setMovie(movie.value)
  } catch (e) {
    movieError.value = e.response?.data?.message || 'Không tải được thông tin phim'
  } finally {
    loadingMovie.value = false
  }
}

// ── load all schedules for this movie (no date filter — we filter client-side) ──
async function loadSchedules() {
  const id = route.params.id
  if (!id) return
  loadingSchedules.value = true
  schedulesError.value = ''
  try {
    const res = await api.get(`/phim/${id}/lich-chieu`)
    schedules.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    schedulesError.value = 'Không tải được lịch chiếu'
    schedules.value = []
  } finally {
    loadingSchedules.value = false
  }
}

function selectDate(iso) {
  selectedDate.value = iso
  selectedShowtime.value = null
}

function pickShowtime(s) {
  selectedShowtime.value = s
  bookingStore.setShowtime({
    id: s.id,
    phimId: s.phimId,
    phongChieuId: s.phongChieuId,
    tenPhong: s.tenPhong,
    loaiPhong: s.loaiPhong,
    thoiGianBatDau: s.thoiGianBatDau,
    thoiGianKetThuc: s.thoiGianKetThuc,
    giaCoBan: s.giaCoBan,
    // Provide phongChieu sub-object for downstream pages
    phongChieu: { id: s.phongChieuId, tenPhong: s.tenPhong, loaiPhong: s.loaiPhong },
  })
}

function goBook() {
  if (!selectedShowtime.value) return
  if (!authStore.isLoggedIn) {
    router.push('/auth')
    return
  }
  router.push(`/seat-selection/${selectedShowtime.value.id}`)
}

// ── formatting helpers ──────────────────────────────────────
function fmtTime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}
function fmtDate(d) {
  if (!d) return '—'
  try { return new Date(d).toLocaleDateString('vi-VN') } catch { return d }
}
function fmtPrice(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}
function fmtCount(n) {
  if (!n) return '0'
  return n >= 1000 ? (n / 1000).toFixed(1) + 'k' : String(n)
}
function embedUrl(url) {
  if (!url) return ''
  if (url.includes('youtu.be')) return `https://www.youtube.com/embed/${url.split('youtu.be/')[1]}`
  if (url.includes('youtube.com')) {
    const v = url.split('v=')[1]?.split('&')[0]
    return v ? `https://www.youtube.com/embed/${v}` : url
  }
  return url
}
function typeClass(t) {
  if (!t) return ''
  if (t.toUpperCase().includes('IMAX')) return 'type-imax'
  if (t.toUpperCase().includes('3D'))   return 'type-3d'
  return 'type-2d'
}

// ── lifecycle ──────────────────────────────────────────────
onMounted(async () => {
  selectedDate.value = days.value[0].iso
  await loadMovie()
  await loadSchedules()
})

watch(() => route.params.id, async (id) => {
  if (id) {
    selectedShowtime.value = null
    selectedDate.value = days.value[0].iso
    await loadMovie()
    await loadSchedules()
  }
})
</script>

<style scoped>
/* ── base ──────────────────────────────────────────────────── */
.movie-detail {
  background: linear-gradient(160deg, #0b1120 0%, #0f172a 40%, #1a1f35 100%);
  color: #f1f5f9;
  min-height: 100vh;
  padding-bottom: 100px;
}

/* ── top bar ──────────────────────────────────────────────── */
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px;
  background: rgba(11,17,32,0.85); backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255,215,0,0.12);
  position: sticky; top: 0; z-index: 60;
}
.top-bar__title { font-size: 16px; font-weight: 700; }
.icon-btn {
  width: 38px; height: 38px; border-radius: 8px;
  border: 1px solid rgba(255,215,0,0.25);
  background: rgba(255,215,0,0.07);
  color: #ffd700; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background .2s;
}
.icon-btn:hover { background: rgba(255,215,0,0.15); }
.icon-btn svg { width: 18px; height: 18px; }

/* ── states ───────────────────────────────────────────────── */
.state-box {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 14px;
  min-height: 60vh; padding: 40px; text-align: center;
}
.state-box--error { color: #fca5a5; }
.state-box--small { min-height: 120px; padding: 24px; }
.state-icon { width: 48px; height: 48px; color: #ef4444; }
.spinner {
  width: 46px; height: 46px;
  border: 4px solid rgba(255,215,0,0.15);
  border-top-color: #ffd700; border-radius: 50%;
  animation: spin 0.9s linear infinite;
}
.spinner--sm { width: 28px; height: 28px; border-width: 3px; }
@keyframes spin { to { transform: rotate(360deg); } }
.btn-retry {
  padding: 10px 26px; background: #ffd700; color: #0f172a;
  border: none; border-radius: 8px; font-weight: 800; cursor: pointer;
}
.btn-retry:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(255,215,0,.3); }

/* ── hero ─────────────────────────────────────────────────── */
.hero {
  position: relative; min-height: 340px;
  background: #1a1f35 center/cover no-repeat;
  display: flex; align-items: flex-end;
}
.hero__overlay {
  position: absolute; inset: 0;
  background: linear-gradient(to bottom, rgba(11,17,32,.2) 0%, rgba(11,17,32,.85) 70%, #0f172a 100%);
}
.hero__body {
  position: relative; z-index: 2;
  display: flex; gap: 24px; align-items: flex-end;
  padding: 0 24px 28px; width: 100%; max-width: 1100px; margin: 0 auto;
}
.poster-wrap { position: relative; flex-shrink: 0; }
.poster-img {
  width: 110px; aspect-ratio: 2/3; object-fit: cover;
  border-radius: 10px; box-shadow: 0 16px 40px rgba(0,0,0,.6);
  border: 2px solid rgba(255,215,0,.2);
}
.poster-fallback {
  width: 110px; aspect-ratio: 2/3; border-radius: 10px;
  background: linear-gradient(145deg,#1a2540,#263348);
  border: 2px solid rgba(255,215,0,.2);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; padding: 12px; text-align: center;
}
.poster-fallback span:first-child { font-size: 32px; }
.poster-fallback__title { font-size: 11px; font-weight: 700; color: #94a3b8; line-height: 1.3; }

.age-badge {
  position: absolute; top: 8px; right: 8px;
  padding: 3px 7px; border-radius: 5px;
  font-size: 11px; font-weight: 900; color: #fff;
}
.age-badge--green { background: rgba(22,163,74,.9); }
.age-badge--yellow { background: rgba(202,138,4,.9); }
.age-badge--red { background: rgba(220,38,38,.9); }

.hero__info { flex: 1; }
.movie-title { font-size: clamp(20px,4vw,32px); font-weight: 900; margin: 0 0 4px; line-height: 1.2; }
.movie-title-en { font-size: 13px; color: #94a3b8; font-style: italic; margin: 0 0 12px; }
.rating-row { display: flex; align-items: center; gap: 6px; margin-bottom: 12px; }
.star-icon { width: 18px; height: 18px; color: #ffd700; }
.rating-value { font-size: 20px; font-weight: 900; color: #ffd700; }
.rating-count { font-size: 12px; color: #94a3b8; }
.meta-chips { display: flex; gap: 8px; flex-wrap: wrap; }
.chip {
  padding: 4px 10px; border-radius: 999px;
  background: rgba(255,255,255,.07);
  font-size: 12px; font-weight: 700; color: #cbd5e1;
  border: 1px solid rgba(255,255,255,.1);
}
.chip--genre { background: rgba(255,215,0,.1); border-color: rgba(255,215,0,.25); color: #ffd700; }

/* ── sections ─────────────────────────────────────────────── */
.detail-wrap { max-width: 1100px; margin: 0 auto; }
.section { padding: 28px 24px; border-bottom: 1px solid rgba(255,215,0,.07); }
.section-title { font-size: 18px; font-weight: 800; color: #ffd700; margin: 0 0 16px; }
.meta-grid { display: grid; grid-template-columns: repeat(auto-fill,minmax(200px,1fr)); gap: 16px; }
.meta-cell { display: flex; flex-direction: column; gap: 4px; }
.meta-label { font-size: 11px; text-transform: uppercase; letter-spacing: .5px; color: #64748b; font-weight: 700; }
.meta-value { font-size: 14px; font-weight: 600; color: #e2e8f0; }
.text-green { color: #4ade80; }
.text-yellow { color: #fbbf24; }
.description { font-size: 14px; line-height: 1.8; color: #cbd5e1; margin: 0; }

/* ── trailer ──────────────────────────────────────────────── */
.trailer-wrap {
  position: relative; width: 100%; padding-bottom: 56.25%; height: 0;
  border-radius: 12px; overflow: hidden;
}
.trailer-wrap iframe { position: absolute; inset: 0; width: 100%; height: 100%; border: 0; }

/* ── schedule ─────────────────────────────────────────────── */
.schedule-section { padding-bottom: 40px; }
.date-scroll {
  display: flex; gap: 10px; overflow-x: auto;
  padding-bottom: 12px; margin-bottom: 20px;
  scrollbar-width: thin; scrollbar-color: rgba(255,215,0,.2) transparent;
}
.date-scroll::-webkit-scrollbar { height: 4px; }
.date-scroll::-webkit-scrollbar-thumb { background: rgba(255,215,0,.2); border-radius: 2px; }
.date-btn {
  flex-shrink: 0; width: 56px; padding: 10px 6px;
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  border-radius: 10px; border: 2px solid rgba(255,215,0,.2);
  background: rgba(255,215,0,.04); color: #94a3b8;
  cursor: pointer; transition: all .2s;
}
.date-btn:hover { border-color: #ffd700; color: #ffd700; background: rgba(255,215,0,.08); }
.date-btn--active { background: #ffd700; border-color: #ffd700; color: #0f172a; }
.date-btn__num { font-size: 18px; font-weight: 900; line-height: 1; }
.date-btn__dow { font-size: 10px; font-weight: 700; text-transform: uppercase; }

.no-show { text-align: center; padding: 32px; color: #64748b; font-size: 14px; }
.show-grid { display: grid; grid-template-columns: repeat(auto-fill,minmax(140px,1fr)); gap: 12px; }
.show-card {
  display: flex; flex-direction: column; gap: 6px; text-align: center;
  padding: 14px 10px; border-radius: 10px;
  border: 2px solid rgba(255,215,0,.2);
  background: rgba(255,215,0,.04);
  color: #f1f5f9; cursor: pointer; transition: all .2s;
}
.show-card:hover { border-color: #ffd700; background: rgba(255,215,0,.1); transform: translateY(-3px); }
.show-card--selected { border-color: #ffd700; background: rgba(255,215,0,.15); box-shadow: 0 0 20px rgba(255,215,0,.2); }
.show-time { font-size: 22px; font-weight: 900; color: #ffd700; }
.show-room { font-size: 11px; color: #94a3b8; }
.show-type {
  display: inline-block; padding: 2px 8px; border-radius: 4px;
  font-size: 10px; font-weight: 900; text-transform: uppercase;
  align-self: center;
}
.type-2d  { background: rgba(59,130,246,.2); color: #93c5fd; }
.type-3d  { background: rgba(168,85,247,.2); color: #d8b4fe; }
.type-imax { background: rgba(234,179,8,.2); color: #fde047; }
.show-price { font-size: 13px; font-weight: 800; color: #ffd700; }

/* ── book bar ─────────────────────────────────────────────── */
.book-bar {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 55;
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  padding: 14px 24px;
  background: rgba(11,17,32,.96); backdrop-filter: blur(16px);
  border-top: 1px solid rgba(255,215,0,.2);
}
.book-bar__info { display: flex; flex-direction: column; gap: 2px; }
.book-bar__label { font-size: 13px; font-weight: 700; color: #f1f5f9; }
.book-bar__price { font-size: 16px; font-weight: 900; color: #ffd700; }
.book-bar__hint { font-size: 13px; color: #64748b; }
.btn-book {
  padding: 13px 32px; border-radius: 10px;
  background: linear-gradient(135deg,#ffd700,#ffed4e);
  color: #0f172a; border: none; font-size: 15px; font-weight: 900;
  cursor: pointer; transition: all .2s; white-space: nowrap;
  box-shadow: 0 6px 20px rgba(255,215,0,.3);
}
.btn-book:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 10px 28px rgba(255,215,0,.4); }
.btn-book:disabled { opacity: .4; cursor: not-allowed; box-shadow: none; }

/* ── responsive ───────────────────────────────────────────── */
@media (max-width: 640px) {
  .hero__body { flex-direction: column; align-items: flex-start; gap: 16px; padding: 16px; }
  .poster-img, .poster-fallback { width: 90px; }
  .section { padding: 20px 16px; }
  .book-bar { padding: 12px 16px; }
  .btn-book { padding: 12px 20px; font-size: 14px; }
}
</style>

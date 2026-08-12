<template>
  <div class="movie-detail" :class="{ 'no-scroll': false }">
    <!-- ── Sticky Header ── -->
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Chi tiết phim</span>
      <ThemeToggle />
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
              <template v-if="movie.theLoais && movie.theLoais.length > 0">
                <span v-for="t in movie.theLoais" :key="t.id" class="chip chip--genre">{{ t.tenTheLoai }}</span>
              </template>
              <span v-else-if="movie.genre" class="chip chip--genre">{{ movie.genre }}</span>
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
            <span class="meta-value" :class="movie.status === 'dang_chieu' ? 'text-green' : movie.status === 'sap_chieu' ? 'text-yellow' : 'text-muted'">
              {{ movie.status === 'dang_chieu' ? 'Đang chiếu' : movie.status === 'sap_chieu' ? 'Sắp chiếu' : movie.status === 'chua_chieu' ? 'Chưa chiếu' : movie.status === 'da_ket_thuc' ? 'Đã kết thúc' : movie.status }}
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

        <!-- City + Format filters -->
        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label" for="city-select">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              Thành phố
            </label>
            <select
              id="city-select"
              class="filter-select"
              v-model="selectedCity"
              @change="onFilterChange"
              :aria-label="'Chọn thành phố'"
            >
              <option value="">Tất cả thành phố</option>
              <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>

          <div class="filter-group">
            <label class="filter-label" for="format-select">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
              Định dạng
            </label>
            <select
              id="format-select"
              class="filter-select"
              v-model="selectedDinhDang"
              @change="onFilterChange"
              :aria-label="'Chọn định dạng'"
            >
              <option value="">Tất cả định dạng</option>
              <option v-for="dd in dinhDangs" :key="dd.id" :value="String(dd.id)">{{ dd.tenDinhDang }}</option>
            </select>
          </div>
        </div>

        <!-- Date tabs -->
        <div class="date-scroll" role="tablist" aria-label="Chọn ngày">
          <DayChip
            v-for="d in days"
            :key="d.iso"
            :num="d.num"
            :mo="d.mo"
            :dow="d.dow"
            :active="selectedDate === d.iso"
            @select="selectDate(d.iso)"
          />
        </div>

        <!-- Loading showtimes -->
        <div v-if="loadingSchedules" class="state-box state-box--small">
          <div class="spinner spinner--sm"></div>
          <p>Đang tải lịch chiếu...</p>
        </div>

        <!-- No showtimes -->
        <div v-else-if="cinemaGroups.length === 0" class="no-show">
          <p>Không có suất chiếu ngày này</p>
        </div>

        <!-- Cinema-grouped showtime display -->
        <div v-else class="cinema-groups">
          <div
            v-for="group in cinemaGroups"
            :key="group.rapChieuId"
            class="cinema-group"
          >
            <!-- Cinema header -->
            <div class="cinema-group__header">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              <span class="cinema-group__name">{{ group.tenRap }}</span>
              <span v-if="group.thanhPho" class="cinema-group__city">{{ group.thanhPho }}</span>
            </div>

            <!-- Showtime buttons for this cinema -->
            <div v-for="g in groupByBuoi(group.showtimes)" :key="g.label" class="show-buoi">
              <div class="show-buoi__label">{{ g.label }}</div>
              <div class="show-grid">
                <button
                  v-for="s in g.list"
                  :key="s.id"
                  :class="['show-card', { 'show-card--selected': selectedShowtime?.id === s.id }]"
                  @click="pickShowtime(s)"
                >
                  <span class="show-time">{{ fmtTime(s.thoiGianBatDau) }}</span>
                  <span class="show-room">{{ s.tenPhong }}</span>
                  <span class="show-type" :class="typeClass(s.tenDinhDang || s.loaiPhong)">
                    {{ s.tenDinhDang || s.loaiPhong }}
                  </span>
                  <span class="show-price">{{ fmtPrice(s.giaCoBan) }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Book Button ── -->
      <div class="book-bar">
        <div class="book-bar__info">
          <template v-if="selectedShowtime">
            <span class="book-bar__label">
              {{ selectedShowtime.tenRap ? selectedShowtime.tenRap + ' · ' : '' }}{{ fmtTime(selectedShowtime.thoiGianBatDau) }} · {{ selectedShowtime.tenPhong }}
            </span>
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
import ThemeToggle from '@/components/ThemeToggle.vue'
import DayChip from '@/components/DayChip.vue'
import api from '@/services/api'
import { fmtTime12 } from '@/utils/homeHelpers'

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

// ── Phase 4: city / format filter state ────────────────────
const cities         = ref([])   // ['TP.HCM', 'Hà Nội', ...]
const dinhDangs      = ref([])   // [{ id, tenDinhDang }, ...]
const selectedCity   = ref('')   // '' = all
const selectedDinhDang = ref('') // '' = all (stored as string id)

// ── 30-day tab list ─────────────────────────────────────────
// Use local-date arithmetic only — never toISOString() which converts to UTC
// and produces the wrong date string in timezones ahead of UTC (e.g. UTC+7).
const days = computed(() => {
  const out = []
  const DOW = ['CN','T2','T3','T4','T5','T6','T7']
  // Anchor to today's local midnight to avoid any intra-day UTC drift
  const todayLocal = new Date()
  const baseY = todayLocal.getFullYear()
  const baseM = todayLocal.getMonth()
  const baseD = todayLocal.getDate()

  for (let i = 0; i < 30; i++) {
    const d = new Date(baseY, baseM, baseD + i) // local midnight, no UTC shift
    const y  = d.getFullYear()
    const mo = String(d.getMonth() + 1).padStart(2, '0')
    const dy = String(d.getDate()).padStart(2, '0')
    out.push({
      iso: `${y}-${mo}-${dy}`,   // "YYYY-MM-DD" in local time
      num: d.getDate(),
      mo: Number(mo),
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

// ── showtimes grouped by cinema for the new Phase 4 UI ─────
const cinemaGroups = computed(() => {
  const on = schedulesOnDate.value
  if (!on.length) return []
  const map = new Map()
  for (const s of on) {
    const key = s.rapChieuId ?? 0
    if (!map.has(key)) {
      map.set(key, {
        rapChieuId: s.rapChieuId,
        tenRap:     s.tenRap     || 'Rạp không rõ',
        thanhPho:   s.thanhPho   || '',
        showtimes:  [],
      })
    }
    map.get(key).showtimes.push(s)
  }
  // sort each cinema's showtimes by start time
  for (const g of map.values()) {
    g.showtimes.sort((a, b) => a.thoiGianBatDau.localeCompare(b.thoiGianBatDau))
  }
  return [...map.values()].sort((a, b) => a.tenRap.localeCompare(b.tenRap))
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
      // new N-N genre list
      theLoais: Array.isArray(m.theLoais) ? m.theLoais : [],
      genre: Array.isArray(m.theLoais) && m.theLoais.length > 0
        ? m.theLoais.map(t => t.tenTheLoai).join(', ')
        : '',
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

// ── load schedules — uses /search when filters active, otherwise /phim/{id}/lich-chieu ──
async function loadSchedules() {
  const id = route.params.id
  if (!id) return
  loadingSchedules.value = true
  schedulesError.value = ''
  try {
    const params = { phimId: id }
    if (selectedCity.value)   params.thanhPho   = selectedCity.value
    if (selectedDinhDang.value) params.dinhDangId = selectedDinhDang.value

    const useSearch = selectedCity.value || selectedDinhDang.value
    const endpoint  = useSearch ? '/lich-chieu/search' : `/phim/${id}/lich-chieu`
    const res = await api.get(endpoint, { params: useSearch ? params : {} })
    schedules.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    schedulesError.value = 'Không tải được lịch chiếu'
    schedules.value = []
  } finally {
    loadingSchedules.value = false
  }
}

// ── load filter options (cities + formats) ──────────────────
async function loadFilterOptions() {
  try {
    const [citiesRes, formatsRes] = await Promise.all([
      api.get('/rap-chieu/cities'),
      api.get('/dinh-dang'),
    ])
    cities.value   = citiesRes.data  || []
    dinhDangs.value = formatsRes.data || []
  } catch { /* non-fatal — selectors just stay empty */ }
}

// ── react to filter changes ─────────────────────────────────
async function onFilterChange() {
  selectedShowtime.value = null
  await loadSchedules()
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
    loaiPhong: s.loaiPhong || s.tenDinhDang || '',
    rapChieuId: s.rapChieuId,
    tenRap: s.tenRap,
    thanhPho: s.thanhPho,
    tenDinhDang: s.tenDinhDang,
    thoiGianBatDau: s.thoiGianBatDau,
    thoiGianKetThuc: s.thoiGianKetThuc,
    giaCoBan: s.giaCoBan,
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
  return fmtTime12(dt)
}
function groupByBuoi(list) {
  const morning = []
  const evening = []
  for (const s of list || []) {
    const h = new Date(s.thoiGianBatDau).getHours()
    if (h >= 1 && h <= 12) morning.push(s)
    else evening.push(s)
  }
  return [
    { label: 'Buổi sáng', list: morning },
    { label: 'Buổi chiều / tối', list: evening },
  ].filter(g => g.list.length)
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
  await Promise.all([loadSchedules(), loadFilterOptions()])
})

watch(() => route.params.id, async (id) => {
  if (id) {
    selectedShowtime.value = null
    selectedCity.value = ''
    selectedDinhDang.value = ''
    selectedDate.value = days.value[0].iso
    await loadMovie()
    await Promise.all([loadSchedules(), loadFilterOptions()])
  }
})
</script>

<style scoped>
/* ── base ──────────────────────────────────────────────────── */
.movie-detail {
  background: var(--void, #050508);
  color: var(--text-secondary, #94a3b8);
  min-height: 100vh;
  padding-bottom: 100px;
  font-family: var(--font-ui, 'Inter', sans-serif);
}

/* ── top bar ──────────────────────────────────────────────── */
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px;
  background: rgba(5,5,8,0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  position: sticky; top: 0; z-index: 60;
}
.top-bar__title {
  font-size: 16px; font-weight: 700;
  color: var(--text-primary, #f1f5f9);
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
}
.icon-btn {
  width: 44px; height: 44px;
  border-radius: var(--radius-sm, 6px);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--electric, #29bcea); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s, border-color 0.2s;
}
.icon-btn:hover {
  background: var(--glass-bg-heavy, rgba(255,255,255,0.08));
  border-color: var(--electric, #29bcea);
}
.icon-btn svg { width: 18px; height: 18px; }

/* ── states ───────────────────────────────────────────────── */
.state-box {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 14px;
  min-height: 60vh; padding: 40px; text-align: center;
  color: var(--text-secondary, #94a3b8);
}
.state-box--error { color: #fca5a5; }
.state-box--small { min-height: 120px; padding: 24px; }
.state-icon { width: 48px; height: 48px; color: #ef4444; }
.spinner {
  width: 46px; height: 46px;
  border: 3px solid var(--glass-border, rgba(255,255,255,0.08));
  border-top-color: var(--electric, #29bcea); border-radius: 50%;
  animation: spin 0.9s linear infinite;
}
.spinner--sm { width: 28px; height: 28px; border-width: 2px; }
@keyframes spin { to { transform: rotate(360deg); } }
.btn-retry {
  padding: 10px 26px;
  background: var(--electric, #29bcea); color: var(--on-accent, #ffffff);
  border: none; border-radius: var(--radius-sm, 6px);
  font-weight: 700; cursor: pointer; min-height: 44px;
  transition: background 0.2s;
}
.btn-retry:hover { background: var(--electric-hover, #1a9fbd); }

/* ── hero ─────────────────────────────────────────────────── */
.hero {
  position: relative;
  height: 100svh; min-height: 600px;
  background: var(--deep, #0a0a0f) center/cover no-repeat;
  display: flex; align-items: flex-end;
  overflow: hidden;
}
.hero__overlay {
  position: absolute; inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(5,5,8,0.15) 0%,
    rgba(5,5,8,0.55) 60%,
    var(--void, #050508) 100%
  );
}
.hero__body {
  position: relative; z-index: 2;
  display: flex; gap: 28px; align-items: flex-end;
  padding: 0 32px 48px; width: 100%; max-width: 1100px; margin: 0 auto;
}
.poster-wrap { position: relative; flex-shrink: 0; }
.poster-img {
  width: 160px; aspect-ratio: 2/3; object-fit: cover;
  border-radius: var(--radius-md, 12px);
  box-shadow: 0 0 0 1px rgba(255,255,255,0.15), 0 0 40px rgba(0,0,0,0.7);
}
.poster-fallback {
  width: 160px; aspect-ratio: 2/3;
  border-radius: var(--radius-md, 12px);
  background: var(--surface-2, #14141f);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; padding: 12px; text-align: center;
}
.poster-fallback span:first-child { font-size: 32px; }
.poster-fallback__title { font-size: 11px; font-weight: 700; color: var(--text-ghost, rgba(241,245,249,0.45)); line-height: 1.3; }

.age-badge {
  position: absolute; top: 10px; right: 10px;
  padding: 4px 8px; border-radius: var(--radius-sm, 6px);
  font-size: 11px; font-weight: 900; color: #fff;
  backdrop-filter: blur(8px);
}
.age-badge--green  { background: rgba(22,163,74,0.85); }
.age-badge--yellow { background: rgba(202,138,4,0.85); }
.age-badge--red    { background: rgba(220,38,38,0.85); }

.hero__info { flex: 1; color: var(--text-primary, #f1f5f9); }
.movie-title {
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  font-size: clamp(28px, 5vw, 56px);
  font-weight: 700; margin: 0 0 6px; line-height: 1.1;
  letter-spacing: -0.03em;
  color: var(--text-primary, #f1f5f9);
}
.movie-title-en {
  font-size: 13px; color: var(--text-ghost, rgba(241,245,249,0.45));
  font-style: italic; margin: 0 0 14px;
}
.rating-row { display: flex; align-items: center; gap: 6px; margin-bottom: 14px; }
.star-icon { width: 18px; height: 18px; color: var(--gold-bright, #F5D17E); }
.rating-value { font-size: 20px; font-weight: 900; color: var(--gold-bright, #F5D17E); }
.rating-count { font-size: 12px; color: var(--text-ghost, rgba(241,245,249,0.45)); }
.meta-chips { display: flex; gap: 8px; flex-wrap: wrap; }
.chip {
  padding: 5px 12px; border-radius: var(--radius-pill, 999px);
  background: var(--glass-bg, rgba(255,255,255,0.04));
  font-size: 12px; font-weight: 600; color: var(--text-secondary, #94a3b8);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  backdrop-filter: blur(8px);
}
.chip--genre {
  background: var(--electric-soft, rgba(41,188,234,0.08));
  border-color: rgba(41,188,234,0.25); color: var(--electric, #29bcea);
}

/* ── sections ─────────────────────────────────────────────── */
.detail-wrap { max-width: 1100px; margin: 0 auto; }
.section {
  padding: 28px 32px;
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.section-title {
  font-size: 18px; font-weight: 700;
  color: var(--text-primary, #f1f5f9); margin: 0 0 16px;
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  letter-spacing: -0.02em;
}
.meta-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px,1fr)); gap: 16px; }
.meta-cell { display: flex; flex-direction: column; gap: 4px; }
.meta-label { font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-ghost, rgba(241,245,249,0.45)); font-weight: 700; }
.meta-value { font-size: 14px; font-weight: 600; color: var(--text-primary, #f1f5f9); }
.text-green  { color: #4ade80; }
.text-yellow { color: #fbbf24; }
.description { font-size: 14px; line-height: 1.8; color: var(--text-secondary, #94a3b8); margin: 0; }

/* ── trailer ──────────────────────────────────────────────── */
.trailer-wrap {
  position: relative; width: 100%; padding-bottom: 56.25%; height: 0;
  border-radius: var(--radius-md, 12px); overflow: hidden;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.trailer-wrap iframe { position: absolute; inset: 0; width: 100%; height: 100%; border: 0; }

/* ── schedule ─────────────────────────────────────────────── */
.schedule-section { padding-bottom: 40px; }

/* Filter row: city + format selectors */
.filter-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1 1 160px;
  min-width: 140px;
  max-width: 260px;
}
.filter-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-ghost, rgba(241,245,249,0.45));
}
.filter-select {
  padding: 9px 14px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-sm, 6px);
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--text-primary, #f1f5f9);
  font-size: 14px;
  font-family: var(--font-ui, 'Inter', sans-serif);
  cursor: pointer;
  outline: none;
  min-height: 40px;
  transition: border-color 0.2s;
  -webkit-appearance: none;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%239CA3AF' stroke-width='1.5' fill='none' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
}
.filter-select:focus { border-color: var(--electric, #29bcea); }
.filter-select option { background: var(--surface-1, #0f0f17); color: var(--text-primary, #f1f5f9); }

/* Cinema groups */
.cinema-groups { display: flex; flex-direction: column; gap: 20px; }

.cinema-group {
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md, 12px);
  overflow: hidden;
  background: var(--glass-bg, rgba(255,255,255,0.02));
}

.cinema-group__header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--glass-bg, rgba(255,255,255,0.04));
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  color: var(--text-primary, #f1f5f9);
}
.cinema-group__header svg { color: var(--electric, #29bcea); flex-shrink: 0; }
.cinema-group__name { font-size: 14px; font-weight: 700; }
.cinema-group__city {
  font-size: 11px;
  color: var(--electric, #29bcea);
  font-weight: 600;
  margin-left: auto;
  background: rgba(41,188,234,0.08);
  border: 1px solid rgba(41,188,234,0.2);
  border-radius: var(--radius-pill, 999px);
  padding: 2px 8px;
}

.cinema-group .show-grid { padding: 12px; }
.show-buoi {}
.show-buoi__label {
  font-size: 12px; font-weight: 700;
  color: var(--electric, #29bcea);
  text-transform: uppercase; letter-spacing: 0.05em;
  padding: 12px 12px 0;
}
.date-scroll {
  display: flex; gap: 10px; overflow-x: auto;
  padding-bottom: 12px; margin-bottom: 20px;
  scrollbar-width: thin; scrollbar-color: var(--electric-soft, rgba(41,188,234,0.08)) transparent;
}
.date-scroll::-webkit-scrollbar { height: 4px; }
.date-scroll::-webkit-scrollbar-thumb { background: var(--electric-soft, rgba(41,188,234,0.08)); border-radius: 2px; }
.date-btn__dow { font-size: 10px; font-weight: 700; text-transform: uppercase; }

.no-show { text-align: center; padding: 32px; color: var(--text-ghost, rgba(241,245,249,0.45)); font-size: 14px; }
.show-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px,1fr)); gap: 12px; }
.show-card {
  display: flex; flex-direction: column; gap: 6px; text-align: center;
  padding: 14px 10px;
  border-radius: var(--radius-md, 12px);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--text-secondary, #94a3b8); cursor: pointer;
  transition: all 0.2s;
  backdrop-filter: blur(8px);
}
.show-card:hover { border-color: var(--electric, #29bcea); background: var(--glass-bg-heavy, rgba(255,255,255,0.08)); transform: translateY(-3px); }
.show-card--selected { border-color: var(--electric, #29bcea); background: var(--electric-soft, rgba(41,188,234,0.08)); }
.show-time { font-size: 22px; font-weight: 900; color: var(--electric, #29bcea); }
.show-room { font-size: 11px; color: var(--text-ghost, rgba(241,245,249,0.45)); }
.show-type {
  display: inline-block; padding: 2px 8px; border-radius: var(--radius-pill, 999px);
  font-size: 10px; font-weight: 900; text-transform: uppercase; align-self: center;
}
.type-2d   { background: rgba(59,130,246,0.2); color: #93c5fd; }
.type-3d   { background: rgba(168,85,247,0.2); color: #d8b4fe; }
.type-imax { background: rgba(234,179,8,0.2); color: #fde047; }
.show-price { font-size: 13px; font-weight: 800; color: var(--electric, #29bcea); }

/* ── book bar ─────────────────────────────────────────────── */
.book-bar {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 55;
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  padding: 14px 24px;
  background: rgba(5,5,8,0.9);
  backdrop-filter: var(--glass-blur, blur(20px));
  -webkit-backdrop-filter: var(--glass-blur, blur(20px));
  border-top: 1px solid var(--glass-border, rgba(255,255,255,0.08));
}
.book-bar__info { display: flex; flex-direction: column; gap: 2px; }
.book-bar__label { font-size: 13px; font-weight: 700; color: var(--text-primary, #f1f5f9); }
.book-bar__price { font-size: 16px; font-weight: 900; color: var(--electric, #29bcea); }
.book-bar__hint { font-size: 13px; color: var(--text-ghost, rgba(241,245,249,0.45)); }
.btn-book {
  position: relative;
  padding: 13px 36px;
  border-radius: var(--radius-sm, 6px);
  background: var(--electric, #29bcea);
  color: var(--on-accent, #ffffff); border: none;
  font-size: 15px; font-weight: 700; cursor: pointer;
  white-space: nowrap; min-height: 44px;
  outline: 1.5px solid rgba(41,188,234,0.45); outline-offset: 3px;
  transition: transform 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1)),
              box-shadow 0.3s var(--ease-out, cubic-bezier(0.4,0,0.2,1)),
              outline-offset 0.3s var(--spring, cubic-bezier(0.34,1.56,0.64,1));
  will-change: transform;
}
.btn-book:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--electric-glow, rgba(41,188,234,0.3));
  outline-offset: 5px;
}
.btn-book:disabled { opacity: 0.4; cursor: not-allowed; box-shadow: none; }

/* ── responsive ───────────────────────────────────────────── */
@media (max-width: 640px) {
  .hero { min-height: 500px; }
  .hero__body { flex-direction: column; align-items: flex-start; gap: 16px; padding: 16px; }
  .poster-img, .poster-fallback { width: 110px; }
  .section { padding: 20px 16px; }
  .book-bar { padding: 12px 16px; }
  .btn-book { padding: 12px 20px; font-size: 14px; }
}
</style>

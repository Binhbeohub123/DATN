<template>
  <div class="cinema-detail">
    <header class="top-bar">
      <button class="icon-btn" @click="router.back()" aria-label="Quay lại">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <span class="top-bar__title">Chi tiết rạp</span>
      <ThemeToggle />
    </header>

    <div v-if="loadingCinema" class="state-box">
      <div class="spinner"></div>
      <p>Đang tải thông tin rạp...</p>
    </div>

    <div v-else-if="cinemaError" class="state-box state-box--error">
      <p>{{ cinemaError }}</p>
      <button class="btn-retry" @click="loadCinema">Thử lại</button>
    </div>

    <template v-else-if="cinema">
      <div class="hero" :style="cinema.hinhAnh ? `background-image:url(${cinema.hinhAnh})` : ''">
        <div class="hero__overlay"></div>
        <div class="hero__body">
          <div v-if="!cinema.hinhAnh" class="hero__placeholder">🎬</div>
          <div class="hero__info">
            <h1 class="cinema-title">{{ cinema.tenRap }}</h1>
            <p v-if="cinema.thanhPho" class="cinema-city">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              {{ cinema.thanhPho }}
            </p>
            <p v-if="cinema.diaChi" class="cinema-addr">{{ cinema.diaChi }}</p>
            <p v-if="cinema.soDienThoai" class="cinema-phone">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 1.28l3-.08a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L7.91 9a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
              {{ cinema.soDienThoai }}
            </p>
            <a :href="mapUrl" target="_blank" rel="noopener noreferrer" class="btn-map">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="3 11 22 2 13 21 11 13 3 11"/></svg>
              Xem bản đồ
            </a>
          </div>
        </div>
      </div>

      <div class="page-wrap">
        <div class="tabs-bar" role="tablist">
          <button :class="['tab-btn', { 'tab-btn--active': activeTab === 'dang_chieu' }]" @click="activeTab = 'dang_chieu'" role="tab">Phim đang chiếu</button>
          <button :class="['tab-btn', { 'tab-btn--active': activeTab === 'sap_chieu' }]" @click="activeTab = 'sap_chieu'" role="tab">Phim sắp chiếu</button>
          <button :class="['tab-btn', { 'tab-btn--active': activeTab === 'gia_ve' }]" @click="activeTab = 'gia_ve'" role="tab">Bảng giá vé</button>
        </div>

        <div class="tab-head">
          <h2 class="page-title">{{ pageTitle }}</h2>
          <span v-if="activeTab === 'dang_chieu'" class="tab-hint">Hiển thị suất chiếu trong 2 ngày tới — muốn xem thêm vui lòng vào chi tiết phim.</span>
        </div>

        <div v-show="activeTab === 'dang_chieu'" class="tab-panel">
          <div v-if="loadingSchedules" class="state-box state-box--small"><div class="spinner spinner--sm"></div><p>Đang tải...</p></div>
          <div v-else-if="dangChieuGroups.length === 0" class="no-show">Không có phim đang chiếu</div>
          <div v-else class="movie-grid">
            <div v-for="mg in dangChieuGroups" :key="mg.phimId" class="movie-card">
              <div class="movie-card__poster">
                <img v-if="mg.posterUrl" :src="mg.posterUrl" :alt="mg.tenPhim" @error="(e) => e.target.style.display='none'" />
                <div v-else class="movie-card__poster-fallback">🎬</div>
              </div>
              <div class="movie-card__info">
                <h3 class="movie-card__title">{{ mg.tenPhim }}</h3>
                <div class="movie-card__meta">
                  <span v-if="(mg.theLoai || []).length" class="meta-item">{{ (mg.theLoai || []).join(', ') }}</span>
                  <span v-if="mg.thoiLuong" class="meta-item">⏱ {{ mg.thoiLuong }} phút</span>
                  <span v-if="mg.ngonNgu" class="meta-item">🌐 {{ mg.ngonNgu }}</span>
                  <span class="meta-item"><span class="age-badge" :class="ageBadgeClass(mg.phanLoaiDoTuoi)">{{ mg.phanLoaiDoTuoi || 'P' }}</span></span>
                </div>
                <div v-if="mg.diemDanhGia" class="movie-card__rating">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor" style="color:#ffd400"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                  {{ Number(mg.diemDanhGia).toFixed(1) }}
                </div>

                <div class="date-acc-list">
                  <div v-for="d in days" :key="d.iso">
                    <div v-if="mg.byDay[d.iso] && mg.byDay[d.iso].length" class="date-acc" :class="{ 'date-acc--open': openDays[mg.phimId + ':' + d.iso] !== false }">
                      <button class="date-acc__head" @click="toggleDay(mg, d.iso)">
                        <DayChip static :num="d.num" :mo="d.mon" :dow="d.dow" class="date-acc__chip" />
                        <span class="date-acc__count">{{ mg.byDay[d.iso].length }} suất</span>
                        <svg class="date-acc__chev" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg>
                      </button>
                      <div v-show="openDays[mg.phimId + ':' + d.iso] !== false" class="date-acc__body">
                        <div v-for="fmt in formatGroups(mg.byDay[d.iso])" :key="fmt.label" class="fmt-group">
                          <div class="fmt-group__label">{{ fmt.label }}</div>
                          <div class="fmt-group__times">
                            <button
                              v-for="s in fmt.shows" :key="s.lichChieuId"
                              class="time-btn"
                              :class="{ 'time-btn--active': selectedShows['m' + mg.phimId]?.lichChieuId === s.lichChieuId }"
                              @click="selectShow(mg, s)"
                            >{{ fmtTime24(s.thoiGianBatDau) }}</button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="selectedShows['m' + mg.phimId]" class="selected-bar">
                  <div class="selected-bar__info">
                    <span class="selected-bar__time">{{ fmtTime(selectedShows['m' + mg.phimId].thoiGianBatDau) }} — Phòng {{ selectedShows['m' + mg.phimId].tenPhong }}</span>
                    <span class="selected-bar__price">{{ fmtPrice(selectedShows['m' + mg.phimId].giaCoBan) }}</span>
                  </div>
                  <button class="btn-book" @click="goBook(selectedShows['m' + mg.phimId])">Đặt vé</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-show="activeTab === 'sap_chieu'" class="tab-panel">
          <div v-if="loadingSapChieu" class="state-box state-box--small"><div class="spinner spinner--sm"></div><p>Đang tải...</p></div>
          <div v-else-if="sapChieuMovies.length === 0" class="no-show">Không có phim sắp chiếu</div>
          <div v-else class="movie-grid">
            <div v-for="m in sapChieuMovies" :key="m.id" class="movie-card">
              <div class="movie-card__poster">
                <img v-if="m.posterUrl" :src="m.posterUrl" :alt="m.tenPhim" @error="(e) => e.target.style.display='none'" />
                <div v-else class="movie-card__poster-fallback">🎬</div>
              </div>
              <div class="movie-card__info">
                <h3 class="movie-card__title">{{ m.tenPhim }}</h3>
                <div class="movie-card__meta">
                  <span v-if="(m.theLoais || []).length" class="meta-item">{{ (m.theLoais || []).map(g => g.tenTheLoai).join(', ') }}</span>
                  <span v-if="m.thoiLuong" class="meta-item">⏱ {{ m.thoiLuong }} phút</span>
                  <span v-if="m.ngonNgu" class="meta-item">🌐 {{ m.ngonNgu }}</span>
                  <span v-if="m.phanLoaiDoTuoi" class="meta-item"><span class="age-badge" :class="ageBadgeClass(m.phanLoaiDoTuoi)">{{ m.phanLoaiDoTuoi }}</span></span>
                </div>
                <p v-if="m.ngayCongChieu" class="movie-card__date">📅 Khởi chiếu: {{ fmtDateVN(m.ngayCongChieu) }}</p>
                <button class="btn-detail" @click="router.push('/phim/' + m.id)">Xem chi tiết</button>
              </div>
            </div>
          </div>
        </div>

        <div v-show="activeTab === 'gia_ve'" class="tab-panel">
          <p class="price-note">Bảng giá tham khảo — giá thực tế có thể thay đổi theo suất chiếu.</p>
          <div class="price-table-wrap">
            <table class="price-table">
              <thead>
                <tr>
                  <th>Đối tượng</th>
                  <th v-for="d in priceDays" :key="d">{{ d }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in priceRows" :key="row.label">
                  <td>{{ row.label }}</td>
                  <td v-for="(v, i) in row.prices" :key="i">{{ fmtPrice(v) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useBookingStore } from '@/stores/bookingStore'
import { useAuthStore } from '@/stores/authStore'
import ThemeToggle from '@/components/ThemeToggle.vue'
import DayChip from '@/components/DayChip.vue'
import api from '@/services/api'
import { fmtTime12 } from '@/utils/homeHelpers'

const props = defineProps({ id: { type: [String, Number], required: true } })
const router = useRouter()
const bookingStore = useBookingStore()
const authStore = useAuthStore()

const cinema = ref(null)
const loadingCinema = ref(false)
const cinemaError = ref('')
const schedules = ref([])
const loadingSchedules = ref(false)
const selectedShows = reactive({})
const openDays = reactive({})
const sapChieuMovies = ref([])
const loadingSapChieu = ref(false)

const activeTab = ref('dang_chieu')
const DOW = ['CN','T2','T3','T4','T5','T6','T7']

const days = computed(() => {
  const out = []
  const now = new Date()
  const baseY = now.getFullYear(), baseM = now.getMonth(), baseD = now.getDate()
  for (let i = 0; i < 2; i++) {
    const d = new Date(baseY, baseM, baseD + i)
    const y = d.getFullYear()
    const mo = String(d.getMonth() + 1).padStart(2, '0')
    const dy = String(d.getDate()).padStart(2, '0')
    out.push({ iso: `${y}-${mo}-${dy}`, num: d.getDate(), mon: Number(mo), dow: DOW[d.getDay()] })
  }
  return out
})

const mapUrl = computed(() => {
  if (!cinema.value) return '#'
  if (cinema.value.banDoUrl) return cinema.value.banDoUrl
  if (cinema.value.latitude != null && cinema.value.longitude != null)
    return `https://maps.google.com/?q=${cinema.value.latitude},${cinema.value.longitude}`
  return `https://maps.google.com/?q=${encodeURIComponent(cinema.value.diaChi || cinema.value.tenRap)}`
})

const pageTitle = computed(() => ({
  dang_chieu: 'PHIM ĐANG CHIẾU',
  sap_chieu: 'PHIM SẮP CHIẾU',
  gia_ve: 'BẢNG GIÁ VÉ',
}[activeTab.value] || ''))

const dangChieuGroups = computed(() => {
  const filtered = schedules.value.filter(s => s.trangThai === 'dang_chieu')
  const map = new Map()
  for (const s of filtered) {
    if (!map.has(s.phimId)) {
      map.set(s.phimId, {
        phimId: s.phimId, tenPhim: s.tenPhim, posterUrl: s.posterUrl,
        thoiLuong: s.thoiLuong, ngonNgu: s.ngonNgu, phanLoaiDoTuoi: s.phanLoaiDoTuoi,
        diemDanhGia: s.diemDanhGia, theLoai: s.theLoai || [],
        byDay: {},
      })
    }
    const grp = map.get(s.phimId)
    const dateKey = s.thoiGianBatDau?.slice(0, 10)
    if (!dateKey) continue
    if (!grp.byDay[dateKey]) grp.byDay[dateKey] = []
    const now = new Date()
    if (new Date(s.thoiGianBatDau) > now) grp.byDay[dateKey].push(s)
  }
  return [...map.values()]
})

const basePrice = computed(() => {
  const s = schedules.value[0]
  return s?.giaCoBan ?? 80000
})
const priceDays = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN', 'Ngày lễ']
const priceRows = computed(() => {
  const b = Number(basePrice.value)
  const prices = (mult) => [b * mult, b * mult, b * mult, b * mult, b * mult * 1.2, b * mult * 1.2, b * mult * 1.2, b * mult * 1.5]
  return [
    { label: 'Người lớn', prices: prices(1) },
    { label: 'Học sinh – SV', prices: prices(0.8) },
    { label: 'Trẻ em', prices: prices(0.7) },
    { label: 'Người cao tuổi', prices: prices(0.75) },
  ]
})

async function loadCinema() {
  loadingCinema.value = true; cinemaError.value = ''
  try {
    const res = await api.get(`/rap-chieu/${props.id}`)
    cinema.value = res.data
  } catch (e) {
    cinemaError.value = e.response?.data?.message || 'Không tải được thông tin rạp'
  } finally { loadingCinema.value = false }
}

async function loadSchedules() {
  loadingSchedules.value = true
  try {
    const tu = days.value[0].iso
    const den = days.value[days.value.length - 1].iso
    const res = await api.get(`/lich-chieu/rap/${props.id}`, { params: { tu, den } })
    schedules.value = Array.isArray(res.data) ? res.data : []
  } catch { schedules.value = [] }
  finally { loadingSchedules.value = false }
}

async function loadSapChieu() {
  loadingSapChieu.value = true
  try {
    const res = await api.get('/phim/sap-chieu')
    sapChieuMovies.value = Array.isArray(res.data) ? res.data : []
  } catch { sapChieuMovies.value = [] }
  finally { loadingSapChieu.value = false }
}

function ageBadgeClass(r) {
  if (!r || r === 'P' || r === 'K') return 'age-badge--green'
  if (r.includes('13')) return 'age-badge--yellow'
  return 'age-badge--red'
}

function toggleDay(mg, iso) {
  const key = `${mg.phimId}:${iso}`
  openDays[key] = openDays[key] !== false
}

function formatGroups(list) {
  const map = new Map()
  for (const s of list || []) {
    const key = s.tenDinhDang || s.loaiPhong || 'Suất chiếu'
    if (!map.has(key)) map.set(key, [])
    map.get(key).push(s)
  }
  return [...map.entries()].map(([label, shows]) => ({
    label,
    shows: [...shows].sort((a, b) => String(a.thoiGianBatDau).localeCompare(String(b.thoiGianBatDau))),
  }))
}

function selectShow(mg, s) {
  const key = `m${mg.phimId}`
  if (selectedShows[key]?.lichChieuId === s.lichChieuId) {
    delete selectedShows[key]
    return
  }
  for (const k of Object.keys(selectedShows)) delete selectedShows[k]
  selectedShows[key] = s
}

function goBook(s) {
  if (!s) return
  if (!authStore.isLoggedIn) {
    authStore.setRedirectPath(`/seat-selection/${s.lichChieuId}`)
    router.push('/auth')
    return
  }
  bookingStore.setMovie({
    id: s.phimId, title: s.tenPhim, poster: s.posterUrl,
    duration: s.thoiLuong, ageRating: s.phanLoaiDoTuoi, language: s.ngonNgu,
  })
  bookingStore.setShowtime({
    id: s.lichChieuId, phimId: s.phimId,
    phongChieuId: s.phongChieuId, tenPhong: s.tenPhong, loaiPhong: s.loaiPhong,
    rapChieuId: cinema.value?.id, tenRap: cinema.value?.tenRap, thanhPho: cinema.value?.thanhPho,
    tenDinhDang: s.tenDinhDang,
    thoiGianBatDau: s.thoiGianBatDau, thoiGianKetThuc: s.thoiGianKetThuc,
    giaCoBan: s.giaCoBan,
    phongChieu: { id: s.phongChieuId, tenPhong: s.tenPhong, loaiPhong: s.loaiPhong },
  })
  router.push(`/seat-selection/${s.lichChieuId}`)
}

function fmtTime(dt) {
  return fmtTime12(dt)
}
function fmtTime24(dt, fallback = '—') {
  if (!dt) return fallback
  const d = new Date(dt)
  if (isNaN(d.getTime())) return fallback
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
function fmtPrice(v) {
  if (v == null) return '—'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}
function fmtDateVN(d) {
  if (!d) return '—'
  try {
    const dt = new Date(d)
    return `${String(dt.getDate()).padStart(2,'0')}/${String(dt.getMonth()+1).padStart(2,'0')}/${dt.getFullYear()}`
  } catch { return d }
}

onMounted(async () => {
  await Promise.all([loadCinema(), loadSchedules(), loadSapChieu()])
})
</script>

<style scoped>
.cinema-detail { background: #0d1233; color: #ffffff; min-height: 100vh; padding-bottom: 80px; font-family: var(--font-ui, 'Inter', sans-serif); }

.top-bar { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; background: rgba(8, 12, 38, 0.92); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border-bottom: 1px solid rgba(255,255,255,0.1); position: sticky; top: 0; z-index: 60; }
.top-bar__title { font-size: 16px; font-weight: 700; color: #ffffff; font-family: var(--font-display, 'Playfair Display', Georgia, serif); }
.icon-btn { width: 44px; height: 44px; border-radius: 6px; border: 1px solid rgba(255,255,255,0.14); background: rgba(255,255,255,0.06); color: #ffd400; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: background 0.2s, border-color 0.2s; }
.icon-btn:hover { background: rgba(255,255,255,0.1); border-color: #ffd400; }
.icon-btn svg { width: 18px; height: 18px; }

.state-box { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 14px; min-height: 60vh; padding: 40px; text-align: center; color: #c7cbe4; }
.state-box--error { color: #fca5a5; }
.state-box--small { min-height: 100px; padding: 24px; }
.spinner { width: 46px; height: 46px; border: 3px solid rgba(255,255,255,0.12); border-top-color: #ffd400; border-radius: 50%; animation: spin 0.9s linear infinite; }
.spinner--sm { width: 28px; height: 28px; border-width: 2px; }
@keyframes spin { to { transform: rotate(360deg); } }
.btn-retry { padding: 10px 26px; background: #ffd400; color: #0d1233; border: none; border-radius: 6px; font-weight: 700; cursor: pointer; }

.hero { position: relative; min-height: 380px; background: #0a0e2e center/cover no-repeat; display: flex; align-items: flex-end; overflow: hidden; }
.hero__overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, rgba(10,14,46,0.2) 0%, rgba(10,14,46,0.65) 60%, #0d1233 100%); }
.hero__body { position: relative; z-index: 2; display: flex; align-items: flex-end; gap: 24px; padding: 0 28px 40px; width: 100%; max-width: 1100px; margin: 0 auto; }
.hero__placeholder { font-size: 64px; flex-shrink: 0; }
.hero__info { color: #ffffff; }
.cinema-title { font-family: var(--font-display, 'Playfair Display', Georgia, serif); font-size: clamp(24px, 4vw, 48px); font-weight: 700; margin: 0 0 8px; line-height: 1.1; letter-spacing: -0.02em; }
.cinema-city, .cinema-addr, .cinema-phone { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #c7cbe4; margin: 0 0 4px; }
.btn-map { display: inline-flex; align-items: center; gap: 6px; margin-top: 14px; padding: 9px 18px; border-radius: 6px; background: #ffd400; color: #0d1233; text-decoration: none; font-size: 13px; font-weight: 700; transition: background 0.2s; }
.btn-map:hover { background: #ffdf40; }

.page-wrap { max-width: 1280px; margin: 0 auto; padding: 28px 20px; }

.tabs-bar { display: flex; width: 100%; border-bottom: 2px solid rgba(255,255,255,0.16); margin-bottom: 30px; }
.tab-btn { flex: 1; padding: 14px 10px; background: none; border: none; border-bottom: 4px solid transparent; margin-bottom: -2px; font-size: 15px; font-weight: 700; color: #ffffff; cursor: pointer; transition: color 0.2s, border-color 0.2s; font-family: var(--font-ui, 'Inter', sans-serif); text-transform: uppercase; letter-spacing: 0.04em; }
.tab-btn:hover { color: #ffd400; }
.tab-btn--active { color: #ffd400; border-bottom-color: #ffd400; }

.tab-head { display: flex; flex-direction: column; align-items: center; gap: 8px; margin-bottom: 30px; }
.page-title { font-family: var(--font-display, 'Playfair Display', Georgia, serif); font-size: clamp(28px, 4.5vw, 46px); font-weight: 800; color: #ffffff; margin: 0; text-align: center; letter-spacing: 0.02em; text-transform: uppercase; }
.tab-hint { font-size: 12.5px; color: rgba(255,255,255,0.5); text-align: center; }

.tab-panel { min-height: 200px; }

.no-show { padding: 28px; text-align: center; color: rgba(255,255,255,0.5); font-size: 14px; }

.movie-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 28px; }
.movie-card { display: flex; background: linear-gradient(180deg, #5a4fb8 0%, #4A3FA5 100%); border-radius: 16px; overflow: hidden; box-shadow: 0 18px 40px rgba(0,0,0,0.35); }
.movie-card__poster { flex: 0 0 300px; width: 300px; background: #2c275f; }
.movie-card__poster img { width: 100%; height: 100%; object-fit: cover; display: block; }
.movie-card__poster-fallback { width: 100%; height: 100%; min-height: 260px; display: flex; align-items: center; justify-content: center; font-size: 48px; background: #2c275f; }
.movie-card__info { flex: 1; padding: 24px 22px; display: flex; flex-direction: column; min-width: 0; }
.movie-card__title { font-family: var(--font-display, 'Playfair Display', Georgia, serif); font-size: 26px; font-weight: 800; color: #ffffff; margin: 0 0 12px; line-height: 1.15; }
.movie-card__meta { display: flex; flex-wrap: wrap; align-items: center; column-gap: 12px; row-gap: 8px; margin-bottom: 10px; }
.meta-item { font-size: 13px; font-weight: 600; color: rgba(255,255,255,0.92); display: inline-flex; align-items: center; white-space: nowrap; }
.meta-item + .meta-item::before { content: '•'; margin-right: 12px; color: rgba(255,255,255,0.45); }
.age-badge { padding: 3px 8px; border-radius: 5px; font-size: 10px; font-weight: 900; color: #fff; }
.age-badge--green { background: rgba(34,197,94,0.85); }
.age-badge--yellow { background: rgba(234,179,8,0.85); }
.age-badge--red { background: rgba(239,68,68,0.85); }
.movie-card__rating { display: flex; align-items: center; gap: 4px; font-size: 14px; font-weight: 700; color: #ffffff; margin-bottom: 14px; }
.movie-card__date { font-size: 13px; color: rgba(255,255,255,0.85); margin: 0 0 14px; }

.date-acc-list { display: flex; flex-direction: column; gap: 10px; }
.date-acc { border: 1px solid rgba(255,255,255,0.22); border-radius: 10px; overflow: hidden; background: rgba(0,0,0,0.18); }
.date-acc--open { border-color: #ffd400; }
.date-acc__head { width: 100%; display: flex; align-items: center; gap: 12px; padding: 10px 12px; background: transparent; border: none; cursor: pointer; color: #ffffff; }
.date-acc__head:hover { background: rgba(255,255,255,0.06); }
.date-acc__chip { flex-shrink: 0; }
.date-acc__count { flex: 1; text-align: left; font-size: 13px; font-weight: 700; color: rgba(255,255,255,0.85); }
.date-acc__chev { flex-shrink: 0; color: rgba(255,255,255,0.6); transition: transform 0.2s; }
.date-acc--open .date-acc__chev { transform: rotate(180deg); }
.date-acc__body { padding: 6px 12px 14px; }
.fmt-group { margin-top: 12px; }
.fmt-group__label { display: flex; align-items: center; gap: 10px; font-size: 12px; font-weight: 800; color: #ffd400; text-transform: uppercase; letter-spacing: 0.08em; margin: 0 0 10px; }
.fmt-group__label::after { content: ''; flex: 1; height: 1px; background: rgba(255,212,0,0.25); }
.fmt-group__times { display: grid; grid-template-columns: repeat(auto-fill, minmax(78px, 1fr)); gap: 10px; }
.time-btn { padding: 10px 4px; border-radius: 6px; border: 1.5px solid #ffffff; background: transparent; color: #ffffff; font-size: 15px; font-weight: 800; text-align: center; white-space: nowrap; font-family: var(--font-ui, 'Inter', sans-serif); cursor: pointer; transition: background 0.15s, color 0.15s, border-color 0.15s; }
.time-btn:hover { border-color: #ffd400; color: #ffd400; }
.time-btn--active { background: #ffd400; border-color: #ffd400; color: #0d1233; }
.time-btn--active:hover { color: #0d1233; }

.selected-bar { margin-top: 14px; display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 10px 12px; border-radius: 10px; background: rgba(255,212,0,0.12); border: 1px solid rgba(255,212,0,0.45); }
.selected-bar__info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.selected-bar__time { font-size: 13px; font-weight: 700; color: #ffffff; }
.selected-bar__price { font-size: 12px; font-weight: 700; color: #ffd400; }
.btn-book { flex-shrink: 0; padding: 9px 18px; border-radius: 6px; border: none; background: #ffd400; color: #0d1233; font-size: 13px; font-weight: 800; font-family: var(--font-ui, 'Inter', sans-serif); cursor: pointer; transition: filter 0.15s; }
.btn-book:hover { filter: brightness(1.08); }

.btn-detail { align-self: flex-start; margin-top: auto; padding: 10px 22px; border-radius: 6px; background: #ffd400; border: none; color: #0d1233; font-size: 13px; font-weight: 800; cursor: pointer; transition: filter 0.15s; font-family: var(--font-ui, 'Inter', sans-serif); }
.btn-detail:hover { filter: brightness(1.08); }

.price-note { font-size: 12px; color: rgba(255,255,255,0.5); margin-bottom: 16px; font-style: italic; }
.price-table-wrap { overflow-x: auto; }
.price-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.price-table th, .price-table td { padding: 10px 14px; border: 1px solid rgba(255,255,255,0.16); text-align: center; }
.price-table th { background: rgba(255,255,255,0.1); color: #ffffff; font-weight: 700; font-size: 11px; text-transform: uppercase; letter-spacing: 0.04em; }
.price-table td { color: #c7cbe4; }
.price-table td:first-child { text-align: left; color: #ffffff; font-weight: 600; }

@media (max-width: 900px) {
  .movie-grid { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .page-wrap { padding: 20px 14px; }
  .hero__body { flex-direction: column; padding: 0 16px 32px; }
  .movie-card { flex-direction: column; }
  .movie-card__poster { flex: none; width: 100%; }
  .movie-card__poster img { aspect-ratio: 16/10; object-fit: cover; height: auto; }
  .movie-card__poster-fallback { min-height: 140px; }
  .movie-card__info { padding: 18px 16px; }
  .movie-card__title { font-size: 22px; }
  .selected-bar { flex-direction: column; align-items: stretch; }
  .btn-book { width: 100%; }
}
@media (max-width: 480px) {
  .tabs-bar { gap: 0; }
  .tab-btn { padding: 12px 8px; font-size: 13px; }
  .price-table { font-size: 11px; }
  .price-table th, .price-table td { padding: 7px 8px; }
}
</style>

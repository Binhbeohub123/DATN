import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useMovieStore = defineStore('movie', () => {
  // ── State ──
  const phimDangChieu = ref([])
  const phimSapChieu  = ref([])
  const phimNoiBat    = ref([])   // top-10 by rating — Phase 3
  const cinemas       = ref([])   // RapChieu list — Phase 3
  const currentMovie  = ref(null)
  const lichChieu     = ref([])
  const banners       = ref([])

  const loading = ref({
    dangChieu: false,
    sapChieu:  false,
    noiBat:    false,
    cinemas:   false,
    detail:    false,
    lichChieu: false,
    banner:    false,
  })

  const error = ref({
    dangChieu: '',
    sapChieu:  '',
    noiBat:    '',
    cinemas:   '',
    detail:    '',
    lichChieu: '',
    banner:    '',
  })

  // ── Getters ──
  const activeBanner = computed(() => {
    if (banners.value.length > 0) return banners.value[0]
    return null
  })

  // ── Actions ──

  async function fetchDangChieu() {
    loading.value.dangChieu = true
    error.value.dangChieu = ''
    try {
      const res = await api.get('/phim/dang-chieu')
      phimDangChieu.value = normalizeMovies(res.data)
    } catch {
      error.value.dangChieu = 'Không tải được phim đang chiếu'
    } finally {
      loading.value.dangChieu = false
    }
  }

  async function fetchSapChieu() {
    loading.value.sapChieu = true
    error.value.sapChieu = ''
    try {
      const res = await api.get('/phim/sap-chieu')
      phimSapChieu.value = normalizeMovies(res.data)
    } catch {
      error.value.sapChieu = 'Không tải được phim sắp chiếu'
    } finally {
      loading.value.sapChieu = false
    }
  }

  /** GET /api/phim/noi-bat — top 10 now-showing sorted by rating DESC */
  async function fetchNoiBat() {
    loading.value.noiBat = true
    error.value.noiBat = ''
    try {
      const res = await api.get('/phim/noi-bat')
      phimNoiBat.value = normalizeMovies(res.data)
    } catch {
      error.value.noiBat = 'Không tải được phim nổi bật'
    } finally {
      loading.value.noiBat = false
    }
  }

  /** GET /api/rap-chieu — active cinemas with lat/lng/city */
  async function fetchCinemas() {
    loading.value.cinemas = true
    error.value.cinemas = ''
    try {
      const res = await api.get('/rap-chieu')
      cinemas.value = (res.data || []).map(r => ({
        id:        r.id,
        tenRap:    r.tenRap    || '',
        diaChi:    r.diaChi    || '',
        thanhPho:  r.thanhPho  || '',
        hinhAnh:   r.hinhAnh   || '',
        banDoUrl:  r.banDoUrl  || '',
        latitude:  r.latitude  != null ? Number(r.latitude)  : null,
        longitude: r.longitude != null ? Number(r.longitude) : null,
      }))
    } catch {
      error.value.cinemas = 'Không tải được danh sách rạp'
    } finally {
      loading.value.cinemas = false
    }
  }

  async function fetchMovieById(id) {
    loading.value.detail = true
    error.value.detail = ''
    try {
      const res = await api.get(`/phim/${id}`)
      console.log('Movie API Response:', res.data)
      currentMovie.value = normalizeMovie(res.data)
      console.log('Normalized Movie:', currentMovie.value)
      return currentMovie.value
    } catch (err) {
      console.error('fetchMovieById Error:', err.response?.data || err.message)
      error.value.detail = 'Không tải được thông tin phim'
      return null
    } finally {
      loading.value.detail = false
    }
  }

  async function fetchLichChieu(phimId, ngay) {
    loading.value.lichChieu = true
    error.value.lichChieu = ''
    try {
      const params = ngay ? { ngay } : {}
      const res = await api.get(`/phim/${phimId}/lich-chieu`, { params })
      console.log('LichChieu API Response:', res.data)

      // Normalize LichChieuResponse DTO to include phongChieu object
      const normalized = (res.data || []).map(lc => ({
        ...lc,
        phongChieu: lc.phongChieuId ? {
          id: lc.phongChieuId,
          tenPhong: lc.tenPhong,
          loaiPhong: lc.loaiPhong
        } : null
      }))

      lichChieu.value = normalized
      console.log('Processed LichChieu:', lichChieu.value)
      return lichChieu.value
    } catch (err) {
      console.error('fetchLichChieu Error:', err.response?.data || err.message)
      error.value.lichChieu = 'Không tải được lịch chiếu'
      return []
    } finally {
      loading.value.lichChieu = false
    }
  }

  async function fetchBanners() {
    loading.value.banner = true
    error.value.banner = ''
    try {
      const res = await api.get('/banner')
      const data = res.data
      const raw = Array.isArray(data) ? data : data ? [data] : []
      banners.value = raw.map(b => ({
        id:          b.id,
        tieuDe:      b.tieuDe  || b.title       || '',
        hinhAnh:     b.hinhAnh || b.image       || '',
        linkUrl:     b.linkUrl || b.link        || '',
        thuTu:       b.thuTu   || 0,
        moTa:        b.moTa    || b.description || '',
        ngayBatDau:  b.ngayBatDau  || null,
        ngayKetThuc: b.ngayKetThuc || null,
      }))
    } catch {
      error.value.banner = 'Không tải được banner'
    } finally {
      loading.value.banner = false
    }
  }

  async function searchMovies(query) {
    try {
      const res = await api.get('/phim/search', { params: { q: query } })
      return normalizeMovies(res.data)
    } catch {
      return []
    }
  }

  // ── Helpers ──
  function normalizeMovies(data) {
    if (!Array.isArray(data)) return []
    return data.map(normalizeMovie)
  }

  function normalizeMovie(m) {
    if (!m) return null
    return {
      id:          m.id,
      title:       m.tenPhim          || m.title       || '',
      titleEn:     m.tenPhimTiengAnh  || m.titleEn     || '',
      theLoais:    Array.isArray(m.theLoais) ? m.theLoais : [],
      genre:       Array.isArray(m.theLoais) && m.theLoais.length > 0
                     ? m.theLoais.map(t => t.tenTheLoai).join(', ')
                     : (m.genre || ''),
      director:    m.daoDien          || m.director    || '',
      cast:        m.dienVienChinh    || m.cast        || '',
      duration:    m.thoiLuong        || m.duration    || 0,
      language:    m.ngonNgu          || m.language    || '',
      ageRating:   m.phanLoaiDoTuoi   || m.ageRating   || 'P',
      poster:      m.posterUrl        || m.poster      || '',
      trailer:     m.trailerUrl       || m.trailer     || '',
      description: m.moTa             || m.description || '',
      rating:      m.diemDanhGia      || m.rating      || 0,
      ratingCount: m.soLuongDanhGia   || m.ratingCount || 0,
      status:      m.trangThai        || m.status      || '',
      releaseDate: m.ngayCongChieu    || m.releaseDate || '',
    }
  }

  return {
    phimDangChieu,
    phimSapChieu,
    phimNoiBat,
    cinemas,
    currentMovie,
    lichChieu,
    banners,
    loading,
    error,
    activeBanner,
    fetchDangChieu,
    fetchSapChieu,
    fetchNoiBat,
    fetchCinemas,
    fetchMovieById,
    fetchLichChieu,
    fetchBanners,
    searchMovies,
  }
})

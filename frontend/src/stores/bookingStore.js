import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import api from '@/services/api'

// ── sessionStorage snapshot (sống sót qua F5, chết khi đóng tab) ──
const SNAPSHOT_KEY = 'booking-snapshot'

function readSnapshotRaw() {
  try {
    const raw = sessionStorage.getItem(SNAPSHOT_KEY)
    const snap = raw ? JSON.parse(raw) : null
    return (snap && snap.lichChieuId) ? snap : null
  } catch { return null }
}

// Xoá snapshot mà không cần instance store (dùng được ở bất kỳ đâu)
export function clearBookingSnapshot() {
  try { sessionStorage.removeItem(SNAPSHOT_KEY) } catch { /* private mode */ }
}

// Đọc nhẹ xem có snapshot đáng kể nào không (không tạo store instance)
export function hasBookingSnapshot() {
  return !!readSnapshotRaw()
}

export const useBookingStore = defineStore('booking', () => {
  // ── State ──
  const selectedMovie = ref(null)
  const selectedShowtime = ref(null)
  const selectedSeats = ref([])
  const selectedCombos = ref([])
  const promoCode = ref('')
  const promoData = ref(null)
  const paymentMethod = ref('PayOS')
  const useLoyaltyPoints = ref(false)
  const loyaltyPointsToUse = ref(0)

  const loading = ref({
    seats: false,
    products: false,
    promo: false,
    booking: false,
  })

  const error = ref({
    seats: '',
    products: '',
    promo: '',
    booking: '',
  })

  // ── Snapshot persistence (F5 recovery) ─────────────────────
  // Watcher duy nhất ghi lại toàn bộ thay đổi state — không rải gọi tay.
  // Vue watcher flush async nên các state trung gian trong 1 tick không bao giờ
  // được ghi xuống storage (chỉ bản cuối cùng của tick được lưu).
  function writeSnapshot() {
    try {
      const st = selectedShowtime.value
      if (!st || !st.id) return // chưa có ngữ cảnh suất thì không ghi
      sessionStorage.setItem(SNAPSHOT_KEY, JSON.stringify({
        lichChieuId: st.id,
        movie: selectedMovie.value
          ? { id: selectedMovie.value.id, title: selectedMovie.value.title }
          : null,
        showtime: st, // object phẳng nhỏ (id, tenPhong, thoiGianBatDau, phongChieu...)
        gheNgoiIds: selectedSeats.value.map(s => s.id),
        combos: selectedCombos.value.map(c => ({ id: c.id, soLuong: c.soLuong })),
        ts: Date.now(),
      }))
    } catch { /* quota/private mode */ }
  }

  function clearSnapshot() {
    clearBookingSnapshot()
  }

  watch(
    [selectedMovie, selectedShowtime, selectedSeats, selectedCombos],
    () => writeSnapshot(),
    { deep: true }
  )

  /**
   * Khôi phục showtime/movie meta sau F5 (header "Phim / Suất / Phòng").
   * Ưu tiên dữ liệu display trong snapshot; fallback object tối thiểu { id }.
   */
  function hydrateShowtimeMeta(lichChieuId) {
    const snap = readSnapshotRaw()
    if (snap && snap.lichChieuId === lichChieuId && snap.showtime) {
      setShowtime({ ...snap.showtime, id: lichChieuId })
      if (snap.movie) setMovie(snap.movie)
    } else {
      setShowtime({ id: lichChieuId })
    }
  }

  /**
   * Khôi phục toàn bộ ngữ cảnh đặt vé từ snapshot.
   * @param {{verify?: boolean}} opts — verify=true: kiểm tra server rằng TOÀN BỘ
   *   ghế vẫn đang bị chính user này khoá (qua GET /lich-chieu/{id}/locked-seats);
   *   ghế không còn lock bị loại bỏ. Trả về mảng gheNgoiId còn giữ được,
   *   hoặc null nếu snapshot không dùng được / không còn ghế nào.
   */
  async function hydrateFromSnapshot({ verify = false } = {}) {
    const snap = readSnapshotRaw()
    if (!snap || !Array.isArray(snap.gheNgoiIds) || snap.gheNgoiIds.length === 0) return null

    let kept = [...snap.gheNgoiIds]
    try {
      if (verify) {
        const res = await api.get(`/lich-chieu/${snap.lichChieuId}/locked-seats`)
        const mineIds = new Set((res.data?.myLockedSeatIds || []).map(m => m.gheNgoiId))
        kept = kept.filter(id => mineIds.has(id))
        if (kept.length === 0) {
          clearBookingSnapshot()
          return null
        }
      }

      // Showtime/movie meta (nếu store chưa có hoặc khác suất)
      if (!selectedShowtime.value || selectedShowtime.value.id !== snap.lichChieuId) {
        hydrateShowtimeMeta(snap.lichChieuId)
      }

      // Ghế: cần object đầy đủ (hangGhe/soGhe/loaiGhe/giaTien) cho nhãn + giá
      let seatObjs = []
      try {
        seatObjs = (await api.get(`/lich-chieu/${snap.lichChieuId}/ghe-trong`)).data || []
      } catch { /* sơ đồ tạm thời lỗi — vẫn giữ ids đã verify */ }
      const byId = new Map(seatObjs.map(s => [s.id, s]))
      kept.forEach(gid => {
        const seat = byId.get(gid)
        if (seat) addSeat(seat) // addSeat tự dedup theo id
      })

      // Combo: chỉ nhận sản phẩm vẫn còn bán; bỏ qua item lạ
      if (
        Array.isArray(snap.combos) && snap.combos.length > 0 &&
        selectedCombos.value.length === 0
      ) {
        try {
          const prods = (await api.get('/san-pham')).data || []
          const pmap = new Map(prods.map(p => [p.id, p]))
          snap.combos.forEach(c => {
            const p = pmap.get(c.id)
            if (p && c.soLuong > 0) addCombo(p, c.soLuong)
          })
        } catch { /* danh sách sản phẩm lỗi — bỏ qua combo */ }
      }

      writeSnapshot() // đồng bộ snapshot với phần còn giữ được
      return kept
    } catch {
      return null
    }
  }


  // ── Getters ──
  const totalSeatPrice = computed(() => {
    return selectedSeats.value.reduce((sum, seat) => sum + (seat.giaTien || 0), 0)
  })

  const totalComboPrice = computed(() => {
    return selectedCombos.value.reduce((sum, combo) => sum + (combo.giaTien * combo.soLuong), 0)
  })

  const subtotal = computed(() => totalSeatPrice.value + totalComboPrice.value)

  const promoDiscount = computed(() => {
    if (!promoData.value) return 0

    // Prefer the pre-computed discountAmount returned by the backend validate endpoint.
    // This avoids any NaN/null arithmetic on the raw percent/fixed fields.
    const precomputed = promoData.value.discountAmount
    if (precomputed != null && !isNaN(Number(precomputed))) {
      return Math.min(Number(precomputed), subtotal.value)
    }

    // Fallback: re-compute locally from raw promo fields
    const rate    = Number(promoData.value.giaTriGiam)     ?? 0
    const maxDisc = promoData.value.giaTriGiamToiDa != null
                      ? Number(promoData.value.giaTriGiamToiDa)
                      : Infinity

    if (promoData.value.loaiGiamGia === 'percent') {
      const pct = (subtotal.value * rate) / 100
      return Math.min(pct, isFinite(maxDisc) ? maxDisc : pct)
    }
    // fixed amount
    return Math.min(rate, subtotal.value)
  })

  const pointsDiscount = computed(() => {
    // 1 điểm = 100đ
    return loyaltyPointsToUse.value * 100
  })

  const totalPrice = computed(() => {
    return Math.max(0, subtotal.value - promoDiscount.value - pointsDiscount.value)
  })

  // ── Actions ──
  function setMovie(movie) {
    selectedMovie.value = movie
  }

  function setShowtime(showtime) {
    selectedShowtime.value = showtime
  }

  function addSeat(seat) {
    if (selectedSeats.value.length >= 8) {
      error.value.seats = 'Tối đa 8 ghế/lần đặt'
      return false
    }
    if (!selectedSeats.value.find(s => s.id === seat.id)) {
      selectedSeats.value.push(seat)
      error.value.seats = ''
      return true
    }
    return false
  }

  function removeSeat(seatId) {
    selectedSeats.value = selectedSeats.value.filter(s => s.id !== seatId)
  }

  function clearSeats() {
    selectedSeats.value = []
  }

  function addCombo(product, quantity = 1) {
    const existing = selectedCombos.value.find(c => c.id === product.id)
    if (existing) {
      existing.soLuong += quantity
    } else {
      selectedCombos.value.push({
        ...product,
        soLuong: quantity,
        giaTien: product.gia,
      })
    }
  }

  function updateComboQuantity(productId, quantity) {
    const combo = selectedCombos.value.find(c => c.id === productId)
    if (combo) {
      if (quantity <= 0) {
        removeCombo(productId)
      } else {
        combo.soLuong = quantity
      }
    }
  }

  function removeCombo(productId) {
    selectedCombos.value = selectedCombos.value.filter(c => c.id !== productId)
  }

  function clearCombos() {
    selectedCombos.value = []
  }

  async function validatePromo(code) {
    if (!code) {
      promoCode.value = ''
      promoData.value = null
      error.value.promo = ''
      return true
    }

    loading.value.promo = true
    error.value.promo = ''
    try {
      // Send tongTien so the backend can compute the actual discountAmount.
      // Without it, percent-type promos return discountAmount = 0 (0 * rate / 100).
      const res = await api.post('/khuyen-mai/validate', {
        maKhuyenMai: code,
        tongTien:    subtotal.value,
      })

      // Backend returns { valid, discountAmount, tenKhuyenMai, loaiGiamGia, giaTriGiam, ... }
      if (!res.data?.valid) {
        error.value.promo = res.data?.message || 'Mã không hợp lệ'
        promoCode.value = ''
        promoData.value = null
        return false
      }

      promoCode.value = code
      promoData.value = res.data
      return true
    } catch (err) {
      error.value.promo = err.response?.data?.message || 'Mã khuyến mãi không hợp lệ'
      promoCode.value = ''
      promoData.value = null
      return false
    } finally {
      loading.value.promo = false
    }
  }

  function clearPromo() {
    promoCode.value = ''
    promoData.value = null
    error.value.promo = ''
  }

  function setPaymentMethod(method) {
    paymentMethod.value = method
  }

  function setUseLoyaltyPoints(use, points = 0) {
    useLoyaltyPoints.value = use
    loyaltyPointsToUse.value = points
  }

  async function createBooking() {
    if (!selectedMovie.value || !selectedShowtime.value || selectedSeats.value.length === 0) {
      error.value.booking = 'Vui lòng chọn phim, suất chiếu và ghế'
      return null
    }

    loading.value.booking = true
    error.value.booking = ''
    try {
      const bookingData = {
        lichChieuId: selectedShowtime.value.id,
        gheIds: selectedSeats.value.map(s => s.id),
        comboData: selectedCombos.value.map(c => ({ id: c.id, soLuong: c.soLuong })),
        maKhuyenMai: promoCode.value || null,
        diemSuDung: useLoyaltyPoints.value ? loyaltyPointsToUse.value : 0,
      }

      const res = await api.post('/dat-ve', bookingData)
      return res.data
    } catch (err) {
      error.value.booking = err.response?.data?.message || 'Lỗi khi tạo đơn đặt vé'
      return null
    } finally {
      loading.value.booking = false
    }
  }

  function clearBooking() {
    selectedMovie.value = null
    selectedShowtime.value = null
    selectedSeats.value = []
    selectedCombos.value = []
    promoCode.value = ''
    promoData.value = null
    paymentMethod.value = 'PayOS'
    useLoyaltyPoints.value = false
    loyaltyPointsToUse.value = 0
    error.value = { seats: '', products: '', promo: '', booking: '' }
    clearBookingSnapshot() // payment success / reset chủ động → hết phiên
  }

  return {
    selectedMovie,
    selectedShowtime,
    selectedSeats,
    selectedCombos,
    promoCode,
    promoData,
    paymentMethod,
    useLoyaltyPoints,
    loyaltyPointsToUse,
    loading,
    error,
    totalSeatPrice,
    totalComboPrice,
    subtotal,
    promoDiscount,
    pointsDiscount,
    totalPrice,
    setMovie,
    setShowtime,
    addSeat,
    removeSeat,
    clearSeats,
    addCombo,
    updateComboQuantity,
    removeCombo,
    clearCombos,
    validatePromo,
    clearPromo,
    setPaymentMethod,
    setUseLoyaltyPoints,
    createBooking,
    clearBooking,
    clearSnapshot,
    hydrateShowtimeMeta,
    hydrateFromSnapshot,
  }
})

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

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
  }
})

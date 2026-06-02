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
  const paymentMethod = ref('VNPay')
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
    if (promoData.value.loaiGiamGia === 'percent') {
      return Math.min(
        (subtotal.value * promoData.value.giaTriGiam) / 100,
        promoData.value.giaTriGiamToiDa || Infinity
      )
    }
    return Math.min(promoData.value.giaTriGiam, subtotal.value)
  })

  const pointsDiscount = computed(() => {
    return loyaltyPointsToUse.value * 1000
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
      const res = await api.post('/khuyen-mai/validate', { maKhuyenMai: code })
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
    paymentMethod.value = 'VNPay'
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

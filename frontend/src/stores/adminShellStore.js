import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAdminShellStore = defineStore('adminShell', () => {
  const searchQuery = ref('')
  const filterDate = ref(toIsoDate(new Date()))
  const pendingCount = ref(0)
  const notifications = ref([])
  const loadingNotif = ref(false)
  const showNotifPanel = ref(false)
  const showDatePanel = ref(false)

  /** Set by AdminDashboard when user runs search — child pages watch this */
  const searchTargetPage = ref(null)
  const searchTick = ref(0)
  const ticketStatusFilter = ref('')

  const formattedFilterDate = computed(() => {
    try {
      const [y, m, d] = filterDate.value.split('-').map(Number)
      const dt = new Date(y, m - 1, d)
      return dt.toLocaleDateString('vi-VN', {
        weekday: 'long',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
      })
    } catch {
      return filterDate.value
    }
  })

  function toIsoDate(d) {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }

  function closePanels() {
    showNotifPanel.value = false
    showDatePanel.value = false
  }

  function toggleNotifPanel() {
    showDatePanel.value = false
    showNotifPanel.value = !showNotifPanel.value
    if (showNotifPanel.value) refreshNotifications()
  }

  function toggleDatePanel() {
    showNotifPanel.value = false
    showDatePanel.value = !showDatePanel.value
  }

  async function refreshPendingCount() {
    try {
      const { data } = await api.get('/admin/stats')
      pendingCount.value = Number(data?.pendingBookings) || 0
    } catch {
      pendingCount.value = 0
    }
  }

  async function refreshNotifications() {
    loadingNotif.value = true
    try {
      await refreshPendingCount()
      const { data } = await api.get('/admin/dat-ve', { params: { page: 0, size: 100 } })
      const list = data?.content || []
      const isPending = (b) =>
        b.trangThai === 'pending' || b.trangThaiThanhToan === 'unpaid'
      const pending = list.filter(isPending)
      const recent = list
        .filter((b) => !isPending(b))
        .slice(0, 5)
      notifications.value = [...pending, ...recent].slice(0, 12).map(mapNotif)
    } catch {
      notifications.value = []
    } finally {
      loadingNotif.value = false
    }
  }

  function mapNotif(b) {
    const email = b.nguoiDung?.email || b.email || 'Khách'
    const phim = b.lichChieu?.phim?.tenPhim || b.tenPhim || '—'
    const pending =
      b.trangThai === 'pending' || b.trangThaiThanhToan === 'unpaid'
    return {
      id: b.id,
      maDatVe: b.maDatVe,
      title: pending ? 'Chờ thanh toán' : 'Đặt vé mới',
      subtitle: `${email} · ${phim}`,
      pending,
      time: b.ngayTao,
    }
  }

  /** Pick admin page + bump tick so children apply search */
  function applyGlobalSearch() {
    const q = searchQuery.value.trim()
    if (!q) {
      searchTargetPage.value = null
      searchTick.value += 1
      return null
    }

    let page = 'movies'
    if (q.includes('@')) page = 'customers'
    else if (/^dv|pc|ticket/i.test(q) || /^[A-Z0-9-]{6,}$/i.test(q)) page = 'tickets'
    else if (/^\d{4}-\d{2}-\d{2}$/.test(q)) {
      filterDate.value = q
      page = 'schedule'
    }

    if (page !== 'tickets') ticketStatusFilter.value = ''
    searchTargetPage.value = page
    searchTick.value += 1
    return page
  }

  function applyFilterDate(dateStr) {
    if (dateStr) filterDate.value = dateStr
    showDatePanel.value = false
    searchTargetPage.value = 'schedule'
    searchTick.value += 1
    return 'schedule'
  }

  function openTicketFromNotif(n) {
    searchQuery.value = n.maDatVe || ''
    ticketStatusFilter.value = n.pending ? 'pending' : ''
    searchTargetPage.value = 'tickets'
    searchTick.value += 1
    closePanels()
  }

  function goToPendingTickets() {
    searchQuery.value = ''
    ticketStatusFilter.value = 'pending'
    searchTargetPage.value = 'tickets'
    searchTick.value += 1
    closePanels()
  }

  return {
    searchQuery,
    filterDate,
    formattedFilterDate,
    pendingCount,
    notifications,
    loadingNotif,
    showNotifPanel,
    showDatePanel,
    searchTargetPage,
    searchTick,
    ticketStatusFilter,
    openTicketFromNotif,
    goToPendingTickets,
    closePanels,
    toggleNotifPanel,
    toggleDatePanel,
    refreshPendingCount,
    refreshNotifications,
    applyGlobalSearch,
    applyFilterDate,
    toIsoDate,
  }
})

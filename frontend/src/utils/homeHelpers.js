export const cinemas = [
  { id: 1, name: 'PolyCinema Quận 1', address: '135 Lê Thánh Tôn, Q.1, TP.HCM' },
  { id: 2, name: 'PolyCinema Thủ Đức', address: 'Vincom Thủ Đức, TP.HCM' },
  { id: 3, name: 'PolyCinema Quận 7', address: 'SC VivoCity, Q.7, TP.HCM' },
]

export const getAgeClass = (rating) => {
  if (!rating) return 'badge-green'
  if (rating === 'P' || rating === 'G') return 'badge-green'
  if (String(rating).includes('13')) return 'badge-yellow'
  return 'badge-red'
}

// 12-hour clock display matching the admin SchedulePage (e.g. "12:27 AM", "03:05 PM")
export const fmtTime12 = (dt, fallback = '—') => {
  if (!dt) return fallback
  const d = new Date(dt)
  const m = String(d.getMinutes()).padStart(2, '0')
  let h = d.getHours()
  const ampm = h < 12 ? 'AM' : 'PM'
  h = h % 12 || 12
  return `${String(h).padStart(2, '0')}:${m} ${ampm}`
}

export const fmtDateTime12 = (dt, fallback = '—') => {
  if (!dt) return fallback
  const d = new Date(dt)
  const dd = String(d.getDate()).padStart(2, '0')
  const mo = String(d.getMonth() + 1).padStart(2, '0')
  return `${dd}/${mo} ${fmtTime12(dt)}`
}

export const fmtDateTimeFull12 = (dt, fallback = '—') => {
  if (!dt) return fallback
  const d = new Date(dt)
  const dd = String(d.getDate()).padStart(2, '0')
  const mo = String(d.getMonth() + 1).padStart(2, '0')
  return `${dd}/${mo}/${d.getFullYear()} ${fmtTime12(dt)}`
}

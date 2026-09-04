export function fmtDate(d) {
  if (!d) return '—'
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return String(d)
  const dd = String(dt.getDate()).padStart(2, '0')
  const mm = String(dt.getMonth() + 1).padStart(2, '0')
  return `${dd}/${mm}/${dt.getFullYear()}`
}

export function fmtTime12(d, fallback = '—') {
  if (!d) return fallback
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return fallback
  let h = dt.getHours()
  const m = String(dt.getMinutes()).padStart(2, '0')
  const ampm = h >= 12 ? 'PM' : 'AM'
  h = h % 12 || 12
  return `${String(h).padStart(2, '0')}:${m} ${ampm}`
}

export function fmtDateTime12(d, fallback = '—') {
  if (!d) return fallback
  const dt = new Date(d)
  if (isNaN(dt.getTime())) return fallback
  return `${fmtDate(dt)} ${fmtTime12(d)}`
}

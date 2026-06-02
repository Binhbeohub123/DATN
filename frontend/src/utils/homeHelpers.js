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

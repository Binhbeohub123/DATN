// Full round-trip timezone simulation
console.log('=== FULL ROUND-TRIP SIMULATION (UTC+7) ===')
console.log('Timezone:', Intl.DateTimeFormat().resolvedOptions().timeZone, 'offset:', new Date().getTimezoneOffset(), 'min')
console.log()

// Step 1: Admin enters 14:00 on 2026-07-22
const date = '2026-07-22', startTime = '14:00', durationMin = 120
const start = new Date(`${date}T${startTime}`)
const end = new Date(start.getTime() + durationMin*60000)
console.log(`Admin enters: ${date} ${startTime} (local)`)
console.log('JS Date start:', start.toString())

// Step 2: Frontend builds payload (EXACTLY as save() does)
const payload = {
  thoiGianBatDau: start.toISOString().slice(0,19),
  thoiGianKetThuc: end.toISOString().slice(0,19),
}
console.log('Payload sent to backend:', JSON.stringify(payload))
console.log('  Note: this is UTC time with Z stripped!')

// Step 3: Backend stores LocalDateTime (no timezone)
console.log()
console.log('Backend stores: thoiGianBatDau = ' + payload.thoiGianBatDau)

// Step 4: Backend returns same value, frontend re-parses
const returned = payload.thoiGianBatDau
const reparsed = new Date(returned)
const h = reparsed.getHours()
const m = String(reparsed.getMinutes()).padStart(2,'0')
console.log(`Frontend re-parses: new Date("${returned}") => ${h}:${m}`)
console.log()
console.log(`RESULT: Admin entered 14:00, sees ${h}:${m}`)
console.log(`TIME SHIFTED BY: ${14 - h} hours (= timezone offset)`)
console.log()

// Step 5: Overlap scenario
console.log('=== OVERLAP FALSE-NEGATIVE SCENARIO ===')
console.log('Existing showtime in Room R1 (stored): 07:00-09:00')
console.log('Admin sees it in table as: 07:00-09:00')
console.log('Admin enters 08:00 to overlap it')
const overlapStart = new Date('2026-07-22T08:00')
const overlapEnd = new Date(overlapStart.getTime() + 120*60000)
const overlapPayloadStart = overlapStart.toISOString().slice(0,19)
const overlapPayloadEnd = overlapEnd.toISOString().slice(0,19)
console.log(`Payload sent: ${overlapPayloadStart} to ${overlapPayloadEnd}`)
console.log(`Backend checks: ${overlapPayloadStart} vs existing 07:00-09:00`)
const existStart = new Date('2026-07-22T07:00:00')  // stored in DB
const existEnd = new Date('2026-07-22T09:00:00')  // stored in DB
const sentStart = new Date(overlapPayloadStart)
const sentEnd = new Date(overlapPayloadEnd)
// Use the STORED (UTC-ish) times for comparison, not local
const conflicts = overlapPayloadStart < '2026-07-22T09:00:00' && overlapPayloadEnd > '2026-07-22T07:00:00'
console.log(`Overlap detected? ${conflicts}`)
console.log(`Actual sent start (UTC): ${overlapPayloadStart} = ${overlapStart.getHours()-7}:00 UTC`)
console.log(`Existing range (stored): 07:00-09:00`)
console.log(`01:00 < 09:00? ${overlapPayloadStart < '2026-07-22T09:00:00'}`)
console.log(`03:00 > 07:00? ${overlapPayloadEnd > '2026-07-22T07:00:00'}`)
console.log()
console.log('Since 01:00-03:00 does NOT overlap 07:00-09:00,')
console.log('the save SUCCEEDS and stores 01:00-03:00.')
console.log('Table then shows 01:00, NOT the 08:00 the admin entered.')
console.log('Admin sees: "the system shifted my time to avoid the conflict!"')

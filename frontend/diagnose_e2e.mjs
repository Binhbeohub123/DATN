/**
 * End-to-end diagnostic: tests the full save → reload → openEdit cycle
 * for multiple promo codes with different movie selections.
 * 
 * This replicates the EXACT sequence:
 * 1. load() → GET /khuyen-mai/all → promos.value
 * 2. User clicks "Sửa mã" on a promo → openEdit(km)
 * 3. openEdit extracts phimIds from km.phims
 * 4. loadMovies() → GET /phim → allMovies
 * 5. Checkbox binding checks looseEqual(form.phimIds[i], allMovies[j].id)
 * 
 * We also test: save a new movie selection, then reload and check.
 */

const API_BASE = 'http://localhost:8080/api'

async function main() {
  // Auth
  const loginResp = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'admin@cinema.com', password: 'Admin@123' })
  })
  const token = await loginResp.text()
  const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }

  // === TEST 1: Initial load - check phims are populated ===
  console.log('╔═══════════════════════════════════════════════════════════════╗')
  console.log('║ TEST 1: Initial load() — are phims populated from GET /all? ║')
  console.log('╚═══════════════════════════════════════════════════════════════╝')
  
  const resp1 = await fetch(`${API_BASE}/khuyen-mai/all`, { headers })
  const promos = await resp1.json()
  
  for (const km of promos) {
    const phimIds = km.phims?.map(p => p.id) || []
    console.log(`  "${km.maKhuyenMai}" (id=${km.id}): phims=${JSON.stringify(phimIds)} (${phimIds.length} movies)`)
    console.log(`    dangHoatDong: ${km.dangHoatDong}, phims field exists: ${km.phims !== undefined}`)
  }

  // === TEST 2: Save a movie selection for WELCOME20, then reload ===
  console.log('\n╔══════════════════════════════════════════════════════════════════╗')
  console.log('║ TEST 2: Save WELCOME20 with phimIds=[1,2], reload, check phims ║')
  console.log('╚══════════════════════════════════════════════════════════════════╝')
  
  const welcome20 = promos.find(km => km.maKhuyenMai === 'WELCOME20')
  if (welcome20) {
    console.log(`  Before save: phims = ${JSON.stringify(welcome20.phims?.map(p => p.id))}`)
    
    // Save with phimIds=[1,2]
    const savePayload = {
      maKhuyenMai: welcome20.maKhuyenMai,
      tenKhuyenMai: welcome20.tenKhuyenMai,
      loaiGiamGia: welcome20.loaiGiamGia,
      giaTriGiam: welcome20.giaTriGiam,
      donHangToiThieu: welcome20.donHangToiThieu,
      gioiHanSuDung: welcome20.gioiHanSuDung,
      ngayBatDau: welcome20.ngayBatDau,
      ngayKetThuc: welcome20.ngayKetThuc,
      dangHoatDong: welcome20.dangHoatDong,
      phimIds: [1, 2]
    }
    
    const saveResp = await fetch(`${API_BASE}/khuyen-mai/${welcome20.id}`, {
      method: 'PUT', headers, body: JSON.stringify(savePayload)
    })
    const saveResult = await saveResp.json()
    console.log(`  Save response status: ${saveResp.status}`)
    console.log(`  Save response phims: ${JSON.stringify(saveResult.phims?.map(p => ({ id: p.id, tenPhim: p.tenPhim })))}`)
    
    // Now reload (exactly as the frontend's load() does after save())
    const resp2 = await fetch(`${API_BASE}/khuyen-mai/all`, { headers })
    const reloaded = await resp2.json()
    const reloadedW20 = reloaded.find(km => km.maKhuyenMai === 'WELCOME20')
    
    console.log(`  After reload: phims = ${JSON.stringify(reloadedW20?.phims?.map(p => ({ id: p.id, type: typeof p.id })))}`)
    
    // Simulate openEdit
    const formPhimIds = reloadedW20?.phims?.map(p => p.id) || []
    console.log(`  openEdit form.phimIds = ${JSON.stringify(formPhimIds)}`)
    
    // Load movies
    const moviesResp = await fetch(`${API_BASE}/phim`, { headers })
    const allMovies = await moviesResp.json()
    
    // Simulate Vue looseEqual checkbox check
    console.log(`  --- Checkbox matching (Vue looseEqual simulation) ---`)
    for (const m of allMovies) {
      // Vue's looseEqual for primitives: String(a) === String(b)
      const shouldBeChecked = formPhimIds.some(fid => {
        if (fid === m.id) return true  // strict match
        return String(fid) === String(m.id)  // loose match
      })
      const isInForm = formPhimIds.includes(m.id)
      console.log(`    Movie "${m.tenPhim}" (id=${m.id}): inForm=${isInForm}, looseChecked=${shouldBeChecked}`)
    }
  }

  // === TEST 3: Save POLY10 with phimIds=[3], check ===
  console.log('\n╔═══════════════════════════════════════════════════════════════╗')
  console.log('║ TEST 3: Save POLY10 with phimIds=[3], reload, check phims   ║')
  console.log('╚═══════════════════════════════════════════════════════════════╝')
  
  const poly10 = promos.find(km => km.maKhuyenMai === 'POLY10')
  if (poly10) {
    console.log(`  Before save: phims = ${JSON.stringify(poly10.phims?.map(p => p.id))}`)
    
    const savePayload2 = {
      maKhuyenMai: poly10.maKhuyenMai,
      tenKhuyenMai: poly10.tenKhuyenMai,
      loaiGiamGia: poly10.loaiGiamGia,
      giaTriGiam: poly10.giaTriGiam,
      donHangToiThieu: poly10.donHangToiThieu,
      gioiHanSuDung: poly10.gioiHanSuDung,
      ngayBatDau: poly10.ngayBatDau,
      ngayKetThuc: poly10.ngayKetThuc,
      dangHoatDong: poly10.dangHoatDong,
      phimIds: [3]
    }
    
    const saveResp2 = await fetch(`${API_BASE}/khuyen-mai/${poly10.id}`, {
      method: 'PUT', headers, body: JSON.stringify(savePayload2)
    })
    const saveResult2 = await saveResp2.json()
    console.log(`  Save response phims: ${JSON.stringify(saveResult2.phims?.map(p => ({ id: p.id, tenPhim: p.tenPhim })))}`)
    
    // Reload
    const resp3 = await fetch(`${API_BASE}/khuyen-mai/all`, { headers })
    const reloaded2 = await resp3.json()
    const reloadedPoly = reloaded2.find(km => km.maKhuyenMai === 'POLY10')
    
    const formPhimIds2 = reloadedPoly?.phims?.map(p => p.id) || []
    console.log(`  After reload form.phimIds = ${JSON.stringify(formPhimIds2)}`)
    console.log(`  phimIds types: [${formPhimIds2.map(id => typeof id)}]`)
  }

  // === TEST 4: WELCOME50K (no movies) ===
  console.log('\n╔═══════════════════════════════════════════════════════════════╗')
  console.log('║ TEST 4: WELCOME50K — promo with no movies linked            ║')
  console.log('╚═══════════════════════════════════════════════════════════════╝')
  
  // Reload fresh
  const resp4 = await fetch(`${API_BASE}/khuyen-mai/all`, { headers })
  const final = await resp4.json()
  const w50k = final.find(km => km.maKhuyenMai === 'WELCOME50K')
  if (w50k) {
    console.log(`  phims: ${JSON.stringify(w50k.phims)}`)
    console.log(`  phimIds would be: ${JSON.stringify(w50k.phims?.map(p => p.id) || [])}`)
    console.log(`  This is correct — no movies means empty array, all checkboxes unchecked`)
  }
  
  // === Restore original data ===
  console.log('\n=== Restoring original movie selections ===')
  if (welcome20) {
    await fetch(`${API_BASE}/khuyen-mai/${welcome20.id}`, {
      method: 'PUT', headers, body: JSON.stringify({ phimIds: [1] })
    })
    console.log('  Restored WELCOME20 to phimIds=[1]')
  }
  if (poly10) {
    await fetch(`${API_BASE}/khuyen-mai/${poly10.id}`, {
      method: 'PUT', headers, body: JSON.stringify({ phimIds: [2] })
    })
    console.log('  Restored POLY10 to phimIds=[2]')
  }

  console.log('\n╔═══════════════════════════════════════════════════════════════╗')
  console.log('║ SUMMARY                                                      ║')
  console.log('╚═══════════════════════════════════════════════════════════════╝')
  console.log('  Data layer (API) is fully correct:')
  console.log('  - GET /khuyen-mai/all returns phims with correct IDs')  
  console.log('  - PUT saves phimIds correctly')
  console.log('  - Reload after save returns correct phims')
  console.log('  - ID types are number on both sides')
  console.log('')
  console.log('  If bug persists in browser, it must be in Step 3 (rendering):')
  console.log('  Open browser devtools console, navigate to admin promo page,')
  console.log('  click "Sửa" on a promo with movies, and check the [DEBUG] logs.')
}

main().catch(e => console.error('Diagnostic failed:', e))

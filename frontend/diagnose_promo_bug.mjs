/**
 * Diagnostic script: simulates the exact frontend data flow for the promo edit modal.
 * 
 * Reproduces the openEdit() logic:
 * 1. Fetches promos from GET /khuyen-mai/all  (same as load())
 * 2. Fetches movies from GET /phim             (same as loadMovies())
 * 3. For each promo that has phims, extracts phimIds = km.phims.map(p => p.id)
 * 4. Checks whether each phimId matches any allMovies[i].id using ===
 * 5. Reports type mismatches or other issues
 */

const API_BASE = 'http://localhost:8080/api'

async function main() {
  // 1. Authenticate
  console.log('=== Step 0: Authenticate ===')
  const loginResp = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'admin@cinema.com', password: 'Admin@123' })
  })
  const token = await loginResp.text()
  console.log('Token obtained:', token.substring(0, 30) + '...')
  const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }

  // 2. Load promos (same as frontend load())
  console.log('\n=== Step 1: GET /khuyen-mai/all (same as frontend load()) ===')
  const promosResp = await fetch(`${API_BASE}/khuyen-mai/all`, { headers })
  const promos = await promosResp.json()
  console.log(`Loaded ${promos.length} promos`)

  for (const km of promos) {
    console.log(`\n  Promo "${km.maKhuyenMai}" (id=${km.id}):`)
    console.log(`    dangHoatDong: ${km.dangHoatDong}`)
    console.log(`    phims count: ${km.phims?.length ?? 'null/undefined'}`)
    if (km.phims && km.phims.length > 0) {
      for (const p of km.phims) {
        console.log(`    phim id=${p.id} (typeof=${typeof p.id}) tenPhim="${p.tenPhim}"`)
      }
    }
  }

  // 3. Load movies (same as frontend loadMovies())
  console.log('\n=== Step 2: GET /phim (same as frontend loadMovies()) ===')
  const moviesResp = await fetch(`${API_BASE}/phim`, { headers })
  const allMovies = await moviesResp.json()
  console.log(`Loaded ${allMovies.length} movies`)

  for (const m of allMovies) {
    console.log(`  Movie id=${m.id} (typeof=${typeof m.id}) tenPhim="${m.tenPhim}"`)
  }

  // 4. Simulate openEdit for each promo that has phims
  console.log('\n=== Step 3: Simulate openEdit() for each promo with phims ===')
  for (const km of promos) {
    if (!km.phims || km.phims.length === 0) {
      console.log(`\n  [SKIP] Promo "${km.maKhuyenMai}": no phims linked`)
      continue
    }

    console.log(`\n  [EDIT] Promo "${km.maKhuyenMai}":`)

    // This is the exact line from openEdit():
    const phimIds = km.phims?.map(p => p.id) || []
    console.log(`    form.phimIds = ${JSON.stringify(phimIds)}`)
    console.log(`    form.phimIds types = [${phimIds.map(id => typeof id).join(', ')}]`)

    // Check checkbox binding: Vue v-model with array uses looseEqual
    // The checkbox :value="m.id" for each movie m in allMovies
    console.log(`    --- Checkbox matching simulation ---`)
    for (const movieId of phimIds) {
      const matchingMovie = allMovies.find(m => m.id === movieId)
      const looseMatch = allMovies.find(m => String(m.id) === String(movieId))
      
      console.log(`    phimId=${movieId} (typeof=${typeof movieId}):`)
      console.log(`      strict === match in allMovies: ${matchingMovie ? 'YES (id=' + matchingMovie.id + ')' : 'NO'}`)
      console.log(`      loose string match in allMovies: ${looseMatch ? 'YES (id=' + looseMatch.id + ')' : 'NO'}`)
      
      if (!matchingMovie && looseMatch) {
        console.log(`      ⚠️  TYPE MISMATCH DETECTED: phimId typeof=${typeof movieId}, allMovies.id typeof=${typeof looseMatch.id}`)
      }
      if (!matchingMovie && !looseMatch) {
        console.log(`      ❌ NO MATCH AT ALL — movie ID ${movieId} not found in allMovies!`)
      }
    }
  }

  // 5. Check: does saving and re-loading preserve the data?
  console.log('\n=== Step 4: Verify phims survive a full round-trip ===')
  const promoWithPhims = promos.find(km => km.phims && km.phims.length > 0)
  if (promoWithPhims) {
    console.log(`Testing with promo "${promoWithPhims.maKhuyenMai}" (id=${promoWithPhims.id})`)
    
    // Re-fetch the same promo's data from the all-list
    const reloadResp = await fetch(`${API_BASE}/khuyen-mai/all`, { headers })
    const reloaded = await reloadResp.json()
    const samePromo = reloaded.find(km => km.id === promoWithPhims.id)
    
    console.log(`  Original phims: ${JSON.stringify(promoWithPhims.phims?.map(p => p.id))}`)
    console.log(`  Reloaded phims: ${JSON.stringify(samePromo?.phims?.map(p => p.id))}`)
    console.log(`  Data consistent: ${JSON.stringify(promoWithPhims.phims?.map(p => p.id)) === JSON.stringify(samePromo?.phims?.map(p => p.id))}`)
  }

  console.log('\n=== Diagnosis complete ===')
}

main().catch(e => console.error('Diagnostic failed:', e))

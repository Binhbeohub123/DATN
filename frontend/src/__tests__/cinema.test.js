/**
 * Task 3.2 — Unit test for cinema.css shared pattern library
 *
 * Strategy (mirrors tokens.test.js):
 *   1. Read cinema.css from disk with fs.readFileSync.
 *   2. Parse individual CSS rule blocks with lightweight regex helpers.
 *   3. Assert that each required selector exists and contains the expected
 *      declarations as specified in tasks.md task 3.1.
 *
 * Validates: Requirements — cinema.css pattern library (task 3.1 / task 3.2)
 */

import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

// ── Locate cinema.css relative to this test file ──────────────────────────────

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const CINEMA_PATH = path.resolve(__dirname, '../assets/cinema.css')

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Extract the raw declaration block for a given CSS selector string.
 * Handles simple selectors like `.glass-card`, `.btn-bib`, `.chip-glass`, etc.
 * Returns the inner text between { … } of the FIRST matching rule, or null.
 */
function getRuleBlock(cssText, selector) {
  // Escape special regex characters in the selector
  const escaped = selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  // Match the selector followed by optional whitespace and a { … } block
  // Use a non-greedy match up to the first closing brace
  const re = new RegExp(escaped + '\\s*\\{([^}]+)\\}')
  const m = cssText.match(re)
  return m ? m[1] : null
}

/**
 * Returns true if the CSS text contains a @supports not (backdrop-filter …)
 * fallback block.
 */
function hasSupportsNotBackdropFilter(cssText) {
  return /@supports\s+not\s+\(backdrop-filter/.test(cssText)
}

// ── Test suite ────────────────────────────────────────────────────────────────

describe('cinema.css — shared CSS pattern library verification', () => {
  let cssText

  beforeAll(() => {
    cssText = fs.readFileSync(CINEMA_PATH, 'utf-8')
  })

  // ── File sanity ──────────────────────────────────────────────────────────

  it('cinema.css file is readable and non-empty', () => {
    expect(cssText).toBeTruthy()
    expect(cssText.length).toBeGreaterThan(100)
  })

  // ── 1. .glass-card — backdrop-filter ────────────────────────────────────

  it('.glass-card rule exists in cinema.css', () => {
    const block = getRuleBlock(cssText, '.glass-card')
    expect(block, '.glass-card rule block should exist').not.toBeNull()
  })

  it('.glass-card rule includes backdrop-filter declaration', () => {
    const block = getRuleBlock(cssText, '.glass-card')
    expect(block).not.toBeNull()
    expect(block).toMatch(/backdrop-filter\s*:/)
  })

  it('.glass-card rule has background and border-radius declarations', () => {
    const block = getRuleBlock(cssText, '.glass-card')
    expect(block).not.toBeNull()
    expect(block).toMatch(/background\s*:/)
    expect(block).toMatch(/border-radius\s*:/)
  })

  // ── 2. .glass-card--heavy — own backdrop-filter ──────────────────────────

  it('.glass-card--heavy rule exists in cinema.css', () => {
    const block = getRuleBlock(cssText, '.glass-card--heavy')
    expect(block, '.glass-card--heavy rule block should exist').not.toBeNull()
  })

  it('.glass-card--heavy rule has its own backdrop-filter declaration', () => {
    const block = getRuleBlock(cssText, '.glass-card--heavy')
    expect(block).not.toBeNull()
    expect(block).toMatch(/backdrop-filter\s*:/)
  })

  // ── 3. .btn-bib — outline-offset ────────────────────────────────────────

  it('.btn-bib rule exists in cinema.css', () => {
    const block = getRuleBlock(cssText, '.btn-bib')
    expect(block, '.btn-bib rule block should exist').not.toBeNull()
  })

  it('.btn-bib rule contains outline-offset declaration', () => {
    const block = getRuleBlock(cssText, '.btn-bib')
    expect(block).not.toBeNull()
    expect(block).toMatch(/outline-offset\s*:/)
  })

  // ── 4. .chip-glass — border-radius: var(--radius-pill) ──────────────────

  it('.chip-glass rule exists in cinema.css', () => {
    const block = getRuleBlock(cssText, '.chip-glass')
    expect(block, '.chip-glass rule block should exist').not.toBeNull()
  })

  it('.chip-glass rule contains border-radius: var(--radius-pill)', () => {
    const block = getRuleBlock(cssText, '.chip-glass')
    expect(block).not.toBeNull()
    expect(block).toMatch(/border-radius\s*:\s*var\(--radius-pill\)/)
  })

  // ── 5. .spinner-cinema — animation: cinema-spin ──────────────────────────

  it('.spinner-cinema rule exists in cinema.css', () => {
    const block = getRuleBlock(cssText, '.spinner-cinema')
    expect(block, '.spinner-cinema rule block should exist').not.toBeNull()
  })

  it('.spinner-cinema rule contains animation: cinema-spin', () => {
    const block = getRuleBlock(cssText, '.spinner-cinema')
    expect(block).not.toBeNull()
    expect(block).toMatch(/animation\s*:.*cinema-spin/)
  })

  // ── 6. .reveal — translateY(24px) ────────────────────────────────────────

  it('.reveal rule exists in cinema.css', () => {
    const block = getRuleBlock(cssText, '.reveal')
    expect(block, '.reveal rule block should exist').not.toBeNull()
  })

  it('.reveal rule contains translateY(24px)', () => {
    const block = getRuleBlock(cssText, '.reveal')
    expect(block).not.toBeNull()
    expect(block).toMatch(/translateY\(24px\)/)
  })

  // ── 7. @supports not (backdrop-filter) fallback ──────────────────────────

  it('@supports not (backdrop-filter …) fallback block exists in cinema.css', () => {
    expect(hasSupportsNotBackdropFilter(cssText)).toBe(true)
  })

  it('@supports fallback block contains .glass-card fallback rule', () => {
    // The fallback block should mention .glass-card and a background override
    const supportsIdx = cssText.indexOf('@supports not (backdrop-filter')
    expect(supportsIdx).toBeGreaterThan(-1)
    const supportsBlock = cssText.slice(supportsIdx)
    expect(supportsBlock).toMatch(/\.glass-card/)
    expect(supportsBlock).toMatch(/background\s*:/)
  })
})

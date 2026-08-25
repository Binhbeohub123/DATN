import { describe, it, expect } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))
const src = readFileSync(join(__dirname, '..', 'admin', 'components', 'CinemasPage.vue'), 'utf-8')

describe('CinemasPage — seat type "trống" (aisle) support', () => {
  it('bulk toolbar offers the Trống option', () => {
    expect(src.includes('<option value="trống">Trống (lối đi)</option>')).toBe(true)
  })

  it('add-row modal offers the Trống option', () => {
    const modal = src.slice(src.indexOf('addRowForm.loaiGhe'))
    expect(modal.includes('<option value="trống">')).toBe(true)
  })

  it('seatStyle renders trống cells dashed and transparent', () => {
    const fn = src.slice(src.indexOf('function seatStyle'), src.indexOf("// 'thường'"))
    expect(fn.includes("loaiGhe === 'trống'")).toBe(true)
    expect(fn.includes("background: 'transparent'")).toBe(true)
    expect(fn.includes("'dashed'")).toBe(true)
  })

  it('legend shows a Trống / lối đi entry with its own chip class', () => {
    expect(src.includes('Trống / lối đi')).toBe(true)
    expect(src.includes('.chip--trong')).toBe(true)
  })
})

describe('CinemasPage — drag-select multi-seat', () => {
  it('seat chips react to mousedown/mouseenter instead of click', () => {
    expect(src.includes('@mousedown="startDragSeat(seat)"')).toBe(true)
    expect(src.includes('@mouseenter="dragSeatHover(seat)"')).toBe(true)
    expect(src.includes('@click="toggleSeat(seat)"')).toBe(false)
  })

  it('drag mode is decided by the first pressed cell state', () => {
    expect(src.includes("? 'deselect' : 'select'")).toBe(true)
  })

  it('drag ends on a window mouseup listener that is cleaned up', () => {
    expect(src.includes("window.addEventListener('mouseup', stopDragSelect)")).toBe(true)
    expect(src.includes("window.removeEventListener('mouseup', stopDragSelect)")).toBe(true)
    expect(src.includes('onUnmounted')).toBe(true)
  })

  it('seat map disables text selection while dragging', () => {
    expect(src.includes('.seat-map { padding: 24px; user-select: none')).toBe(true)
  })
})

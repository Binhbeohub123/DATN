/** Briefly flash + scroll a table/list row into view after palette navigation.
 *  Used by admin pages that receive a targeted search from the command palette. */
export function flashRow(rowEl) {
  if (!rowEl) return
  rowEl.classList.remove('row-flash')
  // Force a reflow so re-triggering the CSS animation actually restarts it.
  void rowEl.offsetWidth
  rowEl.classList.add('row-flash')
  setTimeout(() => rowEl.classList.remove('row-flash'), 2400)
  try {
    rowEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
  } catch {
    /* scrollIntoView options unsupported — ignore */
  }
}

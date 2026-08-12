import { reactive } from 'vue'

const state = reactive({
  toasts: [],
})

let nextId = 0

export function useToast() {
  function show(message, type = 'info', duration = 3500) {
    // Deduplicate: if an identical message+type toast is already showing,
    // reset its timer instead of stacking a duplicate
    const existing = state.toasts.find(t => t.message === message && t.type === type)
    if (existing) {
      // Clear old timer and restart
      clearTimeout(existing._timer)
      existing._timer = setTimeout(() => {
        state.toasts = state.toasts.filter(t => t.id !== existing.id)
      }, duration)
      return
    }

    const id = ++nextId
    const entry = { id, message, type, _timer: null }
    entry._timer = setTimeout(() => {
      state.toasts = state.toasts.filter(t => t.id !== id)
    }, duration)
    state.toasts.push(entry)
  }

  function success(msg, dur) { show(msg, 'success', dur) }
  function error(msg, dur)   { show(msg, 'error',   dur) }
  function info(msg, dur)    { show(msg, 'info',    dur) }
  function warn(msg, dur)    { show(msg, 'warn',    dur) }

  return { state, show, success, error, info, warn }
}

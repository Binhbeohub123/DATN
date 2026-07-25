import { reactive } from 'vue'

const state = reactive({
  toasts: [],
})

let nextId = 0

export function useToast() {
  function show(message, type = 'info', duration = 3500) {
    const id = ++nextId
    state.toasts.push({ id, message, type })
    setTimeout(() => {
      state.toasts = state.toasts.filter(t => t.id !== id)
    }, duration)
  }

  function success(msg, dur) { show(msg, 'success', dur) }
  function error(msg, dur)   { show(msg, 'error',   dur) }
  function info(msg, dur)    { show(msg, 'info',    dur) }
  function warn(msg, dur)    { show(msg, 'warn',    dur) }

  return { state, show, success, error, info, warn }
}

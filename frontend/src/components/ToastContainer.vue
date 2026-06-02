<template>
  <teleport to="body">
    <div class="toast-wrap" aria-live="polite">
      <transition-group name="toast">
        <div
          v-for="t in state.toasts"
          :key="t.id"
          :class="['toast', `toast--${t.type}`]"
          @click="dismiss(t.id)"
        >
          <span class="toast-icon">{{ icons[t.type] }}</span>
          <span class="toast-msg">{{ t.message }}</span>
          <button class="toast-close" aria-label="Đóng">✕</button>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<script setup>
import { useToast } from '@/composables/useToast'

const { state } = useToast()

const icons = {
  success: '✓',
  error:   '⚠',
  warn:    '⚠',
  info:    'ℹ',
}

function dismiss(id) {
  state.toasts = state.toasts.filter(t => t.id !== id)
}
</script>

<style scoped>
.toast-wrap {
  position: fixed; top: 20px; right: 20px; z-index: 9999;
  display: flex; flex-direction: column; gap: 10px;
  max-width: min(360px, calc(100vw - 40px));
  pointer-events: none;
}
.toast {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 13px 16px; border-radius: 11px;
  font-size: 13px; font-weight: 700;
  box-shadow: 0 8px 24px rgba(0,0,0,.25);
  cursor: pointer; pointer-events: all;
  backdrop-filter: blur(8px);
}
.toast--success { background: #14532d; color: #bbf7d0; border: 1px solid rgba(74,222,128,.3); }
.toast--error   { background: #7f1d1d; color: #fecaca; border: 1px solid rgba(239,68,68,.3); }
.toast--warn    { background: #78350f; color: #fed7aa; border: 1px solid rgba(251,146,60,.3); }
.toast--info    { background: #1e3a5f; color: #bae6fd; border: 1px solid rgba(56,189,248,.3); }
.toast-icon  { font-size: 16px; flex-shrink: 0; margin-top: 1px; }
.toast-msg   { flex: 1; line-height: 1.4; }
.toast-close { background: none; border: none; color: inherit; opacity: .6;
  cursor: pointer; font-size: 13px; padding: 0; margin-left: 4px; flex-shrink: 0; }
.toast-close:hover { opacity: 1; }

.toast-enter-active { transition: all .3s ease; }
.toast-leave-active { transition: all .25s ease; }
.toast-enter-from   { opacity: 0; transform: translateX(20px) scale(.96); }
.toast-leave-to     { opacity: 0; transform: translateX(20px) scale(.96); }
</style>

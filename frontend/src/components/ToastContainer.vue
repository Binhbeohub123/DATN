<template>
  <teleport to="body">
    <div class="toast-wrap" aria-live="polite">
      <transition-group name="toast">
        <div
          v-for="t in state.toasts"
          :key="t.id"
          :class="['toast', `toast--${t.type}`]"
          @click="dismiss(t.id)"
          role="alert"
        >
          <span class="toast-icon" aria-hidden="true">{{ icons[t.type] }}</span>
          <span class="toast-msg">{{ t.message }}</span>
          <button class="toast-close" aria-label="Đóng thông báo">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
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
  info:    '⌕',
}

function dismiss(id) {
  state.toasts = state.toasts.filter(t => t.id !== id)
}
</script>

<style scoped>
.toast-wrap {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: min(380px, calc(100vw - 48px));
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  line-height: 1.4;
  cursor: pointer;
  pointer-events: all;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.10);
  box-shadow: 0 8px 24px rgba(0,0,0,0.35), 0 1px 3px rgba(0,0,0,0.25);
  background: rgba(17,24,39,0.92);
  color: #e5e5e5;
}

/* Type-specific left accent bar via border-left */
.toast--success {
  border-left: 3px solid #10B981;
  color: #d1fae5;
}
.toast--error {
  border-left: 3px solid #EF4444;
  color: #fee2e2;
}
.toast--warn {
  border-left: 3px solid #F59E0B;
  color: #fef3c7;
}
.toast--info {
  border-left: 3px solid #29bcea;
  color: #e0f2fe;
}

.toast-icon {
  font-size: 15px;
  font-style: normal;
  flex-shrink: 0;
  width: 20px;
  text-align: center;
  line-height: 1;
}

.toast-msg {
  flex: 1;
  min-width: 0;
  word-break: break-word;
}

.toast-close {
  background: none;
  border: none;
  color: rgba(229,229,229,0.45);
  cursor: pointer;
  padding: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 4px;
  transition: color 150ms, background 150ms;
  min-height: unset;
}
.toast-close:hover {
  color: #e5e5e5;
  background: rgba(255,255,255,0.08);
}

/* Animations */
.toast-enter-active { transition: all 0.25s cubic-bezier(0.34,1.56,0.64,1); }
.toast-leave-active { transition: all 0.2s ease; }
.toast-enter-from {
  opacity: 0;
  transform: translateY(12px) scale(0.96);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(16px) scale(0.96);
}
</style>

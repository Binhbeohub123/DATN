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
  error: '⚠',
  warn: '⚠',
  info: 'ℹ',
}

function dismiss(id) {
  state.toasts = state.toasts.filter(t => t.id !== id)
}
</script>

<style scoped>
.toast-wrap {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: min(360px, calc(100vw - 40px));
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 13px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 700;
  font-family: 'Raleway', sans-serif;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  pointer-events: all;
  border: 1px solid #efefef;
}

.toast--success {
  background: #ffffff;
  color: #166534;
  border-color: #86efac;
}

.toast--error {
  background: #ffffff;
  color: #991b1b;
  border-color: #fca5a5;
}

.toast--warn {
  background: #ffffff;
  color: #92400e;
  border-color: #fcd34d;
}

.toast--info {
  background: #ffffff;
  color: #0c4a6e;
  border-color: #7dd3fc;
}

.toast-icon {
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.toast-msg {
  flex: 1;
  line-height: 1.4;
}

.toast-close {
  background: none;
  border: none;
  color: inherit;
  opacity: 0.6;
  cursor: pointer;
  font-size: 13px;
  padding: 0;
  margin-left: 4px;
  flex-shrink: 0;
  min-height: auto;
}

.toast-close:hover {
  opacity: 1;
}

.toast-enter-active {
  transition: all 0.3s ease;
}

.toast-leave-active {
  transition: all 0.25s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(20px) scale(0.96);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(20px) scale(0.96);
}
</style>

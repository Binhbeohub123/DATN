<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div
        v-if="isOpen"
        class="modal-overlay"
        @click.self="close"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="'confirm-modal-title'"
      >
        <div class="modal-card">
          <div class="modal-icon" aria-hidden="true">🗺️</div>
          <h3 id="confirm-modal-title" class="modal-title">{{ modalConfig.title }}</h3>
          <p class="modal-desc">{{ modalConfig.description }}</p>
          <div class="modal-actions">
            <button class="btn-cancel" @click="close">{{ modalConfig.cancelLabel }}</button>
            <button class="btn-confirm" @click="confirm">{{ modalConfig.confirmLabel }}</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useConfirmModal } from '../composables/useConfirmModal'

const { isOpen, modalConfig, close, confirm } = useConfirmModal()

const handleKeydown = (e) => {
  if (e.key === 'Escape' && isOpen.value) close()
}

onMounted(() => window.addEventListener('keydown', handleKeydown))
onUnmounted(() => window.removeEventListener('keydown', handleKeydown))
</script>

<style scoped>
/* ── Overlay ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

/* ── Card ── */
.modal-card {
  background: #111827;
  border: 1px solid #374151;
  border-radius: 16px;
  padding: 32px;
  max-width: 400px;
  width: 100%;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.6);
}

/* ── Icon ── */
.modal-icon {
  font-size: 48px;
  margin-bottom: 4px;
  line-height: 1;
}

/* ── Title ── */
.modal-title {
  font-size: 20px;
  font-weight: 700;
  color: #E5E5E5;
  margin: 0;
  line-height: 1.3;
}

/* ── Description ── */
.modal-desc {
  font-size: 14px;
  color: #9CA3AF;
  margin: 0;
  line-height: 1.6;
}

/* ── Actions ── */
.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  width: 100%;
}

.btn-cancel {
  flex: 1;
  padding: 10px 20px;
  border: 1px solid #374151;
  background: transparent;
  color: #9CA3AF;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
  transition: border-color 0.2s ease, color 0.2s ease;
}
.btn-cancel:hover {
  border-color: #FFFFFF;
  color: #FFFFFF;
}

.btn-confirm {
  flex: 1;
  padding: 10px 20px;
  background: #FFFFFF;
  color: #0D0D0D;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.2s ease;
}
.btn-confirm:hover {
  background: #E5E5E5;
}

/* ── Transition: overlay fades, card scales ── */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-active .modal-card,
.modal-fade-leave-active .modal-card {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-card,
.modal-fade-leave-to .modal-card {
  transform: scale(0.9);
  opacity: 0;
}
</style>

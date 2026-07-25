import { ref } from 'vue'

// Module-level singletons — one modal instance shared across the app
const isOpen = ref(false)
const modalConfig = ref({
  title: '',
  description: '',
  confirmLabel: 'Xác nhận',
  cancelLabel: 'Hủy',
  onConfirm: null,
})

export function useConfirmModal() {
  const open = (config) => {
    modalConfig.value = { ...modalConfig.value, ...config }
    isOpen.value = true
  }

  const close = () => {
    isOpen.value = false
  }

  const confirm = () => {
    if (modalConfig.value.onConfirm) modalConfig.value.onConfirm()
    close()
  }

  return { isOpen, modalConfig, open, close, confirm }
}

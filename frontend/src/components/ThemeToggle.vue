<template>
  <button
    type="button"
    class="theme-toggle"
    :title="themeStore.isDark ? 'Chế độ sáng' : 'Chế độ tối'"
    :aria-label="themeStore.isDark ? 'Bật chế độ sáng' : 'Bật chế độ tối'"
    @click="themeStore.toggle()"
  >
    <span class="theme-toggle__icon" aria-hidden="true">{{ themeStore.isDark ? '☀️' : '🌙' }}</span>
    <span v-if="showLabel" class="theme-toggle__label">{{ themeStore.isDark ? 'Sáng' : 'Tối' }}</span>
  </button>
</template>

<script setup>
import { useThemeStore } from '@/stores/themeStore'

defineProps({
  showLabel: { type: Boolean, default: false },
})

const themeStore = useThemeStore()
</script>

<style scoped>
.theme-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 44px;
  min-height: 44px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--surface);
  color: var(--accent);
  font-family: inherit;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, color 0.2s;
}

.theme-toggle:hover {
  border-color: var(--accent);
  background: var(--surface-plain);
}

.theme-toggle__icon {
  font-size: 16px;
  line-height: 1;
}

.theme-toggle__label {
  font-size: 13px;
}
</style>

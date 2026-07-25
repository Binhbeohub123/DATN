import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'poly_theme'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref('light')

  const isDark = computed(() => theme.value === 'dark')

  function apply() {
    if (typeof document === 'undefined') return
    document.documentElement.setAttribute('data-theme', theme.value)
    localStorage.setItem(STORAGE_KEY, theme.value)
  }

  function init() {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved === 'dark' || saved === 'light') {
      theme.value = saved
    } else if (window.matchMedia?.('(prefers-color-scheme: dark)').matches) {
      theme.value = 'dark'
    }
    apply()
  }

  function setTheme(mode) {
    if (mode !== 'light' && mode !== 'dark') return
    theme.value = mode
    apply()
  }

  function toggle() {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  return { theme, isDark, init, setTheme, toggle, apply }
})

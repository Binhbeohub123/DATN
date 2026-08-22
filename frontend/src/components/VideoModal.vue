<template>
  <Teleport to="body">
    <Transition name="vm-fade">
      <div v-if="visible" class="vm-overlay" @click.self="$emit('close')">
        <div class="vm-container">
          <button class="vm-close" @click="$emit('close')" aria-label="Đóng">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
          <div class="vm-video-wrap">
            <iframe
              v-if="embedSrc"
              :src="embedSrc"
              frameborder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowfullscreen
              title="Video trailer"
            ></iframe>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  url: { type: String, default: '' },
})

const emit = defineEmits(['close'])

const embedSrc = computed(() => {
  const url = props.url
  if (!url) return ''
  if (url.includes('youtu.be')) {
    const id = url.split('youtu.be/')[1]?.split('?')[0]
    return id ? `https://www.youtube.com/embed/${id}?autoplay=1` : ''
  }
  if (url.includes('youtube.com')) {
    const v = url.split('v=')[1]?.split('&')[0]
    return v ? `https://www.youtube.com/embed/${v}?autoplay=1` : url
  }
  return url
})

function onEsc(e) {
  if (e.key === 'Escape' && props.visible) emit('close')
}

onMounted(() => document.addEventListener('keydown', onEsc))
onUnmounted(() => document.removeEventListener('keydown', onEsc))

watch(() => props.visible, (val) => {
  document.body.style.overflow = val ? 'hidden' : ''
})
</script>

<style scoped>
.vm-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.85);
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
}
.vm-container {
  position: relative;
  width: 100%; max-width: 900px;
  aspect-ratio: 16/9;
}
.vm-video-wrap {
  width: 100%; height: 100%;
  border-radius: 12px; overflow: hidden;
  background: #000;
}
.vm-video-wrap iframe {
  width: 100%; height: 100%; border: 0;
}
.vm-close {
  position: absolute; top: -40px; right: 0;
  background: none; border: none; color: #fff;
  cursor: pointer; padding: 4px; border-radius: 50%;
  transition: background 0.2s;
}
.vm-close:hover { background: rgba(255,255,255,0.15); }
.vm-fade-enter-active, .vm-fade-leave-active { transition: opacity 0.25s ease; }
.vm-fade-enter-from, .vm-fade-leave-to { opacity: 0; }
</style>

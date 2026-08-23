<template>
  <transition name="gs-fade">
    <div v-if="open" class="gs-overlay" role="dialog" aria-modal="true" aria-label="Tìm kiếm toàn trang" @click.self="close">
      <div class="gs-panel">
        <div class="gs-input-row">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="gs-icon" aria-hidden="true"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input
            ref="inputRef"
            v-model="query"
            type="search"
            class="gs-input"
            placeholder="Tìm phim, rạp chiếu..."
            autocomplete="off"
            @keydown.escape="close"
            @keydown.enter.prevent="goFirstResult"
          />
          <button class="gs-close" @click="close" aria-label="Đóng tìm kiếm">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            <kbd>Esc</kbd>
          </button>
        </div>

        <div class="gs-body">
          <div v-if="loading" class="gs-state">
            <div class="gs-spinner"></div>
            <span>Đang tìm...</span>
          </div>

          <div v-else-if="query.trim() && movieResults.length === 0 && rapResults.length === 0" class="gs-state gs-empty">
            Không tìm thấy phim hoặc rạp phù hợp
          </div>

          <template v-else-if="movieResults.length > 0 || rapResults.length > 0">
            <template v-if="movieResults.length > 0">
              <div class="gs-section-label">🎬 Phim</div>
              <button
                v-for="m in movieResults"
                :key="'phim-' + m.id"
                type="button"
                class="gs-item"
                @click="selectMovie(m)"
              >
                <span class="gs-item__poster">
                  <img v-if="m.poster" :src="m.poster" :alt="''" loading="lazy" />
                  <span v-else class="gs-item__poster-fallback">🎬</span>
                </span>
                <span class="gs-item__main">
                  <span class="gs-item__title">{{ m.title }}</span>
                  <span class="gs-item__sub">{{ m.genre || '—' }}<template v-if="m.duration"> · {{ m.duration }} phút</template></span>
                </span>
                <span v-if="m.status" class="gs-item__badge" :class="{ 'gs-item__badge--soon': m.status === 'sap_chieu' }">
                  {{ m.status === 'dang_chieu' ? 'Đang chiếu' : 'Sắp chiếu' }}
                </span>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="gs-item__go" aria-hidden="true"><polyline points="9 18 15 12 9 6"/></svg>
              </button>
            </template>

            <template v-if="rapResults.length > 0">
              <div class="gs-section-label">🏠 Rạp chiếu</div>
              <button
                v-for="r in rapResults"
                :key="'rap-' + r.id"
                type="button"
                class="gs-item"
                @click="selectRap(r)"
              >
                <span class="gs-item__poster gs-item__poster--wide">
                  <img v-if="r.hinhAnh" :src="r.hinhAnh" :alt="''" loading="lazy" />
                  <span v-else class="gs-item__poster-fallback">🏠</span>
                </span>
                <span class="gs-item__main">
                  <span class="gs-item__title">{{ r.tenRap }}</span>
                  <span class="gs-item__sub">{{ [r.diaChi, r.thanhPho].filter(Boolean).join(', ') || '—' }}</span>
                </span>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="gs-item__go" aria-hidden="true"><polyline points="9 18 15 12 9 6"/></svg>
              </button>
            </template>
          </template>

          <div v-else class="gs-state gs-hint">
            Gõ để tìm phim, rạp chiếu...
            <span class="gs-kbd-hint"><kbd>/</kbd> để mở · <kbd>Esc</kbd> để đóng</span>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useMovieStore } from '@/stores/movieStore'
import api from '@/services/api'

const router = useRouter()
const movieStore = useMovieStore()

const open = ref(false)
const query = ref('')
const loading = ref(false)
const movieResults = ref([])
const rapResults = ref([])
const inputRef = ref(null)
let timer = null

function doOpen() {
  open.value = true
  query.value = ''
  movieResults.value = []
  rapResults.value = []
  loading.value = false
  nextTick(() => inputRef.value?.focus())
}

function close() {
  open.value = false
  query.value = ''
  movieResults.value = []
  rapResults.value = []
  loading.value = false
}

watch(query, (q) => {
  clearTimeout(timer)
  const term = q.trim()
  if (!term) {
    movieResults.value = []
    rapResults.value = []
    loading.value = false
    return
  }
  loading.value = true
  timer = setTimeout(async () => {
    try {
      const [movies, raps] = await Promise.allSettled([
        movieStore.searchMovies(term),
        api.get('/rap-chieu/search', { params: { q: term } }),
      ])
      movieResults.value = movies.status === 'fulfilled' ? (movies.value || []) : []
      rapResults.value = raps.status === 'fulfilled' ? (raps.value?.data || []) : []
    } catch {}
    loading.value = false
  }, 300)
})

function goFirstResult() {
  if (movieResults.value[0]) return selectMovie(movieResults.value[0])
  if (rapResults.value[0]) return selectRap(rapResults.value[0])
}

function selectMovie(m) {
  close()
  if (m?.id) router.push(`/phim/${m.id}`)
}

function selectRap(r) {
  close()
  if (r?.id) router.push(`/rap/${r.id}`)
}

function onKeySlash(e) {
  if (!open.value && e.key === '/' && !['INPUT', 'TEXTAREA', 'SELECT'].includes(e.target.tagName)) {
    e.preventDefault()
    doOpen()
  }
}

onMounted(() => window.addEventListener('keydown', onKeySlash))
onBeforeUnmount(() => {
  clearTimeout(timer)
  window.removeEventListener('keydown', onKeySlash)
})

defineExpose({ open: doOpen, close })
</script>

<style scoped>
.gs-fade-enter-active, .gs-fade-leave-active { transition: opacity 0.18s ease; }
.gs-fade-enter-from, .gs-fade-leave-to { opacity: 0; }

.gs-overlay {
  position: fixed; inset: 0; z-index: 200;
  background: rgba(4, 6, 20, 0.72);
  backdrop-filter: blur(6px);
  display: flex; align-items: flex-start; justify-content: center;
  padding: 10vh 16px 16px;
}
.gs-panel {
  width: min(640px, 100%);
  background: #14142b;
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 14px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.55);
  overflow: hidden;
}
.gs-input-row { display: flex; align-items: center; gap: 10px; padding: 14px 16px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.gs-icon { flex-shrink: 0; color: rgba(255,255,255,0.5); }
.gs-input {
  flex: 1; background: transparent; border: none; outline: none;
  font-size: 16px; color: #fff;
}
.gs-input::placeholder { color: rgba(255,255,255,0.4); }
.gs-close {
  flex-shrink: 0; display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 10px; border-radius: 8px; cursor: pointer;
  background: rgba(255,255,255,0.08); border: none; color: rgba(255,255,255,0.7);
}
.gs-close:hover { background: rgba(255,255,255,0.15); color: #fff; }
.gs-close kbd {
  font-size: 10px; padding: 2px 5px; border-radius: 4px;
  background: rgba(255,255,255,0.12); color: rgba(255,255,255,0.8); font-family: inherit;
}
.gs-body { max-height: 56vh; overflow-y: auto; padding: 8px; }
.gs-section-label {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px 6px;
  font-size: 11px; font-weight: 800; letter-spacing: 0.08em; text-transform: uppercase;
  color: rgba(255,255,255,0.45);
}
.gs-section-label::after { content: ''; flex: 1; height: 1px; background: rgba(255,255,255,0.1); }
.gs-state {
  display: flex; align-items: center; justify-content: center; gap: 10px;
  padding: 36px 16px; color: rgba(255,255,255,0.55); font-size: 14px;
}
.gs-empty { color: rgba(255,255,255,0.6); }
.gs-hint { flex-direction: column; gap: 8px; }
.gs-kbd-hint kbd {
  font-size: 11px; padding: 2px 6px; border-radius: 4px; margin: 0 2px;
  background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.18); color: rgba(255,255,255,0.75);
}
.gs-spinner {
  width: 22px; height: 22px; border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.15); border-top-color: #e50914;
  animation: gs-spin 0.8s linear infinite;
}
@keyframes gs-spin { to { transform: rotate(360deg); } }
.gs-item {
  width: 100%; display: flex; align-items: center; gap: 12px;
  padding: 10px 12px; margin-bottom: 4px;
  background: transparent; border: none; border-radius: 10px;
  color: #fff; text-align: left; cursor: pointer;
  transition: background 0.15s ease;
}
.gs-item:last-child { margin-bottom: 0; }
.gs-item:hover { background: rgba(229, 9, 20, 0.14); }
.gs-item__poster {
  flex-shrink: 0; width: 42px; height: 60px; border-radius: 6px; overflow: hidden;
  background: #1a1a2e; display: flex; align-items: center; justify-content: center;
}
.gs-item__poster--wide { width: 60px; height: 42px; }
.gs-item__poster img { width: 100%; height: 100%; object-fit: cover; display: block; }
.gs-item__poster-fallback { font-size: 20px; opacity: 0.7; }
.gs-item__main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 3px; }
.gs-item__title {
  font-size: 14px; font-weight: 600; color: #fff;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.gs-item__sub {
  font-size: 12px; color: rgba(255,255,255,0.5);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.gs-item__badge {
  flex-shrink: 0; font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 999px;
  background: rgba(34,197,94,0.18); color: #4ade80;
}
.gs-item__badge--soon { background: rgba(234,179,8,0.16); color: #facc15; }
.gs-item__go { flex-shrink: 0; color: rgba(255,255,255,0.35); }
</style>

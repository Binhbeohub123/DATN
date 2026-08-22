<template>
    <footer class="footer">
      <!-- Hero CTA -->
      <div class="footer-hero">
        <div class="footer-hero__copy">
          <p class="footer-hero__tag">BE HAPPY, BE A STAR</p>
          <p class="footer-hero__sub">{{ t('footerTagline') }}</p>
        </div>
        <div class="footer-hero__actions">
          <button class="footer-btn footer-btn--primary" @click="$emit('footer-nav', { action: 'movies' })">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>
            {{ t('footerBuyTickets') }}
          </button>
          <button class="footer-btn footer-btn--ghost" @click="$emit('footer-nav', { action: 'movies' })">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M8.5 2h7l-1 7h-5l-1-7z"/><path d="M5 3h14M7 9h10l1.5 4.5-1 4H6.5l-1-4L7 9z"/><path d="M9 17.5V22M15 17.5V22"/></svg>
            {{ t('footerBuyPopcorn') }}
          </button>
        </div>
      </div>

      <!-- Link columns -->
      <div class="footer-grid">
        <div class="footer-col">
          <h4 class="footer-col__title">{{ t('footerMovies') }}</h4>
          <button class="footer-link" @click="$emit('footer-nav', { action: 'movies-tab', tab: 'dang_chieu' })">{{ t('nowShowing') }}</button>
          <button class="footer-link" @click="$emit('footer-nav', { action: 'movies-tab', tab: 'sap_chieu' })">{{ t('comingSoon') }}</button>
          <button class="footer-link" @click="$emit('footer-nav', { action: 'movies' })">{{ t('bookNow') }}</button>
        </div>

        <div class="footer-col">
          <h4 class="footer-col__title">{{ t('footerAccount') }}</h4>
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/auth" class="footer-link">{{ t('login') }}</router-link>
            <router-link to="/auth?mode=register" class="footer-link">{{ t('register') }}</router-link>
          </template>
          <template v-else>
            <router-link to="/profile" class="footer-link">{{ t('profile') }}</router-link>
            <router-link v-if="authStore.userRole !== 'STAFF'" to="/my-tickets" class="footer-link">{{ t('tickets') }}</router-link>
            <router-link v-if="authStore.userRole !== 'STAFF'" to="/transaction-history" class="footer-link">Lịch sử GD</router-link>
          </template>
        </div>

        <div class="footer-col">
          <h4 class="footer-col__title">{{ t('footerExplore') }}</h4>
          <button class="footer-link" @click="$emit('footer-nav', { action: 'tab', tab: 'rap_chieu' })">{{ t('tabRap') }}</button>
          <button class="footer-link" @click="$emit('footer-nav', { action: 'tab', tab: 'khuyen_mai' })">{{ t('tabKhuyenMai') }}</button>
          <button class="footer-link" @click="$emit('footer-nav', { action: 'tab', tab: 'gioi_thieu' })">{{ t('tabGioiThieu') }}</button>
        </div>

        <div class="footer-col">
          <h4 class="footer-col__title">{{ t('footerTheaters') }}</h4>
          <template v-if="cinemas.length">
            <router-link
              v-for="cinema in cinemas.slice(0, 5)"
              :key="cinema.id"
              :to="'/rap/' + cinema.id"
              class="footer-link footer-link--truncate"
              :title="cinema.tenRap"
            >{{ cinema.tenRap }}</router-link>
          </template>
          <button class="footer-link" @click="$emit('footer-nav', { action: 'tab', tab: 'rap_chieu' })">{{ t('footerAllTheaters') }}</button>
        </div>
      </div>

      <!-- Language + legal -->
      <div class="footer-bottom">
        <div class="footer-lang">
          <button :class="['footer-lang__btn', { active: lang === 'vi' }]" @click="$emit('toggle-lang', 'vi')">VI</button>
          <span class="footer-lang__sep">|</span>
          <button :class="['footer-lang__btn', { active: lang === 'en' }]" @click="$emit('toggle-lang', 'en')">EN</button>
        </div>
        <p class="footer-copy">&copy; 2026 PolyCinema. {{ t('allRights') }}</p>
      </div>
    </footer>
</template>

<script setup>
import { useAuthStore } from '@/stores/authStore'

defineProps({
  t: { type: Function, required: true },
  lang: { type: String, default: 'vi' },
  cinemas: { type: Array, default: () => [] }
})

defineEmits(['toggle-lang', 'footer-nav'])

const authStore = useAuthStore()
</script>

<style>
/* ── Footer ── */
.footer {
  background: #0a0a0f;
  border-top: 1px solid rgba(255,255,255,0.06);
  color: var(--text-secondary, #94a3b8);
  padding: 0;
}
.footer-hero {
  display: flex; align-items: center; justify-content: space-between; gap: 24px; flex-wrap: wrap;
  max-width: 1400px; margin: 0 auto; padding: 56px 40px 40px;
}
.footer-hero__tag {
  margin: 0 0 8px;
  font-family: var(--font-display, 'Playfair Display', Georgia, serif);
  font-size: clamp(28px, 4vw, 44px); font-weight: 700; line-height: 1.15; letter-spacing: 0.5px; color: #fff;
}
.footer-hero__sub { margin: 0; font-size: 15px; color: var(--text-secondary, #94a3b8); }
.footer-hero__actions { display: flex; gap: 12px; flex-wrap: wrap; }
.footer-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 12px 22px; border: 1px solid transparent;
  border-radius: var(--radius-pill, 999px);
  font-family: var(--font-ui, 'Inter', sans-serif); font-size: 14px; font-weight: 700;
  cursor: pointer; text-decoration: none;
  transition: transform 0.25s var(--ease-out, cubic-bezier(0.4,0,0.2,1)), box-shadow 0.25s, background 0.25s, border-color 0.25s, color 0.25s;
}
.footer-btn--primary { background: var(--electric, #29bcea); color: #fff; box-shadow: var(--glow-elec, 0 0 16px rgba(41,188,234,0.30)); }
.footer-btn--primary:hover { background: var(--electric-hover, #1a9fbd); transform: translateY(-2px); box-shadow: 0 0 28px var(--electric-glow, rgba(41,188,234,0.30)); }
.footer-btn--ghost { background: var(--glass-bg, rgba(255,255,255,0.04)); border-color: var(--glass-border, rgba(255,255,255,0.08)); color: #fff; }
.footer-btn--ghost:hover { background: var(--gold-soft, rgba(201,168,76,0.10)); border-color: var(--gold, #C9A84C); color: var(--gold-bright, #F5D17E); transform: translateY(-2px); }

.footer-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 32px;
  max-width: 1400px; margin: 0 auto; padding: 40px 40px 48px;
  border-top: 1px solid rgba(255,255,255,0.06); text-align: left;
}
.footer-col__title {
  margin: 0 0 14px; font-size: 13px; font-weight: 700; letter-spacing: 1.2px;
  text-transform: uppercase; color: var(--text-primary, #f1f5f9);
}
.footer-link {
  display: block; width: 100%; margin: 0; padding: 6px 0;
  background: none; border: none;
  font-family: var(--font-ui, 'Inter', sans-serif); font-size: 14px;
  color: var(--text-secondary, #94a3b8); text-align: left; text-decoration: none;
  cursor: pointer; transition: color 0.2s;
}
.footer-link:hover { color: var(--electric, #29bcea); }
.footer-link--truncate { max-width: 100%; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }

.footer-bottom {
  display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap;
  max-width: 1400px; margin: 0 auto; padding: 20px 40px 28px;
  border-top: 1px solid rgba(255,255,255,0.06);
}
.footer-lang { display: inline-flex; align-items: center; gap: 8px; }
.footer-lang__btn {
  padding: 5px 10px; background: none; border: 1px solid transparent;
  border-radius: var(--radius-sm, 6px);
  font-family: var(--font-ui, 'Inter', sans-serif); font-size: 12px; font-weight: 700;
  letter-spacing: 0.6px; color: var(--text-ghost, rgba(241,245,249,0.45));
  cursor: pointer; transition: color 0.2s, border-color 0.2s, background 0.2s;
}
.footer-lang__btn:hover { color: var(--text-primary, #f1f5f9); }
.footer-lang__btn.active { color: var(--electric, #29bcea); border-color: rgba(41,188,234,0.35); background: var(--electric-soft, rgba(41,188,234,0.08)); }
.footer-lang__sep { color: var(--text-ghost, rgba(241,245,249,0.45)); }
.footer-copy { margin: 0; font-size: 13px; color: var(--text-ghost, rgba(241,245,249,0.45)); }

/* ── Responsive ── */
@media (max-width: 768px) {
  .footer-hero { flex-direction: column; align-items: flex-start; padding: 40px 20px 32px; }
  .footer-grid { grid-template-columns: 1fr; gap: 24px; padding: 32px 20px 36px; }
  .footer-bottom { padding: 18px 20px 24px; }
}
</style>

# Implementation Plan: Premium UI Redesign (Bugfix)

This is a bugfix spec. The implementation is entirely visual — no routing, API, store, auth,
or booking logic changes. Every `<script setup>` block is preserved byte-for-byte. Only
`<template>` markup and `<style>` sections change, plus new shared CSS/composable files and
SVG icon components are added.

## Overview

This is a bugfix spec. The fix is entirely visual — no routing, API, store, auth, or booking logic changes. Implementation proceeds in waves: exploration PBT first, then the foundational CSS token and pattern layer, then composables and SVG icons, then page-level redesigns, and finally fix verification PBTs.

## Task Dependency Graph

```json
{
  "waves": [
    {
      "wave": 1,
      "tasks": ["1"],
      "description": "Exploration PBT — run on unfixed code to confirm bug exists"
    },
    {
      "wave": 2,
      "tasks": ["2", "3"],
      "description": "Foundational CSS: tokens.css and cinema.css (can be built in parallel)"
    },
    {
      "wave": 3,
      "tasks": ["4"],
      "description": "Wire up main.css + base.css to import new files and dual typeface"
    },
    {
      "wave": 4,
      "tasks": ["5", "6"],
      "description": "Motion composables and SVG icon components (can be built in parallel)"
    },
    {
      "wave": 5,
      "tasks": ["7", "8", "9", "10", "11"],
      "description": "Page redesigns — all depend on waves 2–4 (can be built in parallel)"
    },
    {
      "wave": 6,
      "tasks": ["12"],
      "description": "Fix verification PBTs — require all implementation complete"
    }
  ]
}
```

## Tasks

- [x] 1. Write bug condition exploration property test
  - [x] 1.1 Write property test for bug condition exploration (PBT — expected to fail on unfixed code)
    - Mount `home.vue` and assert `.logo` text content contains `🎬` (emoji not yet replaced)
    - Assert computed style of nav does NOT use `backdrop-filter` (glass morphism absent)
    - Assert `h1.movie-title` computed `font-family` does NOT contain `Playfair` (single typeface)
    - Assert no `scroll` event listener on `window` after mount (no parallax)
    - Assert page background resolves to `#ffffff` or does not equal `#050508` (no void token)
    - Assert `.glass-card` class is absent from rendered DOM (no depth system)
    - This test is expected to FAIL on unfixed code — failure confirms bug exists
  - [x] 1.2 Document the counterexamples and confirm all six bug indicators are present in current codebase

- [x] 2. Create the CSS token system
  - [x] 2.1 Create `src/assets/tokens.css` with all 40+ CSS custom properties
    - Void/depth ladder: `--void: #050508`, `--deep: #0a0a0f`, `--surface-1` through `--surface-4` with progressive luminance
    - Gold accent tier: `--gold: #C9A84C`, `--gold-bright: #F5D17E`, `--gold-glow`, `--gold-soft`
    - Electric accent tier: `--electric: #29bcea`, `--electric-hover`, `--electric-glow`, `--electric-soft`
    - Text tokens: `--text-primary: #f1f5f9`, `--text-secondary: #94a3b8`, `--text-ghost`, `--on-accent`
    - Glass morphism tokens: `--glass-bg`, `--glass-bg-heavy`, `--glass-border`, `--glass-blur: blur(20px)`, `--glass-blur-lg: blur(40px)`
    - Elevation shadow tokens: `--shadow-sm`, `--shadow-md`, `--shadow-lg`, `--glow-gold`, `--glow-elec`
    - Spring easing tokens: `--spring: cubic-bezier(0.34,1.56,0.64,1)`, `--ease-out: cubic-bezier(0.4,0,0.2,1)`
    - Border radius tokens: `--radius-sm: 6px`, `--radius-md: 12px`, `--radius-lg: 20px`, `--radius-pill: 999px`
    - Typography tokens: `--font-display: 'Playfair Display', Georgia, serif`, `--font-ui: 'Inter', -apple-system, sans-serif`
    - Page/border tokens: `--page-bg: var(--void)`, `--border: var(--glass-border)`
  - [x] 2.2 Write unit test verifying `getComputedStyle(document.documentElement)` exposes `--void: #050508` after `tokens.css` is loaded

- [x] 3. Create shared CSS pattern library
  - [x] 3.1 Create `src/assets/cinema.css` with all shared cross-page patterns
    - `.glass-card`: `background: var(--glass-bg)`, `border: 1px solid var(--glass-border)`, `backdrop-filter: var(--glass-blur)`, `border-radius: var(--radius-md)`
    - `.glass-card--heavy`: heavier `background: var(--glass-bg-heavy)`, `backdrop-filter: var(--glass-blur-lg)`
    - `.btn-bib`: Button-in-Button CTA with double-bezel outer `outline`/`outline-offset` pattern, spring transition, `will-change: transform`
    - `.btn-bib--gold`: gold variant using `var(--gold)` and `var(--glow-gold)`
    - `.chip-glass`, `.chip-glass--electric`, `.chip-glass--gold` glass chip variants
    - `.spinner-cinema`: `44×44px` border spinner with `@keyframes cinema-spin`
    - `.reveal` and `.reveal.is-visible`: scroll reveal base (`translateY(24px) → 0`, `opacity: 0 → 1`, `0.6s var(--ease-out)`)
    - `@supports not (backdrop-filter: blur(1px))` fallback: `.glass-card → var(--surface-2)`, `.nav-island → rgba(5,5,8,0.97)`, `.book-bar → var(--surface-1)`
  - [x] 3.2 Write unit test verifying `.glass-card` CSS rule includes `backdrop-filter` declaration

- [x] 4. Update `main.css` and `base.css` to wire up new files and dual typeface
  - [x] 4.1 Add at the top of `src/assets/main.css`:
    - `@import './tokens.css'`
    - `@import './cinema.css'`
    - `@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Inter:wght@400;500;600;700&display=swap')`
    - Keep existing Raleway import unchanged (admin panel fallback)
  - [x] 4.2 Update `body` rule in `src/assets/base.css`:
    - `font-family: var(--font-ui)`
    - `color: var(--text-secondary)`
    - `background: var(--page-bg)`
    - Remove any hardcoded hex values now covered by tokens

- [x] 5. Create motion composables
  - [x] 5.1 Create `src/composables/useReveal.js` — IntersectionObserver stagger reveal
    - Accept `root` ref, optional `selector` (default `.reveal`), optional `delayStep` (default `50`)
    - On mount: query all matching elements; set `transitionDelay` per index; observe each
    - On intersection (threshold `0.12`): add `is-visible` class and unobserve element
    - On unmount: disconnect observer
  - [x] 5.2 Create `src/composables/useMagnetic.js` — mousemove magnetic offset
    - Accept `el` DOM element and optional `strength` (default `0.3`)
    - `mousemove` handler: compute `dx = (clientX - cx) * strength`, `dy = (clientY - cy) * strength`; apply via `el.style.transform`
    - `mouseleave` handler: clear `el.style.transform` (springs back via CSS transition)
    - Return cleanup function that removes both listeners (for use in `onUnmounted`)
  - [x] 5.3 Write unit tests verifying:
    - `useReveal` attaches IntersectionObserver and adds `is-visible` class when threshold is met
    - `useMagnetic` applies `translate()` transform on `mousemove` and clears it on `mouseleave`

- [x] 6. Create SVG icon components in `src/components/icons/`
  - All icons must share: `viewBox="0 0 24 24"`, `fill="none"`, `stroke="currentColor"`, `stroke-width="1.5"`, `stroke-linecap="round"`, `stroke-linejoin="round"`, optional `size` prop defaulting to `24`
  - [x] 6.1 Create `IconFilm.vue` — replaces `🎬` (film strip / clapperboard path data)
  - [x] 6.2 Create `IconTicket.vue` — replaces `🎟️` (ticket with perforations)
  - [x] 6.3 Create `IconUser.vue` — replaces `👤` (person silhouette / head + shoulders)
  - [x] 6.4 Create `IconCard.vue` — replaces `💳` (credit card rectangle with stripe)
  - [x] 6.5 Create `IconSettings.vue` — replaces `⚙️` (cog / gear wheel)
  - [x] 6.6 Create `IconBack.vue` — back arrow (`M19 12H5 M12 19l-7-7 7-7`)
  - [x] 6.7 Create `IconStar.vue` — replaces `⭐` (five-point polygon star)
  - [x] 6.8 Create `IconClock.vue` — replaces `⏱` (clock circle with hands)
  - [x] 6.9 Create `IconPlay.vue` — replaces inline play SVG (filled triangle `M5 3l14 9-14 9V3z`)
  - [x] 6.10 Create `IconCheck.vue` — checkmark inside circle
  - [x] 6.11 Create `IconLogout.vue` — replaces `🔓` (arrow-right-from-box / door-open)
  - [x] 6.12 Create `IconHistory.vue` — replaces `📋` (list lines / clipboard)
  - [x] 6.13 Create `IconGlobe.vue` — replaces `🌐` (globe / sphere with meridians)
  - [x] 6.14 Create `IconKey.vue` — replaces `🔑` (key with bow and blade)
  - [x] 6.15 Write unit test verifying each icon component renders `<svg>` with correct `width`, `height` matching `size` prop, and `stroke-width="1.5"`

- [x] 7. Redesign `home.vue` — template and style only
  - [x] 7.1 Redesign Navbar block — Fluid Island nav
    - Wrap nav contents in `.nav-island`: `border-radius: var(--radius-pill)`, `backdrop-filter: var(--glass-blur)`, `background: rgba(5,5,8,0.85)`
    - Replace `🎬` emoji logo with `<IconFilm />` + logotype in `var(--font-display)`
    - Nav links: `transition: color 0.3s var(--spring)` + `::after` underline `scaleX(0 → 1)` on hover
    - Convert "Đăng ký" CTA to `.btn-bib`; attach `useMagnetic` on mount, clean up on unmount
    - User dropdown: `.glass-card` with `box-shadow: var(--shadow-lg)`; replace all emoji menu items with icon components (`<IconUser />`, `<IconHistory />`, `<IconSettings />`, `<IconLogout />`)
    - Preserve all `v-if`, `v-show`, `@click`, `router-link`, and auth-conditional bindings byte-for-byte
  - [x] 7.2 Redesign Hero / Banner Slider block — cinematic stage
    - Container becomes `.hero-stage`: `height: 100svh; min-height: 600px; position: relative; overflow: hidden`
    - Separate background image into `.hero-bg` child: `width: 100%; height: 120%; background-size: cover; will-change: transform`
    - Add layer stack children (bottom → top): `.hero-vignette` (radial gradient to void), `.hero-fade` (linear gradient to void at bottom), `.hero-grain` (`opacity: 0.03; mix-blend-mode: overlay`)
    - `.hero-content` holds title + subtitle + CTA
    - Title: `font-family: var(--font-display); font-size: clamp(48px,7vw,96px); letter-spacing: -0.04em`
    - CTA: `.btn-bib` with `useMagnetic`
    - Active dot nav: `width: 24px; border-radius: var(--radius-pill); transition: width 0.4s var(--spring)`
    - Add `requestAnimationFrame` parallax: scroll listener sets `.hero-bg` `transform: translateY(scrollY * 0.4px)`; listener added in `onMounted`, removed in `onUnmounted`; guard: only runs while `scrollY < heroEl.offsetHeight`
    - Preserve `bannerIndex`, `startBannerTimer`, `watch(movieStore.banners.length)`, dot-click, and prev/next handlers byte-for-byte
  - [x] 7.3 Redesign Movie Card grid block — editorial cards
    - `.movie-card`: `border-radius: var(--radius-md); transform-style: preserve-3d; transition: transform 0.15s ease-out`
    - Add `@mousemove` tilt handler: `rotateX/Y` from cursor-centre offset capped at ±8 deg via `perspective(1000px)`
    - Add `@mouseleave` handler: reset `el.style.transform = ''` (CSS transition springs back)
    - Stagger: bind `:style="{ '--card-index': index }"` on each card; CSS `animation-delay: calc(var(--card-index) * 50ms)`
    - Wrap card grid in `useReveal` container ref
    - Poster `::after` double-bezel: `box-shadow: inset 0 0 0 1px rgba(255,255,255,0.12)`
    - `.movie-overlay`: `backdrop-filter: blur(8px)`, gradient from void, `transform: translateY(100% → 0)` on parent hover
    - Replace inline play SVG with `<IconPlay />`; rating with `<IconStar color="var(--gold-bright)" />`; genre tags with `.chip-glass--electric`
    - Preserve all `v-for`, `v-if`, `:key`, `router-link`, and `movieStore` data bindings byte-for-byte
  - [x] 7.4 Write unit tests verifying:
    - Nav contains `<svg>` element and no `🎬` emoji text
    - `.hero-stage` element is present in rendered DOM
    - Movie card genre tags use `.chip-glass--electric` class
    - `movieStore.fetchDangChieu`, `fetchSapChieu`, and `fetchBanners` are all called on `onMounted`

- [x] 8. Redesign `AuthPage.vue` — template and style only
  - [x] 8.1 Redesign left panel — cinematic brand split
    - `.page` background: `var(--void)`
    - `.left` panel: `background: linear-gradient(135deg, var(--deep) 0%, var(--surface-1) 100%)`
    - `.left::before` pseudo-element: `radial-gradient` glow at 30% 40% with `animation: glow-pulse 4s ease-in-out infinite alternate` (`opacity: 0.4 → 0.8`, `transform: scale(1 → 1.15)`)
    - Left heading: `font-family: var(--font-display); font-size: clamp(32px,4vw,48px); letter-spacing: -0.03em`
    - Feature items: replace flat bordered divs with `.chip-glass` + SVG icon components (`<IconFilm />`, `<IconStar />`, `<IconCard />`)
  - [x] 8.2 Redesign right panel — glass form card
    - `.auth-card` (or `.right`): becomes `.glass-card--heavy`
    - Input floating-label treatment: wrap each `<input>` in a `.input-wrap` div with sibling `<label>`; label starts at input midpoint and animates up + shrinks (`translateY(-24px) scale(0.85)`) via `:focus-within` + `input:not(:placeholder-shown) ~ label`; `::after` underline accent uses `transform: scaleX(0 → 1)` on `:focus-within`
    - All transitions: `0.4s cubic-bezier(0.4,0,0.2,1)`
    - Primary submit `<button>`: `.btn-bib`
    - Social login buttons (Google, Discord): `.btn-bib` with `background: transparent; border: 1px solid var(--glass-border)`
    - Error/success message boxes: `.glass-card` with `color: var(--text-primary)` or semantic error colour
    - Preserve all `v-model`, `@input`, `@keyup.enter`, `@click`, `:disabled`, `:class` bindings byte-for-byte
  - [x] 8.3 Write unit tests verifying:
    - Each `<input>` wrapper has a sibling `<label>` (floating-label DOM structure present)
    - No emoji characters (`🎬`, `👤`, `💳`, `⚙️`, etc.) present in rendered template
    - `authStore.login` is still called with trimmed, lowercased email on form submit

- [x] 9. Redesign `MovieDetailPage.vue` — template and style only
  - [x] 9.1 Redesign movie detail hero — cinematic stage
    - `.hero`: `height: 100svh; min-height: 600px`
    - Same multi-layer stack as home hero: `.hero-bg` (parallax), `.hero-vignette`, `.hero-fade`
    - `.poster-img`: width `160px` on desktop; double-bezel `box-shadow: 0 0 0 1px rgba(255,255,255,0.15), 0 0 32px rgba(0,0,0,0.6)`
    - Add `.poster-glow` sibling div behind poster: `position: absolute; filter: blur(40px); background: var(--electric-glow); opacity: 0.6`
    - `.movie-title`: `font-family: var(--font-display); letter-spacing: -0.03em; font-size: clamp(28px,5vw,56px)`
    - Meta chips: `.chip-glass` with `<IconClock />`, `<IconStar />`, `<IconGlobe />`
    - `.book-bar` fixed-bottom: `backdrop-filter: var(--glass-blur)`, `background: rgba(5,5,8,0.85)`, `border-top: 1px solid var(--glass-border)`
    - `.btn-book` becomes `.btn-bib`; attach `useMagnetic`
    - Preserve all `<script setup>` logic, `v-if`, `v-for`, `router` usage, and store bindings byte-for-byte
  - [x] 9.2 Write unit tests verifying:
    - `.hero` element is present in rendered DOM
    - `.btn-bib` class is present on the book-ticket CTA
    - Movie title, poster `src`, and rating meta still bind correctly from store data

- [x] 10. Redesign `SeatSelectionPage.vue` — template and style only
  - [x] 10.1 Redesign seat map and layout — spatial cinema stage
    - `.seat-page` background: `var(--void)`
    - `.top-bar`: `backdrop-filter: var(--glass-blur)`, `background: rgba(5,5,8,0.85)`, glass border-bottom
    - `.showtime-strip`: `.glass-card` with `border-radius: 0; border-left: none; border-right: none`
    - `.screen-bar`: `transform: perspective(800px) rotateX(-8deg)`, `background: linear-gradient(90deg, transparent, var(--electric), transparent)`, add glow `box-shadow: 0 4px 32px var(--electric-glow)`
    - `.seat`: `width: 36px; height: 36px; border-radius: 8px`
    - `.seat--selected`: class adds `background: var(--electric); box-shadow: 0 0 12px var(--electric-glow)` (resting state, not animated)
    - `.seat--vip`: `background: var(--gold); box-shadow: 0 0 8px var(--gold-glow)`
    - `.seat--couple`: `background: #ec4899`
    - Legend items: `.chip-glass` pattern
    - `.bottom-bar`: `.glass-card` fixed-bottom with `backdrop-filter`; seat summary chips use `.chip-glass--electric`
    - `.btn-next` becomes `.btn-bib`
    - Preserve all `v-for`, seat state (`seat.status`), `isMaxReached` disable logic, `bookingStore` bindings, and max-8 warning display byte-for-byte
  - [x] 10.2 Write unit tests verifying:
    - Page root background references `--void` token
    - Adding 8 seats results in `isMaxReached.value === true` and 9th click is disabled
    - `.btn-bib` proceed CTA is present in rendered template

- [x] 11. Redesign `CheckoutPage.vue` — template and style only
  - [x] 11.1 Redesign checkout layout — glass morphism cards and double-bezel price summary
    - `.checkout-page` background: `var(--void)`
    - All `.card` section containers become `.glass-card`
    - Card titles: remove emoji; replace with icon components (`<IconTicket />`, `<IconStar />`, `<IconCard />`, `<IconCheck />`); `font-family: var(--font-ui); color: var(--text-primary)`
    - Promo and loyalty inputs: floating-label treatment (same `.input-wrap` + `<label>` pattern as auth)
    - `.pay-card` active state: `border-color: var(--electric); box-shadow: 0 0 0 1px var(--electric-soft)`
    - `.price-card` double-bezel: outer `.price-bezel` div (`border: 1px solid var(--glass-border); border-radius: var(--radius-md)`); inner `.price-bezel-inner` div (`border: 1px solid rgba(255,255,255,0.05); border-radius: calc(var(--radius-md) - 4px); margin: 4px`)
    - `.btn-confirm` becomes `.btn-bib`; loading state inserts `.spinner-cinema` and hides label text
    - Payment method icon column: `<IconCard />` for VNPay/MoMo, `<IconClock />` for Cash
    - Preserve all `v-model`, `applyPromo()`, `bookingStore.createBooking()`, payment method selection, `:disabled` bindings, and loyalty points logic byte-for-byte
  - [x] 11.2 Write unit tests verifying:
    - Checkout `.price-card` contains nested double-bezel DOM structure (`.price-bezel` wrapping `.price-bezel-inner`)
    - No emoji characters present in rendered checkout template
    - `bookingStore.validatePromo` is called with `'POLY10'` when promo code is submitted
    - Confirm button is still wired to `bookingStore.createBooking`

- [x] 12. Write fix verification property tests (PBT)
  - [x] 12.1 Write property test verifying cinematic design system is applied (PBT — expected to pass on fixed code)
    - Assert `tokens.css` custom property `--void` resolves to `#050508` via `getComputedStyle`
    - Assert `home.vue` nav rendered DOM contains `<svg>` with `stroke-width="1.5"` and does NOT contain `🎬` text
    - Assert `.btn-bib` computed transition includes `cubic-bezier(0.34,1.56,0.64,1)` spring easing
    - Assert `.glass-card` computed style includes `backdrop-filter` property
    - Assert `.reveal` CSS rule includes `translateY(24px)` and `opacity: 0` initial state
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12**
  - [x] 12.2 Write property test verifying preservation of non-visual logic (PBT)
    - For random sequences of `addSeat` / `removeSeat` calls (random seat objects, 0–20 calls), assert `bookingStore.selectedSeats` length and `totalSeatPrice` are identical between pre-fix and post-fix code paths
    - For random banner array lengths (1–10), assert `bannerIndex` never exceeds `banners.length - 1` and cycles correctly via `startBannerTimer`
    - For random promo code strings (alphanumeric, length 4–12), assert the promo input trims whitespace and uppercases before calling `bookingStore.validatePromo`
    - For random valid login credentials, assert `authStore.login` is called and auth token is set identically pre/post-fix
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10**

## Notes

- The admin panel (`src/admin/`) is explicitly out of scope — no changes to any admin files.
- All `<script setup>` blocks in `home.vue`, `AuthPage.vue`, `MovieDetailPage.vue`, `SeatSelectionPage.vue`, and `CheckoutPage.vue` must be preserved byte-for-byte — only `<template>` and `<style>` sections change.
- The `themeStore` `data-theme` attribute logic is untouched; new tokens must work under both `[data-theme='light']` and `[data-theme='dark']` attribute selectors.
- GPU-only rule: every animation and transition must use only `transform` and `opacity`. No layout-triggering properties (`width`, `height`, `top`, `left`, `padding`, `margin`) and no paint-triggering properties (`background`, `box-shadow`, `color`, `border-color`) may change during animation — only at resting/toggled states via class switches.
- `will-change` is declared sparingly: `.hero-bg` (parallax), `.movie-card` (tilt), `.btn-bib` (magnetic). No other elements need it.
- Font loading uses `display=swap` so text remains visible immediately with system-font fallback during Playfair Display load.

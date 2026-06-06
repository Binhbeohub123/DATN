# Premium UI Redesign — Bugfix Design

## Overview

The PolyCinema frontend currently renders as a generic flat template: single accent colour
(`#29bcea`), zero luminance hierarchy, emoji icons, a single typeface at two weights, and
`transition: 0.2s ease` as the only motion primitive. This design document formalises the fix
as a full design-system overhaul applied component-by-component across every user-facing route.

The fix is entirely visual — no routing, API, store, auth, or booking logic changes. Every
`<script setup>` block in the existing `.vue` files is preserved byte-for-byte. Only `<template>`
markup (to swap emoji for SVG, add BEM class names, attach new container elements) and `<style>`
blocks are modified, plus new shared CSS files and one composable are added.

The strategy is:
1. Introduce a CSS custom-property token layer in `src/assets/tokens.css`.
2. Add a motion composable `src/composables/useReveal.js` (IntersectionObserver).
3. Add a magnetic-hover composable `src/composables/useMagnetic.js`.
4. Rewrite the `<style>` sections of the seven primary page/component files.
5. Add a shared `src/assets/cinema.css` for cross-page patterns (glass cards, Button-in-Button, chips).
6. Add `src/components/icons/` inline SVG icon components.

---

## Glossary

- **Bug_Condition (C)**: Any UI render state that fails the premium standard — identified in
  `isBugCondition(X)` pseudocode in `bugfix.md`.
- **Property (P)**: The desired visual output for any page covered by C — cinematic colour,
  SVG icons, dual typeface, glass morphism, GPU-safe motion.
- **Preservation**: All non-visual behaviour — routing, Pinia stores, API calls, auth flows,
  booking flows — that must remain identical after the fix.
- **Token**: A CSS custom property defined in `:root` inside `tokens.css`.
- **Glass card**: A container with `backdrop-filter: blur()` + semi-transparent background.
- **Button-in-Button (BiB)**: A CTA pattern with an outer bezel ring and an inner lit button.
- **Fluid Island Nav**: A floating pill-shaped navbar with glass morphism.
- **IntersectionObserver reveal**: Scroll-triggered `opacity`/`translateY` entrance animation.
- **Magnetic hover**: A `mousemove` handler that offsets an element toward the cursor.
- **GPU-composited**: Animations that use only `transform` and `opacity` — no `width`, `height`,
  `top`, `left`, `background`, or `box-shadow` changes during animation (only at rest states).
- **`handleKeyPress`**: Not applicable — this is a UI redesign bug, not a keyboard handler bug.
- **`bookingStore`**: `src/stores/bookingStore.js` — state/mutations are untouched.
- **`movieStore`**: `src/stores/movieStore.js` — state/mutations are untouched.
- **`authStore`**: `src/stores/authStore.js` — state/mutations are untouched.
- **`themeStore`**: `src/stores/themeStore.js` — logic is untouched; dark mode tokens are additive.

---

## Bug Details

### Bug Condition

The bug manifests on every page load: the renderer produces a UI that fails the premium
visual standard because it applies a single flat accent, zero depth layering, emoji icons,
monotypeface treatment, and no motion choreography.


**Formal Specification:**
```
FUNCTION isBugCondition(X)
  INPUT:  X of type UIRenderState
  OUTPUT: boolean

  RETURN (
    X.usesGenericColourSystem   = true   // single flat #29bcea
    OR X.usesEmojiAsIcons       = true   // Unicode emoji in place of SVG
    OR X.hasNoMotionChoreography = true  // transition: 0.2s ease only
    OR X.usesMonotypeSystem     = true   // single typeface, 2 weights
    OR X.lacksDepthSystem       = true   // no blur, no layering, no glass morphism
    OR X.hasNoScrollAnimations  = true   // no IntersectionObserver-driven reveals
  )
END FUNCTION
```

### Examples

- **Home hero**: 300px flat banner with a single linear-gradient overlay, no parallax, no
  film-grain → should be a `100svh` cinematic stage with multi-layer parallax.
- **Movie card**: `border-radius: 0`, flat hover `border-color` swap, emoji play icon overlay
  → should be `border-radius: 12px`, `perspective(1000px)` 3D tilt, SVG play icon.
- **Auth page**: flat `#f7f7f7` left panel, `border: 1px solid #efefef` inputs, rectangular
  buttons → should be void-background cinematic split, glass-morphism form card, floating-label
  inputs, Button-in-Button CTA.
- **Seat map**: flat `34×34px` rectangles on white background → should be `36×36px` rounded
  squares with per-seat ambient glow on selection, dark void background.
- **Checkout**: plain grey card panels → should be glass-morphism cards with double-bezel
  price summary card.

---

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviours:**
- All Vue Router routes (`/`, `/phim/:id`, `/seat-selection/:showtimeId`, `/combo`,
  `/checkout`, `/auth`, `/profile`, `/my-tickets`, `/transaction-history`,
  `/payment-result/:bookingId`, `/admin/:tab`) must continue to resolve correctly.
- `router.beforeEach` guard logic (OAuth token handling, role check, redirect-to-intended) must
  remain unchanged.
- All `bookingStore` actions (`setMovie`, `setShowtime`, `addSeat`, `removeSeat`, `clearSeats`,
  `validatePromo`, `createBooking`, `setPaymentMethod`) must fire with the same arguments and
  return the same values.
- All `movieStore` actions (`fetchDangChieu`, `fetchSapChieu`, `fetchBanners`) and the
  `setInterval` banner timer must continue to work identically.
- All `authStore` actions (`login`, `logout`, `setRedirectPath`, `popRedirectPath`) must remain
  unchanged.
- The `themeStore` `data-theme` attribute must continue to be applied to `<html>`;
  new tokens must work under both `[data-theme='light']` and `[data-theme='dark']`.
- The 8-seat maximum warning (`isMaxReached`) must continue to disable further seat selection.
- Loading spinners and error states must continue to render on all pages; their visual style
  will be upgraded but their v-if/v-else logic is untouched.
- The admin panel (`/admin`) is explicitly out of scope — no changes to `src/admin/`.

**Scope of Change:**
Only `<template>` and `<style>` sections of the following files are modified:
`src/view/home.vue`, `src/view/MovieDetailPage.vue`, `src/view/SeatSelectionPage.vue`,
`src/view/CheckoutPage.vue`, `src/Auth/AuthPage.vue`.
New files added: `src/assets/tokens.css`, `src/assets/cinema.css`,
`src/composables/useReveal.js`, `src/composables/useMagnetic.js`,
`src/components/icons/` (individual SVG components).

---

## Hypothesized Root Cause

1. **No Token System**: `base.css` defines only 11 tokens — all at the same luminance tier.
   No depth ladder (`--void → --deep → --surface-1 → --surface-4`), no gold accent, no
   glass-morphism shadow/glow tokens. Fix: add `tokens.css` with a complete 40+ token system.

2. **Single Generic Typeface**: `main.css` imports only `Raleway:wght@400;700`. No display
   typeface, no optical-size `clamp()` expressions, no tracking modulation. Fix: add Google
   Fonts import for `Playfair+Display:wght@400;600;700` and `Inter:wght@400;500;600;700`.

3. **Emoji as Icons**: `home.vue`, `AuthPage.vue`, and dropdowns use `🎬`, `🎟️`, `👤`, `💳`,
   `⚙️`. Fix: add inline SVG components in `src/components/icons/` and replace all emoji usage.

4. **No Motion System**: No `@keyframes` entrance animations, no `IntersectionObserver`, no
   spring easing, no parallax scroll handler. Fix: add `useReveal.js` composable (IO-based
   stagger reveals) and `useMagnetic.js` (mousemove magnetic offset).

5. **No Depth / Layering**: Surfaces use opaque flat colours with `border: 1px solid #efefef`.
   No `backdrop-filter`, no layered `box-shadow`, no gradient overlays. Fix: `cinema.css`
   defines `.glass-card`, `.glass-surface`, `.glass-chip` and the hero overlay stack.

6. **Flat Interactive States**: All hover states swap `background-color` or `border-color`
   using `transition: 0.2s ease`. No spatial lift, no spring overshoot, no magnetic tracking.
   Fix: replace with `cubic-bezier(0.34,1.56,0.64,1)` spring transitions and magnetic
   `mousemove` handlers via `useMagnetic.js`.


---

## Correctness Properties

Property 1: Bug Condition — Cinematic Design System Applied

_For any_ page render where `isBugCondition(X)` returns `true` (i.e. the current flat-template
render), the fixed renderer SHALL produce a UI state where:
- `hasCinematicColourSystem = true` — deep void/surface token ladder applied via CSS custom
  properties from `tokens.css`
- `usesSVGIconSystem = true` — all emoji replaced with `src/components/icons/` inline SVGs
- `hasMotionChoreography = true` — spring transitions, IO stagger reveals, magnetic hover active
- `usesDualTypefaceSystem = true` — Playfair Display for H1–H3, Inter for body/UI
- `hasDepthAndLayering = true` — glass-morphism cards, multi-layer hero overlays present
- `hasScrollRevealAnimations = true` — IntersectionObserver-driven entrance on all sections

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12**

Property 2: Preservation — Non-Visual Logic Unchanged

_For any_ user interaction where `isBugCondition(X)` does NOT hold (routing, API calls, store
mutations, auth flows, booking flows), the fixed codebase SHALL produce exactly the same
runtime behaviour as the original codebase:
- `routingBehavior` — identical (same routes, same guards, same redirect logic)
- `apiCalls` — identical (same endpoints, same payloads, same response handling)
- `storeState` — identical (same Pinia state shape, same actions, same getters)
- `authFlows` — identical (login, register, OTP verify, OAuth, password reset)
- `bookingFlows` — identical (seat selection, promo validation, payment redirect)

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10**

---

## Fix Implementation

### Architecture Overview

```
src/
├── assets/
│   ├── tokens.css          ← NEW: all CSS custom properties (~40 tokens)
│   ├── cinema.css          ← NEW: shared patterns (glass, BiB, chips, spinner)
│   ├── base.css            ← MODIFIED: imports tokens.css; removes hardcoded hex values
│   └── main.css            ← MODIFIED: adds dual-typeface Google Fonts import
├── composables/
│   ├── useReveal.js        ← NEW: IntersectionObserver stagger reveal
│   └── useMagnetic.js      ← NEW: mousemove magnetic offset
├── components/
│   └── icons/
│       ├── IconFilm.vue    ← NEW: 24×24 SVG — replaces 🎬
│       ├── IconTicket.vue  ← NEW: 24×24 SVG — replaces 🎟️
│       ├── IconUser.vue    ← NEW: 24×24 SVG — replaces 👤
│       ├── IconCard.vue    ← NEW: 24×24 SVG — replaces 💳
│       ├── IconSettings.vue← NEW: 24×24 SVG — replaces ⚙️
│       ├── IconBack.vue    ← NEW: 24×24 SVG — back-arrow
│       ├── IconStar.vue    ← NEW: 24×24 SVG — star rating
│       ├── IconClock.vue   ← NEW: 24×24 SVG — duration
│       ├── IconPlay.vue    ← NEW: 24×24 SVG — movie overlay play button
│       └── IconCheck.vue   ← NEW: 24×24 SVG — promo success checkmark
├── view/
│   ├── home.vue            ← MODIFIED: template + style only
│   ├── MovieDetailPage.vue ← MODIFIED: template + style only
│   ├── SeatSelectionPage.vue ← MODIFIED: template + style only
│   ├── CheckoutPage.vue    ← MODIFIED: template + style only
│   └── (other pages)       ← minimal token-level update only
└── Auth/
    └── AuthPage.vue        ← MODIFIED: template + style only
```

### File 1: `src/assets/tokens.css`

**Purpose:** Single source of truth for all design tokens.

```css
:root {
  /* ── Void / Depth Ladder ── */
  --void:       #050508;
  --deep:       #0a0a0f;
  --surface-1:  #0f0f17;
  --surface-2:  #14141f;
  --surface-3:  #1a1a28;
  --surface-4:  #222235;

  /* ── Gold Accent Tier ── */
  --gold:         #C9A84C;
  --gold-bright:  #F5D17E;
  --gold-glow:    rgba(201,168,76,0.35);
  --gold-soft:    rgba(201,168,76,0.10);

  /* ── Electric Accent Tier (existing #29bcea upgraded) ── */
  --electric:       #29bcea;
  --electric-hover: #1a9fbd;
  --electric-glow:  rgba(41,188,234,0.30);
  --electric-soft:  rgba(41,188,234,0.08);

  /* ── Text Tokens ── */
  --text-primary:   #f1f5f9;
  --text-secondary: #94a3b8;
  --text-ghost:     rgba(241,245,249,0.45);
  --on-accent:      #ffffff;

  /* ── Glass Morphism ── */
  --glass-bg:       rgba(255,255,255,0.04);
  --glass-bg-heavy: rgba(255,255,255,0.08);
  --glass-border:   rgba(255,255,255,0.08);
  --glass-blur:     blur(20px);
  --glass-blur-lg:  blur(40px);

  /* ── Elevation Shadows ── */
  --shadow-sm:  0 1px 3px rgba(0,0,0,0.35);
  --shadow-md:  0 4px 16px rgba(0,0,0,0.45);
  --shadow-lg:  0 12px 40px rgba(0,0,0,0.55);
  --glow-gold:  0 0 24px var(--gold-glow);
  --glow-elec:  0 0 16px var(--electric-glow);

  /* ── Spring Easing ── */
  --spring:   cubic-bezier(0.34,1.56,0.64,1);
  --ease-out: cubic-bezier(0.4,0,0.2,1);

  /* ── Border Radius ── */
  --radius-sm:  6px;
  --radius-md:  12px;
  --radius-lg:  20px;
  --radius-pill: 999px;

  /* ── Type Scale — Display (Playfair Display) ── */
  --font-display: 'Playfair Display', Georgia, serif;
  --font-ui:      'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;

  /* ── Page Background ── */
  --page-bg: var(--void);
  --border:  var(--glass-border);
}
```


### File 2: `src/assets/cinema.css`

**Purpose:** Shared cross-page patterns. Imported once in `main.css`.

#### Glass Card

```css
.glass-card {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-md);
}

.glass-card--heavy {
  background: var(--glass-bg-heavy);
  backdrop-filter: var(--glass-blur-lg);
  -webkit-backdrop-filter: var(--glass-blur-lg);
}
```

#### Button-in-Button (BiB) CTA

The outer ring is a transparent border; the inner fill is the lit button surface. The two
layers are achieved with `box-shadow` and `outline` so no extra DOM element is needed.

```css
.btn-bib {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14px 32px;
  background: var(--electric);
  color: var(--on-accent);
  border: none;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  /* Outer bezel */
  outline: 1.5px solid rgba(41,188,234,0.45);
  outline-offset: 3px;
  transition: transform 0.3s var(--spring),
              box-shadow 0.3s var(--ease-out),
              outline-offset 0.3s var(--spring);
  will-change: transform;
}

.btn-bib:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--electric-glow);
  outline-offset: 5px;
}

.btn-bib--gold {
  background: var(--gold);
  outline-color: var(--gold-glow);
}

.btn-bib--gold:hover:not(:disabled) {
  box-shadow: var(--glow-gold);
}
```

#### Glass Chips

```css
.chip-glass {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-pill);
  font-family: var(--font-ui);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.chip-glass--electric {
  background: var(--electric-soft);
  border-color: rgba(41,188,234,0.25);
  color: var(--electric);
}

.chip-glass--gold {
  background: var(--gold-soft);
  border-color: rgba(201,168,76,0.30);
  color: var(--gold-bright);
}
```

#### Premium Spinner

```css
.spinner-cinema {
  width: 44px;
  height: 44px;
  border: 3px solid var(--glass-border);
  border-top-color: var(--electric);
  border-radius: 50%;
  animation: cinema-spin 0.8s linear infinite;
}

@keyframes cinema-spin {
  to { transform: rotate(360deg); }
}
```

#### Scroll Reveal Base (toggled by `useReveal.js`)

```css
.reveal {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.6s var(--ease-out),
              transform 0.6s var(--ease-out);
  will-change: opacity, transform;
}

.reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}
```

### File 3: `src/assets/main.css` — Additions

```css
/* Add to existing imports at top */
@import './tokens.css';
@import './cinema.css';
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;600;700&family=Inter:wght@400;500;600;700&display=swap');
```

The existing `Raleway` import is kept as a fallback for the admin panel. The `body`
`font-family` declaration in `base.css` is updated to `var(--font-ui)`.

### File 4: `src/composables/useReveal.js`

```js
import { onMounted, onUnmounted } from 'vue'

/**
 * Attaches IntersectionObserver to elements matching `selector` inside `root`.
 * Each element receives a staggered `transition-delay` of `delayStep * index` ms.
 */
export function useReveal(root, selector = '.reveal', delayStep = 50) {
  let observer

  onMounted(() => {
    const targets = root.value?.querySelectorAll(selector) ?? []
    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible')
            observer.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.12 }
    )
    targets.forEach((el, i) => {
      el.style.transitionDelay = `${i * delayStep}ms`
      observer.observe(el)
    })
  })

  onUnmounted(() => observer?.disconnect())
}
```

### File 5: `src/composables/useMagnetic.js`

```js
/**
 * Adds a magnetic hover effect to `el`.
 * On mousemove the element offsets toward the cursor by `strength` fraction.
 * On mouseleave it springs back to origin.
 */
export function useMagnetic(el, strength = 0.3) {
  function onMove(e) {
    const rect = el.getBoundingClientRect()
    const cx = rect.left + rect.width / 2
    const cy = rect.top + rect.height / 2
    const dx = (e.clientX - cx) * strength
    const dy = (e.clientY - cy) * strength
    el.style.transform = `translate(${dx}px, ${dy}px)`
  }

  function onLeave() {
    el.style.transform = ''
  }

  el.addEventListener('mousemove', onMove)
  el.addEventListener('mouseleave', onLeave)

  // Return cleanup for onUnmounted
  return () => {
    el.removeEventListener('mousemove', onMove)
    el.removeEventListener('mouseleave', onLeave)
  }
}
```

Usage in components: `const cleanup = useMagnetic(btnRef.value)` inside `onMounted`;
call `cleanup()` in `onUnmounted`.


### SVG Icon System — `src/components/icons/`

All icons share the same specification:
- `viewBox="0 0 24 24"`
- `fill="none"` with `stroke="currentColor"` for line icons
- `stroke-width="1.5"`
- `stroke-linecap="round"` and `stroke-linejoin="round"`
- Root element: `<svg width="24" height="24" ...>`, size overridable via CSS `width`/`height`
- Each icon is a single-purpose `.vue` SFC accepting an optional `size` prop (default `24`)

**Catalogue:**

| Component | Replaces | Path data source |
|---|---|---|
| `IconFilm.vue` | `🎬` | Film strip clapperboard |
| `IconTicket.vue` | `🎟️` | Ticket with perforations |
| `IconUser.vue` | `👤` | Person silhouette |
| `IconCard.vue` | `💳` | Credit card |
| `IconSettings.vue` | `⚙️` | Cog / gear |
| `IconBack.vue` | back arrow SVG (already inline in pages) | `M19 12H5 M12 19l-7-7 7-7` |
| `IconStar.vue` | `⭐` inline SVG | Filled polygon star |
| `IconClock.vue` | `⏱` | Clock circle |
| `IconPlay.vue` | play `<path d="M8 5v14l11-7z">` | Filled triangle |
| `IconCheck.vue` | `✓` | Checkmark circle |
| `IconLogout.vue` | `🔓` | Arrow-right-from-box |
| `IconHistory.vue` | `📋` | List lines |
| `IconGlobe.vue` | `🌐` | Globe |
| `IconKey.vue` | `🔑` | Key |

Usage: `<IconFilm :size="20" class="nav-icon" />`. Color is inherited from `currentColor`.

---

### Component Redesign Plan

#### Component 1: Navbar (`home.vue` — `.nav` block)

**Current state:** Plain white sticky bar, emoji logo, flat rectangular nav items.

**Target state:** Fluid Island — a floating pill container centered on desktop, glass morphism,
spring-animated nav items, BiB CTA.

**Key structural changes:**
- Wrap nav contents in an inner `.nav-island` with `border-radius: var(--radius-pill)`,
  `backdrop-filter: var(--glass-blur)`, `background: rgba(5,5,8,0.85)`.
- Replace `🎬 Poly<span>Cinema</span>` with `<IconFilm />` + logotype text using
  `font-family: var(--font-display)`.
- Nav links get `transition: color 0.3s var(--spring)` and a `::after` underline that
  scales in on hover using `transform: scaleX(0) → scaleX(1)`.
- "Đăng ký" CTA becomes `.btn-bib`.
- Dropdown uses `.glass-card` with `box-shadow: var(--shadow-lg)`.
- Emoji in dropdown items replaced with icon components.
- Magnetic hover applied to BiB CTA via `useMagnetic`.

**Performance note:** The nav `backdrop-filter` is applied to `.nav-island` (small element),
not `<nav>` itself, to keep compositing cost minimal.

#### Component 2: Hero / Banner Slider (`home.vue` — `.banner-slider` block)

**Current state:** 300px background-image div, single `linear-gradient`, static text.

**Target state:** `100svh` (min 600px) cinematic stage with multi-layer parallax and film-grain.

**Key structural changes:**
- Container `.hero-stage` becomes `height: 100svh; min-height: 600px; position: relative; overflow: hidden`.
- Background image element is separated into its own `.hero-bg` child with
  `will-change: transform` — the `onScroll` parallax handler sets
  `heroEl.style.transform = \`translateY(\${scrollY * 0.4}px)\`` using `requestAnimationFrame`.
- Layer stack (bottom to top):
  1. `.hero-bg` — background image, `background-size: cover`
  2. `.hero-vignette` — `radial-gradient(ellipse at center, transparent 40%, rgba(5,5,8,0.85) 100%)`
  3. `.hero-fade` — `linear-gradient(to bottom, transparent 50%, var(--void) 100%)`
  4. `.hero-grain` — `opacity: 0.03`, SVG/CSS noise texture, `mix-blend-mode: overlay`
  5. `.hero-content` — text + CTA
- Title: `font-family: var(--font-display); font-size: clamp(48px,7vw,96px); letter-spacing: -0.03em`.
- CTA button: `.btn-bib` + `useMagnetic` on mount.
- Dot navigation: active dot uses `width: 24px; border-radius: var(--radius-pill)` with
  `transition: width 0.4s var(--spring)`.
- The existing `bannerIndex` ref, `startBannerTimer`, and `watch` on `movieStore.banners.length`
  are preserved exactly.

**Performance note:** Parallax uses `transform: translateY()` only — no `top` or `background-position`.
The scroll listener is debounced with `requestAnimationFrame` and removed in `onUnmounted`.

#### Component 3: Movie Cards (`home.vue` — `.movie-card` loop)

**Current state:** `border-radius: 0`, flat white/grey cards, emoji play overlay.

**Target state:** `border-radius: 12px`, `perspective(1000px)` 3D tilt on hover, staggered
entrance via `useReveal`, glass metadata overlay on hover.

**Key structural changes:**
- `.movie-card` gains `border-radius: var(--radius-md)`, `transform-style: preserve-3d`.
- `mousemove` handler computes `rotateX`/`rotateY` from cursor offset within card bounds
  (capped at ±8 deg). Applied as `transform: perspective(1000px) rotateX(Xdeg) rotateY(Ydeg)`.
  `mouseleave` resets to identity.
- Stagger: cards in the grid get `animation-delay: calc(var(--card-index) * 50ms)` via
  inline style binding `:style="{ '--card-index': index }"`.
- Poster gets a double-bezel treatment: an `::after` pseudo-element creates an inset ring
  using `box-shadow: inset 0 0 0 1px rgba(255,255,255,0.12)`.
- `.movie-overlay` becomes a glass slide-up panel using `backdrop-filter: blur(8px)`,
  `background: linear-gradient(to top, rgba(5,5,8,0.9) 0%, rgba(5,5,8,0.4) 100%)`,
  and `transform: translateY(100%) → translateY(0)` on card hover.
- `<IconPlay />` replaces the inline `<svg>` play icon.
- Rating row uses `<IconStar />` + `color: var(--gold-bright)`.
- Genre tags use `.chip-glass--electric`.
- All transitions use `transform` and `opacity` only.

#### Component 4: Auth Page (`AuthPage.vue`)

**Current state:** Flat `#f7f7f7` left panel, `border: 1px solid #efefef` inputs,
rectangular buttons.

**Target state:** Full-screen cinematic split; left panel with void background and animated
radial glow; right panel with glass-morphism form card; floating-label inputs; BiB CTA.

**Key structural changes:**
- `.page` background: `var(--void)`.
- `.left` panel: `background: linear-gradient(135deg, var(--deep) 0%, var(--surface-1) 100%)`,
  with a `::before` pseudo-element `radial-gradient` glow centered at 30%/40% at
  `animation: glow-pulse 4s ease-in-out infinite alternate`.
- Left panel heading switches to `font-family: var(--font-display)`.
- Feature items use `.chip-glass` instead of flat bordered divs, with SVG icons.
- `.auth-card` `.right` becomes a `.glass-card--heavy` card.
- Inputs gain floating-label treatment: the `<label>` starts overlaid at input midpoint
  and animates up + shrinks on focus/fill via `:focus-within` + `:not(:placeholder-shown)`
  CSS. Underline accent uses `::after` with `transform: scaleX(0) → scaleX(1)` on focus.
- All existing `v-model`, `@input`, `@keyup.enter`, `@click`, `:disabled`, `:class` bindings
  are preserved — only the surrounding markup wrapper changes.
- Primary `<button>` becomes `.btn-bib`.
- Social login buttons (Google, Discord) become `.btn-bib` variant with `background: transparent`
  and `outline` border.
- Error/success message boxes use `.glass-card` with semantic text colours.

#### Component 5: Movie Detail Hero (`MovieDetailPage.vue` — `.hero` block)

**Current state:** `min-height: 340px` background image, plain gradient overlay, 110px poster
with `border: 1px solid #efefef`.

**Target state:** `100svh` cinematic stage, large display-typeface title, editorial poster
with double-bezel and ambient glow, sticky glass CTA bar.

**Key structural changes:**
- `.hero` becomes `height: 100svh; min-height: 600px`.
- Same multi-layer overlay stack as the home hero (bg / vignette / fade-to-void).
- `.poster-img` width increases to `160px` on desktop with a double-bezel:
  `box-shadow: 0 0 0 1px rgba(255,255,255,0.15), 0 0 32px rgba(0,0,0,0.6)`.
- An ambient glow `div` is positioned behind the poster using `var(--electric-glow)` — this
  is a blurred pseudo-element, not a computed dominant colour (keeps it static/safe).
- `.movie-title` switches to `font-family: var(--font-display); letter-spacing: -0.03em;
  font-size: clamp(28px,5vw,56px)`.
- Meta chips become `.chip-glass`.
- `.book-bar` (already fixed-bottom) gains `backdrop-filter: var(--glass-blur)`,
  `background: rgba(5,5,8,0.85)`, `border-top: 1px solid var(--glass-border)`.
- `.btn-book` becomes `.btn-bib`.
- All `<script setup>` logic is unchanged.

#### Component 6: Seat Selection (`SeatSelectionPage.vue`)

**Current state:** Flat `34×34px` rectangles on white background, plain coloured states.

**Target state:** Void/deep background, `36×36px` rounded-square seats, per-seat ambient
glow on selection, glass bottom summary bar.

**Key structural changes:**
- `.seat-page` background: `var(--void)`.
- `.top-bar` becomes glass (same pattern as `book-bar` above).
- `.showtime-strip` becomes `.glass-card` with `border-radius: 0; border-left: none; border-right: none`.
- `.screen-bar` gets a vertical perspective taper: `transform: perspective(800px) rotateX(-8deg);
  background: linear-gradient(90deg, transparent, var(--electric), transparent)`.
- `.seat` dimensions: `width: 36px; height: 36px; border-radius: 8px`.
- `.seat--selected` adds `box-shadow: 0 0 12px var(--electric-glow)` — this is a resting
  state shadow applied via class toggle, not during animation, so no paint cost.
- `.seat--vip` uses `var(--gold); box-shadow: 0 0 8px var(--gold-glow)`.
- `.seat--couple` uses a rose token `#ec4899`.
- Legend uses `.chip-glass` items.
- `.bottom-bar` becomes `.glass-card` fixed-bottom with `backdrop-filter`.
- `.btn-next` becomes `.btn-bib`.
- Seat chips in bottom bar use `.chip-glass--electric`.

#### Component 7: Checkout (`CheckoutPage.vue`)

**Current state:** Plain grey `background: #f7f7f7` card panels, flat inputs, flat confirm button.

**Target state:** Glass-morphism section cards, double-bezel price summary card, BiB confirm CTA.

**Key structural changes:**
- `.checkout-page` background: `var(--void)`.
- `.card` sections become `.glass-card`.
- `.card__title` font switches to `var(--font-ui); color: var(--text-primary)` (no emoji).
- `.promo-input` and `.loyalty-input` get the floating-label treatment consistent with auth.
- `.pay-card` becomes `.glass-card` with `border: 1px solid var(--glass-border)`; active
  state gets `border-color: var(--electric); box-shadow: 0 0 0 1px var(--electric-soft)`.
- `.price-card` becomes the double-bezel pattern:
  - Outer wrapper: `border: 1px solid var(--glass-border); border-radius: var(--radius-md)`.
  - Inner wrapper: `border: 1px solid rgba(255,255,255,0.05); border-radius: calc(var(--radius-md) - 4px); margin: 4px`.
- `.btn-confirm` becomes `.btn-bib` with a loading state using `.spinner-cinema` from `cinema.css`.
- Emoji card titles (`📋`, `🎟️`, `⭐`, `💳`) replaced with icon components.
- Payment method icon column: `<IconCard />`, `<IconTicket />`, `<IconClock />` (for Cash).


---

## Animation and Motion System

### Easing Vocabulary

| Token | Value | Use case |
|---|---|---|
| `--spring` | `cubic-bezier(0.34,1.56,0.64,1)` | Hover lift, button press, nav item, card entrance |
| `--ease-out` | `cubic-bezier(0.4,0,0.2,1)` | Scroll reveal, overlay fade, dropdown open |
| `linear` | (native) | Spinner rotation, parallax offset |

### Transition Catalogue

| Element | Property | Duration | Easing |
|---|---|---|---|
| Nav item | `color`, `transform` | `0.3s` | `--spring` |
| Nav item underline `::after` | `transform: scaleX()` | `0.3s` | `--spring` |
| Hero dot nav | `width` | `0.4s` | `--spring` |
| Movie card 3D tilt | `transform` | `0.15s` | `ease-out` |
| Movie card overlay | `transform: translateY`, `opacity` | `0.3s` | `--ease-out` |
| BiB button | `transform: translateY`, `box-shadow`, `outline-offset` | `0.3s` | `--spring` |
| Scroll reveal | `opacity`, `transform: translateY` | `0.6s` | `--ease-out` |
| Stagger card delay | per index × 50ms | — | — |
| Input floating label | `transform: translateY, scale`, `color` | `0.25s` | `--ease-out` |
| Glass dropdown | `opacity`, `transform: translateY` | `0.2s` | `--ease-out` |
| Auth panel glow | `opacity`, `transform: scale` | `4s alternate` | `ease-in-out` |

### Parallax

- **Hero background**: `requestAnimationFrame` scroll listener on `window`.
  `heroEl.style.transform = \`translateY(\${window.scrollY * 0.4}px)\``
  Applied to a dedicated `.hero-bg` child — never to the container.
  Listener attached in `onMounted`, removed in `onUnmounted`.
- **Performance guard**: The listener only runs while `window.scrollY < heroEl.offsetHeight`.

### IntersectionObserver Stagger Reveals

- Every `.section` block on home, detail, and checkout wraps its children in `.reveal` class.
- `useReveal(containerRef)` is called in `onMounted` of each page component.
- Movie grid: `useReveal(gridRef, '.movie-card', 50)` staggers cards at 50ms increments.
- Threshold: `0.12` — elements reveal when 12% is visible.

### 3D Card Tilt (Movie Cards)

```js
function onCardMove(e, el) {
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  const rx = ((e.clientY - cy) / (rect.height / 2)) * -8  // max ±8deg
  const ry = ((e.clientX - cx) / (rect.width / 2)) * 8
  el.style.transform = `perspective(1000px) rotateX(${rx}deg) rotateY(${ry}deg)`
}
function onCardLeave(el) {
  el.style.transform = ''  // spring back via CSS transition
}
```

Attached via `@mousemove` and `@mouseleave` directly in template (no extra composable needed).

### Magnetic Hover

Applied to: BiB CTAs on hero, nav, movie detail book bar.
Strength `0.3` — element moves 30% of cursor offset from its centre.
Cleaned up via the returned function in `onUnmounted`.

---

## Typography System

### Font Loading

```css
/* In main.css */
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Inter:wght@400;500;600;700&display=swap');
```

### Type Scale

| Role | Font | Size | Weight | Tracking |
|---|---|---|---|---|
| H1 hero | Playfair Display | `clamp(48px, 7vw, 96px)` | 700 | `-0.04em` |
| H1 detail | Playfair Display | `clamp(28px, 5vw, 56px)` | 700 | `-0.03em` |
| H2 section | Playfair Display | `clamp(24px, 3vw, 36px)` | 600 | `-0.02em` |
| H3 card | Playfair Display | `16px` | 600 | `0` |
| Auth brand | Playfair Display | `clamp(32px, 4vw, 48px)` | 700 | `-0.03em` |
| Body / UI | Inter | `14px–16px` | 400–600 | `0` |
| Label / meta | Inter | `12px–13px` | 600 | `0.02em` |
| Price / stat | Inter | `18px–24px` | 700 | `0` |
| Button | Inter | `14px–15px` | 700 | `0.01em` |

### Body Defaults (updated in `base.css`)

```css
body {
  font-family: var(--font-ui);
  color: var(--text-secondary);
  background: var(--page-bg);
}
```

---

## Performance Constraints

### GPU-Only Rule

**Every animation and transition in the redesign MUST use only `transform` and `opacity`.**
No animation may trigger layout (no `width`, `height`, `top`, `left`, `padding`, `margin`
changes during animation) or paint (no `background`, `box-shadow`, `color`, `border-color`
changes during animation — only at rest/toggled states via class switches).

### Checklist per Component

| Component | Parallax | Tilt | Reveal | Magnetic | `will-change` applied to |
|---|---|---|---|---|---|
| Hero bg | `translateY` | — | — | — | `.hero-bg` |
| Movie card | — | `perspective(1000px) rotate` | `translateY + opacity` | — | `.movie-card` |
| BiB button | — | — | — | `translate` | `.btn-bib` |
| Nav island | — | — | — | — | none needed (static) |
| Seat buttons | — | — | — | — | none (box-shadow resting only) |

`will-change` is declared sparingly — only on elements where measurement confirms benefit.

### `backdrop-filter` Fallback

For browsers that don't support `backdrop-filter`:
```css
@supports not (backdrop-filter: blur(1px)) {
  .glass-card  { background: var(--surface-2); }
  .nav-island  { background: rgba(5,5,8,0.97); }
  .book-bar    { background: var(--surface-1); }
}
```

### Font Loading Strategy

`display=swap` ensures text is visible immediately with system font fallback.
Playfair Display is only used for headings — FOUT impact is cosmetic only.

---

## Testing Strategy

### Validation Approach

Two-phase approach: first run exploratory tests on **unfixed** code to confirm the current
defective render state, then verify the fix and run preservation checks.

### Exploratory Bug Condition Checking

**Goal**: Confirm that `isBugCondition(X)` currently returns `true` for all pages by
documenting observable defects before any changes.

**Test Plan**: Visual snapshot tests (Vitest + `@vue/test-utils`) that mount each page
component and assert that bug indicators are present in the rendered DOM.

**Test Cases:**
1. **Flat colour test** (will pass on unfixed code, fail on fixed):
   Mount `home.vue`; assert `nav.style.backgroundColor` contains `#ffffff` or that
   the computed style does not use `backdrop-filter`.
2. **Emoji icon test** (will pass on unfixed code, fail on fixed):
   Mount `home.vue`; assert `.logo` text content contains `🎬`.
3. **No display typeface test** (will pass on unfixed code, fail on fixed):
   Assert computed `font-family` of `h1.movie-title` does NOT contain `Playfair`.
4. **No parallax test** (unfixed): Assert no `scroll` event listener is attached to `window`
   by `home.vue` during mount.

**Expected observations on unfixed code:**
- All assertions confirming flat/generic render will pass.
- The exploration confirms root causes 1–6 listed in Hypothesised Root Cause section.

### Fix Checking

**Goal**: Verify that for all `X` where `isBugCondition(X)` holds, the fixed renderer
produces the expected premium output.

```
FOR ALL X WHERE isBugCondition(X) DO
  result := renderPage'(X)
  ASSERT result.hasCinematicColourSystem = true
  AND result.usesSVGIconSystem = true
  AND result.hasMotionChoreography = true
  AND result.usesDualTypefaceSystem = true
  AND result.hasDepthAndLayering = true
  AND result.hasScrollRevealAnimations = true
END FOR
```

**Unit test cases (post-fix):**
1. `tokens.css` loaded → `getComputedStyle(document.documentElement)` exposes `--void: #050508`.
2. `home.vue` nav does NOT contain emoji; contains `<svg>` icon with `stroke-width="1.5"`.
3. `.hero-stage` computed height equals `100svh`.
4. `.movie-card:nth-child(3)` has `transition-delay: 100ms` (index 2 × 50ms stagger).
5. `.btn-bib` has `outline` property set and `cubic-bezier(0.34,1.56,0.64,1)` in its
   computed `transition`.
6. Auth `<input>` wrapper has a `<label>` sibling (floating-label structure).
7. Seat page background colour resolves to `--void` (`#050508`).
8. Checkout `.price-card` has nested double-bezel DOM structure (two `.price-bezel` divs).

### Preservation Checking

**Goal**: Verify that for all `X` where `isBugCondition(X)` is false (all non-visual logic),
the fixed code produces the same runtime result as the original.

```
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT F(X).routingBehavior  = F'(X).routingBehavior
  AND F(X).apiCalls            = F'(X).apiCalls
  AND F(X).storeState          = F'(X).storeState
  AND F(X).authFlows           = F'(X).authFlows
  AND F(X).bookingFlows        = F'(X).bookingFlows
END FOR
```

**Property-based test cases:**
1. **Router guard preservation**: For any random `to.path` in the route table with any
   combination of `meta.requiresAuth` and token presence, the navigation guard must
   resolve identically before and after the fix.
2. **Store action preservation**: For any random sequence of `addSeat` / `removeSeat` calls
   with random seat objects, `bookingStore.selectedSeats` length and `totalSeatPrice` must
   be identical pre- and post-fix.
3. **Booking flow preservation**: For any random `createBooking()` call where the payload
   fields are valid, the API endpoint called and the POST body structure must be identical.
4. **Auth flow preservation**: For any valid login credential, `authStore.login()` must
   return the same success/failure and set the same token.

**Unit test cases (preservation):**
1. Mount `SeatSelectionPage.vue` post-fix; call `bookingStore.addSeat(mockSeat)` eight
   times; assert `isMaxReached.value === true` and the 9th seat click is disabled.
2. Mount `CheckoutPage.vue` post-fix; call `applyPromo('POLY10')`; assert
   `bookingStore.validatePromo` is called with `'POLY10'`.
3. Mount `home.vue` post-fix; verify `movieStore.fetchDangChieu`, `fetchSapChieu`, and
   `fetchBanners` are called on `onMounted`.
4. Mount `AuthPage.vue` post-fix; submit login form; verify `authStore.login` is called
   with trimmed lowercase email.

### Unit Tests

- Verify each SVG icon component renders a `<svg>` with `width="24"` and `stroke-width="1.5"`.
- Verify `useReveal` attaches observer and adds `is-visible` class when threshold met.
- Verify `useMagnetic` applies `translate()` transform on `mousemove` and removes it on `mouseleave`.
- Verify `tokens.css` defines all 40+ custom properties (CSS parsing test).
- Verify `cinema.css` `.glass-card` rule includes `backdrop-filter` declaration.

### Property-Based Tests

- Generate random banner arrays of length 1–10; verify the slider `bannerIndex` cycles
  correctly and never exceeds `banners.length - 1`.
- Generate random seat grids (random rows A–L, random seat counts 1–20 per row); verify
  the `rows` computed property always sorts rows alphabetically and seats numerically.
- Generate random promo codes (alphanumeric strings, 4–12 chars); verify the promo input
  validation trims and uppercases before calling `bookingStore.validatePromo`.

### Integration Tests

- Full booking flow: home → movie detail → seat selection → combo → checkout → payment result.
  Verify the page background is `--void` on each step and all store state is passed correctly.
- Auth flow: register → OTP verify → login. Verify visual panels display correctly and API
  endpoints are hit in the correct sequence.
- Responsive: At 375px width, verify the Fluid Island nav collapses to hamburger; hero title
  `clamp()` resolves to the minimum value; seat grid is horizontally scrollable.


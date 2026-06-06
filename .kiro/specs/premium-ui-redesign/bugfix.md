# Bugfix Requirements Document

## Introduction

The PolyCinema frontend currently renders at the quality level of a generic free template. Every page — home, movie detail, seat selection, checkout, authentication, combos, user profile, ticket history — uses a single flat accent colour (`#29bcea`), border-radius 0–4px rectangles, no layering or depth, emoji substitutes for icons, a single weight of the Raleway typeface, and zero motion choreography beyond basic CSS `transition: 0.2s`. The result is a product that fails to communicate premium positioning, fails to differentiate the brand, and fails to create the cinematic emotional atmosphere that drives conversion in entertainment e-commerce.

The fix is a full elite design-system overhaul: introducing a deep cinematic colour palette with true luminance hierarchy, a dual-typeface editorial system, GPU-safe micro-motion choreography, haptic micro-aesthetics (double-bezel card architecture, Button-in-Button CTAs, spatial rhythm), and page-level motion choreography — transforming every user-facing route into an Awwwards-tier experience while preserving all existing booking, authentication, and payment flows.

---

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN any page is loaded THEN the system renders a single flat accent colour (`#29bcea`) with no luminance hierarchy, no dark cinematic base, and no colour system differentiation between surfaces, elevations, or interactive states.

1.2 WHEN a user views the navigation bar THEN the system displays a plain white sticky bar with a `border-bottom: 1px solid #efefef` and a text-only logo using an emoji (`🎬`), with no visual weight, glass morphism, or brand depth.

1.3 WHEN a user views the hero/banner section THEN the system renders a short 200–300 px background-image div with a single linear-gradient overlay, static text, and a flat `btn-primary` button — providing no cinematic immersion, no parallax, no scroll-driven choreography, and no atmospheric depth.

1.4 WHEN a user browses the movie grid THEN the system renders uniform flat white/grey cards with `border-radius: 0`, a single-colour hover state (`border-color: #29bcea`), and a plain semi-transparent play icon overlay — with no tilt interaction, no staggered entrance animation, no editorial typographic treatment, and no premium poster presentation.

1.5 WHEN a user visits the authentication page THEN the system renders a split card with a flat `#f7f7f7` left panel, plain rectangular inputs with `border: 1px solid #efefef`, rectangular buttons, and no spatial depth — providing no premium brand experience at the highest-friction conversion point.

1.6 WHEN a user views the movie detail hero THEN the system renders a `340px` min-height background image with a plain linear gradient, a small `110px` poster with `border: 1px solid #efefef`, and text aligned in a basic flex row — with no cinematic parallax, no layered editorial typography, and no editorial poster treatment.

1.7 WHEN a user opens the seat selection page THEN the system renders seats as flat `34×34px` rectangles with `border: 1px solid rgba(255,255,255,0.1)` and plain colour-fill states, on a plain `#ffffff` background — with no spatial stage metaphor, no ambient glow on selected seats, and no haptic-class interaction feedback.

1.8 WHEN a user reaches the checkout page THEN the system renders plain grey card panels with `background: #f7f7f7; border: 1px solid #efefef` and flat rectangular inputs — providing no premium purchase experience, no progressive trust signals, and no motion that reinforces the significance of the transaction.

1.9 WHEN interactive elements (buttons, cards, links) are hovered or focused THEN the system responds with a flat background-colour swap at `transition: 0.2s` — with no magnetic repulsion, no scale choreography, no spatial lift, and no typographic state change.

1.10 WHEN any page scrolls THEN the system provides no scroll-linked animation, no stagger reveal, no scroll-interpolated parallax, and no cinematic entrance choreography for content sections.

1.11 WHEN icon elements are needed THEN the system substitutes Unicode emoji characters (`🎬`, `🎟️`, `👤`, `💳`) rather than a coherent SVG icon system — producing visual inconsistency, non-scalable rendering, and a non-premium aesthetic.

1.12 WHEN typography is applied across all pages THEN the system uses a single typeface (`Raleway`) at only two weights (400 and 700) with no variable optical sizing, no editorial display treatment, no tracking modulation, and no hierarchical type scale — producing typographic monotony throughout the entire product.

### Expected Behavior (Correct)

2.1 WHEN any page is loaded THEN the system SHALL apply a deep cinematic design token system with a layered colour palette (`--void: #050508`, `--deep: #0a0a0f`, `--surface-1` through `--surface-4` with progressive luminance), a gold/amber accent tier (`--gold: #C9A84C`, `--gold-bright: #F5D17E`), an electric accent tier (`--electric: #29bcea`, `--electric-glow`), semantic text tokens (`--text-primary`, `--text-secondary`, `--text-ghost`), and full GPU-safe shadow/glow tokens — establishing a coherent cinematic visual language.

2.2 WHEN a user views the navigation bar THEN the system SHALL render a "Fluid Island" navigation: a floating pill-shaped container with `backdrop-filter: blur(20px)` glass morphism, `background: rgba(5,5,8,0.85)`, a logotype using the display typeface at proper weight, magnetic-hover nav items with a `0.3s cubic-bezier(0.34,1.56,0.64,1)` spring, and a CTA button using the Button-in-Button double-bezel pattern.

2.3 WHEN a user views the hero/banner section THEN the system SHALL render a full-viewport cinematic stage with a `100svh` height (min 600px), a multi-layer parallax: background image on transform layer, a `radial-gradient` atmospheric vignette, a `linear-gradient` bottom fade-to-void, and animated film-grain texture at 3% opacity. The title SHALL use the display typeface at `clamp(48px, 7vw, 96px)`, and the CTA SHALL use the Button-in-Button pattern with a subtle ambient glow pulse. Dot navigation SHALL use progressive width morphing with a spring transition.

2.4 WHEN a user browses the movie grid THEN the system SHALL render editorial movie cards with: `border-radius: 12px`, a `perspective: 1000px` 3D tilt on hover (max ±8deg), a staggered `@keyframes` entrance with `animation-delay` per card index, a double-bezel inner ring on the poster, a `backdrop-filter` metadata overlay that slides up on hover, genre tags using coloured glass chips, and a gold star rating treatment — all with GPU-composited transforms only.

2.5 WHEN a user visits the authentication page THEN the system SHALL render a full-screen cinematic split: left panel with the void background, an animated radial glow, and display-typeface brand messaging; right panel with glass-morphism form card (`backdrop-filter: blur(40px)`, `background: rgba(255,255,255,0.04)`), floating-label inputs with an animated underline accent, and a Button-in-Button primary CTA. All state transitions SHALL use `transition: all 0.4s cubic-bezier(0.4,0,0.2,1)`.

2.6 WHEN a user views the movie detail hero THEN the system SHALL render a `100svh` cinematic stage with a full-bleed backdrop image, a layered editorial layout: large display-typeface title with letter-spacing `-0.03em`, a metadata ribbon using glass chips, a premium poster frame with double-bezel treatment and age-rating badge, and an ambient colour-extracted glow behind the poster. The book-ticket CTA SHALL use the Button-in-Button pattern and be fixed in a glass-morphism bottom bar on scroll.

2.7 WHEN a user opens the seat selection page THEN the system SHALL render a spatial cinema stage metaphor: a curved `perspective` screen bar with a glow effect, seats as `36×36px` rounded-square buttons with `border-radius: 8px`, per-state ambient glow (selected seats emit a soft `box-shadow: 0 0 12px var(--electric-glow)`), VIP seats use the gold token, couple seats use a soft rose token, and the legend uses the glass chip pattern. The bottom summary bar SHALL use glass morphism.

2.8 WHEN a user reaches the checkout page THEN the system SHALL render sections as glass-morphism cards with `background: rgba(255,255,255,0.03)`, `border: 1px solid rgba(255,255,255,0.08)`, and `backdrop-filter: blur(16px)`. The price summary sticky card SHALL use the double-bezel outer-ring pattern. The confirm CTA SHALL use the Button-in-Button pattern with a loading state that uses a premium spinner animation.

2.9 WHEN interactive elements are hovered or focused THEN the system SHALL respond with: buttons using a magnetic offset transform (`translate(x*0.3, y*0.3)`) relative to cursor position, cards using a `perspective(1000px) rotateX/Y` tilt with `transition: transform 0.15s ease-out`, and all interactive state changes using `cubic-bezier(0.34,1.56,0.64,1)` spring easing — producing haptic-class feedback without any layout reflow.

2.10 WHEN any page scrolls THEN the system SHALL trigger `IntersectionObserver`-driven stagger reveals: content sections enter with `translateY(24px) → translateY(0)` and `opacity: 0 → 1` at `0.6s cubic-bezier(0.4,0,0.2,1)`, movie cards stagger by `50ms` per index, and the hero background SHALL apply a `transform: translateY(scrollY * 0.4)` GPU-composited parallax — all without triggering layout or paint.

2.11 WHEN icon elements are needed THEN the system SHALL use a coherent inline SVG icon system with consistent `24×24` viewport, `1.5` stroke-width, rounded line-caps, and icon tokens aligned to the type scale — replacing all emoji usage in navigation, feature lists, dropdown menus, and form labels.

2.12 WHEN typography is applied THEN the system SHALL use a dual-typeface system: a display typeface (Playfair Display or Cormorant Garamond) for all headings H1–H3 and hero text with variable optical sizing `clamp()` expressions and tracking `−0.03em` to `−0.05em`; and a text typeface (Inter or DM Sans) for all body, UI labels, metadata, and form elements at 14–16px — establishing clear editorial hierarchy across all pages.

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a user navigates between all existing routes (`/`, `/phim/:id`, `/seat-selection/:showtimeId`, `/combo`, `/checkout`, `/auth`, `/profile`, `/my-tickets`, `/transaction-history`, `/payment-result/:bookingId`, `/admin`) THEN the system SHALL CONTINUE TO route correctly using Vue Router with all existing route guards and redirect logic intact.

3.2 WHEN a user logs in, registers, verifies OTP, resets password, or uses OAuth (Google/Discord) THEN the system SHALL CONTINUE TO execute all authentication flows with the same API endpoints, validation rules, success redirects, and error handling — only the visual presentation of the auth forms changes.

3.3 WHEN a user selects a movie, showtime, date, and seats THEN the system SHALL CONTINUE TO use all existing Pinia store state (`bookingStore`, `movieStore`, `authStore`) with the same state shape, mutations, getters, and API calls — only the visual components consuming the store change.

3.4 WHEN a user applies a promo code, uses loyalty points, or selects a payment method (VNPay, MoMo, Cash) THEN the system SHALL CONTINUE TO execute the checkout flow with the same `bookingStore.createBooking()`, `bookingStore.validatePromo()`, and payment redirect logic — only the visual layout of the checkout page changes.

3.5 WHEN the admin navigates to `/admin` THEN the system SHALL CONTINUE TO render the admin panel with its existing `AdminDashboard.vue` and all sub-pages (`DashboardPage`, `MoviesPage`, `SchedulePage`, etc.) — the admin UI is explicitly out of scope for this redesign.

3.6 WHEN a user's browser or device is in dark mode or when the `ThemeToggle` is used THEN the system SHALL CONTINUE TO apply the existing `themeStore` logic — the new design token system SHALL be compatible with both light and dark theme tokens, with the dark/cinematic palette being the primary theme.

3.7 WHEN the application is viewed on mobile (max-width 768px) or tablet (max-width 1024px) THEN the system SHALL CONTINUE TO render all pages responsively using the same mobile breakpoints — the premium design SHALL apply across all viewport sizes.

3.8 WHEN the banner slider auto-advances THEN the system SHALL CONTINUE TO use the existing `setInterval`-based timer in `movieStore` and the existing dot navigation controls — only the visual treatment of the slider changes.

3.9 WHEN a user's seat selection reaches 8 seats THEN the system SHALL CONTINUE TO display the maximum seat warning and disable further selection — only the visual presentation of the warning changes.

3.10 WHEN API calls fail or data is loading THEN the system SHALL CONTINUE TO show loading spinners and error states on all pages — the spinners and error UI SHALL be restyled to match the premium design system but SHALL preserve all existing retry, fallback, and conditional rendering logic.

---

## Bug Condition Pseudocode

**Bug Condition Function** — identifies any UI render that fails the premium standard:

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type UIRenderState
  OUTPUT: boolean

  RETURN (
    X.usesGenericColourSystem = true  // flat single-colour #29bcea only
    OR X.usesEmojiAsIcons = true      // Unicode emoji in place of SVG icons
    OR X.hasNoMotionChoreography = true  // transition: 0.2s ease only
    OR X.usesMonotypeSystem = true    // single typeface, 2 weights
    OR X.lacksDepthSystem = true      // no blur, no layering, no glass morphism
    OR X.hasNoScrollAnimations = true // no IntersectionObserver-driven reveals
  )
END FUNCTION
```

**Property: Fix Checking**
```pascal
FOR ALL X WHERE isBugCondition(X) DO
  result ← renderPage'(X)
  ASSERT result.hasCinematicColourSystem = true
  AND result.usesSVGIconSystem = true
  AND result.hasMotionChoreography = true
  AND result.usesDualTypefaceSystem = true
  AND result.hasDepthAndLayering = true
  AND result.hasScrollRevealAnimations = true
END FOR
```

**Property: Preservation Checking**
```pascal
FOR ALL X WHERE NOT isBugCondition(X) DO
  // Non-visual logic (routing, API, state management) must be unchanged
  ASSERT F(X).routingBehavior = F'(X).routingBehavior
  AND F(X).apiCalls = F'(X).apiCalls
  AND F(X).storeState = F'(X).storeState
  AND F(X).authFlows = F'(X).authFlows
  AND F(X).bookingFlows = F'(X).bookingFlows
END FOR
```

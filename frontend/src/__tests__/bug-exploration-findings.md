# Bug Exploration Findings — Premium UI Redesign

**Spec:** `premium-ui-redesign`  
**Task:** 1.2 — Document counterexamples and confirm all six bug indicators  
**Date generated:** post-task-1.1 (all 7 exploration tests PASSED)

---

## Summary

All six bug indicators are confirmed present in the current (unfixed) codebase.
The exploration PBT (task 1.1) ran 7 tests and every assertion passed, meaning every
defect it was looking for was found. The source-level evidence below cross-references each
test assertion with the exact file, line, and code snippet that causes it.

---

## Bug Indicator 1 — Emoji logo instead of SVG icon

**Requirement:** `bugfix.md §1.11` — emoji characters used in place of SVG icons  
**Test assertion:** `expect(wrapper.find('.logo').text()).toContain('🎬')`

### Source evidence

**File:** `frontend/src/view/home.vue`  
**Line 4 (template):**

```html
<router-link to="/" class="logo">🎬 Poly<span>Cinema</span></router-link>
```

The `🎬` Unicode emoji is embedded directly in the anchor text of the `.logo` element.
The same pattern is repeated throughout the file for other navigation items:

| Line | Element | Emoji |
|------|---------|-------|
| 24 | dropdown-item (Profile) | `👤` |
| 25 | dropdown-item (My Tickets) | `🎟️` |
| 26 | dropdown-item (Transaction History) | `📋` |
| 27 | dropdown-item (Admin Panel) | `⚙️` |
| 30 | dropdown-item (Logout) | `🔓` |
| 55 | drawer lang toggle | `🌐` |
| 59 | drawer login link | `🔑` |
| 60 | drawer register link | `📝` |
| 94 | poster-placeholder | `🎬` |
| 109 | movie-info duration | `⏱` |
| 110 | movie-info rating | `⭐` |

**No SVG icon system exists** — searching `frontend/src/**/*.vue` for `glass-card`, `Playfair`, `nav-island`, or `050508` returns zero results.

**Consistent with test output:** ✅ `BUG-1` passed — `.logo` text contains `🎬`.

---

## Bug Indicator 2 — No glass morphism on navigation bar

**Requirement:** `bugfix.md §1.2` — plain white sticky nav, no glass morphism, no visual weight  
**Test assertion:** `expect(wrapper.find('.nav-island').exists()).toBe(false)`  
**Test assertion:** `expect(navStyle).toBe('')` (no inline `backdrop-filter`)

### Source evidence

**File:** `frontend/src/view/home.vue`  
**Lines 3–35 (template nav block):**

```html
<nav class="nav">
  <router-link to="/" class="logo">🎬 Poly<span>Cinema</span></router-link>
  <div class="nav-actions">
    ...
  </div>
  <button class="hamburger" ...>...</button>
</nav>
```

The nav element uses class `nav` only — there is **no `.nav-island` wrapper element** inside or around it.

**Lines in `<style scoped>` — nav CSS (lines ~238–250):**

```css
.nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 40px;
  background: var(--surface-plain);    /* resolves to #ffffff in light theme */
  border-bottom: 1px solid var(--border); /* #efefef */
  position: sticky;
  top: 0;
  z-index: 100;
}
```

No `backdrop-filter`, no `blur()`, no `rgba` transparency, no pill/island shape. The nav is a
plain white full-width bar with a 1px bottom border — exactly the defect described in `bugfix.md §1.2`.

**Consistent with test output:** ✅ `BUG-2` passed — `.nav-island` absent, nav has no `backdrop-filter`.

---

## Bug Indicator 3 — Single typeface (no Playfair Display)

**Requirement:** `bugfix.md §1.12` — single typeface `Raleway` at two weights, no editorial display font  
**Test assertion:** `expect(html).not.toContain('Playfair')`  
**Test assertion:** `expect(sectionTitle.element.className).not.toContain('playfair')`

### Source evidence

**File:** `frontend/src/assets/main.css`  
**Lines 4–13:**

```css
@import url('https://fonts.googleapis.com/css2?family=Raleway:wght@400;700&display=swap');

html {
  font-family: 'Raleway', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

body {
  font-family: 'Raleway', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
```

`Raleway` only. `Playfair Display` (or any display typeface) is **not imported anywhere** in the project.

**File:** `frontend/src/view/home.vue`  
**Line in `<style scoped>` (`.home` rule):**

```css
.home {
  font-family: 'Raleway', sans-serif;
  ...
}
```

The entire home page explicitly inherits `Raleway`. There is no `var(--font-display)` token, no
`font-family: 'Playfair Display'` rule, and no class name containing `playfair` anywhere in the codebase
(`grep` across all `.vue` and `.css` files returns zero matches for `Playfair` and `050508`).

**File:** `frontend/src/assets/base.css`  
**Lines 45–48:**

```css
body {
  font-family: 'Raleway', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 14.4px;
  ...
}
```

**Consistent with test output:** ✅ `BUG-3` passed — rendered HTML contains no `Playfair` string.

---

## Bug Indicator 4 — No scroll event listener / no parallax

**Requirement:** `bugfix.md §1.10` — no scroll-linked animation, no parallax  
**Test assertion:** `expect(scrollCalls).toHaveLength(0)`

### Source evidence

**File:** `frontend/src/view/home.vue`  
**`onMounted` block (lines 213–219):**

```js
onMounted(() => {
  movieStore.fetchDangChieu()
  movieStore.fetchSapChieu()
  movieStore.fetchBanners()
  document.addEventListener('click', closeDropdown)   // ← only click, no scroll
})
```

**`onUnmounted` block (lines 221–225):**

```js
onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
  document.removeEventListener('click', closeDropdown)   // ← only click cleanup
})
```

The only `addEventListener` call in `onMounted` registers a `'click'` handler for the dropdown
close logic. There is **no `window.addEventListener('scroll', ...)` call** anywhere in `home.vue`.
There is no `requestAnimationFrame` loop, no `IntersectionObserver`, no `useMagnetic`, and no
`useReveal` composable referenced — because those composables do not exist yet.

**Consistent with test output:** ✅ `BUG-4` passed — zero `scroll` listeners found on `window` after mount.

---

## Bug Indicator 5 — No cinematic void background (`#050508`)

**Requirement:** `bugfix.md §1.1` — flat single accent colour, no dark cinematic base  
**Test assertion:** `expect(isVoidColour).toBe(false)` where `isVoidColour = bg === 'rgb(5,5,8)' || bg === '#050508'`

### Source evidence

**File:** `frontend/src/assets/base.css`  
**Light theme (`:root, [data-theme='light']`) lines 1–17:**

```css
:root,
[data-theme='light'] {
  --page-bg: #ffffff;          /* ← pure white, not void */
  --accent: #29bcea;            /* ← single flat accent only */
  ...
}
```

**Dark theme (`[data-theme='dark']`) lines 19–34:**

```css
[data-theme='dark'] {
  --page-bg: #0f1419;           /* ← dark blue-grey, still NOT #050508 */
  ...
}
```

Neither theme token resolves `--page-bg` to the cinematic void `#050508`. The light theme uses
`#ffffff` and the dark theme uses `#0f1419`.

**File:** `frontend/src/view/home.vue` — `.home` rule:

```css
.home {
  background: var(--page-bg);   /* → #ffffff in light, #0f1419 in dark */
}
```

`--void`, `--deep`, `--surface-1` through `--surface-4`, `--gold`, `--electric-glow`, `--glass-bg`,
`--glass-blur`, `--spring`, `--radius-pill`, `--font-display`, `--font-ui` — **none of these tokens
exist** in the codebase. No `tokens.css` file exists at `frontend/src/assets/tokens.css`.

**Consistent with test output:** ✅ `BUG-5` passed — background is not `rgb(5,5,8)` / `#050508`.

---

## Bug Indicator 6 — No `.glass-card` in rendered DOM

**Requirement:** `bugfix.md §1.2, 1.5, 1.8` — no glass morphism, no depth system  
**Test assertion:** `expect(wrapper.findAll('.glass-card')).toHaveLength(0)`

### Source evidence

A global search for `glass-card` across all files in `frontend/src/` (`.vue`, `.css`, `.js`) returns
**zero matches** (excluding the test file itself).

**File:** `frontend/src/view/home.vue` — `.dropdown` rule in `<style scoped>`:

```css
.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: #ffffff;           /* ← plain white, not glass-morphism */
  border: 1px solid #efefef;     /* ← flat 1px border */
  border-radius: 4px;            /* ← 4px, not var(--radius-md) = 12px */
  min-width: 220px;
  z-index: 200;
}
```

**File:** `frontend/src/view/home.vue` — `.movie-card` rule:

```css
.movie-card {
  background: var(--surface);    /* ← flat surface, no glass */
  border-radius: 0;              /* ← zero radius */
  border: 1px solid var(--border);
}
```

No `backdrop-filter`, no `rgba` semi-transparent backgrounds with blur, no `--glass-bg` token, and
no `.glass-card` class exists anywhere in the source. The `cinema.css` pattern library has not
been created yet.

**Consistent with test output:** ✅ `BUG-6` passed — `.glass-card` is absent from rendered DOM.

---

## Composite / PROPERTY test confirmation

The seventh test (`PROPERTY: all six bug indicators are simultaneously present`) verified all indicators
in a single render pass:

| Check | Assertion | Source location |
|-------|-----------|-----------------|
| `🎬` in `.logo` text | `toContain('🎬')` | `home.vue` line 4 |
| `.nav-island` absent | `.exists() === false` | No such element in template |
| `Playfair` not in HTML | `not.toContain('Playfair')` | No import in any `.css` or `.vue` |
| `050508` not in HTML | `not.toContain('050508')` | Not in `base.css` or any source |
| `.glass-card` absent | `toHaveLength(0)` | Class does not exist in source |

The scroll-listener absence (indicator 4) is confirmed by the dedicated `BUG-4` test (separate
re-mount with spy) rather than the composite test, because it requires a fresh mount with a clean spy.

---

## Conclusion

All six bug indicators from `bugfix.md §1.1–1.12` are simultaneously and concretely present in
the current codebase. The exploration PBT results are 100% consistent with the source evidence above.
Implementation should proceed to Wave 2: creating `tokens.css` (task 2) and `cinema.css` (task 3).

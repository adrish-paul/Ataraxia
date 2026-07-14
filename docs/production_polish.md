# ATARAXIA — PRODUCTION POLISH & ENGINEERING STANDARDS

> This document specifies the non-functional benchmarks, visual details, and quality checks required to transform Ataraxia into a premium-tier wellness application.

---

## 1. Interaction & Visual Standards

### Animation Profiles
*   **Transitions:** Must use slow, flowing interpolation resembling breathing or drifting. Avoid snappy curves, spring bounces, and gaming effects.
*   **Duration Guidelines:**
    *   *Button Press:* `150ms` (gently scales down to `97%` scale, subtle haptic pulse).
    *   *Screen Navigation:* `350ms - 400ms` (combined fade and horizontal slide, resembling a physical journal page turning).
    *   *Staggered Feed:* Cards fade upward sequentially when pages open (`400ms` staggered layout entry).
    *   *Theme Switching:* Seamless crossfade. Avoid blinking black/white states.

### Sound & Haptics
*   **Ambient Loops:** Smoothly fade in over `2000ms` when played and fade out over `2000ms` when paused or stopped. No abrupt silent cuts.
*   **Button Sounds:** Soft, gentle click-feedback that is almost inaudible. Disabled by default.
*   **Subtle Haptics:** Single gentle vibration pulse on key moments: selecting a mood chip, pressing main buttons, saving a journal page, starting breathing sessions, or completing focus periods.

---

## 2. Core State Visualizers

### Custom Loading Screen (`GentleLoading`)
*   **Important:** Never use default circular spinning progress bars.
*   Use a continuous loop of drifting petals or slowly breathing dots fading in and out.
*   Display comforting sub-text: `"Preparing your quiet space..."` or `"Just a gentle moment..."`.

### Empty States (`EmptyState`)
*   Provide nature-inspired illustrations (e.g., notebook, lotus, clouds) and gentle invitations:
    *   *Journal:* `"Your first page is waiting for you."`
    *   *Focus:* `"A quiet moment can begin anytime."`
    *   *Breathe:* `"Take one gentle breath to begin."`

### Error States
*   Do not display stack traces, tech jargon, or "Failed" alerts.
*   Display a friendly placeholder: `"Something quietly interrupted this moment. Let's try again."` with a primary `Retry` button.

---

## 3. Battery & System Performance

*   **Animation Suspension:** Pause all graphics, particles, and lotus loops when the application moves into the background.
*   **Audio Release:** Release audio player resources when sessions end or when the app is minimized to save device battery.
*   **Compose Optimizations:** Use `remember`, `rememberSaveable`, and `derivedStateOf` to prevent redundant UI thread recompositions. 
*   **Cold Startup:** Under `2.0` seconds on API 31+ devices. Maintain solid `60fps` scrolling.

---

## 4. App Resume & State Preservation

Returning to the app must restore state seamlessly:
*   Unsaved journal editor drafts are kept intact.
*   Active focus timers or breathing sessions resume from pause states.
*   The active theme and floating bottom nav tab selection persist.
*   Scroll positions of timelines are remembered.

---

## 5. Production Release Checklist

Verify all items before shipping:
*   [ ] **No Crashes:** Clean try-catches around Room database transactions and Preference reads.
*   [ ] **No Placeholders:** All placeholder text, layout coordinates, and `lorem ipsum` blocks are replaced.
*   [ ] **Resource Check:** Zero hardcoded colors, spacing dimensions, or string texts (use `Color.kt`, `dimens.xml`, `strings.xml`).
*   [ ] **Accessibility Compliance:** Touch targets are `48.dp` minimum, dynamic fonts scale cleanly, and TalkBack descriptions are added to all illustrations and icons.
*   [ ] **Permissions Flow:** Notification runtime permissions are requested *only* inside settings or after explicit user consent.

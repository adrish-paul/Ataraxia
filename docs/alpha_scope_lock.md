# ATARAXIA — ALPHA SCOPE LOCK

> This scope lock document acts as the boundary contract for the Ataraxia Alpha implementation. Features not listed in the "Included" list are deferred to post-Alpha releases to prevent scope creep and guarantee high design and code polish.

---

## 🌸 Included in Alpha Release

### 1. Onboarding & First Launch Flow
*   Time-cached Splash Screen (petals, fades).
*   Name input screen (optional, up to 30 chars).
*   Notification request page (explains why, politely asks before triggering system dialog).
*   Completion flag saved in Preferences DataStore.

### 2. Base App Layout & Shell
*   Consistent Top Bar (Logo left, circular avatar right).
*   Floating bottom navigation bar (Home, Journal, Breathe, Focus, Me, 32dp corner radius, 16dp bottom margin, Breathe icon 15% larger).
*   Time-aware low-opacity background on Home Screen shifting through Morning, Afternoon, Evening, and Night.

### 3. Home Screen (Sanctuary Dashboard)
*   Dynamic time/name greetings and local date indicator.
*   Daily Quote/Affirmation card (cached, updates once per 24 hours).
*   Mood Check-In grid (Peaceful, Calm, Heavy, Tired, Hopeful, Grateful). Updates state locally in DataStore/Room.
*   Quick Action 2x2 grid.
*   Continue Session Card (conditional).
*   Today's Reflection prompt card (leads to pre-filled journal).
*   Daily Calm Tip card (gentle suggestion).

### 4. Journal Module (Diary thoughts)
*   Timeline view (LazyColumn, sorted newest first).
*   Time & Weather contextual stamp (e.g. `🌧️ Rainy Evening • 8:42 PM`) saved with each entry.
*   Editor: Single-pane multiline TextField, horizontal mood chip scroller, title, `Let Go` / `Keep This Page` actions.
*   Automatic background drafting and autosave every 10 seconds.
*   Safety confirmation dialogs on exit/discard.
*   Reader view (large margins, comfortable typography, overflow edit/delete settings).
*   Room Database implementation.

### 5. Breathe Module (Sanctuary breathing)
*   Method Cards: Calm Breathing, Box Breathing, 4-7-8 Breathing, Gentle (custom user timing).
*   Lotus Guided Animation: Visual scaling guide (Opens -> Inhale, Holds -> Hold, Closes -> Exhale, Rests -> Rest).
*   Thin glowing circular progress tracker.
*   Ambient background sounds selection panel (Rain, Ocean, Forest, Wind, Fireplace, Night) with master volume slider.
*   Session history database log (method, duration, timestamp).

### 6. Focus Module (Quiet Time study)
*   Duration Selection chips (15m, 25m, 45m, 60m, 90m, Custom).
*   Focus Intention Cards grid (Studying, Coding, Reading, Writing, Creating, Something Else).
*   Atmosphere Loops selectors.
*   **Immersive Sanctuary Mode:** Active timer layout fading top/bottom bars, centering the soft white countdown, and keeping screen awake.
*   Local database history logger.

### 7. Me & Settings Module
*   Profile details (photo, custom display name).
*   Local summary statistics totals (Days, entries, focus, breathing sessions completed). No streaks.
*   Personalization Settings: Light, Dark, AMOLED, and Material You toggle changes.
*   Central Notifications Settings time picker and Quiet Hours toggle.
*   Volume controls & Haptics toggle.
*   App Lock: Device Biometrics or PIN credentials check on app startup.

---

## 🚫 Excluded from Alpha Release (Post-Alpha Roadmap)

*   ❌ **AI reflections, summaries, & coach suggestions:** Keep AI integration out of Alpha.
*   ❌ **Cloud Sync & Remote Databases:** Zero cloud dependencies. No email/account registration flows, logins, or social integration. All storage is offline-first.
*   ❌ **Meditation Audio libraries / Sleep Stories:** Only static ambient loops are played.
*   ❌ **Timeline search bars, search widgets, & tagging/filtering:** Keep lists raw and simple.
*   ❌ **Calendar Visual Dashboards / History graphs:** Keep logs to a simple text-based vertical timeline.
*   ❌ **Gamification elements:** Absolutely no streaks, XP, level badges, coins, or leaderboards.
*   ❌ **Multi-language support:** English default for Alpha.
*   ❌ **Tablet / Wear OS / Desktop widgets:** Restrict focus to standard Android phones in portrait layout.

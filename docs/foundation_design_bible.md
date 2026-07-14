# ATARAXIA — FOUNDATION & DESIGN BIBLE

> This document acts as the core technical and structural specification for Ataraxia. All future prompts, folder structures, database models, viewmodels, and screen code must adhere to these specifications.

---

## Technical Specifications

| Requirement | Specification |
| :--- | :--- |
| **Platform** | Android |
| **IDE** | Android Studio |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Architecture** | MVVM (Model-View-ViewModel) with modular structure |
| **Navigation** | Navigation Compose |
| **Local Storage** | Room Database & DataStore Preferences |
| **Minimum SDK** | Android 12 (API 31) |
| **Target SDK** | Latest Stable |

---

## Core Features (Alpha)
Ataraxia combines the following features into a cohesive, distraction-free sanctuary:
1.  **Journaling:** A simple, gentle space to write and store pages locally.
2.  **Meditation:** Peaceful guided or self-guided experiences.
3.  **Guided Breathing:** Slow, visual breathing indicators (4-6s duration).
4.  **Focus Sessions:** A tool to remove distractions and build calm presence.
5.  **Ambient Sounds:** Seamlessly looped natural sounds that fade in and out.
6.  **Mood & Emotional Reflection:** Gentle check-ins for the user's emotional state.

---

## Architecture & Code Organization

### Folder Structure
Code will be organized under `com.ataraxia` with a clear layer-based separation:

```
com.ataraxia
├── ui
│   ├── screens       # Compose screen implementations (Home, Journal, etc.)
│   ├── components    # Reusable Lunaflora UI elements (Buttons, Cards, Chips)
│   ├── animations    # Custom Compose breathing and petal transitions
│   ├── navigation    # Navigation graph and host setup
│   └── theme         # Color scheme, shapes, typography (Lunaflora Theme)
├── data
│   ├── database      # Room DB, Entity classes, and DAOs
│   ├── datastore     # Preferences DataStore for simple configurations
│   └── repository    # Repository implementations (SSOT for data)
├── domain
│   ├── model         # Clean domain entities
│   └── usecase       # Clean domain business logic units
├── viewmodel         # UI state holders and event handlers
└── util              # General utilities, extensions, and helper functions
```

### Naming Conventions
*   **Screens:** `<Feature>Screen.kt` (e.g., `HomeScreen`, `JournalScreen`)
*   **Components:** `<Purpose><Type>.kt` (e.g., `MoodChip`, `QuoteCard`, `FocusTimer`, `LotusAnimation`)
*   **Avoid:** Generic or numbered names like `Component1`, `ScreenA`, `RandomButton`.

---

## Interface & Layout Rules

*   **Generous Whitespace:** The interface must breathe. No screen should feel crowded.
*   **Simple Composition:** A maximum of **5 major sections** on one page. Remove all unnecessary elements.
*   **Navigation:** Bottom navigation only with exactly five tabs. No side drawers or hamburger menus.
    *   🏠 **Home**
    *   📖 **Journal**
    *   🌸 **Breathe**
    *   ⏳ **Focus**
    *   👤 **Me**
*   **Top Bar Structure:** Every main screen uses the identical top bar setup:
    *   **Left:** App Logo / Brand Text ("Ataraxia")
    *   **Right:** Profile Avatar / Icon (`👤`)
    *   *Never* include settings, notifications, search, overflow, or filtering in the top bar.

---

## UI Component Specifications (Lunaflora Language)

### Floating Action Button (FAB)
*   **Rule:** FAB is allowed **only** on the **Journal Screen** (for creating a entry). No other screen may use a FAB.

### Cards (Containers)
*   **Corner Radius:** `28.dp`
*   **Shadow:** Soft, diffused shadow.
*   **Internal Padding:** `20.dp`
*   **Appearance:** Must look "floating" and soft. Never flat rectangles.

### Buttons
*   **Primary Button:** Filled, styled with a soft pastel gradient, height `56.dp`, corner radius `28.dp`. Expands to match parent width (minus screen margins) and is centered.
*   **Secondary Button:** Outlined with a soft border.
*   **Danger Button:** Muted Rose (never bright red).

### Typography & Icons
*   **Typography:** Large readable headings, comfortable line heights, no all-caps, and highly legible body text sizes.
*   **Icons:** Outlined, rounded, soft shapes. Avoid harsh or heavy filled icons.
*   **Illustrations:** Soft nature-inspired drawings (Lotus, Moon, Clouds, Lanterns, Petals). No cartoon mascots or memes.

---

## Motion, Sound & Notification Rules

### Animation Timings
Animations should feel like wind, flowing water, or floating petals. No bouncing, spinning, or flashy elements.
*   **Fade:** `300ms`
*   **Card Transition:** `350ms`
*   **Screen Transition:** `400ms`
*   **Breathing Animation Cycle:** `4000ms – 6000ms`
*   **Button Press:** `150ms`

### Sound & Notifications
*   **Button Sounds:** Extremely soft, subtle, and optional.
*   **Ambient Sound Loops:** Must loop seamlessly, fading in and out on play/stop with no abrupt volume jumps.
*   **Notification Philosophy:** No icons in status bars, no badges, no banners, and no permission prompts until settings are visited. Quiet by default.

---

## Data, Privacy & Copywriting

### Data Philosophy
*   **Local-First:** Everything remains on the device (Room / DataStore) for Alpha.
*   **Privacy-First:** No accounts, logins, analytics, tracking, or cloud storage required.

### Tone of Voice & Copywriting
*   All copy is written from the perspective of a warm, kind friend.
*   **Avoid:** Save, Delete, Start, Completed, Expired, Error, Failed.
*   **Prefer:** Keep this page, Let go, Begin Quiet Time, Finished peacefully, Whenever you're ready, Take your time, Let's try again.

---

## Things Never Allowed (Alpha)
*   ❌ Advertisements
*   ❌ Streak pressure, daily goals, gamification (XP, coins, levels)
*   ❌ Badges, leaderboards, comparisons
*   ❌ Search bar
*   ❌ Notification badges and counters
*   ❌ Loud or neon colors, pure black theme background

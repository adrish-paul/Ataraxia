# ATARAXIA — NAVIGATION & APP STRUCTURE

> This document defines the navigation design and structure. Switch tabs should preserve screen state, avoid recreating screens, and maintain background ambient audio.

---

## 1. Onboarding & First Launch Flow

*   **First Launch Only:**
    `Launch App` ──> `Splash Screen` ──> `Name Screen` ──> `Notification Screen` ──> `Home Screen`
*   **Subsequent Launches:**
    `Launch App` ──> `Splash Screen` ──> `Home Screen`
*   State tracked locally via **DataStore Preferences**.

---

## 2. Global Navigation Bars

### Floating Bottom Navigation Bar
*   **Position:** Bottom Center
*   **Margin:** `16.dp` from bottom screen edge.
*   **Height:** `72.dp`
*   **Corner Radius:** `32.dp`
*   **Background:** Translucent glass effect with soft blur.
*   **Items:** Exactly 5 tabs. Icons use outlined rounded Material Symbols. Emojis represent items here but are replaced by icons in code.
    1.  🏠 **Home** (Dashboard, Greeting, Quote, Mood, Quick Actions)
    2.  📖 **Journal** (List of Entries, Add Entry FAB)
    3.  🌸 **Breathe** (Breathing exercises, Visual Lotus, 10–15% larger icon size)
    4.  ⏳ **Focus** (Quiet Time, Timer, Ambient Sessions)
    5.  👤 **Me** (Profile, Settings, Privacy, About)

### Consistent Top Bar
*   Layout: `☾ Ataraxia                                 👤`
*   **Left:** Ataraxia text / logo.
*   **Right:** Circular profile picture avatar.
*   No search, notifications, or overflow menus.

---

## 3. Screen Layout Specifications

### Home Screen
*   Logo / Profile Top Bar
*   Dynamic Greeting (e.g., `Good Morning, Alex ☀️` or `Good Evening 🌙`)
*   Daily Inspirational Quote Card
*   Mood Check-In Card (`How is your heart feeling today?`)
*   Quick Actions Grid (2x2: Journal, Breathe, Focus, Sounds)
*   Reflection Prompt Card

### Journal Screen
*   Logo / Profile Top Bar
*   Journal Title
*   Scrollable list of Journal Cards (28dp corners, 20dp padding, date, snippet, mood)
*   **FAB (Bottom Right):** Circular, `64.dp`, Pencil Icon (navigates to New Journal screen)

### New Journal Screen
*   Back Button (Top Left)
*   Today's Date & Optional Prompt
*   Mood Selector (chips representing emotions)
*   Writing Area (uninterrupted Textfield)
*   **Bottom Buttons:**
    *   Left: `Let Go` (Outlined Button - discards draft/deletes)
    *   Right: `Keep This Page` (Filled Gradient Button - saves entry)

### Breathe Screen
*   Logo / Profile Top Bar
*   Title: "Breathe"
*   **Animated Lotus Composable:** (Performs inhale/exhale cycles of 4–6s)
*   Current Instruction ("Breathe In", "Breathe Out")
*   Countdown Timer
*   Primary Button: `Begin Session` (transforms to `Pause`/`End Session` when active)

### Focus Screen
*   Logo / Profile Top Bar
*   Session Type Selector (Slider/Tabs)
*   Large Digital Countdown Timer
*   Ambient Sound Picker
*   Bottom Controls: `Begin Quiet Time` (or `Pause`/`Resume`/`End Session`)

### Ambient Sounds (Sub-feature)
*   Accessed from Home Quick Action or inside Focus.
*   Scrollable 2-column grid of Sound Cards (with illustration, title, play/pause button).
*   **Bottom Mini Player:** Fades in when sound starts, stays visible across tabs to allow quick pause/play controls.

### Me Screen
*   Large Profile Avatar & Display Name edit field
*   Grouped settings cards (Appearance, Notifications, Audio, Privacy, About)

---

## 4. Back Navigation & Confirmation Dialogs

*   **Journal Safety:** If a user attempts to leave the *New Journal* screen with unsaved text, prompt them with a dialog to prevent data loss.
*   **Focus Session Safety:** Leaving an active *Focus* timer triggers a confirmation dialog.
*   **Dialog Style:**
    *   `28.dp` corner radius.
    *   Soft pastel colors (no harsh warning reds).
    *   Left: `Cancel` (Secondary button).
    *   Right: `Let Go` (Destructive button, Muted Rose color) or `Continue` (Primary button).

---

## 5. Screen Transition Animations

| Transition Context | Animation Type | Duration |
| :--- | :--- | :--- |
| **Tab Switching** | Fade + subtle horizontal slide | `300ms` |
| **Deep Screen Navigation** | Journal/Detail slide + fade | `350ms` |
| **Dialog Trigger** | Scale + fade in | `250ms` |
| **Bottom Sheet** | Smooth slide up from bottom edge | `300ms` |

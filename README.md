# Ataraxia 💮

Ataraxia is a beautiful, minimalist mindfulness companion app built with modern Android development technologies. It is designed to be a local, offline-first, private sanctuary for journaling, guided breathing, focus tracking, and self-reflection.

Featuring the custom **Lunaflora Design Language**, Ataraxia delivers a premium, serene user experience with glassmorphism effects, smooth animations, rich typography, and custom-designed theme packs.

---

## 🎨 The Lunaflora Design Language & Themes

Ataraxia is powered by Lunaflora, a design aesthetic focused on tranquility, organic shapes, and minimalist UI components. It features six distinct theme packs that change accent colors, illustrations, and background rendering dynamically:

*   🌸 **Sakura:** A gorgeous pink-accented light mode with a custom background image (`sakura_bg.png`).
*   ✨ **Aurora:** A rich dark mode with neon teal/purple accents and a custom background image (`aurora_bg.png`).
*   🌙 **Cosmos:** An ultra-dark AMOLED black mode designed to reduce eye strain in low-light environments with true pitch blacks.
*   🌿 **Serene:** A clean, soft light mode designed for maximum legibility and warm ivory tones.
*   🌲 **Forest:** A forest-green dark mode inspired by deep woodlands.
*   🌊 **Aqua:** A blue, ocean-inspired accented light mode.

### Visual Polish & Glassmorphism
*   **Haze Glassmorphism Blur**: Uses Chris Banes' `haze` library for real-time frosted glass blurs on the floating bottom navigation bar and all `LunafloraCard` containers.
*   **Contrast Scrims**: Integrates semi-transparent overlays matching each theme to expose background images while guaranteeing high-contrast accessibility for text.
*   **Borderless Dialogs**: Custom platform window decorations completely eliminate default rectangular borders and system shadow frames on all dialog popups.

---

## 🚀 Key Modules & Features

### 1. Home Sanctuary
*   **Contextual Greetings:** Welcomes the user dynamically by name, adjusting according to time of day.
*   **Daily Quote/Calm Tip:** A rotating collection of calming advice and quotes.
*   **Mood Tracker:** A 6-option check-in panel (*Peaceful, Calm, Heavy, Tired, Hopeful, Grateful*) that logs emotional states locally.
*   **Quick Sanctuary Launcher:** Simple row-based navigation buttons to launch Breathe, Journal, Focus, and Settings.

### 2. Private Journal
*   **Timeline View:** A chronological timeline of reflections showing date headers, weather/time metadata, title, content, and attached photo indicators.
*   **Top-Bar Search Toggle:** Dynamically transforms the header bar into an inline text search box, filtering entries in real-time.
*   **Unified Calendar Popup:** A dialog calendar showing consistency flowers (`🌸`) on days with entries. Tapping any day filters the timeline log.
*   **Multi-Attachment Support**: 
    *   *Multiple Voice Notes*: Supports recording and playing back multiple voice recordings named sequentially (`Voice Note 1`, `Voice Note 2`).
    *   *Multiple Photos*: Allows selecting multiple images from camera/gallery, displaying as horizontal scrollable cards during drafting and stacked vertically in reader view.
*   **UX View Dialog**: Features wider dimensions, fixed height constraints, scrollable content with custom scrollbars, and pinned header/action buttons.
*   **Storage Cleanups**: Deleting a journal entry automatically splits and deletes all associated voice note and photo files from local storage.

### 3. Breathing Guided Meditation
*   **Lotus UI Guide:** A beautifully animated visual lotus guide matching inhaling, holding, exhaling, and resting cycles.
*   **Predefined Cycles:** Features Calm, Box, and 4-7-8 breathing techniques.
*   **Ambient Sound Panel:** Configures loops (rain, fire, ocean, forest, wind, night) with a master volume slider.
*   **Breathe Calendar Logs**: Displays history entries on a monthly calendar dialog, filtering sessions by selected dates.

### 4. Immersive Focus Sanctuary (Quiet Time)
*   **Countdown & Flow Modes**: Choose between a 5-180 minute countdown timer or indefinite Flow Mode.
*   **Focus Intention Tags**: Log sessions under Study, Code, Read, Write, Create, or Other.
*   **Focus Streak Indicator**: Dynamically calculates and displays the current consecutive daily focus streak (`🔥 3 Day Streak`).
*   **Midway Session Tracking**: Logs and saves focus sessions running 5 seconds or longer; marks them as `Stopped midway` in the logs if canceled early.
*   **Post-Session Reflections**: Prompts users for custom notes on exiting the focus session, logged alongside session metrics.
*   **Screen Awake Lock**: Implemented optional toggle to keep the device screen on during active focus.
*   **Focus History Logs**: Simple list timeline showing past focus sessions with delete buttons to clean logs.

### 5. Settings & Security (Me Screen)
*   **Profile Center:** Customize name, profile photo, and select active UI themes.
*   **Local Statistics**: Summary totals tracking days active, focus hours, breathing logs, and reflections recorded.
*   **App Lock**: Secure startup lock verifying user PIN credentials before launching the dashboard.
*   **Clear All Data**: Deletes all preferences, local databases, and files, completely resetting the sanctuary.

---

## 🛠️ Technical Stack & Architecture

Ataraxia is built on top of Google's recommended Android app architecture:

*   **Language:** 100% Kotlin
*   **UI Framework:** Jetpack Compose (Declarative UI) with Material Design 3
*   **Architecture Pattern:** Feature-focused Clean Architecture (split into UI/Presentation, Domain Use Cases, Data Repository)
*   **Dependency Injection:** Local DI Container (`AppContainer` and `DefaultAppContainer`) initialized in `AtaraxiaApplication`
*   **Database:** Room (SQLite framework for offline-first journal/focus data persistence)
*   **Configuration Storage:** Preferences DataStore (key-value storage for settings and onboarding flags)
*   **Navigation:** Navigation Compose (with screen-slide and fade animation configurations)
*   **Min SDK:** Android 12 (API 31)
*   **Target SDK:** Android 12 (API 31)

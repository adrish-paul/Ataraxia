# Ataraxia 💮

Ataraxia is a beautiful, minimalist mindfulness companion app built with modern Android development technologies. It is designed to be a local, offline-first, private sanctuary for journaling, guided breathing, focus tracking, and self-reflection.

Featuring the custom **Lunaflora Design Language**, Ataraxia delivers a premium, serene user experience with smooth animations, rich typography, and custom-designed theme packs.

---

## 🎨 The Lunaflora Design Language

Ataraxia is powered by Lunaflora, a design aesthetic focused on tranquility, organic shapes, and minimalist UI components. It features six distinct theme packs that change accent colors, illustrations, and background rendering dynamically:

*   💮 **Serene:** A clean, soft light mode designed for maximum legibility.
*   🌸 **Sakura:** A gorgeous pink-accented light mode with a custom background image (`sakura_bg.png`).
*   ✨ **Aurora:** A rich dark mode with neon teal/purple accents and a custom background image (`aurora_bg.png`).
*   🌙 **Cosmos:** An ultra-dark AMOLED black mode designed to reduce eye strain in low-light environments.
*   🌿 **Forest:** A forest-green accented dark mode inspired by deep woodlands.
*   🌊 **Aqua:** A blue, ocean-inspired accented light mode.

---

## 🚀 Key Modules & Features

### 1. Home Sanctuary
*   **Contextual Greetings:** Dynamic greetings welcoming the user by name, adapting to whether it is their first launch or a returning session.
*   **Daily Reflection Prompt:** A daily self-reflection prompt rotation that updates automatically every time the app is launched.
*   **Meditation Done Today:** A focused daily tracking banner displaying if meditation/breathing sessions have been completed today.
*   **Mood Tracker:** An interactive, one-tap mood logger allowing users to register their current emotional state.

### 2. Private Journal
*   **Offline-First Privacy:** All thoughts are saved locally in a secure SQLite database using Room.
*   **Timeline View:** A chronological history of all past entries, organized by date.
*   **Guided Prompt Rotation:** Refreshes prompts on-demand to inspire daily reflection.
*   **Mood Slider:** Seamless mood value selection recorded with each entry.
*   **Auto-Save Drafts:** Automatically saves journal drafts every 10 seconds to prevent data loss.
*   **Contextual Weather Stamps:** Metadata tags capturing local environment parameters alongside the entry.

### 3. Breathing Guided Meditation
*   **Breathing Style Pop-up Selector:** A custom overlay menu showcasing all available breathing methods.
*   **Predefined Cycles:**
    *   *Calm:* A relaxing standard pattern.
    *   *Box Breathing:* Professional focus-inducing technique.
    *   *4-7-8 Technique:* Ancient sleep-inducing cycle.
    *   *Custom Cycle:* Set your own inhale, hold, exhale, and rest intervals.
*   **Lotus UI Guide:** A beautifully animated visual guide matching the exact cycle intervals.
*   **Haptic & Sound Feedback:** Fully customizable audio guides and physical haptic ticks to help guide breathing cycles without looking at the screen.

### 4. Immersive Focus Sanctuary (Quiet Time)
*   **Focus Categories:** Select your task (Study, Code, Read, Write, Create) to log session metrics.
*   **Countdown Session Timer:** Highly custom productivity timers.
*   **Focus Stats:** Visual log displays showing total focus time accumulated today.
*   **Serene Ambient Sounds:** Fully configurable ambient audio options to aid concentration.

### 5. Personalization & Security (Me Screen)
*   **Profile Center:** Customize username, preferences, and reset app onboarding states.
*   **Notification Controls:** Set reminders, customize alert schedules, and configure quiet hours.
*   **App Lock:** Secure local app entry with PIN/biometrics verification.

---

## 🛠️ Technical Stack & Architecture

Ataraxia is built on top of Google's recommended Android app architecture:

*   **Language:** 100% Kotlin
*   **UI Framework:** Jetpack Compose (Declarative UI) with Material Design 3
*   **Database:** Room (for offline-first journal/focus data persistence)
*   **Configuration Storage:** Preferences DataStore (highly efficient key-value preferences caching)
*   **Navigation:** Navigation Compose (with custom screen-slide animation configurations)
*   **Design Tokens:** Semantic theme variables linked directly to system color schemes
*   **Min SDK:** Android 12 
*   **Target SDK:** Android 16

---

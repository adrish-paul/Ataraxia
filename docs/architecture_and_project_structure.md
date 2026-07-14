# ATARAXIA — ANDROID ARCHITECTURE & PROJECT STRUCTURE

> This document specifies Ataraxia's technical architecture. It enforces strict separation of concerns, clean MVVM data flows, centralized resource files, and offline-first capabilities.

---

## 1. Core Engineering Stack

*   **UI Layer:** Jetpack Compose (Material Design 3).
*   **Architecture:** Clean MVVM (Model-View-ViewModel) modular separation.
*   **State Management:** StateFlow (Kotlin Coroutines) using immutable `UiState` classes.
*   **Navigation:** Navigation Compose (Single Activity navigation).
*   **Persistence Layer:** Room Database (structured tables) and Preferences DataStore (key-value settings).
*   **Async Operations:** Kotlin Coroutines & Flow (maintaining non-blocking UI threads).
*   **Dependency Injection:** Hilt-ready package layouts (to be integrated post-Alpha).

---

## 2. Package Structure & Code Organization

The code will reside under the base package `com.ataraxia` (or mapped namespace e.g. `com.example.ataraxia`):

```
com.ataraxia
├── data
│   ├── database
│   │   ├── dao         # Room DAOs (JournalDao, SessionDao)
│   │   ├── entity      # Database models (JournalEntry, FocusSession, BreatheSession)
│   │   ├── converters  # TypeConverters (e.g. UUID <-> String, Date <-> Long)
│   │   └── AppDatabase # Room database abstract class
│   ├── datastore       # DataStore Preference wrappers (SettingsDataStore)
│   ├── repository      # Core repository implementations (SSOT)
│   └── local           # Local assets or local data helpers
├── domain
│   ├── model           # Clean domain entity structures
│   ├── repository      # Domain repository interfaces
│   └── usecase         # Domain business logic execution units
├── ui
│   ├── screens         # Feature Compose screens (HomeScreen, JournalScreen)
│   ├── navigation      # NavHost, Navigation Graph, and route definitions
│   ├── components      # Lunaflora design system reusable components
│   ├── animations      # Custom visual transition composables
│   ├── theme           # Lunaflora Theme, Color.kt, Typography.kt, Shape.kt
│   └── utils           # Compose-specific context extension utilities
├── viewmodel           # ViewModel implementations (HomeViewModel, JournalViewModel)
├── util                # General utility classes (Log, Time, String helpers)
└── MainActivity.kt     # Single Activity entry point
```

---

## 3. Navigation Graph Routes

The Navigation Host will map the following linear and deep routes safely:
```
Splash (Splash Screen)
 └── Name (Welcome Setup: Name Input)
      └── Notification (Welcome Setup: Permission Request)
           └── Home (Home tab)
                ├── Journal (Journal Timeline tab)
                │    ├── JournalDetail (View Journal entry)
                │    └── JournalEditor (New Journal entry / Edit mode)
                ├── Breathe (Breathe tab)
                │    └── BreathingSession (Active guided session)
                ├── Focus (Focus tab)
                │    └── FocusSession (Active Sanctuary Mode)
                └── Me (Me/Settings tab)
                     ├── Appearance (Theme toggles)
                     ├── Notifications (Reminder configurations)
                     ├── Privacy (App lock configurations)
                     └── About (Licenses and credits)
```

---

## 4. MVVM Data Flows & Persistence

No Composable UI screens may access Room databases or DataStore files directly. All queries pass through ViewModels and Repositories:

`Compose UI Screen` ──> `UiState (StateFlow)` ──> `ViewModel` ──> `Repository` ──> `Room / DataStore`

### Room Database Tables
1.  **Journal Entries (`JournalEntry`):** `id` (UUID), `title`, `content`, `mood`, `weather_description`, `created_date`, `modified_date`, `word_count`.
2.  **Focus Sessions (`FocusSession`):** `id` (UUID), `duration_minutes`, `intention`, `ambience`, `date_timestamp`.
3.  **Breathe Sessions (`BreatheSession`):** `id` (UUID), `method_name`, `duration_seconds`, `completed`, `date_timestamp`.

### DataStore Preferences
Stores settings: `user_name`, `notifications_enabled`, `has_completed_setup`, `setup_completed_date`, `app_theme` (Light/Dark/AMOLED), `material_you_enabled`, `preferred_focus_duration`, `preferred_breathing_method`, `haptics_enabled`, `ambient_sound_volume`.

---

## 5. Engineering & Development Rules

*   **Resource Centralization:** All visual strings belong in `strings.xml`, colors in `Color.kt`, dimensions in `dimens.xml` (or custom spacing Compose keys), and animations/ambient sound loops in `res/raw`. No hardcoded strings or dimensions in Composables.
*   **Error Handling:** Catch database transaction exceptions, permission denials, and preference read errors gracefully. Show a calming empty state or a gentle message: `"Something quietly interrupted this moment. Let's try again."`
*   **Security:** Avoid writing sensitive journal entries or usernames to debug logs. Keep all data local-first (no network requests during Alpha).

---

## 6. Non-Functional Sanctuary Rule

> Every feature introduced into Ataraxia must be able to justify its existence by improving the user's sense of calm, reflection, or emotional wellbeing. Features that primarily add complexity, increase cognitive load, or distract from the core experience should be omitted, even if they are technically impressive.

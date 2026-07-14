# Ataraxia Project Documentation Index

Welcome to the central documentation workspace for **Ataraxia** (featuring the **Lunaflora** Design Language). 

These documents define the brand guidelines, technical architecture, visual patterns, and user experience specs. They are organized sequentially and build upon one another:

---

## 📚 Document Index

1.  ### [Brand Identity (Brand Bible)](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/brand_identity.md)
    *   **Purpose:** Defines Ataraxia's core mission, values, voice tone guidelines, rules ("Ataraxia promises"), target users, and visual theme directions.
    *   **Codename:** Lunaflora Design System.

2.  ### [Foundation & Design Bible](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/foundation_design_bible.md)
    *   **Purpose:** Outlines the core technical specifications (Jetpack Compose, Room, MVVM, DataStore), naming conventions, folder structure, UI layout rules, and standard component sizing.

3.  ### [First Launch Experience & Welcome Setup](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/first_launch_experience.md)
    *   **Purpose:** Specifies onboarding behaviors, Splash Screen transitions, personalization setup steps (Name entry), and Notification runtime permission prompt rules.

4.  ### [Lunaflora Design System (Visual Bible)](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/lunaflora_design_system.md)
    *   **Purpose:** The comprehensive visual style specification, describing hex color tables (for Light, Dark, and AMOLED), typography, grid spacings, button states, floating navigation setup, micro-interactions, animation durations, and accessibility standards.

5.  ### [Navigation & App Structure](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/navigation_app_structure.md)
    *   **Purpose:** Defines the navigation flow, layouts (Home, Journal, Breathe, Focus, Me), safety dialogs, and screen transition animation configurations.

6.  ### [Home Screen Specification](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/home_screen.md)
    *   **Purpose:** Details layout zones, greeting logic, affirmations caching, dynamic mood chip interactions, and the dynamic time-of-day low-opacity animated backdrops.

7.  ### [Journal Module Specification](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/journal_module.md)
    *   **Purpose:** Outlines timeline view, prompt refreshing, text editing, horizonal mood scroll bars, 10s auto-saves, and the contextual time-and-weather stamp system.

8.  ### [Breathe Module Specification](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/breathe_module.md)
    *   **Purpose:** Establishes breathing cycles (Calm, Box, 4-7-8, Custom), session controls, haptic/sound integration, and the intuitively guided breathing lotus guidelines.

9.  ### [Focus Module Specification (Quiet Time)](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/focus_module.md)
    *   **Purpose:** Outlines focus categories (Study, Code, Read, Write, Create), ambient sounds, session timers, simple logs, and the distraction-free "Immersive Sanctuary Mode" transition.

10. ### [Me & Settings Module Specification](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/me_and_settings_module.md)
    *   **Purpose:** Outlines profile settings, custom theme selections (Light, Dark, AMOLED, Material You), notification reminders, quiet hours, audio levels, and local app access lock.

11. ### [Component Library & Design Components](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/component_library.md)
    *   **Purpose:** Establishes the reusable design tokens (Colors, Shapes, Spacing, Animations) and UI widgets (buttons, cards, chips, textfields, dialogs, loading states) used across all screens.

12. ### [Android Architecture & Project Structure](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/architecture_and_project_structure.md)
    *   **Purpose:** Outlines target engineering stacks (MVVM, Room, DataStore), package structure layouts, Navigation Compose graphs, repository patterns, resource management, and test readiness.

13. ### [Production Polish & Engineering Standards](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/production_polish.md)
    *   **Purpose:** Details non-functional benchmarks, including custom loaders, empty/error state layouts, haptic limits, memory/battery optimizations, and final shipping verification checklists.

14. ### [Alpha Scope Lock](file:///c:/Users/adris/AndroidStudioProjects/Ataraxia/docs/alpha_scope_lock.md)
    *   **Purpose:** Enforces functional boundaries, explicitly separating included offline features from deferred cloud, social, and AI expansions to prevent development creep.

---

## 🛠️ Tech Stack Cheat Sheet
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose + Material Design 3
*   **Database:** Room (SQLite)
*   **Key-Value Config:** Preferences DataStore
*   **Navigation:** Navigation Compose
*   **Min SDK:** Android 12 (API 31)

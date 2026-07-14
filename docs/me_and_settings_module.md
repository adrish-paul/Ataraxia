# ATARAXIA — ME & SETTINGS MODULE SPECIFICATION

> The Me/Profile section represents the user's private, calm corner. It avoids looking like a standard system settings checklist. Instead, it groups configuration cards with generous margins, soft outlines, and serene icons.

---

## 1. Screen Layout Structure

### Me Main Screen
*   Logo / Profile Top Bar.
*   **Profile Header:**
    *   Large circular profile photo (`96.dp` diameter).
    *   Display Name (Alex, or customized). If skipped during setup: `"Add your name"`.
    *   Calming daily sub-label (e.g. `"Take care of yourself today."`, `"One quiet step at a time."`).
    *   `Edit Profile` button (Outlined style, centers layout, edits name & image).
*   **Quick Summary Card:** Floating container detailing aggregate local user metrics:
    *   `Quiet Days` • `Journal Entries` • `Focus Sessions` • `Breathing Sessions`
    *   *No streaks, daily targets, checkmarks, or competitive achievements are displayed.*
*   **Settings Sections (Floating Cards):**
    *   **Personalization:** Appearance, Themes.
    *   **Preferences:** Notifications, Audio, Haptics.
    *   **Privacy:** App Lock (PIN/Biometrics), Privacy Policy, Data export/import (future).
    *   **About:** App version, Developers, Open Source Licenses, Feedback (sends email).

---

## 2. Personalization & Appearance Settings

Allows full-screen theme preview updates saved instantly to Preferences DataStore:
*   **Themes:** 
    *   `☀ Light` (Warm Ivory background with Soft Cream card surface)
    *   `🌙 Dark` (Deep Indigo background with Midnight Violet card surface)
    *   `⚫ AMOLED` (Near Black background with Midnight Violet/Indigo card surface)
    *   `🎨 Material You` (Soft desaturated system dynamic color support)
*   **Accent Color (Future-ready placeholder):** Default accent set to Lavender. Placeholders for Sage, Peach, and Blue accents.

---

## 3. Preferences & Audio Settings

### Notifications Control Screen
This is the single centralized location for all app reminder toggles:
*   Master Toggle (enable/disable reminders).
*   Individual reminders (Daily Reflection, Journal, Breathing, Quiet Time).
*   **Time Picker:** Default time set to `08:00 PM`.
*   **Quiet Hours Option:** Allows blocking reminders between specific hours (e.g., `10 PM — 7 AM`).

### Audio & Haptics Control Screen
*   Sliders to adjust ambient play volumes, toggle button press clicks, and session finish indicators.
*   Toggles for session pulse vibration haptics.

---

## 4. Privacy & App Lock
*   **App Lock:** Enables authentication using system credentials (PIN/Pattern) or Biometric credentials (Fingerprint/Face Unlock) on app startup.
*   No database credentials or cloud registration are required; all keys remain stored locally.

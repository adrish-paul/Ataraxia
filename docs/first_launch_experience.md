# ATARAXIA — FIRST LAUNCH EXPERIENCE & WELCOME SETUP

> This document defines the design and logic for the first-launch user experience. It serves as the foundation for the upcoming screens, preferences, viewmodels, and navigation graph implementations.

---

## Onboarding Navigation Flow

```
Launch App ──> Splash Screen ──> First Launch? 
                                    ├──> Yes ──> Name Screen ──> Notification Screen ──> Home Screen
                                    └──> No ───────────────────────────────────────────> Home Screen
```

---

## 1. Splash Screen

*   **Aesthetic:** Extremely minimal and calming. Centered Ataraxia logo.
*   **Background:** App theme background (Light/Dark).
*   **Effects:** Subtle floating particles/drifting petals moving slowly.
*   **Logo Animation:** Fades in over `600ms`.
*   **Duration:** Remains visible for `1.5` to `2.0` seconds.
*   **Constraints:** No loading bars, no branding clutter, no advertisements.

---

## 2. Onboarding Screens

### Screen One — Name Screen (Personalization)
*   **Goal:** Provide a warm, conversational introduction.
*   **Layout:**
    *   *Top:* Centered Ataraxia Logo with generous surrounding whitespace.
    *   *Center:* Heading: `"Before we begin..."` and subtitle: `"What should I call you?"`
    *   *Input:* Single-line fully rounded Material 3 TextField with placeholder `"Your name"` (max 30 chars).
    *   *Bottom:* Large centered primary button (`Continue`) and text button (`Skip`) below it.
*   **Behavior:**
    *   Keyboard opens automatically on entry.
    *   Tapping `Continue` with text saves the name to DataStore and proceeds.
    *   Tapping `Continue` (empty field) or `Skip` proceeds without saving a name.
    *   Dismisses keyboard on completion.

### Screen Two — Notification Screen (Reminders)
*   **Goal:** Politely ask for notification permission without pressure.
*   **Layout:**
    *   *Top:* Centered Ataraxia Logo.
    *   *Center:* Heading: `"One last thing..."` and subtitle: `"Would you like gentle reminders to pause, breathe, or write?"`
    *   *Bottom:* Large primary button (`Allow Notifications`) and text button (`Skip`) below it.
*   **Behavior:**
    *   Tapping `Allow Notifications` triggers the Android system runtime permission prompt (`POST_NOTIFICATIONS` for API 33+).
    *   Tapping `Skip` or denying the permission proceeds directly to the Home Screen.
    *   DataStore variables are updated, marking setup as complete.

---

## 3. Storage & State Management (DataStore)

We will persist the following state in DataStore Preferences:

| Key Name | Data Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `user_name` | String | `""` (Empty) | User's preferred display name. |
| `notifications_enabled` | Boolean | `false` | True if user allowed reminders. |
| `has_completed_setup` | Boolean | `false` | True once setup flow completes/skips. |
| `setup_completed_date` | Long | `0L` | Epoch timestamp of completion. |

---

## 4. UI/UX Rules & Details

### Greeting Logic (Home Screen)
The greeting on the Home screen adapts dynamically based on name and time of day:
*   **Morning (6 AM - 11:59 AM):** `"Good Morning, [Name] ☀️"` or `"Good Morning ☀️"`
*   **Afternoon (12 PM - 5:59 PM):** `"Good Afternoon, [Name] 🌤️"` or `"Good Afternoon 🌤️"`
*   **Evening/Night (6 PM - 5:59 AM):** `"Good Evening, [Name] 🌙"` or `"Good Evening 🌙"`

### Customization Settings (Me Tab)
Users can edit setup preferences later:
*   **Name:** `Me` → `Profile` → `Display Name`
*   **Notifications:** `Me` → `Settings` → `Notifications`

### Visual & Interactive Feel
*   **Transitions:** Horizontal slide combined with a fade animation (`350ms - 400ms`).
*   **Button Press:** Scale down gently on press (`150ms`).
*   **Freedom:** No forced fields, accounts, emails, or analytics prompts.

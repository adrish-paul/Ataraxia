# ATARAXIA — BREATHE MODULE SPECIFICATION

> The Breathe module is designed to feel like an immersive, distraction-free sanctuary. It avoids stopwatch or fitness-app style styling in favor of elegant visual breathing guides, smooth cycles, and gentle haptic pulses.

---

## 1. Screen Layout Structure

### Breathe Main Tab
*   Logo / Profile Top Bar.
*   Greeting: Title `"Breathe"` with subtitle `"Slow down. Your next breath is enough."`
*   **Hero Illustration:** An animated lotus floating gently, expanding and contracting dynamically, glowing with a soft ambient outline.
*   **Breathing Methods List:** Scrollable list of breathing method cards:
    *   **Calm Breathing:** 4s Inhale, 4s Exhale. Duration 2–10 mins. (Default rhythm).
    *   **Box Breathing:** 4s Inhale, 4s Hold, 4s Exhale, 4s Hold. (Clarity and balance).
    *   **4-7-8 Breathing:** 4s Inhale, 7s Hold, 8s Exhale. (Deep relaxation).
    *   **Gentle Breathing (Custom):** User-configured inhale, hold, exhale times stored locally.
*   **Recent Session Card (Conditional):** Shows details of the last completed session (e.g. `Box Breathing • 10 Minutes • Yesterday`) with a quick `Repeat` action button.

### Active Session Screen
*   Back arrow (returns home after confirmation).
*   Method Name title.
*   **Guided Lotus Composable:** Anchored at the center of the screen, floating above a low-opacity radial gradient backdrop with drifting particles.
*   **Instruction Text:** Centered below the lotus with large, soft typography, fading between states.
*   **Progress Ring:** A thin, glowing circular tracker outlining the lotus area representing elapsed time.
*   **Controls:** Bottom-aligned `Pause` and `End Session` buttons. (Transforms to `Resume` and `End Session` on pause).

### Session Completion Screen
*   Large lotus blooming animation.
*   Message: `"You gave yourself a quiet moment."`
*   Secondary: `"Every gentle breath matters."`
*   Buttons: `Return Home` (Outline) and `Repeat Session` (Filled Gradient).

---

## 2. Intuitive Lotus Visual Guide

The breathing guide relies on natural, organic visual motion rather than textual commands:
*   🌸 **Lotus Opens (Scales Up):** Instructs user to **Inhale**.
*   🌸 **Lotus Remains Open (Static at Max Scale):** Instructs user to **Hold**.
*   🌸 **Lotus Slowly Closes (Scales Down):** Instructs user to **Exhale**.
*   🌸 **Lotus Rests (Static at Min Scale):** Instructs user to **Pause/Rest**.

*Text prompts (Inhale, Hold, Exhale, Rest) fade in and out to support this, but the speed, scale, and state of the lotus serve as the primary intuitive guide.*

---

## 3. Sounds & Haptics

*   **Ambient Music Integration:** Users can toggle and slide volume for ambient loops during a session (`Rain`, `Ocean`, `Forest`, `Wind`, `Fireplace`, `Night`). The last used background sound is stored and auto-applied.
*   **Haptic Pulses:** Optional subtle pulse triggers when an inhale begins and when an exhale begins. Toggled in settings.

---

## 4. Room Database Schema (`BreatheSession`)

Completed sessions are logged locally for basic stats (no streaks or competitive badges).

| Column Name | Data Type | Key Type | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID (String) | Primary Key | Unique session identifier. |
| `method_name` | String | | Name of breathing pattern used. |
| `duration_seconds` | Integer | | Length of the session. |
| `completed` | Boolean | | True if completed without early exit. |
| `date_timestamp` | Long | | Epoch timestamp of session start. |

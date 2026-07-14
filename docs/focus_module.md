# ATARAXIA — FOCUS MODULE SPECIFICATION (QUIET TIME)

> Focus is designed to protect attention rather than maximize productivity. It rejects the Pomodoro tomato aesthetics, graphs, streaks, and numeric statistics, opting for an immersive, calm study room atmosphere.

---

## 1. Screen Layout Structure

### Focus Main Tab
*   Logo / Profile Top Bar.
*   Greeting: Title `"Quiet Time"` with subtitle `"Give your attention to just one thing."`
*   **Choose Duration:** Horizontal chips with a soft glow animation on selection:
    *   `15 min` • `25 min` • `45 min` • `60 min` • `90 min` • `Custom` (user-input timer)
*   **Focus Intention Cards:** Grid selection indicating the current focus category (no analytics are tracked for this, selection is purely session-based):
    *   `📚 Studying` • `💻 Coding` • `📖 Reading` • `✍ Writing` • `🎨 Creating` • `🌱 Something Else`
*   **Choose Ambience:** Scrollable horizontal layout of ambient cards (each card includes a title, illustration, and a small play preview):
    *   `🌧 Rain` • `🌲 Forest` • `🌊 Ocean` • `☕ Cafe` • `🔥 Fireplace` • `🌌 Night`
*   **Begin Quiet Time Button:** Centered, large rounded gradient button (`56.dp` height).

### Active Session Screen (Immersive Sanctuary Mode)
*   Visual transition detailed in Section 2.
*   Layout contains:
    *   Back button (triggers warning modal).
    *   Current Intention label (e.g., `"Coding"` in small, soft typography).
    *   **Large Digital Timer:** Centered, displayed in soft warm-white text (no flashing, ticking, or harsh red coloring).
    *   **Ambience Mini Controls:** Discreet volume adjustments and play/pause sound toggles.
    *   **Controls:** Bottom-aligned `Pause` and `End Session` buttons. (Transforms to `Resume` / `End Session` on pause).

### Session Completion Screen
*   Message: `"Thank you for giving yourself uninterrupted time."`
*   Secondary: `"One quiet moment can accomplish a lot."`
*   Buttons: `Return Home` (Outline) and `Begin Again` (Filled).

---

## 2. Immersive Sanctuary Mode Transformation

When the user taps `Begin Quiet Time`, the application transforms from a mobile screen into a physical sanctuary:

1.  **UI Fading:** The floating bottom navigation bar and consistent top bar fade out completely (`300ms`).
2.  **Ambience Transition:** The screen background expands into a full-bleed, low-opacity animated loop representing the selected atmosphere (e.g., slow raindrops sliding down glass for `Rain`, glowing wood coals for `Fireplace`).
3.  **Visual Centering:** The timer transitions smoothly from the top/middle into the direct visual center of the screen (`350ms`).
4.  **System Lockdown:** Keyboard closes. All notifications are quietly hidden (if DND permission is granted). The screen is configured to stay awake (using `Keep Screen On` flags) so the phone behaves like a dedicated desk accessory.

*This transition removes the feeling of 'interacting with a smartphone app' and turns the device into a quiet room dedicated to concentration.*

---

## 3. Storage & History Schema (`FocusSession`)

Sessions are logged locally in Room to let the user review their timeline. No graphs, comparisons, streaks, or charts are shown.

### Room Database Entity (`FocusSession`)

| Column Name | Data Type | Key Type | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID (String) | Primary Key | Unique session identifier. |
| `duration_minutes` | Integer | | Saved duration length. |
| `intention` | String | | Focus intention string (e.g. "Reading"). |
| `ambience` | String | | Ambience name used (e.g. "Rain"). |
| `date_timestamp` | Long | | Epoch timestamp of completion. |

*Timeline rendering example:* `Yesterday • 45 min • Coding • Rain`

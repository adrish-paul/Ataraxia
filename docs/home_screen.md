# ATARAXIA — HOME SCREEN SPECIFICATION

> The Home screen is the emotional center of Ataraxia. It must welcome the user warmly and gently without looking like a busy dashboard. It avoids charts, numbers, and stats in favor of spacious calmness.

---

## 1. Layout Structure
The home screen is a single vertically scrollable page with `24.dp` horizontal padding and generous spacing between cards (`20.dp`) and sections (`32.dp`).

```
☾ Ataraxia                         👤   <-- Consistent Top Bar
─────────────────────────────────────────
[Time-Aware Animated Low-Opacity Background]
─────────────────────────────────────────
Greeting Section ("Good Morning, Alex ☀️")
Date ("Tuesday • 15 July")
─────────────────────────────────────────
[ Daily Affirmation / Quote Card (28dp) ]
─────────────────────────────────────────
Mood Check-in: "How is your heart today?"
[ 🌸 Peaceful ] [ 🍃 Calm ] [ 🌧 Heavy ]
[ 🌙 Tired ] [ ✨ Hopeful ] [ ❤️ Grateful ]
─────────────────────────────────────────
Quick Actions Grid (2x2 Card Layout)
┌──────────────────┐ ┌──────────────────┐
│  📖 Journal      │ │  🌸 Breathe      │
├──────────────────┤ ├──────────────────┤
│  ⏳ Focus        │ │  🎵 Sounds       │
└──────────────────┘ └──────────────────┘
─────────────────────────────────────────
[ Continue Session Card ] (Conditional)
─────────────────────────────────────────
[ Today's Reflection Prompt Notebook Card ]
   [ Begin Writing (Primary Button) ]
─────────────────────────────────────────
[ Daily Gentle Calm Tip Card ]
─────────────────────────────────────────
🏠     📖     🌸     ⏳     👤   <-- Floating Bottom Navigation Bar
```

---

## 2. Animated Time-Aware Background

A low-opacity backdrop sits behind the main content area, shifting dynamically to represent the current hour:

*   🌅 **Morning (6:00 AM - 11:59 AM):** Soft sunrise gradient (pastel rose to warm ivory) with slow, drifting cloud silhouettes.
*   ☀️ **Afternoon (12:00 PM - 5:59 PM):** Warm cream sky with subtle, floating leaf silhouettes drifting downwards.
*   🌇 **Evening (6:00 PM - 7:59 PM):** Lavender-to-indigo twilight gradient with slowly blinking, soft glowing firefly particles.
*   🌌 **Night (8:00 PM - 5:59 AM):** Deep indigo sky with slow-moving stars and a faint, glowing crescent moon silhouette.

*All backdrop animations run continuously at a slow, breathing pace, and opacity is capped at 10-15% to guarantee text legibility.*

---

## 3. Section Specifications

### Top Bar
*   Left: App Logo `☾ Ataraxia` (not interactive).
*   Right: Circular profile picture (`48.dp` diameter) which navigates to the **Me Screen**.

### Greeting & Date
*   Adapts to name configuration (omitted if empty) and local system clock:
    *   `Good Morning, [Name] ☀️` (Morning)
    *   `Good Afternoon, [Name] 🌤️` (Afternoon)
    *   `Good Evening, [Name] 🌙` (Evening)
    *   `Good Night, [Name] 🌌` (Night)
*   Sub-label: Current date formatted as `DayOfWeek • Day Month` (e.g. `Tuesday • 15 July`).

### Daily Quote Card
*   Floating container (`28.dp` corners, `20.dp` internal padding) featuring a random daily affirmation (e.g., *"One gentle breath can change everything."*).
*   Quotes are cached locally so they change exactly once per 24 hours.

### Mood Check-in
*   Heading: `"How is your heart today?"`
*   Six mood chips arranged in a 3x2 grid:
    1.  🌸 Peaceful
    2.  🍃 Calm
    3.  🌧 Heavy
    4.  🌙 Tired
    5.  ✨ Hopeful
    6.  ❤️ Grateful
*   **Behavior:** Only one chip can be active. Tapping a chip triggers a soft glow, saves the selection locally in DataStore/Room, and replaces the chips with a gentle confirmation message: `"Thank you for checking in."`

### Quick Actions Grid (2x2)
*   Cards containing an illustration, title, and subtitle. The *entire card* is clickable:
    *   **Journal:** *"Write today's thoughts."* (opens Journal tab / creates draft)
    *   **Breathe:** *"Take one quiet breath."* (opens Breathe tab)
    *   **Focus:** *"Begin Quiet Time."* (opens Focus tab)
    *   **Sounds:** *"Listen and relax."* (opens Ambient Sounds picker)

### Continue Card (Conditional)
*   Appears only if a draft or active session exists (e.g., unfinished journal, paused focus, active breathing).
*   Displays description (e.g., *"Continue Journal"* or *"Continue Quiet Time"*) with a centered primary `Continue` button.

### Today's Reflection
*   Notebook-styled card featuring today's prompt (e.g., *"What brought you peace today?"*).
*   Contains a full-width `Begin Writing` button (`56.dp` height) which navigates directly to the journaling screen with the prompt pre-filled.

### Daily Calm Tip
*   A small card containing a simple, non-demanding suggestion (e.g., *"Try taking three slow breaths before checking your phone."* or *"Drink a glass of water."*). No lists, checkmarks, or trackers.

---

## 4. Animations & Micro-Interactions
*   **Entrance:** Staggered fade and rise from the bottom (`400ms` total duration).
*   **Quick Actions:** Scale down to 97% and lift slightly on touch.
*   **Mood Check:** Smooth fading selection transition with glowing drop-shadow.

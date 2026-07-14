# ATARAXIA — JOURNAL MODULE SPECIFICATION

> The Journal is Ataraxia's signature feature, designed to feel like a private, supportive, and timeless diary. It avoids document-like styling or layout toolbars in favor of a clean, writing-first sanctuary.

---

## 1. Layout Structure

### Journal Home (Timeline)
*   Logo / Profile Top Bar.
*   Greeting Section: `"Your Journal"` with subtitle `"Every page is a conversation with yourself."`
*   **Today's Prompt Card:** A floating rounded card with a refresh button. Tapping the refresh button triggers a smooth animation and selects a new prompt.
*   **Journal Timeline:** A vertical lazy column displaying entries as elegant floating cards sorted by date (newest first). Each card shows:
    *   Time & Weather context (e.g., `🌧️ Rainy Evening • 8:42 PM`)
    *   Selected Mood indicator (e.g., `🌸 Peaceful`)
    *   Title of the entry (max 60 chars)
    *   Short content preview snippet (first 3-4 lines).
*   **Floating Action Button (FAB):** Positioned bottom-right. A circular `64.dp` button with a soft lavender gradient containing a rounded pencil icon. (This is the only FAB in the application).

### New Journal Screen
*   Logo / Back Button Top Bar.
*   **Date, Time & Weather Header:** (e.g., `Tuesday, 15 July 2026 • 🌤️ Sunny Morning`)
*   **Mood Selector:** Horizontally scrollable row of rounded mood chips:
    *   `🌸 Peaceful` • `🌤 Happy` • `🌧 Heavy` • `🌙 Tired` • `✨ Hopeful` • `❤️ Grateful` • `🍃 Calm` • `💙 Lonely`
*   **Title Input:** Rounded, borderless text field with placeholder `"Give today a title..."` (Max 60 characters).
*   **Writing Area:** Large multiline text field occupying most of the screen. Placeholder: `"What's on your mind today?"` Automatically expands and supports paragraph separation. Supports plain writing only (no markdown, no bold, no lists).
*   **Bottom Buttons:**
    *   Left: `Let Go` (Outlined Button, Muted Rose border and text; discards draft/deletes entry after confirmation).
    *   Right: `Keep This Page` (Filled Gradient Button; saves entry).

### Reading Screen
*   Back Button.
*   Header: Date, Time & Weather, Mood tag, and Title.
*   Scrollable body containing the full text with comfortable margins, spacing, and clean typography.
*   **Actions:** An edit button (returns to editing mode, restoring cursor position) and a delete option located in a quiet dropdown overflow menu.

---

## 2. Dynamic Time & Weather Context

Each journal entry stores emotional context from the moment of creation:
*   **Time of Day:** Formatted as `HH:MM AM/PM` (e.g., `8:42 PM`).
*   **Time Context:** String representation (`Morning`, `Afternoon`, `Evening`, `Night`).
*   **Weather Conditions:** Stored locally. If the device is online, retrieves current local weather; if offline or permission is restricted, defaults to a neutral time-based tag (e.g., `Quiet Evening` or `Warm Afternoon`) or relies on manual user settings.
*   *Format Example:* `🌧️ Rainy Evening • 8:42 PM` or `🌤️ Sunny Morning • 9:15 AM`

---

## 3. Data Integrity & Room Schema

To ensure thoughts are never lost, drafts autosave every **10 seconds** and whenever the app transitions into the background.

### Room Database Entity (`JournalEntry`)

| Column Name | Data Type | Key Type | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID (String) | Primary Key | Unique identifier. |
| `title` | String | | Entry title (optional). |
| `content` | String | | Main journal text body. |
| `mood` | String | | Selected mood string (optional). |
| `weather_icon` | String | | Icon code/emoji representing weather. |
| `weather_description` | String | | Context tag (e.g., "Rainy Evening"). |
| `created_date` | Long | | Epoch timestamp of creation. |
| `modified_date` | Long | | Epoch timestamp of last edit. |
| `word_count` | Integer | | Calculated dynamically. |

---

## 4. UI/UX Rules & Animations

### Safety Dialogs
*   **Unsaved Exit Confirmation:** Tapping `Back` with unsaved content triggers:
    *   Title: `"Leave this page?"`
    *   Text: `"Your thoughts haven't been saved yet."`
    *   Buttons: `Stay` (Primary) & `Leave` (Secondary).
*   **Delete Draft Confirmation:** Tapping `Let Go` triggers:
    *   Title: `"Let go of this page?"`
    *   Text: `"This action cannot be undone."`
    *   Buttons: `Cancel` (Secondary) & `Let Go` (Destructive, Muted Rose).

### Visual Transitions & Feeds
*   **Bloom Animation:** Saving successfully triggers a soft, fading full-screen modal showing a blooming lotus `🌸` and the confirmation: `"Your thoughts have been safely kept."` (Fades out after 2 seconds).
*   **Transitions:** Slide-in page-turn transitions for detail pages and timeline items fading upwards.

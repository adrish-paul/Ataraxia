# LUNAFLORA DESIGN SYSTEM (VISUAL BIBLE)

> This document is the single source of truth for the visual design of Ataraxia. Every screen, composable theme, color class, shape, text style, and custom component must reference these parameters.

---

## 1. Color System

| Token Name | Hex Code | Usage / Context |
| :--- | :--- | :--- |
| **Primary: Dusty Lavender** | `#B9A7D6` | Primary accents, interactive highlights, key visual elements. |
| **Primary: Moonlight Indigo** | `#6F6CA8` | Selected states, primary text in light theme, main brand color. |
| **Secondary: Misty Sage** | `#C6D5C0` | Calm UI blocks, chips, soft secondary backgrounds. |
| **Secondary: Powder Blue** | `#C7D9F1` | Gentle container fills, subtle accents. |
| **Secondary: Blush Pink** | `#EBC9D8` | Relaxed highlights, focus state indicators. |
| **Background: Warm Ivory** | `#FAF7F2` | Light theme screen background. |
| **Background: Soft Cream** | `#F5F1EA` | Light theme card/container surface background. |
| **Background: Deep Indigo** | `#1F2235` | Dark theme screen background. |
| **Background: Midnight Violet** | `#2B2740` | Dark theme card/container surface background. |
| **Background: Near Black** | `#0D0D12` | AMOLED black mode background. |

> **Important:**
> *   Avoid pure black (`#000000`) and pure, harsh white (`#FFFFFF`).
> *   Dynamic Material You colors must be softened and desaturated to fit these hex ranges.

---

## 2. Typography & Fonts

*   **Font Family:** Modern rounded sans-serif font (e.g., `Nunito`, `Manrope`, `Outfit`, or `Plus Jakarta Sans`).
*   **Optional Serif:** A soft serif or handwritten font can be used *exclusively* for inspirational quotes or journal entries.
*   **Rules:** Never use ALL CAPS. Use comfortable, airy line spacing.

### Typography Hierarchy

| Style Role | Font Size (sp) | Weight | Line Spacing / Details |
| :--- | :--- | :--- | :--- |
| **H1 (Large headings)** | `32.sp` | SemiBold | Generous line-height, quiet presence. |
| **H2 (Sub-headings)** | `26.sp` | Medium | Soft hierarchy separation. |
| **Section Title** | `20.sp` | Medium | Used for card groups and lists. |
| **Body (Default Text)** | `16.sp` | Normal / Regular | Highly readable body font. |
| **Caption (Small details)** | `13.sp` | Normal | Meta info, subtle secondary text. |

---

## 3. Spacing & Dimensions (8dp Grid)

*   **Screen Margins:** `24.dp` (Left, Right, Top, Bottom)
*   **Spacing between major sections:** `32.dp`
*   **Spacing between cards:** `20.dp`
*   **Spacing between inline elements:** `16.dp`
*   **Floating Navigation Margin:** `16.dp` from bottom screen edge.
*   **Touch Targets:** Minimum `48.dp` (with `56.dp` preferred for primary buttons).

---

## 4. Corner Radius System

Uniform rounded corners define the organic feel of the Lunaflora design system:

| UI Component | Corner Radius (dp) |
| :--- | :--- |
| **Cards** | `28.dp` |
| **Buttons (Primary & Secondary)** | `28.dp` |
| **TextFields** | `24.dp` |
| **Bottom Navigation Bar** | `32.dp` |
| **Dialogs & Modals** | `28.dp` |
| **Images & Thumbnails** | `24.dp` |

---

## 5. UI Components & Layouts

### Cards (Containers)
*   Must look like floating paper resting gently above the background.
*   Use `28.dp` corner radius, soft diffused elevation shadow (2–4dp equivalent), no hard outlines, and `20.dp` internal padding.

### Buttons
*   **Primary Button:** Filled, styled with a soft pastel gradient, height `56.dp`, corner radius `28.dp`. Expands to match parent width (minus screen margins) and is centered.
*   **Secondary Button:** Outlined, transparent background, soft border, low emphasis.
*   **Text Button:** Minimal emphasis, transparent background, used for Skip and secondary settings.

### Text Fields
*   Fully rounded (`24.dp` corners), soft background fill, no hard borders or lines, friendly placeholder prompts.

### Floating Bottom Navigation
*   Floating above the bottom edge (`16.dp` margin), `32.dp` corners.
*   Exactly 5 tabs.
*   **Icon Style:** Rounded, outlined Material Symbols (no emojis!).
*   **Center Breathe Icon:** Styled 10–15% larger than the others to serve as a visual anchor.
*   *Tabs:* `Home`, `Journal`, `Breathe`, `Focus`, `Me`

### Top Bar
*   Consistent throughout the app.
*   Left: Logo/App brand title (`Ataraxia`). Right: Rounded profile avatar (`👤` or photo).
*   No search bars, overflow dots, filter buttons, or notification bells.

---

## 6. Motion & Micro-Interactions

Animations should resemble wind, breathing, flowing water, or floating petals. Never use snappy, bouncy, or game-like motion.

### Durations
*   **Button Press:** `150ms` (Gently shrinks to 97% scale).
*   **Fade Transitions:** `300ms`
*   **Card Entrance / Floating Animations:** `350ms`
*   **Screen transitions:** `400ms` (Visual feel of a page turning in a paper journal).
*   **Breathing Animation cycle:** `4000ms – 6000ms` (Inhale/Exhale fade).

### Interactive Details
*   Selected mood chips must glow softly.
*   Cards should lift slightly on touch.
*   Bottom navigation tabs must smoothly transition active states without sudden jumps.

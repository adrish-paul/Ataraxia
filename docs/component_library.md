# ATARAXIA — COMPONENT LIBRARY & DESIGN COMPONENTS

> This document acts as the master component blueprint. Every screen and feature must reuse these components to ensure complete layout consistency and to prevent duplicate design definitions in code.

---

## 1. Design Token Specifications

All values are parameterized as tokens to avoid hardcoded styling.

### Color Tokens
*   `PrimaryLavender` (#B9A7D6)
*   `MoonIndigo` (#6F6CA8)
*   `WarmIvory` (#FAF7F2)
*   `Cream` (#F5F1EA)
*   `Sage` (#C6D5C0)
*   `Peach` (#EBC9D8)
*   `PowderBlue` (#C7D9F1)
*   `MutedRose` (#D99EA5)
*   `DeepIndigo` (#1F2235)

### Spacing Tokens
*   `Space4` = `4.dp`
*   `Space8` = `8.dp` (Base Grid Unit)
*   `Space12` = `12.dp`
*   `Space16` = `16.dp` (Element gap)
*   `Space20` = `20.dp` (Card gaps / Internal Card padding)
*   `Space24` = `24.dp` (Screen Margins)
*   `Space32` = `32.dp` (Section gap)
*   `Space40` = `40.dp`
*   `Space48` = `48.dp`

### Shape Tokens (Corner Radii)
*   `ExtraLarge` = `32.dp` (Bottom Navigation)
*   `Large` = `28.dp` (Cards, Buttons, Dialogs)
*   `Medium` = `24.dp` (TextFields, Images)
*   `Small` = `12.dp` (Chips, Badges)
*   `Pill` = Rounded corners (`50%` of height)

### Elevation Tokens
*   `None` = `0.dp`
*   `Low` = `2.dp` (Standard floating cards)
*   `Medium` = `4.dp` (Pressed states / Dialogs)
*   `Floating` = `8.dp` (Bottom Navigation / Modals)

### Animation Timings
*   `Fast` = `150ms` (Button Press scaling)
*   `Normal` = `300ms` (Fades / Card transitions)
*   `Slow` = `400ms` (Screen Transitions)
*   `Breathing` = `4000ms – 6000ms` (Lotus Inhale/Exhale cycles)

---

## 2. Reusable Visual Primitives

| Composable Name | Description & Visual Parameters |
| :--- | :--- |
| **AtaraxiaPrimaryButton** | Filled height `56.dp`, fully rounded `28.dp` corners, desaturated gradient fill, centers parent content, shrinks to 97% scale on touch. |
| **AtaraxiaSecondaryButton**| Outlined height `56.dp`, transparent background, soft thin border, fully rounded corners. |
| **AtaraxiaTextButton** | Low-emphasis borderless button. Minimum touch target `48.dp`. |
| **LunafloraCard** | Default container component with `28.dp` corner radius, `2.dp` soft elevation, and `20.dp` internal padding. |
| **GreetingCard** | Displays time-aware backdrop, customized greeting text, and a decorative illustration. |
| **QuoteCard** | Floating affirmation box with centered typography and clean line-height parameters. |
| **ReflectionCard** | Displays a notebook-styled prompt with a primary button to launch writing. |
| **JournalCard** | Lists entries on the timeline, exposing title, snippet, time & weather icon, and mood label. |
| **MoodChip** | Selectable chip displaying an emoji or icon and state label. Selected chip scales to 102% and glows. |
| **PrimaryTextField** | Borderless text field with `24.dp` corner radius and soft cream/indigo background fill. |
| **SectionHeader** | Custom title and subtitle layout separating main pages. |
| **AmbientCard** | Displays a decorative cover image, title, and a play/pause toggle. |
| **DurationChip** | Selectable circular timer button with state outline glow. |
| **AnimatedLotus** | Performs smooth Inhale, Hold, Exhale, and Rest transitions. |
| **FloatingBottomNavigation**| Floating navbar (`72.dp` height, `16.dp` bottom margin, translucent glass-blur background). |
| **ProfileAvatar** | Circular avatar (`48.dp` or `96.dp`) supporting initials, user image, or neutral illustrations. |
| **SettingRow** | Layout wrapper containing list items with dynamic sliders, switches, arrows, or dropdowns. |
| **AtaraxiaDialog** | Modal container (`28.dp` corner radius, custom cancel, primary, and muted rose destructive options). |
| **EmptyState** | Clean placeholder layout displaying a simple icon/illustration, descriptive text, and a prompt action. |
| **GentleLoading** | Replaces circular progress indicators with slow breathing dots or drifting petals fading continuously. |
| **AtaraxiaSnackbar** | Reusable notification popup sliding in from the bottom with soft rounded corners and auto-dismiss. |

---

## 3. Composable Animation Helpers

We define the following standard API animations:
*   `FadeIn(content)` & `FadeOut(content)`: `300ms` duration.
*   `SlideUp(content)` & `SlideDown(content)`: `350ms` slide duration.
*   `GentleScale(content)`: `150ms` scaling down on press to `97%` and returning to `100%`.
*   `GlowAnimation(content)`: Applies an animated, pulsating ambient blur shadow.

---
name: "MedClinic — Appointment desk"
description: "The clinic visit rendered as a clear appointment document."
colors:
  primary: "#176b9b"
  primary-hover: "#0e527b"
  primary-soft: "#eaf4fb"
  primary-subtle: "#d6eafa"
  bg: "#f5f9fc"
  surface: "#ffffff"
  surface-subtle: "#edf3f7"
  border: "#d9e4ec"
  border-strong: "#768c9c"
  text: "#203442"
  text-muted: "#506775"
  success: "#26734f"
  warning: "#8a5b16"
  danger: "#a93e47"
  info: "#275f88"
  success-soft: "#edf7f1"
  warning-soft: "#fbf3e3"
  danger-soft: "#fbedef"
  info-soft: "#edf5fc"
  dark-primary: "#8dcbf4"
  dark-primary-hover: "#b6ddfa"
  dark-primary-soft: "#203f54"
  dark-primary-subtle: "#2c5068"
  dark-on-action: "#0e2b40"
  dark-bg: "#101e2a"
  dark-surface: "#172b3a"
  dark-surface-subtle: "#203847"
  dark-border: "#344e60"
  dark-border-strong: "#748fa0"
  dark-text: "#e9f2f8"
  dark-text-muted: "#b4c8d6"
  dark-success: "#a0d6b5"
  dark-warning: "#e6c486"
  dark-danger: "#f0b0b6"
  dark-info: "#a4cdef"
  dark-success-soft: "#1e3c32"
  dark-warning-soft: "#3d3423"
  dark-danger-soft: "#422c35"
  dark-info-soft: "#203c52"
typography:
  display:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: "clamp(2.2rem, 4.4vw, 3.6rem)"
    fontWeight: 650
    lineHeight: 1.1
    letterSpacing: "-0.04em"
  headline:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: "1.875rem"
    fontWeight: 650
    lineHeight: 1.25
    letterSpacing: "-0.03em"
  title:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: "1rem"
    fontWeight: 650
    lineHeight: 1.2
    letterSpacing: "-0.025em"
  body:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.6
  body-small:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: ".875rem"
    fontWeight: 400
    lineHeight: 1.6
  label:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: ".875rem"
    fontWeight: 550
    lineHeight: 1.6
  meta:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: ".8125rem"
    fontWeight: 400
    lineHeight: 1.6
  action:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: ".9375rem"
    fontWeight: 600
    lineHeight: 1.5
    letterSpacing: "0"
  field:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.5
  navigation:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: ".875rem"
    fontWeight: 500
    lineHeight: 1.6
  status:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: ".75rem"
    fontWeight: 550
    lineHeight: 1.3
    letterSpacing: "0"
  slot:
    fontFamily: "Geist, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"
    fontSize: ".875rem"
    fontWeight: 550
    lineHeight: 1.6
rounded:
  sm: ".375rem"
  md: ".75rem"
  lg: ".875rem"
  avatar: ".5rem"
  portrait: "4px"
spacing:
  quarter: ".25rem"
  half: ".5rem"
  three-quarter: ".75rem"
  base: "1rem"
  group: "1.25rem"
  section: "1.5rem"
  wide: "2rem"
  large: "3rem"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.surface}"
    typography: "{typography.action}"
    rounded: "{rounded.sm}"
    padding: ".6rem 1rem"
    height: "44px"
  button-primary-hover:
    backgroundColor: "{colors.primary-hover}"
    textColor: "{colors.surface}"
  button-primary-dark:
    backgroundColor: "{colors.dark-primary}"
    textColor: "{colors.dark-on-action}"
  button-primary-dark-hover:
    backgroundColor: "{colors.dark-primary-hover}"
    textColor: "{colors.dark-on-action}"
  button-outline:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.primary}"
    typography: "{typography.action}"
    rounded: "{rounded.sm}"
    padding: ".6rem 1rem"
    height: "44px"
  button-outline-hover:
    backgroundColor: "{colors.primary-soft}"
    textColor: "{colors.primary}"
  input-field:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    typography: "{typography.field}"
    rounded: "{rounded.sm}"
    padding: ".375rem .75rem"
    height: "46px"
  navigation-public:
    textColor: "{colors.text-muted}"
    typography: "{typography.navigation}"
    rounded: "0"
    padding: ".7rem .75rem"
  navigation-public-active:
    backgroundColor: "{colors.surface-subtle}"
    textColor: "{colors.primary}"
  navigation-portal:
    textColor: "{colors.text}"
    typography: "{typography.navigation}"
    rounded: ".35rem"
    padding: ".7rem .65rem"
  navigation-portal-active:
    backgroundColor: "{colors.primary-soft}"
    textColor: "{colors.primary}"
  status-success:
    backgroundColor: "{colors.success-soft}"
    textColor: "{colors.success}"
    typography: "{typography.status}"
    rounded: "{rounded.sm}"
    padding: ".4em .65em"
  workflow-card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    rounded: "{rounded.md}"
    padding: "1.25rem"
  appointment-slot:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    typography: "{typography.slot}"
    rounded: "{rounded.sm}"
    padding: ".65rem .5rem"
    height: "46px"
  appointment-slot-selected:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.surface}"
  appointment-slot-disabled:
    backgroundColor: "{colors.surface-subtle}"
    textColor: "{colors.text-muted}"
---

# Design System: MedClinic

## Overview

**Creative North Star: "Appointment desk"**

Appointment desk translates the clinic appointment slip into a calm working interface. Cool white, white reading surfaces, blue ink and medical-blue actions give clinician, time, fee and next action a clear place. The existing MedClinic logo and real clinician portraits keep the clinic recognizable.

One local Geist family carries both broad public headings and compact operational data. Fine rules and coherent document sections define hierarchy; borders establish surfaces, while soft shadows belong to temporary menus and contextual help. Light and dark themes preserve the same relationships.

The user's approved rejection is an interchangeable AI-looking interface: ornamental gradients, halos, repeated benefit tiles and floating cards do not belong to this world. Mobile adapts the composition to the patient's next action; desktop preserves the density needed for clinical work.

**Key Characteristics:**

- Useful appointment and clinical facts before decoration.
- One local Geist family, aligned numerals and visible focus.
- Cool-white and blue-ink surfaces, medical-blue actions and fine rules.
- Responsive document groups with real portraits and retained interaction contracts.

Extracted from the implemented shared stylesheet and public/portal shells, with sampled home, booking, authentication, inventory and examination templates. The frontmatter records reusable source values; it is documentation, not another CSS source. Theme colors prefixed `dark-` are the actual overrides under `data-bs-theme="dark"`. Component primitives describe the light baseline unless explicitly named dark; live snippets use the source custom properties to follow either theme. Component `height` values document a minimum, not a fixed box.

## Colors

Cool white and blue ink form the neutral reading field; medical blue supplies the action voice. Token values in the frontmatter are normative. Sidecar tonal ramps are synthesized, gamut-bounded OKLCH preview strips with reduced chroma near black and white, not additional shipped CSS variants.

### Primary

- **Medical Blue** (`primary`) identifies links, primary actions and selected appointment slots; **Deep Medical Blue** (`primary-hover`) provides their hovered and pressed state.
- **Blue Paper** (`primary-soft`) marks quiet hover and selection areas; **Blue Highlight** (`primary-subtle`) carries text selection and stronger tonal emphasis.
- Dark mode uses **Pale Medical Blue** (`dark-primary`) and its brighter hover value on the dark reading field. Its primary action text uses `dark-on-action`; light primary actions use `surface`.

### Neutral

- **Cool White** (`bg`) is the page field; **Clean White** (`surface`) is the reading surface; `surface-subtle` separates headers and inactive controls.
- **Blue Ink** (`text`) carries the main copy; **Quiet Blue Ink** (`text-muted`) carries context and placeholders.
- `border` separates document groups; `border-strong` defines controls and the scrollbar thumb.
- The corresponding dark tokens preserve blue-cast backgrounds, pale ink, distinct surfaces and readable boundaries rather than mechanically inverting the light palette.
- Success, warning, danger and information each have a foreground and soft background in both themes. They describe real clinical, stock, booking and validation states rather than adding decorative accents.
- The public footer retains a deep blue field with pale-blue reading text and separators. The portal drawer backdrop uses the deep-blue canvas hue at 45% opacity. These shell treatments are source-specific, not extra shared color tokens.

**The Action Blue Rule.** Medical blue marks actions, links and selected states; semantic feedback retains its own meaning.

## Typography

**Display Font:** locally bundled Geist variable, with the source platform sans fallbacks.
**Body Font:** the same Geist family.
**Character:** plain, precise and document-like, with variable weights providing hierarchy. Geist Mono is bundled by the vendor stylesheet but is not the adopted visual voice.

### Hierarchy

- **Display** (`display`): public home heading; balanced text, a maximum measure of 18ch on wide screens, and the measured responsive sizes below.
- **Headline** (`headline`): shared public and portal page title; its narrow-screen size becomes 1.5rem below the medium boundary.
- **Title** (`title`): compact data headings. The public section heading uses 1.55rem with weight 650 and tracking -0.03em, becoming 1.35rem on the smallest layout.
- **Body** (`body`): Bootstrap's observed base size and weight with the shared line-height override. Public introductory copy uses 1.05rem / 1.7 and a 60ch measure; shared page subtitles use 70ch.
- **Label** (`label`) and **Meta** (`meta`): form labels and supporting facts. Table column labels use the same meta size with weight 550, and ordinary small body copy uses `body-small`.
- **Action**, **Field**, **Navigation** and **Status** tokens record the actual reused component scales. Table, money, fee, queue and time values use tabular numerals.

**The One Family Rule.** Use local Geist for headings, body, labels and numbers; tabular numerals align clinical and financial data without introducing a display or monospace costume.

## Layout

Use coherent sections and rows rather than promoting every fact into an equal card. The shared rhythm combines the spacing steps in the frontmatter; small controls and metadata sit close together, while document sections use the larger steps. Public layouts inherit the bundled Bootstrap container: maximum widths of 540, 720, 960, 1140 and 1320px at the 576, 768, 992, 1200 and 1400px boundaries respectively.

The public home uses a broad introduction and narrower visit sequence (1.7fr / 1fr) with a 3.5rem column gap. At and below 1199.98px the gap is 2rem and the navigation collapses. At and below 991.98px the home becomes one column in the order introduction, facts, specialties, clinicians, journey, support; booking and consultation summaries stop sticking. On wide layouts those summaries stick 6rem from the top.

At and below 575.98px, home search and the primary booking action take their own full-width lines. The specialty index changes from three columns to two. Clinician rows use `72px minmax(0, 1fr)`, a 1rem column gap, a 72 × 88px portrait and the booking link in column two on its following line. The corrected actual 390px viewport provides about 263px to the text column; this is verification evidence for that viewport, not a universal text-width token. Clinician names use 1rem here.

The home display changes to `clamp(2.1rem, 4.3vw, 3.1rem)` below the wide navigation boundary, `clamp(2rem, 5.5vw, 3rem)` below 991.98px, then 2.25rem with line-height 1.12 below 575.98px.

The portal has a fixed 244px sidebar, a 72px topbar and a content maximum of 1540px, with 2rem horizontal gutters. At and below 767.98px the sidebar becomes the existing drawer, content fills the viewport, the topbar becomes 64px and horizontal gutters become 1rem. Auth layouts become a single form column; the desktop explanatory aside is hidden.

Desktop inventory and prescription tables deliberately retain bounded horizontal regions when their columns need more width. The original JavaScript adds region labels and conditional keyboard focus; focusing offscreen actions scrolls those controls into view. Below the medium boundary, prepared tables become labeled two-column records; primary information and actions span both columns. Keep the existing table preparation, resize handling and drawer focus behavior.

## Elevation & Depth

Depth is flat by default: paper, white or dark reading fields and fine borders establish hierarchy without lifting persistent content. The small shadow is the observed temporary menu/help elevation. The existing larger shadow declaration is unused in the inspected surfaces and is not promoted into the reusable vocabulary.

### Shadow Vocabulary

- **Temporary surface:** the exact `--mc-shadow-md` value is recorded in the sidecar, used by dropdown menus and contextual help bubbles.
- **Flat surface:** cards and persistent documents use `box-shadow: none`.

**The Flat Document Rule.** Keep persistent reading surfaces flat with a fine border or sectional rule; reserve the observed soft shadow for temporary menus and contextual help.

Color and border feedback lasts 150ms in public navigation, 160ms in portal navigation and 180ms in shared controls and the mobile drawer, all using the source `ease`. Preserve reduced-motion overrides: transitions stop, modal transforms are removed and scrolling becomes immediate. The retained loading spinner slows to 1.5s under reduced motion; this is an existing exception, not a general animation pattern.

## Shapes

Small controls and feedback use `rounded.sm`; persistent outlined containers use `rounded.md`; modal surfaces use `rounded.lg`. Portraits use the observed small rectangular corners; avatars use `rounded.avatar`. Actual step markers and user identities may be circular because they communicate a sequence or person. Fine borders remain one pixel. Public navigation keeps square edges; portal route links retain their observed .35rem corners.

## Components

### Buttons

Direct, compact actions use the shared action typography and a minimum height of 44px. The primary and outlined MedClinic variants use the frontmatter padding. Primary hover and active use the darker or brighter theme action value; outlined hover uses a soft blue surface and primary border. Light buttons use the quiet neutral hover. Small buttons retain the minimum target height while reducing text to .8125rem and padding to .45rem .7rem. Icon-only buttons also have a 44px minimum width. Disabled actions use opacity .55; preserve native disabled semantics.

### Inputs / Fields

Fields use the control corner, a strong one-pixel border, a theme surface and a minimum height of 46px. Placeholders use quiet blue ink at full opacity. Focus changes the border to primary and applies a two-pixel outline with a two-pixel offset, without a halo. Invalid fields use the danger border, focus outline and existing validation message. Disabled fields use the subtle surface and muted text. Keep visible labels and the actual form validation contracts.

### Navigation

Public links use quiet blue ink, a soft neutral hover and primary text for the current route; active links also carry an underline. Below the wide boundary they appear in the existing collapsible panel with a three-rem minimum row height. Portal links use a fine transparent boundary at rest; the active route uses soft blue, a visible border and weight 650. The original JavaScript supplies the current-route class and `aria-current`; the mobile drawer keeps its existing dismissal and focus management.

Shared focus is a three-pixel outline with a three-pixel offset; public and portal shells specialize it to two pixels. Preserve those effective shell rules and the inset outline for horizontal table regions.

### Status Tags

Compact rectangular labels use the status typography and padding. Pair each semantic foreground with its corresponding soft surface in the active theme, retaining a text label. These are status indicators, not replacement buttons; no hover or pressed state is invented for noninteractive tags.

### Cards / Containers

Persistent containers have a theme surface, a fine border and no shadow. Workflow and patient-summary containers use the documented workflow padding; profile, clinician and record variants use the observed 1.5rem or 2rem padding where present. Directory clinician and record sections can use top/bottom rules without a rounded box. On narrow layouts the shared form, profile and workflow containers reduce to 1.25rem padding.

### Appointment Slots and Summary

Available slots use the strong control boundary and a minimum height of 46px. Hover uses the soft primary surface; selection uses the primary action surface and its on-action text. Disabled slots remain legible with muted text and a subtle surface, full opacity and the native unavailable state. Keep the existing slot-picker status announcements and selection logic. The summary presents clinician, date, time and fee in ruled rows with values allowed to wrap; it becomes an ordinary flow section on narrow layouts.

### Browser and Identity Surfaces

Use the observed primary caret, themed text selection, themed scrollbar and underline offsets. Both layout theme-color metadata and the manifest use the light medical-blue action color; the offline document uses the same light page, ink and action values. The original logo remains recognizable; use the existing clinician portraits with meaningful alternative text and the original contextual help assets. No new media is required by this system.

The extraction checked `.impeccable/review/asset-origins.json`: it records embedded repository provenance and unchanged decoded pixels for all 23 retained originals, without asserting original authorship or licensing. The source-of-decision archive in `.impeccable/review/seed-evidence.json` was recovered at finish, as the direction document states. These evidence files are project records, not visual tokens.

## Do's and Don'ts

### Do:

- **Do** group clinician, date, time, fee and the next action as a coherent document.
- **Do** use theme-bound semantic custom properties so every component adapts to light and dark.
- **Do** preserve the minimum action and field sizes, visible focus, labels, reduced motion and validation feedback.
- **Do** let mobile information columns shrink safely while moving their action to the following line.
- **Do** retain accessible inner horizontal table regions and labeled mobile records through the original JavaScript.
- **Do** retain real clinician portraits, the existing logo and meaningful sequential numbers.

### Don't:

- **Don't** introduce ornamental gradients, halos, floating card effects or interchangeable benefit-card grids.
- **Don't** add another display family, decorative monospace, glyph icons or invented clinical claims.
- **Don't** make the clinician's next action compete with the name and fee on the same narrow mobile line.
- **Don't** remove semantic status labels or depend on color alone to explain a state.
- **Don't** treat the generated direction image as an approved pixel specification or the fix-only verdict as a new whole-surface review.

The independent full review initially required the mobile clinician correction; its follow-up verdict accepts that sole scored fix. This document records the corrected source and does not claim another whole-surface review, independently exercised keyboard session or payment test. Declared but unused accent, secondary, largest radius and large shadow tokens were not canonized; local exceptions and future findings remain subject to review rather than becoming new house rules.

The approved medical-blue/cool-white refinement is separately accepted by `.impeccable/review/color/finish-review.md` with disposition `ship`. Its supplied contrast and computed-color evidence are in the same color review directory. That review covers the palette refinement and does not replace the original whole-surface review.

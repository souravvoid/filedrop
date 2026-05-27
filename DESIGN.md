---
name: PeerLink
colors:
  background: "#121212"
  surface: "#1c1c1e"
  surface-elevated: "#242426"
  surface-hover: "#2c2c2e"
  border: "#3a3a3c"
  border-hover: "#48484a"
  primary: "#0a84ff"
  primary-hover: "#007aff"
  success: "#30d158"
  success-hover: "#28cd41"
  danger: "#ff453a"
  text-primary: "#ffffff"
  text-secondary: "#ebebf5"
  text-muted: "#98989d"
  shadow: "rgba(0, 0, 0, 0.5)"
  progress-track: "#3a3a3c"
typography:
  title:
    fontFamily: "Segoe UI"
    fontSize: 20px
    fontWeight: "700"
  status:
    fontFamily: "Segoe UI"
    fontSize: 15px
    fontWeight: "700"
  label:
    fontFamily: "Segoe UI"
    fontSize: 14px
    fontWeight: "400"
  button:
    fontFamily: "Segoe UI"
    fontSize: 14px
    fontWeight: "700"
  metrics:
    fontFamily: "Segoe UI"
    fontSize: 13px
    fontWeight: "400"
rounded:
  sm: 6px
  md: 8px
  lg: 12px
spacing:
  unit: 8px
  xs: 4px
  sm: 8px
  md: 10px
  lg: 15px
  xl: 20px
  xxl: 25px
  root-padding: 30px
  status-top-margin: 25px
elevation:
  card:
    type: dropshadow
    passes: three-pass-box
    color: "rgba(0, 0, 0, 0.5)"
    radius: 10px
    offsetY: 4px
motion:
  button-hover:
    type: instant
    duration: 0ms
  drop-zone-hover:
    type: instant
    duration: 0ms
dimensions:
  window-width: 700px
  window-height: 500px
  card-pref-width: 350px
  drop-zone-height: 160px
  progress-bar-height: 8px
components:
  card:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
    padding: "{spacing.xl}"
    elevation: "{elevation.card}"
  drop-zone:
    backgroundColor: "{colors.surface-elevated}"
    borderColor: "{colors.border}"
    borderStyle: dashed
    borderWidth: 2px
    rounded: "{rounded.md}"
    height: "{dimensions.drop-zone-height}"
  drop-zone-hover:
    backgroundColor: "{colors.surface-hover}"
    borderColor: "{colors.primary}"
  code-field:
    backgroundColor: "{colors.surface-hover}"
    textColor: "{colors.text-primary}"
    borderColor: "{colors.border}"
    rounded: "{rounded.sm}"
    padding: "{spacing.sm}"
    typography: "{typography.label}"
  code-field-focused:
    borderColor: "{colors.primary}"
  button:
    typography: "{typography.button}"
    rounded: "{rounded.sm}"
    padding: "8px 16px"
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.text-primary}"
  button-primary-hover:
    backgroundColor: "{colors.primary-hover}"
  button-secondary:
    backgroundColor: "{colors.border}"
    textColor: "{colors.text-primary}"
  button-secondary-hover:
    backgroundColor: "{colors.border-hover}"
  button-success:
    backgroundColor: "{colors.success}"
    textColor: "{colors.text-primary}"
  button-success-hover:
    backgroundColor: "{colors.success-hover}"
  button-danger:
    backgroundColor: transparent
    borderColor: "{colors.danger}"
    textColor: "{colors.danger}"
    rounded: "{rounded.sm}"
  button-danger-hover:
    backgroundColor: "{colors.danger}"
    textColor: "{colors.text-primary}"
  status-panel:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
    padding: "15px 20px"
  progress-bar:
    accentColor: "{colors.primary}"
    trackColor: "{colors.progress-track}"
    rounded: 4px
    height: "{dimensions.progress-bar-height}"
---

## Brand & Style

PeerLink is a peer-to-peer file transfer desktop application with a minimalist, dark-themed interface inspired by Apple's design language. The aesthetic is clean and utilitarian -- built for a single purpose: securely transferring files between devices on the same network.

The UI centers on a two-panel card layout (Send / Receive) with a status bar at the bottom. The design prioritizes clarity and speed over visual flourish, using a restrained color palette where semantic colors (blue for primary actions, green for receiving, red for cancellation) carry all functional meaning.

## Colors

The palette is a near-black canvas punctuated by high-contrast Apple-system accent colors.

- **Background (#121212):** A deep near-black that reduces eye strain and lets elevated surfaces pop.
- **Surface (#1c1c1e):** Cards and panels sit one step above the background, creating subtle hierarchy without harsh borders.
- **Elevated Surface (#242426):** Interactive zones like the drop zone use a lighter surface to invite interaction.
- **Primary Blue (#0a84ff):** The main action color, used for the "Start Sending" button, progress bar fill, and focus states. This is the classic Apple system blue -- instantly recognizable and associated with primary actions.
- **Success Green (#30d158):** Reserved exclusively for the "Receive File" button, creating a clear visual distinction between send and receive workflows.
- **Danger Red (#ff453a):** Used as an outlined ghost button for "Cancel Transfer," filling solid red on hover to signal destructive intent.
- **Text Hierarchy:** White (#ffffff) for titles and input text, soft white (#ebebf5) for labels and status text, and muted gray (#98989d) for secondary metrics and helper text.

## Typography

The system uses **Segoe UI** with Arial as fallback -- the default Windows system font stack, chosen for its neutrality and ubiquity across platforms.

- **Titles (20px bold):** Card headers ("Send File", "Receive File") and status text use the largest, boldest type to establish clear section hierarchy.
- **Labels (14px regular):** Form labels, drop zone text, and code field prompts sit at the body level for comfortable reading.
- **Buttons (14px bold):** All buttons use bold weight at 14px to feel actionable and distinct from static labels.
- **Metrics (13px regular):** Speed and ETA readouts use the smallest text size, relegated to supporting information.

## Layout & Spacing

The layout is a fixed two-column card arrangement within a 700x500 window.

- **Root Padding:** A generous 30px padding surrounds all content, giving the interface breathing room.
- **Card Gap:** 25px separates the Send and Receive panels.
- **Card Internal Spacing:** 20px vertical gap between elements within each card.
- **8px Base Unit:** Most smaller spacing values (button padding, code field padding, element gaps) are multiples of 8px or close to it (10px), maintaining visual rhythm.
- **Status Panel:** Separated from the main content by a 25px top margin, with internal 15px vertical and 20px horizontal padding.

## Elevation & Depth

Depth is achieved through a single elevated layer: cards.

- **Card Shadow:** A three-pass box drop shadow with 50% black at 10px blur and 4px Y offset creates a subtle lift effect. This is the only shadow in the entire interface.
- **No Multi-Level Elevation:** Unlike Material Design's layered elevation system, PeerLink uses a flat two-level model: background surface and card surface. The status panel shares the card background but has no shadow, keeping it visually grounded at the bottom.

## Shapes

Corner radii are modest and functional, scaling with element importance.

- **Cards (12px):** The largest radius, giving the two main panels a soft, approachable feel.
- **Drop Zone & Borders (8px):** Interactive surfaces use a medium radius.
- **Buttons & Inputs (6px):** The smallest radius, keeping actionable elements feeling precise and clickable.
- **Progress Bar (4px):** Minimal rounding for the thin progress indicator.

## Components

### Cards

Two identical container cards flank the window: "Send File" (left) and "Receive File" (right). Each contains a title, interactive content area, optional spacer, and a full-width CTA button. Cards have rounded corners, a dark surface, and a soft drop shadow.

### Drop Zone

The send card features a large dashed-border drop zone (160px tall) for drag-and-drop file input. On hover, the background lightens and the border shifts from gray to primary blue, providing clear visual feedback. Contains a muted text label and a "Select File" secondary button as an alternative input method.

### Code Fields

Read-only and editable text fields display or accept invite codes (IP:port strings). They use a slightly elevated background with a subtle border. On focus, the border turns primary blue to indicate the active field.

### Buttons

Four button variants serve distinct roles:
- **Primary (blue):** Main call-to-action ("Start Sending"). Solid fill.
- **Secondary (gray):** Supporting actions ("Select File", "Copy", "Paste", "Open Folder"). Solid fill.
- **Success (green):** The receive workflow's primary action ("Receive File"). Solid fill.
- **Danger (red outline):** Destructive/cancel action ("Cancel Transfer"). Outlined by default, fills solid red on hover for emphasis.

All buttons share the same padding (8px 16px), border radius (6px), bold 14px type, and hand cursor.

### Status Panel

Anchored at the bottom of the window, the status panel displays:
- A status label with the current transfer state.
- A progress bar with blue accent fill.
- Speed and ETA metrics in muted text.
- Contextual action buttons (Cancel, Open Folder) that appear only during active transfers.

The panel shares the card background color but omits the drop shadow, keeping it visually anchored to the window bottom.

# Design System Specification: High-End Dark Mode

## 1. Overview & Creative North Star
The Creative North Star for this design system is **"The Neon Observatory."** 

This isn't a standard dark mode; it is a high-contrast, editorial experience that treats the interface as a deep-space viewport. We are moving away from the "flat box" aesthetic of traditional fintech and toward a layered, atmospheric environment. By utilizing deep navy voids (`#060E20`) and vibrant, neon-gas accents, we create a sense of immense depth and premium precision.

To break the "template" look, designers should lean into **intentional asymmetry**. Align large `display-lg` typography to the left while allowing data visualizations to bleed off the right edge of the grid. Use overlapping elements—such as glassmorphic cards that partially cover headline text—to create a sophisticated, tactile feel that suggests the UI is composed of physical, translucent layers.

---

## 2. Colors: Tonal Depth & Soul
Our palette is anchored in a monochromatic navy foundation, punctuated by high-energy primary, secondary, and tertiary "glows."

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning or structural separation. Boundaries must be defined solely through:
- **Background Color Shifts:** Moving from `surface` to `surface-container-low`.
- **Vertical Spacing:** Using the `12` (3rem) or `16` (4rem) spacing tokens to create logical "rooms" in the layout.
- **Tonal Transitions:** Subtle linear gradients that define the edge of a container.

### Surface Hierarchy & Nesting
Treat the UI as a series of stacked sheets of frosted glass.
- **Base Layer:** `surface` (#060E20)
- **Secondary Sections:** `surface-container-low` (#091328)
- **Interactive/Floating Cards:** `surface-container-highest` (#192540) or `surface-bright` (#1F2B49)

### The "Glass & Gradient" Rule
To inject "soul" into the interface, avoid flat fills for main CTAs. Use gradients that transition from `primary` (#5BF4DE) to `primary-container` (#11C9B4). For premium "glass" elements, use a semi-transparent `surface-variant` with a `backdrop-filter: blur(20px)`.

---

## 3. Typography: Editorial Authority
We use **Manrope** exclusively. Its geometric yet humanist qualities provide the "Modern Ledger" feel required for a premium experience.

*   **Display Scale (`display-lg` to `display-sm`):** Use these for hero numbers and high-impact headings. They should feel massive and authoritative. Set them with slightly tighter letter-spacing (-0.02em) to increase the "editorial" look.
*   **Headline & Title:** These serve as the structural anchors. Use `headline-lg` for section headers, always paired with a `label-md` in `primary` color for a "category" tag above the headline.
*   **Body & Labels:** `body-lg` is your workhorse. For secondary data or metadata, use `on_surface_variant` (#A3AAC4) to ensure the hierarchy is clear without needing to change font weight.

---

## 4. Elevation & Depth: Tonal Layering
In this system, "Elevation" does not mean a drop shadow; it means **light emission**.

### The Layering Principle
Depth is achieved by "stacking" the surface tiers. A `surface-container-lowest` (#000000) element placed on a `surface` background creates an "inset" or "carved" look, ideal for input fields. Conversely, a `surface-bright` element on `surface` creates a natural "lift."

### Ambient Shadows
Shadows are only permitted for floating modals or pop-overs. They must be extra-diffused:
*   **Shadow Color:** A 10% opacity version of `secondary` (#699CFF).
*   **Blur:** 40px - 60px.
*   **Spread:** -10px.
This creates a "neon glow" effect rather than a muddy grey shadow.

### The "Ghost Border" Fallback
If accessibility requires a container boundary, use a **Ghost Border**:
*   **Token:** `outline-variant` (#40485D).
*   **Opacity:** 15%.
*   **Width:** 1px.

---

## 5. Components

### Buttons
*   **Primary:** A vibrant gradient fill (Teal to Blue). `Roundedness: full`. No border. Text is `on_primary`.
*   **Secondary:** Ghost style. `Ghost Border` (outline-variant at 20%) with `on_surface` text.
*   **Tertiary:** Text-only using `tertiary` (#C180FF) color with a subtle hover state using a 10% opacity glow.

### Input Fields
*   **Style:** `surface-container-lowest` fill. 
*   **Border:** Bottom-only `outline` (#6D758C) at 1px, which transitions to 2px `primary` on focus.
*   **Corners:** `md` (0.75rem) for a more technical, ledger-like feel.

### Cards (The "Glass" Container)
*   **Rules:** Forbid divider lines within cards. Use `spacing-4` (1rem) as a minimum gutter between content chunks.
*   **Background:** Use `surface-container-high` at 80% opacity with a `backdrop-filter: blur(12px)`.

### Chips
*   **Selection:** `primary_container` fill with `on_primary_container` text.
*   **Filter:** `surface_variant` fill with `outline` ghost border.

### New Component: The "Lumina Glow" Indicator
Used for high-priority status or active states. A 4px circle of `primary` with an 8px outer glow of the same color at 30% opacity.

---

## 6. Do's and Don'ts

### Do
*   **Do** use `primary` and `secondary` gradients for data visualization (charts/graphs) to make them feel like "light" against the dark void.
*   **Do** use `rounded-xl` (1.5rem) for main dashboard containers to maintain the approachable, premium feel.
*   **Do** prioritize `on_surface` (#DEE5FF) for high-readability text; it is off-white to prevent eye strain.

### Don't
*   **Don't** use 100% white (#FFFFFF). It is too harsh against the `#060E20` background and breaks the premium "midnight" atmosphere.
*   **Don't** use standard "Grey" shadows. They make the UI look "dirty." Always tint shadows with a hint of navy or blue.
*   **Don't** use dividers. If you feel the need to separate two items, double the vertical spacing instead.
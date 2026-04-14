# Design System Documentation: The Luminous Ledger

## 1. Overview & Creative North Star
This design system is built to transform the mundane task of subscription management into a premium, editorial experience. We are moving away from the "utility dashboard" aesthetic toward a **Creative North Star** we call **"Luminous Clarity."** 

The interface should feel like a high-end digital sanctuary—organized, trustworthy, and deeply intentional. We break the "template" look by utilizing extreme roundedness (derived from the brand icon), generous whitespace, and a departure from traditional structural lines. Instead of rigid grids, we use tonal depth and sophisticated typography scales to guide the user’s eye.

## 2. Colors & Tonal Depth
The palette is a sophisticated gradient journey from vibrant teals to deep, authoritative blues and soft, intellectual purples.

### Core Brand Roles
*   **Primary (Teal - `#006a60`):** Used for growth, action, and "Memberships" category.
*   **Secondary (Blue - `#0048d8`):** Represents stability and "Streaming" services.
*   **Tertiary (Purple - `#7d2dd9`):** Represents creativity and "Software/SaaS" subscriptions.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to define sections. Boundaries must be established through background color shifts. 
*   Use `surface-container-low` for large background sections.
*   Use `surface-container-lowest` (White) for foreground cards to create natural separation.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers.
1.  **Base:** `surface` (`#f7f9fb`)
2.  **Sectioning:** `surface-container-low` (`#f2f4f6`)
3.  **Content Cards:** `surface-container-lowest` (`#ffffff`)

### Signature Textures & Glassmorphism
To achieve a high-end feel, floating elements (like bottom navigation or modal headers) should utilize **Glassmorphism**. Apply a semi-transparent `surface-container-lowest` with a 20px backdrop blur. For primary CTAs, do not use flat colors; use a subtle linear gradient from `primary` (`#006a60`) to `primary_container` (`#00d2be`) at a 135-degree angle.

## 3. Typography
Our typography pairing balances the "Engineered" feel of **Inter** with the "Editorial" character of **Manrope**.

*   **Display & Headlines (Manrope):** These are your "Statement" styles. Use `display-lg` for total monthly spend to command authority. The wide, geometric nature of Manrope mirrors the roundedness of our UI.
*   **Titles & Body (Inter):** Used for transactional data and subscription names. Inter provides the high-legibility "Trust" factor required for financial tracking.
*   **The Scale:** Create high contrast. Pair a `headline-sm` subscription name with a `label-sm` billing date in `on_surface_variant` to create a clear information hierarchy.

## 4. Elevation & Depth
We eschew traditional drop shadows in favor of **Tonal Layering**.

*   **The Layering Principle:** Depth is achieved by stacking. A card (`surface-container-lowest`) sitting on a section (`surface-container-low`) creates a soft, tactile lift.
*   **Ambient Shadows:** If a floating action button or card requires a shadow, it must be "Ambient." Use a blur of 32px, an offset of Y: 8, and an opacity of 6% using a tinted version of `on_surface`.
*   **The "Ghost Border" Fallback:** If accessibility requires a stroke (e.g., in high-contrast modes), use the `outline_variant` (`#bacac6`) at **15% opacity**. Never use 100% opaque borders.

## 5. Components

### Buttons
*   **Primary:** Uses the "Signature Texture" gradient. Shape: `full` (9999px). 
*   **Secondary:** `surface-container-high` background with `on_secondary_container` text.
*   **Behavior:** On press, scale down to 0.96 for a tactile "click" feel.

### Subscription Cards
*   **Structure:** No dividers. Use `spacing-5` (1.25rem) internal padding.
*   **Rounding:** Use `lg` (2rem) for the card container to mirror the icon's silhouette.
*   **Visual Cue:** Use a vertical "pill" of the category color (Teal, Blue, or Purple) on the far left edge (4px wide, `spacing-2` height) to denote the subscription type.

### Category Chips
*   **Style:** `surface-container-highest` background.
*   **Active State:** Use `primary_fixed` with `on_primary_fixed` text for high-end vibrance.

### Input Fields
*   **State:** Use `surface-container-low` as the fill. 
*   **Interaction:** On focus, the background transitions to `surface-container-lowest` with a "Ghost Border" of `primary` at 20% opacity.

### Navigation (The "Floating Dock")
*   Positioned `spacing-10` from the bottom.
*   Style: Glassmorphic `surface-container-lowest` (80% opacity) with `xl` (3rem) rounding.

## 6. Do's and Don'ts

### Do:
*   **Do** use `spacing-8` or `spacing-10` between major content blocks to allow the design to "breathe."
*   **Do** use color to categorize. Teal = Memberships, Blue = Streaming, Purple = Software.
*   **Do** ensure all icons use a "Duotone" style with `primary` and `primary_container` to match the brand icon's depth.

### Don't:
*   **Don't** use black (`#000000`) for text. Use `on_surface` (`#191c1e`) to maintain the "premium soft" feel.
*   **Don't** use sharp corners. Every container must have at least `sm` (0.5rem) rounding, though `lg` (2rem) is preferred for primary containers.
*   **Don't** use standard list dividers. Separate list items with `spacing-4` of vertical whitespace instead.
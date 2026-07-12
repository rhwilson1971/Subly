# Payment brand icons — design

## Problem

`PaymentType` already models specific card networks and payment services (VISA, MASTERCARD, DISCOVER, AMEX, PAYPAL, VENMO, CASHAPP, AFFIRM, KLARNA) alongside generic methods (DEBIT_CARD, BANK_TRANSFER, CASH, OTHER). Both apps currently render every type with a generic system icon (Android: `Icons.Default.Star`/`Info`/`MoreVert`; iOS: SF Symbols like `creditcard`, `dollarsign.circle`), so a Visa card and a PayPal account look identical in the UI. Users should be able to recognize their payment method at a glance.

## Scope

Update the 4 spots where a `PaymentType` already renders an icon today:

- Android: `PaymentMethodsScreen.kt` (list row), `AddEditPaymentMethodScreen.kt` (selected-type header row + type picker dialog rows)
- iOS: `PaymentMethodsView.swift` (list row), `AddEditPaymentMethodView.swift` (selected-type label + type picker sheet rows)

Subscription Detail shows the payment method as plain text (label/value row) on both platforms and is out of scope — no layout change there.

No changes to `PaymentType`, `PaymentMethod`, persistence, or view models. This is a pure rendering change.

## Visual spec

Each mark renders inside a rounded-rect "chip," default 44pt/dp (existing call sites that use smaller icons today keep their current size — 20–28dp — via a `size` parameter). Chip background is neutral (white in light mode / dark surface in dark mode, with a hairline border) for brands typically shown on a white background, or a solid brand color for brands typically shown as a colored badge:

| Type | Chip style | Mark |
|---|---|---|
| Visa | neutral | navy `#1A1F71` bold italic serif "VISA" wordmark |
| Mastercard | neutral | red `#EB001B` + orange `#F79E1B` overlapping circles |
| Amex | filled blue `#006FCF` | white "AMEX" |
| Discover | neutral | dark "Discover" wordmark + small orange (`#FF6600`) dot accent |
| PayPal | neutral | two-tone wordmark, "Pay" `#003087` / "Pal" `#009cde` |
| Venmo | filled blue `#008CFF` | white "V" |
| Cash App | filled black | white "$" |
| Affirm | filled black | white "affirm" + small blue dot accent |
| Klarna | filled pink `#FFB3C7` | black "Klarna" |
| Debit Card | neutral, tinted icon | generic credit-card icon |
| Bank Transfer | neutral, tinted icon | generic bank/columns icon |
| Cash | neutral, tinted icon | generic banknote icon |
| Other | neutral, tinted icon | generic help/more icon |

These are hand-built vector approximations (color + shape + typography cues), not traced copies of trademarked artwork.

## Technical approach

Code-drawn vectors on both platforms — no image assets, no new asset pipeline. Chosen over sourcing/importing SVG or PDF brand files because it stays crisp at any size, adapts to light/dark mode via the app's existing color system, and avoids a per-brand asset file to source and maintain on two platforms.

### Android

Rewrite `subly_android/.../ui/payment/PaymentTypeIcons.kt`:

- Remove `getPaymentTypeIcon`.
- Add `@Composable fun PaymentBrandIcon(type: PaymentType, modifier: Modifier = Modifier, size: Dp = 44.dp)`.
  - Renders a `Box` chip (`RoundedCornerShape`, background/border per the table above).
  - Wordmark brands: centered `Text`, font size scaled proportionally to `size`.
  - Mastercard: two overlapping circles drawn with `Canvas`/`drawCircle` (or two offset `Box`es with `CircleShape` background).
  - Generic types (`DEBIT_CARD`, `BANK_TRANSFER`, `CASH`, `OTHER`): tinted background + an extended Material icon (`Icons.Filled.CreditCard` / `Icons.Filled.AccountBalance` / `Icons.Filled.Payments` / `Icons.Filled.HelpOutline`) — the extended icons dependency is already in `build.gradle.kts`.
- Keep `formatPaymentType` unchanged.
- Update call sites to use `PaymentBrandIcon` instead of `Icon(imageVector = getPaymentTypeIcon(...))`:
  - `PaymentMethodsScreen.kt:204` (40dp)
  - `AddEditPaymentMethodScreen.kt:158` (20dp, header row)
  - `AddEditPaymentMethodScreen.kt:380` (24dp, picker dialog row)

### iOS

New file `subly_ios/.../UI/PaymentMethods/PaymentBrandMark.swift`:

- `struct PaymentBrandMark: View` taking `type: PaymentType` and `size: CGFloat = 44`.
- `ZStack`/`RoundedRectangle` chip matching the table above.
- Wordmark brands: `Text` (serif italic bold, or two-tone via `Text` concatenation with `+`) sized proportionally to `size`.
- Mastercard: two overlapping `Circle` shapes.
- Generic types: falls back to `Image(systemName: type.sfSymbol)` on a tinted background — keeps the existing `sfSymbol` property on `PaymentType` in use, no dead code.
- Update call sites:
  - `PaymentMethodsView.swift:126` — replace the `Image(systemName:)` + manual `.background`/`.clipShape` with `PaymentBrandMark(type:, size: 44)` (the component owns its own chip styling).
  - `AddEditPaymentMethodView.swift:66` — replace `Label(state.selectedType.displayName, systemImage: state.selectedType.sfSymbol)` with an `HStack` of `PaymentBrandMark(type:, size: 28)` + `Text(displayName)`.
  - `AddEditPaymentMethodView.swift:124` — replace `Image(systemName: type.sfSymbol).frame(width: 24)` with `PaymentBrandMark(type:, size: 28)`.

## Error handling

None needed — this is static, deterministic rendering from an existing enum value with no external input or I/O.

## Testing

No new unit tests: no ViewModel or business logic changes, purely presentational. Verify manually by running both apps and checking the Payment Methods list and the Add/Edit type selector (header + picker) in light and dark mode for a representative sample of types: Visa, Mastercard, PayPal, Klarna, and Debit Card.

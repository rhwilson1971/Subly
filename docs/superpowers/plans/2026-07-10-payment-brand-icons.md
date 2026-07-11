# Payment Brand Icons Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the generic system icons used for `PaymentType` in the Payment Methods list and Add/Edit screens with recognizable, hand-drawn brand marks (Visa, Mastercard, Amex, Discover, PayPal, Venmo, Cash App, Affirm, Klarna) on both Android and iOS.

**Architecture:** Add one new rendering unit per platform — `PaymentBrandIcon` (Jetpack Compose) and `PaymentBrandMark` (SwiftUI) — that takes a `PaymentType` and a size and draws a rounded-rect "chip" containing either a code-drawn brand mark (colored shapes/text) or a tinted generic icon. Wire it into the existing 4 call sites (2 per platform), then delete the old generic-icon helper it replaces.

**Tech Stack:** Jetpack Compose (Android, `androidx.compose.material.icons.extended` already a dependency), SwiftUI (iOS). No new dependencies, no data/model changes.

**Reference:** Design spec at `docs/superpowers/specs/2026-07-10-payment-brand-icons-design.md`.

**Prerequisite (already done):** The iOS project previously failed to build due to a corrupted duplicate `.sheet` block and a type mismatch in `AddEditSubscriptionView.swift`, unrelated to this feature. That was fixed and committed (`f6a73d0`) before this plan was written — `xcodebuild` now succeeds. No task below needs to touch that file.

---

### Task 1: Android — Add `PaymentBrandIcon` composable

**Files:**
- Modify: `subly_android/app/src/main/java/net/cynreub/subly/ui/payment/PaymentTypeIcons.kt`

- [ ] **Step 1: Replace the file contents**

Replace the entire contents of `PaymentTypeIcons.kt` with the following. This adds the new `PaymentBrandIcon` composable and its private helpers, while keeping the existing `getPaymentTypeIcon` and `formatPaymentType` functions untouched for now (they're removed in Task 4, once nothing references `getPaymentTypeIcon` anymore).

```kotlin
package net.cynreub.subly.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.cynreub.subly.domain.model.PaymentType

private val VisaBlue = Color(0xFF1A1F71)
private val MastercardRed = Color(0xFFEB001B)
private val MastercardOrange = Color(0xFFF79E1B)
private val AmexBlue = Color(0xFF006FCF)
private val DiscoverOrange = Color(0xFFFF6600)
private val PayPalDarkBlue = Color(0xFF003087)
private val PayPalLightBlue = Color(0xFF009CDE)
private val VenmoBlue = Color(0xFF008CFF)
private val AffirmAccentBlue = Color(0xFF4A4AF4)
private val KlarnaPink = Color(0xFFFFB3C7)

/**
 * Chip-style brand mark for a [PaymentType]: a colored/text mark for recognizable
 * card networks and payment services, or a tinted generic icon for the rest.
 */
@Composable
fun PaymentBrandIcon(
    type: PaymentType,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val cornerRadius = size * 0.22f
    val neutralBackground = MaterialTheme.colorScheme.surface
    val neutralBorder = MaterialTheme.colorScheme.outlineVariant

    when (type) {
        PaymentType.VISA -> BrandChip(modifier, size, cornerRadius, neutralBackground, neutralBorder) {
            BrandText(text = "VISA", color = VisaBlue, size = size, italic = true)
        }

        PaymentType.MASTERCARD -> BrandChip(modifier, size, cornerRadius, neutralBackground, neutralBorder) {
            val circleSize = size * 0.5f
            Box(
                Modifier
                    .offset(x = -circleSize * 0.25f)
                    .size(circleSize)
                    .background(MastercardRed, CircleShape)
            )
            Box(
                Modifier
                    .offset(x = circleSize * 0.25f)
                    .size(circleSize)
                    .background(MastercardOrange, CircleShape)
            )
        }

        PaymentType.AMEX -> BrandChip(modifier, size, cornerRadius, AmexBlue, AmexBlue) {
            BrandText(text = "AMEX", color = Color.White, size = size)
        }

        PaymentType.DISCOVER -> BrandChip(modifier, size, cornerRadius, neutralBackground, neutralBorder) {
            BrandText(
                text = "Discover",
                color = MaterialTheme.colorScheme.onSurface,
                size = size,
                fontSizeRatio = 0.16f
            )
            Box(
                Modifier
                    .offset(x = size * 0.22f, y = size * 0.14f)
                    .size(size * 0.14f)
                    .background(DiscoverOrange, CircleShape)
            )
        }

        PaymentType.PAYPAL -> BrandChip(modifier, size, cornerRadius, neutralBackground, neutralBorder) {
            TwoToneText(
                first = "Pay",
                firstColor = PayPalDarkBlue,
                second = "Pal",
                secondColor = PayPalLightBlue,
                size = size
            )
        }

        PaymentType.VENMO -> BrandChip(modifier, size, cornerRadius, VenmoBlue, VenmoBlue) {
            BrandText(text = "V", color = Color.White, size = size, fontSizeRatio = 0.42f)
        }

        PaymentType.CASHAPP -> BrandChip(modifier, size, cornerRadius, Color.Black, Color.Black) {
            BrandText(text = "$", color = Color.White, size = size, fontSizeRatio = 0.42f)
        }

        PaymentType.AFFIRM -> BrandChip(modifier, size, cornerRadius, Color.Black, Color.Black) {
            BrandText(text = "affirm", color = Color.White, size = size, fontSizeRatio = 0.16f)
            Box(
                Modifier
                    .offset(x = size * 0.27f, y = -size * 0.12f)
                    .size(size * 0.09f)
                    .background(AffirmAccentBlue, CircleShape)
            )
        }

        PaymentType.KLARNA -> BrandChip(modifier, size, cornerRadius, KlarnaPink, KlarnaPink) {
            BrandText(text = "Klarna", color = Color.Black, size = size, fontSizeRatio = 0.17f)
        }

        PaymentType.DEBIT_CARD -> GenericChip(
            modifier, size, cornerRadius, neutralBackground, neutralBorder, Icons.Filled.CreditCard
        )

        PaymentType.BANK_TRANSFER -> GenericChip(
            modifier, size, cornerRadius, neutralBackground, neutralBorder, Icons.Filled.AccountBalance
        )

        PaymentType.CASH -> GenericChip(
            modifier, size, cornerRadius, neutralBackground, neutralBorder, Icons.Filled.Payments
        )

        PaymentType.OTHER -> GenericChip(
            modifier, size, cornerRadius, neutralBackground, neutralBorder, Icons.Filled.HelpOutline
        )
    }
}

@Composable
private fun BrandChip(
    modifier: Modifier,
    size: Dp,
    cornerRadius: Dp,
    background: Color,
    border: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .background(background, RoundedCornerShape(cornerRadius))
            .border(1.dp, border, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun GenericChip(
    modifier: Modifier,
    size: Dp,
    cornerRadius: Dp,
    background: Color,
    border: Color,
    icon: ImageVector
) {
    BrandChip(modifier, size, cornerRadius, background, border) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size * 0.55f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BrandText(
    text: String,
    color: Color,
    size: Dp,
    fontSizeRatio: Float = 0.24f,
    italic: Boolean = false
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        fontSize = (size.value * fontSizeRatio).sp,
        maxLines = 1
    )
}

@Composable
private fun TwoToneText(
    first: String,
    firstColor: Color,
    second: String,
    secondColor: Color,
    size: Dp
) {
    Row {
        BrandText(text = first, color = firstColor, size = size, fontSizeRatio = 0.17f)
        BrandText(text = second, color = secondColor, size = size, fontSizeRatio = 0.17f)
    }
}

/**
 * Get the Material Icon for a given PaymentType
 * Note: Using simplified icons as extended Material Icons require additional dependencies
 */
fun getPaymentTypeIcon(type: PaymentType): ImageVector {
    return when (type) {
        // Credit Card Brands - using Star icon
        PaymentType.VISA,
        PaymentType.MASTERCARD,
        PaymentType.DISCOVER,
        PaymentType.AMEX,
        PaymentType.DEBIT_CARD -> Icons.Default.Star

        // Digital Payment Services - using Info icon
        PaymentType.PAYPAL,
        PaymentType.VENMO,
        PaymentType.CASHAPP,
        PaymentType.AFFIRM,
        PaymentType.KLARNA -> Icons.Default.Info

        // Traditional Methods - using Star icon
        PaymentType.BANK_TRANSFER,
        PaymentType.CASH -> Icons.Default.Star

        // Fallback
        PaymentType.OTHER -> Icons.Default.MoreVert
    }
}

/**
 * Format PaymentType enum as human-readable string
 */
fun formatPaymentType(type: PaymentType): String {
    return when (type) {
        PaymentType.VISA -> "Visa"
        PaymentType.MASTERCARD -> "Mastercard"
        PaymentType.DISCOVER -> "Discover"
        PaymentType.AMEX -> "American Express"
        PaymentType.PAYPAL -> "PayPal"
        PaymentType.VENMO -> "Venmo"
        PaymentType.CASHAPP -> "Cash App"
        PaymentType.AFFIRM -> "Affirm"
        PaymentType.KLARNA -> "Klarna"
        PaymentType.DEBIT_CARD -> "Debit Card"
        PaymentType.BANK_TRANSFER -> "Bank Transfer"
        PaymentType.CASH -> "Cash"
        PaymentType.OTHER -> "Other"
    }
}
```

- [ ] **Step 2: Compile to verify it builds**

Run (from `subly_android/`): `./gradlew :app:compileDebugKotlin -q`
Expected: no output, exit code 0. (`getPaymentTypeIcon` is unused-by-nothing-new at this point, so nothing else needs to change yet.)

- [ ] **Step 3: Commit**

```bash
git add subly_android/app/src/main/java/net/cynreub/subly/ui/payment/PaymentTypeIcons.kt
git commit -m "Add PaymentBrandIcon composable for brand-specific payment icons"
```

---

### Task 2: Android — Use `PaymentBrandIcon` in the Payment Methods list

**Files:**
- Modify: `subly_android/app/src/main/java/net/cynreub/subly/ui/payment/PaymentMethodsScreen.kt:203-209`

- [ ] **Step 1: Replace the list-item icon**

`PaymentMethodsScreen.kt` is in the same package as `PaymentTypeIcons.kt` (`net.cynreub.subly.ui.payment`), so no new import is needed. Find:

```kotlin
            // Payment Type Icon
            Icon(
                imageVector = getPaymentTypeIcon(paymentMethod.type),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
```

Replace with:

```kotlin
            // Payment Type Icon
            PaymentBrandIcon(
                type = paymentMethod.type,
                size = 40.dp
            )
```

- [ ] **Step 2: Compile to verify it builds**

Run (from `subly_android/`): `./gradlew :app:compileDebugKotlin -q`
Expected: no output, exit code 0.

- [ ] **Step 3: Commit**

```bash
git add subly_android/app/src/main/java/net/cynreub/subly/ui/payment/PaymentMethodsScreen.kt
git commit -m "Use PaymentBrandIcon in Payment Methods list"
```

---

### Task 3: Android — Use `PaymentBrandIcon` in Add/Edit Payment Method

**Files:**
- Modify: `subly_android/app/src/main/java/net/cynreub/subly/ui/payment/addedit/AddEditPaymentMethodScreen.kt`

- [ ] **Step 1: Swap the import**

This file is in package `net.cynreub.subly.ui.payment.addedit`, a different package from `PaymentTypeIcons.kt`, so it needs an explicit import. Find:

```kotlin
import net.cynreub.subly.ui.payment.formatPaymentType
import net.cynreub.subly.ui.payment.getPaymentTypeIcon
```

Replace with:

```kotlin
import net.cynreub.subly.ui.payment.PaymentBrandIcon
import net.cynreub.subly.ui.payment.formatPaymentType
```

- [ ] **Step 2: Replace the selected-type header icon**

Find (around line 157):

```kotlin
                                    Icon(
                                        imageVector = getPaymentTypeIcon(uiState.selectedType),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
```

Replace with:

```kotlin
                                    PaymentBrandIcon(
                                        type = uiState.selectedType,
                                        size = 20.dp
                                    )
```

- [ ] **Step 3: Replace the type-picker row icon**

Find (in `PaymentTypeRow`, around line 379):

```kotlin
        Icon(
            imageVector = getPaymentTypeIcon(type),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
```

Replace with:

```kotlin
        PaymentBrandIcon(
            type = type,
            size = 24.dp
        )
```

- [ ] **Step 4: Compile to verify it builds**

Run (from `subly_android/`): `./gradlew :app:compileDebugKotlin -q`
Expected: no output, exit code 0.

- [ ] **Step 5: Commit**

```bash
git add subly_android/app/src/main/java/net/cynreub/subly/ui/payment/addedit/AddEditPaymentMethodScreen.kt
git commit -m "Use PaymentBrandIcon in Add/Edit Payment Method screen"
```

---

### Task 4: Android — Remove the now-unused `getPaymentTypeIcon`

**Files:**
- Modify: `subly_android/app/src/main/java/net/cynreub/subly/ui/payment/PaymentTypeIcons.kt`

- [ ] **Step 1: Confirm nothing references it anymore**

Run: `grep -rn "getPaymentTypeIcon" subly_android/app/src/main --include="*.kt"`
Expected: only the definition itself in `PaymentTypeIcons.kt` (no other call sites — Tasks 2 and 3 already replaced them).

- [ ] **Step 2: Delete the function and its now-unused imports**

In `PaymentTypeIcons.kt`, delete this block:

```kotlin
/**
 * Get the Material Icon for a given PaymentType
 * Note: Using simplified icons as extended Material Icons require additional dependencies
 */
fun getPaymentTypeIcon(type: PaymentType): ImageVector {
    return when (type) {
        // Credit Card Brands - using Star icon
        PaymentType.VISA,
        PaymentType.MASTERCARD,
        PaymentType.DISCOVER,
        PaymentType.AMEX,
        PaymentType.DEBIT_CARD -> Icons.Default.Star

        // Digital Payment Services - using Info icon
        PaymentType.PAYPAL,
        PaymentType.VENMO,
        PaymentType.CASHAPP,
        PaymentType.AFFIRM,
        PaymentType.KLARNA -> Icons.Default.Info

        // Traditional Methods - using Star icon
        PaymentType.BANK_TRANSFER,
        PaymentType.CASH -> Icons.Default.Star

        // Fallback
        PaymentType.OTHER -> Icons.Default.MoreVert
    }
}

```

Then remove these two now-unused imports (the `Icons.Filled.*` imports used by `PaymentBrandIcon`'s `GenericChip` calls are still needed and must stay):

```kotlin
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
```

and

```kotlin
import androidx.compose.material.icons.filled.Star
```

(`Icons.Default.Star`/`Icons.Default.Info`/`Icons.Default.MoreVert` were only used inside the deleted function; `Icons.Filled.CreditCard`, `AccountBalance`, `Payments`, `HelpOutline` used by `PaymentBrandIcon` are separate imports and stay.)

- [ ] **Step 3: Compile to verify it builds**

Run (from `subly_android/`): `./gradlew :app:compileDebugKotlin -q`
Expected: no output, exit code 0.

- [ ] **Step 4: Commit**

```bash
git add subly_android/app/src/main/java/net/cynreub/subly/ui/payment/PaymentTypeIcons.kt
git commit -m "Remove unused getPaymentTypeIcon now that PaymentBrandIcon replaces it"
```

---

### Task 5: iOS — Add `PaymentBrandMark` view

**Files:**
- Create: `subly_ios/Subly/Subly/UI/PaymentMethods/PaymentBrandMark.swift`

- [ ] **Step 1: Create the file**

The project uses Xcode's file-system-synchronized groups (`PBXFileSystemSynchronizedRootGroup`), so a new `.swift` file placed under `Subly/Subly/UI/PaymentMethods/` is automatically included in the `Subly` target — no `project.pbxproj` edit needed.

```swift
import SwiftUI

/// Chip-style brand mark for a `PaymentType`: a colored/text mark for
/// recognizable card networks and payment services, or a tinted system
/// icon for the rest.
struct PaymentBrandMark: View {
    let type: PaymentType
    var size: CGFloat = 44

    private var cornerRadius: CGFloat { size * 0.22 }

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: cornerRadius)
                .fill(backgroundColor)
                .overlay(
                    RoundedRectangle(cornerRadius: cornerRadius)
                        .stroke(borderColor, lineWidth: 1)
                )
            content
        }
        .frame(width: size, height: size)
    }

    @ViewBuilder
    private var content: some View {
        switch type {
        case .visa:
            brandText("VISA", color: Color(red: 0.102, green: 0.122, blue: 0.443), sizeRatio: 0.24, italic: true)

        case .mastercard:
            ZStack {
                Circle()
                    .fill(Color(red: 0.922, green: 0.0, blue: 0.106))
                    .frame(width: size * 0.5, height: size * 0.5)
                    .offset(x: -size * 0.125)
                Circle()
                    .fill(Color(red: 0.969, green: 0.620, blue: 0.106))
                    .frame(width: size * 0.5, height: size * 0.5)
                    .offset(x: size * 0.125)
            }

        case .amex:
            brandText("AMEX", color: .white, sizeRatio: 0.24)

        case .discover:
            ZStack {
                brandText("Discover", color: .primary, sizeRatio: 0.16)
                Circle()
                    .fill(Color(red: 1.0, green: 0.4, blue: 0.0))
                    .frame(width: size * 0.14, height: size * 0.14)
                    .offset(x: size * 0.22, y: size * 0.14)
            }

        case .paypal:
            HStack(spacing: 0) {
                brandText("Pay", color: Color(red: 0.0, green: 0.188, blue: 0.529), sizeRatio: 0.17)
                brandText("Pal", color: Color(red: 0.0, green: 0.612, blue: 0.871), sizeRatio: 0.17)
            }

        case .venmo:
            brandText("V", color: .white, sizeRatio: 0.42)

        case .cashApp:
            brandText("$", color: .white, sizeRatio: 0.42)

        case .affirm:
            ZStack {
                brandText("affirm", color: .white, sizeRatio: 0.16)
                Circle()
                    .fill(Color(red: 0.290, green: 0.290, blue: 0.957))
                    .frame(width: size * 0.09, height: size * 0.09)
                    .offset(x: size * 0.27, y: -size * 0.12)
            }

        case .klarna:
            brandText("Klarna", color: .black, sizeRatio: 0.17)

        case .debitCard, .bankTransfer, .cash, .other:
            Image(systemName: type.sfSymbol)
                .font(.system(size: size * 0.42))
                .foregroundColor(.secondary)
        }
    }

    private var backgroundColor: Color {
        switch type {
        case .amex: return Color(red: 0.0, green: 0.435, blue: 0.812)
        case .venmo: return Color(red: 0.0, green: 0.549, blue: 1.0)
        case .cashApp, .affirm: return .black
        case .klarna: return Color(red: 1.0, green: 0.702, blue: 0.780)
        default: return Color(.secondarySystemBackground)
        }
    }

    private var borderColor: Color {
        switch type {
        case .visa, .mastercard, .discover, .paypal,
             .debitCard, .bankTransfer, .cash, .other:
            return Color(.separator)
        default:
            return .clear
        }
    }

    private func brandText(_ text: String, color: Color, sizeRatio: CGFloat, italic: Bool = false) -> some View {
        Group {
            if italic {
                Text(text).font(.custom("Georgia-BoldItalic", size: size * sizeRatio))
            } else {
                Text(text).font(.custom("Georgia-Bold", size: size * sizeRatio))
            }
        }
        .foregroundColor(color)
        .lineLimit(1)
        .minimumScaleFactor(0.5)
    }
}

#Preview {
    VStack(alignment: .leading, spacing: 12) {
        ForEach(PaymentType.allCases, id: \.self) { type in
            HStack {
                PaymentBrandMark(type: type)
                Text(type.displayName)
            }
        }
    }
    .padding()
}
```

- [ ] **Step 2: Build to verify it compiles**

Run (from `subly_ios/Subly/`):

```bash
xcodebuild -project Subly.xcodeproj -scheme Subly -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' -configuration Debug \
  CODE_SIGNING_ALLOWED=NO build 2>&1 | tail -5
```

Expected: last line is `** BUILD SUCCEEDED **`. (This new file isn't referenced by anything yet, so this just confirms it compiles standalone.)

- [ ] **Step 3: Commit**

```bash
git add subly_ios/Subly/Subly/UI/PaymentMethods/PaymentBrandMark.swift
git commit -m "Add PaymentBrandMark view for brand-specific payment icons"
```

---

### Task 6: iOS — Use `PaymentBrandMark` in the Payment Methods list

**Files:**
- Modify: `subly_ios/Subly/Subly/UI/PaymentMethods/PaymentMethodsView.swift:121-131`

- [ ] **Step 1: Replace the row icon**

Find:

```swift
private struct PaymentMethodRow: View {
    let item: PaymentMethodWithUsage

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: item.paymentMethod.type.sfSymbol)
                .font(.title2)
                .foregroundColor(.accentColor)
                .frame(width: 44, height: 44)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 2) {
```

Replace with:

```swift
private struct PaymentMethodRow: View {
    let item: PaymentMethodWithUsage

    var body: some View {
        HStack(spacing: 12) {
            PaymentBrandMark(type: item.paymentMethod.type, size: 44)

            VStack(alignment: .leading, spacing: 2) {
```

- [ ] **Step 2: Build to verify it compiles**

Run (from `subly_ios/Subly/`):

```bash
xcodebuild -project Subly.xcodeproj -scheme Subly -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' -configuration Debug \
  CODE_SIGNING_ALLOWED=NO build 2>&1 | tail -5
```

Expected: last line is `** BUILD SUCCEEDED **`.

- [ ] **Step 3: Commit**

```bash
git add subly_ios/Subly/Subly/UI/PaymentMethods/PaymentMethodsView.swift
git commit -m "Use PaymentBrandMark in Payment Methods list"
```

---

### Task 7: iOS — Use `PaymentBrandMark` in Add/Edit Payment Method

**Files:**
- Modify: `subly_ios/Subly/Subly/UI/PaymentMethods/AddEdit/AddEditPaymentMethodView.swift`

- [ ] **Step 1: Replace the selected-type header row**

Find (around line 62):

```swift
                Button {
                    viewModel.uiState.showTypePicker = true
                } label: {
                    HStack {
                        Label(state.selectedType.displayName, systemImage: state.selectedType.sfSymbol)
                            .foregroundColor(.primary)
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .buttonStyle(.plain)
```

Replace with:

```swift
                Button {
                    viewModel.uiState.showTypePicker = true
                } label: {
                    HStack {
                        PaymentBrandMark(type: state.selectedType, size: 28)
                        Text(state.selectedType.displayName)
                            .foregroundColor(.primary)
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .buttonStyle(.plain)
```

- [ ] **Step 2: Replace the type-picker sheet row**

Find (around line 118, inside `PaymentTypePickerSheet`):

```swift
            List(PaymentType.allCases, id: \.self) { type in
                Button {
                    onSelect(type)
                    dismiss()
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: type.sfSymbol)
                            .foregroundColor(.accentColor)
                            .frame(width: 24)
                        Text(type.displayName)
                            .foregroundColor(.primary)
                        Spacer()
                        if type == selected {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
```

Replace with:

```swift
            List(PaymentType.allCases, id: \.self) { type in
                Button {
                    onSelect(type)
                    dismiss()
                } label: {
                    HStack(spacing: 12) {
                        PaymentBrandMark(type: type, size: 28)
                        Text(type.displayName)
                            .foregroundColor(.primary)
                        Spacer()
                        if type == selected {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
```

- [ ] **Step 3: Build to verify it compiles**

Run (from `subly_ios/Subly/`):

```bash
xcodebuild -project Subly.xcodeproj -scheme Subly -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' -configuration Debug \
  CODE_SIGNING_ALLOWED=NO build 2>&1 | tail -5
```

Expected: last line is `** BUILD SUCCEEDED **`.

- [ ] **Step 4: Commit**

```bash
git add subly_ios/Subly/Subly/UI/PaymentMethods/AddEdit/AddEditPaymentMethodView.swift
git commit -m "Use PaymentBrandMark in Add/Edit Payment Method screen"
```

---

### Task 8: Manual verification on both platforms

No new automated tests are added — this is a purely presentational change with no ViewModel/business-logic changes (see design spec's Testing section). Verify by running both apps.

**Files:** none (manual QA only)

- [ ] **Step 1: Run the Android app and check the Payment Methods screens**

Launch the app on an emulator or device, navigate to Settings → Payment Methods (or wherever it's linked from), and:
- Add payment methods of type Visa, Mastercard, PayPal, Klarna, and Debit Card.
- Confirm the list row for each shows the correct brand mark (not a generic star/info icon).
- Open Add/Edit for one of them and confirm the selected-type header and the type-picker dialog both show matching brand marks.
- Toggle the device to dark mode and repeat — confirm the neutral chip backgrounds (Visa, Mastercard, Discover, PayPal, generic types) remain legible against `MaterialTheme.colorScheme.surface`/`outlineVariant`.

- [ ] **Step 2: Run the iOS app and check the Payment Methods screens**

Launch the app in the iOS Simulator, navigate to Payment Methods, and repeat the same checks as Step 1 (list row, Add/Edit header, type-picker sheet, light and dark mode via the simulator's appearance toggle).

- [ ] **Step 3: Report results**

If everything matches the design spec's table, the feature is done — no commit needed for this task (it's verification only). If something looks wrong (e.g. text overflowing a chip at a given size), note which type/size/platform and fix it in the relevant file from Tasks 1 or 5 before considering the plan complete.

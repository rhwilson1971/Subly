package net.cynreub.subly.util

import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.PaymentType
import net.cynreub.subly.domain.model.Subscription
import java.time.LocalDate
import java.util.UUID

// ---------------------------------------------------------------------------
// Subscription fixtures
// ---------------------------------------------------------------------------

fun testSubscription(
    id: UUID = UUID.randomUUID(),
    name: String = "Netflix",
    categoryId: UUID = Category.ID_STREAMING,
    amount: Double = 15.99,
    currency: String = "USD",
    frequency: BillingFrequency = BillingFrequency.MONTHLY,
    startDate: LocalDate = LocalDate.of(2026, 1, 1),
    nextBillingDate: LocalDate = LocalDate.of(2026, 2, 1),
    paymentMethodId: UUID? = UUID.randomUUID(),
    notes: String? = null,
    isActive: Boolean = true,
    reminderDaysBefore: Int = 2
): Subscription = Subscription(
    id = id,
    name = name,
    categoryId = categoryId,
    amount = amount,
    currency = currency,
    frequency = frequency,
    startDate = startDate,
    nextBillingDate = nextBillingDate,
    paymentMethodId = paymentMethodId,
    notes = notes,
    isActive = isActive,
    reminderDaysBefore = reminderDaysBefore
)

// ---------------------------------------------------------------------------
// Category fixtures
// ---------------------------------------------------------------------------

fun testCategory(
    id: UUID = Category.ID_STREAMING,
    name: String = "STREAMING",
    displayName: String = "Streaming",
    emoji: String = "📺",
    colorHex: String = "#E91E63"
): Category = Category(
    id = id,
    name = name,
    displayName = displayName,
    emoji = emoji,
    colorHex = colorHex
)

// ---------------------------------------------------------------------------
// PaymentMethod fixtures
// ---------------------------------------------------------------------------

fun testPaymentMethod(
    id: UUID = UUID.randomUUID(),
    nickname: String = "Visa Rewards",
    type: PaymentType = PaymentType.CREDIT_CARD,
    lastFourDigits: String? = "4242",
    icon: Int? = null
): PaymentMethod = PaymentMethod(
    id = id,
    nickname = nickname,
    type = type,
    lastFourDigits = lastFourDigits,
    icon = icon
)

package net.cynreub.subly.domain.model

import java.time.LocalDate
import java.util.UUID

data class Subscription(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val type: SubscriptionType,
    val amount: Double,
    val currency: String,
    val frequency: BillingFrequency,
    val startDate: LocalDate,
    val nextBillingDate: LocalDate,
    val paymentMethodId: UUID?,
    val notes: String? = null,
    val isActive: Boolean = true,
    val reminderDaysBefore: Int = 2
)

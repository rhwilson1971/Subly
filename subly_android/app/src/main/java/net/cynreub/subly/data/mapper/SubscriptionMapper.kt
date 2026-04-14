package net.cynreub.subly.data.mapper

import net.cynreub.subly.data.local.entity.SubscriptionEntity
import net.cynreub.subly.domain.model.Subscription
import java.util.UUID

fun SubscriptionEntity.toDomain(): Subscription = Subscription(
    id = UUID.fromString(id),
    name = name,
    categoryId = UUID.fromString(categoryId),
    amount = amount,
    currency = currency,
    frequency = frequency,
    startDate = startDate,
    nextBillingDate = nextBillingDate,
    paymentMethodId = paymentMethodId?.let { UUID.fromString(it) },
    notes = notes,
    isActive = isActive,
    reminderDaysBefore = reminderDaysBefore
)

fun Subscription.toEntity(): SubscriptionEntity = SubscriptionEntity(
    id = id.toString(),
    name = name,
    categoryId = categoryId.toString(),
    amount = amount,
    currency = currency,
    frequency = frequency,
    startDate = startDate,
    nextBillingDate = nextBillingDate,
    paymentMethodId = paymentMethodId?.toString(),
    notes = notes,
    isActive = isActive,
    reminderDaysBefore = reminderDaysBefore
)

package net.cynreub.subly.data.mapper

import net.cynreub.subly.data.local.entity.SubscriptionEntity
import net.cynreub.subly.domain.model.Subscription
import java.util.UUID

fun SubscriptionEntity.toDomain(): Subscription {
    return Subscription(
        id = UUID.fromString(id),
        name = name,
        type = type,
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
}

fun Subscription.toEntity(): SubscriptionEntity {
    return SubscriptionEntity(
        id = id.toString(),
        name = name,
        type = type,
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
}

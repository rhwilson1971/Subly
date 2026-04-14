package net.cynreub.subly.data.mapper

import net.cynreub.subly.data.local.entity.PaymentMethodEntity
import net.cynreub.subly.domain.model.PaymentMethod
import java.util.UUID

fun PaymentMethodEntity.toDomain(): PaymentMethod {
    return PaymentMethod(
        id = UUID.fromString(id),
        nickname = nickname,
        type = type,
        lastFourDigits = lastFourDigits,
        icon = icon
    )
}

fun PaymentMethod.toEntity(): PaymentMethodEntity {
    return PaymentMethodEntity(
        id = id.toString(),
        nickname = nickname,
        type = type,
        lastFourDigits = lastFourDigits,
        icon = icon
    )
}

package net.cynreub.subly.domain.model

import java.util.UUID

data class PaymentMethod(
    val id: UUID = UUID.randomUUID(),
    val nickname: String,
    val type: PaymentType,
    val lastFourDigits: String? = null,
    val icon: Int? = null
)

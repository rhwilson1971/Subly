package net.cynreub.subly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.cynreub.subly.domain.model.PaymentType

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey
    val id: String,
    val nickname: String,
    val type: PaymentType,
    val lastFourDigits: String?,
    val icon: Int?
)

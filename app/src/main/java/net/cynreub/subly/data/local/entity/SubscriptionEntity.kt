package net.cynreub.subly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.SubscriptionType
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: SubscriptionType,
    val amount: Double,
    val currency: String,
    val frequency: BillingFrequency,
    val startDate: LocalDate,
    val nextBillingDate: LocalDate,
    val paymentMethodId: String?,
    val notes: String?,
    val isActive: Boolean,
    val reminderDaysBefore: Int
)

package net.cynreub.subly.data.local.database

import androidx.room.TypeConverter
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.PaymentType
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromBillingFrequency(value: BillingFrequency): String {
        return value.name
    }

    @TypeConverter
    fun toBillingFrequency(value: String): BillingFrequency {
        return BillingFrequency.valueOf(value)
    }

    @TypeConverter
    fun fromPaymentType(value: PaymentType): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentType(value: String): PaymentType {
        return PaymentType.valueOf(value)
    }
}

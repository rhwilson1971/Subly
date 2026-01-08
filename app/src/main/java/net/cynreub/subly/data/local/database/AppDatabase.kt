package net.cynreub.subly.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.cynreub.subly.data.local.dao.PaymentMethodDao
import net.cynreub.subly.data.local.dao.SubscriptionDao
import net.cynreub.subly.data.local.entity.PaymentMethodEntity
import net.cynreub.subly.data.local.entity.SubscriptionEntity

@Database(
    entities = [
        SubscriptionEntity::class,
        PaymentMethodEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentMethodDao(): PaymentMethodDao

    companion object {
        const val DATABASE_NAME = "subly_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Map old CREDIT_CARD type to new VISA type
                db.execSQL("UPDATE payment_methods SET type = 'VISA' WHERE type = 'CREDIT_CARD'")
            }
        }
    }
}

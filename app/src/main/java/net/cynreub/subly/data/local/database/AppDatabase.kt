package net.cynreub.subly.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.cynreub.subly.data.local.dao.CategoryDao
import net.cynreub.subly.data.local.dao.PaymentMethodDao
import net.cynreub.subly.data.local.dao.SubscriptionDao
import net.cynreub.subly.data.local.entity.CategoryEntity
import net.cynreub.subly.data.local.entity.PaymentMethodEntity
import net.cynreub.subly.data.local.entity.SubscriptionEntity

@Database(
    entities = [
        SubscriptionEntity::class,
        PaymentMethodEntity::class,
        CategoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "subly_database"

        // Fixed UUIDs for default categories — must stay stable forever
        private const val ID_STREAMING  = "00000000-0000-4000-a000-000000000001"
        private const val ID_MAGAZINE   = "00000000-0000-4000-a000-000000000002"
        private const val ID_SERVICE    = "00000000-0000-4000-a000-000000000003"
        private const val ID_MEMBERSHIP = "00000000-0000-4000-a000-000000000004"
        private const val ID_CLUB       = "00000000-0000-4000-a000-000000000005"
        private const val ID_UTILITY    = "00000000-0000-4000-a000-000000000006"
        private const val ID_SOFTWARE   = "00000000-0000-4000-a000-000000000007"
        private const val ID_OTHER      = "00000000-0000-4000-a000-000000000008"

        fun seedDefaultCategories(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT OR IGNORE INTO categories VALUES ('$ID_STREAMING',  'STREAMING',  'Streaming',  '📺', '#E91E63')")
            db.execSQL("INSERT OR IGNORE INTO categories VALUES ('$ID_MAGAZINE',   'MAGAZINE',   'Magazine',   '📰', '#9C27B0')")
            db.execSQL("INSERT OR IGNORE INTO categories VALUES ('$ID_SERVICE',    'SERVICE',    'Service',    '⚙️', '#2196F3')")
            db.execSQL("INSERT OR IGNORE INTO categories VALUES ('$ID_MEMBERSHIP', 'MEMBERSHIP', 'Membership', '🏷️', '#FF9800')")
            db.execSQL("INSERT OR IGNORE INTO categories VALUES ('$ID_CLUB',       'CLUB',       'Club',       '🎯', '#4CAF50')")
            db.execSQL("INSERT OR IGNORE INTO categories VALUES ('$ID_UTILITY',    'UTILITY',    'Utility',    '💡', '#607D8B')")
            db.execSQL("INSERT OR IGNORE INTO categories VALUES ('$ID_SOFTWARE',   'SOFTWARE',   'Software',   '💻', '#00BCD4')")
            db.execSQL("INSERT OR IGNORE INTO categories VALUES ('$ID_OTHER',      'OTHER',      'Other',      '📦', '#795548')")
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE payment_methods SET type = 'VISA' WHERE type = 'CREDIT_CARD'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create categories table
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS categories (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        emoji TEXT NOT NULL,
                        colorHex TEXT NOT NULL
                    )"""
                )

                // 2. Seed default categories
                seedDefaultCategories(db)

                // 3. Recreate subscriptions with categoryId instead of type
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS subscriptions_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        categoryId TEXT NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL,
                        frequency TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        nextBillingDate TEXT NOT NULL,
                        paymentMethodId TEXT,
                        notes TEXT,
                        isActive INTEGER NOT NULL,
                        reminderDaysBefore INTEGER NOT NULL
                    )"""
                )

                // 4. Migrate rows, mapping old type strings to category UUIDs
                db.execSQL(
                    """INSERT INTO subscriptions_new
                        SELECT id, name,
                            CASE type
                                WHEN 'STREAMING'  THEN '$ID_STREAMING'
                                WHEN 'MAGAZINE'   THEN '$ID_MAGAZINE'
                                WHEN 'SERVICE'    THEN '$ID_SERVICE'
                                WHEN 'MEMBERSHIP' THEN '$ID_MEMBERSHIP'
                                WHEN 'CLUB'       THEN '$ID_CLUB'
                                WHEN 'UTILITY'    THEN '$ID_UTILITY'
                                WHEN 'SOFTWARE'   THEN '$ID_SOFTWARE'
                                ELSE '$ID_OTHER'
                            END,
                            amount, currency, frequency, startDate, nextBillingDate,
                            paymentMethodId, notes, isActive, reminderDaysBefore
                        FROM subscriptions"""
                )

                // 5. Swap tables
                db.execSQL("DROP TABLE subscriptions")
                db.execSQL("ALTER TABLE subscriptions_new RENAME TO subscriptions")
            }
        }
    }
}

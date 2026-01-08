package net.cynreub.subly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.cynreub.subly.data.local.entity.SubscriptionEntity
import java.time.LocalDate

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions ORDER BY nextBillingDate ASC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextBillingDate ASC")
    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getSubscriptionById(id: String): Flow<SubscriptionEntity?>

    @Query("SELECT * FROM subscriptions WHERE nextBillingDate BETWEEN :startDate AND :endDate ORDER BY nextBillingDate ASC")
    fun getSubscriptionsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE type = :type ORDER BY nextBillingDate ASC")
    fun getSubscriptionsByType(type: String): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: String)

    @Query("SELECT SUM(amount) FROM subscriptions WHERE isActive = 1 AND frequency = 'MONTHLY'")
    fun getMonthlyTotal(): Flow<Double?>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1 AND nextBillingDate <= :date ORDER BY nextBillingDate ASC")
    fun getUpcomingSubscriptions(date: LocalDate): Flow<List<SubscriptionEntity>>
}

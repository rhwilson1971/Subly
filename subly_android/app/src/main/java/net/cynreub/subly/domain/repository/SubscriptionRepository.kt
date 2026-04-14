package net.cynreub.subly.domain.repository

import kotlinx.coroutines.flow.Flow
import net.cynreub.subly.domain.model.Subscription
import java.time.LocalDate
import java.util.UUID

interface SubscriptionRepository {
    fun getAllSubscriptions(): Flow<List<Subscription>>
    fun getActiveSubscriptions(): Flow<List<Subscription>>
    fun getSubscriptionById(id: UUID): Flow<Subscription?>
    fun getSubscriptionsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Subscription>>
    fun getUpcomingSubscriptions(date: LocalDate): Flow<List<Subscription>>
    fun getMonthlyTotal(): Flow<Double>
    suspend fun insertSubscription(subscription: Subscription)
    suspend fun updateSubscription(subscription: Subscription)
    suspend fun deleteSubscription(subscription: Subscription)
    suspend fun deleteSubscriptionById(id: UUID)
}

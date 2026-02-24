package net.cynreub.subly.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.cynreub.subly.data.local.dao.SubscriptionDao
import net.cynreub.subly.data.mapper.toDomain
import net.cynreub.subly.data.mapper.toEntity
import net.cynreub.subly.data.remote.firestore.SubscriptionSyncService
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.repository.SubscriptionRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val syncService: SubscriptionSyncService
) : SubscriptionRepository {

    override fun getAllSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllSubscriptions()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getActiveSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getActiveSubscriptions()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getSubscriptionById(id: UUID): Flow<Subscription?> {
        return subscriptionDao.getSubscriptionById(id.toString())
            .map { it?.toDomain() }
    }

    override fun getSubscriptionsBetweenDates(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Subscription>> {
        return subscriptionDao.getSubscriptionsBetweenDates(startDate, endDate)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getUpcomingSubscriptions(date: LocalDate): Flow<List<Subscription>> {
        return subscriptionDao.getUpcomingSubscriptions(date)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getMonthlyTotal(): Flow<Double> {
        return subscriptionDao.getMonthlyTotal()
            .map { it ?: 0.0 }
    }

    override suspend fun insertSubscription(subscription: Subscription) {
        subscriptionDao.insertSubscription(subscription.toEntity())
        syncService.upsert(subscription)
    }

    override suspend fun updateSubscription(subscription: Subscription) {
        subscriptionDao.updateSubscription(subscription.toEntity())
        syncService.upsert(subscription)
    }

    override suspend fun deleteSubscription(subscription: Subscription) {
        subscriptionDao.deleteSubscription(subscription.toEntity())
        syncService.delete(subscription.id)
    }

    override suspend fun deleteSubscriptionById(id: UUID) {
        subscriptionDao.deleteSubscriptionById(id.toString())
        syncService.delete(id)
    }
}

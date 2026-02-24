package net.cynreub.subly.data.remote.firestore

import net.cynreub.subly.data.local.dao.PaymentMethodDao
import net.cynreub.subly.data.local.dao.SubscriptionDao
import net.cynreub.subly.data.mapper.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncOrchestrator @Inject constructor(
    private val subscriptionSyncService: SubscriptionSyncService,
    private val paymentMethodSyncService: PaymentMethodSyncService,
    private val subscriptionDao: SubscriptionDao,
    private val paymentMethodDao: PaymentMethodDao
) {
    suspend fun initialPullIfEmpty(uid: String) {
        if (subscriptionDao.getCount() > 0) return // already has local data

        // PaymentMethods first — subscriptions reference them
        paymentMethodSyncService.fetchAll(uid)
            .forEach { paymentMethodDao.insertPaymentMethod(it.toEntity()) }

        subscriptionSyncService.fetchAll(uid)
            .forEach { subscriptionDao.insertSubscription(it.toEntity()) }
    }
}

package net.cynreub.subly.data.remote.firestore

import net.cynreub.subly.data.local.dao.CategoryDao
import net.cynreub.subly.data.local.dao.PaymentMethodDao
import net.cynreub.subly.data.local.dao.SubscriptionDao
import net.cynreub.subly.data.mapper.toEntity
import net.cynreub.subly.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncOrchestrator @Inject constructor(
    private val subscriptionSyncService: SubscriptionSyncService,
    private val paymentMethodSyncService: PaymentMethodSyncService,
    private val categorySyncService: CategorySyncService,
    private val subscriptionDao: SubscriptionDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val categoryDao: CategoryDao
) {
    suspend fun clearAllLocalData() {
        subscriptionDao.deleteAll()
        paymentMethodDao.deleteAll()
        categoryDao.deleteAll()
    }

    /** Clears local cache and pulls fresh data for [uid] from Firestore. */
    suspend fun syncForUser(uid: String) {
        clearAllLocalData()

        // 1. Fetch categories from Firestore; fall back to defaults for new users
        val remoteCategories = categorySyncService.fetchAll(uid)
        val categoriesToSeed = if (remoteCategories.isEmpty()) Category.DEFAULTS else remoteCategories
        categoriesToSeed.forEach { categoryDao.insertCategory(it.toEntity()) }

        if (remoteCategories.isEmpty()) {
            Category.DEFAULTS.forEach { categorySyncService.upsert(it) }
        }

        // 2. Payment methods (subscriptions reference them)
        paymentMethodSyncService.fetchAll(uid)
            .forEach { paymentMethodDao.insertPaymentMethod(it.toEntity()) }

        // 3. Subscriptions
        subscriptionSyncService.fetchAll(uid)
            .forEach { subscriptionDao.insertSubscription(it.toEntity()) }
    }
}

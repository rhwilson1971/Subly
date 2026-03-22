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
    suspend fun initialPullIfEmpty(uid: String) {
        if (subscriptionDao.getCount() > 0) return // already has local data

        // 1. Fetch categories from Firestore; fall back to defaults if none exist yet
        val remoteCategories = categorySyncService.fetchAll(uid)
        val categoriesToSeed = if (remoteCategories.isEmpty()) Category.DEFAULTS else remoteCategories
        categoriesToSeed.forEach { categoryDao.insertCategory(it.toEntity()) }

        // Push defaults to Firestore for new users
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

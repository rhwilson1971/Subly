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
    private val firestoreSyncProvider: FirestoreSyncProvider,
    private val subscriptionDao: SubscriptionDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val categoryDao: CategoryDao
) {
    suspend fun initialPullIfEmpty(uid: String) {
        if (subscriptionDao.getCount() > 0) return // already has local data

        // 1. Fetch categories from Firestore; fall back to defaults if none exist yet
        val remoteCategories = firestoreSyncProvider.fetchAllCategories(uid)
        val categoriesToSeed = if (remoteCategories.isEmpty()) Category.DEFAULTS else remoteCategories
        categoriesToSeed.forEach { categoryDao.insertCategory(it.toEntity()) }

        // Push defaults to Firestore for new users
        if (remoteCategories.isEmpty()) {
            Category.DEFAULTS.forEach { firestoreSyncProvider.upsertCategory(it) }
        }

        // 2. Payment methods (subscriptions reference them)
        firestoreSyncProvider.fetchAllPaymentMethods(uid)
            .forEach { paymentMethodDao.insertPaymentMethod(it.toEntity()) }

        // 3. Subscriptions
        firestoreSyncProvider.fetchAllSubscriptions(uid)
            .forEach { subscriptionDao.insertSubscription(it.toEntity()) }
    }
}

package net.cynreub.subly.data.remote.firestore

import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.sync.SyncProvider
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncProvider @Inject constructor(
    private val subscriptionSyncService: SubscriptionSyncService,
    private val paymentMethodSyncService: PaymentMethodSyncService,
    private val categorySyncService: CategorySyncService
) : SyncProvider {

    override suspend fun upsertSubscription(subscription: Subscription) =
        subscriptionSyncService.upsert(subscription)

    override suspend fun deleteSubscription(id: UUID) =
        subscriptionSyncService.delete(id)

    override suspend fun fetchAllSubscriptions(uid: String): List<Subscription> =
        subscriptionSyncService.fetchAll(uid)

    override suspend fun upsertPaymentMethod(paymentMethod: PaymentMethod) =
        paymentMethodSyncService.upsert(paymentMethod)

    override suspend fun deletePaymentMethod(id: UUID) =
        paymentMethodSyncService.delete(id)

    override suspend fun fetchAllPaymentMethods(uid: String): List<PaymentMethod> =
        paymentMethodSyncService.fetchAll(uid)

    override suspend fun upsertCategory(category: Category) =
        categorySyncService.upsert(category)

    override suspend fun deleteCategory(id: UUID) =
        categorySyncService.delete(id)

    override suspend fun fetchAllCategories(uid: String): List<Category> =
        categorySyncService.fetchAll(uid)
}

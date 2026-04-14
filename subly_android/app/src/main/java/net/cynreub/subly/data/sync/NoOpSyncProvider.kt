package net.cynreub.subly.data.sync

import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.sync.SyncProvider
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpSyncProvider @Inject constructor() : SyncProvider {
    override suspend fun upsertSubscription(subscription: Subscription) = Unit
    override suspend fun deleteSubscription(id: UUID) = Unit
    override suspend fun fetchAllSubscriptions(uid: String): List<Subscription> = emptyList()

    override suspend fun upsertPaymentMethod(paymentMethod: PaymentMethod) = Unit
    override suspend fun deletePaymentMethod(id: UUID) = Unit
    override suspend fun fetchAllPaymentMethods(uid: String): List<PaymentMethod> = emptyList()

    override suspend fun upsertCategory(category: Category) = Unit
    override suspend fun deleteCategory(id: UUID) = Unit
    override suspend fun fetchAllCategories(uid: String): List<Category> = emptyList()
}

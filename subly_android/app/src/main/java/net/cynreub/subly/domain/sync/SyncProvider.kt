package net.cynreub.subly.domain.sync

import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.Subscription
import java.util.UUID

interface SyncProvider {
    suspend fun upsertSubscription(subscription: Subscription)
    suspend fun deleteSubscription(id: UUID)
    suspend fun fetchAllSubscriptions(uid: String): List<Subscription>

    suspend fun upsertPaymentMethod(paymentMethod: PaymentMethod)
    suspend fun deletePaymentMethod(id: UUID)
    suspend fun fetchAllPaymentMethods(uid: String): List<PaymentMethod>

    suspend fun upsertCategory(category: Category)
    suspend fun deleteCategory(id: UUID)
    suspend fun fetchAllCategories(uid: String): List<Category>
}

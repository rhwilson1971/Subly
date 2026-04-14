package net.cynreub.subly.domain.repository

import kotlinx.coroutines.flow.Flow
import net.cynreub.subly.domain.model.PaymentMethod
import java.util.UUID

interface PaymentMethodRepository {
    fun getAllPaymentMethods(): Flow<List<PaymentMethod>>
    fun getPaymentMethodById(id: UUID): Flow<PaymentMethod?>
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethod)
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod)
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod)
    suspend fun deletePaymentMethodById(id: UUID)
    suspend fun getSubscriptionCountForPaymentMethod(paymentMethodId: UUID): Int
}

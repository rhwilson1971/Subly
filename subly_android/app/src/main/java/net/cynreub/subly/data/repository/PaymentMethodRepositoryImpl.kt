package net.cynreub.subly.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.cynreub.subly.data.local.dao.PaymentMethodDao
import net.cynreub.subly.data.mapper.toDomain
import net.cynreub.subly.data.mapper.toEntity
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.domain.sync.SyncProvider
import java.util.UUID
import javax.inject.Inject

class PaymentMethodRepositoryImpl @Inject constructor(
    private val paymentMethodDao: PaymentMethodDao,
    private val syncProvider: SyncProvider
) : PaymentMethodRepository {

    override fun getAllPaymentMethods(): Flow<List<PaymentMethod>> {
        return paymentMethodDao.getAllPaymentMethods()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getPaymentMethodById(id: UUID): Flow<PaymentMethod?> {
        return paymentMethodDao.getPaymentMethodById(id.toString())
            .map { it?.toDomain() }
    }

    override suspend fun insertPaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.insertPaymentMethod(paymentMethod.toEntity())
        syncProvider.upsertPaymentMethod(paymentMethod)
    }

    override suspend fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.updatePaymentMethod(paymentMethod.toEntity())
        syncProvider.upsertPaymentMethod(paymentMethod)
    }

    override suspend fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.deletePaymentMethod(paymentMethod.toEntity())
        syncProvider.deletePaymentMethod(paymentMethod.id)
    }

    override suspend fun deletePaymentMethodById(id: UUID) {
        paymentMethodDao.deletePaymentMethodById(id.toString())
        syncProvider.deletePaymentMethod(id)
    }

    override suspend fun getSubscriptionCountForPaymentMethod(paymentMethodId: UUID): Int {
        return paymentMethodDao.getSubscriptionCountForPaymentMethod(paymentMethodId.toString())
    }
}

package net.cynreub.subly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.cynreub.subly.data.local.entity.PaymentMethodEntity

@Dao
interface PaymentMethodDao {

    @Query("SELECT * FROM payment_methods ORDER BY nickname ASC")
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE id = :id")
    fun getPaymentMethodById(id: String): Flow<PaymentMethodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity)

    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity)

    @Delete
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethodEntity)

    @Query("DELETE FROM payment_methods WHERE id = :id")
    suspend fun deletePaymentMethodById(id: String)

    @Query("SELECT COUNT(*) FROM subscriptions WHERE paymentMethodId = :paymentMethodId")
    suspend fun getSubscriptionCountForPaymentMethod(paymentMethodId: String): Int
}

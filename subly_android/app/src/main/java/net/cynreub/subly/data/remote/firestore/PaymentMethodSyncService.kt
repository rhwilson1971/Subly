package net.cynreub.subly.data.remote.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import net.cynreub.subly.domain.model.PaymentMethod
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentMethodSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun col(uid: String) =
        firestore.collection("users").document(uid).collection("paymentMethods")

    suspend fun upsert(paymentMethod: PaymentMethod) {
        val uid = auth.currentUser?.uid ?: return
        runCatching {
            col(uid).document(paymentMethod.id.toString())
                .set(paymentMethod.toFirestoreMap(), SetOptions.merge())
                .await()
        }
    }

    suspend fun delete(id: UUID) {
        val uid = auth.currentUser?.uid ?: return
        runCatching { col(uid).document(id.toString()).delete().await() }
    }

    suspend fun fetchAll(uid: String): List<PaymentMethod> =
        runCatching {
            col(uid).get().await().documents.mapNotNull { it.toPaymentMethod() }
        }.getOrDefault(emptyList())
}

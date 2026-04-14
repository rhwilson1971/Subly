package net.cynreub.subly.data.remote.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import net.cynreub.subly.domain.model.Subscription
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun col(uid: String) =
        firestore.collection("users").document(uid).collection("subscriptions")

    suspend fun upsert(subscription: Subscription) {
        val uid = auth.currentUser?.uid ?: return
        runCatching {
            col(uid).document(subscription.id.toString())
                .set(subscription.toFirestoreMap(), SetOptions.merge())
                .await()
        }
    }

    suspend fun delete(id: UUID) {
        val uid = auth.currentUser?.uid ?: return
        runCatching { col(uid).document(id.toString()).delete().await() }
    }

    suspend fun fetchAll(uid: String): List<Subscription> =
        runCatching {
            col(uid).get().await().documents.mapNotNull { it.toSubscription() }
        }.getOrDefault(emptyList())
}

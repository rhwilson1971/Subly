package net.cynreub.subly.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import net.cynreub.subly.data.remote.firestore.toDomain
import net.cynreub.subly.data.remote.firestore.toFirestoreMap
import net.cynreub.subly.data.remote.firestore.toProfileDto
import net.cynreub.subly.data.remote.firestore.toUserProfileDto
import net.cynreub.subly.domain.model.User
import net.cynreub.subly.domain.repository.UserProfileRepository
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserProfileRepository {

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)

    override suspend fun saveProfile(user: User): Result<Unit> = runCatching {
        userDoc(user.uid).set(user.toProfileDto().toFirestoreMap()).await()
    }

    override suspend fun getProfile(uid: String): Result<User?> = runCatching {
        val snapshot = userDoc(uid).get().await()
        if (!snapshot.exists()) return@runCatching null
        val dto = snapshot.toUserProfileDto() ?: return@runCatching null
        val firebaseUser = auth.currentUser
        val base = User(
            uid = uid,
            email = firebaseUser?.email,
            displayName = firebaseUser?.displayName
        )
        dto.toDomain(base)
    }

    override suspend fun markOnboardingCompleted(uid: String): Result<Unit> = runCatching {
        userDoc(uid).update("onboardingCompleted", true).await()
    }
}

package net.cynreub.subly.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import net.cynreub.subly.data.remote.firestore.FirestoreSyncOrchestrator
import net.cynreub.subly.domain.model.User
import net.cynreub.subly.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val syncOrchestrator: FirestoreSyncOrchestrator
) : AuthRepository {

    override val currentUser: User?
        get() = auth.currentUser?.let { firebaseUser ->
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName
            )
        }

    override val isLoggedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user!!
            try {
                syncOrchestrator.initialPullIfEmpty(user.uid)
            } catch (e: Exception) {
                // Log error but allow login to proceed
                e.printStackTrace()
            }
            Result.success(User(uid = user.uid, email = user.email, displayName = user.displayName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            try {
                syncOrchestrator.initialPullIfEmpty(user.uid)
            } catch (e: Exception) {
                // Log error but allow login to proceed
                e.printStackTrace()
            }
            Result.success(User(uid = user.uid, email = user.email, displayName = user.displayName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user!!
            try {
                syncOrchestrator.initialPullIfEmpty(user.uid)
            } catch (e: Exception) {
                // Log error but allow login to proceed
                e.printStackTrace()
            }
            Result.success(User(uid = user.uid, email = user.email, displayName = user.displayName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun syncOnLogin(uid: String) {
        try {
            syncOrchestrator.initialPullIfEmpty(uid)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

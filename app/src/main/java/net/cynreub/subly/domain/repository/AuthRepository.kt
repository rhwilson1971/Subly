package net.cynreub.subly.domain.repository

import kotlinx.coroutines.flow.Flow
import net.cynreub.subly.domain.model.User

interface AuthRepository {
    val currentUser: User?
    val isLoggedIn: Boolean
    val authStateFlow: Flow<User?>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun registerWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
}

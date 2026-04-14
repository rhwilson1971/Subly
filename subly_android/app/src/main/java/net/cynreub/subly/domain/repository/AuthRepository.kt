package net.cynreub.subly.domain.repository

import net.cynreub.subly.domain.model.User

interface AuthRepository {
    val currentUser: User?
    val isLoggedIn: Boolean
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun registerWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
    suspend fun syncOnLogin(uid: String)
}

package net.cynreub.subly.domain.repository

import net.cynreub.subly.domain.model.User

interface UserProfileRepository {
    suspend fun saveProfile(user: User): Result<Unit>
    suspend fun getProfile(uid: String): Result<User?>
    suspend fun markOnboardingCompleted(uid: String): Result<Unit>
}

package net.cynreub.subly.data.remote.firestore

import com.google.firebase.firestore.DocumentSnapshot
import net.cynreub.subly.domain.model.User

data class UserProfileDto(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val dateOfBirth: String? = null,
    val phoneNumber: String? = null,
    val profilePictureUrl: String? = null,
    val onboardingCompleted: Boolean = false
)

fun UserProfileDto.toFirestoreMap(): Map<String, Any?> = mapOf(
    "uid" to uid,
    "fullName" to fullName,
    "email" to email,
    "dateOfBirth" to dateOfBirth,
    "phoneNumber" to phoneNumber,
    "profilePictureUrl" to profilePictureUrl,
    "onboardingCompleted" to onboardingCompleted
)

fun DocumentSnapshot.toUserProfileDto(): UserProfileDto? = runCatching {
    UserProfileDto(
        uid = getString("uid") ?: id,
        fullName = getString("fullName") ?: "",
        email = getString("email") ?: "",
        dateOfBirth = getString("dateOfBirth"),
        phoneNumber = getString("phoneNumber"),
        profilePictureUrl = getString("profilePictureUrl"),
        onboardingCompleted = getBoolean("onboardingCompleted") ?: false
    )
}.getOrNull()

fun UserProfileDto.toDomain(existingUser: User): User = existingUser.copy(
    fullName = fullName.ifBlank { null },
    dateOfBirth = dateOfBirth,
    phoneNumber = phoneNumber,
    profilePictureUrl = profilePictureUrl,
    onboardingCompleted = onboardingCompleted
)

fun User.toProfileDto(): UserProfileDto = UserProfileDto(
    uid = uid,
    fullName = fullName ?: "",
    email = email ?: "",
    dateOfBirth = dateOfBirth,
    phoneNumber = phoneNumber,
    profilePictureUrl = profilePictureUrl,
    onboardingCompleted = onboardingCompleted
)

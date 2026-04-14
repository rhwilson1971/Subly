package net.cynreub.subly.domain.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val fullName: String? = null,
    val dateOfBirth: String? = null,       // ISO-8601 (yyyy-MM-dd)
    val phoneNumber: String? = null,
    val profilePictureUrl: String? = null,
    val onboardingCompleted: Boolean = false
)

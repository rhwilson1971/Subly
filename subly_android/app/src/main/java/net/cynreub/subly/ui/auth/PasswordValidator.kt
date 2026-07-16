package net.cynreub.subly.ui.auth

/**
 * Minimum bar for a "strong" password: 8+ characters, at least one
 * uppercase letter, one lowercase letter, and one digit.
 */
object PasswordValidator {
    private const val MIN_LENGTH = 8

    fun errors(password: String): List<String> {
        val errors = mutableListOf<String>()
        if (password.length < MIN_LENGTH) errors += "At least $MIN_LENGTH characters"
        if (password.none { it.isUpperCase() }) errors += "At least one uppercase letter"
        if (password.none { it.isLowerCase() }) errors += "At least one lowercase letter"
        if (password.none { it.isDigit() }) errors += "At least one number"
        return errors
    }

    fun isStrong(password: String): Boolean = errors(password).isEmpty()
}

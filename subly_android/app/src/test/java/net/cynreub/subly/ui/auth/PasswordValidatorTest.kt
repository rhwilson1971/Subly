package net.cynreub.subly.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun `blank password reports all requirements unmet`() {
        val errors = PasswordValidator.errors("")
        assertEquals(4, errors.size)
        assertFalse(PasswordValidator.isStrong(""))
    }

    @Test
    fun `password missing uppercase reports that requirement only`() {
        val errors = PasswordValidator.errors("password1")
        assertEquals(listOf("At least one uppercase letter"), errors)
    }

    @Test
    fun `password missing digit reports that requirement only`() {
        val errors = PasswordValidator.errors("Password")
        assertEquals(listOf("At least one number"), errors)
    }

    @Test
    fun `password under 8 characters reports length requirement`() {
        val errors = PasswordValidator.errors("Pas1")
        assertTrue(errors.contains("At least 8 characters"))
    }

    @Test
    fun `strong password reports no errors`() {
        assertTrue(PasswordValidator.isStrong("Password1"))
        assertEquals(emptyList<String>(), PasswordValidator.errors("Password1"))
    }
}

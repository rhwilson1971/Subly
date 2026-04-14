package net.cynreub.subly.ui.auth

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.model.User
import net.cynreub.subly.domain.repository.AuthRepository
import net.cynreub.subly.util.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val authRepository: AuthRepository = mockk()
    private val testUser = User(uid = "user-123", email = "test@example.com", displayName = null)

    private fun createViewModel() = AuthViewModel(authRepository)

    @Before
    fun setUp() {
        // No default stubs; each test sets up its own
    }

    // ── Initial state ──────────────────────────────────────────────────────────

    @Test
    fun `initial state is not loading, not authenticated, no error`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
        assertNull(state.error)
    }

    // ── Sign in ────────────────────────────────────────────────────────────────

    @Test
    fun `signIn success sets isAuthenticated true`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(testUser)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.signIn("test@example.com", "password")

            // With UnconfinedTestDispatcher the coroutine runs eagerly; the single emitted
            // state is the final authenticated state.
            val done = awaitItem()
            assertFalse(done.isLoading)
            assertTrue(done.isAuthenticated)
            assertNull(done.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signIn failure sets error message`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns
            Result.failure(RuntimeException("Invalid credentials"))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.signIn("test@example.com", "wrongpass")

            val done = awaitItem()
            assertFalse(done.isLoading)
            assertFalse(done.isAuthenticated)
            assertEquals("Invalid credentials", done.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signIn clears previous error before attempting`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns
            Result.failure(RuntimeException("First error"))
        val viewModel = createViewModel()

        viewModel.signIn("test@example.com", "pass")

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertNotNull(state.error)

            coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(testUser)
            viewModel.signIn("test@example.com", "pass")

            val loadingAgain = awaitItem()
            assertNull(loadingAgain.error) // error cleared on new attempt
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Register ───────────────────────────────────────────────────────────────

    @Test
    fun `register success sets isAuthenticated true`() = runTest {
        coEvery { authRepository.registerWithEmail(any(), any()) } returns Result.success(testUser)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.register("new@example.com", "newpass")

            val done = awaitItem()
            assertFalse(done.isLoading)
            assertTrue(done.isAuthenticated)
            assertNull(done.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register failure sets error message`() = runTest {
        coEvery { authRepository.registerWithEmail(any(), any()) } returns
            Result.failure(RuntimeException("Email already in use"))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.register("existing@example.com", "pass")

            val done = awaitItem()
            assertFalse(done.isLoading)
            assertFalse(done.isAuthenticated)
            assertEquals("Email already in use", done.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Sign in with Google ────────────────────────────────────────────────────

    @Test
    fun `signInWithGoogle success sets isAuthenticated true`() = runTest {
        coEvery { authRepository.signInWithGoogle(any()) } returns Result.success(testUser)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.signInWithGoogle("google-id-token")

            val done = awaitItem()
            assertFalse(done.isLoading)
            assertTrue(done.isAuthenticated)
            assertNull(done.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signInWithGoogle failure sets error message`() = runTest {
        coEvery { authRepository.signInWithGoogle(any()) } returns
            Result.failure(RuntimeException("Google sign-in failed"))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.signInWithGoogle("invalid-token")

            val done = awaitItem()
            assertFalse(done.isLoading)
            assertFalse(done.isAuthenticated)
            assertEquals("Google sign-in failed", done.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Clear error ────────────────────────────────────────────────────────────

    @Test
    fun `clearError removes error from state`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns
            Result.failure(RuntimeException("Some error"))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.signIn("test@example.com", "pass")
            // UnconfinedTestDispatcher runs coroutine eagerly — only final (error) state emits.
            val errorState = awaitItem()
            assertNotNull(errorState.error)

            viewModel.clearError()
            val cleared = awaitItem()
            assertNull(cleared.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

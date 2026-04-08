package net.cynreub.subly.ui.settings

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.data.preferences.NotificationPreferences
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.ThemePreference
import net.cynreub.subly.notification.NotificationScheduler
import net.cynreub.subly.notification.PermissionHandler
import net.cynreub.subly.util.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val preferencesManager: PreferencesManager = mockk(relaxed = true)
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val permissionHandler: PermissionHandler = mockk()

    private val defaultPreferences = NotificationPreferences(
        notificationsEnabled = true,
        morningNotificationTime = "09:00",
        eveningNotificationTime = "18:00",
        defaultReminderDays = 2
    )

    @Before
    fun setUp() {
        every { preferencesManager.notificationPreferences } returns flowOf(defaultPreferences)
        every { preferencesManager.themePreference } returns flowOf(ThemePreference.SYSTEM)
        every { permissionHandler.isNotificationPermissionGranted() } returns true
    }

    private fun createViewModel() = SettingsViewModel(
        preferencesManager,
        notificationScheduler,
        permissionHandler
    )

    // ── Load settings ──────────────────────────────────────────────────────────

    @Test
    fun `loads preferences on init`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.notificationsEnabled)
            assertEquals("09:00", state.morningNotificationTime)
            assertEquals("18:00", state.eveningNotificationTime)
            assertEquals(2, state.defaultReminderDays)
            assertTrue(state.hasNotificationPermission)
            assertFalse(state.isLoading)
            assertEquals(ThemePreference.SYSTEM, state.selectedTheme)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state set when preferences load throws`() = runTest {
        every { preferencesManager.notificationPreferences } returns kotlinx.coroutines.flow.flow {
            throw RuntimeException("Prefs error")
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Prefs error"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Notifications toggle ───────────────────────────────────────────────────

    @Test
    fun `onNotificationsEnabledChange true with permission saves preference and schedules`() = runTest {
        every { permissionHandler.isNotificationPermissionGranted() } returns true
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loaded state

            viewModel.onNotificationsEnabledChange(true)

            coVerify { preferencesManager.updateNotificationsEnabled(true) }
            verify { notificationScheduler.scheduleDailyReminders(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNotificationsEnabledChange true without permission shows permission dialog`() = runTest {
        every { permissionHandler.isNotificationPermissionGranted() } returns false
        every { preferencesManager.notificationPreferences } returns flowOf(
            defaultPreferences.copy(notificationsEnabled = false)
        )
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loaded state

            viewModel.onNotificationsEnabledChange(true)

            val state = awaitItem()
            assertTrue(state.showPermissionDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNotificationsEnabledChange false cancels reminders`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onNotificationsEnabledChange(false)

            coVerify { preferencesManager.updateNotificationsEnabled(false) }
            verify { notificationScheduler.cancelReminders() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Permission handling ────────────────────────────────────────────────────

    @Test
    fun `onPermissionGranted enables notifications and schedules reminders`() = runTest {
        // Start with permission denied so onPermissionGranted produces a visible state change.
        every { permissionHandler.isNotificationPermissionGranted() } returns false
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loaded = awaitItem()
            assertFalse(loaded.hasNotificationPermission)

            viewModel.onPermissionGranted()

            val state = awaitItem()
            assertTrue(state.hasNotificationPermission)
            assertFalse(state.showPermissionDialog)

            coVerify { preferencesManager.updateNotificationsEnabled(true) }
            verify { notificationScheduler.scheduleDailyReminders(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onPermissionDenied sets hasNotificationPermission false and disables notifications`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onPermissionDenied()

            val state = awaitItem()
            assertFalse(state.hasNotificationPermission)
            assertFalse(state.showPermissionDialog)
            assertFalse(state.notificationsEnabled)

            coVerify { preferencesManager.updateNotificationsEnabled(false) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showPermissionDialog and dismissPermissionDialog toggle flag`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.showPermissionDialog()
            assertTrue(awaitItem().showPermissionDialog)

            viewModel.dismissPermissionDialog()
            assertFalse(awaitItem().showPermissionDialog)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Time changes ───────────────────────────────────────────────────────────

    @Test
    fun `onMorningTimeChange saves time and reschedules when notifications enabled`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onMorningTimeChange("08:00")

            coVerify { preferencesManager.updateMorningTime("08:00") }
            verify { notificationScheduler.scheduleDailyReminders(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEveningTimeChange saves time and reschedules when notifications enabled`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEveningTimeChange("20:00")

            coVerify { preferencesManager.updateEveningTime("20:00") }
            verify { notificationScheduler.scheduleDailyReminders(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onMorningTimeChange does not reschedule when notifications disabled`() = runTest {
        every { preferencesManager.notificationPreferences } returns flowOf(
            defaultPreferences.copy(notificationsEnabled = false)
        )
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onMorningTimeChange("08:00")

            coVerify { preferencesManager.updateMorningTime("08:00") }
            verify(exactly = 0) { notificationScheduler.scheduleDailyReminders(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Time pickers ───────────────────────────────────────────────────────────

    @Test
    fun `showMorningTimePicker and dismissMorningTimePicker toggle flag`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.showMorningTimePicker()
            assertTrue(awaitItem().showMorningTimePicker)

            viewModel.dismissMorningTimePicker()
            assertFalse(awaitItem().showMorningTimePicker)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showEveningTimePicker and dismissEveningTimePicker toggle flag`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.showEveningTimePicker()
            assertTrue(awaitItem().showEveningTimePicker)

            viewModel.dismissEveningTimePicker()
            assertFalse(awaitItem().showEveningTimePicker)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Default reminder days ──────────────────────────────────────────────────

    @Test
    fun `onDefaultReminderDaysChange saves value`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onDefaultReminderDaysChange(5)

            coVerify { preferencesManager.updateDefaultReminderDays(5) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Theme change ───────────────────────────────────────────────────────────

    @Test
    fun `onThemeChange saves theme preference`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onThemeChange(ThemePreference.DARK)

            coVerify { preferencesManager.updateTheme(ThemePreference.DARK) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Refresh permission status ──────────────────────────────────────────────

    @Test
    fun `refreshPermissionStatus updates permission from handler`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            every { permissionHandler.isNotificationPermissionGranted() } returns false
            viewModel.refreshPermissionStatus()

            val state = awaitItem()
            assertFalse(state.hasNotificationPermission)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

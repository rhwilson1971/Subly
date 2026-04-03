package net.cynreub.subly.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit [TestWatcher] rule that sets [Dispatchers.Main] to a [TestDispatcher] for the
 * duration of each test, then resets it afterward. Use as a `@get:Rule` in any test class
 * that needs coroutine or ViewModel testing.
 *
 * Defaults to [UnconfinedTestDispatcher] so coroutines execute eagerly without needing
 * explicit `advanceUntilIdle()` calls in most tests.
 */
class MainCoroutineRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

package net.cynreub.subly.domain.usecase

import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class UpdateNextBillingDateUseCaseTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val repository: SubscriptionRepository = mockk()
    private lateinit var useCase: UpdateNextBillingDateUseCase

    private val baseDate: LocalDate = LocalDate.of(2026, 1, 15)

    @Before
    fun setUp() {
        useCase = UpdateNextBillingDateUseCase(repository)
        coJustRun { repository.updateSubscription(any()) }
    }

    @Test
    fun `WEEKLY advances nextBillingDate by 1 week`() = runTest {
        val sub = testSubscription(frequency = BillingFrequency.WEEKLY, nextBillingDate = baseDate)
        val updatedSlot = slot<net.cynreub.subly.domain.model.Subscription>()
        coJustRun { repository.updateSubscription(capture(updatedSlot)) }

        useCase(sub)

        assertEquals(baseDate.plusWeeks(1), updatedSlot.captured.nextBillingDate)
    }

    @Test
    fun `MONTHLY advances nextBillingDate by 1 month`() = runTest {
        val sub = testSubscription(frequency = BillingFrequency.MONTHLY, nextBillingDate = baseDate)
        val updatedSlot = slot<net.cynreub.subly.domain.model.Subscription>()
        coJustRun { repository.updateSubscription(capture(updatedSlot)) }

        useCase(sub)

        assertEquals(baseDate.plusMonths(1), updatedSlot.captured.nextBillingDate)
    }

    @Test
    fun `QUARTERLY advances nextBillingDate by 3 months`() = runTest {
        val sub = testSubscription(frequency = BillingFrequency.QUARTERLY, nextBillingDate = baseDate)
        val updatedSlot = slot<net.cynreub.subly.domain.model.Subscription>()
        coJustRun { repository.updateSubscription(capture(updatedSlot)) }

        useCase(sub)

        assertEquals(baseDate.plusMonths(3), updatedSlot.captured.nextBillingDate)
    }

    @Test
    fun `SEMI_ANNUAL advances nextBillingDate by 6 months`() = runTest {
        val sub = testSubscription(frequency = BillingFrequency.SEMI_ANNUAL, nextBillingDate = baseDate)
        val updatedSlot = slot<net.cynreub.subly.domain.model.Subscription>()
        coJustRun { repository.updateSubscription(capture(updatedSlot)) }

        useCase(sub)

        assertEquals(baseDate.plusMonths(6), updatedSlot.captured.nextBillingDate)
    }

    @Test
    fun `ANNUAL advances nextBillingDate by 1 year`() = runTest {
        val sub = testSubscription(frequency = BillingFrequency.ANNUAL, nextBillingDate = baseDate)
        val updatedSlot = slot<net.cynreub.subly.domain.model.Subscription>()
        coJustRun { repository.updateSubscription(capture(updatedSlot)) }

        useCase(sub)

        assertEquals(baseDate.plusYears(1), updatedSlot.captured.nextBillingDate)
    }

    @Test
    fun `CUSTOM advances nextBillingDate by 1 month`() = runTest {
        val sub = testSubscription(frequency = BillingFrequency.CUSTOM, nextBillingDate = baseDate)
        val updatedSlot = slot<net.cynreub.subly.domain.model.Subscription>()
        coJustRun { repository.updateSubscription(capture(updatedSlot)) }

        useCase(sub)

        assertEquals(baseDate.plusMonths(1), updatedSlot.captured.nextBillingDate)
    }

    @Test
    fun `calls repository updateSubscription exactly once`() = runTest {
        val sub = testSubscription(frequency = BillingFrequency.MONTHLY, nextBillingDate = baseDate)

        useCase(sub)

        coVerify(exactly = 1) { repository.updateSubscription(any()) }
    }

    @Test
    fun `only nextBillingDate changes — all other fields are preserved`() = runTest {
        val sub = testSubscription(
            name = "Netflix",
            amount = 15.99,
            frequency = BillingFrequency.MONTHLY,
            nextBillingDate = baseDate
        )
        val updatedSlot = slot<net.cynreub.subly.domain.model.Subscription>()
        coJustRun { repository.updateSubscription(capture(updatedSlot)) }

        useCase(sub)

        val updated = updatedSlot.captured
        assertEquals(sub.id, updated.id)
        assertEquals(sub.name, updated.name)
        assertEquals(sub.amount, updated.amount, 0.001)
        assertEquals(sub.frequency, updated.frequency)
        assertEquals(sub.isActive, updated.isActive)
    }
}

package net.cynreub.subly.domain.usecase

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testCategory
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetCategorySpendUseCaseTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val subscriptionRepository: SubscriptionRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private lateinit var useCase: GetCategorySpendUseCase

    @Before
    fun setUp() {
        useCase = GetCategorySpendUseCase(subscriptionRepository, categoryRepository)
    }

    @Test
    fun `returns empty list when there are no active subscriptions`() = runTest {
        every { subscriptionRepository.getActiveSubscriptions() } returns flowOf(emptyList())
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(emptyList<Any>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `groups subscriptions by category and sums monthly amounts`() = runTest {
        val streamingCategory = testCategory(id = Category.ID_STREAMING, displayName = "Streaming")
        val softwareCategory = testCategory(id = Category.ID_SOFTWARE, displayName = "Software")

        val subscriptions = listOf(
            testSubscription(categoryId = Category.ID_STREAMING, amount = 10.0, frequency = BillingFrequency.MONTHLY),
            testSubscription(categoryId = Category.ID_STREAMING, amount = 5.0, frequency = BillingFrequency.MONTHLY),
            testSubscription(categoryId = Category.ID_SOFTWARE, amount = 20.0, frequency = BillingFrequency.MONTHLY)
        )

        every { subscriptionRepository.getActiveSubscriptions() } returns flowOf(subscriptions)
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(streamingCategory, softwareCategory))

        useCase().test {
            val result = awaitItem()
            val streamingSpend = result.first { it.category.id == Category.ID_STREAMING }
            val softwareSpend = result.first { it.category.id == Category.ID_SOFTWARE }

            assertEquals(15.0, streamingSpend.monthlyAmount, 0.001)
            assertEquals(20.0, softwareSpend.monthlyAmount, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `percentage sums to approximately 1 across all categories`() = runTest {
        val streamingCategory = testCategory(id = Category.ID_STREAMING)
        val softwareCategory = testCategory(id = Category.ID_SOFTWARE)

        val subscriptions = listOf(
            testSubscription(categoryId = Category.ID_STREAMING, amount = 30.0, frequency = BillingFrequency.MONTHLY),
            testSubscription(categoryId = Category.ID_SOFTWARE, amount = 70.0, frequency = BillingFrequency.MONTHLY)
        )

        every { subscriptionRepository.getActiveSubscriptions() } returns flowOf(subscriptions)
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(streamingCategory, softwareCategory))

        useCase().test {
            val result = awaitItem()
            val totalPercentage = result.sumOf { it.percentage.toDouble() }
            assertEquals(1.0, totalPercentage, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `result is sorted descending by monthly amount`() = runTest {
        val categories = listOf(
            testCategory(id = Category.ID_STREAMING, displayName = "Streaming"),
            testCategory(id = Category.ID_SOFTWARE, displayName = "Software"),
            testCategory(id = Category.ID_OTHER, displayName = "Other")
        )
        val subscriptions = listOf(
            testSubscription(categoryId = Category.ID_STREAMING, amount = 10.0, frequency = BillingFrequency.MONTHLY),
            testSubscription(categoryId = Category.ID_SOFTWARE, amount = 50.0, frequency = BillingFrequency.MONTHLY),
            testSubscription(categoryId = Category.ID_OTHER, amount = 25.0, frequency = BillingFrequency.MONTHLY)
        )

        every { subscriptionRepository.getActiveSubscriptions() } returns flowOf(subscriptions)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)

        useCase().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertTrue(result[0].monthlyAmount >= result[1].monthlyAmount)
            assertTrue(result[1].monthlyAmount >= result[2].monthlyAmount)
            awaitComplete()
        }
    }

    @Test
    fun `subscriptions with unknown category are excluded from results`() = runTest {
        val knownCategory = testCategory(id = Category.ID_STREAMING)
        val unknownCategoryId = Category.ID_SOFTWARE // not included in getAllCategories

        val subscriptions = listOf(
            testSubscription(categoryId = Category.ID_STREAMING, amount = 10.0),
            testSubscription(categoryId = unknownCategoryId, amount = 50.0)
        )

        every { subscriptionRepository.getActiveSubscriptions() } returns flowOf(subscriptions)
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(knownCategory))

        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(Category.ID_STREAMING, result.first().category.id)
            awaitComplete()
        }
    }

    @Test
    fun `percentage calculation does not divide by zero when total is zero`() = runTest {
        // coerceAtLeast(1.0) guard prevents NaN/Infinity when all amounts are 0
        val category = testCategory(id = Category.ID_STREAMING)
        val subscriptions = listOf(
            testSubscription(categoryId = Category.ID_STREAMING, amount = 0.0, frequency = BillingFrequency.MONTHLY)
        )

        every { subscriptionRepository.getActiveSubscriptions() } returns flowOf(subscriptions)
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(category))

        useCase().test {
            val result = awaitItem()
            assertTrue(result.first().percentage.isFinite())
            awaitComplete()
        }
    }
}

package net.cynreub.subly.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import javax.inject.Inject

data class CategorySpend(
    val category: Category,
    val monthlyAmount: Double,
    val percentage: Float   // 0..1 share of total monthly spend
)

class GetCategorySpendUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<CategorySpend>> =
        combine(
            subscriptionRepository.getActiveSubscriptions(),
            categoryRepository.getAllCategories()
        ) { subscriptions, categories ->
            val categoryMap = categories.associateBy { it.id }

            val breakdown = subscriptions
                .groupBy { it.categoryId }
                .mapValues { (_, subs) -> subs.sumOf { it.toMonthlyAmount() } }

            val total = breakdown.values.sum().coerceAtLeast(1.0)

            breakdown.entries
                .mapNotNull { (categoryId, amount) ->
                    val category = categoryMap[categoryId] ?: return@mapNotNull null
                    CategorySpend(
                        category = category,
                        monthlyAmount = amount,
                        percentage = (amount / total).toFloat()
                    )
                }
                .sortedByDescending { it.monthlyAmount }
        }

    private fun Subscription.toMonthlyAmount(): Double =
        when (frequency) {
            BillingFrequency.WEEKLY -> amount * 4
            BillingFrequency.MONTHLY -> amount
            BillingFrequency.QUARTERLY -> amount / 3
            BillingFrequency.SEMI_ANNUAL -> amount / 6
            BillingFrequency.ANNUAL -> amount / 12
            BillingFrequency.CUSTOM -> amount
        }
}

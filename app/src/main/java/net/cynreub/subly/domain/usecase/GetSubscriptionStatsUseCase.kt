package net.cynreub.subly.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.repository.SubscriptionRepository
import java.util.UUID
import javax.inject.Inject

data class SubscriptionStats(
    val totalMonthly: Double,
    val totalYearly: Double,
    val activeCount: Int,
    val categoryBreakdown: Map<UUID, Double> // categoryId → monthly spend equivalent
)

class GetSubscriptionStatsUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(): Flow<SubscriptionStats> {
        return subscriptionRepository.getActiveSubscriptions()
            .map { subscriptions ->
                val totalMonthly = subscriptions.sumOf { it.toMonthlyAmount() }

                val categoryBreakdown = subscriptions
                    .groupBy { it.categoryId }
                    .mapValues { (_, subs) -> subs.sumOf { it.toMonthlyAmount() } }

                SubscriptionStats(
                    totalMonthly = totalMonthly,
                    totalYearly = totalMonthly * 12,
                    activeCount = subscriptions.size,
                    categoryBreakdown = categoryBreakdown
                )
            }
    }

    private fun net.cynreub.subly.domain.model.Subscription.toMonthlyAmount(): Double =
        when (frequency) {
            BillingFrequency.WEEKLY -> amount * 4
            BillingFrequency.MONTHLY -> amount
            BillingFrequency.QUARTERLY -> amount / 3
            BillingFrequency.SEMI_ANNUAL -> amount / 6
            BillingFrequency.ANNUAL -> amount / 12
            BillingFrequency.CUSTOM -> amount
        }
}

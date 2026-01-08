package net.cynreub.subly.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.SubscriptionType
import net.cynreub.subly.domain.repository.SubscriptionRepository
import javax.inject.Inject

data class SubscriptionStats(
    val totalMonthly: Double,
    val totalYearly: Double,
    val activeCount: Int,
    val categoryBreakdown: Map<SubscriptionType, Double>
)

class GetSubscriptionStatsUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(): Flow<SubscriptionStats> {
        return subscriptionRepository.getActiveSubscriptions()
            .map { subscriptions ->
                val totalMonthly = subscriptions.sumOf { subscription ->
                    when (subscription.frequency) {
                        BillingFrequency.WEEKLY -> subscription.amount * 4
                        BillingFrequency.MONTHLY -> subscription.amount
                        BillingFrequency.QUARTERLY -> subscription.amount / 3
                        BillingFrequency.SEMI_ANNUAL -> subscription.amount / 6
                        BillingFrequency.ANNUAL -> subscription.amount / 12
                        BillingFrequency.CUSTOM -> subscription.amount
                    }
                }

                val categoryBreakdown = subscriptions
                    .groupBy { it.type }
                    .mapValues { (_, subs) ->
                        subs.sumOf { subscription ->
                            when (subscription.frequency) {
                                BillingFrequency.WEEKLY -> subscription.amount * 4
                                BillingFrequency.MONTHLY -> subscription.amount
                                BillingFrequency.QUARTERLY -> subscription.amount / 3
                                BillingFrequency.SEMI_ANNUAL -> subscription.amount / 6
                                BillingFrequency.ANNUAL -> subscription.amount / 12
                                BillingFrequency.CUSTOM -> subscription.amount
                            }
                        }
                    }

                SubscriptionStats(
                    totalMonthly = totalMonthly,
                    totalYearly = totalMonthly * 12,
                    activeCount = subscriptions.size,
                    categoryBreakdown = categoryBreakdown
                )
            }
    }
}

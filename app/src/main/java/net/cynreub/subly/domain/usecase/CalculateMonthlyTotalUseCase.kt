package net.cynreub.subly.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.repository.SubscriptionRepository
import javax.inject.Inject

class CalculateMonthlyTotalUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(): Flow<Double> {
        return subscriptionRepository.getActiveSubscriptions()
            .map { subscriptions ->
                subscriptions.sumOf { subscription ->
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
    }
}

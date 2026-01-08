package net.cynreub.subly.domain.usecase

import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.repository.SubscriptionRepository
import java.time.LocalDate
import javax.inject.Inject

class UpdateNextBillingDateUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription) {
        val nextDate = calculateNextBillingDate(subscription)
        val updatedSubscription = subscription.copy(nextBillingDate = nextDate)
        subscriptionRepository.updateSubscription(updatedSubscription)
    }

    private fun calculateNextBillingDate(subscription: Subscription): LocalDate {
        val current = subscription.nextBillingDate
        return when (subscription.frequency) {
            BillingFrequency.WEEKLY -> current.plusWeeks(1)
            BillingFrequency.MONTHLY -> current.plusMonths(1)
            BillingFrequency.QUARTERLY -> current.plusMonths(3)
            BillingFrequency.SEMI_ANNUAL -> current.plusMonths(6)
            BillingFrequency.ANNUAL -> current.plusYears(1)
            BillingFrequency.CUSTOM -> current.plusMonths(1)
        }
    }
}

package net.cynreub.subly.domain.usecase

import kotlinx.coroutines.flow.Flow
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.repository.SubscriptionRepository
import java.time.LocalDate
import javax.inject.Inject

class GetUpcomingSubscriptionsUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(days: Int = 30): Flow<List<Subscription>> {
        val targetDate = LocalDate.now().plusDays(days.toLong())
        return subscriptionRepository.getUpcomingSubscriptions(targetDate)
    }
}

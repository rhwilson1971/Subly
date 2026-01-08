package net.cynreub.subly.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.cynreub.subly.data.repository.PaymentMethodRepositoryImpl
import net.cynreub.subly.data.repository.SubscriptionRepositoryImpl
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        subscriptionRepositoryImpl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindPaymentMethodRepository(
        paymentMethodRepositoryImpl: PaymentMethodRepositoryImpl
    ): PaymentMethodRepository
}

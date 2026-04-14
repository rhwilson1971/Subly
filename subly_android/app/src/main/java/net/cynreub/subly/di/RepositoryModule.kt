package net.cynreub.subly.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.cynreub.subly.data.repository.CategoryRepositoryImpl
import net.cynreub.subly.data.repository.PaymentMethodRepositoryImpl
import net.cynreub.subly.data.repository.SubscriptionRepositoryImpl
import net.cynreub.subly.data.repository.UserProfileRepositoryImpl
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.domain.repository.UserProfileRepository
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

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository
}

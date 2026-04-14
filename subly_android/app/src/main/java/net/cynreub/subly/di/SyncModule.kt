package net.cynreub.subly.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.cynreub.subly.data.sync.DelegatingSyncProvider
import net.cynreub.subly.data.sync.SyncMigratorImpl
import net.cynreub.subly.domain.sync.SyncMigrator
import net.cynreub.subly.domain.sync.SyncProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncProvider(impl: DelegatingSyncProvider): SyncProvider

    @Binds
    @Singleton
    abstract fun bindSyncMigrator(impl: SyncMigratorImpl): SyncMigrator
}

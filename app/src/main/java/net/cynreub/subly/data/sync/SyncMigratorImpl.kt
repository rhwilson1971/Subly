package net.cynreub.subly.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.cynreub.subly.domain.sync.MigrationProgress
import net.cynreub.subly.domain.sync.SyncMigrator
import net.cynreub.subly.domain.sync.SyncProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncMigratorImpl @Inject constructor() : SyncMigrator {

    private val _progress = MutableStateFlow<MigrationProgress>(MigrationProgress.Idle)
    override val progress: StateFlow<MigrationProgress> = _progress.asStateFlow()

    override suspend fun migrate(source: SyncProvider, destination: SyncProvider, uid: String) {
        _progress.value = MigrationProgress.Running(step = 0, total = 3)
        runCatching {
            val subscriptions = source.fetchAllSubscriptions(uid)
            _progress.value = MigrationProgress.Running(step = 1, total = 3)

            val paymentMethods = source.fetchAllPaymentMethods(uid)
            subscriptions.forEach { destination.upsertSubscription(it) }
            _progress.value = MigrationProgress.Running(step = 2, total = 3)

            val categories = source.fetchAllCategories(uid)
            paymentMethods.forEach { destination.upsertPaymentMethod(it) }
            _progress.value = MigrationProgress.Running(step = 3, total = 3)

            categories.forEach { destination.upsertCategory(it) }
        }.onSuccess {
            _progress.value = MigrationProgress.Success
        }.onFailure { cause ->
            _progress.value = MigrationProgress.Failure(cause)
            throw cause
        }
    }

    override fun reset() {
        _progress.value = MigrationProgress.Idle
    }
}

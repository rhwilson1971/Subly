package net.cynreub.subly.domain.sync

import kotlinx.coroutines.flow.StateFlow

interface SyncMigrator {
    val progress: StateFlow<MigrationProgress>

    /**
     * Copies all subscriptions, payment methods, and categories from [source] to [destination].
     * Progress is emitted via [progress]. The flow goes: Idle → Running(1,3) → Running(2,3) →
     * Running(3,3) → Success | Failure.
     *
     * @param uid The user ID to pass to providers that require it (e.g. Firestore).
     */
    suspend fun migrate(source: SyncProvider, destination: SyncProvider, uid: String)

    fun reset()
}

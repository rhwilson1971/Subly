package net.cynreub.subly.data.sync

import kotlinx.coroutines.flow.first
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.StorageProviderPreference
import net.cynreub.subly.data.remote.dropbox.DropboxSyncProvider
import net.cynreub.subly.data.remote.firestore.FirestoreSyncProvider
import net.cynreub.subly.data.remote.gdrive.GoogleDriveSyncProvider
import net.cynreub.subly.data.remote.onedrive.OneDriveSyncProvider
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.sync.SyncProvider
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DelegatingSyncProvider @Inject constructor(
    private val firestoreProvider: FirestoreSyncProvider,
    private val googleDriveProvider: GoogleDriveSyncProvider,
    private val dropboxProvider: DropboxSyncProvider,
    private val oneDriveProvider: OneDriveSyncProvider,
    private val noOpProvider: NoOpSyncProvider,
    private val preferencesManager: PreferencesManager,
    private val syncStateTracker: SyncStateTracker
) : SyncProvider {

    private suspend fun active(): SyncProvider =
        when (preferencesManager.storageProviderPreference.first()) {
            StorageProviderPreference.FIREBASE -> firestoreProvider
            StorageProviderPreference.GOOGLE_DRIVE -> googleDriveProvider
            StorageProviderPreference.DROPBOX -> dropboxProvider
            StorageProviderPreference.ONEDRIVE -> oneDriveProvider
            StorageProviderPreference.LOCAL -> noOpProvider
        }

    private suspend fun <T> tracked(block: suspend () -> T): T {
        return try {
            val result = block()
            syncStateTracker.onSyncSuccess()
            result
        } catch (e: Exception) {
            syncStateTracker.onSyncError(e.message ?: "Sync failed")
            throw e
        }
    }

    override suspend fun upsertSubscription(subscription: Subscription) =
        tracked { active().upsertSubscription(subscription) }

    override suspend fun deleteSubscription(id: UUID) =
        tracked { active().deleteSubscription(id) }

    override suspend fun fetchAllSubscriptions(uid: String): List<Subscription> =
        tracked { active().fetchAllSubscriptions(uid) }

    override suspend fun upsertPaymentMethod(paymentMethod: PaymentMethod) =
        tracked { active().upsertPaymentMethod(paymentMethod) }

    override suspend fun deletePaymentMethod(id: UUID) =
        tracked { active().deletePaymentMethod(id) }

    override suspend fun fetchAllPaymentMethods(uid: String): List<PaymentMethod> =
        tracked { active().fetchAllPaymentMethods(uid) }

    override suspend fun upsertCategory(category: Category) =
        tracked { active().upsertCategory(category) }

    override suspend fun deleteCategory(id: UUID) =
        tracked { active().deleteCategory(id) }

    override suspend fun fetchAllCategories(uid: String): List<Category> =
        tracked { active().fetchAllCategories(uid) }
}

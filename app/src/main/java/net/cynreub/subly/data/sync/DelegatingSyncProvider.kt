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
    private val preferencesManager: PreferencesManager
) : SyncProvider {

    private suspend fun active(): SyncProvider =
        when (preferencesManager.storageProviderPreference.first()) {
            StorageProviderPreference.FIREBASE -> firestoreProvider
            StorageProviderPreference.GOOGLE_DRIVE -> googleDriveProvider
            StorageProviderPreference.DROPBOX -> dropboxProvider
            StorageProviderPreference.ONEDRIVE -> oneDriveProvider
            StorageProviderPreference.LOCAL -> noOpProvider
        }

    override suspend fun upsertSubscription(subscription: Subscription) =
        active().upsertSubscription(subscription)

    override suspend fun deleteSubscription(id: UUID) =
        active().deleteSubscription(id)

    override suspend fun fetchAllSubscriptions(uid: String): List<Subscription> =
        active().fetchAllSubscriptions(uid)

    override suspend fun upsertPaymentMethod(paymentMethod: PaymentMethod) =
        active().upsertPaymentMethod(paymentMethod)

    override suspend fun deletePaymentMethod(id: UUID) =
        active().deletePaymentMethod(id)

    override suspend fun fetchAllPaymentMethods(uid: String): List<PaymentMethod> =
        active().fetchAllPaymentMethods(uid)

    override suspend fun upsertCategory(category: Category) =
        active().upsertCategory(category)

    override suspend fun deleteCategory(id: UUID) =
        active().deleteCategory(id)

    override suspend fun fetchAllCategories(uid: String): List<Category> =
        active().fetchAllCategories(uid)
}

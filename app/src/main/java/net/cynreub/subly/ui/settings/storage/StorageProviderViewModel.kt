package net.cynreub.subly.ui.settings.storage

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.StorageProviderPreference
import net.cynreub.subly.data.remote.dropbox.DropboxAuthManager
import net.cynreub.subly.data.remote.gdrive.GoogleDriveAuthManager
import net.cynreub.subly.data.remote.onedrive.MsalAuthManager
import net.cynreub.subly.data.sync.SyncStateTracker
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.domain.sync.SyncProvider
import javax.inject.Inject

@HiltViewModel
class StorageProviderViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val syncProvider: SyncProvider,
    private val syncStateTracker: SyncStateTracker,
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val categoryRepository: CategoryRepository,
    private val googleDriveAuthManager: GoogleDriveAuthManager,
    private val dropboxAuthManager: DropboxAuthManager,
    private val msalAuthManager: MsalAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageProviderUiState())
    val uiState: StateFlow<StorageProviderUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch {
            val providerFlow = combine(
                preferencesManager.storageProviderPreference,
                preferencesManager.googleDriveAccountEmail,
                preferencesManager.dropboxCredential,
                preferencesManager.oneDriveAccountEmail
            ) { provider, driveEmail, dropboxCred, oneDriveEmail ->
                listOf<Any?>(provider, driveEmail, dropboxCred, oneDriveEmail)
            }
            combine(providerFlow, syncStateTracker.lastSyncAt, syncStateTracker.lastSyncError) {
                providerData, lastSyncAt, lastSyncError ->
                @Suppress("UNCHECKED_CAST")
                StorageProviderUiState(
                    selectedProvider = providerData[0] as StorageProviderPreference,
                    googleDriveAccountEmail = providerData[1] as String?,
                    isDropboxConnected = providerData[2] != null,
                    oneDriveAccountEmail = providerData[3] as String?,
                    lastSyncAt = lastSyncAt,
                    lastSyncError = lastSyncError,
                    isSyncing = _uiState.value.isSyncing,
                    isMigrating = _uiState.value.isMigrating,
                    pendingProvider = _uiState.value.pendingProvider,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    // --- Provider selection ---

    /**
     * Called when user taps a new provider chip/card.
     * If different from the current provider, shows the migration dialog.
     * For providers that require OAuth, connect must happen separately.
     */
    fun onProviderSelected(provider: StorageProviderPreference) {
        if (provider == _uiState.value.selectedProvider) return
        _uiState.value = _uiState.value.copy(pendingProvider = provider)
    }

    fun dismissMigrationDialog() {
        _uiState.value = _uiState.value.copy(pendingProvider = null)
    }

    /** Migrate all local Room data to [pendingProvider], then switch. */
    fun migrateAndSwitch() {
        val target = _uiState.value.pendingProvider ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMigrating = true, pendingProvider = null)
            runCatching {
                // 1. Switch preference so DelegatingSyncProvider routes to the new provider
                preferencesManager.updateStorageProvider(target)

                // 2. Push all Room data to the new provider
                val subscriptions = subscriptionRepository.getAllSubscriptions().first()
                val paymentMethods = paymentMethodRepository.getAllPaymentMethods().first()
                val categories = categoryRepository.getAllCategories().first()

                subscriptions.forEach { syncProvider.upsertSubscription(it) }
                paymentMethods.forEach { syncProvider.upsertPaymentMethod(it) }
                categories.forEach { syncProvider.upsertCategory(it) }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    lastSyncError = "Migration failed: ${e.message}"
                )
            }
            _uiState.value = _uiState.value.copy(isMigrating = false)
        }
    }

    /** Switch provider without migrating existing data. */
    fun switchWithoutMigration() {
        val target = _uiState.value.pendingProvider ?: return
        viewModelScope.launch {
            preferencesManager.updateStorageProvider(target)
            _uiState.value = _uiState.value.copy(pendingProvider = null)
        }
    }

    // --- Sync Now ---

    /** Push all current Room data to the active provider and update the last-sync timestamp. */
    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, lastSyncError = null)
            syncStateTracker.clearError()
            runCatching {
                val uid = ""
                val subscriptions = subscriptionRepository.getAllSubscriptions().first()
                val paymentMethods = paymentMethodRepository.getAllPaymentMethods().first()
                val categories = categoryRepository.getAllCategories().first()

                subscriptions.forEach { syncProvider.upsertSubscription(it) }
                paymentMethods.forEach { syncProvider.upsertPaymentMethod(it) }
                categories.forEach { syncProvider.upsertCategory(it) }
            }.onSuccess {
                syncStateTracker.onSyncSuccess()
            }.onFailure { e ->
                syncStateTracker.onSyncError(e.message ?: "Sync failed")
            }
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    fun clearError() {
        syncStateTracker.clearError()
        _uiState.value = _uiState.value.copy(lastSyncError = null)
    }

    // --- Google Drive ---

    fun getGoogleDriveSignInIntent(): Intent =
        googleDriveAuthManager.buildSignInClient().signInIntent

    fun onGoogleDriveConnected(account: GoogleSignInAccount) {
        viewModelScope.launch {
            preferencesManager.updateGoogleDriveAccountEmail(account.email)
            preferencesManager.updateStorageProvider(StorageProviderPreference.GOOGLE_DRIVE)
        }
    }

    fun disconnectGoogleDrive() {
        viewModelScope.launch {
            googleDriveAuthManager.buildSignInClient().signOut()
            preferencesManager.updateGoogleDriveAccountEmail(null)
            preferencesManager.updateStorageProvider(StorageProviderPreference.FIREBASE)
        }
    }

    // --- Dropbox ---

    fun getDropboxAuthUrl(): String = dropboxAuthManager.buildAuthUrl()

    fun disconnectDropbox() {
        viewModelScope.launch {
            preferencesManager.updateDropboxCredential(null)
            preferencesManager.updateStorageProvider(StorageProviderPreference.FIREBASE)
        }
    }

    // --- OneDrive ---

    fun disconnectOneDrive() {
        viewModelScope.launch {
            msalAuthManager.signOut()
            preferencesManager.updateOneDriveAccountEmail(null)
            preferencesManager.updateStorageProvider(StorageProviderPreference.FIREBASE)
        }
    }
}

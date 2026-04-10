package net.cynreub.subly.ui.oauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.exception.MsalException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.StorageProviderPreference
import net.cynreub.subly.data.remote.onedrive.MsalAuthManager
import javax.inject.Inject

/**
 * Transparent Activity that launches MSAL's interactive token acquisition for OneDrive.
 * On success it stores the account email and switches the active provider to ONEDRIVE,
 * then immediately finishes so the user returns to Settings.
 */
@AndroidEntryPoint
class OneDriveOAuthActivity : ComponentActivity() {

    @Inject lateinit var msalAuthManager: MsalAuthManager
    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Only start auth on fresh launch, not on config-change recreations.
        if (savedInstanceState != null) return
        lifecycleScope.launch {
            val app = msalAuthManager.getApp() ?: run { finish(); return@launch }
            app.acquireToken(
                AcquireTokenParameters.Builder()
                    .startAuthorizationFromActivity(this@OneDriveOAuthActivity)
                    .withScopes(MsalAuthManager.SCOPES)
                    .withCallback(object : AuthenticationCallback {
                        override fun onSuccess(result: IAuthenticationResult) {
                            lifecycleScope.launch {
                                preferencesManager.updateOneDriveAccountEmail(result.account.username)
                                preferencesManager.updateStorageProvider(StorageProviderPreference.ONEDRIVE)
                                finish()
                            }
                        }
                        override fun onError(exception: MsalException) { finish() }
                        override fun onCancel() { finish() }
                    })
                    .build()
            )
        }
    }
}

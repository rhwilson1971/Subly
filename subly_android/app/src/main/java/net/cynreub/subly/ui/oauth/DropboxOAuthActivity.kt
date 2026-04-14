package net.cynreub.subly.ui.oauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.StorageProviderPreference
import net.cynreub.subly.data.remote.dropbox.DropboxAuthManager
import javax.inject.Inject

/**
 * Transparent trampoline activity that captures the Dropbox OAuth redirect URI,
 * exchanges it for a credential via [DropboxAuthManager], persists it, then finishes.
 */
@AndroidEntryPoint
class DropboxOAuthActivity : ComponentActivity() {

    @Inject lateinit var dropboxAuthManager: DropboxAuthManager
    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val redirectUri = intent?.data?.toString()
        if (redirectUri == null) {
            finish()
            return
        }
        lifecycleScope.launch {
            val credential = withContext(Dispatchers.IO) {
                dropboxAuthManager.finishAuth(redirectUri)
            }
            if (credential != null) {
                preferencesManager.updateDropboxCredential(credential)
                preferencesManager.updateStorageProvider(StorageProviderPreference.DROPBOX)
            }
            finish()
        }
    }
}

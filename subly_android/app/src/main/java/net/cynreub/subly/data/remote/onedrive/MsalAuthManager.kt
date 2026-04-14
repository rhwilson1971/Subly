package net.cynreub.subly.data.remote.onedrive

import android.content.Context
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.cynreub.subly.R
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class MsalAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SCOPES = listOf("Files.ReadWrite.AppFolder")
    }

    private var pca: ISingleAccountPublicClientApplication? = null

    /**
     * Returns the MSAL [ISingleAccountPublicClientApplication], initialising it on first call.
     * Safe to call from any coroutine context.
     */
    suspend fun getApp(): ISingleAccountPublicClientApplication? {
        pca?.let { return it }
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                runCatching {
                    PublicClientApplication.createSingleAccountPublicClientApplication(
                        context,
                        R.raw.msal_auth_config,
                        object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                            override fun onCreated(app: ISingleAccountPublicClientApplication) {
                                pca = app
                                cont.resume(app)
                            }
                            override fun onError(exception: MsalException) {
                                cont.resume(null)
                            }
                        }
                    )
                }.onFailure { cont.resume(null) }
            }
        }
    }

    /**
     * Silently acquires a fresh access token for the currently signed-in account.
     * Returns null if no account is signed in or token refresh fails.
     */
    suspend fun acquireTokenSilent(): String? {
        val app = getApp() ?: return null
        return runCatching {
            withContext(Dispatchers.IO) {
                val account = app.currentAccount.currentAccount ?: return@withContext null
                val params = AcquireTokenSilentParameters.Builder()
                    .forAccount(account)
                    .fromAuthority(account.authority)
                    .withScopes(SCOPES)
                    .build()
                app.acquireTokenSilent(params).accessToken
            }
        }.getOrNull()
    }

    /** Returns the currently signed-in account's username (email), or null. */
    suspend fun getAccountEmail(): String? {
        val app = getApp() ?: return null
        return runCatching { app.currentAccount.currentAccount?.username }.getOrNull()
    }

    /** Signs out and clears the MSAL token cache. */
    suspend fun signOut() {
        val app = getApp() ?: return
        withContext(Dispatchers.Main) {
            runCatching { app.signOut() }
        }
    }
}

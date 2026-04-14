package net.cynreub.subly.data.remote.dropbox

import android.content.Context
import com.dropbox.core.DbxAppInfo
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.DbxSessionStore
import com.dropbox.core.DbxWebAuth
import com.dropbox.core.v2.DbxClientV2
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DropboxAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Replace with the real key from https://www.dropbox.com/developers/apps
        const val APP_KEY = "subly_dropbox_app_key"
        const val REDIRECT_URI = "subly://dropbox-oauth"
        private const val CLIENT_ID = "SublyAndroid/1.0"
        private const val PREFS_NAME = "dropbox_oauth_session"
    }

    private val requestConfig = DbxRequestConfig(CLIENT_ID)
    private val appInfo = DbxAppInfo(APP_KEY)

    // SharedPreferences-backed session store persists PKCE state across Activity restarts
    private val sessionStore = object : DbxSessionStore {
        private val prefs by lazy {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        override fun get(): String? = prefs.getString("session", null)
        override fun set(s: String) { prefs.edit().putString("session", s).apply() }
        override fun clear() { prefs.edit().remove("session").apply() }
    }

    /** Returns the URL to open in a browser to start the OAuth 2 flow. */
    fun buildAuthUrl(): String {
        val webAuth = DbxWebAuth(requestConfig, appInfo)
        return webAuth.authorize(
            DbxWebAuth.Request.newBuilder()
                .withRedirectUri(REDIRECT_URI, sessionStore)
                .build()
        )
    }

    /**
     * Call when the OAuth redirect arrives. [redirectUri] is the full URI received by
     * [DropboxOAuthActivity]. Returns the access token string, or null on failure.
     */
    fun finishAuth(redirectUri: String): String? = runCatching {
        val webAuth = DbxWebAuth(requestConfig, appInfo)
        val uri = android.net.Uri.parse(redirectUri)
        // DbxWebAuth.finishFromRedirect expects Map<String, String[]>
        val params: Map<String, Array<String>> = uri.queryParameterNames
            .associateWith { key -> arrayOf(uri.getQueryParameter(key) ?: "") }
        val result = webAuth.finishFromRedirect(REDIRECT_URI, sessionStore, params)
        result.accessToken
    }.getOrNull()

    /** Builds a [DbxClientV2] from a stored access token, or null if invalid. */
    fun buildClient(accessToken: String): DbxClientV2? = runCatching {
        DbxClientV2(requestConfig, accessToken)
    }.getOrNull()
}

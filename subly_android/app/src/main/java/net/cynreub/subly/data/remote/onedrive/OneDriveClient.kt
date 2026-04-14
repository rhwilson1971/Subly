package net.cynreub.subly.data.remote.onedrive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HTTP client for Microsoft Graph OneDrive appRoot operations.
 * Files are stored at `/me/drive/special/approot:/{filename}`.
 * A single PUT creates or overwrites — no separate create/update check needed.
 */
@Singleton
class OneDriveClient @Inject constructor(
    private val authManager: MsalAuthManager
) {
    private val graphBase = "https://graph.microsoft.com/v1.0/me/drive/special/approot:"

    suspend fun readFile(fileName: String): String? = withContext(Dispatchers.IO) {
        val token = authManager.acquireTokenSilent() ?: return@withContext null
        val conn = URL("$graphBase/$fileName:/content").openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $token")
        return@withContext try {
            if (conn.responseCode == 200) conn.inputStream.bufferedReader().readText() else null
        } finally {
            conn.disconnect()
        }
    }

    suspend fun writeFile(fileName: String, content: String) = withContext(Dispatchers.IO) {
        val token = authManager.acquireTokenSilent() ?: return@withContext
        val bytes = content.toByteArray(Charsets.UTF_8)
        val conn = URL("$graphBase/$fileName:/content").openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.doOutput = true
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.setRequestProperty("Content-Length", bytes.size.toString())
        try {
            conn.outputStream.write(bytes)
            conn.responseCode
        } finally {
            conn.disconnect()
        }
    }
}

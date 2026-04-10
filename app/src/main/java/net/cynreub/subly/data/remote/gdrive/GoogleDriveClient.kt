package net.cynreub.subly.data.remote.gdrive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveClient @Inject constructor(
    private val authManager: GoogleDriveAuthManager
) {
    private val baseUrl = "https://www.googleapis.com/drive/v3"
    private val uploadUrl = "https://www.googleapis.com/upload/drive/v3"

    suspend fun readFile(fileName: String): String? = withContext(Dispatchers.IO) {
        val token = authManager.getAccessToken() ?: return@withContext null
        val fileId = findFileId(token, fileName) ?: return@withContext null
        downloadFileContent(token, fileId)
    }

    suspend fun writeFile(fileName: String, content: String) = withContext(Dispatchers.IO) {
        val token = authManager.getAccessToken() ?: return@withContext
        val existingId = findFileId(token, fileName)
        if (existingId != null) {
            updateFile(token, existingId, content)
        } else {
            createFile(token, fileName, content)
        }
    }

    suspend fun deleteAllFiles() = withContext(Dispatchers.IO) {
        val token = authManager.getAccessToken() ?: return@withContext
        listOf("subscriptions.json", "payment_methods.json", "categories.json").forEach { name ->
            findFileId(token, name)?.let { id -> deleteFileById(token, id) }
        }
    }

    private fun findFileId(token: String, fileName: String): String? {
        val query = URLEncoder.encode(
            "name='$fileName' and 'appDataFolder' in parents and trashed=false", "UTF-8"
        )
        val conn = URL("$baseUrl/files?spaces=appDataFolder&q=$query&fields=files(id)")
            .openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $token")
        return try {
            if (conn.responseCode == 200) {
                val files = JSONObject(conn.inputStream.bufferedReader().readText())
                    .getJSONArray("files")
                if (files.length() > 0) files.getJSONObject(0).getString("id") else null
            } else null
        } finally {
            conn.disconnect()
        }
    }

    private fun downloadFileContent(token: String, fileId: String): String? {
        val conn = URL("$baseUrl/files/$fileId?alt=media").openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $token")
        return try {
            if (conn.responseCode == 200) conn.inputStream.bufferedReader().readText() else null
        } finally {
            conn.disconnect()
        }
    }

    private fun createFile(token: String, fileName: String, content: String) {
        val boundary = "subly_boundary_${System.currentTimeMillis()}"
        val metadata = """{"name":"$fileName","parents":["appDataFolder"]}"""
        val body = "--$boundary\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n\r\n" +
                "$metadata\r\n" +
                "--$boundary\r\n" +
                "Content-Type: application/json\r\n\r\n" +
                "$content\r\n" +
                "--$boundary--"

        val conn = URL("$uploadUrl/files?uploadType=multipart").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
        conn.outputStream.write(body.toByteArray(Charsets.UTF_8))
        conn.responseCode
        conn.disconnect()
    }

    private fun updateFile(token: String, fileId: String, content: String) {
        val conn = URL("$uploadUrl/files/$fileId?uploadType=media")
            .openConnection() as HttpURLConnection
        conn.requestMethod = "PATCH"
        conn.doOutput = true
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.outputStream.write(content.toByteArray(Charsets.UTF_8))
        conn.responseCode
        conn.disconnect()
    }

    private fun deleteFileById(token: String, fileId: String) {
        val conn = URL("$baseUrl/files/$fileId").openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.responseCode
        conn.disconnect()
    }
}

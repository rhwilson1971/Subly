package net.cynreub.subly.data.remote.dropbox

import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.remote.sync.toCategory
import net.cynreub.subly.data.remote.sync.toJson
import net.cynreub.subly.data.remote.sync.toPaymentMethod
import net.cynreub.subly.data.remote.sync.toSubscription
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.sync.SyncProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores all data as three JSON files in /Apps/Subly/ on Dropbox:
 * subscriptions.json, payment_methods.json, categories.json.
 */
@Singleton
class DropboxSyncProvider @Inject constructor(
    private val authManager: DropboxAuthManager,
    private val preferencesManager: PreferencesManager
) : SyncProvider {

    companion object {
        private const val SUBSCRIPTIONS_PATH = "/subscriptions.json"
        private const val PAYMENT_METHODS_PATH = "/payment_methods.json"
        private const val CATEGORIES_PATH = "/categories.json"
    }

    private suspend fun client(): DbxClientV2? {
        val token = preferencesManager.dropboxCredential.first() ?: return null
        return authManager.buildClient(token)
    }

    // --- Subscriptions ---

    override suspend fun upsertSubscription(subscription: Subscription) {
        val list = readSubscriptions().toMutableList()
        list.removeAll { it.id == subscription.id }
        list.add(subscription)
        writeFile(SUBSCRIPTIONS_PATH, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun deleteSubscription(id: UUID) {
        val list = readSubscriptions().filter { it.id != id }
        writeFile(SUBSCRIPTIONS_PATH, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun fetchAllSubscriptions(uid: String): List<Subscription> =
        readSubscriptions()

    private suspend fun readSubscriptions(): List<Subscription> =
        parseJsonArray(readFile(SUBSCRIPTIONS_PATH)) { it.toSubscription() }

    // --- Payment Methods ---

    override suspend fun upsertPaymentMethod(paymentMethod: PaymentMethod) {
        val list = readPaymentMethods().toMutableList()
        list.removeAll { it.id == paymentMethod.id }
        list.add(paymentMethod)
        writeFile(PAYMENT_METHODS_PATH, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun deletePaymentMethod(id: UUID) {
        val list = readPaymentMethods().filter { it.id != id }
        writeFile(PAYMENT_METHODS_PATH, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun fetchAllPaymentMethods(uid: String): List<PaymentMethod> =
        readPaymentMethods()

    private suspend fun readPaymentMethods(): List<PaymentMethod> =
        parseJsonArray(readFile(PAYMENT_METHODS_PATH)) { it.toPaymentMethod() }

    // --- Categories ---

    override suspend fun upsertCategory(category: Category) {
        val list = readCategories().toMutableList()
        list.removeAll { it.id == category.id }
        list.add(category)
        writeFile(CATEGORIES_PATH, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun deleteCategory(id: UUID) {
        val list = readCategories().filter { it.id != id }
        writeFile(CATEGORIES_PATH, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun fetchAllCategories(uid: String): List<Category> = readCategories()

    private suspend fun readCategories(): List<Category> =
        parseJsonArray(readFile(CATEGORIES_PATH)) { it.toCategory() }

    // --- Dropbox file I/O ---

    private suspend fun readFile(path: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            client()?.files()?.downloadBuilder(path)?.start()?.inputStream
                ?.bufferedReader()?.readText()
        }.getOrNull()
    }

    private suspend fun writeFile(path: String, content: String) = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = content.toByteArray(Charsets.UTF_8)
            client()?.files()?.uploadBuilder(path)
                ?.withMode(WriteMode.OVERWRITE)
                ?.uploadAndFinish(ByteArrayInputStream(bytes))
        }
    }

    // --- Helpers ---

    private fun <T> parseJsonArray(raw: String?, transform: (JSONObject) -> T?): List<T> {
        raw ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { transform(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    private fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray =
        JSONArray().also { arr -> forEach { arr.put(transform(it)) } }
}

package net.cynreub.subly.ui.settings.storage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import net.cynreub.subly.data.preferences.StorageProviderPreference
import net.cynreub.subly.ui.oauth.OneDriveOAuthActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageProviderScreen(
    onNavigateBack: () -> Unit,
    viewModel: StorageProviderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val driveSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            runCatching {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
                viewModel.onGoogleDriveConnected(account)
            }
        }
    }

    val oneDriveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* handled inside OneDriveOAuthActivity */ }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage & Sync") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Choose where your subscription data is saved and synced.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Migration in-progress banner
            if (uiState.isMigrating) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val stepLabels = listOf(
                            "Reading subscriptions…",
                            "Copying subscriptions…",
                            "Copying payment methods…",
                            "Copying categories…"
                        )
                        val step = uiState.migrationStep ?: 0
                        val total = uiState.migrationTotal ?: 3
                        Text(
                            stepLabels.getOrElse(step) { "Migrating data…" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        if (total > 0) {
                            LinearProgressIndicator(
                                progress = { step.toFloat() / total.toFloat() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Step $step of $total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // Sync error banner
            uiState.lastSyncError?.let { error ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Sync error",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { viewModel.clearError() }) { Text("Dismiss") }
                    }
                }
            }

            // Last-sync timestamp + Sync Now
            if (uiState.selectedProvider != StorageProviderPreference.LOCAL &&
                uiState.selectedProvider != StorageProviderPreference.FIREBASE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val syncLabel = uiState.lastSyncAt?.let { ms ->
                        "Last synced: ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ms))}"
                    } ?: "Not yet synced"
                    Text(syncLabel, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(
                        onClick = { viewModel.syncNow() },
                        enabled = !uiState.isSyncing && !uiState.isMigrating
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.width(16.dp).height(16.dp),
                                strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp))
                        }
                        Text("Sync Now")
                    }
                }
            }

            // Provider cards
            ProviderEntry.entries.forEach { entry ->
                ProviderCard(
                    entry = entry,
                    isActive = uiState.selectedProvider == entry.preference,
                    connectionLabel = entry.connectionLabel(uiState),
                    onSelect = { viewModel.onProviderSelected(entry.preference) },
                    onConnect = {
                        when (entry) {
                            ProviderEntry.GOOGLE_DRIVE ->
                                driveSignInLauncher.launch(viewModel.getGoogleDriveSignInIntent())
                            ProviderEntry.DROPBOX ->
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.getDropboxAuthUrl()))
                                )
                            ProviderEntry.ONEDRIVE ->
                                oneDriveLauncher.launch(Intent(context, OneDriveOAuthActivity::class.java))
                            else -> {}
                        }
                    },
                    onDisconnect = {
                        when (entry) {
                            ProviderEntry.GOOGLE_DRIVE -> viewModel.disconnectGoogleDrive()
                            ProviderEntry.DROPBOX -> viewModel.disconnectDropbox()
                            ProviderEntry.ONEDRIVE -> viewModel.disconnectOneDrive()
                            else -> {}
                        }
                    },
                    isConnected = entry.isConnected(uiState),
                    supportsOAuth = entry.supportsOAuth
                )
            }
        }
    }

    // Migration dialog
    uiState.pendingProvider?.let { pending ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissMigrationDialog() },
            icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
            title = { Text("Switch to ${pending.displayName()}?") },
            text = {
                Text(
                    "Would you like to copy your current data to ${pending.displayName()} " +
                    "before switching? If you skip, your existing data in the new provider " +
                    "will be used instead."
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.migrateAndSwitch() }) { Text("Migrate & Switch") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.dismissMigrationDialog() }) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.switchWithoutMigration() }) { Text("Just Switch") }
                }
            }
        )
    }
}

@Composable
private fun ProviderCard(
    entry: ProviderEntry,
    isActive: Boolean,
    connectionLabel: String,
    isConnected: Boolean,
    supportsOAuth: Boolean,
    onSelect: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                         else MaterialTheme.colorScheme.surfaceVariant

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(if (isActive) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = connectionLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isActive) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (supportsOAuth) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isConnected) {
                        OutlinedButton(onClick = onConnect) { Text("Connect") }
                    } else {
                        if (!isActive) {
                            Button(onClick = onSelect) { Text("Use This") }
                        }
                        TextButton(onClick = onDisconnect) { Text("Disconnect") }
                    }
                }
            } else if (!isActive) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onSelect) { Text("Use This") }
            }
        }
    }
}

private enum class ProviderEntry(
    val preference: StorageProviderPreference,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val supportsOAuth: Boolean
) {
    LOCAL(StorageProviderPreference.LOCAL, "Local only", Icons.Default.PhoneAndroid, false),
    FIREBASE(StorageProviderPreference.FIREBASE, "Firebase", Icons.Default.Cloud, false),
    GOOGLE_DRIVE(StorageProviderPreference.GOOGLE_DRIVE, "Google Drive", Icons.Default.Cloud, true),
    DROPBOX(StorageProviderPreference.DROPBOX, "Dropbox", Icons.Default.Cloud, true),
    ONEDRIVE(StorageProviderPreference.ONEDRIVE, "OneDrive", Icons.Default.Cloud, true);

    fun isConnected(state: StorageProviderUiState): Boolean = when (this) {
        GOOGLE_DRIVE -> state.googleDriveAccountEmail != null
        DROPBOX -> state.isDropboxConnected
        ONEDRIVE -> state.oneDriveAccountEmail != null
        else -> true
    }

    fun connectionLabel(state: StorageProviderUiState): String = when (this) {
        LOCAL -> "Data stays on this device only"
        FIREBASE -> "Syncs via your Firebase account"
        GOOGLE_DRIVE -> state.googleDriveAccountEmail?.let { "Connected as $it" } ?: "Not connected"
        DROPBOX -> if (state.isDropboxConnected) "Connected" else "Not connected"
        ONEDRIVE -> state.oneDriveAccountEmail?.let { "Connected as $it" } ?: "Not connected"
    }
}

private fun StorageProviderPreference.displayName(): String = when (this) {
    StorageProviderPreference.LOCAL -> "Local"
    StorageProviderPreference.FIREBASE -> "Firebase"
    StorageProviderPreference.GOOGLE_DRIVE -> "Google Drive"
    StorageProviderPreference.DROPBOX -> "Dropbox"
    StorageProviderPreference.ONEDRIVE -> "OneDrive"
}

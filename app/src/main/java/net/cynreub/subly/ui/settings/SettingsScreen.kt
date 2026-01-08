package net.cynreub.subly.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    // Refresh permission status when returning to screen
    LaunchedEffect(Unit) {
        viewModel.refreshPermissionStatus()
    }

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Notification Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Notifications Enabled Switch
        SettingsSection(title = "General") {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Enable Notifications",
                subtitle = if (uiState.hasNotificationPermission) {
                    "Receive reminders for upcoming payments"
                } else {
                    "Permission required"
                },
                trailing = {
                    Switch(
                        checked = uiState.notificationsEnabled && uiState.hasNotificationPermission,
                        onCheckedChange = { viewModel.onNotificationsEnabledChange(it) }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notification Times
        SettingsSection(title = "Reminder Times") {
            SettingsItem(
                icon = Icons.Default.Schedule,
                title = "Morning Reminder",
                subtitle = uiState.morningNotificationTime,
                enabled = uiState.notificationsEnabled && uiState.hasNotificationPermission,
                onClick = { viewModel.showMorningTimePicker() }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsItem(
                icon = Icons.Default.Schedule,
                title = "Evening Reminder",
                subtitle = uiState.eveningNotificationTime,
                enabled = uiState.notificationsEnabled && uiState.hasNotificationPermission,
                onClick = { viewModel.showEveningTimePicker() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Default Reminder Days
        SettingsSection(title = "Defaults") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Default Reminder Days",
                subtitle = "Remind ${uiState.defaultReminderDays} days before payment",
                trailing = {
                    Text(
                        text = "${uiState.defaultReminderDays}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        uiState.error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // Permission Dialog
    if (uiState.showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPermissionDialog() },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            title = { Text("Enable Notifications") },
            text = {
                Text(
                    "Subly needs notification permission to remind you about upcoming subscription payments. " +
                            "You can customize when you receive these reminders in the settings."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissPermissionDialog()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPermissionDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Time Pickers
    if (uiState.showMorningTimePicker) {
        TimePickerDialog(
            initialTime = uiState.morningNotificationTime,
            onTimeSelected = { time ->
                viewModel.onMorningTimeChange(time)
                viewModel.dismissMorningTimePicker()
            },
            onDismiss = { viewModel.dismissMorningTimePicker() }
        )
    }

    if (uiState.showEveningTimePicker) {
        TimePickerDialog(
            initialTime = uiState.eveningNotificationTime,
            onTimeSelected = { time ->
                viewModel.onEveningTimeChange(time)
                viewModel.dismissEveningTimePicker()
            },
            onDismiss = { viewModel.dismissEveningTimePicker() }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null && enabled) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = clickModifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailing()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Parse initial time
    val (initialHour, initialMinute) = initialTime.split(":").map { it.toInt() }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedTime = String.format(
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onTimeSelected(selectedTime)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

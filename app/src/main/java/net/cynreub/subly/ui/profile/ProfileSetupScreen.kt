package net.cynreub.subly.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateNext) {
        if (uiState.navigateNext) {
            viewModel.onNavigateNextHandled()
            onNavigateNext()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Set Up Your Profile") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile picture placeholder
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile picture",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Profile photo coming soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Full Name — required
            item {
                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.fullNameError != null,
                    supportingText = uiState.fullNameError?.let { { Text(it) } }
                )
            }

            // Email — read-only, pre-populated from Firebase Auth
            item {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true,
                    supportingText = { Text("Linked to your account") }
                )
            }

            // Date of Birth — optional, DatePickerDialog
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showDatePicker() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Date of Birth (Optional)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.dateOfBirth
                                    ?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                    ?: "Not set",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.dateOfBirth != null) {
                                TextButton(onClick = viewModel::clearDateOfBirth) {
                                    Text("Clear")
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pick date",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Phone Number — optional
            item {
                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChange,
                    label = { Text("Phone Number (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            // Error banner
            if (uiState.error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.error!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Save / Continue button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = viewModel::saveProfile,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (uiState.isSaving) "Saving…" else "Continue",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (uiState.showDatePicker) {
        DateOfBirthPickerDialog(
            onDateSelected = viewModel::onDateOfBirthSelected,
            onDismiss = viewModel::dismissDatePicker
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthPickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // Default to 25 years ago as a sensible starting point
    val defaultMillis = LocalDate.now().minusYears(25).toEpochDay() * 24 * 60 * 60 * 1000
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = defaultMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000)))
                    }
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(
            state = datePickerState,
            headline = { Text("Select date of birth", modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)) },
            showModeToggle = true
        )
    }
}

package net.cynreub.subly.ui.subscriptions.addedit

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.SubscriptionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionScreen(
    subscriptionId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditSubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Edit Subscription" else "Add Subscription")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null && !uiState.isEditMode) {
            // Only show error screen if it's a loading error (not validation)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            AddEditSubscriptionForm(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    // Dialogs
    if (uiState.showTypeDialog) {
        TypeSelectorDialog(
            selectedType = uiState.selectedType,
            onTypeSelected = viewModel::onTypeSelected,
            onDismiss = viewModel::dismissTypeDialog
        )
    }

    if (uiState.showFrequencyDialog) {
        FrequencySelectorDialog(
            selectedFrequency = uiState.selectedFrequency,
            onFrequencySelected = viewModel::onFrequencySelected,
            onDismiss = viewModel::dismissFrequencyDialog
        )
    }

    if (uiState.showPaymentMethodDialog) {
        PaymentMethodSelectorDialog(
            paymentMethods = uiState.availablePaymentMethods,
            selectedPaymentMethod = uiState.selectedPaymentMethod,
            onPaymentMethodSelected = viewModel::onPaymentMethodSelected,
            onDismiss = viewModel::dismissPaymentMethodDialog
        )
    }

    if (uiState.showDatePicker) {
        DatePickerModal(
            initialDate = uiState.startDate,
            onDateSelected = viewModel::onStartDateSelected,
            onDismiss = viewModel::dismissDatePicker
        )
    }
}

@Composable
private fun AddEditSubscriptionForm(
    uiState: AddEditSubscriptionUiState,
    viewModel: AddEditSubscriptionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Error banner (for save errors)
        if (uiState.error != null && uiState.isEditMode) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Name Field
        item {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Subscription Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } }
            )
        }

        // Type Selector
        item {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showTypeDialog() }
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
                            text = "Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatSubscriptionType(uiState.selectedType),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Amount Field
        item {
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Text("$", style = MaterialTheme.typography.bodyLarge) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.amountError != null,
                supportingText = uiState.amountError?.let { { Text(it) } } ?: {
                    Text("Currency: USD")
                }
            )
        }

        // Frequency Selector
        item {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showFrequencyDialog() }
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
                            text = "Billing Frequency",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatBillingFrequency(uiState.selectedFrequency),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Start Date Picker
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
                            text = "Start Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.startDate.format(
                                DateTimeFormatter.ofPattern("MMM dd, yyyy")
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Auto-calculated Next Billing Date (Read-only display)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
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
                            text = "Next Billing Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = calculateNextBillingDatePreview(
                                uiState.startDate,
                                uiState.selectedFrequency
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Auto-calculated from start date + frequency",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Payment Method Selector
        item {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showPaymentMethodDialog() }
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
                            text = "Payment Method (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.selectedPaymentMethod?.nickname ?: "None",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Notes Field
        item {
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                supportingText = { Text("Add any additional notes") }
            )
        }

        // Reminder Days Field
        item {
            OutlinedTextField(
                value = uiState.reminderDaysBefore,
                onValueChange = viewModel::onReminderDaysChange,
                label = { Text("Remind Me (Days Before)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.reminderDaysError != null,
                supportingText = uiState.reminderDaysError?.let { { Text(it) } } ?: {
                    Text("Get notified before the bill is due (0-30 days)")
                }
            )
        }

        // Active/Inactive Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
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
                            text = "Active Subscription",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.isActive) "This subscription is active"
                            else "This subscription is paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isActive,
                        onCheckedChange = viewModel::onIsActiveChange
                    )
                }
            }
        }

        // Save Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.onSaveClick(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (uiState.isEditMode) "Update Subscription" else "Add Subscription")
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Helper Composables for Dialogs

@Composable
private fun TypeSelectorDialog(
    selectedType: SubscriptionType,
    onTypeSelected: (SubscriptionType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Subscription Type") },
        text = {
            Column {
                SubscriptionType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTypeSelected(type) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == selectedType,
                            onClick = { onTypeSelected(type) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = formatSubscriptionType(type))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun FrequencySelectorDialog(
    selectedFrequency: BillingFrequency,
    onFrequencySelected: (BillingFrequency) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Billing Frequency") },
        text = {
            Column {
                BillingFrequency.entries.forEach { frequency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFrequencySelected(frequency) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = frequency == selectedFrequency,
                            onClick = { onFrequencySelected(frequency) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = formatBillingFrequency(frequency))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PaymentMethodSelectorDialog(
    paymentMethods: List<PaymentMethod>,
    selectedPaymentMethod: PaymentMethod?,
    onPaymentMethodSelected: (PaymentMethod?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Payment Method") },
        text = {
            Column {
                // None option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPaymentMethodSelected(null) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == null,
                        onClick = { onPaymentMethodSelected(null) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "None")
                }

                // Payment methods
                paymentMethods.forEach { paymentMethod ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPaymentMethodSelected(paymentMethod) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = paymentMethod.id == selectedPaymentMethod?.id,
                            onClick = { onPaymentMethodSelected(paymentMethod) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = paymentMethod.nickname)
                    }
                }

                if (paymentMethods.isEmpty()) {
                    Text(
                        text = "No payment methods available. Add one in the Payments tab.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Helper Functions

private fun formatSubscriptionType(type: SubscriptionType): String {
    return when (type) {
        SubscriptionType.STREAMING -> "Streaming"
        SubscriptionType.MAGAZINE -> "Magazine"
        SubscriptionType.SERVICE -> "Service"
        SubscriptionType.MEMBERSHIP -> "Membership"
        SubscriptionType.CLUB -> "Club"
        SubscriptionType.UTILITY -> "Utility"
        SubscriptionType.SOFTWARE -> "Software"
        SubscriptionType.OTHER -> "Other"
    }
}

private fun formatBillingFrequency(frequency: BillingFrequency): String {
    return when (frequency) {
        BillingFrequency.WEEKLY -> "Weekly"
        BillingFrequency.MONTHLY -> "Monthly"
        BillingFrequency.QUARTERLY -> "Quarterly"
        BillingFrequency.SEMI_ANNUAL -> "Semi-Annual (6 months)"
        BillingFrequency.ANNUAL -> "Annual (Yearly)"
        BillingFrequency.CUSTOM -> "Custom"
    }
}

private fun calculateNextBillingDatePreview(
    startDate: LocalDate,
    frequency: BillingFrequency
): String {
    val nextDate = when (frequency) {
        BillingFrequency.WEEKLY -> startDate.plusWeeks(1)
        BillingFrequency.MONTHLY -> startDate.plusMonths(1)
        BillingFrequency.QUARTERLY -> startDate.plusMonths(3)
        BillingFrequency.SEMI_ANNUAL -> startDate.plusMonths(6)
        BillingFrequency.ANNUAL -> startDate.plusYears(1)
        BillingFrequency.CUSTOM -> startDate.plusMonths(1)
    }
    return nextDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
}

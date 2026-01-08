package net.cynreub.subly.ui.subscriptions.detail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.PaymentType
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.model.SubscriptionType
import net.cynreub.subly.ui.payment.formatPaymentType
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailScreen(
    subscriptionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle delete success navigation
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Subscription Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Edit action
                    IconButton(
                        onClick = { uiState.subscription?.let { onNavigateToEdit(it.id.toString()) } },
                        enabled = uiState.subscription != null && !uiState.isDeleting
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    // Delete action
                    IconButton(
                        onClick = { viewModel.showDeleteDialog() },
                        enabled = uiState.subscription != null && !uiState.isDeleting
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.subscription == null -> {
                // Critical error (failed to load)
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
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.subscription != null -> {
                SubscriptionDetailContent(
                    subscription = uiState.subscription!!,
                    paymentMethod = uiState.paymentMethod,
                    isMarkingAsPaid = uiState.isMarkingAsPaid,
                    isDeleting = uiState.isDeleting,
                    error = uiState.error,
                    onMarkAsPaid = viewModel::onMarkAsPaid,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        DeleteConfirmationDialog(
            subscriptionName = uiState.subscription?.name ?: "",
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::dismissDeleteDialog
        )
    }
}

@Composable
private fun SubscriptionDetailContent(
    subscription: Subscription,
    paymentMethod: PaymentMethod?,
    isMarkingAsPaid: Boolean,
    isDeleting: Boolean,
    error: String?,
    onMarkAsPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Error banner (for action errors)
        if (error != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Header with name and status badge
        item {
            HeaderSection(subscription = subscription)
        }

        // Billing Information Card
        item {
            BillingInformationCard(subscription = subscription)
        }

        // Payment Method Card (if available)
        if (paymentMethod != null || subscription.paymentMethodId != null) {
            item {
                PaymentMethodCard(paymentMethod = paymentMethod)
            }
        }

        // Additional Details Card
        item {
            AdditionalDetailsCard(subscription = subscription)
        }

        // Action Buttons
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ActionButtons(
                isActive = subscription.isActive,
                isMarkingAsPaid = isMarkingAsPaid,
                isDeleting = isDeleting,
                onMarkAsPaid = onMarkAsPaid
            )
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeaderSection(
    subscription: Subscription,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Status Badge
            StatusBadge(isActive = subscription.isActive)

            Spacer(modifier = Modifier.height(12.dp))

            // Subscription Name
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Type
            Text(
                text = formatSubscriptionType(subscription.type),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StatusBadge(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (isActive) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.error
        }
    ) {
        Text(
            text = if (isActive) "ACTIVE" else "INACTIVE",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isActive) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                MaterialTheme.colorScheme.onError
            }
        )
    }
}

@Composable
private fun BillingInformationCard(
    subscription: Subscription,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Billing Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount
            DetailRow(
                label = "Amount",
                value = "${subscription.currency} ${String.format("%.2f", subscription.amount)}"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Frequency
            DetailRow(
                label = "Frequency",
                value = formatBillingFrequency(subscription.frequency)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Start Date
            DetailRow(
                label = "Start Date",
                value = subscription.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Next Billing Date (highlighted)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next Billing Date",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = subscription.nextBillingDate.format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy")
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    paymentMethod: PaymentMethod?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (paymentMethod != null) {
                DetailRow(
                    label = "Name",
                    value = paymentMethod.nickname
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(
                    label = "Type",
                    value = formatPaymentType(paymentMethod.type)
                )

                paymentMethod.lastFourDigits?.let { digits ->
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(
                        label = "Last 4 Digits",
                        value = "****$digits"
                    )
                }
            } else {
                Text(
                    text = "Loading payment method...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdditionalDetailsCard(
    subscription: Subscription,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Additional Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder Days
            DetailRow(
                label = "Reminder",
                value = "${subscription.reminderDaysBefore} ${
                    if (subscription.reminderDaysBefore == 1) "day" else "days"
                } before"
            )

            // Notes (if available)
            if (!subscription.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subscription.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isActive: Boolean,
    isMarkingAsPaid: Boolean,
    isDeleting: Boolean,
    onMarkAsPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mark as Paid Button (only for active subscriptions)
        if (isActive) {
            Button(
                onClick = onMarkAsPaid,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isMarkingAsPaid && !isDeleting
            ) {
                if (isMarkingAsPaid) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marking as Paid...")
                } else {
                    Text("Mark as Paid")
                }
            }
        }

        // Delete action is in TopAppBar, but show loading overlay if deleting
        if (isDeleting) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Deleting subscription...",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    subscriptionName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Delete Subscription?")
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$subscriptionName\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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

package net.cynreub.subly.ui.payment

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentMethodsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error in Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add payment method"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
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

            uiState.paymentMethods.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No payment methods",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first payment method",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.paymentMethods,
                        key = { it.paymentMethod.id.toString() }
                    ) { paymentMethodWithUsage ->
                        PaymentMethodListItem(
                            paymentMethodWithUsage = paymentMethodWithUsage,
                            onEdit = { onNavigateToEdit(it.paymentMethod.id.toString()) },
                            onDelete = { viewModel.showDeleteDialog(it) }
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (uiState.showDeleteDialog) {
            val paymentMethodWithUsage = uiState.deletingPaymentMethod
            if (paymentMethodWithUsage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDeleteDialog() },
                    title = { Text("Delete Payment Method?") },
                    text = {
                        val message = if (paymentMethodWithUsage.subscriptionCount > 0) {
                            "This payment method is used by ${paymentMethodWithUsage.subscriptionCount} subscription(s) and cannot be deleted."
                        } else {
                            "Are you sure you want to delete \"${paymentMethodWithUsage.paymentMethod.nickname}\"?"
                        }
                        Text(message)
                    },
                    confirmButton = {
                        if (paymentMethodWithUsage.subscriptionCount == 0) {
                            TextButton(
                                onClick = { viewModel.onDeleteConfirm() }
                            ) {
                                Text("Delete")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.dismissDeleteDialog() }
                        ) {
                            Text(if (paymentMethodWithUsage.subscriptionCount > 0) "OK" else "Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodListItem(
    paymentMethodWithUsage: PaymentMethodWithUsage,
    onEdit: (PaymentMethodWithUsage) -> Unit,
    onDelete: (PaymentMethodWithUsage) -> Unit,
    modifier: Modifier = Modifier
) {
    val paymentMethod = paymentMethodWithUsage.paymentMethod
    val subscriptionCount = paymentMethodWithUsage.subscriptionCount
    val canDelete = subscriptionCount == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit(paymentMethodWithUsage) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Payment Type Icon
            Icon(
                imageVector = getPaymentTypeIcon(paymentMethod.type),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Payment Method Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = paymentMethod.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatPaymentType(paymentMethod.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    paymentMethod.lastFourDigits?.let { digits ->
                        Text(
                            text = "••$digits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (subscriptionCount == 1) {
                        "1 subscription"
                    } else {
                        "$subscriptionCount subscriptions"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canDelete) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            // Delete Button
            IconButton(
                onClick = { onDelete(paymentMethodWithUsage) },
                enabled = canDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (canDelete) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}

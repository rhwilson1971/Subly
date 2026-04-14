package net.cynreub.subly.ui.payment.addedit

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.cynreub.subly.domain.model.PaymentType
import net.cynreub.subly.ui.payment.formatPaymentType
import net.cynreub.subly.ui.payment.getPaymentTypeIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPaymentMethodScreen(
    paymentMethodId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditPaymentMethodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate back on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

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
                title = {
                    Text(if (uiState.isEditMode) "Edit Payment Method" else "Add Payment Method")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
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

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nickname Field
                    item {
                        OutlinedTextField(
                            value = uiState.nickname,
                            onValueChange = { viewModel.onNicknameChange(it) },
                            label = { Text("Nickname *") },
                            placeholder = { Text("e.g., My Chase Card") },
                            isError = uiState.nicknameError != null,
                            supportingText = {
                                uiState.nicknameError?.let { Text(it) }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Payment Type Selector
                    item {
                        Column {
                            Text(
                                text = "Payment Type *",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.showTypeDialog() }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getPaymentTypeIcon(uiState.selectedType),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = formatPaymentType(uiState.selectedType),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // Last Four Digits Field
                    item {
                        OutlinedTextField(
                            value = uiState.lastFourDigits,
                            onValueChange = { viewModel.onLastFourDigitsChange(it) },
                            label = { Text("Last 4 Digits (Optional)") },
                            placeholder = { Text("1234") },
                            isError = uiState.lastFourDigitsError != null,
                            supportingText = {
                                uiState.lastFourDigitsError?.let { Text(it) }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Privacy Notice
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "We never store full card numbers, CVV, or sensitive payment data",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Save Button
                    item {
                        Button(
                            onClick = { viewModel.onSaveClick() },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (uiState.isEditMode) "Update" else "Save")
                        }
                    }
                }
            }
        }

        // Payment Type Dialog
        if (uiState.showTypeDialog) {
            PaymentTypeSelectionDialog(
                selectedType = uiState.selectedType,
                onTypeSelected = { viewModel.onTypeSelected(it) },
                onDismiss = { viewModel.dismissTypeDialog() }
            )
        }
    }
}

@Composable
private fun PaymentTypeSelectionDialog(
    selectedType: PaymentType,
    onTypeSelected: (PaymentType) -> Unit,
    onDismiss: () -> Unit
) {
    val creditCards = listOf(
        PaymentType.VISA,
        PaymentType.MASTERCARD,
        PaymentType.DISCOVER,
        PaymentType.AMEX
    )

    val digitalPayments = listOf(
        PaymentType.PAYPAL,
        PaymentType.VENMO,
        PaymentType.CASHAPP,
        PaymentType.AFFIRM,
        PaymentType.KLARNA
    )

    val otherMethods = listOf(
        PaymentType.DEBIT_CARD,
        PaymentType.BANK_TRANSFER,
        PaymentType.CASH,
        PaymentType.OTHER
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Payment Type") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Credit Cards Section
                item {
                    SectionHeader(title = "Credit Cards")
                }
                items(creditCards) { type ->
                    PaymentTypeRow(
                        type = type,
                        isSelected = type == selectedType,
                        onClick = { onTypeSelected(type) }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Digital Payments Section
                item {
                    SectionHeader(title = "Digital Payments")
                }
                items(digitalPayments) { type ->
                    PaymentTypeRow(
                        type = type,
                        isSelected = type == selectedType,
                        onClick = { onTypeSelected(type) }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Other Methods Section
                item {
                    SectionHeader(title = "Other")
                }
                items(otherMethods) { type ->
                    PaymentTypeRow(
                        type = type,
                        isSelected = type == selectedType,
                        onClick = { onTypeSelected(type) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PaymentTypeRow(
    type: PaymentType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = getPaymentTypeIcon(type),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = formatPaymentType(type),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

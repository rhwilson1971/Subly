package net.cynreub.subly.ui.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.cynreub.subly.domain.model.Subscription
import java.time.format.DateTimeFormatter

@Composable
fun SubscriptionsScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Subscription")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and Filter Bar
            SearchAndFilterBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onFilterClick = { showFilterDialog = true },
                onSortClick = { showSortDialog = true }
            )

            // Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
            } else {
                SubscriptionsList(
                    subscriptions = uiState.filteredSubscriptions,
                    onSubscriptionClick = { subscription ->
                        onNavigateToDetail(subscription.id.toString())
                    }
                )
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = uiState.selectedFilter,
            onFilterSelected = { filter ->
                viewModel.onFilterChange(filter)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Sort Dialog
    if (showSortDialog) {
        SortDialog(
            currentSortOrder = uiState.sortOrder,
            onSortOrderSelected = { sortOrder ->
                viewModel.onSortOrderChange(sortOrder)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search subscriptions...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )

            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.Menu, contentDescription = "Filter")
            }

            IconButton(onClick = onSortClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Sort")
            }
        }
    }
}

@Composable
private fun SubscriptionsList(
    subscriptions: List<Subscription>,
    onSubscriptionClick: (Subscription) -> Unit,
    modifier: Modifier = Modifier
) {
    if (subscriptions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No subscriptions found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add your first subscription to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(subscriptions, key = { it.id.toString() }) { subscription ->
                SubscriptionListItem(
                    subscription = subscription,
                    onClick = { onSubscriptionClick(subscription) }
                )
            }
        }
    }
}

@Composable
private fun SubscriptionListItem(
    subscription: Subscription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (subscription.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due: ${subscription.nextBillingDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subscription.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!subscription.isActive) {
                        Text(
                            text = "INACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${subscription.currency} ${String.format("%.2f", subscription.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subscription.frequency.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FilterDialog(
    currentFilter: SubscriptionFilter,
    onFilterSelected: (SubscriptionFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Subscriptions") },
        text = {
            Column {
                SubscriptionFilter.entries.forEach { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFilterSelected(filter) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = filter == currentFilter,
                            onClick = { onFilterSelected(filter) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (filter) {
                                SubscriptionFilter.ALL -> "All Subscriptions"
                                SubscriptionFilter.ACTIVE -> "Active Only"
                                SubscriptionFilter.INACTIVE -> "Inactive Only"
                                SubscriptionFilter.BY_TYPE -> "By Type"
                            }
                        )
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
private fun SortDialog(
    currentSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort By") },
        text = {
            Column {
                SortOrder.entries.forEach { sortOrder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortOrderSelected(sortOrder) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortOrder == currentSortOrder,
                            onClick = { onSortOrderSelected(sortOrder) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (sortOrder) {
                                SortOrder.NAME_ASC -> "Name (A-Z)"
                                SortOrder.NAME_DESC -> "Name (Z-A)"
                                SortOrder.AMOUNT_ASC -> "Amount (Low to High)"
                                SortOrder.AMOUNT_DESC -> "Amount (High to Low)"
                                SortOrder.NEXT_BILLING_DATE -> "Next Billing Date"
                            }
                        )
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

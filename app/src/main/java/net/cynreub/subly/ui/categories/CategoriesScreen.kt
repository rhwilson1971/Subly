package net.cynreub.subly.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.cynreub.subly.ui.subscriptions.DashboardFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFilter: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddSheet) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                uiState.categories.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No categories yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.categories,
                            key = { it.category.id.toString() }
                        ) { cwc ->
                            SwipeableCategoryItem(
                                categoryWithCount = cwc,
                                onEdit = { viewModel.openEditSheet(cwc.category) },
                                onDelete = { viewModel.requestDelete(cwc) },
                                onClick = {
                                    val arg = DashboardFilter.toRouteArg(
                                        DashboardFilter.ByCategory(
                                            categoryId = cwc.category.id,
                                            displayName = cwc.category.displayName
                                        )
                                    )
                                    onNavigateToFilter(arg)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Edit bottom sheet
    if (uiState.showAddEditSheet) {
        AddEditCategorySheet(
            uiState = uiState,
            sheetState = sheetState,
            onDismiss = viewModel::closeSheet,
            onSave = { name, emoji, colorHex ->
                viewModel.saveCategory(name, emoji, colorHex)
            }
        )
    }

    // Delete confirmation dialog
    uiState.deleteCandidate?.let { candidate ->
        DeleteCategoryDialog(
            categoryWithCount = candidate,
            deleteError = uiState.deleteError,
            isDeleting = uiState.isDeleting,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableCategoryItem(
    categoryWithCount: CategoryWithCount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false // don't commit the swipe — let sheet open instead
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false // dialog handles the actual delete
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    // Reset swipe state after action resolves (sheet/dialog opens)
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val isStartToEnd = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val isEndToStart = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isStartToEnd -> MaterialTheme.colorScheme.primaryContainer
                            isEndToStart -> MaterialTheme.colorScheme.errorContainer
                            else -> Color.Transparent
                        }
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = if (isStartToEnd) Icons.Default.Edit else Icons.Default.Delete,
                    contentDescription = if (isStartToEnd) "Edit" else "Delete",
                    tint = if (isStartToEnd)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        CategoryCard(categoryWithCount = categoryWithCount, onClick = onClick)
    }
}

@Composable
private fun CategoryCard(
    categoryWithCount: CategoryWithCount,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = categoryWithCount.category
    val parsedColor = runCatching { Color(android.graphics.Color.parseColor(category.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Color dot with emoji
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(parsedColor.copy(alpha = 0.15f))
                ) {
                    Text(text = category.emoji, style = MaterialTheme.typography.titleMedium)
                }

                Column {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${categoryWithCount.subscriptionCount} subscription${if (categoryWithCount.subscriptionCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Count badge
            if (categoryWithCount.subscriptionCount > 0) {
                Badge(
                    containerColor = parsedColor.copy(alpha = 0.2f),
                    contentColor = parsedColor
                ) {
                    Text(
                        text = categoryWithCount.subscriptionCount.toString(),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Color accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(parsedColor)
            )
        }
    }
}

@Composable
private fun DeleteCategoryDialog(
    categoryWithCount: CategoryWithCount,
    deleteError: String?,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val count = categoryWithCount.subscriptionCount
    val hasSubscriptions = count > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (hasSubscriptions) "Cannot Delete" else "Delete Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasSubscriptions) {
                    Text(
                        text = "\"${categoryWithCount.category.displayName}\" is used by $count subscription${if (count != 1) "s" else ""}. " +
                                "Reassign them to another category before deleting.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Delete \"${categoryWithCount.category.displayName}\"? This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                deleteError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            if (!hasSubscriptions) {
                TextButton(
                    onClick = onConfirm,
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (hasSubscriptions) "OK" else "Cancel")
            }
        }
    )
}

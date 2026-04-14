package net.cynreub.subly.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.cynreub.subly.domain.model.Category

private val PRESET_EMOJIS = listOf(
    "📺", "🎵", "🎮", "📰", "🏋️", "🛒", "📦", "💼",
    "🏠", "🚗", "✈️", "🍔", "☕", "📱", "💻", "🌐",
    "🎓", "🏥", "💅", "🐾", "📷", "🎨", "🌿", "⚡"
)

private val PRESET_COLORS = listOf(
    "#E91E63", "#9C27B0", "#3F51B5", "#2196F3",
    "#00BCD4", "#009688", "#4CAF50", "#8BC34A",
    "#CDDC39", "#FFC107", "#FF9800", "#FF5722",
    "#795548", "#607D8B", "#F06292", "#CE93D8"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategorySheet(
    uiState: CategoriesUiState,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, colorHex: String) -> Unit
) {
    val editing = uiState.editingCategory

    var name by rememberSaveable(editing?.id) { mutableStateOf(editing?.displayName ?: "") }
    var emoji by rememberSaveable(editing?.id) { mutableStateOf(editing?.emoji ?: PRESET_EMOJIS[0]) }
    var colorHex by rememberSaveable(editing?.id) { mutableStateOf(editing?.colorHex ?: PRESET_COLORS[0]) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            Text(
                text = if (editing != null) "Edit Category" else "New Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                singleLine = true,
                isError = uiState.saveError != null,
                supportingText = {
                    uiState.saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Emoji picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Emoji",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                ) {
                    items(PRESET_EMOJIS) { e ->
                        val selected = e == emoji
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { emoji = e }
                                .padding(4.dp)
                        ) {
                            Text(text = e, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // Color picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                ) {
                    items(PRESET_COLORS) { hex ->
                        val selected = hex == colorHex
                        val parsedColor = runCatching { Color(android.graphics.Color.parseColor(hex)) }
                            .getOrDefault(Color.Gray)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }

            // Preview + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini preview chip
                val previewColor = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
                    .getOrDefault(Color.Gray)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(previewColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = name.ifBlank { "Preview" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = previewColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = { onSave(name, emoji, colorHex) },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (editing != null) "Update" else "Save")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(0.dp)) // bottom breathing room
        }
    }
}

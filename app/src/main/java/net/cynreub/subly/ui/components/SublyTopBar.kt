package net.cynreub.subly.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import net.cynreub.subly.ui.theme.ZillaSlabFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SublyTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Subly",
                fontFamily = ZillaSlabFontFamily,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

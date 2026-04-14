package net.cynreub.subly.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SublyShapes = Shapes(
    // Minimal rounding – inputs (0.75rem = 12dp)
    extraSmall = RoundedCornerShape(4.dp),
    // Cards and containers (0.5rem = 8dp minimum; prefer 2rem = 32dp)
    small = RoundedCornerShape(8.dp),
    // Default container rounding (1.5rem = 24dp for main dashboard cards)
    medium = RoundedCornerShape(16.dp),
    // Primary containers / subscription cards (2rem = 32dp)
    large = RoundedCornerShape(24.dp),
    // FABs and pill buttons (full = 9999dp)
    extraLarge = RoundedCornerShape(32.dp),
)

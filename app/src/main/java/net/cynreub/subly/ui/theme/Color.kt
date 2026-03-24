package net.cynreub.subly.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Light Theme ──────────────────────────────────────────────────────────────
// Primary (Teal) – action, growth, memberships
val LightPrimary = Color(0xFF006A60)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFF9DF2E5)
val LightOnPrimaryContainer = Color(0xFF00201D)

// Secondary (Blue) – stability, streaming
val LightSecondary = Color(0xFF0048D8)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFDCE1FF)
val LightOnSecondaryContainer = Color(0xFF001258)

// Tertiary (Purple) – creativity, software/SaaS
val LightTertiary = Color(0xFF7D2DD9)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFEEDCFF)
val LightOnTertiaryContainer = Color(0xFF26005D)

// Error
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

// Surface hierarchy (tonal layering, no explicit borders)
val LightBackground = Color(0xFFF7F9FB)
val LightOnBackground = Color(0xFF191C1E)
val LightSurface = Color(0xFFF7F9FB)
val LightOnSurface = Color(0xFF191C1E)           // "premium soft" – never pure black
val LightSurfaceVariant = Color(0xFFDAE5E2)
val LightOnSurfaceVariant = Color(0xFF3F4947)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)  // foreground cards
val LightSurfaceContainerLow = Color(0xFFF2F4F6)     // large background sections
val LightSurfaceContainer = Color(0xFFECEEF0)
val LightSurfaceContainerHigh = Color(0xFFE6E8EA)
val LightSurfaceContainerHighest = Color(0xFFE0E2E5)
val LightSurfaceBright = Color(0xFFF7F9FB)
val LightSurfaceDim = Color(0xFFD8DADD)

// Outline
val LightOutline = Color(0xFF6F7978)
val LightOutlineVariant = Color(0xFFBDCAC7)  // ghost border base

// Inverse
val LightInverseSurface = Color(0xFF2C3031)
val LightInverseOnSurface = Color(0xFFEFF1F1)
val LightInversePrimary = Color(0xFF5BF4DE)  // dark-mode primary as inverse
val LightScrim = Color(0xFF000000)

// ─── Dark Theme ───────────────────────────────────────────────────────────────
// Primary (Neon Teal)
val DarkPrimary = Color(0xFF5BF4DE)
val DarkOnPrimary = Color(0xFF003731)
val DarkPrimaryContainer = Color(0xFF11C9B4)
val DarkOnPrimaryContainer = Color(0xFF00504A)

// Secondary (Neon Blue)
val DarkSecondary = Color(0xFF699CFF)
val DarkOnSecondary = Color(0xFF001E80)
val DarkSecondaryContainer = Color(0xFF1A3060)
val DarkOnSecondaryContainer = Color(0xFFDCE1FF)

// Tertiary (Neon Purple)
val DarkTertiary = Color(0xFFC180FF)
val DarkOnTertiary = Color(0xFF3F0080)
val DarkTertiaryContainer = Color(0xFF5C1AAA)
val DarkOnTertiaryContainer = Color(0xFFEEDCFF)

// Error
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// Surface hierarchy (deep-space layers)
val DarkBackground = Color(0xFF060E20)
val DarkOnBackground = Color(0xFFDEE5FF)
val DarkSurface = Color(0xFF060E20)              // base void
val DarkOnSurface = Color(0xFFDEE5FF)            // off-white, no eye strain
val DarkSurfaceVariant = Color(0xFF1A2340)
val DarkOnSurfaceVariant = Color(0xFFA3AAC4)     // secondary data / metadata
val DarkSurfaceContainerLowest = Color(0xFF000000) // inset / carved inputs
val DarkSurfaceContainerLow = Color(0xFF091328)    // secondary sections
val DarkSurfaceContainer = Color(0xFF0F1A32)
val DarkSurfaceContainerHigh = Color(0xFF152038)
val DarkSurfaceContainerHighest = Color(0xFF192540) // interactive / floating cards
val DarkSurfaceBright = Color(0xFF1F2B49)           // surface-bright from spec
val DarkSurfaceDim = Color(0xFF060E20)

// Outline
val DarkOutline = Color(0xFF6D758C)
val DarkOutlineVariant = Color(0xFF40485D)  // ghost border base at 15% opacity

// Inverse
val DarkInverseSurface = Color(0xFFDEE4E4)
val DarkInverseOnSurface = Color(0xFF191C1C)
val DarkInversePrimary = Color(0xFF006A60)  // light-mode primary as inverse
val DarkScrim = Color(0xFF000000)

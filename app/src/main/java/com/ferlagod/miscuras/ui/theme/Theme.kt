package com.ferlagod.miscuras.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = Color(0xFF0277BD),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1F5FE),
    onSecondaryContainer = Color(0xFF01579B),

    background = SurfaceLight,
    onBackground = SlateDark,
    surface = CardBackground,
    onSurface = SlateDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = SlateMedium,
    outline = OutlineLight,

    error = ChipInfectionSelected,
    onError = Color.White,
    errorContainer = ChipInfection,
    onErrorContainer = ChipInfectionSelected
)

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = TealDark,
    primaryContainer = TealDark,
    onPrimaryContainer = TealLight,
    secondary = Color(0xFF29B6F6),
    onSecondary = Color(0xFF01579B),
    secondaryContainer = Color(0xFF01579B),
    onSecondaryContainer = Color(0xFFE1F5FE),

    background = SurfaceDark,
    onBackground = Color(0xFFECEFF1),
    surface = CardBackgroundDark,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCFD8DC),
    outline = OutlineDark,

    error = ChipInfectionSelectedDark,
    onError = Color(0xFF4A0000),
    errorContainer = ChipInfectionDark,
    onErrorContainer = ChipInfectionSelectedDark
)

@Composable
fun MisCurasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
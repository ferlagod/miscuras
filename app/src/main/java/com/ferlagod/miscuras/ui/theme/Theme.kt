package com.ferlagod.miscuras.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoLight,
    onPrimaryContainer = IndigoDark,

    background = SurfaceLight,
    onBackground = SlateDark,
    surface = SurfaceLight,
    onSurface = SlateDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = SlateDark,
    outline = OutlineLight,

    error = ChipInfectionSelected,
    onError = Color.White,
    errorContainer = ChipInfection,
    onErrorContainer = ChipInfectionDark
)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = IndigoDark,
    primaryContainer = IndigoPrimary,
    onPrimaryContainer = Color.White,

    background = SurfaceDark,
    onBackground = Color(0xFFECEFF1),
    surface = SurfaceDark,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCFD8DC),
    outline = OutlineDark,

    error = ChipInfectionSelectedDark,
    onError = Color.Black,
    errorContainer = ChipInfectionDark,
    onErrorContainer = ChipInfection
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
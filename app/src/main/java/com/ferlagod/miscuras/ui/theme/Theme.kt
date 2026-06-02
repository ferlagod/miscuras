/*
 * Mis Curas
 * Copyright (C) 2026 Fernando Lago (ferlagod)
 *
 * Este programa es software libre: puede redistribuirlo y/o modificarlo
 * bajo los términos de la Licencia Pública General GNU publicada por
 * la Free Software Foundation, ya sea la versión 3 de la Licencia, o
 * (a su elección) cualquier versión posterior.
 */
package com.ferlagod.miscuras.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BlueMedical40,
    onPrimary = Color.White,
    primaryContainer = BlueMedical90,
    onPrimaryContainer = BlueMedical10,

    secondary = GreenHealth40,
    onSecondary = Color.White,
    secondaryContainer = GreenHealth90,
    onSecondaryContainer = GreenHealth10,

    tertiary = AmberCaution40,
    onTertiary = Color.White,
    tertiaryContainer = AmberCaution90,
    onTertiaryContainer = AmberCaution10,

    error = RedAlert40,
    onError = Color.White,
    errorContainer = RedAlert90,
    onErrorContainer = RedAlert10,

    background = SurfaceLight,
    onBackground = BlueMedical10,
    surface = SurfaceLight,
    onSurface = BlueMedical10,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF43474E),
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueMedical80,
    onPrimary = BlueMedical20,
    primaryContainer = BlueMedical30,
    onPrimaryContainer = BlueMedical90,

    secondary = GreenHealth80,
    onSecondary = GreenHealth20,
    secondaryContainer = GreenHealth30,
    onSecondaryContainer = GreenHealth90,

    tertiary = AmberCaution80,
    onTertiary = AmberCaution20,
    tertiaryContainer = AmberCaution30,
    onTertiaryContainer = AmberCaution90,

    error = RedAlert80,
    onError = RedAlert20,
    errorContainer = RedAlert30,
    onErrorContainer = RedAlert90,

    background = SurfaceDark,
    onBackground = Color(0xFFE2E2E6),
    surface = SurfaceDark,
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC3C7CF),
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

@Composable
fun MisCurasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Usamos siempre la paleta médica propia, sin dynamic color,
    // para mantener la identidad visual de la app.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
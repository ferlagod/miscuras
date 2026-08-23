/*
 * Mis Curas
 * Copyright (C) Fernando Lago. 2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ferlagod.miscuras.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = OnBlueContainer,
    secondary = GreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = GreenContainer,
    onSecondaryContainer = OnGreenContainer,

    background = SurfaceLight,
    onBackground = BluePrimary, // Emphasize navy blue text
    surface = CardBackground,
    onSurface = BluePrimary, // Navy blue text on cards
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = BlueLight, // Slightly lighter blue for secondary text
    outline = OutlineLight,

    error = ChipInfectionSelected,
    onError = Color.White,
    errorContainer = ChipInfection,
    onErrorContainer = ChipInfectionSelected
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA), // Vibrant Blue
    onPrimary = Color(0xFF090E17), // Deep Navy Text
    primaryContainer = Color(0xFF1D4ED8), // Deep vibrant blue
    onPrimaryContainer = Color(0xFFEFF6FF), // White-blue text
    secondary = Color(0xFF34D399), // Vibrant Mint Green
    onSecondary = Color(0xFF090E17),
    secondaryContainer = Color(0xFF065F46), // Deep emerald
    onSecondaryContainer = Color(0xFFD1FAE5),

    background = SurfaceDark,
    onBackground = Color(0xFFF8FAFC), // Crisp white text
    surface = CardBackgroundDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1), // Slate 300 for secondary text
    outline = OutlineDark,

    error = ChipInfectionSelectedDark,
    onError = Color(0xFF450A0A),
    errorContainer = ChipInfectionDark,
    onErrorContainer = ChipInfectionSelectedDark
)

// Custom shapes for consistent rounded corners across the app
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
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
        shapes = AppShapes,
        content = content
    )
}
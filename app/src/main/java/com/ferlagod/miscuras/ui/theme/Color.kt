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

import androidx.compose.ui.graphics.Color

// === Premium Navy & Light Blue Palette (from Design Mockups) ===

// Primary — Deep Navy Blue
val BluePrimary = Color(0xFF002B5B) // Navy blue
val BlueDark = Color(0xFF001A38)
val BlueLight = Color(0xFF3A5A80) // For secondary text
val BlueContainer = Color(0xFFE6EDF5) // Very light blue for selected chips / cards
val OnBlueContainer = Color(0xFF002B5B)

// Secondary — Green (from Logo Leaf / Success states)
val GreenSecondary = Color(0xFF2E7D32)
val GreenContainer = Color(0xFFE8F5E9)
val OnGreenContainer = Color(0xFF1B5E20)

// Textos y Neutrales
val GreyDark = Color(0xFF1F1F1F)
val GreyMedium = Color(0xFF5E5E5E)
val GreyLight = Color(0xFFE0E0E0)

// Surfaces (Modo claro)
val SurfaceLight = Color(0xFFF9FAFB) // App background
val SurfaceVariantLight = Color(0xFFFFFFFF) // White cards
val OutlineLight = Color(0xFFE0E0E0)

// Surfaces (Modo oscuro) - Premium Dark Mode
val SurfaceDark = Color(0xFF0B1120) // Deep Navy/Slate background
val SurfaceVariantDark = Color(0xFF1E293B) // Slate 800 for unselected items
val OutlineDark = Color(0xFF334155) // Slate 700

// --- Chips Semánticos ---
// Tejido (Verde)
val ChipTissue = Color(0xFFE6F4EA)
val ChipTissueSelected = Color(0xFF1E8E3E)
val ChipTissueDark = Color(0xFF132A24) // Subtle green-tinted dark surface
val ChipTissueSelectedDark = Color(0xFF10B981) // Vibrant Emerald

// Exudado (Azul)
val ChipExudate = Color(0xFFE8F0FE)
val ChipExudateSelected = Color(0xFF1A73E8)
val ChipExudateDark = Color(0xFF12233D) // Subtle blue-tinted dark surface
val ChipExudateSelectedDark = Color(0xFF3B82F6) // Vibrant Blue

// Infección (Rojo)
val ChipInfection = Color(0xFFFCE8E6)
val ChipInfectionSelected = Color(0xFFD93025)
val ChipInfectionDark = Color(0xFF32181A) // Subtle red-tinted dark surface
val ChipInfectionSelectedDark = Color(0xFFEF4444) // Vibrant Red

// Bordes (Amarillo/Naranja)
val ChipEdge = Color(0xFFFEF7E0)
val ChipEdgeSelected = Color(0xFFF9AB00)
val ChipEdgeDark = Color(0xFF2D2214) // Subtle amber-tinted dark surface
val ChipEdgeSelectedDark = Color(0xFFF59E0B) // Vibrant Amber

// Colores especiales heredados
val CardBackground = Color(0xFFFFFFFF)
val CardBackgroundDark = Color(0xFF111827) // Elevación sutil para tarjetas en modo oscuro
val InfectionBadge = Color(0xFFD93025)
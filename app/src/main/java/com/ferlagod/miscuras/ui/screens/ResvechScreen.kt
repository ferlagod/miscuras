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
package com.ferlagod.miscuras.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.ferlagod.miscuras.R

private data class ResvechOption(val text: String, val points: Int)
private data class ResvechCategory(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val options: List<ResvechOption>,
    /** Ítem 6 (signos de infección/inflamación): cada signo presente suma 1 punto. */
    val multiSelect: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResvechScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // RESVECH 2.0 (Restrepo-Medrano JC, Verdú Soriano J, 2011). 6 ítems, rango 0-35.
    val categories = remember {
        listOf(
            ResvechCategory(
                title = "1. Dimensiones de la lesión",
                subtitle = "Superficie (largo x ancho mayores)",
                icon = Icons.Rounded.Straighten,
                options = listOf(
                    ResvechOption("Superficie = 0 cm²", 0),
                    ResvechOption("Superficie < 4 cm²", 1),
                    ResvechOption("Superficie = 4 – < 16 cm²", 2),
                    ResvechOption("Superficie = 16 – < 36 cm²", 3),
                    ResvechOption("Superficie = 36 – < 64 cm²", 4),
                    ResvechOption("Superficie = 64 – < 100 cm²", 5),
                    ResvechOption("Superficie ≥ 100 cm²", 6)
                )
            ),
            ResvechCategory(
                title = "2. Profundidad / Tejidos afectados",
                subtitle = "Estructura más profunda afectada",
                icon = Icons.Rounded.Layers,
                options = listOf(
                    ResvechOption("Piel intacta cicatrizada", 0),
                    ResvechOption("Afectación de la dermis-epidermis", 1),
                    ResvechOption("Afectación del tejido subcutáneo (tejido adiposo sin llegar a la fascia del músculo)", 2),
                    ResvechOption("Afectación del músculo", 3),
                    ResvechOption("Afectación del hueso y/o tejidos anexos (tendones, ligamentos, cápsula articular) o escara negra que no permite ver los tejidos debajo de ella", 4)
                )
            ),
            ResvechCategory(
                title = "3. Bordes",
                subtitle = "Estado de los bordes de la herida",
                icon = Icons.Rounded.CropFree,
                options = listOf(
                    ResvechOption("No distinguibles (no hay herida)", 0),
                    ResvechOption("Difusos", 1),
                    ResvechOption("Delimitados", 2),
                    ResvechOption("Dañados", 3),
                    ResvechOption("Engrosados (\"envejecidos\", \"evertidos\")", 4)
                )
            ),
            ResvechCategory(
                title = "4. Tipo de tejido en el lecho",
                subtitle = "Tejido de peor pronóstico presente",
                icon = Icons.Rounded.Healing,
                options = listOf(
                    ResvechOption("Cerrada / cicatrizada", 0),
                    ResvechOption("Tejido epitelial", 1),
                    ResvechOption("Tejido de granulación", 2),
                    ResvechOption("Tejido necrótico y/o esfacelos en el lecho", 3),
                    ResvechOption("Necrótico (escara negra seca o húmeda)", 4)
                )
            ),
            ResvechCategory(
                title = "5. Exudado",
                subtitle = "Nivel de humedad del lecho",
                icon = Icons.Rounded.WaterDrop,
                options = listOf(
                    ResvechOption("Húmedo", 0),
                    ResvechOption("Mojado", 1),
                    ResvechOption("Saturado", 2),
                    ResvechOption("Con fuga de exudado", 3),
                    ResvechOption("Seco", 3)
                )
            ),
            ResvechCategory(
                title = "6. Infección / Inflamación (signos de biofilm)",
                subtitle = "Marque todos los signos presentes (1 punto cada uno)",
                icon = Icons.Rounded.Coronavirus,
                multiSelect = true,
                options = listOf(
                    "Dolor que va en aumento",
                    "Eritema en la perilesión",
                    "Edema en la perilesión",
                    "Aumento de la temperatura",
                    "Exudado que va en aumento",
                    "Exudado purulento",
                    "Tejido friable o que sangra con facilidad",
                    "Herida estancada, que no progresa",
                    "Tejido compatible con biofilm",
                    "Olor",
                    "Hipergranulación",
                    "Aumento del tamaño de la herida",
                    "Lesiones satélite",
                    "Palidez del tejido"
                ).map { ResvechOption(it, 1) }
            )
        )
    }

    // Índice de la opción elegida en cada ítem de respuesta única (null = sin responder)
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    // Signos marcados en los ítems de selección múltiple: catIndex -> índices de opción
    val multiAnswers = remember { mutableStateMapOf<Int, Set<Int>>() }

    val totalScore = categories.indices.sumOf { catIndex ->
        val category = categories[catIndex]
        if (category.multiSelect) {
            (multiAnswers[catIndex] ?: emptySet()).sumOf { category.options[it].points }
        } else {
            selectedAnswers[catIndex]?.let { category.options[it].points } ?: 0
        }
    }
    val answeredItems = categories.indices.count { selectedAnswers.containsKey(it) || categories[it].multiSelect }
    val isComplete = answeredItems == categories.size

    // RESVECH 2.0 no define puntos de corte validados: la interpretación es evolutiva
    // (la puntuación debe descender hacia 0 a medida que la herida cicatriza).
    val (severityText, severityColor, severityDescription) = when {
        !isComplete -> Triple("Incompleta ($answeredItems/${categories.size} ítems)", Color(0xFF757575), "Responda todos los ítems para obtener la puntuación total de la escala.")
        totalScore == 0 -> Triple("Herida cicatrizada", Color(0xFF43A047), "Puntuación 0: lesión cerrada.")
        totalScore <= 11 -> Triple("Puntuación baja (orientativo)", Color(0xFF1E88E5), "Compare con valoraciones anteriores: un descenso de la puntuación indica evolución hacia la cicatrización.")
        totalScore <= 23 -> Triple("Puntuación intermedia (orientativo)", Color(0xFFFB8C00), "Compare con valoraciones anteriores. Si la puntuación no desciende, reevalúe el plan de cuidados (desbridamiento, carga bacteriana, exudado).")
        else -> Triple("Puntuación alta (orientativo)", Color(0xFFE53935), "Lesión con gran afectación. Si no mejora entre valoraciones, valore abordaje interdisciplinar y cura especializada.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Escala RESVECH 2.0", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Monitorización objetiva de cicatrización", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val summary = """
                            --- ESCALA RESVECH 2.0 ---
                            Puntuación Total: $totalScore / 35 puntos
                            Estado: $severityText
                            $severityDescription
                            (RESVECH 2.0 no tiene puntos de corte validados; las bandas son orientativas.)
                        """.trimIndent()
                        clipboardManager.setText(AnnotatedString(summary))
                        Toast.makeText(context, "Resumen copiado al portapapeles", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copiar resumen")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("PUNTUACIÓN TOTAL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "$totalScore / 35",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = severityColor
                                )
                            }

                            Surface(
                                color = severityColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = severityText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = severityColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { totalScore / 35f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = severityColor,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            severityDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Categories
            categories.forEachIndexed { catIndex, category ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    category.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(category.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(category.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            category.options.forEachIndexed { optIndex, option ->
                                val isSelected = if (category.multiSelect) {
                                    multiAnswers[catIndex]?.contains(optIndex) == true
                                } else {
                                    selectedAnswers[catIndex] == optIndex
                                }
                                val itemBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                val itemTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(itemBg)
                                        .clickable {
                                            if (category.multiSelect) {
                                                val current = multiAnswers[catIndex] ?: emptySet()
                                                multiAnswers[catIndex] = if (optIndex in current) current - optIndex else current + optIndex
                                            } else {
                                                selectedAnswers[catIndex] = optIndex
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        option.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = itemTextColor,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            "${option.points} pts",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

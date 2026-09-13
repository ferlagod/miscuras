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
    val options: List<ResvechOption>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResvechScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val categories = remember {
        listOf(
            ResvechCategory(
                title = "1. Dimensiones / Superficie",
                subtitle = "Área calculada de la lesión (Largo x Ancho)",
                icon = Icons.Rounded.Straighten,
                options = listOf(
                    ResvechOption("Cicatrizada / < 1 cm²", 0),
                    ResvechOption("1 a 4 cm²", 1),
                    ResvechOption("4 a 16 cm²", 2),
                    ResvechOption("16 a 36 cm²", 3),
                    ResvechOption("36 a 64 cm²", 4),
                    ResvechOption("64 a 100 cm²", 5),
                    ResvechOption("> 100 cm²", 6)
                )
            ),
            ResvechCategory(
                title = "2. Tejidos en el Lecho",
                subtitle = "Tipo de tejido predominante o de peor pronóstico",
                icon = Icons.Rounded.Layers,
                options = listOf(
                    ResvechOption("Tejido de epitelización", 0),
                    ResvechOption("Tejido de granulación limpio y brillante", 1),
                    ResvechOption("Esfacelos < 25% del lecho", 2),
                    ResvechOption("Esfacelos 25% - 75% del lecho", 3),
                    ResvechOption("Esfacelos > 75% del lecho", 4),
                    ResvechOption("Tejido necrótico / Escara < 25%", 5),
                    ResvechOption("Tejido necrótico / Escara > 25%", 6)
                )
            ),
            ResvechCategory(
                title = "3. Bordes de la Herida",
                subtitle = "Estado de los bordes perilesionales",
                icon = Icons.Rounded.CropFree,
                options = listOf(
                    ResvechOption("Difuso / Sano / En fase de avance epitelial", 0),
                    ResvechOption("Delimitado / Definido pero sin avance", 1),
                    ResvechOption("Dañado / Macerado por exceso de exudado", 2),
                    ResvechOption("Engrosado / Hiperqueratósico / Epibólico", 3),
                    ResvechOption("Socavado / Cavitado / Fondo de saco", 4)
                )
            ),
            ResvechCategory(
                title = "4. Exudado / Nivel de Humedad",
                subtitle = "Cantidad de fluido generado en 24h",
                icon = Icons.Rounded.WaterDrop,
                options = listOf(
                    ResvechOption("Nulo / Lecho seco", 0),
                    ResvechOption("Bajo / Humedad adecuada", 1),
                    ResvechOption("Moderado / Apósito saturado al 50%", 2),
                    ResvechOption("Alto / Muy abundante / Fuga o saturación rápida", 3)
                )
            ),
            ResvechCategory(
                title = "5. Infección / Carga Bacteriana",
                subtitle = "Signos de contaminación, colonización o infección activa",
                icon = Icons.Rounded.Coronavirus,
                options = listOf(
                    ResvechOption("Sin signos de infección", 0),
                    ResvechOption("Contaminación / Colonización normal", 1),
                    ResvechOption("Colonización crítica / Sospecha de Biofilm", 2),
                    ResvechOption("Infección local activa (calor, rubor, dolor, exudado purulento)", 3),
                    ResvechOption("Infección diseminada / Celulitis / Afectación sistémica", 5)
                )
            ),
            ResvechCategory(
                title = "6. Dolor Local (Escala EVA)",
                subtitle = "Intensidad del dolor referido por el paciente",
                icon = Icons.Rounded.SentimentDissatisfied,
                options = listOf(
                    ResvechOption("Sin dolor (EVA 0)", 0),
                    ResvechOption("Dolor leve (EVA 1 - 3)", 1),
                    ResvechOption("Dolor moderado (EVA 4 - 6)", 2),
                    ResvechOption("Dolor severo / Incontrolado (EVA 7 - 10)", 3)
                )
            ),
            ResvechCategory(
                title = "7. Piel Perilesional",
                subtitle = "Estado de los tejidos adyacentes a la lesión",
                icon = Icons.Rounded.Shield,
                options = listOf(
                    ResvechOption("Piel íntegra y sana", 0),
                    ResvechOption("Eritema no blanqueable o edema leve", 1),
                    ResvechOption("Maceración / Eccematización perilesional", 2),
                    ResvechOption("Descamación intensa / Fragilidad dérmica", 3),
                    ResvechOption("Induración / Calor o celulitis circundante", 4)
                )
            )
        )
    }

    val selectedAnswers = remember { mutableStateMapOf<Int, Int>().apply {
        categories.indices.forEach { this[it] = 0 }
    }}

    val totalScore = selectedAnswers.values.sum()

    val (severityText, severityColor, severityDescription) = when {
        totalScore <= 5 -> Triple("Excelente evolución / Fase final", Color(0xFF43A047), "La lesión muestra signos de cicatrización avanzada. Priorizar protección y apósitos no adherentes.")
        totalScore in 6..14 -> Triple("Evolución Favorable", Color(0xFF1E88E5), "Preparación del lecho adecuada. Mantener cura en ambiente húmedo y control de factores predisponentes.")
        totalScore in 15..24 -> Triple("Estancamiento / Riesgo Moderado", Color(0xFFFB8C00), "Carga de exudado o esfacelos elevada. Reevaluar desbridamiento, protección de bordes y control de carga bacteriana.")
        else -> Triple("Herida Compleja / Alto Riesgo", Color(0xFFE53935), "Carga bacteriana elevada, tejido necrótico o exudado severo. Requiere abordaje interdisciplinar, cura especializada y valoración médica.")
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

                            category.options.forEach { option ->
                                val isSelected = selectedAnswers[catIndex] == option.points
                                val itemBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                val itemTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(itemBg)
                                        .clickable { selectedAnswers[catIndex] = option.points }
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

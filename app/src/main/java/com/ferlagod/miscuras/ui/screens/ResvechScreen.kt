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
import androidx.compose.ui.res.stringResource

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
    onBackClick: () -> Unit,
    /** Si se indica, la escala se ha abierto desde una evaluación y su puntuación se puede guardar en ella. */
    onScoreCalculated: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // RESVECH 2.0 (Restrepo-Medrano JC, Verdú Soriano J, 2011). 6 ítems, rango 0-35.
    val categories = remember {
        listOf(
            ResvechCategory(
                title = context.getString(R.string.resvech_c1_title),
                subtitle = context.getString(R.string.resvech_c1_sub),
                icon = Icons.Rounded.Straighten,
                options = listOf(
                    ResvechOption(context.getString(R.string.resvech_c1_o0), 0),
                    ResvechOption(context.getString(R.string.resvech_c1_o1), 1),
                    ResvechOption(context.getString(R.string.resvech_c1_o2), 2),
                    ResvechOption(context.getString(R.string.resvech_c1_o3), 3),
                    ResvechOption(context.getString(R.string.resvech_c1_o4), 4),
                    ResvechOption(context.getString(R.string.resvech_c1_o5), 5),
                    ResvechOption(context.getString(R.string.resvech_c1_o6), 6)
                )
            ),
            ResvechCategory(
                title = context.getString(R.string.resvech_c2_title),
                subtitle = context.getString(R.string.resvech_c2_sub),
                icon = Icons.Rounded.Layers,
                options = listOf(
                    ResvechOption(context.getString(R.string.resvech_c2_o0), 0),
                    ResvechOption(context.getString(R.string.resvech_c2_o1), 1),
                    ResvechOption(context.getString(R.string.resvech_c2_o2), 2),
                    ResvechOption(context.getString(R.string.resvech_c2_o3), 3),
                    ResvechOption(context.getString(R.string.resvech_c2_o4), 4)
                )
            ),
            ResvechCategory(
                title = context.getString(R.string.resvech_c3_title),
                subtitle = context.getString(R.string.resvech_c3_sub),
                icon = Icons.Rounded.CropFree,
                options = listOf(
                    ResvechOption(context.getString(R.string.resvech_c3_o0), 0),
                    ResvechOption(context.getString(R.string.resvech_c3_o1), 1),
                    ResvechOption(context.getString(R.string.resvech_c3_o2), 2),
                    ResvechOption(context.getString(R.string.resvech_c3_o3), 3),
                    ResvechOption(context.getString(R.string.resvech_c3_o4), 4)
                )
            ),
            ResvechCategory(
                title = context.getString(R.string.resvech_c4_title),
                subtitle = context.getString(R.string.resvech_c4_sub),
                icon = Icons.Rounded.Healing,
                options = listOf(
                    ResvechOption(context.getString(R.string.resvech_c4_o0), 0),
                    ResvechOption(context.getString(R.string.resvech_c4_o1), 1),
                    ResvechOption(context.getString(R.string.resvech_c4_o2), 2),
                    ResvechOption(context.getString(R.string.resvech_c4_o3), 3),
                    ResvechOption(context.getString(R.string.resvech_c4_o4), 4)
                )
            ),
            ResvechCategory(
                title = context.getString(R.string.resvech_c5_title),
                subtitle = context.getString(R.string.resvech_c5_sub),
                icon = Icons.Rounded.WaterDrop,
                options = listOf(
                    ResvechOption(context.getString(R.string.resvech_c5_o0), 0),
                    ResvechOption(context.getString(R.string.resvech_c5_o1), 1),
                    ResvechOption(context.getString(R.string.resvech_c5_o2), 2),
                    ResvechOption(context.getString(R.string.resvech_c5_o3), 3),
                    ResvechOption(context.getString(R.string.resvech_c5_o4), 3)
                )
            ),
            ResvechCategory(
                title = context.getString(R.string.resvech_c6_title),
                subtitle = context.getString(R.string.resvech_c6_sub),
                icon = Icons.Rounded.Coronavirus,
                multiSelect = true,
                options = listOf(
                    context.getString(R.string.resvech_s1),
                    context.getString(R.string.resvech_s2),
                    context.getString(R.string.resvech_s3),
                    context.getString(R.string.resvech_s4),
                    context.getString(R.string.resvech_s5),
                    context.getString(R.string.resvech_s6),
                    context.getString(R.string.resvech_s7),
                    context.getString(R.string.resvech_s8),
                    context.getString(R.string.resvech_s9),
                    context.getString(R.string.resvech_s10),
                    context.getString(R.string.resvech_s11),
                    context.getString(R.string.resvech_s12),
                    context.getString(R.string.resvech_s13),
                    context.getString(R.string.resvech_s14)
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
        !isComplete -> Triple(stringResource(R.string.resvech_incomplete, answeredItems, categories.size), Color(0xFF757575), stringResource(R.string.resvech_incomplete_desc))
        totalScore == 0 -> Triple(stringResource(R.string.resvech_healed), Color(0xFF43A047), stringResource(R.string.resvech_healed_desc))
        totalScore <= 11 -> Triple(stringResource(R.string.resvech_low), Color(0xFF1E88E5), stringResource(R.string.resvech_low_desc))
        totalScore <= 23 -> Triple(stringResource(R.string.resvech_mid), Color(0xFFFB8C00), stringResource(R.string.resvech_mid_desc))
        else -> Triple(stringResource(R.string.resvech_high), Color(0xFFE53935), stringResource(R.string.resvech_high_desc))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.cd_resvech), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.resvech_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val summary = context.getString(R.string.resvech_copy_summary, totalScore, severityText, severityDescription)
                        clipboardManager.setText(AnnotatedString(summary))
                        Toast.makeText(context, context.getString(R.string.copy_summary_toast), Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = stringResource(R.string.cd_copy_summary))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (onScoreCalculated != null) {
                Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                    Button(
                        onClick = {
                            onScoreCalculated(totalScore)
                            onBackClick()
                        },
                        enabled = isComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (isComplete) stringResource(R.string.resvech_use_in_evaluation, totalScore)
                            else stringResource(R.string.resvech_incomplete, answeredItems, categories.size)
                        )
                    }
                }
            }
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
                                Text(stringResource(R.string.resvech_total_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
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

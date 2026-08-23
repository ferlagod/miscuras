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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.ferlagod.miscuras.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BradenScreen(
    onBackClick: () -> Unit,
    onScoreCalculated: (Int) -> Unit,
    ) {
    var percepcion by remember { mutableStateOf(4) }
    var humedad by remember { mutableStateOf(4) }
    var actividad by remember { mutableStateOf(4) }
    var movilidad by remember { mutableStateOf(4) }
    var nutricion by remember { mutableStateOf(4) }
    var roce by remember { mutableStateOf(3) }

    val totalScore = percepcion + humedad + actividad + movilidad + nutricion + roce

    LaunchedEffect(totalScore) {
        onScoreCalculated(totalScore)
    }

    val riskLevel = when {
        totalScore >= 15 -> stringResource(R.string.braden_risk_low)
        totalScore in 13..14 -> stringResource(R.string.braden_risk_moderate)
        totalScore in 10..12 -> stringResource(R.string.braden_risk_high)
        else -> stringResource(R.string.braden_risk_very_high)
    }

    val colorRisk = when {
        totalScore >= 15 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        totalScore in 13..14 -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.error
    }

    val recommendations = when {
        totalScore >= 15 -> stringResource(R.string.braden_rec_low)
        totalScore in 13..14 -> stringResource(R.string.braden_rec_moderate)
        totalScore in 10..12 -> stringResource(R.string.braden_rec_high)
        else -> stringResource(R.string.braden_rec_very_high)
    }

    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var showSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.braden_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.braden_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSnackbar = false }) { Text(stringResource(R.string.braden_ok)) }
                    }
                ) { Text(stringResource(R.string.braden_copied_snackbar)) }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resultado de riesgo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$totalScore ${stringResource(R.string.braden_points)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorRisk
                    )
                    Text(
                        text = riskLevel,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorRisk
                    )
                }
            }

            // Categorías
            BradenCategory(
                title = stringResource(R.string.braden_sensory),
                options = listOf(stringResource(R.string.braden_sensory1), stringResource(R.string.braden_sensory2), stringResource(R.string.braden_sensory3), stringResource(R.string.braden_sensory4)),
                selectedValue = percepcion,
                onSelected = { percepcion = it }
            )
            BradenCategory(
                title = stringResource(R.string.braden_moisture),
                options = listOf(stringResource(R.string.braden_moisture1), stringResource(R.string.braden_moisture2), stringResource(R.string.braden_moisture3), stringResource(R.string.braden_moisture4)),
                selectedValue = humedad,
                onSelected = { humedad = it }
            )
            BradenCategory(
                title = stringResource(R.string.braden_activity),
                options = listOf(stringResource(R.string.braden_activity1), stringResource(R.string.braden_activity2), stringResource(R.string.braden_activity3), stringResource(R.string.braden_activity4)),
                selectedValue = actividad,
                onSelected = { actividad = it }
            )
            BradenCategory(
                title = stringResource(R.string.braden_mobility),
                options = listOf(stringResource(R.string.braden_mobility1), stringResource(R.string.braden_mobility2), stringResource(R.string.braden_mobility3), stringResource(R.string.braden_mobility4)),
                selectedValue = movilidad,
                onSelected = { movilidad = it }
            )
            BradenCategory(
                title = stringResource(R.string.braden_nutrition),
                options = listOf(stringResource(R.string.braden_nutrition1), stringResource(R.string.braden_nutrition2), stringResource(R.string.braden_nutrition3), stringResource(R.string.braden_nutrition4)),
                selectedValue = nutricion,
                onSelected = { nutricion = it }
            )
            BradenCategory(
                title = stringResource(R.string.braden_friction),
                options = listOf(stringResource(R.string.braden_friction1), stringResource(R.string.braden_friction2), stringResource(R.string.braden_friction3)),
                selectedValue = roce,
                onSelected = { roce = it }
            )

            // Recomendaciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.braden_suggested_plan),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = recommendations,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val repBradenTitle = stringResource(R.string.rep_braden_title)
                    val repBradenScoreFormat = stringResource(R.string.rep_braden_score_format)
                    val bradenSuggestedPlan = stringResource(R.string.braden_suggested_plan)
                    Button(
                        onClick = {
                            val text = "$repBradenTitle\n" +
                                       "${String.format(repBradenScoreFormat, totalScore, riskLevel)}\n\n" +
                                       "$bradenSuggestedPlan\n$recommendations"
                            coroutineScope.launch {
                                clipboard?.setText(AnnotatedString(text))
                                showSnackbar = true
                                kotlinx.coroutines.delay(2000)
                                showSnackbar = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.braden_copy_clipboard))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BradenCategory(
    title: String,
    options: List<String>,
    selectedValue: Int,
    onSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEachIndexed { index, option ->
                    val value = index + 1
                    val isSelected = value == selectedValue
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    val borderColor = if (isSelected) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(50))
                            .clickable { onSelected(value) }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

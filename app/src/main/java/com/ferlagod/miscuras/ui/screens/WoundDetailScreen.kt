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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ferlagod.miscuras.ui.viewmodels.PatientViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.ferlagod.miscuras.R
import com.ferlagod.miscuras.data.entities.EvaluationEntity
import java.text.SimpleDateFormat
import java.util.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.res.stringResource
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WoundDetailScreen(
    woundId: Long,
    patientViewModel: PatientViewModel,
    onBackClick: () -> Unit,
    onNewEvaluationClick: (Long) -> Unit
) {
    val wound by patientViewModel.currentWound.collectAsState()
    val evaluations by patientViewModel.currentWoundEvaluations.collectAsState()

    val modelProducer = remember { ChartEntryModelProducer() }
    
    val context = LocalContext.current
        
    LaunchedEffect(woundId) {
        patientViewModel.loadEvaluationsForWound(woundId)
    }

    LaunchedEffect(evaluations) {
        if (evaluations.isNotEmpty()) {
            val areas = evaluations.mapIndexedNotNull { index, eval ->
                val l = eval.length.toFloatOrNull() ?: 0f
                val w = eval.width.toFloatOrNull() ?: 0f
                if (l > 0f && w > 0f) FloatEntry(x = index.toFloat(), y = l * w) else null
            }
            if (areas.isNotEmpty()) {
                modelProducer.setEntries(areas)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        wound?.name ?: stringResource(R.string.wound_detail_title_fallback),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    if (wound?.isDischarged == false) {
                        TextButton(onClick = { 
                            wound?.id?.let { patientViewModel.dischargeWound(it) }
                            onBackClick()
                        }) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.action_discharge_wound))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (wound?.isDischarged == false) {
                ExtendedFloatingActionButton(
                    onClick = { onNewEvaluationClick(woundId) },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.evaluate_btn)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    ) { paddingValues ->
        if (evaluations.isEmpty()) {
            // === Empty State ===
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.Url("https://lottie.host/c4d6ed1a-69da-4f94-adf0-62a774e63b10/WKoRcxUJt0.lottie")
                    )
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(160.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.empty_evaluations_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.empty_evaluations_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // === Content with chart + timeline ===
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Chart Card
                item {
                    ChartCard(
                        evaluations = evaluations,
                        modelProducer = modelProducer
                    )
                }

                // Timeline Header
                item {
                    Text(
                        stringResource(R.string.eval_history_title),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Timeline Evaluations
                val reversedEvals = evaluations.reversed()
                itemsIndexed(reversedEvals, key = { _, e -> e.id }) { index, eval ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(eval.id) {
                        delay(index * 60L)
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInVertically(
                                    initialOffsetY = { it / 4 },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                    ) {
                        TimelineEvaluationItem(
                            eval = eval,
                            isFirst = index == 0,
                            isLast = index == reversedEvals.lastIndex
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Chart Card — Premium
// ============================================================

@Composable
private fun ChartCard(
    evaluations: List<EvaluationEntity>,
    modelProducer: ChartEntryModelProducer
) {
    val hasChartData = evaluations.any { 
        (it.length.toFloatOrNull() ?: 0f) > 0f && (it.width.toFloatOrNull() ?: 0f) > 0f 
    }

    if (!hasChartData) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.TrendingDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.size_evolution_chart_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val evalChartLabel = stringResource(R.string.eval_chart_label)
            Chart(
                chart = lineChart(),
                chartModelProducer = modelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = { value, _ -> "$evalChartLabel ${value.toInt() + 1}" }),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

// ============================================================
// Timeline Evaluation Item
// ============================================================

@Composable
private fun TimelineEvaluationItem(
    eval: EvaluationEntity,
    isFirst: Boolean,
    isLast: Boolean
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(eval.timestamp))
    val timelineColor = MaterialTheme.colorScheme.primary
    val dotColor = if (isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
    ) {
        // Timeline line + dot
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(32.dp)
            ) {
                val dotRadius = 6.dp.toPx()
                val centerX = size.width / 2
                val dotCenterY = dotRadius + 16.dp.toPx()

                // Line above dot
                if (!isFirst) {
                    drawLine(
                        color = timelineColor.copy(alpha = 0.3f),
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, dotCenterY - dotRadius),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Dot
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(centerX, dotCenterY)
                )

                // Line below dot
                if (!isLast) {
                    drawLine(
                        color = timelineColor.copy(alpha = 0.3f),
                        start = Offset(centerX, dotCenterY + dotRadius),
                        end = Offset(centerX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        // Evaluation Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Date header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateStr,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (isFirst) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = stringResource(R.string.latest_evaluation),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clinical chips row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EvalChip(
                        text = eval.bedState,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    EvalChip(
                        text = eval.exudateLevel,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (eval.infection) {
                        EvalChip(
                            text = "⚠ ${eval.infectionGerm}",
                            color = MaterialTheme.colorScheme.errorContainer,
                            textColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Size info
                if (eval.length.isNotEmpty() && eval.width.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(
                            stringResource(R.string.eval_size_format),
                            eval.length, eval.width,
                            if (eval.depth.isNotEmpty()) "x ${eval.depth}" else ""
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Treatment
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format(stringResource(R.string.eval_treatment_format), eval.recommendedTreatment),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Selected products
                if (!eval.selectedProducts.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val productsList = eval.selectedProducts.split(",")
                    productsList.forEach { product ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = product.trim(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Photo
                if (eval.photoPath != null && eval.photoPath.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = File(eval.photoPath),
                        contentDescription = stringResource(R.string.photo_evolution_desc),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

// ============================================================
// Evaluation Chip
// ============================================================

@Composable
private fun EvalChip(
    text: String,
    color: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

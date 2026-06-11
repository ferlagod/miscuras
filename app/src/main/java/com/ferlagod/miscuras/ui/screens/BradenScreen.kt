package com.ferlagod.miscuras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import com.ferlagod.miscuras.ui.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BradenScreen(
    onBackClick: () -> Unit,
    onScoreCalculated: (Int) -> Unit,
    strings: AppStrings
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
        totalScore >= 15 -> strings.bradenRiskLow
        totalScore in 13..14 -> strings.bradenRiskModerate
        totalScore in 10..12 -> strings.bradenRiskHigh
        else -> strings.bradenRiskVeryHigh
    }

    val colorRisk = when {
        totalScore >= 15 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        totalScore in 13..14 -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.error
    }

    val recommendations = when {
        totalScore >= 15 -> strings.bradenRecLow
        totalScore in 13..14 -> strings.bradenRecModerate
        totalScore in 10..12 -> strings.bradenRecHigh
        else -> strings.bradenRecVeryHigh
    }

    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var showSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.bradenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.bradenBack)
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
                        TextButton(onClick = { showSnackbar = false }) { Text(strings.bradenOk) }
                    }
                ) { Text(strings.bradenCopiedSnackbar) }
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
                        text = "$totalScore ${strings.bradenPoints}",
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
                title = strings.bradenSensory,
                options = listOf(strings.bradenSensory1, strings.bradenSensory2, strings.bradenSensory3, strings.bradenSensory4),
                selectedValue = percepcion,
                onSelected = { percepcion = it }
            )
            BradenCategory(
                title = strings.bradenMoisture,
                options = listOf(strings.bradenMoisture1, strings.bradenMoisture2, strings.bradenMoisture3, strings.bradenMoisture4),
                selectedValue = humedad,
                onSelected = { humedad = it }
            )
            BradenCategory(
                title = strings.bradenActivity,
                options = listOf(strings.bradenActivity1, strings.bradenActivity2, strings.bradenActivity3, strings.bradenActivity4),
                selectedValue = actividad,
                onSelected = { actividad = it }
            )
            BradenCategory(
                title = strings.bradenMobility,
                options = listOf(strings.bradenMobility1, strings.bradenMobility2, strings.bradenMobility3, strings.bradenMobility4),
                selectedValue = movilidad,
                onSelected = { movilidad = it }
            )
            BradenCategory(
                title = strings.bradenNutrition,
                options = listOf(strings.bradenNutrition1, strings.bradenNutrition2, strings.bradenNutrition3, strings.bradenNutrition4),
                selectedValue = nutricion,
                onSelected = { nutricion = it }
            )
            BradenCategory(
                title = strings.bradenFriction,
                options = listOf(strings.bradenFriction1, strings.bradenFriction2, strings.bradenFriction3),
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
                        text = strings.bradenSuggestedPlan,
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
                    Button(
                        onClick = {
                            val text = "${strings.repBradenTitle}\n" +
                                       "${String.format(strings.repBradenScoreFormat, totalScore, riskLevel)}\n\n" +
                                       "${strings.bradenSuggestedPlan}\n$recommendations"
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
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.bradenCopyClipboard)
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

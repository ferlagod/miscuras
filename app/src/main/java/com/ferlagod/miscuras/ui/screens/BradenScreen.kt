package com.ferlagod.miscuras.ui.screens

import androidx.compose.foundation.background
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
        totalScore >= 15 -> "Riesgo Bajo / Sin Riesgo"
        totalScore in 13..14 -> "Riesgo Moderado"
        totalScore in 10..12 -> "Riesgo Alto"
        else -> "Riesgo Muy Alto"
    }

    val colorRisk = when {
        totalScore >= 15 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        totalScore in 13..14 -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.error
    }

    val recommendations = when {
        totalScore >= 15 -> "• Cuidados básicos de la piel.\n• Fomentar la movilidad."
        totalScore in 13..14 -> "• Cambios posturales regulares.\n• Aplicación de Ácidos Grasos Hiperoxigenados (AGHO).\n• Vigilancia estrecha de puntos de apoyo."
        totalScore in 10..12 -> "• Cambios posturales cada 2-3 horas.\n• Uso de Superficies Especiales de Manejo de la Presión (SEMP) estáticas/dinámicas.\n• AGHO diarios.\n• Suplementación nutricional si procede."
        else -> "• Cambios posturales estrictos cada 2 horas.\n• Uso de SEMP dinámicas de alta gama (colchón de aire alternante).\n• Elevación de talones.\n• Protección proactiva con apósitos multicapa."
    }

    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var showSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escala de Braden") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                        TextButton(onClick = { showSnackbar = false }) { Text("OK") }
                    }
                ) { Text("Plan preventivo copiado al portapapeles") }
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
                        text = "$totalScore Puntos",
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
                title = "Percepción Sensorial",
                options = listOf("Completamente limitada (1)", "Muy limitada (2)", "Ligeramente limitada (3)", "Sin limitación (4)"),
                selectedValue = percepcion,
                onSelected = { percepcion = it }
            )
            BradenCategory(
                title = "Exposición a la Humedad",
                options = listOf("Constantemente húmeda (1)", "A menudo húmeda (2)", "Ocasionalmente húmeda (3)", "Raramente húmeda (4)"),
                selectedValue = humedad,
                onSelected = { humedad = it }
            )
            BradenCategory(
                title = "Actividad",
                options = listOf("Encamado (1)", "En silla (2)", "Deambula ocasionalmente (3)", "Deambula frecuentemente (4)"),
                selectedValue = actividad,
                onSelected = { actividad = it }
            )
            BradenCategory(
                title = "Movilidad",
                options = listOf("Completamente inmóvil (1)", "Muy limitada (2)", "Ligeramente limitada (3)", "Sin limitaciones (4)"),
                selectedValue = movilidad,
                onSelected = { movilidad = it }
            )
            BradenCategory(
                title = "Nutrición",
                options = listOf("Muy pobre (1)", "Probablemente inadecuada (2)", "Adecuada (3)", "Excelente (4)"),
                selectedValue = nutricion,
                onSelected = { nutricion = it }
            )
            BradenCategory(
                title = "Roce y Peligro de Lesiones",
                options = listOf("Problema (1)", "Problema potencial (2)", "No hay problema aparente (3)"),
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
                        text = "Plan Preventivo Sugerido:",
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
                            val text = "[VALORACIÓN DE RIESGO DE UPP - BRADEN]\n" +
                                       "- Puntuación: $totalScore/23 ($riskLevel)\n\n" +
                                       "[PLAN PREVENTIVO]\n$recommendations"
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
                        Text("Copiar al Portapapeles")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BradenCategory(
    title: String,
    options: List<String>,
    selectedValue: Int,
    onSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                val value = index + 1
                val isSelected = value == selectedValue
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .clickable { onSelected(value) }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelected(value) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

package com.ferlagod.miscuras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ferlagod.miscuras.ui.viewmodels.PatientViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.ferlagod.miscuras.R
import java.text.SimpleDateFormat
import java.util.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource

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
    val prefs = context.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
    val lang = prefs.getString("language", "es") ?: "es"
        
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_symbol),
                            contentDescription = "Logo Mis Curas",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(wound?.name ?: stringResource(R.string.wound_detail_title_fallback)) 
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNewEvaluationClick(woundId) },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.evaluate_btn)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (evaluations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        stringResource(R.string.no_evaluations_msg),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Gráfica
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.size_evolution_chart_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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

                Text(
                    stringResource(R.string.eval_history_title),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(evaluations.reversed()) { eval ->
                        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(eval.timestamp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(date, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                val sizeText = if (eval.length.isNotEmpty() && eval.width.isNotEmpty()) {
                                    String.format(stringResource(R.string.eval_size_format), eval.length, eval.width, if(eval.depth.isNotEmpty()) "x ${eval.depth}" else "")
                                } else {
                                    "Tamaño: No medido"
                                }
                                Text(sizeText)
                                Text(String.format(stringResource(R.string.eval_bed_format), eval.bedState))
                                Text(String.format(stringResource(R.string.eval_exudate_format), eval.exudateLevel, eval.exudateType))
                                if (eval.infection) {
                                    Text(String.format(stringResource(R.string.eval_infection_format), eval.infectionGerm), color = MaterialTheme.colorScheme.error)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(String.format(stringResource(R.string.eval_treatment_format), eval.recommendedTreatment), fontWeight = FontWeight.SemiBold)
                                
                                if (!eval.selectedProducts.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Productos Seleccionados:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    val productsList = eval.selectedProducts.split(",")
                                    productsList.forEach { product ->
                                        Text("• ${product.trim()}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                
                                if (eval.photoPath != null && eval.photoPath.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    AsyncImage(
                                        model = File(eval.photoPath),
                                        contentDescription = stringResource(R.string.photo_evolution_desc),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
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

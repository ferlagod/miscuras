package com.ferlagod.miscuras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.ar.core.Pose
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArNode
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARMeasureScreen(
    onBackClick: () -> Unit,
    onMeasured: (Float, Float) -> Unit
) {
    val points = remember { mutableStateListOf<Pose>() }
    val nodes = remember { mutableStateListOf<ArNode>() }

    var lengthCm by remember { mutableStateOf(0f) }
    var widthCm by remember { mutableStateOf(0f) }
    
    var engine by remember { mutableStateOf<com.google.android.filament.Engine?>(null) }

    val instructionText = when (points.size) {
        0 -> "Mueve el móvil para detectar la superficie.\nToca para fijar el inicio del LARGO."
        1 -> "Toca para fijar el fin del LARGO."
        2 -> "Largo: ${String.format(java.util.Locale.US, "%.1f", lengthCm)} cm.\nToca para fijar el inicio del ANCHO."
        3 -> "Toca para fijar el fin del ANCHO."
        else -> "Largo: ${String.format(java.util.Locale.US, "%.1f", lengthCm)} cm | Ancho: ${String.format(java.util.Locale.US, "%.1f", widthCm)} cm.\nPulsa Confirmar."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Regla AR") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (points.size == 4) {
                FloatingActionButton(
                    onClick = { onMeasured(lengthCm, widthCm) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar", tint = Color.White)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ARScene(
                modifier = Modifier.fillMaxSize(),
                nodes = nodes,
                planeRenderer = true,
                onCreate = { arSceneView ->
                    engine = arSceneView.engine
                    arSceneView.lightEstimationMode = com.google.ar.core.Config.LightEstimationMode.DISABLED
                },
                onTap = { hitResult ->
                    if (points.size < 4 && engine != null) {
                        val anchor = hitResult.createAnchor()
                        points.add(anchor.pose)
                        val node = ArNode(engine!!, anchor)
                        nodes.add(node)

                        if (points.size == 2) {
                            lengthCm = calculateDistanceCm(points[0], points[1])
                        } else if (points.size == 4) {
                            widthCm = calculateDistanceCm(points[2], points[3])
                        }
                    }
                }
            )

            // Overlay de instrucciones
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                Text(
                    text = instructionText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Botón de reiniciar
            if (points.isNotEmpty()) {
                Button(
                    onClick = {
                        points.clear()
                        nodes.clear()
                        lengthCm = 0f
                        widthCm = 0f
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reiniciar")
                }
            }
        }
    }
}

fun calculateDistanceCm(pose1: Pose, pose2: Pose): Float {
    val dx = pose1.tx() - pose2.tx()
    val dy = pose1.ty() - pose2.ty()
    val dz = pose1.tz() - pose2.tz()
    val distanceMeters = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    return distanceMeters * 100f // Convertir a centímetros
}

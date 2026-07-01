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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.magnifier
import androidx.compose.ui.geometry.isSpecified
import io.github.sceneview.ar.ArSceneView
import kotlin.math.sqrt
import androidx.compose.ui.res.stringResource
import com.ferlagod.miscuras.R

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
    var arView by remember { mutableStateOf<ArSceneView?>(null) }
    
    var magnifierCenter by remember { mutableStateOf(Offset.Unspecified) }
    var showMagnifier by remember { mutableStateOf(false) }

    val instructionText = when (points.size) {
        0 -> stringResource(R.string.ar_instruction_start_length)
        1 -> stringResource(R.string.ar_instruction_end_length)
        2 -> String.format(java.util.Locale.US, stringResource(R.string.ar_instruction_start_width), String.format(java.util.Locale.US, "%.1f", lengthCm))
        3 -> stringResource(R.string.ar_instruction_end_width)
        else -> String.format(java.util.Locale.US, stringResource(R.string.ar_instruction_confirm), String.format(java.util.Locale.US, "%.1f", lengthCm), String.format(java.util.Locale.US, "%.1f", widthCm))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ar_ruler_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ar_cancel_button))
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
                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.confirm_button), tint = Color.White)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            magnifierCenter = offset
                            showMagnifier = true
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            magnifierCenter += dragAmount
                        },
                        onDragEnd = {
                            showMagnifier = false
                            if (points.size < 4 && engine != null && magnifierCenter.isSpecified) {
                                val hitResult = arView?.currentFrame?.hitTest(magnifierCenter.x, magnifierCenter.y)
                                if (hitResult != null) {
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
                            magnifierCenter = Offset.Unspecified
                        },
                        onDragCancel = {
                            showMagnifier = false
                            magnifierCenter = Offset.Unspecified
                        }
                    )
                }
                .then(
                    if (showMagnifier && android.os.Build.VERSION.SDK_INT >= 28 && magnifierCenter.isSpecified) {
                        Modifier.magnifier(
                            sourceCenter = { magnifierCenter },
                            magnifierCenter = { magnifierCenter - Offset(0f, 200f) },
                            zoom = 2f
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            ARScene(
                modifier = Modifier.fillMaxSize(),
                nodes = nodes,
                planeRenderer = true,
                onCreate = { arSceneView ->
                    engine = arSceneView.engine
                    arView = arSceneView
                    arSceneView.lightEstimationMode = com.google.ar.core.Config.LightEstimationMode.DISABLED
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
                    Text(stringResource(R.string.ar_restart_button))
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

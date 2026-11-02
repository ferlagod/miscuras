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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.ferlagod.miscuras.R
import com.ferlagod.miscuras.data.entities.EvaluationEntity
import com.ferlagod.miscuras.ui.ClinicalTermMapper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Zoom y desplazamiento compartidos por las dos fotos para compararlas a la misma escala. */
private class ZoomState {
    var scale by mutableFloatStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
    fun reset() { scale = 1f; offset = Offset.Zero }
}

private fun Modifier.zoomable(state: ZoomState): Modifier = this
    .pointerInput(state) { detectTapGestures(onDoubleTap = { state.reset() }) }

@Composable
private fun rememberTransform(state: ZoomState) = rememberTransformableState { zoomChange, panChange, _ ->
    state.scale = (state.scale * zoomChange).coerceIn(1f, 5f)
    state.offset = if (state.scale == 1f) Offset.Zero else state.offset + panChange
}

private fun Modifier.applyZoom(state: ZoomState): Modifier = graphicsLayer {
    scaleX = state.scale
    scaleY = state.scale
    translationX = state.offset.x
    translationY = state.offset.y
}

/**
 * Comparador de la evolución fotográfica de una herida.
 * - Lado a lado: las dos fotos con zoom sincronizado.
 * - Superpuesto: la foto posterior se descubre sobre la anterior con un deslizador.
 * Pellizcar para ampliar; doble toque para restablecer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCompareDialog(
    evaluationsWithPhotos: List<EvaluationEntity>,
    onDismiss: () -> Unit
) {
    if (evaluationsWithPhotos.size < 2) return

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var beforeIndex by remember { mutableIntStateOf(0) }
    var afterIndex by remember { mutableIntStateOf(evaluationsWithPhotos.lastIndex) }
    val beforeEval = evaluationsWithPhotos.getOrNull(beforeIndex) ?: evaluationsWithPhotos.first()
    val afterEval = evaluationsWithPhotos.getOrNull(afterIndex) ?: evaluationsWithPhotos.last()

    var overlayMode by remember { mutableStateOf(false) }
    var reveal by remember { mutableFloatStateOf(0.5f) }
    val zoom = remember { ZoomState() }
    val transformState = rememberTransform(zoom)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Cabecera
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Compare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.photo_compare_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close_button))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Modo de comparación
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !overlayMode,
                        onClick = { overlayMode = false; zoom.reset() },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text(stringResource(R.string.photo_compare_side_by_side)) }
                    SegmentedButton(
                        selected = overlayMode,
                        onClick = { overlayMode = true; zoom.reset() },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text(stringResource(R.string.photo_compare_overlay)) }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    if (overlayMode) {
                        OverlayCompare(beforeEval, afterEval, reveal, zoom, transformState)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PhotoPane(beforeEval, stringResource(R.string.cd_photo_before), zoom, transformState, Modifier.weight(1f))
                            PhotoPane(afterEval, stringResource(R.string.cd_photo_after), zoom, transformState, Modifier.weight(1f))
                        }
                    }
                }

                if (overlayMode) {
                    Slider(value = reveal, onValueChange = { reveal = it }, modifier = Modifier.fillMaxWidth())
                }

                // Pie: datos de cada foto
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhotoCaption(beforeEval, dateFormat, Modifier.weight(1f))
                    PhotoCaption(afterEval, dateFormat, Modifier.weight(1f))
                }
                Text(
                    stringResource(R.string.photo_compare_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Selección de fotos cuando hay más de dos
                if (evaluationsWithPhotos.size > 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PhotoSelector(
                            label = stringResource(R.string.photo_compare_before_format, dateFormat.format(Date(beforeEval.timestamp))),
                            evaluations = evaluationsWithPhotos,
                            selectedIndex = beforeIndex,
                            dateFormat = dateFormat,
                            onSelect = { beforeIndex = it },
                            modifier = Modifier.weight(1f)
                        )
                        PhotoSelector(
                            label = stringResource(R.string.photo_compare_after_format, dateFormat.format(Date(afterEval.timestamp))),
                            evaluations = evaluationsWithPhotos,
                            selectedIndex = afterIndex,
                            dateFormat = dateFormat,
                            onSelect = { afterIndex = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoPane(
    eval: EvaluationEntity,
    description: String,
    zoom: ZoomState,
    transformState: androidx.compose.foundation.gestures.TransformableState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .clipToBounds()
            .transformable(transformState)
            .zoomable(zoom)
    ) {
        AsyncImage(
            model = eval.photoPath?.let { File(it) },
            contentDescription = description,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().applyZoom(zoom)
        )
        Text(
            description,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun OverlayCompare(
    before: EvaluationEntity,
    after: EvaluationEntity,
    reveal: Float,
    zoom: ZoomState,
    transformState: androidx.compose.foundation.gestures.TransformableState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .clipToBounds()
            .transformable(transformState)
            .zoomable(zoom)
    ) {
        AsyncImage(
            model = before.photoPath?.let { File(it) },
            contentDescription = stringResource(R.string.cd_photo_before),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().applyZoom(zoom)
        )
        // La foto posterior solo se dibuja a la izquierda del deslizador
        AsyncImage(
            model = after.photoPath?.let { File(it) },
            contentDescription = stringResource(R.string.cd_photo_after),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(right = size.width * reveal) { this@drawWithContent.drawContent() }
                }
                .applyZoom(zoom)
        )
        // Línea divisoria
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * reveal
            drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), strokeWidth = 3.dp.toPx())
        }
        Text(
            stringResource(R.string.cd_photo_after),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Text(
            stringResource(R.string.cd_photo_before),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun PhotoCaption(eval: EvaluationEntity, dateFormat: SimpleDateFormat, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(modifier = modifier.padding(top = 6.dp)) {
        Text(dateFormat.format(Date(eval.timestamp)), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        val size = if (eval.length.isNotBlank() && eval.width.isNotBlank()) "${eval.length}×${eval.width} cm · " else ""
        Text(
            size + ClinicalTermMapper.translateClinicalTerm(eval.bedState, context),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
        )
    }
}

@Composable
private fun PhotoSelector(
    label: String,
    evaluations: List<EvaluationEntity>,
    selectedIndex: Int,
    dateFormat: SimpleDateFormat,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            evaluations.forEachIndexed { index, eval ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${dateFormat.format(Date(eval.timestamp))} (${eval.length}x${eval.width} cm)",
                            fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onSelect(index); expanded = false }
                )
            }
        }
    }
}

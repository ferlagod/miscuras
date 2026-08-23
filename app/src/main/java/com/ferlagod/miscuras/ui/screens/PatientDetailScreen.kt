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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferlagod.miscuras.ui.viewmodels.PatientViewModel
import androidx.compose.ui.platform.LocalContext
import com.ferlagod.miscuras.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.ferlagod.miscuras.data.entities.PatientEntity

import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    patientId: Long,
    patientViewModel: PatientViewModel,
    onBackClick: () -> Unit,
    onWoundClick: (Long) -> Unit
) {
    val wounds by patientViewModel.currentPatientWounds.collectAsState()
    val patients by patientViewModel.patients.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    val patient = patients.find { it.id == patientId }
    
    LaunchedEffect(patientId) {
        patientViewModel.loadWoundsForPatient(patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.patient_wounds_title),
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
                )
            )
        },
        floatingActionButton = {
            if (wounds.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddSheet = true },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_wound_desc)) },
                    text = { Text(stringResource(R.string.new_wound_title)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // === Patient Info Header Card ===
            var showEditSheet by remember { mutableStateOf(false) }
            if (patient != null) {
                PatientInfoHeader(
                    patient = patient,
                    woundCount = wounds.size,
                    onEditClick = { showEditSheet = true }
                )
                if (showEditSheet) {
                    EditPatientBottomSheet(
                        patient = patient,
                        onDismiss = { showEditSheet = false },
                        onSave = { updatedPatient -> 
                            patientViewModel.updatePatientDetails(updatedPatient)
                            showEditSheet = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val activeWounds = wounds.filter { !it.isDischarged }
            val dischargedWounds = wounds.filter { it.isDischarged }
            
            if (activeWounds.isEmpty() && dischargedWounds.isEmpty()) {
                // === Empty State ===
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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
                            text = stringResource(R.string.empty_wounds_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.empty_wounds_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { showAddSheet = true },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.empty_wounds_cta))
                        }
                    }
                }
            } else {
                // === Wound List with staggered animations ===
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    if (activeWounds.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.wounds_active_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        itemsIndexed(activeWounds, key = { _, w -> "active_${w.id}" }) { index, wound ->
                            WoundAnimatedItem(wound = wound, index = index, onWoundClick = onWoundClick)
                        }
                    }
                    if (dischargedWounds.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.wounds_discharged_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        itemsIndexed(dischargedWounds, key = { _, w -> "discharged_${w.id}" }) { index, wound ->
                            WoundAnimatedItem(wound = wound, index = index, onWoundClick = onWoundClick, isDischarged = true)
                        }
                    }
                }
            }
        }
    }

    // === BottomSheet para crear herida ===
    if (showAddSheet) {
        AddWoundBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { woundName ->
                patientViewModel.addWound(patientId, woundName)
                showAddSheet = false
            }
        )
    }
}

// ============================================================
// Patient Info Header
// ============================================================

@Composable
private fun PatientInfoHeader(
    patient: PatientEntity,
    woundCount: Int,
    onEditClick: () -> Unit
) {
    val name = patient.anonymizedName
    val room = patient.roomNumber
    val createdAt = patient.createdAt

    val avatarColor = remember(name) {
        val hue = (abs(name.hashCode()) % 360).toFloat()
        Color.hsl(hue, 0.45f, 0.55f)
    }
    val initials = remember(name) {
        val parts = name.trim().split("\\s+".toRegex(), limit = 3)
        when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }
    val dateStr = remember(createdAt) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(createdAt))
    }

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = avatarColor,
                modifier = Modifier.size(56.dp),
                shadowElevation = 2.dp
            ) {
                if (patient.photoUri != null) {
                    AsyncImage(
                        model = patient.photoUri,
                        contentDescription = "Foto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (room.isNotBlank()) {
                        Icon(
                            Icons.Rounded.MeetingRoom,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = room,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Icon(
                        Icons.Rounded.LocalHospital,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (woundCount == 1) stringResource(R.string.wounds_count_singular)
                               else String.format(stringResource(R.string.wounds_count), woundCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(stringResource(R.string.registered_since), dateStr),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    if (!patient.allergies.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(R.string.patient_allergies_label, patient.allergies), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    if (!patient.medication.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(stringResource(R.string.patient_medication_label, patient.medication), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!patient.medicalHistory.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(stringResource(R.string.patient_medical_history_label, patient.medicalHistory), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onEditClick) {
                    Icon(androidx.compose.material.icons.Icons.Rounded.Edit, contentDescription = stringResource(R.string.content_desc_edit), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }


// ============================================================
// Wound Card — Premium
// ============================================================

@Composable
private fun WoundCard(
    wound: com.ferlagod.miscuras.data.entities.WoundEntity,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "wound_card_press"
    )
    val dateStr = remember(wound.createdAt) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(wound.createdAt))
    }
    val formattedDischargedDate = remember(wound.dischargedAt) {
        wound.dischargedAt?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) }
    }
    val dischargedDateStr = formattedDischargedDate?.let { stringResource(R.string.wound_discharged_date_format, it) }
    val isDischarged = wound.isDischarged

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDischarged) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDischarged) 0.dp else 2.dp,
            pressedElevation = 0.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDischarged) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isDischarged) Icons.Rounded.CheckCircle else Icons.Rounded.LocalHospital,
                        contentDescription = null,
                        tint = if (isDischarged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wound.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isDischarged && dischargedDateStr != null) dischargedDateStr else dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================
// Bottom Sheet — Crear herida
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWoundBottomSheet(
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var woundName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.size(width = 40.dp, height = 4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ) {}
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Rounded.LocalHospital,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.new_wound_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Wound name field
            OutlinedTextField(
                value = woundName,
                onValueChange = { woundName = it },
                label = { Text(stringResource(R.string.wound_location_name_label)) },
                placeholder = { Text(stringResource(R.string.wound_name_hint)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = {
                    if (woundName.isNotBlank()) {
                        onSave(woundName)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = woundName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.save_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.cancel_button),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WoundAnimatedItem(wound: com.ferlagod.miscuras.data.entities.WoundEntity, index: Int, onWoundClick: (Long) -> Unit, isDischarged: Boolean = false) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(wound.id) {
        delay(index * 60L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
    ) {
        WoundCard(
            wound = wound,
            onClick = { onWoundClick(wound.id) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPatientBottomSheet(
    patient: PatientEntity,
    onDismiss: () -> Unit,
    onSave: (PatientEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var photoUri by remember { mutableStateOf(patient.photoUri) }
    var allergies by remember { mutableStateOf(patient.allergies ?: "") }
    var medication by remember { mutableStateOf(patient.medication ?: "") }
    var medicalHistory by remember { mutableStateOf(patient.medicalHistory ?: "") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "patient_photo_${System.currentTimeMillis()}.jpg"
                val file = java.io.File(context.filesDir, fileName)
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                photoUri = file.toURI().toString()
            } catch (e: Exception) {
                e.printStackTrace()
                photoUri = uri.toString()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(stringResource(R.string.edit_patient_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedButton(onClick = { 
                        photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    }) {
                        Text(stringResource(R.string.add_photo))
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text(stringResource(R.string.patient_allergies_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            item {
                OutlinedTextField(
                    value = medication,
                    onValueChange = { medication = it },
                    label = { Text(stringResource(R.string.patient_medication_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
            
            item {
                OutlinedTextField(
                    value = medicalHistory,
                    onValueChange = { medicalHistory = it },
                    label = { Text(stringResource(R.string.patient_medical_history_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onSave(patient.copy(
                            photoUri = photoUri,
                            allergies = allergies.ifBlank { null },
                            medication = medication.ifBlank { null },
                            medicalHistory = medicalHistory.ifBlank { null }
                        ))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.save_changes), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

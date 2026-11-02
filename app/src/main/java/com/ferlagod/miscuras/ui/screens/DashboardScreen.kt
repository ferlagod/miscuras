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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.ferlagod.miscuras.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.ferlagod.miscuras.data.models.summarize
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ferlagod.miscuras.ui.viewmodels.PatientViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ferlagod.miscuras.ui.WoundViewModel
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Helper to generate a deterministic color from a patient's name for the avatar.
 */
private fun avatarColorFromName(name: String): Color {
    val hue = (abs(name.hashCode()) % 360).toFloat()
    return Color.hsl(hue, 0.45f, 0.55f)
}

/**
 * Helper to extract up to 2 initials from a name string.
 */
private fun initialsFromName(name: String): String {
    val parts = name.trim().split("\\s+".toRegex(), limit = 3)
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
        parts.isNotEmpty() -> parts[0].take(2).uppercase()
        else -> "?"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    patientViewModel: PatientViewModel,
    woundViewModel: WoundViewModel,
    onQuickEvaluationClick: () -> Unit,
    onPatientClick: (Long) -> Unit,
    onNavigateToWoundEval: () -> Unit,
    onNavigateToCatalog: () -> Unit = {},
    onNavigateToResvech: () -> Unit = {},
    onNavigateToBraden: () -> Unit = {},
    onNavigateToGlossary: () -> Unit = {}
) {
    val patients by patientViewModel.patients.collectAsState()
    val configState by woundViewModel.configState.collectAsStateWithLifecycle()
    val backupState by woundViewModel.backupState.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var patientToDelete by remember { mutableStateOf<com.ferlagod.miscuras.data.entities.PatientEntity?>(null) }

    val alreadyAnimated = remember { mutableSetOf<Long>() }
    val cureStatus by patientViewModel.cureStatus.collectAsState()
    val now by com.ferlagod.miscuras.ui.components.rememberCurrentTimeMillis()
    val cureSummaries = remember(cureStatus, now) {
        cureStatus.groupBy { it.patientId }.mapValues { (_, list) -> list.summarize(now) }
    }

    val filteredPatients = remember(patients, searchQuery, cureSummaries) {
        // Pacientes con curas vencidas primero; el resto mantiene su orden
        val ordered = patients.sortedByDescending { (cureSummaries[it.id]?.overdueWounds ?: 0) > 0 }
        if (searchQuery.isBlank()) {
            ordered
        } else {
            // Búsqueda sin distinguir mayúsculas ni tildes ("Gomez" encuentra "Gómez")
            val norm = com.ferlagod.miscuras.domain.rules.RulesEngine::normalize
            val q = norm(searchQuery.trim())
            ordered.filter {
                norm(it.anonymizedName).contains(q) || norm(it.roomNumber).contains(q)
            }
        }
    }

    // Contraseña de la copia cifrada: se pide antes de abrir el selector de archivos
    var backupPasswordMode by remember { mutableStateOf<String?>(null) } // "create" | "restore"
    var pendingBackupPassword by remember { mutableStateOf("") }

    val createDocumentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { woundViewModel.createBackup(it, pendingBackupPassword) }
        pendingBackupPassword = ""
    }

    val openDocumentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { woundViewModel.restoreBackup(it, pendingBackupPassword) }
        pendingBackupPassword = ""
    }

    backupPasswordMode?.let { mode ->
        BackupPasswordDialog(
            isCreate = mode == "create",
            onDismiss = { backupPasswordMode = null },
            onConfirm = { password ->
                pendingBackupPassword = password
                backupPasswordMode = null
                if (mode == "create") {
                    createDocumentLauncher.launch("miscuras_backup.mcbk")
                } else {
                    openDocumentLauncher.launch(arrayOf("application/octet-stream", "application/zip", "application/x-zip-compressed", "multipart/x-zip", "*/*"))
                }
            }
        )
    }

    val context = LocalContext.current
    val lang = configState.currentLanguage
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_symbol),
                            contentDescription = stringResource(R.string.cd_logo),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.my_patients_title), fontWeight = FontWeight.Bold) 
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (patients.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddSheet = true },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_patient_desc)) },
                    text = { Text(stringResource(R.string.new_patient_title)) },
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // === Hero Card: Evaluación Rápida (con gradiente) ===
            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            val heroGradient = if (isDark) {
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                )
            }

            Card(
                onClick = onQuickEvaluationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroGradient)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animated flash icon
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(56.dp),
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                Icons.Rounded.FlashOn,
                                contentDescription = null,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.quick_evaluation_btn),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.quick_evaluation_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // === Herramientas clínicas con nombre (antes, iconos sin texto en la barra) ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolShortcut(Icons.Rounded.MedicalServices, stringResource(R.string.tool_catalog), onNavigateToCatalog, Modifier.weight(1f))
                ToolShortcut(Icons.Rounded.Bed, stringResource(R.string.tool_braden), onNavigateToBraden, Modifier.weight(1f))
                ToolShortcut(Icons.Rounded.BarChart, stringResource(R.string.tool_resvech), onNavigateToResvech, Modifier.weight(1f))
                ToolShortcut(Icons.AutoMirrored.Rounded.MenuBook, stringResource(R.string.tool_glossary), onNavigateToGlossary, Modifier.weight(1f))
            }

            // === Section Header + Search ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.registered_patients_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (patients.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = "${filteredPatients.size} / ${patients.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (patients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_patient_hint)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = stringResource(R.string.cd_clear))
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (patients.isEmpty()) {
                // === Empty State Premium ===
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.PersonAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.empty_patients_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.empty_patients_subtitle),
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
                            Text(stringResource(R.string.empty_patients_cta))
                        }
                    }
                }
            } else {
                // === Patient List with staggered animation ===
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(filteredPatients, key = { _, p -> p.id }) { index, patient ->
                        // Animación de entrada solo la primera vez y con retraso acotado:
                        // antes cada fila esperaba índice × 40 ms y se re-animaba al hacer scroll.
                        var visible by remember { mutableStateOf(patient.id in alreadyAnimated) }
                        LaunchedEffect(patient.id) {
                            if (!visible) {
                                delay(index.coerceAtMost(8) * 40L)
                                visible = true
                                alreadyAnimated.add(patient.id)
                            }
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
                            PatientCard(
                                name = patient.anonymizedName,
                                room = patient.roomNumber,
                                photoUri = patient.photoUri,
                                cureSummary = cureSummaries[patient.id],
                                now = now,
                                onClick = { onPatientClick(patient.id) },
                                onDeleteClick = { patientToDelete = patient }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for deleting patient
    if (patientToDelete != null) {
        AlertDialog(
            onDismissRequest = { patientToDelete = null },
            icon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.delete_patient_title)) },
            text = { Text(stringResource(R.string.delete_patient_text, patientToDelete?.anonymizedName.orEmpty())) },
            confirmButton = {
                Button(
                    onClick = {
                        patientToDelete?.let { patientViewModel.deletePatient(it.id) }
                        patientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { patientToDelete = null }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    // === BottomSheet para crear paciente ===
    if (showAddSheet) {
        AddPatientBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, room ->
                patientViewModel.addPatient(name, room)
                showAddSheet = false
            }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentLanguage = configState.currentLanguage,
            onLanguageChanged = { woundViewModel.changeLanguage(it) },
            currentTheme = configState.currentTheme,
            onThemeChanged = { woundViewModel.changeTheme(it) },
            onDismiss = { showSettings = false },
            onSuggestProductClick = { 
                showSettings = false
                woundViewModel.setAddProductDialogVisibility(true)
            },
            onBackupClick = {
                showSettings = false
                backupPasswordMode = "create"
            },
            onRestoreClick = {
                showSettings = false
                backupPasswordMode = "restore"
            }
        )
    }
    
    // Backup State handling
    when (backupState) {
        is com.ferlagod.miscuras.domain.BackupState.Loading -> {
            androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
                Box(modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        is com.ferlagod.miscuras.domain.BackupState.Success -> {
            LaunchedEffect(Unit) {
                android.widget.Toast.makeText(context, context.getString(R.string.backup_success), android.widget.Toast.LENGTH_SHORT).show()
                woundViewModel.resetBackupState()
            }
        }
        is com.ferlagod.miscuras.domain.BackupState.Error -> {
            LaunchedEffect(Unit) {
                android.widget.Toast.makeText(context, context.getString(R.string.backup_error_prefix, (backupState as com.ferlagod.miscuras.domain.BackupState.Error).message), android.widget.Toast.LENGTH_LONG).show()
                woundViewModel.resetBackupState()
            }
        }
        else -> {}
    }
}

// ============================================================
// Patient Card — Premium with avatar, initials, room
// ============================================================

@Composable
private fun PatientCard(
    name: String,
    room: String,
    photoUri: String? = null,
    cureSummary: com.ferlagod.miscuras.data.models.PatientCureSummary? = null,
    now: Long = System.currentTimeMillis(),
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_press"
    )

    val avatarColor = remember(name) { avatarColorFromName(name) }
    val initials = remember(name) { initialsFromName(name) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
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
            // Avatar with initials
            Surface(
                shape = CircleShape,
                color = avatarColor,
                modifier = Modifier.size(48.dp),
                shadowElevation = 2.dp
            ) {
                if (photoUri != null) {
                    androidx.compose.foundation.Image(
                        painter = coil.compose.rememberAsyncImagePainter(photoUri),
                        contentDescription = stringResource(R.string.content_desc_patient_photo),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (room.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.MeetingRoom,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = room,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (cureSummary != null && cureSummary.activeWounds > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    PatientCureInfo(cureSummary, now)
                }
            }

            if (onDeleteClick != null) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_delete_patient),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Heridas activas, última cura y aviso de cura vencida/próxima. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PatientCureInfo(summary: com.ferlagod.miscuras.data.models.PatientCureSummary, now: Long) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        com.ferlagod.miscuras.ui.components.StatusPill(
            text = pluralStringResource(R.plurals.active_wounds_count, summary.activeWounds, summary.activeWounds),
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Rounded.Healing
        )
        when {
            summary.overdueWounds > 0 -> com.ferlagod.miscuras.ui.components.StatusPill(
                text = pluralStringResource(R.plurals.overdue_cures_count, summary.overdueWounds, summary.overdueWounds),
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
                icon = Icons.Rounded.Alarm
            )
            summary.nextCureDueAt != null -> com.ferlagod.miscuras.ui.components.StatusPill(
                text = stringResource(R.string.next_cure_format, com.ferlagod.miscuras.ui.components.relativeTime(summary.nextCureDueAt, now)),
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
                icon = Icons.Rounded.Schedule
            )
        }
        if (summary.woundsWithoutEvaluation > 0) {
            com.ferlagod.miscuras.ui.components.StatusPill(
                text = pluralStringResource(R.plurals.unevaluated_wounds_count, summary.woundsWithoutEvaluation, summary.woundsWithoutEvaluation),
                container = MaterialTheme.colorScheme.surfaceVariant,
                content = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    summary.lastEvaluationAt?.let {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.last_cure_format, com.ferlagod.miscuras.ui.components.relativeTime(it, now)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Acceso con nombre a una herramienta clínica. */
@Composable
private fun ToolShortcut(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================
// Bottom Sheet — Crear paciente
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPatientBottomSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, room: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }

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
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Rounded.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.new_patient_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.quick_evaluation_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name_initials_label)) },
                placeholder = { Text(stringResource(R.string.patient_name_hint)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Person,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Room Field
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text(stringResource(R.string.room_bed_label)) },
                placeholder = { Text(stringResource(R.string.room_bed_hint)) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.MeetingRoom,
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
                    if (name.isNotBlank()) {
                        onSave(name, room)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank(),
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

            // Cancel link
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

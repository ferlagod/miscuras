package com.ferlagod.miscuras.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.ferlagod.miscuras.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ferlagod.miscuras.ui.viewmodels.PatientViewModel
import com.ferlagod.miscuras.ui.AppStrings
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ferlagod.miscuras.ui.WoundViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    patientViewModel: PatientViewModel,
    woundViewModel: WoundViewModel,
    onQuickEvaluationClick: () -> Unit,
    onPatientClick: (Long) -> Unit,
    onNavigateToWoundEval: () -> Unit
) {
    val patients by patientViewModel.patients.collectAsState()
    val uiState by woundViewModel.uiState.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
    val lang = prefs.getString("language", "es") ?: "es"
    val strings = AppStrings.getStrings(lang)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_symbol),
                            contentDescription = "Logo Mis Curas",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(strings.myPatientsTitle, fontWeight = FontWeight.Bold) 
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        woundViewModel.showBraden()
                        onNavigateToWoundEval()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Calculadora Braden"
                        )
                    }
                    IconButton(onClick = { 
                        woundViewModel.showGlossary()
                        onNavigateToWoundEval()
                    }) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Glosario / Biblioteca"
                        )
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsTitle
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = strings.addPatientDesc) },
                text = { Text(strings.newPatientTitle) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Hero Card para Evaluación Rápida
            Card(
                onClick = onQuickEvaluationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = strings.quickEvaluationBtn,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Valoración directa sin paciente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = strings.registeredPatientsLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (patients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = strings.noPatientsMsg,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el FAB
                ) {
                    items(patients) { patient ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPatientClick(patient.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = patient.anonymizedName, 
                                        fontWeight = FontWeight.Bold, 
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Habitación: ${patient.roomNumber}", 
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var room by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(strings.newPatientTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(strings.nameInitialsLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text(strings.roomBedLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            patientViewModel.addPatient(name, room)
                            showAddDialog = false
                        }
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(strings.saveButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(strings.cancelButton)
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentLanguage = uiState.currentLanguage,
            onLanguageChanged = { woundViewModel.changeLanguage(it) },
            currentTheme = uiState.currentTheme,
            onThemeChanged = { woundViewModel.changeTheme(it) },
            onDismiss = { showSettings = false },
            onSuggestProductClick = { 
                showSettings = false
                woundViewModel.setAddProductDialogVisibility(true)
            }
        )
    }
}

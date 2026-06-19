package com.ferlagod.miscuras.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ferlagod.miscuras.ui.viewmodels.PatientViewModel
import com.ferlagod.miscuras.ui.AppStrings
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    patientViewModel: PatientViewModel,
    onQuickEvaluationClick: () -> Unit,
    onPatientClick: (Long) -> Unit
) {
    val patients by patientViewModel.patients.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
    val lang = prefs.getString("language", "es") ?: "es"
    val strings = AppStrings.getStrings(lang)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.myPatientsTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = strings.addPatientDesc)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Quick Evaluation Button
            Button(
                onClick = onQuickEvaluationClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.quickEvaluationBtn)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = strings.registeredPatientsLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (patients.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        strings.noPatientsMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(patients) { patient ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPatientClick(patient.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(patient.anonymizedName, fontWeight = FontWeight.Bold)
                                    Text(patient.roomNumber, style = MaterialTheme.typography.bodySmall)
                                }
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
            title = { Text(strings.newPatientTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(strings.nameInitialsLabel) }
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text(strings.roomBedLabel) }
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
                    }
                ) {
                    Text(strings.saveButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(strings.cancelButton)
                }
            }
        )
    }
}

/*
 * Mis Curas
 * Copyright (C) 2026 Fernando Lago (ferlagod)
 *
 * Este programa es software libre: puede redistribuirlo y/o modificarlo
 * bajo los términos de la Licencia Pública General GNU publicada por
 * la Free Software Foundation, ya sea la versión 3 de la Licencia, o
 * (a su elección) cualquier versión posterior.
 */
package com.ferlagod.miscuras

import android.os.Bundle
import android.app.AlertDialog
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.ferlagod.miscuras.network.AsistenteIA
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ferlagod.miscuras.data.database.AppDatabase
import com.ferlagod.miscuras.data.repository.ApositosRepository
import com.ferlagod.miscuras.ui.AppStrings
import com.ferlagod.miscuras.ui.WoundViewModel
import com.ferlagod.miscuras.ui.screens.WoundScreen
import com.ferlagod.miscuras.ui.theme.MisCurasTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ferlagod.miscuras.ui.screens.DashboardScreen
import com.ferlagod.miscuras.ui.screens.PatientDetailScreen
import com.ferlagod.miscuras.ui.screens.WoundDetailScreen
import com.ferlagod.miscuras.ui.viewmodels.PatientViewModel

/**
 * Actividad principal y punto de entrada de la aplicación.
 * Configura la base de datos de Room en su primera ejecución y establece 
 * la raíz de navegación de Jetpack Compose invocando a [WoundScreen].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Diseño inmersivo (edge-to-edge)
        enableEdgeToEdge()

        // 1. Instanciar la base de datos (La despensa)
        val database = AppDatabase.getDatabase(this)
        val repository = ApositosRepository(
            apositoDao = database.apositoDao(),
            aiCacheDao = database.aiCacheDao()
        )

        // 3. Crear un "Factory" para el ViewModel
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WoundViewModel(application, repository) as T
            }
        }

        // 4. Obtener el ViewModel
        val viewModel: WoundViewModel by viewModels { factory }

        // ViewModel de Pacientes
        val patientFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PatientViewModel(application) as T
            }
        }
        val patientViewModel: PatientViewModel by viewModels { patientFactory }

        // 5. Dibujar la pantalla con Navegación
        setContent {
            val isDarkTheme = when (viewModel.uiState.collectAsState().value.currentTheme) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            MisCurasTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                patientViewModel = patientViewModel,
                                onQuickEvaluationClick = { 
                                    viewModel.resetWizard() // Helper to add later or just clear state
                                    navController.navigate("wound_eval/-1") 
                                },
                                onPatientClick = { patientId ->
                                    navController.navigate("patient_detail/$patientId")
                                }
                            )
                        }

                        composable("patient_detail/{patientId}") { backStackEntry ->
                            val patientId = backStackEntry.arguments?.getString("patientId")?.toLongOrNull() ?: return@composable
                            PatientDetailScreen(
                                patientId = patientId,
                                patientViewModel = patientViewModel,
                                onBackClick = { navController.popBackStack() },
                                onWoundClick = { woundId ->
                                    navController.navigate("wound_detail/$woundId")
                                }
                            )
                        }

                        composable("wound_detail/{woundId}") { backStackEntry ->
                            val woundId = backStackEntry.arguments?.getString("woundId")?.toLongOrNull() ?: return@composable
                            WoundDetailScreen(
                                woundId = woundId,
                                patientViewModel = patientViewModel,
                                onBackClick = { navController.popBackStack() },
                                onNewEvaluationClick = { wId ->
                                    viewModel.resetWizard()
                                    navController.navigate("wound_eval/$wId")
                                }
                            )
                        }

                        composable("wound_eval/{woundId}") { backStackEntry ->
                            val woundId = backStackEntry.arguments?.getString("woundId")?.toLongOrNull() ?: -1L
                            
                            val uiState by viewModel.uiState.collectAsState()
                            
                            // Si se ha guardado, volver a la pantalla anterior
                            LaunchedEffect(uiState.isSaved) {
                                if (uiState.isSaved) {
                                    navController.popBackStack()
                                    viewModel.resetSavedState()
                                }
                            }

                            WoundScreen(
                                viewModel = viewModel,
                                woundIdForSave = if (woundId != -1L) woundId else null,
                                onBackToDashboard = { navController.popBackStack("dashboard", false) }
                            )
                        }
                    }
                }
            }
        }
    }
}

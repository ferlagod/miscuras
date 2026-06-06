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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

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
        val database = AppDatabase.getDatabase(applicationContext)

        // 2. Instanciar el Repositorio (El ayudante)
        val repository = ApositosRepository(database.apositoDao())

        // 3. Crear un "Factory" para el ViewModel
        val sharedPrefs = getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WoundViewModel(repository, sharedPrefs) as T
            }
        }

        // 4. Obtener el ViewModel
        val viewModel: WoundViewModel by viewModels { factory }

        // 5. Dibujar la pantalla (Abrir la puerta a los clientes)
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val strings = AppStrings.getStrings(uiState.currentLanguage)
            var showExitDialog by remember { mutableStateOf(false) }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text(strings.exitDialogTitle) },
                    text = { Text(strings.exitDialogText) },
                    confirmButton = {
                        TextButton(onClick = {
                            showExitDialog = false
                            finish()
                        }) {
                            Text(strings.exitDialogConfirm)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text(strings.exitDialogDismiss)
                        }
                    }
                )
            }

            BackHandler {
                showExitDialog = true
            }

            val isDarkTheme = when (uiState.currentTheme) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MisCurasTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    WoundScreen(viewModel = viewModel)
                }
            }
        }
    }
}

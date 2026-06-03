/*
 * Mis Curas
 * Copyright (C) 2026 Fernando Lago (ferlagod)
 *
 * Este programa es software libre: puede redistribuirlo y/o modificarlo
 * bajo los términos de la Licencia Pública General GNU publicada por
 * la Free Software Foundation, ya sea la versión 3 de la Licencia, o
 * (a su elección) cualquier versión posterior.
 */
package com.ferlagod.miscuras.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.data.repository.ApositosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado unificado de la UI para la pantalla de evaluación de heridas.
 * Toda la información necesaria para renderizar la pantalla vive aquí
 * de forma inmutable.
 */
data class WoundUiState(
    val selectedLecho: String = "Piel Intacta (Prevención)",
    val selectedExudado: String = "Nulo",
    val selectedInfeccion: Boolean = false,
    val familiaRecomendada: String? = null,
    val productos: List<ApositoEntity> = emptyList(),
    val showResults: Boolean = false,
    val isLoading: Boolean = false,
    val noMatchFound: Boolean = false,
    val showSplash: Boolean = true,
    val currentLanguage: String = "es",
    val currentTheme: String = "dark", // system, light, dark
    // Estado del formulario de sugerencia
    val showAddProductDialog: Boolean = false,
    val isFormSubmitting: Boolean = false,
    val formResultMsg: String? = null,
    val formSuccess: Boolean = false,
    val hasSeenDisclaimer: Boolean = false
)

/**
 * ViewModel principal de la aplicación.
 * Gestiona el estado de la UI ([WoundUiState]), interactúa con el [ApositosRepository]
 * para realizar búsquedas basadas en los parámetros clínicos, y maneja las
 * preferencias de usuario como el idioma y el tema.
 *
 * @property repository Repositorio de datos para obtener las reglas y productos.
 * @property sharedPrefs Preferencias locales para persistir configuraciones.
 */
class WoundViewModel(
    private val repository: ApositosRepository,
    private val sharedPrefs: android.content.SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(WoundUiState())
    val uiState: StateFlow<WoundUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val savedLang = sharedPrefs.getString("language", "es") ?: "es"
            val savedTheme = sharedPrefs.getString("theme", "dark") ?: "dark"
            val disclaimerSeen = sharedPrefs.getBoolean("has_seen_disclaimer", false)
            _uiState.update { 
                it.copy(
                    currentLanguage = savedLang, 
                    currentTheme = savedTheme,
                    hasSeenDisclaimer = disclaimerSeen
                ) 
            }
            
            // Forzar inicialización y carga de la BD mientras se muestra el splash
            repository.preCargarBaseDeDatos()
            
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(showSplash = false) }
        }
    }

    companion object {
        val opcionesLecho = listOf("Necrosis", "Esfacelo", "Granulación", "Epitelización", "Piel Intacta (Prevención)")
        val opcionesExudado = listOf("Nulo", "Bajo", "Moderado", "Alto")
    }

    /**
     * Actualiza el estado clínico del lecho de la herida seleccionado.
     * @param lecho El nuevo estado del lecho.
     */
    fun onLechoChanged(lecho: String) {
        _uiState.update { it.copy(selectedLecho = lecho) }
    }

    /**
     * Actualiza el nivel de exudado de la herida seleccionado.
     * @param exudado El nuevo nivel de exudado.
     */
    fun onExudadoChanged(exudado: String) {
        _uiState.update { it.copy(selectedExudado = exudado) }
    }

    /**
     * Actualiza si la herida presenta signos clínicos de infección.
     * @param infeccion Verdadero si hay signos de infección, falso en caso contrario.
     */
    fun onInfeccionChanged(infeccion: Boolean) {
        _uiState.update { it.copy(selectedInfeccion = infeccion) }
    }


    /**
     * Ejecuta la búsqueda de apósitos basados en los parámetros clínicos
     * actuales almacenados en el estado de la UI.
     */
    fun buscarAposito() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, noMatchFound = false) }

        viewModelScope.launch(Dispatchers.IO) {
            val familia = repository.obtenerRecomendacion(
                state.selectedLecho,
                state.selectedExudado,
                state.selectedInfeccion
            )

            if (familia != null) {
                val productos = repository.obtenerProductosPorFamilias(familia)
                val familiaFormateada = familia.split("/").joinToString(" y ") { it.trim() }
                _uiState.update {
                    it.copy(
                        familiaRecomendada = familiaFormateada,
                        productos = productos,
                        showResults = true,
                        isLoading = false,
                        noMatchFound = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        familiaRecomendada = null,
                        productos = emptyList(),
                        showResults = true,
                        isLoading = false,
                        noMatchFound = true
                    )
                }
            }
        }
    }

    /**
     * Reinicia el estado de resultados, regresando al usuario
     * a la vista de selección/evaluación.
     */
    fun volverASeleccion() {
        _uiState.update {
            it.copy(
                showResults = false,
                familiaRecomendada = null,
                productos = emptyList(),
                noMatchFound = false
            )
        }
    }

    /**
     * Cambia el idioma de la aplicación de forma persistente.
     * @param lang Código de idioma (ej. "es", "en", "pt").
     */
    fun changeLanguage(lang: String) {
        sharedPrefs.edit().putString("language", lang).apply()
        _uiState.update { it.copy(currentLanguage = lang) }
    }

    /**
     * Cambia el tema de la aplicación de forma persistente.
     * @param theme Código del tema ("light", "dark", "system").
     */
    fun changeTheme(theme: String) {
        sharedPrefs.edit().putString("theme", theme).apply()
        _uiState.update { it.copy(currentTheme = theme) }
    }

    /**
     * Marca el disclaimer como aceptado para que no vuelva a aparecer.
     */
    fun acceptDisclaimer() {
        sharedPrefs.edit().putBoolean("has_seen_disclaimer", true).apply()
        _uiState.update { it.copy(hasSeenDisclaimer = true) }
    }

    /**
     * Muestra u oculta el diálogo para sugerir productos.
     */
    fun setAddProductDialogVisibility(show: Boolean) {
        _uiState.update { it.copy(showAddProductDialog = show) }
    }

    /**
     * Limpia el mensaje de resultado del formulario (por ej. al cerrar el Snackbar).
     */
    fun clearFormResultMsg() {
        _uiState.update { it.copy(formResultMsg = null, formSuccess = false) }
    }

    /**
     * Envía la sugerencia del producto mediante la API de FormSubmit.
     */
    fun submitProductSuggestion(
        name: String,
        isHealthProfessional: Boolean,
        belongsToLaboratory: Boolean,
        productName: String,
        woundBed: String,
        exudateLevel: String,
        otherSuggestions: String,
        strings: AppStrings
    ) {
        _uiState.update { it.copy(isFormSubmitting = true, formResultMsg = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = com.ferlagod.miscuras.network.FormPayload(
                    name = name,
                    is_health_professional = if (isHealthProfessional) "Sí" else "No",
                    belongs_to_laboratory = if (belongsToLaboratory) "Sí" else "No",
                    product_name = productName,
                    wound_bed = woundBed,
                    exudate_level = exudateLevel,
                    other_suggestions = otherSuggestions
                )
                val response = com.ferlagod.miscuras.network.NetworkClient.formSubmitApi.submitForm(
                    formId = "mykvarap",
                    payload = payload
                )
                
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isFormSubmitting = false,
                            showAddProductDialog = false,
                            formResultMsg = strings.formSuccessMsg,
                            formSuccess = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isFormSubmitting = false,
                            formResultMsg = "${strings.formErrorMsg}\nHTTP ${response.code()}: ${response.errorBody()?.string()}",
                            formSuccess = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isFormSubmitting = false,
                        formResultMsg = "${strings.formErrorMsg}\nException: ${e.localizedMessage}",
                        formSuccess = false
                    )
                }
            }
        }
    }
}
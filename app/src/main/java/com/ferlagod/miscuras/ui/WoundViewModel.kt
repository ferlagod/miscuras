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

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferlagod.miscuras.data.dao.PatientDao
import com.ferlagod.miscuras.domain.usecase.EvaluateWoundUseCase
import com.ferlagod.miscuras.data.repository.FeedbackRepository
import com.ferlagod.miscuras.data.database.AppDatabase
import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.data.entities.EvaluationEntity
import com.ferlagod.miscuras.data.repository.ApositosRepository
import com.ferlagod.miscuras.domain.rules.RulesEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ferlagod.miscuras.R

enum class WizardStep(val progress: Float) {
    ETIOLOGY(0.20f),
    SIZE_AND_LOCATION(0.40f),
    EXUDATE(0.60f),
    EDGES(0.80f),
    INFECTION(1.0f)
}

/**
 * Estado unificado de la UI para la pantalla de evaluación de heridas.
 * Toda la información necesaria para renderizar la pantalla vive aquí
 * de forma inmutable.
 */

data class WizardState(
    val currentWizardStep: WizardStep = WizardStep.ETIOLOGY,
    val selectedEtiology: String = "Indeterminada",
    val selectedLecho: String = "Piel Intacta (Prevención)",
    val selectedExudado: String = "Nulo",
    val selectedExudateType: String = "Seroso",
    val selectedInfeccion: Boolean = false,
    val woundLength: String = "",
    val woundWidth: String = "",
    val woundDepth: String = "",
    val hasCavitation: Boolean = false,
    val cavitationDetails: String = "",
    val specialLocation: String = "Ninguno",
    val infectionGerm: String = "Desconocido",
    val selectedBordes: String = "Sanos/Íntegros",
    val selectedPerilesional: String = "Sana",
    val painLevel: Float = 0f,
    val photoPath: String? = null
)

data class EvaluationState(
    val bradenScore: Int? = null,
    val familiaRecomendada: String? = null,
    val productos: List<ApositoEntity> = emptyList(),
    val selectedTreatmentProducts: Set<String> = emptySet(),
    val cureFrequency: String? = null,
    val showResults: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val noMatchFound: Boolean = false,
    val safetyAlerts: List<String> = emptyList(),
    val aiResponse: String? = null,
    val isAiLoading: Boolean = false
)

data class ConfigState(
    val showSplash: Boolean = true,
    val showArMeasure: Boolean = false,
    val showBraden: Boolean = false,
    val showGlossary: Boolean = false,
    val currentLanguage: String = "es",
    val currentTheme: String = "system",
    val showAddProductDialog: Boolean = false,
    val isFormSubmitting: Boolean = false,
    val formResultMsg: String? = null,
    val formSuccess: Boolean = false,
    val hasSeenDisclaimer: Boolean = false
)
/**
 * ViewModel principal para el flujo de evaluación de heridas.
 * Gestiona el estado de la UI ([WoundUiState]), procesa la lógica de negocio, reglas clínicas y obtiene recomendaciones desde el repositorio.
 */
class WoundViewModel(
    private val patientDao: PatientDao,
    private val sharedPrefs: SharedPreferences,
    private val repository: ApositosRepository,
    private val evaluateWoundUseCase: EvaluateWoundUseCase,
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {


    private val _wizardState = MutableStateFlow(WizardState())
    val wizardState: StateFlow<WizardState> = _wizardState.asStateFlow()

    private val _evaluationState = MutableStateFlow(EvaluationState())
    val evaluationState: StateFlow<EvaluationState> = _evaluationState.asStateFlow()

    private val _configState = MutableStateFlow(ConfigState())
    val configState: StateFlow<ConfigState> = _configState.asStateFlow()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            val savedLang = sharedPrefs.getString("language", "es") ?: "es"
            val savedTheme = sharedPrefs.getString("theme", "system") ?: "system"
            val disclaimerSeen = sharedPrefs.getBoolean("has_seen_disclaimer", false)
            _configState.update { 
                it.copy(
                    currentLanguage = savedLang, 
                    currentTheme = savedTheme,
                    hasSeenDisclaimer = disclaimerSeen
                ) 
            }
            
            // Forzar inicialización y carga de la BD mientras se muestra el splash
            repository.preCargarBaseDeDatos()
            
            kotlinx.coroutines.delay(2000)
            _configState.update { it.copy(showSplash = false) }
        }
    }

    companion object {
        val opcionesEtiologia = listOf("Indeterminada", "Pie Diabético", "Quemadura", "Quirúrgica", "Traumática", "UPP", "Úlcera Arterial", "Úlcera Venosa")
        val opcionesLecho = listOf("Necrosis", "Esfacelo", "Granulación", "Epitelización", "Piel Intacta (Prevención)")
        val opcionesExudado = listOf("Nulo", "Bajo", "Moderado", "Alto")
        val opcionesTipoExudado = listOf("Seroso", "Turbio", "Purulento", "Hemorrágico", "Serohemorrágico")
        val opcionesBordes = listOf("Sanos/Íntegros", "Macerados", "Descamativos", "Hiperqueratósicos", "Socavados", "Epibólicos (enrollados)")
        val opcionesPerilesional = listOf("Sana", "Macerada", "Descamativa", "Eccematosa", "Eritematosa")
    }

    /**
     * Navegación del asistente (Wizard)
     */
    fun nextStep() {
        val current = _wizardState.value.currentWizardStep
        val values = WizardStep.values()
        
        var nextOrdinal = current.ordinal + 1
        if (nextOrdinal >= values.size) return
        
        var nextStep = values[nextOrdinal]
        val wizard = _wizardState.value
        val eval = _evaluationState.value
        val config = _configState.value
        
        // Branching logic
        if (current == WizardStep.ETIOLOGY && wizard.selectedLecho == "Piel Intacta (Prevención)") {
            buscarAposito()
            return
        }
        
        _wizardState.update { it.copy(currentWizardStep = nextStep) }
    }

    fun previousStep() {
        val current = _wizardState.value.currentWizardStep
        val values = WizardStep.values()
        
        var prevOrdinal = current.ordinal - 1
        if (prevOrdinal < 0) return
        
        var prevStep = values[prevOrdinal]
        val wizard = _wizardState.value
        val eval = _evaluationState.value
        val config = _configState.value
        
        // Reverse branching logic
        if (current == WizardStep.INFECTION && wizard.selectedLecho == "Piel Intacta (Prevención)") {
            prevStep = WizardStep.ETIOLOGY
        }
        
        _wizardState.update { it.copy(currentWizardStep = prevStep) }
    }

    /**
     * Actualiza la etiología seleccionada.
     */
    fun onEtiologyChanged(etiology: String) {
        _wizardState.update { it.copy(selectedEtiology = etiology) }
    }

    /**
     * Actualiza el estado clínico del lecho de la herida seleccionado.
     * @param lecho El nuevo estado del lecho.
     */
    fun onLechoChanged(lecho: String) {
        _wizardState.update { 
            if (lecho == "Piel Intacta (Prevención)") {
                it.copy(selectedLecho = lecho, selectedExudado = "Nulo", selectedExudateType = "Seroso", selectedInfeccion = false)
            } else {
                it.copy(selectedLecho = lecho)
            }
        }
    }

    /**
     * Actualiza el nivel de exudado de la herida seleccionado.
     * @param exudado El nuevo nivel de exudado.
     */
    fun onExudadoChanged(exudado: String) {
        _wizardState.update { it.copy(selectedExudado = exudado) }
    }

    fun onExudateTypeChanged(type: String) {
        _wizardState.update { it.copy(selectedExudateType = type) }
    }

    /**
     * Actualiza si la herida presenta signos clínicos de infección.
     * @param infeccion Verdadero si hay signos de infección, falso en caso contrario.
     */
    fun onInfeccionChanged(infeccion: Boolean) {
        _wizardState.update { 
            it.copy(
                selectedInfeccion = infeccion,
                infectionGerm = if (!infeccion) "Desconocido" else it.infectionGerm
            )
        }
    }

    fun onWoundLengthChanged(length: String) {
        _wizardState.update { it.copy(woundLength = length) }
    }

    fun onWoundWidthChanged(width: String) {
        _wizardState.update { it.copy(woundWidth = width) }
    }

    fun onWoundDepthChanged(depth: String) {
        _wizardState.update { it.copy(woundDepth = depth) }
    }

    fun onHasCavitationChanged(hasCavitation: Boolean) {
        _wizardState.update { it.copy(hasCavitation = hasCavitation) }
    }

    fun onCavitationDetailsChanged(details: String) {
        _wizardState.update { it.copy(cavitationDetails = details) }
    }

    fun onSpecialLocationChanged(location: String) {
        _wizardState.update { it.copy(specialLocation = location) }
    }



    fun showGlossary() {
        _configState.update { it.copy(showGlossary = true) }
    }

    fun hideGlossary() {
        _configState.update { it.copy(showGlossary = false) }
    }

    fun showArMeasure() {
        _configState.update { it.copy(showArMeasure = true) }
    }

    fun hideArMeasure() {
        _configState.update { it.copy(showArMeasure = false) }
    }

    fun showBraden() {
        _configState.update { it.copy(showBraden = true) }
    }

    fun hideBraden() {
        _configState.update { it.copy(showBraden = false) }
    }

    fun onPainLevelChanged(pain: Float) {
        _wizardState.update { it.copy(painLevel = pain) }
    }

    fun onArMeasured(lengthCm: Float, widthCm: Float) {
        val lengthStr = String.format(java.util.Locale.US, "%.1f", lengthCm)
        val widthStr = String.format(java.util.Locale.US, "%.1f", widthCm)
        _wizardState.update { 
            it.copy(
                woundLength = lengthStr, 
                woundWidth = widthStr
            ) 
        }
        _configState.update { it.copy(showArMeasure = false) }
    }

    fun onInfectionGermChanged(germ: String) {
        _wizardState.update { it.copy(infectionGerm = germ) }
    }

    fun onBordesChanged(bordes: String) {
        _wizardState.update { it.copy(selectedBordes = bordes) }
    }

    fun onPerilesionalChanged(peri: String) {
        _wizardState.update { it.copy(selectedPerilesional = peri) }
    }

    fun onBradenScoreUpdated(score: Int) {
        _evaluationState.update { it.copy(bradenScore = score) }
    }

    /**
     * Genera un resumen clínico estructurado basado en los criterios TIME
     */
    fun generarResumenEvolutivo(productoSeleccionado: String, context: android.content.Context): String {
        val wizard = _wizardState.value
        val eval = _evaluationState.value
        val infText = if (wizard.selectedInfeccion) String.format(context.getString(R.string.rep_inf_yes_format), wizard.infectionGerm) else context.getString(R.string.no)
        val tamaño = if (wizard.woundLength.isNotEmpty() && wizard.woundWidth.isNotEmpty()) {
            val depthStr = if (wizard.woundDepth.isNotEmpty()) " x ${wizard.woundDepth}" else ""
            "${wizard.woundLength} x ${wizard.woundWidth}$depthStr cm"
        } else {
            context.getString(R.string.rep_unspecified)
        }
        
        val aiPlan = eval.aiResponse ?: context.getString(R.string.rep_pending)

        val bradenText = if (eval.bradenScore != null) {
            val riskText = when {
                eval.bradenScore >= 15 -> context.getString(R.string.braden_risk_low)
                eval.bradenScore in 13..14 -> context.getString(R.string.braden_risk_moderate)
                eval.bradenScore in 10..12 -> context.getString(R.string.braden_risk_high)
                else -> context.getString(R.string.braden_risk_very_high)
            }
            "\n            ${context.getString(R.string.rep_braden_title)}\n            ${String.format(context.getString(R.string.rep_braden_score_format), eval.bradenScore, riskText)}\n"
        } else ""

        val bradenPrevencion = if (eval.bradenScore != null && eval.bradenScore < 12) {
            "\n            ${context.getString(R.string.rep_braden_preventive_title)}\n            ${context.getString(R.string.rep_braden_preventive_text)}\n"
        } else ""

        return """
            ${context.getString(R.string.rep_timers_title)}
            ${context.getString(R.string.etiology_label)}: ${wizard.selectedEtiology}
            ${context.getString(R.string.rep_tissue)}${wizard.selectedLecho}
            ${context.getString(R.string.rep_infection)}$infText
            ${context.getString(R.string.rep_moisture)}${wizard.selectedExudado} (${wizard.selectedExudateType})
            ${String.format(context.getString(R.string.rep_edges_format), wizard.selectedBordes, wizard.selectedPerilesional)}
            ${String.format(context.getString(R.string.rep_pain_format), wizard.painLevel.toInt())}
            ${String.format(context.getString(R.string.rep_size_format), tamaño)}${if (wizard.hasCavitation) "\n            - Cavitaciones: ${if (wizard.cavitationDetails.isNotEmpty()) wizard.cavitationDetails else "Sí"}" else ""}
            ${String.format(context.getString(R.string.rep_location_format), wizard.specialLocation)}$bradenText

            ${context.getString(R.string.rep_plan_title)}
            $aiPlan$bradenPrevencion

            ${context.getString(R.string.rep_product_title)}
            - $productoSeleccionado
        """.trimIndent()
    }
    /**
     * Ejecuta la búsqueda de apósitos basados en los parámetros clínicos
     * actuales almacenados en el estado de la UI.
     */
    fun buscarAposito() {
        val wizard = _wizardState.value
        val eval = _evaluationState.value
        val config = _configState.value
        
        _evaluationState.update { it.copy(isLoading = true, noMatchFound = false) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = evaluateWoundUseCase.getClinicalRecommendation(_wizardState.value)

            if (result.familiaRecomendada != null) {
                _evaluationState.update {
                    it.copy(
                        familiaRecomendada = result.familiaRecomendada,
                        cureFrequency = result.cureFrequency,
                        productos = result.productos,
                        safetyAlerts = result.safetyAlerts,
                        showResults = true,
                        isLoading = false,
                        noMatchFound = result.productos.isEmpty(),
                        aiResponse = null,
                        isAiLoading = true
                    )
                }

                viewModelScope.launch(Dispatchers.IO) {
                    val respuesta = evaluateWoundUseCase.getAiExplanation(_wizardState.value, result.familiaRecomendada)
                    _evaluationState.update { it.copy(aiResponse = respuesta, isAiLoading = false) }
                }
            } else {
                _evaluationState.update {
                    it.copy(
                        familiaRecomendada = null,
                        cureFrequency = null,
                        productos = emptyList(),
                        safetyAlerts = result.safetyAlerts,
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
        _wizardState.update {
            it.copy(currentWizardStep = WizardStep.ETIOLOGY)
        }
        _evaluationState.update {
            it.copy(
                showResults = false,
                familiaRecomendada = null,
                cureFrequency = null,
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
        _configState.update { it.copy(currentLanguage = lang) }
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags(lang))
    }

    /**
     * Cambia el tema de la aplicación de forma persistente.
     * @param theme Código del tema ("light", "dark", "system").
     */
    fun changeTheme(theme: String) {
        sharedPrefs.edit().putString("theme", theme).apply()
        _configState.update { it.copy(currentTheme = theme) }
    }

    /**
     * Marca el disclaimer como aceptado para que no vuelva a aparecer.
     */
    fun acceptDisclaimer() {
        sharedPrefs.edit().putBoolean("has_seen_disclaimer", true).apply()
        _configState.update { it.copy(hasSeenDisclaimer = true) }
    }

    /**
     * Muestra u oculta el diálogo para sugerir productos.
     */
    fun setAddProductDialogVisibility(show: Boolean) {
        _configState.update { it.copy(showAddProductDialog = show) }
    }

    /**
     * Limpia el mensaje de resultado del formulario (por ej. al cerrar el Snackbar).
     */
    fun clearFormResultMsg() {
        _configState.update { it.copy(formResultMsg = null, formSuccess = false) }
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
        context: android.content.Context
    ) {
        _configState.update { it.copy(isFormSubmitting = true, formResultMsg = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val payload = com.ferlagod.miscuras.network.FormPayload(
                name = name,
                is_health_professional = if (isHealthProfessional) "Sí" else "No",
                belongs_to_laboratory = if (belongsToLaboratory) "Sí" else "No",
                product_name = productName,
                wound_bed = woundBed,
                exudate_level = exudateLevel,
                other_suggestions = otherSuggestions
            )
            
            val result = feedbackRepository.submitProductSuggestion(payload)
            
            if (result.isSuccess) {
                _configState.update {
                    it.copy(
                        isFormSubmitting = false,
                        showAddProductDialog = false,
                        formResultMsg = context.getString(R.string.form_success_msg),
                        formSuccess = true
                    )
                }
            } else {
                val error = result.exceptionOrNull()
                _configState.update {
                    it.copy(
                        isFormSubmitting = false,
                        formResultMsg = "${context.getString(R.string.form_error_msg)}\nException: ${error?.localizedMessage}",
                        formSuccess = false
                    )
                }
            }
        }
    }

    fun saveEvaluation(woundId: Long) {
        val wizard = _wizardState.value
        val eval = _evaluationState.value
        val config = _configState.value
        viewModelScope.launch(Dispatchers.IO) {
            val evaluation = EvaluationEntity(
                woundId = woundId,
                length = wizard.woundLength,
                width = wizard.woundWidth,
                depth = wizard.woundDepth,
                hasCavitation = wizard.hasCavitation,
                cavitationDetails = wizard.cavitationDetails,
                etiology = wizard.selectedEtiology,
                bedState = wizard.selectedLecho,
                exudateLevel = wizard.selectedExudado,
                exudateType = wizard.selectedExudateType,
                infection = wizard.selectedInfeccion,
                infectionGerm = wizard.infectionGerm,
                painLevel = wizard.painLevel,
                edges = wizard.selectedBordes,
                perilesional = wizard.selectedPerilesional,
                recommendedTreatment = "${eval.familiaRecomendada ?: ""}\n\nPauta recomendada: ${eval.cureFrequency ?: ""}",
                aiExplanation = eval.aiResponse ?: "",
                photoPath = wizard.photoPath,
                selectedProducts = eval.selectedTreatmentProducts.joinToString(",")
            )
            patientDao.insertEvaluation(evaluation)
            _evaluationState.update { it.copy(isSaved = true) }
        }
    }

    fun setPhotoPath(path: String) {
        _wizardState.update { it.copy(photoPath = path) }
    }

    fun resetWizard(woundId: Long = -1L) {
        val currentLang = _configState.value.currentLanguage
        val currentTheme = _configState.value.currentTheme
        val seenDisclaimer = _configState.value.hasSeenDisclaimer
        
        _wizardState.value = WizardState()
        _evaluationState.value = EvaluationState()
        _configState.value = ConfigState(
            currentLanguage = currentLang,
            currentTheme = currentTheme,
            hasSeenDisclaimer = seenDisclaimer,
            showSplash = false
        )

        if (woundId != -1L) {
            viewModelScope.launch(Dispatchers.IO) {
                val latestEval = patientDao.getLatestEvaluationForWound(woundId)
                if (latestEval != null) {
                    _wizardState.update {
                        it.copy(
                            selectedEtiology = latestEval.etiology,
                            selectedLecho = latestEval.bedState,
                            selectedExudado = latestEval.exudateLevel,
                            selectedExudateType = latestEval.exudateType,
                            selectedInfeccion = latestEval.infection,
                            infectionGerm = latestEval.infectionGerm,
                            painLevel = latestEval.painLevel,
                            selectedBordes = latestEval.edges,
                            selectedPerilesional = latestEval.perilesional,
                            woundLength = latestEval.length,
                            woundWidth = latestEval.width,
                            woundDepth = latestEval.depth,
                            hasCavitation = latestEval.hasCavitation,
                            cavitationDetails = latestEval.cavitationDetails
                        )
                    }
                }
            }
        }
    }

    fun resetSavedState() {
        _evaluationState.update { it.copy(isSaved = false) }
    }

    fun toggleProductSelection(codigoCn: String) {
        _evaluationState.update { currentState ->
            val newSelection = if (currentState.selectedTreatmentProducts.contains(codigoCn)) {
                currentState.selectedTreatmentProducts - codigoCn
            } else {
                currentState.selectedTreatmentProducts + codigoCn
            }
            currentState.copy(selectedTreatmentProducts = newSelection)
        }
    }


}
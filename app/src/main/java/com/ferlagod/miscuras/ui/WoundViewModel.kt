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
data class WoundUiState(
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
    val bradenScore: Int? = null,
    val familiaRecomendada: String? = null,
    val productos: List<ApositoEntity> = emptyList(),
    val showResults: Boolean = false,
    val isLoading: Boolean = false,
    val noMatchFound: Boolean = false,
    val safetyAlerts: List<String> = emptyList(),
    val showSplash: Boolean = true,
    val showArMeasure: Boolean = false,
    val showBraden: Boolean = false,
    val painLevel: Float = 0f,
    val aiResponse: String? = null,
    val isAiLoading: Boolean = false,
    val showGlossary: Boolean = false,
    val currentLanguage: String = "es",
    val currentTheme: String = "system", // system, light, dark
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
/**
 * ViewModel principal para el flujo de evaluación de heridas.
 * Gestiona el estado de la UI ([WoundUiState]), procesa la lógica de negocio, reglas clínicas y obtiene recomendaciones desde el repositorio.
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
            val savedTheme = sharedPrefs.getString("theme", "system") ?: "system"
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
        val current = _uiState.value.currentWizardStep
        val values = WizardStep.values()
        
        var nextOrdinal = current.ordinal + 1
        if (nextOrdinal >= values.size) return
        
        var nextStep = values[nextOrdinal]
        val state = _uiState.value
        
        // Branching logic
        if (current == WizardStep.ETIOLOGY && state.selectedLecho == "Piel Intacta (Prevención)") {
            buscarAposito()
            return
        }
        
        _uiState.update { it.copy(currentWizardStep = nextStep) }
    }

    fun previousStep() {
        val current = _uiState.value.currentWizardStep
        val values = WizardStep.values()
        
        var prevOrdinal = current.ordinal - 1
        if (prevOrdinal < 0) return
        
        var prevStep = values[prevOrdinal]
        val state = _uiState.value
        
        // Reverse branching logic
        if (current == WizardStep.INFECTION && state.selectedLecho == "Piel Intacta (Prevención)") {
            prevStep = WizardStep.ETIOLOGY
        }
        
        _uiState.update { it.copy(currentWizardStep = prevStep) }
    }

    /**
     * Actualiza la etiología seleccionada.
     */
    fun onEtiologyChanged(etiology: String) {
        _uiState.update { it.copy(selectedEtiology = etiology) }
    }

    /**
     * Actualiza el estado clínico del lecho de la herida seleccionado.
     * @param lecho El nuevo estado del lecho.
     */
    fun onLechoChanged(lecho: String) {
        _uiState.update { 
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
        _uiState.update { it.copy(selectedExudado = exudado) }
    }

    fun onExudateTypeChanged(type: String) {
        _uiState.update { it.copy(selectedExudateType = type) }
    }

    /**
     * Actualiza si la herida presenta signos clínicos de infección.
     * @param infeccion Verdadero si hay signos de infección, falso en caso contrario.
     */
    fun onInfeccionChanged(infeccion: Boolean) {
        _uiState.update { 
            it.copy(
                selectedInfeccion = infeccion,
                infectionGerm = if (infeccion) "Desconocido" else it.infectionGerm
            )
        }
    }

    fun onWoundLengthChanged(length: String) {
        _uiState.update { it.copy(woundLength = length) }
    }

    fun onWoundWidthChanged(width: String) {
        _uiState.update { it.copy(woundWidth = width) }
    }

    fun onWoundDepthChanged(depth: String) {
        _uiState.update { it.copy(woundDepth = depth) }
    }

    fun onHasCavitationChanged(hasCavitation: Boolean) {
        _uiState.update { it.copy(hasCavitation = hasCavitation) }
    }

    fun onCavitationDetailsChanged(details: String) {
        _uiState.update { it.copy(cavitationDetails = details) }
    }

    fun onSpecialLocationChanged(location: String) {
        _uiState.update { it.copy(specialLocation = location) }
    }

    fun setSpecialLocation(location: String) {
        _uiState.update { it.copy(specialLocation = location) }
    }

    fun showGlossary() {
        _uiState.update { it.copy(showGlossary = true) }
    }

    fun hideGlossary() {
        _uiState.update { it.copy(showGlossary = false) }
    }

    fun showArMeasure() {
        _uiState.update { it.copy(showArMeasure = true) }
    }

    fun hideArMeasure() {
        _uiState.update { it.copy(showArMeasure = false) }
    }

    fun showBraden() {
        _uiState.update { it.copy(showBraden = true) }
    }

    fun hideBraden() {
        _uiState.update { it.copy(showBraden = false) }
    }

    fun onPainLevelChanged(pain: Float) {
        _uiState.update { it.copy(painLevel = pain) }
    }

    fun onArMeasured(lengthCm: Float, widthCm: Float) {
        val lengthStr = String.format(java.util.Locale.US, "%.1f", lengthCm)
        val widthStr = String.format(java.util.Locale.US, "%.1f", widthCm)
        _uiState.update { 
            it.copy(
                woundLength = lengthStr, 
                woundWidth = widthStr,
                showArMeasure = false
            ) 
        }
    }

    fun onInfectionGermChanged(germ: String) {
        _uiState.update { it.copy(infectionGerm = germ) }
    }

    fun onBordesChanged(bordes: String) {
        _uiState.update { it.copy(selectedBordes = bordes) }
    }

    fun onPerilesionalChanged(peri: String) {
        _uiState.update { it.copy(selectedPerilesional = peri) }
    }

    fun onBradenScoreUpdated(score: Int) {
        _uiState.update { it.copy(bradenScore = score) }
    }

    /**
     * Genera un resumen clínico estructurado basado en los criterios TIME
     */
    fun generarResumenEvolutivo(productoSeleccionado: String, strings: AppStrings): String {
        val state = uiState.value
        val infText = if (state.selectedInfeccion) String.format(strings.repInfYesFormat, state.infectionGerm) else strings.no
        val tamaño = if (state.woundLength.isNotEmpty() && state.woundWidth.isNotEmpty()) {
            val depthStr = if (state.woundDepth.isNotEmpty()) " x ${state.woundDepth}" else ""
            "${state.woundLength} x ${state.woundWidth}$depthStr cm"
        } else {
            strings.repUnspecified
        }
        
        val aiPlan = state.aiResponse ?: strings.repPending

        val bradenText = if (state.bradenScore != null) {
            val riskText = when {
                state.bradenScore >= 15 -> strings.bradenRiskLow
                state.bradenScore in 13..14 -> strings.bradenRiskModerate
                state.bradenScore in 10..12 -> strings.bradenRiskHigh
                else -> strings.bradenRiskVeryHigh
            }
            "\n            ${strings.repBradenTitle}\n            ${String.format(strings.repBradenScoreFormat, state.bradenScore, riskText)}\n"
        } else ""

        val bradenPrevencion = if (state.bradenScore != null && state.bradenScore < 12) {
            "\n            ${strings.repBradenPreventiveTitle}\n            ${strings.repBradenPreventiveText}\n"
        } else ""

        return """
            ${strings.repTimersTitle}
            ${strings.etiologyLabel}: ${state.selectedEtiology}
            ${strings.repTissue}${state.selectedLecho}
            ${strings.repInfection}$infText
            ${strings.repMoisture}${state.selectedExudado} (${state.selectedExudateType})
            ${String.format(strings.repEdgesFormat, state.selectedBordes, state.selectedPerilesional)}
            ${String.format(strings.repPainFormat, state.painLevel.toInt())}
            ${String.format(strings.repSizeFormat, tamaño)}${if (state.hasCavitation) "\n            - Cavitaciones: ${if (state.cavitationDetails.isNotEmpty()) state.cavitationDetails else "Sí"}" else ""}
            ${String.format(strings.repLocationFormat, state.specialLocation)}$bradenText

            ${strings.repPlanTitle}
            $aiPlan$bradenPrevencion

            ${strings.repProductTitle}
            - $productoSeleccionado
        """.trimIndent()
    }
    /**
     * Ejecuta la búsqueda de apósitos basados en los parámetros clínicos
     * actuales almacenados en el estado de la UI.
     */
    fun buscarAposito() {
        val state = _uiState.value
        
        val alerts = mutableListOf<String>()
        if (state.selectedEtiology == "Úlcera Arterial") {
            alerts.add("Alerta Crítica: En úlceras arteriales el desbridamiento está contraindicado sin valoración vascular previa. Evite la terapia compresiva.")
        }
        if (state.selectedEtiology == "Úlcera Venosa") {
            alerts.add("Aviso: En úlceras venosas, la terapia de compresión es el pilar fundamental del tratamiento si el índice tobillo-brazo (ITB) es adecuado.")
        }
        if (state.selectedEtiology == "Pie Diabético") {
            alerts.add("Aviso: En pie diabético, la descarga eficaz de la presión y el control glucémico son esenciales para la cicatrización.")
        }

        if (state.selectedInfeccion) {
            alerts.add("Precaución: Evitar apósitos oclusivos como hidrocoloides. Priorizar apósitos de plata, DACC o cadexómero yodado.")
        }
        if (state.selectedLecho == "Necrosis") {
            alerts.add("Recordatorio: No aplicar desbridamiento enzimático (colagenasa) junto con apósitos de plata porque se inactiva la enzima.")
            if (state.selectedExudado == "Nulo") {
                alerts.add("Alerta: Ante necrosis seca con sospecha de isquemia, evite el desbridamiento y valore derivación a especialista/cirugía vascular.")
            }
        }
        if (state.selectedExudado == "Alto") {
            alerts.add("Aviso: Ante exudado alto, vigilar maceración en bordes. Considerar películas barrera no irritantes.")
        }
        if (state.painLevel >= 4f) {
            alerts.add("Aviso: Dolor significativo (${state.painLevel.toInt()}/10). Priorizar apósitos atraumáticos (bordes de silicona suave, hidrogeles) y evitar gasas adherentes.")
        }
        if (state.woundDepth.isNotEmpty() || state.hasCavitation) {
            alerts.add("Aviso: Herida cavitada o con profundidad. Considere apósitos de relleno (cintas de alginato/hidrofibra) para el lecho de la herida antes de aplicar el apósito secundario para evitar espacios muertos.")
        }
        
        _uiState.update { it.copy(isLoading = true, noMatchFound = false, safetyAlerts = alerts) }

        viewModelScope.launch(Dispatchers.IO) {
            val familia = repository.obtenerRecomendacion(
                state.selectedLecho,
                state.selectedExudado,
                state.selectedInfeccion
            )

            if (familia != null) {
                // --- NUEVA LÓGICA DE MICROORGANISMOS ---
                var familiaModificada = familia
                    if (state.selectedInfeccion && state.infectionGerm != "Desconocido") {
                        val germ = state.infectionGerm
                        // Dependiendo del germen, forzamos la búsqueda de familias específicas además o en lugar de la genérica
                        val nuevasFamilias = mutableSetOf<String>()
                        
                        // Añadir la familia base que corresponde al nivel de exudado (para conservar la textura/absorción correcta)
                        // Por ejemplo, si es "Plata / Alginato", queremos conservar que es un Alginato si el exudado es alto.
                        nuevasFamilias.addAll(familia.split("/").map { it.trim() })
                        
                        when (germ) {
                            "Pseudomonas aeruginosa" -> {
                                nuevasFamilias.add("Plata")
                                nuevasFamilias.add("Cadexómero Yodado")
                                nuevasFamilias.add("Limpieza de heridas") // Prontosan (PHMB)
                                nuevasFamilias.add("Alginogel") // Flaminal
                            }
                            "MRSA" -> {
                                nuevasFamilias.add("Plata")
                                nuevasFamilias.add("Limpieza de heridas") // Prontosan (PHMB)
                                nuevasFamilias.add("Alginogel") // Flaminal
                                nuevasFamilias.add("Malla DACC") // Cutimed Sorbact
                            }
                            "Candida albicans" -> {
                                nuevasFamilias.add("Plata")
                                nuevasFamilias.add("Limpieza de heridas")
                                nuevasFamilias.add("Malla DACC") // Cutimed Sorbact (muy eficaz contra hongos)
                            }
                            "Acinetobacter" -> {
                                nuevasFamilias.add("Plata")
                                nuevasFamilias.add("Limpieza de heridas") // PHMB
                                nuevasFamilias.add("Malla DACC") // Cutimed Sorbact
                            }
                            "Biofilm complejo" -> {
                                nuevasFamilias.add("Cadexómero Yodado")
                                nuevasFamilias.add("Limpieza de heridas")
                                nuevasFamilias.add("Plata")
                                nuevasFamilias.add("Alginogel")
                            }
                        }
                        
                        // Reescribimos 'familiaModificada'
                        familiaModificada = nuevasFamilias.joinToString(" / ")
                    }

                    val productosBrutos = repository.obtenerProductosPorFamilias(familiaModificada)
                    
                    val wLength = state.woundLength.replace(",", ".").toFloatOrNull()
                val wWidth = state.woundWidth.replace(",", ".").toFloatOrNull()
                val hasSizeInfo = wLength != null && wWidth != null
                val hasLocationInfo = state.specialLocation != "Ninguno"
                val margin = 4.0f // 4 cm de margen total (2cm por lado)

                val productos = productosBrutos.filter { producto ->
                    val dimStr = producto.dimensiones.lowercase()
                    val nomStr = producto.nombreComercial.lowercase()
                    
                    val isGeneric = dimStr.contains("pomada") || dimStr.contains("crema") || 
                        dimStr.contains("gel") || dimStr.contains("ml") || 
                        dimStr.contains("spray") || Regex("\\d+g").containsMatchIn(dimStr) ||
                        nomStr.contains("cinta") || nomStr.contains("paching")

                    if (isGeneric) return@filter true
                    if (!hasSizeInfo && !hasLocationInfo) return@filter true

                    var validByLocation = false
                    if (hasLocationInfo) {
                        val locStr = state.specialLocation.lowercase()
                        if ((locStr == "talón" && dimStr.contains("talón")) || 
                            (locStr == "sacro" && dimStr.contains("sacro"))) {
                            validByLocation = true
                        }
                    }

                    var validBySize = false
                    if (hasSizeInfo) {
                        val regex = Regex("([0-9]+(?:\\.[0-9]+)?(?:,[0-9]+)?)[xX]([0-9]+(?:\\.[0-9]+)?(?:,[0-9]+)?)")
                        val matches = regex.findAll(dimStr)
                        for (match in matches) {
                            val dim1 = match.groupValues[1].replace(",", ".").toFloatOrNull() ?: continue
                            val dim2 = match.groupValues[2].replace(",", ".").toFloatOrNull() ?: continue
                            
                            val fitsNormal = dim1 >= (wWidth!! + margin) && dim2 >= (wLength!! + margin)
                            val fitsRotated = dim1 >= (wLength!! + margin) && dim2 >= (wWidth!! + margin)
                            
                            if (fitsNormal || fitsRotated) {
                                validBySize = true
                                break
                            }
                        }
                    }

                    if (hasLocationInfo && hasSizeInfo) {
                        validByLocation || validBySize
                    } else if (hasLocationInfo) {
                        validByLocation
                    } else if (hasSizeInfo) {
                        validBySize
                    } else {
                        true
                    }
                }.sortedByDescending { producto ->
                    val dimStr = producto.dimensiones.lowercase()
                    val locStr = state.specialLocation.lowercase()
                    if (state.specialLocation != "Ninguno" && ((locStr == "talón" && dimStr.contains("talón")) || (locStr == "sacro" && dimStr.contains("sacro")))) {
                        1
                    } else {
                        0
                    }
                }

                val familiaFormateada = familiaModificada.split("/").joinToString(" y ") { it.trim() }
                _uiState.update {
                    it.copy(
                        familiaRecomendada = familiaFormateada,
                        productos = productos,
                        showResults = true,
                        isLoading = false,
                        noMatchFound = productos.isEmpty(),
                        aiResponse = null,
                        isAiLoading = true
                    )
                }

                viewModelScope.launch(Dispatchers.IO) {
                    val cacheKey = "${state.selectedLecho}|${state.selectedExudado}|${state.selectedExudateType}|${state.selectedInfeccion}|${state.infectionGerm}|${state.woundLength}|${state.woundWidth}|${state.woundDepth}|${state.hasCavitation}|${state.cavitationDetails}|${state.selectedBordes}|${state.selectedPerilesional}|${familiaFormateada}|${state.painLevel.toInt()}|${state.specialLocation}"
                    val cachedResponse = repository.getCachedAiResponse(cacheKey)

                    if (cachedResponse != null) {
                        _uiState.update { it.copy(aiResponse = cachedResponse, isAiLoading = false) }
                    } else {
                        val respuesta = com.ferlagod.miscuras.network.AsistenteIA().obtenerExplicacionEducativa(
                            etiologia = state.selectedEtiology,
                            lecho = state.selectedLecho,
                            exudado = state.selectedExudado,
                            tipoExudado = state.selectedExudateType,
                            infeccion = state.selectedInfeccion,
                            germen = state.infectionGerm,
                            tamanoLargo = state.woundLength,
                            tamanoAncho = state.woundWidth,
                            tamanoProfundidad = state.woundDepth,
                            tieneCavitacion = state.hasCavitation,
                            detallesCavitacion = state.cavitationDetails,
                            bordes = state.selectedBordes,
                            zonaEspecial = state.specialLocation,
                            pielPerilesional = state.selectedPerilesional,
                            recomendacionBD = familiaFormateada,
                            dolor = state.painLevel.toInt()
                        )
                        if (respuesta != null) {
                            repository.saveCachedAiResponse(cacheKey, respuesta)
                        }
                        _uiState.update { it.copy(aiResponse = respuesta, isAiLoading = false) }
                    }
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
                currentWizardStep = WizardStep.ETIOLOGY,
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
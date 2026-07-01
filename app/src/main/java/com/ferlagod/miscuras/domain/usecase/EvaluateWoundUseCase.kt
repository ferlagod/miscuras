package com.ferlagod.miscuras.domain.usecase

import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.data.repository.ApositosRepository
import com.ferlagod.miscuras.domain.rules.RulesEngine
import com.ferlagod.miscuras.network.AsistenteIA
import com.ferlagod.miscuras.ui.WizardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class EvaluationResult(
    val familiaRecomendada: String?,
    val cureFrequency: String?,
    val productos: List<ApositoEntity>,
    val safetyAlerts: List<String>
)

class EvaluateWoundUseCase(
    private val repository: ApositosRepository,
    private val rulesEngine: RulesEngine
) {

    suspend fun getClinicalRecommendation(state: WizardState): EvaluationResult = withContext(Dispatchers.IO) {
        val alerts = rulesEngine.generateSafetyAlerts(state).toMutableList()
        
        val familia = repository.obtenerRecomendacion(
            state.selectedLecho,
            state.selectedExudado,
            state.selectedInfeccion
        )

        if (familia != null) {
            val familiaConGermen = rulesEngine.applyGermOverrides(
                baseFamilies = familia,
                isSuperficialInfection = state.selectedInfeccion,
                germ = state.infectionGerm
            )
            val familiaModificada = rulesEngine.applyEtiologyOverrides(
                baseFamilies = familiaConGermen,
                etiology = state.selectedEtiology,
                state = state
            )
            
            alerts.addAll(rulesEngine.generatePostRecommendationAlerts(state, familiaModificada))

            val productosBrutos = repository.obtenerProductosPorFamilias(familiaModificada)
                
            val wLength = state.woundLength.replace(",", ".").toFloatOrNull()
            val wWidth = state.woundWidth.replace(",", ".").toFloatOrNull()
            
            var productos = rulesEngine.filterProductsByDimensions(
                products = productosBrutos,
                woundLength = wLength,
                woundWidth = wWidth,
                specialLocation = state.specialLocation
            ).sortedByDescending { producto ->
                val dimStr = producto.dimensiones.lowercase()
                val locStr = state.specialLocation.lowercase()
                if (state.specialLocation != "Ninguno" && ((locStr == "talón" && dimStr.contains("talón")) || (locStr == "sacro" && dimStr.contains("sacro")))) {
                    1
                } else {
                    0
                }
            }

            // Fallback para heridas gigantes
            if (productos.isEmpty() && productosBrutos.isNotEmpty() && wLength != null && wWidth != null) {
                // Recuperar los apósitos más grandes disponibles que no sean genéricos
                productos = productosBrutos.filter { p ->
                    val d = p.dimensiones.lowercase()
                    !(d.contains("pomada") || d.contains("crema") || d.contains("gel") || d.contains("ml") || 
                      d.contains("spray") || Regex("\\d+g").containsMatchIn(d) || d.contains("solucion") || 
                      d.contains("venda") || d.contains("kit"))
                }.sortedByDescending { p ->
                    val match = Regex("(\\d+(?:\\.\\d+)?)\\s*x\\s*(\\d+(?:\\.\\d+)?)").find(p.dimensiones.lowercase())
                    if (match != null) match.groupValues[1].toFloat() * match.groupValues[2].toFloat() else 0f
                }.take(3) // Tomar los 3 más grandes
                
                if (productos.isNotEmpty()) {
                    alerts.add("Aviso: Las dimensiones de la herida superan el apósito máximo disponible. Considere utilizar múltiples apósitos solapados.")
                }
            }

            val familiaFormateada = familiaModificada.split("/").joinToString(" y ") { it.trim() }
            val recommendedFrequency = rulesEngine.determineCureFrequency(state)

            EvaluationResult(
                familiaRecomendada = familiaFormateada,
                cureFrequency = recommendedFrequency,
                productos = productos,
                safetyAlerts = alerts
            )
        } else {
            EvaluationResult(
                familiaRecomendada = null,
                cureFrequency = null,
                productos = emptyList(),
                safetyAlerts = alerts
            )
        }
    }

    private fun String.sha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun getAiExplanation(state: WizardState, familiaFormateada: String): String? = withContext(Dispatchers.IO) {
        val rawKey = "${state.selectedLecho}|${state.selectedExudado}|${state.selectedExudateType}|${state.selectedInfeccion}|${state.infectionGerm}|${state.woundLength}|${state.woundWidth}|${state.woundDepth}|${state.hasCavitation}|${state.cavitationDetails}|${state.selectedBordes}|${state.selectedPerilesional}|${familiaFormateada}|${state.painLevel.toInt()}|${state.specialLocation}"
        val cacheKey = rawKey.sha256()
        val cachedResponse = repository.getCachedAiResponse(cacheKey)

        if (cachedResponse != null) {
            return@withContext cachedResponse
        }

        val respuesta = AsistenteIA.obtenerExplicacionEducativa(
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
        
        return@withContext respuesta
    }
}

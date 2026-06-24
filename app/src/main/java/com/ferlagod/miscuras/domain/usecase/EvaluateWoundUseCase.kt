package com.ferlagod.miscuras.domain.usecase

import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.data.repository.ApositosRepository
import com.ferlagod.miscuras.domain.rules.RulesEngine
import com.ferlagod.miscuras.network.AsistenteIA
import com.ferlagod.miscuras.ui.WoundUiState
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

    suspend fun getClinicalRecommendation(state: WoundUiState): EvaluationResult = withContext(Dispatchers.IO) {
        val alerts = rulesEngine.generateSafetyAlerts(state)
        
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
                etiology = state.selectedEtiology
            )

            val productosBrutos = repository.obtenerProductosPorFamilias(familiaModificada)
                
            val wLength = state.woundLength.replace(",", ".").toFloatOrNull()
            val wWidth = state.woundWidth.replace(",", ".").toFloatOrNull()
            
            val productos = rulesEngine.filterProductsByDimensions(
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

    suspend fun getAiExplanation(state: WoundUiState, familiaFormateada: String): String? = withContext(Dispatchers.IO) {
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

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
package com.ferlagod.miscuras.domain.usecase

import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.data.repository.ApositosRepository
import com.ferlagod.miscuras.domain.rules.RulesEngine
import com.ferlagod.miscuras.network.AsistenteIA
import com.ferlagod.miscuras.ui.WizardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Resultado encapsulado tras evaluar una herida usando el motor de reglas.
 */
data class EvaluationResult(
    val familiaRecomendada: String?,
    val cureFrequency: String?,
    val productos: List<ApositoEntity>,
    val safetyAlerts: List<String>
)

/**
 * Caso de uso que orquesta la evaluación de una herida y la generación de recomendaciones.
 */
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
            
            // Seguridad del paciente: alergias, antecedentes y antibióticos tópicos sin infección
            val safety = rulesEngine.filterProductsByPatientSafety(productosBrutos, state)
            alerts.addAll(safety.alerts)
            val productosSeguros = safety.productos

            var productos = rulesEngine.filterProductsByDimensions(
                products = productosSeguros,
                woundLength = wLength,
                woundWidth = wWidth,
                specialLocation = state.specialLocation
            ).sortedByDescending { producto ->
                if (rulesEngine.matchesAnatomicLocation(producto, state.specialLocation)) 1 else 0
            }

            // Fallback para heridas gigantes
            if (productos.isEmpty() && productosSeguros.isNotEmpty() && wLength != null && wWidth != null) {
                // Recuperar los apósitos más grandes disponibles que no sean genéricos
                productos = productosSeguros.filter { p ->
                    !rulesEngine.isGenericFormat(RulesEngine.normalize(p.dimensiones))
                }.sortedByDescending { p ->
                    val matches = Regex("(\\d+(?:[,.]\\d+)?)\\s*x\\s*(\\d+(?:[,.]\\d+)?)").findAll(p.dimensiones.lowercase())
                    matches.map { match ->
                        val l = match.groupValues[1].replace(",", ".").toFloatOrNull() ?: 0f
                        val w = match.groupValues[2].replace(",", ".").toFloatOrNull() ?: 0f
                        l * w
                    }.maxOrNull() ?: 0f
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
        val rawKey = "v2|${state.selectedEtiology}|${state.selectedLecho}|${state.selectedExudado}|${state.selectedExudateType}|${state.selectedInfeccion}|${state.infectionGerm}|${state.woundLength}|${state.woundWidth}|${state.woundDepth}|${state.hasCavitation}|${state.cavitationDetails}|${state.selectedBordes}|${state.selectedPerilesional}|${familiaFormateada}|${state.painLevel.toInt()}|${state.specialLocation}"
        val cacheKey = rawKey.sha256()
        val cachedResponse = repository.getCachedAiResponse(cacheKey)

        if (cachedResponse != null) {
            return@withContext cachedResponse
        }

        val (respuesta, esFallback) = AsistenteIA.obtenerExplicacionEducativa(
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
        
        // Solo se cachean respuestas reales de la IA: el texto de contingencia offline no debe
        // bloquear durante 7 días la explicación cuando vuelva la conexión.
        if (respuesta.isNotBlank() && !esFallback) {
            repository.saveCachedAiResponse(cacheKey, respuesta)
        }
        
        return@withContext respuesta
    }
}

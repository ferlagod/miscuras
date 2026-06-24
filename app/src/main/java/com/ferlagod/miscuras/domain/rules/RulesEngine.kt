package com.ferlagod.miscuras.domain.rules

import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.ui.WoundUiState

/**
 * Motor de reglas clínicas desacoplado del ViewModel.
 * Evalúa las alertas de seguridad, modificaciones por microorganismos y filtrado dimensional.
 */
class RulesEngine {

    /**
     * Genera la lista de alertas de seguridad basadas en el estado clínico de la herida.
     */
    fun generateSafetyAlerts(state: WoundUiState): List<String> {
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
        
        return alerts
    }

    /**
     * Modifica las familias de apósitos base según el germen infectante detectado.
     */
    fun applyGermOverrides(baseFamilies: String, isSuperficialInfection: Boolean, germ: String): String {
        if (!isSuperficialInfection || germ == "Desconocido") return baseFamilies

        val newFamilies = mutableSetOf<String>()
        newFamilies.addAll(baseFamilies.split("/").map { it.trim() })
        
        when (germ) {
            "Pseudomonas aeruginosa" -> {
                newFamilies.add("Plata")
                newFamilies.add("Cadexómero Yodado")
                newFamilies.add("Limpieza de heridas")
                newFamilies.add("Alginogel")
            }
            "MRSA" -> {
                newFamilies.add("Plata")
                newFamilies.add("Limpieza de heridas")
                newFamilies.add("Alginogel")
                newFamilies.add("Malla DACC")
            }
            "Candida albicans" -> {
                newFamilies.add("Plata")
                newFamilies.add("Limpieza de heridas")
                newFamilies.add("Malla DACC")
            }
            "Acinetobacter" -> {
                newFamilies.add("Plata")
                newFamilies.add("Limpieza de heridas")
                newFamilies.add("Malla DACC")
            }
            "Biofilm complejo" -> {
                newFamilies.add("Cadexómero Yodado")
                newFamilies.add("Limpieza de heridas")
                newFamilies.add("Plata")
                newFamilies.add("Alginogel")
            }
        }
        return newFamilies.joinToString(" / ")
    }

    /**
     * Agrega familias según la etiología seleccionada.
     */
    fun applyEtiologyOverrides(baseFamilies: String, etiology: String): String {
        if (etiology == "Úlcera Venosa") {
            val families = baseFamilies.split("/").map { it.trim() }.toMutableSet()
            families.add("Compresión Bicomponente")
            return families.joinToString(" / ")
        }
        return baseFamilies
    }

    /**
     * Filtra los productos brutos basándose en las dimensiones y la localización.
     */
    fun filterProductsByDimensions(
        products: List<ApositoEntity>,
        woundLength: Float?,
        woundWidth: Float?,
        specialLocation: String
    ): List<ApositoEntity> {
        val hasSizeInfo = woundLength != null && woundWidth != null
        val hasLocationInfo = specialLocation != "Ninguno"
        val margin = 4.0f // Margen de seguridad para asegurar cobertura completa
        
        return products.filter { producto ->
            val dimStr = producto.dimensiones.lowercase()
            val nomStr = producto.nombreComercial.lowercase()
            
            // Si es crema, gel, spray o similar no tiene dimensiones
            val isGeneric = dimStr.contains("pomada") || dimStr.contains("crema") || 
                dimStr.contains("gel") || dimStr.contains("ml") || 
                dimStr.contains("spray") || Regex("\\d+g").containsMatchIn(dimStr) ||
                dimStr.contains("solucion") || dimStr.contains("venda") || dimStr.contains("kit")
                
            if (isGeneric) return@filter true

            // Filtro por tamaño
            val matchesSize = if (hasSizeInfo && !isGeneric) {
                val requiredL = woundLength!! + margin
                val requiredW = woundWidth!! + margin
                val match = Regex("(\\d+(?:\\.\\d+)?)\\s*x\\s*(\\d+(?:\\.\\d+)?)").find(dimStr)
                if (match != null) {
                    val pL = match.groupValues[1].toFloat()
                    val pW = match.groupValues[2].toFloat()
                    (pL >= requiredL && pW >= requiredW) || (pW >= requiredL && pL >= requiredW)
                } else {
                    true // Fallback
                }
            } else true

            // Filtro por localización
            val matchesLocation = if (hasLocationInfo) {
                when (specialLocation) {
                    "Talón" -> dimStr.contains("talon") || dimStr.contains("heel") || nomStr.contains("talon")
                    "Sacro" -> dimStr.contains("sacro") || dimStr.contains("sacrum") || nomStr.contains("sacro")
                    "Codos/Rodillas" -> dimStr.contains("multisite") || dimStr.contains("flex") || dimStr.contains("borde") || dimStr.contains("lite")
                    else -> true
                }
            } else {
                !(dimStr.contains("talon") || dimStr.contains("sacro") || dimStr.contains("heel") || dimStr.contains("sacrum"))
            }

            matchesSize && matchesLocation
        }
    }

    /**
     * Determina la frecuencia recomendada para el cambio de apósitos
     * basándose en las directrices clínicas de exudado e infección.
     */
    fun determineCureFrequency(state: WoundUiState): String {
        return if (state.selectedInfeccion && state.selectedExudado == "Alto") {
            "Cada 24 horas (exudado alto e infección activa)."
        } else if (state.selectedInfeccion) {
            "Cada 24-48 horas (vigilar signos de infección local)."
        } else if (state.selectedExudado == "Alto") {
            "Cada 48 horas (riesgo de saturación y maceración)."
        } else if (state.selectedLecho == "Piel Intacta (Prevención)") {
            "Revisión por turno (8-12 horas) y reaplicación de ácidos grasos."
        } else if (state.selectedLecho == "Epitelización" && state.selectedExudado == "Nulo") {
            "Cada 5-7 días (favorecer reposo del tejido neoformado)."
        } else if (state.selectedExudado == "Nulo" || state.selectedExudado == "Bajo") {
            "Cada 3-4 días."
        } else {
            "Cada 48-72 horas (según saturación del apósito secundario)."
        }
    }
}

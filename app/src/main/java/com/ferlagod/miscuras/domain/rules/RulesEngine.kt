package com.ferlagod.miscuras.domain.rules

import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.ui.WizardState

/**
 * Motor de reglas clínicas desacoplado del ViewModel.
 * Evalúa las alertas de seguridad, modificaciones por microorganismos y filtrado dimensional.
 */
class RulesEngine {

    /**
     * Genera la lista de alertas de seguridad basadas en el estado clínico de la herida.
     */
    fun generateSafetyAlerts(state: WizardState): List<String> {
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

    fun generatePostRecommendationAlerts(state: WizardState, recommendedFamilies: String): List<String> {
        val alerts = mutableListOf<String>()
        val families = recommendedFamilies.split("/").map { it.trim() }

        if ((families.contains("Alginato") || families.contains("Hidrofibra")) && (state.selectedExudado == "Nulo" || state.selectedExudado == "Bajo")) {
            alerts.add("Precaución: El uso de alginatos o hidrofibras en heridas con escaso exudado puede desecar el lecho y adherirse al tejido. Considere usar apósitos pre-hidratados o mallas de contacto.")
        }

        if (families.contains("Cadexómero Yodado") && state.selectedExudado == "Nulo") {
            alerts.add("Precaución: El Cadexómero Yodado requiere humedad para activarse. Al tener exudado nulo, requiere combinarse con hidrogel o suero para activarse en la herida seca.")
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
    fun applyEtiologyOverrides(baseFamilies: String, etiology: String, state: WizardState): String {
        val families = baseFamilies.split("/").map { it.trim() }.toMutableSet()
        
        if (etiology == "Úlcera Venosa") {
            families.add("Compresión Bicomponente")
        } else if (etiology == "Úlcera Arterial") {
            families.remove("Compresión Bicomponente")
            families.remove("Vendaje Compresivo")
        }
        
        // Reglas de cavitación
        if (state.hasCavitation || state.woundDepth.isNotEmpty()) {
            if (!families.contains("Hidrofibra") && !families.contains("Alginato")) {
                families.add("Hidrofibra") // Añadimos por defecto hidrofibra para cavidades
            }
        }
        
        // Reglas de Cadexómero Yodado en necrosis seca
        if (families.contains("Cadexómero Yodado") && (state.selectedExudado == "Nulo" || state.selectedExudado == "Bajo")) {
            families.add("Hidrogel")
        }
        
        return families.joinToString(" / ")
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
                val matches = Regex("(\\d+(?:[,.]\\d+)?)\\s*x\\s*(\\d+(?:[,.]\\d+)?)").findAll(dimStr)
                if (matches.any()) {
                    matches.any { match ->
                        val pL = match.groupValues[1].replace(",", ".").toFloat()
                        val pW = match.groupValues[2].replace(",", ".").toFloat()
                        (pL >= requiredL && pW >= requiredW) || (pW >= requiredL && pL >= requiredW)
                    }
                } else {
                    true // Fallback
                }
            } else true

            // Filtro por localización (Flexible)
            val matchesLocation = if (hasLocationInfo) {
                val isAnatomic = when (specialLocation) {
                    "Talón" -> dimStr.contains("talon") || dimStr.contains("heel") || nomStr.contains("talon")
                    "Sacro" -> dimStr.contains("sacro") || dimStr.contains("sacrum") || nomStr.contains("sacro")
                    "Codos/Rodillas" -> dimStr.contains("multisite") || dimStr.contains("flex") || dimStr.contains("borde") || dimStr.contains("lite")
                    else -> true
                }
                // Permitimos los anatómicos O aquellos que no tienen reborde/silicona rígida (que puedan adaptarse)
                // O si no, simplemente no excluimos drásticamente si no son anatómicos, pero priorizamos luego
                true // No descartamos nada por localización de manera destructiva aquí, lo ordenamos después
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
    fun determineCureFrequency(state: WizardState): String {
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

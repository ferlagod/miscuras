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
package com.ferlagod.miscuras.domain.rules

import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.ui.WizardState
import java.text.Normalizer

/**
 * Resultado del filtrado de seguridad por paciente (alergias, antecedentes, antibióticos tópicos).
 */
data class SafetyFilterResult(
    val productos: List<ApositoEntity>,
    val alerts: List<String>
)

/**
 * Motor de reglas clínicas desacoplado del ViewModel.
 * Evalúa las alertas de seguridad, modificaciones por microorganismos, etiología,
 * alergias del paciente y filtrado dimensional.
 */
class RulesEngine {

    companion object {
        private val SILVER_FAMILIES = setOf("Plata", "Malla Plata", "Carbón y plata")
        private val SILVER_NAME_REGEX = Regex("\\bS?Ag\\b")

        /** Normaliza a minúsculas y sin tildes para comparaciones robustas. */
        fun normalize(text: String): String =
            Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
                .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

        /** Herida cavitada: marcada explícitamente o con profundidad numérica > 0. */
        fun isCavitated(state: WizardState): Boolean {
            val depth = state.woundDepth.replace(",", ".").trim().toFloatOrNull()
            return state.hasCavitation || (depth != null && depth > 0f)
        }

        /** Antibióticos tópicos (y combinaciones con antibiótico) detectados por su ficha. */
        fun isTopicalAntibiotic(p: ApositoEntity): Boolean {
            val d = normalize(p.descripcion)
            return d.contains("antibiotico topico") || d.contains("neomicina")
        }

        private fun containsSilver(p: ApositoEntity): Boolean {
            val d = normalize(p.descripcion)
            return p.familiaGenerica in SILVER_FAMILIES ||
                SILVER_NAME_REGEX.containsMatchIn(p.nombreComercial) ||
                d.contains("argentic") || d.contains("plata")
        }

        private fun isDressingFiller(p: ApositoEntity): Boolean {
            val d = normalize(p.dimensiones)
            val n = normalize(p.nombreComercial)
            return d.contains("cinta") || n.contains("cinta") || n.contains("packing") || n.contains("paching")
        }
    }

    /**
     * Genera la lista de alertas de seguridad basadas en el estado clínico de la herida.
     */
    fun generateSafetyAlerts(state: WizardState): List<String> {
        val alerts = mutableListOf<String>()

        if (state.selectedEtiology == "Úlcera Arterial") {
            alerts.add("Alerta Crítica: En úlceras arteriales el desbridamiento y la terapia compresiva están contraindicados sin valoración vascular previa (ITB). Se han excluido de la recomendación el desbridamiento, la TPN, la compresión y los hidrocoloides oclusivos.")
        }
        if (state.selectedEtiology == "Úlcera Venosa") {
            alerts.add("Aviso: En úlceras venosas la compresión es el pilar del tratamiento, pero solo tras medir el índice tobillo-brazo (ITB): compresión completa con ITB ≥ 0,8; reducida (p. ej. sistemas «Lite») con ITB 0,6-0,8; contraindicada con ITB < 0,6 o ante sospecha de componente arterial.")
        }
        if (state.selectedEtiology == "Pie Diabético") {
            alerts.add("Aviso: En pie diabético, la descarga eficaz de la presión y el control glucémico son esenciales. Se evitan los hidrocoloides oclusivos (pueden enmascarar una infección) y debe descartarse isquemia antes de desbridar.")
        }

        if (state.selectedInfeccion) {
            alerts.add("Precaución: Evitar apósitos oclusivos como hidrocoloides. Priorizar apósitos de plata, DACC o cadexómero yodado.")
        }

        if (state.selectedLecho == "Necrosis") {
            alerts.add("Recordatorio: No aplicar desbridamiento enzimático (colagenasa) junto con apósitos de plata ni yodo porque inactivan la enzima. La TPN no debe aplicarse sobre escara necrótica sin desbridar.")
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

        if (isCavitated(state)) {
            alerts.add("Aviso: Herida cavitada o con profundidad. Rellene el espacio muerto (cintas de alginato/hidrofibra si hay exudado; hidrogel si el lecho está seco) antes de aplicar el apósito secundario.")
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

        if (families.contains("Compresión Bicomponente")) {
            alerts.add("Precaución: Compresión incluida en la recomendación. No aplicar sin un ITB documentado en rango seguro.")
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
        newFamilies.remove("No Aplica")

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
     * Agrega o elimina familias según la etiología y la morfología de la herida.
     */
    fun applyEtiologyOverrides(baseFamilies: String, etiology: String, state: WizardState): String {
        val families = baseFamilies.split("/").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()

        when (etiology) {
            "Úlcera Venosa" -> families.add("Compresión Bicomponente")
            "Úlcera Arterial" -> {
                families.remove("Compresión Bicomponente")
                families.remove("Vendaje Compresivo")
                families.remove("Desbridante Enzimatico")
                families.remove("Desbridamiento Mecanico")
                families.remove("TPN")
                families.remove("Hidrocoloide")
            }
            "Pie Diabético" -> families.remove("Hidrocoloide")
        }

        // TPN nunca sobre escara necrótica sin desbridar
        if (state.selectedLecho == "Necrosis") families.remove("TPN")

        // Reglas de cavitación: rellenar el espacio muerto según la humedad del lecho
        if (isCavitated(state) && !families.contains("No Aplica")) {
            val lechoSeco = state.selectedExudado == "Nulo" || state.selectedExudado == "Bajo"
            if (lechoSeco) {
                families.add("Hidrogel")
            } else if (!families.contains("Hidrofibra") && !families.contains("Alginato")) {
                families.add("Hidrofibra")
            }
        }

        // Cadexómero Yodado en lecho seco necesita hidratación
        if (families.contains("Cadexómero Yodado") && (state.selectedExudado == "Nulo" || state.selectedExudado == "Bajo")) {
            families.add("Hidrogel")
        }

        if (families.isEmpty()) families.add("Limpieza de heridas")
        if (families.size > 1) families.remove("No Aplica")

        return families.joinToString(" / ")
    }

    /**
     * Excluye productos inseguros para el paciente concreto:
     * - Antibióticos tópicos cuando no hay infección (uso no recomendado por GNEAUPP/TIME).
     * - Productos con alérgenos registrados en el perfil del paciente.
     * - Cadexómero yodado ante patología tiroidea.
     */
    fun filterProductsByPatientSafety(products: List<ApositoEntity>, state: WizardState): SafetyFilterResult {
        val alerts = mutableListOf<String>()
        var result = products

        if (!state.selectedInfeccion) {
            val removed = result.filter { isTopicalAntibiotic(it) }
            if (removed.isNotEmpty()) {
                result = result - removed.toSet()
                alerts.add("Info: Se han omitido antibióticos tópicos (${removed.joinToString { it.nombreComercial }}) porque no hay signos de infección; su uso rutinario favorece resistencias.")
            }
        }

        val allergies = normalize(state.patientAllergies)
        val history = normalize(state.patientHistory)
        val excluded = linkedMapOf<String, MutableList<String>>()

        fun exclude(reason: String, predicate: (ApositoEntity) -> Boolean) {
            val removed = result.filter(predicate)
            if (removed.isNotEmpty()) {
                result = result - removed.toSet()
                excluded.getOrPut(reason) { mutableListOf() }.addAll(removed.map { it.nombreComercial })
            }
        }

        fun has(text: String, vararg keys: String) = keys.any { text.contains(it) }

        if (allergies.isNotBlank()) {
            if (has(allergies, "yod", "iod", "povidona", "betadin")) {
                exclude("alergia al yodo") { it.familiaGenerica == "Cadexómero Yodado" || normalize(it.descripcion).contains("yodo") }
            }
            if (has(allergies, "plata", "silver", "argent")) {
                exclude("alergia a la plata") { containsSilver(it) }
            }
            if (has(allergies, "sulf")) {
                exclude("alergia a sulfamidas") { normalize(it.descripcion).contains("sulfadiazina") }
            }
            if (has(allergies, "neomicin", "aminogluc")) {
                exclude("alergia a neomicina/aminoglucósidos") { normalize(it.descripcion).contains("neomicina") }
            }
            if (has(allergies, "fusid")) {
                exclude("alergia al ácido fusídico") { normalize(it.descripcion).contains("fusid") }
            }
            if (has(allergies, "nitrofur", "furacin")) {
                exclude("alergia a nitrofuranos") { normalize(it.descripcion).contains("nitrofura") }
            }
            if (has(allergies, "colagenasa")) {
                exclude("alergia a la colagenasa") { normalize(it.descripcion).contains("colagenasa") || normalize(it.nombreComercial).contains("colagenasa") }
            }
            if (has(allergies, "miel", "abeja", "propoleo", "honey")) {
                exclude("alergia a la miel/productos apícolas") { it.familiaGenerica == "Miel Médica" }
            }
            if (has(allergies, "polihexanida", "phmb", "prontosan")) {
                exclude("alergia a polihexanida (PHMB)") { normalize(it.descripcion).contains("polihexanida") }
            }
        }

        if (has(history, "tiroid", "tirox", "metimazol", "carbimazol", "graves", "hashimoto", "bocio")) {
            exclude("patología tiroidea (yodo)") { it.familiaGenerica == "Cadexómero Yodado" }
        }

        excluded.forEach { (reason, names) ->
            alerts.add("Alerta de Alergia: Excluidos por $reason: ${names.distinct().joinToString()}.")
        }
        if (state.patientAllergies.isNotBlank()) {
            alerts.add("Recordatorio: Alergias registradas del paciente: «${state.patientAllergies.trim()}». Verifique la composición de cada producto antes de aplicarlo.")
        }

        return SafetyFilterResult(result, alerts)
    }

    /**
     * Filtra los productos brutos basándose en las dimensiones y la localización.
     * - Los productos de relleno (cintas) y los de aplicación libre (geles, cremas, soluciones,
     *   almohadillas de desbridamiento) no se filtran por tamaño.
     * - Los apósitos primarios (que se recortan) solo deben cubrir la herida; los apósitos con
     *   borde o de cobertura deben sobrepasarla un margen de seguridad.
     */
    fun filterProductsByDimensions(
        products: List<ApositoEntity>,
        woundLength: Float?,
        woundWidth: Float?,
        specialLocation: String
    ): List<ApositoEntity> {
        val hasSizeInfo = woundLength != null && woundWidth != null
        val hasLocationInfo = specialLocation != "Ninguno"
        val coverMargin = 4.0f // Margen de seguridad para apósitos de cobertura/con borde

        return products.filter { producto ->
            val dimStr = normalize(producto.dimensiones)
            val nomStr = normalize(producto.nombreComercial)

            val isGeneric = isGenericFormat(dimStr) ||
                isDressingFiller(producto) ||
                producto.familiaGenerica == "Desbridamiento Mecanico"

            if (isGeneric) return@filter true

            val matchesSize = if (hasSizeInfo) {
                val uso = normalize(producto.usoPrimarioSecundario)
                val margin = if (uso.contains("ambos") || uso.contains("secundari")) coverMargin else 0f
                val requiredL = woundLength!! + margin
                val requiredW = woundWidth!! + margin
                val matches = DIM_REGEX.findAll(dimStr)
                if (matches.any()) {
                    matches.any { match ->
                        val pL = match.groupValues[1].replace(",", ".").toFloat()
                        val pW = match.groupValues[2].replace(",", ".").toFloat()
                        (pL >= requiredL && pW >= requiredW) || (pW >= requiredL && pL >= requiredW)
                    }
                } else {
                    true // Sin dimensiones interpretables
                }
            } else true

            // Sin localización especial se excluyen los apósitos anatómicos (talón/sacro);
            // con localización no se descarta nada y se prioriza al ordenar.
            val matchesLocation = if (hasLocationInfo) {
                true
            } else {
                !isAnatomic(dimStr, nomStr)
            }

            matchesSize && matchesLocation
        }
    }

    /** Indica si el producto es anatómico para la localización indicada (para ordenar). */
    fun matchesAnatomicLocation(producto: ApositoEntity, specialLocation: String): Boolean {
        val dimStr = normalize(producto.dimensiones)
        val nomStr = normalize(producto.nombreComercial)
        return when (normalize(specialLocation)) {
            "talon" -> dimStr.contains("talon") || dimStr.contains("heel") || nomStr.contains("talon") || nomStr.contains("heel")
            "sacro" -> dimStr.contains("sacro") || dimStr.contains("sacrum") || nomStr.contains("sacro") || nomStr.contains("sacrum")
            else -> false
        }
    }

    fun isGenericFormat(normalizedDims: String): Boolean =
        normalizedDims.contains("pomada") || normalizedDims.contains("crema") ||
            normalizedDims.contains("gel") || normalizedDims.contains("ml") ||
            normalizedDims.contains("spray") || Regex("\\d+\\s*g\\b").containsMatchIn(normalizedDims) ||
            normalizedDims.contains("solucion") || normalizedDims.contains("venda") || normalizedDims.contains("kit")

    private fun isAnatomic(dimStr: String, nomStr: String): Boolean =
        listOf("talon", "heel", "sacro", "sacrum").any { dimStr.contains(it) || nomStr.contains(it) }

    private val DIM_REGEX = Regex("(\\d+(?:[,.]\\d+)?)\\s*x\\s*(\\d+(?:[,.]\\d+)?)")

    /**
     * Determina la frecuencia recomendada para el cambio de apósitos
     * basándose en las directrices clínicas de exudado e infección.
     */
    /**
     * Intervalo máximo (horas) hasta la siguiente cura, coherente con [determineCureFrequency].
     * Se usa el extremo superior del rango para avisar de curas vencidas.
     */
    fun determineCureIntervalHours(state: WizardState): Int = when {
        state.selectedInfeccion && state.selectedExudado == "Alto" -> 24
        state.selectedInfeccion -> 48
        state.selectedExudado == "Alto" -> 48
        state.selectedLecho == "Piel Intacta (Prevención)" -> 12
        state.selectedLecho == "Epitelización" && state.selectedExudado == "Nulo" -> 168
        state.selectedExudado == "Nulo" || state.selectedExudado == "Bajo" -> 96
        else -> 72
    }

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

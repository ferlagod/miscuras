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
package com.ferlagod.miscuras.ui

import android.content.Context
import com.ferlagod.miscuras.R

/**
 * Mapea términos clínicos a cadenas de recursos para su internacionalización.
 */
object ClinicalTermMapper {
    fun translateClinicalTerm(term: String, context: Context): String {
        return when (term.trim()) {
            "No especificada" -> context.getString(R.string.etiology_not_specified)
            "UPP" -> context.getString(R.string.etiology_upp)
            "Úlcera Venosa" -> context.getString(R.string.etiology_venous)
            "Úlcera Arterial" -> context.getString(R.string.etiology_arterial)
            "Pie Diabético" -> context.getString(R.string.etiology_diabetic)
            "Quirúrgica" -> context.getString(R.string.etiology_surgical)
            "Quemadura" -> context.getString(R.string.etiology_burn)
            "Traumática" -> context.getString(R.string.etiology_traumatic)
            "Indeterminada" -> context.getString(R.string.etiology_indeterminate)
            "Necrosis" -> context.getString(R.string.necrosis)
            "Esfacelo" -> context.getString(R.string.esfacelo)
            "Granulación" -> context.getString(R.string.granulacion)
            "Epitelización" -> context.getString(R.string.epitelizacion)
            "Piel Intacta (Prevención)" -> context.getString(R.string.piel_intacta)
            "Nulo" -> context.getString(R.string.nulo)
            "Bajo" -> context.getString(R.string.bajo)
            "Moderado" -> context.getString(R.string.moderado)
            "Alto" -> context.getString(R.string.alto)
            "Seroso" -> context.getString(R.string.exu_seroso)
            "Turbio" -> context.getString(R.string.exu_turbio)
            "Purulento" -> context.getString(R.string.exu_purulento)
            "Hemorrágico" -> context.getString(R.string.exu_hemorragico)
            "Serohemorrágico" -> context.getString(R.string.exu_serohemorragico)
            "Sana" -> context.getString(R.string.peri_sana)
            "Macerada" -> context.getString(R.string.peri_macerada)
            "Descamativa" -> context.getString(R.string.peri_descamativa)
            "Eccematosa" -> context.getString(R.string.peri_eccematosa)
            "Eritematosa" -> context.getString(R.string.peri_eritematosa)
            "Ninguno" -> context.getString(R.string.location_none)
            "Talón" -> context.getString(R.string.location_heel)
            "Sacro" -> context.getString(R.string.location_sacrum)
            "Desconocido" -> context.getString(R.string.germ_none)
            "Pseudomonas aeruginosa" -> context.getString(R.string.germ_pseudomonas)
            "MRSA" -> context.getString(R.string.germ_mrsa)
            "Candida albicans" -> context.getString(R.string.germ_candida)
            "Acinetobacter" -> context.getString(R.string.germ_acinetobacter)
            "Biofilm complejo" -> context.getString(R.string.germ_biofilm)
            else -> term
        }
    }

    fun mapToDbTerm(translatedTerm: String, context: Context): String {
        return when (translatedTerm.trim()) {
            context.getString(R.string.etiology_not_specified) -> "No especificada"
            context.getString(R.string.etiology_upp) -> "UPP"
            context.getString(R.string.etiology_venous) -> "Úlcera Venosa"
            context.getString(R.string.etiology_arterial) -> "Úlcera Arterial"
            context.getString(R.string.etiology_diabetic) -> "Pie Diabético"
            context.getString(R.string.etiology_surgical) -> "Quirúrgica"
            context.getString(R.string.etiology_burn) -> "Quemadura"
            context.getString(R.string.etiology_traumatic) -> "Traumática"
            context.getString(R.string.etiology_indeterminate) -> "Indeterminada"
            context.getString(R.string.necrosis) -> "Necrosis"
            context.getString(R.string.esfacelo) -> "Esfacelo"
            context.getString(R.string.granulacion) -> "Granulación"
            context.getString(R.string.epitelizacion) -> "Epitelización"
            context.getString(R.string.piel_intacta) -> "Piel Intacta (Prevención)"
            context.getString(R.string.nulo) -> "Nulo"
            context.getString(R.string.bajo) -> "Bajo"
            context.getString(R.string.moderado) -> "Moderado"
            context.getString(R.string.alto) -> "Alto"
            context.getString(R.string.exu_seroso) -> "Seroso"
            context.getString(R.string.exu_turbio) -> "Turbio"
            context.getString(R.string.exu_purulento) -> "Purulento"
            context.getString(R.string.exu_hemorragico) -> "Hemorrágico"
            context.getString(R.string.exu_serohemorragico) -> "Serohemorrágico"
            context.getString(R.string.peri_sana) -> "Sana"
            context.getString(R.string.peri_macerada) -> "Macerada"
            context.getString(R.string.peri_descamativa) -> "Descamativa"
            context.getString(R.string.peri_eccematosa) -> "Eccematosa"
            context.getString(R.string.peri_eritematosa) -> "Eritematosa"
            context.getString(R.string.location_none) -> "Ninguno"
            context.getString(R.string.location_heel) -> "Talón"
            context.getString(R.string.location_sacrum) -> "Sacro"
            context.getString(R.string.germ_none) -> "Desconocido"
            context.getString(R.string.germ_pseudomonas) -> "Pseudomonas aeruginosa"
            context.getString(R.string.germ_mrsa) -> "MRSA"
            context.getString(R.string.germ_candida) -> "Candida albicans"
            context.getString(R.string.germ_acinetobacter) -> "Acinetobacter"
            context.getString(R.string.germ_biofilm) -> "Biofilm complejo"
            else -> translatedTerm
        }
    }
}

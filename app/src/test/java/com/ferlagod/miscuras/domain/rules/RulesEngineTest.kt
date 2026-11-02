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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesEngineTest {

    private val engine = RulesEngine()

    private fun product(
        name: String,
        family: String,
        dims: String = "10x10",
        desc: String = "",
        uso: String = "Primario"
    ) = ApositoEntity(
        nombreComercial = name, fabricante = "X", familiaGenerica = family, dimensiones = dims,
        imagenUrl = "", codigoCn = "1", descripcion = desc, interacciones = "", usoPrimarioSecundario = uso
    )

    private fun families(s: String) = s.split("/").map { it.trim() }.toSet()

    @Test
    fun arterial_removesDebridementTpnCompressionAndHydrocolloid() {
        val state = WizardState(selectedEtiology = "Úlcera Arterial", selectedLecho = "Esfacelo", selectedExudado = "Moderado")
        val result = families(engine.applyEtiologyOverrides(
            "Espuma Poliuretano/Desbridante Enzimatico/Desbridamiento Mecanico/TPN/Hidrocoloide/Compresión Bicomponente",
            state.selectedEtiology, state
        ))
        assertEquals(setOf("Espuma Poliuretano"), result)
    }

    @Test
    fun venous_addsCompressionAndAlert() {
        val state = WizardState(selectedEtiology = "Úlcera Venosa", selectedLecho = "Granulación", selectedExudado = "Bajo")
        val fams = engine.applyEtiologyOverrides("Malla Silicona", state.selectedEtiology, state)
        assertTrue(families(fams).contains("Compresión Bicomponente"))
        assertTrue(engine.generatePostRecommendationAlerts(state, fams).any { it.contains("ITB") })
    }

    @Test
    fun diabeticFoot_removesHydrocolloid() {
        val state = WizardState(selectedEtiology = "Pie Diabético", selectedLecho = "Esfacelo", selectedExudado = "Bajo")
        assertFalse(families(engine.applyEtiologyOverrides("Hidrogel/Hidrocoloide", state.selectedEtiology, state)).contains("Hidrocoloide"))
    }

    @Test
    fun necrosis_neverIncludesTpn() {
        val state = WizardState(selectedEtiology = "UPP", selectedLecho = "Necrosis", selectedExudado = "Alto")
        assertFalse(families(engine.applyEtiologyOverrides("Alginato/TPN", "UPP", state)).contains("TPN"))
    }

    @Test
    fun depthZero_isNotCavitated() {
        assertFalse(RulesEngine.isCavitated(WizardState(woundDepth = "0")))
        assertFalse(RulesEngine.isCavitated(WizardState(woundDepth = "")))
        assertTrue(RulesEngine.isCavitated(WizardState(woundDepth = "0,5")))
    }

    @Test
    fun dryCavity_getsHydrogelNotHydrofiber() {
        val state = WizardState(selectedEtiology = "UPP", selectedLecho = "Granulación", selectedExudado = "Nulo", hasCavitation = true)
        val f = families(engine.applyEtiologyOverrides("Colágeno", "UPP", state))
        assertTrue(f.contains("Hidrogel"))
        assertFalse(f.contains("Hidrofibra"))
    }

    @Test
    fun wetCavity_getsHydrofiber() {
        val state = WizardState(selectedEtiology = "UPP", selectedLecho = "Granulación", selectedExudado = "Alto", hasCavitation = true)
        assertTrue(families(engine.applyEtiologyOverrides("Espuma Poliuretano", "UPP", state)).contains("Hidrofibra"))
    }

    @Test
    fun fillerRibbonsAndPrimaryDressings_notDiscardedBySize() {
        val ribbon = product("Aquacel (Cinta)", "Hidrofibra", dims = "2x45")
        val primary = product("Biatain Alginate", "Alginato", dims = "10x10")
        val bordered = product("Mepilex Border", "Espuma Poliuretano", dims = "10x10", uso = "Ambos")
        val result = engine.filterProductsByDimensions(listOf(ribbon, primary, bordered), 8f, 8f, "Ninguno")
        assertTrue(result.contains(ribbon))
        assertTrue(result.contains(primary))   // 10x10 cubre 8x8 (se recorta)
        assertFalse(result.contains(bordered)) // con borde necesita 12x12
    }

    @Test
    fun topicalAntibiotics_excludedWithoutInfection() {
        val fucidina = product("Fucidina", "Pomada", desc = "Antibiótico tópico con fusidina")
        val other = product("RYM Cicatrizante", "Pomada", desc = "Pomada cicatrizante")
        val noInf = engine.filterProductsByPatientSafety(listOf(fucidina, other), WizardState(selectedInfeccion = false))
        assertEquals(listOf(other), noInf.productos)
        val inf = engine.filterProductsByPatientSafety(listOf(fucidina, other), WizardState(selectedInfeccion = true))
        assertEquals(2, inf.productos.size)
    }

    @Test
    fun allergies_excludeMatchingProducts() {
        val iodosorb = product("Iodosorb Pomada", "Cadexómero Yodado")
        val silver = product("Aquacel Ag Foam", "Plata")
        val sulfa = product("Silvederma", "Plata", desc = "Crema de sulfadiazina argéntica al 1%")
        val sorbact = product("Cutimed Sorbact", "Malla DACC", desc = "sin liberar agentes químicos")
        val all = listOf(iodosorb, silver, sulfa, sorbact)

        val iodine = engine.filterProductsByPatientSafety(all, WizardState(selectedInfeccion = true, patientAllergies = "Povidona yodada"))
        assertFalse(iodine.productos.contains(iodosorb))
        assertTrue(iodine.alerts.any { it.contains("yodo") })

        val plata = engine.filterProductsByPatientSafety(all, WizardState(selectedInfeccion = true, patientAllergies = "plata"))
        assertEquals(listOf(iodosorb, sorbact), plata.productos)

        val sulfamidas = engine.filterProductsByPatientSafety(all, WizardState(selectedInfeccion = true, patientAllergies = "Sulfamidas"))
        assertFalse(sulfamidas.productos.contains(sulfa))
        assertTrue(sulfamidas.productos.contains(silver))
    }

    @Test
    fun thyroidHistory_excludesIodine() {
        val iodosorb = product("Iodosorb Pomada", "Cadexómero Yodado")
        val r = engine.filterProductsByPatientSafety(listOf(iodosorb), WizardState(selectedInfeccion = true, patientHistory = "Hipotiroidismo, Levotiroxina"))
        assertTrue(r.productos.isEmpty())
    }
}

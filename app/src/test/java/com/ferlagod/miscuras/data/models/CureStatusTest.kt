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
package com.ferlagod.miscuras.data.models

import com.ferlagod.miscuras.domain.rules.RulesEngine
import com.ferlagod.miscuras.ui.WizardState
import com.ferlagod.miscuras.ui.WizardStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CureStatusTest {

    private val hour = 3_600_000L

    @Test
    fun cureInterval_matchesFrequencyRules() {
        val e = RulesEngine()
        assertEquals(24, e.determineCureIntervalHours(WizardState(selectedInfeccion = true, selectedExudado = "Alto")))
        assertEquals(48, e.determineCureIntervalHours(WizardState(selectedInfeccion = true, selectedExudado = "Bajo")))
        assertEquals(168, e.determineCureIntervalHours(WizardState(selectedLecho = "Epitelización", selectedExudado = "Nulo")))
        assertEquals(72, e.determineCureIntervalHours(WizardState(selectedLecho = "Granulación", selectedExudado = "Moderado")))
    }

    @Test
    fun summary_countsOverdueAndNextDue() {
        val now = 100 * hour
        val list = listOf(
            WoundCureStatus(1, 9, lastEvaluationAt = 10 * hour, cureIntervalHours = 24), // vencida
            WoundCureStatus(2, 9, lastEvaluationAt = 90 * hour, cureIntervalHours = 48), // vence a las 138 h
            WoundCureStatus(3, 9, lastEvaluationAt = null, cureIntervalHours = null)     // sin evaluar
        )
        val s = list.summarize(now)
        assertEquals(3, s.activeWounds)
        assertEquals(1, s.overdueWounds)
        assertEquals(1, s.woundsWithoutEvaluation)
        assertEquals(34 * hour, s.nextCureDueAt)
        assertEquals(90 * hour, s.lastEvaluationAt)
    }

    @Test
    fun noInterval_isNeverOverdue() {
        val w = WoundCureStatus(1, 1, lastEvaluationAt = 0, cureIntervalHours = null)
        assertNull(w.nextCureDueAt)
        assertFalse(w.isOverdue(Long.MAX_VALUE))
    }

    @Test
    fun wizardValidation_requiresClinicalFields() {
        val base = WizardState(selectedEtiology = "UPP", selectedLecho = "Granulación")
        assertTrue(base.isStepComplete(WizardStep.ETIOLOGY))
        assertFalse(base.isStepComplete(WizardStep.EXUDATE))
        assertTrue(base.copy(selectedExudado = "Nulo").isStepComplete(WizardStep.EXUDATE))
        assertFalse(base.copy(selectedExudado = "Alto").isStepComplete(WizardStep.EXUDATE))
        assertTrue(base.copy(selectedExudado = "Alto", selectedExudateType = "Seroso").isStepComplete(WizardStep.EXUDATE))
        assertFalse(base.isComplete)
        assertTrue(WizardState(selectedEtiology = "UPP", selectedLecho = "Piel Intacta (Prevención)").isComplete)
    }
}

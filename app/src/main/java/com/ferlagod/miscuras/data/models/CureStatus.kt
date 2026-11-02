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

/**
 * Estado de cura de una herida activa: última evaluación y su intervalo recomendado.
 * Resultado de una consulta de Room (no es una tabla).
 */
data class WoundCureStatus(
    val woundId: Long,
    val patientId: Long,
    val lastEvaluationAt: Long?,
    val cureIntervalHours: Int?
) {
    /** Momento en que vence la siguiente cura, o null si no hay pauta conocida. */
    val nextCureDueAt: Long?
        get() = if (lastEvaluationAt != null && cureIntervalHours != null) lastEvaluationAt + cureIntervalHours * 3_600_000L else null

    fun isOverdue(now: Long): Boolean = nextCureDueAt?.let { now > it } ?: false
}

/**
 * Resumen por paciente para el Dashboard.
 */
data class PatientCureSummary(
    val activeWounds: Int,
    val lastEvaluationAt: Long?,
    val overdueWounds: Int,
    val nextCureDueAt: Long?,
    val woundsWithoutEvaluation: Int
)

fun List<WoundCureStatus>.summarize(now: Long): PatientCureSummary = PatientCureSummary(
    activeWounds = size,
    lastEvaluationAt = mapNotNull { it.lastEvaluationAt }.maxOrNull(),
    overdueWounds = count { it.isOverdue(now) },
    nextCureDueAt = mapNotNull { it.nextCureDueAt }.minOrNull(),
    woundsWithoutEvaluation = count { it.lastEvaluationAt == null }
)

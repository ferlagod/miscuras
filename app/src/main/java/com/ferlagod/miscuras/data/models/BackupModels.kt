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

import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.data.entities.EvaluationEntity
import com.ferlagod.miscuras.data.entities.PatientEntity
import com.ferlagod.miscuras.data.entities.ReglaEntity
import com.ferlagod.miscuras.data.entities.WoundEntity

/**
 * Data Transfer Object representing the entire database for backup purposes.
 */
data class MisCurasBackup(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val patients: List<PatientEntity> = emptyList(),
    val wounds: List<WoundEntity> = emptyList(),
    val evaluations: List<EvaluationEntity> = emptyList(),
    val apositos: List<ApositoEntity> = emptyList(),
    val reglas: List<ReglaEntity> = emptyList()
)

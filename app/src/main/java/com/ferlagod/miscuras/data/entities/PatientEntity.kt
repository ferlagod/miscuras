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
package com.ferlagod.miscuras.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de base de datos que representa a un paciente.
 * Almacenada en la tabla "pacientes".
 * 
 * @property id Identificador único autogenerado del paciente.
 * @property anonymizedName Nombre o iniciales anonimizadas del paciente para privacidad.
 * @property roomNumber Número de habitación o cama asignada.
 * @property createdAt Fecha de creación del registro en milisegundos.
 */
@Entity(tableName = "pacientes")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val anonymizedName: String,
    val roomNumber: String,
    val allergies: String? = null,
    val medication: String? = null,
    val medicalHistory: String? = null,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

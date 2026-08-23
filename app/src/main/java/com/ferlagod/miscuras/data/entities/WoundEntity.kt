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
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad de base de datos que representa una herida específica de un paciente.
 * Almacenada en la tabla "heridas".
 * Mantiene una relación de clave foránea (Foreign Key) con la tabla de pacientes,
 * por lo que si se elimina el paciente, se eliminan en cascada sus heridas.
 *
 * @property id Identificador único autogenerado de la herida.
 * @property patientId ID del paciente al que pertenece esta herida.
 * @property name Nombre descriptivo de la localización o herida (ej. "Talón Izquierdo").
 * @property createdAt Fecha y hora de registro de la herida en milisegundos.
 */
@Entity(
    tableName = "heridas",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId")]
)
/**
 * Entidad de base de datos que representa una herida.
 */
data class WoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val name: String, // ej. "Talón Izquierdo"
    val createdAt: Long = System.currentTimeMillis()
)

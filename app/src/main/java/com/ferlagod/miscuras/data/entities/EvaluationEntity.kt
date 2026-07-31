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
 * Entidad de base de datos que representa una evaluación clínica individual de una herida.
 * Almacenada en la tabla "evaluaciones".
 * Mantiene una relación de clave foránea (Foreign Key) con la tabla de heridas, 
 * por lo que si se elimina la herida, se eliminan en cascada sus evaluaciones.
 *
 * @property id Identificador único autogenerado de la evaluación.
 * @property woundId ID de la herida a la que pertenece esta evaluación.
 * @property timestamp Fecha y hora de la evaluación en milisegundos.
 * @property length Longitud de la herida en cm.
 * @property width Anchura de la herida en cm.
 * @property depth Profundidad de la herida en cm.
 * @property hasCavitation Indica si la herida presenta cavitación o tunelización.
 * @property cavitationDetails Detalles adicionales sobre la cavitación, si existe.
 * @property etiology Etiología (causa) subyacente de la herida.
 * @property bedState Estado principal del lecho de la herida (ej. Necrosis, Granulación).
 * @property exudateLevel Nivel de exudado (ej. Nulo, Bajo, Moderado, Alto).
 * @property exudateType Tipo de exudado (ej. Seroso, Purulento).
 * @property infection Verdadero si existen signos clínicos de infección.
 * @property infectionGerm Tipo de germen sospechoso o confirmado, si lo hay.
 * @property painLevel Nivel de dolor reportado por el paciente (escala visual analógica).
 * @property edges Estado de los bordes de la herida (ej. Macerados, Epitelizados).
 * @property perilesional Estado de la piel perilesional.
 * @property recommendedTreatment Tratamiento genérico recomendado por el sistema de reglas.
 * @property aiExplanation Explicación proporcionada por la Inteligencia Artificial.
 * @property photoPath Ruta local en el dispositivo hacia la fotografía clínica, si se ha tomado.
 * @property selectedProducts Nombres de los productos comerciales seleccionados finalmente por el profesional.
 */
@Entity(
    tableName = "evaluaciones",
    foreignKeys = [
        ForeignKey(
            entity = WoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["woundId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("woundId")]
)
data class EvaluationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val woundId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Medidas
    val length: String,
    val width: String,
    val depth: String,
    val hasCavitation: Boolean,
    val cavitationDetails: String,
    
    // Clínico
    val etiology: String,
    val bedState: String,
    val exudateLevel: String,
    val exudateType: String,
    val infection: Boolean,
    val infectionGerm: String,
    val painLevel: Float,
    val edges: String,
    val perilesional: String,
    
    // Tratamiento
    val recommendedTreatment: String,
    val aiExplanation: String,
    
    // Multimedia
    val photoPath: String? = null,
    
    // Tratamiento Seleccionado por el Profesional
    val selectedProducts: String? = null
)

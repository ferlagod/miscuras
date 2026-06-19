package com.ferlagod.miscuras.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

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

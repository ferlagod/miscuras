package com.ferlagod.miscuras.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pacientes")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val anonymizedName: String,
    val roomNumber: String,
    val createdAt: Long = System.currentTimeMillis()
)

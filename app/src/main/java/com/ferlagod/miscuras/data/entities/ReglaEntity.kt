package com.ferlagod.miscuras.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una regla clínica que asocia un conjunto de parámetros
 * (lecho, exudado, infección) con una familia genérica de apósitos.
 */
@Entity(tableName = "ReglasClinicas")
data class ReglaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "estado_lecho") val estadoLecho: String,
    @ColumnInfo(name = "nivel_exudado") val nivelExudado: String,
    @ColumnInfo(name = "infeccion") val infeccion: Boolean,
    @ColumnInfo(name = "familia_buscada") val familiaBuscada: String
)
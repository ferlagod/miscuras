/*
 * Mis Curas
 * Copyright (C) 2026
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
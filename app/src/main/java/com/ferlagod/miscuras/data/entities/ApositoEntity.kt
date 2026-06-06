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
 * Representa un producto (apósito) en la base de datos local.
 * Contiene información clínica y comercial sobre cada apósito disponible.
 */
@Entity(tableName = "ProductosApositos")
data class ApositoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nombre_comercial") val nombreComercial: String,
    @ColumnInfo(name = "fabricante") val fabricante: String,
    @ColumnInfo(name = "familia_generica") val familiaGenerica: String,
    @ColumnInfo(name = "dimensiones") val dimensiones: String,
    @ColumnInfo(name = "imagen_url") val imagenUrl: String,
    @ColumnInfo(name = "codigo_cn") val codigoCn: String,
    @ColumnInfo(name = "descripcion") val descripcion: String,
    @ColumnInfo(name = "interacciones") val interacciones: String,
    @ColumnInfo(name = "uso_primario_secundario") val usoPrimarioSecundario: String
)
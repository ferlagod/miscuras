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
 * Entidad que representa la caché de una respuesta del Asistente Educativo de IA.
 * Utiliza un hash de los parámetros de entrada como clave primaria para asegurar
 * que consultas idénticas devuelvan la misma respuesta sin consumir la API.
 *
 * @property promptHash Hash (SHA-256 u otro) generado a partir del prompt de entrada, usado como clave primaria.
 * @property response La respuesta generada por la IA almacenada en texto.
 * @property timestamp Marca de tiempo en milisegundos de cuándo se guardó esta respuesta para controlar su validez temporal (TTL).
 */
@Entity(tableName = "ai_cache")
data class AiCacheEntity(
    @PrimaryKey
    val promptHash: String,
    val response: String,
    val timestamp: Long
)

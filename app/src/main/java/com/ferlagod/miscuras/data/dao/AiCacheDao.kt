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
package com.ferlagod.miscuras.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ferlagod.miscuras.data.entities.AiCacheEntity

/**
 * Interfaz Data Access Object (DAO) para interactuar con la tabla de caché de IA.
 * Permite buscar respuestas previas, insertar nuevas y limpiar la caché antigua.
 */
@Dao
interface AiCacheDao {
    /**
     * Busca una respuesta en caché utilizando el hash generado a partir del prompt.
     * @param hash Clave primaria generada desde la consulta.
     * @return [AiCacheEntity] si se encuentra una coincidencia, null si no existe.
     */
    @Query("SELECT * FROM ai_cache WHERE promptHash = :hash LIMIT 1")
    fun getCachedResponse(hash: String): AiCacheEntity?

    /**
     * Inserta una nueva respuesta en la caché.
     * Reemplaza el registro existente en caso de colisión (mismo hash).
     * @param cache El objeto [AiCacheEntity] que contiene el hash, la respuesta y el timestamp.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCache(cache: AiCacheEntity)
    
    /**
     * Elimina todos los registros de la tabla de caché de la IA.
     */
    @Query("DELETE FROM ai_cache")
    fun clearCache()

    /**
     * Elimina las entradas de la caché de IA anteriores a una fecha límite.
     * Útil para implementar un TTL (Time To Live) y mantener la base de datos limpia.
     * @param threshold Límite de tiempo en milisegundos; todo registro anterior a este valor será eliminado.
     */
    @Query("DELETE FROM ai_cache WHERE timestamp < :threshold")
    fun deleteOldCacheEntries(threshold: Long)
}

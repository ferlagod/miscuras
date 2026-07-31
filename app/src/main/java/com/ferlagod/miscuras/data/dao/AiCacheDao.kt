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

@Dao
interface AiCacheDao {
    @Query("SELECT * FROM ai_cache WHERE promptHash = :hash LIMIT 1")
    fun getCachedResponse(hash: String): AiCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCache(cache: AiCacheEntity)
    
    @Query("DELETE FROM ai_cache")
    fun clearCache()

    @Query("DELETE FROM ai_cache WHERE timestamp < :threshold")
    fun deleteOldCacheEntries(threshold: Long)
}

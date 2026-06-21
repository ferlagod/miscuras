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

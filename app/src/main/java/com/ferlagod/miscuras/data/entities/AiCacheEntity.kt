package com.ferlagod.miscuras.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la caché de una respuesta del Asistente Educativo de IA.
 * Utiliza un hash de los parámetros de entrada como clave primaria para asegurar
 * que consultas idénticas devuelvan la misma respuesta sin consumir la API.
 */
@Entity(tableName = "ai_cache")
data class AiCacheEntity(
    @PrimaryKey
    val promptHash: String,
    val response: String,
    val timestamp: Long
)

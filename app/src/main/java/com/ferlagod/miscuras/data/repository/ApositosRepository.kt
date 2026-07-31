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
package com.ferlagod.miscuras.data.repository

import com.ferlagod.miscuras.data.dao.AiCacheDao
import com.ferlagod.miscuras.data.dao.ApositoDao
import com.ferlagod.miscuras.data.entities.AiCacheEntity
import com.ferlagod.miscuras.data.entities.ApositoEntity

/**
 * Repositorio que gestiona el acceso a los datos de apósitos y reglas clínicas.
 * Actúa como intermediario entre el ViewModel y el DAO ([ApositoDao]).
 *
 * @property apositoDao Objeto de acceso a datos de Room para apósitos y reglas.
 * @property aiCacheDao Objeto de acceso a datos para la caché de IA.
 */
class ApositosRepository(
    private val apositoDao: ApositoDao,
    private val aiCacheDao: AiCacheDao
) {

    /**
     * Fuerza la creación e inicialización de la base de datos de manera temprana.
     * Al ejecutar una consulta de lectura básica, Room abre la base de datos
     * y ejecuta el callback de `onCreate` si está vacía.
     */
    fun preCargarBaseDeDatos() {
        // Ejecuta una consulta ligera para forzar la creación de la BD y su prepoblación
        apositoDao.obtenerCantidadReglas()
    }

    /**
     * Consulta la regla clínica aplicable para las características dadas.
     * @param lecho Estado del lecho (ej. Necrosis).
     * @param exudado Nivel de exudado (ej. Moderado).
     * @param infeccion Indica si hay signos de infección.
     * @return El nombre de la familia recomendada, o null si no se encuentra.
     */
    fun obtenerRecomendacion(lecho: String, exudado: String, infeccion: Boolean): String? {
        return apositoDao.obtenerFamiliaRecomendada(lecho, exudado, infeccion)
    }

    /**
     * Obtiene una lista de productos concretos a partir de un string de familias.
     * Si hay múltiples familias separadas por barra (e.g. "Plata / Alginato"), 
     * se separan y se buscan todas las coincidencias.
     * @param familiasRecomendadas Cadena con los nombres de la familia.
     * @return Lista de [ApositoEntity] que pertenecen a esas familias.
     */
    fun obtenerProductosPorFamilias(familiasRecomendadas: String): List<ApositoEntity> {
        val listaFamilias = familiasRecomendadas.split("/").map { it.trim() }
        return apositoDao.obtenerProductosPorFamilias(listaFamilias)
    }

    /**
     * Recupera una respuesta de la caché de IA si existe.
     */
    fun getCachedAiResponse(hash: String): String? {
        return aiCacheDao.getCachedResponse(hash)?.response
    }

    /**
     * Guarda una nueva respuesta de la IA en la caché.
     */
    fun saveCachedAiResponse(hash: String, response: String) {
        val entity = AiCacheEntity(
            promptHash = hash,
            response = response,
            timestamp = System.currentTimeMillis()
        )
        aiCacheDao.insertCache(entity)
    }
}
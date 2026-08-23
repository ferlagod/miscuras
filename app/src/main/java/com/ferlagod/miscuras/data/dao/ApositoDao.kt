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
import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.data.entities.ReglaEntity

/**
 * Objeto de Acceso a Datos (DAO) para la base de datos de Mis Curas.
 * Contiene todas las consultas SQL y operaciones de inserción necesarias
 * para obtener reglas clínicas y productos.
 */
@Dao
interface ApositoDao {
    /**
     * Busca la familia de apósitos recomendada según las características clínicas.
     * @param lecho Estado del lecho de la herida.
     * @param exudado Nivel de exudado.
     * @param infeccion Si existe infección.
     * @return El nombre de la familia recomendada o null si no hay coincidencia.
     */
    @Query("""
        SELECT familia_buscada 
        FROM ReglasClinicas 
        WHERE estado_lecho = :lecho 
          AND nivel_exudado = :exudado 
          AND infeccion = :infeccion
        LIMIT 1
    """)
    fun obtenerFamiliaRecomendada(lecho: String, exudado: String, infeccion: Boolean): String?

    /**
     * Obtiene todos los productos que pertenecen a una lista de familias.
     * @param familias Lista de nombres de familias genéricas.
     * @return Lista de entidades de apósitos.
     */
    @Query("SELECT * FROM ProductosApositos WHERE familia_generica IN (:familias)")
    fun obtenerProductosPorFamilias(familias: List<String>): List<ApositoEntity>

    /** Inserta una lista de reglas clínicas (reemplaza si hay conflicto). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarReglas(reglas: List<ReglaEntity>)

    /** Inserta una lista de productos/apósitos (reemplaza si hay conflicto). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarProductos(productos: List<ApositoEntity>)

    /** Devuelve la cantidad total de productos en la tabla. */
    @Query("SELECT COUNT(*) FROM ProductosApositos")
    fun obtenerCantidadProductos(): Int

    /** Devuelve la cantidad total de reglas clínicas en la tabla. */
    @Query("SELECT COUNT(*) FROM ReglasClinicas")
    fun obtenerCantidadReglas(): Int

    // --- Backup/Restore Methods ---

    @Query("SELECT * FROM ProductosApositos")
    suspend fun getAllApositosSync(): @JvmSuppressWildcards List<ApositoEntity>

    @Query("SELECT * FROM ReglasClinicas")
    suspend fun getAllReglasSync(): @JvmSuppressWildcards List<ReglaEntity>

    @Query("DELETE FROM ProductosApositos")
    suspend fun deleteAllApositos(): @JvmSuppressWildcards Int

    @Query("DELETE FROM ReglasClinicas")
    suspend fun deleteAllReglas(): @JvmSuppressWildcards Int
}
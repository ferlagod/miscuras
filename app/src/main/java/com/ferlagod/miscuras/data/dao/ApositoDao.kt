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
     * @param desbridamiento Si se requiere desbridamiento enzimático.
     * @return El nombre de la familia recomendada o null si no hay coincidencia.
     */
    @Query("""
        SELECT familia_buscada 
        FROM ReglasClinicas 
        WHERE estado_lecho = :lecho 
          AND nivel_exudado = :exudado 
          AND infeccion = :infeccion 
          AND desbridamiento = :desbridamiento
        LIMIT 1
    """)
    fun obtenerFamiliaRecomendada(lecho: String, exudado: String, infeccion: Boolean, desbridamiento: Boolean): String?

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
}
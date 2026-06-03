package com.ferlagod.miscuras.data.repository

import com.ferlagod.miscuras.data.dao.ApositoDao
import com.ferlagod.miscuras.data.entities.ApositoEntity

/**
 * Repositorio que gestiona el acceso a los datos de apósitos y reglas clínicas.
 * Actúa como intermediario entre el ViewModel y el DAO ([ApositoDao]).
 *
 * @property apositoDao Objeto de acceso a datos de Room.
 */
class ApositosRepository(private val apositoDao: ApositoDao) {

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
}
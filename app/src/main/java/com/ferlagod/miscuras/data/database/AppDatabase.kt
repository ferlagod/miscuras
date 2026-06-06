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
package com.ferlagod.miscuras.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ferlagod.miscuras.R
import com.ferlagod.miscuras.data.dao.ApositoDao
import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.data.entities.ReglaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Base de datos principal de la aplicación utilizando la librería Room.
 * Gestiona las entidades [ReglaEntity] y [ApositoEntity].
 * Contiene la lógica para poblar la base de datos inicialmente
 * a partir de archivos CSV ubicados en `res/raw`.
 */
@Database(
    entities = [ReglaEntity::class, ApositoEntity::class],
    version = 21,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Provee acceso al DAO de apósitos y reglas clínicas. */
    abstract fun apositoDao(): ApositoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna la instancia singleton de la base de datos.
         * Si no existe, la construye, aplica migraciones destructivas (si cambia la versión)
         * y le adjunta el callback para la población inicial.
         *
         * @param context Contexto de la aplicación.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mis_curas_database"
                )
                    .fallbackToDestructiveMigration() // Recrear la base de datos si la versión cambia
                    .addCallback(DatabaseCallback(context)) // Disparador para la primera ejecución
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback que se dispara durante el ciclo de vida de creación de la base de datos.
     * Se usa para leer los archivos CSV la primera vez que se crea el archivo físico.
     */
    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            // Ejecutar la lectura de los CSV en un hilo secundario si las tablas están vacías
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                val dao = database.apositoDao()

                if (dao.obtenerCantidadReglas() == 0) {
                    cargarReglasClinicas(context, dao)
                }
                if (dao.obtenerCantidadProductos() == 0) {
                    cargarProductosApositos(context, dao)
                }
            }
        }

        /**
         * Lee el archivo `reglas_clinicas.csv` y guarda sus registros en la tabla.
         */
        private suspend fun cargarReglasClinicas(context: Context, dao: ApositoDao) {
            // Abrir el archivo CSV
            val inputStream = context.resources.openRawResource(R.raw.reglas_clinicas)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val reglas = mutableListOf<ReglaEntity>()

            reader.useLines { lines ->
                // drop(1) ignora la primera línea porque son las cabeceras
                lines.drop(1).forEach { line ->
                    val columnas = line.split(",")
                    if (columnas.size == 4) {
                        reglas.add(
                            ReglaEntity(
                                estadoLecho = columnas[0].trim(),
                                nivelExudado = columnas[1].trim(),
                                infeccion = columnas[2].trim().toBoolean(),
                                familiaBuscada = columnas[3].trim()
                            )
                        )
                    }
                }
            }
            dao.insertarReglas(reglas)
        }

        /**
         * Lee el archivo `productos_apositos.csv` y guarda los productos disponibles.
         */
        private suspend fun cargarProductosApositos(context: Context, dao: ApositoDao) {
            val inputStream = context.resources.openRawResource(R.raw.productos_apositos)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val productos = mutableListOf<ApositoEntity>()

            reader.useLines { lines ->
                lines.drop(1).forEach { line ->
                    val columnas = line.split("|")
                    if (columnas.size == 9) {
                        productos.add(
                            ApositoEntity(
                                nombreComercial = columnas[0].trim(),
                                fabricante = columnas[1].trim(),
                                familiaGenerica = columnas[2].trim(),
                                dimensiones = columnas[3].trim(),
                                imagenUrl = columnas[4].trim(),
                                codigoCn = columnas[5].trim(),
                                descripcion = columnas[6].trim(),
                                interacciones = columnas[7].trim(),
                                usoPrimarioSecundario = columnas[8].trim()
                            )
                        )
                    }
                }
            }
            dao.insertarProductos(productos)
        }
    }
}
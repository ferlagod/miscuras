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
package com.ferlagod.miscuras.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ferlagod.miscuras.R
import com.ferlagod.miscuras.data.dao.AiCacheDao
import com.ferlagod.miscuras.data.dao.ApositoDao
import com.ferlagod.miscuras.data.entities.AiCacheEntity
import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.data.entities.ReglaEntity
import com.ferlagod.miscuras.data.entities.PatientEntity
import com.ferlagod.miscuras.data.entities.WoundEntity
import com.ferlagod.miscuras.data.entities.EvaluationEntity
import com.ferlagod.miscuras.data.dao.PatientDao
import com.ferlagod.miscuras.data.security.CryptoManager
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Base de datos principal de la aplicación utilizando la librería Room.
 * Gestiona las entidades [ReglaEntity] y [ApositoEntity].
 * Contiene la lógica para poblar la base de datos inicialmente
 * a partir de archivos CSV ubicados en `res/raw`.
 */
@Database(
    entities = [
        ReglaEntity::class, 
        ApositoEntity::class, 
        AiCacheEntity::class,
        PatientEntity::class,
        WoundEntity::class,
        EvaluationEntity::class
    ],
    version = 33,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Provee acceso al DAO de apósitos y reglas clínicas. */
    abstract fun apositoDao(): ApositoDao

    /** Provee acceso al DAO de la caché de respuestas de la IA. */
    abstract fun aiCacheDao(): AiCacheDao

    /** Provee acceso al DAO de gestión de pacientes. */
    abstract fun patientDao(): PatientDao

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
                val dbName = "mis_curas_database"
                val dbFile = context.getDatabasePath(dbName)
                
                var useEncryption = true
                
                // Si la base de datos existe, comprobamos si es texto plano (versiones anteriores)
                if (dbFile.exists()) {
                    try {
                        val header = ByteArray(16)
                        java.io.FileInputStream(dbFile).use { it.read(header) }
                        val headerString = String(header)
                        // Las bases de datos SQLite no encriptadas empiezan siempre por "SQLite format 3"
                        if (headerString.startsWith("SQLite format 3")) {
                            // Comprobar si la BD contiene datos de usuario antes de borrarla
                            val hasUserData = try {
                                val plainDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                                    dbFile.absolutePath, null,
                                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                                )
                                val hasTables = plainDb.rawQuery(
                                    "SELECT name FROM sqlite_master WHERE type='table' AND name IN ('pacientes', 'heridas', 'evaluaciones')",
                                    null
                                ).use { cursor -> cursor.count > 0 }
                                
                                val hasRecords = if (hasTables) {
                                    var count = 0
                                    try { plainDb.rawQuery("SELECT COUNT(*) FROM pacientes", null).use { c -> if (c.moveToFirst()) count += c.getInt(0) } } catch(e:Exception){}
                                    try { plainDb.rawQuery("SELECT COUNT(*) FROM heridas", null).use { c -> if (c.moveToFirst()) count += c.getInt(0) } } catch(e:Exception){}
                                    try { plainDb.rawQuery("SELECT COUNT(*) FROM evaluaciones", null).use { c -> if (c.moveToFirst()) count += c.getInt(0) } } catch(e:Exception){}
                                    count > 0
                                } else false
                                plainDb.close()
                                hasRecords
                            } catch (e: Exception) {
                                // Ante cualquier error al leer, asumimos que tiene datos para evitar borrado accidental
                                true
                            }

                            if (hasUserData) {
                                // La BD tiene datos de usuario: NO borrar. Desactivar cifrado para no perder datos.
                                Log.w("AppDatabase", "Base de datos sin cifrar con datos de usuario. Desactivando cifrado para conservar los datos.")
                                useEncryption = false
                            } else {
                                // Solo reglas CSV y caché: seguro borrar y recrear cifrada.
                                Log.i("AppDatabase", "Migrando BD sin cifrar (sin datos de usuario) a cifrada.")
                                dbFile.delete()
                                context.getDatabasePath("$dbName-journal").delete()
                                context.getDatabasePath("$dbName-shm").delete()
                                context.getDatabasePath("$dbName-wal").delete()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val MIGRATION_25_26 = object : androidx.room.migration.Migration(25, 26) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("ALTER TABLE evaluaciones ADD COLUMN selectedProducts TEXT")
                    }
                }

                // Versión 27: solo cambió la librería SQLCipher (net.sqlcipher → net.zetetic),
                // sin cambios de esquema SQL.
                val MIGRATION_26_27 = object : androidx.room.migration.Migration(26, 27) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Sin cambios de esquema
                    }
                }

                val MIGRATION_27_28 = object : androidx.room.migration.Migration(27, 28) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Limpiamos la tabla de reglas para que onOpen vuelva a cargar el CSV actualizado
                        db.execSQL("DELETE FROM ReglasClinicas")
                    }
                }

                val MIGRATION_28_29 = object : androidx.room.migration.Migration(28, 29) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Limpiamos la tabla de reglas para que onOpen vuelva a cargar el CSV actualizado
                        db.execSQL("DELETE FROM ReglasClinicas")
                    }
                }

                val MIGRATION_29_30 = object : androidx.room.migration.Migration(29, 30) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Limpiamos reglas y productos para que onOpen recargue los CSV actualizados
                        // (nuevos productos, códigos CN corregidos, advertencias clínicas, nuevas familias)
                        db.execSQL("DELETE FROM ReglasClinicas")
                        db.execSQL("DELETE FROM Productos")
                    }
                }

                val MIGRATION_30_31 = object : androidx.room.migration.Migration(30, 31) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("DELETE FROM ReglasClinicas")
                        db.execSQL("DELETE FROM ProductosApositos")
                    }
                }

                val MIGRATION_31_32 = object : androidx.room.migration.Migration(31, 32) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("ALTER TABLE pacientes ADD COLUMN allergies TEXT")
                        db.execSQL("ALTER TABLE pacientes ADD COLUMN medication TEXT")
                        db.execSQL("ALTER TABLE pacientes ADD COLUMN medicalHistory TEXT")
                        db.execSQL("ALTER TABLE pacientes ADD COLUMN photoUri TEXT")
                        db.execSQL("ALTER TABLE heridas ADD COLUMN isDischarged INTEGER NOT NULL DEFAULT 0")
                    }
                }

                val MIGRATION_32_33 = object : androidx.room.migration.Migration(32, 33) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("ALTER TABLE heridas ADD COLUMN dischargedAt INTEGER")
                    }
                }

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                    .addMigrations(MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33)
                    .addCallback(DatabaseCallback(context)) // Disparador para la primera ejecución
                
                if (useEncryption) {
                    System.loadLibrary("sqlcipher")
                    val factory = SupportOpenHelperFactory(CryptoManager.getDatabasePassphrase(context))
                    builder.openHelperFactory(factory)
                }

                val instance = builder.build()
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
            GlobalScope.launch(Dispatchers.IO) {
                val database = getDatabase(context)
                
                // B2: Limpiar la caché de IA con más de 7 días (TTL)
                val sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000
                val threshold = System.currentTimeMillis() - sevenDaysInMillis
                database.aiCacheDao().deleteOldCacheEntries(threshold)

                val dao = database.apositoDao()

                if (dao.obtenerCantidadReglas() == 0 || dao.obtenerCantidadProductos() == 0) {
                    database.runInTransaction {
                        kotlinx.coroutines.runBlocking {
                            if (dao.obtenerCantidadReglas() == 0) {
                                cargarReglasClinicas(context, dao)
                            }
                            if (dao.obtenerCantidadProductos() == 0) {
                                cargarProductosApositos(context, dao)
                            }
                        }
                    }
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
                    } else {
                        Log.w("AppDatabase", "Línea descartada en reglas_clinicas.csv: $line")
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
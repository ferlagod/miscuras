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
package com.ferlagod.miscuras.domain

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ferlagod.miscuras.data.database.AppDatabase
import com.ferlagod.miscuras.data.models.MisCurasBackup
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Estado que representa el progreso de una operación de copia de seguridad.
 */
sealed class BackupState {
    /**
     * Estado inactivo de la copia de seguridad.
     */
    object Idle : BackupState()
    /**
     * Estado de carga o procesamiento de la copia de seguridad.
     */
    object Loading : BackupState()
    /**
     * Estado de finalización exitosa de la copia de seguridad.
     */
    object Success : BackupState()
    /**
     * Estado de error durante la copia de seguridad.
     */
    data class Error(val message: String) : BackupState()
}

/**
 * Servicio encargado de exportar e importar la base de datos y las imágenes.
 */
class BackupService(private val context: Context, private val database: AppDatabase) {

    private val gson = Gson()

    fun createBackup(outputUri: Uri): Flow<BackupState> = flow {
        emit(BackupState.Loading)
        try {
            val patients = database.patientDao().getAllPatientsSync()
            val wounds = database.patientDao().getAllWoundsSync()
            val evaluations = database.patientDao().getAllEvaluationsSync()
            val apositos = database.apositoDao().getAllApositosSync()
            val reglas = database.apositoDao().getAllReglasSync()

            val backup = MisCurasBackup(
                patients = patients,
                wounds = wounds,
                evaluations = evaluations,
                apositos = apositos,
                reglas = reglas
            )
            val jsonStr = gson.toJson(backup)

            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->
                    // 1. Save data.json
                    val jsonEntry = ZipEntry("data.json")
                    zipOut.putNextEntry(jsonEntry)
                    zipOut.write(jsonStr.toByteArray(Charsets.UTF_8))
                    zipOut.closeEntry()

                    // 2. Save images
                    val photoPaths = evaluations.mapNotNull { it.photoPath }.distinct()
                    for (path in photoPaths) {
                        val file = File(path)
                        if (file.exists()) {
                            val imgEntry = ZipEntry("images/${file.name}")
                            zipOut.putNextEntry(imgEntry)
                            file.inputStream().use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                    }
                }
            } ?: throw Exception("Cannot open output stream")

            emit(BackupState.Success)
        } catch (e: Exception) {
            Log.e("BackupService", "Error creating backup", e)
            emit(BackupState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    fun restoreBackup(inputUri: Uri): Flow<BackupState> = flow {
        emit(BackupState.Loading)
        try {
            var backup: MisCurasBackup? = null
            val newImagesDir = context.filesDir

            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        if (entry.name == "data.json") {
                            val jsonStr = zipIn.bufferedReader(Charsets.UTF_8).readText()
                            backup = gson.fromJson(jsonStr, MisCurasBackup::class.java)
                        } else if (entry.name.startsWith("images/")) {
                            val fileName = File(entry.name).name
                            val destFile = File(newImagesDir, fileName)
                            destFile.outputStream().use { zipIn.copyTo(it) }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            } ?: throw Exception("Cannot open input stream")

            if (backup == null) {
                throw Exception("Invalid backup file: data.json not found")
            }

            // Update photoPaths to new absolute paths
            val updatedEvaluations = backup!!.evaluations.map { eval ->
                if (eval.photoPath != null) {
                    val fileName = File(eval.photoPath).name
                    eval.copy(photoPath = File(newImagesDir, fileName).absolutePath)
                } else {
                    eval
                }
            }

            // Restore DB inside a transaction to ensure all or nothing
            database.runInTransaction {
                kotlinx.coroutines.runBlocking {
                    database.patientDao().deleteAllPatients() // Cascade deletes wounds & evals usually, but let's be explicit if not handled properly in some DBs
                    // Re-insert. ID conflicts won't happen because we replace and autoGenerate might just reuse or create new if we insert with ID 0.
                    // Wait, if we replace, Room inserts them with their original IDs (since they have a >0 id), which preserves relations!
                    database.patientDao().insertPatients(backup!!.patients)
                    database.patientDao().insertWounds(backup!!.wounds)
                    database.patientDao().insertEvaluations(updatedEvaluations)
                    
                    // Apositos & Reglas might be fine to overwrite
                    database.apositoDao().deleteAllApositos()
                    database.apositoDao().deleteAllReglas()
                    database.apositoDao().insertarProductos(backup!!.apositos)
                    database.apositoDao().insertarReglas(backup!!.reglas)
                }
            }

            emit(BackupState.Success)
        } catch (e: Exception) {
            Log.e("BackupService", "Error restoring backup", e)
            emit(BackupState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)
}

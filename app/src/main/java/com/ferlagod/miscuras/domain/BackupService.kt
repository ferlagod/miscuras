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
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
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
 * Contraseña incorrecta o archivo de copia manipulado/dañado.
 */
class BackupAuthenticationException : Exception("Contraseña incorrecta o copia de seguridad dañada")

/**
 * Servicio encargado de exportar e importar la base de datos y las imágenes.
 *
 * Formato cifrado (v2): "MCENC1" | salt(16) | iv(16) | AES-256-CTR(zip) | HMAC-SHA256(32)
 * Claves derivadas de la contraseña con PBKDF2-HMAC-SHA256. El HMAC (encrypt-then-MAC) cubre
 * cabecera, salt, iv y texto cifrado y se verifica antes de descifrar nada.
 * Las copias antiguas (ZIP sin cifrar) se siguen pudiendo restaurar.
 */
class BackupService(private val context: Context, private val database: AppDatabase) {

    private val gson = Gson()

    companion object {
        private val MAGIC = "MCENC1".toByteArray(Charsets.US_ASCII)
        private const val SALT_LEN = 16
        private const val IV_LEN = 16
        private const val MAC_LEN = 32
        private const val PBKDF2_ITERATIONS = 150_000
        const val MIN_PASSWORD_LENGTH = 8

        private val PATIENT_STRING_FIELDS = listOf("anonymizedName", "roomNumber")
        private val WOUND_STRING_FIELDS = listOf("name")
        private val EVALUATION_STRING_FIELDS = listOf(
            "length", "width", "depth", "cavitationDetails", "etiology", "bedState",
            "exudateLevel", "exudateType", "infectionGerm", "edges", "perilesional",
            "recommendedTreatment", "aiExplanation"
        )
    }

    private data class Keys(val enc: SecretKeySpec, val mac: SecretKeySpec)

    private fun deriveKeys(password: String, salt: ByteArray): Keys {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 512)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return Keys(SecretKeySpec(bytes.copyOfRange(0, 32), "AES"), SecretKeySpec(bytes.copyOfRange(32, 64), "HmacSHA256"))
    }

    private fun encryptTo(plainFile: File, out: OutputStream, password: String) {
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { SecureRandom().nextBytes(it) }
        val keys = deriveKeys(password, salt)
        val cipher = Cipher.getInstance("AES/CTR/NoPadding").apply { init(Cipher.ENCRYPT_MODE, keys.enc, IvParameterSpec(iv)) }
        val mac = Mac.getInstance("HmacSHA256").apply { init(keys.mac) }

        fun emit(bytes: ByteArray) { if (bytes.isNotEmpty()) { mac.update(bytes); out.write(bytes) } }
        emit(MAGIC); emit(salt); emit(iv)
        plainFile.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                cipher.update(buffer, 0, n)?.let { emit(it) }
            }
        }
        cipher.doFinal()?.let { emit(it) }
        out.write(mac.doFinal())
        out.flush()
    }

    private fun isEncrypted(file: File): Boolean {
        if (file.length() < MAGIC.size) return false
        val head = ByteArray(MAGIC.size)
        file.inputStream().use { DataInputStream(it).readFully(head) }
        return head.contentEquals(MAGIC)
    }

    /** Verifica el HMAC y descifra [encFile] en [outFile]. */
    private fun decryptTo(encFile: File, outFile: File, password: String) {
        val total = encFile.length()
        if (total < MAGIC.size + SALT_LEN + IV_LEN + MAC_LEN) throw BackupAuthenticationException()
        val salt = ByteArray(SALT_LEN)
        val iv = ByteArray(IV_LEN)
        encFile.inputStream().use { DataInputStream(it).apply { skipBytes(MAGIC.size); readFully(salt); readFully(iv) } }
        val keys = deriveKeys(password, salt)
        val dataLen = total - MAC_LEN

        // 1) Verificar integridad/contraseña antes de descifrar
        val mac = Mac.getInstance("HmacSHA256").apply { init(keys.mac) }
        val expectedTag = ByteArray(MAC_LEN)
        encFile.inputStream().use { input ->
            copyRange(input, dataLen) { buf, n -> mac.update(buf, 0, n) }
            DataInputStream(input).readFully(expectedTag)
        }
        if (!MessageDigest.isEqual(mac.doFinal(), expectedTag)) throw BackupAuthenticationException()

        // 2) Descifrar
        val cipher = Cipher.getInstance("AES/CTR/NoPadding").apply { init(Cipher.DECRYPT_MODE, keys.enc, IvParameterSpec(iv)) }
        encFile.inputStream().use { input ->
            DataInputStream(input).skipBytes(MAGIC.size + SALT_LEN + IV_LEN)
            outFile.outputStream().use { out ->
                copyRange(input, dataLen - MAGIC.size - SALT_LEN - IV_LEN) { buf, n ->
                    cipher.update(buf, 0, n)?.let { out.write(it) }
                }
                cipher.doFinal()?.let { out.write(it) }
            }
        }
    }

    private inline fun copyRange(input: InputStream, length: Long, onChunk: (ByteArray, Int) -> Unit) {
        val buffer = ByteArray(64 * 1024)
        var remaining = length
        while (remaining > 0) {
            val n = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
            if (n < 0) throw BackupAuthenticationException()
            onChunk(buffer, n)
            remaining -= n
        }
    }

    /**
     * Rellena con "" los campos de texto obligatorios ausentes en copias de versiones antiguas.
     * Gson no aplica los valores por defecto de Kotlin y dejaría nulls en campos no nulos,
     * lo que haría fallar la inserción (NOT NULL) y abortaría toda la restauración.
     */
    private fun sanitizeBackupJson(root: JsonObject) {
        fun fill(listKey: String, fields: List<String>) {
            val arr = root.get(listKey)
            if (arr == null || !arr.isJsonArray) {
                root.add(listKey, com.google.gson.JsonArray())
                return
            }
            arr.asJsonArray.forEach { el: JsonElement ->
                if (el.isJsonObject) {
                    val obj = el.asJsonObject
                    fields.forEach { f ->
                        val v = obj.get(f)
                        if (v == null || v is JsonNull) obj.add(f, JsonPrimitive(""))
                    }
                }
            }
        }
        fill("patients", PATIENT_STRING_FIELDS)
        fill("wounds", WOUND_STRING_FIELDS)
        fill("evaluations", EVALUATION_STRING_FIELDS)
        fill("apositos", emptyList())
        fill("reglas", emptyList())
    }

    fun createBackup(outputUri: Uri, password: String): Flow<BackupState> = flow {
        emit(BackupState.Loading)
        val tempZip = File(context.cacheDir, "backup_${System.currentTimeMillis()}.zip")
        try {
            require(password.length >= MIN_PASSWORD_LENGTH) { "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres" }

            val patients = database.patientDao().getAllPatientsSync()
            val wounds = database.patientDao().getAllWoundsSync()
            val evaluations = database.patientDao().getAllEvaluationsSync()
            val apositos = database.apositoDao().getAllApositosSync()
            val reglas = database.apositoDao().getAllReglasSync()

            val backup = MisCurasBackup(
                version = 2,
                patients = patients,
                wounds = wounds,
                evaluations = evaluations,
                apositos = apositos,
                reglas = reglas
            )
            val jsonStr = gson.toJson(backup)

            tempZip.outputStream().use { fileOut ->
                ZipOutputStream(fileOut).use { zipOut ->
                    // 1. Save data.json
                    zipOut.putNextEntry(ZipEntry("data.json"))
                    zipOut.write(jsonStr.toByteArray(Charsets.UTF_8))
                    zipOut.closeEntry()

                    // 2. Save images (evaluations + patient photos)
                    val evalPhotoPaths = evaluations.mapNotNull { it.photoPath }
                    val patientPhotoPaths = patients.mapNotNull { it.photoUri }
                    val allPhotoPaths = (evalPhotoPaths + patientPhotoPaths).distinct()
                    val addedNames = mutableSetOf<String>()
                    for (path in allPhotoPaths) {
                        val cleanPath = when {
                            path.startsWith("file://") -> path.removePrefix("file://")
                            path.startsWith("file:") -> path.removePrefix("file:")
                            else -> path
                        }
                        val file = File(cleanPath)
                        if (file.exists() && addedNames.add(file.name)) {
                            zipOut.putNextEntry(ZipEntry("images/${file.name}"))
                            file.inputStream().use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                    }
                }
            }

            context.contentResolver.openOutputStream(outputUri)?.use { out ->
                encryptTo(tempZip, out.buffered(), password)
            } ?: throw Exception("Cannot open output stream")

            emit(BackupState.Success)
        } catch (e: Exception) {
            Log.e("BackupService", "Error creating backup", e)
            emit(BackupState.Error(e.message ?: "Unknown error"))
        } finally {
            tempZip.delete()
        }
    }.flowOn(Dispatchers.IO)

    fun restoreBackup(inputUri: Uri, password: String): Flow<BackupState> = flow {
        emit(BackupState.Loading)
        val tempIn = File(context.cacheDir, "restore_in_${System.currentTimeMillis()}")
        val tempZip = File(context.cacheDir, "restore_zip_${System.currentTimeMillis()}.zip")
        try {
            var backup: MisCurasBackup? = null
            val newImagesDir = context.filesDir

            context.contentResolver.openInputStream(inputUri)?.use { input ->
                tempIn.outputStream().use { input.copyTo(it) }
            } ?: throw Exception("Cannot open input stream")

            val zipFile = if (isEncrypted(tempIn)) {
                if (password.isEmpty()) throw BackupAuthenticationException()
                decryptTo(tempIn, tempZip, password)
                tempZip
            } else {
                tempIn // Copia antigua sin cifrar
            }

            zipFile.inputStream().use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        if (entry.name == "data.json") {
                            val jsonStr = zipIn.bufferedReader(Charsets.UTF_8).readText()
                            val root = JsonParser.parseString(jsonStr).asJsonObject
                            sanitizeBackupJson(root)
                            backup = gson.fromJson(root, MisCurasBackup::class.java)
                        } else if (entry.name.startsWith("images/")) {
                            val fileName = File(entry.name).name
                            if (fileName.isNotBlank()) {
                                val destFile = File(newImagesDir, fileName)
                                destFile.outputStream().use { zipIn.copyTo(it) }
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
            if (backup == null) {
                throw Exception("Invalid backup file: data.json not found")
            }

            // Update photoPaths and photoUris to new absolute paths
            val updatedPatients = backup!!.patients.map { patient ->
                if (patient.photoUri != null) {
                    val clean = when {
                        patient.photoUri.startsWith("file://") -> patient.photoUri.removePrefix("file://")
                        patient.photoUri.startsWith("file:") -> patient.photoUri.removePrefix("file:")
                        else -> patient.photoUri
                    }
                    val fileName = File(clean).name
                    val localFile = File(newImagesDir, fileName)
                    patient.copy(photoUri = if (localFile.exists()) localFile.absolutePath else patient.photoUri)
                } else {
                    patient
                }
            }

            val updatedEvaluations = backup!!.evaluations.map { eval ->
                if (eval.photoPath != null) {
                    val clean = when {
                        eval.photoPath.startsWith("file://") -> eval.photoPath.removePrefix("file://")
                        eval.photoPath.startsWith("file:") -> eval.photoPath.removePrefix("file:")
                        else -> eval.photoPath
                    }
                    val fileName = File(clean).name
                    val localFile = File(newImagesDir, fileName)
                    eval.copy(photoPath = if (localFile.exists()) localFile.absolutePath else eval.photoPath)
                } else {
                    eval
                }
            }

            // Fotos de los datos actuales (se sustituyen): borrar después las que ya no se usen
            val oldPhotoPaths = (database.patientDao().getAllEvaluationsSync().mapNotNull { it.photoPath } +
                database.patientDao().getAllPatientsSync().mapNotNull { it.photoUri })
                .map { it.removePrefix("file://").removePrefix("file:") }
                .toSet()

            // Restore DB inside a transaction to ensure all or nothing
            database.runInTransaction {
                kotlinx.coroutines.runBlocking {
                    database.patientDao().deleteAllEvaluations()
                    database.patientDao().deleteAllWounds()
                    database.patientDao().deleteAllPatients()
                    database.patientDao().insertPatients(updatedPatients)
                    database.patientDao().insertWounds(backup!!.wounds)
                    database.patientDao().insertEvaluations(updatedEvaluations)
                    
                    // Proteger el catálogo oficial: no borrar ni degradar productos/reglas al restaurar datos de pacientes.
                    // Solo poblar desde el backup si la base de datos local estuviera vacía.
                    if (database.apositoDao().obtenerCantidadProductos() == 0 && backup!!.apositos.isNotEmpty()) {
                        database.apositoDao().insertarProductos(backup!!.apositos)
                    }
                    if (database.apositoDao().obtenerCantidadReglas() == 0 && backup!!.reglas.isNotEmpty()) {
                        database.apositoDao().insertarReglas(backup!!.reglas)
                    }
                }
            }

            val newPhotoPaths = (updatedEvaluations.mapNotNull { it.photoPath } + updatedPatients.mapNotNull { it.photoUri })
                .map { it.removePrefix("file://").removePrefix("file:") }
                .toSet()
            (oldPhotoPaths - newPhotoPaths).forEach { path ->
                try { File(path).delete() } catch (e: Exception) { Log.w("BackupService", "No se pudo borrar $path", e) }
            }

            emit(BackupState.Success)
        } catch (e: Exception) {
            Log.e("BackupService", "Error restoring backup", e)
            emit(BackupState.Error(e.message ?: "Unknown error"))
        } finally {
            tempIn.delete()
            tempZip.delete()
        }
    }.flowOn(Dispatchers.IO)
}

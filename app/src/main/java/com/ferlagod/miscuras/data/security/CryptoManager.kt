package com.ferlagod.miscuras.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gestor de cifrado para la passphrase de la base de datos SQLCipher.
 *
 * Genera una passphrase aleatoria de 32 bytes y la almacena cifrada con
 * una clave AES-256 gestionada por el Android Keystore (respaldada por
 * hardware en dispositivos compatibles).
 *
 * Sustituye a la implementación anterior basada en EncryptedSharedPreferences
 * (androidx.security:security-crypto), que está oficialmente deprecada y
 * causa crashes fatales en Android 16+ (API 36+).
 */
object CryptoManager {
    private const val TAG = "CryptoManager"

    // Android Keystore
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "mis_curas_db_key"

    // SharedPreferences (sin cifrar — solo almacena la passphrase ya cifrada)
    private const val PREFS_NAME = "mis_curas_db_prefs"
    private const val KEY_ENCRYPTED_PASSPHRASE = "encrypted_passphrase"
    private const val KEY_IV = "passphrase_iv"

    // Antiguas SharedPreferences cifradas (para migración)
    private const val LEGACY_PREFS_NAME = "mis_curas_secure_prefs"
    private const val LEGACY_KEY = "db_passphrase"

    // GCM tag length
    private const val GCM_TAG_LENGTH = 128

    fun getDatabasePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Intentar leer la passphrase del nuevo almacenamiento
        val existingPassphrase = readFromNewStorage(prefs)
        if (existingPassphrase != null) {
            return existingPassphrase
        }

        // 2. Intentar migrar desde EncryptedSharedPreferences (instalaciones existentes)
        val legacyPassphrase = tryReadLegacy(context)
        if (legacyPassphrase != null) {
            Log.i(TAG, "Migrada passphrase desde EncryptedSharedPreferences al nuevo almacenamiento.")
            saveToNewStorage(prefs, legacyPassphrase)
            cleanupLegacy(context)
            return legacyPassphrase
        }

        // 3. Generar nueva passphrase (instalación nueva)
        val newPassphrase = ByteArray(32)
        SecureRandom().nextBytes(newPassphrase)
        saveToNewStorage(prefs, newPassphrase)
        Log.i(TAG, "Nueva passphrase generada y almacenada.")
        return newPassphrase
    }

    // ── Nuevo almacenamiento (AndroidKeyStore + SharedPreferences normales) ──

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        val existingKey = keyStore.getEntry(KEY_ALIAS, null)
        if (existingKey is KeyStore.SecretKeyEntry) {
            return existingKey.secretKey
        }

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGen.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGen.generateKey()
    }

    private fun readFromNewStorage(prefs: SharedPreferences): ByteArray? {
        val encryptedB64 = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null) ?: return null
        val ivB64 = prefs.getString(KEY_IV, null) ?: return null

        return try {
            val key = getOrCreateKey()
            val iv = Base64.decode(ivB64, Base64.DEFAULT)
            val encrypted = Base64.decode(encryptedB64, Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            cipher.doFinal(encrypted)
        } catch (e: Exception) {
            Log.e(TAG, "Error al descifrar passphrase del nuevo almacenamiento", e)
            // Si la clave del Keystore se corrompió, borrar y regenerar
            prefs.edit().clear().apply()
            null
        }
    }

    private fun saveToNewStorage(prefs: SharedPreferences, passphrase: ByteArray) {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(passphrase)
        val iv = cipher.iv

        prefs.edit()
            .putString(KEY_ENCRYPTED_PASSPHRASE, Base64.encodeToString(encrypted, Base64.DEFAULT))
            .putString(KEY_IV, Base64.encodeToString(iv, Base64.DEFAULT))
            .apply()
    }

    // ── Migración desde EncryptedSharedPreferences (legacy) ──

    private fun tryReadLegacy(context: Context): ByteArray? {
        return try {
            val masterKey = androidx.security.crypto.MasterKey.Builder(context)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build()

            val legacyPrefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                context,
                LEGACY_PREFS_NAME,
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val passphraseB64 = legacyPrefs.getString(LEGACY_KEY, null)
            if (passphraseB64 != null) {
                Base64.decode(passphraseB64, Base64.DEFAULT)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo leer EncryptedSharedPreferences (esperado en Android 16+)", e)
            null
        }
    }

    private fun cleanupLegacy(context: Context) {
        try {
            // Borrar el archivo de preferencias cifradas antiguo
            val prefsFile = java.io.File(context.applicationInfo.dataDir, "shared_prefs/$LEGACY_PREFS_NAME.xml")
            if (prefsFile.exists()) {
                prefsFile.delete()
                Log.i(TAG, "Archivo de preferencias cifradas antiguo eliminado.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo limpiar EncryptedSharedPreferences antiguo", e)
        }
    }
}

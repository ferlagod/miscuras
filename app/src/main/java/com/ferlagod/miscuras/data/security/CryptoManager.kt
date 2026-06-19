package com.ferlagod.miscuras.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import android.util.Base64

object CryptoManager {
    private const val PREFS_NAME = "mis_curas_secure_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"

    fun getDatabasePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var passphraseBase64 = sharedPreferences.getString(KEY_DB_PASSPHRASE, null)
        if (passphraseBase64 == null) {
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            passphraseBase64 = Base64.encodeToString(randomBytes, Base64.DEFAULT)
            sharedPreferences.edit().putString(KEY_DB_PASSPHRASE, passphraseBase64).apply()
        }

        return Base64.decode(passphraseBase64, Base64.DEFAULT)
    }
}

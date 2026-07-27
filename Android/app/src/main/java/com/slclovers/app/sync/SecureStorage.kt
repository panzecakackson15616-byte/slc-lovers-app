package com.slclovers.app.sync

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * 安全存储：用 EncryptedSharedPreferences 存敏感信息
 */
object SecureStorage {

    private const val PREFS_NAME = "slc_secure_prefs"

    private fun masterKey(context: Context): MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey(context),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(context: Context, key: String, value: String) {
        prefs(context).edit().putString(key, value).apply()
    }

    fun load(context: Context, key: String): String? = prefs(context).getString(key, null)

    fun delete(context: Context, key: String) {
        prefs(context).edit().remove(key).apply()
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    // 业务键
    object Key {
        const val GITHUB_TOKEN = "github.token"
        const val GITHUB_OWNER = "github.owner"
        const val GITHUB_REPO = "github.repo"
        const val PAIRING_CODE = "pairing.code"
        const val PAIRING_ID = "pairing.id"
        const val LAST_SYNC_AT = "sync.lastAt"
    }
}
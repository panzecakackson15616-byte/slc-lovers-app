package com.slclovers.app.sync

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 端到端加密
 * AES-GCM-256，密钥从配对码 + 配对 ID 派生
 */
object SLCCrypto {

    private const val AES_GCM = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128 // bits
    private const val IV_LENGTH = 12 // bytes

    /**
     * 从配对码 + 配对 ID 派生密钥
     * 简化版：直接 SHA-256（生产建议用 PBKDF2）
     */
    fun deriveKey(pairingCode: String, pairingId: String): SecretKey {
        val password = "$pairingCode:$pairingId:salt-SLC-Lovers-v1".toByteArray(Charsets.UTF_8)
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val keyBytes = md.digest(password)
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * 加密
     * 输出格式：Base64( IV(12) || Ciphertext || Tag(16) )
     */
    fun encrypt(data: ByteArray, key: SecretKey): String {
        val cipher = Cipher.getInstance(AES_GCM)
        val iv = ByteArray(IV_LENGTH).also { java.security.SecureRandom().nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(data)

        // 拼接 IV + ciphertext(with tag)
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * 解密
     */
    fun decrypt(base64: String, key: SecretKey): ByteArray {
        val combined = Base64.decode(base64, Base64.NO_WRAP)
        require(combined.size > IV_LENGTH) { "Invalid ciphertext length" }

        val iv = combined.copyOfRange(0, IV_LENGTH)
        val ciphertext = combined.copyOfRange(IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }
}
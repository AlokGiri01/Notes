package com.shagworld.notes.uitls

import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Alok Giri on 4/17/2025
 * Mailing Add :- alokgiri1790@gmail.com
 */
object EncryptionHelper {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"


    fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    fun encryptAESKeyWithRSA(aesKey: SecretKey): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val publicKey = keyStore.getCertificate("rsa_key_alias").publicKey

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(aesKey.encoded)
    }

    fun encryptAESKeyWithPassword(secretKey: SecretKey, password: String): Map<String, String> {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val tmp = factory.generateSecret(spec)
        val passwordBasedKey = SecretKeySpec(tmp.encoded, "AES")

        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, passwordBasedKey, ivSpec)

        val encrypted = cipher.doFinal(secretKey.encoded)

        LogMgr().i("encryptedKey-->$encrypted")
        LogMgr().i("salt-->$salt")
        LogMgr().i("iv-->$iv")
        return mapOf(
            "encryptedKey" to Base64.encodeToString(encrypted, Base64.NO_WRAP),
            "salt" to Base64.encodeToString(salt, Base64.NO_WRAP),
            "iv" to Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    fun decryptAESKeyWithPassword(
        encryptedKeyBase64: String,
        saltBase64: String,
        ivBase64: String,
        password: String
    ): SecretKey {
        val encryptedKey = Base64.decode(encryptedKeyBase64, Base64.NO_WRAP)
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val tmp = factory.generateSecret(spec)
        val passwordBasedKey = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, passwordBasedKey, IvParameterSpec(iv))

        val decrypted = cipher.doFinal(encryptedKey)
        return SecretKeySpec(decrypted, "AES")
    }

    fun uploadEncryptedAESKeyToFirestore(
        aesKey: SecretKey,
        map: Map<String, String>,
        userId: String
    ) {
        LogMgr().i("map-->$map")
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection(Constants.SECURE_KEY).document(userId)
            .set(map)
            .addOnSuccessListener {
                LogMgr().i("Firebase-->AES key uploaded!")
                Pref.secretAESKey =  secretKeyToBase64(aesKey)
            }
            .addOnFailureListener { e -> LogMgr().e("Firebase-->Upload failed-->${e.message}") }
    }


    fun decryptAESKeyWithRSA(encryptedAES: ByteArray): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val privateKey = keyStore.getKey("rsa_key_alias", null) as PrivateKey

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val aesKeyBytes = cipher.doFinal(encryptedAES)
        return SecretKeySpec(aesKeyBytes, "AES")
    }

    fun encrypt(text: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, base64ToSecretKey(Pref.secretAESKey))
        val iv = cipher.iv
        val encrypted = cipher.doFinal(text.toByteArray())
        return Base64.encodeToString(iv + encrypted, Base64.DEFAULT)
    }

    fun decrypt(data: String): String {
        val raw = Base64.decode(data, Base64.DEFAULT)
        val iv = raw.copyOfRange(0, 12)
        val encrypted = raw.copyOfRange(12, raw.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, base64ToSecretKey(Pref.secretAESKey), GCMParameterSpec(128, iv))
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted)
    }

    fun getSecretAESKey(encryptedBase64: String): SecretKey {
        val encryptedKeyBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
        return decryptAESKeyWithRSA(encryptedKeyBytes)
    }
    fun secretKeyToBase64(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
    }

    private fun base64ToSecretKey(encodedKey: String): SecretKey {
        val decodedKey = Base64.decode(encodedKey, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

}
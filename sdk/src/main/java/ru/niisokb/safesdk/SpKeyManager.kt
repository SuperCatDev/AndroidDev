package ru.niisokb.safesdk

import android.annotation.SuppressLint
import android.os.Build
import android.os.Environment
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Key
import java.security.KeyStore
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import kotlin.experimental.xor

/** Предоставляет функции шифрования. */
object SpKeyManager {
    const val MODE_ECB = "ECB"
    const val MODE_CBC = "CBC"

    private const val TAG = "SpKeyManager"
    private const val ALIAS = "KEY_ALIAS"
    private const val PASS = "pass"

    private const val IV_SIZE = 16
    private const val WRONG_DATA_SIZE_MSG = "You data for encryption have wrong size"

    private val DIRECTORY_KEY_STORE_PATH = Environment.getExternalStorageDirectory().path +
            "/SafePhone"
    private val KEY_STORE_PATH = "$DIRECTORY_KEY_STORE_PATH/SafePhoneStorage"

    /** Настройка для выбранного метода шифрования. */
    @RequiresApi(Build.VERSION_CODES.M)
    private const val AES_ECB_MODE = KeyProperties.KEY_ALGORITHM_AES + "/" +
            KeyProperties.BLOCK_MODE_ECB + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7

    @RequiresApi(Build.VERSION_CODES.M)
    private const val AES_CBC_MODE = KeyProperties.KEY_ALGORITHM_AES + "/" +
            KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7

    @RequiresApi(Build.VERSION_CODES.M)
    private val entryProtectionParameter = KeyStore.PasswordProtection("EXCEPTIONS_123".toCharArray())
    @RequiresApi(Build.VERSION_CODES.M)
    private var cipher: Cipher = Cipher.getInstance(AES_CBC_MODE)
    private var keyStore = KeyStore.getInstance("AndroidKeyStore")

    private var currentMode = MODE_CBC

    fun getCurrentMode() = currentMode

    @SuppressLint("GetInstance")
    @RequiresApi(Build.VERSION_CODES.M)
    fun setEncryptionMode(mode: String) {
        var futureMode = MODE_CBC

        if (mode == MODE_ECB) {
            futureMode = MODE_ECB
        }

        if (futureMode == currentMode) {
            return
        }

        currentMode = futureMode

        keyStore = if (futureMode == MODE_CBC) {
            KeyStore.getInstance("AndroidKeyStore")
        } else {
            KeyStore.getInstance(KeyStore.getDefaultType())
        }

        cipher = if (futureMode == MODE_CBC) {
            Cipher.getInstance(AES_CBC_MODE)
        } else {
            Cipher.getInstance(AES_ECB_MODE)
        }

    }

    /** Шифрует [ByteArray] и возвращает его. */
    fun encrypt(data: ByteArray): ByteArray =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (currentMode == MODE_ECB) {
                    encryptAesEcb(data)
                } else {
                    encryptAesCbc(data)
                }
            } else {
                encryptXor(data)
            }


    /** Дешифрует [ByteArray] и возвращает его. */
    fun decrypt(data: ByteArray): ByteArray =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (currentMode == MODE_ECB) {
                    decryptAesEcb(data)
                } else {
                    decryptAesCbc(data)
                }
            } else {
                // До Marshmallow используется XOR, поэтому encryptAesEcb == decryptAesEcb
                encryptXor(data)
            }

    /** Шифрование с помощью ключа полученного из [KeyStore]. */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun encryptAesEcb(data: ByteArray): ByteArray {
        var result = ByteArray(0)

        try {
            val key = getStoreKeyEcb(ALIAS)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            result = cipher.doFinal(data)
        } catch (e: IllegalBlockSizeException) {
            Log.e(TAG, e.toString())
        } catch (e: BadPaddingException) {
            Log.e(TAG, e.toString())
        }

        return result
    }

    /** Дешифрование с помощью ключа полученного из [KeyStore]. */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun decryptAesEcb(data: ByteArray): ByteArray {
        var result = ByteArray(0)

        try {
            val key = getStoreKeyEcb(ALIAS)
            cipher.init(Cipher.DECRYPT_MODE, key)
            result = cipher.doFinal(data)
        } catch (e: IllegalBlockSizeException) {
            Log.e(TAG, e.toString())
        } catch (e: BadPaddingException) {
            Log.e(TAG, e.toString())
        }

        return result
    }

    /** Шифрование с помощью ключа полученного из [KeyStore]. */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun encryptAesCbc(data: ByteArray): ByteArray {
        var result = ByteArray(0)

        try {
            val key = getStoreKeyCbc(ALIAS)

            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv

            result = encodeIv(iv) + cipher.doFinal(data)
        } catch (e: IllegalBlockSizeException) {
            Log.e(TAG, e.toString())
        } catch (e: BadPaddingException) {
            Log.e(TAG, e.toString())
        }

        return result
    }

    /** Дешифрование с помощью ключа полученного из [KeyStore]. */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun decryptAesCbc(data: ByteArray): ByteArray {
        var result = ByteArray(0)

        if (data.size < IV_SIZE) {
            Log.e(TAG, WRONG_DATA_SIZE_MSG)
        } else {

            try {
                val key = getStoreKeyCbc(ALIAS)

                cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(encodeIv(data.copyOfRange(0, 16))))

                result = cipher.doFinal(data.copyOfRange(16, data.size))
            } catch (e: IllegalBlockSizeException) {
                Log.e(TAG, e.toString())
            } catch (e: BadPaddingException) {
                Log.e(TAG, e.toString())
            }

        }

        return result
    }

    /** Реализует функцию шифрования для версий android меньше 6.0. */
    private fun encryptXor(data: ByteArray): ByteArray {
        val keyBytes = ALIAS.toByteArray()
        val length = data.size

        for (i in 0 until length) {
            data[i] = data[i] xor keyBytes[i % keyBytes.size]
        }

        return data
    }

    /** Получает ключ из [KeyStore] или создает новый, если ранее ключ был не создан. */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getStoreKeyEcb(alias: String): SecretKey {
        val file = File(KEY_STORE_PATH)

        if (!file.exists()) {
            createAndroidKeyStoreKeyEcb(file, alias)
        } else {
            FileInputStream(file.path).use { fis -> keyStore.load(fis, PASS.toCharArray()) }
        }

        var entry = keyStore.getEntry(alias, entryProtectionParameter) as KeyStore.SecretKeyEntry?

        if (entry == null) {
            createAndroidKeyStoreKeyEcb(file, alias)
            entry = keyStore.getEntry(alias, entryProtectionParameter) as KeyStore.SecretKeyEntry
        }

        return entry.secretKey
    }

    /**
     * Создает симметричный ключ шифрования для алгоритма AES и сохраняет в [KeyStore]
     * и возвращает значение.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun createAndroidKeyStoreKeyEcb(file: File, alias: String) {
        File(DIRECTORY_KEY_STORE_PATH).mkdirs()
        file.createNewFile()

        //Generate our secret key
        keyStore.load(null, null)
        val key = KeyGenerator.getInstance("AES").generateKey()
        val keyStoreEntry = KeyStore.SecretKeyEntry(key)

        //Store our secret key
        keyStore.setEntry(alias, keyStoreEntry, entryProtectionParameter)

        FileOutputStream(file.path).use { fos -> keyStore.store(fos, PASS.toCharArray()) }
    }

    /** Кодирование iv с помощью XOR */
    private fun encodeIv(iv: ByteArray): ByteArray {
        val key = ByteArray(4)
        key[0] = 4
        key[1] = 7
        key[2] = 1
        key[3] = 2

        for (i in 0 until iv.size) {
            iv[i] = iv[i] xor key[i % key.size]
        }

        return iv
    }

    /** Получает ключ из [KeyStore] или создает новый, если ранее ключ был не создан. */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getStoreKeyCbc(alias: String): Key {
        keyStore.load(null)

        return keyStore.getKey(alias, null) ?: createAndroidKeyStoreKeyCbc(alias)
    }

    /**
     * Создает симметричный ключ шифрования для алгоритма AES и сохраняет в [KeyStore]
     * и возвращает значение.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun createAndroidKeyStoreKeyCbc(alias: String): Key {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        initGeneratorWithKeyGenParameterSpec(generator, alias)

        return generator.generateKey()
    }

    /** Настройка параметров для [KeyGenerator]. */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initGeneratorWithKeyGenParameterSpec(generator: KeyGenerator, alias: String) {
        val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        generator.init(builder.build())
    }

}
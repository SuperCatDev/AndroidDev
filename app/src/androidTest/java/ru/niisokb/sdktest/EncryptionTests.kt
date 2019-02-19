package ru.niisokb.sdktest

import android.os.Environment
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import ru.niisokb.safesdk.SpKeyManager
import java.io.File

class EncryptionTests {
    private lateinit var bigString: String
    private lateinit var encryptedFile: File

    private var encryptedData: ByteArray? = null
    private var decryptedData: ByteArray? = null

    @Before
    fun initBigString() {
        bigString = "dskfkd;lskgfkjdskgkhdskgdfkjgkjs"

        for (i in 1..1000) {
            bigString += "BFJhfldskfds" + i.toString()
        }
    }

    @Before
    fun createFile() {
        encryptedFile = File(Environment.getExternalStorageDirectory().toString() +
                "/encryptedFile")
        if (!encryptedFile.exists()) {
            encryptedFile.createNewFile()
        }
    }

    @Test
    fun simpleStringEncryptionEcb() {
        SpKeyManager.setEncryptionMode(SpKeyManager.MODE_ECB)

        val expected = "It's supper-pupper TEST!@*&^"
        this.encrypt(expected)

        assertEquals(expected, this.decrypt())
    }

    @Test
    fun bigStringEncryptionEcb() {
        SpKeyManager.setEncryptionMode(SpKeyManager.MODE_ECB)

        this.encrypt(bigString)

        assertEquals(bigString, this.decrypt())
    }

    @Test
    fun differentFileIDVariantsEcb() {
        SpKeyManager.setEncryptionMode(SpKeyManager.MODE_ECB)

        val encrypted = SpKeyManager.encrypt("Hello".toByteArray())
        val encrypted2 = SpKeyManager.encrypt("Hello".toByteArray())
        val trueDecrypt = SpKeyManager.decrypt(encrypted)
        val trueDecrypt2 = SpKeyManager.decrypt(encrypted2)

        assertEquals("Hello", String(trueDecrypt2))
        assertNotEquals("Hello", String(encrypted2))
        assertEquals("Hello", String(trueDecrypt))
    }

    @Test
    fun simpleStringEncryptionCbc() {
        SpKeyManager.setEncryptionMode(SpKeyManager.MODE_CBC)

        val expected = "It's supper-pupper TEST!@*&^"
        this.encrypt(expected)

        assertEquals(expected, this.decrypt())
    }

    @Test
    fun bigStringEncryptionCbc() {
        SpKeyManager.setEncryptionMode(SpKeyManager.MODE_CBC)

        this.encrypt(bigString)

        assertEquals(bigString, this.decrypt())
    }

    @Test
    fun differentFileIDVariantsCbc() {
        SpKeyManager.setEncryptionMode(SpKeyManager.MODE_CBC)

        val encrypted = SpKeyManager.encrypt("Hello".toByteArray())
        val encrypted2 = SpKeyManager.encrypt("Hello".toByteArray())
        val trueDecrypt = SpKeyManager.decrypt(encrypted)
        val trueDecrypt2 = SpKeyManager.decrypt(encrypted2)

        assertEquals("Hello", String(trueDecrypt2))
        assertNotEquals("Hello", String(encrypted2))
        assertEquals("Hello", String(trueDecrypt))
    }

    @After
    fun cleanup() {
        this.encryptedFile.delete()
    }

    private fun encrypt(message: String) {
        val bytes = message.toByteArray()
        encryptedData = SpKeyManager.encrypt(bytes)
        encryptedFile.writeBytes(encryptedData!!)
    }

    private fun decrypt(): String {
        val bytes = encryptedFile.readBytes()
        decryptedData = SpKeyManager.decrypt(bytes)

        return String(decryptedData!!)
    }
}

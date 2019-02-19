package ru.niisokb.safesdk.modules.ssl

import android.annotation.SuppressLint
import android.util.Log
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal class DefaultTrustManager
@Throws(KeyStoreException::class, NoSuchAlgorithmException::class, CertificateException::class, IOException::class)
internal constructor() : X509TrustManager {
    private val defaultTrustManager: X509TrustManager

    init {
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)

        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val dtmf = TrustManagerFactory.getInstance(tmfAlgorithm)
        dtmf.init(keyStore)
        defaultTrustManager = dtmf.trustManagers[0] as X509TrustManager
    }

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        // Актуально только для сервера, а мы клиент
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        Log.d(TAG, "Checking server certificate by Android default CAs")
        defaultTrustManager.checkServerTrusted(chain, authType)
        Log.d(TAG, "Server certificate approved.")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }

    companion object {
        private const val TAG = "DefaultTrustManager"
    }
}
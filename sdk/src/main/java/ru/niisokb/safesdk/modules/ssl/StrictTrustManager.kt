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

internal class StrictTrustManager
@Throws(KeyStoreException::class, NoSuchAlgorithmException::class, CertificateException::class,
        IOException::class)
internal constructor(localKeyStore: KeyStore) : X509TrustManager {
    private var localTrustManager: X509TrustManager? = null

    init {
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(localKeyStore)
        localTrustManager = tmf.trustManagers[0] as X509TrustManager
    }

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        // Актуально только для сервера, а мы клиент
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        if (localTrustManager != null) {
            Log.d(TAG, "Checking server certificate by local CA")
            localTrustManager!!.checkServerTrusted(chain, authType)
            Log.d(TAG, "Server certificate approved.")
        } else {
            throw CertificateException("Server certificate cannot be trusted.")
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }

    companion object {
        private const val TAG = "StrictTrustManager"
    }
}

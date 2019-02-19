package ru.niisokb.safesdk.modules.ssl

import android.text.TextUtils
import okhttp3.OkHttpClient
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Возвращает HTTP-клиент со строгим или нестрогим SSL [TrustManager] в зависимости от типа сборки.
 */
internal fun getClient(cert: String?, nonStrictMode: Boolean = false): OkHttpClient {
    try {
        val builder = OkHttpClient.Builder()
        var ca: Certificate? = null

        if (!nonStrictMode && !TextUtils.isEmpty(cert)) {
            val cf = CertificateFactory.getInstance("X.509")
            val cs = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                charset(StandardCharsets.UTF_8.name())
            else
                Charset.forName("UTF-8")

            ByteArrayInputStream(cert!!.toByteArray(cs))
                    .use { caInput -> ca = cf.generateCertificate(caInput) }
        }

        val trustCerts = if (nonStrictMode) {
            provideNonStrictTrustManager()
        } else {
            provideTrustManager(ca)
        }

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustCerts, java.security.SecureRandom())

        val sslSocketFactory = sslContext.socketFactory
        builder.sslSocketFactory(sslSocketFactory, trustCerts[0] as X509TrustManager)
        builder.hostnameVerifier { _, _ -> true }

        return builder.build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

/**
 * Разрешает соединение с любыми сертификатами
 */
private fun provideNonStrictTrustManager(): Array<TrustManager> {
    return arrayOf(NonStrictTrustManager())
}

/**
 * Если есть сертификат, разрешает соединение только с ним, иначе, только с системными
 */
@Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
private fun provideTrustManager(certificate: Certificate?): Array<TrustManager> {
    if (certificate == null) {
        return arrayOf(DefaultTrustManager())
    }

    val keyStoreType = KeyStore.getDefaultType()
    val keyStore = KeyStore.getInstance(keyStoreType)
    keyStore.load(null, null)
    keyStore.setCertificateEntry("ca", certificate)

    return arrayOf(StrictTrustManager(keyStore))
}
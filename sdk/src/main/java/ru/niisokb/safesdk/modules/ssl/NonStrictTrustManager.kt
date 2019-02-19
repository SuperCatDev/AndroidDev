package ru.niisokb.safesdk.modules.ssl

import android.annotation.SuppressLint

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

internal class NonStrictTrustManager : X509TrustManager {
    @SuppressLint("TrustAllX509TrustManager")
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        // Доверяем всем сертификатам
    }

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        // Доверяем всем сертификатам
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}

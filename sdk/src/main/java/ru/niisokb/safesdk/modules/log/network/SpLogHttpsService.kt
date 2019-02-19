package ru.niisokb.safesdk.modules.log.network

import android.util.Log
import arrow.core.Either
import arrow.core.Left
import kotlinx.coroutines.delay
import ru.niisokb.safesdk.HttpsError
import ru.niisokb.safesdk.SpHttpsService
import ru.niisokb.safesdk.SpLog
import ru.niisokb.safesdk.config.CommonConfig
import ru.niisokb.safesdk.configuration.AppInfo

internal object SpLogHttpsService : SpHttpsService<MdmLogApi>(MdmLogApi::class.java) {
    private const val TAG = "SpLogHttpsService"
    private const val minRetryTimeout = 1000L

    private var mobileId: Int = -1
    private var logsSendInterval = SpLog.logsSendInterval
    private var currentUrl = ""
    private var currentCert = ""
    private var nonStrictMode = false

    private suspend fun initHttpsService(): Boolean {
        mobileId = CommonConfig.getMobileId()
        logsSendInterval = SpLog.logsSendInterval
        val host = CommonConfig.getMdmAddr()
        val cert = CommonConfig.getMdmCert()
        val debugMode = AppInfo.debug

        // Превращаем хост в протокол
        val url = "https://$host"

        // Доверяем тому, что пришло из конфигурации, даже если там будет пустая ссылка
        if (currentUrl != url || currentCert != cert || nonStrictMode != debugMode) {
            currentUrl = url
            currentCert = cert
            nonStrictMode = debugMode

            return initHttpsService(currentUrl, currentCert, nonStrictMode)
        }

        if (service == null) {
            Log.w(TAG, "[initHttpsService] HTTPS service not initialized: URL = $url")
            return false
        }

        return true
    }

    suspend fun sendLog(log: List<String>) {
        var timeout = minRetryTimeout
        while (true) {
            var sendComplete = false
            var responseCode = 0

            if (initHttpsService()) {
                val response = tryToSend(log)
                when (response) {
                    is Either.Right -> {
                        responseCode = 200
                        sendComplete = true
                    }
                    is Either.Left -> {
                        val error = response.a
                        responseCode = when (error) {
                            is HttpsError.ResponseException -> -1
                            is HttpsError.BadResponse -> error.code
                        }
                    }
                }
            }

            responseCallback?.invoke(responseCode, getResponseCodeName(responseCode))

            if (!sendComplete) {
                Log.d(TAG, "[sendLog] Sleeping for $timeout millis...")
                delay(timeout)
                timeout = (timeout * 2).coerceAtMost(logsSendInterval)
            } else {
                break
            }
        }
    }

    private suspend fun tryToSend(log: List<String>): Either<HttpsError, Int> {
        val srv = service
                ?: return Left(HttpsError.ResponseException(null, "service not initialized"))

        val call = srv.sendLog(AppInfo.packageName, CommonConfig.getAppLocation(), mobileId, log)
        return send(call)
    }
}

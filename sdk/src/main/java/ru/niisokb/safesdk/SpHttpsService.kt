package ru.niisokb.safesdk

import android.util.Log
import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.niisokb.safesdk.modules.ssl.getClient

typealias ResponseCallback = (Int, String) -> Unit

abstract class SpHttpsService<T>(private val serviceApi: Class<T>) {
    /** HTTPS сервис. */
    protected var service: T? = null

    /** Позволяет подписаться на код ответа HTTPS сервиса. */
    var responseCallback: ResponseCallback? = null

    protected fun initHttpsService(hostUrl: String, cert: String, nonStrictMode: Boolean = false): Boolean {
        Log.i(TAG, "[initHttpsService] Initializing HTTPS service: URL = $hostUrl")

        return try {
            val retrofit = Retrofit.Builder()
                    .baseUrl(hostUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient(cert, nonStrictMode))
                    .build()

            service = retrofit.create(serviceApi)

            Log.i(TAG, "[initHttpsService] HTTPS service initialized: URL = $hostUrl")

            service != null
        } catch (e: Exception) {
            Log.w(TAG, "[initHttpsService] Couldn't initialize HTTPS service: URL = $hostUrl")

            false
        }
    }

    /** Отправка запроса, если нет необходимости получать данные */
    protected fun <C> send(call: Call<C>): Either<HttpsError, Int> {
        try {
            val response = call.execute()

            if (!response.isSuccessful) {
                val msg = getResponseErrorMsg(response)
                Log.w(TAG, "[send] $msg")
                return Left(HttpsError.BadResponse(response.code(), msg))
            }

            return Right(response.code())
        } catch (e: Exception) {
            val msg = "[send] Request failed: " + e.message
            Log.w(TAG, msg)

            return Left(HttpsError.ResponseException(e, msg))
        }
    }

    /** Отправка запроса, и возврат полученных данных */
    protected fun <C> request(call: Call<C>): Either<HttpsError, C?> {
        try {
            val response = call.execute()

            if (response.isSuccessful) {
                return Right(response.body())
            }

            val msg = getResponseErrorMsg(response)
            Log.w(TAG, "[request] $msg")

            return Left(HttpsError.BadResponse(response.code(), msg))
        } catch (e: Exception) {
            val msg = "[request] Request failed: " + e.message
            Log.w(TAG, msg)

            return Left(HttpsError.ResponseException(e, msg))
        }
    }

    companion object {
        protected const val TAG = "SpHttpsService"
        protected val httpResponseCodes = mapOf(
                0 to "Service not initialized",
                -1 to "No response",
                200 to "OK",
                400 to "Bad Request",
                404 to "Not Found",
                429 to "Too Many Requests",
                503 to "Service Unavailable"
        )

        fun getResponseCodeName(code: Int) =
                if (httpResponseCodes.containsKey(code)) httpResponseCodes[code]!! else ""

        private fun <C> getResponseErrorMsg(response: Response<C>) = "Response is not successful. " +
                "Code: ${response.code()} ${getResponseCodeName(response.code())}, " +
                "message: ${response.message()}, error: ${response.errorBody()}"
    }
}

sealed class HttpsError {
    data class BadResponse(val code: Int, val msg: String) : HttpsError()

    data class ResponseException(val th: Throwable?, val msg: String) : HttpsError()
}

package ru.niisokb.safesdk.modules.log.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface MdmLogApi {
    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("/v1/logs")
    fun sendLog(@Header("X-Application-Id") packageName: String,
                @Header("X-Installation-Id") installLocation: String,
                @Header("X-MCC-ID") mobileId: Int,
                @Body log: List<String>)
            : Call<Void>
}

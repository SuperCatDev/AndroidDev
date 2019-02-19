package ru.niisokb.safesdk.config

import kotlinx.coroutines.runBlocking

typealias ResponseCallback = (String, String, Int, String) -> Unit

/** Позволяет получить текущие настройки SDK. */
@Suppress("unused")
object SpConfigInfo {

    fun subscribeToConfigChange(responseCallback: ResponseCallback) {
        CommonConfig.subscribeToConfigChange(responseCallback)
    }

    fun getMdmAddr(): String = runBlocking {
        CommonConfig.getMdmAddr()
    }

    fun getMdmCrt(): String = runBlocking {
        CommonConfig.getMdmCert()
    }

    fun getMobileId(): Int = runBlocking {
        CommonConfig.getMobileId()
    }

    fun getAppLocation(): String = runBlocking {
        CommonConfig.getAppLocation()
    }
}
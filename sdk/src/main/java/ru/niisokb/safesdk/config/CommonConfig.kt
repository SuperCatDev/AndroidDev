package ru.niisokb.safesdk.config

import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import ru.niisokb.safesdk.configuration.ConfigAttr
import ru.niisokb.safesdk.configuration.ValType
import ru.niisokb.safesdk.mixins.ConfigurableModule

@Suppress("MemberVisibilityCanBePrivate")
internal object CommonConfig : ConfigurableModule {

    private const val TAG = "CommonConfig"

    private const val MDM_ADDR_KEY = "safephone.common.mdm_addr"
    private const val MDM_ADDR_DEFAULT = ""
    private const val MDM_CERT_KEY = "safephone.common.mdm_cert"
    private const val MDM_CERT_DEFAULT = ""
    private const val MOBILE_ID_KEY = "safephone.common.mobile_id"
    private const val MOBILE_ID_DEFAULT = -1
    private const val APP_LOCATION_KEY = "safephone.common.app_location"
    private const val APP_LOCATION_DEFAULT = "device"

    override val attrs = listOf(
            ConfigAttr(MDM_ADDR_KEY, ValType.String, MDM_ADDR_DEFAULT),
            ConfigAttr(MDM_CERT_KEY, ValType.String, MDM_CERT_DEFAULT),
            ConfigAttr(MOBILE_ID_KEY, ValType.Int, MOBILE_ID_DEFAULT),
            ConfigAttr(APP_LOCATION_KEY, ValType.String, APP_LOCATION_DEFAULT)
    )

    override val dispatch: (Bundle) -> Unit = { config ->
        mdmAddr = config.getString(MDM_ADDR_KEY, MDM_ADDR_DEFAULT)
        mdmCert = config.getString(MDM_CERT_KEY, MDM_CERT_DEFAULT)
        mobileId = config.getInt(MOBILE_ID_KEY, MOBILE_ID_DEFAULT)
        appLocation = config.getString(APP_LOCATION_KEY, APP_LOCATION_DEFAULT)

        Log.i(TAG, """
            [dispatch] SDK config updated:
            mdmAddr=$mdmAddr
            mdmCert=$mdmCert
            mobileId=$mobileId
            appLocation=$appLocation
        """.trimIndent())

        trigger.complete(Unit)

        // Send config values to subscriber.
        responseCallback?.invoke(mdmAddr, mdmCert, mobileId, appLocation)
    }

    init {
        register()
    }

    private var mdmAddr = MDM_ADDR_DEFAULT
    private var mdmCert = MDM_CERT_DEFAULT
    private var mobileId = MOBILE_ID_DEFAULT
    private var appLocation = APP_LOCATION_DEFAULT
    private val trigger = CompletableDeferred<Unit>()
    private var responseCallback: ResponseCallback? = null

    val getMdmAddr = suspend {
        trigger.await()
        mdmAddr
    }

    val getMdmCert = suspend {
        trigger.await()
        mdmCert
    }

    val getMobileId = suspend {
        trigger.await()
        mobileId
    }

    val getAppLocation = suspend {
        trigger.await()
        appLocation
    }

    fun subscribeToConfigChange(responseCallback: ResponseCallback) {
        this.responseCallback = responseCallback
    }
}
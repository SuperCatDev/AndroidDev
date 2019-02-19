package ru.niisokb.sdktest

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test
import ru.niisokb.safesdk.SpConfigDispatcher
import ru.niisokb.safesdk.SpLog

class LogTests {
    private val mdmUrl = "http://10.79.8.206:8088"
    private lateinit var policyManager: DevicePolicyManager
    private val mobileId = 109

    @Before
    fun initSdk() {
        SpConfigDispatcher.load(InstrumentationRegistry.getInstrumentation().targetContext)

        policyManager = InstrumentationRegistry.getInstrumentation().targetContext
                .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    @Test
    fun nothingFalls() {
        val settings = Bundle()
        settings.putString("safephone.common_mdm_addr", mdmUrl)
        settings.putInt("safephone.common_mobile_id", mobileId)

        policyManager.setApplicationRestrictions(ComponentName(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MyDeviceAdminReceiver::class.java),
                "ru.niisokb.sdktest",
                settings)

        repeat(200) {
            SpLog.i("TEST", "MSG $it")
        }

        Log.d("TEST", "WAITING START")

    }
}
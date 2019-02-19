package ru.niisokb.sdktest

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.niisokb.safesdk.SpConfigDispatcher
import ru.niisokb.safesdk.SpLocationManager

class LocationManagerTests {
    private lateinit var policyManager: DevicePolicyManager

    @Before
    fun initPolicyManager() {
        policyManager = InstrumentationRegistry.getInstrumentation().targetContext
                .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        SpConfigDispatcher.load(InstrumentationRegistry.getInstrumentation().targetContext)

        SpLocationManager.load(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun restrictionsDisableCheck() {
        val settings = Bundle()
        settings.putInt(SpLocationManager.GPS_ENABLED_KEY, 0)

        policyManager.setApplicationRestrictions(ComponentName(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MyDeviceAdminReceiver::class.java),
                "ru.niisokb.sdktest",
                settings)

        Thread.sleep(1_000)  // TODO Сделать через IdlingResource

        Assert.assertEquals(true, SpLocationManager.isInitialized())
        Assert.assertEquals(false, SpLocationManager.isGpsAccessEnabled())
    }

    @Test
    fun restrictionsEnableCheck() {
        val settings = Bundle()
        settings.putInt(SpLocationManager.GPS_ENABLED_KEY, 1)

        policyManager.setApplicationRestrictions(ComponentName(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MyDeviceAdminReceiver::class.java)
                , "ru.niisokb.sdktest",
                settings)

        Thread.sleep(1_000)

        Assert.assertEquals(true, SpLocationManager.isInitialized())
        Assert.assertEquals(true, SpLocationManager.isGpsAccessEnabled())
    }
}
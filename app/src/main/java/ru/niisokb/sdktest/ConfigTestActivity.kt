package ru.niisokb.sdktest

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.RestrictionsManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_config_test.*
import ru.niisokb.safesdk.SpLog

class ConfigTestActivity : AppCompatActivity() {

    private lateinit var policyManager: DevicePolicyManager
    private lateinit var restrictionsManager: RestrictionsManager
    private var initialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_test)

        restrictionsManager = applicationContext.getSystemService(Context.RESTRICTIONS_SERVICE)
                as RestrictionsManager

        requestAdmin()

        button_apply_mdm.setOnClickListener { applyMdm() }
        button_apply_packet_size.setOnClickListener { applyPacketSize() }
        button_apply_sending_interval.setOnClickListener { applySendingInterval() }
        button_apply_configs.setOnClickListener { applyAll() }
        button_request_admin.setOnClickListener { requestAdmin() }
        button_to_log_test.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    fun initPolicyManager() {
        policyManager = applicationContext
                .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        initialized = true
    }

    fun requestAdmin() {
        val deviceAdminComponentName = ComponentName(applicationContext, MyDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Your boss told you to do this")
        this.startActivityForResult(intent, 1)
    }

    private fun applyMdm() {
        if (!initialized) {
            initPolicyManager()
        }

        val settings = getCurrentConfig()
        if (!et_mdm_addr.text.toString().isEmpty()) {
            settings.putString(MDM_ADDR_KEY, et_mdm_addr.text.toString())
        }

        setNewConfig(settings)
    }

    private fun applyPacketSize() {
        if (!initialized) {
            initPolicyManager()
        }

        val settings = getCurrentConfig()
        if (!et_packet_size.text.toString().isEmpty()) {
            settings.putInt(SpLog.LOGS_CHUNK_SIZE_KEY, et_packet_size.text.toString().toInt())
        }

        setNewConfig(settings)
    }

    private fun applySendingInterval() {
        if (!initialized) {
            initPolicyManager()
        }

        val settings = getCurrentConfig()
        if (!et_sending_interval.text.toString().isEmpty()) {
            settings.putInt(SpLog.LOGS_SEND_INTERVAL_KEY, et_sending_interval.text.toString().toInt())
        }

        setNewConfig(settings)
    }

    private fun applyAll() {
        if (!initialized) {
            initPolicyManager()
        }

        val settings = getCurrentConfig()
        if (!et_mdm_addr.text.toString().isEmpty()) {
            settings.putString(MDM_ADDR_KEY, et_mdm_addr.text.toString())
        }
        if (!et_packet_size.text.toString().isEmpty()) {
            settings.putInt(SpLog.LOGS_CHUNK_SIZE_KEY, et_packet_size.text.toString().toInt())
        }
        if (!et_sending_interval.text.toString().isEmpty()) {
            settings.putInt(SpLog.LOGS_SEND_INTERVAL_KEY, et_sending_interval.text.toString().toInt())
        }

        setNewConfig(settings)
    }

    private fun setNewConfig(settings: Bundle) {
        policyManager.setApplicationRestrictions(ComponentName(
                applicationContext,
                MyDeviceAdminReceiver::class.java),
                "ru.niisokb.sdktest",
                settings)
    }

    private fun getCurrentConfig(): Bundle {
        return restrictionsManager.applicationRestrictions
    }

    companion object {
        private const val MDM_ADDR_KEY = "safephone.common.mdm_addr"
    }
}

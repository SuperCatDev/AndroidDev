package ru.niisokb.sdktest

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import ru.niisokb.safesdk.SpConfigDispatcher
import ru.niisokb.safesdk.SpLog
import ru.niisokb.safesdk.config.SpConfigInfo
import java.util.*

class MainActivity : ScopedAppActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val ACTIVATION_REQUEST = 1
        private const val DEBUG_WITH_ADMIN = false

        fun requestAdmin(applicationContext: Context, activity: Activity) {
            val deviceAdminComponentName = ComponentName(applicationContext, MyDeviceAdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Your boss told you to do this")
            activity.startActivityForResult(intent, ACTIVATION_REQUEST)
        }

        fun requestStoragePermissions(context: Context) {
            val readStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            val writeStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (readStatus != PackageManager.PERMISSION_GRANTED || writeStatus != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1)
            }
        }
    }

    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        @Suppress("ConstantConditionIf")
        if (DEBUG_WITH_ADMIN) {
            requestAdmin(applicationContext, this)
            requestStoragePermissions(this)
        }

        button_single_msg.setOnClickListener { sendSingleMsg() }
        button_many_msg.setOnClickListener { sendManyMsg() }
        button_multiline_msg.setOnClickListener { sendMultilineMsg() }
        button_levels_msg.setOnClickListener { sendLevelsMsg() }
        button_apply_config.setOnClickListener {
            startActivity(
                    Intent(this, ConfigTestActivity::class.java))
        }

        // Инициализируем SDK.
        if (!SpConfigDispatcher.isInitialized()) {
            SpConfigDispatcher.load(applicationContext)
        }
        // Подписываемся на код ответа HTTP сервиса.
        SpLog.subscribeToServiceResponse { code, codeName -> showResponseCodeView(code, codeName) }
        // Подписываемся на изменение конфигурации.
        SpConfigInfo.subscribeToConfigChange { mdmAddr, _, _, _ -> showServiceAddr(mdmAddr) }
    }

    private fun sendSingleMsg() {
        SpLog.i(TAG, "This is a single log message.")
        showToast("A single message added to send queue.")
    }

    private fun sendManyMsg() {
        val i = 1000
        repeat(i) {
            SpLog.i(TAG, "One from many ($it of $i).")
        }
        showToast("1000 messages added to send queue.")
    }

    private fun sendMultilineMsg() {
        SpLog.i(TAG, """
            Alas, poor Yorick! I knew him, Horatio: a fellow
            of infinite jest, of most excellent fancy: he hath
            borne me on his back a thousand times; and now, how
            abhorred in my imagination it is! my gorge rims at
            it. Here hung those lips that I have kissed I know
            not how oft. Where be your gibes now? your
            gambols? your songs? your flashes of merriment,
            that were wont to set the table on a roar? Not one
            now, to mock your own grinning? quite chap-fallen?
            Now get you to my lady's chamber, and tell her, let
            her paint an inch thick, to this favour she must
            come; make her laugh at that. Prithee, Horatio, tell
            me one thing.
        """.trimIndent())
        showToast("Multiline message added to send queue.")
    }

    private fun sendLevelsMsg() {
        SpLog.v(TAG, "This is a VERBOSE log message.")
        SpLog.d(TAG, "This is a DEBUG log message.")
        SpLog.i(TAG, "This is an INFO log message.")
        SpLog.w(TAG, "This is a WARN log message.")
        SpLog.e(TAG, "This is an ERROR log message.")
        SpLog.wtf(TAG, "This is a ASSERT log message.")
        showToast("V-D-I-W-E-A messages added to send queue.")
    }

    /** Shows service response in UI. */
    private fun showResponseCodeView(code: Int, codeName: String) = launch {
        val t = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = t }
        val hour = "%02d".format(calendar.get(Calendar.HOUR))
        val minute = "%02d".format(calendar.get(Calendar.MINUTE))
        val second = "%02d".format(calendar.get(Calendar.SECOND))
        var msg = "[$hour:$minute:$second] $code"
        if (!TextUtils.isEmpty(codeName)) {
            msg += " $codeName"
        }
        log_service_status.text = msg
    }

    /** Show toast in a non-overlapping way. */
    private fun showToast(msg: String) {
        toast?.cancel()
        toast = Toast.makeText(this, msg, Toast.LENGTH_LONG).apply { show() }
    }

    /** Shows service URL in UI. */
    private fun showServiceAddr(host: String) = launch {
        val url = "https://$host"
        if (!TextUtils.isEmpty(url)) {
            log_service_url.text = url
        }
    }
}

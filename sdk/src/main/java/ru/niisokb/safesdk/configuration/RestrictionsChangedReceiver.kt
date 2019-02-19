package ru.niisokb.safesdk.configuration

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import ru.niisokb.safesdk.SpConfigDispatcher

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object RestrictionsChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            SpConfigDispatcher.onUpdateConfig(context)
        }
    }
}

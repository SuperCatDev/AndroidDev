package ru.niisokb.safesdk

import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import arrow.core.Try
import arrow.core.orNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.niisokb.safesdk.configuration.AppInfo
import ru.niisokb.safesdk.configuration.ConfigTarget
import ru.niisokb.safesdk.configuration.RestrictionsChangedReceiver
import ru.niisokb.safesdk.configuration.ValType
import ru.niisokb.safesdk.mixins.ScopedComponent

object SpConfigDispatcher : ScopedComponent {
    override var job = Job()

    private const val TAG = "SpConfigDispatcher"

    /** Потребители конфигурации */
    private var targets = mutableListOf<ConfigTarget>()

    /** Кэш последней конфигурации. */
    private var configCache = Bundle()

    /** Флаг успешной инициализации [SpConfigDispatcher]. */
    private var initialized = false

    fun getTargets(): List<ConfigTarget> = targets

    fun isInitialized() = initialized

    /** Инициализация. */
    fun load(context: Context) {
        if (initialized) return
        super.onStart()

        AppInfo.packageName = context.packageName
        AppInfo.internalStoragePath = context.filesDir.absolutePath

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Подписываемся на изменения Restrictions
            context.registerReceiver(RestrictionsChangedReceiver,
                    IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED))

            // Кэшируем текущие настройки
            val rm = context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
            configCache = Try { rm.applicationRestrictions }.orNull() ?: configCache
        }

        initialized = true
    }

    /** Деинициализация. */
    fun unload(context: Context) {
        if (!initialized) return
        super.onStop()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Отписываемся от изменений Restrictions
            context.unregisterReceiver(RestrictionsChangedReceiver)
        }

        initialized = false
    }

    /** Регистрация модуля. При регистрации модуль получает последнюю версию конфигурации. */
    fun register(target: ConfigTarget) {
        targets.add(target)
        launch { updateTargets(configCache, listOf(target)) }
    }

    /** Раздача конфигурации модулям. */
    @Suppress("MemberVisibilityCanBePrivate") // метод должен быть доступен из клиента
    fun updateTargets(bundle: Bundle, configTargets: List<ConfigTarget> = targets) {
        configTargets.forEach { it.dispatch(bundle) }
    }

    /** Обрабатывает изменение Restrictions. */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun onUpdateConfig(context: Context) {
        launch { updateTargets(getBundleFromRestrictions(context)) }
    }

    /**
     * Получает бандл из Restrictions и заполняет недостающие ключи дефолтными значениями
     * из ManifestRestrictions.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getBundleFromRestrictions(context: Context): Bundle {
        val resultBundle = configCache

        if (!initialized) {
            Log.e(TAG, "[getBundleFromRestrictions] Config manager is not initialized. " +
                    "Use SpConfigDispatcher.load().")
            return resultBundle
        }

        val rm = context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
        val restrictions = Try { rm.applicationRestrictions }.orNull() ?: return resultBundle

        val attrs = targets.flatMap { it.attrs }
        for (attr in attrs) {
            // Пытаемся получить значение из restrictions
            var value: Any? = when (attr.type) {
                ValType.Float -> restrictions.get(attr.key) as? Float
                ValType.Int -> restrictions.get(attr.key) as? Int
                ValType.String -> restrictions.get(attr.key) as? String
                ValType.Byte -> restrictions.get(attr.key) as? Byte
                ValType.ByteArray -> restrictions.get(attr.key) as? ByteArray
                ValType.Boolean -> restrictions.get(attr.key) as? Boolean
            }

            // Если значение не найдено в restrictions, то пробуем дефолт из manifest restrictions
            if (value == null) {
                val manifestRestrictions = Try { rm.getManifestRestrictions(AppInfo.packageName) }.orNull()
                val restrictionEntry: RestrictionEntry? = manifestRestrictions?.find { t -> t.key == attr.key }

                value = when (attr.type) {
                    ValType.Boolean -> restrictionEntry?.selectedState
                    ValType.String -> restrictionEntry?.selectedString
                    ValType.Int -> restrictionEntry?.intValue
                    else -> null
                }
            }

            // Если значение найдено -- кладем его в бандл
            if (value != null) {
                when (attr.type) {
                    ValType.Float -> resultBundle.putFloat(attr.key, value as Float)
                    ValType.Int -> resultBundle.putInt(attr.key, value as Int)
                    ValType.String -> resultBundle.putString(attr.key, value as String)
                    ValType.Byte -> resultBundle.putByte(attr.key, value as Byte)
                    ValType.ByteArray -> resultBundle.putByteArray(attr.key, value as ByteArray)
                    ValType.Boolean -> resultBundle.putBoolean(attr.key, value as Boolean)
                }
            } else {
                Log.w(TAG, "[getBundleFromRestrictions] Value for ${attr.key} not found")
            }
        }

        configCache = resultBundle
        return resultBundle
    }
}

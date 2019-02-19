package ru.niisokb.safesdk

import android.os.Bundle
import android.util.Log
import ru.niisokb.safesdk.configuration.ConfigAttr
import ru.niisokb.safesdk.mixins.ConfigurableModule
import ru.niisokb.safesdk.configuration.ValType
import ru.niisokb.safesdk.modules.log.Gate
import ru.niisokb.safesdk.modules.log.network.SpLogHttpsService

@Suppress("MemberVisibilityCanBePrivate", "unused")
object SpLog : ConfigurableModule {

    const val LOGS_MAX_SIZE_KEY = "safephone.logs.max_size"
    const val LOGS_MAX_SIZE_DEFAULT = 1000
    const val LOGS_CHUNK_SIZE_KEY = "safephone.logs.chunk_size"
    const val LOGS_CHUNK_SIZE_DEFAULT = 100
    const val LOGS_SEND_INTERVAL_KEY = "safephone.logs.send_interval"
    const val LOGS_SEND_INTERVAL_DEFAULT = 60
    const val SYSTEM_LOG_ENABLED_KEY = "safephone.logs.system_log_enabled"
    const val SYSTEM_LOG_ENABLED_DEFAULT = true
    const val LOGS_SEND_ENABLED_KEY = "safephone.logs.send_enabled"
    const val LOGS_SEND_ENABLED_DEFAULT = true

    override val attrs = listOf(
            ConfigAttr(LOGS_MAX_SIZE_KEY, ValType.Int, LOGS_MAX_SIZE_DEFAULT),
            ConfigAttr(LOGS_CHUNK_SIZE_KEY, ValType.Int, LOGS_CHUNK_SIZE_DEFAULT),
            ConfigAttr(LOGS_SEND_INTERVAL_KEY, ValType.Int, LOGS_SEND_INTERVAL_DEFAULT),
            ConfigAttr(SYSTEM_LOG_ENABLED_KEY, ValType.Boolean, SYSTEM_LOG_ENABLED_DEFAULT),
            ConfigAttr(LOGS_SEND_ENABLED_KEY, ValType.Boolean, LOGS_SEND_ENABLED_DEFAULT)
    )

    override val dispatch = { config: Bundle ->
        logsMaxSize = config.getInt(LOGS_MAX_SIZE_KEY, LOGS_MAX_SIZE_DEFAULT)
        logsChunkSize = config.getInt(LOGS_CHUNK_SIZE_KEY, LOGS_CHUNK_SIZE_DEFAULT)
        logsSendInterval = config.getInt(LOGS_SEND_INTERVAL_KEY, LOGS_SEND_INTERVAL_DEFAULT).toLong() * 1000L
        systemLogEnabled = config.getBoolean(SYSTEM_LOG_ENABLED_KEY, SYSTEM_LOG_ENABLED_DEFAULT)
        serviceLogEnabled = config.getBoolean(LOGS_SEND_ENABLED_KEY, LOGS_SEND_ENABLED_DEFAULT)

        Gate.updateConfig()
    }

    init {
        register()
    }

    internal var logsMaxSize = LOGS_MAX_SIZE_DEFAULT
    internal var logsChunkSize = LOGS_CHUNK_SIZE_DEFAULT
    internal var logsSendInterval = LOGS_SEND_INTERVAL_DEFAULT * 1000L
    internal var systemLogEnabled = SYSTEM_LOG_ENABLED_DEFAULT
    internal var serviceLogEnabled = LOGS_SEND_ENABLED_DEFAULT
    internal var logsEntryTrimThreshold = 10 * 1024

    fun v(tag: String, msg: String): Int {
        if (systemLogEnabled) {
            Log.v(tag, msg)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("D", mergeTag(tag, msg)))
        }

        return 0
    }

    fun d(tag: String, msg: String): Int {
        if (systemLogEnabled) {
            Log.d(tag, msg)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("D", mergeTag(tag, msg)))
        }

        return 0
    }

    fun i(tag: String, msg: String): Int {
        if (systemLogEnabled) {
            Log.i(tag, msg)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("I", mergeTag(tag, msg)))
        }

        return 0
    }

    fun w(tag: String, msg: String): Int {
        if (systemLogEnabled) {
            Log.w(tag, msg)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("W", mergeTag(tag, msg)))
        }

        return 0
    }

    fun e(tag: String, msg: String): Int {
        if (systemLogEnabled) {
            Log.e(tag, msg)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("E", mergeTag(tag, msg)))
        }

        return 0
    }

    fun wtf(tag: String, msg: String): Int {
        if (systemLogEnabled) {
            Log.wtf(tag, msg)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("F", mergeTag(tag, msg)))
        }

        return 0
    }

    fun v(tag: String, msg: String, tr: Throwable): Int {
        if (systemLogEnabled) {
            Log.v(tag, msg, tr)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("D", mergeThrowable(mergeTag(tag, msg), tr)))
        }

        return 0
    }

    fun d(tag: String, msg: String, tr: Throwable): Int {
        if (systemLogEnabled) {
            Log.d(tag, msg, tr)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("D", mergeThrowable(mergeTag(tag, msg), tr)))
        }

        return 0
    }

    fun i(tag: String, msg: String, tr: Throwable): Int {
        if (systemLogEnabled) {
            Log.i(tag, msg, tr)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("I", mergeThrowable(mergeTag(tag, msg), tr)))
        }

        return 0
    }

    fun w(tag: String, msg: String, tr: Throwable): Int {
        if (systemLogEnabled) {
            Log.w(tag, msg, tr)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("W", mergeThrowable(mergeTag(tag, msg), tr)))
        }

        return 0
    }

    fun e(tag: String, msg: String, tr: Throwable): Int {
        if (systemLogEnabled) {
            Log.e(tag, msg, tr)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("E", mergeThrowable(mergeTag(tag, msg), tr)))
        }

        return 0
    }

    fun wtf(tag: String, msg: String, tr: Throwable): Int {
        if (systemLogEnabled) {
            Log.wtf(tag, msg, tr)
        }

        if (serviceLogEnabled) {
            Gate.sendLog(formatLog("F", mergeThrowable(mergeTag(tag, msg), tr)))
        }

        return 0
    }

    /** Подписывает на код ответа HTTP сервиса. */
    fun subscribeToServiceResponse(responseCallback: ResponseCallback) {
        SpLogHttpsService.responseCallback = responseCallback
    }

    private fun getTimeStamp(): String {
        val timestamp = System.currentTimeMillis()
        return timestamp.toString()
    }

    private fun mergeThrowable(msg: String, th: Throwable): String {
        return "$msg\n$th\n${th.stackTrace.fold("")
        { acc, element -> "$acc    at $element\n" }}"
    }

    private fun mergeTag(tag: String, msg: String): String {
        return "$tag $msg"
    }

    private fun formatLog(logLevel: String, msg: String): String {
        var message = msg

        // Обрезаем слишком длинные логи
        if (message.length > logsEntryTrimThreshold) {
            message = msg.substring(logsEntryTrimThreshold)
        }

        return "${getTimeStamp()} $logLevel $message"
    }
}
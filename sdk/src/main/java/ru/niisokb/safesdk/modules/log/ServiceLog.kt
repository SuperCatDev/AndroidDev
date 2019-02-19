package ru.niisokb.safesdk.modules.log

import android.util.Log
import ru.niisokb.safesdk.SpLog
import ru.niisokb.safesdk.configuration.AppInfo
import ru.niisokb.safesdk.modules.log.network.SpLogHttpsService

internal object ServiceLog {
    private const val TAG = "ServiceLog"
    private const val fileName = "/LogsRingBuffer"
    private var fileBufferPath = AppInfo.internalStoragePath

    private var fileBuffer: FileBuffer? = null
    private lateinit var ramBuffer: RamBuffer

    init {
        initialize()
        AppInfo.subscribe {
            if (!it.internalStoragePath.isEmpty()) {
                fileBufferPath = it.internalStoragePath
                Gate.updateConfig()
            }
        }
    }

    fun refresh() {
        ramBuffer.onDestroy()
        fileBuffer?.onDestroy()
        initialize()
        Gate.updateFinished()
    }

    fun flushLogs(logs: List<String>) {
        synchronized(ramBuffer) {
            try {
                ramBuffer.addAll(logs)
            } catch (e: InterruptedException) {
                Log.e(TAG, "[flushLogs] Failed with $e")
            }
        }
    }

    fun sendLog(log: String) {
        synchronized(ramBuffer) {
            try {
                ramBuffer.add(log)
            } catch (e: InterruptedException) {
                Log.e(TAG, "[sendLog] Failed with $e")
            }
        }
    }

    private fun initialize() {
        if (!fileBufferPath.isEmpty()) {
            fileBuffer = FileBuffer(
                    fileBufferPath + fileName,
                    SpLog.logsMaxSize,
                    SpLog.logsSendInterval,
                    SpLog.logsChunkSize,
                    SpLogHttpsService::sendLog)
        }

        ramBuffer = RamBuffer(SpLog.logsMaxSize) {
            fileBuffer?.add(it) ?: SpLogHttpsService::sendLog
        }
    }
}

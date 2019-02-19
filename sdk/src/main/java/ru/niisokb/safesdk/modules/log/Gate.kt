package ru.niisokb.safesdk.modules.log

import ru.niisokb.safesdk.modules.log.datastructures.EvictingQueue

internal object Gate {
    private const val BUFFER_SIZE = 1000
    private val buffer = EvictingQueue<String>(BUFFER_SIZE)
    private var isTransparent = true

    fun sendLog(log: String) {
        synchronized(this) {
            if (isTransparent) {
                ServiceLog.sendLog(log)
            } else {
                buffer.add(log)
            }
        }
    }

    internal fun updateConfig() {
        synchronized(this) {
            isTransparent = false
        }
        ServiceLog.refresh()
    }

    internal fun updateFinished() {
        synchronized(this) {
            ServiceLog.flushLogs(buffer.toList())
            buffer.clear()
            isTransparent = true
        }
    }
}
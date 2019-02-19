package ru.niisokb.safesdk.modules.log

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.niisokb.safesdk.modules.log.datastructures.RingBuffer
import java.io.File
import kotlin.coroutines.CoroutineContext

internal class FileBuffer(fileName: String,
                          bufferCapacity: Int = 1000,
                          private val sendTimeout: Long = 1000L,
                          private val sendThreshold: Int = 100,
                          private val sendCallback: suspend (List<String>) -> Unit) : CoroutineScope {

    override val coroutineContext: CoroutineContext get() = job
    private val job = Job()
    private val buffer = RingBuffer(File(fileName), bufferCapacity)
    private var sendTrigger = CompletableDeferred<Boolean>()
    private val bufferLock = Mutex() // synchronization mechanism for coroutines

    init {
        loop()
    }

    fun onDestroy() {
        job.cancel()
    }

    suspend fun add(msg: String) {
        bufferLock.withLock {
            buffer.add(msg)
        }

        enoughToSend(buffer.size)
    }

    private fun loop() = launch {
        while (true) {
            if (buffer.size < sendThreshold) {
                listen()
            }
            if (buffer.size > 0) {
                send()
            }
        }
    }

    private suspend fun listen() = coroutineScope {
        sendTrigger = CompletableDeferred()
        val timerJob = launch { runTimer() }
        sendTrigger.await()
        if (timerJob.isActive) {
            timerJob.cancel()
        }
    }

    private suspend fun send() {
        val messages = ArrayList<String>(sendThreshold)
        bufferLock.withLock {
            messages.addAll(buffer.removeMany(sendThreshold))
        }
        sendCallback(messages)
    }

    private suspend fun runTimer() {
        delay(sendTimeout)
        if (sendTrigger.isActive) {
            sendTrigger.complete(true)
        }
    }

    private fun enoughToSend(size: Int) {
        if (sendTrigger.isActive && size >= sendThreshold) {
            sendTrigger.complete(true)
        }
    }
}

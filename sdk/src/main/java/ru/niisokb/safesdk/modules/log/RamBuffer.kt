package ru.niisokb.safesdk.modules.log

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.niisokb.safesdk.modules.log.datastructures.EvictingQueue
import kotlin.coroutines.CoroutineContext

internal class RamBuffer(bufferCapacity: Int = 1000,
                         private val sendCallback: suspend (String) -> Unit) : CoroutineScope {

    override val coroutineContext: CoroutineContext get() = job
    private val job = Job()
    private val buffer = EvictingQueue<String>(bufferCapacity)
    private var sendTrigger = CompletableDeferred<Boolean>()
    private val bufferLock = Mutex()

    init {
        loop()
    }

    fun onDestroy() {
        job.cancel()
    }

    fun addAll(logs: Collection<String>) = runBlocking {
        bufferLock.withLock {
            buffer.addAll(logs)
            sendTrigger.complete(true)
        }
    }

    fun add(msg: String) = runBlocking {
        bufferLock.withLock {
            buffer.add(msg)
        }

        sendTrigger.complete(true)
    }

    private fun loop() = launch {
        while (true) {
            if (buffer.size == 0) {
                listen()
            }
            if (buffer.size > 0) {
                send()
            }
        }
    }

    private suspend fun listen() = coroutineScope {
        sendTrigger = CompletableDeferred()
        sendTrigger.await()
    }

    private suspend fun send() {
        var msg = ""
        bufferLock.withLock {
            msg = buffer.remove()
        }

        sendCallback(msg)
    }
}

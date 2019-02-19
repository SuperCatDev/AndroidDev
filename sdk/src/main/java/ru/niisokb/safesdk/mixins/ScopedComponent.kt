package ru.niisokb.safesdk.mixins

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/** A component which can host coroutine hierarchy. */
interface  ScopedComponent: CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = job

    var job: Job

    /** Initializes Scope lifecycle. */
    fun onStart() {
        job = Job()
    }

    /** Finalizes Scope lifecycle. */
    fun onStop() {
        job.cancel()
    }
}
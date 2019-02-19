package ru.niisokb.safesdk.mixins

interface ObservableProperty<T> {
    val callbacks: MutableSet<(T) -> Unit>

    fun notifyAll(value: T) = callbacks.forEach { it(value) }

    fun subscribe(callback: (T) -> Unit): Subscription<T> {
        callbacks.add(callback)
        return Subscription(this, callback)
    }

    fun unsubscribe(callback: (T) -> Unit) = callbacks.remove(callback)
}

class Subscription<T>(private val receiver: ObservableProperty<T>, private val callback: (T) -> Unit) {
    fun close() = receiver.unsubscribe(callback)
}

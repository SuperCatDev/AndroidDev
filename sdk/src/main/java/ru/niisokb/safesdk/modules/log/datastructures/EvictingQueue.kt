package ru.niisokb.safesdk.modules.log.datastructures

import java.lang.IllegalArgumentException
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
internal class EvictingQueue<E>(val capacity: Int) : Queue<E> {
    private val delegate: Queue<E>

    init {
        if (capacity < 0) {
            throw IllegalArgumentException("Capacity must be greater than or equal to zero.")
        }
        delegate = ArrayDeque<E>(capacity)
    }

    override val size get() = delegate.size

    val remainingCapacity get() = capacity - size

    override fun add(element: E): Boolean {
        if (capacity == 0) {
            return true
        }
        if (size == capacity) {
            delegate.remove()
        }
        delegate.add(element)
        return true
    }

    override fun addAll(elements: Collection<E>): Boolean {
        if (elements.size >= capacity) {
            clear()
            return delegate.addAll(elements.drop(elements.size - capacity))
        }
        if (elements.size + size > capacity) {
            delegate.drop(elements.size + size - capacity)
        }
        return delegate.addAll(elements)
    }

    override fun contains(element: E): Boolean = delegate.contains(element)

    override fun clear() = delegate.clear()

    override fun element(): E = delegate.element()

    override fun isEmpty(): Boolean = delegate.isEmpty()

    override fun remove(): E = delegate.remove()

    override fun containsAll(elements: Collection<E>): Boolean = delegate.containsAll(elements)

    override fun iterator(): MutableIterator<E> = delegate.iterator()

    override fun remove(element: E): Boolean = delegate.remove(element)

    override fun removeAll(elements: Collection<E>): Boolean = delegate.removeAll(elements)

    override fun offer(element: E): Boolean = add(element)

    override fun retainAll(elements: Collection<E>): Boolean = delegate.retainAll(elements)

    override fun peek(): E = delegate.peek()

    override fun poll(): E = delegate.poll()
}

package com.jvziyaoyao.scale.image.sampling

import com.jvziyaoyao.scale.zoomable.util.getMilliseconds
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BlockingDeque<T> {
    private val deque = ArrayDeque<T>()
    private val mutex = Mutex()
    private val flow = MutableSharedFlow<Long>()

    suspend fun putFirst(element: T) = mutex.withLock {
        deque.addFirst(element)
        flow.emit(getMilliseconds())
    }

    suspend fun removeFirst(): T = mutex.withLock {
        return@withLock deque.removeFirst()
    }

    suspend fun take(): T {
        if (deque.isEmpty()) {
            flow.first()
        }
        return removeFirst()
    }

    suspend fun clear() = mutex.withLock { deque.clear() }

    suspend fun size(): Int = mutex.withLock { deque.size }

    suspend fun isEmpty(): Boolean = mutex.withLock { deque.isEmpty() }

    suspend fun peekFirst(): T? = mutex.withLock { deque.firstOrNull() }

    suspend fun peekLast(): T? = mutex.withLock { deque.lastOrNull() }

    fun contains(element: T): Boolean = deque.contains(element)

    suspend fun remove(element: T) = mutex.withLock { deque.remove(element) }

}
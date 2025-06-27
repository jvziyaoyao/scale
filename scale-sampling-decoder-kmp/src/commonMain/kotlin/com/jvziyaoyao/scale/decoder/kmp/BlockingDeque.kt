package com.jvziyaoyao.scale.decoder.kmp

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

//    // 堵塞获取，队列空时挂起，直到有元素
//    suspend fun take(): T = mutex.withLock {
//        if (deque.isNotEmpty()) {
//            deque.removeFirst()
//        } else {
//            val deferred = CompletableDeferred<T>()
//            waitingReceivers.add(deferred)
//            // 先释放锁再挂起等待
//            // 这里要先退出withLock块，所以用下面写法
//            return@withLock null
//        }
//    } ?: run {
//        // 如果上面返回了null，说明刚刚添加了deferred，挂起等待
//        waitingReceivers.last().await()
//    }

    suspend fun clear() = mutex.withLock {
        deque.clear()
    }

    suspend fun size(): Int = mutex.withLock { deque.size }

    suspend fun isEmpty(): Boolean = mutex.withLock { deque.isEmpty() }

    suspend fun peekFirst(): T? = mutex.withLock { deque.firstOrNull() }

    suspend fun peekLast(): T? = mutex.withLock { deque.lastOrNull() }

    fun contains(element: T): Boolean = deque.contains(element)

    fun remove(element: T) = deque.remove(element)

}
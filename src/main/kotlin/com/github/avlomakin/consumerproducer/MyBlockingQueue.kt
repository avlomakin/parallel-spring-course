package com.github.avlomakin.consumerproducer

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MyBlockingQueue<T>(
    private val capacity: Int
) : MyQueue<T> {

    private val buffer = ArrayList<T>()
    private val lock = ReentrantLock()
    private val bufferNotEmptyCondition = lock.newCondition()
    private val bufferNotFullCondition = lock.newCondition()

    override fun put(element: T) {
        lock.withLock {
            while (buffer.size == capacity) {
                bufferNotFullCondition.await()
            }
            buffer.add(element)
            bufferNotEmptyCondition.signalAll()
        }
    }

    override fun get(): T {
        try {
            lock.lock()
            while (buffer.size == 0) {
                bufferNotEmptyCondition.await()
            }
            val element = buffer[0]
            buffer.removeAt(0)
            return element
        } finally {
            bufferNotEmptyCondition.signalAll()
            lock.unlock()
        }
    }

    fun size() : Int{
        return buffer.size
    }
}
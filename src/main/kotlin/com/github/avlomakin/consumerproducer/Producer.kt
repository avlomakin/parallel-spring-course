package com.github.avlomakin.consumerproducer

class Producer<T>(
    id: Int,
    queue: MyQueue<T>,
    intervalMs: Long,
    private val elementFactory: () -> T
) : MyQueueWorker<T>(queue, intervalMs, "producer-$id") {

    override fun action() {
        val element = elementFactory()
        queue.put(element)
    }
}
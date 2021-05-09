package com.github.avlomakin.consumerproducer

class Consumer<T>(
    id: Int,
    queue: MyQueue<T>,
    intervalMs: Long,
    private val consumingFun: (T) -> Unit
) : MyQueueWorker<T>(queue, intervalMs, "consumer-$id") {
    override fun action() {
        consumingFun(queue.get())
    }
}
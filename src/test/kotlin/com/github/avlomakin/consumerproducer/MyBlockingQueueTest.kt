package com.github.avlomakin.consumerproducer

import org.junit.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class MyBlockingQueueTest {

    private val queue = MyBlockingQueue<String>(1000)

    @Test(timeout = 100000)
    fun `consumers stop even if there are no producers`() {
        val consumers = (1..5).map { id ->
            Consumer(id, queue, 10) {
                println("${Thread.currentThread().name}: '$it' received")
            }
        }

        consumers.forEach { it.start() }

        Thread.sleep(100)

        consumers.forEach { it.stop() }
    }

    @Test(timeout = 100000)
    fun `producers stop even if there are no consumers`() {
        val producers = (1..5).map { id ->
            Producer(id, queue, 10) {
                val msg = "message-$id"
                println("${Thread.currentThread().name}: '$msg' sent")
                msg
            }
        }
        producers.forEach { it.start() }

        Thread.sleep(100)

        producers.forEach { it.stop() }
    }

    @Test(timeout = 100000)
    fun `elements produced by producers are consumed by consumers only once`() {

        val int = AtomicInteger()
        val producers = (1..5).map { id ->
            Producer(id, queue, 30) {
                "message-${int.incrementAndGet()}"
            }
        }

        val consumedElements = ConcurrentLinkedQueue<String>()
        val consumers = (1..5).map { id ->
            Consumer(id, queue, 10) {
                consumedElements.add(it)
            }
        }

        producers.forEach { it.start() }
        consumers.forEach { it.start() }

        Thread.sleep(1000)

        producers.forEach { it.stop() }
        consumers.forEach { it.stop() }

        assert(!consumedElements.groupBy { it }.any { (_, value) -> value.size > 1 })
    }

    @Test(timeout = 100000)
    fun `after stop, produced elements count = consumed elements count + buffer size`(){
        val queue = MyBlockingQueue<Int>(10000)
        val lastProducedElement = AtomicInteger()
        val producers = (1..5).map { id ->
            Producer(id, queue, 40) {
                lastProducedElement.incrementAndGet()
            }
        }

        val consumedElements = ConcurrentLinkedQueue<Int>()
        val consumers = (1..5).map { id ->
            Consumer(id, queue, 60) {
                consumedElements.add(it)
            }
        }

        producers.forEach { it.start() }
        consumers.forEach { it.start() }

        Thread.sleep(1000)

        producers.forEach { it.stop() }
        consumers.forEach { it.stop() }

        assert(lastProducedElement.get() == (consumedElements.size + queue.size()))
    }
}

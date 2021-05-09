package com.github.avlomakin.consumerproducer

import kotlin.Int.Companion.MAX_VALUE

object Program {

    @JvmStatic
    fun main(args: Array<String>) {
        val intervalMs = 1000L
        val queue = MyBlockingQueue<String>(MAX_VALUE) //no upper bound

        val producers = (1..5).map { id ->
            Producer(id, queue, intervalMs) {
                val msg = "message-$id"
                println("${Thread.currentThread().name}: '$msg' sent")
                msg
            }
        }
        val consumers = (1..5).map { id ->
            Consumer(id, queue, intervalMs) {
                println("${Thread.currentThread().name}: '$it' received")
            }
        }

        producers.forEach { it.start() }
        consumers.forEach { it.start() }

        System.`in`.read()

        producers.forEach { it.stop() }
        consumers.forEach { it.stop() }
    }
}
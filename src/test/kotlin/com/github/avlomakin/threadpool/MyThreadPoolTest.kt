package com.github.avlomakin.threadpool

import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertFailsWith

class MyThreadPoolTest {

    @Test
    fun `task result - must return correct result`() {
        val threadPool = MyThreadPool(1)
        val task = threadPool.submit {
            println("started")
            Thread.sleep(1000)
            1
        }
        assert(task.result == 1)
    }

    @Test
    fun `continueWith - must execute only after initial task is completed`() {
        val threadPool = MyThreadPool(1)
        val first = threadPool.submit {
            Thread.sleep(100)
            1
        }

        val second = first.continueWith { i ->
            assert(first.isCompleted)
            Thread.sleep(100)
            i + 1
        }

        val third = second.continueWith { i ->
            assert(second.isCompleted)
            Thread.sleep(100)
            i + 1
        }

        assert(third.result == 3)
    }

    @Test
    fun `submit - must complete all tasks even if the number is greater than thread count`() {
        val threadPool = MyThreadPool(3)
        val taskList = (0..10).map { i ->
            threadPool.submit {
                Thread.sleep(10)
                i
            }
        }
        val sum = taskList.sumBy { it.result }
        assert(sum == 55)
    }

    @Test
    fun `pool must contain exactly n threads`() {
        val count = 7
        val threadPool = MyThreadPool(count)
        val threadNameMap = ConcurrentHashMap<String, Int>()
        val taskList = (0..100).map { i ->
            threadPool.submit {
                threadNameMap[Thread.currentThread().name] = 1
                Thread.sleep(10)
                i
            }
        }

        taskList.sumBy { it.result }

        assert(threadNameMap.size == count)
    }

    @Test
    fun `task should fail with AggregateException`() {
        val threadPool = MyThreadPool(1)
        val task = threadPool.submit<Int> {
            throw Exception("oops")
        }

        assertFailsWith<AggregateException> { task.result }
    }

    @Test
    fun `should shutdown correctly`() {
        val threadPool = MyThreadPool(2)
        val first = threadPool.submit {
            Thread.sleep(1000)
            1
        }
        threadPool.submit {
            Thread.sleep(1000)
            2
        }
        threadPool.submit {
            Thread.sleep(1000)
            3
        }

        Thread.sleep(10)

        threadPool.shutdown()
        assert(first.result == 1)
        var areWorkersAlive = true
        repeat(5) {
            areWorkersAlive = threadPool.areWorkersAlive()
            if(!areWorkersAlive){
                return@repeat
            }
            Thread.sleep(100)
        }

        assert(!areWorkersAlive)
    }
}
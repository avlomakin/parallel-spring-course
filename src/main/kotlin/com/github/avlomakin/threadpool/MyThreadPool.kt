package com.github.avlomakin.threadpool

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit


class MyThreadPool(threadCount: Int)  {
    private val taskQueue = ConcurrentLinkedQueue<MyTask<*>>()
    private val lock = ReentrantLock()
    private val taskQueueNotEmpty = lock.newCondition()
    private val workers = (0 until threadCount).map {
        val worker = initWorkerThread(it)
        worker.start()
        worker
    }

    @Volatile
    var isStopped = false
        private set

    private fun initWorkerThread(id: Int) : Thread {
        val worker = Thread {
            while (true) {
                val task = lock.withLock {
                    while (taskQueue.isEmpty() && !isStopped) {
                        taskQueueNotEmpty.await(300, TimeUnit.MILLISECONDS)
                    }

                    if (isStopped) {
                        return@Thread
                    }
                    taskQueue.poll()
                }
                task()
            }
        }
        worker.name = "my-thread-pool-worker-$id"
        return worker
    }


    /**
     * More java-like approach (Instead of MyThreadPool.Enqueue(IMyTask)) - code block is submitted for execution
     * See [AbstractExecutorService.submit]
     * @return [IMyTask] submitted for execution
     */
    fun <T> submit(task: () -> T): IMyTask<T> {
        if (isStopped) throw IllegalStateException("ThreadPool was stopped")
        return lock.withLock {
            val myTask = MyTask(task, this)
            taskQueue.add(myTask)
            taskQueueNotEmpty.signalAll()
            myTask
        }
    }

    fun shutdown() {
        isStopped = true
        lock.withLock { taskQueueNotEmpty.signalAll() }
    }

    fun areWorkersAlive(): Boolean {
        return workers.any { it.isAlive }
    }
}
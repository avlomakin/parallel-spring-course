package com.github.avlomakin.threadpool

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MyTask<T> (private val task: () -> T,
              private val executor: MyThreadPool
) : IMyTask<T> {

    private val lock = ReentrantLock()
    private val resultReady = lock.newCondition()

    private var _exception: Exception? = null

    private var _result: T? = null

    override var isCompleted: Boolean = false
        private set

    override val result: T
        get() {
            while (!isCompleted  && executor.areWorkersAlive()) lock.withLock {
                resultReady.await(500, TimeUnit.MILLISECONDS)
            }

            if(!isCompleted && executor.isStopped){
                throw AggregateException("Thread pool has been stopped before the task execution is completed")
            }

            val exception = _exception
            return if(exception != null) {
                throw AggregateException(exception)
            } else {
                _result!!
            }
        }

    operator fun invoke() {
        try {
            _result = task.invoke()
            lock.withLock {
                resultReady.signalAll()
            }
        } catch (e: Exception) {
            _exception = e
        } finally {
            isCompleted = true
        }
    }

    override fun <E> continueWith(func: (T) -> E): IMyTask<E> {
        return executor.submit {
            func(result)
        }
    }
}
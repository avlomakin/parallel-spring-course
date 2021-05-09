package com.github.avlomakin.threadpool


interface IMyTask<T> {
    val isCompleted: Boolean

    /**
     * Blocks thread until the result is ready
     * @return Task result
     */
    val result: T

    /**
     * Return a new task that'll use current task's result as an input & will be enqueued to the
     * [MyThreadPool] executing current task
     */
    fun <E> continueWith(func: (T) -> E): IMyTask<E>
}
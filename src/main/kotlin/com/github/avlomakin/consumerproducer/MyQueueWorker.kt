package com.github.avlomakin.consumerproducer

/**
 * Manages lifecycle of a queue-related tasks
 */
abstract class MyQueueWorker<T>(
    protected val queue: MyQueue<T>,
    private val intervalMs: Long,
    private val workingThreadName: String? = null,
) {

    @Volatile
    private var stopped = false
    private var workingThread: Thread? = null

    /**
     * It's illegal to call this method from multiple threads
     */
    fun start() {
        if(workingThread != null){
            throw IllegalStateException("worker's already been started")
        }
        val newThread = Thread {
            try {
                while (!stopped) {
                    action()
                    Thread.sleep(intervalMs)
                }
            } catch (e: InterruptedException) {
                println("${Thread.currentThread().name}: interrupt signal received")
            } finally {
                stopped = false
            }
        }
        if(workingThreadName != null){
            newThread.name = workingThreadName
        }
        workingThread = newThread
        newThread.start()
    }

    protected abstract fun action()

    /**
     * It's illegal to call this method from multiple threads
     */
    fun stop() {
        val workingThread = this.workingThread ?: throw IllegalStateException("nothing to stop")

        println("${workingThread.name}: setting stop flag")
        stopped = true
        workingThread.join(1000)
        if (workingThread.isAlive) {
            println("failed to stop ${workingThread.name} gracefully, sending interrupt signal")
            workingThread.interrupt()
        }
        workingThread.join(10000)
        this.workingThread = null
    }
}
package ch.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.utils.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProcessManager {
    private class EndOfFrameInfo(val lambda: () -> Unit, var skip: Int)

    private val mutex = Mutex() // all public functions of ProcessManager need to be thread-safe
    private val endOfFrameLambdas = mutableListOf<EndOfFrameInfo>()

    private val cpuScope = CoroutineScope(Dispatchers.Default)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val mainThread: Thread = Thread.currentThread().also {
        require(it.name == MAIN_THREAD_NAME) {
            "ProcessManager created from another thread, or unexpected name: ${Thread.currentThread().name}"
        }
    }

    fun requireMainThread() {
        // using a reference to avoid string comparisions
        require(Thread.currentThread() == mainThread) {
            "Expected main thread, but got instead: ${Thread.currentThread().name}"
        }
    }

    /**
     * Runs the given suspended lambda in a coroutine. Use this for CPU-heavy tasks.
     * @return An instance of Job, which has a cancel() function
     */
    fun runAsync(block: suspend CoroutineScope.() -> Unit) =
        cpuScope.launch(block = block)

    /**
     * Runs the given suspended lambda in a coroutine. Use this for IO-heavy tasks.
     * @return An instance of Job, which has a cancel() function
     */
    fun runAsyncIO(block: suspend CoroutineScope.() -> Unit) =
        ioScope.launch(block = block)

    /**
     * Thread-safe function to add a lambda that will be called on the main thread at the end of this frame.
     */
    fun runAtEndOfFrame(skip: Int = 0, lambda: () -> Unit) {
        require(skip >= 0)

        runBlocking {
            mutex.withLock {
                endOfFrameLambdas.add(EndOfFrameInfo(lambda, skip))
            }
        }
    }

    fun runEndOfFrameLambdas() {
        var runList: MutableList<EndOfFrameInfo>? = null
        requireMainThread() // AppImpl should call this at the end of each frame, and nobody else

        runBlocking {
            mutex.withLock {
                if (endOfFrameLambdas.isNotEmpty()) {
                    runList = mutableListOf()

                    endOfFrameLambdas.removeAll {
                        if (it.skip >= 0) {
                            it.skip--
                            false // don't remove yet
                        } else {
                            // The lambdas are called outside the mutex lock, to prevent from deadlocks when the lambda
                            // tries to access it.
                            runList.add(it)
                            true // remove
                        }
                    }
                }
            }
        }

        if (runList != null && runList.isNotEmpty()) {
            runList.forEach {
                try {
                    it.lambda()
                } catch (e: Exception) {
                    Log.error(TAG, "An end-of-frame lambda crashed: ${e.message}\n${e.stackTraceToString()}")
                }
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("ProcessManager")
        const val MAIN_THREAD_NAME = "main"
    }
}

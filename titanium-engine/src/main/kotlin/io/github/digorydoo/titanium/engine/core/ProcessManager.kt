package io.github.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.logging.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class ProcessManager {
    private class EndOfFrameInfo(val lambda: () -> Unit, var skip: Int)

    private val mutex = Mutex() // all public functions of ProcessManager need to be thread-safe
    private val endOfFrameLambdas = mutableListOf<EndOfFrameInfo>()

    private val cpuScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Apparently, there is no Java function to check whether we're in the main thread directly.
    private val mainThreadId: Long = Thread.currentThread().let {
        // Note that the name is not guaranteed by the JVM to always be "main", but since we're using jpackage to
        // provide the JVM, this should be safe.
        require(it.name == "main") {
            "ProcessManager must be constructed from main thread! Current thread: ${it.name}"
        }
        it.id
    }

    fun isMainThread() = Thread.currentThread().id == mainThreadId

    fun requireMainThread() {
        require(isMainThread()) {
            "Code is required to run in main thread! Current thread is: ${Thread.currentThread().name}"
        }
    }

    abstract fun exit()

    /**
     * Launches the given suspending lambda in our cpuScope. Use this for CPU-heavy tasks.
     * @return An instance of Job
     */
    fun runAsync(block: suspend CoroutineScope.() -> Unit) =
        cpuScope.launch(block = block)

    /**
     * Launches the given suspending lambda in our ioScope. Use this for IO-heavy tasks.
     * @return An instance of Job
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

        if (!runList.isNullOrEmpty()) {
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
    }
}

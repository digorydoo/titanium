package io.github.digorydoo.titanium.engine.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

/**
 * Custom coroutine dispatcher that schedules coroutine execution on the main thread by posting continuations to the
 * end of the current frame.
 */
internal class MainThreadDispatcher: CoroutineDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext) =
        !App.process.isMainThread() // blocks will run immediately if already on main thread

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        App.process.runAtEndOfFrame {
            block.run()
        }
    }
}

package io.github.digorydoo.titanium.engine.intermission

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.core.MainThreadDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class IntermissionManager {
    class IntermissionAlreadyRunningException: Exception("An intermission is already running")

    private var intermission: Intermission? = null
    val anyRunning get() = intermission != null
    val canCancel get() = intermission?.hasCancellable == true

    private val scope = CoroutineScope(MainThreadDispatcher() + SupervisorJob())

    fun begin(lambda: suspend Intermission.() -> Unit) {
        if (anyRunning) throw IntermissionAlreadyRunningException()

        Log.info(TAG, "Intermission starting")
        val im = Intermission(scope)
        intermission = im

        im.begin(lambda)
            .invokeOnCompletion { exc ->
                intermission = null

                if (exc != null) {
                    Log.error(TAG, "Intermission crashed: $exc")
                } else {
                    Log.info(TAG, "Intermission ended")
                }
            }
    }

    fun handle() {
        // If there is a paused coroutine, waitingFor will be not-null.
        intermission?.waitingFor?.invoke()
    }

    companion object {
        private val TAG = Log.Tag("IntermisionManager")
    }
}

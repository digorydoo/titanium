package io.github.digorydoo.titanium.engine.intermission

import ch.digorydoo.kutils.logging.Log

class IntermissionManager {
    class IntermissionAlreadyRunningException: Exception("An intermission is already running")

    private var intermission: Intermission? = null
    val anyRunning get() = intermission != null
    val canCancel get() = intermission?.canCancel == true

    fun begin(lambda: suspend Intermission.() -> Unit) {
        if (anyRunning) throw IntermissionAlreadyRunningException()

        Log.info(TAG, "Intermission starting")
        val im = Intermission()
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

    fun cancel() {
        // This function does nothing if there is no mission, or if the intermission is not cancellable.
        intermission?.cancelIfCancellable()
    }

    companion object {
        private val TAG = Log.Tag("IntermisionManager")
    }
}

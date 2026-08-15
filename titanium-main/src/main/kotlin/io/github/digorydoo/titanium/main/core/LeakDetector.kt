package io.github.digorydoo.titanium.main.core

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.core.App
import java.lang.ref.Cleaner

/**
 * Replacement for finalize() based leak detection approach. JVMs are removing support for finalize().
 * NOTE: The Action class must not reference LeakDetector, otherwise the clean-up code never gets called!
 */
class LeakDetector(private val name: String, initiallyValid: Boolean) {
    private class Registration(val action: Action, val cleanable: Cleaner.Cleanable)

    private var registration: Registration? = null

    var resourceValid: Boolean
        get() = registration != null
        set(valid) {
            if (valid) {
                if (registration == null) {
                    val action = Action(name)
                    val cleanable = App.cleaner.register(this, action)
                    registration = Registration(action, cleanable)
                } else {
                    Log.warn(TAG, "Resource $name was set to valid again. A leak may have gone undetected!")
                    registration?.action?.shouldReport = true // should already be true
                }
            } else {
                registration?.apply {
                    action.shouldReport = false // prevent action from reporting on de-register
                    cleanable.clean() // de-register (calls action)
                }
                registration = null
            }
        }

    init {
        resourceValid = initiallyValid
    }

    companion object {
        private val TAG = Log.Tag("LeakDetector")

        private class Action(val name: String): Runnable {
            @Volatile // Action is run inside a thread
            var shouldReport = true

            override fun run() {
                if (shouldReport) {
                    Log.error(TAG, "$name was not properly freed!")
                }
            }
        }
    }
}

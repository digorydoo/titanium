package ch.digorydoo.titanium.engine.input

import ch.digorydoo.titanium.engine.core.App

class InputState {
    var pressed = false
    var handleTime: Float? = null

    val pressedOnce: Boolean
        get() = when {
            pressed && handleTime == null -> {
                handleTime = App.time.sessionTime
                true
            }
            else -> false
        }

    val pressedWithRepeat: Boolean
        get() = when {
            !pressed -> false
            handleTime == null -> {
                handleTime = App.time.sessionTime + REPEAT_DELAY1
                true
            }
            (handleTime ?: 0.0f) - App.time.sessionTime <= 0.0f -> {
                handleTime = App.time.sessionTime + REPEAT_DELAY2
                true
            }
            else -> false
        }

    companion object {
        private const val REPEAT_DELAY1 = 0.5f
        private const val REPEAT_DELAY2 = 0.05f
    }
}

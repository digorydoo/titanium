package io.github.digorydoo.titanium.engine.input

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.core.App

/**
 * This class represents the state of a Gamepad button or Keyboard key.
 */
class InputState {
    private var pressed = false
    private var handleTime: Float? = null

    val isDown: Boolean get() = pressed

    fun checkPressed(): Boolean =
        pressed && handleTime == null

    fun checkPressedOnce(): Boolean {
        if (!pressed || handleTime != null) return false
        handleTime = App.time.sessionTime
        return true
    }

    fun checkPressedWithRepeat(): Boolean = when {
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

    fun didHandle() {
        if (pressed) {
            handleTime = App.time.sessionTime
        } else {
            Log.warn(TAG, "didHandle was called even though button is not currently pressed")
        }
    }

    fun setPressed() {
        pressed = true
        handleTime = null
    }

    fun setReleased() {
        pressed = false
        handleTime = null
    }

    companion object {
        private val TAG = Log.Tag("InputState")
        private const val REPEAT_DELAY1 = 0.5f
        private const val REPEAT_DELAY2 = 0.05f
    }
}

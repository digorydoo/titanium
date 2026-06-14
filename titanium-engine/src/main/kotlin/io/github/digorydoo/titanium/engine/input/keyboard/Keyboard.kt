package io.github.digorydoo.titanium.engine.input.keyboard

import io.github.digorydoo.titanium.engine.input.InputState
import io.github.digorydoo.titanium.engine.input.InputStateMap

abstract class Keyboard {
    protected val keyState = InputStateMap<KeyboardKey>()
    val anyKeyPressed get() = keyState.anyPressed

    protected val charState = InputState()
    protected var charPressedLast = '\u0000'

    fun isDown(key: KeyboardKey) = keyState.isDown(key)
    fun checkPressed(key: KeyboardKey) = keyState.checkPressed(key)
    fun checkPressedOnce(key: KeyboardKey) = keyState.checkPressedOnce(key)
    fun checkPressedWithRepeat(key: KeyboardKey) = keyState.checkPressedWithRepeat(key)
    fun didHandle(key: KeyboardKey) = keyState.didHandle(key)

    fun checkPressed(c: Char) = charPressedLast == c && charState.checkPressed()
    fun checkPressedOnce(c: Char) = charPressedLast == c && charState.checkPressedOnce()
    fun checkPressedWithRepeat(c: Char) = charPressedLast == c && charState.checkPressedWithRepeat()
}

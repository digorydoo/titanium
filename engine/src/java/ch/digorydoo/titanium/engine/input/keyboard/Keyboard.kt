package ch.digorydoo.titanium.engine.input.keyboard

import ch.digorydoo.titanium.engine.input.InputState
import ch.digorydoo.titanium.engine.input.InputStateMap

abstract class Keyboard {
    protected val keyState = InputStateMap<KeyboardKey>()
    protected val charState = InputState()
    protected var charPressedLast = '\u0000'

    fun isPressed(key: KeyboardKey) =
        keyState.isPressed(key)

    fun isPressedOnce(key: KeyboardKey) =
        keyState.isPressedOnce(key)

    fun isPressedWithRepeat(key: KeyboardKey) =
        keyState.isPressedWithRepeat(key)

    fun isPressed(c: Char) =
        charPressedLast == c && charState.pressed

    fun isPressedOnce(c: Char) =
        charPressedLast == c && charState.pressedOnce

    fun isPressedWithRepeat(c: Char) =
        charPressedLast == c && charState.pressedWithRepeat
}

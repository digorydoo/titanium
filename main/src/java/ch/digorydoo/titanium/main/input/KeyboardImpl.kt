package ch.digorydoo.titanium.main.input

import ch.digorydoo.titanium.engine.input.Input.Key
import ch.digorydoo.titanium.engine.input.KeyState
import ch.digorydoo.titanium.engine.input.KeyStateMap
import ch.digorydoo.titanium.engine.input.Keyboard
import ch.digorydoo.titanium.main.input.InputImpl.KeyAction

class KeyboardImpl(private val keyStateMap: KeyStateMap): Keyboard {
    private val keymap = Keymap()
    private var charPressedLast = '\u0000'
    private val charKeyState = KeyState()

    override fun onCharPressedOnce(lambda: (c: Char) -> Unit) {
        if (charKeyState.pressedOnce) {
            lambda(charPressedLast)
        }
    }

    override fun onCharPressedWithRepeat(lambda: (c: Char) -> Unit) {
        if (charKeyState.pressedWithRepeat) {
            lambda(charPressedLast)
        }
    }

    override fun onNumberPressedOnce(lambda: (num: Int) -> Unit) {
        if (charKeyState.pressedOnce) {
            val num = charPressedLast.code - '0'.code
            if (num in 0 .. 9) {
                lambda(num)
            }
        }
    }

    override fun onNumberPressedWithRepeat(lambda: (num: Int) -> Unit) {
        if (charKeyState.pressedWithRepeat) {
            val num = charPressedLast.code - '0'.code
            if (num in 0 .. 9) {
                lambda(num)
            }
        }
    }

    fun onGLFWKeyEvent(glfwKey: Int, action: KeyAction) {
        val isDownEvent = when (action) {
            KeyAction.PRESS -> true
            KeyAction.RELEASE -> false
        }

        val isCtrlDown = keyStateMap.isPressed(Key.CTRL)

        if (!isDownEvent) {
            charKeyState.pressed = false
        }

        val match = keymap.getMatch(glfwKey, isCtrlDown)

        // The glfwKey may map to multiple Keys depending on the context.
        // Make sure all possible matches get a keyup before keyOfEvent gets a keydown.
        match.forEachPossibleKey { keyStateMap.setReleased(it) }

        match.keyOfEvent?.let { keyOfEvent ->
            if (isDownEvent) {
                keyStateMap.setPressed(keyOfEvent)
            }
        }
    }

    fun onGLFWCharEvent(charCode: Int) {
        charPressedLast = Char(charCode)
        charKeyState.pressed = true
        charKeyState.handleTime = null
    }
}

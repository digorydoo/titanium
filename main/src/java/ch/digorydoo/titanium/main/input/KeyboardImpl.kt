package ch.digorydoo.titanium.main.input

import ch.digorydoo.titanium.engine.input.keyboard.Keyboard
import ch.digorydoo.titanium.engine.input.keyboard.KeyboardKey
import org.lwjgl.glfw.GLFW

class KeyboardImpl: Keyboard() {
    fun onGLFWKeyEvent(glfwKey: Int, action: InputManagerImpl.KeyAction) {
        val isDownEvent = when (action) {
            InputManagerImpl.KeyAction.PRESS -> true
            InputManagerImpl.KeyAction.RELEASE -> false
        }

        if (!isDownEvent) {
            charState.pressed = false
        }

        val key = glfwToKeyboardKey(glfwKey) ?: return
        keyState.setReleased(key)

        if (isDownEvent) {
            keyState.setPressed(key)
        }
    }

    fun onGLFWCharEvent(charCode: Int) {
        charPressedLast = Char(charCode)
        charState.pressed = true
        charState.handleTime = null
    }

    companion object {
        private fun glfwToKeyboardKey(glfwKey: Int) = when (glfwKey) {
            GLFW.GLFW_KEY_LEFT_SHIFT -> KeyboardKey.LEFT_SHIFT
            GLFW.GLFW_KEY_RIGHT_SHIFT -> KeyboardKey.RIGHT_SHIFT
            GLFW.GLFW_KEY_LEFT_ALT -> KeyboardKey.LEFT_ALT
            GLFW.GLFW_KEY_RIGHT_ALT -> KeyboardKey.RIGHT_ALT
            GLFW.GLFW_KEY_LEFT_CONTROL -> KeyboardKey.LEFT_CTRL
            GLFW.GLFW_KEY_RIGHT_CONTROL -> KeyboardKey.RIGHT_CTRL
            GLFW.GLFW_KEY_LEFT_SUPER -> KeyboardKey.LEFT_META
            GLFW.GLFW_KEY_RIGHT_SUPER -> KeyboardKey.RIGHT_META

            GLFW.GLFW_KEY_LEFT -> KeyboardKey.ARROW_LEFT
            GLFW.GLFW_KEY_RIGHT -> KeyboardKey.ARROW_RIGHT
            GLFW.GLFW_KEY_UP -> KeyboardKey.ARROW_UP
            GLFW.GLFW_KEY_DOWN -> KeyboardKey.ARROW_DOWN

            GLFW.GLFW_KEY_TAB -> KeyboardKey.TAB
            GLFW.GLFW_KEY_HOME -> KeyboardKey.HOME
            GLFW.GLFW_KEY_END -> KeyboardKey.END
            GLFW.GLFW_KEY_PAGE_DOWN -> KeyboardKey.PAGE_DOWN
            GLFW.GLFW_KEY_PAGE_UP -> KeyboardKey.PAGE_UP

            GLFW.GLFW_KEY_ENTER -> KeyboardKey.ENTER
            GLFW.GLFW_KEY_ESCAPE -> KeyboardKey.ESCAPE

            GLFW.GLFW_KEY_BACKSPACE -> KeyboardKey.BACKSPACE
            GLFW.GLFW_KEY_DELETE -> KeyboardKey.FWDDEL

            GLFW.GLFW_KEY_KP_ADD -> KeyboardKey.KEYPAD_PLUS
            GLFW.GLFW_KEY_KP_SUBTRACT -> KeyboardKey.KEYPAD_MINUS

            GLFW.GLFW_KEY_LEFT_BRACKET -> KeyboardKey.LEFT_BRACKET
            GLFW.GLFW_KEY_APOSTROPHE -> KeyboardKey.APOSTROPHE

            GLFW.GLFW_KEY_A -> KeyboardKey.RAW_A
            GLFW.GLFW_KEY_B -> KeyboardKey.RAW_B
            GLFW.GLFW_KEY_C -> KeyboardKey.RAW_C
            GLFW.GLFW_KEY_D -> KeyboardKey.RAW_D
            GLFW.GLFW_KEY_E -> KeyboardKey.RAW_E
            GLFW.GLFW_KEY_F -> KeyboardKey.RAW_F
            GLFW.GLFW_KEY_G -> KeyboardKey.RAW_G
            GLFW.GLFW_KEY_H -> KeyboardKey.RAW_H
            GLFW.GLFW_KEY_I -> KeyboardKey.RAW_I
            GLFW.GLFW_KEY_J -> KeyboardKey.RAW_J
            GLFW.GLFW_KEY_K -> KeyboardKey.RAW_K
            GLFW.GLFW_KEY_L -> KeyboardKey.RAW_L
            GLFW.GLFW_KEY_M -> KeyboardKey.RAW_M
            GLFW.GLFW_KEY_N -> KeyboardKey.RAW_N
            GLFW.GLFW_KEY_O -> KeyboardKey.RAW_O
            GLFW.GLFW_KEY_P -> KeyboardKey.RAW_P
            GLFW.GLFW_KEY_Q -> KeyboardKey.RAW_Q
            GLFW.GLFW_KEY_R -> KeyboardKey.RAW_R
            GLFW.GLFW_KEY_S -> KeyboardKey.RAW_S
            GLFW.GLFW_KEY_T -> KeyboardKey.RAW_T
            GLFW.GLFW_KEY_U -> KeyboardKey.RAW_U
            GLFW.GLFW_KEY_V -> KeyboardKey.RAW_V
            GLFW.GLFW_KEY_W -> KeyboardKey.RAW_W
            GLFW.GLFW_KEY_X -> KeyboardKey.RAW_X
            GLFW.GLFW_KEY_Y -> KeyboardKey.RAW_Y
            GLFW.GLFW_KEY_Z -> KeyboardKey.RAW_Z
            else -> null
        }
    }
}

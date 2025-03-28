package ch.digorydoo.titanium.main.input

import ch.digorydoo.titanium.engine.input.InputManager

class InputManagerImpl: InputManager() {
    enum class KeyAction { PRESS, RELEASE }

    fun onGLFWKeyEvent(glfwKey: Int, action: KeyAction) {
        (keyboard as KeyboardImpl).onGLFWKeyEvent(glfwKey, action)

        if (action == KeyAction.PRESS) {
            mode = InputMode.KEYBOARD
        }
    }

    fun onGLFWCharEvent(charCode: Int) {
        (keyboard as KeyboardImpl).onGLFWCharEvent(charCode)
        mode = InputMode.KEYBOARD
    }
}

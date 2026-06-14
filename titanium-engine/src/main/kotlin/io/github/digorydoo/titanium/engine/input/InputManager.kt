package io.github.digorydoo.titanium.engine.input

import io.github.digorydoo.titanium.engine.input.gamepad.Gamepad
import io.github.digorydoo.titanium.engine.input.keyboard.Keyboard

/**
 * The InputManager is the owner of the object related to gamepad and keyboard input. Accessing these values go
 * preferable over App.input (which resolves to App.inputMgr.accessor).
 */
abstract class InputManager(
    protected val gamepad: Gamepad,
    protected val keyboard: Keyboard,
) {
    enum class InputMode { KEYBOARD, GAMEPAD }

    var mode = InputMode.KEYBOARD; protected set

    val accessor = InputAccessor(gamepad, keyboard)

    fun findAvailableGamepad() =
        gamepad.findAvailable()

    fun bindGamepad(gamepadId: Int) {
        gamepad.bind(gamepadId)
        mode = InputMode.GAMEPAD
    }

    fun unbindGamepad() {
        gamepad.unbind()
        mode = InputMode.KEYBOARD
    }

    fun update() {
        gamepad.update() // always update so that we can check anyBtnPressed

        if (mode == InputMode.KEYBOARD) {
            accessor.updateSynthesized()

            if (gamepad.anyBtnPressed) {
                mode = InputMode.GAMEPAD
            }
        }
    }
}

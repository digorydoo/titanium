package ch.digorydoo.titanium.engine.input

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.input.gamepad.GamepadBtn

/**
 * The InputManager is the owner of the object related to gamepad and keyboard input. Accessing these values go
 * preferable over App.input (which resolves to App.inputMgr.accessor).
 */
abstract class InputManager {
    enum class InputMode { KEYBOARD, GAMEPAD }

    var mode = InputMode.KEYBOARD; protected set

    protected val gamepad = App.factory.createGamepad()
    protected val keyboard = App.factory.createKeyboard()

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
        gamepad.update()

        if (mode != InputMode.GAMEPAD) {
            val shouldUseGamepad = gamepad.run {
                leftJoy.x != 0.0f ||
                    leftJoy.y != 0.0f ||
                    rightJoy.x != 0.0f ||
                    rightJoy.y != 0.0f ||
                    isPressed(GamepadBtn.ACTION_A) ||
                    isPressed(GamepadBtn.OPEN_MENU_LEFT) ||
                    isPressed(GamepadBtn.OPEN_MENU_RIGHT) ||
                    isPressed(GamepadBtn.REAR_UPPER_LEFT) ||
                    isPressed(GamepadBtn.REAR_UPPER_RIGHT)
            }

            if (shouldUseGamepad) {
                mode = InputMode.GAMEPAD
            }
        }

        if (mode == InputMode.KEYBOARD) {
            accessor.updateSynthesized()
        }
    }
}

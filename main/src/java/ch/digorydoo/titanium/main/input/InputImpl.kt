package ch.digorydoo.titanium.main.input

import ch.digorydoo.titanium.engine.input.GamepadState
import ch.digorydoo.titanium.engine.input.Input
import ch.digorydoo.titanium.engine.input.Input.InputMode
import ch.digorydoo.titanium.engine.input.Input.Key
import ch.digorydoo.titanium.engine.input.InputValues
import ch.digorydoo.titanium.engine.input.KeyStateMap

class InputImpl: Input {
    enum class KeyAction { PRESS, RELEASE }

    private val keyStateMap = KeyStateMap()
    private val keyboard = KeyboardImpl(keyStateMap)
    private val gamepadState = GamepadState()
    private val gamepad = GamepadImpl(gamepadState)
    override val values = InputValues(keyStateMap, keyboard, gamepadState)
    override var mode = InputMode.KEYBOARD; private set

    fun findJoyId() {
        gamepad.findJoyId()
    }

    fun bindJoyId(joyId: Int) {
        gamepad.bindJoyId(joyId)
        mode = InputMode.GAMEPAD
    }

    fun onGLFWKeyEvent(glfwKey: Int, action: KeyAction) {
        keyboard.onGLFWKeyEvent(glfwKey, action)

        if (action == KeyAction.PRESS) {
            mode = InputMode.KEYBOARD
        }
    }

    fun onGLFWCharEvent(charCode: Int) {
        keyboard.onGLFWCharEvent(charCode)
        mode = InputMode.KEYBOARD
    }

    fun updateGamepad() {
        gamepad.update()

        val s = gamepadState

        if (s.anyAboveThreshold) {
            mode = InputMode.GAMEPAD
        }

        if (mode != InputMode.GAMEPAD) {
            return
        }

        // Translate all gamepad values that may need a handleTime to Keys.

        keyStateMap.setPressedOrReleased(Key.ACTION_A, s.btnA)
        keyStateMap.setPressedOrReleased(Key.ACTION_B, s.btnB)
        keyStateMap.setPressedOrReleased(Key.ACTION_X, s.btnX)
        keyStateMap.setPressedOrReleased(Key.ACTION_Y, s.btnY)

        keyStateMap.setPressedOrReleased(Key.MENU_LEFT, s.backBtn)
        keyStateMap.setPressedOrReleased(Key.MENU_RIGHT, s.startBtn)

        keyStateMap.setPressedOrReleased(Key.LJOY_LEFT, s.leftJoy.x < -0.1f)
        keyStateMap.setPressedOrReleased(Key.LJOY_RIGHT, s.leftJoy.x > 0.1f)
        keyStateMap.setPressedOrReleased(Key.LJOY_UP, s.leftJoy.y < -0.1f)
        keyStateMap.setPressedOrReleased(Key.LJOY_DOWN, s.leftJoy.y > 0.1f)

        keyStateMap.setPressedOrReleased(Key.RJOY_LEFT, s.rightJoy.x < -0.1f)
        keyStateMap.setPressedOrReleased(Key.RJOY_RIGHT, s.rightJoy.x > 0.1f)
        keyStateMap.setPressedOrReleased(Key.RJOY_UP, s.rightJoy.y < -0.1f)
        keyStateMap.setPressedOrReleased(Key.RJOY_DOWN, s.rightJoy.y > 0.1f)

        keyStateMap.setPressedOrReleased(Key.HAT_LEFT, s.hat.x < -0.1f)
        keyStateMap.setPressedOrReleased(Key.HAT_RIGHT, s.hat.x > 0.1f)
        keyStateMap.setPressedOrReleased(Key.HAT_UP, s.hat.y < -0.1f)
        keyStateMap.setPressedOrReleased(Key.HAT_DOWN, s.hat.y > 0.1f)

        keyStateMap.setPressedOrReleased(Key.REAR_LEFT_BTN, s.rearLeftBtn)
        keyStateMap.setPressedOrReleased(Key.REAR_RIGHT_BTN, s.rearRightBtn)
        keyStateMap.setPressedOrReleased(Key.REAR_LEFT_THRUSH, s.rearLeftThrush > 0.1f)
        keyStateMap.setPressedOrReleased(Key.REAR_RIGHT_THRUSH, s.rearRightThrush > 0.1f)
    }
}

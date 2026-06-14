package io.github.digorydoo.titanium.engine.input

import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.input.InputManager.InputMode
import io.github.digorydoo.titanium.engine.input.gamepad.Gamepad
import io.github.digorydoo.titanium.engine.input.gamepad.GamepadBtn
import io.github.digorydoo.titanium.engine.input.keyboard.DirectionalKeys
import io.github.digorydoo.titanium.engine.input.keyboard.Keyboard
import io.github.digorydoo.titanium.engine.input.keyboard.KeyboardKey

/**
 * This class tries to unify gamepad and keyboard access and is available through App.input.
 */
class InputAccessor(private val gamepad: Gamepad, private val keyboard: Keyboard) {
    val mode get() = App.inputMgr.mode

    // Gamepad buttons that have a perfect mapping with a KeyboardKey should be accessed with the following shorthands.

    val selectBtn = BtnOrKeyAccessor(GamepadBtn.ACTION_A, KeyboardKey.ENTER)
    val dismissBtn = BtnOrKeyAccessor(GamepadBtn.ACTION_B, KeyboardKey.ESCAPE)
    val dashBtn = BtnOrKeyAccessor(GamepadBtn.ACTION_B, KeyboardKey.RIGHT_SHIFT)
    val jumpBtn = BtnOrKeyAccessor(GamepadBtn.ACTION_X, KeyboardKey.RIGHT_META)
    val useBtn = BtnOrKeyAccessor(GamepadBtn.ACTION_Y, KeyboardKey.LEFT_CTRL)
    val skipBtn = BtnOrKeyAccessor(GamepadBtn.ACTION_X, KeyboardKey.ESCAPE)

    val hatOrArrowLeft = BtnOrKeyAccessor(GamepadBtn.HAT_LEFT, KeyboardKey.ARROW_LEFT)
    val hatOrArrowRight = BtnOrKeyAccessor(GamepadBtn.HAT_RIGHT, KeyboardKey.ARROW_RIGHT)
    val hatOrArrowUp = BtnOrKeyAccessor(GamepadBtn.HAT_UP, KeyboardKey.ARROW_UP)
    val hatOrArrowDown = BtnOrKeyAccessor(GamepadBtn.HAT_DOWN, KeyboardKey.ARROW_DOWN)

    // Some gamepad buttons do not have a perfect mapping with a KeyboardKey. For instance, menus may need to behave
    // slightly differently when in gamepad mode vs. keyboard mode. The reason is that gamepad navigation can involve
    // the hat switch as well as ZL and ZR, while keyboard navigation should rely on arrow keys and TAB. In these cases,
    // GamepadBtn and KeyboardKey should be directly accessed with the following functions.

    fun checkPressed(btn: GamepadBtn) = gamepad.checkPressed(btn)
    fun checkPressedOnce(btn: GamepadBtn) = gamepad.checkPressedOnce(btn)
    fun checkPressedWithRepeat(btn: GamepadBtn) = gamepad.checkPressedWithRepeat(btn)
    fun didHandle(btn: GamepadBtn) = gamepad.didHandle(btn)

    fun checkPressed(key: KeyboardKey) = keyboard.checkPressed(key)
    fun checkPressedOnce(key: KeyboardKey) = keyboard.checkPressedOnce(key)
    fun checkPressedWithRepeat(key: KeyboardKey) = keyboard.checkPressedWithRepeat(key)
    fun didHandle(key: KeyboardKey) = keyboard.didHandle(key)

    val anyBtnOrKeyPressed get() = gamepad.anyBtnPressed || keyboard.anyKeyPressed

    // Accessing keys by Char means that they depend on the system's current keyboard layout, and the key in question
    // may even be unavailable. Therefore, these functions should be used by the editor only.

    fun checkPressed(c: Char) = keyboard.checkPressed(c)
    fun checkPressedOnce(c: Char) = keyboard.checkPressedOnce(c)
    fun checkPressedWithRepeat(c: Char) = keyboard.checkPressedWithRepeat(c)

    // The following shorthands should be used by the engine only. The game should use BtnOrKeyAccessor and should
    // distinguish between the left and right modifier keys.

    internal val altIsDown get() = keyboard.isDown(KeyboardKey.LEFT_ALT) || keyboard.isDown(KeyboardKey.RIGHT_ALT)
    internal val ctrlIsDown get() = keyboard.isDown(KeyboardKey.LEFT_CTRL) || keyboard.isDown(KeyboardKey.RIGHT_CTRL)
    internal val shiftIsDown get() = keyboard.isDown(KeyboardKey.LEFT_SHIFT) || keyboard.isDown(KeyboardKey.RIGHT_SHIFT)

    // The left and right joystick get a special treatment. They are available as Vector2f even in keyboard mode, and
    // they are available as Boolean button values even in gamepad mode.

    val leftJoy: Vector2f
        get() = when (mode) {
            InputMode.GAMEPAD -> gamepad.leftJoy
            InputMode.KEYBOARD -> ljoySynthesized.dir
        }

    val rightJoy: Vector2f
        get() = when (mode) {
            InputMode.GAMEPAD -> gamepad.rightJoy
            InputMode.KEYBOARD -> rjoySynthesized.dir
        }

    private val ljoyKeys = DirectionalKeys(KeyboardKey.RAW_A, KeyboardKey.RAW_D, KeyboardKey.RAW_W, KeyboardKey.RAW_S)
    private val rjoyKeys = DirectionalKeys(KeyboardKey.RAW_H, KeyboardKey.RAW_K, KeyboardKey.RAW_U, KeyboardKey.RAW_J)

    val ljoyLeft = BtnOrKeyAccessor(GamepadBtn.LJOY_LEFT, ljoyKeys.xNegativeKey)
    val ljoyRight = BtnOrKeyAccessor(GamepadBtn.LJOY_RIGHT, ljoyKeys.xPositiveKey)
    val ljoyUp = BtnOrKeyAccessor(GamepadBtn.LJOY_UP, ljoyKeys.yNegativeKey)
    val ljoyDown = BtnOrKeyAccessor(GamepadBtn.LJOY_DOWN, ljoyKeys.yPositiveKey)

    val rjoyLeft = BtnOrKeyAccessor(GamepadBtn.RJOY_LEFT, rjoyKeys.xNegativeKey)
    val rjoyRight = BtnOrKeyAccessor(GamepadBtn.RJOY_RIGHT, rjoyKeys.xPositiveKey)
    val rjoyUp = BtnOrKeyAccessor(GamepadBtn.RJOY_UP, rjoyKeys.yNegativeKey)
    val rjoyDown = BtnOrKeyAccessor(GamepadBtn.RJOY_DOWN, rjoyKeys.yPositiveKey)

    private val ljoySynthesized = SynthesizedJoy(ljoyKeys, keyboard)
    private val rjoySynthesized = SynthesizedJoy(rjoyKeys, keyboard)

    fun updateSynthesized() {
        ljoySynthesized.update()
        rjoySynthesized.update()
    }

    inner class BtnOrKeyAccessor(val btn: GamepadBtn, val key: KeyboardKey) {
        val isDown
            get() = when (App.inputMgr.mode) {
                InputMode.GAMEPAD -> gamepad.isDown(btn)
                InputMode.KEYBOARD -> keyboard.isDown(key)
            }

        fun checkPressed() = when (App.inputMgr.mode) {
            InputMode.GAMEPAD -> gamepad.checkPressed(btn)
            InputMode.KEYBOARD -> keyboard.checkPressed(key)
        }

        fun checkPressedOnce() = when (App.inputMgr.mode) {
            InputMode.GAMEPAD -> gamepad.checkPressedOnce(btn)
            InputMode.KEYBOARD -> keyboard.checkPressedOnce(key)
        }

        fun checkPressedWithRepeat() = when (App.inputMgr.mode) {
            InputMode.GAMEPAD -> gamepad.checkPressedWithRepeat(btn)
            InputMode.KEYBOARD -> keyboard.checkPressedWithRepeat(key)
        }

        fun didHandle() = when (App.inputMgr.mode) {
            InputMode.GAMEPAD -> gamepad.didHandle(btn)
            InputMode.KEYBOARD -> keyboard.didHandle(key)
        }
    }
}

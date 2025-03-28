package ch.digorydoo.titanium.engine.input

import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.input.InputManager.InputMode
import ch.digorydoo.titanium.engine.input.gamepad.Gamepad
import ch.digorydoo.titanium.engine.input.gamepad.GamepadBtn
import ch.digorydoo.titanium.engine.input.keyboard.DirectionalKeys
import ch.digorydoo.titanium.engine.input.keyboard.Keyboard
import ch.digorydoo.titanium.engine.input.keyboard.KeyboardKey

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

    val hatOrArrowLeft = BtnOrKeyAccessor(GamepadBtn.HAT_LEFT, KeyboardKey.ARROW_LEFT)
    val hatOrArrowRight = BtnOrKeyAccessor(GamepadBtn.HAT_RIGHT, KeyboardKey.ARROW_RIGHT)
    val hatOrArrowUp = BtnOrKeyAccessor(GamepadBtn.HAT_UP, KeyboardKey.ARROW_UP)
    val hatOrArrowDown = BtnOrKeyAccessor(GamepadBtn.HAT_DOWN, KeyboardKey.ARROW_DOWN)

    // Some gamepad buttons do not have a perfect mapping with a KeyboardKey. For instance, menus may need to behave
    // slightly differently when in gamepad mode vs. keyboard mode. The reason is that gamepad navigation can involve
    // the hat switch as well as ZL and ZR, while keyboard navigation should rely on arrow keys and TAB. In these cases,
    // GamepadBtn and KeyboardKey should be directly accessed with the following functions.

    fun isPressed(btn: GamepadBtn) = gamepad.isPressed(btn)
    fun isPressedOnce(btn: GamepadBtn) = gamepad.isPressedOnce(btn)
    fun isPressedWithRepeat(btn: GamepadBtn) = gamepad.isPressedWithRepeat(btn)

    fun isPressed(key: KeyboardKey) = keyboard.isPressed(key)
    fun isPressedOnce(key: KeyboardKey) = keyboard.isPressedOnce(key)
    fun isPressedWithRepeat(key: KeyboardKey) = keyboard.isPressedWithRepeat(key)

    // Accessing keys by Char means that they depend on the system's current keyboard layout, and the key in question
    // may even be unavailable. Therefore, these functions should be used by the editor only.

    fun isPressed(c: Char) = keyboard.isPressed(c)
    fun isPressedOnce(c: Char) = keyboard.isPressedOnce(c)
    fun isPressedWithRepeat(c: Char) = keyboard.isPressedWithRepeat(c)

    // The following shorthands should be used by the Editor only. The game should use BtnOrKeyAccessor and should
    // distinguish between the left and right modifier key.

    val altPressed get() = isPressed(KeyboardKey.LEFT_ALT) || isPressed(KeyboardKey.RIGHT_ALT)
    val ctrlPressed get() = isPressed(KeyboardKey.LEFT_CTRL) || isPressed(KeyboardKey.RIGHT_CTRL)
    val shiftPressed get() = isPressed(KeyboardKey.LEFT_SHIFT) || isPressed(KeyboardKey.RIGHT_SHIFT)

    // The left and right joystick get a special treatment. They are available as Point2f even in keyboard mode, and
    // they are available as Boolean button values even in gamepad mode.

    val leftJoy: Point2f
        get() = when (mode) {
            InputMode.GAMEPAD -> gamepad.leftJoy
            InputMode.KEYBOARD -> ljoySynthesized.dir
        }

    val rightJoy: Point2f
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

    inner class BtnOrKeyAccessor(private val btn: GamepadBtn, private val key: KeyboardKey) {
        val pressed
            get() = when (App.inputMgr.mode) {
                InputMode.GAMEPAD -> gamepad.isPressed(btn)
                InputMode.KEYBOARD -> keyboard.isPressed(key)
            }
        val pressedOnce
            get() = when (App.inputMgr.mode) {
                InputMode.GAMEPAD -> gamepad.isPressedOnce(btn)
                InputMode.KEYBOARD -> keyboard.isPressedOnce(key)
            }
        val pressedWithRepeat
            get() = when (App.inputMgr.mode) {
                InputMode.GAMEPAD -> gamepad.isPressedWithRepeat(btn)
                InputMode.KEYBOARD -> keyboard.isPressedWithRepeat(key)
            }
    }
}

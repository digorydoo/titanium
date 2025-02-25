package ch.digorydoo.titanium.engine.input

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.input.Input.InputMode
import ch.digorydoo.titanium.engine.input.Input.Key

@Suppress("unused")
class InputValues(
    private val keyStateMap: KeyStateMap,
    private val keyboard: Keyboard,
    private val gamepadState: GamepadState,
) {
    interface SpecialWrapper<T> {
        fun onPressedOnce(lambda: (t: T) -> Unit)
        fun onPressedWithRepeat(lambda: (t: T) -> Unit)
    }

    inner class KeyWrapper(private val key: Key) {
        val pressed get() = keyStateMap.isPressed(key)
        val pressedOnce get() = keyStateMap.isPressedOnce(key)
        val pressedWithRepeat get() = keyStateMap.isPressedWithRepeat(key)
    }

    val shift = KeyWrapper(Key.SHIFT)
    val alt = KeyWrapper(Key.ALT)
    val actionA = KeyWrapper(Key.ACTION_A)
    val actionB = KeyWrapper(Key.ACTION_B)
    val actionX = KeyWrapper(Key.ACTION_X)
    val actionY = KeyWrapper(Key.ACTION_Y)
    val menuLeft = KeyWrapper(Key.MENU_LEFT)
    val menuRight = KeyWrapper(Key.MENU_RIGHT)
    val enter = KeyWrapper(Key.ENTER)
    val escape = KeyWrapper(Key.ESCAPE)

    private val _leftJoy = MutablePoint2f() // to avoid new objects
    private val _rightJoy = MutablePoint2f() // to avoid new objects

    // Use leftJoy and rightJoy when working with analog values, e.g. walk, move camera.
    // These values are synthesised ad hoc when in keyboard mode.

    val leftJoy: Point2f
        get() = when (App.input.mode) {
            InputMode.KEYBOARD -> _leftJoy.set(
                keysToFloat(Key.LJOY_LEFT, Key.LJOY_RIGHT),
                keysToFloat(Key.LJOY_UP, Key.LJOY_DOWN),
            )
            else -> gamepadState.leftJoy
        }

    val rightJoy: Point2f
        get() = when (App.input.mode) {
            InputMode.KEYBOARD -> _rightJoy.set(
                keysToFloat(Key.RJOY_LEFT, Key.RJOY_RIGHT),
                keysToFloat(Key.RJOY_UP, Key.RJOY_DOWN),
            )
            else -> gamepadState.rightJoy
        }

    // Use ljoy* and rjoy* when you need pressedOnce, e.g. select the next item in a menu.
    // These keys are synthesized in InputImpl.updateGamepad when in gamepad mode.

    val ljoyLeft = KeyWrapper(Key.LJOY_LEFT)
    val ljoyRight = KeyWrapper(Key.LJOY_RIGHT)
    val ljoyUp = KeyWrapper(Key.LJOY_UP)
    val ljoyDown = KeyWrapper(Key.LJOY_DOWN)

    val rjoyLeft = KeyWrapper(Key.RJOY_LEFT)
    val rjoyRight = KeyWrapper(Key.RJOY_RIGHT)
    val rjoyUp = KeyWrapper(Key.RJOY_UP)
    val rjoyDown = KeyWrapper(Key.RJOY_DOWN)

    // Menus should listen to the hat switch. This is most natural in gamepad mode. In keyboard mode, the hat switch
    // gets mapped to ASDW, but it's more natural to use the arrow keys, which are mapped to ljoy*. So, menus should
    // usually listen to *both* the hat switch and ljoy*.

    val hatLeft = KeyWrapper(Key.HAT_LEFT)
    val hatRight = KeyWrapper(Key.HAT_RIGHT)
    val hatUp = KeyWrapper(Key.HAT_UP)
    val hatDown = KeyWrapper(Key.HAT_DOWN)

    val editorToggle = KeyWrapper(Key.EDITOR_TOGGLE)
    val editorCut = KeyWrapper(Key.EDITOR_CUT)
    val editorCopy = KeyWrapper(Key.EDITOR_COPY)
    val editorPaste = KeyWrapper(Key.EDITOR_PASTE)
    val editorSave = KeyWrapper(Key.EDITOR_SAVE)
    val editorUndo = KeyWrapper(Key.EDITOR_UNDO)
    val editorInfo = KeyWrapper(Key.EDITOR_INFO)
    val editorDraw = KeyWrapper(Key.EDITOR_DRAW)
    val editorPosZInc = KeyWrapper(Key.EDITOR_POSZ_INC)
    val editorPosZDec = KeyWrapper(Key.EDITOR_POSZ_DEC)
    val editorBackspace = KeyWrapper(Key.EDITOR_BACKSPACE)
    val editorCameraMode = KeyWrapper(Key.EDITOR_CAMERA_MODE)
    val editorSwitchCameraTarget = KeyWrapper(Key.EDITOR_SWITCH_CAMERA_TARGET)
    val editorRotate = KeyWrapper(Key.EDITOR_ROTATE)
    val editorShapeMenu = KeyWrapper(Key.EDITOR_SHAPE_MENU)
    val editorApplyShape = KeyWrapper(Key.EDITOR_APPLY_SHAPE)
    val editorMaterialMenu = KeyWrapper(Key.EDITOR_MATERIAL_MENU)
    val editorApplyMaterial = KeyWrapper(Key.EDITOR_APPLY_MATERIAL)
    val editorPickShapeMaterial = KeyWrapper(Key.EDITOR_PICK_SHAPE_MATERIAL)
    val editorPageLeft = KeyWrapper(Key.EDITOR_PAGE_LEFT)
    val editorPageRight = KeyWrapper(Key.EDITOR_PAGE_RIGHT)
    val editorPageUp = KeyWrapper(Key.EDITOR_PAGE_UP)
    val editorPageDown = KeyWrapper(Key.EDITOR_PAGE_DOWN)
    val editorNextSpawnPt = KeyWrapper(Key.EDITOR_NEXT_SPAWN_PT)
    val editorSpawnPtMenu = KeyWrapper(Key.EDITOR_SPAWN_PT_MENU)
    val editorWizardMenu = KeyWrapper(Key.EDITOR_WIZARD_MENU)

    val charKey = object: SpecialWrapper<Char> {
        override fun onPressedOnce(lambda: (c: Char) -> Unit) = keyboard.onCharPressedOnce(lambda)
        override fun onPressedWithRepeat(lambda: (c: Char) -> Unit) = keyboard.onCharPressedWithRepeat(lambda)
    }

    val numberKey = object: SpecialWrapper<Int> {
        override fun onPressedOnce(lambda: (num: Int) -> Unit) = keyboard.onNumberPressedOnce(lambda)
        override fun onPressedWithRepeat(lambda: (num: Int) -> Unit) = keyboard.onNumberPressedWithRepeat(lambda)
    }

    private fun keysToFloat(negKey: Key, posKey: Key) = when {
        keyStateMap.isPressed(negKey) -> -1.0f
        keyStateMap.isPressed(posKey) -> 1.0f
        else -> 0.0f
    }
}

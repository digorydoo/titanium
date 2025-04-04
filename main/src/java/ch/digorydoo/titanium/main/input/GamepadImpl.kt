package ch.digorydoo.titanium.main.input

import ch.digorydoo.kutils.math.spow
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.input.gamepad.Gamepad
import ch.digorydoo.titanium.engine.input.gamepad.GamepadBtn
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWGamepadState

class GamepadImpl: Gamepad() {
    private val rawState = GLFWGamepadState.create()
    private var gamepadId = -1
    private var name = ""

    /**
     * GLFW internally uses SDL's gamepad database for looking up the mapping of the gamepad buttons. This flag
     * indicates whether there is a mapping for the currently bound controller.
     */
    private var hasMapping = false

    /**
     * The GUID is an SDL-compatible id that identifies the vendor and product and may sometimes also depend on the OS.
     * "030000007e0500000920000010020000" (macOS): Nintendo Switch Pro controller
     * "030000006d04000016c2000014040000" (macOS): Logitech Gamepad F310
     */
    private var guid = ""

    override fun findAvailable(): Int {
        for (i in 0 ..< GLFW_JOYSTICK_LAST) {
            if (glfwJoystickPresent(i)) return i
        }
        return -1
    }

    override fun bind(gamepadId: Int) {
        this.gamepadId = gamepadId
        name = glfwGetGamepadName(gamepadId)?.takeIf { it.isNotEmpty() } ?: "Controller #${gamepadId}"
        hasMapping = glfwJoystickIsGamepad(gamepadId)
        guid = glfwGetJoystickGUID(gamepadId) ?: ""
        state.reset()
        Log.info(TAG, "Game controller bound: $name, id=$gamepadId, hasMapping=$hasMapping, guid=$guid")
    }

    override fun unbind() {
        if (gamepadId >= 0) Log.info(TAG, "Unbinding game controller $name, id=$gamepadId")
        gamepadId = -1
        hasMapping = false
        name = ""
        state.reset()
    }

    override fun update() {
        if (gamepadId < 0) {
            return
        }

        // Hats is a ByteBuffer. Its layout should be the same for all devices.
        val hats = glfwGetJoystickHats(gamepadId)

        if (hats == null || hats.limit() < 1) {
            state.set(GamepadBtn.HAT_LEFT, false)
            state.set(GamepadBtn.HAT_RIGHT, false)
            state.set(GamepadBtn.HAT_UP, false)
            state.set(GamepadBtn.HAT_DOWN, false)
        } else {
            // If there are multiple hat-switches, we only take the first one into account.
            val bits = (hats.get(0).toUInt() and 255u).toInt()
            state.set(GamepadBtn.HAT_LEFT, (bits and GLFW_HAT_LEFT) != 0)
            state.set(GamepadBtn.HAT_RIGHT, (bits and GLFW_HAT_RIGHT) != 0)
            state.set(GamepadBtn.HAT_UP, (bits and GLFW_HAT_UP) != 0)
            state.set(GamepadBtn.HAT_DOWN, (bits and GLFW_HAT_DOWN) != 0)
        }

        val btnA: Boolean
        val btnB: Boolean
        val btnX: Boolean
        val btnY: Boolean

        if (hasMapping) {
            glfwGetGamepadState(gamepadId, rawState)

            // axes is a FloatBuffer.
            // x0, y0, x1, y1 are -1 (left/up) .. +1 (right/down).
            // lt and rt are -1 (released) .. +1 (pressed); digital under macOS unfortunately.
            val axes = rawState.axes()
            val numAxes = axes.limit()

            state.leftJoy.x = if (numAxes <= 0) 0.0f else axes.get(0)
            state.leftJoy.y = if (numAxes <= 1) 0.0f else axes.get(1)
            state.rightJoy.x = if (numAxes <= 2) 0.0f else axes.get(2)
            state.rightJoy.y = if (numAxes <= 3) 0.0f else axes.get(3)
            state.rearLowerLeft = if (numAxes <= 4) 0.0f else 0.5f + 0.5f * axes.get(4)
            state.rearLowerRight = if (numAxes <= 5) 0.0f else 0.5f + 0.5f * axes.get(5)

            // buttons is a ByteBuffer.
            val btns = rawState.buttons()
            val numBtns = btns.limit()
            val nullByte = 0.toByte()

            btnA = if (numBtns <= 0) false else btns.get(0) != nullByte
            btnB = if (numBtns <= 1) false else btns.get(1) != nullByte
            btnX = if (numBtns <= 2) false else btns.get(2) != nullByte
            btnY = if (numBtns <= 3) false else btns.get(3) != nullByte

            state.set(GamepadBtn.REAR_UPPER_LEFT, if (numBtns <= 4) false else btns.get(4) != nullByte)
            state.set(GamepadBtn.REAR_UPPER_RIGHT, if (numBtns <= 5) false else btns.get(5) != nullByte)
            state.set(GamepadBtn.OPEN_MENU_LEFT, if (numBtns <= 6) false else btns.get(6) != nullByte)
            state.set(GamepadBtn.OPEN_MENU_RIGHT, if (numBtns <= 7) false else btns.get(7) != nullByte)
            // state.set(GamepadBtn.VENDOR_BTN, if (numBtns <= 8) false else btns.get(8) != nullByte)
            // state.set(GamepadBtn.LJOY_MIDDLE_BTN, if (numBtns <= 9) false else btns.get(9) != nullByte)
            // state.set(GamepadBtn.RJOY_MIDDLE_BTN, if (numBtns <= 10) false else btns.get(10) != nullByte)
        } else {
            // GLFW does not have a mapping for the connected device. Try applying a layout that's identical with
            // Logitech Gamepad F310, even though it's not likely to succeed.
            //
            // We come here for the Nintendo Switch Pro controller, which is not supported by GLFW at this time:
            // https://github.com/glfw/glfw/issues/1627
            // Our mapping does not help either, because the controller would need non-standard initialisation, and thus
            // all values will stay 0 or -1.

            val axes = glfwGetJoystickAxes(gamepadId)

            if (axes == null) {
                state.leftJoy.x = 0.0f
                state.leftJoy.y = 0.0f
                state.rightJoy.x = 0.0f
                state.rightJoy.y = 0.0f
            } else {
                val numAxes = axes.limit()
                state.leftJoy.x = if (numAxes <= 0) 0.0f else axes.get(0)
                state.leftJoy.y = if (numAxes <= 1) 0.0f else axes.get(1)
                state.rightJoy.x = if (numAxes <= 2) 0.0f else axes.get(2)
                state.rightJoy.y = if (numAxes <= 3) 0.0f else axes.get(3)
            }

            val btns = glfwGetJoystickButtons(gamepadId)

            if (btns == null) {
                btnA = false
                btnB = false
                btnX = false
                btnY = false
                state.set(GamepadBtn.REAR_UPPER_LEFT, false)
                state.set(GamepadBtn.REAR_UPPER_RIGHT, false)
                state.rearLowerLeft = 0.0f
                state.rearLowerRight = 0.0f
                state.set(GamepadBtn.OPEN_MENU_LEFT, false)
                state.set(GamepadBtn.OPEN_MENU_RIGHT, false)
                // state.set(GamepadBtn.VENDOR_BTN, false)
                // state.set(GamepadBtn.LJOY_MIDDLE_BTN, false)
                // state.set(GamepadBtn.RJOY_MIDDLE_BTN, false)
            } else {
                val numBtns = btns.limit()
                val nullByte = 0.toByte()

                btnA = if (numBtns <= 1) false else btns.get(1) != nullByte
                btnB = if (numBtns <= 2) false else btns.get(2) != nullByte
                btnX = if (numBtns <= 0) false else btns.get(0) != nullByte
                btnY = if (numBtns <= 3) false else btns.get(3) != nullByte
                state.set(GamepadBtn.REAR_UPPER_LEFT, if (numBtns <= 4) false else btns.get(4) != nullByte)
                state.set(GamepadBtn.REAR_UPPER_RIGHT, if (numBtns <= 5) false else btns.get(5) != nullByte)
                state.rearLowerLeft = if (numBtns <= 6) 0.0f else if (btns.get(6) != nullByte) 1.0f else 0.0f
                state.rearLowerRight = if (numBtns <= 7) 0.0f else if (btns.get(7) != nullByte) 1.0f else 0.0f
                state.set(GamepadBtn.OPEN_MENU_LEFT, if (numBtns <= 8) false else btns.get(8) != nullByte)
                state.set(GamepadBtn.OPEN_MENU_RIGHT, if (numBtns <= 9) false else btns.get(9) != nullByte)
                // state.set(GamepadBtn.VENDOR_BTN, false)
                // state.set(GamepadBtn.LJOY_MIDDLE_BTN, if (numBtns <= 10) false else btns.get(10) != nullByte)
                // state.set(GamepadBtn.RJOY_MIDDLE_BTN, if (numBtns <= 11) false else btns.get(11) != nullByte)
            }
        }

        // Apply a dead zone to axes

        applyDeadZone(state.leftJoy)
        applyDeadZone(state.rightJoy)

        // Make axes behave non-linearly

        state.leftJoy.apply {
            x = spow(x, AXES_NON_LINEARITY)
            y = spow(y, AXES_NON_LINEARITY)
        }
        state.rightJoy.apply {
            x = spow(x, AXES_NON_LINEARITY)
            y = spow(y, AXES_NON_LINEARITY)
        }

        // Set ABXY, and swap them if desired

        val swap = App.prefs.swapGamepadBtnsABXY

        state.apply {
            set(GamepadBtn.ACTION_X, if (swap) btnY else btnX)
            set(GamepadBtn.ACTION_Y, if (swap) btnX else btnY)
            set(GamepadBtn.ACTION_A, if (swap) btnB else btnA)
            set(GamepadBtn.ACTION_B, if (swap) btnA else btnB)
        }

        // Derive button values from float values

        state.apply {
            set(GamepadBtn.LJOY_LEFT, leftJoy.x < -0.1f)
            set(GamepadBtn.LJOY_RIGHT, leftJoy.x > 0.1f)
            set(GamepadBtn.LJOY_UP, leftJoy.y < -0.1f)
            set(GamepadBtn.LJOY_DOWN, leftJoy.y > 0.1f)

            set(GamepadBtn.RJOY_LEFT, rightJoy.x < -0.1f)
            set(GamepadBtn.RJOY_RIGHT, rightJoy.x > 0.1f)
            set(GamepadBtn.RJOY_UP, rightJoy.y < -0.1f)
            set(GamepadBtn.RJOY_DOWN, rightJoy.y > 0.1f)

            set(GamepadBtn.REAR_LOWER_LEFT, rearLowerLeft > 0.1f)
            set(GamepadBtn.REAR_LOWER_RIGHT, rearLowerRight > 0.1f)
        }
    }

    companion object {
        private val TAG = Log.Tag("GamepadImpl")
        private const val AXES_NON_LINEARITY = 2.0f
    }
}

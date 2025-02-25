package ch.digorydoo.titanium.main.input

import ch.digorydoo.kutils.math.spow
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.input.Gamepad
import ch.digorydoo.titanium.engine.input.GamepadState
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWGamepadState

class GamepadImpl(private val state: GamepadState): Gamepad {
    private val rawState = GLFWGamepadState.create()
    private var joyId = -1
    private var hasMapping = false
    private var name = ""

    override fun bindJoyId(jid: Int) {
        joyId = jid
        hasMapping = glfwJoystickIsGamepad(jid)
        name = glfwGetGamepadName(jid) ?: ""
        // guid = glfwGetJoystickGUID(0) ?: ""
        Log.info("Gamepad: joyId=$jid, hasMapping=$hasMapping, name=$name")
    }

    override fun findJoyId() {
        for (i in 0 ..< GLFW_JOYSTICK_LAST) {
            if (glfwJoystickPresent(i)) {
                bindJoyId(i)
                return
            }
        }

        Log.info("Gamepad: No joystick or gamepad found.")
        joyId = -1
        hasMapping = false
        name = ""
        state.reset()
    }

    override fun update() {
        if (joyId < 0) {
            return
        }

        // Hats is a ByteBuffer. Its layout should be the same for all devices.
        val hats = glfwGetJoystickHats(0)

        if (hats == null || hats.limit() < 1) {
            state.hat.set(0.0f, 0.0f)
        } else {
            // If there are multiple hat-switches, we only take the first one into account.
            val bits = (hats.get(0).toUInt() and 255u).toInt()

            state.hat.x = when {
                (bits and GLFW_HAT_LEFT) != 0 -> -1.0f
                (bits and GLFW_HAT_RIGHT) != 0 -> 1.0f
                else -> 0.0f
            }

            state.hat.y = when {
                (bits and GLFW_HAT_UP) != 0 -> -1.0f
                (bits and GLFW_HAT_DOWN) != 0 -> 1.0f
                else -> 0.0f
            }
        }

        if (hasMapping) {
            glfwGetGamepadState(0, rawState)

            // axes is a FloatBuffer. The layout should be the same for all devices.
            // x0, y0, x1, y1 are -1 (left/up) .. +1 (right/down).
            // lt and rt are -1 (released) .. +1 (pressed); digital under macOS unfortunately.
            val axes = rawState.axes()
            val numAxes = axes.limit()

            state.leftJoy.x = if (numAxes <= 0) 0.0f else axes.get(0)
            state.leftJoy.y = if (numAxes <= 1) 0.0f else axes.get(1)
            state.rightJoy.x = if (numAxes <= 2) 0.0f else axes.get(2)
            state.rightJoy.y = if (numAxes <= 3) 0.0f else axes.get(3)
            state.rearLeftThrush = if (numAxes <= 4) 0.0f else 0.5f + 0.5f * axes.get(4)
            state.rearRightThrush = if (numAxes <= 5) 0.0f else 0.5f + 0.5f * axes.get(5)

            // buttons is a ByteBuffer. The layout should be the same for all devices.
            // The vendor button is not properly recognized.
            val btns = rawState.buttons()
            val numBtns = btns.limit()
            val nullByte = 0.toByte()

            state.btnA = if (numBtns <= 0) false else btns.get(0) != nullByte
            state.btnB = if (numBtns <= 1) false else btns.get(1) != nullByte
            state.btnX = if (numBtns <= 2) false else btns.get(2) != nullByte
            state.btnY = if (numBtns <= 3) false else btns.get(3) != nullByte
            state.rearLeftBtn = if (numBtns <= 4) false else btns.get(4) != nullByte
            state.rearRightBtn = if (numBtns <= 5) false else btns.get(5) != nullByte
            state.backBtn = if (numBtns <= 6) false else btns.get(6) != nullByte
            state.startBtn = if (numBtns <= 7) false else btns.get(7) != nullByte
            // state.vendor = if (numBtns <= 8) false else btns.get(8) != nullByte
            state.leftJoyBtn = if (numBtns <= 9) false else btns.get(9) != nullByte
            state.rightJoyBtn = if (numBtns <= 10) false else btns.get(10) != nullByte
        } else {
            // GLFW does not have a mapping for the connected device. We use a fixed mapping, assuming it's identical
            // to Logitech Gamepad.

            val axes = glfwGetJoystickAxes(0)

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

            val btns = glfwGetJoystickButtons(0)

            if (btns == null) {
                state.btnX = false
                state.btnA = false
                state.btnB = false
                state.btnY = false
                state.rearLeftBtn = false
                state.rearRightBtn = false
                state.rearLeftThrush = 0.0f
                state.rearRightThrush = 0.0f
                state.backBtn = false
                state.startBtn = false
                state.leftJoyBtn = false
                state.rightJoyBtn = false
            } else {
                val numBtns = btns.limit()
                val nullByte = 0.toByte()

                state.btnX = if (numBtns <= 0) false else btns.get(0) != nullByte
                state.btnA = if (numBtns <= 1) false else btns.get(1) != nullByte
                state.btnB = if (numBtns <= 2) false else btns.get(2) != nullByte
                state.btnY = if (numBtns <= 3) false else btns.get(3) != nullByte
                state.rearLeftBtn = if (numBtns <= 4) false else btns.get(4) != nullByte
                state.rearRightBtn = if (numBtns <= 5) false else btns.get(5) != nullByte
                state.rearLeftThrush = if (numBtns <= 6) 0.0f else if (btns.get(6) != nullByte) 1.0f else 0.0f
                state.rearRightThrush = if (numBtns <= 7) 0.0f else if (btns.get(7) != nullByte) 1.0f else 0.0f
                state.backBtn = if (numBtns <= 8) false else btns.get(8) != nullByte
                state.startBtn = if (numBtns <= 9) false else btns.get(9) != nullByte
                state.leftJoyBtn = if (numBtns <= 10) false else btns.get(10) != nullByte
                state.rightJoyBtn = if (numBtns <= 11) false else btns.get(11) != nullByte
            }
        }

        // Apply a dead zone to axes

        applyDeadZone(state.leftJoy)
        applyDeadZone(state.rightJoy)

        // Make axes behave non-linear

        state.leftJoy.x = spow(state.leftJoy.x, AXES_NON_LINEARITY)
        state.leftJoy.y = spow(state.leftJoy.y, AXES_NON_LINEARITY)
        state.rightJoy.x = spow(state.rightJoy.x, AXES_NON_LINEARITY)
        state.rightJoy.y = spow(state.rightJoy.y, AXES_NON_LINEARITY)
    }

    private fun applyDeadZone(pt: MutablePoint2f) {
        pt.x = applyDeadZone(pt.x)
        pt.y = applyDeadZone(pt.y)
    }

    private fun applyDeadZone(value: Float) = when {
        value < 0.0f && value >= -DEAD_ZONE -> 0.0f
        value > 0.0f && value <= DEAD_ZONE -> 0.0f
        else -> value
    }

    companion object {
        private const val DEAD_ZONE = 0.06f
        private const val AXES_NON_LINEARITY = 2.0f
    }
}

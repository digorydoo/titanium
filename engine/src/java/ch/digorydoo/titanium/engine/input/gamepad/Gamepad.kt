package ch.digorydoo.titanium.engine.input.gamepad

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f

abstract class Gamepad {
    protected val state = GamepadState()

    val leftJoy: Point2f get() = state.leftJoy
    val rightJoy: Point2f get() = state.rightJoy

    fun isPressed(btn: GamepadBtn) =
        state.isPressed(btn)

    fun isPressedOnce(btn: GamepadBtn) =
        state.isPressedOnce(btn)

    fun isPressedWithRepeat(btn: GamepadBtn) =
        state.isPressedWithRepeat(btn)

    abstract fun findAvailable(): Int
    abstract fun bind(gamepadId: Int)
    abstract fun unbind()

    abstract fun update()

    protected fun applyDeadZone(pt: MutablePoint2f) {
        pt.x = applyDeadZone(pt.x)
        pt.y = applyDeadZone(pt.y)
    }

    protected fun applyDeadZone(value: Float) = when {
        value < 0.0f && value >= -DEAD_ZONE -> 0.0f
        value > 0.0f && value <= DEAD_ZONE -> 0.0f
        else -> value
    }

    companion object {
        private const val DEAD_ZONE = 0.06f
    }
}

package io.github.digorydoo.titanium.engine.input.gamepad

import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.Vector2f

abstract class Gamepad {
    protected val state = GamepadState()

    val leftJoy: Vector2f get() = state.leftJoy
    val rightJoy: Vector2f get() = state.rightJoy

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

    protected fun applyDeadZone(vec: MutableVector2f) {
        vec.x = applyDeadZone(vec.x)
        vec.y = applyDeadZone(vec.y)
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

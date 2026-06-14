package io.github.digorydoo.titanium.engine.input.gamepad

import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.input.InputStateMap

abstract class Gamepad {
    protected val buttons = InputStateMap<GamepadBtn>()
    val anyBtnPressed get() = buttons.anyPressed

    protected val _leftJoy = MutableVector2f()
    val leftJoy: Vector2f get() = _leftJoy

    protected val _rightJoy = MutableVector2f()
    val rightJoy: Vector2f get() = _rightJoy

    var rearLowerLeft = 0.0f; protected set
    var rearLowerRight = 0.0f; protected set

    fun isDown(key: GamepadBtn) = buttons.isDown(key)
    fun checkPressed(key: GamepadBtn) = buttons.checkPressed(key)
    fun checkPressedOnce(key: GamepadBtn) = buttons.checkPressedOnce(key)
    fun checkPressedWithRepeat(key: GamepadBtn) = buttons.checkPressedWithRepeat(key)
    fun set(key: GamepadBtn, pressed: Boolean) = buttons.set(key, pressed)
    fun didHandle(key: GamepadBtn) = buttons.didHandle(key)

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

    protected fun resetState() {
        buttons.setAllReleased()
        _leftJoy.set(0.0f, 0.0f)
        _rightJoy.set(0.0f, 0.0f)
        rearLowerLeft = 0.0f
        rearLowerRight = 0.0f
    }

    companion object {
        private const val DEAD_ZONE = 0.06f
    }
}

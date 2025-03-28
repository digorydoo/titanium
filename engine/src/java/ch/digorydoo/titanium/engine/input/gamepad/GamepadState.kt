package ch.digorydoo.titanium.engine.input.gamepad

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.input.InputStateMap

class GamepadState {
    private val map = InputStateMap<GamepadBtn>()

    val leftJoy = MutablePoint2f()
    val rightJoy = MutablePoint2f()
    var rearLowerLeft = 0.0f
    var rearLowerRight = 0.0f

    fun isPressed(key: GamepadBtn) = map.isPressed(key)
    fun isPressedOnce(key: GamepadBtn) = map.isPressedOnce(key)
    fun isPressedWithRepeat(key: GamepadBtn) = map.isPressedWithRepeat(key)
    fun set(key: GamepadBtn, pressed: Boolean) = map.set(key, pressed)

    fun reset() {
        map.setAllReleased()
        leftJoy.set(0.0f, 0.0f)
        rightJoy.set(0.0f, 0.0f)
        rearLowerLeft = 0.0f
        rearLowerRight = 0.0f
    }
}

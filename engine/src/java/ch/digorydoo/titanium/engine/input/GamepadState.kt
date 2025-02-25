package ch.digorydoo.titanium.engine.input

import ch.digorydoo.kutils.point.MutablePoint2f

class GamepadState {
    var btnA = false
    var btnB = false
    var btnX = false
    var btnY = false
    var startBtn = false
    var backBtn = false
    val hat = MutablePoint2f()
    val leftJoy = MutablePoint2f()
    val rightJoy = MutablePoint2f()
    var leftJoyBtn = false
    var rightJoyBtn = false
    var rearLeftBtn = false
    var rearRightBtn = false
    var rearLeftThrush = 0.0f
    var rearRightThrush = 0.0f

    fun reset() {
        btnA = false
        btnB = false
        btnX = false
        btnY = false
        startBtn = false
        backBtn = false
        hat.set(0.0f, 0.0f)
        leftJoy.set(0.0f, 0.0f)
        rightJoy.set(0.0f, 0.0f)
        leftJoyBtn = false
        rightJoyBtn = false
        rearLeftBtn = false
        rearRightBtn = false
        rearLeftThrush = 0.0f
        rearRightThrush = 0.0f
    }

    /**
     * Used by Input to determine if a Gamepad is being used.
     * Keep this as efficient as possible. Booleans first!
     */
    val anyAboveThreshold: Boolean
        get() = btnA ||
            btnB ||
            btnX ||
            btnY ||
            startBtn ||
            backBtn ||
            leftJoyBtn ||
            rightJoyBtn ||
            rearLeftBtn ||
            rearRightBtn ||
            rearLeftThrush > THRESHOLD ||
            rearRightThrush > THRESHOLD ||
            hat.maxAbsComponent() > THRESHOLD ||
            leftJoy.maxAbsComponent() > THRESHOLD ||
            rightJoy.maxAbsComponent() > THRESHOLD

    companion object {
        private const val THRESHOLD = 0.1f
    }
}

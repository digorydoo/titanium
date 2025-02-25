package ch.digorydoo.titanium.engine.utils

import ch.digorydoo.titanium.engine.core.GameTime.Companion.DELTA_TIME
import kotlin.math.abs

class SmoothFloat(
    initVal: Float = 0.0f,
    private val accel: Float = 0.4f,
    private val nearTargetThreshold: Float = 0.027f,
    private val stopThreshold: Float = 0.0008f,
    private val normalBrake: Float = 0.10f, // 1=stop immediately, 0=don't brake
    private val strongBrake: Float = 0.15f,
) {
    var desired = initVal
    var current = initVal; private set
    private var speed = 0.0f

    fun jump() {
        jumpTo(desired)
    }

    private fun jumpTo(f: Float) {
        desired = f
        current = f
        speed = 0.0f
    }

    fun animate() {
        val deltaSpeed = accel * DELTA_TIME
        val deltaOffset = desired - current
        val isNear = abs(deltaOffset) <= nearTargetThreshold

        if (isNear && abs(speed) <= stopThreshold) {
            speed = 0.0f
            return
        }

        speed += deltaSpeed * deltaOffset
        val brake = if (!isNear) normalBrake else strongBrake
        speed -= speed * brake
        current += speed
    }
}

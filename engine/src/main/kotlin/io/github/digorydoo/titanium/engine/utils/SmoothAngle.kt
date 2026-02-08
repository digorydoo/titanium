package io.github.digorydoo.titanium.engine.utils

import ch.digorydoo.kutils.math.angleDiff
import ch.digorydoo.kutils.math.clamp
import io.github.digorydoo.titanium.engine.core.GameTime.Companion.DELTA_TIME
import kotlin.math.PI
import kotlin.math.abs

/**
 * Note: SmoothFloat may also be used for angles. Consider what happens with a very slow acceleration while the target
 * is moving away at constant speed:
 *    - SmoothFloat tries to unwind the absolute distance, disregarding of the distance around the circle.
 *       - The current value is not restricted to the range of the circle
 *       - If the target is set to a value between 0 ..< 2 * PI while the current value is beyond that range, it will
 *         unroll the entire distance, which is often undesired for angles.
 *    - SmoothAngle tries to move the value in the direction of the shortest distance around the circle.
 *       - The current value will always be in the range 0 ..< 2 * PI
 *       - However, if the target moves too fast, it will start to oscillate between forward and backward acceleration.
 */
class SmoothAngle(
    initVal: Float = 0.0f,
    private val accel: Float = 0.4f,
    private val nearTargetThreshold: Float = 0.027f,
    private val stopThreshold: Float = 0.0008f,
    private val normalBrake: Float = 0.10f, // 1=stop immediately, 0=don't brake
    private val strongBrake: Float = 0.15f,
) {
    var desired = initVal % (2.0 * PI).toFloat()
    var current = desired; private set
    var speed = 0.0f; private set

    fun jump() {
        jumpTo(desired)
    }

    private fun jumpTo(f: Float) {
        desired = f % (2.0 * PI).toFloat()
        current = desired
        speed = 0.0f
    }

    fun animate() {
        val curDelta = angleDiff(current, desired)
        val isNear = abs(curDelta) <= nearTargetThreshold

        if (isNear && abs(speed) <= stopThreshold) {
            speed = 0.0f
            return
        }

        speed += accel * DELTA_TIME * curDelta
        speed -= speed * (if (!isNear) normalBrake else strongBrake)
        speed = clamp(speed, -PI.toFloat(), PI.toFloat())

        current = (current + speed) % (2.0 * PI).toFloat()
    }
}

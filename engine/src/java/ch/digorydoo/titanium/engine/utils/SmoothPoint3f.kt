package ch.digorydoo.titanium.engine.utils

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.GameTime.Companion.DELTA_TIME

class SmoothPoint3f(
    private val accel: Float = 0.4f,
    private val nearTargetThreshold: Float = 0.027f,
    private val stopThreshold: Float = 0.0008f,
    private val normalBrake: Float = 0.10f,
    private val strongBrake: Float = 0.15f,
) {
    val desired = MutablePoint3f()

    private val _current = MutablePoint3f()
    val current get() = _current as Point3f

    private val speed = MutablePoint3f()

    fun jump() {
        jumpTo(desired)
    }

    private fun jumpTo(pt: Point3f) {
        desired.set(pt)
        _current.set(pt)
        speed.set(0.0f, 0.0f, 0.0f)
    }

    fun animate() {
        val deltaSpeed = accel * DELTA_TIME
        val deltaOffset = desired - _current
        val isNear = deltaOffset.maxAbsComponent() <= nearTargetThreshold

        if (isNear && speed.maxAbsComponent() <= stopThreshold) {
            speed.set(0.0f, 0.0f, 0.0f)
            return
        }

        speed.x += deltaSpeed * deltaOffset.x
        speed.y += deltaSpeed * deltaOffset.y
        speed.z += deltaSpeed * deltaOffset.z

        val brake = if (!isNear) normalBrake else strongBrake
        speed.x -= speed.x * brake
        speed.y -= speed.y * brake
        speed.z -= speed.z * brake

        _current += speed
    }
}

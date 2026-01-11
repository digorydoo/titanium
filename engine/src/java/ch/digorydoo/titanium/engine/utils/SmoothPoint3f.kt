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

    private val _speed = MutablePoint3f()
    val speed: Point3f = _speed

    fun jump() {
        jumpTo(desired)
    }

    private fun jumpTo(pt: Point3f) {
        desired.set(pt)
        _current.set(pt)
        _speed.set(0.0f, 0.0f, 0.0f)
    }

    fun animate() {
        val deltaSpeed = accel * DELTA_TIME
        val deltaOffset = desired - _current
        val isNear = deltaOffset.maxAbsComponent() <= nearTargetThreshold

        if (isNear && _speed.maxAbsComponent() <= stopThreshold) {
            _speed.set(0.0f, 0.0f, 0.0f)
            return
        }

        _speed.x += deltaSpeed * deltaOffset.x
        _speed.y += deltaSpeed * deltaOffset.y
        _speed.z += deltaSpeed * deltaOffset.z

        val brake = if (!isNear) normalBrake else strongBrake
        _speed.x -= _speed.x * brake
        _speed.y -= _speed.y * brake
        _speed.z -= _speed.z * brake

        _current += _speed
    }
}

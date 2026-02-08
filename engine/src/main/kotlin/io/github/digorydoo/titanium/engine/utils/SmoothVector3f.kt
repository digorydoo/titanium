package io.github.digorydoo.titanium.engine.utils

import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.core.GameTime.Companion.DELTA_TIME

class SmoothVector3f(
    private val accel: Float = 0.4f,
    private val nearTargetThreshold: Float = 0.027f,
    private val stopThreshold: Float = 0.0008f,
    private val normalBrake: Float = 0.10f,
    private val strongBrake: Float = 0.15f,
) {
    val desired = MutableVector3f()

    private val _current = MutableVector3f()
    val current get() = _current as Vector3f

    private val _speed = MutableVector3f()
    val speed: Vector3f = _speed

    fun jump() {
        jumpTo(desired)
    }

    private fun jumpTo(vec: Vector3f) {
        desired.set(vec)
        _current.set(vec)
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

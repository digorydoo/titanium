package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.GameTime.Companion.DELTA_TIME
import kotlin.math.sqrt

sealed class RigidBody protected constructor(
    val name: String, // for logging purposes only
    val pos: MutablePoint3f, // mutable object is shared with the caller!
    val elasticity: Float, // 0=totally inelastic, 1=fully elastic
    val friction: Float, // 0=no friction, 1=very high friction
    val mass: Float,
    val gravity: Boolean,
) {
    val speed = MutablePoint3f()

    private val resultingForce = MutablePoint3f()

    val nextPos = MutablePoint3f()
    val nextSpeed = MutablePoint3f()

    private val _speedBeforeCollisions = MutablePoint3f()
    val speedBeforeCollisions: Point3f get() = _speedBeforeCollisions

    private val _normDirOfSpeedBeforeCollisions = MutablePoint3f()
    private var isSpeedBeforeCollisionsSignificant = false

    val normDirOfSpeedBeforeCollisions: Point3f?
        get() = when (isSpeedBeforeCollisionsSignificant) {
            true -> _normDirOfSpeedBeforeCollisions
            false -> null
        }

    fun addForce(x: Float, y: Float, z: Float) {
        resultingForce.x += x
        resultingForce.y += y
        resultingForce.z += z
    }

    fun stopAllMotion() {
        speed.x = 0.0f
        speed.y = 0.0f
        speed.z = 0.0f

        nextSpeed.x = 0.0f
        nextSpeed.y = 0.0f
        nextSpeed.z = 0.0f

        resultingForce.x = 0.0f
        resultingForce.y = 0.0f
        resultingForce.z = 0.0f
    }

    /**
     * Updates nextSpeed and nextPos. Called by GraphicElement at the end of animPhase1.
     */
    fun applyForces() {
        if (gravity) {
            resultingForce.z -= GRAVITY * mass
        }

        if (mass < LARGE_MASS) {
            val ax = resultingForce.x / mass
            val ay = resultingForce.y / mass
            val az = resultingForce.z / mass

            nextSpeed.x = clamp(speed.x + ax * DELTA_TIME, -MAX_SPEED, MAX_SPEED)
            nextSpeed.y = clamp(speed.y + ay * DELTA_TIME, -MAX_SPEED, MAX_SPEED)
            nextSpeed.z = clamp(speed.z + az * DELTA_TIME, -MAX_SPEED, MAX_SPEED)
        }

        val vx = nextSpeed.x
        val vy = nextSpeed.y
        val vz = nextSpeed.z

        nextPos.x = pos.x + vx * DELTA_TIME
        nextPos.y = pos.y + vy * DELTA_TIME
        nextPos.z = pos.z + vz * DELTA_TIME

        _speedBeforeCollisions.x = vx
        _speedBeforeCollisions.y = vy
        _speedBeforeCollisions.z = vz

        val vlen = sqrt(vx * vx + vy * vy + vz * vz)

        if (vlen < MIN_SPEED_FOR_SIGNIFICANT_DIRECTION) {
            isSpeedBeforeCollisionsSignificant = false
        } else {
            _normDirOfSpeedBeforeCollisions.x = vx / vlen
            _normDirOfSpeedBeforeCollisions.y = vy / vlen
            _normDirOfSpeedBeforeCollisions.z = vz / vlen
            isSpeedBeforeCollisionsSignificant = true
        }

        // We applied the force and reset it here. Collisions happen after animPhase1, so if gels add forces in their
        // didCollide, those forces will be applied in the next frame.
        resultingForce.x = 0.0f
        resultingForce.y = 0.0f
        resultingForce.z = 0.0f
    }

    /**
     * Makes nextPos and nextSpeed the new pos and speed, respectively. Called by GraphicElement from animPhase2.
     */
    fun move() {
        pos.set(nextPos)
        speed.set(nextSpeed)
    }

    companion object {
        const val LARGE_MASS = 10000.0f // bodies with a mass >= this will be considered immovable

        private const val MAX_SPEED = 100.0f
        private const val GRAVITY = 9.81f
        private const val MIN_SPEED_FOR_SIGNIFICANT_DIRECTION = 0.0001f // 1 mm/100 per second
    }
}

package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
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
    val force = MutablePoint3f()

    val nextPos = MutablePoint3f()
    val nextSpeed = MutablePoint3f()

    /**
     * Adds a force that brings the body to a certain target speed at a certain acceleration.
     */
    fun addSpeedRelativeForce(targetSpeedX: Float, targetSpeedY: Float, targetSpeedZ: Float, accel: Float) {
        val dx = targetSpeedX - speed.x
        val dy = targetSpeedY - speed.y
        val dz = targetSpeedZ - speed.z

        // Let maxAccel be the acceleration that would directly lead to the target speed.
        val maxAX = dx / DELTA_TIME
        val maxAY = dy / DELTA_TIME
        val maxAZ = dz / DELTA_TIME
        val maxAccel = sqrt((maxAX * maxAX) + (maxAY * maxAY) + (maxAZ * maxAZ))

        if (accel > maxAccel) {
            force.x += maxAX * mass
            force.y += maxAY * mass
            force.z += maxAZ * mass
        } else {
            val dlen = sqrt((dx * dx) + (dy * dy) + (dz * dz))
            force.x += accel * mass * dx / dlen
            force.y += accel * mass * dy / dlen
            force.z += accel * mass * dz / dlen
        }
    }

    /**
     * Updates nextSpeed and nextPos. Called by GraphicElement at the end of animPhase1.
     */
    fun applyForces() {
        if (gravity) {
            force.z -= GRAVITY * mass
        }

        if (mass < LARGE_MASS) {
            val ax = force.x / mass
            val ay = force.y / mass
            val az = force.z / mass

            nextSpeed.x = clamp(speed.x + ax * DELTA_TIME, -MAX_SPEED, MAX_SPEED)
            nextSpeed.y = clamp(speed.y + ay * DELTA_TIME, -MAX_SPEED, MAX_SPEED)
            nextSpeed.z = clamp(speed.z + az * DELTA_TIME, -MAX_SPEED, MAX_SPEED)
        }

        nextPos.x = pos.x + nextSpeed.x * DELTA_TIME
        nextPos.y = pos.y + nextSpeed.y * DELTA_TIME
        nextPos.z = pos.z + nextSpeed.z * DELTA_TIME

        force.set(0, 0, 0)
    }

    /**
     * Makes nextPos and nextSpeed the new pos and speed, respectively. Called by GraphicElement from animPhase2.
     */
    fun move() {
        pos.set(nextPos)
        speed.set(nextSpeed)

        if (force.x != 0.0f || force.y != 0.0f || force.z != 0.0f) {
            Log.warn("RigidBody $this added a force during collision phase!")
            force.set(0, 0, 0)
        }
    }

    companion object {
        const val LARGE_MASS = 10000.0f // bodies with a mass >= this will be considered immovable

        private const val MAX_SPEED = 100.0f
        private const val GRAVITY = 9.81f
    }
}

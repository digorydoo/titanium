package ch.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.GameTime.Companion.DELTA_TIME
import kotlin.math.sqrt

/**
 * This is the base class of all rigid bodies. The role of a RigidBody within the game loop is as follows:
 *    During anim phase 1
 *       pos is the current world coordinates of the body's reference point (which isn't necessarily its centre).
 *          GraphicElement may change pos directly, but should do so only for special actions, e.g. to implement warp
 *          points.
 *       speed is the current speed. GraphicElement may change speed directly, but should do so only for special
 *          actions, e.g. give the player a fixed speed for a more controlled if less physically accurate behaviour.
 *          GraphicElement should call stopAllMotion() if a body is to be fully stopped and forces are to be reset, e.g.
 *          before moving it to a different pos.
 *       nextPos, nextSpeed and speedBeforeCollisions should be ignored during anim phase 1.
 *       Forces may be added. This is the preferred way of changing the body's speed. At the end of anim phase 1,
 *          GraphicElement calls applyForces(), which computes nextPos and nextSpeed based on the resultingForce.
 *    During collision phase
 *       pos and speed are still the old position and speed from anim phase 1. Collision strategies use this as the
 *          location and speed a body originated from.
 *       nextPos and nextSpeed are the preliminary new position and speed, which may be changed by collision strategies.
 *       speedBeforeCollisions is the value of nextSpeed at the start of the collision phase and cannot be changed.
 *       normDirOfSpeedBeforeCollisions is speedBeforeCollisions normalised, and may be null if it was insignificant.
 *       Forces may be added, e.g. by didCollide callbacks for special actions, but these forces will by applied in the
 *          next frame, not in this one.
 *    During anim phase 2
 *       GelLayer calls move() to make nextPos and nextSpeed the new pos and speed, respectively.
 */
sealed class RigidBody protected constructor(
    val name: String, // for logging purposes only
    initialPos: Point3f,
    val mass: Float, // must be > EPSILON
    val elasticity: Float, // 0=totally inelastic, 1=fully elastic
    val friction: Float, // 0=no friction, 1=very high friction
    val gravity: Boolean,
    val collisionRadius: Float, // must cover the entire body plus COLLISION_VICINITY
) {
    val pos = MutablePoint3f(initialPos)
    val speed = MutablePoint3f()

    private val _resultingForce = MutablePoint3f()
    val resultingForce: Point3f get() = _resultingForce

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
        _resultingForce.x += x
        _resultingForce.y += y
        _resultingForce.z += z
    }

    fun stopAllMotion() {
        speed.x = 0.0f
        speed.y = 0.0f
        speed.z = 0.0f

        nextSpeed.x = 0.0f
        nextSpeed.y = 0.0f
        nextSpeed.z = 0.0f

        _speedBeforeCollisions.x = 0.0f
        _speedBeforeCollisions.y = 0.0f
        _speedBeforeCollisions.z = 0.0f
        isSpeedBeforeCollisionsSignificant = false

        _resultingForce.x = 0.0f
        _resultingForce.y = 0.0f
        _resultingForce.z = 0.0f
    }

    /**
     * Updates nextSpeed and nextPos. Called by GraphicElement at the end of animPhase1.
     */
    fun applyForces() {
        if (gravity) {
            _resultingForce.z -= GRAVITY * mass
        }

        if (mass < LARGE_MASS) {
            val ax = _resultingForce.x / mass
            val ay = _resultingForce.y / mass
            val az = _resultingForce.z / mass

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
        _resultingForce.x = 0.0f
        _resultingForce.y = 0.0f
        _resultingForce.z = 0.0f
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
        const val COLLISION_VICINITY = 0.25f

        private const val MAX_SPEED = 100.0f
        private const val GRAVITY = 9.81f
        private const val MIN_SPEED_FOR_SIGNIFICANT_DIRECTION = 0.0001f // 1 mm/100 per second
    }
}

package ch.digorydoo.titanium.engine.physics.collide_regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

internal abstract class CollisionStrategy<B1: RigidBody, B2: RigidBody> {
    /**
     * Called by CollisionManager to check if two bodies collide at their nextPos. If they collide and true was passed
     * to canBounce, they are also separated, and the next speeds are computed.
     */
    fun checkAndBounceIfNeeded(body1: B1, body2: B2, canBounce: Boolean, outHit: MutableHitResult): Boolean {
        val didCollide: Boolean

        try {
            val p1 = body1.nextPos
            val p2 = body2.nextPos
            didCollide = check(body1, p1.x, p1.y, p1.z, body2, p2.x, p2.y, p2.z, outHit)
            if (didCollide && canBounce) bounce(body1, body2, outHit)
        } finally {
            done()
        }

        return didCollide
    }

    /**
     * Method that may be overridden by strategies to clear any internal structures after checks and bouncing are done.
     */
    protected open fun done() {}

    /**
     * Method that needs to be implemented by the strategy to check if the two bodies collide at the given position. If
     * outHit is not null, the method must update it when a collision is detected. When there is no collision and the
     * method returns with false, the values of outHit may or may not have been modified, and the caller should ignore
     * them.
     * @return true if the two collide; false if they don't
     */
    protected abstract fun check(
        body1: B1,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body2: B2,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        outHit: MutableHitResult?,
    ): Boolean

    /**
     * Method that needs to be implemented by the strategy to separate the two bodies after a collision and properly
     * assign new values of nextSpeed. The implementation should call separate().
     */
    protected abstract fun bounce(body1: B1, body2: B2, hit: HitResult)

    /**
     * This method should be called from the implementation of bounce() to separate the two bodies such that they no
     * longer collide. Each body must only be moved along the negative of the direction of its speed before the
     * collision, because any other direction might lead to new collisions with nearby bodies. Bodies whose speed is
     * zero or whose direction of speed can't have led to the collision, are not to be moved. Bodies of LARGE_MASS that
     * are in motion may be moved (otherwise the smaller mass would have to retreat all the way, moving it beyond its
     * original position, leading to new possible collisions).
     */
    protected fun separate(body1: B1, body2: B2, hit: HitResult) {
        // Compute the distance between the centres of the bodies at their original position

        val orig1PosX = body1.pos.x
        val orig1PosY = body1.pos.y
        val orig1PosZ = body1.pos.z

        val orig2PosX = body2.pos.x
        val orig2PosY = body2.pos.y
        val orig2PosZ = body2.pos.z

        val d12X = orig2PosX - orig1PosX
        val d12Y = orig2PosY - orig1PosY
        val d12Z = orig2PosZ - orig1PosZ
        val d12Len = sqrt(d12X * d12X + d12Y * d12Y + d12Z * d12Z)

        if (d12Len <= EPSILON) {
            Log.warn("Forcing $body1 and $body2 apart, because their original position was too close")
            forceApartInRandomDir(body1, body2, hit)
            return
        }

        val normDist12X = d12X / d12Len
        val normDist12Y = d12Y / d12Len
        val normDist12Z = d12Z / d12Len

        // Check if the two bodies are stuck, i.e. collide at their original position

        if (check(body1, orig1PosX, orig1PosY, orig1PosZ, body2, orig2PosX, orig2PosY, orig2PosZ, null)) {
            Log.warn("Forcing $body1 and $body2 apart, because they are colliding at their original position")
            forceApart(body1, body2, -normDist12X, -normDist12Y, -normDist12Z, hit)
            return
        }

        // Compute how much the speed of each body contributed to the direction of the collision

        val normSpeed1 = body1.normDirOfSpeedBeforeCollisions
        val speed1TowardsBody2 = when {
            normSpeed1 == null -> 0.0f // body1 did not move, or its speed is insignificant
            else -> normSpeed1.x * normDist12X + normSpeed1.y * normDist12Y + normSpeed1.z * normDist12Z
        }

        val normSpeed2 = body2.normDirOfSpeedBeforeCollisions
        val speed2TowardsBody1 = when {
            normSpeed2 == null -> 0.0f
            else -> normSpeed2.x * (-normDist12X) + normSpeed2.y * (-normDist12Y) + normSpeed2.z * (-normDist12Z)
        }

        // Determine whether to move each body

        val move1 = if (speed1TowardsBody2 > 0.0f) 1.0f else 0.0f
        val move2 = if (speed2TowardsBody1 > 0.0f) 1.0f else 0.0f

        if (move1 == 0.0f && move2 == 0.0f) {
            // None of the two bodies have a significant share of the motion towards each-other.
            Log.warn("Forcing $body1 and $body2 apart, because none seems to be moving towards the other")
            forceApart(body1, body2, -normDist12X, -normDist12Y, -normDist12Z, hit)
            return
        }

        // Compute the position where we move back each body according to their weight.

        var forwardX1 = body1.nextPos.x
        var forwardY1 = body1.nextPos.y
        var forwardZ1 = body1.nextPos.z

        var backwardX1 = forwardX1 + move1 * (orig1PosX - forwardX1)
        var backwardY1 = forwardY1 + move1 * (orig1PosY - forwardY1)
        var backwardZ1 = forwardZ1 + move1 * (orig1PosZ - forwardZ1)

        var forwardX2 = body2.nextPos.x
        var forwardY2 = body2.nextPos.y
        var forwardZ2 = body2.nextPos.z

        var backwardX2 = forwardX2 + move2 * (orig2PosX - forwardX2)
        var backwardY2 = forwardY2 + move2 * (orig2PosY - forwardY2)
        var backwardZ2 = forwardZ2 + move2 * (orig2PosZ - forwardZ2)

        // Check if the bodies collide at their backward position.

        if (check(body1, backwardX1, backwardY1, backwardZ1, body2, backwardX2, backwardY2, backwardZ2, null)) {
            // This may indicate a bug, or just floating-point inaccuracies.
            Log.warn("Forcing $body1 and $body2 apart, because they collide at their backward position")
            forceApart(body1, body2, -normDist12X, -normDist12Y, -normDist12Z, hit)
            return
        }

        // Now do a binary search to find a position close to just before they collided.

        var toGo = MAX_SEPARATION_ITERATIONS

        do {
            val dx1 = abs(forwardX1 - backwardX1)
            val dy1 = abs(forwardY1 - backwardY1)
            val dz1 = abs(forwardZ1 - backwardZ1)

            val dx2 = abs(forwardX2 - backwardX2)
            val dy2 = abs(forwardY2 - backwardY2)
            val dz2 = abs(forwardZ2 - backwardZ2)

            val maxAbsDiff = max(dx1, max(dy1, max(dz1, max(dx2, max(dy2, dz2)))))
            if (maxAbsDiff <= SEPARATION_ACCURACY) break

            val midX1 = 0.5f * (forwardX1 + backwardX1)
            val midY1 = 0.5f * (forwardY1 + backwardY1)
            val midZ1 = 0.5f * (forwardZ1 + backwardZ1)

            val midX2 = 0.5f * (forwardX2 + backwardX2)
            val midY2 = 0.5f * (forwardY2 + backwardY2)
            val midZ2 = 0.5f * (forwardZ2 + backwardZ2)

            val midCollides = check(body1, midX1, midY1, midZ1, body2, midX2, midY2, midZ2, null)

            if (midCollides) {
                forwardX1 = midX1
                forwardY1 = midY1
                forwardZ1 = midZ1

                forwardX2 = midX2
                forwardY2 = midY2
                forwardZ2 = midZ2
            } else {
                backwardX1 = midX1
                backwardY1 = midY1
                backwardZ1 = midZ1

                backwardX2 = midX2
                backwardY2 = midY2
                backwardZ2 = midZ2
            }
        } while (--toGo > 0)

        if (move1 > 0.0f) {
            body1.nextPos.x = backwardX1
            body1.nextPos.y = backwardY1
            body1.nextPos.z = backwardZ1
        }

        if (move2 > 0.0f) {
            body2.nextPos.x = backwardX2
            body2.nextPos.y = backwardY2
            body2.nextPos.z = backwardZ2
        }
    }

    /**
     * Changes the speeds v1 and v2 according to friction. The normDir must be the normal on the tangential plane at
     * the point of touch and must point from body1 to body2. This function ignores the size area where the two bodies
     * touch, and computes the friction only from the masses and the speeds along the surface (i.e. perpendicular to
     * normDir).
     */
    protected fun applyFriction(
        m1: Float,
        v1: MutablePoint3f,
        friction1: Float,
        m2: Float,
        v2: MutablePoint3f,
        friction2: Float,
        normDir12X: Float,
        normDir12Y: Float,
        normDir12Z: Float,
    ) {
        val friction = 1.0f - (1.0f - friction1) * (1.0f - friction2)
        if (friction <= 0.0f) return

        val v1dotn = v1.x * normDir12X + v1.y * normDir12Y + v1.z * normDir12Z
        val v1parallelX = normDir12X * v1dotn
        val v1parallelY = normDir12Y * v1dotn
        val v1parallelZ = normDir12Z * v1dotn

        var v1perpendX = v1.x - v1parallelX
        var v1perpendY = v1.y - v1parallelY
        var v1perpendZ = v1.z - v1parallelZ

        val v2dotn = v2.x * normDir12X + v2.y * normDir12Y + v2.z * normDir12Z
        val v2parallelX = normDir12X * v2dotn
        val v2parallelY = normDir12Y * v2dotn
        val v2parallelZ = normDir12Z * v2dotn

        var v2perpendX = v2.x - v2parallelX
        var v2perpendY = v2.y - v2parallelY
        var v2perpendZ = v2.z - v2parallelZ

        val vparallelDx = v1parallelX - v2parallelX
        val vparallelDy = v1parallelY - v2parallelY
        val vparallelDz = v1parallelZ - v2parallelZ

        val vperpendDx = v1perpendX - v2perpendX
        val vperpendDy = v1perpendY - v2perpendY
        val vperpendDz = v1perpendZ - v2perpendZ
        val vperpendDiff = sqrt(vperpendDx * vperpendDx + vperpendDy * vperpendDy + vperpendDz * vperpendDz)

        val vfricX = vparallelDx * friction
        val vfricY = vparallelDy * friction
        val vfricZ = vparallelDz * friction
        val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY + vfricZ * vfricZ)

        if (m1 >= LARGE_MASS) {
            if (vfricLen >= vperpendDiff) {
                v2perpendX += vperpendDx
                v2perpendY += vperpendDy
                v2perpendZ += vperpendDz
            } else {
                v2perpendX += vfricLen * vperpendDx / vperpendDiff
                v2perpendY += vfricLen * vperpendDy / vperpendDiff
                v2perpendZ += vfricLen * vperpendDz / vperpendDiff
            }
        } else if (m2 >= LARGE_MASS) {
            if (vfricLen >= vperpendDiff) {
                v1perpendX -= vperpendDx
                v1perpendY -= vperpendDy
                v1perpendZ -= vperpendDz
            } else {
                v1perpendX -= vfricLen * vperpendDx / vperpendDiff
                v1perpendY -= vfricLen * vperpendDy / vperpendDiff
                v1perpendZ -= vfricLen * vperpendDz / vperpendDiff
            }
        } else {
            val totalMass = m1 + m2

            if (vfricLen >= vperpendDiff) {
                v1perpendX -= m2 * vperpendDx / totalMass
                v1perpendY -= m2 * vperpendDy / totalMass
                v1perpendZ -= m2 * vperpendDz / totalMass

                v2perpendX += m1 * vperpendDx / totalMass
                v2perpendY += m1 * vperpendDy / totalMass
                v2perpendZ += m1 * vperpendDz / totalMass
            } else {
                val vdfricX = vfricLen * vperpendDx / vperpendDiff
                val vdfricY = vfricLen * vperpendDy / vperpendDiff
                val vdfricZ = vfricLen * vperpendDz / vperpendDiff

                v1perpendX -= m2 * vdfricX / totalMass
                v1perpendY -= m2 * vdfricY / totalMass
                v1perpendZ -= m2 * vdfricZ / totalMass

                v2perpendX += m1 * vdfricX / totalMass
                v2perpendY += m1 * vdfricY / totalMass
                v2perpendZ += m1 * vdfricZ / totalMass
            }
        }

        v1.x = v1perpendX + v1parallelX
        v1.y = v1perpendY + v1parallelY
        v1.z = v1perpendZ + v1parallelZ

        v2.x = v2perpendX + v2parallelX
        v2.y = v2perpendY + v2parallelY
        v2.z = v2perpendZ + v2parallelZ
    }

    /**
     * Strategies need to implement this as a fallback for a failed attempt of separation. The given normDir points in
     * the direction to move body1.nextPos; body2.nextPos should be moved in the opposite direction.
     */
    abstract fun forceApart(body1: B1, body2: B2, normDirX1: Float, normDirY1: Float, normDirZ1: Float, hit: HitResult)

    /**
     * Alternative to forceApart() that may be used when a good direction of separation is unknown.
     */
    private fun forceApartInRandomDir(body1: B1, body2: B2, hit: HitResult) {
        val r = Random.nextFloat()
        forceApart(body1, body2, cos(r), sin(r), 0.0f, hit)
    }

    companion object {
        private const val MAX_SEPARATION_ITERATIONS = 4 // must be small to keep this efficient
        private const val SEPARATION_ACCURACY = 0.0005f // 5 mm/10
    }
}

package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.collision_strategy.CollisionStrategy
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

internal class CollisionHelper<B1: RigidBody, B2: RigidBody> {
    /**
     * This function may be called from the implementation of bounce() to separate the two bodies such that they no
     * longer collide. It uses a binary search algorithm, which is less efficient and also less accurate than a direct
     * mathematical approach, therefore use this function only when a direct mathematical approach is difficult, or as
     * a preliminary implementation for a newly implemented body.
     */
    fun separateByBinarySearch(body1: B1, body2: B2, hitNormal12: Point3f, strategy: CollisionStrategy<B1, B2>) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos

        val pdx = p2.x - p1.x
        val pdy = p2.y - p1.y
        val pdz = p2.z - p1.z

        val distanceAlongHitNormal = pdx * hitNormal12.x + pdy * hitNormal12.y + pdz * hitNormal12.z
        var moveBy = body1.enclosingRadius + body2.enclosingRadius + EPSILON - distanceAlongHitNormal

        if (moveBy <= 0.0f) {
            Log.warn("separateByBinarySearch called, but bodies seem to be far enough already")
            moveBy = EPSILON
        }

        var backX1: Float
        var backY1: Float
        var backZ1: Float

        var backX2: Float
        var backY2: Float
        var backZ2: Float

        var toGo = MAX_BINARY_SEARCH_ITERATIONS

        do {
            when {
                body1.mass < LARGE_MASS -> when {
                    body2.mass < LARGE_MASS -> {
                        val move1By = moveBy * body2.mass / (body1.mass + body2.mass)
                        val move2By = moveBy - move1By

                        backX1 = p1.x - hitNormal12.x * move1By
                        backY1 = p1.y - hitNormal12.y * move1By
                        backZ1 = p1.z - hitNormal12.z * move1By

                        backX2 = p2.x + hitNormal12.x * move2By
                        backY2 = p2.y + hitNormal12.y * move2By
                        backZ2 = p2.z + hitNormal12.z * move2By
                    }
                    else -> {
                        backX1 = p1.x - hitNormal12.x * moveBy
                        backY1 = p1.y - hitNormal12.y * moveBy
                        backZ1 = p1.z - hitNormal12.z * moveBy

                        backX2 = p2.x
                        backY2 = p2.y
                        backZ2 = p2.z
                    }
                }
                body2.mass < LARGE_MASS -> {
                    backX1 = p1.x
                    backY1 = p1.y
                    backZ1 = p1.z

                    backX2 = p2.x + hitNormal12.x * moveBy
                    backY2 = p2.y + hitNormal12.y * moveBy
                    backZ2 = p2.z + hitNormal12.z * moveBy
                }
                else -> {
                    Log.warn("Separating $body1 from $body2 failed, because both bodies are LARGE_MASS")
                    return
                }
            }

            if (!strategy.check(body1, backX1, backY1, backZ1, body2, backX2, backY2, backZ2, null)) {
                // The computed back position does not collide, that's good.
                break
            } else {
                // This should not happen except in a rare case when the hitNormal was set to a random direction,
                // because the bodies are in the exact same spot, and the random direction was chosen unluckily.
                Log.warn(
                    arrayOf(
                        "Separating $body1 from $body2 failed. Are enclosingRadius and hitNormal correct?",
                        "   enclosingRadius1=${body1.enclosingRadius}",
                        "   enclosingRadius2=${body2.enclosingRadius}",
                        "   hitNormal12=$hitNormal12",
                        "   p1=$p1",
                        "   p2=$p2",
                        "   moveBy=$moveBy",
                        "   retried=${MAX_BINARY_SEARCH_ITERATIONS - toGo}",
                    ).joinToString("\n")
                )
                moveBy = moveBy * 2.0f + 0.2f
            }
        } while (--toGo > 0)

        toGo = MAX_BINARY_SEARCH_ITERATIONS

        var forwardX1 = p1.x
        var forwardY1 = p1.y
        var forwardZ1 = p1.z

        var forwardX2 = p2.x
        var forwardY2 = p2.y
        var forwardZ2 = p2.z

        do {
            // Invariant: Bodies collide at their forward position, and they don't collide at their back position.

            val dx1 = abs(forwardX1 - backX1)
            val dy1 = abs(forwardY1 - backY1)
            val dz1 = abs(forwardZ1 - backZ1)

            val dx2 = abs(forwardX2 - backX2)
            val dy2 = abs(forwardY2 - backY2)
            val dz2 = abs(forwardZ2 - backZ2)

            val maxAbsDiff = max(dx1, max(dy1, max(dz1, max(dx2, max(dy2, dz2)))))
            if (maxAbsDiff <= SEPARATION_ACCURACY) break

            val midX1 = 0.5f * (forwardX1 + backX1)
            val midY1 = 0.5f * (forwardY1 + backY1)
            val midZ1 = 0.5f * (forwardZ1 + backZ1)

            val midX2 = 0.5f * (forwardX2 + backX2)
            val midY2 = 0.5f * (forwardY2 + backY2)
            val midZ2 = 0.5f * (forwardZ2 + backZ2)

            val midCollides = strategy.check(body1, midX1, midY1, midZ1, body2, midX2, midY2, midZ2, null)

            if (midCollides) {
                forwardX1 = midX1
                forwardY1 = midY1
                forwardZ1 = midZ1

                forwardX2 = midX2
                forwardY2 = midY2
                forwardZ2 = midZ2
            } else {
                backX1 = midX1
                backY1 = midY1
                backZ1 = midZ1

                backX2 = midX2
                backY2 = midY2
                backZ2 = midZ2
            }
        } while (--toGo > 0)

        body1.nextPos.x = backX1
        body1.nextPos.y = backY1
        body1.nextPos.z = backZ1

        body2.nextPos.x = backX2
        body2.nextPos.y = backY2
        body2.nextPos.z = backZ2

        // Do a sanity check for now. May be removed later.
        if (strategy.check(body1, backX1, backY1, backZ1, body2, backX2, backY2, backZ2, null)) {
            Log.error("separateByBinarySearch left the bodies $body1 and $body2 still colliding!")
        }
    }

    /**
     * Changes the bodies' nextSpeed according to friction. The normDir must be the normal on the tangential plane at
     * the point of touch and must point from body1 to body2. This function ignores the size area where the two bodies
     * touch, and computes the friction only from the masses and the speeds along the surface (i.e. perpendicular to
     * normDir).
     */
    fun applyFriction(body1: B1, body2: B2, normDir12X: Float, normDir12Y: Float, normDir12Z: Float) {
        val m1 = body1.mass
        val m2 = body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val friction = 1.0f - (1.0f - body1.friction) * (1.0f - body2.friction)
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
     * Given that body1 and body2 collide at a point with a common tangential plane, this function computes the
     * nextSpeed of each body after the collision. Friction along the plane is not taken into account and should be
     * handled before calling this function. Change of angular momentum is not computed. The tangential plane must be
     * given by its normal, which must point in the direction from body1 to body2.
     */
    fun bounceAtPlane(body1: B1, body2: B2, normDir12X: Float, normDir12Y: Float, normDir12Z: Float) {
        val m1 = body1.mass
        val m2 = body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val elasticity = body1.elasticity * body2.elasticity

        val v1dotn = v1.x * normDir12X + v1.y * normDir12Y + v1.z * normDir12Z
        val v1parallelX = normDir12X * v1dotn
        val v1parallelY = normDir12Y * v1dotn
        val v1parallelZ = normDir12Z * v1dotn

        val v2dotn = v2.x * normDir12X + v2.y * normDir12Y + v2.z * normDir12Z
        val v2parallelX = normDir12X * v2dotn
        val v2parallelY = normDir12Y * v2dotn
        val v2parallelZ = normDir12Z * v2dotn

        val vparallelDx = v1parallelX - v2parallelX
        val vparallelDy = v1parallelY - v2parallelY
        val vparallelDz = v1parallelZ - v2parallelZ

        if (m1 >= LARGE_MASS) {
            val v2perpendX = v2.x - v2parallelX
            val v2perpendY = v2.y - v2parallelY
            val v2perpendZ = v2.z - v2parallelZ

            v2.x = v2perpendX + v1parallelX + vparallelDx * elasticity
            v2.y = v2perpendY + v1parallelY + vparallelDy * elasticity
            v2.z = v2perpendZ + v1parallelZ + vparallelDz * elasticity
        } else if (m2 >= LARGE_MASS) {
            val v1perpendX = v1.x - v1parallelX
            val v1perpendY = v1.y - v1parallelY
            val v1perpendZ = v1.z - v1parallelZ

            v1.x = v1perpendX + v2parallelX - vparallelDx * elasticity
            v1.y = v1perpendY + v2parallelY - vparallelDy * elasticity
            v1.z = v1perpendZ + v2parallelZ - vparallelDz * elasticity
        } else {
            val v1perpendX = v1.x - v1parallelX
            val v1perpendY = v1.y - v1parallelY
            val v1perpendZ = v1.z - v1parallelZ

            val v2perpendX = v2.x - v2parallelX
            val v2perpendY = v2.y - v2parallelY
            val v2perpendZ = v2.z - v2parallelZ

            val totalMass = m1 + m2

            val sx = v1parallelX * m1 + v2parallelX * m2
            val sy = v1parallelY * m1 + v2parallelY * m2
            val sz = v1parallelZ * m1 + v2parallelZ * m2

            v1.x = v1perpendX + (sx - vparallelDx * elasticity * m2) / totalMass
            v1.y = v1perpendY + (sy - vparallelDy * elasticity * m2) / totalMass
            v1.z = v1perpendZ + (sz - vparallelDz * elasticity * m2) / totalMass

            v2.x = v2perpendX + (sx + vparallelDx * elasticity * m1) / totalMass
            v2.y = v2perpendY + (sy + vparallelDy * elasticity * m1) / totalMass
            v2.z = v2perpendZ + (sz + vparallelDz * elasticity * m1) / totalMass
        }
    }

    /**
     * Same as bounceAtPlane, but optimised for the case when normDir12Z is known to be 0
     */
    fun bounceAtVerticalPlane(body1: B1, body2: B2, normDir12X: Float, normDir12Y: Float) {
        val m1 = body1.mass
        val m2 = body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val elasticity = body1.elasticity * body2.elasticity

        val v1dotn = v1.x * normDir12X + v1.y * normDir12Y
        val v1parallelX = normDir12X * v1dotn
        val v1parallelY = normDir12Y * v1dotn

        val v2dotn = v2.x * normDir12X + v2.y * normDir12Y
        val v2parallelX = normDir12X * v2dotn
        val v2parallelY = normDir12Y * v2dotn

        val vparallelDx = v1parallelX - v2parallelX
        val vparallelDy = v1parallelY - v2parallelY

        if (m1 >= LARGE_MASS) {
            val v2perpendX = v2.x - v2parallelX
            val v2perpendY = v2.y - v2parallelY

            v2.x = v2perpendX + v1parallelX + vparallelDx * elasticity
            v2.y = v2perpendY + v1parallelY + vparallelDy * elasticity
        } else if (m2 >= LARGE_MASS) {
            val v1perpendX = v1.x - v1parallelX
            val v1perpendY = v1.y - v1parallelY

            v1.x = v1perpendX + v2parallelX - vparallelDx * elasticity
            v1.y = v1perpendY + v2parallelY - vparallelDy * elasticity
        } else {
            val v1perpendX = v1.x - v1parallelX
            val v1perpendY = v1.y - v1parallelY

            val v2perpendX = v2.x - v2parallelX
            val v2perpendY = v2.y - v2parallelY

            val totalMass = m1 + m2

            val sx = v1parallelX * m1 + v2parallelX * m2
            val sy = v1parallelY * m1 + v2parallelY * m2

            v1.x = v1perpendX + (sx - vparallelDx * elasticity * m2) / totalMass
            v1.y = v1perpendY + (sy - vparallelDy * elasticity * m2) / totalMass

            v2.x = v2perpendX + (sx + vparallelDx * elasticity * m1) / totalMass
            v2.y = v2perpendY + (sy + vparallelDy * elasticity * m1) / totalMass
        }
    }

    companion object {
        private const val MAX_BINARY_SEARCH_ITERATIONS = 4 // must be small to keep this efficient
        private const val SEPARATION_ACCURACY = 0.0005f // 5 mm/10
    }
}

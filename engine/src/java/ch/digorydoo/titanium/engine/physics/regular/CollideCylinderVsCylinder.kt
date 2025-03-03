package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt

internal class CollideCylinderVsCylinder: CollisionStrategy<FixedCylinderBody, FixedCylinderBody>() {
    override fun checkNextPos(body1: FixedCylinderBody, body2: FixedCylinderBody, outHitPt: MutablePoint3f) =
        check(
            cx1 = body1.nextPos.x,
            cy1 = body1.nextPos.y,
            cz1 = body1.nextPos.z + body1.zOffset,
            v1x = body1.nextSpeed.x,
            v1y = body1.nextSpeed.y,
            v1z = body1.nextSpeed.z,
            r1 = body1.radius,
            h1 = body1.height,
            cx2 = body2.nextPos.x,
            cy2 = body2.nextPos.y,
            cz2 = body2.nextPos.z + body2.zOffset,
            v2x = body2.nextSpeed.x,
            v2y = body2.nextSpeed.y,
            v2z = body2.nextSpeed.z,
            r2 = body2.radius,
            h2 = body2.height,
            outHitPt,
        )

    private fun check(
        // Cylinder 1
        cx1: Float,
        cy1: Float,
        cz1: Float,
        v1x: Float,
        v1y: Float,
        v1z: Float,
        r1: Float,
        h1: Float,
        // Cylinder 2
        cx2: Float,
        cy2: Float,
        cz2: Float,
        v2x: Float,
        v2y: Float,
        v2z: Float,
        r2: Float,
        h2: Float,
        // out
        outHitPt: MutablePoint3f?,
    ): Boolean {
        if (cz1 + h1 * 0.5f < cz2 - h2 * 0.5f) return false
        if (cz1 - h1 * 0.5f > cz2 + h2 * 0.5f) return false

        val dx = cx2 - cx1
        val dy = cy2 - cy1
        val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val rsum = r1 + r2
        if (dsqr > rsum * rsum) return false

        if (outHitPt != null) {
            val smallr = min(r1, r2)
            val vertical = dsqr <= smallr * smallr || abs(v1z - v2z) > max(abs(v1x - v2x), abs(v1y - v2y))

            val d = sqrt(dsqr) // always >= 0

            if (d <= EPSILON || vertical) {
                if (cz1 < cz2) {
                    // c1's top plane collides with c2's bottom plane.
                    outHitPt.x = cx1
                    outHitPt.y = cy1
                    outHitPt.z = cz1 + h1 * 0.5f
                } else {
                    // c1's bottom plane collides with c2's top plane.
                    outHitPt.x = cx1
                    outHitPt.y = cy1
                    outHitPt.z = cz1 - h1 * 0.5f
                }
            } else {
                // The two circles collide within the XY plane
                outHitPt.x = cx1 + r1 * (dx / d)
                outHitPt.y = cy1 + r1 * (dy / d)
                outHitPt.z = cz1
            }
        }

        return true
    }

    override fun bounce(body1: FixedCylinderBody, body2: FixedCylinderBody) {
        val vertical: Boolean

        if (body1.mass < LARGE_MASS && body1.mass <= body2.mass) {
            vertical = separate(body1, body2) // updates body1.nextPos
        } else if (body2.mass < LARGE_MASS) {
            vertical = separate(body2, body1) // updates body2.nextPos
        } else {
            Log.warn("Cannot separate $body1 from $body2")
            return
        }

        val elasticity = body1.elasticity * body2.elasticity

        val m1 = if (body1.mass <= EPSILON) EPSILON else body1.mass
        val m2 = if (body2.mass <= EPSILON) EPSILON else body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y
        val p1z = body1.nextPos.z + body1.zOffset

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y
        val p2z = body2.nextPos.z + body2.zOffset

        val pdx = p2x - p1x
        val pdy = p2y - p1y
        val p2DDistance = sqrt(pdx * pdx + pdy * pdy)

        val normDir12X = pdx / p2DDistance
        val normDir12Y = pdy / p2DDistance

        // This may change v1 and/or v2
        applyFriction(
            m1 = m1,
            v1 = v1,
            m2 = m2,
            v2 = v2,
            normDir12X = normDir12X,
            normDir12Y = normDir12Y,
            vertical = vertical,
            friction = 1.0f - (1.0f - body1.friction) * (1.0f - body2.friction),
        )

        if (vertical) {
            // We treat this like bouncing two planes against each-other.

            val v1z = v1.z
            val v2z = v2.z
            val vparallelDz = v1z - v2z
            val expectedSign = if (p1z < p2z) 1.0f else -1.0f

            if (sign(vparallelDz) != expectedSign) {
                // The two objects are separating
                return
            }

            if (m1 >= LARGE_MASS) {
                v2.z = v1z + vparallelDz * elasticity
            } else if (m2 >= LARGE_MASS) {
                v1.z = v2z - vparallelDz * elasticity
            } else {
                val totalMass = m1 + m2
                val sz = v1z * m1 + v2z * m2
                v1.z = (sz - vparallelDz * elasticity * m2) / totalMass
                v2.z = (sz + vparallelDz * elasticity * m1) / totalMass
            }
        } else {
            // We treat this like bouncing a circle off another circle in the XY plane.

            val v1dotn = v1.x * normDir12X + v1.y * normDir12Y
            val v1parallelX = normDir12X * v1dotn
            val v1parallelY = normDir12Y * v1dotn

            val v2dotn = v2.x * normDir12X + v2.y * normDir12Y
            val v2parallelX = normDir12X * v2dotn
            val v2parallelY = normDir12Y * v2dotn

            val vparallelDx = v1parallelX - v2parallelX
            val vparallelDy = v1parallelY - v2parallelY

            if (m1 >= LARGE_MASS) {
                var v2perpendX = v2.x - v2parallelX
                var v2perpendY = v2.y - v2parallelY
                var v2perpendZ = v2.z // v2parallelZ is 0

                v2.x = v2perpendX + v1parallelX + vparallelDx * elasticity
                v2.y = v2perpendY + v1parallelY + vparallelDy * elasticity
                v2.z = v2perpendZ // v2parallelZ is 0
            } else if (m2 >= LARGE_MASS) {
                var v1perpendX = v1.x - v1parallelX
                var v1perpendY = v1.y - v1parallelY
                var v1perpendZ = v1.z // v1parallelZ is 0

                v1.x = v1perpendX + v2parallelX - vparallelDx * elasticity
                v1.y = v1perpendY + v2parallelY - vparallelDy * elasticity
                v1.z = v1perpendZ // v1parallelZ is 0
            } else {
                var v1perpendX = v1.x - v1parallelX
                var v1perpendY = v1.y - v1parallelY
                var v1perpendZ = v1.z // v1parallelZ is 0

                var v2perpendX = v2.x - v2parallelX
                var v2perpendY = v2.y - v2parallelY
                var v2perpendZ = v2.z // v2parallelZ is 0

                val totalMass = m1 + m2

                val sx = v1parallelX * m1 + v2parallelX * m2
                val sy = v1parallelY * m1 + v2parallelY * m2

                v1.x = v1perpendX + (sx - vparallelDx * elasticity * m2) / totalMass
                v1.y = v1perpendY + (sy - vparallelDy * elasticity * m2) / totalMass
                v1.z = v1perpendZ // v1parallelZ is 0

                v2.x = v2perpendX + (sx + vparallelDx * elasticity * m1) / totalMass
                v2.y = v2perpendY + (sy + vparallelDy * elasticity * m1) / totalMass
                v2.z = v2perpendZ // v2parallelZ is 0
            }
        }
    }

    private fun applyFriction(
        m1: Float,
        v1: MutablePoint3f,
        m2: Float,
        v2: MutablePoint3f,
        vertical: Boolean,
        normDir12X: Float,
        normDir12Y: Float,
        friction: Float,
    ) {
        if (friction <= 0.0f) return

        if (vertical) {
            val v1z = v1.z
            val v2z = v2.z
            val vparallelDz = v1z - v2z

            if (m1 >= LARGE_MASS) {
                val vfricZ = vparallelDz * friction
                val v2pLen = sqrt(v2.x * v2.x + v2.y * v2.y)

                if (vfricZ >= v2pLen) {
                    v2.x = 0.0f
                    v2.y = 0.0f
                } else {
                    v2.x -= vfricZ * v2.x / v2pLen
                    v2.y -= vfricZ * v2.y / v2pLen
                }
            } else if (m2 >= LARGE_MASS) {
                val vfricZ = vparallelDz * friction
                val v1pLen = sqrt(v1.x * v1.x + v1.y * v1.y)

                if (vfricZ >= v1pLen) {
                    v1.x = 0.0f
                    v1.y = 0.0f
                } else {
                    v1.x -= vfricZ * v1.x / v1pLen
                    v1.y -= vfricZ * v1.y / v1pLen
                }
            } else {
                val vfricZ = vparallelDz * friction

                val v1pLen = sqrt(v1.x * v1.x + v1.y * v1.y)
                val v2pLen = sqrt(v2.x * v2.x + v2.y * v2.y)

                if (vfricZ >= v1pLen) {
                    v1.x = 0.0f
                    v1.y = 0.0f
                } else {
                    v1.x -= vfricZ * v1.x / v1pLen
                    v1.y -= vfricZ * v1.y / v1pLen
                }

                if (vfricZ >= v2pLen) {
                    v2.x = 0.0f
                    v2.y = 0.0f
                } else {
                    v2.x -= vfricZ * v2.x / v2pLen
                    v2.y -= vfricZ * v2.y / v2pLen
                }
            }
        } else {
            // We treat this like bouncing a circle off another circle in the XY plane.

            val v1dotn = v1.x * normDir12X + v1.y * normDir12Y
            val v1parallelX = normDir12X * v1dotn
            val v1parallelY = normDir12Y * v1dotn
            // val v1parallelZ = 0.0f in the XY plane

            var v1perpendX = v1.x - v1parallelX
            var v1perpendY = v1.y - v1parallelY
            var v1perpendZ = v1.z // v1parallelZ is 0

            val v2dotn = v2.x * normDir12X + v2.y * normDir12Y
            val v2parallelX = normDir12X * v2dotn
            val v2parallelY = normDir12Y * v2dotn
            // val v2parallelZ = 0.0f in the XY plane

            var v2perpendX = v2.x - v2parallelX
            var v2perpendY = v2.y - v2parallelY
            var v2perpendZ = v2.z // v2parallelZ is 0

            val vparallelDx = v1parallelX - v2parallelX
            val vparallelDy = v1parallelY - v2parallelY
            // val vparallelDz = 0.0f

            val vfricX = vparallelDx * friction
            val vfricY = vparallelDy * friction
            val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY)

            val vperpendDx = v1perpendX - v2perpendX
            val vperpendDy = v1perpendY - v2perpendY
            val vperpendDz = v1perpendZ - v2perpendZ
            val vperpendDiff = sqrt(vperpendDx * vperpendDx + vperpendDy * vperpendDy + vperpendDz * vperpendDz)

            if (m1 >= LARGE_MASS) {
                if (vfricLen >= vperpendDiff) {
                    v2perpendX += vperpendDx
                    v2perpendY += vperpendDy
                    v2perpendZ += vperpendDz
                } else {
                    v2perpendX += vfricLen * vperpendDx / vperpendDiff
                    v2perpendX += vfricLen * vperpendDy / vperpendDiff
                    v2perpendX += vfricLen * vperpendDz / vperpendDiff
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
            v1.z = v1perpendZ // v1parallelZ is 0

            v2.x = v2perpendX + v2parallelX
            v2.y = v2perpendY + v2parallelY
            v2.z = v2perpendZ // v2parallelZ is 0
        }
    }

    /**
     * Updates body1.nextPos such that it no longer collides with body2.
     * @return whether or not the hit was vertical
     */
    private fun separate(body1: FixedCylinderBody, body2: FixedCylinderBody): Boolean {
        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y
        val p1z = body1.nextPos.z + body1.zOffset

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y
        val p2z = body2.nextPos.z + body2.zOffset

        val minTop = min(p1z + body1.height / 2, p2z + body2.height / 2)
        val maxBottom = max(p1z - body1.height / 2, p2z - body2.height / 2)
        val overlapHeight = minTop - maxBottom
        val vertical = overlapHeight < VERTICAL_HIT_OVERLAP_HEIGHT_THRESHOLD

        if (vertical) {
            // Separate the bodies along the z-axis
            val moveBy = (body1.height + body2.height) * 0.5f + 2 * EPSILON

            if (p1z < p2z) {
                body1.nextPos.z = body2.nextPos.z + body2.zOffset - moveBy - body1.zOffset
            } else {
                body1.nextPos.z = body2.nextPos.z + body2.zOffset + moveBy - body1.zOffset
            }
        } else {
            // Separate the bodies in the XY plane
            val dx = p2x - p1x
            val dy = p2y - p1y
            val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
            val dlen = sqrt(dsqr)
            val nx = dx / dlen
            val ny = dy / dlen
            val moveBy = body1.radius + body2.radius + 2 * EPSILON
            body1.nextPos.x = body2.nextPos.x - nx * moveBy
            body1.nextPos.y = body2.nextPos.y - ny * moveBy
        }

        return vertical
    }

    companion object {
        private const val VERTICAL_HIT_OVERLAP_HEIGHT_THRESHOLD = 0.05f
    }
}

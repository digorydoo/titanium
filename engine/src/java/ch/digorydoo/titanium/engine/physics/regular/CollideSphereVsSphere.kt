package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

internal class CollideSphereVsSphere: CollisionStrategy<FixedSphereBody, FixedSphereBody>() {
    override fun checkNextPos(body1: FixedSphereBody, body2: FixedSphereBody, outHitPt: MutablePoint3f) =
        check(
            cx1 = body1.nextPos.x,
            cy1 = body1.nextPos.y,
            cz1 = body1.nextPos.z + body1.zOffset,
            r1 = body1.radius,
            cx2 = body2.nextPos.x,
            cy2 = body2.nextPos.y,
            cz2 = body2.nextPos.z + body2.zOffset,
            r2 = body2.radius,
            outHitPt
        )

    private fun check(
        // Sphere 1
        cx1: Float,
        cy1: Float,
        cz1: Float,
        r1: Float,
        // Sphere 2
        cx2: Float,
        cy2: Float,
        cz2: Float,
        r2: Float,
        // out
        outHitPt: MutablePoint3f?,
    ): Boolean {
        val dx = cx2 - cx1
        val dy = cy2 - cy1
        val dz = cz2 - cz1

        val dsqr = (dx * dx) + (dy * dy) + (dz * dz)
        val rsum = r1 + r2
        if (dsqr > rsum * rsum) return false

        val d = sqrt(dsqr) // always >= 0

        if (outHitPt != null) {
            if (d <= EPSILON) {
                outHitPt.set(cx1, cy1, cz1)
            } else {
                outHitPt.x = cx1 + r1 * (dx / d)
                outHitPt.y = cy1 + r1 * (dy / d)
                outHitPt.z = cz1 + r1 * (dz / d)
            }
        }

        return true
    }

    /**
     * See docs/physics.txt
     */
    override fun bounce(body1: FixedSphereBody, body2: FixedSphereBody) {
        if (body1.mass < LARGE_MASS && body1.mass <= body2.mass) {
            separate(body1, body2) // updates body1.nextPos
        } else if (body2.mass < LARGE_MASS) {
            separate(body2, body1) // updates body2.nextPos
        } else {
            Log.warn("Cannot separate $body1 from $body2")
        }

        val elasticity = body1.elasticity * body2.elasticity
        val friction = 1.0f - (1.0f - body1.friction) * (1.0f - body2.friction)

        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y
        val p1z = body1.nextPos.z + body1.zOffset

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y
        val p2z = body2.nextPos.z + body2.zOffset

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val m1 = if (body1.mass <= EPSILON) EPSILON else body1.mass
        val m2 = if (body2.mass <= EPSILON) EPSILON else body2.mass

        val pdx = p2x - p1x
        val pdy = p2y - p1y
        val pdz = p2z - p1z
        val pLen = sqrt(pdx * pdx + pdy * pdy + pdz * pdz)

        val nx = pdx / pLen
        val ny = pdy / pLen
        val nz = pdz / pLen

        val v1dotn = v1.x * nx + v1.y * ny + v1.z * nz
        val v1parallelX = nx * v1dotn
        val v1parallelY = ny * v1dotn
        val v1parallelZ = nz * v1dotn

        val v2dotn = v2.x * nx + v2.y * ny + v2.z * nz
        val v2parallelX = nx * v2dotn
        val v2parallelY = ny * v2dotn
        val v2parallelZ = nz * v2dotn

        val vdiffX = v1parallelX - v2parallelX
        val vdiffY = v1parallelY - v2parallelY
        val vdiffZ = v1parallelZ - v2parallelZ

        if (m1 >= LARGE_MASS) {
            val v2perpendX = v2.x - v2parallelX
            val v2perpendY = v2.y - v2parallelY
            val v2perpendZ = v2.z - v2parallelZ
            v2.x = v2perpendX + v1parallelX + vdiffX * elasticity
            v2.y = v2perpendY + v1parallelY + vdiffY * elasticity
            v2.z = v2perpendZ + v1parallelZ + vdiffZ * elasticity

            if (friction > 0.0f) {
                val vfricX = vdiffX * friction
                val vfricY = vdiffY * friction
                val vfricZ = vdiffZ * friction
                val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY + vfricZ * vfricZ)
                val v2pLen = sqrt(v2perpendX * v2perpendX + v2perpendY * v2perpendY + v2perpendZ * v2perpendZ)

                if (vfricLen >= v2pLen) {
                    v2.x -= v2perpendX
                    v2.y -= v2perpendY
                    v2.z -= v2perpendZ
                } else {
                    v2.x -= vfricLen * v2perpendX / v2pLen
                    v2.y -= vfricLen * v2perpendY / v2pLen
                    v2.z -= vfricLen * v2perpendZ / v2pLen
                }
            }
        } else if (m2 >= LARGE_MASS) {
            val v1perpendX = v1.x - v1parallelX
            val v1perpendY = v1.y - v1parallelY
            val v1perpendZ = v1.z - v1parallelZ
            v1.x = v1perpendX + v2parallelX - vdiffX * elasticity
            v1.y = v1perpendY + v2parallelY - vdiffY * elasticity
            v1.z = v1perpendZ + v2parallelZ - vdiffZ * elasticity

            if (friction > 0.0f) {
                val vfricX = vdiffX * friction
                val vfricY = vdiffY * friction
                val vfricZ = vdiffZ * friction
                val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY + vfricZ * vfricZ)
                val v1pLen = sqrt(v1perpendX * v1perpendX + v1perpendY * v1perpendY + v1perpendZ * v1perpendZ)

                if (vfricLen >= v1pLen) {
                    v1.x -= v1perpendX
                    v1.y -= v1perpendY
                    v1.z -= v1perpendZ
                } else {
                    v1.x -= vfricLen * v1perpendX / v1pLen
                    v1.y -= vfricLen * v1perpendY / v1pLen
                    v1.z -= vfricLen * v1perpendZ / v1pLen
                }
            }
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

            v1.x = v1perpendX + (sx - vdiffX * elasticity * m2) / totalMass
            v1.y = v1perpendY + (sy - vdiffY * elasticity * m2) / totalMass
            v1.z = v1perpendZ + (sz - vdiffZ * elasticity * m2) / totalMass

            v2.x = v2perpendX + (sx + vdiffX * elasticity * m1) / totalMass
            v2.y = v2perpendY + (sy + vdiffY * elasticity * m1) / totalMass
            v2.z = v2perpendZ + (sz + vdiffZ * elasticity * m1) / totalMass

            if (friction > 0.0f) {
                val vfricX = vdiffX * friction
                val vfricY = vdiffY * friction
                val vfricZ = vdiffZ * friction
                val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY + vfricZ * vfricZ)

                val v1pLen = sqrt(v1perpendX * v1perpendX + v1perpendY * v1perpendY + v1perpendZ * v1perpendZ)
                val v2pLen = sqrt(v2perpendX * v2perpendX + v2perpendY * v2perpendY + v2perpendZ * v2perpendZ)

                if (vfricLen >= v1pLen) {
                    v1.x -= v1perpendX
                    v1.y -= v1perpendY
                    v1.z -= v1perpendZ
                } else {
                    v1.x -= vfricLen * v1perpendX / v1pLen
                    v1.y -= vfricLen * v1perpendY / v1pLen
                    v1.z -= vfricLen * v1perpendZ / v1pLen
                }

                if (vfricLen >= v2pLen) {
                    v2.x -= v2perpendX
                    v2.y -= v2perpendY
                    v2.z -= v2perpendZ
                } else {
                    v2.x -= vfricLen * v2perpendX / v2pLen
                    v2.y -= vfricLen * v2perpendY / v2pLen
                    v2.z -= vfricLen * v2perpendZ / v2pLen
                }
            }
        }
    }

    /**
     * Updates body1.nextPos such that it no longer collides with body2.
     */
    private fun separate(body1: FixedSphereBody, body2: FixedSphereBody) {
        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y
        val p1z = body1.nextPos.z + body1.zOffset

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y
        val p2z = body2.nextPos.z + body2.zOffset

        var dx = p2x - p1x
        var dy = p2y - p1y
        var dz = p2z - p1z
        var dist = sqrt((dx * dx) + (dy * dy) + (dz * dz))

        if (dist < EPSILON) {
            // The two bodies are too close. Try the original position before the collision!
            Log.warn("Bodies $body1 and $body2 are very close")
            val oldPos1 = body1.pos
            dx = p2x - oldPos1.x
            dy = p2y - oldPos1.y
            dz = p2z - (oldPos1.z + body1.zOffset)
            dist = sqrt((dx * dx) + (dy * dy) + (dz * dz))

            if (dist < EPSILON) {
                // We still can't tell a proper direction!
                Log.warn("Bodies $body1 and $body2 are too close! Forcing a separation along the x-axis!")
                dx = 1.0f
                dy = 0.0f
                dz = 0.0f
                dist = 1.0f
            }
        }

        require(body1.mass < LARGE_MASS)
        val moveBy = body1.radius + body2.radius + 2 * EPSILON
        body1.nextPos.x = p2x - moveBy * dx / dist
        body1.nextPos.y = p2y - moveBy * dy / dist
        body1.nextPos.z = p2z - moveBy * dz / dist - body1.zOffset
    }
}

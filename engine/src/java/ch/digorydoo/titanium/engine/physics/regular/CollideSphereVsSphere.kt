package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.sqrt

internal class CollideSphereVsSphere: CollisionStrategy<FixedSphereBody, FixedSphereBody, Unit>() {
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
        separate(body1, body1.zOffset, body2, body2.zOffset, Unit)

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
        val pdz = p2z - p1z
        val pLen = sqrt(pdx * pdx + pdy * pdy + pdz * pdz)

        val normDir12X = pdx / pLen
        val normDir12Y = pdy / pLen
        val normDir12Z = pdz / pLen

        // This may change v1 and/or v2
        applyFriction(
            m1 = m1,
            v1 = v1,
            m2 = m2,
            v2 = v2,
            normDir12X = normDir12X,
            normDir12Y = normDir12Y,
            normDir12Z = normDir12Z,
            friction = 1.0f - (1.0f - body1.friction) * (1.0f - body2.friction),
        )

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

    override fun separate(
        body1: FixedSphereBody,
        weight1: Float,
        normDir1X: Float,
        normDir1Y: Float,
        normDir1Z: Float,
        body2: FixedSphereBody,
        normDir2X: Float,
        normDir2Y: Float,
        normDir2Z: Float,
        params: Unit, // unused
    ) {
        if (weight1 > 0.0f) {
            separate(body1, body2, weight1, normDir1X, normDir1Y, normDir1Z)
        }

        if (weight1 < 1.0f) {
            // Since body1.nextPos may now be a bit further away from the point of collision, we pass a weight of 1.0f
            // in order that body2 moves the full remaining distance.
            separate(body2, body1, 1.0f, normDir2X, normDir2Y, normDir2Z)
        }
    }

    private fun separate(
        bodyToMove: FixedSphereBody,
        otherBody: FixedSphereBody,
        weight: Float,
        nx: Float,
        ny: Float,
        nz: Float,
    ) {
        require(bodyToMove.mass < LARGE_MASS)

        val x1 = bodyToMove.nextPos.x
        val y1 = bodyToMove.nextPos.y
        val z1 = bodyToMove.nextPos.z + bodyToMove.zOffset

        val x2 = otherBody.nextPos.x
        val y2 = otherBody.nextPos.y
        val z2 = otherBody.nextPos.z + otherBody.zOffset

        val dx = x1 - x2
        val dy = y1 - y2
        val dz = z1 - z2

        val moveBy = bodyToMove.radius + otherBody.radius + EPSILON

        // See physics.txt: "Finding the position of (almost) touch of two colliding spheres"

        val a = nx * nx + ny * ny + nz * nz
        val b = 2.0f * ((x1 - x2) * nx + (y1 - y2) * ny + (z1 - z2) * nz)
        val c = dx * dx + dy * dy + dz * dz - moveBy * moveBy
        val d = sqrt(b * b - 4 * a * c)

        val t1 = abs((-b + d) / 2.0f * a)
        val t2 = abs((-b - d) / 2.0f * a)
        val t = if (t1 > 0.0f) t1 else t2

        val tw = t * weight // we don't go all the way as the other body may be moved, too
        bodyToMove.nextPos.x = x1 + tw * nx
        bodyToMove.nextPos.y = y1 + tw * ny
        bodyToMove.nextPos.z = z1 + tw * nz - bodyToMove.zOffset
    }

    private fun applyFriction(
        m1: Float,
        v1: MutablePoint3f,
        m2: Float,
        v2: MutablePoint3f,
        normDir12X: Float,
        normDir12Y: Float,
        normDir12Z: Float,
        friction: Float,
    ) {
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
}

package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.FixedPlaneBody
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.sqrt

internal class CollideSphereVsPlane: CollisionStrategy<FixedSphereBody, FixedPlaneBody>() {
    /**
     * See docs/physics.txt
     */
    override fun checkNextPos(body1: FixedSphereBody, body2: FixedPlaneBody, outHitPt: MutablePoint3f): Boolean {
        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y
        val p1z = body1.nextPos.z + body1.zOffset

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y
        val p2z = body2.nextPos.z

        val n = body2.normal

        val dx = p1x - p2x
        val dy = p1y - p2y
        val dz = p1z - p2z
        val d = dx * n.x + dy * n.y + dz * n.z

        if (abs(d) > body1.radius) return false

        outHitPt.set(
            body1.nextPos.x - d * body2.normal.x,
            body1.nextPos.y - d * body2.normal.y,
            body1.nextPos.z + body1.zOffset - d * body2.normal.z,
        )
        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedPlaneBody) {
        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y
        val p1z = body1.nextPos.z + body1.zOffset

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y
        val p2z = body2.nextPos.z

        val nx = body2.normal.x
        val ny = body2.normal.y
        val nz = body2.normal.z

        val dx = p1x - p2x
        val dy = p1y - p2y
        val dz = p1z - p2z
        val d = dx * nx + dy * ny + dz * nz

        if (body1.mass < LARGE_MASS && body1.mass <= body2.mass) {
            // Move the sphere
            val q = if (d < 0.0f) -d - body1.radius else -d + body1.radius
            body1.nextPos.set(
                p1x + q * nx,
                p1y + q * ny,
                p1z + q * nz - body1.zOffset,
            )
        } else if (body2.mass < LARGE_MASS) {
            // Move the plane
            val q = if (d < 0.0f) d + body1.radius else d - body1.radius
            body2.nextPos.set(
                p2x + q * nx,
                p2y + q * ny,
                p2z + q * nz,
            )
        } else {
            Log.warn("Cannot separate $body1 from $body2")
            return
        }

        val m1 = if (body1.mass <= EPSILON) EPSILON else body1.mass
        val m2 = if (body2.mass <= EPSILON) EPSILON else body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        // This may change v1 and/or v2
        applyFriction(
            m1 = m1,
            v1 = v1,
            m2 = m2,
            v2 = v2,
            normalX = nx,
            normalY = ny,
            normalZ = nz,
            friction = 1.0f - (1.0f - body1.friction) * (1.0f - body2.friction),
        )

        val elasticity = body1.elasticity * body2.elasticity

        val v1dotn = v1.x * nx + v1.y * ny + v1.z * nz
        val v1parallelX = nx * v1dotn
        val v1parallelY = ny * v1dotn
        val v1parallelZ = nz * v1dotn

        val v2dotn = v2.x * nx + v2.y * ny + v2.z * nz
        val v2parallelX = nx * v2dotn
        val v2parallelY = ny * v2dotn
        val v2parallelZ = ny * v2dotn

        val vparallelDx = v1parallelX - v2parallelX
        val vparallelDy = v1parallelY - v2parallelY
        val vparallelDz = v1parallelZ - v2parallelZ

        if (m1 >= LARGE_MASS) {
            var v2perpendX = v2.x - v2parallelX
            var v2perpendY = v2.y - v2parallelY
            var v2perpendZ = v2.z - v2parallelZ

            v2.x = v2perpendX + v1parallelX + vparallelDx * elasticity
            v2.y = v2perpendY + v1parallelY + vparallelDy * elasticity
            v2.z = v2perpendZ + v1parallelZ + vparallelDz * elasticity
        } else if (m2 >= LARGE_MASS) {
            var v1perpendX = v1.x - v1parallelX
            var v1perpendY = v1.y - v1parallelY
            var v1perpendZ = v1.z - v1parallelZ

            v1.x = v1perpendX + v2parallelX - vparallelDx * elasticity
            v1.y = v1perpendY + v2parallelY - vparallelDy * elasticity
            v1.z = v1perpendZ + v2parallelZ - vparallelDz * elasticity
        } else {
            var v1perpendX = v1.x - v1parallelX
            var v1perpendY = v1.y - v1parallelY
            var v1perpendZ = v1.z - v1parallelZ

            var v2perpendX = v2.x - v2parallelX
            var v2perpendY = v2.y - v2parallelY
            var v2perpendZ = v2.z - v2parallelZ

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

    private fun applyFriction(
        m1: Float,
        v1: MutablePoint3f,
        m2: Float,
        v2: MutablePoint3f,
        normalX: Float,
        normalY: Float,
        normalZ: Float,
        friction: Float,
    ) {
        if (friction <= 0.0f) return

        val v1dotn = v1.x * normalX + v1.y * normalY + v1.z * normalZ
        val v1parallelX = normalX * v1dotn
        val v1parallelY = normalY * v1dotn
        val v1parallelZ = normalZ * v1dotn

        var v1perpendX = v1.x - v1parallelX
        var v1perpendY = v1.y - v1parallelY
        var v1perpendZ = v1.z - v1parallelZ

        val v2dotn = v2.x * normalX + v2.y * normalY + v2.z * normalZ
        val v2parallelX = normalX * v2dotn
        val v2parallelY = normalY * v2dotn
        val v2parallelZ = normalY * v2dotn

        var v2perpendX = v2.x - v2parallelX
        var v2perpendY = v2.y - v2parallelY
        var v2perpendZ = v2.z - v2parallelZ

        val vparallelDx = v1parallelX - v2parallelX
        val vparallelDy = v1parallelY - v2parallelY
        val vparallelDz = v1parallelZ - v2parallelZ

        val vfricX = vparallelDx * friction
        val vfricY = vparallelDy * friction
        val vfricZ = vparallelDz * friction
        val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY + vfricZ * vfricZ)

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

package ch.digorydoo.titanium.engine.physics.collide_regular

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

internal class CollideSphereVsSphere: CollisionStrategy<FixedSphereBody, FixedSphereBody>() {
    override fun check(
        body1: FixedSphereBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body2: FixedSphereBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        outHit: MutableHitResult?,
    ): Boolean {
        val dx = centreX2 - centreX1
        val dy = centreY2 - centreY1
        val dz = centreZ2 - centreZ1
        val dsqr = (dx * dx) + (dy * dy) + (dz * dz)
        val rsum = body1.radius + body2.radius
        if (dsqr > rsum * rsum) return false
        if (outHit == null) return true

        val d = sqrt(dsqr) // always >= 0

        if (d <= EPSILON) {
            outHit.hitPt.set(centreX1, centreY1, centreZ1)
        } else {
            outHit.hitPt.set(
                centreX1 + body1.radius * (dx / d),
                centreY1 + body1.radius * (dy / d),
                centreZ1 + body1.radius * (dz / d),
            )
        }

        // Spheres do not have specific areas
        outHit.area1 = HitArea.UNSPECIFIED
        outHit.area2 = HitArea.UNSPECIFIED
        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedSphereBody, hit: HitResult) {
        separate(body1, body2, hit)

        val m1 = body1.mass
        val m2 = body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y
        val p1z = body1.nextPos.z

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y
        val p2z = body2.nextPos.z

        val pdx = p2x - p1x
        val pdy = p2y - p1y
        val pdz = p2z - p1z
        val pLen = sqrt(pdx * pdx + pdy * pdy + pdz * pdz)

        if (pLen < EPSILON) {
            Log.warn("Failed to compute new speeds for $body1 and $body2, because they are too close")
            return
        }

        val normDir12X = pdx / pLen
        val normDir12Y = pdy / pLen
        val normDir12Z = pdz / pLen

        // This may change v1 and/or v2
        applyFriction(
            m1 = m1,
            v1 = v1,
            friction1 = body1.friction,
            m2 = m2,
            v2 = v2,
            friction2 = body2.friction,
            normDir12X = normDir12X,
            normDir12Y = normDir12Y,
            normDir12Z = normDir12Z,
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

    override fun forceApart(
        body1: FixedSphereBody,
        body2: FixedSphereBody,
        normDirX1: Float,
        normDirY1: Float,
        normDirZ1: Float,
        hit: HitResult,
    ) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos

        // Some tests are flaky unless we're adding here more than EPSILON (must be float inaccuracies)
        val moveBy = body1.radius + body2.radius + 6.0f * EPSILON

        if (body1.mass >= LARGE_MASS) {
            if (body2.mass >= LARGE_MASS) {
                Log.warn("Cannot force $body1 and $body2 apart, because both are LARGE_MASS")
            } else {
                p2.x = p1.x - normDirX1 * moveBy
                p2.y = p1.y - normDirY1 * moveBy
                p2.z = p1.z - normDirZ1 * moveBy
            }
        } else if (body2.mass >= LARGE_MASS) {
            p1.x = p2.x + normDirX1 * moveBy
            p1.y = p2.y + normDirY1 * moveBy
            p1.z = p2.z + normDirZ1 * moveBy
        } else {
            val centreX = (p1.x + p2.x) / 2.0f
            val centreY = (p1.y + p2.y) / 2.0f
            val centreZ = (p1.z + p2.z) / 2.0f
            val half = moveBy / 2.0f

            p1.x = centreX + normDirX1 * half
            p1.y = centreY + normDirY1 * half
            p1.z = centreZ + normDirZ1 * half

            p2.x = centreX - normDirX1 * half
            p2.y = centreY - normDirY1 * half
            p2.z = centreZ - normDirZ1 * half
        }
    }
}

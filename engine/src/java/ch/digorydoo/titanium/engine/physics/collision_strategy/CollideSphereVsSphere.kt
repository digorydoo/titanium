package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

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
            Log.warn("Setting normal to a random direction, because bodies $body1 and $body2 are too close")
            val r = Random.nextFloat()
            outHit.apply {
                hitPt.set(centreX1, centreY1, centreZ1)
                hitNormal12.set(cos(r), sin(r), 0.0f)
            }
        } else {
            outHit.apply {
                hitPt.set(
                    centreX1 + body1.radius * (dx / d),
                    centreY1 + body1.radius * (dy / d),
                    centreZ1 + body1.radius * (dz / d),
                )
                hitNormal12.set(dx / d, dy / d, dz / d)
            }
        }

        // Spheres do not have specific areas
        outHit.area1 = HitArea.UNSPECIFIED
        outHit.area2 = HitArea.UNSPECIFIED
        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedSphereBody, hit: HitResult) {
        helper.separateByBinarySearch(body1, body2, hit.hitNormal12, this) // FIXME replace with a direct approach

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

        helper.apply {
            applyFriction(body1, body2, normDir12X, normDir12Y, normDir12Z)
            bounceAtPlane(body1, body2, normDir12X, normDir12Y, normDir12Z)
        }
    }

    // TODO
    private fun separate(
        body1: FixedSphereBody,
        body2: FixedSphereBody,
        normDir12X: Float,
        normDir12Y: Float,
        normDir12Z: Float,
    ) {
        //         val p1 = body1.nextPos
        //         val p2 = body2.nextPos
        //
        //         // Some tests are flaky unless we're adding here more than EPSILON (must be float inaccuracies)
        //         val moveBy = body1.radius + body2.radius + 6.0f * EPSILON
        //
        //         if (body1.mass >= LARGE_MASS) {
        //             if (body2.mass >= LARGE_MASS) {
        //                 Log.warn("Cannot force $body1 and $body2 apart, because both are LARGE_MASS")
        //             } else {
        //                 p2.x = p1.x - normDirX1 * moveBy
        //                 p2.y = p1.y - normDirY1 * moveBy
        //                 p2.z = p1.z - normDirZ1 * moveBy
        //             }
        //         } else if (body2.mass >= LARGE_MASS) {
        //             p1.x = p2.x + normDirX1 * moveBy
        //             p1.y = p2.y + normDirY1 * moveBy
        //             p1.z = p2.z + normDirZ1 * moveBy
        //         } else {
        //             val centreX = (p1.x + p2.x) / 2.0f
        //             val centreY = (p1.y + p2.y) / 2.0f
        //             val centreZ = (p1.z + p2.z) / 2.0f
        //             val half = moveBy / 2.0f
        //
        //             p1.x = centreX + normDirX1 * half
        //             p1.y = centreY + normDirY1 * half
        //             p1.z = centreZ + normDirZ1 * half
        //
        //             p2.x = centreX - normDirX1 * half
        //             p2.y = centreY - normDirY1 * half
        //             p2.z = centreZ - normDirZ1 * half
        //         }
        //     }
    }
}

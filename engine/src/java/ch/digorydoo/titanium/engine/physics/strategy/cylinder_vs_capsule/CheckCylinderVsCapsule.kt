package ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_capsule

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionCheckStrategy
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

internal class CheckCylinderVsCapsule: CollisionCheckStrategy<FixedCylinderBody, FixedCapsuleBody>() {
    override fun check(
        body1: FixedCylinderBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body1IsBrick: Boolean,
        body2: FixedCapsuleBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        body2IsBrick: Boolean,
        bfcr: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
        outHit: MutableHitResult?,
    ): Boolean {
        val cylinderTop = centreZ1 + body1.height / 2.0f
        val capsuleTop = centreZ2 + body2.height / 2.0f

        val cylinderBottom = centreZ1 - body1.height / 2.0f
        val capsuleBottom = centreZ2 - body2.height / 2.0f

        if (cylinderTop < capsuleBottom) return false
        if (cylinderBottom > capsuleTop) return false

        val dx = centreX2 - centreX1
        val dy = centreY2 - centreY1
        val dsqrXY = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val cylinderRadius = body1.radius
        val capsuleRadius = body2.radius
        val rsum = cylinderRadius + capsuleRadius
        if (dsqrXY > rsum * rsum) return false

        if (dsqrXY <= cylinderRadius * cylinderRadius) {
            if (cylinderTop < capsuleBottom + capsuleRadius) {
                // The tip of the capsule bottom hits the cylinder top
                if (outHit != null) {
                    outHit.area1 = HitArea.TOP_FACE // cylinder
                    outHit.area2 = HitArea.BOTTOM_TIP // capsule
                    outHit.hitPt.set(centreX2, centreY2, capsuleBottom)
                    outHit.hitNormal12.set(Direction.upVector)
                }
                return true
            } else if (cylinderBottom > capsuleTop - capsuleRadius) {
                // The tip of the capsule top hits the cylinder bottom
                if (outHit != null) {
                    outHit.area1 = HitArea.BOTTOM_FACE // cylinder
                    outHit.area2 = HitArea.TOP_TIP // capsule
                    outHit.hitPt.set(centreX2, centreY2, capsuleTop)
                    outHit.hitNormal12.set(Direction.downVector)
                }
                return true
            }
        }

        // Find the point on the cylinder circle that's closest to the capsule in XY

        val distXY = sqrt(dsqrXY)
        val closestX1 = centreX1 + cylinderRadius * dx / distXY
        val closestY1 = centreY1 + cylinderRadius * dy / distXY

        // Check capsule top

        val dClosestX1To2 = centreX2 - closestX1
        val dClosestY1To2 = centreY2 - closestY1
        val dClosestXYTo2Sqr = (dClosestX1To2 * dClosestX1To2) + (dClosestY1To2 * dClosestY1To2)

        val capsuleTopCentreZ = capsuleTop - capsuleRadius

        if (cylinderBottom > capsuleTopCentreZ) {
            val topDz = capsuleTopCentreZ - cylinderBottom
            val topDsqr = dClosestXYTo2Sqr + (topDz * topDz)
            if (topDsqr > capsuleRadius * capsuleRadius) return false

            // The capsule's top sphere was hit

            if (outHit != null) {
                outHit.area1 = HitArea.BOTTOM_FACE // cylinder
                outHit.area2 = HitArea.TOP_SPHERE // capsule
                outHit.hitPt.set(closestX1, closestY1, cylinderBottom)

                if (topDsqr < EPSILON) {
                    outHit.hitNormal12.set(Direction.downVector)
                } else {
                    val topDist = sqrt(topDsqr)
                    outHit.hitNormal12.set(dClosestX1To2 / topDist, dClosestY1To2 / topDist, topDz / topDist)
                }
            }
            return true
        }

        // Check capsule bottom

        val capsuleBottomCentreZ = capsuleBottom + capsuleRadius

        if (cylinderTop < capsuleBottomCentreZ) {
            val topDz = capsuleBottomCentreZ - cylinderTop
            val topDsqr = dClosestXYTo2Sqr + (topDz * topDz)
            if (topDsqr > capsuleRadius * capsuleRadius) return false

            // The capsule's bottom sphere was hit

            if (outHit != null) {
                outHit.area1 = HitArea.TOP_FACE // cylinder
                outHit.area2 = HitArea.BOTTOM_SPHERE // capsule
                outHit.hitPt.set(closestX1, closestY1, cylinderTop)

                if (topDsqr < EPSILON) {
                    outHit.hitNormal12.set(Direction.upVector)
                } else {
                    val topDist = sqrt(topDsqr)
                    outHit.hitNormal12.set(dClosestX1To2 / topDist, dClosestY1To2 / topDist, topDz / topDist)
                }
            }
            return true
        }

        // The capsule side was hit
        if (outHit == null) return true

        outHit.area1 = HitArea.SIDE
        outHit.area2 = HitArea.SIDE

        // The hit point's xy must lie within both circles, and the weighted average fulfills this criterion.

        outHit.hitPt.x = centreX1 + (cylinderRadius / rsum) * dx
        outHit.hitPt.y = centreY1 + (cylinderRadius / rsum) * dy

        // The hit point's z lies in the centre of the vertical hit area.

        val minTop = min(cylinderTop, capsuleTop)
        val maxBottom = max(cylinderBottom, capsuleBottom)
        outHit.hitPt.z = (minTop + maxBottom) / 2.0f

        val dXY = sqrt(dsqrXY)

        if (dXY <= EPSILON) {
            Log.warn(TAG, "Setting normal to a random direction, because bodies $body1 and $body2 are too close")
            val r = Random.nextFloat()
            outHit.hitNormal12.set(cos(r), sin(r), 0.0f)
        } else {
            outHit.hitNormal12.set(dx / dXY, dy / dXY, 0.0f)
        }

        return true
    }

    companion object {
        private val TAG = Log.Tag("CheckCylinderVsCapsule")
    }
}

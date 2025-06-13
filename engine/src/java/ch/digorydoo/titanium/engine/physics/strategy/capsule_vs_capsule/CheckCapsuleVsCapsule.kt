package ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_capsule

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionCheckStrategy
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

internal class CheckCapsuleVsCapsule: CollisionCheckStrategy<FixedCapsuleBody, FixedCapsuleBody>() {
    override fun check(
        body1: FixedCapsuleBody,
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
        val top1 = centreZ1 + body1.height / 2.0f
        val top2 = centreZ2 + body2.height / 2.0f

        val bottom1 = centreZ1 - body1.height / 2.0f
        val bottom2 = centreZ2 - body2.height / 2.0f

        if (top1 < bottom2) return false
        if (bottom1 > top2) return false

        val dx = centreX2 - centreX1
        val dy = centreY2 - centreY1
        val dsqrXY = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val r1 = body1.radius
        val r2 = body2.radius
        val rsum = r1 + r2
        if (dsqrXY > rsum * rsum) return false

        // Check bottom 1 against top 2

        val bottom1CentreZ = bottom1 + r1
        val top2CentreZ = top2 - r2

        if (bottom1 > top2CentreZ && top2 < bottom1CentreZ) {
            val topDz = top2CentreZ - bottom1CentreZ
            val topDsqr = dsqrXY + (topDz * topDz)
            if (topDsqr > rsum * rsum) return false

            if (outHit != null) {
                outHit.area1 = HitArea.BOTTOM_SPHERE
                outHit.area2 = HitArea.TOP_SPHERE

                if (topDsqr <= EPSILON) {
                    outHit.hitNormal12.set(Direction.downVector)
                    outHit.hitPt.set(centreX1, centreY1, bottom1)
                } else {
                    val topDist = sqrt(topDsqr)
                    val nx = dx / topDist
                    val ny = dy / topDist
                    val nz = topDz / topDist
                    outHit.hitNormal12.set(nx, ny, nz)
                    outHit.hitPt.set(centreX1 + r1 * nx, centreY1 + r1 * ny, bottom1CentreZ + r1 * nz)
                }
            }
            return true
        }

        // Check top 1 against bottom 2

        val top1CentreZ = top1 - r1
        val bottom2CentreZ = bottom2 + r2

        if (top1 < bottom2CentreZ && bottom2 > top1CentreZ) {
            val topDz = bottom2CentreZ - top1CentreZ
            val topDsqr = dsqrXY + (topDz * topDz)
            if (topDsqr > rsum * rsum) return false

            if (outHit != null) {
                outHit.area1 = HitArea.TOP_SPHERE
                outHit.area2 = HitArea.BOTTOM_SPHERE

                if (topDsqr <= EPSILON) {
                    outHit.hitNormal12.set(Direction.upVector)
                    outHit.hitPt.set(centreX1, centreY1, top1)
                } else {
                    val topDist = sqrt(topDsqr)
                    val nx = dx / topDist
                    val ny = dy / topDist
                    val nz = topDz / topDist
                    outHit.hitNormal12.set(nx, ny, nz)
                    outHit.hitPt.set(centreX1 + r1 * nx, centreY1 + r1 * ny, top1CentreZ + r1 * nz)
                }
            }
            return true
        }

        // The capsules hit on their side
        if (outHit == null) return true

        outHit.area1 = HitArea.SIDE
        outHit.area2 = HitArea.SIDE

        // The hit point's xy must lie within both circles, and the weighted average fulfills this criterion

        outHit.hitPt.x = centreX1 + (r1 / rsum) * dx
        outHit.hitPt.y = centreY1 + (r1 / rsum) * dy

        // The hit point's z lies in the centre of the vertical hit area

        val minTop = min(top1, top2)
        val maxBottom = max(bottom1, bottom2)
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
        private val TAG = Log.Tag("CheckCapsuleVsCapsule")
    }
}

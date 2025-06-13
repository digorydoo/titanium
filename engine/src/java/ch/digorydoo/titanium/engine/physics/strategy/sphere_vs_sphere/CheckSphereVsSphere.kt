package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_sphere

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionCheckStrategy
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

internal class CheckSphereVsSphere: CollisionCheckStrategy<FixedSphereBody, FixedSphereBody>() {
    override fun check(
        body1: FixedSphereBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body1IsBrick: Boolean,
        body2: FixedSphereBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        body2IsBrick: Boolean,
        bfcr: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
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
            Log.warn(TAG, "Setting normal to a random direction, because bodies $body1 and $body2 are too close")
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

    companion object {
        private val TAG = Log.Tag("CheckSphereVsSphere")
    }
}

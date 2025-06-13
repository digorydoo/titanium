package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_capsule

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionCheckStrategy
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

internal class CheckSphereVsCapsule: CollisionCheckStrategy<FixedSphereBody, FixedCapsuleBody>() {
    override fun check(
        body1: FixedSphereBody,
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
        val sphereTop = centreZ1 + body1.radius
        val capsuleTop = centreZ2 + body2.height / 2.0f

        val sphereBottom = centreZ1 - body1.radius
        val capsuleBottom = centreZ2 - body2.height / 2.0f

        if (sphereTop < capsuleBottom) return false
        if (sphereBottom > capsuleTop) return false

        val dx = centreX2 - centreX1
        val dy = centreY2 - centreY1
        val dsqrXY = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val rsum = body1.radius + body2.radius
        val rsumSqr = rsum * rsum
        if (dsqrXY > rsumSqr) return false

        val capsuleTopCentreZ = capsuleTop - body2.radius

        if (centreZ1 > capsuleTopCentreZ) {
            val dz = capsuleTopCentreZ - centreZ1
            val dsqrTop = dsqrXY + (dz * dz)
            if (dsqrTop > rsumSqr) return false

            // The capsule's top was hit
            if (outHit == null) return true

            val d = sqrt(dsqrTop) // always >= 0

            if (d <= EPSILON) {
                outHit.apply {
                    hitPt.set(centreX1, centreY1, centreZ1)
                    hitNormal12.set(Direction.downVector) // sphere is above capsule centre
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

            outHit.area1 = HitArea.UNSPECIFIED // sphere
            outHit.area2 = HitArea.TOP_SPHERE // capsule
            return true
        }

        val capsuleBottomCentreZ = capsuleBottom + body2.radius

        if (centreZ1 < capsuleBottomCentreZ) {
            val dz = capsuleBottomCentreZ - centreZ1
            val dsqrBottom = dsqrXY + (dz * dz)
            if (dsqrBottom > rsumSqr) return false

            // The capsule's bottom was hit
            if (outHit == null) return true

            val d = sqrt(dsqrBottom) // always >= 0

            if (d <= EPSILON) {
                outHit.apply {
                    hitPt.set(centreX1, centreY1, centreZ1)
                    hitNormal12.set(Direction.upVector) // sphere is below capsule centre
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

            outHit.area1 = HitArea.UNSPECIFIED // sphere
            outHit.area2 = HitArea.BOTTOM_SPHERE // capsule
            return true
        }

        // The capsule's side was hit
        if (outHit == null) return true

        outHit.area1 = HitArea.UNSPECIFIED // sphere
        outHit.area2 = HitArea.SIDE // capsule

        // The hit point's xy must lie within both 2D circles, and the weighted average fulfills this criterion.
        outHit.hitPt.x = centreX1 + (body1.radius / rsum) * dx
        outHit.hitPt.y = centreY1 + (body1.radius / rsum) * dy

        // When hitting from the side, the hitPt.z must be the z of the sphere.
        outHit.hitPt.z = centreZ1

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
        private val TAG = Log.Tag("CheckSphereVsCapsule")
    }
}

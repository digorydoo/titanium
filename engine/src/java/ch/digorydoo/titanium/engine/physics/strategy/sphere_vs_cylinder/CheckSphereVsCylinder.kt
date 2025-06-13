package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cylinder

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionCheckStrategy
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.*
import kotlin.random.Random

internal class CheckSphereVsCylinder: CollisionCheckStrategy<FixedSphereBody, FixedCylinderBody>() {
    override fun check(
        body1: FixedSphereBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body1IsBrick: Boolean,
        body2: FixedCylinderBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        body2IsBrick: Boolean,
        bfcr: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
        outHit: MutableHitResult?,
    ): Boolean {
        val sphereTop = centreZ1 + body1.radius
        val cylinderTop = centreZ2 + body2.height / 2.0f

        val sphereBottom = centreZ1 - body1.radius
        val cylinderBottom = centreZ2 - body2.height / 2.0f

        if (sphereTop < cylinderBottom) return false
        if (sphereBottom > cylinderTop) return false

        val dx = centreX2 - centreX1
        val dy = centreY2 - centreY1
        val dsqrXY = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val rsum = body1.radius + body2.radius
        if (dsqrXY > rsum * rsum) return false
        if (outHit == null) return true

        // The hit point's xy must lie within both circles, and the weighted average fulfills this criterion.

        outHit.hitPt.x = centreX1 + (body1.radius / rsum) * dx
        outHit.hitPt.y = centreY1 + (body1.radius / rsum) * dy

        // The hit point's z lies in the centre of the vertical hit area.

        var minTop = min(sphereTop, cylinderTop)
        var maxBottom = max(sphereBottom, cylinderBottom)
        outHit.hitPt.z = (minTop + maxBottom) / 2.0f

        // Compute the overlap on the z-axis at the original position

        val sphereOrigCentreX = body1.pos.x
        val sphereOrigCentreY = body1.pos.y
        val sphereOrigCentreZ = body1.pos.z

        val cylinderOrigCentreX = body2.pos.x
        val cylinderOrigCentreY = body2.pos.y
        val cylinderOrigCentreZ = body2.pos.z

        val sphereOrigTop = sphereOrigCentreZ + body1.radius
        val cylinderOrigTop = cylinderOrigCentreZ + body2.height / 2.0f

        val sphereOrigBottom = sphereOrigCentreZ - body1.radius
        val cylinderOrigBottom = cylinderOrigCentreZ - body2.height / 2.0f

        minTop = min(sphereOrigTop, cylinderOrigTop)
        maxBottom = max(sphereOrigBottom, cylinderOrigBottom)
        val zOverlap = minTop - maxBottom

        // Compute the overlap in XY at the original position

        val origDx = sphereOrigCentreX - cylinderOrigCentreX
        val origDy = sphereOrigCentreY - cylinderOrigCentreY
        val origXYDistance = sqrt(origDx * origDx + origDy * origDy)
        val xyOverlap = body1.radius + body2.radius - origXYDistance

        // Determine where the cylinder was hit

        val isSide: Boolean

        if (xyOverlap > 0.0f && zOverlap <= 0.0f) {
            // The bodies had an overlap in XY prior to the collision, but no overlap in Z. It's impossible that
            // their speed could have led to a collision on the side, so it must be top or bottom.
            isSide = false
        } else if (zOverlap > 0.0f && xyOverlap <= 0.0f) {
            // The bodies had an overlap in Z prior to the collision, but no overlap in XY. It's impossible that
            // their speed could have led to a collision on the top or bottom, so it must be side.
            isSide = true
        } else {
            // Either there was no overlap at all prior to the collision, or there was an overlap on both axes.
            // The latter does not necessarily mean that there was a collision already at the original position.
            // We interpret it that the sphere is approaching one of the two circular edges of the cylinder. The
            // relative speed tells us which side the hit was on.

            val vsphere = body1.speedBeforeCollisions
            val vcylinder = body2.speedBeforeCollisions

            val relSpeedZ = vsphere.z - vcylinder.z
            val posDz = cylinderOrigCentreZ - sphereOrigCentreZ

            if (abs(relSpeedZ) <= EPSILON || sign(relSpeedZ) != sign(posDz)) {
                // The relative speed in Z is insignificant, or the Zs are moving away from each-other. It must be side!
                isSide = true
            } else {
                val relSpeedX = vsphere.x - vcylinder.x
                val relSpeedY = vsphere.y - vcylinder.y
                val relSpeedXYLen = sqrt(relSpeedX * relSpeedX + relSpeedY * relSpeedY)

                if (relSpeedXYLen <= EPSILON) {
                    // The relative speed in XY was insignificant, so it must be top or bottom.
                    isSide = false
                } else {
                    val posDx = cylinderOrigCentreX - sphereOrigCentreX
                    val posDy = cylinderOrigCentreY - sphereOrigCentreY
                    val posXYLen = sqrt(posDx * posDx + posDy * posDy)

                    if (posXYLen <= EPSILON) {
                        // The relative position in XY was very close, which is odd, but it must be top or bottom.
                        isSide = false
                    } else {
                        val xyNormRelSpeedX = relSpeedX / relSpeedXYLen
                        val xyNormRelSpeedY = relSpeedY / relSpeedXYLen

                        val xyNormPosX = posDx / posXYLen
                        val xyNormPosY = posDx / posXYLen

                        val speedXYDotPosXY = xyNormRelSpeedX * xyNormPosX + xyNormRelSpeedY * xyNormPosY

                        if (speedXYDotPosXY <= 0.0f) {
                            // The bodies are moving away from each-other in XY. It must be top or bottom!
                            isSide = false
                        } else {
                            // The speeds look towards each-other both in XY and Z.
                            isSide = relSpeedXYLen > abs(relSpeedZ)
                        }
                    }
                }
            }
        }

        outHit.area1 = HitArea.UNSPECIFIED // sphere do not have specific areas

        when {
            isSide -> {
                outHit.area2 = HitArea.SIDE
                val dXY = sqrt(dsqrXY)

                if (dXY <= EPSILON) {
                    Log.warn(
                        TAG,
                        "Setting normal to a random direction, because bodies $body1 and $body2 are too close"
                    )
                    val r = Random.nextFloat()
                    outHit.hitNormal12.set(cos(r), sin(r), 0.0f)
                } else {
                    outHit.hitNormal12.set(dx / dXY, dy / dXY, 0.0f)
                }
            }
            sphereOrigCentreZ < cylinderOrigCentreZ -> {
                outHit.apply {
                    area2 = HitArea.BOTTOM_FACE
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            }
            else -> {
                outHit.apply {
                    area2 = HitArea.TOP_FACE
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
        }

        return true
    }

    companion object {
        private val TAG = Log.Tag("CheckSphereVsCylinder")
    }
}

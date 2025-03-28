package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.*
import kotlin.random.Random

internal class CollideSphereVsCylinder: CollisionStrategy<FixedSphereBody, FixedCylinderBody>() {
    override fun check(
        body1: FixedSphereBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body2: FixedCylinderBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
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
                    Log.warn("Setting normal to a random direction, because bodies $body1 and $body2 are too close")
                    val r = Random.nextFloat()
                    outHit.hitNormal12.set(cos(r), sin(r), 0.0f)
                } else {
                    outHit.hitNormal12.set(dx / dXY, dy / dXY, 0.0f)
                }
            }
            sphereOrigCentreZ < cylinderOrigCentreZ -> {
                outHit.apply {
                    area2 = HitArea.BOTTOM
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            }
            else -> {
                outHit.apply {
                    area2 = HitArea.TOP
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
        }

        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedCylinderBody, hit: HitResult) {
        helper.separateByBinarySearch(body1, body2, hit.hitNormal12, this) // FIXME replace with a direct approach

        val m1 = body1.mass
        val m2 = body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        if (hit.area2 != HitArea.SIDE) {
            val normDir12Z = if (hit.area2 == HitArea.BOTTOM) 1.0f else -1.0f
            helper.applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

            val v1z = v1.z // sphere
            val v2z = v2.z // cylinder
            val vparallelDz = v1z - v2z

            if (sign(vparallelDz) != normDir12Z) {
                // The two objects are separating
                return
            }

            val elasticity = body1.elasticity * body2.elasticity

            if (m1 >= LARGE_MASS) {
                v2.z = v1z + vparallelDz * elasticity
            } else if (m2 >= LARGE_MASS) {
                v1.z = v2z - vparallelDz * elasticity
            } else {
                val totalMass = m1 + m2
                val sz = v1z * m1 + v2z * m2
                v1.z = (sz - vparallelDz * elasticity * m2) / totalMass
                v2.z = (sz + vparallelDz * elasticity * m1) / totalMass
            }
        } else {
            // We treat this like bouncing a circle off another circle in the XY plane.

            val p1x = body1.nextPos.x
            val p1y = body1.nextPos.y

            val p2x = body2.nextPos.x
            val p2y = body2.nextPos.y

            val pdx = p2x - p1x
            val pdy = p2y - p1y
            val p2DDistance = sqrt(pdx * pdx + pdy * pdy)

            if (p2DDistance < EPSILON) {
                Log.warn("Cannot compute new speeds of $body1 and $body2, because the distance in XY is too short")
                return
            }

            val normDir12X = pdx / p2DDistance
            val normDir12Y = pdy / p2DDistance

            helper.apply {
                applyFriction(body1, body2, normDir12X, normDir12Y, 0.0f)
                bounceAtVerticalPlane(body1, body2, normDir12X, normDir12Y)
            }
        }
    }

    // TODO
    private fun separateVertically(body1: FixedSphereBody, body2: FixedCylinderBody, normDir12Z: Float) {
        //         val p1 = body1.nextPos
        //         val p2 = body2.nextPos
        //
        //         // Try moving the bodies until they are separated in Z
        //
        //         val moveByWhenZ: Float
        //
        //         if (abs(normDirZ1) <= EPSILON) {
        //             moveByWhenZ = Float.POSITIVE_INFINITY
        //         } else {
        //             // We want to scale normDir by a factor k such that the resulting z will have the required distance.
        //             // Adding more than just EPSILON here because of failing tests (floating point inaccuracies)
        //             val requiredZDistance = body1.radius + body2.height / 2 + 8.0f * EPSILON
        //             val k = requiredZDistance / normDirZ1
        //
        //             // The length of the scaled normDir is the move distance
        //             val dx = k * normDirX1
        //             val dy = k * normDirY1
        //             // dz == k * normDirZ1 == requiredZDistance
        //             moveByWhenZ = sqrt(dx * dx + dy * dy + requiredZDistance * requiredZDistance)
        //         }
        //
    }

    // TODO
    private fun separateHorizontally(
        body1: FixedSphereBody,
        body2: FixedCylinderBody,
        normDir12X: Float,
        normDir12Y: Float,
    ) {
        //         // Try moving the bodies until they are separated in XY
        //
        //         val moveByWhenXY: Float
        //         val normDirXYLen = sqrt(normDirX1 * normDirX1 + normDirY1 * normDirY1)
        //
        //         if (normDirXYLen <= EPSILON) {
        //             moveByWhenXY = Float.POSITIVE_INFINITY
        //         } else {
        //             // We want to scale normDir by a factor k such that the distance in XY will have the required value.
        //             // |k * (nx; ny)| = d, which is the same as k * |(nx; ny)| = d, or k = d / |(nx; ny)|
        //             val requiredXYDistance = body1.radius + body2.radius + 8.0f * EPSILON
        //             val k = requiredXYDistance / normDirXYLen
        //
        //             // The length of the scaled normDir is the move distance
        //             val dx = k * normDirX1
        //             val dy = k * normDirY1
        //             val dz = k * normDirZ1
        //             moveByWhenXY = sqrt(dx * dx + dy * dy + dz * dz)
        //         }
    }
}

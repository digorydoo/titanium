package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.helper.HitArea
import ch.digorydoo.titanium.engine.physics.helper.HitResult
import ch.digorydoo.titanium.engine.physics.helper.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.*
import kotlin.random.Random

internal class CollideSphereVsCylinder: CollisionStrategy<FixedSphereBody, FixedCylinderBody>() {
    override fun configure(
        body1IsBrick: Boolean,
        body2IsBrick: Boolean,
        bricks: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
    ) {
        if (body1IsBrick) throw NotImplementedError()
        if (body2IsBrick) throw NotImplementedError()
    }

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

    override fun separate(body1: FixedSphereBody, body2: FixedCylinderBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP,
            HitArea.BOTTOM,
            -> {
                val normDir12Z = hit.hitNormal12.z
                separateInZ(body1, body2, normDir12Z)
            }
            else -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y
                separateInXY(body1, body2, normDir12X, normDir12Y)
            }
        }

        verifySeparation(body1, body2, hit)
    }

    private fun separateInZ(sphere: FixedSphereBody, cylinder: FixedCylinderBody, normDir12Z: Float) {
        val p1 = sphere.nextPos
        val p2 = cylinder.nextPos

        // Adding more than just EPSILON here because of flaky tests (must be floating-point inaccuracies)
        val requiredDistance = sphere.radius + cylinder.height / 2.0f + 2.0f * EPSILON
        val distanceAlongNormDir = (p2.z - p1.z) * normDir12Z
        val moveBy = requiredDistance - distanceAlongNormDir

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separateInZ was called, but bodies seem to be vertically separated already")
            return
        }

        when {
            sphere.mass < LARGE_MASS -> when {
                cylinder.mass < LARGE_MASS -> {
                    // Do not distribute the distance by mass! If the lighter object is cornered, the CollisionManager
                    // would have trouble moving the heavier object away!
                    val move1By = moveBy * 0.5f
                    val move2By = moveBy - move1By

                    p1.z -= normDir12Z * move1By
                    p2.z += normDir12Z * move2By
                }
                else -> {
                    p1.z -= normDir12Z * moveBy
                }
            }
            cylinder.mass < LARGE_MASS -> {
                p2.z += normDir12Z * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $sphere from $cylinder failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    private fun separateInXY(
        body1: FixedSphereBody,
        body2: FixedCylinderBody,
        normDir12X: Float,
        normDir12Y: Float,
    ) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos

        // Adding more than just EPSILON here because of flaky tests (must be floating-point inaccuracies)
        val requiredDistance = body1.radius + body2.radius + 6.0f * EPSILON
        val distanceAlongNormDir = (p2.x - p1.x) * normDir12X + (p2.y - p1.y) * normDir12Y
        val moveBy = requiredDistance - distanceAlongNormDir

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separateInXY was called, but bodies seem to be vertically separated already")
            return
        }

        when {
            body1.mass < LARGE_MASS -> when {
                body2.mass < LARGE_MASS -> {
                    // Do not distribute the distance by mass! If the lighter object is cornered, the CollisionManager
                    // would have trouble moving the heavier object away!
                    val move1By = moveBy * 0.5f
                    val move2By = moveBy - move1By

                    p1.x -= normDir12X * move1By
                    p1.y -= normDir12Y * move1By

                    p2.x += normDir12X * move2By
                    p2.y += normDir12Y * move2By
                }
                else -> {
                    p1.x -= normDir12X * moveBy
                    p1.y -= normDir12Y * moveBy
                }
            }
            body2.mass < LARGE_MASS -> {
                p2.x += normDir12X * moveBy
                p2.y += normDir12Y * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $body1 from $body2 failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    override fun computeNextSpeed(body1: FixedSphereBody, body2: FixedCylinderBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP -> {
                val normDir12Z = hit.hitNormal12.z
                helper.applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                val deltaSpeedZ = body2.speed.z - body1.speed.z // positive if sphere falls down on cylinder

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent sphere lying on cylinder from constantly hopping due to gravity
                    body1.nextSpeed.z = body2.nextSpeed.z
                }

                helper.bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            HitArea.BOTTOM -> {
                val normDir12Z = hit.hitNormal12.z
                helper.applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                // No remedy for cylinder standing on sphere hopping due to gravity, because this configuration should
                // be instable anyway.

                helper.bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            else -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y

                helper.apply {
                    applyFriction(body1, body2, normDir12X, normDir12Y, 0.0f)
                    bounceAtVerticalPlane(body1, body2, normDir12X, normDir12Y)
                }
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("CollideSphereVsCylinder")
    }
}

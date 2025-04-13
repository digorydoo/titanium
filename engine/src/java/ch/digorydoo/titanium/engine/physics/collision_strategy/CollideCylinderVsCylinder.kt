package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.*
import kotlin.random.Random

internal class CollideCylinderVsCylinder: CollisionStrategy<FixedCylinderBody, FixedCylinderBody>() {
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
        body1: FixedCylinderBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body2: FixedCylinderBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
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
        val rsum = body1.radius + body2.radius
        if (dsqrXY > rsum * rsum) return false
        if (outHit == null) return true

        // The hit point's xy must lie within both circles, and the weighted average fulfills this criterion.

        outHit.hitPt.x = centreX1 + (body1.radius / rsum) * dx
        outHit.hitPt.y = centreY1 + (body1.radius / rsum) * dy

        // The hit point's z lies in the centre of the vertical hit area.

        var minTop = min(top1, top2)
        var maxBottom = max(bottom1, bottom2)
        outHit.hitPt.z = (minTop + maxBottom) / 2.0f

        // Compute the overlap on the z-axis at the original position

        val body1OrigCentreZ = body1.pos.z
        val body2OrigCentreZ = body2.pos.z

        val body1OrigTop = body1OrigCentreZ + body1.height / 2.0f
        val body2OrigTop = body2OrigCentreZ + body2.height / 2.0f

        val body1OrigBottom = body1OrigCentreZ - body1.height / 2.0f
        val body2OrigBottom = body2OrigCentreZ - body2.height / 2.0f

        minTop = min(body1OrigTop, body2OrigTop)
        maxBottom = max(body1OrigBottom, body2OrigBottom)
        val zOverlap = minTop - maxBottom

        // Compute the overlap in XY at the original position

        val origDx = body1.pos.x - body2.pos.x
        val origDy = body1.pos.y - body2.pos.y
        val xyDistance = sqrt(origDx * origDx + origDy * origDy)
        val xyOverlap = body1.radius + body2.radius - xyDistance

        // Determine whether this was a hit on the side

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
            // The latter means the two cylinder must have collided at the original position already, which is an edge
            // case that shouldn't happen under normal conditions. So, we ignore that case and assume there was no
            // overlap, which means that the bodies flew towards the edge of the other. The relative speed tells us
            // which side the hit was on.

            val speed1 = body1.speedBeforeCollisions
            val speed2 = body2.speedBeforeCollisions

            val relSpeedZ = speed1.z - speed2.z
            val posDz = body2OrigCentreZ - body1OrigCentreZ

            if (abs(relSpeedZ) <= EPSILON || sign(relSpeedZ) != sign(posDz)) {
                // The relative speed in Z is insignificant, or the Zs are moving away from each-other. It must be side!
                isSide = true
            } else {
                val relSpeedX = speed1.x - speed2.x
                val relSpeedY = speed1.y - speed2.y
                val relSpeedXYLen = sqrt(relSpeedX * relSpeedX + relSpeedY * relSpeedY)

                if (relSpeedXYLen <= EPSILON) {
                    // The relative speed in XY was insignificant, so it must be top or bottom.
                    isSide = false
                } else {
                    val posDx = body2.pos.x - body1.pos.x
                    val posDy = body2.pos.y - body1.pos.y
                    val posXYLen = sqrt(posDx * posDx + posDy * posDy)

                    if (posXYLen <= EPSILON) {
                        // The relative position in XY was very close, which we interpret as being top or bottom.
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

        // Determine the area that was hit

        when {
            isSide -> {
                outHit.apply {
                    area1 = HitArea.SIDE
                    area2 = HitArea.SIDE
                }

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
            body1OrigCentreZ < body2OrigCentreZ -> {
                outHit.apply {
                    area1 = HitArea.TOP
                    area2 = HitArea.BOTTOM
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            }
            else -> {
                outHit.apply {
                    area1 = HitArea.BOTTOM
                    area2 = HitArea.TOP
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
        }

        return true
    }

    override fun bounce(body1: FixedCylinderBody, body2: FixedCylinderBody, hit: HitResult) {
        when (hit.area1) {
            HitArea.TOP -> {
                val normDir12Z = hit.hitNormal12.z

                separateInZ(body1, body2, normDir12Z)
                helper.applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                val deltaSpeedZ = body1.speed.z - body2.speed.z // positive if body2 falls down on body1

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent body2 standing on body1 from constantly hopping due to gravity
                    body2.nextSpeed.z = body1.nextSpeed.z
                }

                helper.bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            HitArea.BOTTOM -> {
                val normDir12Z = hit.hitNormal12.z

                separateInZ(body1, body2, normDir12Z)
                helper.applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                val deltaSpeedZ = body2.speed.z - body1.speed.z // positive if body1 falls down on body2

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent body1 standing on body2 from constantly hopping due to gravity
                    body1.nextSpeed.z = body2.nextSpeed.z
                }

                helper.bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            else -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y

                separateInXY(body1, body2, normDir12X, normDir12Y)

                helper.apply {
                    applyFriction(body1, body2, normDir12X, normDir12Y, 0.0f)
                    bounceAtVerticalPlane(body1, body2, normDir12X, normDir12Y)
                }
            }
        }

        verifySeparation(body1, body2, hit)
    }

    private fun separateInZ(body1: FixedCylinderBody, body2: FixedCylinderBody, normDir12Z: Float) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos

        val distanceAlongNormDir = (p2.z - p1.z) * normDir12Z
        val requiredDistance = (body1.height + body2.height) / 2.0f + EPSILON
        val moveBy = requiredDistance - distanceAlongNormDir

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separateInZ was called, but bodies seem to be vertically separated already")
            return
        }

        when {
            body1.mass < LARGE_MASS -> when {
                body2.mass < LARGE_MASS -> {
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
            body2.mass < LARGE_MASS -> {
                p2.z += normDir12Z * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $body1 from $body2 failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    private fun separateInXY(
        body1: FixedCylinderBody,
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

    companion object {
        private val TAG = Log.Tag("CollideCylinderVsCylinder")
    }
}

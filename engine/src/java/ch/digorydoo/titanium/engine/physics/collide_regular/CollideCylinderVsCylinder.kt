package ch.digorydoo.titanium.engine.physics.collide_regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt

internal class CollideCylinderVsCylinder: CollisionStrategy<FixedCylinderBody, FixedCylinderBody>() {
    override fun check(
        body1: FixedCylinderBody,
        centreX1: Float,
        centreY1: Float,
        refPtZ1: Float,
        body2: FixedCylinderBody,
        centreX2: Float,
        centreY2: Float,
        refPtZ2: Float,
        outHit: MutableHitResult?,
    ): Boolean {
        val centreZ1 = refPtZ1 + body1.zOffset
        val centreZ2 = refPtZ2 + body2.zOffset

        val top1 = centreZ1 + body1.height / 2.0f
        val top2 = centreZ2 + body2.height / 2.0f

        val bottom1 = centreZ1 - body1.height / 2.0f
        val bottom2 = centreZ2 - body2.height / 2.0f

        if (top1 < bottom2) return false
        if (bottom1 > top2) return false

        var dx = centreX2 - centreX1
        var dy = centreY2 - centreY1
        val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val rsum = body1.radius + body2.radius
        if (dsqr > rsum * rsum) return false
        if (outHit == null) return true

        // The hit point's xy must lie within both circles, and the weighted average fulfills this criterion.

        outHit.hitPt.x = centreX1 + (body1.radius / rsum) * dx
        outHit.hitPt.y = centreY1 + (body1.radius / rsum) * dy

        // The hit point's z lies in the centre of the vertical hit area.

        var minTop = min(top1, top2)
        var maxBottom = max(bottom1, bottom2)
        outHit.hitPt.z = (minTop + maxBottom) / 2.0f

        // Compute the overlap on the z-axis at the original position

        val body1OrigCentreZ = body1.pos.z + body1.zOffset
        val body2OrigCentreZ = body2.pos.z + body2.zOffset

        val body1OrigTop = body1OrigCentreZ + body1.height / 2.0f
        val body2OrigTop = body2OrigCentreZ + body2.height / 2.0f

        val body1OrigBottom = body1OrigCentreZ - body1.height / 2.0f
        val body2OrigBottom = body2OrigCentreZ - body2.height / 2.0f

        minTop = min(body1OrigTop, body2OrigTop)
        maxBottom = max(body1OrigBottom, body2OrigBottom)
        val zOverlap = minTop - maxBottom

        // Compute the overlap in XY at the original position

        dx = body1.pos.x - body2.pos.x
        dy = body1.pos.y - body2.pos.y
        val xyDistance = sqrt(dx * dx + dy * dy)
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
                        // The relative position in XY was very close, which we interprete as being top or bottom.
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
                outHit.area1 = HitArea.SIDE
                outHit.area2 = HitArea.SIDE
            }
            body1OrigCentreZ < body2OrigCentreZ -> {
                outHit.area1 = HitArea.TOP
                outHit.area2 = HitArea.BOTTOM
            }
            else -> {
                outHit.area1 = HitArea.BOTTOM
                outHit.area2 = HitArea.TOP
            }
        }

        return true
    }

    override fun bounce(body1: FixedCylinderBody, body2: FixedCylinderBody, hit: HitResult) {
        separate(body1, body1.zOffset, body2, body2.zOffset)

        val m1 = if (body1.mass <= EPSILON) EPSILON else body1.mass
        val m2 = if (body2.mass <= EPSILON) EPSILON else body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val elasticity = body1.elasticity * body2.elasticity
        val friction = 1.0f - (1.0f - body1.friction) * (1.0f - body2.friction)

        if (hit.area1 != HitArea.SIDE) {
            applyFrictionTopBottom(m1 = m1, v1 = v1, m2 = m2, v2 = v2, friction = friction)

            val v1z = v1.z
            val v2z = v2.z
            val vparallelDz = v1z - v2z
            val expectedSign = if (hit.area1 == HitArea.TOP) 1.0f else -1.0f

            if (sign(vparallelDz) != expectedSign) {
                // The two objects are separating
                return
            }

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

            applyFrictionSide(
                m1 = m1,
                v1 = v1,
                m2 = m2,
                v2 = v2,
                normDir12X = normDir12X,
                normDir12Y = normDir12Y,
                friction = friction,
            )

            val v1dotn = v1.x * normDir12X + v1.y * normDir12Y
            val v1parallelX = normDir12X * v1dotn
            val v1parallelY = normDir12Y * v1dotn

            val v2dotn = v2.x * normDir12X + v2.y * normDir12Y
            val v2parallelX = normDir12X * v2dotn
            val v2parallelY = normDir12Y * v2dotn

            val vparallelDx = v1parallelX - v2parallelX
            val vparallelDy = v1parallelY - v2parallelY

            if (m1 >= LARGE_MASS) {
                var v2perpendX = v2.x - v2parallelX
                var v2perpendY = v2.y - v2parallelY
                var v2perpendZ = v2.z // v2parallelZ is 0

                v2.x = v2perpendX + v1parallelX + vparallelDx * elasticity
                v2.y = v2perpendY + v1parallelY + vparallelDy * elasticity
                v2.z = v2perpendZ // v2parallelZ is 0
            } else if (m2 >= LARGE_MASS) {
                var v1perpendX = v1.x - v1parallelX
                var v1perpendY = v1.y - v1parallelY
                var v1perpendZ = v1.z // v1parallelZ is 0

                v1.x = v1perpendX + v2parallelX - vparallelDx * elasticity
                v1.y = v1perpendY + v2parallelY - vparallelDy * elasticity
                v1.z = v1perpendZ // v1parallelZ is 0
            } else {
                var v1perpendX = v1.x - v1parallelX
                var v1perpendY = v1.y - v1parallelY
                var v1perpendZ = v1.z // v1parallelZ is 0

                var v2perpendX = v2.x - v2parallelX
                var v2perpendY = v2.y - v2parallelY
                var v2perpendZ = v2.z // v2parallelZ is 0

                val totalMass = m1 + m2

                val sx = v1parallelX * m1 + v2parallelX * m2
                val sy = v1parallelY * m1 + v2parallelY * m2

                v1.x = v1perpendX + (sx - vparallelDx * elasticity * m2) / totalMass
                v1.y = v1perpendY + (sy - vparallelDy * elasticity * m2) / totalMass
                v1.z = v1perpendZ // v1parallelZ is 0

                v2.x = v2perpendX + (sx + vparallelDx * elasticity * m1) / totalMass
                v2.y = v2perpendY + (sy + vparallelDy * elasticity * m1) / totalMass
                v2.z = v2perpendZ // v2parallelZ is 0
            }
        }
    }

    private fun applyFrictionTopBottom(m1: Float, v1: MutablePoint3f, m2: Float, v2: MutablePoint3f, friction: Float) {
        if (friction <= 0.0f) return

        val v1z = v1.z
        val v2z = v2.z
        val vparallelDz = v1z - v2z

        if (m1 >= LARGE_MASS) {
            val vfricZ = vparallelDz * friction
            val v2pLen = sqrt(v2.x * v2.x + v2.y * v2.y)

            if (vfricZ >= v2pLen || v2pLen < EPSILON) {
                v2.x = 0.0f
                v2.y = 0.0f
            } else {
                v2.x -= vfricZ * v2.x / v2pLen
                v2.y -= vfricZ * v2.y / v2pLen
            }
        } else if (m2 >= LARGE_MASS) {
            val vfricZ = vparallelDz * friction
            val v1pLen = sqrt(v1.x * v1.x + v1.y * v1.y)

            if (vfricZ >= v1pLen || v1pLen < EPSILON) {
                v1.x = 0.0f
                v1.y = 0.0f
            } else {
                v1.x -= vfricZ * v1.x / v1pLen
                v1.y -= vfricZ * v1.y / v1pLen
            }
        } else {
            val vfricZ = vparallelDz * friction

            val v1pLen = sqrt(v1.x * v1.x + v1.y * v1.y)
            val v2pLen = sqrt(v2.x * v2.x + v2.y * v2.y)

            if (vfricZ >= v1pLen || v1pLen < EPSILON) {
                v1.x = 0.0f
                v1.y = 0.0f
            } else {
                v1.x -= vfricZ * v1.x / v1pLen
                v1.y -= vfricZ * v1.y / v1pLen
            }

            if (vfricZ >= v2pLen || v2pLen < EPSILON) {
                v2.x = 0.0f
                v2.y = 0.0f
            } else {
                v2.x -= vfricZ * v2.x / v2pLen
                v2.y -= vfricZ * v2.y / v2pLen
            }
        }
    }

    private fun applyFrictionSide(
        m1: Float,
        v1: MutablePoint3f,
        m2: Float,
        v2: MutablePoint3f,
        normDir12X: Float,
        normDir12Y: Float,
        friction: Float,
    ) {
        if (friction <= 0.0f) return

        val v1dotn = v1.x * normDir12X + v1.y * normDir12Y
        val v1parallelX = normDir12X * v1dotn
        val v1parallelY = normDir12Y * v1dotn
        // val v1parallelZ = 0.0f in the XY plane

        var v1perpendX = v1.x - v1parallelX
        var v1perpendY = v1.y - v1parallelY
        var v1perpendZ = v1.z // v1parallelZ is 0

        val v2dotn = v2.x * normDir12X + v2.y * normDir12Y
        val v2parallelX = normDir12X * v2dotn
        val v2parallelY = normDir12Y * v2dotn
        // val v2parallelZ = 0.0f in the XY plane

        var v2perpendX = v2.x - v2parallelX
        var v2perpendY = v2.y - v2parallelY
        var v2perpendZ = v2.z // v2parallelZ is 0

        val vparallelDx = v1parallelX - v2parallelX
        val vparallelDy = v1parallelY - v2parallelY
        // val vparallelDz = 0.0f

        val vfricX = vparallelDx * friction
        val vfricY = vparallelDy * friction
        val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY)

        val vperpendDx = v1perpendX - v2perpendX
        val vperpendDy = v1perpendY - v2perpendY
        val vperpendDz = v1perpendZ - v2perpendZ
        val vperpendDiff = sqrt(vperpendDx * vperpendDx + vperpendDy * vperpendDy + vperpendDz * vperpendDz)

        if (m1 >= LARGE_MASS) {
            if (vfricLen >= vperpendDiff) {
                v2perpendX += vperpendDx
                v2perpendY += vperpendDy
                v2perpendZ += vperpendDz
            } else {
                v2perpendX += vfricLen * vperpendDx / vperpendDiff
                v2perpendX += vfricLen * vperpendDy / vperpendDiff
                v2perpendX += vfricLen * vperpendDz / vperpendDiff
            }
        } else if (m2 >= LARGE_MASS) {
            if (vfricLen >= vperpendDiff) {
                v1perpendX -= vperpendDx
                v1perpendY -= vperpendDy
                v1perpendZ -= vperpendDz
            } else {
                v1perpendX -= vfricLen * vperpendDx / vperpendDiff
                v1perpendY -= vfricLen * vperpendDy / vperpendDiff
                v1perpendZ -= vfricLen * vperpendDz / vperpendDiff
            }
        } else {
            val totalMass = m1 + m2

            if (vfricLen >= vperpendDiff) {
                v1perpendX -= m2 * vperpendDx / totalMass
                v1perpendY -= m2 * vperpendDy / totalMass
                v1perpendZ -= m2 * vperpendDz / totalMass

                v2perpendX += m1 * vperpendDx / totalMass
                v2perpendY += m1 * vperpendDy / totalMass
                v2perpendZ += m1 * vperpendDz / totalMass
            } else {
                val vdfricX = vfricLen * vperpendDx / vperpendDiff
                val vdfricY = vfricLen * vperpendDy / vperpendDiff
                val vdfricZ = vfricLen * vperpendDz / vperpendDiff

                v1perpendX -= m2 * vdfricX / totalMass
                v1perpendY -= m2 * vdfricY / totalMass
                v1perpendZ -= m2 * vdfricZ / totalMass

                v2perpendX += m1 * vdfricX / totalMass
                v2perpendY += m1 * vdfricY / totalMass
                v2perpendZ += m1 * vdfricZ / totalMass
            }
        }

        v1.x = v1perpendX + v1parallelX
        v1.y = v1perpendY + v1parallelY
        v1.z = v1perpendZ // v1parallelZ is 0

        v2.x = v2perpendX + v2parallelX
        v2.y = v2perpendY + v2parallelY
        v2.z = v2perpendZ // v2parallelZ is 0
    }

    override fun forceApart(
        body1: FixedCylinderBody,
        body2: FixedCylinderBody,
        normDirX1: Float,
        normDirY1: Float,
        normDirZ1: Float,
    ) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos
        val centreX = (p1.x + p2.x) / 2.0f
        val centreY = (p1.y + p2.y) / 2.0f
        val centreZ = (p1.z + body1.zOffset + p2.z + body2.zOffset) / 2.0f

        // Try moving the bodies until they are separated in Z

        val moveByWhenZ: Float

        if (abs(normDirZ1) <= EPSILON) {
            moveByWhenZ = Float.POSITIVE_INFINITY
        } else {
            // We want to scale normDir by a factor k such that the resulting z will have the required distance.
            // Adding more than just EPSILON here because of failing tests (floating point inaccuracies)
            val requiredZDistance = body1.height / 2 + body2.height / 2 + 8.0f * EPSILON
            val k = requiredZDistance / normDirZ1

            // The length of the scaled normDir is the move distance
            val dx = k * normDirX1
            val dy = k * normDirY1
            // dz == k * normDirZ1 == requiredZDistance
            moveByWhenZ = sqrt(dx * dx + dy * dy + requiredZDistance * requiredZDistance)
        }

        // Try moving the bodies until they are separated in XY

        val moveByWhenXY: Float
        val normDirXYLen = sqrt(normDirX1 * normDirX1 + normDirY1 * normDirY1)

        if (normDirXYLen <= EPSILON) {
            moveByWhenXY = Float.POSITIVE_INFINITY
        } else {
            // We want to scale normDir by a factor k such that the distance in XY will have the required value.
            // |k * (nx; ny)| = d, which is the same as k * |(nx; ny)| = d, or k = d / |(nx; ny)|
            val requiredXYDistance = body1.radius + body2.radius + 8.0f * EPSILON
            val k = requiredXYDistance / normDirXYLen

            // The length of the scaled normDir is the move distance
            val dx = k * normDirX1
            val dy = k * normDirY1
            val dz = k * normDirZ1
            moveByWhenXY = sqrt(dx * dx + dy * dy + dz * dz)
        }

        // Take the smaller of the two move distances

        val moveBy = min(moveByWhenZ, moveByWhenXY) / 2.0f
        require(moveBy < Float.POSITIVE_INFINITY) // this can only fail if normDir does not have length 1

        p1.x = centreX + normDirX1 * moveBy
        p1.y = centreY + normDirY1 * moveBy
        p1.z = centreZ + normDirZ1 * moveBy - body1.zOffset

        p2.x = centreX - normDirX1 * moveBy
        p2.y = centreY - normDirY1 * moveBy
        p2.z = centreZ - normDirZ1 * moveBy - body2.zOffset
    }
}

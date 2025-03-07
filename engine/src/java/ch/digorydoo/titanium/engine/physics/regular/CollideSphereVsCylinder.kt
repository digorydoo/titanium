package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.RigidBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt

internal class CollideSphereVsCylinder:
    CollisionStrategy<FixedSphereBody, FixedCylinderBody, CollideSphereVsCylinder.HitArea>() {

    enum class HitArea { TOP1_HITS_BOTTOM2, BOTTOM1_HITS_TOP2, SIDE }

    override fun checkNextPos(body1: FixedSphereBody, body2: FixedCylinderBody, outHitPt: MutablePoint3f) =
        check(
            sphereX = body1.nextPos.x,
            sphereY = body1.nextPos.y,
            sphereZ = body1.nextPos.z + body1.zOffset,
            sphereRadius = body1.radius,
            cylinderX = body2.nextPos.x,
            cylinderY = body2.nextPos.y,
            cylinderZ = body2.nextPos.z + body2.zOffset,
            cylinderRadius = body2.radius,
            cylinderHeight = body2.height,
            outHitPt,
        )

    private fun check(
        // Sphere
        sphereX: Float,
        sphereY: Float,
        sphereZ: Float,
        sphereRadius: Float,
        // Cylinder
        cylinderX: Float,
        cylinderY: Float,
        cylinderZ: Float,
        cylinderRadius: Float,
        cylinderHeight: Float,
        // out
        outHitPt: MutablePoint3f?,
    ): Boolean {
        val sphereTop = sphereZ + sphereRadius
        val cylinderTop = cylinderZ + cylinderHeight / 2.0f

        val sphereBottom = sphereZ - sphereRadius
        val cylinderBottom = cylinderZ - cylinderHeight / 2.0f

        if (sphereTop < cylinderBottom) return false
        if (sphereBottom > cylinderTop) return false

        val dx = cylinderX - sphereX
        val dy = cylinderY - sphereY
        val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val rsum = sphereRadius + cylinderRadius
        if (dsqr > rsum * rsum) return false

        if (outHitPt != null) {
            // The hit point's xy must lie within both circles, and the weighted average fulfills this criterion.

            outHitPt.x = sphereX + (sphereRadius / rsum) * dx
            outHitPt.y = sphereY + (sphereRadius / rsum) * dy

            // The hit point's z lies in the centre of the vertical hit area.

            val minTop = min(sphereTop, cylinderTop)
            val maxBottom = max(sphereBottom, cylinderBottom)
            outHitPt.z = (minTop + maxBottom) / 2.0f
        }

        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedCylinderBody) {
        val hitArea = determineHitArea(body1, body2)
        separate(body1, body1.zOffset, body2, body2.zOffset, hitArea)

        val m1 = if (body1.mass <= EPSILON) EPSILON else body1.mass
        val m2 = if (body2.mass <= EPSILON) EPSILON else body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y

        val pdx = p2x - p1x
        val pdy = p2y - p1y
        val p2DDistance = sqrt(pdx * pdx + pdy * pdy)

        val normDir12X = pdx / p2DDistance
        val normDir12Y = pdy / p2DDistance

        // This may change v1 and/or v2
        applyFriction(
            m1 = m1,
            v1 = v1,
            m2 = m2,
            v2 = v2,
            normDir12X = normDir12X,
            normDir12Y = normDir12Y,
            hitArea = hitArea,
            friction = 1.0f - (1.0f - body1.friction) * (1.0f - body2.friction),
        )

        val elasticity = body1.elasticity * body2.elasticity

        when (hitArea) {
            HitArea.BOTTOM1_HITS_TOP2, HitArea.TOP1_HITS_BOTTOM2 -> {
                val v1z = v1.z // sphere
                val v2z = v2.z // cylinder
                val vparallelDz = v1z - v2z
                val expectedSign = if (hitArea == HitArea.TOP1_HITS_BOTTOM2) 1.0f else -1.0f

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
            }
            HitArea.SIDE -> {
                // We treat this like bouncing a circle off another circle in the XY plane.

                val v1dotn = v1.x * normDir12X + v1.y * normDir12Y
                val v1parallelX = normDir12X * v1dotn
                val v1parallelY = normDir12Y * v1dotn

                val v2dotn = v2.x * normDir12X + v2.y * normDir12Y
                val v2parallelX = normDir12X * v2dotn
                val v2parallelY = normDir12Y * v2dotn

                val vparallelDx = v1parallelX - v2parallelX
                val vparallelDy = v1parallelY - v2parallelY

                if (m1 >= LARGE_MASS) {
                    val v2perpendX = v2.x - v2parallelX
                    val v2perpendY = v2.y - v2parallelY
                    val v2perpendZ = v2.z // v2parallelZ is 0

                    v2.x = v2perpendX + v1parallelX + vparallelDx * elasticity
                    v2.y = v2perpendY + v1parallelY + vparallelDy * elasticity
                    v2.z = v2perpendZ // v2parallelZ is 0
                } else if (m2 >= LARGE_MASS) {
                    val v1perpendX = v1.x - v1parallelX
                    val v1perpendY = v1.y - v1parallelY
                    val v1perpendZ = v1.z // v1parallelZ is 0

                    v1.x = v1perpendX + v2parallelX - vparallelDx * elasticity
                    v1.y = v1perpendY + v2parallelY - vparallelDy * elasticity
                    v1.z = v1perpendZ // v1parallelZ is 0
                } else {
                    val v1perpendX = v1.x - v1parallelX
                    val v1perpendY = v1.y - v1parallelY
                    val v1perpendZ = v1.z // v1parallelZ is 0

                    val v2perpendX = v2.x - v2parallelX
                    val v2perpendY = v2.y - v2parallelY
                    val v2perpendZ = v2.z // v2parallelZ is 0

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
    }

    private fun determineHitArea(sphere: FixedSphereBody, cylinder: FixedCylinderBody): HitArea {
        // Compute the overlap on the z-axis at the original position

        val sphereCentreZ = sphere.pos.z + sphere.zOffset
        val cylinderCentreZ = cylinder.pos.z + cylinder.zOffset

        val sphereTop = sphereCentreZ + sphere.radius
        val cylinderTop = cylinderCentreZ + cylinder.height / 2.0f

        val sphereBottom = sphereCentreZ - sphere.radius
        val cylinderBottom = cylinderCentreZ - cylinder.height / 2.0f

        val minTop = min(sphereTop, cylinderTop)
        val maxBottom = max(sphereBottom, cylinderBottom)
        val zOverlap = minTop - maxBottom

        // Compute the overlap in XY at the original position

        val dx = sphere.pos.x - cylinder.pos.x
        val dy = sphere.pos.y - cylinder.pos.y
        val xyDistance = sqrt(dx * dx + dy * dy)
        val xyOverlap = sphere.radius + cylinder.radius - xyDistance

        // Determine hit area

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
            // We interprete it that the sphere is approaching one of the two circular edges of the cylinder. The
            // relative speed tells us which side the hit was on.

            val vsphere = sphere.speedBeforeCollisions
            val vcylinder = cylinder.speedBeforeCollisions

            val relSpeedZ = vsphere.z - vcylinder.z
            val posDz = cylinder.pos.z - sphere.pos.z

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
                    val posDx = cylinder.pos.x - sphere.pos.x
                    val posDy = cylinder.pos.y - sphere.pos.y
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

        return when {
            isSide -> HitArea.SIDE
            sphere.pos.z < cylinder.pos.z -> HitArea.TOP1_HITS_BOTTOM2
            else -> HitArea.BOTTOM1_HITS_TOP2
        }
    }

    override fun separate(
        body1: FixedSphereBody,
        weight1: Float,
        normDir1X: Float,
        normDir1Y: Float,
        normDir1Z: Float,
        body2: FixedCylinderBody,
        normDir2X: Float,
        normDir2Y: Float,
        normDir2Z: Float,
        params: HitArea,
    ) {
        when (params) {
            HitArea.TOP1_HITS_BOTTOM2, HitArea.BOTTOM1_HITS_TOP2 -> {
                if (weight1 > 0.0f) {
                    separateTopBottom(
                        bodyToMove = body1,
                        zOffsetOfBodyToMove = body1.zOffset,
                        zHalfHeightOfBodyToMove = body1.radius,
                        otherBody = body2,
                        zOffsetOfOtherBody = body2.zOffset,
                        zHalfHeightOfOtherBody = body2.height / 2.0f,
                        weight = weight1,
                        normDirX = normDir1X,
                        normDirY = normDir1Y,
                        normDirZ = normDir1Z,
                    )
                }

                if (weight1 < 1.0f) {
                    separateTopBottom(
                        bodyToMove = body2,
                        zOffsetOfBodyToMove = body2.zOffset,
                        zHalfHeightOfBodyToMove = body2.height / 2.0f,
                        otherBody = body1,
                        zOffsetOfOtherBody = body1.zOffset,
                        zHalfHeightOfOtherBody = body1.radius,
                        weight = 1.0f, // the full remaining distance since we've already moved body1
                        normDirX = normDir2X,
                        normDirY = normDir2Y,
                        normDirZ = normDir2Z,
                    )
                }
            }
            HitArea.SIDE -> {
                if (weight1 > 0.0f) {
                    separateSide(
                        bodyToMove = body1,
                        radiusInXYOfBodyToMove = body1.radius,
                        otherBody = body2,
                        radiusInXYOfOtherBody = body2.radius,
                        weight = weight1,
                        normDirX = normDir1X,
                        normDirY = normDir1Y,
                        normDirZ = normDir1Z,
                    )
                }

                if (weight1 < 1.0f) {
                    separateSide(
                        bodyToMove = body2,
                        radiusInXYOfBodyToMove = body2.radius,
                        otherBody = body1,
                        radiusInXYOfOtherBody = body1.radius,
                        weight = 1.0f, // the full remaining distance since we've already moved body1
                        normDirX = normDir2X,
                        normDirY = normDir2Y,
                        normDirZ = normDir2Z,
                    )
                }
            }
        }
    }

    private fun separateTopBottom(
        bodyToMove: RigidBody,
        zOffsetOfBodyToMove: Float,
        zHalfHeightOfBodyToMove: Float,
        otherBody: RigidBody,
        zOffsetOfOtherBody: Float,
        zHalfHeightOfOtherBody: Float,
        weight: Float,
        normDirX: Float,
        normDirY: Float,
        normDirZ: Float,
    ) {
        if (abs(normDirZ) <= EPSILON) {
            Log.warn("Cannot separate $bodyToMove from $otherBody in z, because normDir is insignificant in z")
            return
        }

        // The distance between the centres of the bodies in Z required for a full separation in Z
        val requiredDistanceInZ = zHalfHeightOfBodyToMove + zHalfHeightOfOtherBody + EPSILON

        // Compute the new value for bodyToMove.nextPos.z for a full separation
        val newZ = if (normDirZ > 0.0f) {
            (otherBody.nextPos.z + zOffsetOfOtherBody) + requiredDistanceInZ - zOffsetOfBodyToMove
        } else {
            (otherBody.nextPos.z + zOffsetOfOtherBody) - requiredDistanceInZ - zOffsetOfBodyToMove
        }

        // Since we want to move along the normDir, we're looking for some t, which:
        // newZ = bodyToMoveZ + t * normDirZ, or t = (newZ - bodyToMoveZ) / normDirZ
        val t = (newZ - bodyToMove.nextPos.z) / normDirZ // zOffsetOfBodyToMove cancels out

        // Approach the separation with the given weight
        val tw = t * weight
        bodyToMove.nextPos.x += tw * normDirX
        bodyToMove.nextPos.y += tw * normDirY
        bodyToMove.nextPos.z += tw * normDirZ
    }

    private fun separateSide(
        bodyToMove: RigidBody,
        radiusInXYOfBodyToMove: Float,
        otherBody: RigidBody,
        radiusInXYOfOtherBody: Float,
        weight: Float,
        normDirX: Float,
        normDirY: Float,
        normDirZ: Float,
    ) {
        val xyLen = sqrt(normDirX * normDirX + normDirY * normDirY)

        if (xyLen <= EPSILON) {
            Log.warn("Cannot separate $bodyToMove from $otherBody in XY, because normDir is insignificant in XY")
            return
        }

        // The distance between the centres of the bodies in XY required for a full separation in XY
        val requiredDistanceInXY = radiusInXYOfBodyToMove + radiusInXYOfOtherBody + EPSILON

        // (nx; ny) has the direction of normDirXY, but length 1; note that |normDirXY| <= 1
        val nx = normDirX / xyLen
        val ny = normDirY / xyLen

        val x1 = bodyToMove.nextPos.x
        val y1 = bodyToMove.nextPos.y

        val x2 = otherBody.nextPos.x
        val y2 = otherBody.nextPos.y

        val dx = x1 - x2
        val dy = y1 - y2

        // See physics.txt: "Finding the position of (almost) touch of two colliding circles"
        val a = nx * nx + ny * ny
        val b = 2.0f * ((x1 - x2) * nx + (y1 - y2) * ny)
        val c = dx * dx + dy * dy - requiredDistanceInXY * requiredDistanceInXY
        val d = sqrt(b * b - 4 * a * c)

        val t1 = abs((-b + d) / 2.0f * a)
        val t2 = abs((-b - d) / 2.0f * a)
        val t = if (t1 > 0.0f) t1 else t2

        // Approach the separation with the given weight
        val tw = t * weight
        bodyToMove.nextPos.x += tw * nx
        bodyToMove.nextPos.y += tw * ny

        // We need to move along normDir, so we also need to figure out the z value of the new position.
        // The vector tw * (nx; ny; newZ) is distance we want to move. This should be the same as scaling the
        // normDir with some factor q, i.e. tw * (nx; ny; newZ) = q * (normDirX; normDirY; normDirZ).
        // We have tw * nx = q * normDirX, and so if normDirX is not close to zero, q = tw * nx / normDirX.
        val q = if (abs(normDirX) > abs(normDirY)) tw * nx / normDirX else tw * ny / normDirY
        bodyToMove.nextPos.z += q * normDirZ
    }

    private fun applyFriction(
        m1: Float,
        v1: MutablePoint3f,
        m2: Float,
        v2: MutablePoint3f,
        normDir12X: Float,
        normDir12Y: Float,
        hitArea: HitArea,
        friction: Float,
    ) {
        if (friction <= 0.0f) return

        if (hitArea != HitArea.SIDE) {
            // We treat this like bouncing the sphere off the XY plane.

            val vparallelDz = v1.z - v2.z

            if (m1 >= LARGE_MASS) {
                val vfricZ = vparallelDz * friction
                val v2pLen = sqrt(v2.x * v2.x + v2.y * v2.y)

                if (vfricZ >= v2pLen) {
                    v2.x = 0.0f
                    v2.y = 0.0f
                } else {
                    v2.x -= vfricZ * v2.x / v2pLen
                    v2.y -= vfricZ * v2.y / v2pLen
                }
            } else if (m2 >= LARGE_MASS) {
                val vfricZ = vparallelDz * friction
                val v1pLen = sqrt(v1.x * v1.x + v1.y * v1.y)

                if (vfricZ >= v1pLen) {
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

                if (vfricZ >= v1pLen) {
                    v1.x = 0.0f
                    v1.y = 0.0f
                } else {
                    v1.x -= vfricZ * v1.x / v1pLen
                    v1.y -= vfricZ * v1.y / v1pLen
                }

                if (vfricZ >= v2pLen) {
                    v2.x = 0.0f
                    v2.y = 0.0f
                } else {
                    v2.x -= vfricZ * v2.x / v2pLen
                    v2.y -= vfricZ * v2.y / v2pLen
                }
            }
        } else {
            // We treat this like bouncing a circle off another circle in the XY plane.

            val v1dotn = v1.x * normDir12X + v1.y * normDir12Y
            val v1parallelX = normDir12X * v1dotn
            val v1parallelY = normDir12Y * v1dotn
            // v1parallelZ is 0

            var v1perpendX = v1.x - v1parallelX
            var v1perpendY = v1.y - v1parallelY
            var v1perpendZ = v1.z // v1parallelZ is 0

            val v2dotn = v2.x * normDir12X + v2.y * normDir12Y
            val v2parallelX = normDir12X * v2dotn
            val v2parallelY = normDir12Y * v2dotn
            // v2parallelZ is 0

            var v2perpendX = v2.x - v2parallelX
            var v2perpendY = v2.y - v2parallelY
            var v2perpendZ = v2.z // v2parallelZ is 0

            val vparallelDx = v1parallelX - v2parallelX
            val vparallelDy = v1parallelY - v2parallelY
            // vparallelDz is 0

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
                    v2perpendY += vfricLen * vperpendDy / vperpendDiff
                    v2perpendZ += vfricLen * vperpendDz / vperpendDiff
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
    }
}

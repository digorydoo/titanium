package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.physics.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.RigidBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt

internal class CollideCylinderVsCylinder:
    CollisionStrategy<FixedCylinderBody, FixedCylinderBody, CollideCylinderVsCylinder.HitArea>() {
    enum class HitArea {
        TOP1_HITS_BOTTOM2, BOTTOM1_HITS_TOP2, SIDE;

        fun swapped() = when (this) {
            TOP1_HITS_BOTTOM2 -> BOTTOM1_HITS_TOP2
            BOTTOM1_HITS_TOP2 -> TOP1_HITS_BOTTOM2
            SIDE -> SIDE
        }
    }

    override fun checkNextPos(body1: FixedCylinderBody, body2: FixedCylinderBody, outHitPt: MutablePoint3f) =
        check(
            cx1 = body1.nextPos.x,
            cy1 = body1.nextPos.y,
            cz1 = body1.nextPos.z + body1.zOffset,
            r1 = body1.radius,
            h1 = body1.height,
            cx2 = body2.nextPos.x,
            cy2 = body2.nextPos.y,
            cz2 = body2.nextPos.z + body2.zOffset,
            r2 = body2.radius,
            h2 = body2.height,
            outHitPt,
        )

    private fun check(
        // Cylinder 1
        cx1: Float,
        cy1: Float,
        cz1: Float,
        r1: Float,
        h1: Float,
        // Cylinder 2
        cx2: Float,
        cy2: Float,
        cz2: Float,
        r2: Float,
        h2: Float,
        // out
        outHitPt: MutablePoint3f?,
    ): Boolean {
        val top1 = cz1 + h1 / 2.0f
        val top2 = cz2 + h2 / 2.0f

        val bottom1 = cz1 - h1 / 2.0f
        val bottom2 = cz2 - h2 / 2.0f

        if (top1 < bottom2) return false
        if (bottom1 > top2) return false

        val dx = cx2 - cx1
        val dy = cy2 - cy1
        val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val rsum = r1 + r2
        if (dsqr > rsum * rsum) return false

        if (outHitPt != null) {
            // The hit point's xy must lie within both circles, and the weighted average fulfills this criterion.

            outHitPt.x = cx1 + (r1 / rsum) * dx
            outHitPt.y = cy1 + (r1 / rsum) * dy

            // The hit point's z lies in the centre of the vertical hit area.

            val minTop = min(top1, top2)
            val maxBottom = max(bottom1, bottom2)
            outHitPt.z = (minTop + maxBottom) / 2.0f
        }

        return true
    }

    override fun bounce(body1: FixedCylinderBody, body2: FixedCylinderBody) {
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
            HitArea.TOP1_HITS_BOTTOM2, HitArea.BOTTOM1_HITS_TOP2 -> {
                // We treat this like bouncing two planes against each-other.

                val v1z = v1.z
                val v2z = v2.z
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
    }

    private fun determineHitArea(body1: FixedCylinderBody, body2: FixedCylinderBody): HitArea {
        val p1z = body1.nextPos.z + body1.zOffset
        val p2z = body2.nextPos.z + body2.zOffset

        val top1 = p1z + body1.height / 2
        val top2 = p2z + body2.height / 2

        val bottom1 = p1z - body1.height / 2
        val bottom2 = p2z - body2.height / 2

        val minTop = min(top1, top2)
        val maxBottom = max(bottom1, bottom2)
        val overlapHeight = minTop - maxBottom

        return when {
            overlapHeight > VERTICAL_HIT_OVERLAP_HEIGHT_THRESHOLD -> HitArea.SIDE
            top1 == minTop -> HitArea.TOP1_HITS_BOTTOM2
            else -> HitArea.BOTTOM1_HITS_TOP2
        }
    }

    override fun separate(
        body1: FixedCylinderBody,
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
        val bodyToMove: RigidBody
        val otherBody: RigidBody
        val zOffsetOfBodyToMove: Float
        val zOffsetOfOtherBody: Float
        val normDirX: Float
        val normDirY: Float
        val hitAreaFromBodyToMovesPOV: HitArea

        when {
            weight1 > 0.0f -> {
                bodyToMove = body1
                otherBody = body2
                zOffsetOfBodyToMove = body1.zOffset
                zOffsetOfOtherBody = body2.zOffset
                normDirX = normDir1X
                normDirY = normDir1Y
                hitAreaFromBodyToMovesPOV = params
            }
            else -> {
                bodyToMove = body2
                otherBody = body1
                zOffsetOfBodyToMove = body2.zOffset
                zOffsetOfOtherBody = body1.zOffset
                normDirX = normDir2X
                normDirY = normDir2Y
                hitAreaFromBodyToMovesPOV = params.swapped()
            }
        }

        require(bodyToMove.mass < LARGE_MASS)

        when (hitAreaFromBodyToMovesPOV) {
            HitArea.TOP1_HITS_BOTTOM2 -> {
                val moveBy = (bodyToMove.height + otherBody.height) * 0.5f + EPSILON
                bodyToMove.nextPos.z = otherBody.nextPos.z + otherBody.zOffset - moveBy - bodyToMove.zOffset
            }
            HitArea.BOTTOM1_HITS_TOP2 -> {
                val moveBy = (bodyToMove.height + otherBody.height) * 0.5f + EPSILON
                bodyToMove.nextPos.z = otherBody.nextPos.z + otherBody.zOffset + moveBy - bodyToMove.zOffset
            }
            HitArea.SIDE -> {
                // Find the direction of separation in the XY plane

                val xyLen = sqrt(normDirX * normDirX + normDirY * normDirY)
                val nx = normDirX / xyLen
                val ny = normDirY / xyLen

                val x1 = bodyToMove.nextPos.x
                val y1 = bodyToMove.nextPos.y

                val x2 = otherBody.nextPos.x
                val y2 = otherBody.nextPos.y

                val dx = x1 - x2
                val dy = y1 - y2

                val moveBy = bodyToMove.radius + otherBody.radius + EPSILON

                // See physics.txt: "Finding the position of (almost) touch of two colliding circles"

                val a = nx * nx + ny * ny
                val b = 2.0f * ((x1 - x2) * nx + (y1 - y2) * ny)
                val c = dx * dx + dy * dy - moveBy * moveBy
                val d = sqrt(b * b - 4 * a * c)

                val t1 = abs((-b + d) / 2.0f * a)
                val t2 = abs((-b - d) / 2.0f * a)
                val t = if (t1 > 0.0f) t1 else t2

                bodyToMove.nextPos.x = x1 + t * nx
                bodyToMove.nextPos.y = y1 + t * ny
            }
        }
    }

    private fun applyFriction(
        m1: Float,
        v1: MutablePoint3f,
        m2: Float,
        v2: MutablePoint3f,
        hitArea: HitArea,
        normDir12X: Float,
        normDir12Y: Float,
        friction: Float,
    ) {
        if (friction <= 0.0f) return

        if (hitArea == HitArea.TOP1_HITS_BOTTOM2 || hitArea == HitArea.BOTTOM1_HITS_TOP2) {
            val v1z = v1.z
            val v2z = v2.z
            val vparallelDz = v1z - v2z

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
    }

    companion object {
        private const val VERTICAL_HIT_OVERLAP_HEIGHT_THRESHOLD = 0.05f
    }
}

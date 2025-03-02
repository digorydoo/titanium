package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt

internal class CollideSphereVsCylinder: CollisionStrategy<FixedSphereBody, FixedCylinderBody>() {
    override fun checkNextPos(body1: FixedSphereBody, body2: FixedCylinderBody, outHitPt: MutablePoint3f) =
        check(
            cx1 = body1.nextPos.x,
            cy1 = body1.nextPos.y,
            cz1 = body1.nextPos.z + body1.zOffset,
            r1 = body1.radius,
            cx2 = body2.nextPos.x,
            cy2 = body2.nextPos.y,
            cz2 = body2.nextPos.z + body2.zOffset,
            r2 = body2.radius,
            h2 = body2.height,
            outHitPt,
        )

    private fun check(
        // Sphere
        cx1: Float,
        cy1: Float,
        cz1: Float,
        r1: Float,
        // Cylinder
        cx2: Float,
        cy2: Float,
        cz2: Float,
        r2: Float,
        h2: Float,
        // out
        outHitPt: MutablePoint3f?,
    ): Boolean {
        if (cz1 + r1 < cz2 - h2 * 0.5f) return false
        if (cz1 - r1 > cz2 + h2 * 0.5f) return false

        val dx = cx2 - cx1
        val dy = cy2 - cy1
        val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val rsum = r1 + r2
        if (dsqr > rsum * rsum) return false

        if (outHitPt != null) {
            val vertical = dsqr <= r2 * r2 // vertical hit if sphere's centre is within cylinder's circle in XY plane
            val d = sqrt(dsqr) // always >= 0

            if (d <= EPSILON || vertical) {
                if (cz1 < cz2) {
                    // The sphere's top centre point collides with the cylinder's bottom plane.
                    outHitPt.x = cx1
                    outHitPt.y = cy1
                    outHitPt.z = cz1 + r1
                } else {
                    // The sphere's bottom centre point collides with the cylinder's top plane.
                    outHitPt.x = cx1
                    outHitPt.y = cy1
                    outHitPt.z = cz1 - r1
                }
            } else {
                // The two circles collide within the XY plane
                outHitPt.x = cx1 + r1 * (dx / d)
                outHitPt.y = cy1 + r1 * (dy / d)
                outHitPt.z = cz1
            }
        }

        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedCylinderBody) {
        val vertical: Boolean

        if (body1.mass < LARGE_MASS && body1.mass <= body2.mass) {
            vertical = separate(body1, body2) // updates body1.nextPos
        } else if (body2.mass < LARGE_MASS) {
            vertical = separate(body2, body1) // updates body2.nextPos
        } else {
            Log.warn("Cannot separate $body1 from $body2")
            return
        }

        val elasticity = body1.elasticity * body2.elasticity
        val friction = 1.0f - (1.0f - body1.friction) * (1.0f - body2.friction)

        val m1 = if (body1.mass <= EPSILON) EPSILON else body1.mass
        val m2 = if (body2.mass <= EPSILON) EPSILON else body2.mass

        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y
        val p1z = body1.nextPos.z + body1.zOffset

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y
        val p2z = body2.nextPos.z + body2.zOffset

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        if (vertical) {
            // We treat this like bouncing the sphere off the XY plane.

            val vparallelDz = v1.z - v2.z
            val expectedSign = if (p1z < p2z) 1.0f else -1.0f

            if (sign(vparallelDz) != expectedSign) {
                // The two objects are separating
                return
            }

            if (m1 >= LARGE_MASS) {
                v2.z = v1.z + vparallelDz * elasticity

                if (friction > 0.0f) {
                    val vfricZ = vparallelDz * friction
                    val v2pLen = sqrt(v2.x * v2.x + v2.y * v2.y)

                    if (vfricZ >= v2pLen) {
                        v2.x = 0.0f
                        v2.y = 0.0f
                    } else {
                        v2.x -= vfricZ * v2.x / v2pLen
                        v2.y -= vfricZ * v2.y / v2pLen
                    }
                }
            } else if (m2 >= LARGE_MASS) {
                v1.z = v2.z - vparallelDz * elasticity

                if (friction > 0.0f) {
                    val vfricZ = vparallelDz * friction
                    val v1pLen = sqrt(v1.x * v1.x + v1.y * v1.y)

                    if (vfricZ >= v1pLen) {
                        v1.x = 0.0f
                        v1.y = 0.0f
                    } else {
                        v1.x -= vfricZ * v1.x / v1pLen
                        v1.y -= vfricZ * v1.y / v1pLen
                    }
                }
            } else {
                val totalMass = m1 + m2
                val sz = v1.z * m1 + v2.z * m2

                v1.z = (sz - vparallelDz * elasticity * m2) / totalMass
                v2.z = (sz + vparallelDz * elasticity * m1) / totalMass

                if (friction > 0.0f) {
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
            }
        } else {
            // We treat this like bouncing a circle off another circle in the XY plane.

            val pdx = p2x - p1x
            val pdy = p2y - p1y
            val pLen = sqrt(pdx * pdx + pdy * pdy)

            val nx = pdx / pLen
            val ny = pdy / pLen

            val v1dotn = v1.x * nx + v1.y * ny
            val v1parallelX = nx * v1dotn
            val v1parallelY = ny * v1dotn

            val v2dotn = v2.x * nx + v2.y * ny
            val v2parallelX = nx * v2dotn
            val v2parallelY = ny * v2dotn

            val vparallelDx = v1parallelX - v2parallelX
            val vparallelDy = v1parallelY - v2parallelY

            if (m1 >= LARGE_MASS) {
                val v2perpendX = v2.x - v2parallelX
                val v2perpendY = v2.y - v2parallelY
                v2.x = v2perpendX + v1parallelX + vparallelDx * elasticity
                v2.y = v2perpendY + v1parallelY + vparallelDy * elasticity

                if (friction > 0.0f) {
                    val v1perpendX = v1.x - v1parallelX
                    val v1perpendY = v1.y - v1parallelY

                    val v1perpendZ = v1.z // v1parallelZ is 0, because nz is 0
                    val v2perpendZ = v2.z

                    val vfricX = vparallelDx * friction
                    val vfricY = vparallelDy * friction
                    val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY)

                    val vperpendDx = v1perpendX - v2perpendX
                    val vperpendDy = v1perpendY - v2perpendY
                    val vperpendDz = v1perpendZ - v2perpendZ
                    val vperpendDiff = sqrt(vperpendDx * vperpendDx + vperpendDy * vperpendDy + vperpendDz * vperpendDz)

                    if (vfricLen >= vperpendDiff) {
                        v2.x += vperpendDx
                        v2.y += vperpendDy
                        v2.z += vperpendDz
                    } else {
                        val vdfricX = vfricLen * vperpendDx / vperpendDiff
                        val vdfricY = vfricLen * vperpendDy / vperpendDiff
                        val vdfricZ = vfricLen * vperpendDz / vperpendDiff

                        v2.x += vdfricX
                        v2.y += vdfricY
                        v2.z += vdfricZ
                    }
                }
            } else if (m2 >= LARGE_MASS) {
                val v1perpendX = v1.x - v1parallelX
                val v1perpendY = v1.y - v1parallelY
                v1.x = v1perpendX + v2parallelX - vparallelDx * elasticity
                v1.y = v1perpendY + v2parallelY - vparallelDy * elasticity

                if (friction > 0.0f) {
                    val v2perpendX = v2.x - v2parallelX
                    val v2perpendY = v2.y - v2parallelY

                    val v1perpendZ = v1.z // v1parallelZ is 0, because nz is 0
                    val v2perpendZ = v2.z

                    val vfricX = vparallelDx * friction
                    val vfricY = vparallelDy * friction
                    val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY)

                    val vperpendDx = v1perpendX - v2perpendX
                    val vperpendDy = v1perpendY - v2perpendY
                    val vperpendDz = v1perpendZ - v2perpendZ
                    val vperpendDiff = sqrt(vperpendDx * vperpendDx + vperpendDy * vperpendDy + vperpendDz * vperpendDz)

                    if (vfricLen >= vperpendDiff) {
                        v1.x -= vperpendDx
                        v1.y -= vperpendDy
                        v1.z -= vperpendDz
                    } else {
                        val vdfricX = vfricLen * vperpendDx / vperpendDiff
                        val vdfricY = vfricLen * vperpendDy / vperpendDiff
                        val vdfricZ = vfricLen * vperpendDz / vperpendDiff

                        v1.x -= vdfricX
                        v1.y -= vdfricY
                        v1.z -= vdfricZ
                    }
                }
            } else {
                val v1perpendX = v1.x - v1parallelX
                val v1perpendY = v1.y - v1parallelY

                val v2perpendX = v2.x - v2parallelX
                val v2perpendY = v2.y - v2parallelY

                val totalMass = m1 + m2

                val sx = v1parallelX * m1 + v2parallelX * m2
                val sy = v1parallelY * m1 + v2parallelY * m2

                v1.x = v1perpendX + (sx - vparallelDx * elasticity * m2) / totalMass
                v1.y = v1perpendY + (sy - vparallelDy * elasticity * m2) / totalMass

                v2.x = v2perpendX + (sx + vparallelDx * elasticity * m1) / totalMass
                v2.y = v2perpendY + (sy + vparallelDy * elasticity * m1) / totalMass

                if (friction > 0.0f) {
                    val v1perpendZ = v1.z // v1parallelZ is 0, because nz is 0
                    val v2perpendZ = v2.z

                    val vfricX = vparallelDx * friction
                    val vfricY = vparallelDy * friction
                    val vfricLen = sqrt(vfricX * vfricX + vfricY * vfricY)

                    val vperpendDx = v1perpendX - v2perpendX
                    val vperpendDy = v1perpendY - v2perpendY
                    val vperpendDz = v1perpendZ - v2perpendZ
                    val vperpendDiff = sqrt(vperpendDx * vperpendDx + vperpendDy * vperpendDy + vperpendDz * vperpendDz)

                    if (vfricLen >= vperpendDiff) {
                        v1.x -= m2 * vperpendDx / totalMass
                        v1.y -= m2 * vperpendDy / totalMass
                        v1.z -= m2 * vperpendDz / totalMass

                        v2.x += m1 * vperpendDx / totalMass
                        v2.y += m1 * vperpendDy / totalMass
                        v2.z += m1 * vperpendDz / totalMass
                    } else {
                        val vdfricX = vfricLen * vperpendDx / vperpendDiff
                        val vdfricY = vfricLen * vperpendDy / vperpendDiff
                        val vdfricZ = vfricLen * vperpendDz / vperpendDiff

                        v1.x -= m2 * vdfricX / totalMass
                        v1.y -= m2 * vdfricY / totalMass
                        v1.z -= m2 * vdfricZ / totalMass

                        v2.x += m1 * vdfricX / totalMass
                        v2.y += m1 * vdfricY / totalMass
                        v2.z += m1 * vdfricZ / totalMass
                    }
                }
            }
        }
    }

    /**
     * Updates body1.nextPos such that it no longer collides with body2.
     * @return whether or not the hit was vertical
     */
    private fun separate(sphere: FixedSphereBody, cylinder: FixedCylinderBody): Boolean {
        val p1x = sphere.nextPos.x
        val p1y = sphere.nextPos.y
        val p1z = sphere.nextPos.z + sphere.zOffset

        val p2x = cylinder.nextPos.x
        val p2y = cylinder.nextPos.y
        val p2z = cylinder.nextPos.z + cylinder.zOffset

        val minTop = min(p1z + sphere.radius, p2z + cylinder.height / 2)
        val maxBottom = max(p1z - sphere.radius, p2z - cylinder.height / 2)
        val overlapHeight = minTop - maxBottom
        val vertical = overlapHeight < VERTICAL_HIT_OVERLAP_HEIGHT_THRESHOLD

        if (vertical) {
            // Separate the bodies along the z-axis
            val moveBy = sphere.radius + cylinder.height * 0.5f + 2 * EPSILON

            if (p1z < p2z) {
                sphere.nextPos.z = cylinder.nextPos.z + cylinder.zOffset - moveBy - sphere.zOffset
            } else {
                sphere.nextPos.z = cylinder.nextPos.z + cylinder.zOffset + moveBy - sphere.zOffset
            }
        } else {
            // Separate the bodies in the XY plane
            val dx = p2x - p1x
            val dy = p2y - p1y
            val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
            val dlen = sqrt(dsqr)
            val nx = dx / dlen
            val ny = dy / dlen
            val moveBy = sphere.radius + cylinder.radius + 2 * EPSILON
            sphere.nextPos.x = cylinder.nextPos.x - nx * moveBy
            sphere.nextPos.y = cylinder.nextPos.y - ny * moveBy
        }

        return vertical
    }

    /**
     * Updates body1.nextPos such that it no longer collides with body2.
     * @return whether or not the hit was vertical
     */
    private fun separate(cylinder: FixedCylinderBody, sphere: FixedSphereBody): Boolean {
        val p1x = cylinder.nextPos.x
        val p1y = cylinder.nextPos.y
        val p1z = cylinder.nextPos.z + cylinder.zOffset

        val p2x = sphere.nextPos.x
        val p2y = sphere.nextPos.y
        val p2z = sphere.nextPos.z + sphere.zOffset

        val dx = p2x - p1x
        val dy = p2y - p1y
        val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val vertical = dsqr <= cylinder.radius * cylinder.radius

        if (vertical) {
            // Separate the bodies along the z-axis
            val moveBy = sphere.radius + cylinder.height * 0.5f + 2 * EPSILON

            if (p1z < p2z) {
                cylinder.nextPos.z = sphere.nextPos.z + sphere.zOffset - moveBy - cylinder.zOffset
            } else {
                cylinder.nextPos.z = sphere.nextPos.z + sphere.zOffset + moveBy - cylinder.zOffset
            }
        } else {
            // Separate the bodies in the XY plane
            val dlen = sqrt(dsqr)
            val nx = dx / dlen
            val ny = dy / dlen
            val moveBy = sphere.radius + cylinder.radius + 2 * EPSILON
            cylinder.nextPos.x = sphere.nextPos.x - nx * moveBy
            cylinder.nextPos.y = sphere.nextPos.y - ny * moveBy
        }

        return vertical
    }

    companion object {
        private const val VERTICAL_HIT_OVERLAP_HEIGHT_THRESHOLD = 0.05f
    }
}

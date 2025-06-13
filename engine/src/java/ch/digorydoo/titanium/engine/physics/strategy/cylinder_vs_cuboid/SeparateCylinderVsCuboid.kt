package ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cuboid

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.CollisionSeparateStrategy
import ch.digorydoo.titanium.engine.utils.TINY_GAP
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

internal class SeparateCylinderVsCuboid: CollisionSeparateStrategy<FixedCylinderBody, FixedCuboidBody>() {
    override fun separate(body1: FixedCylinderBody, body2: FixedCuboidBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP_FACE -> {
                val normDir12Z = -1.0f // points from body1 to body2
                separateVertically(body1, body2, normDir12Z)
            }
            HitArea.BOTTOM_FACE -> {
                val normDir12Z = 1.0f // points from body1 to body2
                separateVertically(body1, body2, normDir12Z)
            }
            else -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y
                separateHorizontally(body1, body2, normDir12X, normDir12Y)
            }
        }
    }

    private fun separateVertically(cylinder: FixedCylinderBody, cuboid: FixedCuboidBody, normDir12Z: Float) {
        val p1 = cylinder.nextPos
        val p2 = cuboid.nextPos

        val currentDistance = (p2.z - p1.z) * normDir12Z
        val requiredDistance = cylinder.height / 2.0f + cuboid.halfSizeZ + TINY_GAP
        val moveBy = requiredDistance - currentDistance

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separateVertically was called, but bodies seem to be vertically separated already")
            return
        }

        when {
            cylinder.mass < LARGE_MASS -> when {
                cuboid.mass < LARGE_MASS -> {
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
            cuboid.mass < LARGE_MASS -> {
                p2.z += normDir12Z * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $cylinder from $cuboid failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    private fun separateHorizontally(
        body1: FixedCylinderBody,
        body2: FixedCuboidBody,
        normDir12X: Float,
        normDir12Y: Float,
    ) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos
        val cylinderRadius = body1.radius

        // Compute the position of the circle if we moved it completely to the other side of the plane.

        val distanceAlongNormal = (p2.x - p1.x) * normDir12X + (p2.y - p1.y) * normDir12Y
        val halfSizeDotN = body2.halfSizeX * normDir12X + body2.halfSizeY * normDir12Y
        val requiredDistanceAlongNormal = cylinderRadius + abs(halfSizeDotN)
        var moveBy = requiredDistanceAlongNormal - distanceAlongNormal

        if (moveBy + TINY_GAP <= 0.0f) {
            Log.warn(TAG, "separate was called, but bodies seem to be separated already")
            return
        }

        val m1x = p1.x - normDir12X * moveBy
        val m1y = p1.y - normDir12Y * moveBy

        // If the circle is close to a cuboid corner, we may have moved it too far.

        val distanceToPlane = distanceAlongNormal - abs(halfSizeDotN)
        val sphereCentreProjectedOntoPlaneX = p1.x + distanceToPlane * normDir12X
        val sphereCentreProjectedOntoPlaneY = p1.y + distanceToPlane * normDir12Y

        val clampedX = clamp(sphereCentreProjectedOntoPlaneX, p2.x - body2.halfSizeX, p2.x + body2.halfSizeX)
        val clampedY = clamp(sphereCentreProjectedOntoPlaneY, p2.y - body2.halfSizeY, p2.y + body2.halfSizeY)

        val m1cx = m1x - clampedX
        val m1cy = m1y - clampedY
        val sqrDistOfM1ToClamped = m1cx * m1cx + m1cy * m1cy
        val distanceOfM1ToClamped = sqrt(sqrDistOfM1ToClamped)
        val m1TooFar = distanceOfM1ToClamped - cylinderRadius

        if (m1TooFar > TINY_GAP) {
            // See physics.txt: "Separating a sphere and a cuboid"

            val m1dotn = m1x * normDir12X + m1y * normDir12Y
            val pcdotn = clampedX * normDir12X + clampedY * normDir12Y
            val b = 2.0f * (m1dotn - pcdotn)
            val c = sqrDistOfM1ToClamped - cylinderRadius * cylinderRadius
            val discriminant = b * b - 4 * c // A=1

            if (discriminant < 0.0f) {
                Log.warn(TAG, "Quadratic equation has no solution, probably a bug")
            } else {
                val sqrDiscr = sqrt(discriminant)
                val q1 = (-b + sqrDiscr) / 2.0f
                val q2 = (-b - sqrDiscr) / 2.0f

                // We take the smaller q, because the other solution is on the other side of the plane.
                val q = min(q1, q2)

                if (q < 0.0f) {
                    Log.warn(TAG, "q is negative, probably a bug")
                } else {
                    moveBy -= q
                }
            }
        }

        moveBy += TINY_GAP

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
        private val TAG = Log.Tag("SeparateCylinderVsCuboid")
    }
}

package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cylinder

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.CollisionSeparateStrategy
import ch.digorydoo.titanium.engine.utils.TINY_GAP

internal class SeparateSphereVsCylinder: CollisionSeparateStrategy<FixedSphereBody, FixedCylinderBody>() {
    override fun separate(body1: FixedSphereBody, body2: FixedCylinderBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP_FACE,
            HitArea.BOTTOM_FACE,
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
    }

    private fun separateInZ(sphere: FixedSphereBody, cylinder: FixedCylinderBody, normDir12Z: Float) {
        val p1 = sphere.nextPos
        val p2 = cylinder.nextPos

        val requiredDistance = sphere.radius + cylinder.height / 2.0f + TINY_GAP
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

        val requiredDistance = body1.radius + body2.radius + TINY_GAP
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
        private val TAG = Log.Tag("SeparateSphereVsCylinder")
    }
}

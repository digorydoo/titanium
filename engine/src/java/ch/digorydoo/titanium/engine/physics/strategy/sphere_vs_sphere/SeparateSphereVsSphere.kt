package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_sphere

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.CollisionSeparateStrategy
import ch.digorydoo.titanium.engine.utils.TINY_GAP

internal class SeparateSphereVsSphere: CollisionSeparateStrategy<FixedSphereBody, FixedSphereBody>() {
    override fun separate(body1: FixedSphereBody, body2: FixedSphereBody, hit: HitResult) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos

        val normDir12 = hit.hitNormal12
        val normDir12X = normDir12.x
        val normDir12Y = normDir12.y
        val normDir12Z = normDir12.z

        val requiredDistance = body1.radius + body2.radius + TINY_GAP
        val distanceAlongNormal = (p2.x - p1.x) * normDir12X + (p2.y - p1.y) * normDir12Y + (p2.z - p1.z) * normDir12Z
        val moveBy = requiredDistance - distanceAlongNormal

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separate was called, but bodies seem to be separated already")
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
                    p1.z -= normDir12Z * move1By

                    p2.x += normDir12X * move2By
                    p2.y += normDir12Y * move2By
                    p2.z += normDir12Z * move2By
                }
                else -> {
                    p1.x -= normDir12X * moveBy
                    p1.y -= normDir12Y * moveBy
                    p1.z -= normDir12Z * moveBy
                }
            }
            body2.mass < LARGE_MASS -> {
                p2.x += normDir12X * moveBy
                p2.y += normDir12Y * moveBy
                p2.z += normDir12Z * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $body1 from $body2 failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("SeparateSphereVsSphere")
    }
}

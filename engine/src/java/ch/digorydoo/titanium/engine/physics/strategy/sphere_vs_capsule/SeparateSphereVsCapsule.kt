package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_capsule

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.CollisionSeparateStrategy
import ch.digorydoo.titanium.engine.utils.TINY_GAP

internal class SeparateSphereVsCapsule: CollisionSeparateStrategy<FixedSphereBody, FixedCapsuleBody>() {
    override fun separate(body1: FixedSphereBody, body2: FixedCapsuleBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP_SPHERE -> separateTopBottom(
                body1,
                body2,
                body2.nextPos.z + body2.height / 2 - body2.radius,
                hit.hitNormal12
            )
            HitArea.BOTTOM_SPHERE -> separateTopBottom(
                body1,
                body2,
                body2.nextPos.z - body2.height / 2 + body2.radius,
                hit.hitNormal12
            )
            else -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y
                separateInXY(body1, body2, normDir12X, normDir12Y)
            }
        }
    }

    private fun separateTopBottom(
        sphere: FixedSphereBody,
        capsule: FixedCapsuleBody,
        capsuleEndZ: Float,
        normDir12: Point3f,
    ) {
        val sphereCentreX = sphere.nextPos.x
        val sphereCentreY = sphere.nextPos.y
        val sphereCentreZ = sphere.nextPos.z

        val capsuleEndX = capsule.nextPos.x
        val capsuleEndY = capsule.nextPos.y

        val normDir12X = normDir12.x
        val normDir12Y = normDir12.y
        val normDir12Z = normDir12.z

        val requiredDistance = sphere.radius + capsule.radius + TINY_GAP

        val distanceAlongNormal = (capsuleEndX - sphereCentreX) * normDir12X +
            (capsuleEndY - sphereCentreY) * normDir12Y +
            (capsuleEndZ - sphereCentreZ) * normDir12Z

        val moveBy = requiredDistance - distanceAlongNormal

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separate was called, but bodies seem to be separated already")
            return
        }

        when {
            sphere.mass < LARGE_MASS -> when {
                capsule.mass < LARGE_MASS -> {
                    // Do not distribute the distance by mass! If the lighter object is cornered, the CollisionManager
                    // would have trouble moving the heavier object away!
                    val move1By = moveBy * 0.5f
                    val move2By = moveBy - move1By

                    sphere.nextPos.apply {
                        x -= normDir12X * move1By
                        y -= normDir12Y * move1By
                        z -= normDir12Z * move1By
                    }

                    capsule.nextPos.apply {
                        x += normDir12X * move2By
                        y += normDir12Y * move2By
                        z += normDir12Z * move2By
                    }
                }
                else -> {
                    sphere.nextPos.apply {
                        x -= normDir12X * moveBy
                        y -= normDir12Y * moveBy
                        z -= normDir12Z * moveBy
                    }
                }
            }
            capsule.mass < LARGE_MASS -> {
                capsule.nextPos.apply {
                    x += normDir12X * moveBy
                    y += normDir12Y * moveBy
                    z += normDir12Z * moveBy
                }
            }
            else -> {
                Log.warn(TAG, "Separating $sphere from $capsule failed, because both bodies are LARGE_MASS")
                return
            }
        }

    }

    private fun separateInXY(
        body1: FixedSphereBody,
        body2: FixedCapsuleBody,
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
        private val TAG = Log.Tag("SeparateSphereVsCapsule")
    }
}

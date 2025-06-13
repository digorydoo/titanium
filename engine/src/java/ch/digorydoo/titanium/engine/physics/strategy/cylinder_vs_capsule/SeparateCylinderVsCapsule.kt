package ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_capsule

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.UnexpectedHitAreaError
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.CollisionSeparateStrategy
import ch.digorydoo.titanium.engine.utils.TINY_GAP
import kotlin.math.sqrt

internal class SeparateCylinderVsCapsule: CollisionSeparateStrategy<FixedCylinderBody, FixedCapsuleBody>() {
    override fun separate(body1: FixedCylinderBody, body2: FixedCapsuleBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP_TIP -> separateWhenCapsuleTipWasHit(
                body1,
                body2,
                body2.nextPos.z + body2.height / 2 - body2.radius,
                hit.hitNormal12.z
            )
            HitArea.BOTTOM_TIP -> separateWhenCapsuleTipWasHit(
                body1,
                body2,
                body2.nextPos.z - body2.height / 2 + body2.radius,
                hit.hitNormal12.z
            )
            HitArea.TOP_SPHERE -> separateWhenCylinderEndEdgeWasHit(
                body1,
                body1.nextPos.z - body1.height / 2,
                body2,
                body2.nextPos.z + body2.height / 2 - body2.radius,
                hit.hitNormal12
            )
            HitArea.BOTTOM_SPHERE -> separateWhenCylinderEndEdgeWasHit(
                body1,
                body1.nextPos.z + body1.height / 2,
                body2,
                body2.nextPos.z - body2.height / 2 + body2.radius,
                hit.hitNormal12
            )
            HitArea.SIDE -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y
                separateInXY(body1, body2, normDir12X, normDir12Y)
            }
            else -> throw UnexpectedHitAreaError(hit.area2)
        }
    }

    private fun separateWhenCapsuleTipWasHit(
        cylinder: FixedCylinderBody,
        capsule: FixedCapsuleBody,
        capsuleEndSphereCentreZ: Float,
        normDir12Z: Float,
    ) {
        val cylinderPos = cylinder.nextPos
        val capsulePos = capsule.nextPos

        val requiredDistance = capsule.radius + cylinder.height / 2.0f + TINY_GAP
        val distanceAlongNormDir = (capsuleEndSphereCentreZ - cylinderPos.z) * normDir12Z
        val moveBy = requiredDistance - distanceAlongNormDir

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separate was called, but bodies seem to be vertically separated already")
            return
        }

        when {
            cylinder.mass < LARGE_MASS -> when {
                capsule.mass < LARGE_MASS -> {
                    // Each object moves half the distance independent from mass.
                    val move1By = moveBy * 0.5f
                    val move2By = moveBy - move1By
                    cylinderPos.z -= normDir12Z * move1By
                    capsulePos.z += normDir12Z * move2By
                }
                else -> {
                    cylinderPos.z -= normDir12Z * moveBy
                }
            }
            capsule.mass < LARGE_MASS -> {
                capsulePos.z += normDir12Z * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $cylinder from $capsule failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    private fun separateWhenCylinderEndEdgeWasHit(
        cylinder: FixedCylinderBody,
        cylinderEndZ: Float,
        capsule: FixedCapsuleBody,
        capsuleEndSphereCentreZ: Float,
        normDir12: Point3f,
    ) {
        val cylinderCentreX = cylinder.nextPos.x
        val cylinderCentreY = cylinder.nextPos.y
        val cylinderRadius = cylinder.radius

        val capsuleEndX = capsule.nextPos.x
        val capsuleEndY = capsule.nextPos.y

        val normDir12X = normDir12.x
        val normDir12Y = normDir12.y
        val normDir12Z = normDir12.z

        val dx = capsuleEndX - cylinderCentreX
        val dy = capsuleEndY - cylinderCentreY
        val distXY = sqrt(dx * dx + dy * dy)
        val closestX1 = cylinderCentreX + cylinderRadius * dx / distXY
        val closestY1 = cylinderCentreY + cylinderRadius * dy / distXY

        val distanceAlongNormal = (capsuleEndX - closestX1) * normDir12X +
            (capsuleEndY - closestY1) * normDir12Y +
            (capsuleEndSphereCentreZ - cylinderEndZ) * normDir12Z

        val requiredDistance = capsule.radius + TINY_GAP
        val moveBy = requiredDistance - distanceAlongNormal

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separate was called, but bodies seem to be separated already")
            return
        }

        when {
            cylinder.mass < LARGE_MASS -> when {
                capsule.mass < LARGE_MASS -> {
                    // Do not distribute the distance by mass! If the lighter object is cornered, the CollisionManager
                    // would have trouble moving the heavier object away!
                    val move1By = moveBy * 0.5f
                    val move2By = moveBy - move1By

                    cylinder.nextPos.apply {
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
                    cylinder.nextPos.apply {
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
                Log.warn(TAG, "Separating $cylinder from $capsule failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    private fun separateInXY(
        cylinder: FixedCylinderBody,
        capsule: FixedCapsuleBody,
        normDir12X: Float,
        normDir12Y: Float,
    ) {
        val p1 = cylinder.nextPos
        val p2 = capsule.nextPos

        val requiredDistance = cylinder.radius + capsule.radius + TINY_GAP
        val distanceAlongNormDir = (p2.x - p1.x) * normDir12X + (p2.y - p1.y) * normDir12Y
        val moveBy = requiredDistance - distanceAlongNormDir

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separateInXY was called, but bodies seem to be vertically separated already")
            return
        }

        when {
            cylinder.mass < LARGE_MASS -> when {
                capsule.mass < LARGE_MASS -> {
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
            capsule.mass < LARGE_MASS -> {
                p2.x += normDir12X * moveBy
                p2.y += normDir12Y * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $cylinder from $capsule failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("SeparateCylinderVsCapsule")
    }
}

package ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_cuboid

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.CollisionSeparateStrategy
import ch.digorydoo.titanium.engine.utils.EPSILON
import ch.digorydoo.titanium.engine.utils.TINY_GAP
import kotlin.math.sqrt

internal class SeparateCapsuleVsCuboid: CollisionSeparateStrategy<FixedCapsuleBody, FixedCuboidBody>() {
    override fun separate(body1: FixedCapsuleBody, body2: FixedCuboidBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP_FACE -> separateVertically(
                body1,
                body2,
                hit.hitPt,
                hit.hitNormal12,
                body1.nextPos.z - body1.height / 2 + body1.radius // bottom end
            )
            HitArea.BOTTOM_FACE -> separateVertically(
                body1,
                body2,
                hit.hitPt,
                hit.hitNormal12,
                body1.nextPos.z + body1.height / 2 - body1.radius // top end
            )
            else -> {
                // separateHorizontally assumes capsule end will always be assigned to a vertical face,
                // even though technically the edge between the faces is hit.
                require(hit.area1 == HitArea.SIDE) { "Unexpected cuboid ${hit.area2} vs capsule ${hit.area1}" }

                val nx = hit.hitNormal12.x
                val ny = hit.hitNormal12.y
                separateHorizontally(body1, body2, hit.hitPt, nx, ny)
            }
        }
    }

    private fun separateVertically(
        capsule: FixedCapsuleBody,
        cuboid: FixedCuboidBody,
        hitPt: Point3f,
        hitNormal12: Point3f,
        capsuleEndSphereCentreZ: Float,
    ) {
        val capsuleX = capsule.nextPos.x
        val capsuleY = capsule.nextPos.y

        // The hitNormal12 will not be a negative face normal when the edge of the cuboid was hit.
        val nx = hitNormal12.x
        val ny = hitNormal12.y
        val nz = hitNormal12.z

        val distanceAlongNormal = (hitPt.x - capsuleX) * nx +
            (hitPt.y - capsuleY) * ny +
            (hitPt.z - capsuleEndSphereCentreZ) * nz

        val requiredDistance = capsule.radius + TINY_GAP
        val moveBy = requiredDistance - distanceAlongNormal

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separateVertically was called, but bodies seem to be separated already")
            return
        }

        when {
            cuboid.mass < LARGE_MASS -> when {
                capsule.mass < LARGE_MASS -> {
                    // Do not distribute the distance by mass! If the lighter object is cornered, the CollisionManager
                    // would have trouble moving the heavier object away!
                    val move1By = moveBy * 0.5f
                    val move2By = moveBy - move1By

                    cuboid.nextPos.apply {
                        x += nx * move1By
                        y += ny * move1By
                        z += nz * move1By
                    }

                    capsule.nextPos.apply {
                        x -= nx * move2By
                        y -= ny * move2By
                        z -= nz * move2By
                    }
                }
                else -> {
                    cuboid.nextPos.apply {
                        x += nx * moveBy
                        y += ny * moveBy
                        z += nz * moveBy
                    }
                }
            }
            capsule.mass < LARGE_MASS -> {
                capsule.nextPos.apply {
                    x -= nx * moveBy
                    y -= ny * moveBy
                    z -= nz * moveBy
                }
            }
            else -> {
                Log.warn(TAG, "Separating $cuboid from $capsule failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    private fun separateHorizontally(
        capsule: FixedCapsuleBody,
        cuboid: FixedCuboidBody,
        hitPt: Point3f,
        normDir12X: Float,
        normDir12Y: Float,
    ) {
        val p1 = capsule.nextPos
        val p2 = cuboid.nextPos

        val r1 = capsule.radius

        val dx = hitPt.x - p1.x
        val dy = hitPt.y - p1.y
        val len = sqrt(dx * dx + dy * dy)

        val dirTowardsHitX: Float
        val dirTowardsHitY: Float

        if (len <= EPSILON) {
            dirTowardsHitX = normDir12X
            dirTowardsHitY = normDir12Y
        } else {
            dirTowardsHitX = dx / len
            dirTowardsHitY = dy / len
        }

        val beyondHitX = dirTowardsHitX * r1
        val beyondHitY = dirTowardsHitY * r1
        val beyondHitDotN = beyondHitX * normDir12X + beyondHitY * normDir12Y
        val towardsHitPtDotN = dx * normDir12X + dy * normDir12Y
        var moveBy = beyondHitDotN - towardsHitPtDotN

        if (moveBy + TINY_GAP <= 0.0f) {
            Log.warn(TAG, "separateHorizontally was called, but bodies seem to be separated already, moveBy=$moveBy")
            return
        }

        moveBy += TINY_GAP

        when {
            capsule.mass < LARGE_MASS -> when {
                cuboid.mass < LARGE_MASS -> {
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
            cuboid.mass < LARGE_MASS -> {
                p2.x += normDir12X * moveBy
                p2.y += normDir12Y * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $capsule from $cuboid failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("SeparateCapsuleVsCuboid")
    }
}

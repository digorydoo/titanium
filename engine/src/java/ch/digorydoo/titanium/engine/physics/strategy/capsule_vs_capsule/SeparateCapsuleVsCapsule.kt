package ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_capsule

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.UnexpectedHitAreaError
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.CollisionSeparateStrategy
import ch.digorydoo.titanium.engine.utils.TINY_GAP

internal class SeparateCapsuleVsCapsule: CollisionSeparateStrategy<FixedCapsuleBody, FixedCapsuleBody>() {
    override fun separate(body1: FixedCapsuleBody, body2: FixedCapsuleBody, hit: HitResult) {
        when (hit.area1) {
            HitArea.TOP_SPHERE -> separateWhenEndsCollide(
                body1,
                body1.nextPos.z + body1.height / 2 - body1.radius,
                body2,
                body2.nextPos.z - body2.height / 2 + body2.radius,
                hit.hitNormal12
            )
            HitArea.BOTTOM_SPHERE -> separateWhenEndsCollide(
                body1,
                body1.nextPos.z - body1.height / 2 + body1.radius,
                body2,
                body2.nextPos.z + body2.height / 2 - body2.radius,
                hit.hitNormal12
            )
            HitArea.SIDE -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y
                separateInXY(body1, body2, normDir12X, normDir12Y)
            }
            else -> throw UnexpectedHitAreaError(hit.area1)
        }
    }

    private fun separateWhenEndsCollide(
        body1: FixedCapsuleBody,
        sphereCentreZ1: Float,
        body2: FixedCapsuleBody,
        sphereCentreZ2: Float,
        normDir12: Point3f,
    ) {
        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y

        val nx = normDir12.x
        val ny = normDir12.y
        val nz = normDir12.z

        val requiredDistance = body1.radius + body2.radius + TINY_GAP
        val distanceAlongNormal = (p2x - p1x) * nx + (p2y - p1y) * ny + (sphereCentreZ2 - sphereCentreZ1) * nz
        val moveBy = requiredDistance - distanceAlongNormal

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separate was called, but bodies seem to be separated already")
            return
        }

        when {
            body1.mass < LARGE_MASS -> when {
                body2.mass < LARGE_MASS -> {
                    val move1By = moveBy * 0.5f // distribute evenly, not by mass
                    val move2By = moveBy - move1By

                    body1.nextPos.apply {
                        x -= nx * move1By
                        y -= ny * move1By
                        z -= nz * move1By
                    }
                    body2.nextPos.apply {
                        x += nx * move2By
                        y += ny * move2By
                        z += nz * move2By
                    }
                }
                else -> {
                    body1.nextPos.apply {
                        x -= nx * moveBy
                        y -= ny * moveBy
                        z -= nz * moveBy
                    }
                }
            }
            body2.mass < LARGE_MASS -> {
                body2.nextPos.apply {
                    x += nx * moveBy
                    y += ny * moveBy
                    z += nz * moveBy
                }
            }
            else -> {
                Log.warn(TAG, "Separating $body1 from $body2 failed, because both bodies are LARGE_MASS")
                return
            }
        }

    }

    private fun separateInXY(
        body1: FixedCapsuleBody,
        body2: FixedCapsuleBody,
        nx: Float,
        ny: Float,
    ) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos

        val requiredDistance = body1.radius + body2.radius + TINY_GAP
        val distanceAlongNormDir = (p2.x - p1.x) * nx + (p2.y - p1.y) * ny
        val moveBy = requiredDistance - distanceAlongNormDir

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separateInXY was called, but bodies seem to be vertically separated already")
            return
        }

        when {
            body1.mass < LARGE_MASS -> when {
                body2.mass < LARGE_MASS -> {
                    val move1By = moveBy * 0.5f // distribute evenly, not by mass
                    val move2By = moveBy - move1By

                    p1.x -= nx * move1By
                    p1.y -= ny * move1By

                    p2.x += nx * move2By
                    p2.y += ny * move2By
                }
                else -> {
                    p1.x -= nx * moveBy
                    p1.y -= ny * moveBy
                }
            }
            body2.mass < LARGE_MASS -> {
                p2.x += nx * moveBy
                p2.y += ny * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $body1 from $body2 failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("SeparateCapsuleVsCapsule")
    }
}

package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

internal class CollideSphereVsSphere: CollisionStrategy<FixedSphereBody, FixedSphereBody>() {
    override fun configure(
        body1IsBrick: Boolean,
        body2IsBrick: Boolean,
        bricks: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
    ) {
        if (body1IsBrick) throw NotImplementedError()
        if (body2IsBrick) throw NotImplementedError()
    }

    override fun check(
        body1: FixedSphereBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body2: FixedSphereBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        outHit: MutableHitResult?,
    ): Boolean {
        val dx = centreX2 - centreX1
        val dy = centreY2 - centreY1
        val dz = centreZ2 - centreZ1
        val dsqr = (dx * dx) + (dy * dy) + (dz * dz)
        val rsum = body1.radius + body2.radius
        if (dsqr > rsum * rsum) return false
        if (outHit == null) return true

        val d = sqrt(dsqr) // always >= 0

        if (d <= EPSILON) {
            Log.warn(TAG, "Setting normal to a random direction, because bodies $body1 and $body2 are too close")
            val r = Random.nextFloat()
            outHit.apply {
                hitPt.set(centreX1, centreY1, centreZ1)
                hitNormal12.set(cos(r), sin(r), 0.0f)
            }
        } else {
            outHit.apply {
                hitPt.set(
                    centreX1 + body1.radius * (dx / d),
                    centreY1 + body1.radius * (dy / d),
                    centreZ1 + body1.radius * (dz / d),
                )
                hitNormal12.set(dx / d, dy / d, dz / d)
            }
        }

        // Spheres do not have specific areas
        outHit.area1 = HitArea.UNSPECIFIED
        outHit.area2 = HitArea.UNSPECIFIED
        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedSphereBody, hit: HitResult) {
        separate(body1, body2, hit.hitNormal12)

        val p1x = body1.nextPos.x
        val p1y = body1.nextPos.y
        val p1z = body1.nextPos.z

        val p2x = body2.nextPos.x
        val p2y = body2.nextPos.y
        val p2z = body2.nextPos.z

        val pdx = p2x - p1x
        val pdy = p2y - p1y
        val pdz = p2z - p1z
        val pLen = sqrt(pdx * pdx + pdy * pdy + pdz * pdz)

        if (pLen < EPSILON) {
            Log.warn(TAG, "Failed to compute new speeds for $body1 and $body2, because they are too close")
            return
        }

        val normDir12X = pdx / pLen
        val normDir12Y = pdy / pLen
        val normDir12Z = pdz / pLen

        helper.apply {
            applyFriction(body1, body2, normDir12X, normDir12Y, normDir12Z)
            bounceAtPlane(body1, body2, normDir12X, normDir12Y, normDir12Z)
        }

        verifySeparation(body1, body2, hit)
    }

    private fun separate(body1: FixedSphereBody, body2: FixedSphereBody, normDir12: Point3f) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos

        val normDir12X = normDir12.x
        val normDir12Y = normDir12.y
        val normDir12Z = normDir12.z

        // Some tests are flaky unless we're adding here more than EPSILON (must be float inaccuracies)
        val requiredDistance = body1.radius + body2.radius + 6.0f * EPSILON
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
        private val TAG = Log.Tag("CollideSphereVsSphere")
    }
}

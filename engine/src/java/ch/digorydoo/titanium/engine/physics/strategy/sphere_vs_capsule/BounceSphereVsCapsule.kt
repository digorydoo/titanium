package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_capsule

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionBounceStrategy
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

internal class BounceSphereVsCapsule: CollisionBounceStrategy<FixedSphereBody, FixedCapsuleBody>() {
    override fun bounce(body1: FixedSphereBody, body2: FixedCapsuleBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP_SPHERE -> computeNextSpeedTopBottom(
                body1,
                body2,
                body2.nextPos.z + body2.height / 2 - body2.radius,
            )
            HitArea.BOTTOM_SPHERE -> computeNextSpeedTopBottom(
                body1,
                body2,
                body2.nextPos.z - body2.height / 2 + body2.radius,
            )
            else -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y
                applyFriction(body1, body2, normDir12X, normDir12Y, 0.0f)
                bounceAtVerticalPlane(body1, body2, normDir12X, normDir12Y)
            }
        }
    }

    private fun computeNextSpeedTopBottom(sphere: FixedSphereBody, capsule: FixedCapsuleBody, capsuleEndZ: Float) {
        val p1x = sphere.nextPos.x
        val p1y = sphere.nextPos.y
        val p1z = sphere.nextPos.z

        val p2x = capsule.nextPos.x
        val p2y = capsule.nextPos.y

        val pdx = p2x - p1x
        val pdy = p2y - p1y
        val pdz = capsuleEndZ - p1z
        val pLen = sqrt(pdx * pdx + pdy * pdy + pdz * pdz)

        if (pLen < EPSILON) {
            Log.warn(TAG, "Failed to compute new speeds for $sphere and $capsule, because they are too close")
            return
        }

        val normDir12X = pdx / pLen
        val normDir12Y = pdy / pLen
        val normDir12Z = pdz / pLen
        applyFriction(sphere, capsule, normDir12X, normDir12Y, normDir12Z)
        bounceAtPlane(sphere, capsule, normDir12X, normDir12Y, normDir12Z)
    }

    companion object {
        private val TAG = Log.Tag("BounceSphereVsCapsule")
    }
}

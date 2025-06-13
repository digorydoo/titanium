package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_sphere

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionBounceStrategy
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

internal class BounceSphereVsSphere: CollisionBounceStrategy<FixedSphereBody, FixedSphereBody>() {
    override fun bounce(body1: FixedSphereBody, body2: FixedSphereBody, hit: HitResult) {
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
        applyFriction(body1, body2, normDir12X, normDir12Y, normDir12Z)
        bounceAtPlane(body1, body2, normDir12X, normDir12Y, normDir12Z)
    }

    companion object {
        private val TAG = Log.Tag("BounceSphereVsSphere")
    }
}

package ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_capsule

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.UnexpectedHitAreaError
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionBounceStrategy
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

internal class BounceCapsuleVsCapsule: CollisionBounceStrategy<FixedCapsuleBody, FixedCapsuleBody>() {
    override fun bounce(body1: FixedCapsuleBody, body2: FixedCapsuleBody, hit: HitResult) {
        when (hit.area1) {
            HitArea.TOP_SPHERE -> computeNextSpeedWhenEndsCollide(
                body1,
                body1.nextPos.z + body1.height / 2 - body1.radius,
                body2,
                body2.nextPos.z - body2.height / 2 + body2.radius,
            )
            HitArea.BOTTOM_SPHERE -> computeNextSpeedWhenEndsCollide(
                body1,
                body1.nextPos.z - body1.height / 2 + body1.radius,
                body2,
                body2.nextPos.z + body2.height / 2 - body2.radius,
            )
            HitArea.SIDE -> {
                val nx = hit.hitNormal12.x
                val ny = hit.hitNormal12.y

                applyFriction(body1, body2, nx, ny, 0.0f)
                bounceAtVerticalPlane(body1, body2, nx, ny)
            }
            else -> throw UnexpectedHitAreaError(hit.area1)
        }
    }

    private fun computeNextSpeedWhenEndsCollide(
        body1: FixedCapsuleBody,
        sphereCentreZ1: Float,
        body2: FixedCapsuleBody,
        sphereCentreZ2: Float,
    ) {
        val dx = body2.nextPos.x - body1.nextPos.x
        val dy = body2.nextPos.y - body1.nextPos.y
        val dz = sphereCentreZ2 - sphereCentreZ1
        val len = sqrt(dx * dx + dy * dy + dz * dz)

        if (len <= EPSILON) {
            Log.warn(TAG, "Failed to compute new speeds for $body1 and $body2, because they are too close")
            return
        }

        val nx = dx / len
        val ny = dy / len
        val nz = dz / len

        applyFriction(body1, body2, nx, ny, nz)
        bounceAtPlane(body1, body2, nx, ny, nz)
    }

    companion object {
        private val TAG = Log.Tag("BounceCapsuleVsCapsule")
    }
}

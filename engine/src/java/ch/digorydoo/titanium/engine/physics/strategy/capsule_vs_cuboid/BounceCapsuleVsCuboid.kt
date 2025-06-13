package ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_cuboid

import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionBounceStrategy

internal class BounceCapsuleVsCuboid: CollisionBounceStrategy<FixedCapsuleBody, FixedCuboidBody>() {
    override fun bounce(body1: FixedCapsuleBody, body2: FixedCuboidBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP_FACE, HitArea.BOTTOM_FACE -> {
                val n = hit.hitNormal12 // not an up/down vector when an edge was hit
                applyFriction(body1, body2, n.x, n.y, n.z)
                bounceAtPlane(body1, body2, n.x, n.y, n.z)
            }
            else -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y

                applyFriction(body1, body2, normDir12X, normDir12Y, 0.0f)
                bounceAtVerticalPlane(body1, body2, normDir12X, normDir12Y)
            }
        }
    }
}

package ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_capsule

import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.UnexpectedHitAreaError
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionBounceStrategy

internal class BounceCylinderVsCapsule: CollisionBounceStrategy<FixedCylinderBody, FixedCapsuleBody>() {
    override fun bounce(body1: FixedCylinderBody, body2: FixedCapsuleBody, hit: HitResult) {
        // We're checking the cylinder's hit area here, so TOP_TIP and BOTTOM_TIP will not be seen here.
        when (hit.area1) {
            HitArea.TOP_FACE -> {
                val normDir12Z = hit.hitNormal12.z
                applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                val deltaSpeedZ = body1.speed.z - body2.speed.z // positive if capsule falls down on cylinder

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent capsule standing on cylinder from constantly hopping due to gravity
                    body2.nextSpeed.z = body1.nextSpeed.z
                }

                bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            HitArea.BOTTOM_FACE -> {
                val normDir12Z = hit.hitNormal12.z
                applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)
                bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            HitArea.SIDE -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y

                applyFriction(body1, body2, normDir12X, normDir12Y, 0.0f)
                bounceAtVerticalPlane(body1, body2, normDir12X, normDir12Y)
            }
            else -> throw UnexpectedHitAreaError(hit.area1)
        }
    }
}

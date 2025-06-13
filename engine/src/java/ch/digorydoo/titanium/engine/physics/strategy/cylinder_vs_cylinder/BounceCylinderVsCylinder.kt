package ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cylinder

import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionBounceStrategy

internal class BounceCylinderVsCylinder: CollisionBounceStrategy<FixedCylinderBody, FixedCylinderBody>() {
    override fun bounce(body1: FixedCylinderBody, body2: FixedCylinderBody, hit: HitResult) {
        when (hit.area1) {
            HitArea.TOP_FACE -> {
                val normDir12Z = hit.hitNormal12.z
                applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                val deltaSpeedZ = body1.speed.z - body2.speed.z // positive if body2 falls down on body1

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent body2 standing on body1 from constantly hopping due to gravity
                    body2.nextSpeed.z = body1.nextSpeed.z
                }

                bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            HitArea.BOTTOM_FACE -> {
                val normDir12Z = hit.hitNormal12.z
                applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                val deltaSpeedZ = body2.speed.z - body1.speed.z // positive if body1 falls down on body2

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent body1 standing on body2 from constantly hopping due to gravity
                    body1.nextSpeed.z = body2.nextSpeed.z
                }

                bounceAtHorizontalPlane(body1, body2, normDir12Z)
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

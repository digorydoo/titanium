package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cylinder

import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionBounceStrategy

internal class BounceSphereVsCylinder: CollisionBounceStrategy<FixedSphereBody, FixedCylinderBody>() {
    override fun bounce(body1: FixedSphereBody, body2: FixedCylinderBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP_FACE -> {
                val normDir12Z = hit.hitNormal12.z
                applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                val deltaSpeedZ = body2.speed.z - body1.speed.z // positive if sphere falls down on cylinder

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent sphere lying on cylinder from constantly hopping due to gravity
                    body1.nextSpeed.z = body2.nextSpeed.z
                }

                bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            HitArea.BOTTOM_FACE -> {
                val normDir12Z = hit.hitNormal12.z
                applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                // No remedy for cylinder standing on sphere hopping due to gravity, because this configuration should
                // be instable anyway.

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

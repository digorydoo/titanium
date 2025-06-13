package ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cuboid

import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionBounceStrategy

internal class BounceCylinderVsCuboid: CollisionBounceStrategy<FixedCylinderBody, FixedCuboidBody>() {
    override fun bounce(body1: FixedCylinderBody, body2: FixedCuboidBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP_FACE -> {
                val normDir12Z = -1.0f // points from body1 to body2
                applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                val deltaSpeedZ = body2.speed.z - body1.speed.z // positive if cylinder falls down on cuboid

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent cylinder standing on cuboid from constantly hopping due to gravity
                    body1.nextSpeed.z = body2.nextSpeed.z
                }

                bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            HitArea.BOTTOM_FACE -> {
                val normDir12Z = 1.0f // points from body1 to body2
                applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z) // z points from body1 to body2

                val deltaSpeedZ = body1.speed.z - body2.speed.z // positive if cuboid falls down on cylinder

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent cuboid standing on cylinder from constantly hopping due to gravity
                    body2.nextSpeed.z = body1.nextSpeed.z
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

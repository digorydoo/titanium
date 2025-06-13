package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cuboid

import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionBounceStrategy

internal class BounceSphereVsCuboid: CollisionBounceStrategy<FixedSphereBody, FixedCuboidBody>() {
    override fun bounce(body1: FixedSphereBody, body2: FixedCuboidBody, hit: HitResult) {
        val normal = hit.hitNormal12
        val normDir12X = normal.x
        val normDir12Y = normal.y
        val normDir12Z = normal.z

        applyFriction(body1, body2, normDir12X, normDir12Y, normDir12Z)

        if (hit.area2 == HitArea.TOP_FACE) {
            val deltaSpeedZ = body2.speed.z - body1.speed.z // positive if sphere falls down on cuboid

            if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                // Prevent sphere standing on cuboid from constantly hopping due to gravity
                body1.nextSpeed.z = body2.nextSpeed.z
            }
        }

        bounceAtPlane(body1, body2, normDir12X, normDir12Y, normDir12Z)
    }
}

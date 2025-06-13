package ch.digorydoo.titanium.engine.physics.strategy

import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody

internal abstract class CollisionSeparateStrategy<B1: RigidBody, B2: RigidBody> {
    abstract fun separate(body1: B1, body2: B2, hit: HitResult)
}

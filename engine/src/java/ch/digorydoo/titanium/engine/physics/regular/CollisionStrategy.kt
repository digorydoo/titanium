package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.physics.RigidBody

internal abstract class CollisionStrategy<B1: RigidBody, B2: RigidBody> {
    abstract fun checkNextPos(body1: B1, body2: B2, outHitPt: MutablePoint3f): Boolean
    abstract fun bounce(body1: B1, body2: B2)

    companion object {
        @JvmStatic
        protected val EPSILON = 0.000001f // small number to avoid division by almost zero
    }
}

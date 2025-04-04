package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.titanium.engine.physics.CollisionHelper
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody

internal abstract class CollisionStrategy<B1: RigidBody, B2: RigidBody> {
    protected val helper = CollisionHelper<B1, B2>()

    /**
     * Called by CollisionManager to check if two bodies collide at their nextPos. If they collide and true was passed
     * to canBounce, they are also separated, and the next speeds are computed.
     */
    fun checkAndBounceIfNeeded(body1: B1, body2: B2, canBounce: Boolean, outHit: MutableHitResult): Boolean {
        val didCollide: Boolean

        try {
            val p1 = body1.nextPos
            val p2 = body2.nextPos
            didCollide = check(body1, p1.x, p1.y, p1.z, body2, p2.x, p2.y, p2.z, outHit)
            if (didCollide && canBounce) bounce(body1, body2, outHit)
        } finally {
            done()
        }

        return didCollide
    }

    /**
     * Method that may be overridden by strategies to clear any internal structures after checks and bouncing are done.
     */
    protected open fun done() {}

    /**
     * Method that needs to be implemented by the strategy to check if the two bodies collide at the given position. If
     * outHit is not null, the function must update it when a collision is detected. When there is no collision and the
     * function returns with false, the values of outHit may or may not have been modified, and the caller should ignore
     * them.
     * @return true if the two collide; false if they don't
     */
    abstract fun check(
        body1: B1,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body2: B2,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        outHit: MutableHitResult?,
    ): Boolean

    /**
     * Method that needs to be implemented by the strategy to separate the two bodies after a collision and properly
     * assign new values of nextSpeed. The implementation should call separate().
     */
    protected abstract fun bounce(body1: B1, body2: B2, hit: HitResult)

    /**
     * FIXME Remove this function once strategies work well
     */
    protected fun verifySeparation(body1: B1, body2: B2, hit: HitResult) {
        val ck = check(
            body1,
            body1.nextPos.x,
            body1.nextPos.y,
            body1.nextPos.z,
            body2,
            body2.nextPos.x,
            body2.nextPos.y,
            body2.nextPos.z,
            null
        )
        if (ck) {
            throw Exception(
                arrayOf(
                    "bounce() failed to properly separate the bodies:",
                    "   body1=$body1",
                    "   body2=$body2",
                    "   hit=$hit",
                    "   body1.pos=${body1.pos}",
                    "   body2.pos=${body2.pos}",
                    "   body1.speed=${body1.speed}",
                    "   body2.speed=${body2.speed}",
                    "   body1.sBeforeC=${body1.speedBeforeCollisions}",
                    "   body2.sBeforeC=${body2.speedBeforeCollisions}",
                ).joinToString("\n")
            )
        }
    }

    companion object {
        @JvmStatic
        protected val HOPPING_PREVENTION_MAX_SPEED = 0.8f // a larger speed won't count as "hopping"
    }
}

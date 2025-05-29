package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.helper.CollisionHelper
import ch.digorydoo.titanium.engine.physics.helper.HitResult
import ch.digorydoo.titanium.engine.physics.helper.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody

internal abstract class CollisionStrategy<B1: RigidBody, B2: RigidBody> {
    protected val helper = CollisionHelper<B1, B2>()

    /**
     * Call this function before using any of the other functions of this class. If you do not call this function, the
     * bodies are implicitly treated as not being bricks in a BrickVolume.
     */
    internal abstract fun configure(
        body1IsBrick: Boolean,
        body2IsBrick: Boolean,
        bricks: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
    )

    /**
     * Function that needs to be implemented by the strategy to check if the two bodies collide at the given position.
     * If outHit is not null, the function must update it when a collision is detected. When there is no collision and
     * the function returns with false, the values of outHit may or may not have been modified, and the caller should
     * ignore them.
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

    fun check(body1: B1, centre1: Point3f, body2: B2, centre2: Point3f, outHit: MutableHitResult?) =
        check(body1, centre1.x, centre1.y, centre1.z, body2, centre2.x, centre2.y, centre2.z, outHit)

    /**
     * Function that needs to be implemented by the strategy to separate the two bodies after a collision.
     */
    abstract fun separate(body1: B1, body2: B2, hit: HitResult)

    /**
     * Function that needs to be implemented by the strategy to compute the new speed after a collision.
     */
    abstract fun computeNextSpeed(body1: B1, body2: B2, hit: HitResult)

    protected fun verifySeparation(body1: B1, body2: B2, hit: HitResult) {
        if (!BuildConfig.isProduction()) {
            val stillCollide = check(
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
            if (stillCollide) {
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
    }

    companion object {
        @JvmStatic
        protected val HOPPING_PREVENTION_MAX_SPEED = 0.8f // a larger speed won't count as "hopping"
    }
}

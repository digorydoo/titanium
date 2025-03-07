package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.RigidBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

internal abstract class CollisionStrategy<B1: RigidBody, B2: RigidBody, SeparationParams> {
    abstract fun checkNextPos(body1: B1, body2: B2, outHitPt: MutablePoint3f): Boolean
    abstract fun bounce(body1: B1, body2: B2)

    /**
     * This method should be called from the implementation of bounce() to separate the two bodies such that they no
     * longer collide. Each body must only be moved along the negative of the direction of its speed before the
     * collision, because any other direction might lead to new collisions with nearby bodies. Bodies of LARGE_MASS are
     * not to be moved. Also, bodies whose direction of speed can't have led to the collision are not to be moved.
     * This method computes the weights of how much of the distance for separation should be crossed by each body, and
     * then calls the abstract separate() function that needs to be implemented by the strategy.
     */
    protected fun separate(body1: B1, zOffset1: Float, body2: B2, zOffset2: Float, params: SeparationParams) {
        if (body1.mass >= LARGE_MASS && body2.mass >= LARGE_MASS) {
            Log.warn("Cannot separate $body1 from $body2, because both are too massive")
            return
        }

        // We use pos here, because the motion that led to a collision started off with these positions.
        val d12X = body2.pos.x - body1.pos.x
        val d12Y = body2.pos.y - body1.pos.y
        val d12Z = (body2.pos.z + zOffset2) - (body1.pos.z + zOffset1)
        val d12Len = sqrt(d12X * d12X + d12Y * d12Y + d12Z * d12Z)

        if (d12Len <= EPSILON) {
            Log.warn(
                "Cannot separate $body1 p=(${body1.pos.x}, ${body1.pos.y}, ${body1.pos.z + zOffset1})" +
                    " from $body2 p=(${body2.pos.x}, ${body2.pos.y}, ${body2.pos.z + zOffset2})" +
                    ", because they are too close"
            )
            return
        }

        // Compute the normalised direction vector from body1.pos to body2.pos
        val normDist12X = d12X / d12Len
        val normDist12Y = d12Y / d12Len
        val normDist12Z = d12Z / d12Len

        // The two bodies moved along their speeds before they collided.
        val s1 = body1.normDirOfSpeedBeforeCollisions
        val s2 = body2.normDirOfSpeedBeforeCollisions

        // Compute the amount of the speed of body1 that is in the direction of the collision.
        val speed1TowardsBody2 = when {
            body1.mass >= LARGE_MASS -> 0.0f // we must not move body1, because it's too massive
            s1 == null -> 0.0f // we can't move body1, because its speed direction is undefined
            else -> s1.x * normDist12X + s1.y * normDist12Y + s1.z * normDist12Z
        }

        // The same for body2.
        val speed2TowardsBody1 = when {
            body2.mass >= LARGE_MASS -> 0.0f
            s2 == null -> 0.0f
            else -> s2.x * (-normDist12X) + s2.y * (-normDist12Y) + s2.z * (-normDist12Z)
        }

        // Compute the weight how much to move body1 back along its original speed.
        // The weight for body2 will be implicitly (1 - weight1).
        val weight1: Float

        when {
            speed1TowardsBody2 > EPSILON -> when {
                speed2TowardsBody1 > EPSILON -> {
                    // Both bodies have a significant amount.
                    val sum = speed1TowardsBody2 + speed2TowardsBody1
                    weight1 = speed1TowardsBody2 / sum
                }
                else -> {
                    // Move body1 the whole way, don't move body2.
                    weight1 = 1.0f
                }
            }
            speed2TowardsBody1 > EPSILON -> {
                // Move body2 the whole way, don't move body1.
                weight1 = 0.0f
            }
            else -> {
                // None of the two bodies have a significant share of the motion towards each-other.
                // This may be because:
                //    - One body is not moving and thus can't be pushed back (because moving it in a direction other
                //      than its speed could lead to new collisions with nearby bodies), while the one that is moving is
                //      a LARGE_MASS, which is too massive to get pushed back by the other body; or
                //    - the two bodies were already colliding at their original position, e.g. they were spawned in the
                //      same spot.
                // We add a small force to each of them that might lead to a successful separation in later frames. If
                // one of them is a LARGE_MASS, that force won't have any effect on it.
                Log.warn("Cannot separate $body1 from $body2, adding a small force to each instead")
                val fx = normDist12X * SEPARATION_FORCE
                val fy = normDist12Y * SEPARATION_FORCE
                val fz = normDist12Z * SEPARATION_FORCE
                body1.addForce(-fx, -fy, -fz)
                body2.addForce(fx, fy, fz)
                return
            }
        }

        require(weight1 >= 0.0f)

        separate(
            body1 = body1,
            weight1 = weight1,
            normDir1X = -(s1?.x ?: 0.0f),
            normDir1Y = -(s1?.y ?: 0.0f),
            normDir1Z = -(s1?.z ?: 0.0f),
            body2 = body2,
            normDir2X = -(s2?.x ?: 0.0f),
            normDir2Y = -(s2?.y ?: 0.0f),
            normDir2Z = -(s2?.z ?: 0.0f),
            params = params,
        )
    }

    /**
     * Strategies must implement this function to move both bodies in order to separate them. body1 is to be moved along
     * the direction given by (normDir1X; normDir1Y; normDir1Z), while body2 is to be moved along the direction given by
     * (normDir2X; normDir2Y; normDir2Z). The weight1 defines how much of the required distance for separation body1
     * should be moved; weight2 is implicitly always (1 - weight1), i.e. body2 should be moved the remaining distance.
     * The absolute distance to be crossed for proper separation depends on the shape of the body and needs to be
     * computed by the strategy. The strategy is then to update nextPos of each body.
     */
    protected abstract fun separate(
        body1: B1,
        weight1: Float,
        normDir1X: Float,
        normDir1Y: Float,
        normDir1Z: Float,
        body2: B2,
        normDir2X: Float,
        normDir2Y: Float,
        normDir2Z: Float,
        params: SeparationParams,
    )

    companion object {
        private const val SEPARATION_FORCE = 1.0f
    }
}

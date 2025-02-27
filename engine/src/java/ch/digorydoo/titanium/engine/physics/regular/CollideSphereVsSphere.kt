package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.STATIC_MASS
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

internal class CollideSphereVsSphere: CollisionStrategy<FixedSphereBody, FixedSphereBody>() {
    private val tmp1 = MutablePoint3f()
    private val tmp2 = MutablePoint3f()

    override fun checkNextPos(body1: FixedSphereBody, body2: FixedSphereBody, outHitPt: MutablePoint3f) =
        check(
            cx1 = body1.nextPos.x,
            cy1 = body1.nextPos.y,
            cz1 = body1.nextPos.z + body1.zOffset,
            r1 = body1.radius,
            cx2 = body2.nextPos.x,
            cy2 = body2.nextPos.y,
            cz2 = body2.nextPos.z + body2.zOffset,
            r2 = body2.radius,
            outHitPt
        )

    private fun check(
        // Sphere 1
        cx1: Float,
        cy1: Float,
        cz1: Float,
        r1: Float,
        // Sphere 2
        cx2: Float,
        cy2: Float,
        cz2: Float,
        r2: Float,
        // out
        outHitPt: MutablePoint3f?,
    ): Boolean {
        val dx = cx2 - cx1
        val dy = cy2 - cy1
        val dz = cz2 - cz1

        val dsqr = (dx * dx) + (dy * dy) + (dz * dz)
        val rsum = r1 + r2
        if (dsqr > rsum * rsum) return false

        val d = sqrt(dsqr) // always >= 0

        if (outHitPt != null) {
            if (d <= EPSILON) {
                outHitPt.set(cx1, cy1, cz1)
            } else {
                outHitPt.x = cx1 + r1 * (dx / d)
                outHitPt.y = cy1 + r1 * (dy / d)
                outHitPt.z = cz1 + r1 * (dz / d)
            }
        }

        return true
    }

    /**
     * See docs/physics.txt
     */
    override fun bounce(body1: FixedSphereBody, body2: FixedSphereBody) {
        if (body1.mass < STATIC_MASS && body1.mass <= body2.mass) {
            separate(body1, body2) // updates body1.nextPos
        } else if (body2.mass < STATIC_MASS) {
            separate(body2, body1) // updates body2.nextPos
        } else {
            Log.warn("Cannot separate $body1 from $body2")
        }

        val p1 = tmp1.set(body1.nextPos.x, body1.nextPos.y, body1.nextPos.z + body1.zOffset)
        val p2 = tmp2.set(body2.nextPos.x, body2.nextPos.y, body2.nextPos.z + body2.zOffset)

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val m1 = if (body1.mass <= EPSILON) EPSILON else body1.mass
        val m2 = if (body2.mass <= EPSILON) EPSILON else body2.mass

        val pdiff = p2 - p1
        val n = pdiff / pdiff.length()

        val v1parallel = n * v1.dotProduct(n)
        val v2parallel = n * v2.dotProduct(n)

        val v1perpendicular = v1 - v1parallel
        val v2perpendicular = v2 - v2parallel

        val totalMass = m1 + m2
        val vdiff = v1parallel - v2parallel
        val e = body1.elasticity * body2.elasticity
        val p = v1parallel * m1 + v2parallel * m2

        v1.set(v1perpendicular + (p - vdiff * e * m2) / totalMass)
        v2.set(v2perpendicular + (p + vdiff * e * m1) / totalMass)
    }

    /**
     * Updates body1.nextPos such that it no longer collides with body2.
     */
    private fun separate(body1: FixedSphereBody, body2: FixedSphereBody) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos

        var dx = p2.x - p1.x
        var dy = p2.y - p1.y
        var dz = (p2.z + body2.zOffset) - (p1.z + body1.zOffset)
        var dist = sqrt((dx * dx) + (dy * dy) + (dz * dz))

        if (dist < EPSILON) {
            // The two bodies are too close. Try the original position before the collision!
            Log.warn("Bodies $body1 and $body2 are very close")
            val oc1 = body1.pos
            dx = p2.x - oc1.x
            dy = p2.y - oc1.y
            dz = (p2.z + body2.zOffset) - (oc1.z + body1.zOffset)
            dist = sqrt((dx * dx) + (dy * dy) + (dz * dz))

            if (dist < EPSILON) {
                // We still can't tell a proper direction!
                Log.warn("Bodies $body1 and $body2 are too close! Forcing a separation along the x-axis!")
                dx = 1.0f
                dy = 0.0f
                dz = 0.0f
                dist = 1.0f
            }
        }

        require(body1.mass < STATIC_MASS)
        val moveBy = body1.radius + body2.radius + 2 * EPSILON
        p1.x = p2.x - moveBy * dx / dist
        p1.y = p2.y - moveBy * dy / dist
        p1.z = (p2.z + body2.zOffset) - moveBy * dz / dist - body1.zOffset
    }
}

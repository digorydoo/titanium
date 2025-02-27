package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.FixedPlaneBody
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.STATIC_MASS
import kotlin.math.abs

internal class CollideSphereVsPlane: CollisionStrategy<FixedSphereBody, FixedPlaneBody>() {
    private val tmp1 = MutablePoint3f()

    /**
     * See docs/physics.txt
     */
    override fun checkNextPos(body1: FixedSphereBody, body2: FixedPlaneBody, outHitPt: MutablePoint3f): Boolean {
        tmp1.set(
            body1.nextPos.x - body2.nextPos.x,
            body1.nextPos.y - body2.nextPos.y,
            body1.nextPos.z + body1.zOffset - body2.nextPos.z,
        )
        val d = tmp1.dotProduct(body2.normal)
        if (abs(d) > body1.radius) return false
        outHitPt.set(
            body1.nextPos.x - d * body2.normal.x,
            body1.nextPos.y - d * body2.normal.y,
            body1.nextPos.z + body1.zOffset - d * body2.normal.z,
        )
        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedPlaneBody) {
        tmp1.set(
            body1.nextPos.x - body2.nextPos.x,
            body1.nextPos.y - body2.nextPos.y,
            body1.nextPos.z + body1.zOffset - body2.nextPos.z,
        )

        val d = tmp1.dotProduct(body2.normal)
        val n = body2.normal

        if (body1.mass < STATIC_MASS && body1.mass <= body2.mass) {
            // Move the sphere
            val q = if (d < 0.0f) -d - body1.radius else -d + body1.radius
            body1.nextPos.set(
                body1.nextPos.x + q * n.x,
                body1.nextPos.y + q * n.y,
                body1.nextPos.z + q * n.z,
            )
        } else if (body2.mass < STATIC_MASS) {
            // Move the plane
            val q = if (d < 0.0f) d + body1.radius else d - body1.radius
            body2.nextPos.set(
                body2.nextPos.x + q * n.x,
                body2.nextPos.y + q * n.y,
                body2.nextPos.z + q * n.z,
            )
        } else {
            Log.warn("Cannot separate $body1 from $body2")
            return
        }

        val m1 = if (body1.mass <= EPSILON) EPSILON else body1.mass
        val m2 = if (body2.mass <= EPSILON) EPSILON else body2.mass
        val e = body1.elasticity * body2.elasticity

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val v1parallel = n * v1.dotProduct(n)
        val v2parallel = n * v2.dotProduct(n)

        val v1perpendicular = v1 - v1parallel
        val v2perpendicular = v2 - v2parallel

        val totalMass = m1 + m2
        val vdiff = v1parallel - v2parallel
        val p = v1parallel * m1 + v2parallel * m2

        v1.set(v1perpendicular + (p - vdiff * e * m2) / totalMass)
        v2.set(v2perpendicular + (p + vdiff * e * m1) / totalMass)
    }
}
